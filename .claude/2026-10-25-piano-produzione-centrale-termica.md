# 2026-10-25 — PIANO: Centrale termica in produzione (wera2026 su .106, DB .75)

STATO: PIANO, NIENTE ESEGUITO. Ogni passo che scrive (DDL su .75, push, Jenkins con DEPLOY=true,
cartella sulla share) richiede l'ok esplicito dell'operatore al momento. Le credenziali non stanno qui:
le fornisce l'operatore.

## Cosa va in produzione
- Commit da pushare su `restyle/ui-2026-r2` (oggi 6 avanti a `origin`): `d29ed0e`, `72b8aaa` (note),
  `474999e` (schema), `272782a` (backend), `6d68b8d` (pagina e menu), `9bd5168` (scadenze e
  configurazione), piu' il commit di questo piano.
- DB .75: solo lo schema NUOVO `centrale_termica` (script `src/resources/sql/wera2026/001-centrale_termica-ddl.sql`,
  transazione unica, idempotente). Nessun oggetto di `public` cambia; le viste LEGGONO
  `public.elenco_file_importati_file_manager` e `public.parametri`.
- WAR wera2026 profilo prod: classi nuove, 12+10 dataset aggiunti in fondo a config.cfg, 8 funzioni in
  functionList, template/JS nuovi, tema `?20261025c`, menu con la voce Centrale termica.
- File nuovi su disco: `directory.external_files` del profilo prod = `/usr/share/webapps/wera/`
  (share di .81 condivisa con /wera) + parametro `FILE.CARTELLA` = `centrale_termica/`. /wera non usa
  quella sottocartella.

**Ordine obbligato: DDL prima del deploy.** Con il WAR nuovo e lo schema assente la pagina va in errore
(la Home e il resto di wera2026 no: la voce di menu resterebbe nascosta). Lo schema senza il WAR nuovo
invece e' innocuo (nessuno lo usa).

## Fase 0 — Decisioni e finestra (operatore)
- [ ] Finestra: dalle 06:00 alle 23:00 (FortiGate: Jenkins scarica da GitHub/Maven solo in quella
  fascia). Consigliata una fascia di basso uso: il deploy a caldo di wera2026 dura pochi secondi, /wera
  (.70) non si ferma.
- [ ] Chi esegue: DDL e comandi su .106 l'operatore oppure Claude con ok passo per passo.
- [ ] Account per il collaudo in PROD: il superuser e UN amministratore reale (solo lettura) per la
  verifica del perimetro. Nessuna password nel repo o nelle note.

## Fase 1 — Verifiche in sola lettura (il giorno prima o subito prima)
DB .75 (psql 9.4 del laptop, in LAN: `D:\Programmi\PostgreSQL\9.4\bin\psql.exe -h 192.168.0.75 -U postgres -d wera`):
```sql
SELECT inet_server_addr(), current_database(), version();                 -- deve essere .75 / wera / 9.4+
SELECT shobj_description(oid,'pg_database') FROM pg_database WHERE datname='wera'; -- NON 'WERA-TEST...'
SELECT nspname FROM pg_namespace WHERE nspname='centrale_termica';         -- nessuna riga
SELECT codice, descrizione FROM parametri WHERE dominio='DOC' ORDER BY 1;  -- 001..016 (016 INAIL)
SELECT codice FROM profili ORDER BY 1;                                     -- ADM, AMM, CEN, CON, INS
SELECT count(*) FROM elenco_file_importati_file_manager;                   -- volume per le viste
SELECT current_user, usesuper FROM pg_user WHERE usename=current_user;     -- proprietario dello schema
```
- [ ] La versione deve essere >= 9.4 (lo script usa `to_regclass` e `LATERAL`). Se fosse < 9.4: STOP.
- [ ] Impronta dello schema `public` PRIMA (per dimostrare dopo che /wera non e' cambiato):
  `pg_dump -h 192.168.0.75 -U postgres -s -n public wera | sha256sum > public-prima.sha` (solo struttura).
- [ ] Ultimo backup notturno di .75 presente e recente (chi gestisce i backup del DB).

Server .106 (ssh root con il client OpenSSH di Windows):
```bash
ls -l --time-style=long-iso /var/lib/tomcat9/webapps/   # wera2026.war in esercizio (fc8da71, deploy Jenkins #6)
mount | grep -E '/usr/share/webapps/wera( |$)'                       # mount CIFS di .81 presente
grep -E '^directory.external_files' /var/lib/tomcat9/webapps/wera2026/WEB-INF/config/config.cfg
sudo -u tomcat test -w /usr/share/webapps/wera && echo scrivibile  # tomcat scrive sulla share?
```
- [ ] Se la share non e' scrivibile dall'utente tomcat: STOP, va sistemato il mount (opzioni uid/gid)
  prima del deploy, altrimenti i caricamenti falliscono (il resto funziona).

Apache di frontend .50 (`webfe01`), sola lettura:
- [ ] `LimitRequestBody` e timeout del proxy per `/wera2026` compatibili con caricamenti fino a
  20 file x 25 MB (parametri `FILE.MAX_PER_CARICAMENTO`, `FILE.DIMENSIONE_MAX_MB`). Se piu' bassi:
  abbassare i parametri dalla pagina di configurazione dopo il deploy, non toccare Apache.

## Fase 2 — Backup e push
- [ ] Copia del WAR in esercizio (rollback in 1 comando):
  `cp -p /var/lib/tomcat9/webapps/wera2026.war /root/wera2026.war.bak-AAAAMMGG-fc8da71`
- [ ] Push del branch (lo fa l'operatore, regola di progetto): `git push origin restyle/ui-2026-r2`.
  Non serve il merge su `main` per il deploy (il job builda il branch).

## Fase 3 — DDL su .75
```bash
D:\Programmi\PostgreSQL\9.4\bin\psql.exe -h 192.168.0.75 -U postgres -d wera -v ON_ERROR_STOP=1 -f src/resources/sql/wera2026/001-centrale_termica-ddl.sql
```
- [ ] Esito 0 (lo script ha BEGIN/COMMIT: o tutto o niente).
- [ ] Verifiche:
```sql
SELECT count(*) FROM pg_tables WHERE schemaname='centrale_termica';  -- 9
SELECT count(*) FROM pg_views  WHERE schemaname='centrale_termica';  -- 3 (v_documenti_wera, v_documenti, v_scadenze)
SELECT (SELECT count(*) FROM centrale_termica.sezioni), (SELECT count(*) FROM centrale_termica.parametri),
       (SELECT count(*) FROM centrale_termica.categorie_wera), (SELECT count(*) FROM centrale_termica.tipi_scadenza); -- 8, 21, 16, 6
SELECT codice_sezione, count(*), sum(fl_file_sovrascritto::int) FROM centrale_termica.v_documenti_wera GROUP BY 1; -- documenti di /wera smistati
EXPLAIN ANALYZE SELECT * FROM centrale_termica.v_documenti WHERE id_condominio = <un condominio con molti file>; -- tempo accettabile
```
- [ ] Impronta di `public` DOPO identica a quella di prima (`pg_dump -s -n public ... | sha256sum`).

## Fase 4 — Build e deploy Jenkins (192.168.0.115, job "wera2026 PROD")
1. [ ] Lancio con `BRANCH=restyle/ui-2026-r2`, **DEPLOY=false**: solo build e WAR patchato.
2. [ ] Controllo dell'artefatto (log del job o WAR scaricato):
   - `Quartz-scheduler.tasklist=` vuota; `mail.SMTPHost=127.0.0.1`, porta 1025 (profilo prod senza mail);
   - `grep -c DSCentraleTermica WEB-INF/config/config.cfg` > 0; `grep -c CentraleTermica WEB-INF/config/functionList.txt` = 8;
   - versione = ultimo commit del branch.
3. [ ] Rilancio con **DEPLOY=true** (copia a caldo in `/var/lib/tomcat9/webapps/`, gestit non si ferma),
   poi rimettere/lasciare DEPLOY=false come default.
4. [ ] Log: `journalctl -u tomcat9 --since "-10 min"` e `catalina.out`: redeploy completato, nessuna
   eccezione all'avvio (il NullPointerException allo stop della vecchia istanza e i SEVERE ThreadLocal
   sono attesi e preesistenti).

## Fase 5 — Verifiche post-deploy (prima solo lettura)
Su https://weraweb.it/wera2026/ :
- [ ] Superuser: voce "Centrale termica" nel menu (se non compare subito: logout/login, i campi di
  sessione si scrivono al primo accesso); pagina di un condominio con documenti di /wera: riquadri,
  cartelle, "da WERA"; download di un documento di /wera (sola lettura sul file, scrive solo un evento);
  pagina Configurazione apribile.
- [ ] Amministratore reale: vede solo i suoi condomini, nessun "Carica documenti", niente Stato
  pagamenti, vecchia voce "Centrali termiche" nascosta.
- [ ] Condomino (se disponibile): nessuna voce; l'URL della pagina rimanda alla Home.
- [ ] /wera su .70 invariato: login e "Visualizzazione" del file manager come prima.
- [ ] Registro: `SELECT tipo_evento, count(*) FROM centrale_termica.eventi GROUP BY 1;` (solo DOWNLOAD /
  ACCESSO_NEGATO attesi a questo punto).

Configurazione iniziale (superuser, dalla pagina Configurazione):
- [ ] contatti Assistenza (ufficio, email, telefono, orari, link segnalazione) e `ETICHETTA.GESTIONE`;
- [ ] periodicita' dei tipi di scadenza confermate da SIRBI; posizione del cartello (oggi Documentazione
  tecnica);
- [ ] se Apache limita le richieste: `FILE.DIMENSIONE_MAX_MB` / `FILE.MAX_PER_CARICAMENTO`.

## Fase 6 — Primi caricamenti reali (contratti di Andrea)
- Ogni caricamento e' PERMANENTE (storico mai cancellato, trigger sul DB): nessun file di prova in PROD.
  Un errore si corregge con una nuova versione o mettendo il documento ANNULLATO, non cancellandolo.
- [ ] I PDF scaricati dal Dropbox in una cartella locale dall'operatore (mai nel repo).
- [ ] Primo contratto: condominio giusto, sezione "Nomina Terzo Responsabile", data del documento; poi
  Scarica e confronto byte per byte / impronta SHA-256 mostrata dal caricamento.
- [ ] Verifica sul server: `ls -l /usr/share/webapps/wera/centrale_termica/<azienda>/<condominio>/2026/`.
- [ ] Poi gli altri contratti; se sono molti, valutare uno strumento di caricamento in blocco (CSV
  nome file -> condominio), da fare come ondata a parte.

## Rollback (dal piu' leggero al piu' pesante)
1. **Spegnere la funzione senza deploy** (kill switch): via SQL, perche' dalla pagina non ci si puo'
   togliere l'accesso:
   `UPDATE centrale_termica.parametri SET valore='' WHERE id_azienda IS NULL AND chiave='ACCESSO.PROFILI';`
   La pagina rifiuta subito tutti; la voce di menu sparisce al login successivo. Ripristino: `'ADM,INS,AMM'`.
2. **WAR precedente** (wera2026 torna a `fc8da71`, ~5 s, gestit non si ferma):
   `cp -p /root/wera2026.war.bak-AAAAMMGG-fc8da71 /var/lib/tomcat9/webapps/wera2026.war`
   Lo schema `centrale_termica` resta: non lo usa nessun altro ed e' innocuo per /wera.
3. **Schema** (SOLO se non e' stato caricato nessun documento reale, altrimenti si perde lo storico):
   `SELECT count(*) FROM centrale_termica.documenti;` = 0, poi
   `DROP SCHEMA centrale_termica CASCADE;` e rimozione di `/usr/share/webapps/wera/centrale_termica/`
   (vuota). Con documenti reali: NON eseguire, basta il punto 1 o 2.

## Rischi noti
- Installatore (INS) mai provato (assente nel DB locale): verificarlo in PROD in sola lettura.
- I parametri di menu/titolo sono in sessione: valgono dal login successivo.
- `v_documenti` ricalcola la finestra su tutti i file di /wera: tempo da misurare in Fase 3.
- Nessuna mail di avviso scadenze (Quartz spento in wera2026).
- Le scadenze e i documenti di prova esistono solo sul DB del laptop; in PROD si parte vuoti.

## Registro di esecuzione
### 2026-10-25 22:55 — Fase 1 (DB) e Fase 3 ESEGUITE (ok dell'operatore), deploy NON eseguito
- Verifiche in sola lettura (sessione `default_transaction_read_only`): server 192.168.0.75, DB `wera`,
  PostgreSQL 9.4.26 UTF8, nessun commento sul DB (non e' la copia di test), schema assente, utente
  `postgres` superuser, categorie DOC 001-016, profili ADM/AMM/CEN/CON/INS, plpgsql presente.
  File di /wera in PROD: 5 righe, 4 nomi distinti (categorie 001, 003 x3, 016), nessuna data prima
  segnalazione valorizzata. Push del branch fatto prima (`585b7fd..703162c`).
- DDL `001-centrale_termica-ddl.sql` (versione del commit `9bd5168`) eseguito alle 22:55:59, exit 0.
- Dopo: 9 tabelle, 3 viste, 2 funzioni, 8 trigger; dati iniziali 8 sezioni, 23 permessi, 21 parametri,
  16 categorie, 6 tipi; documenti/versioni/scadenze/eventi vuoti. `v_documenti_wera`: DOC_TECNICA 3
  (1 file sovrascritto), FOTO 1, SICUREZZA 1; `v_documenti` 4 righe WERA. EXPLAIN ANALYZE di
  `v_documenti` per un condominio: 0,6 ms.
- Struttura di `public` (pg_dump -s -n public): contenuto IDENTICO prima e dopo (impronta delle righe
  ordinate `0a991ca4...d273` in entrambi i casi). L'impronta del file grezzo cambia solo perche'
  pg_dump sposta in alto le due tabelle lette dalle viste nuove (`elenco_file_importati_file_manager`,
  `parametri`): nessuna riga aggiunta o tolta.
- Restano prima del deploy: verifica della share su .106 scrivibile da tomcat, Apache .50, backup del
  WAR in esercizio, poi Fase 4 (Jenkins).
