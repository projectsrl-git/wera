# 2026-10-26 — Centrale termica wera2026: icone delle sezioni nella Panoramica

## Obiettivo
Richiesta dell'operatore: nella Panoramica usare per ogni cartella l'icona della sezione (quella scelta in
Configurazione > Sezioni, es. `fa-book`, `fa-file-text-o`, `fa-user`) al posto dell'icona cartella uguale
per tutte, per coerenza con la pagina di configurazione.

## File toccati
- `src/main/webapp/js/wera-centrale-termica.js` — `cartella()`: icona = `ICONA` della sezione (gia'
  restituita da `DSCentraleTermicaSezioni`), validata come `fa-[a-z0-9-]` (stessa regola del server);
  vuota o non valida -> `fa-folder-o` come prima. Aggiunta `fa-fw` per allineare icone di larghezza diversa.
- `src/main/webapp/WEB-INF/template/centraletermica.html` — cache-buster del JS `?20261026c`.

## Cosa NON e stato toccato (conferma)
Java, config.cfg, dataset, tema CSS, cablaggio: invariati. Nessun dato in PROD modificato.

## QA
- [x] `node --check` del JS; file ASCII.
- [ ] Controllo visivo nel browser dopo il deploy (operatore).
- Va in PROD col prossimo rilascio (merge su `release/wera2026` + job Jenkins).
