# 2026-10-25 — Landing: statistiche allineate ai numeri reali (difetto D1)

## Obiettivo
L'operatore ha notato che la landing mostrava "4.596 ripartitori gestiti" e "49 condomini
attivi", mentre la home reale ne mostra altri: erano i numeri del mockup (`wera_demo/index.html`
r.377-378) copiati nel restyle r2 (commit 872e4c1). Richiesta: numeri precisi.

## File toccati
- `src/main/webapp/WEB-INF/template/login.html` r.39-40 — `4.596` -> `4.838`, `49` -> `54`
  (valori della home del superuser in PROD il 2026-10-25: card Ripartitori e Condomini). Solo
  testo; nessuna direttiva FreeMarker, nessun cablaggio. File ASCII, edit byte-level.
- `CLAUDE.md` — i numeri sono testo fisso da riaggiornare a mano.
- `QA.md` — D1 chiuso nel repo.

## Cosa NON e stato toccato (conferma)
Java, `config.cfg`, CSS (nessun cache-buster da aggiornare), altri template.

## QA
- [x] laptop, /wera2026 profilo test: template copiato nella webapp esplosa, la landing serve
      4.838 e 54 (prima copia salvata nello scratchpad della sessione)
- [ ] PROD: dopo il prossimo deploy Jenkins del branch

## Rischi / punti aperti
- I numeri restano statici e torneranno a divergere quando cambiano utenze/condomini. Renderli
  dinamici richiede o un dataset leggibile senza login (il `JSONDataSet` attuale prende la
  WHERECONDITION dal client: da non esporre in una pagina pubblica) o un valore passato dal Java
  alla pagina di login: entrambe le strade toccano `config.cfg`/Java e servono l'ok dell'operatore.
