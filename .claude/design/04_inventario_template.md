# WERA — Inventario superficie da restylizzare

Percorsi: template in `src/main/webapp/WEB-INF/template/`, layout condivisi in `.../template/include/`, asset statici in `src/main/webapp/{css,js,fonts,img,font-awesome}`.

CSS attuali caricati (in `included_head.include`): `bootstrap.min.css`, `font-awesome`, plugin (jasny, datepicker, daterangepicker, select2, dataTables) e — più avanti — `css/style.css` (INSPINIA) + `css/bow.css`. **Il nuovo `wera-theme.css` va linkato per ULTIMO** così vince sugli altri.

## Ondata 0 — Layout condivisi (massima leva: ~80% del risultato)
`include/included_head.include`, `included_header.include`, `included_menu.include`, `included_footer.include`, `included_box_title.include`, `included_box_crediti.include`, `divs_and_form.include`

## Ondata 1 — Auth & dashboard
`login.html`, `registrazione.html`, `resetpassword.html`, `newpassword.html`, `profilo.html`, `home.html`

## Ondata 2 — Anagrafiche (inserimento + ricerca)
inserimento/ricerca: `anagraficautenze`, `anagraficacondomini`, `anagraficacontatori`, `anagraficaantenne`, `anagraficatecnici`, `anagraficasoftware`; `aziende`, `utenti`, `profili`, `domini`

## Ondata 3 — Ripartizioni & letture (i più complessi)
`inserimentoripartizioni(.html/letture/uni/uni2018)`, `ricercaripartizioni(letture/storico/uni/uni2018/unistorico/unistorico2018)`, `ricercalettureconsumi(storico)`, `ricercascaricostorico`

## Ondata 4 — Configurazione guidata (wizard 0→6)
`configurazioneguidata0..6`, `configurazioneguidata0riepilogo`, `configurazioneguidata`

## Ondata 5 — Importazione / file / rilevatori
`importdatianagraficautenze`, `importdatirilevatori`, `fileuploaddati*`, `importfilemanager`, `filemanager`, `ricercafilemanager`, `calendariofilemanager`, `archiviofileimportati`, `riepilogodati*`, `scaricodatimanuale`, `gestionedatirilevatori`, `gestionedatisingolorilevatore`, `gestionerilevatori*`, `inserimentogestionerilevatorierrore`

## Ondata 6 — Config di sistema & varie
`inserimento/ricerca` di: `parametri`, `mailconfig`, `distributionlist`, `news`, `configurazionecsv`; `configurazioneerrori`, `inserimentoconfigurazioneerrori`; `mail.html`, `message.html`, `errore.html`, `errorpage.html`

Totale: **84 template** + 7 include condivisi. Le cartelle `template/non usate` e `css/non usati` sono da ignorare (codice morto).
