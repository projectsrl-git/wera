#!/bin/bash
# ============================================================================
# WERA - Preparazione WAR per istanza di TEST GUI in parallelo alla produzione
# ----------------------------------------------------------------------------
# Prende in input il WAR prodotto da `mvn clean package` (target/wera.war),
# lo esplode, neutralizza schedulazioni e I/O condivisi, e riconfeziona
# wera-test.war (context /wera-test).
#
# NON modifica il repo: agisce solo sull'artefatto di deploy.
#
# Uso:  ./patch-config-test.sh /percorso/target/wera.war
# ============================================================================

set -euo pipefail

WAR_IN="${1:-target/wera.war}"
CTX="wera-test"
BUILD="/tmp/wera-test-build"
EXT_DIR="/usr/share/webapps/${CTX}/"
LOG_DIR="/usr/share/logs/${CTX}/"

[ -f "$WAR_IN" ] || { echo "ERRORE: WAR non trovato: $WAR_IN"; exit 1; }

echo ">> Esplodo $WAR_IN"
rm -rf "$BUILD"; mkdir -p "$BUILD"
unzip -q "$WAR_IN" -d "$BUILD"

CFG="$BUILD/WEB-INF/config/config.cfg"
[ -f "$CFG" ] || { echo "ERRORE: config.cfg non trovato nel WAR"; exit 1; }

cp "$CFG" "$CFG.orig"

# --- Il file e' ISO-8859-1: sed opera byte-wise, l'encoding e' preservato ----

echo ">> [1/6] Kill switch Quartz (tasklist vuota)"
# SchedulerServletApplication.init(): se tasklist == "" fa return PRIMA di
# makeScheduler() -> nessun thread Quartz viene mai creato.
sed -i -E 's|^Quartz-scheduler\.tasklist=.*|Quartz-scheduler.tasklist=|' "$CFG"

echo ">> [2/6] Commento le singole definizioni job (difesa in profondita')"
sed -i -E 's|^(Quartz-scheduler\.job\..*)$|#TEST-OFF# \1|' "$CFG"

echo ">> [3/6] Isolo le directory di I/O condivise"
sed -i -E "s|^directory\.external_files=.*|directory.external_files=${EXT_DIR}|" "$CFG"
sed -i -E "s|^cartella\.scarico\.ACS=.*|cartella.scarico.ACS=${EXT_DIR}ACS26V3/|" "$CFG"
sed -i -E "s|^cartella\.scarico\.ACS\.webapp=.*|cartella.scarico.ACS.webapp=C:/ACS26V3-TEST/|" "$CFG"
sed -i -E "s|^xmlconfig\.ACS=.*|xmlconfig.ACS=${EXT_DIR}ANLCONFIG/AnlConfig.xml|" "$CFG"

echo ">> [4/6] Dirotto SMTP su mailcatcher locale"
# mail.SMTPHost.user DEVE restare non vuoto: SendSMTPMail ha un invariant.
sed -i -E 's|^mail\.SMTPHost=.*|mail.SMTPHost=127.0.0.1|' "$CFG"
sed -i -E 's|^mail\.SMTPHost\.port=.*|mail.SMTPHost.port=1025|' "$CFG"
sed -i -E 's|^mail\.SMTPHost\.user=.*|mail.SMTPHost.user=test|' "$CFG"
sed -i -E 's|^mail\.SMTPHost\.password=.*|mail.SMTPHost.password=test|' "$CFG"
sed -i -E 's|^mail\.SMTPHost\.starttls\.enable=.*|mail.SMTPHost.starttls.enable=false|' "$CFG"
sed -i -E 's|^mail\.from=.*|mail.from=noreply@test.invalid|' "$CFG"
sed -i -E 's|^mail\.from\.service=.*|mail.from.service=noreply@test.invalid|' "$CFG"

echo ">> [5/6] Datasource JNDI separato"
sed -i -E 's|^DB\.ConnectionURL=.*|DB.ConnectionURL=java:comp/env/jdbc/wera_test|' "$CFG"
sed -i -E 's|^DB\.ConnectionURL\.generic=.*|DB.ConnectionURL.generic=java:comp/env/jdbc/wera_test|' "$CFG"

echo ">> [6/6] Log applicativi separati da quelli di produzione"
sed -i -E "s|^Logger\.CrashDir=.*|Logger.CrashDir=${LOG_DIR}crash/wera_crash|" "$CFG"
sed -i -E "s|^Logger\.DebugFile=.*|Logger.DebugFile=${LOG_DIR}wera_debug|" "$CFG"
sed -i -E "s|^Logger\.ApplicationLog=.*|Logger.ApplicationLog=${LOG_DIR}wera_application.log|" "$CFG"

# ---------------------------------------------------------------------------
echo
echo "===================== VERIFICA ====================="
echo "--- Quartz ---"
grep -n -E "^Quartz-scheduler\.tasklist=|^#TEST-OFF#" "$CFG" || true
echo "--- Percorsi ---"
grep -n -E "^directory\.external_files=|^cartella\.scarico|^xmlconfig\.ACS=" "$CFG"
echo "--- Mail ---"
grep -n -E "^mail\.SMTPHost=|^mail\.SMTPHost\.port=|^mail\.from" "$CFG"
echo "--- DB ---"
grep -n -E "^DB\.ConnectionURL" "$CFG"
echo "--- Log ---"
grep -n -E "^Logger\.(CrashDir|DebugFile|ApplicationLog)=" "$CFG"
echo "===================================================="

# Nessun riferimento residuo alla produzione
echo
echo ">> Controllo residui verso /usr/share/webapps/wera/ e /usr/share/logs/wera/ :"
if grep -n -E "/usr/share/(webapps|logs)/wera/" "$CFG" | grep -v "^[0-9]*:#"; then
    echo "!! ATTENZIONE: trovati riferimenti attivi ai path di produzione (sopra)"
    exit 1
else
    echo "   OK - nessun riferimento attivo"
fi

rm -f "$CFG.orig"

echo
echo ">> Creo le directory isolate"
mkdir -p "${EXT_DIR}"{import,upload,output,ACS26V3,ANLCONFIG}
# senza la sottocartella crash gli stack trace andrebbero persi
mkdir -p "${LOG_DIR}crash"

echo ">> Riconfeziono ${CTX}.war"
( cd "$BUILD" && zip -qr "/tmp/${CTX}.war" . )

echo
echo "FATTO -> /tmp/${CTX}.war"
echo "Deploy:  cp /tmp/${CTX}.war \$CATALINA_BASE/webapps/"
echo "URL:     http://<host>:<porta>/${CTX}/astro"
