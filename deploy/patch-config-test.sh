#!/bin/bash
# ============================================================================
# WERA - Preparazione WAR di un'istanza IN PARALLELO alla produzione (wera)
# ----------------------------------------------------------------------------
# Prende in input il WAR prodotto da `mvn clean package` (target/wera.war),
# lo esplode, applica gli override del profilo e riconfeziona <contesto>.war.
#
# Profili:
#   test  istanza isolata su una COPIA del DB: Quartz spento, cartelle di I/O e
#         AnlConfig separati, SMTP su mailcatcher, datasource jdbc/<contesto>,
#         log separati.
#   prod  nuova interfaccia sui dati veri accanto a wera: stesso DB e cartelle
#         (modifiche e upload reali); Quartz SPENTO (job solo su wera), NESSUNA
#         mail reale (SMTP sul mailcatcher locale) e log separati.
#
# NON modifica il repo: agisce solo sull'artefatto di deploy.
#
# Uso:  ./patch-config-test.sh [war] [contesto] [test|prod]
#       ./patch-config-test.sh target/wera.war wera2026 prod
# ============================================================================

set -euo pipefail

WAR_IN="${1:-target/wera.war}"
CTX="${2:-wera-test}"
PROFILO="${3:-test}"

[ "$CTX" != "wera" ] || { echo "ERRORE: 'wera' e' il contesto di produzione"; exit 1; }
case "$PROFILO" in test|prod) ;; *) echo "ERRORE: profilo '$PROFILO' non valido (test|prod)"; exit 1;; esac
[ -f "$WAR_IN" ] || { echo "ERRORE: WAR non trovato: $WAR_IN"; exit 1; }

BUILD="/tmp/${CTX}-${PROFILO}-build"
OUT_DIR="/tmp/${PROFILO}"
EXT_DIR="/usr/share/webapps/${CTX}/"
LOG_DIR="/usr/share/logs/${CTX}/"
DS_NAME="jdbc/${CTX//-/_}"
MARKER="#${PROFILO^^}-OFF#"

echo ">> [$CTX / $PROFILO] Esplodo $WAR_IN"
rm -rf "$BUILD"; mkdir -p "$BUILD"
unzip -q "$WAR_IN" -d "$BUILD"

CFG="$BUILD/WEB-INF/config/config.cfg"
[ -f "$CFG" ] || { echo "ERRORE: config.cfg non trovato nel WAR"; exit 1; }

# --- Il file e' ISO-8859-1: sed opera byte-wise, l'encoding e' preservato ----

# Sostituisce chiave=valore e fallisce se la chiave non esiste piu'.
# Il valore si ferma prima di un eventuale CR: il file e' a fine-riga CRLF e
# le righe sostituite devono restare CRLF come le altre.
CR=$'\r'
set_key() {
    grep -q -E "^${1//./\\.}=" "$CFG" || { echo "ERRORE: chiave $1 non trovata in config.cfg"; exit 2; }
    sed -i -E "s|^${1//./\\.}=[^${CR}]*|$1=$2|" "$CFG"
}

echo ">> Kill switch Quartz (tasklist vuota) + job commentati"
# SchedulerServletApplication.init(): se tasklist == "" fa return PRIMA di
# makeScheduler() -> nessun thread Quartz viene mai creato.
set_key Quartz-scheduler.tasklist ""
sed -i -E "s|^(Quartz-scheduler\.job\..*)$|${MARKER} \1|" "$CFG"

echo ">> Log applicativi separati da quelli di produzione"
set_key Logger.CrashDir "${LOG_DIR}crash/wera_crash"
set_key Logger.DebugFile "${LOG_DIR}wera_debug"
set_key Logger.ApplicationLog "${LOG_DIR}wera_application.log"

echo ">> Dirotto SMTP su mailcatcher locale (nessuna mail reale)"
# mail.SMTPHost.user DEVE restare non vuoto: SendSMTPMail ha un invariant.
set_key mail.SMTPHost 127.0.0.1
set_key mail.SMTPHost.port 1025
set_key mail.SMTPHost.user test
set_key mail.SMTPHost.password test
set_key mail.SMTPHost.starttls.enable false
set_key mail.from noreply@test.invalid
set_key mail.from.service noreply@test.invalid

if [ "$PROFILO" = "test" ]; then
    echo ">> Isolo le directory di I/O condivise"
    set_key directory.external_files "${EXT_DIR}"
    set_key cartella.scarico.ACS "${EXT_DIR}ACS26V3/"
    set_key cartella.scarico.ACS.webapp "C:/ACS26V3-TEST/"
    set_key xmlconfig.ACS "${EXT_DIR}ANLCONFIG/AnlConfig.xml"

    echo ">> Datasource JNDI separato ($DS_NAME)"
    set_key DB.ConnectionURL "java:comp/env/${DS_NAME}"
    set_key DB.ConnectionURL.generic "java:comp/env/${DS_NAME}"
    VIETATI="/usr/share/(webapps|logs)/wera/"
else
    VIETATI="/usr/share/logs/wera/"
fi

# ---------------------------------------------------------------------------
echo
echo "========== VERIFICA  contesto=$CTX  profilo=$PROFILO =========="
grep -n -E "^Quartz-scheduler\.tasklist=|^#(TEST|PROD)-OFF#" "$CFG" || true
grep -n -E "^directory\.external_files=|^cartella\.scarico|^xmlconfig\.ACS=" "$CFG"
grep -n -E "^mail\.SMTPHost=|^mail\.SMTPHost\.port=|^mail\.from" "$CFG"
grep -n -E "^DB\.ConnectionURL" "$CFG"
grep -n -E "^Logger\.(CrashDir|DebugFile|ApplicationLog)=" "$CFG"
echo "==============================================================="

echo
echo ">> Controllo: Quartz spento, SMTP su 127.0.0.1, nessun riferimento attivo a $VIETATI"
if grep -n -E "^Quartz-scheduler\.tasklist=[[:space:]]*[^[:space:]]" "$CFG" \
   || grep -n -E "^mail\.SMTPHost=" "$CFG" | grep -v -E ":mail\.SMTPHost=127\.0\.0\.1${CR}?\$" \
   || grep -n -E "$VIETATI" "$CFG" | grep -v "^[0-9]*:#"; then
    echo "!! ATTENZIONE: riferimenti attivi che dovevano essere isolati (sopra)"
    exit 3
else
    echo "   OK"
fi

echo
echo ">> Creo le directory"
mkdir -p "${LOG_DIR}crash"   # senza crash gli stack trace andrebbero persi
if [ "$PROFILO" = "test" ]; then
    mkdir -p "${EXT_DIR}"{import,upload,output,ACS26V3,ANLCONFIG}
fi

echo ">> Riconfeziono ${OUT_DIR}/${CTX}.war"
mkdir -p "$OUT_DIR"
rm -f "${OUT_DIR}/${CTX}.war"
( cd "$BUILD" && zip -qr "${OUT_DIR}/${CTX}.war" . )
rm -rf "$BUILD"

echo
echo "FATTO -> ${OUT_DIR}/${CTX}.war   [contesto /${CTX}, profilo ${PROFILO}]"
echo "Deploy:  cp ${OUT_DIR}/${CTX}.war \$CATALINA_BASE/webapps/"
if [ "$PROFILO" = "test" ]; then
    echo "Serve:   conf/Catalina/localhost/${CTX}.xml con ${DS_NAME} su una COPIA del DB"
else
    echo "Nessun descrittore conf/Catalina/localhost/${CTX}.xml: usa jdbc/wera (DB di produzione)"
fi
echo "URL:     http://<host>:<porta>/${CTX}/astro"
