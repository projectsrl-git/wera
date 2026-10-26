# 2026-10-26 — PIANO: registrazione amministratore in produzione (wera2026 su .106, DB .75)

STATO: PIANO, NIENTE ESEGUITO. Ogni passo che scrive richiede l'ok esplicito dell'operatore al momento:
DDL e UPDATE su .75, push, Jenkins con `-Deploy`, apertura del modulo. Le credenziali non stanno qui:
le fornisce l'operatore. Disegno: `2026-10-26-disegno-registrazione-amministratore.md`; ondate 1-3 nelle note
dello stesso giorno.

## Cosa va in produzione
- Commit: `b5efe99` (schema), `e291d00` (pagina pubblica), `8560a77` (approvazione), piu' il commit di questo
  piano. Sul branch ci sono anche `75d5994`, una nota di un'altra sessione (solo documentazione, nessun
  effetto sul WAR), e i commit gia' in esercizio fino a `f535152`.
  `release/wera2026` e' a `f535152` ed e' antenata del branch di lavoro: il rilascio e' un fast-forward.
- DB .75: solo lo schema NUOVO `registrazioni` (`src/resources/sql/wera2026/003-registrazioni-ddl.sql`,
  transazione unica, idempotente, 9.4). Nessun oggetto di `public` cambia; la vista `v_richieste` LEGGE
  `public.utenti`.
- Scritture su tabelle di /wera: SOLO all'approvazione di una richiesta e solo con INSERT di righe nuove in
  `utenti`, `utenti_lingue`, `utenti_profili`, `utenti_aziende`, `password` (come la gestione utenti di
  /wera). Il deploy da solo non scrive nulla in `public`.
- WAR wera2026 profilo prod: pacchetto `net.projectsrl.wera.registrazioni`, 4 funzioni in functionList
  (RegistrazioneAmministratore, RegistrazioniRichieste, RegistrazioniDati, RegistrazioniEsito), template
  `registrazioneamministratore.html` e `registrazionirichieste.html`, JS e `css/wera-registrazioni.css` nuovi,
  `wera-landing.css?20261026`, CTA amministratore della landing, voce "Registrazioni" nel menu.
  `config.cfg` NON cambia (nessun dataset nuovo).
- File nuovi su disco: `directory.external_files` del profilo prod (`/usr/share/webapps/wera/`, share di .81
  condivisa con /wera) + parametro `FILE.CARTELLA` = `registrazioni/`. /wera non usa quella sottocartella;
  la crea l'applicazione alla prima foto.

**Ordine obbligato: DDL prima del deploy.** Con il WAR nuovo e lo schema assente il modulo pubblico va in
errore 500 e la voce di menu resta nascosta (il resto di wera2026 funziona). Lo schema senza il WAR nuovo
invece e' innocuo: nessuno lo usa.

**Apertura in due tempi (raccomandata).** Il DDL crea il parametro `REGISTRAZIONE.ATTIVA` = S; subito dopo il
DDL lo si mette a N. Cosi' il deploy esce "spento": il modulo pubblico mostra "registrazioni sospese",
mentre la pagina di approvazione e il resto si possono verificare. Si apre (S) solo dopo le verifiche.

## Fase 0 — Decisioni e valori (operatore)
- [ ] Finestra: 06:00-23:00 (il FortiGate lascia uscire Jenkins verso GitHub/Maven solo in quella fascia).
  Deploy a caldo: wera2026 sparisce per circa 5 s, /wera (.70) e gestit non si fermano.
- [ ] Chi esegue: Claude con ok passo per passo (come per la Centrale termica), push compreso se l'operatore
  lo chiede.
- [ ] Valori dei parametri, tutti facoltativi (vuoto = voce nascosta o nessun invio):
  - `MAIL.NOTIFICA_DESTINATARI`: chi riceve l'avviso di nuova richiesta. Con `MAIL.ATTIVA=N` non serve:
    il contatore nel menu "Registrazioni" basta;
  - `ASSISTENZA.EMAIL`, `ASSISTENZA.TELEFONO`: contatti mostrati nel modulo pubblico;
  - `PRIVACY.LINK`: link all'informativa privacy accanto al consenso. Consigliato prima dell'apertura: il
    modulo raccoglie dati personali (referente, telefono, foto). Testo da SIRBI.
  - `TESTO.TEMPI_RISPOSTA` (oggi "entro 2 giorni lavorativi"), da confermare con SIRBI.
- [ ] Richiesta di prova in PROD dopo l'apertura: si', con l'email dell'operatore, poi ANNULLATA (non
  approvata, cosi' non nasce nessun utente). La riga resta nello schema `registrazioni` come ANNULLATA:
  il registro eventi e' append-only e la referenzia. Alternativa: nessuna prova, ci si affida al collaudo
  del laptop (in quel caso la verifica dell'IP del punto 6.4 si fa sulla prima richiesta vera).
- [ ] Account per le verifiche: superuser (ADM) e un amministratore reale (AMM) in sola lettura.

## Fase 1 — Verifiche in sola lettura (subito prima)
DB .75, sessione in sola lettura (`PGOPTIONS='-c default_transaction_read_only=on'`), psql 9.4 del laptop:
`D:\Programmi\PostgreSQL\9.4\bin\psql.exe -h 192.168.0.75 -U postgres -d wera`
```sql
SELECT inet_server_addr(), current_database(), version();                    -- .75 / wera / 9.4.26
SELECT shobj_description(oid,'pg_database') FROM pg_database WHERE datname='wera'; -- vuoto, NON 'WERA-TEST...'
SELECT nspname FROM pg_namespace WHERE nspname='registrazioni';               -- nessuna riga
SELECT id_azienda, codice FROM aziende WHERE codice='SIRBI';                  -- esattamente 1 riga
SELECT id_profilo, codice FROM profili WHERE trim(codice)='AMM';              -- 1 riga
SELECT id_lingue_iso FROM lingue_iso WHERE trim(codice_iso)='it';             -- 1 riga
-- colonne usate dall'approvazione (devono esistere come sul DB locale)
SELECT table_name, column_name, data_type, character_maximum_length, column_default
  FROM information_schema.columns WHERE table_schema='public'
   AND table_name IN ('utenti','utenti_lingue','utenti_profili','utenti_aziende','password') ORDER BY 1, ordinal_position;
-- sequenze allineate (una sequenza indietro farebbe fallire l'approvazione con "chiave duplicata")
SELECT (SELECT last_value FROM utenti_id_utente_seq) >= (SELECT max(id_utente) FROM utenti),
       (SELECT last_value FROM password_id_password_seq) >= (SELECT max(id_password) FROM password),
       (SELECT last_value FROM utenti_lingue_id_utenti_lingue_seq) >= (SELECT max(id_utenti_lingue) FROM utenti_lingue),
       (SELECT last_value FROM utenti_profili_id_utente_profilo_seq) >= (SELECT max(id_utente_profilo) FROM utenti_profili),
       (SELECT last_value FROM utenti_aziende_id_utente_azienda_seq) >= (SELECT max(id_utente_azienda) FROM utenti_aziende); -- tutti t
SELECT count(*) FROM pg_constraint WHERE conname='k2_utenti';                 -- 1 (username univoco)
```
- [ ] Se `SIRBI` non esiste o non e' unica: STOP, scegliere il codice giusto e metterlo in
  `ATTIVAZIONE.AZIENDA_CODICE` dopo il DDL (nessuna modifica al codice).
- [ ] Se una sequenza e' indietro: STOP e segnalarlo (riguarderebbe anche /wera); non correggere senza ok.
- [ ] Impronta della struttura di `public` PRIMA, ordinata come il 25-26/10:
  `pg_dump -h 192.168.0.75 -U postgres -s -n public wera | sort | sha256sum` (atteso `0a991ca4...d273`).
- [ ] Ultimo backup notturno di .75 presente e recente (chi gestisce i backup del DB).

Server .106 (ssh root, client OpenSSH di Windows), sola lettura:
```bash
ssh deploy@192.168.0.106 "wera2026 list"                       # in esercizio: f535152 (o successivo), backup presenti
mount | grep -E '/usr/share/webapps/wera( |$)'                # mount CIFS di .81 presente, rw, uid 997
ls -ld /usr/share/webapps/wera/registrazioni 2>/dev/null      # non deve esistere ancora
grep -E '^(directory.external_files|mail.SMTPHost|ResetPassword.Mail.Address.From)=' /var/lib/tomcat9/webapps/wera2026/WEB-INF/config/config.cfg
tail -5 /var/log/tomcat9/localhost_access_log.$(date +%F).txt # IP sorgente delle richieste da Internet
```
- [ ] La scrittura di Tomcat sulla share e' gia' stata provata il 25/10 (nel namespace della sandbox): basta
  che il mount sia lo stesso.
- [x] (esito 2026-10-26) Tutti i client arrivano da `192.168.50.253` (NAT a monte di .50) e Tomcat ha gia' un
  `RemoteIpValve`: il limite per IP vale come tetto giornaliero per tutto il sito. Decisione dell'operatore:
  `ANTIABUSO.MAX_RICHIESTE_IP_24H` = 30 come default nel DDL 003; `ANTIABUSO.PROXY_FIDATI` non serve.

Apache di frontend .50 (`webfe01`), sola lettura:
- [ ] `grep -n -i -E 'LimitRequestBody|ProxyTimeout|Timeout|RequestHeader|X-Forwarded' /etc/apache2/sites-available/ssl.weraweb.it.conf /etc/apache2/apache2.conf`
  - il modulo invia fino a 6 foto x 10 MB (+ campi) in una richiesta, circa 62 MB: `LimitRequestBody`
    assente (illimitato) o maggiore. Se piu' basso: abbassare `FILE.DIMENSIONE_MAX_MB` /
    `FILE.MAX_PER_RICHIESTA` dopo il DDL, non toccare Apache;
  - `X-Forwarded-For` lo aggiunge mod_proxy da solo: nessuna riga che lo tolga o lo riscriva.

## Fase 2 — Push e branch di rilascio (operatore, o Claude su sua richiesta)
```bash
git fetch origin
git push origin restyle/ui-2026-r2                        # branch di lavoro (4 commit + questo piano)
git push origin restyle/ui-2026-r2:release/wera2026       # fast-forward, MAI --force
```
- [ ] Le modifiche non committate di altre sessioni (`deploy/README.md`, `deploy/jenkins-wera2026.ps1`, file
  non tracciati) NON fanno parte del rilascio: il push porta solo i commit.

## Fase 3 — DDL su .75 e parametri iniziali
```bash
D:\Programmi\PostgreSQL\9.4\bin\psql.exe -h 192.168.0.75 -U postgres -d wera -v ON_ERROR_STOP=1 -f src/resources/sql/wera2026/003-registrazioni-ddl.sql
```
- [ ] Esito 0 (BEGIN/COMMIT: o tutto o niente).
- [ ] Parametri della produzione, in una transazione, controllando che ogni UPDATE tocchi 1 riga:
```sql
BEGIN;
UPDATE registrazioni.parametri SET valore='N', ts_mod=now(), id_utente_mod=0 WHERE id_azienda IS NULL AND chiave='MAIL.ATTIVA';          -- mail spente finche' l'SMTP e' il mailcatcher
UPDATE registrazioni.parametri SET valore='N', ts_mod=now(), id_utente_mod=0 WHERE id_azienda IS NULL AND chiave='REGISTRAZIONE.ATTIVA'; -- deploy "spento"
-- solo se decisi in Fase 0 (valori dell'operatore, mai nel repo):
-- UPDATE ... SET valore='<...>' WHERE id_azienda IS NULL AND chiave IN ('MAIL.NOTIFICA_DESTINATARI','ASSISTENZA.EMAIL','ASSISTENZA.TELEFONO','PRIVACY.LINK','TESTO.TEMPI_RISPOSTA');
-- dalla Fase 1 non serve altro: SIRBI esiste (id 1), Apache non limita la dimensione, il tetto di 30 invii/24h e' gia' il default del DDL
COMMIT;
```
- [ ] Verifiche:
```sql
SELECT count(*) FROM pg_tables WHERE schemaname='registrazioni';                    -- 4 (richieste, allegati, parametri, eventi)
SELECT count(*) FROM pg_views  WHERE schemaname='registrazioni';                    -- 1 (v_richieste)
SELECT count(*) FROM pg_proc p JOIN pg_namespace n ON n.oid=p.pronamespace WHERE nspname='registrazioni'; -- 2
SELECT count(*) FROM pg_trigger WHERE tgrelid='registrazioni.eventi'::regclass AND NOT tgisinternal;      -- 2
SELECT count(*) FROM registrazioni.parametri;                                       -- 42
SELECT valore FROM registrazioni.parametri WHERE id_azienda IS NULL AND chiave='ANTIABUSO.MAX_RICHIESTE_IP_24H'; -- 30 (tetto giornaliero del sito)
SELECT chiave, valore FROM registrazioni.parametri WHERE ts_mod IS NOT NULL ORDER BY 1; -- solo quelli cambiati sopra
SELECT (SELECT count(*) FROM registrazioni.richieste), (SELECT count(*) FROM registrazioni.eventi); -- 0, 0
```
- [ ] Impronta ordinata di `public` DOPO identica a quella di prima.

## Fase 4 — Build e deploy (laptop, `deploy/jenkins-wera2026.ps1`, job "wera2026 PROD" su .115)
1. [ ] Solo build: `.\deploy\jenkins-wera2026.ps1` -> SUCCESS, controllo dell'artefatto dello script "OK".
2. [ ] Controllo in piu' sull'artefatto (log della build o WAR scaricato):
   - `Quartz-scheduler.tasklist=` vuota; `mail.SMTPHost=127.0.0.1`, porta 1025; `jdbc/wera`;
   - `grep -c -E '^Registrazion(eAmministratore|i)' WEB-INF/config/functionList.txt` = 4 (un semplice
     `grep -c Registrazion` conta anche la registrazione inquilino esistente: in PROD vale gia' 1); presenti
     `WEB-INF/template/registrazioneamministratore.html`, `WEB-INF/template/registrazionirichieste.html`,
     `WEB-INF/classes/net/projectsrl/wera/registrazioni/FunctionRegistrazioniEsito.class`;
   - commit della build = ultimo di `release/wera2026`.
3. [ ] Deploy: `.\deploy\jenkins-wera2026.ps1 -Deploy` (il job fa da solo il backup della versione in esercizio
   in `/var/backups/deploy-war/`, poi la copia a caldo e attende HTTP 200).
4. [ ] Log su .106: redeploy di `/wera2026` completato, nessuna eccezione della nuova istanza (gli avvisi di
   Tomcat allo undeploy della vecchia, ThreadLocal/driver JDBC/ObjectStreamClass/NPE in `unload`, sono attesi);
   nessun file nuovo in `/usr/share/logs/wera2026/crash`; `ssh deploy@192.168.0.106 "wera2026 list"`.

## Fase 5 — Verifiche post-deploy (modulo ancora chiuso)
Su https://weraweb.it/wera2026/ :
- [ ] Landing: la slide amministratore e la card profilo hanno "Registrati" e portano al modulo; il dialog di
  login ha "Registra il tuo studio"; il login funziona come prima.
- [ ] Modulo pubblico: mostra "Le registrazioni sono momentaneamente sospese" (REGISTRAZIONE.ATTIVA=N).
  Un POST diretto risponde 503 con lo stesso testo.
- [ ] Superuser (logout/login: i campi di sessione del menu si scrivono al primo accesso): voce
  "Registrazioni" senza contatore; pagina con 0 richieste in tutti gli stati e l'avviso "mail spente".
- [ ] Amministratore reale: nessuna voce "Registrazioni"; l'URL della pagina rimanda alla Home.
- [ ] Centrale termica e resto di wera2026 come prima; /wera su .70 invariato (login).
- [ ] Registro: `SELECT tipo_evento, count(*) FROM registrazioni.eventi GROUP BY 1;` (al piu' ACCESSO_NEGATO).

## Fase 6 — Apertura (ok dell'operatore)
1. [ ] `UPDATE registrazioni.parametri SET valore='S', ts_mod=now(), id_utente_mod=0 WHERE id_azienda IS NULL AND chiave='REGISTRAZIONE.ATTIVA';`
   (vale subito, senza deploy).
2. [ ] Se decisa in Fase 0: richiesta di prova dal browser dell'operatore, possibilmente da telefono (foto
   dalla fotocamera), con la sua email e dati fiscali formalmente validi. Atteso: esito "Richiesta inviata",
   contatore 1 nel menu del superuser, dettaglio con la foto, nessuna mail (registro `NON_INVIATA`).
3. [ ] Sul server: `ls -l /usr/share/webapps/wera/registrazioni/2026/<n>/` (la foto, proprietario tomcat).
4. [ ] Registro: `SELECT tipo_evento, ip FROM registrazioni.eventi ORDER BY id_evento DESC LIMIT 5;` -> RICHIESTA e
   MAIL NON_INVIATA; l'IP sara' `192.168.50.253` (atteso, vedi Fase 1): conta per il tetto di 30 invii al giorno.
5. [ ] Annullare la richiesta di prova dalla pagina ("Annulla (spam/doppione)"): nessun utente creato,
   nessuna mail.

## Fase 7 — Esercizio
- Le mail sono spente: SIRBI controlla il contatore della voce "Registrazioni"; dopo l'approvazione avvisa
  il richiedente di persona (entra con la sua email e la password scelta); poi collega i condomini
  dall'anagrafica condominio (campo Amministratore). Al primo accesso l'amministratore vede solo i condomini
  gia' collegati.
- Quando l'SMTP vero del profilo prod sara' acceso (stesso momento di `ALERT.ATTIVO` della Centrale termica):
  `MAIL.ATTIVA=S`. Mittente: con `MAIL.MITTENTE` vuoto si usa `ResetPassword.Mail.Address.From` di config.cfg
  (`noreply@weraweb.it`, che la patch prod NON cambia: cambia solo `mail.from` e `mail.from.service` in
  `noreply@test.invalid`); valorizzare `MAIL.MITTENTE` solo se serve un altro mittente. Le richieste gia'
  esaminate non ricevono mail retroattive.
- La password scelta scade dopo `PASSWORD.GIORNI_VALIDITA` (60) giorni: poi il login chiede di cambiarla
  (NewPassword), come per tutti gli utenti.

## Rollback (dal piu' leggero al piu' pesante)
1. **Chiudere il modulo senza deploy**: `REGISTRAZIONE.ATTIVA=N` (UPDATE come in Fase 6 con 'N'). Le richieste
   gia' arrivate restano consultabili e approvabili. Per nascondere anche la pagina di approvazione:
   `APPROVAZIONE.PROFILI` = '' (la voce sparisce al login successivo).
2. **WAR precedente**: `.\deploy\jenkins-wera2026.ps1 -Rollback` (nessuna build, funziona anche fuori dalla fascia
   06-23): rimette la versione in esercizio prima del deploy (`f535152`). Lo schema `registrazioni` resta,
   innocuo per /wera. Gli utenti gia' approvati restano utenti normali di /wera.
3. **Annullare un'approvazione**: NON cancellare l'utente; disattivarlo dalla gestione utenti
   (`utenti.fl_disattivo`) e togliergli i condomini. La richiesta resta APPROVATA nello storico.
4. **Schema** (SOLO se non c'e' nessuna richiesta APPROVATA, altrimenti si perde l'anagrafica fiscale degli
   amministratori attivati): `SELECT count(*) FROM registrazioni.richieste WHERE stato='APPROVATA';` = 0, poi
   `DROP SCHEMA registrazioni CASCADE;` e rimozione di `/usr/share/webapps/wera/registrazioni/`.

## Rischi noti
- Il modulo e' pubblico: anti-abuso (trappola, tempo minimo, limite per IP, un'attesa per email/P.IVA) e
  approvazione umana; nessun captcha. Se arriva spam: `ANTIABUSO.MAX_RICHIESTE_IP_24H` piu' basso, o modulo
  chiuso.
- Tetto di 30 invii al giorno per TUTTO il sito (tutti i client hanno lo stesso IP dietro il NAT): oltre, il
  modulo risponde 429 a chiunque fino allo scorrere delle 24 ore, con il testo "Troppe richieste da questa
  connessione..." (testo nel codice, non cambiato per decisione dell'operatore). Valore modificabile al volo
  con un UPDATE del parametro; NON usare 0 (bloccherebbe tutto). Per un limite per singolo client servirebbe
  far arrivare l'IP vero fino a Tomcat (FortiGate/Apache): lavoro di rete a parte.
- Conservazione dei dati di richieste rifiutate o annullate: oggi restano (senza password). Regola di
  cancellazione da decidere con SIRBI (privacy).
- Pagine non viste a occhio sul laptop (screenshot non riusciti): guardarle in Fase 5, desktop e telefono.
- Con `MAIL.ATTIVA=N` il richiedente non riceve neanche la ricevuta: la pagina "Richiesta inviata" lo dice
  senza promettere la mail.

## Registro di esecuzione
### 2026-10-26 12:55 — Fase 1 ESEGUITA (ok dell'operatore), solo letture; nient'altro eseguito
- .75 (sessione `default_transaction_read_only=on`, credenziali del `META-INF/context.xml` del WAR, mai
  stampate): server 192.168.0.75, DB `wera`, PostgreSQL 9.4.26, nessun commento sul DB (non e' la copia di
  test), utente `postgres` superuser. Schema `registrazioni` assente; schemi presenti `public`,
  `centrale_termica`. Azienda `SIRBI` = id 1 (unica azienda), profilo `AMM` = id 3 (31 utenti AMM),
  lingua `it` = id 1, vincolo `k2_utenti` presente, nessun username con maiuscole. Sequenze di utenti,
  password, utenti_lingue, utenti_profili, utenti_aziende tutte allineate (last_value >= max).
- Colonne di utenti, utenti_lingue, utenti_profili, utenti_aziende, password, profili, aziende, lingue_iso
  IDENTICHE al DB locale del collaudo (58 righe, tipi, lunghezze, null, default); vincoli uguali; nessun
  trigger su quelle tabelle.
- Impronta ordinata della struttura di `public` (`pg_dump -s -n public | sort | sha256sum`, 6671 righe):
  `0a991ca4bd9569be3035e3c5cb6e2ad1ba0d03f3250d67d5da5aa3381470d273`, IDENTICA al 25 e 26/10.
- Backup notturno di .75: NON verificato (non e' visibile da qui), resta all'operatore.
- .106: `wera2026 list` -> in esercizio `f535152` (sha256 5726a46c3c7b), prossimo rollback
  `bak-20261026-100300-4320eeb`; mount CIFS di .81 su `/usr/share/webapps/wera` rw, uid/gid 997;
  `registrazioni/` non esiste; config in esercizio: `directory.external_files=/usr/share/webapps/wera/`,
  SMTP `127.0.0.1:1025`, `ResetPassword.Mail.Address.From=noreply@weraweb.it`, Quartz vuoto; tomcat9 e
  wera-mailcatcher attivi; spazio: `/` 12 GB liberi, share 34 GB liberi.
- .50: nessun `LimitRequestBody` (vhost e apache2.conf), `Timeout 3600`, nessun `ProxyTimeout`: un invio da
  ~62 MB passa. Nessuna riga che tocchi `X-Forwarded-For`; mod_remoteip non caricato.
- **SCOPERTA (blocca il parametro del limite per IP cosi' com'e')**: Apache .50 vede TUTTI i client come
  `192.168.50.253` (28.097 richieste su 28.097 oggi, comprese quelle degli utenti reali di /wera; uguale nel
  log di ieri): l'IP pubblico si perde prima del frontend (NAT a monte, probabilmente il FortiGate). Su .106
  Tomcat ha gia' un `RemoteIpValve` (`internalProxies` .50 e .170, `X-Forwarded-For`), per cui
  `getRemoteAddr()` restituisce gia' `192.168.50.253`: il parametro `ANTIABUSO.PROXY_FIDATI` non serve, ma
  il limite `ANTIABUSO.MAX_RICHIESTE_IP_24H` diventa un contatore UNICO per tutto Internet (con 5, dopo 5
  invii in 24 ore il modulo risponderebbe 429 a chiunque). Il registro eventi mostrera' sempre lo stesso IP.
  Attenzione anche: con il parametro a 0 il codice blocca tutto (conta >= 0), non disattiva il limite.
  Decisione dell'operatore prima della Fase 3 (vedi riepilogo in chat). Fase 6.4 da rivedere di conseguenza.
- Correzione al piano: il controllo dell'artefatto in Fase 4 conta solo le 4 funzioni nuove.
- Decisione dell'operatore (dopo la Fase 1): limite come tetto giornaliero del sito, default 30 nel DDL 003,
  codice invariato (commit `fca8d07`; DDL riprovato su uno schema temporaneo del DB locale, poi eliminato).

### 2026-10-26 13:12-13:15 (ora del laptop) — Fasi 2 e 3 ESEGUITE (ok dell'operatore); deploy NON eseguito
- Fase 2: `git push origin restyle/ui-2026-r2` (`f535152..fca8d07`) e
  `git push origin restyle/ui-2026-r2:release/wera2026` (`f535152..fca8d07`, fast-forward). Commit portati:
  `75d5994` (nota CT di un'altra sessione), `b5efe99`, `e291d00`, `8560a77`, `cfe9bcc`, `74e25da`, `fca8d07`.
- Prima del DDL (sola lettura, 13:13): server 192.168.0.75, DB `wera` 9.4.26, nessun commento di test, schema
  `registrazioni` assente, 17 connessioni attive; impronta ordinata di `public` `0a991ca4...d273`.
- DDL `003-registrazioni-ddl.sql` (identico al commit `fca8d07`, sha256 del file `3ae66e30b2e0...`), lanciato
  dopo il controllo del server, 13:13:11-13:13:13, exit 0 (solo i 2 NOTICE attesi sui trigger non ancora
  esistenti).
- Parametri (transazione unica, blocco DO con controllo del server e di 1 riga per UPDATE): `MAIL.ATTIVA`=N,
  `REGISTRAZIONE.ATTIVA`=N, `id_utente_mod`=0. NB: `now()` del DB ha dato 12:09:59 mentre il laptop segnava
  circa 13:14: l'orologio (o il fuso) di .75 e' circa un'ora indietro; i `ts_*` delle righe useranno l'ora del DB.
- Dopo (sola lettura): 4 tabelle, 1 vista, 2 funzioni, 2 trigger su `eventi`, 12 indici, 42 parametri;
  cambiati solo `MAIL.ATTIVA`=N e `REGISTRAZIONE.ATTIVA`=N; `ANTIABUSO.MAX_RICHIESTE_IP_24H`=30;
  `ATTIVAZIONE.AZIENDA_CODICE`=SIRBI, `ATTIVAZIONE.PROFILO`=AMM, `APPROVAZIONE.PROFILI`=ADM; 0 richieste, 0 eventi,
  0 allegati; `valore_parametro('MAIL.ATTIVA')`=N; `v_richieste` interrogabile. Schema di proprieta' di `postgres`.
- Struttura di `public`: impronta ordinata IDENTICA (`0a991ca4...d273`) e anche il file grezzo di pg_dump identico.
- HTTP da laptop via weraweb.it: login wera2026 200, /wera 200 (in esercizio ancora il WAR `f535152`).
- Prossimo (serve ok): Fase 4, Jenkins solo build con controllo dell'artefatto, poi `-Deploy`.

### 2026-10-26 13:35-13:46 — Fase 4 ESEGUITA e Fase 5 (senza account) (ok dell'operatore); modulo ANCORA CHIUSO
- `deploy/jenkins-wera2026.ps1` (copia di lavoro con l'opzione -Rollback di un'altra sessione, non committata:
  passa solo `ROLLBACK=false` in piu') -> build #15, `release/wera2026` a `fca8d07`, BUILD SUCCESS, controllo
  dello script "OK", nessun deploy.
- Artefatto della #15 scaricato da Jenkins (100312008 byte, sha256 `13ef63a652e6d867...`): `Quartz-scheduler.tasklist=`
  vuota, SMTP `127.0.0.1:1025`, `ResetPassword.Mail.Address.From=noreply@weraweb.it`,
  `directory.external_files=/usr/share/webapps/wera/`, `jdbc/wera`; 4 funzioni nuove in functionList (piu' la
  `Registrazione` inquilino invariata); template, JS, CSS e classi del pacchetto presenti; voce di menu e CTA
  della landing presenti.
- `-Deploy` -> build #16 (stesso commit, stesso controllo OK), backup della versione in esercizio
  `/var/backups/deploy-war/wera2026.war.bak-20261026-134244-f535152` (sha256 5726a46c3c7b, prossimo rollback),
  WAR integro 100312008 byte, ridistribuito in 15 s, HTTP 200. In esercizio `fca8d07` (sha256 9e6044bb5fb4).
- Log su .106: undeploy 13:42:59, deploy 13:43:00-13:43:04; solo la NPE gia' nota in `InstanceManager.destroy()`
  della vecchia istanza; nessun crash in `/usr/share/logs/wera2026/crash`; tomcat9 attivo dal 24/10 (mai
  riavviato).
- Via https://weraweb.it: login wera2026 200, landing con 4 link al modulo e `wera-landing.css?20261026`; modulo
  200 con "Le registrazioni sono momentaneamente sospese..." e nessun form; POST diretto 503 con lo stesso
  testo; CSS/JS nuovi 200; `RegistrazioniDati` senza login -> landing, nessun dato; /wera 200.
- .75: 0 eventi, 0 richieste, `MAIL.ATTIVA`=N, `REGISTRAZIONE.ATTIVA`=N.
- NON fatte (servono account di produzione, li ha l'operatore): superuser -> logout/login, voce "Registrazioni"
  senza contatore, pagina con 0 richieste e avviso "mail spente"; amministratore reale -> nessuna voce, URL
  della pagina rimanda alla Home; Centrale termica come prima.
- Prossimo (serve ok): queste verifiche con gli account, poi Fase 6 (apertura).

### 2026-10-26 13:57 (ora del laptop) — Fase 6.1 ESEGUITA: modulo APERTO (ok dell'operatore)
- Verifiche con superuser e amministratore reali fatte dall'operatore dal browser; poi "ok apriamo".
- `REGISTRAZIONE.ATTIVA`=S su .75 (blocco DO con controllo del server e di 1 riga, da N a S, `id_utente_mod`=0).
  Parametri ora: `REGISTRAZIONE.ATTIVA`=S, `MAIL.ATTIVA`=N, `ANTIABUSO.MAX_RICHIESTE_IP_24H`=30.
- https://weraweb.it/wera2026/astro?FUNCTIONID=RegistrazioneAmministratore: 200, form presente, nessun testo
  "sospese", gettone anti-abuso generato, fasce dei condomini dai parametri.
- Restano (Fase 6.2-6.5, facoltative): richiesta di prova dell'operatore dal telefono, controllo di foto, registro
  (IP atteso 192.168.50.253) e annullamento della prova dalla pagina "Registrazioni".
