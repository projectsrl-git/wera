# 2026-09-07 — Deploy parallelo istanza TEST GUI

## Obiettivo
Affiancare alla produzione una seconda istanza WERA (context `/wera-test`) per collaudare la
nuova GUI del branch `restyle/ui-2026`, azzerando ogni possibilità che l'istanza di test
interferisca con l'esercizio: schedulazioni Quartz, import automatico dei file rilevatori,
invio mail reali, riscrittura del file di pianificazione ACS26 e scrittura sul DB di produzione.

---

## 1. Mappa delle interferenze (da analisi codice)

### 1.1 Scheduler Quartz — il punto critico
Bootstrap in `net.projectsrl.servlet.scheduler.SchedulerServletApplication.init()`, ereditato da
`AliBOWServletApplication` (la servlet dichiarata in `web.xml`). Legge `Quartz-scheduler.tasklist`
da `config.cfg`. **Se la property è vuota fa `return` prima di `makeScheduler()`**: nessun
`SchedulerFactory`, nessun thread, nessun trigger. È il kill switch pulito.

Job attualmente in `tasklist` (config.cfg righe 3775-3793):

| Job | Cron | Cosa fa | Rischio in parallelo |
|---|---|---|---|
| `JobImportAutomatico` | ogni 5 min | Scansiona `cartella.scarico.ACS`, rinomina i file in `*_A`, li importa e li registra | **CRITICO** — ruba i file alla produzione prima che li elabori, e li importa nel DB |
| `JobMailErrori` | 12:00 giornaliero | Legge `DSSegnalazioneRilevatoriErrore`, invia mail | Mail duplicate a destinatari reali (`LISTA_MAIL` da DB) |
| `JobMailErroriRiepilogo` | lun 08:00 | Riepilogo settimanale via mail | Mail duplicate |
| `JobFileManager` | 12:00 giornaliero | Alert scadenza file centrali termiche via mail | Mail duplicate |
| `JobMailCreditiScadenza` | 08:00 giornaliero | Alert crediti in scadenza via mail | Mail duplicate |

`JobScaricoAutomaticoNotturno` è **definito ma non presente in `tasklist`**: già inattivo oggi,
lo commentiamo comunque.

I quattro job mail sono **read-only sul DB** (solo `makeDataSet` + `open`, nessuna UPDATE):
non marcano nulla come "già inviato", quindi non sopprimerebbero le mail di produzione. Il danno
sarebbe il doppio invio ad amministratori di condominio reali.
`JobImportAutomatico` invece **scrive** (import + `scriviInElencoFileImportati`) ed è distruttivo
sul filesystem (rename).

### 1.2 Connessioni FTP — non stanno nella webapp
La WAR **non apre nessuna connessione FTP/SFTP**. L'unica occorrenza di "FTP" nei sorgenti è il
letterale `<sFTP>false</sFTP>` scritto dentro `AnlConfig.xml` da
`WeraUtilsFileScaricoAnlConfig.creaFileScarico()` — è un flag di configurazione per il tool
Siemens ACS26, non una connessione dell'applicazione.

I trasferimenti SFTP reali stanno in `src/resources/bat_WERA_ACS26/*.bat`, eseguiti via WinSCP
dal **Task Scheduler di una macchina Windows esterna** (definizioni in
`bat_WERA_ACS26/Task scheuler/*.xml`):

- `upload_CSV.bat` / `upload_XML.bat` → caricano i file rilevatori in `/usr/share/webapps/wera/ACS26V3/`
- `download_ANLCONFIG.bat` → scarica `/usr/share/webapps/wera/ANLCONFIG/AnlConfig.xml`

**Questi task NON vanno spenti**: alimentano la produzione, e non sono toccati dal deploy della
seconda WAR. La protezione corretta non è spegnerli ma **isolare i path** dell'istanza di test,
in modo che non veda mai la cartella dove atterrano.

### 1.3 Scrittura di `AnlConfig.xml` raggiungibile dalla GUI
`WeraUtilsCondominiDaScaricare.condomini()` fa `new FileWriter(path)` su `xmlconfig.ACS` —
**tronca e riscrive** il file che la macchina Windows scarica per pilotare i modem.
È invocata sia dal job notturno **sia da `FunctionScaricoDatiManuale.elabora()`**, cioè da un
bottone della GUI. Con Quartz spento ma i path condivisi, un tester che clicca "scarico dati"
azzererebbe la pianificazione di produzione. Va isolato `xmlconfig.ACS`, non basta il kill switch.

### 1.4 Directory condivise
`directory.external_files=/usr/share/webapps/wera/` è un path **assoluto**, usato come base per
`cartella.upload` (`import/`), `cartella.upload.filemanager` (`upload/`) e `cartella.output`
(`output/`). Due istanze con lo stesso valore condividono le cartelle: un file caricato dalla GUI
di test finirebbe nella `import/` di produzione e verrebbe importato dal job della produzione.

### 1.5 Database
`DB.ConnectionURL=java:comp/env/jdbc/wera` (JNDI). Con la stessa risorsa, il collaudo GUI scrive
sui dati reali. Serve un datasource distinto su copia di ripristino.

---

## 2. Interventi

Tutti sul solo artefatto di deploy, **non sul repo**: `config.cfg` è in "DA NON TOCCARE" e il
branch `restyle/ui-2026` resta CSS-only.

| # | Property | Produzione | Test |
|---|---|---|---|
| 1 | `Quartz-scheduler.tasklist` | 5 job | *(vuota)* |
| 2 | `Quartz-scheduler.job.*` | attive | prefissate `#TEST-OFF#` |
| 3 | `directory.external_files` | `/usr/share/webapps/wera/` | `/usr/share/webapps/wera-test/` |
| 4 | `cartella.scarico.ACS` | `/usr/share/webapps/wera/ACS26V3/` | `/usr/share/webapps/wera-test/ACS26V3/` |
| 5 | `cartella.scarico.ACS.webapp` | `C:/ACS26V3/` | `C:/ACS26V3-TEST/` |
| 6 | `xmlconfig.ACS` | `/usr/share/webapps/wera/ANLCONFIG/AnlConfig.xml` | `/usr/share/webapps/wera-test/ANLCONFIG/AnlConfig.xml` |
| 7 | `mail.SMTPHost` + porta + tls | `smtps.aruba.it:587` STARTTLS | `127.0.0.1:1025`, tls off |
| 8 | `mail.from` / `mail.from.service` | dominio reale | `noreply@test.invalid` |
| 9 | `DB.ConnectionURL[.generic]` | `jdbc/wera` | `jdbc/wera_test` |

Nota su (7): `SendSMTPMail` ha un `ErrDetector.invariant(Util.IsNotEmpty(smtpUser))` —
`mail.SMTPHost.user` **deve restare valorizzato**, anche fittizio. Consigliato MailHog o
smtp4dev sulla 1025: le mail vengono catturate e ispezionabili invece di andare in eccezione.

---

## 3. Procedura

Strumenti in `deploy/` (dettagli operativi e avvertenze in `deploy/README.md`):
`make_test_war.bat` + `patch_config_test.ps1` per Windows, `patch-config-test.sh` per Linux.

### Windows (toolchain `deploy_wera_patch.bat`)

```bat
deploy_wera_patch.bat                    REM build senza deploy
deploy\make_test_war.bat                 REM -> target\wera-test.war

deploy_war_tomcat9.bat "D:\SVILUPPO\wera_git\target\wera-test.war" "wera-test" ^
    -catalina "D:\Programmi\apache-tomcat-9.0.70" -service Tomcat9
```

**Mai `-clean`** nel deploy del test: rimuoverebbe la webapp `wera`, che non è fra quelle
preservate. **Mai `-clean-logs`** senza `-app-log-dir` dedicato: `APP_LOG_DIR` punta a
`D:\usr\share\logs\wera`, la cartella log della produzione.

Il deploy riavvia il servizio Tomcat: si riavvia **anche** l'istanza `wera` sullo stesso
Tomcat. Da pianificare se lì gira la produzione.

I path Linux-style di `config.cfg` (`/usr/share/webapps/...`) su Windows risolvono sul drive
corrente (`D:\usr\share\...`, coerente con `APP_LOG_DIR`): restano quindi validi su entrambe
le piattaforme e non vanno riscritti in forma `D:\`.

### Linux (server di produzione)

```bash
mvn clean package
./deploy/patch-config-test.sh target/wera.war     # -> /tmp/wera-test.war
cp /tmp/wera-test.war $CATALINA_BASE/webapps/
```

### Prerequisiti comuni

1. Restore del DB di produzione su `WERA_TEST`
2. Datasource in `conf/Catalina/localhost/wera-test.xml`:
   ```xml
   <Context docBase="wera-test" path="/wera-test">
     <Resource name="jdbc/wera_test" auth="Container" type="javax.sql.DataSource"
               driverClassName="net.sourceforge.jtds.jdbc.Driver"
               url="jdbc:jtds:sqlserver://HOST:1433/WERA_TEST"
               username="..." password="..."
               maxTotal="10" maxIdle="3"/>
   </Context>
   ```
3. Mailcatcher su `127.0.0.1:1025` (MailHog, smtp4dev, `python -m aiosmtpd -n -l 127.0.0.1:1025`)

Contesto separato ⇒ cookie `JSESSIONID` con `path=/wera-test`: nessuna collisione di sessione
con la produzione. `session-timeout=0` in `web.xml` (sessione infinita) è invariato: in test è
scomodo ma innocuo.

---

## 4. Verifiche post-deploy

Windows (PowerShell):

```powershell
# 1. Nessun thread Quartz nella JVM
jcmd <PID> Thread.print | Select-String -Pattern quartz    # atteso: nessun output

# 2. AnlConfig di produzione intatto
Get-FileHash D:\usr\share\webapps\wera\ANLCONFIG\AnlConfig.xml
# ripetere dopo aver premuto "scarico dati" nella GUI di test: hash invariato

# 3. Nessun file rubato alla produzione
Get-ChildItem D:\usr\share\webapps\wera\ACS26V3\ | Select-Object Name, LastWriteTime
# dopo 15 min di uso della GUI di test: nessun file con suffisso _A

# 4. Nessuna connessione SMTP in uscita
Get-NetTCPConnection -State Established | Where-Object { $_.RemotePort -in 25,465,587 }
```

Linux:

```bash
jcmd <PID> Thread.print | grep -i quartz            # atteso: vuoto
lsof -p <PID> | grep '/usr/share/webapps/wera/'     # atteso: vuoto
md5sum /usr/share/webapps/wera/ANLCONFIG/AnlConfig.xml
ls -la --time-style=full-iso /usr/share/webapps/wera/ACS26V3/ | head
ss -tnp | grep -E ':(25|465|587)'
```

Smoke test GUI come da `QA.md`: login, home, una `ricerca*`, una `inserimento*`,
`inserimentoripartizioni` (tab Costi per unità), configurazione guidata, profilo.

---

## 5. Rischi / punti aperti

- **NPE attesa in undeploy.** `SchedulerServletApplication.destroy()` chiama `_scheduler.shutdown()`
  senza null-check; con `tasklist` vuota `_scheduler` resta `null` e la `catch` intercetta solo
  `SchedulerException`. All'arresto/undeploy dell'istanza di test comparirà un
  `NullPointerException` nel log di Tomcat. **È innocuo e atteso** — non è un fallimento di deploy.
  Il fix (`if (_scheduler != null)`) tocca codice Java: fuori scope restyling, da valutare a parte.
- **`-clean` sulla produzione cancella il test.** `deploy_war_tomcat9.bat -clean` preserva solo
  `docs`, `examples`, `host-manager`, `manager`, `ROOT`: il prossimo deploy della produzione con
  quell'opzione rimuoverà `wera-test`. Comportamento corretto, va solo saputo.
- **Riavvio condiviso.** Deploy del test e deploy della produzione riavviano lo stesso servizio
  Tomcat: le due istanze non sono indipendenti quanto a disponibilità, solo quanto a dati.
- **`git_push.bat` / `git_setup.bat`** in root del repo non sono coinvolti nel runtime, ma vanno
  esclusi da qualsiasi automazione sul server.
- **Segreti nel repo (bloccante, già noto in `CLAUDE.md`).** Ho verificato: oltre alle credenziali
  SMTP in chiaro in `config.cfg`, i tre `.bat` sotto `src/resources/bat_WERA_ACS26/` contengono in
  chiaro **utenza root, password e host-key SSH del server 192.168.0.61**, in un repo pubblico.
  Sono credenziali di accesso completo alla macchina che ospita la produzione. Da trattare come
  compromesse: rendere privato il repo, **ruotare la password root e la chiave**, spostare le
  credenziali in un profilo WinSCP protetto o in variabili d'ambiente, poi ripulire la history con
  `git filter-repo`. Non è parte del deploy di test ma è più urgente del deploy stesso.
