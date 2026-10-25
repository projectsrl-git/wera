# 2026-10-25 — Centrale termica wera2026: pagina, menu e tema (ondata 2)

## Obiettivo
Pagina "Centrale termica" di wera2026 sul backend delle ondate 1a/1b, dal mockup di Andrea ma piu'
ricca: testata con scelta del condominio, 4 riquadri di sintesi, 6 cartelle con contatori e scadenze,
pannello laterale con documenti, versioni e archivio, caricamento con trascinamento, ultimi documenti
con "Scarica selezionati", scadenze, assistenza. Autorizzato dall'operatore il 2026-10-25: "template
nuovi, sezione 30 del tema e il ramo FreeMarker nel menu di wera2026".

## File toccati
- `src/main/webapp/WEB-INF/template/centraletermica.html` — NUOVO, ASCII con entita' (Page.Encoding
  ISO-8859-1), cornice standard (head/menu/header/footer); nessuna logica FreeMarker oltre a titolo e
  condominio iniziale (`?html`).
- `src/main/webapp/js/wera-centrale-termica.js` — NUOVO, ES5 + jQuery 2, ASCII (accenti come `\u00xx`):
  tutti i dati del server passano da `esc()`, link dei parametri solo http(s); select2 per il condominio,
  condominio ricordato (URL + localStorage in try/catch); sessione scaduta -> Logout.
- `src/main/webapp/css/wera-theme.css` — AGGIUNTA sezione 30 (classi nuove `ct-*`, nessuna esistente
  ridefinita), responsive 3/2/1 colonne, `prefers-reduced-motion`.
- Cache-buster `wera-theme.css?20261023` -> `?20261025` in `include/included_head.include`
  (ISO-8859-1, edit byte-level), `errore.html`, `errorpage.html`, `message.html` (solo la query string).
- `include/included_menu.include` — AGGIUNTI (ASCII, CRLF): voce "Centrale termica" dopo Home e, in
  fondo, uno script. Nessuna riga esistente cambiata.
- Java (pacchetto `net.projectsrl.wera.centraletermica`, gia' autorizzato):
  - NUOVO `FunctionCentraleTermica` (pagina): accesso negato -> evento + redirect alla Home; costruisce
    percorso "Home / Centrale termica", voce attiva e menu nella lingua corrente (la voce non e' nel menu
    del DB: evita la ClassCastException delle funzioni fuori menu).
  - NUOVO `FunctionCentraleTermicaStato` (POST): ARCHIVIATO / CORRENTE (ripristino) / ANNULLATO, con
    permesso di scrittura, 409 se gia' in quello stato, eventi ARCHIVIAZIONE/RIPRISTINO/ANNULLAMENTO.
    Serve all'archivio storico (senza, i documenti nuovi non ci finirebbero mai).
  - `CentraleTermicaDati`: OPERAZIONE=`accesso` (per il menu, non registra accessi negati);
    `parametri` espone anche `FILE.MAX_PER_CARICAMENTO`.
  - Base: `memorizzaAccesso` scrive in sessione `CT_ACCESSO`, `CT_TITOLO_MENU`, `CT_NASCONDI_VOCE_WERA`.
- `functionList.txt` — AGGIUNTE `CentraleTermica`, `CentraleTermicaStato`.
- DDL: solo la descrizione di `MENU.VOCE_WERA.PROFILI` (vale per i profili abilitati).

## Menu (come funziona)
Il menu e' renderizzato su ogni pagina senza DB: legge i campi di sessione. Alla prima pagina dopo il
login non ci sono: la voce e' nascosta e uno script chiama `CentraleTermicaDati&OPERAZIONE=accesso`, che
risponde e salva i campi in sessione; dalle pagine successive la voce la scrive il server.
- Profili in `ACCESSO.PROFILI` (default ADM,INS,AMM): voce visibile.
- Vecchia voce "Centrali termiche": nascosta (script, sulle voci i cui link sono tutti `*FileManager`)
  solo ai profili ABILITATI alla nuova e fuori da `MENU.VOCE_WERA.PROFILI` (default: la vedono ADM e INS,
  non AMM). Ai profili non abilitati (CON) resta com'e'.
- Un cambio dei parametri vale dal login successivo (i campi sono in sessione).

## QA (laptop, /wera2026 profilo test, DB locale WERA-TEST, browser integrato + curl)
- [x] build JDK 25 release 8, WAR test, deploy a caldo.
- [x] ADM: voce attiva, testata, riquadri (4 documenti, "Da verificare", "Nessuno", 3/6), cartelle con
  contatori e "Scadenza da registrare", Foto e Stato pagamenti in "Altre sezioni", ultimi documenti con
  badge v2 / "da WERA", scadenze da registrare; pannello sezione con versioni (v2 corrente, v1
  scaricabile); caricamento dalla finestra (file .exe scartato lato client, PDF caricato, finestra chiusa
  e dati aggiornati); archiviazione (via curl: 200, ripetuta 409, GET 405, condominio errato 404) e
  pannello con "Archivio storico (1)" e "Ripristina".
- [x] Telefono (375 px): una colonna, nessuno scorrimento orizzontale.
- [x] AMM: voce presente, vecchia voce nascosta, 7 condomini, nessun "Carica documenti", niente Stato
  pagamenti.
- [x] CON: accesso=false, pagina -> 302 Home, nessuna voce nel menu, vecchia voce intatta.
- [x] Prima pagina dopo il login: voce nascosta + chiamata accesso; pagina successiva resa dal server.
- [x] Utenti di test con password temporanea, hash ripristinati identici; sessione di prova chiusa.
- [ ] INS: assente nel DB locale. [ ] "Archivia"/"Ripristina" cliccati dall'interfaccia: chiedono
  conferma con `window.confirm`, che il browser automatizzato non gestisce (provati via curl).
- Osservazione preesistente (non toccata): in console "Bootstrap's JavaScript requires jQuery" da
  `js/bootstrap.js` (non caricato dalla pagina nuova; compare navigando dalle pagine pubbliche).

## Rischi / punti aperti
- Nessuna interfaccia per creare/chiudere le SCADENZE (le tabelle ci sono, la pagina le mostra):
  prossima ondata insieme alla configurazione (sezioni, parametri, tipi di scadenza).
- Contatti dell'assistenza vuoti: il riquadro compare appena l'operatore valorizza i parametri.
- Documenti ANNULLATI non ancora consultabili dall'interfaccia (solo stato e registro).
- Prima del deploy: DDL su .75 con ok, poi Jenkins; poi i contratti di Andrea.
