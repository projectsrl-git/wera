# 2026-10-26 — Centrale termica wera2026: caricamento in PROD dei contratti firmati del referente tecnico

## Obiettivo
Caricare nella Centrale termica di wera2026 in PRODUZIONE (https://weraweb.it/wera2026/, .106, DB .75)
i contratti firmati 2026 condivisi dal referente tecnico su Dropbox (cartella `00_CONTRATTI FIRMATI/02_Contratti
Firmati_2026`, 68 PDF), SOLO tramite l'applicazione, dopo conferma della tabella di destinazione da parte
dell'operatore (26/10 mattina).

## Come (solo applicazione, nessuna scrittura diretta sul DB)
- File: zip scaricato dall'operatore da Dropbox, estratto nella scratchpad della sessione (mai nel repo);
  68 file, stesso elenco della cartella Dropbox.
- Accesso: login del superuser fatto dall'operatore da un terminale (password digitata da lui, mai vista
  ne' salvata); la sessione ha usato solo il cookie, chiuso a fine lavoro.
- Abbinamento ai condomini: con `CentraleTermicaDati&OPERAZIONE=condomini` di PROD (gli ID della copia
  locale del DB sono DIVERSI da PROD: non usarli), indirizzo del PDF confrontato con l'anagrafica.
- Scritture: `CentraleTermicaConfigurazioneSalva` (sezione), `CentraleTermicaUpload` (un file per
  chiamata, FUNCTIONID nella URL), `CentraleTermicaScadenza` crea + chiudi (stesso flusso della finestra
  "Carica documento" dello scadenziario).

## Cosa e' stato fatto in PROD
1. **Sezione nuova `CONTRATTI`** (id 9) "Contratti", icona `fa-file-text-o`, ordine 15, tipo DOCUMENTI,
   scadenza non richiesta, permessi ADM:RW, INS:RW, AMM:R (come le altre sezioni documentali). Scelta
   dell'operatore: nessuna sezione esistente era adatta ai contratti di letture/manutenzione.
2. **56 documenti** (id_documento 1-56, una versione ciascuno) su **31 condomini**:
   - sezione Contratti (9): contratti di letture e ripartizione, contabilizzazione calore, manutenzione e
     conduzione, contratto decennale (condominio 155);
   - sezione Nomina Terzo Responsabile (2): 9 contratti di Terzo Responsabile.
   - Data documento = data di firma (firma digitale) o data scritta sul contratto (scansioni).
   - Uno stesso contratto per piu' condomini, per scelta dell'operatore: complesso "Vittoria" sui 4
     condomini dello stesso indirizzo (131, 133, 134, 139; escluso 130, indirizzo diverso) e "Trieste" sui
     3 condomini omonimi con lo stesso indirizzo (182, 183, 184).
3. **9 scadenze "Rinnovo nomina Terzo responsabile"** (tipo 1, 12 mesi): per ciascun contratto TR
   un'esecuzione chiusa col documento (scadenze 1, 3, 5, ..., 17) e la successiva aperta (2, 4, ..., 18):
   condomini 136, 143 -> 30/09/2027; 65 -> 20/10/2027; 160 -> 29/09/2027; 177 -> 09/09/2027
   (decorrenza); 178 -> 15/09/2027 (decorrenza); 159, 170, 107 -> 01/10/2027 (contratti 2024 con rinnovo
   tacito annuale: ultima esecuzione 01/10/2024, prossima al rinnovo del 2027).
   Semaforo di tutte: REGOLARE.

## Non caricati (22 file): condominio assente nell'anagrafica di PROD
Pratiche 4, 5, 10, 11, 15, 20, 32, 33, 35, 36, 40, 47, 53, 61, 84, 89 (numero = prefisso del file
dell'elenco). Il superuser vede 54 condomini e questi non ci sono: da caricare quando i condomini saranno
creati in WERA (stessa procedura; i PDF restano su Dropbox). Elenco nominativo solo nella sessione.

## Verifiche
- [x] Via applicazione: `riepilogo` per ciascuno dei 31 condomini = documenti attesi (totale 56);
  `scadenziario` = 9 scadenze TR aperte alle date previste; `sezioni` della Panoramica con la cartella
  Contratti e i conteggi giusti.
- [x] Su .106: 56 file in `/usr/share/webapps/wera/centrale_termica/1/<condominio>/2026/`, SHA-256
  identici agli originali (confronto completo, 56/56).
- [ ] Letture SQL dirette su .75 (`v_documenti`, `v_scadenze`, `eventi`): NON eseguite, bloccate dal
  controllo permessi della sessione; sostituite dalle verifiche via applicazione (che leggono le stesse
  viste). Gli eventi CARICAMENTO / SCADENZA_* li scrive l'applicazione a ogni chiamata.
- [ ] Controllo visivo della Panoramica nel browser: a cura dell'operatore.

## Note tecniche
- Primo tentativo dello script KO senza effetti (nessun documento creato): da Git Bash il percorso del
  file in `-F file=@/c/...` non viene convertito per curl.exe; serve `pwd -W` (percorso Windows).
- Lo storico e' immutabile: un caricamento sbagliato si corregge archiviando/annullando dall'interfaccia,
  non cancellando.

## Rischi / punti aperti
- 22 file in attesa dei condomini mancanti.
- I 3 condomini TRIESTE con lo stesso indirizzo sono probabilmente doppioni in anagrafica: da chiarire.
- Contratto decennale (155) e contratti di conduzione con TR inclusi: nessuna scadenza registrata oltre
  a quelle sopra.
