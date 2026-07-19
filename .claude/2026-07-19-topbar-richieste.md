# 2026-07-19 — Topbar-titolo, titoli card, Richieste come mockup

## Obiettivo
Quattro allineamenti al mockup richiesti dopo il primo QA visivo:
1. titoli dei box come nel demo (Bricolage scuro, non eyebrow maiuscoletto)
2. "Richieste di ripartizione in attesa" come lista del mockup (4 righe, lente, chip stato)
3. breadcrumb unico con "/" al posto dei due breadcrumb attuali
3.1 breadcrumb come titolo pagina nella topbar, rimossa la banda intermedia

## File toccati
- `include/included_header.include` — nel navbar, dopo l'hamburger, nuovo blocco `.topbar-crumb`:
  catena completa PathDescriChain/LinkChain (stessa logica del vecchio breadcrumb destro),
  separatore "/", ultimo elemento evidenziato, tutto guardato da `SELECTED_MENU_ITEM?exists`.
- `include/included_box_title.include` — svuotato del doppio titolo/breadcrumb (spostato in
  topbar); restano SOLO i 4 div `.alert-message.hidden-div` usati dai salvataggi. La banda
  `.ibox-title.titolo` presente in tutti i template ora collassa a zero via CSS (sez. 27),
  e torna visibile solo quando il JS mostra un alert.
- `home.html` (byte-level, ISO-8859-1/CRLF ok) — DataTable Richieste in stile mockup:
  rimosso il blocco filtri di colonna clonati; pageLength 4 + lengthChange false;
  dom 'tp' (solo tabella+paginazione); matita -> LENTE (fa-search, STESSO handler editRow
  che apre la ripartizione); colonne visibili ridotte a NR/Data/Denominazione/Localita/Stato;
  Stato renderizzato come chip colorata (approv*->verde, rifiut*/annull*->rosso, altro->ambra);
  aaSorting [[2,'desc']] con type num: le ultime richieste per prime (il dataset e una UNION
  senza ORDER BY).
- `css/wera-theme.css` — sez. 16 aggiornata: `.ibox-title h5` = titolo card mockup (Bricolage
  17px scuro); nuova sez. 27: label metric card muted 14px (`.metricbox`), `.topbar-crumb`,
  collasso `.ibox-title.titolo`, tabella Richieste senza bordi/zebra, `.stato-chip` (ok/warn/ko).
- Cache-buster tema -> `?20260719b` (include + errore/errorpage/message).

## Cosa NON e stato toccato (conferma)
Handler editRow/formEdit, ajax DSRipartizioniHome, modali, blocco CON: invariati. Il vecchio
markup della banda titolo nei template NON e stato rimosso (collassa via CSS): zero rischi di
regressione sugli 80 template.

## QA
- [ ] topbar: titolo "Home" sulla home; "Home / Anagrafiche / Antenne / Ricerca" nelle ricerche;
      i livelli intermedi sono link funzionanti
- [ ] la banda titolo intermedia non occupa piu spazio; il messaggio "Salvataggio dati terminato
      con successo" compare ancora dove previsto (fare un salvataggio)
- [ ] titoli card scuri ("Stato ripartitori", "Richieste di ripartizione...") e label metric
      card muted (Utenze, Condomini, ...)
- [ ] Richieste: 4 righe, ultime per prime, paginazione, lente che apre la ripartizione,
      chip di stato colorate
- [ ] mobile/tablet: il titolo topbar tronca con ellissi senza spingere fuori i link utente

## Rischi / punti aperti
- La chip di stato deduce il colore dal testo DESCR_STATO (approv/rifiut/annull): eventuali
  stati con descrizioni diverse cadono su ambra (neutro "in corso").
- `h4.page-title` e le sue regole (filo gradiente) restano nel tema ma non hanno piu markup.
