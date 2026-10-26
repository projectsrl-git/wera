# 2026-10-22 — DB locale, anonimizzazione email, QA profilo amministratore

## Obiettivo
Rendere collaudabile `/wera-test` sul laptop fuori dalla LAN del DB di prova: DB PostgreSQL
locale (ripristinato dall'operatore), email anonimizzate verso la casella dell'operatore,
smoke test del profilo amministratore (punto 1 dell'handoff r2).

## File toccati
- `deploy/anonimizza-email-test.sql` — NUOVO. Riscrive tutti gli indirizzi del DB di test:
  `utenti.email` -> `<casella>+u<ID_UTENTE>@<dominio>` (unico: il reset password cerca per
  EMAIL e richiede unicita), ogni altra colonna di testo di ogni tabella -> `<casella>`;
  `utenti.username` escluso (login, indice unico). Destinazione passata con `-v dest=...`
  (non sta nel repo). Rifiuta server non loopback. Rilanciabile (seconda esecuzione: 0 righe).
- `CLAUDE.md` — configurazione laptop: DB locale, script email, crash log, bug di sessione,
  come entrare con un utente di test.
- `QA.md` — sezione "QA profilo amministratore" con esiti e bug preesistenti.

## Fatto fuori repo (laptop)
- Creata `D:\usr\share\logs\wera\crash\` (mancava: i crash non venivano scritti).
- Script eseguito sul DB locale: utenti.email 54, distribution_list 12, email_log 437x5
  colonne, mail_config.mittente 60 righe; scansione finale: restano solo i 47 username.
- Utente amministratore di test (id 30): copiato l'hash della password di test del
  superuser, scadenza 2999/12/31. Nessuna credenziale scritta nel repo.

## Diagnosi della sessione
- "utente inesistente": il messaggio `Messages.LoginFailed` non distingue utente/password,
  ma `wera_application.log` confermava "user not found": sul DB di prova lo username
  dell'amministratore e diverso da quello di produzione.
- 500 su modifica utente: laptop su Wi-Fi 10.x, DB 192.168.0.67 irraggiungibile (timeout
  TCP ~21 s). Risolto passando al DB locale.

## Cosa NON e stato toccato (conferma)
Java, `config.cfg`, template, CSS: invariati. Nessuna direttiva FreeMarker modificata.

## QA
- [x] login superuser e amministratore sul DB locale
- [x] amministratore: home, anagrafiche utenze, ripartizioni, storico, centrali termiche, profilo
- [ ] letture consumi per condominio (vista mancante sul DB locale)
- [ ] mailcatcher 127.0.0.1:1025 + registrazione/reset (non ancora installato)
- [ ] smoke test superuser ondate 0-6 su questa istanza

## Rischi / punti aperti
- Plus-addressing (`+u<id>`): il dominio usa MX IONOS; se l'SMTP reale venisse usato e
  IONOS rifiutasse il `+`, le mail utente rimbalzerebbero. Con il mailcatcher non conta.
- Bug preesistenti elencati in QA.md (FreeMarker in inserimentoripartizioni, script 404
  sulle pagine pubbliche, 500 su Login con sessione attiva): da decidere con l'operatore.
