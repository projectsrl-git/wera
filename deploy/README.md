# deploy/ — istanze in parallelo a wera (wera2026)

Strumenti per affiancare alla produzione `/wera` una seconda istanza WERA con la nuova
interfaccia, oggi **`/wera2026`**, senza che i due contesti si disturbino. Il nome del
contesto è un parametro (`wera-test` resta il default storico).

Analisi completa delle interferenze e verifiche post-deploy:
[`.claude/2026-09-07-deploy-parallelo-test-gui.md`](../.claude/2026-09-07-deploy-parallelo-test-gui.md).

| File | Piattaforma | Cosa fa |
|---|---|---|
| `make_wera2026.bat` | Windows | Produce in un colpo `target\test\wera2026.war` e `target\prod\wera2026.war` |
| `make_test_war.bat` | Windows | `[war] [contesto] [test\|prod]`: esplode `target/wera.war`, applica gli override del profilo, riconfeziona `target\<profilo>\<contesto>.war` |
| `patch_config_test.ps1` | Windows | Motore di patch del `config.cfg` (invocato dal `.bat`, usabile a mano) |
| `patch-config-test.sh` | Linux | Equivalente: `./patch-config-test.sh target/wera.war wera2026 prod` -> `/tmp/prod/wera2026.war` |
| `anonimizza-email-test.sql` | PostgreSQL | Riscrive gli indirizzi email di una COPIA del DB (solo profilo test) |
| `mailcatcher.pl` | perl | SMTP locale su 127.0.0.1:1025 che salva le mail come `.eml` (entrambi i profili) |

Nessuno strumento tocca il repo: lavorano su una copia esplosa del WAR.
`config.cfg`, codice Java, SQL e template FreeMarker restano invariati.

---

## I due profili

**test** — server di test, istanza isolata su una **copia** del DB.
**prod** — server di produzione, nuova interfaccia **sui dati veri** accanto a `wera`:
stesso DB e cartelle condivise, così gli utenti modificano dati e caricano file davvero; i job
pianificati restano solo su `wera` e **nessuna mail reale** parte da `wera2026` (SMTP sul
mailcatcher locale, decisione dell'operatore del 2026-10-24).

| Property | `wera` (produzione) | `wera2026` profilo **test** | `wera2026` profilo **prod** |
|---|---|---|---|
| `Quartz-scheduler.tasklist` | 5 job attivi | *(vuota)* | *(vuota)* |
| `Quartz-scheduler.job.*` | attive | prefissate `#TEST-OFF#` | prefissate `#PROD-OFF#` |
| `Logger.CrashDir` / `DebugFile` / `ApplicationLog` | `/usr/share/logs/wera/...` | `/usr/share/logs/wera2026/...` | `/usr/share/logs/wera2026/...` |
| `directory.external_files` | `/usr/share/webapps/wera/` | `/usr/share/webapps/wera2026/` | invariato (condiviso) |
| `cartella.scarico.ACS` | `.../wera/ACS26V3/` | `.../wera2026/ACS26V3/` | invariato (condiviso) |
| `cartella.scarico.ACS.webapp` | `C:/ACS26V3/` | `C:/ACS26V3-TEST/` | invariato |
| `xmlconfig.ACS` | `.../wera/ANLCONFIG/AnlConfig.xml` | `.../wera2026/ANLCONFIG/AnlConfig.xml` | invariato (condiviso) |
| `mail.SMTPHost` (+ porta, TLS) | `smtps.aruba.it:587` STARTTLS | `127.0.0.1:1025`, TLS off | `127.0.0.1:1025`, TLS off |
| `mail.from`, `mail.from.service` | dominio reale | `noreply@test.invalid` | `noreply@test.invalid` |
| `DB.ConnectionURL[.generic]` | `jdbc/wera` | `jdbc/wera2026` | invariato (`jdbc/wera`) |

Perché Quartz è spento anche in prod: i job (import automatico ogni 5 minuti, mail errori,
riepilogo, scadenze file e crediti) girano già su `wera`. Accesi anche su `wera2026`
rubarebbero i file all'import e manderebbero ogni mail due volte.

Lo script **fallisce** (exit 2) se una chiave da patchare non esiste più in `config.cfg`, e
(exit 3) se dopo la patch la tasklist non è vuota o resta un riferimento attivo a cio che va
isolato: `/usr/share/(webapps|logs)/wera/` nel profilo test, `/usr/share/logs/wera/` nel prod.
Il contesto `wera` è rifiutato. I fine riga del `config.cfg` (CRLF) sono conservati: il file
patchato differisce dall'originale solo nelle righe cambiate.

---

## Rilascio in produzione di wera2026 (dal 2026-10-26)

Branch di rilascio FISSA: **`release/wera2026`** su GitHub. Il job Jenkins "wera2026 PROD" (192.168.0.115)
builda sempre quella (parametro `BRANCH`, default `release/wera2026`): per rilasciare non si tocca il job.
STATO 2026-10-26: branch creata (a `4320eeb`); il default del parametro nel job va impostato dalla UI di
Jenkins (Configura > BRANCH > `release/wera2026` > Salva, nessun riavvio). Finche' non e' fatto: lanciare con
`BRANCH=release/wera2026`.

1. Sviluppo e collaudo sul branch di lavoro (oggi `restyle/ui-2026-r2`), DDL eventuali prima in locale.
2. Rilascio = portare il lavoro su `release/wera2026` e pusharla:
   - se il branch di lavoro contiene tutta la release (caso normale, fast-forward):
     `git fetch origin && git push origin restyle/ui-2026-r2:release/wera2026`
   - altrimenti merge esplicito: `git switch release/wera2026 && git merge --no-ff <branch> && git push`
   - mai `push --force` su `release/wera2026`; un fix urgente: branch da `release/wera2026`, merge li e poi
     riporto sul branch di lavoro.
3. DDL su .75 PRIMA del deploy, quando la release ne ha (vedi il piano di produzione nelle note `.claude`).
4. Jenkins "wera2026 PROD": `DEPLOY=false` (controllo dell'artefatto), poi `DEPLOY=true`.
   Il WAR precedente resta su .106 in `/var/backups/deploy-war/wera2026.war.prev` (rollback).

## Procedura (Windows)

```bat
REM 1. build normale, senza deploy
deploy_wera_patch.bat

REM 2. genera i due WAR: target\test\wera2026.war e target\prod\wera2026.war
deploy\make_wera2026.bat

REM 3a. server di TEST
deploy_war_tomcat9.bat "D:\SVILUPPO\wera_git\target\test\wera2026.war" "wera2026" ^
    -catalina "D:\Programmi\apache-tomcat-9.0.70" -service Tomcat9

REM 3b. server di PRODUZIONE
deploy_war_tomcat9.bat "D:\SVILUPPO\wera_git\target\prod\wera2026.war" "wera2026" ^
    -catalina "<CATALINA_HOME del server>" -service Tomcat9
```

### Avvertenze sul passo 3

**Mai `-clean`.** In `deploy_war_tomcat9.bat` quell'opzione rimuove le webapp non standard
preservando solo `docs`, `examples`, `host-manager`, `manager`, `ROOT`: cancellerebbe la
webapp `wera`. Per lo stesso motivo, i deploy successivi *della produzione* con `-clean`
cancelleranno `wera2026` — comportamento corretto, basta saperlo.

**Mai `-clean-logs` senza `-app-log-dir` dedicato.** `APP_LOG_DIR` in `deploy_wera_patch.bat`
punta a `D:\usr\share\logs\wera`, che è la cartella log della produzione. Deployare
`wera2026` con l'azzeramento log attivo la svuoterebbe.

Il deploy riavvia il servizio Tomcat, quindi **anche l'istanza `wera` si riavvia**. Sul server
di produzione va pianificato fuori orario.

Due webapp complete nello stesso Tomcat raddoppiano classi e cache: verificare che l'heap
della JVM basti. Nel profilo prod `wera2026` apre un suo pool di connessioni verso il DB
(`maxActive=100` dal `META-INF/context.xml`): controllare `max_connections` di PostgreSQL.

---

## Prerequisiti — profilo test

**Datasource** in `conf\Catalina\localhost\wera2026.xml`:

```xml
<Context docBase="wera2026" path="/wera2026">
  <Resource name="jdbc/wera2026" auth="Container" type="javax.sql.DataSource"
            driverClassName="org.postgresql.Driver"
            url="jdbc:postgresql://HOST:5432/wera_test"
            username="..." password="..."
            maxTotal="10" maxIdle="3" maxWaitMillis="3000"/>
</Context>
```

Il DB (PostgreSQL) va creato da restore di produzione. Con un datasource che punta al DB
reale, tutto il resto della protezione non serve a niente.

**Email anonimizzate** sulla copia, con `anonimizza-email-test.sql` (istruzioni in testa al
file). Lo script gira solo su un DB marcato come copia di test, da fare UNA volta a mano:
`COMMENT ON DATABASE wera_test IS 'WERA-TEST: copia di prova, dati sacrificabili';`.
Sul DB di produzione quel commento non deve mai esistere: è ciò che lo protegge.

**Cartelle** scrivibili dall'utente di Tomcat: `/usr/share/webapps/wera2026/`
(`import`, `upload`, `output`, `ACS26V3`, `ANLCONFIG`) e `/usr/share/logs/wera2026/crash`.
`patch-config-test.sh` le crea; `make_test_war.bat` le crea solo sulla macchina dove gira.

**Mailcatcher** su `127.0.0.1:1025` (`perl deploy/mailcatcher.pl`, oppure MailHog/smtp4dev).
Serve perché `mail.SMTPHost.user` non può essere svuotato: `SendSMTPMail` ha un
`ErrDetector.invariant(Util.IsNotEmpty(smtpUser))`. Senza nulla in ascolto le mail non partono
comunque, ma si riempiono i log di eccezioni di connessione.

## Prerequisiti — profilo prod

- **Nessun** descrittore `conf\Catalina\localhost\wera2026.xml`: il WAR usa la risorsa
  `jdbc/wera` del proprio `META-INF/context.xml`, cioè il DB di produzione (stesso host
  `db-wera-etc-host` del file hosts del server). Un descrittore con un datasource diverso
  trasformerebbe l'istanza in un test su un altro DB.
- Cartella `/usr/share/logs/wera2026/crash` scrivibile dall'utente di Tomcat.
- Le cartelle `/usr/share/webapps/wera/...` sono **condivise** di proposito: un file caricato
  da `wera2026` è visibile a `wera` e viceversa, come se fosse la stessa applicazione.
- Le modifiche ai dati e "scarico dati" (che riscrive `AnlConfig.xml`) hanno effetti
  **reali**, esattamente come da `wera`.
- **Mailcatcher** in ascolto su `127.0.0.1:1025` (meglio come servizio di sistema): ogni mail
  di `wera2026` finisce lì e nessuna arriva al destinatario. Senza mailcatcher l'invio
  fallisce DOPO le scritture sul DB: un reset password da `wera2026` cambierebbe la password
  reale dell'utente senza fargliela ricevere. Per gli utenti reali reset e registrazione
  vanno fatti da `wera`.
- Il controllo finale degli script blocca il build se `mail.SMTPHost` non è `127.0.0.1`.
- **Non** usare `anonimizza-email-test.sql`: è per le copie del DB, non per la produzione.

---

## Nota attesa nei log

All'arresto o all'undeploy di `wera2026` (entrambi i profili) comparirà un `NullPointerException`:

```
net.projectsrl.servlet.scheduler.SchedulerServletApplication.destroy()
```

`destroy()` chiama `_scheduler.shutdown()` senza null-check, e con la tasklist vuota
`_scheduler` non viene mai istanziato; la `catch` intercetta solo `SchedulerException`.
**È atteso e innocuo**, non è un deploy fallito. Il fix (`if (_scheduler != null)`) tocca
codice Java ed è fuori dallo scope del restyling.
