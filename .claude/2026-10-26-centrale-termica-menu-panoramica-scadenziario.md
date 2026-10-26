# 2026-10-26 — Centrale termica wera2026: menu, Panoramica (mockup di Andrea), scadenziario e avvisi (ondata 4)

## Perche'
Collaudo PROD dell'operatore (26/10 mattina): "la Centrale termica sembra identica a prima". Dagli access
log di .106: dopo il login l'operatore ha aperto `RicercaFileManager` / `FileManager`, cioe' la VECCHIA
voce "Centrali termiche" del menu del DB (ancora visibile al superuser); la pagina nuova non e' mai stata
aperta (la voce nuova c'era: 3 chiamate `OPERAZIONE=accesso`). Richiesta dell'operatore:
1. menu "Centrale termica" con voce principale = mockup di Andrea e sottovoci dal mockup `wera_demo`:
   Scadenze e documenti, Calendario, Alerting e storico invii;
2. Panoramica (prima pagina) come il mockup di Andrea; gestione scadenze/documenti come `wera_demo`;
3. poi, in una sessione a parte, caricamento dei documenti reali di Andrea in PROD.
Scelte dell'operatore (26/10): avvisi = "motore pronto, invio spento" (SMTP del profilo, in PROD il
mailcatcher); vecchia voce nascosta a chi ha la nuova.

## File toccati
- DDL NUOVO `src/resources/sql/wera2026/002-centrale_termica-scadenziario-avvisi.sql` (solo schema
  centrale_termica, idempotente, 9.4): `tipi_scadenza.sigla/ente` (+ valori per i 6 tipi), tipo nuovo
  `VERIFICA_TERRA` (TERRA, DPR 462/01, 24 mesi), vista `v_scadenze` con sigla, ente, ultima esecuzione e
  documento precedente (colonne in coda), tabella `invii_avvisi` (append-only, un solo INVIATO per
  scadenza e soglia), parametri `ALERT.ATTIVO/GIORNI/SCADUTE/AMMINISTRATORE/CC/OGGETTO/LINK/TOKEN`;
  `SCADENZE.GIORNI_PREAVVISO` 30 -> 90 e `MENU.VOCE_WERA.PROFILI` 'ADM,INS' -> '' SOLO se mai modificati.
- Java (pacchetto centraletermica): NUOVI `FunctionCentraleTermicaScadenziario` (3 nomi:
  CentraleTermicaScadenze / Calendario / Avvisi, template unico), `FunctionCentraleTermicaAvvisiInvio`
  (motore: pulsante del superuser o cron con TOKEN solo da localhost, socket reale), `LetturaParametri`;
  `CentraleTermicaDati` operazioni `scadenziario` e `avvisi` (+ `configura` in `accesso`);
  `CentraleTermicaScadenza` chiudi con `DATA_PROSSIMA`; sessione `CT_CONFIGURA`; `CT_SCHEDA` per la
  sottovoce attiva.
- config.cfg: 3 dataset nuovi (`DSCentraleTermicaScadenziario`, `...Invii`, `...AvvisiDaInviare`);
  functionList: 4 funzioni.
- Menu (`included_menu.include`, ASCII): la voce diventa un menu con sottovoci Panoramica, Scadenze e
  documenti, Calendario, Alerting e storico invii, Configurazione (solo chi configura).
- Template: `centraletermica.html` (Panoramica rifatta sul mockup di Andrea), NUOVO
  `centraletermicascadenziario.html`, `centraletermicaconfigurazione.html` (riquadro "Archivio WERA" con le
  vecchie funzioni del file manager).
- JS: `wera-centrale-termica.js` (testata, riquadri, cartelle a righe, ricerca, avviso giallo "Scadenza
  non registrata", assistenza), NUOVO `wera-centrale-termica-scadenziario.js`. Tutto ASCII.
- Tema: sezione 30c (`ctp-*` Panoramica, `cts-*` scadenziario); cache-buster `?20261026` (tema e JS
  della pagina), `?20261026b` JS scadenziario.

## Avvisi (come funziona)
Per ogni scadenza aperta: soglia = la piu' piccola fra `ALERT.GIORNI` >= ai giorni mancanti (oppure -1
se superata, con `ALERT.SCADUTE=S`); se per scadenza+soglia non c'e' gia' un INVIATO/SALTATO, mail
all'amministratore del condominio (`ALERT.AMMINISTRATORE`) con `ALERT.CC` in copia, via
`SendSMTPMail.sendMail()` sincrono, e riga in `invii_avvisi` (INVIATO / ERRORE / SALTATO). Con l'SMTP del
profilo PROD (mailcatcher) NESSUNA mail esce: la pagina lo dice. Accendere l'invio = cambiare l'SMTP del
profilo prod (decisione dell'operatore). Avvio giornaliero: cron su .106 (vedi piano di produzione).

## QA (laptop, /wera2026 profilo test, DB locale WERA-TEST)
- [x] DDL 002 due volte (exit 0), vista con nuove colonne, parametri e scelte applicati.
- [x] Build, WAR test, deploy locale.
- [x] Menu (ADM): "Centrale termica" con 5 sottovoci, sottovoce attiva per pagina, vecchia voce
  "Centrali termiche" nascosta; percorsi "Home / Centrale termica / ...".
- [x] Panoramica a 1280 px (posizioni misurate): titolo + condominio a sinistra, scelta condominio in alto
  a destra, 3 riquadri in riga, documenti (6 cartelle a righe) a sinistra, scadenze (2 avvisi gialli) e
  assistenza a destra ("Configura i contatti" per il superuser); telefono 375 px senza sforamenti.
- [x] Scadenze e documenti: filtro "Tutti i condomini", contatori, righe con sigla/ente/periodicita'/ultimo;
  "Carica documento" CPI (proposta +5 anni) e INAIL al primo documento (esecuzione chiusa col PDF +
  successiva 15/06/2030, riga con "Ultimo" e PDF).
- [x] Calendario: oggi evidenziato, ACQUA a dicembre ambra, TERRA a settembre rossa.
- [x] Alerting: configurazione letta, avviso "server di posta di prova", "Controlla ora": 6 esaminate, 2
  inviati (60 gg e scaduto), mail nel mailcatcher locale; secondo controllo 0 (nessun doppione); cron con
  token OK, token errato 403, senza credenziali 403; AMM: controllo 403, scadenziario in sola lettura,
  token mai esposto.
- [x] Password di test ripristinate identiche.
- [ ] INS non provato (assente nel DB locale). [ ] Vista desktop nel browser solo per misure (pannello
  stretto): controllo visivo finale dell'operatore in PROD.

## Rischi / punti aperti
- In PROD serve prima il DDL 002 su .75 (ok a parte), poi Jenkins: senza DDL lo scadenziario va in errore.
- Cron degli avvisi su .106 da installare (comando nel piano) dopo il deploy.
- Restano dati di prova sul DB del laptop (non cancellabili, DB sacrificabile).
