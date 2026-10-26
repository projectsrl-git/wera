# 2026-10-26 — Registrazione amministratore, ondata 1: schema `registrazioni`

## Obiettivo
Oggetti DB della registrazione amministratore di wera2026 (disegno approvato:
`2026-10-26-disegno-registrazione-amministratore.md`). Le richieste stanno in uno schema nuovo;
l'utente di /wera nasce solo all'approvazione (ondata 3).

## File toccati
- `src/resources/sql/wera2026/003-registrazioni-ddl.sql` — NUOVO. Schema `registrazioni`:
  `richieste` (dati del soggetto, referente, hash della password scelta, consenso, stato ed esito della
  verifica ripartitori), `allegati` (foto), `parametri` + `valore_parametro()` (come centrale_termica),
  `eventi` (append-only, trigger), vista `v_richieste` (senza hash, IP, user agent). 42 parametri iniziali,
  nessun dato personale (destinatari e contatti vuoti).
- `.claude/2026-10-26-disegno-registrazione-amministratore.md` — disegno, ora APPROVATO.

## Vincoli nel DB
- Una sola richiesta IN_ATTESA per email (case-insensitive) e per tipo + P.IVA: indici univoci parziali,
  reggono anche gli invii contemporanei. Dopo un rifiuto si puo ripresentare.
- P.IVA 11 cifre, CAP 5 cifre, provincia 2 lettere maiuscole; APPROVATA richiede `id_utente`,
  RIFIUTATA richiede il motivo. Nessuna FK verso /wera.

## Cosa NON e stato toccato
Nessun oggetto di /wera; Java, config.cfg, template: invariati in questa ondata.

## QA (DB locale del laptop, marcato WERA-TEST, PostgreSQL 9.4)
- [x] DDL lanciato due volte: exit 0 (idempotente), 5 tabelle/viste, 42 parametri.
- [x] Prove in transazione annullata: rifiutate email duplicata in attesa (anche con maiuscole), P.IVA
  malformata, APPROVATA senza utente, UPDATE su eventi; ammessa la ripresentazione dopo un rifiuto;
  `valore_parametro` e `v_richieste` ok.
- [ ] .75 PROD: NON toccato. Servira un ok a parte, poco prima del deploy.

## Rischi / punti aperti
- `ATTIVAZIONE.AZIENDA_CODICE` = `SIRBI` (codice sul DB locale): verificarlo in sola lettura su .75 prima
  del deploy.
- In PROD impostare `MAIL.ATTIVA=N` finche l'SMTP di wera2026 e il mailcatcher (come `ALERT.ATTIVO`).
