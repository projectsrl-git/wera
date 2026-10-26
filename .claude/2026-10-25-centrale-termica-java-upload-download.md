# 2026-10-25 — Centrale termica wera2026: DAO, upload, download e dati JSON (ondata 1b)

## Obiettivo
Backend della nuova Centrale termica sullo schema `centrale_termica` (ondata 1a): caricamento con
versioni e nome file univoco, download singolo / "Apri" / zip con controllo di perimetro e permessi,
dati JSON per la pagina. Autorizzato dall'operatore il 2026-10-25: "pacchetto Java nuovo e dataset
nuovi in config.cfg". Nessuna pagina ancora (ondata 2).

## File toccati
- `src/main/java/net/projectsrl/wera/centraletermica/` — NUOVO pacchetto:
  - `db/DocumentiDAO`, `db/VersioniDAO` (update vietato), `db/EventiDAO` (update vietato): `PjNDAO_base`,
    tabelle qualificate `CENTRALE_TERMICA.*`.
  - `core/CentraleTermicaCostanti` (chiavi parametri, stati, eventi, nomi dataset: fuori dai DAO perche'
    il framework tratta come colonna ogni `public static final String` di un DAO).
  - `core/ContestoUtente` (profilo, azienda, perimetro da `WHERECONDITION_*` di sessione, permessi per
    sezione, parametri con override), `core/LetturaDataset`, `core/ArchivioFile`.
  - `core/FunctionCentraleTermica_base` + `FunctionCentraleTermicaUpload`, `...Download`, `...Dati`.
- `src/main/webapp/WEB-INF/config/config.cfg` — AGGIUNTO in fondo (prima di `dummy=dummy`) il blocco
  "CENTRALE TERMICA wera2026": 12 dataset `DS.DSCentraleTermica*`. Nessuna riga esistente cambiata
  (edit byte-level, resta ISO-8859-1 CRLF).
- `src/main/webapp/WEB-INF/config/functionList.txt` — AGGIUNTE 3 righe (`CentraleTermicaDati`,
  `CentraleTermicaUpload`, `CentraleTermicaDownload`): servono a rendere raggiungibili le classi nuove,
  previste dal disegno (punto "Java: pacchetto nuovo + righe aggiunte a functionList.txt").
- `src/resources/sql/wera2026/001-centrale_termica-ddl.sql` — vista `v_scadenze` (semaforo con
  preavviso scadenza > tipo > parametro dell'azienda) e 2 parametri: `FILE.MAX_PER_CARICAMENTO` (20),
  `RIEPILOGO.SEZIONE_INTERVENTI` (MANUTENZIONI).

## Interfaccia (per l'ondata 2)
- `astro?FUNCTIONID=CentraleTermicaDati&OPERAZIONE=...` (GET, JSON): `condomini`, `parametri`,
  `riepilogo|sezioni|scadenze` (+`ID_CONDOMINIO`), `documenti` (+`ID_CONDOMINIO`, `ID_SEZIONE`,
  `STATO`=CORRENTE|ARCHIVIATO|TUTTI, `ULTIMI=S`), `versioni` (+`ID_DOCUMENTO`).
- `astro?FUNCTIONID=CentraleTermicaUpload` (POST multipart; **FUNCTIONID nella URL**, il servlet non
  legge il corpo multipart): `ID_CONDOMINIO`, `ID_SEZIONE` o `ID_DOCUMENTO` (nuova versione, 1 file),
  `TITOLO`, `DESCRIZIONE`, `DATA_DOCUMENTO` (gg/mm/aaaa o aaaa-mm-gg), `NOTA`, file multipli.
  Risposta: esito per file (OK / PARZIALE / KO).
- `astro?FUNCTIONID=CentraleTermicaDownload` (GET): `ID_VERSIONE` | `ID_DOCUMENTO` | `ID_FILE_WERA` |
  `SELEZIONE=D12,V7,W45` (zip, tutto o niente) ; `APRI=S` = inline per pdf e immagini.
- Errori JSON `{"esito":"KO","messaggio":...}` con 400/403/404/409/410. Sessione scaduta: il framework
  risponde con la pagina di login (HTML), la pagina dovra' riconoscerla.

## Sicurezza (lato server)
- Profilo abilitato da `ACCESSO.PROFILI` (CON escluso), perimetro condomini = quello del login
  (AMM: i suoi; ADM: tutti; INS: le sue aziende), permessi da `sezioni_profili` (AMM sola lettura,
  Stato pagamenti solo ADM/INS), annullati solo a chi scrive. Ogni rifiuto -> `ACCESSO_NEGATO` in eventi.
- Dal browser solo interi validati e scelte da elenchi chiusi; i filtri SQL li compone il server
  (i dataset del framework sostituiscono `#PARAM#` come testo).
- File nuovi in `directory.external_files` + `FILE.CARTELLA` (default `centrale_termica/`, `upload/` e
  `..` rifiutati) come `<azienda>/<condominio>/<anno>/<uuid>.<ext>`; SHA-256; estensioni e dimensione da
  parametri; percorsi risolti in modo canonico (niente path traversal, anche sui nomi dei file di /wera).
- Documenti di /wera con file sovrascritto: 410, mai serviti (su disco c'e' un altro documento).

## Cosa NON e stato toccato (conferma)
Classi Java esistenti, `pom.xml`, template e direttive FreeMarker, menu, righe esistenti di `config.cfg`
e `functionList.txt`, tabelle di `public`. Nessuna scrittura su .75 / .67.

## QA (laptop, /wera2026 profilo test, DB locale WERA-TEST)
- [x] `mvn clean package` (JDK 25, release 8) ok; WAR test con `deploy/make_wera2026.bat`, deploy a caldo.
- [x] DDL rilanciato (idempotente) sul DB locale marcato.
- [x] Collaudo curl con utenti locali ADM (1), AMM (30), CON (9), password di test temporanea
  (hash ripristinati identici a fine prova):
  - ADM: parametri, condomini, sezioni, riepilogo, documenti (nuovi + /wera), upload (doc + nuova
    versione v2), download corrente = v2 e v1 byte per byte, "Apri" inline, zip (2 file), documento
    /wera scaricato, /wera sovrascritto 410 (anche nello zip), id inesistente 404; rifiutati
    estensione `.exe`, data 31/02, versione su documento di un altro condominio (403).
  - AMM: 7 condomini (solo i suoi), legge e scarica i documenti, niente sezione Stato pagamenti,
    upload 403, condominio altrui 403.
  - CON: 403 su dati e download.
  - nomi file e titoli UTF-8 (a, e accentate, grado) salvati e restituiti (`filename*`).
  - eventi registrati per caricamenti, versioni, download, zip, accessi negati.
- [ ] INS: nessun installatore nel DB locale, non provato (stessi permessi di ADM, perimetro per azienda).
- [ ] rollback del file su errore DB: logica presente, non simulato.
- Nel DB locale restano 3 documenti di prova del condominio 34 (non cancellabili per i trigger: DB
  sacrificabile) e i file in `D:\usr\share\webapps\wera2026\centrale_termica\`.

## Rischi / punti aperti
- **Hash password del framework non standard** (preesistente, NON toccato): `WebAppUtils.encryptSHA1`
  usa `Integer.toHexString` per byte e perde gli zeri iniziali (hash di 38-39 caratteri). Chi prepara
  utenti di test deve calcolare l'hash allo stesso modo.
- `v_documenti` ricalcola la finestra su tutta `elenco_file_importati_file_manager` a ogni query:
  ok coi volumi attuali, da rivedere se i file di /wera crescono molto.
- Contratti firmati di Andrea (Dropbox dell'operatore): da caricare in PROD solo DOPO DDL su .75 e
  deploy Jenkins, con l'ok dell'operatore; mai nel repo.
- Apache di frontend (.50): verificare un eventuale `LimitRequestBody` per upload fino a 20 x 25 MB.
