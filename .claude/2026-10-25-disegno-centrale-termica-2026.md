# 2026-10-25 — DISEGNO: "Centrale termica 2026", gestione documentale nuova e parallela (solo wera2026)

STATO: disegno da approvare. Sostituisce la fase 1 di `2026-10-25-proposta-centrale-termica.md`
(raggruppare i 16 codici nella vecchia pagina): su indicazione dell'operatore si crea una funzione
NUOVA, parallela a quella attuale, cosi /wera non e toccato e wera2026 e libero.

## Principi
- **Solo aggiunte.** Tabelle nuove con prefisso `ct26_`, cartella file nuova `ct2026/` sulla share
  di wera, FUNCTIONID nuove `Ct26*`, pacchetto Java nuovo `net.projectsrl.wera.ct2026`. Nessuna
  tabella, query, template o classe esistente cambia comportamento (fanno eccezione le righe
  aggiunte a `functionList.txt` e il ramo aggiunto al menu, vedi sotto).
- **Visibilita**: installatore, superuser, amministratore (solo i suoi condomini). Il condomino no.
  L'elenco dei profili abilitati e un parametro.
- **Tutto cio che e da decidere e un parametro** (regola dell'operatore): sezioni, contatti, soglie,
  etichette, profili, estensioni ammesse, corrispondenza con le vecchie categorie.
- **Controlli lato server**: profilo e perimetro dei condomini verificati dal Java; nessuna
  WHERECONDITION ricevuta dal browser per i dataset nuovi (oggi il pattern JSONDataSet la accetta: qui no).

## Modello dati (DDL nuovo, script in `src/resources/sql/wera2026/`)
- `ct26_sezione` — le sezioni, configurabili: codice, titolo, sottotitolo, icona, ordine, attiva,
  tipo (`DOCUMENTI` | `FOTO` | `RISERVATA`), profili che la vedono, scadenza richiesta s/n,
  periodicita di default in mesi. Valori iniziali: le 6 sezioni di Andrea (Libretto impianto ·
  Nomina Terzo Responsabile · Manutenzioni e controlli · Dichiarazioni di conformita · Sicurezza
  VVFF e INAIL · Documentazione tecnica), piu "Foto impianto" (tipo FOTO, mostrata nella scheda
  dell'impianto e non fra i documenti) e "Stato pagamenti" (tipo RISERVATA, profili da parametro).
- `ct26_documento` — id, azienda, condominio, sezione, titolo, descrizione, nome file originale,
  nome file salvato (UNIVOCO: niente piu sovrascritture), tipo e dimensione, data documento, data
  scadenza, periodicita, stato `CORRENTE` | `ARCHIVIATO`, documento sostituito, chi e quando ha
  caricato/modificato, cancellazione logica. Caricare una nuova versione archivia la precedente:
  **lo storico non si cancella mai**.
- `ct26_parametro` — chiave/valore con override per azienda (azienda vuota = valore di default):
  contatti dell'assistenza (nome ufficio, email, telefono, orari, link "Apri segnalazione"),
  etichetta "Gestione", giorni di preavviso scadenze (default 30), profili abilitati, estensioni e
  dimensione massima dei file, corrispondenza vecchio codice categoria -> nuova sezione.
- `ct26_evento` — registro: caricamento, nuova versione, archiviazione, download.
- Vista `v_ct26_documenti_legacy` — i documenti gia caricati da /wera
  (`elenco_file_importati_file_manager`), smistati nelle nuove sezioni con la corrispondenza in
  `ct26_parametro` e mostrati in **sola lettura** con l'etichetta "da WERA". I file restano dove
  sono (`upload/`). Le vecchie categorie foto/segnalazioni/consigli non hanno corrispondenza e non
  compaiono fra i documenti.

## Funzioni (Java nuovo, registrate in `functionList.txt`)
- `Ct26CentraleTermica` — pagina principale per condominio.
- `Ct26Upload` — caricamento multiplo (drag & drop) con sezione, data, scadenza, "sostituisce".
- `Ct26Download` — singolo file con controllo dei permessi; "Scarica selezionati" in zip.
- `Ct26Archivio` — storico con filtri (sezione, anno, testo).
- `Ct26Configurazione` — gestione di sezioni e parametri (solo superuser).

## Interfaccia (template `ct26*.html` + sezione 30 di `wera-theme.css`), dal mockup di Andrea, migliorata
- Testata: percorso, titolo, condominio e indirizzo, scelta condominio con ricerca (solo quelli
  del profilo), "Gestione: <parametro>".
- Riquadri: documenti disponibili · prossima scadenza (verde / ambra entro il preavviso / rosso
  scaduta / "da verificare") · ultimo intervento · completezza (sezioni con almeno un documento).
- 6 cartelle con contatore, data dell'ultimo aggiornamento e indicatore di scadenza; si aprono in
  un pannello laterale con i documenti correnti e, sotto, le versioni precedenti.
- "Ultimi documenti aggiornati" con Apri / Scarica, selezione multipla e "Scarica selezionati";
  link "Consulta archivio storico".
- "Scadenze e documenti": in scadenza, scadute, scadenze non registrate dove la sezione le richiede.
- "Assistenza": contatti dai parametri (mailto/tel); "Apri segnalazione" compare solo se il
  parametro del link e valorizzato.
- Responsive (telefono compreso), stesso design system del resto di wera2026.

## Menu
Il menu viene da DB condiviso (`menu_list`): una voce nuova in `MENU` comparirebbe anche in /wera.
Quindi la voce "Centrale termica" si aggiunge **nel menu di wera2026** (`included_menu.include`,
ramo FreeMarker dopo "Home", visibile ai profili abilitati). La vecchia voce "Centrali termiche"
resta; in wera2026 si puo nascondere per profilo con un parametro (default: visibile solo a
superuser e installatore durante la transizione).

## Fuori da questo giro
Mail di avviso scadenze (in wera2026 Quartz e spento), funzione Segnalazioni, scheda impianto completa.

## Cosa tocca delle regole "DA NON TOCCARE" (serve l'ok esplicito)
1. DDL: **solo tabelle e vista nuove** sul DB; prima sul DB locale, su .75 (produzione) solo con un
   ok a parte, poco prima del deploy.
2. Java: pacchetto nuovo + righe aggiunte a `functionList.txt`; nessuna classe esistente modificata.
3. `config.cfg`: solo dataset nuovi `DS.Ct26*` e la chiave della cartella `ct2026/`.
4. Template: file nuovi `ct26*.html`, un ramo FreeMarker aggiunto in `included_menu.include`
   (ISO-8859-1, edit byte-level), sezione 30 del tema.

## Ondate
1. DDL + dati iniziali (DB locale), DAO, upload/download, dataset.
2. Pagina principale, pannello sezione, caricamento, archivio.
3. Configurazione (sezioni e parametri), vista dei documenti legacy, voce di menu.
4. Collaudo sul laptop (superuser, installatore, amministratore; condomino senza accesso), poi DDL
   su .75 e deploy Jenkins con il tuo ok.
Un commit per ondata, con nota e QA come da convenzione.
