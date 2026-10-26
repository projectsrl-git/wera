# 2026-10-22 — Deploy /wera-test eseguito + fix make_test_war.bat

## Cosa e successo
Primo deploy end-to-end dell'istanza di test sul Tomcat di sviluppo (procedura di settembre,
mai provata per intero su Windows): build, make_test_war, deploy, avvio, QA della GUI r2
su DB reale di prova (hosts: db-wera-etc-host -> 192.168.0.67).

## Fix al tooling
- `deploy/make_test_war.bat`: il riconfezionamento con PowerShell
  `ZipFile::CreateFromDirectory` produceva entry con separatore BACKSLASH
  (`css\animate.css`): zip non conforme, Tomcat lo rifiutava con
  "Exception fixing docBase" e il contesto non partiva. Ora il WAR viene
  riconfezionato con `jar.exe` del JDK (serve JAVA_HOME o jar nel PATH).
- Segnalazione (script FUORI repo, D:\SVILUPPO\scripts): `deploy_war_tomcat9.bat`
  usa `wmic` nel wait-loop di stop, ma wmic non esiste piu su Windows 11:
  il loop e cieco e lo script procede anche con Tomcat ancora attivo.
- Creato `conf/Catalina/localhost/wera-test.xml` (fuori repo, sul Tomcat):
  risorsa `jdbc/wera_test` -> `jdbc:postgresql://db-wera-etc-host:5432/wera`
  (segue la mappatura hosts scelta dall'operatore), credenziali del context wera.

## Esito QA (istanza live, dati reali)
- login errato -> dialog riaperto con "Accesso negato..." (mobile e desktop)
- login superuser ok -> home board: metric card con dot semantici, Richieste 4 righe
  NR desc con lente e chip stato, card crediti da DSCrediti (barra 99%), donut reale,
  topbar-crumb, voce Home attiva su gradiente, white-label (logo sirbi)
- registrazione e reset password renderizzate live, fedeli al mockup
- isolamento: 0 thread quartz in JVM (jcmd), cartelle wera-test isolate create,
  patch config verificata dal fail-fast dello script
- formato numeri: toLocaleString('it-IT') raggruppa da 10.000 in su
  (CLDR minimumGroupingDigits=2): "1258" senza punto E CORRETTO, "993.033" col punto.

## Punti aperti
- credenziali amministratore fornite (username dell'ambiente di produzione) rifiutate dal DB di
  prova: "utente inesistente oppure password non corretta" -> verificare username/presenza
  nel restore. QA profilo amministratore rimandato.
- mailcatcher su 127.0.0.1:1025 ASSENTE: i test che inviano mail (registrazione, reset)
  falliranno l'SMTP (eccezione nei log, nessuna mail ispezionabile). Avviare MailHog o
  equivalente prima di quei test.
- il deploy e stato fatto con -clean (scelta operatore): le webapp wera e openproteo
  sono state rimosse dal Tomcat di sviluppo.
- Tomcat gira in foreground nel tab "tomcat wera-test" del Terminal panel (Ctrl-C per fermarlo).
