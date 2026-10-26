# CLAUDE.md — WERA (contesto di progetto per Claude)

## Cos'e
WERA / WebCredit WERA: web app Java per contabilizzazione calore e ripartizione spese in condominio.
Framework proprietario ALI BOW/astro (config-driven), template FreeMarker, UI Bootstrap 3 + tema INSPINIA.
Stack: bytecode Java 8 (`<release>8</release>`, build con JDK 25 o 8, gira su Java 8 e su
Java 25), servlet, FreeMarker, Maven (>= 3.6.3) -> WAR, Tomcat 9.
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
  7. home: card "I tuoi crediti" NASCOSTA su decisione dell'operatore (2026-10-23): classe
     `wera-crediti-off` sulla riga Richieste in home.html + regole in fondo alla sez. 29 del
     tema; markup/JS intatti. Per riattivarla togliere la classe. Cache-buster tema `?20261026` (dal 2026-10-26).
  8. landing: le statistiche dello slide 1 di `login.html` ("Ripartitori gestiti",
     "Condomini attivi") sono TESTO FISSO, allineato a mano il 2026-10-25 ai valori della home del
     superuser in PROD (4.838 / 54; prima c'erano i numeri del mockup). Vanno riaggiornate a mano:
     renderle dinamiche richiede un dataset pubblico o un dato passato dal Java (DA NON TOCCARE).
  TUTTO il percorso pubblico (login/registrazione/reset/newpassword) usa `wera-landing.css`;
  `wera-theme.css` (sez. 1-29) resta per l'app interna.
- **ESCLUSO su decisione (fase 2)**: slide 5 "reward"/promo coupon della landing — il LEGGIMI
  del mockup la dichiara testo promozionale senza logica; riaggiungerla SOLO su indicazione
  esplicita dell'operatore (markup pronto nel mockup).
- **Centrale termica wera2026 (in corso dal 2026-10-25)**: gestione documentale NUOVA e parallela a
  quella di /wera, nello schema PostgreSQL dedicato **`centrale_termica`** (tabelle `sezioni`,
  `sezioni_profili`, `documenti`, `versioni`, `tipi_scadenza`, `scadenze`, `parametri`, `categorie_wera`,
  `eventi`; viste `v_documenti_wera`, `v_documenti`). DDL `src/resources/sql/wera2026/001-centrale_termica-ddl.sql`
  (idempotente, 9.4): sul DB locale e, dal 2026-10-25 22:55 (ok operatore), su .75 PROD (`public` invariato,
  verificato con pg_dump). IN ESERCIZIO su .106 dal 2026-10-25 23:39 (Jenkins,
  backup del WAR precedente in `/root/wera2026.war.bak-20261025-0900-fc8da71`); piano e registro in
  `.claude/2026-10-25-piano-produzione-centrale-termica.md`. Disegno approvato in
  `.claude/2026-10-25-disegno-db-centrale-termica.md`. Mai nomi `ct26_`, mai `PARAMETRI` di /wera.
  Backend (ondata 1b, 2026-10-25, solo laptop): pacchetto `net.projectsrl.wera.centraletermica`, FUNCTIONID
  `CentraleTermicaDati` (JSON), `CentraleTermicaUpload` (POST multipart, FUNCTIONID NELLA URL),
  `CentraleTermicaDownload`; dataset `DS.DSCentraleTermica*` in fondo a config.cfg. Interfaccia e QA in
  `.claude/2026-10-25-centrale-termica-java-upload-download.md`. Pagina (ondata 2, 2026-10-25, solo laptop):
  `FUNCTIONID=CentraleTermica` (centraletermica.html ASCII, js/wera-centrale-termica.js, tema sez. 30) e
  `CentraleTermicaStato`; voce di menu da campi di sessione CT_* (included_menu.include). Nota
  `.claude/2026-10-25-centrale-termica-pagina.md`. Scadenze e configurazione (ondata 3):
  `CentraleTermicaScadenza`, `CentraleTermicaConfigurazione[Salva]` (solo CONFIGURAZIONE.PROFILI, default ADM),
  nota `.claude/2026-10-25-centrale-termica-scadenze-configurazione.md`. Scritture con NULL: usare la
  classe `Sql` (il DAO del framework manda i null come varchar).
  Ondata 4 (2026-10-26; DDL 002 su .75 alle 07:11, IN ESERCIZIO su .106 dalle 08:55, Jenkins #11 lanciato con
  `deploy/jenkins-wera2026.ps1 -Deploy`, backup del WAR precedente `/root/wera2026.war.bak-20261026-0854-ondata3-baa96d2c`): menu "Centrale termica" con sottovoci (Panoramica sul mockup di
  Andrea, Scadenze e documenti, Calendario, Alerting e storico invii, Configurazione), DDL `002-...`, motore
  avvisi (`CentraleTermicaAvvisiInvio`, cron `/etc/cron.d/wera2026-avvisi` su .106 alle 06:15 con ALERT.TOKEN;
  in PROD `ALERT.ATTIVO=N` dal 2026-10-26 su decisione dell'operatore, FINCHE' l'SMTP del profilo prod resta il
  mailcatcher: il motore registrerebbe `INVIATO` per mail mai uscite e quelle soglie non verrebbero piu'
  rimandate; riaccendere con `S` insieme all'SMTP vero); nota
  `.claude/2026-10-26-centrale-termica-menu-panoramica-scadenziario.md`.
  Dati reali (2026-10-26, solo via applicazione): sezione `CONTRATTI` (id 9) + 56 contratti firmati su 31
  condomini + 9 scadenze TR in PROD; 22 file in attesa (condomini assenti in PROD). Gli ID condominio
  della copia locale del DB NON coincidono con PROD. Nota
  `.claude/2026-10-26-centrale-termica-caricamento-documenti-andrea.md`.
- **Registrazione amministratore wera2026 (2026-10-26; pushata e su `release/wera2026` a `fca8d07`; schema
  creato su .75 alle 13:13 con l'ok dell'operatore, `MAIL.ATTIVA=N` e `REGISTRAZIONE.ATTIVA=N`, limite invii
  30/giorno per tutto il sito perche' dietro il NAT tutti i client sono 192.168.50.253; IN ESERCIZIO su .106 dalle 13:43, Jenkins #16, modulo pubblico ANCORA CHIUSO)**: schema PostgreSQL nuovo
  **`registrazioni`** (DDL `src/resources/sql/wera2026/003-registrazioni-ddl.sql`: `richieste`, `allegati`,
  `parametri`, `eventi`, vista `v_richieste`), pacchetto Java `net.projectsrl.wera.registrazioni` (nessun
  dataset in config.cfg: query parametriche). Flusso approvato dall'operatore: il form pubblico
  `FUNCTIONID=RegistrazioneAmministratore` (wera-landing.css) crea una RICHIESTA con la password scelta (solo
  hash); l'ADM la approva in `RegistrazioniRichieste` (voce "Registrazioni" nel menu di wera2026, contatore
  `REG_IN_ATTESA`) e solo allora nascono le righe in utenti/utenti_lingue/utenti_profili/utenti_aziende/password
  (profilo AMM, azienda da `ATTIVAZIONE.AZIENDA_CODICE`). I condomini si collegano dall'anagrafica condominio.
  Mail governate da `MAIL.ATTIVA` (in PROD `N` finche' l'SMTP e' il mailcatcher). Disegno
  `.claude/2026-10-26-disegno-registrazione-amministratore.md`, note ondate 1-3 dello stesso giorno.
  Deploy solo con ok dell'operatore. Piano di produzione e registro:
  `.claude/2026-10-26-piano-produzione-registrazione-amministratore.md` (niente eseguito). CTA amministratore della landing ricollegate.
- **In attesa di backend/dataset** (non fattibili senza toccare config.cfg/Java):
  "Andamento letture mensili (HCA)" in home (serve un DS MESE/VALORE in config.cfg),
  "Consumi per condominio", "Previsione consumi", "Scadenziario Centrale termica",
  registrazione installatore (lo schema `registrazioni` e' gia' pronto per il tipo INSTALLATORE),
  "spesa in euro" inquilino/amministratore.
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
  (Eccezione autorizzata dall'operatore il 2026-10-24: `pom.xml` e `net/project/misc/hash.java`
  per la build con JDK 25, vedi `.claude/2026-10-24-build-jdk25.md`.)
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
Build (dal 2026-10-24): `mvn clean package` con **Maven >= 3.6.3** (il compiler plugin 3.13.0
rifiuta Maven 3.3.9) e JDK 25 o 8. Dal Bash tool:
`JAVA_HOME='D:\Programmi\Java\zulu25.36.205-ca-jdk25.0.4.1-win_x64' D:\SVILUPPO\apache-maven-3.9.12\bin\mvn.cmd clean package -DskipTests`
(funziona anche con `JAVA_HOME=D:\Programmi\Java\openjdk8`). WAR in `target\wera.war`.
Nel pom: `<release>8</release>` (bytecode 52, gira su Java 8 e 25), `<encoding>Cp1252</encoding>`
(sorgenti ISO-8859-1/Cp1252: da Java 18 javac leggerebbe UTF-8 e fallirebbe), JAXB 2.3.x
(`jaxb-api` + `jaxb-runtime`, namespace javax), un'unica `javax.activation`
(`com.sun.activation:jakarta.activation` 1.2.2) e `xercesImpl` 2.2.1 ESCLUSO da fop (rompeva
l'unmarshal JAXB, cioe l'upload dati rilevatori XML, anche su Java 8). Avvisi attesi:
"source/target value 8 is obsolete".
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

## Istanza in parallelo a wera: `/wera2026` (`deploy/`)
Dal 2026-10-23 la nuova interfaccia si affianca a `/wera` come contesto **`/wera2026`**
(prima `/wera-test`), in due PROFILI generati da `deploy/make_wera2026.bat`
(-> `target\test\wera2026.war` e `target\prod\wera2026.war`) o da
`deploy/patch-config-test.sh <war> wera2026 <test|prod>` (Linux): il `config.cfg` viene
patchato NEL SOLO ARTEFATTO, mai nel repo.
- **test** (server di test, copia del DB): `Quartz-scheduler.tasklist` azzerata (kill switch:
  `SchedulerServletApplication.init()` esce prima di creare lo scheduler), directory di I/O e
  `xmlconfig.ACS` isolati in `/usr/share/webapps/wera2026/`, SMTP su mailcatcher locale,
  datasource `jdbc/wera2026` (descrittore `conf/Catalina/localhost/wera2026.xml`), log in
  `/usr/share/logs/wera2026/`. Isolare i path e' obbligatorio anche a Quartz spento:
  `FunctionScaricoDatiManuale` riscrive `AnlConfig.xml` da GUI.
- **prod** (server di produzione, dati veri): stesso DB (`jdbc/wera` dal META-INF/context.xml,
  NESSUN descrittore), cartelle di wera condivise (modifiche e upload reali); Quartz SPENTO
  (job solo su wera), NESSUNA mail reale (SMTP su mailcatcher `127.0.0.1:1025`, decisione
  operatore 2026-10-24) e log in `/usr/share/logs/wera2026/`.
- **Server di produzione di wera2026: 192.168.0.106 (`webcont92`)**, Debian 11, Tomcat 9
  Debian (CATALINA_BASE `/var/lib/tomcat9`, sandbox systemd `ProtectSystem=strict`, connettori
  80/443) condiviso con `gestit` (compilato per Java 25: la JVM del Tomcat non puo scendere a
  Java 8). Accesso root con il client OpenSSH di Windows (chiave nell'ssh-agent; l'ssh di
  Git Bash non puo sbloccare `id_ed25519`, che ha passphrase).
  **STATO (2026-10-25): `/wera2026` IN ESERCIZIO su .106, versione `fc8da71`** (build JDK 25
  release 8 con JAXB e senza xerces, deploy Jenkins #6; prima, dal 2026-10-24, `213a552`).
  **URL pubblico: https://weraweb.it/wera2026/** tramite l'Apache di frontend 192.168.0.50
  (`webfe01`), vhost `/etc/apache2/sites-available/ssl.weraweb.it.conf` (2026-10-25, backup
  `.bak-20261025-092510`): `ProxyPass` dal piu specifico al piu generico — `/wera2026` ->
  `http://192.168.0.106/wera2026`, `/weralegacy` -> `.61:8080`, `/wera` -> `.70:8080` — e
  `RedirectMatch 302 "^/$" "/wera/"` al posto della vecchia regola `ProxyPass "/"` (scritta senza
  barra finale coerente). Rimossa `/newgen`. Reload con `systemctl reload apache2` dopo
  `apache2ctl configtest`.
  **Rete**: il FortiGate 192.168.0.253 (FGT60F-PAVIA) lascia uscire la LAN su Internet solo
  dalle 06:00 alle 23:00 (policy 39 "Outgoing LAN 0.x", schedule "VPN h6-23"): build Jenkins
  che scaricano da GitHub/Maven vanno lanciate in quella fascia. Proposta di policy H24 per
  .115 (NON applicata) nelle note della sessione di diagnosi. Profilo prod
  senza mail), verificato: SMTP `127.0.0.1:1025`, Quartz vuoto, `jdbc/wera` -> .75 (dal 2026-10-25; prima .67, che e weralegacy), cartelle
  `/usr/share/webapps/wera` (dal 2026-10-25 la share di wera su .81, mount annidato) e log `/usr/share/logs/wera2026/` sulle share CIFS, HTTP 200, nessun errore all'avvio su
  JVM **Zulu 25** (`/usr/lib/jvm/zulu25`). Setup del server fatto da una sessione separata:
  `/etc/hosts` (`db-wera-etc-host`=.75 dal 2026-10-25, prima .67; `fileserver_logs`=.185, `fileserver_docs`=.186), mount
  CIFS `//fileserver_logs/storage/logs` -> `/usr/share/logs` e `//fileserver_docs/storage/webapps`
  -> `/usr/share/webapps`, override systemd di tomcat9 (`ReadWritePaths` su
  `/usr/share/logs/wera2026` e `/usr/share/webapps/wera`, `RequiresMountsFor`), mailcatcher perl
  come servizio su 127.0.0.1:1025 (mail in `/usr/share/logs/wera2026/mailcatcher/`).
  Dal 2026-10-25 `/usr/share/webapps/wera` e un mount CIFS annidato
  `//fileserver_wera_docs/storage/webapps/wera` (hosts: `fileserver_wera_docs`=192.168.0.81, la
  share di /wera su .70; credenziali uguali, `vers=3.0`) e tomcat9 lo richiede
  (`RequiresMountsFor`, scelta operatore: gestit su .106 e un ambiente di test, wera2026 la
  produzione pilota): senza .81 Tomcat non parte. Rollback: togliere PRIMA la dipendenza
  dall'override (daemon-reload) e POI smontare, altrimenti si ferma Tomcat. Dettagli in
  `.claude/2026-10-25-piano-mount-share-wera-81.md`.
  **Deploy**: job Jenkins (192.168.0.115) "wera2026 PROD", parametro BRANCH (default
  `release/wera2026`, branch di rilascio FISSA dal 2026-10-26: si rilascia facendo merge/fast-forward
  li sopra e si lancia il job senza cambiarlo; default impostato il 2026-10-26, vedi `deploy/README.md`), build con la JDK di sistema Temurin 25 e Maven 3.9.16 (dal
  2026-10-24 nessun tool JDK: tolti `<jdk>JDK8</jdk>` dal job e il tool JDK8 dal config
  globale, `temurin-8-jdk` disinstallato; backup `config.xml.bak-20261024-*`), patch
  `deploy/patch-config-test.sh ... wera2026 prod`, DEPLOY=true fa il BACKUP della versione in
  esercizio e poi copia il WAR patchato in `/var/lib/tomcat9/webapps/` (deploy a caldo, gestit
  non si ferma). **ROLLBACK** (dal 2026-10-26, alternativo a DEPLOY, anche
  `deploy/jenkins-wera2026.ps1 -Rollback`): nessuna build ne' accesso a Internet, rimette la
  versione in esercizio prima dell'attuale (ripetuto risale). Backup in
  `/var/backups/deploy-war/wera2026.war.bak-AAAAMMGG-HHMMSS-<commit>` (ultimi 8, elenco con
  `ssh deploy@192.168.0.106 "wera2026 list"`): i backup manuali in `/root` non servono piu'. Il
  checkout Git e' un passo shell del job (niente sezione SCM), cosi il rollback gira a qualsiasi ora.
  Lasciare DEPLOY=false di default: il 2026-10-24 un lancio con DEPLOY ha messo in esercizio una
  versione con SMTP reale.
  Sui SERVER condivisi MAI `-clean` (rimuoverebbe `wera` o, su .106, `gestit`)
ne' `-clean-logs` senza `-app-log-dir` dedicato. Dettagli in `deploy/README.md` e nella nota
`.claude/2026-09-07-deploy-parallelo-test-gui.md`.

### Configurazione ATTIVA sul laptop (dal 2026-10-22, riferimento corrente)
- Tomcat 9 locale: `D:\Programmi\apache-tomcat-9.0.70`, NESSUN servizio Windows: si avvia in
  un tab del Terminal panel con `$env:JAVA_HOME='D:\Programmi\Java\openjdk8'`,
  `$env:CATALINA_HOME='D:\Programmi\apache-tomcat-9.0.70'`, poi `catalina.bat run`
  (MAI avviarlo da una chiamata Bash dell'agente: muore con la pulizia del process tree).
- Sul laptop gira **`/wera2026` profilo test** (dal 2026-10-23, al posto di `/wera-test`):
  http://localhost:8080/wera2026/. Procedura: `mvn clean package` -> `deploy\make_wera2026.bat`
  -> Tomcat fermo -> copiare `target\test\wera2026.war` in `webapps` (togliendo
  `webapps\wera2026[.war]` e `work\Catalina\localhost\wera2026`) -> avvio nel Terminal panel.
  Lo script `..\scripts\deploy_war_tomcat9.bat <war> wera2026 -clean` funziona ma avvia
  Tomcat da solo (dall'agente muore): il `-clean` sul laptop e accettato dall'operatore, su
  un server condiviso resta vietato. Il vecchio descrittore e' `wera-test.xml.disabled`.
- DB (dal 2026-10-22 pomeriggio): **PostgreSQL 9.4 LOCALE** (servizio `postgresql-x64-9.4`,
  binari `D:\Programmi\PostgreSQL\9.4\bin`), DB `wera` ripristinato dall'operatore: e un DB
  di pasticcio, si possono distruggere i dati. Il file hosts mappa `db-wera-etc-host` su
  `127.0.0.1` (righe `192.168.0.67` e `192.168.0.182` commentate). Datasource del contesto
  in `conf\Catalina\localhost\wera2026.xml` (FUORI repo): risorsa `jdbc/wera2026` ->
  `jdbc:postgresql://db-wera-etc-host:5432/wera`, utente `postgres` (password solo li).
- Email anonimizzate sul DB locale con `deploy/anonimizza-email-test.sql` (destinazione
  passata a riga di comando): `utenti.email` -> `<casella>+u<ID_UTENTE>@<dominio>` (unica per
  utente, serve al reset password), ogni altro indirizzo in qualunque tabella -> `<casella>`.
  `utenti.username` NON toccato (e la login). Rilanciarlo dopo ogni nuovo restore.
  Gira SOLO su un DB marcato `COMMENT ON DATABASE <db> IS 'WERA-TEST: ...'` (il nome non
  basta: anche la produzione si chiama `wera`). Il DB locale del laptop e marcato dal
  2026-10-23; dopo un nuovo restore va rimarcato a mano. MAI marcare la produzione.
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
  Log applicativi di /wera2026 (`Logger.*` isolati in entrambi i profili):
  `D:\usr\share\logs\wera2026`, stack trace in `D:\usr\share\logs\wera2026\crash\`
  (creata da make_test_war.bat: se manca, i crash si perdono e si vede solo la 500).
  `D:\usr\share\logs\wera` resta la cartella dei log di produzione.
- **DB di PRODUZIONE (chiarito dall'operatore il 2026-10-25, verificato sul file hosts di .70):**
  `/wera` PROD (192.168.0.70, `webcont06`, Tomcat 7/Java 7) usa **192.168.0.75**;
  `/weralegacy` (PROD legacy tenuta per lo storico, .61) usa **192.168.0.67**; `/wera2026` su
  .106 usa .75 dal 2026-10-25 (prima, per errore, .67). Mai puntare il laptop su .75 o .67 per
  collaudi che scrivono. Le cartelle di /wera (`ACS26V3`, `ANLCONFIG`, `import`, `upload`) stanno
  sulla share di 192.168.0.81 (`fileserver_docs` di .70), montata anche su .106 (vedi sopra).
  I DB sono raggiungibili solo dalla LAN 192.168.0.x (o VPN): da un'altra
  rete le pagine vanno in timeout (~21 s) e danno 500.
- Sul DB locale l'amministratore di test NON ha lo username dell'ambiente vecchio
  (verificare in Configurazione > Utenti). La scheda utente non ha campo password: per
  entrare con un utente di test si copia sul DB l'hash di una password nota (tabella
  `password`, SHA-1 hex senza sale, `dt_scadenza` 'AAAA/MM/GG'). Credenziali solo in chat.
- Bug di sessione (Java, preesistente): aprire `FUNCTIONID=Login` con una sessione ancora
  attiva da 500 (`ClassCastException ... MenuItem` in `getPathDescription`): passare prima
  da `FUNCTIONID=Logout`.
- Credenziali di test: NON nel repo, le fornisce l'operatore in chat.
- Hash password (preesistente, non toccare): `WebAppUtils.encryptSHA1` scrive ogni byte con
  `Integer.toHexString` e PERDE gli zeri iniziali (38-39 caratteri, non lo SHA-1 standard): per un utente
  di test calcolare l'hash allo stesso modo (es. perl `sprintf("%x")` per byte).
