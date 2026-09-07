# 2026-07-20 — Bottoni alla taglia del mockup, tab senza cornice

## Obiettivo
Due fix dal QA visivo: (1) i bottoni dell'app risultavano piu piccoli del mockup;
(2) i tab avevano cornice grigio scuro e spigoli vivi invece della sottolineatura del demo.

## Analisi
1. I template usano quasi ovunque `btn btn-primary btn-sm`: la taglia small di Bootstrap
   (5px/10px @12px) e molto sotto lo "small" del demo (9px/16px @14px).
2. Le regole INSPINIA `.tabs-container .nav-tabs > li.active > a` (piu specifiche della
   sezione 9 del tema) imponevano `border:1px solid #e7eaec` + sfondo bianco, e il
   `.panel-body` era una scatola bordata; a <768px un `!important` INSPINIA forzava
   di nuovo il bordo grigio.

## File toccati
- `css/wera-theme.css` — sezione 28:
  - `.btn` 10px/18px @14px; `.btn-sm` 9px/16px @13.5px raggio 9px (taglia small del demo);
    eccezione `.btn.navbar-minimalize` per non gonfiare l'hamburger
  - `.tabs-container .nav-tabs`: sottolineatura accento sul tab attivo, angoli superiori
    arrotondati 9px, hover soft su surface-2, nessun bordo scatola; `.panel-body`
    trasparente senza cornice; contro-override con `!important` nella media query <768px
- Cache-buster -> `?20260720` (include + errore/errorpage/message).

## Cosa NON e stato toccato (conferma)
Nessun template modificato (solo il bump del cache-buster). Hook tab (data-toggle, id tab-N),
handler bottoni: invariati.

## QA
- [ ] bottoni Ricerca/Salva/PDF/XLS/CSV: taglia come il mockup, hamburger topbar invariato
- [ ] inserimentoaziende / profilo / inserimentoripartizioni: tab con sottolineatura accento,
      niente cornice grigia; contenuto del pannello senza scatola
- [ ] tab su mobile (<768px): stato attivo con accento (non bordo grigio)
- [ ] modali: bottoni Si/No leggermente piu grandi ma coerenti

## Rischi / punti aperti
- L'aumento di taglia dei bottoni allunga leggermente le righe form: verificare in QA
  eventuali wrap indesiderati su viewport stretti.
