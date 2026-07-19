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
