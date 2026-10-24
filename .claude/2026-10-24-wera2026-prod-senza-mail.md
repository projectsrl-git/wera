# 2026-10-24 — wera2026 profilo prod senza mail + correzione DB di produzione

## Obiettivo
Requisito dell'operatore per l'istanza `/wera2026` in produzione (server 192.168.0.106):
dati veri modificabili e upload di file funzionanti, ma NESSUNA schedulazione e NESSUNA
mail reale. Il profilo prod mandava ancora le mail con l'SMTP reale: ora le dirotta sul
mailcatcher locale come il profilo test.

## File toccati
- `deploy/patch_config_test.ps1` — override SMTP (`127.0.0.1:1025`, TLS off, utente fittizio,
  mittente `noreply@test.invalid`) spostati fra quelli comuni ai due profili; il controllo
  finale (exit 3) blocca anche un `mail.SMTPHost` diverso da `127.0.0.1`.
- `deploy/patch-config-test.sh` — stesso cambio e stesso controllo (compatibile CRLF).
- `deploy/make_test_war.bat` — commenti e prerequisiti del profilo prod (mailcatcher).
- `deploy/README.md` — tabella e prerequisiti del profilo prod: niente mail, mailcatcher come
  servizio, reset/registrazione degli utenti reali da fare da `wera`.
- `CLAUDE.md` — **`192.168.0.67` e il DB di PRODUZIONE** (era indicato per errore come DB di
  prova); profilo prod senza mail; server 192.168.0.106 (webcont92) e suoi vincoli.
- `QA.md`.

## Cosa NON e stato toccato (conferma)
Java, `config.cfg` del repo, template, CSS.

## QA
- [x] `make_wera2026.bat`: test 30 righe diverse dalla produzione (invariato), prod ora
      DB/cartelle invariati + Quartz + log + 7 righe SMTP/mittente
- [x] script Linux in sandbox == motore PowerShell (contenuto) per entrambi i profili
- [x] controllo SMTP: `smtps.aruba.it` bloccato, `127.0.0.1` accettato, con LF e CRLF

## Contesto e punti aperti
- Ricognizione in sola lettura di 192.168.0.106: Debian 11, Tomcat 9.0.43 Debian con
  `ProtectSystem=strict`, JVM Temurin 25, solo `gestit` (compilato per Java 25), nessun mount
  CIFS ne `cifs-utils`, `db-wera-etc-host` non definito. Setup affidato a una sessione separata
  (hosts, mount `//fileserver_logs/storage/logs` e `//fileserver_docs/storage/webapps`, override
  systemd, JVM, mailcatcher come servizio).
- La build su Jenkins (192.168.0.115) e preparata da un'altra sessione: JDK 8 come tool
  dedicato (pom.xml non si tocca), branch `restyle/ui-2026-r2`, WAR patchato con
  `patch-config-test.sh target/wera.war wera2026 prod`.
- WERA (bytecode Java 7) non e mai stata provata su Java 25: da verificare al primo deploy.
- Senza mailcatcher in ascolto l'invio mail fallisce dopo le scritture sul DB (es. reset
  password): il mailcatcher sul server e un prerequisito.
- Correzione storica: la sessione del 2026-10-22 aveva puntato il laptop su 192.168.0.67
  credendolo DB di prova; il 2026-10-24 su quel DB sono state fatte solo letture (ricerca
  utenti, apertura scheda), nessun salvataggio. Le scritture di collaudo sono avvenute sul DB
  locale del laptop.
