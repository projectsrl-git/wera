# 2026-10-25 — PROPOSTA: documenti della Centrale termica in 6 sezioni (richiesta di Andrea Speroni)

STATO: SUPERATA (2026-10-25) da `2026-10-25-disegno-centrale-termica-2026.md`: l'operatore ha scelto
una funzione nuova e parallela, solo per wera2026, invece di raggruppare le categorie nella pagina attuale.
L'analisi dello stato attuale qui sotto resta valida.

## Richiesta (sintesi)
Nella sezione Centrale termica, lato amministratore (e condomino), sostituire la lista di ~15 voci con
6 sezioni: Libretto impianto · Nomina Terzo Responsabile · Manutenzioni e controlli · Dichiarazioni
di conformita · Sicurezza (VVFF e INAIL) · Documentazione tecnica. Fuori dai documenti: foto (nella
scheda impianto), segnalazioni (funzione separata), Consigli; Stato pagamenti in una sezione
riservata; il cartello della centrale fra i documenti. Prima i documenti aggiornati, lo storico in
archivio senza cancellarlo. Mockup: pagina "Centrale termica" con scelta del condominio, 3 riquadri
(documenti disponibili, prossima scadenza, ultimo intervento), 6 cartelle, "Ultimi documenti
aggiornati" + "Consulta archivio storico", riquadro scadenze, riquadro "Assistenza SIRBI".

## Come funziona oggi (analisi del codice e dati PROD in sola lettura)
- Categorie: tabella `PARAMETRI` con `DOMINIO='DOC'`, dataset `DSCategoriaDocumenti`
  (config.cfg:2537). In PROD 16 voci, 001-016 (016 INAIL aggiunto a mano: non c'e negli script SQL).
- File: tabella `elenco_file_importati_file_manager` (`id_condominio`, `id_categoria`, `nome_file`,
  `data_import`, `ora_import`, `data_prima_segnalazione`, `gg_segnalazione`); file su disco in
  `upload/` con il nome originale. **Nessun versionamento**: un file con lo stesso nome sovrascrive
  il precedente su disco, mentre nel DB resta una riga per ogni caricamento.
- "Visualizzazione" (`filemanager.html`): **16 schede scritte a mano** e legate ai codici
  (`#files_001`..`#files_016`); il download funziona solo in "Tutti i file" (nelle schede per
  categoria il link e `#`); blocco della scheda 5 duplicato. Dati da `DSFileManager`, gia filtrato
  per profilo lato server (AMM: condomini di cui e amministratore; CON: condomini delle sue utenze).
- Menu: da DB (`MENU_PROFILI`). Negli script di setup CON vede anche "Importazione file" e AMM non
  vede "Centrali termiche"; sul DB locale invece AMM la vedeva: **da verificare sul DB di PROD**.
- Il mockup `wera_demo` non ha questa pagina: lo "Scadenziario C.T." di app.html e organizzato per
  scadenze normative (CPI, INAIL, RCEE...), non per le 6 sezioni.

## Vincolo decisivo
/wera (.70) e /wera2026 (.106) usano lo STESSO DB (.75). Se si cambiano o accorpano i codici in
`PARAMETRI`, o si ricodifica `id_categoria` sui file, la "Visualizzazione" di /wera (16 schede
legate ai codici) si rompe. Quindi le 6 sezioni vanno ottenute **nella pagina di /wera2026,
raggruppando i 16 codici esistenti**, senza toccare i dati.

## Proposta
### Fase 1 — solo template (nessun Java, nessun config.cfg, nessun dato)
Nuova "Visualizzazione" in `filemanager.html` sul layout del mockup, stessi dati (`DSFileManager`):

| Sezione | Codici esistenti |
|---|---|
| 1. Libretto impianto | 005 |
| 2. Nomina Terzo Responsabile | 004 |
| 3. Manutenzioni e controlli | 009 interventi ordinari, 010 straordinari, 008 analisi dell'acqua |
| 4. Dichiarazioni di conformita | 007 |
| 5. Sicurezza · VVFF e INAIL | 013, 016 |
| 6. Documentazione tecnica | 006 documenti caldaia, 003 cartello centrale termica (*) |
| fuori dalle 6 (solo ADM/INS, in "Altri file") | 001, 002 foto · 011, 012 segnalazioni · 014 stato pagamenti · 015 consigli |

- Ogni cartella si apre sui documenti della sezione, **il piu recente in cima** (data e ora di
  caricamento); quelli precedenti in "Archivio storico" (si vedono, niente viene cancellato).
- Riquadri: documenti disponibili (conteggio), prossima scadenza (da data prima segnalazione +
  intervallo, gli stessi campi del calendario; "Da verificare" se manca), ultimo intervento
  (data piu recente fra 009/010).
- "Ultimi documenti aggiornati": tabella con Apri / Scarica (link sistemati: oggi nelle schede
  non si scaricano; nome file codificato, cosi si chiude anche il difetto D2 del "+").
- Riquadro scadenze: segnala le sezioni senza scadenza registrata (es. Sicurezza).
- Scelta condominio come oggi (gia limitata al profilo).
- AMM e CON vedono SOLO le 6 sezioni; ADM/INS vedono anche "Altri file". /wera non cambia.
- In "Importazione file" (solo template): per AMM/CON la select delle categorie mostra solo quelle
  delle 6 sezioni.

### Fase 2 — richiede il tuo ok (config.cfg / Java / dati condivisi)
- "Scarica selezionati" (zip di piu file): nuova funzione Java.
- Prove di combustione come voce a se: nuovo codice in `PARAMETRI` (017), visibile anche da /wera
  solo in "Tutti i file". In alternativa restano in "Interventi ordinari" (fase 1).
- Versioni vere: oggi lo stesso nome file sovrascrive su disco la versione precedente; per un
  archivio storico affidabile il caricamento deve salvare con un nome univoco (Java).
- Calendario: `DSCalendarioFileManager` filtra solo per azienda e non per profilo, quindi un
  amministratore o condomino puo vedere le scadenze di condomini non suoi: correzione in config.cfg.
- Segnalazioni come funzione separata, foto nella scheda impianto, Stato pagamenti in sezione
  riservata: sono funzioni nuove (oggi non esistono).

## Domande per Andrea / l'operatore
1. (*) Cartello della centrale: in "Documentazione tecnica" o in "Sicurezza"?
2. Prove di combustione: dentro "Manutenzioni e controlli" con i rapportini (fase 1) o voce propria (fase 2)?
3. Stato pagamenti: chi lo deve vedere (ADM, INS, AMM)? Nel frattempo resta solo ADM/INS.
4. "Assistenza SIRBI": quale email/telefono per "Contatta ufficio"? "Apri segnalazione" finche non
   esiste la funzione: nascosto o rimanda a Importazione con categoria segnalazione?
5. Il condomino deve vedere la pagina (oggi CON ha il menu Centrali termiche, Importazione compresa)?
   Gli togliamo l'Importazione?

## Collaudo previsto
Laptop (/wera2026 profilo test, DB locale): AMM, CON, ADM; poi PROD in sola lettura con l'operatore.
