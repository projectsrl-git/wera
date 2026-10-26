# 2026-10-26 — Deploy wera2026: backup come passo del job e opzione ROLLBACK

## Obiettivo
Il backup del WAR precedente, finora fatto a mano in `/root` prima di ogni rilascio, diventa un passo
del job Jenkins "wera2026 PROD" quando e' spuntato DEPLOY. Nuova opzione ROLLBACK, alternativa a
DEPLOY, per tornare alla versione precedente se un rilascio va storto. Richiesta dell'operatore.

## File toccati (repo)
- `deploy/jenkins-wera2026.ps1` — switch `-Rollback` (ROLLBACK=true), rifiutato insieme a `-Deploy`.
- `deploy/README.md` — procedura di rilascio con backup e rollback, tabella dei comandi di `deploy-war`.
- `CLAUDE.md` — paragrafo Deploy aggiornato (entrato per errore nel commit `75d5994` di un'altra
  sessione, che ha committato l'intero file mentre la modifica era in corso: il contenuto e' giusto).

## Modifiche fuori repo (gia' in esercizio)
- **192.168.0.106** `/usr/local/sbin/deploy-war` (forced command dell'utente `deploy`; backup della
  versione precedente in `/root/backup-20261026-deploy-war/deploy-war.v2`). Comandi:
  `<app> [deploy [etichetta]] < war`, `<app> backup`, `<app> rollback`, `<app> list`.
  - Backup `/var/backups/deploy-war/<app>.war.bak-AAAAMMGG-HHMMSS-<etichetta>` + `.sha256`, ultimi 8,
    `<app>.war.prev` = symlink al piu' recente; nessun doppione (stesso sha256).
  - Ordine dei backup = ordine in cui le versioni sono uscite di esercizio (una versione gia' salvata
    che torna in esercizio viene rinominata con la data nuova): il rollback torna sempre alla versione
    attiva subito prima, e ripetuto risale la storia.
  - Il WAR tolto da un rollback e mai salvato va in `<app>.war.scartato-*` (ultimi 2, esclusi).
  - Copiati (originali intatti) i 3 backup manuali di `/root`: `...-20261025-090000-fc8da71`,
    `...-20261026-085400-ondata3-baa96d2c`, `...-20261026-100300-4320eeb`; `wera2026.current` = `f535152`.
- **192.168.0.115** job "wera2026 PROD" (backup `config.xml.bak-20261026-101803-pre-rollback` nella
  cartella del job; riavvio di Jenkins): parametro ROLLBACK; sezione SCM tolta e checkout Git in un
  passo shell (il rollback non deve dipendere da Internet, aperto solo 06-23); Maven via shell; passi
  1 azione/rollback, 2 sorgenti, 3 maven, 4 patch prod, 5 backup (solo DEPLOY), 6 deploy + elenco.
  Effetto collaterale: la pagina della build non mostra piu' le "Changes" Git (la revisione e' in console).

## Cosa NON e' stato toccato (conferma)
Codice Java, `pom.xml`, `config.cfg`, SQL, template FreeMarker, `patch-config-test.sh`: invariati.
Job "TEST - build gestit" invariato (gestit ha comunque i backup datati, senza rollback nel job).

## QA
- [x] `deploy-war` in sandbox `/tmp` su .106: catene deploy/rollback/redeploy, rotazione dei backup,
      WAR identico, nessun backup, input non validi (`../../etc`, `;`, argomenti in piu').
- [x] `wera2026 list` / `gestit list` come utente `deploy` con la chiave di Jenkins.
- [x] Build #13 senza spunte: SUCCESS (checkout dal passo shell, build, patch, artefatto).
- [x] Build #14 con DEPLOY e ROLLBACK insieme: FAILURE al primo passo, nessun artefatto.
- [ ] DEPLOY e ROLLBACK reali: li prova l'operatore (proposta: DEPLOY dello stesso commit in
      esercizio, poi ROLLBACK, stesso codice prima e dopo).

## Rischi / punti aperti
- Il rollback rimette solo il WAR: se una release ha applicato DDL su .75, il rollback NON li annulla
  (finora i DDL di wera2026 sono additivi, in schemi propri).
- Per tornare indietro da questa modifica: rimettere `config.xml.bak-20261026-101803-pre-rollback` e
  riavviare Jenkins; rimettere `deploy-war.v2` (i backup datati restano usabili a mano).
