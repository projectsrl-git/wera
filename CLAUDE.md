# CLAUDE.md — WERA (contesto di progetto per Claude)

## Cos'e
WERA / WebCredit WERA: web app Java per contabilizzazione calore e ripartizione spese in condominio.
Framework proprietario ALI BOW/astro (config-driven), template FreeMarker, UI Bootstrap 3 + tema INSPINIA.
Stack: Java 7 (build ok con JDK8), servlet, FreeMarker, Maven -> WAR, Tomcat 9.
DB: **PostgreSQL** (datasource JNDI definito in `src/main/webapp/META-INF/context.xml`,
host placeholder `db-wera-etc-host` risolto via file hosts; le query in config.cfg sono
sintassi Postgres. I vecchi documenti citano SQL Server/jTDS: il driver jtds e nel WAR
ma non e quello usato).

## Struttura rilevante
- Template FreeMarker: `src/main/webapp/WEB-INF/template/*.html` (84) + layout condivisi in `.../template/include/`.
- CSS/JS statici: `src/main/webapp/{css,js,fonts,img,font-awesome}`.
- Config applicativa (SQL, FUNCTIONID, routing): `src/main/webapp/WEB-INF/config/config.cfg`.
- Le cartelle `template/non usate` e `css/non usati` sono codice morto: NON toccarle.

## Riferimenti design (in repo)
Tutto in `.claude/design/`: `01_wera_design_tokens.css` (token, sorgente di verita),
`02_design_guide.md` (mappatura classi), `03_scope_e_guardrail.md` (ambito/vincoli/consegna,
DA RISPETTARE INTEGRALMENTE), `04_inventario_template.md` (ondate), `demo_riferimento_app.html`
e `demo_riferimento_landing.html` (riferimento visivo).

## Restyling UI — stato (consolidato 2026-10-22)
- **Fase 1** (branch `restyle/ui-2026`, MERGED in main): ondate 0-6 complete (2026-07).
  Quasi tutto CSS-only nel tema: sez. 21 anagrafiche, 22 ripartizioni, 23 config guidata,
  24 import/file, 25 varie, 26 home board, 27 topbar-titolo/Richieste, 28 bottoni+tab.
  Esclusi di proposito: mail.html (template EMAIL con hack MSO: non riskinnare);
  recolor intestazioni Riscaldamento/ACS in inserimentoripartizioniuni2018 (color coding
  funzionale applicato anche via JS in rowCallback).
- **Fase 2 r2** (branch `restyle/ui-2026-r2`, da main, pushato il 2026-10-22, PR verso main): allineamento al mockup
  giro 12 in `D:\SVILUPPO\wera_demo` (fuori repo; la copia in `src/resources/wera_demo` e
  ferma a luglio). Il tema di riferimento del mockup e INVARIATO rispetto a fase 1: le
  novita sono nelle pagine. Commit:
  1. `27f339f` home v2 — card "I tuoi crediti" con dati reali da DSCrediti (stessa chiamata
     JSONDataSet di profilo.html, nessun dataset nuovo), donut in riga propria (tema sez. 29).
  2. `872e4c1` landing v2 — login.html = hero carosello 4 slide con personaggi
     (`img/landing/`), login in DIALOG che si RIAPRE da solo su ERROR_MESSAGE;
     wera-landing.css riscritto dagli style del mockup.
  3. `5455a56` registrazione v2 — guida "Dove trovo il codice?" + form card; campi e POST
     INVARIATI (il mockup aggiunge password/privacy/PROFILO che il backend non prevede: saltati).
  4. `356548c` reset/new password v2 — stesso stack landing (.onecol); script legacy
     validatePassword conservato (morto da sempre), rimosso solo l'ERROR_MESSAGE duplicato.
  5. `4caa34d` fix deploy — make_test_war.bat riconfeziona con jar.exe (vedi Istanza di test).
  6. `174f53f` fix template (2026-10-22, SU RICHIESTA ESPLICITA dell'operatore, deroga a "DA NON
     TOCCARE"): `>` mancante sul div Indirizzo di configurazioneguidata1 e condizione
     `SALVATAGGIO_IN_CORSO` (booleano) nei 4 inserimento ripartizioni. Dettagli nella nota
     `.claude/2026-10-22-fix-bug-preesistenti-template.md`.
  TUTTO il percorso pubblico (login/registrazione/reset/newpassword) usa `wera-landing.css`;
  `wera-theme.css` (sez. 1-29) resta per l'app interna.
- **ESCLUSO su decisione (fase 2)**: slide 5 "reward"/promo coupon della landing — il LEGGIMI
  del mockup la dichiara testo promozionale senza logica; riaggiungerla SOLO su indicazione
  esplicita dell'operatore (markup pronto nel mockup).
- **In attesa di backend/dataset** (non fattibili senza toccare config.cfg/Java):
  "Andamento letture mensili (HCA)" in home (serve un DS MESE/VALORE in config.cfg),
  "Consumi per condominio", "Previsione consumi", "Scadenziario Centrale termica",
  registrazione amministratore/installatore, "spesa in euro" inquilino/amministratore.
- Design system: `src/main/webapp/css/wera-theme.css` (token + override, sezioni numerate
  1-29), linkato per ULTIMO in `include/included_head.include`; errore/errorpage/message lo
  linkano direttamente. IMPORTANTE: a OGNI modifica di un CSS aggiornare il cache-buster
  `?AAAAMMGG` dei link (included_head + pagine standalone + login/registrazione/reset per
  wera-landing), altrimenti i browser servono la copia in cache.
- Font: Bricolage Grotesque (display) + Manrope (UI), via Google Fonts.
- Approccio CSS-first: si ridefiniscono le classi esistenti; si interviene sui template solo
  dove serve (stili inline in conflitto, wrapper), preservando il cablaggio.
- Nota numeri: `toLocaleString('it-IT')` raggruppa solo da 10.000 in su (CLDR
  minimumGroupingDigits=2): "1258" senza punto e corretto, non e un bug.

## Regole "DA NON TOCCARE"
- Codice Java, `pom.xml`, `config.cfg`, SQL/`DBDataSet`, logica di business.
- Direttive/variabili FreeMarker: `${...}`, `<#...>` — preservate identiche.
- Cablaggio form: `name`, `id`, `action`, `method`, `FUNCTIONID`, `OPERATION_TYPE`, handler `onclick`,
  id/classi usati da jQuery e plugin (datatables, select2, datepicker, amcharts, jstree, metisMenu).
- Si AGGIUNGE (classi/wrapper), non si rinomina/rimuove nulla di esistente.
- Nei template di ripartizioni (ondata 3) e config guidata (ondata 4): massima prudenza,
  preferire override CSS puri; in dubbio saltare il punto, annotarlo nella nota di rilascio, proseguire.

## Encoding (IMPORTANTE)
File misti: `home.html` e gli `include/*.include` sono ISO-8859-1; login/registrazione/resetpassword/
newpassword/profilo sono UTF-8. Verificare con `file` PRIMA di editare e PRESERVARE l'encoding
originale (mai risalvare ISO-8859-1 come UTF-8: `git diff` mostrerebbe il file riscritto per intero).

## Sicurezza (aperto)
Repo storicamente pubblico con criticita note: credenziali SMTP in chiaro in `config.cfg`, dati reali
in history, SQL injection, hashing SHA-1. Rendere privato + ruotare credenziali + ripulire history
(`git filter-repo`). Non committare segreti; se emergono segreti o dati personali, segnalarli e non riprodurli.

## Build & QA (riferimento: laptop, D:\SVILUPPO\wera_git)
Build verificata anche da Claude Code: `mvn clean package` (dalla shell dell'operatore mvn e
nel PATH; dal Bash tool usare `D:\SVILUPPO\apache-maven-3.3.9\bin\mvn.cmd` con
`JAVA_HOME=D:\Programmi\Java\openjdk8` — target 1.7 compila con JDK8). WAR in `target\wera.war`.
Smoke test per ondata: login, home, una ricerca*, una inserimento*, inserimentoripartizioni
(tab Costi per unita), configurazione guidata, profilo. Checklist complete in `QA.md`.
Per il collaudo visuale senza Tomcat: servire una copia statica (template + css/img) con un
mini server (es. perl in scratchpad), strippando le direttive FreeMarker.

## Convenzione di rilascio (obbligatoria, ogni ondata)
1. Nota in `.claude/<AAAA-MM-GG>-<ondata>.md` (modello: `.claude/000-template-release.md`).
2. Aggiornamento di questo `CLAUDE.md` se cambia qualcosa di duraturo.
3. `COMMIT_MSG.txt` (conventional) -> `git commit -F COMMIT_MSG.txt`.
Procedere a ondate, mai un unico commit gigante.

## Run autonomo in Claude Code (modalita standard delle ondate)
Procedere SENZA attendere conferme: per OGNI ondata produrre i tre artefatti e committare
con `git commit -F COMMIT_MSG.txt` prima di passare alla successiva. Un commit per ondata.
NON pushare: il push lo fa l'operatore a fine run. Fermarsi SOLO se una modifica estetica
rischia di toccare logica/cablaggio: in quel caso saltare il punto, annotarlo nella nota di
rilascio e proseguire. A fine run: aggiornare `QA.md` e produrre un riepilogo unico
(file toccati per ondata, punti saltati, rischi).

## Tecnica per i file ISO-8859-1 (home.html, include/*.include)
Gli strumenti Edit/Write rifiutano o corromperebbero i file non-UTF8: editarli BYTE-LEVEL
con uno script perl (`open ':raw'`, sostituzioni con `\Q...\E` o regex, CRLF espliciti
`\r\n` nel testo inserito, contatore per verificare che OGNI sostituzione sia univoca,
`die` senza scrivere se no). Dopo l'edit verificare con `file` (deve restare "ISO-8859 ...
CRLF") e col conteggio dei byte non-ASCII. Esempi funzionanti nelle note di luglio/ottobre.

## Istanza di test in parallelo (`deploy/`)
Per collaudare la GUI accanto alla produzione si deploya un secondo contesto `/wera-test`
generato da `deploy/make_test_war.bat` (Windows) o `deploy/patch-config-test.sh` (Linux):
il `config.cfg` viene patchato NEL SOLO ARTEFATTO, mai nel repo. Override: `Quartz-scheduler.tasklist`
azzerata (kill switch: `SchedulerServletApplication.init()` esce prima di creare lo scheduler),
directory di I/O e `xmlconfig.ACS` isolati, SMTP su mailcatcher locale, datasource `jdbc/wera_test`.
Isolare i path e' obbligatorio anche a Quartz spento: `FunctionScaricoDatiManuale` riscrive
`AnlConfig.xml` da GUI. Sul SERVER di produzione MAI `-clean` (rimuoverebbe la webapp `wera`)
ne' `-clean-logs` senza `-app-log-dir` dedicato. Dettagli in `deploy/README.md` e nella nota
`.claude/2026-09-07-deploy-parallelo-test-gui.md`.

### Configurazione ATTIVA sul laptop (dal 2026-10-22, riferimento corrente)
- Tomcat 9 locale: `D:\Programmi\apache-tomcat-9.0.70`, NESSUN servizio Windows: si avvia in
  un tab del Terminal panel con `$env:JAVA_HOME='D:\Programmi\Java\openjdk8'`,
  `$env:CATALINA_HOME='D:\Programmi\apache-tomcat-9.0.70'`, poi `catalina.bat run`
  (MAI avviarlo da una chiamata Bash dell'agente: muore con la pulizia del process tree).
- Procedura (verificata): `mvn clean package` -> `deploy\make_test_war.bat` ->
  `..\scripts\deploy_war_tomcat9.bat D:\SVILUPPO\wera_git\target\wera-test.war wera-test -clean`
  (il `-clean` sul laptop e accettato dall'operatore: ha rimosso le webapp locali
  wera/openproteo; su un server condiviso resta vietato).
- DB (dal 2026-10-22 pomeriggio): **PostgreSQL 9.4 LOCALE** (servizio `postgresql-x64-9.4`,
  binari `D:\Programmi\PostgreSQL\9.4\bin`), DB `wera` ripristinato dall'operatore: e un DB
  di pasticcio, si possono distruggere i dati. Il file hosts mappa `db-wera-etc-host` su
  `127.0.0.1` (righe `192.168.0.67` e `192.168.0.182` commentate). Datasource del contesto
  test in `conf\Catalina\localhost\wera-test.xml` (FUORI repo): risorsa `jdbc/wera_test` ->
  `jdbc:postgresql://db-wera-etc-host:5432/wera`, utente `postgres` (password solo li).
- Email anonimizzate sul DB locale con `deploy/anonimizza-email-test.sql` (destinazione
  passata a riga di comando): `utenti.email` -> `<casella>+u<ID_UTENTE>@<dominio>` (unica per
  utente, serve al reset password), ogni altro indirizzo in qualunque tabella -> `<casella>`.
  `utenti.username` NON toccato (e la login). Rilanciarlo dopo ogni nuovo restore.
- Al DB locale manca la vista `vista_scarico_data_massima` (restore incompleto): Letture
  consumi e Storico letture vanno in 500 finche non viene ricreata. Assenti anche
  `v_utenze_id_condomino`, `v_utenti_aziende_profili`, `v_dipendenti` e altre minori (elenco
  in QA.md). Nessuna ripartizione UNI2018 nel DB locale. Smoke test superuser ondate 0-6
  fatto il 2026-10-22: nessuna regressione del restyle.
- `deploy/make_test_war.bat` riconfeziona con `jar.exe` del JDK (fix 2026-10-22: PowerShell
  ZipFile produceva entry con backslash e Tomcat rifiutava il WAR).
- ATTENZIONE: `D:\SVILUPPO\scripts\deploy_war_tomcat9.bat` (fuori repo) usa `wmic` nel
  wait-loop di stop; wmic non esiste su Win11 -> il loop e cieco, verificare a mano che
  Tomcat sia fermo prima della copia.
- Mailcatcher: `deploy/mailcatcher.pl` (perl puro, nessun download) su `127.0.0.1:1025`,
  accetta AUTH qualsiasi e salva ogni mail in `D:\usr\share\logs\wera\mailcatcher\*.eml`.
  Avvio in un tab del Terminal panel: `& 'D:\Programmi\Git\usr\bin\perl.exe' deploy/mailcatcher.pl`.
  Registrazione e reset password verificati end-to-end il 2026-10-22.
- Login da script (curl): prima una GET di `FUNCTIONID=Login` (crea la sessione), poi POST
  con `FUNCTIONID=Home` (NON `Login`: il form di login.html invia Home) + USERNAME/PASSWORD;
  passare le password con `--data-urlencode "PASSWORD@file"` per non scriverle nei comandi.
  Log applicativi: `D:\usr\share\logs\wera`; gli stack trace finiscono in
  `D:\usr\share\logs\wera\crash\` (cartella creata a mano il 2026-10-22: se manca, i crash
  si perdono e si vede solo la 500).
- Il vecchio DB di prova `192.168.0.67` e raggiungibile SOLO dalla LAN 192.168.0.x: da
  un'altra rete le pagine che leggono dal DB vanno in timeout (~21 s) e danno 500, con
  `Connection to db-wera-etc-host:5432 refused` nel crash log.
- Sul DB di prova/locale l'amministratore di test NON ha lo username di produzione
  (verificare in Configurazione > Utenti). La scheda utente non ha campo password: per
  entrare con un utente di test si copia sul DB l'hash di una password nota (tabella
  `password`, SHA-1 hex senza sale, `dt_scadenza` 'AAAA/MM/GG'). Credenziali solo in chat.
- Bug di sessione (Java, preesistente): aprire `FUNCTIONID=Login` con una sessione ancora
  attiva da 500 (`ClassCastException ... MenuItem` in `getPathDescription`): passare prima
  da `FUNCTIONID=Logout`.
- Credenziali di test: NON nel repo, le fornisce l'operatore in chat.
