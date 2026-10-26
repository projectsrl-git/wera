# 2026-10-26 — Registrazione amministratore, ondata 2: pagina pubblica e invio della richiesta

## Obiettivo
Pagina `FUNCTIONID=RegistrazioneAmministratore` sullo stack della landing (stesso design della registrazione
inquilino v2), invio della richiesta con foto nello schema `registrazioni` (ondata 1), mail di ricevuta e di
notifica, CTA amministratore della landing ricollegate. L'utente NON nasce qui: all'approvazione (ondata 3).

## File toccati
- `src/main/java/net/projectsrl/wera/registrazioni/` — NUOVO pacchetto:
  `FunctionRegistrazioneAmministratore` (GET pagina, POST multipart -> JSON), `FunctionRegistrazioni_base`
  (JSON, IP dietro proxy fidato), `Db` (PreparedStatement, null tipizzati), `Parametri`, `Validazioni`
  (P.IVA e CF con carattere di controllo, omocodie, CF numerico per le societa), `Allegati` (percorso
  univoco `<anno>/<id_richiesta>/<uuid>.<ext>`, firma JPEG/PNG/WebP/HEIC, SHA-256), `Posta` (MAIL.ATTIVA,
  segnaposto, testo traslitterato in ASCII perche SendSMTPMail dichiara us-ascii), `Eventi`, costanti.
  Sorgenti ASCII (pom Cp1252).
- `src/main/webapp/WEB-INF/config/functionList.txt` — una riga `RegistrazioneAmministratore` (CRLF, BOM preservati).
- `src/main/webapp/WEB-INF/template/registrazioneamministratore.html` — NUOVO (ASCII, entita HTML, `?html`).
- `src/main/webapp/js/wera-registrazione-amministratore.js` — NUOVO (ES5, ASCII, nessuna dipendenza).
- `src/main/webapp/css/wera-landing.css` — sezione "REGISTRAZIONE AMMINISTRATORE" in fondo, classi `ra-`.
- Cache-buster `wera-landing.css?20261022` -> `?20261026` in login, registrazione, resetpassword, newpassword.
- `src/main/webapp/WEB-INF/template/login.html` — CTA amministratore: slide 2 "Registrati" + "Accedi con user
  e password", card profilo "Registrati" + link "Accedi", "Come accedere" e dialog di login con il link
  "Registra il tuo studio". Cablaggio del form di login invariato.

## Pagina (mockup giro 12, migliorata)
- Guida laterale "Come funziona" (4 passi), "Tieni a portata di mano", barra "Modulo completato" (desktop),
  contatti assistenza se i parametri sono valorizzati.
- Form in 5 sezioni numerate: tipo di soggetto (card radio; lo studio nasconde il n. registro e cambia
  l'etichetta), dati fiscali, ripartitori (marca/modello OPPURE foto: galleria, fotocamera, trascinamento,
  anteprime con rimozione), condomini (fascia da parametro + elenco facoltativo, NUOVO rispetto al mockup:
  aiuta SIRBI a collegarli), referente e accesso (password con Mostra/Nascondi e regola da parametro).
- Errori per campo, lato client e lato server con gli stessi testi; invio con barra di avanzamento; esito
  "Richiesta inviata" con numero e prossimi passi. Con `REGISTRAZIONE.ATTIVA=N` la pagina mostra solo il
  messaggio di registrazioni sospese.
- SALTATO di proposito: rewardbox "Guadagna con noi" (programma reward escluso).

## Anti-abuso
Campo trappola `SITO_WEB` (risposta OK finta, evento BLOCCO), gettone HMAC con l'ora di apertura del form
(tempo minimo `ANTIABUSO.SECONDI_MINIMI`; gettone scaduto o di prima di un riavvio -> RIPROVA e il JS
ripete da solo una volta), limite `ANTIABUSO.MAX_RICHIESTE_IP_24H` (IP da X-Forwarded-For solo se la
connessione arriva da `ANTIABUSO.PROXY_FIDATI`), una richiesta in attesa per email e per P.IVA.

## QA (laptop, /wera2026 profilo test, DB locale WERA-TEST, mailcatcher)
- [x] `mvn clean package` ok (JDK 25), nessun "unmappable character"; ridistribuzione a caldo di /wera2026.
- [x] GET 200, parametri in pagina (fasce, limiti, gettone).
- [x] Client: invio vuoto -> 15 errori per campo; P.IVA/CF con carattere di controllo sbagliato rifiutati.
- [x] Server (POST diretto): 12 errori per campo, `<` nei testi rifiutato; trappola -> OK finto + BLOCCO;
  gettone alterato -> 409 RIPROVA; file non immagine -> errore FOTO, nessuna riga, nessun file, cartella
  rimossa; email/P.IVA gia in attesa -> errori sul campo; limite IP -> 429.
- [x] Invio dalla UI (studio, P.IVA con prefisso IT, SDI e provincia in minuscolo, foto trascinata):
  richiesta n. 6, foto salvata con SHA-256, ricevuta nel mailcatcher. Nome e citta con accenti
  (richiesta n. 8): nel DB in UTF-8, nelle mail traslitterati (Niccolo', Forli'); notifica a
  `MAIL.NOTIFICA_DESTINATARI` (override di prova solo sul DB locale) con il link alla richiesta.
- [ ] Screenshot visivi: non fatti (finestra dell'app nascosta, il pannello browser non disegnava);
  layout verificato via DOM (griglia 2 colonne a 1280px). Da guardare a occhio desktop e 375px.

## Rischi / punti aperti
- In PROD: `MAIL.ATTIVA=N` finche l'SMTP di wera2026 e il mailcatcher; `MAIL.NOTIFICA_DESTINATARI` da
  valorizzare (vuoto di default, niente indirizzi nel repo).
- La pagina di approvazione e la creazione dell'utente arrivano con l'ondata 3: fino ad allora il link
  `RegistrazioniRichieste` nella mail di notifica non esiste.
