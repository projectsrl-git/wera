# QA — build e smoke test (a carico dell'operatore)

## Build
1. Ambiente: Java 7, Maven, SQL Server (jTDS via JNDI), Tomcat.
2. `mvn clean package` → deploy del WAR su Tomcat.
3. Verificare in rete che il client raggiunga fonts.googleapis.com/fonts.gstatic.com (altrimenti self-hostare i font in `webapp/fonts/`; i fallback Georgia/system-ui restano leggibili).

## Smoke test visivo (ondata 0)
Percorso: login → home → una `ricerca*` → una `inserimento*` → `inserimentoripartizioni` (tab "Costi per unità") → configurazione guidata → profilo.

Checklist:
- [ ] Il tema si applica: sfondo #f5f4f2, card bianche arrotondate, bottoni primari a gradiente, sidebar scura con voce attiva su gradiente.
- [ ] I form si inviano ancora (login, una ricerca, un inserimento).
- [ ] DataTables: ordinamento, ricerca, paginazione funzionano; paginazione attiva colorata.
- [ ] Select2 si apre e seleziona; focus ring corallo visibile.
- [ ] Datepicker si apre; giorno selezionato in accento, "oggi" in ambra.
- [ ] Tab (`.nav-tabs`) si aprono; attivo con underline accento.
- [ ] Toastr e alert leggibili con le nuove tinte.
- [ ] Nessuna regressione layout a 1366×768 e 1920×1080; menu collassabile (hamburger) funziona.
- [ ] Footer e breadcrumb corretti.

## Ondata 1 — auth & dashboard
- [ ] login (landing): sezioni profili/crediti/accesso, form di login funzionante.
- [ ] registrazione / resetpassword / newpassword: invio form, messaggi errore/info visibili.
- [ ] home: i 4 numeri e le voci di dettaglio si popolano; card a pari altezza (2 col <1200px, 1 col mobile); grafici amcharts e DataTable "Richieste di ripartizione" ok.
- [ ] profilo: tab Profilo/Crediti, salvataggio.

## Collaudo PROD parallela /wera2026 su .106 — ricognizione (2026-10-25)
Checklist (bozza da concordare): `.claude/2026-10-25-checklist-collaudo-prod.md`.
- [x] **RISOLTO (DB)**: /wera2026 su .106 NON usava il DB di /wera. Su .70 (`webcont06`) il file hosts
      mappa `db-wera-etc-host` -> 192.168.0.75 e le share su .81/.55; su .106 -> 192.168.0.67 e
      share su .186/.185. Le cartelle di /wera (`ACS26V3`, `ANLCONFIG`, `import` recenti) sono solo
      sulla share di .81. Operatore: /wera = .75, /weralegacy = .67. 10:57 su .106 `db-wera-etc-host` -> .75
      (backup `/etc/hosts.bak-20261025-105745`, etckeeper `0134dbd`), reload del solo contesto
      wera2026 (touch `WEB-INF/web.xml`, 3 s): login 200, 1 connessione stabilita verso .75 e
      nessuna verso .67, gestit 200, nessun nuovo crash. Avviso atteso al reload:
      `ClassCastException ObjectStreamClass$Caches` (Tomcat 9.0.43 + Java 25, innocuo).
- [x] share documenti allineata (11:21, opzione B dell'operatore): `/usr/share/webapps/wera` su .106
      montata da `//192.168.0.81/storage/webapps/wera` (SMB 3.0, stesse credenziali di .186),
      etckeeper `b630b24`; tomcat9 `RequiresMountsFor` anche su quel mount (`f6a2800`). Dalla
      sandbox di Tomcat il mount e visibile e scrivibile come `tomcat`: nessun riavvio. gestit 200,
      wera2026 200, nessun nuovo crash.
- [ ] con login: file manager e archivio file importati mostrano i file di /wera.
- [ ] al prossimo riavvio di .106: tomcat9 parte dopo il mount di .81 (`findmnt /usr/share/webapps/wera`).
- [x] (superato: .67 e weralegacy) su .67 manca la vista materializzata `vista_scarico_data_massima`: 5 crash del 2026-10-25
      09:30 (`DSLettureConsumiCondomini`, `...Dettaglio`, `...Condomino`); letture consumi,
      storico e grafici del condomino in 500; l'upload rilevatori fallirebbe sul REFRESH della
      vista dopo aver inserito le letture.
- [x] da .106 raggiungibili 192.168.0.67 e .75 (5432) e i file server .81, .186, .55, .185 (445).
- [x] crash di /wera2026 al 2026-10-25: solo i 5 della vista; mailcatcher: 2 file presenti (contenuto non controllato).

## Collaudo PROD /wera2026 — superuser, sola lettura (2026-10-25, DB .75, share .81)
Login fatto dall'operatore nel browser integrato; navigazione e verifiche dell'agente (DOM,
rete, console) a 1366x768; crash e log letti su .106. Nessun salvataggio, nessun upload.
- [x] A1 landing: carosello e dialog ok; 404 noti di `js/jquery-3.1.1.min.js`/`js/popper.min.js`
      ("Bootstrap's JavaScript requires jQuery") confermati anche in PROD.
- [x] A4 `FUNCTIONID=Login` con sessione attiva: pagina "500 Error" (bug noto) servita con HTTP 200.
- [x] A5 registrazione e reset password: pagine 200, nessun invio.
- [x] B1 home: 927 utenze, 54 condomini, 4.838 ripartitori, 50 antenne, Richieste 4 righe, donut.
- [x] B2 giro HTTP delle 54 voci di menu (escluso Esci): tutte 200, tema caricato, nessun errore
      FreeMarker, nessuna risposta oltre 5 s.
- [x] B3 ricerca condomini: 54 righe, ricerca in linea, 6 filtri di colonna, export, icone di riga.
- [x] B4 condominio aperto in modifica (8/8 select2, 7/7 campi, datepicker); NON salvato.
- [x] B6 ripartizioni: 87 standard, 7 UNI, 427 letture, **0 UNI2018** (intestazioni UNI2018 non
      verificabili nemmeno in PROD); ripartizione standard aperta con la lente (READONLY=Y): 60/60
      campi disabilitati, Conferma disabilitato, 5 tab, Costi per unita 13 righe, grafico disegnato.
- [x] B9 letture consumi e storico (vista presente su .75, nessun 500): BELTRAMI 1 condominio,
      6 condomini, grafico; storico con 6 date. Ultima lettura dei condomini provati: 27/07/2025
      (da confrontare con /wera).
- [x] B10 gestione rilevatori: censiti 4.894, non censiti 11.169, in errore (riepilogo) 77, senza
      lettura 10, non scaricati 175 (query ~13 s).
- [x] B11 file manager (share .81): 20 card, 5 file, anteprima immagine caricata; ricerca file 5;
      calendario ottobre 2026 ok; archivio file importati 18 righe, ultimo import 19/10/2026 18:10.
- [x] B12 configurazione guidata: select tipo (3) e Inizia presenti; non avviata.
- [x] B14 profilo: tab Profilo, Crediti, Documentazione, Assistenza, Software release.
- [x] .106: un solo crash nuovo (vedi difetto D2), 0 SEVERE, mailcatcher invariato (2 file),
      7 connessioni verso .75 e nessuna verso .67.
Difetti trovati:
- [x] **D1 (restyle r2) CORRETTO nel repo** (4.838 / 54, verificato sul laptop; in PROD dopo il
      prossimo deploy Jenkins). Landing `login.html` r.39-40: "4.596 ripartitori gestiti" e "49
      condomini attivi" sono testo fisso copiato dal mockup; la home reale ne mostra altri
      (segnalato dall'operatore).
- [ ] **D2 (preesistente, dal commit iniziale)** file manager / ricerca file / archivio: il nome
      del file e concatenato nell'URL di download senza `encodeURIComponent`; un file con "+" nel
      nome ("Dic Installatore + Mod RD Firmati.PDF") arriva al server con uno spazio ->
      `FileNotFoundException` nel crash log (11:27:16) e download vuoto. Template:
      `filemanager.html` r.67-123, `ricercafilemanager.html` r.50, `archiviofileimportati.html` r.55.
- [ ] D3 (cosmetico) legenda del donut in home con separatore inglese ("4,817").
Non eseguiti: A2 (login errato: richiede logout, gia verificato sul laptop), B5/B7/B8 e casi E.

## Build con JDK 25 (2026-10-24)
- [x] `mvn clean package` con Zulu 25 + Maven 3.9.12: BUILD SUCCESS, 621 sorgenti, bytecode 52
      (release 8); anche con JDK 8 + Maven 3.9.12. Maven 3.3.9 non e piu supportato.
- [x] WAR: 41 jar, `jaxb-api` 2.3.1, `jaxb-runtime` 2.3.9, una sola `jakarta.activation` 1.2.2,
      nessun `xercesImpl`.
- [x] JAXB: marshal + unmarshal di `Siemeca` riusciti su Zulu 25 e Zulu 8 (prima l'unmarshal
      falliva per xercesImpl 2.2.1 anche con il WAR in esercizio su Java 8).
- [x] FOP 0.20.5 senza xercesImpl: PDF generato da un FO minimo.
- [x] laptop, profilo test, Tomcat su Zulu 25: login, home (donut, richieste, crediti nascosti),
      ricerca utenti, salvataggio condominio con Swal, inserimento ripartizioni (5 tab, nessun
      errore FTL), reset password con mail multipart catturata dal mailcatcher.
- [x] stesso WAR su Zulu 8: login e pagine principali 200, nessun errore.
- [x] Jenkins (.115): job "wera2026 PROD" sulla JDK di sistema (tolto `<jdk>JDK8</jdk>`,
      backup `config.xml.bak-20261024-172546`); build #3 da fc8da71 SUCCESS con Temurin 25 +
      Maven 3.9.16: release 8, bytecode 52, jaxb + una sola activation, niente xerces, config
      prod corretto (SMTP 127.0.0.1, Quartz vuoto, jdbc/wera). Poi tool JDK8 tolto dal config
      globale (backup `/var/lib/jenkins/config.xml.bak-20261024-204548`), Jenkins riavviato,
      `temurin-8-jdk` disinstallato (senza autoremove); resta solo temurin-25-jdk.
- [x] deploy di fc8da71 su .106: build Jenkins #6 (2026-10-25 09:00, Temurin 25), WAR sha256
      5af7e310..., 41 jar con jaxb-api/jaxb-runtime/jakarta.activation e senza xercesImpl,
      bytecode 52; config prod corretto (SMTP 127.0.0.1:1025, Quartz vuoto, jdbc/wera); avvio su
      Zulu 25 senza errori, HTTP 200 su /wera2026 e /gestit, log e mailcatcher ok, crash vuota.
      (La build #5 della sera prima era fallita per il blocco notturno del FortiGate, vedi CLAUDE.md.)
- [ ] giro manuale su .106 con utente reale, incluso un upload dati rilevatori XML.

## wera2026 prod senza mail (2026-10-24)
- [x] profilo prod: `mail.SMTPHost=127.0.0.1:1025`, mittente `noreply@test.invalid`; DB e
      cartelle di wera invariati; Quartz spento; log in `/usr/share/logs/wera2026/`.
- [x] controllo finale degli script: build bloccato se `mail.SMTPHost` non e `127.0.0.1`
      (provato con LF e CRLF).
- [x] script Linux in sandbox: contenuto identico al motore PowerShell per test e prod.
- [x] deploy su 192.168.0.106 (webcont92) dal job Jenkins "wera2026 PROD", versione 213a552,
      2026-10-24 16:42 (il primo deploy delle 16:01 era 2ee7a2d con SMTP reale: sostituito,
      nessuna mail partita, solo 2 richieste da 127.0.0.1).

## Deploy wera2026 PROD su 192.168.0.106 — verifiche (2026-10-24)
- [x] config deployato: `mail.SMTPHost=127.0.0.1:1025`, TLS off, mittente `noreply@test.invalid`;
      tasklist Quartz vuota e 0 job attivi; `jdbc/wera` -> `db-wera-etc-host` = 192.168.0.67
      (DB di produzione); cartelle e `AnlConfig.xml` di wera sulla share CIFS
      `//fileserver_docs/storage/webapps`; log in `/usr/share/logs/wera2026/` sulla share
      `//fileserver_logs/storage/logs`.
- [x] Tomcat: deploy a caldo in ~5 s su Zulu 25, nessun GRAVE/SEVERE/Exception dopo l'avvio
      (allo spegnimento della versione precedente solo il noto avviso ThreadLocal e l'NPE attesa).
- [x] HTTP 200 su `/wera2026/astro?FUNCTIONID=Login` e su `/gestit/`.
- [x] log applicativo scritto (`wera_debug__2026-10-24.log` sulla share), cartella crash vuota.
- [x] mailcatcher (perl) in ascolto su 127.0.0.1:1025: mail di prova salvata in
      `/usr/share/logs/wera2026/mailcatcher/`; nessuna connessione SMTP in uscita.
- [ ] giro manuale con login reale (home, una ricerca, un inserimento, profilo) su Java 25.
- [ ] reset password e registrazione degli utenti reali: farli da `wera`, non da `wera2026`
      (da wera2026 la mail finisce nel mailcatcher e l'utente non la riceve).

## wera2026: WAR in due profili, test e prod (2026-10-23)
- [x] `make_wera2026.bat` -> `target\test\wera2026.war` e `target\prod\wera2026.war`.
- [x] config test vs produzione: 30 righe diverse (Quartz, I/O, SMTP, `jdbc/wera2026`, log);
      config prod vs produzione: 17 righe (Quartz e log; DB, SMTP e cartelle invariati);
      fine riga CRLF conservati; 1971 entry, nessun backslash.
- [x] `patch-config-test.sh` (in sandbox) produce lo stesso contenuto del motore PowerShell
      per entrambi i profili.
- [x] laptop: `/wera2026` profilo test deployato al posto di `/wera-test` (404), login ok,
      log in `D:\usr\share\logs\wera2026\` (crash compresi), nessuna traccia di Quartz.
- [ ] server di test e di produzione: deploy a cura dell'operatore (prerequisiti in deploy/README.md).

## Pacchetto di test: log isolati e guardia script email (2026-10-23)
- [x] wera-test.war rigenerato: `Logger.*` su `/usr/share/logs/wera-test/`, nessun riferimento
      attivo a `/usr/share/(webapps|logs)/wera/`, smtps/aruba, `jdbc/wera`; tasklist Quartz vuota;
      encoding config ISO-8859-1 invariato; nessuna entry con backslash.
- [x] deploy sul laptop: debug, application e crash scritti in `D:\usr\share\logs\wera-test\`,
      nessuna scrittura in `D:\usr\share\logs\wera\`.
- [x] anonimizza-email-test.sql: DB non marcato -> rifiuto (transazione annullata); DB marcato
      `WERA-TEST` -> esegue; rilancio -> 0 righe.

## Home: card "I tuoi crediti" nascosta (2026-10-23)
- [x] superuser e amministratore: card crediti non visibile, Richieste a tutta riga (>=1200px),
      donut ok, nessuno scroll orizzontale; tema `?20261023`.
- Per riattivarla: togliere la classe `wera-crediti-off` dalla riga Richieste in home.html.

## Smoke test superuser ondate 0-6 su /wera-test, DB locale (2026-10-22)
Esito della sessione (superuser, branch restyle/ui-2026-r2 deployato):
- [x] tutte le 53 voci di menu + Profilo + ScaricoDatiManuale: HTTP 200, tema caricato,
      nessuna 500; unici errori FreeMarker nelle 4 Inserimento ripartizioni (vedi sotto).
- [x] ondata 0/home: sfondo #f5f4f2, Manrope, voce attiva a gradiente; 4 card, crediti
      993.033/1.000.000, donut, Richieste 4 righe con lente e chip. Layout 1366x768 senza
      scroll orizzontale. (Su DB locale la home completa impiega ~10 s: query lente.)
- [x] ondata 2: ricerca condomini (filtri colonna, ricerca in linea, export, matita/cestino,
      legenda), inserimento condomini (8 select2, datepicker), SALVATAGGIO reale in UPDATE
      con Swal "Salvataggio completato!" + alert; aziende (tab senza card-nella-card,
      underline accento); profili ("seleziona tutti" 86/86); utenti (toggle in riga).
- [x] ondata 3: inserimentoripartizioni 5 tab e ricalcolo totali; ricerca ripartizioni.
- [ ] ondata 3: intestazioni Riscaldamento/ACS UNI2018 non verificabili (0 ripartizioni
      UNI2018 nel DB locale); storico letture e letture consumi bloccati dalla vista mancante.
- [x] ondata 4: config guidata tipo + Inizia, step a pill; step 0-6 e riepilogo rispondono,
      dropzone negli step 0 e 5.
- [x] ondata 5: dropzone import (bordo dashed), file manager (146 card), calendario
      (mese/settimana), gestione rilevatori non scaricati (265 righe, ~50 s di query).
- [x] ondata 6: summernote in news e mail config; distribution list, configurazione errori;
      SALVATAGGIO reale di un parametro con Swal.
- Bug PREESISTENTI trovati (identici al commit iniziale), CORRETTI su richiesta
  dell'operatore (nota .claude/2026-10-22-fix-bug-preesistenti-template.md):
  - [x] `configurazioneguidata1.html` r.273: `<div class="col-sm-4"` senza `>`: il campo
    Indirizzo del wizard step 1 non esisteva. Ora visibile, obbligatorio e salvato sul DB.
  - [x] `SALVATAGGIO_IN_CORSO!=''` nei 4 inserimento ripartizioni: ora il popup
    "Salvataggio in corso..." compare con flag true e non compare con false/inserimento.
- DB locale incompleto: mancano `vista_scarico_data_massima` (letture/storico), e inoltre
  `v_utenze_id_condomino`, `v_utenti_aziende_profili`, `v_dipendenti`, `gerarchia_aziende`,
  `risorse`, `tipi_richiesta`, `para_old` (citate in config.cfg; alcune forse legacy).
- File manager su /wera-test: le miniature puntano a file assenti nella cartella upload
  isolata (atteso: FileNotFoundException nel crash log, nessun impatto sul test UI).

## QA profilo amministratore su /wera-test, DB locale (2026-10-22)
Esito della sessione (DB PostgreSQL locale con email anonimizzate, vedi CLAUDE.md):
- [x] login amministratore; menu ridotto (Home, Anagrafiche, Letture, Ripartizioni, Storico, Centrali termiche).
- [x] home: 4 card popolate, "I tuoi crediti", donut, Richieste (DataTable).
- [x] Anagrafiche > Utenze > Ricerca: 128 righe (coerente con la card home), DataTable ok.
- [x] tutte le voci di menu del profilo rispondono 200 con il tema (ricerche/inserimenti
      ripartizioni Standard/UNI2018/Letture, Storico, Centrali termiche, Profilo).
- [x] Profilo: dati, mail anonimizzata, scadenza password.
- [ ] Letture consumi > ricerca per condominio: 500 per vista mancante
      `vista_scarico_data_massima` sul DB locale (restore incompleto, non e il restyle).
- Bug PREESISTENTI trovati (template/Java invariati da main, NON corretti: fuori perimetro):
  - `inserimentoripartizioni.html` r.1689: `SALVATAGGIO_IN_CORSO!=''` confronta un booleano
    con una stringa; FreeMarker in DEBUG stampa l'errore DENTRO lo `<script>` e l'intero
    blocco `$(document).ready` (5,4 KB) non viene eseguito.
  - login/registrazione/resetpassword/newpassword caricano `js/jquery-3.1.1.min.js` e
    `js/popper.min.js` che non esistono (404) -> "Bootstrap's JavaScript requires jQuery".
  - `FUNCTIONID=Login` con sessione attiva -> 500 (ClassCastException MenuItem).

## Mail: registrazione e reset con mailcatcher (2026-10-22, DB locale)
Mailcatcher `deploy/mailcatcher.pl` su 127.0.0.1:1025 (vedi CLAUDE.md).
- [x] registrazione v2 con rilevatore senza utente: pagina di esito, mail "Invio password
      provvisoria" catturata; utente creato con profilo 4 (condomino), utenza e azienda.
- [x] reset password v2: pagina di esito, mail catturata.
- [x] login con provvisoria (scade oggi) -> pagina "Nuova password" v2 con avviso
      "LA PASSWORD E' SCADUTA..."; cambio -> Home; login con la nuova -> Home;
      vecchia provvisoria rifiutata con il messaggio di errore nel dialog.
- Note (gia cosi su main, non regressioni): pagina di esito `message.html` senza `<title>`;
  in newpassword lo username non e precompilato anche se il Java lo passa al template.

## Restyle r2 — reset/new password v2 (2026-10-22)
- [ ] reset: invio email con esito server invariato; newpassword: cambio password reale ok.
- [ ] messaggi INFO/ERROR visibili quando il server li manda, nessun box vuoto quando assenti.
- [ ] link incrociati (home/dialog login/registrazione); mobile 350px: topbar su una riga.

## Restyle r2 — registrazione v2 (2026-10-22)
- [ ] registrazione con codice valido: POST identico (NOME/COGNOME/EMAIL/RILEVATORE), esito ok.
- [ ] guida "Dove trovo il codice?" con illustrazione; due colonne su desktop, impilate su mobile.
- [ ] "Torna alla home" → login; "Accedi" apre il dialog (#login).

## Restyle r2 — landing/login v2 (2026-10-22)
- [ ] login con credenziali valide; con credenziali errate il dialog si riapre da solo con l'errore.
- [ ] carosello 4 slide: autoplay 7s, pausa su hover, frecce, dot, swipe; personaggi caricati (img/landing/).
- [ ] dialog: apertura da topbar/CTA/#login, chiusura X/ESC/click fuori.
- [ ] "Password dimenticata?" e "Registrati con il codice ripartitore" funzionanti.
- [ ] mobile (375px): barra controlli sopra le slide, topbar con 2 bottoni, nessuno scroll orizzontale.

## Restyle r2 — home v2 (2026-10-22)
- [ ] card "I tuoi crediti": residui/totale (coerenti col tab Crediti del profilo), barra proporzionata, count-up.
- [ ] stati: nota ambra se scadenza ≤30 gg, rossa se crediti scaduti; "Gestisci crediti" → profilo.
- [ ] donut "Stato ripartitori" nella riga sotto le Richieste, ancora funzionante.
- [ ] profilo CON: home condomino invariata (nessuna card crediti).

## Bottoni & tab (2026-07-20)
- [ ] bottoni alla taglia del mockup (btn-sm ≈ 9px/16px @13.5px); hamburger topbar invariato.
- [ ] tab: sottolineatura accento, angoli alti arrotondati, nessuna cornice grigia (anche <768px); pannello senza scatola.

## Topbar-titolo & Richieste (2026-07-19, sera)
- [ ] breadcrumb unico nella topbar con separatore "/": ultimo elemento bold, intermedi cliccabili.
- [ ] banda titolo intermedia sparita ovunque; alert "Salvataggio dati terminato" ancora visibile dopo un salvataggio.
- [ ] titoli card scuri (Bricolage), label metric card muted sentence-case.
- [ ] Richieste in home: 4 righe (le ultime), paginazione, lente → apre la ripartizione, chip stato colorate.

## Home board (2026-07-19)
- [ ] contatori con animazione di conteggio e separatore migliaia (it-IT).
- [ ] pallini: ambra su "mai effettuato accesso" e "antenne con carica <30%", verde/rosso sugli altri stati.
- [ ] donut "Stato ripartitori" (funzionanti/in errore/non scaricati) accanto alla tabella Richieste; tooltip e legenda ok.
- [ ] card su sfondo sabbia (contrasto come nel mockup), nessun riquadro esterno che racchiude le card.
- [ ] tabella Richieste su 8 colonne: filtri colonna, matita, export invariati.

## Ondata 2 — anagrafiche
- [ ] ricercaanagraficacondomini (o altra ricerca*): filtri di colonna, "ricerca in linea", export — "Ricerca" a gradiente, PDF/XLS/CSV ghost; matita/cestino/PDF riga con hover colorato.
- [ ] legenda icone come chip discreta sotto i bottoni.
- [ ] inserimentoanagraficacondomini: select2, datepicker, salvataggio con dialog Swal.
- [ ] inserimentoaziende: tab DATI AZIENDA / DATI LEGALE RAPPRESENTANTE senza card-nella-card.
- [ ] inserimentoprofili: multiselect LISTA_MENU_ABILITATI + checkbox "seleziona tutti".
- [ ] inserimentoutenti / ricercautenti: toggle abilita/disabilita in riga.

## Ondata 3 — ripartizioni & letture
- [ ] inserimentoripartizioni: i 5 tab (TOTALE COSTI / PRE-RIPARTIZIONI / PREZZO / COSTI PER UNITÀ / GRAFICO); i totali si ricalcolano; input compatti nelle griglie.
- [ ] alert di stato (non scaricati / anomalie / differenza) leggibili; DataTable dtAnomalie ok.
- [ ] inserimentoripartizioniuni2018: intestazioni Riscaldamento (arancio) / ACS (azzurro) INVARIATE — recolor saltato di proposito (color coding funzionale, applicato anche via JS).
- [ ] ricercaripartizioni + storico/uni/uni2018: filtri, export, matite; "PDF con dettagli" (icona arancio) ok.
- [ ] ricercalettureconsumi(storico): grafici chartdiv* renderizzati.
- [ ] verificare allineamento di eventuali datepicker dentro celle tabella (input compatti).

## Ondata 4 — configurazione guidata
- [ ] configurazioneguidata: select tipo + "Inizia".
- [ ] step 1..4: indicatore step a pill (corrente su gradiente, fatti in verde, disabilitati grigi); form e salvataggi ok.
- [ ] step 0 e 5: upload dropzone (bordo dashed, hover accento), barra avanzamento a gradiente durante l'import.
- [ ] step 6: come QA ondata 3 (è la variante wizard di inserimentoripartizioni).
- [ ] configurazioneguidata0riepilogo: DataTable + export.

## Ondata 5 — import / file / rilevatori
- [ ] importdatianagraficautenze / importdatirilevatori: dropzone, progress bar, esito.
- [ ] riepilogodati*: conteggi negli alert leggibili (alert-link eredita colore stato).
- [ ] filemanager: griglia file card con hover; ricercafilemanager; archiviofileimportati.
- [ ] calendariofilemanager: navigazione mese/settimana, oggi in ambra, eventi accento cliccabili.
- [ ] gestionedatirilevatori / gestionedatisingolorilevatore / gestionerilevatori*(noncensiti, nonscaricati, senzalettura, errore): tabelle, matite, form.
- [ ] scaricodatimanuale: invio form.

## Ondata 6 — config di sistema & varie
- [ ] inserimentonews: editor summernote (toolbar, salvataggio DESCRIZIONE); ricercanews.
- [ ] inserimentomailconfig: editor summernote; ricercamailconfig.
- [ ] parametri / distributionlist / configurazionecsv / configurazioneerrori (+ inserimenti): ricerche e salvataggi.
- [ ] message.html: bottone "Continua" → funzione successiva; logo centrato.
- [ ] errore.html / errorpage.html: pagina 500 con card e Bricolage; bottone Home.
- [ ] mail (invio email): NON toccata — verificare solo che le mail arrivino come prima.

## Centrale termica wera2026 (2026-10-25, solo laptop)
Nota: `.claude/2026-10-25-centrale-termica-pagina.md` (backend: `...-java-upload-download.md`).
- [ ] ADM / INS / AMM: voce "Centrale termica" nel menu; CON: nessuna voce e pagina -> Home
- [ ] vecchia voce "Centrali termiche": visibile ad ADM/INS, nascosta ad AMM, intatta per CON
- [ ] scelta condominio (solo quelli del profilo), riquadri, cartelle, ultimi documenti, scadenze
- [ ] carica documento + nuova versione; Apri / Scarica / Scarica selezionati (zip); Versioni
- [ ] Archivia / Ripristina (ADM, INS); AMM in sola lettura, niente Stato pagamenti
- [ ] telefono: una colonna, nessuno scorrimento orizzontale
- [ ] scadenze: Nuova (solo ADM/INS), Eseguita con rinnovo automatico, Annulla, Storico
- [ ] configurazione (solo ADM): sezioni e permessi, tipi di scadenza, parametri con eccezioni, categorie WERA
- [x] DDL su .75 PROD (2026-10-25 22:55): oggetti e dati iniziali verificati, struttura di public identica
- [x] .106 (2026-10-25 22:59): share /usr/share/webapps/wera scrivibile da Tomcat anche nella sandbox; backup WAR in /root
- [x] PROD deploy 2026-10-25 23:39 (Jenkins): config patchata, log puliti, HTTP 200 su wera2026, /wera, gestit
- [ ] PROD dopo il deploy: verifiche per profilo della Fase 5 del piano di produzione

## Rollback rapido
Rimuovere (o commentare) le 5 righe aggiunte in fondo al blocco CSS di `included_head.include`: l'app torna esattamente all'aspetto precedente. Per le sole pagine standalone (errore/errorpage/message) rimuovere i 4 `<link>` aggiunti nell'head.
