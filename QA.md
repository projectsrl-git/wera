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
- Bug PREESISTENTI trovati (identici al commit iniziale, NON corretti: fuori perimetro):
  - `configurazioneguidata1.html` r.273: `<div class="col-sm-4"` senza `>` -> l'input
    INDIRIZZO diventa attributo del div: il campo Indirizzo del condominio NON esiste nello
    step 1 del wizard (ne visibile ne inviato). Fix = un `>`, ma attiva un campo `required`:
    da decidere con l'operatore.
  - `SALVATAGGIO_IN_CORSO!=''` anche in inserimentoripartizioniuni / uni2018 / letture
    (stesso difetto di inserimentoripartizioni): il popup "Salvataggio in corso..." non
    compare mai.
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

## Rollback rapido
Rimuovere (o commentare) le 5 righe aggiunte in fondo al blocco CSS di `included_head.include`: l'app torna esattamente all'aspetto precedente. Per le sole pagine standalone (errore/errorpage/message) rimuovere i 4 `<link>` aggiunti nell'head.
