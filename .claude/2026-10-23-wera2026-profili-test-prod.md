# 2026-10-23 — wera2026: WAR in due profili (test e prod) accanto a wera

## Obiettivo
Portare questa versione sia sul server di test sia su quello di produzione IN PARALLELO a
`/wera`, con il contesto `/wera2026` al posto di `/wera-test`. Decisione dell'operatore:
- **test**: istanza isolata su una copia del DB (come il vecchio wera-test);
- **prod**: nuova interfaccia sui dati veri (stesso DB, SMTP reale, cartelle condivise),
  con Quartz spento (job e mail automatiche solo su wera) e log separati.

## File toccati
- `deploy/patch_config_test.ps1` — riscritto con parametri `-Context` (default `wera-test`) e
  `-Profilo test|prod`: Quartz spento e log `/usr/share/logs/<contesto>/` in entrambi;
  I/O, SMTP e datasource `jdbc/<contesto con _>` solo nel profilo test. Rifiuta il contesto
  `wera`. Controllo finale per profilo (tasklist vuota + nessun path vietato). Conserva i fine
  riga dell'originale (CRLF): prima riscriveva tutto in LF.
- `deploy/make_test_war.bat` — argomenti `[war] [contesto] [test|prod]`, output
  `target\<profilo>\<contesto>.war`, cartelle create secondo il profilo, prerequisiti stampati
  per profilo.
- `deploy/make_wera2026.bat` — NUOVO: produce i due WAR wera2026.
- `deploy/patch-config-test.sh` — stessi argomenti e profili; `set_key` fallisce se la chiave
  manca e non mangia il `\r` delle righe CRLF; output `/tmp/<profilo>/<contesto>.war`.
- `deploy/README.md` — riscritto: tabella dei due profili, procedura, prerequisiti per profilo.
- `CLAUDE.md`, `QA.md`.

## Fatto fuori repo (laptop)
- `conf\Catalina\localhost\wera2026.xml` creato dal vecchio `wera-test.xml` (risorsa
  `jdbc/wera2026`, stesso DB locale); `wera-test.xml` rinominato `wera-test.xml.disabled`.
- Rimossa la webapp `wera-test`, deployato `target\test\wera2026.war`.

## Cosa NON e stato toccato (conferma)
Java, `config.cfg` del repo, template, CSS.

## QA
- [x] config test: 30 righe diverse dalla produzione; config prod: 17 (Quartz + log), DB/SMTP/
      cartelle invariati; CRLF conservati; WAR con 1971 entry e nessun backslash
- [x] script Linux eseguito in sandbox (zip sostituito da jar, mkdir di sistema neutralizzati):
      contenuto identico al motore PowerShell per entrambi i profili; il `\r` e conservato con
      sed binario come su Linux (in Git Bash `sed -i` lavora in modalita testo)
- [x] laptop: /wera2026 su, /wera-test 404, login ok, log in D:\usr\share\logs\wera2026, niente Quartz

## Rischi / punti aperti
- **Profilo prod sui dati veri**: le azioni dalla GUI (registrazione, reset, mail di workflow,
  "scarico dati" -> AnlConfig.xml) hanno effetti reali, come da wera. E voluto.
- Due webapp nello stesso Tomcat: piu memoria (heap) e un secondo pool verso il DB
  (`maxActive=100` dal context.xml del WAR): verificare heap e `max_connections`.
- Il deploy riavvia Tomcat e quindi anche wera: fuori orario in produzione.
- NPE attesa all'undeploy (SchedulerServletApplication.destroy senza null-check).
