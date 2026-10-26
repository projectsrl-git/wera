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
- [ ] Nell'access log le richieste da Internet devono arrivare da 192.168.0.50 (Apache di frontend): e'
  l'IP del parametro `ANTIABUSO.PROXY_FIDATI`. Se l'IP fosse un altro, va messo quello nel parametro
  (dopo il DDL), altrimenti il limite per IP conterebbe tutti gli utenti come uno solo.

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
-- solo se la Fase 1 lo ha chiesto: ATTIVAZIONE.AZIENDA_CODICE, ANTIABUSO.PROXY_FIDATI, FILE.DIMENSIONE_MAX_MB, FILE.MAX_PER_RICHIESTA
COMMIT;
```
- [ ] Verifiche:
```sql
SELECT count(*) FROM pg_tables WHERE schemaname='registrazioni';                    -- 4 (richieste, allegati, parametri, eventi)
SELECT count(*) FROM pg_views  WHERE schemaname='registrazioni';                    -- 1 (v_richieste)
SELECT count(*) FROM pg_proc p JOIN pg_namespace n ON n.oid=p.pronamespace WHERE nspname='registrazioni'; -- 2
SELECT count(*) FROM pg_trigger WHERE tgrelid='registrazioni.eventi'::regclass AND NOT tgisinternal;      -- 2
SELECT count(*) FROM registrazioni.parametri;                                       -- 42
SELECT chiave, valore FROM registrazioni.parametri WHERE ts_mod IS NOT NULL ORDER BY 1; -- solo quelli cambiati sopra
SELECT (SELECT count(*) FROM registrazioni.richieste), (SELECT count(*) FROM registrazioni.eventi); -- 0, 0
```
- [ ] Impronta ordinata di `public` DOPO identica a quella di prima.

## Fase 4 — Build e deploy (laptop, `deploy/jenkins-wera2026.ps1`, job "wera2026 PROD" su .115)
1. [ ] Solo build: `.\deploy\jenkins-wera2026.ps1` -> SUCCESS, controllo dell'artefatto dello script "OK".
2. [ ] Controllo in piu' sull'artefatto (log della build o WAR scaricato):
   - `Quartz-scheduler.tasklist=` vuota; `mail.SMTPHost=127.0.0.1`, porta 1025; `jdbc/wera`;
   - `grep -c Registrazion WEB-INF/config/functionList.txt` = 4; presenti
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
4. [ ] IP del richiedente: `SELECT tipo_evento, ip FROM registrazioni.eventi ORDER BY id_evento DESC LIMIT 5;`
   deve essere l'IP pubblico dell'operatore, NON 192.168.0.50. Se e' .50: rimettere
   `REGISTRAZIONE.ATTIVA=N`, correggere `ANTIABUSO.PROXY_FIDATI` con l'IP visto da Tomcat, riprovare.
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
- L'IP del richiedente dipende da X-Forwarded-For tramite .50 (verifica della Fase 6.4).
- Conservazione dei dati di richieste rifiutate o annullate: oggi restano (senza password). Regola di
  cancellazione da decidere con SIRBI (privacy).
- Pagine non viste a occhio sul laptop (screenshot non riusciti): guardarle in Fase 5, desktop e telefono.
- Con `MAIL.ATTIVA=N` il richiedente non riceve neanche la ricevuta: la pagina "Richiesta inviata" lo dice
  senza promettere la mail.

## Registro di esecuzione
(vuoto: niente eseguito)
