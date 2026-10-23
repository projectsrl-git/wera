# 2026-10-23 — Pacchetto di test: log isolati e guardia dello script email

## Obiettivo
Chiudere i due punti emersi verificando se wera-test.war si puo installare accanto alla
produzione senza interferenze: (1) i log del test finivano nei file della produzione;
(2) `anonimizza-email-test.sql` si proteggeva solo col controllo "server locale", inefficace
sul server di produzione dove anche il DB vero e locale.

## File toccati
- `deploy/patch_config_test.ps1` — nuovo parametro `-LogDir` (default `/usr/share/logs/wera-test/`),
  override di `Logger.CrashDir`, `Logger.DebugFile`, `Logger.ApplicationLog`; il controllo dei
  residui (exit 3) ora copre anche `/usr/share/logs/wera/`; riepilogo con le righe Logger.
- `deploy/make_test_war.bat` — passa `-LogDir`, crea `\usr\share\logs\wera-test\crash`,
  ricorda le cartelle da creare sul server.
- `deploy/patch-config-test.sh` — stesso override (passo 6/6), controllo residui esteso,
  `mkdir -p` della cartella log/crash.
- `deploy/anonimizza-email-test.sql` — rifiuta qualunque DB il cui commento non inizi con
  `WERA-TEST` (`COMMENT ON DATABASE`). Il nome non basta: produzione e copia locale si
  chiamano entrambe `wera`. Il controllo su server locale resta.
- `deploy/README.md` — tabella override con i Logger, esempio datasource aggiornato a
  PostgreSQL (era ancora jTDS/SQL Server), marcatore del DB, cartelle da creare sul server.
- `CLAUDE.md`, `QA.md`.

## Fatto fuori repo (laptop)
- DB locale `wera` marcato `WERA-TEST: copia locale del laptop, dati sacrificabili`.
- Rilanciato lo script email: ha riscritto l'email dell'utente di test registrato il
  2026-10-22 (+reg1 -> +u115) e le righe di email_log di quelle prove.
- /wera-test ridistribuito con il nuovo WAR (Tomcat nel tab "tomcat wera-test").

## Cosa NON e stato toccato (conferma)
Java, `config.cfg` del repo, template, CSS. Le patch agiscono solo sull'artefatto di test.

## QA
- [x] wera-test.war: 30 righe di config diverse dalla produzione (Quartz, I/O, SMTP, DB, Logger),
      nessun residuo attivo verso la produzione, encoding ISO-8859-1 intatto, 1971 entry, nessun backslash
- [x] deploy laptop: log in `D:\usr\share\logs\wera-test\` (debug, application, crash)
- [x] script email: rifiuto su DB non marcato, esecuzione su DB marcato, rilancio idempotente
- [x] `bash -n` su patch-config-test.sh, parser PowerShell su patch_config_test.ps1: 0 errori

## Rischi / punti aperti
- Sul server le cartelle `/usr/share/webapps/wera-test/...` e `/usr/share/logs/wera-test/crash`
  devono esistere ed essere scrivibili da Tomcat (lo script Linux le crea).
- Il marcatore e una scelta manuale: protegge da errori, non da chi marca di proposito la
  produzione.
- `deploy/mailcatcher.pl` salva ancora per default in `D:/usr/share/logs/wera/mailcatcher`
  (solo laptop); si puo passare `--dir`.
