# 2026-07-19 — Home board come nel mockup (count-up, dot, donut, contrasto)

## Obiettivo
Avvicinare la dashboard home al mockup wera_demo: contatori animati, pallini semantici,
grafico "Stato ripartitori", card sullo sfondo --bg senza doppia cornice.

## File toccati
- `src/main/webapp/WEB-INF/template/home.html` (edit BYTE-LEVEL via perl: ISO-8859-1 e CRLF
  preservati, verificato con `file` — unico byte non-ASCII, la à di "Località", intatto):
  1. count-up: helper `weraCountUp` (easing cubico 900ms, formato it-IT, come il runCount del
     demo); `fetchData` ora anima i 4 contatori h1 e continua a scrivere testo semplice nelle
     righe di dettaglio. I valori vengono anche memorizzati in `window.weraStatValues`.
  2. dot: "0 mai effettuato accesso" (div_no_accesso) e "carica <30%" (div_batteria) passano
     da rosso ad AMBRA (`text-warning`), come nel demo; il resto era gia coerente.
  3. donut "Stato ripartitori" (nuova card col-lg-4 a destra della tabella Richieste, che
     scende a col-lg-8): amcharts pie (libreria GIA caricata in pagina) alimentato dai valori
     gia scaricati da DataSetStat31/32/34 — NESSUNA chiamata ne dataset aggiuntivi. Colori
     teal/rosso/ambra come la legenda del demo; empty-state se totale 0.
  4. classe `homeboard` sulla row contenuti (solo aggiunta).
- `src/main/webapp/css/wera-theme.css` — sezione 26: `.homeboard` su sfondo --bg e contenitore
  esterno de-cardizzato (era la "card che racchiude le card" e causava il bianco-su-bianco:
  il token --bg e IDENTICO al demo, il contrasto mancava perche la page-heading e bianca);
  `.fa-circle.text-warning` ambra; dimensioni donut.

## Punti saltati (deliberato)
- "Andamento letture mensili (HCA)": NON esiste un dataset che restituisca la serie mensile
  (i DataSetStat* sono conteggi singoli; i DSLettureConsumi*GraficoHCA danno solo l'ultimo
  scarico o il dettaglio per condomino). Servirebbe un nuovo `DS.*.Query` in `config.cfg`,
  che e nella lista DA NON TOCCARE: da aggiungere a cura dell'operatore (una query
  MESE/VALORE aggregata dallo scarico). Fatto quello, il grafico e ~20 righe di JS + una card.

## Cosa NON e stato toccato (conferma)
DataTable #dt e suoi handler, formEdit, blocco CON (grafici condomino), include, id esistenti:
invariati. Nessun dataset/config/Java modificato.

## Post-deploy (stesso giorno): fix cache CSS
Dal primo deploy il punto "contrasto/riquadro esterno" risultava invariato e il donut mostrava
solo la legenda: il link al tema aveva ancora il cache-buster `?20260716` (ondata 0), quindi il
browser serviva la copia in cache SENZA le sezioni 21-26 (i pallini ambra arrivavano dal
`.text-warning` di INSPINIA, non dal tema). Fix:
- `included_head.include`: cache-buster aggiornato a `?20260719` (regola aggiunta a CLAUDE.md);
- `home.html`: dimensioni di `#chartdiv-ripartitori` duplicate nello `<style>` di pagina
  (belt&braces, immune alla cache del CSS).

## QA
- [ ] home (profilo non-CON): i 4 contatori si animano e mostrano il separatore migliaia
- [ ] dettagli card: pallini verdi/rossi/ambra (ambra su "mai effettuato accesso" e "carica <30%")
- [ ] donut Stato ripartitori con legenda e valori; tooltip al passaggio
- [ ] card su sfondo sabbia con contrasto (niente riquadro esterno)
- [ ] tabella Richieste su 8 colonne: filtri, matita, export ancora ok
- [ ] profilo CON: grafici condomino invariati
- [ ] verificare che le UTENZE con browser datati (IE11?) non siano un requisito: il count-up
      usa requestAnimationFrame/performance.now (gia usati altrove nel progetto)
