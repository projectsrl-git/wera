# 2026-10-22 — Mailcatcher perl e collaudo registrazione / reset password

## Obiettivo
Chiudere il punto 2 dell'handoff r2: un mailcatcher su 127.0.0.1:1025 senza scaricare
nulla, e il collaudo end-to-end dei flussi che inviano mail (registrazione, reset,
primo accesso con password scaduta) sulle pagine v2.

## File toccati
- `deploy/mailcatcher.pl` — NUOVO. SMTP minimale in perl puro (IO::Socket): EHLO/HELO,
  AUTH PLAIN/LOGIN con credenziali qualsiasi, MAIL/RCPT/DATA (dot-unstuffing), RSET/NOOP/QUIT;
  salva ogni mail come `.eml` con intestazioni `X-Mailcatcher-From/Rcpt`; una riga di log
  per mail (mittente, destinatari, oggetto). Opzioni `--host --port --dir`. Niente inoltro.
- `CLAUDE.md` — mailcatcher attivo, come fare login da script.
- `QA.md` — sezione "Mail: registrazione e reset con mailcatcher".

## Esito QA (DB locale, istanza /wera-test)
- [x] sessione SMTP grezza di prova (AUTH LOGIN, riga con punto iniziale) salvata correttamente
- [x] registrazione: utente di test creato (profilo condomino) + mail password provvisoria
- [x] reset: mail password provvisoria
- [x] provvisoria scaduta -> pagina Nuova password v2 con avviso -> cambio -> Home
- [x] login con la nuova password ok; provvisoria vecchia rifiutata con errore nel dialog

## Fatto fuori repo (laptop)
- Tomcat era fermo (riavvio della sessione): riavviato nel Terminal panel, tab "tomcat wera-test";
  mailcatcher nel tab "mailcatcher".
- Utente di test `<casella operatore>+reg1@<dominio>` sul DB locale; password corrente in un
  file dello scratchpad di sessione, mai nel repo.

## Cosa NON e stato toccato (conferma)
Java, `config.cfg`, template, CSS: invariati.

## Rischi / punti aperti
- Il mailcatcher e single-thread (una connessione alla volta, timeout 60 s): sufficiente per il
  collaudo manuale, non per test di carico.
- Preesistenti (anche su main): `message.html` senza `<title>`; newpassword non precompila lo
  username che il Java gli passa. Lasciati com'erano.
