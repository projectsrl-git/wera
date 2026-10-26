# 2026-10-23 — Home: card "I tuoi crediti" nascosta

## Obiettivo
Su decisione dell'operatore la card "I tuoi crediti" della home (introdotta in home v2,
commit 27f339f) viene nascosta per ora. Markup e JS restano nel template, pronti per essere
riattivati.

## File toccati
- `home.html` (ISO-8859-1, edit byte-level) — riga delle Richieste: classe interruttore
  `wera-crediti-off` sulla `.row`, classi `richieste-col` e `creditibox-col` sulle due colonne
  (solo classi aggiunte, nessun id/name/cablaggio toccato).
- `wera-theme.css` sez. 29 — `.wera-crediti-off .creditibox-col{display:none}` e, da 1200px,
  `.wera-crediti-off .richieste-col{width:100%}`: con la card nascosta le Richieste occupano
  tutta la riga.
- cache-buster tema `?20261022` -> `?20261023` in `include/included_head.include`
  (ISO-8859-1, byte-level), `errore.html`, `errorpage.html`, `message.html`.

## Come riattivarla
Togliere la classe `wera-crediti-off` dalla `<div class="row ...">` delle Richieste in
`home.html` (e aggiornare il cache-buster solo se si tocca anche il CSS).

## Cosa NON e stato toccato (conferma)
Java, `config.cfg`, SQL, direttive FreeMarker. La chiamata JSONDataSet a DSCrediti della home
resta attiva (la card e solo nascosta): costo trascurabile, nessun dato mostrato.

## QA (su /wera-test, DB locale, WAR ricostruito e ridistribuito)
- [x] superuser e amministratore a 1366x768: card crediti presente nel DOM ma `display:none`;
      Richieste larghe quanto la riga (1085px), 4 righe; donut ok; nessuno scroll orizzontale
- [x] tema servito con `?20261023`

## Rischi / punti aperti
- Sotto i 1200px le colonne Bootstrap erano gia impilate: nessun cambiamento.
- Profilo condomino: la card non era comunque mostrata (home diversa), invariato.
