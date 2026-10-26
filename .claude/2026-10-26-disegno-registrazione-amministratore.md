# 2026-10-26 — DISEGNO: registrazione amministratore (wera2026)

STATO: APPROVATO dall'operatore il 2026-10-26, tutte le scelte nell'opzione raccomandata: approvazione
SIRBI, password scelta nel form, legame ai condomini dall'anagrafica esistente; ok alle modifiche additive
(Java nuovo, functionList, template, CTA della landing, menu di wera2026), DDL solo sul DB locale.
Riferimento visivo: `D:\SVILUPPO\wera_demo\registrazione-amministratore.html`
(mockup giro 12; pagina introdotta al giro 4, rewardbox aggiunto al giro 6 e qui ESCLUSO).

## Cosa c'e oggi (analisi del codice e del DB locale)
- Registrazione inquilino: `FUNCTIONID=Registrazione` -> `dafne.authentication.FunctionRegistrazione`
  (base `webapp.authentication.FunctionRegistrazione_base`). Riceve NOME/COGNOME/EMAIL/RILEVATORE, crea
  `utenti` (username = email) + `utenti_lingue` (nel DAO) + `utenti_utenze` + `utenti_profili` (id 4 fisso)
  + `utenti_aziende` (azienda dell'utenza), poi `password` con una password casuale in chiaro nella mail,
  scadenza OGGI (al primo accesso va cambiata). Niente transazione: un errore a meta lascia un utente
  monco. Il controllo email unica e bacato (`isUniqueEmail` rifiuta solo se ci sono GIA' 2 utenti).
- Login: vista `V_LOGIN` (serve `utenti_lingue` di default, `password.fl_valida`, `utenti_aziende`).
  `dt_scadenza` passata -> pagina NewPassword (60 giorni di validita).
- Un "amministratore" in WERA e un UTENTE con profilo `AMM`, legato a un'azienda GESTORE (sul DB locale
  tutti i 28 AMM sono dell'azienda SIRBI) via `utenti_aziende`. Il perimetro e
  `condomini.id_amministratore = utenti.id_utente` (`BOWAuthenticationProvider.configureUserProfiles`).
  Nessuna anagrafica dello studio: `utenti` ha solo indirizzo/localita/provincia/cap/codfisc/telefono
  (niente P.IVA, PEC, SDI, registro). `aziende` sono i gestori (con crediti): NON adatte allo studio.
- Gli AMM compaiono in /wera nella tendina "amministratore" del condominio (`DS.DSAmministratore`):
  un utente AMM creato senza filtro finirebbe subito anche li.
- In wera2026 PROD l'SMTP e il mailcatcher: una mail di registrazione non arriva a nessuno.

## Proposta (raccomandata)
1. **Richiesta da approvare**: il form crea una RICHIESTA (schema nuovo), non un utente. L'ADM (SIRBI)
   la vede in una pagina nuova di wera2026, verifica la compatibilita dei ripartitori, approva o rifiuta.
   All'approvazione, in UNA transazione: `utenti` + `utenti_lingue` + `utenti_profili` (AMM per codice)
   + `utenti_aziende` (azienda per codice, parametro) + `password`.
2. **Password scelta dall'utente nel form** (come nel mockup), salvata solo come hash del framework e
   copiata in `password` all'approvazione (scadenza a N giorni, parametro). Cosi l'attivazione NON
   dipende dalla mail: anche con il mailcatcher l'amministratore entra appena approvato.
3. **Schema PostgreSQL nuovo `registrazioni`** (generico: servira anche all'installatore):
   - `richieste`: tipo (AMMINISTRATORE), tipo soggetto (PROFESSIONISTA/STUDIO), denominazione, P.IVA, CF,
     n. registro, PEC, SDI, indirizzo/citta/provincia/CAP, fascia condomini, elenco condomini (facoltativo),
     marca/modello ripartitori, referente nome/cognome/email/telefono, hash password (azzerato dopo
     l'esito), consenso privacy (ts + versione testo), IP, stato (IN_ATTESA/APPROVATA/RIFIUTATA/ANNULLATA),
     esito compatibilita (DA_VERIFICARE/COMPATIBILE/PARZIALE/NON_COMPATIBILE), note, motivo rifiuto,
     id_utente creato, audit.
   - `allegati`: foto dei ripartitori (nome originale, percorso univoco, mime, byte, sha256).
   - `parametri` (chiave/valore con override per azienda, come centrale_termica) ed `eventi` (append-only).
   - Nessuna FK verso tabelle di /wera; nessun oggetto esistente modificato.
4. **Dati fiscali** nella richiesta (= anagrafica dello studio dopo l'approvazione); in `utenti` solo i
   campi che /wera gia mostra (nome, cognome, email, telefono, indirizzo, localita, provincia, CAP, CF).
5. **Foto** in una cartella dedicata sotto `directory.external_files` (parametro `FILE.CARTELLA`,
   default `registrazioni/`), nome univoco, consultabili solo dalla pagina di approvazione.
6. **Legame ai condomini**: come oggi, dall'anagrafica condominio (campo amministratore) in wera2026 o
   in /wera. Nel form un campo facoltativo "Condomini che amministri" aiuta SIRBI. Assegnazione diretta
   dalla pagina di approvazione = possibile ondata successiva (scrive `condomini.id_amministratore`).
7. **Validazioni lato server**: P.IVA (11 cifre + cifra di controllo), CF (16 caratteri con carattere di
   controllo, omocodie comprese, o 11 cifre per le societa), email/PEC, SDI (7 alfanumerici, facoltativo),
   CAP, provincia, telefono, password (regola parametrica), marca/modello o almeno una foto, foto vere
   immagini (firma JPEG/PNG/WebP/HEIC), email non gia usata da un utente ne da una richiesta in attesa.
   Niente verifiche online (VIES/AdE): dipendenza esterna e rete chiusa di notte.
8. **Anti-abuso** (niente captcha di terzi): campo trappola nascosto, tempo minimo di compilazione,
   limite richieste per IP nelle 24 ore, una sola richiesta in attesa per email e per P.IVA, limiti di
   numero/dimensione delle foto, e soprattutto l'approvazione umana.
9. **Mail**: ricevuta al richiedente, notifica a SIRBI (destinatari da parametro), esito (attivo / rifiutato
   col motivo) al richiedente, MAI password nelle mail. Parametro `MAIL.ATTIVA` (S/N): in PROD `N` finche
   l'SMTP e il mailcatcher (come `ALERT.ATTIVO`); la pagina di approvazione lo segnala e il menu dell'ADM
   mostra il numero di richieste in attesa, cosi la notifica non dipende dalla mail.

## Parametri (default; nessun dato personale nel repo)
`ATTIVAZIONE.AZIENDA_CODICE` (SIRBI), `ATTIVAZIONE.PROFILO` (AMM), `APPROVAZIONE.PROFILI` (ADM),
`PASSWORD.LUNGHEZZA_MIN` (8), `PASSWORD.REGOLA`, `PASSWORD.GIORNI_VALIDITA` (60), `FASCE_CONDOMINI`
(1-5,6-15,16-40,oltre 40), `FILE.CARTELLA`, `FILE.ESTENSIONI`, `FILE.DIMENSIONE_MAX_MB` (10),
`FILE.MAX_PER_RICHIESTA` (6), `ANTIABUSO.SECONDI_MINIMI` (5), `ANTIABUSO.MAX_RICHIESTE_IP_24H` (5),
`ANTIABUSO.PROXY_FIDATI`, `MAIL.ATTIVA` (S), `MAIL.NOTIFICA_DESTINATARI` (vuoto), oggetti/testi delle mail,
`LINK.ACCESSO`, `PRIVACY.TESTO`, `PRIVACY.LINK` (vuoto = nessun link), `TESTO.TEMPI_RISPOSTA`
("entro 2 giorni lavorativi"), contatti assistenza (vuoti = nascosti).

## Ondate previste (laptop, un commit per ondata)
1. DDL `src/resources/sql/wera2026/003-registrazioni-ddl.sql` (idempotente, 9.4), solo DB locale.
2. Pagina pubblica `FUNCTIONID=RegistrazioneAmministratore` (stack `wera-landing.css`), invio multipart,
   validazioni, anti-abuso, foto, mail di ricevuta e notifica; CTA amministratore della landing.
3. Pagina ADM `RegistrazioniRichieste` (elenco, dettaglio con foto, approva/rifiuta, esito compatibilita),
   creazione utente in transazione, mail di esito, voce di menu con contatore; collaudo end-to-end.
