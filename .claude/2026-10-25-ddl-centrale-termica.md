# 2026-10-25 — Centrale termica wera2026: modello dati e DDL (ondata 1a)

## Obiettivo
Schema DB della nuova gestione documentale "Centrale termica" di wera2026, parallela a quella di
/wera, disegnato e approvato con l'operatore. Solo DDL e documentazione: nessun Java, config.cfg,
template o oggetto esistente toccato.

## File toccati
- `src/resources/sql/wera2026/001-centrale_termica-ddl.sql` — NUOVO: schema `centrale_termica`
  (9 tabelle, 2 viste, 2 funzioni, trigger di storico, dati iniziali), idempotente, PostgreSQL 9.4.
- `.claude/2026-10-25-disegno-db-centrale-termica.md` — NUOVO: disegno approvato (scelte, convenzioni,
  colonne, prova).
- `.claude/2026-10-25-disegno-centrale-termica-2026.md` — rimando al nuovo modello dati.
- `.claude/2026-10-25-ddl-centrale-termica.md` — questa nota.

## Oggetti creati (schema `centrale_termica`)
`sezioni`, `sezioni_profili`, `documenti`, `versioni`, `tipi_scadenza`, `scadenze`, `parametri`,
`categorie_wera`, `eventi`; viste `v_documenti_wera`, `v_documenti`; funzioni
`valore_parametro(chiave, id_azienda)`, `fn_storico_immutabile()`.

## Cosa NON e stato toccato (conferma)
Java, `pom.xml`, `config.cfg`, template, tabelle/viste/dati di `public`. La vista `v_documenti_wera`
LEGGE `public.elenco_file_importati_file_manager` e `public.parametri`, senza modificarle.
Nessuna FK verso tabelle di /wera.

## QA
- [x] DDL eseguito SOLO sul DB locale del laptop (127.0.0.1, `wera`, marcato `WERA-TEST`), due volte:
  exit 0, il secondo giro non cambia nulla.
- [x] Prove in transazione annullata: versioni e versione corrente, override parametri per azienda,
  trigger di storico (UPDATE/DELETE/TRUNCATE rifiutati), vincoli univoci e CHECK, viste sui dati di /wera.
- [ ] `mvn clean package`: non necessario (nessun sorgente cambiato).
- [ ] DB di produzione .75: NON eseguito; serve un ok a parte, poco prima del deploy.

## Rischi / punti aperti
- Valori iniziali di `tipi_scadenza` (periodicita) e corrispondenze `categorie_wera` (cartello in
  Documentazione tecnica) da far verificare a SIRBI: si cambiano dalla configurazione, non dal DDL.
- Contatti dell'assistenza vuoti finche non li inserisce l'operatore dalla configurazione.
- Su .75 l'utente del datasource e `postgres` (proprietario): se cambiasse servono i GRANT commentati
  in fondo allo script.
- Le sequence consumate dalle prove sul DB locale non hanno effetti (DB sacrificabile).
