# 2026-10-25 — Checklist di collaudo PROD parallela (/wera2026 su .106) — BOZZA DA CONCORDARE

Istanza: https://weraweb.it/wera2026/ (versione `fc8da71`, profilo prod: DB di produzione `jdbc/wera`
-> 192.168.0.67, Quartz spento, mail sul mailcatcher di .106). Ogni salvataggio e upload e REALE.

## Come si esegue
- Browser: pannello del browser integrato. Il **login lo fa l'operatore** (in produzione l'agente
  non digita password); poi l'agente naviga nella sessione aperta e verifica DOM, console, rete.
- Dopo ogni blocco l'agente legge in sola lettura su .106: `crash/`, `wera_application.log`,
  `mailcatcher/` (atteso: nessun nuovo crash oltre ai noti, nessuna mail).
- Classi di rischio: **R** sola lettura · **N** scrittura neutra (salvo un record senza cambiare
  nulla: UPDATE con gli stessi valori) · **W** scrittura reale (dati veri, serve dato scelto
  dall'operatore) · **D** distruttiva/annullamento (solo con ok esplicito, caso per caso).
- Esegue solo i casi marcati "SI" dall'operatore, sui dati che indica lui.

## Bloccanti emersi dalla ricognizione (2026-10-25, sola lettura su .106)
- **B1 — vista materializzata `vista_scarico_data_massima` assente sul DB .67.** 5 crash su
  /wera2026 alle 09:30 di oggi (`PSQLException: relation ... does not exist`) da
  `DSLettureConsumiCondomini`, `...CondominiDettaglio`, `...Condomino`. La vista e usata da ~40
  query di config.cfg (letture consumi, storico, grafici condomino) e l'upload dati rilevatori fa
  `REFRESH MATERIALIZED VIEW CONCURRENTLY` DOPO aver inserito le letture. Se /wera su .70 funziona,
  allora .70 NON usa .67 (o usa un altro schema/utente): va chiarito prima di ogni test W.
- **B2 — le "cartelle condivise" probabilmente non sono quelle di /wera.** Su .106
  `/usr/share/webapps/wera/` (share `//fileserver_docs/storage/webapps`) ha solo `import/`
  (ultimo file 2025-06-26) e `upload/` (ultimo 2025-03-28); mancano `ACS26V3/`, `ANLCONFIG/`,
  `output/`. Un file caricato da /wera2026 (file manager, import) non sarebbe visibile a /wera, e
  "Scarico dati manuale" riscriverebbe un `AnlConfig.xml` in un percorso che /wera non legge.
- **ESITO della verifica in sola lettura su .70 (ok dell'operatore, 2026-10-25): /wera2026 NON
  lavora sui dati di /wera.**

  | | /wera su .70 (`webcont06`, Debian 8, Tomcat 7, Java 7) | /wera2026 su .106 |
  |---|---|---|
  | `db-wera-etc-host` | **192.168.0.75** | **192.168.0.67** |
  | `fileserver_docs` (share webapps) | 192.168.0.81 | 192.168.0.186 |
  | `fileserver_logs` | 192.168.0.55 (`logserver-etc-host`) | 192.168.0.185 |
  | `/usr/share/webapps/wera/` | `ACS26V3/` (mod. 19/10), `ANLCONFIG/`, `import/` (15/10), `upload/`, `doc/`, `ftp_sync_and_delete.sh` | solo `import/` (2025-06) e `upload/` (2025-03) |

  /wera usa la risorsa `jdbc/wera` del proprio `META-INF/context.xml` (stesso URL
  `db-wera-etc-host:5432/wera`, nessun descrittore in `conf/Catalina/localhost`): e il file hosts a
  decidere, e su .70 punta a .75. Da .106 sono raggiungibili sia .67 sia .75 (5432) e tutti e
  quattro i file server (445).
  Conseguenze: le scritture fatte finora da /wera2026 sono finite su .67, che **non e il DB di
  produzione di /wera**; .67 e verosimilmente una copia (vista materializzata assente come nel
  restore del laptop). La nota in CLAUDE.md "192.168.0.67 e il DB di PRODUZIONE" va riverificata
  con l'operatore. Esistenza della vista su .67: confermata dall'errore dell'applicazione stessa
  (nessuna connessione psql fatta: richiederebbe di digitare la password del DB di produzione).

- **AGGIORNAMENTO 2026-10-25 10:57 — B1 risolto.** Operatore: /wera = .75, /weralegacy = .67,
  quindi /wera2026 va su .75. Su .106 `db-wera-etc-host` -> 192.168.0.75 (backup
  `/etc/hosts.bak-20261025-105745`, etckeeper `0134dbd`), reload del solo contesto wera2026:
  connessione stabilita verso .75, nessuna verso .67, login 200, gestit 200, nessun nuovo crash.
  Rollback: ripristinare il backup e toccare di nuovo `WEB-INF/web.xml`.
  **Da adesso ogni scrittura da /wera2026 va sul DB di produzione di /wera.**
  B2 (share documenti .186 contro .81) resta APERTO: finche non e deciso, E1/E6 e il file manager
  scrivono su una share che /wera non legge (l'import delle letture nel DB invece e reale).
- **AGGIORNAMENTO 2026-10-25 11:23 — B2 risolto.** `/usr/share/webapps/wera` su .106 e montata
  dalla share di .81, la stessa di /wera (piano ed esito in
  `.claude/2026-10-25-piano-mount-share-wera-81.md`). **Da adesso anche file manager, upload e
  "Scarico dati manuale" agiscono sulle cartelle di produzione di /wera** (E6 resta escluso salvo
  tuo ok: `AnlConfig.xml` e lo stesso dell'import automatico di /wera).

## A. Accesso e pagine pubbliche (nessun dato toccato)
| # | Caso | Rischio | Note |
|---|---|---|---|
| A1 | Landing: carosello, dialog login, link reset/registrazione, mobile 375px | R | |
| A2 | Login errato -> dialog che si riapre con l'errore | R | username inventato |
| A3 | Login superuser / amministratore / condomino reale | R | lo digita l'operatore |
| A4 | Logout e ritorno alla landing; Login con sessione attiva (bug noto: 500) | R | |
| A5 | Pagine reset/registrazione: solo apertura, NESSUN invio | R | invio = mail nel mailcatcher, password reale cambiata |

## B. Superuser
| # | Caso | Rischio | Note |
|---|---|---|---|
| B1 | Home: 4 card, donut, Richieste (lente apre la ripartizione) | R | confronto numeri con /wera |
| B2 | Giro HTTP di tutte le voci di menu: 200, tema, nessun errore FreeMarker | R | script con il cookie di sessione |
| B3 | Ricerche anagrafiche (condomini, utenze, utenti, aziende): filtri, ricerca in linea, export PDF/XLS/CSV | R | export = download locale |
| B4 | Apertura in modifica di un condominio / utente / azienda, senza salvare | R | |
| B5 | Salvataggio di un condominio SENZA modifiche (Swal "Salvataggio completato") | N | condominio scelto dall'operatore |
| B6 | Ricerca ripartizioni (standard/UNI/UNI2018/letture) + apertura di una ripartizione, 5 tab, ricalcolo totali a video, SENZA salvare | R | verifica intestazioni UNI2018 (mai viste sul DB locale) |
| B7 | Ripartizioni con flag `salvataggio_in_corso` rimasto a true (popup bloccante) | R | conteggio con l'operatore |
| B8 | PDF ripartizione / "PDF con dettagli" (FOP senza xerces su Java 25) | R | genera un file in output? da verificare |
| B9 | Letture consumi e storico letture | R | **bloccato da B1** |
| B10 | Gestione rilevatori (non scaricati, errore, senza lettura, non censiti) | R | query lente |
| B11 | File manager, calendario, archivio file importati: elenco e anteprime | R | dipende da B2 |
| B12 | Configurazione guidata: tipo + Inizia, navigazione step SENZA salvare | R | lo step 1 salvato crea un condominio |
| B13 | Parametri / mail config / news / distribution list: apertura, editor summernote, SENZA salvare | R | |
| B14 | Profilo: dati, tab crediti, scadenza password | R | |

## C. Amministratore (utente reale indicato dall'operatore)
| # | Caso | Rischio | Note |
|---|---|---|---|
| C1 | Menu ridotto corretto; home 4 card, Richieste | R | |
| C2 | Anagrafiche > Utenze: ricerca, numero righe coerente con la card home | R | |
| C3 | Ripartizioni del suo perimetro: apertura senza salvare; nessun dato di altre aziende visibile | R | controllo di perimetro |
| C4 | Storico, Centrali termiche, Letture consumi | R | letture **bloccate da B1** |
| C5 | Profilo | R | |

## D. Condomino (utente reale indicato dall'operatore)
| # | Caso | Rischio | Note |
|---|---|---|---|
| D1 | Home condomino: grafici consumi | R | **probabile 500 per B1** (`DSLettureConsumiCondomino`) |
| D2 | Profilo | R | |

## E. Scritture reali (solo su indicazione puntuale dell'operatore)
| # | Caso | Rischio | Note |
|---|---|---|---|
| E1 | Upload dati rilevatori XML (prova JAXB su Java 25) con un file NUOVO che andrebbe comunque importato | W | lo carica l'operatore; prima chiudere B1 (il REFRESH della vista fallirebbe DOPO l'insert) |
| E2 | Upload di un XML gia importato -> rifiuto "gia importato" | N | NON esercita JAXB; il file viene comunque riscritto in `upload/` prima del controllo |
| E3 | Annullamento di E1 da "Archivio file importati" (cancella le righe di `scarico` e `dati_rilevatori` di quel file) | D | solo se E1 va storto |
| E4 | Modifica reale di un dato anagrafico + ripristino del valore originale | W | |
| E5 | Salvataggio di una ripartizione | W | sconsigliato in questa tornata |
| E6 | Import anagrafica utenze, scarico dati manuale | W | **esclusi** finche B2 non e chiarito |

## Da NON fare da /wera2026 (vale anche dopo il collaudo)
Reset password e registrazione di utenti reali (la mail resta nel mailcatcher, la password reale
cambia), e qualunque funzione che invii mail ai condomini.
