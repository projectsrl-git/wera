# 2026-10-25 — DISEGNO DB: Centrale termica (wera2026), gestione documentale parallela

STATO: APPROVATO dall'operatore il 2026-10-25 (le 4 scelte qui sotto, tutte nell'opzione
raccomandata). DDL: `src/resources/sql/wera2026/001-centrale_termica-ddl.sql`, provato SOLO sul DB
locale del laptop. Sostituisce il paragrafo "Modello dati" di `2026-10-25-disegno-centrale-termica-2026.md`
(prefisso `ct26_` bocciato dall'operatore); il resto di quel disegno (funzioni, interfaccia, menu,
ondate) resta il riferimento.

## Scelte approvate
| Scelta | Decisione | Alternative scartate |
|---|---|---|
| Dove e come chiamarle | **schema PostgreSQL dedicato `centrale_termica`**, nomi senza prefisso | prefisso `centrale_termica_` in public; nomi di dominio in public |
| Versioni | **documento + versioni** in due tabelle | una tabella a catena |
| Scadenze | **scadenziario a se**: catalogo `tipi_scadenza` + `scadenze` per condominio | data scadenza sul documento |
| Tipi | **date/timestamp nativi, PK `id_<entita>` serial, `PjNDAO_base`** | convenzione AliMod (`id_modulo`, `nr_modulo`, date `char(10)`) |

## Vincoli che hanno guidato il disegno (dall'analisi del codice)
- **DB condiviso con /wera** (.75): solo oggetti nuovi; nessuna tabella, vista o query esistente cambia.
  Lo schema separato lo rende evidente e il rollback e `DROP SCHEMA centrale_termica CASCADE`.
- **`PARAMETRI` non va bene per i nuovi parametri**: `CODICE`/`DOMINIO` sono `char(3)`, `DESCRIZIONE`
  `varchar(50)`, e la pagina Parametri di /wera (`V_PARAMETRI`) elenca TUTTE le righe: un dominio
  nuovo comparirebbe (e sarebbe modificabile) anche in /wera. `PARAMETRI_AZIENDA` non ha valori (dice
  solo a quali aziende vale un parametro); `ATTRIBUTI_AZIENDA` e un chiave/valore senza default,
  vincoli ne audit.
- **Framework ALI BOW** (`NDAO_base`/`PjNDAO_base`): colonne = costanti `public static final` del DAO,
  nomi senza virgolette (minuscolo in Postgres va bene); i valori passano per `setString/setInt/
  setLong/setTimestamp/setBoolean/setFloat`. Non c'e `setDate`: per le colonne `date` il DAO dichiara
  `Timestamp` (pgjdbc 9.4 lo manda senza tipo e Postgres lo converte) e il Java converte gg/mm/aaaa.
  Nei DAO nuovi: interi `Integer` (non `Long`: `setNoStringAttribute` non converte stringhe in Long),
  tabelle qualificate (`CENTRALE_TERMICA.DOCUMENTI`) anche nelle query di `config.cfg`.
  `AliModDAO_base` non si usa: impone `id_modulo/nr_modulo/dt_modulo/stato/lista_allegati` e calcola
  `nr_modulo` con `MAX+1`, non sicuro con caricamenti contemporanei. Dopo l'insert l'id si recupera
  con `currval` della sequence o con la chiave univoca (`versioni.percorso_file`).
- **Upload di /wera**: tutti i file in un'unica cartella piatta `upload/` col nome originale, quindi lo
  stesso nome caricato per QUALSIASI condominio sovrascrive il file; `id_utente_ins` vale sempre 0.
  Sul DB locale 3 nomi ripetuti su 38 righe. La vista legacy marca le righe il cui file su disco e
  di un caricamento piu recente (`fl_file_sovrascritto`), che non vanno offerte al download.

## Convenzioni
- Nomi in italiano, minuscolo, tabelle al plurale (come `condomini`, `utenze`, `ripartizioni`).
- PK `id_<entita>` serial (come `utenti.id_utente`, `parametri.id_parametro`); riferimenti col nome
  dell'entita: `id_azienda` -> `aziende.id_azienda`, `id_condominio` -> `condomini.id_modulo`,
  `id_utente_*` -> `utenti.id_utente` (0 = script/sistema).
- Vincoli: `pk_`, `k1_`/`k2_` (univoci), `fk_`, `ck_`, `idx_` come negli script esistenti.
- Booleani `fl_` (come `fl_disattivo`, `fl_valida`). Audit `ts_ins` (default `current_timestamp`),
  `id_utente_ins`, `ts_mod`, `id_utente_mod`.
- `id_azienda` NULL = default per tutte le aziende; valorizzato = override o riga propria
  dell'azienda (indice univoco su `COALESCE(id_azienda, 0)`).
- Profili per CODICE (`ADM`, `INS`, `AMM`, `CON`), non per id: i serial di `PROFILI` possono
  differire fra DB locale e produzione, i codici no.
- **Nessuna FK verso tabelle di /wera** (solo fra tabelle nuove): una FK impedirebbe a /wera di
  cancellare un condominio con documenti e prende un lock sulla tabella referenziata.
- **Storico garantito dal DB**: trigger `fn_storico_immutabile` che rifiutano DELETE/TRUNCATE su
  `documenti` e `scadenze`, e UPDATE/DELETE/TRUNCATE su `versioni` ed `eventi`.

## Oggetti dello schema `centrale_termica`
### `sezioni` — le cartelle, configurabili
`id_sezione`, `id_azienda` (NULL = tutte), `codice` (stabile, es. `LIBRETTO`), `titolo`,
`sottotitolo`, `icona` (Font Awesome 4), `tipo` (`DOCUMENTI` | `FOTO` = nella scheda impianto |
`RISERVATA` = fuori dalle cartelle), `ordine`, `fl_scadenza_richiesta`, `fl_disattivo`, audit.
Iniziali: LIBRETTO, TERZO_RESPONSABILE, MANUTENZIONI, CONFORMITA, SICUREZZA, DOC_TECNICA (le 6 di
Andrea), FOTO, PAGAMENTI (riservata).

### `sezioni_profili` — chi vede e chi carica (come `MENU_PROFILI`)
`id_sezione_profilo`, `id_sezione`, `codice_profilo`, `fl_lettura`, `fl_scrittura`, audit.
Iniziali: ADM e INS lettura+scrittura ovunque; AMM sola lettura su tutto tranne PAGAMENTI; CON nulla.
Il perimetro dei condomini (AMM = i suoi) resta applicato dal Java.

### `documenti` — il documento logico
`id_documento`, `id_azienda`, `id_condominio`, `id_sezione`, `titolo`, `descrizione`,
`data_documento` (data del documento, non del caricamento), `id_versione_corrente`, `stato`
(`CORRENTE` | `ARCHIVIATO` | `ANNULLATO`), `ts_stato`/`id_utente_stato`/`nota_stato`, audit.
Mai cancellato: si archivia (storico) o si annulla (caricato per errore, visibile a chi gestisce).

### `versioni` — i file, immutabili
`id_versione`, `id_documento`, `numero_versione` (univoco per documento), `nome_file_originale`,
`percorso_file` (UNIVOCO, relativo a `FILE.CARTELLA`, es. `<azienda>/<condominio>/<anno>/<uuid>.pdf`),
`tipo_mime`, `dimensione_byte`, `impronta_sha256`, `nota`, `ts_ins`, `id_utente_ins`.
Nuova versione = nuova riga + `documenti.id_versione_corrente` aggiornato; FK composta
`(id_documento, id_versione_corrente)` garantisce che la versione corrente sia dello stesso documento.

### `tipi_scadenza` — catalogo degli adempimenti
`id_tipo_scadenza`, `id_azienda`, `codice`, `descrizione`, `id_sezione` (dove va il documento che la
soddisfa), `periodicita_mesi` (NULL = una tantum), `giorni_preavviso` (NULL = parametro),
`riferimento_normativo`, `ordine`, `fl_disattivo`, audit.
Iniziali (DA VERIFICARE con SIRBI, modificabili): manutenzione ordinaria 12 mesi; controllo di
efficienza / prova di combustione 24 (DPR 74/2013); analisi dell'acqua 12 (UNI 8065); rinnovo nomina
Terzo responsabile 12; CPI VVFF 60 (DPR 151/2011); verifica INAIL 60.

### `scadenze` — scadenziario per condominio
`id_scadenza`, `id_azienda`, `id_condominio`, `id_tipo_scadenza`, `descrizione` (distingue piu
scadenze dello stesso tipo), `data_scadenza`, `periodicita_mesi`/`giorni_preavviso` (override del
tipo), `stato` (`APERTA` | `CHIUSA` | `ANNULLATA`), `data_esecuzione`, `id_documento_chiusura`,
`ts_chiusura`, `id_utente_chiusura`, `id_scadenza_precedente` (catena dei rinnovi), `nota`, audit.
Chiusura con documento -> il Java crea la successiva se il tipo e periodico.

### `parametri` — chiave/valore con override per azienda
`id_parametro`, `id_azienda`, `chiave` (`[A-Z0-9_.]`), `valore`, `tipo_valore` (TESTO, NUMERO,
BOOLEANO S/N, ELENCO a virgole, EMAIL, TELEFONO, URL), `descrizione`, audit. Lettura con la funzione
`centrale_termica.valore_parametro(chiave, id_azienda)` (override, poi default).
Chiavi iniziali: `ACCESSO.PROFILI` (ADM,INS,AMM), `MENU.VOCE_WERA.PROFILI` (ADM,INS),
`ETICHETTA.TITOLO`, `ETICHETTA.GESTIONE`, `ASSISTENZA.TITOLO/UFFICIO/EMAIL/TELEFONO/ORARI/
LINK_SEGNALAZIONE`, `SCADENZE.GIORNI_PREAVVISO` (30), `ULTIMI_DOCUMENTI.NUMERO` (10),
`FILE.CARTELLA` (`centrale_termica/`, sotto `directory.external_files`: nessuna chiave nuova in
`config.cfg`), `FILE.ESTENSIONI`, `FILE.DIMENSIONE_MAX_MB` (25), `DOWNLOAD.ZIP_MAX_FILE` (50),
`DOCUMENTI_WERA.VISIBILI` (S), `DOCUMENTI_WERA.ETICHETTA` ("da WERA").
I contatti dell'assistenza sono VUOTI di proposito (niente dati nel repo): vuoto = voce nascosta.

### `categorie_wera` — vecchie categorie -> sezioni
`id_categoria_wera`, `id_azienda`, `codice_categoria` (`PARAMETRI` DOC), `id_sezione` (NULL = non
mostrata), audit. Default: 001-002 FOTO; 003 cartello e 006 documenti caldaia DOC_TECNICA;
004 TERZO_RESPONSABILE; 005 LIBRETTO; 007 CONFORMITA; 008-010 MANUTENZIONI; 013 e 016 SICUREZZA;
014 PAGAMENTI; 011, 012 (segnalazioni) e 015 (consigli) non mostrate.

### `eventi` — registro append-only
`id_evento` (bigserial), `ts_evento`, `tipo_evento` (CARICAMENTO, NUOVA_VERSIONE, MODIFICA,
ARCHIVIAZIONE, RIPRISTINO, ANNULLAMENTO, DOWNLOAD, DOWNLOAD_ZIP, SCADENZA_CREATA/CHIUSA/ANNULLATA,
CONFIGURAZIONE, ACCESSO_NEGATO), `id_utente`, `codice_profilo`, `id_azienda`, `id_condominio`,
`id_documento`, `id_versione`, `id_scadenza`, `id_file_wera` (download dei documenti di /wera),
`dettaglio`.

### Viste
- `v_documenti_wera`: documenti di /wera in sola lettura, nella sezione data da `categorie_wera`
  (override azienda prima del default), con `fl_file_sovrascritto` e `data_prossima_segnalazione`
  (dalla data prima segnalazione + intervallo in giorni, come il calendario di /wera). Date non
  valide o vuote -> NULL (sul DB locale sono tutte vuote).
- `v_documenti`: elenco unico `origine` WERA2026 (versione corrente, esclusi gli annullati) + WERA
  (file non sovrascritti), con le date anche come testo `AAAA/MM/GG` per i template.
- `v_scadenze` (aggiunta nell'ondata 1b): scadenze non annullate con giorni mancanti e semaforo
  SCADUTA / IN_SCADENZA / REGOLARE; preavviso = scadenza, tipo, parametro dell'azienda, 30.
- Nessuna filtra per perimetro: profilo e condomini li applica il Java.
- Parametri aggiunti nell'ondata 1b: `FILE.MAX_PER_CARICAMENTO` (20), `RIEPILOGO.SEZIONE_INTERVENTI`
  (MANUTENZIONI). Backend: `2026-10-25-centrale-termica-java-upload-download.md`.

## Prova sul DB locale (2026-10-25)
DB `wera` su 127.0.0.1, marcato `WERA-TEST`, PostgreSQL 9.4.26, UTF8. DDL lanciato due volte
(idempotente, exit 0). Prove in transazione annullata: documento con 2 versioni -> `v_documenti`
mostra la v2; override `SCADENZE.GIORNI_PREAVVISO` per azienda 1 = 15, azienda 2 = 30 (default);
rifiutati UPDATE su versioni, DELETE su documenti, TRUNCATE su eventi, versione corrente di un altro
documento, percorso file duplicato, parametro default duplicato, tipo evento sconosciuto.
Documenti di /wera: 38 righe, 36 smistate (1 con file sovrascritto, quindi 35 in `v_documenti`).

## Da fare nelle ondate successive (serve l'ok dell'operatore per Java / config.cfg / template)
DAO nel pacchetto nuovo, upload con nome univoco e SHA-256, download con controllo permessi e zip,
dataset `DS.CentraleTermica*` (senza WHERECONDITION dal browser), pagine, configurazione, voce di menu
in `included_menu.include`. DDL su .75 solo con un ok a parte, poco prima del deploy.

## Ondata 2 (pagina)
Pagina, menu e tema: `2026-10-25-centrale-termica-pagina.md`. Aggiunta la funzione `CentraleTermicaStato`
(archivia / ripristina / annulla) e i campi di sessione `CT_*` per la voce di menu.
