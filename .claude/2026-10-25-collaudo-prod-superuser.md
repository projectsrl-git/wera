# 2026-10-25 — Collaudo PROD /wera2026, superuser, casi di sola lettura

## Obiettivo
Primo giro di collaudo sulla produzione pilota (https://weraweb.it/wera2026/, DB .75, share .81),
casi A e B della checklist `.claude/2026-10-25-checklist-collaudo-prod.md`, partendo da Letture
consumi e file manager (richiesta dell'operatore).

## Metodo
Login fatto dall'operatore nel browser integrato; l'agente ha navigato nella sessione aperta
(viewport 1366x768), verificato DOM/rete/console e letto crash e log su .106. Solo GET e aperture
in sola lettura (lente `READONLY=Y`, schede aperte senza Salva); nessun salvataggio, upload,
export scaricato, invio di form pubblici.

## Esito
Nessuna regressione del restyle ne della build JDK 25 sui dati reali. Dettaglio e difetti in
`QA.md`, sezione "Collaudo PROD /wera2026 — superuser, sola lettura".
- D1 (restyle r2): numeri fissi nella landing (4.596 / 49 dal mockup).
- D2 (preesistente): nomi file non codificati negli URL di download (file con "+").
- D3 (cosmetico): separatore delle migliaia inglese nella legenda del donut.

## File toccati
- `QA.md` — esiti e difetti. Nessun file applicativo modificato.

## Rischi / punti aperti
- 0 ripartizioni UNI2018 anche in produzione: intestazioni Riscaldamento/ACS non verificabili.
- Ultima lettura dei condomini provati 27/07/2025 e ultimo import 19/10/2026: da confrontare con /wera.
- Restano: profili amministratore e condomino (C, D), casi N/W/E su indicazione dell'operatore.
