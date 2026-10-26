# 2026-10-26 — Registrazione amministratore, ondata 3: approvazione e attivazione dell'utente

## Obiettivo
Pagina "Richieste di registrazione" per i profili di `APPROVAZIONE.PROFILI` (default ADM): elenco per stato,
dettaglio con foto e controlli, verifica dei ripartitori, approvazione che crea l'utente AMM con la password
scelta dal richiedente, rifiuto con motivo, annullamento. Voce di menu con il numero di richieste in attesa.

## File toccati
- `src/main/java/net/projectsrl/wera/registrazioni/` — NUOVI `FunctionRegistrazioniRichieste` (pagina, percorso
  e voce attiva costruiti come per la Centrale termica), `FunctionRegistrazioniDati` (GET JSON: accesso, elenco,
  dettaglio, foto), `FunctionRegistrazioniEsito` (POST: verifica, approva, rifiuta, annulla), `Approvatore`;
  `FunctionRegistrazioni_base` con i campi di sessione `REG_ACCESSO` / `REG_IN_ATTESA`.
- `src/main/webapp/WEB-INF/config/functionList.txt` — 3 righe (RegistrazioniRichieste, RegistrazioniDati,
  RegistrazioniEsito).
- `src/main/webapp/WEB-INF/template/registrazionirichieste.html` — NUOVO (ASCII, classi `ct-` del tema sez. 30).
- `src/main/webapp/js/wera-registrazioni-richieste.js` — NUOVO (jQuery come la Centrale termica, ASCII).
- `src/main/webapp/css/wera-registrazioni.css` — NUOVO, collegato solo dalla pagina (classi `reg-`): nessun
  cambio a `wera-theme.css` ne al cache-buster di `included_head.include`.
- `src/main/webapp/WEB-INF/template/include/included_menu.include` — voce "Registrazioni" (fa-user-plus, contatore
  `label-warning`) dopo la Centrale termica + script che chiede l'accesso a `RegistrazioniDati&OPERAZIONE=accesso`.
  Edit byte-level con perl (ancore univoche, CRLF): il file resta ASCII con CRLF, nessun byte non-ASCII.

## Approvazione (una transazione)
`SELECT ... FOR UPDATE` della richiesta (solo IN_ATTESA), controllo email gia usata, azienda per codice
(`ATTIVAZIONE.AZIENDA_CODICE`), profilo per codice (`ATTIVAZIONE.PROFILO`), lingua `it`; poi INSERT in
`utenti` (username = email, referente, telefono, indirizzo, localita, provincia, CAP, CF), `utenti_lingue`,
`utenti_profili`, `utenti_aziende`, `password` (hash scelto nel form, scadenza a `PASSWORD.GIORNI_VALIDITA`
giorni come NewPassword, `fl_valida`), UPDATE della richiesta (APPROVATA, id_utente, hash azzerato). Un
errore qualunque annulla tutto. Dopo il commit: evento e mail di esito con l'esito della verifica ripartitori.
Il legame ai condomini resta nell'anagrafica condominio (link nella pagina e nell'avviso).

## Cosa NON e stato toccato
Nessun oggetto o query di /wera; tabelle utenti di /wera solo con INSERT di righe nuove all'approvazione
(come la gestione utenti esistente); config.cfg, pom, wera-theme.css invariati.

## QA (laptop, /wera2026 profilo test, DB locale WERA-TEST, mailcatcher)
- [x] build JDK 25 ok; ridistribuzione a caldo; senza login le URL nuove rispondono con la landing (nessun dato).
- [x] ADM (password di test impostata sul DB locale e poi RIPRISTINATA): voce di menu attiva con contatore,
  stati con conteggi, elenco, dettaglio (dati, condomini indicati, foto servita 200 image/jpeg, registro).
- [x] Salva verifica (COMPATIBILE + note) -> approva n. 6: utente creato con profilo AMM, azienda SIRBI, lingua,
  password valida, presente in V_LOGIN; mail di approvazione con esito e nota nel mailcatcher; contatore 2 -> 1.
- [x] Login del nuovo amministratore con la password scelta nel form: Home ok. Per lui la pagina delle
  richieste fa redirect alla Home e i dati rispondono 403 (evento ACCESSO_NEGATO).
- [x] Rifiuta senza motivo -> 400; seconda approvazione -> 409; rifiuta n. 8 con motivo -> mail con il motivo.
- [x] `MAIL.ATTIVA=N`: nuova richiesta n. 9 senza mail (eventi NON_INVIATA), la pagina pubblica non la promette;
  annulla n. 9 -> nessuna mail. Parametri locali riportati ai default (MAIL.ATTIVA=S, limite IP 5, notifica vuota).
- [ ] Da guardare a occhio (screenshot non riusciti con la finestra dell'app nascosta).

## Dati di prova rimasti sul DB locale (sacrificabile)
Richieste 6 (APPROVATA, utente AMM `amm.prova1@example.org` id 116), 8 (RIFIUTATA), 9 (ANNULLATA); foto in
`D:\usr\share\webapps\wera2026\registrazioni\2026\6\`; mail nel mailcatcher. Indirizzi example.org fittizi.

## Rischi / punti aperti
- PROD (con ok dell'operatore): DDL 003 su .75; `MAIL.ATTIVA=N` finche l'SMTP e il mailcatcher;
  `MAIL.NOTIFICA_DESTINATARI` da valorizzare (o lasciare vuoto: il contatore nel menu basta); verificare in sola
  lettura che `aziende.codice='SIRBI'` esista su .75; la cartella `registrazioni/` nascera sotto
  `/usr/share/webapps/wera/` (share di .81, scrivibile da Tomcat).
- In PROD la richiesta arriva da `X-Forwarded-For` tramite .50 (`ANTIABUSO.PROXY_FIDATI`): verificare dopo il
  deploy che nel registro eventi compaia l'IP del client e non 192.168.0.50.
- Conservazione dei dati delle richieste rifiutate/annullate: oggi restano (con hash azzerato); una regola di
  cancellazione dopo N mesi e una decisione da prendere.
- La registrazione inquilino esistente ha un controllo email bacato (`isUniqueEmail` rifiuta solo con 2 utenti):
  non toccato, fuori ambito.
