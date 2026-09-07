# deploy/ — istanza di test in parallelo

Strumenti per affiancare alla produzione una seconda istanza WERA (`/wera-test`)
su cui collaudare la GUI, senza che possa interferire con l'esercizio.

Analisi completa delle interferenze e verifiche post-deploy:
[`.claude/2026-09-07-deploy-parallelo-test-gui.md`](../.claude/2026-09-07-deploy-parallelo-test-gui.md).

| File | Piattaforma | Cosa fa |
|---|---|---|
| `make_test_war.bat` | Windows | Esplode `target/wera.war`, applica gli override, riconfeziona `wera-test.war` |
| `patch_config_test.ps1` | Windows | Motore di patch del `config.cfg` (invocato dal `.bat`, usabile a mano) |
| `patch-config-test.sh` | Linux | Equivalente per il server di produzione |

Nessuno dei tre tocca il repo: lavorano su una copia esplosa del WAR.
`config.cfg`, codice Java, SQL e template FreeMarker restano invariati.

---

## Cosa viene neutralizzato

| Property | Produzione | Test |
|---|---|---|
| `Quartz-scheduler.tasklist` | 5 job attivi | *(vuota)* |
| `Quartz-scheduler.job.*` | attive | prefissate `#TEST-OFF#` |
| `directory.external_files` | `/usr/share/webapps/wera/` | `/usr/share/webapps/wera-test/` |
| `cartella.scarico.ACS` | `.../wera/ACS26V3/` | `.../wera-test/ACS26V3/` |
| `cartella.scarico.ACS.webapp` | `C:/ACS26V3/` | `C:/ACS26V3-TEST/` |
| `xmlconfig.ACS` | `.../wera/ANLCONFIG/AnlConfig.xml` | `.../wera-test/ANLCONFIG/AnlConfig.xml` |
| `mail.SMTPHost` (+ porta, TLS) | `smtps.aruba.it:587` STARTTLS | `127.0.0.1:1025`, TLS off |
| `mail.from`, `mail.from.service` | dominio reale | `noreply@test.invalid` |
| `DB.ConnectionURL[.generic]` | `jdbc/wera` | `jdbc/wera_test` |

Lo script **fallisce** (exit 2) se una di queste chiavi non esiste più in `config.cfg`,
e (exit 3) se dopo la patch resta un riferimento attivo a `/usr/share/webapps/wera/`.
Meglio un build che si ferma di un WAR di test che scrive in produzione.

---

## Procedura (Windows)

```bat
REM 1. build normale, senza deploy
deploy_wera_patch.bat

REM 2. genera il WAR di test
deploy\make_test_war.bat

REM 3. deploy come contesto separato
deploy_war_tomcat9.bat "D:\SVILUPPO\wera_git\target\wera-test.war" "wera-test" ^
    -catalina "D:\Programmi\apache-tomcat-9.0.70" -service Tomcat9
```

### Due avvertenze sul passo 3

**Mai `-clean`.** In `deploy_war_tomcat9.bat` quell'opzione rimuove le webapp non standard
preservando solo `docs`, `examples`, `host-manager`, `manager`, `ROOT`: cancellerebbe la
webapp `wera`. Per lo stesso motivo, i deploy successivi *della produzione* con `-clean`
cancelleranno `wera-test` — comportamento corretto, basta saperlo.

**Mai `-clean-logs` senza `-app-log-dir` dedicato.** `APP_LOG_DIR` in `deploy_wera_patch.bat`
punta a `D:\usr\share\logs\wera`, che è la cartella log della produzione. Deployare il test
con l'azzeramento log attivo la svuoterebbe.

Il deploy riavvia il servizio Tomcat, quindi **anche l'istanza `wera` si riavvia**. Se
sullo stesso Tomcat gira la produzione, va pianificato fuori orario; se è solo la macchina di
sviluppo, è irrilevante.

---

## Prerequisiti

**Datasource** in `conf\Catalina\localhost\wera-test.xml`:

```xml
<Context docBase="wera-test" path="/wera-test">
  <Resource name="jdbc/wera_test" auth="Container" type="javax.sql.DataSource"
            driverClassName="net.sourceforge.jtds.jdbc.Driver"
            url="jdbc:jtds:sqlserver://HOST:1433/WERA_TEST"
            username="..." password="..."
            maxTotal="10" maxIdle="3"/>
</Context>
```

Il DB `WERA_TEST` va creato da restore di produzione. Con un datasource che punta al DB reale,
tutto il resto della protezione non serve a niente.

**Mailcatcher** su `127.0.0.1:1025` (MailHog, smtp4dev, o `python -m aiosmtpd -n -l 127.0.0.1:1025`).
Serve perché `mail.SMTPHost.user` non può essere svuotato: `SendSMTPMail` ha un
`ErrDetector.invariant(Util.IsNotEmpty(smtpUser))`. Senza nulla in ascolto le mail non partono
comunque, ma si riempiono i log di eccezioni di connessione.

---

## Nota attesa nei log

All'arresto o all'undeploy dell'istanza di test comparirà un `NullPointerException`:

```
net.projectsrl.servlet.scheduler.SchedulerServletApplication.destroy()
```

`destroy()` chiama `_scheduler.shutdown()` senza null-check, e con la tasklist vuota
`_scheduler` non viene mai istanziato; la `catch` intercetta solo `SchedulerException`.
**È atteso e innocuo**, non è un deploy fallito. Il fix (`if (_scheduler != null)`) tocca
codice Java ed è fuori dallo scope del restyling.
