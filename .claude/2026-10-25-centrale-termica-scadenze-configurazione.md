# 2026-10-25 — Centrale termica wera2026: scadenze e configurazione (ondata 3)

## Obiettivo
Gestire lo scadenziario dalla pagina (registrare, segnare come eseguita con rinnovo automatico,
annullare, storico) e configurare sezioni e permessi, tipi di scadenza, parametri (con eccezioni per
azienda) e corrispondenze delle categorie di /wera, riservato al superuser. Autorizzato dall'operatore
il 2026-10-25: "gestione delle scadenze dalla pagina e configurazione di sezioni, parametri e tipi di
scadenza (solo superuser)".

## File toccati
- Java (pacchetto `net.projectsrl.wera.centraletermica.core`):
  - NUOVO `FunctionCentraleTermicaScadenza` (POST): `AZIONE=crea|chiudi|annulla`. Chiudi = stato CHIUSA,
    data di esecuzione (default oggi), documento facoltativo dello stesso condominio; se periodica crea
    la successiva (esecuzione + mesi) collegata con `id_scadenza_precedente`. Tutto in transazione con
    `SELECT ... FOR UPDATE`. Eventi SCADENZA_CREATA/CHIUSA/ANNULLATA. Permesso: scrittura sulla sezione
    del tipo.
  - NUOVO `FunctionCentraleTermicaConfigurazione` (pagina, estende la pagina principale: percorso
    "Home / Centrale termica / Configurazione") e `FunctionCentraleTermicaConfigurazioneSalva` (POST,
    `OGGETTO=sezione|tipo|parametro|categoria`), NUOVO `DatiConfigurazione`.
  - NUOVO `Sql`: statement parametrizzati con NULL tipizzati (il DAO del framework manda i null come
    varchar e Postgres li rifiuta su integer/date).
  - `CentraleTermicaDati`: operazioni `tipi_scadenza`, `storico_scadenze`, `config` (solo chi configura);
    `parametri` espone `PUO_CONFIGURARE`; `scadenze` espone tipo, periodicita' e permesso di scrittura.
  - `ContestoUtente.puoConfigurare()` (parametro `CONFIGURAZIONE.PROFILI`, default ADM); base: `campo`,
    `interoOpzionale`, `dataSql`, `registraEvento` con id scadenza; `FunctionCentraleTermica`: percorso
    estendibile.
- `config.cfg` — AGGIUNTI 10 dataset `DS.DSCentraleTermica*` (tipi, scadenza, storico, config) in fondo;
  `DSCentraleTermicaScadenze` (nostro) con 3 colonne in piu'. Edit byte-level, resta ISO-8859-1 CRLF.
- `functionList.txt` — AGGIUNTE `CentraleTermicaScadenza`, `CentraleTermicaConfigurazione`,
  `CentraleTermicaConfigurazioneSalva`.
- DDL — parametro `CONFIGURAZIONE.PROFILI` = ADM (rilanciato sul DB locale, idempotente).
- Template: `centraletermica.html` (pulsanti Storico / Nuova, finestra scadenza, pulsante
  Configurazione); NUOVO `centraletermicaconfigurazione.html` (ASCII, schede Bootstrap).
- JS: `wera-centrale-termica.js` (scadenze, storico, pulsante configurazione; `?20261025b`), NUOVO
  `wera-centrale-termica-config.js`. Entrambi ASCII.
- Tema: sezione 30b (scadenze, configurazione) + correzioni responsive (testata sotto 290 px, riquadri
  su una colonna sotto 360 px, chip delle scadenze a capo, piede delle finestre flessibile).
  Cache-buster tema `?20261025c` in included_head (ISO, byte-level), errore, errorpage, message.

## Regole lato server (configurazione)
- Solo profili di `CONFIGURAZIONE.PROFILI`; aziende delle eccezioni limitate a `WHERECONDITION_AZIENDE`.
- Sezioni e tipi non si cancellano (si disattivano); codice e azienda fissati alla creazione, codici
  univoci per azienda (409). Permessi `PROFILO:R|RW|-` solo su profili esistenti; icona `fa-*`.
- Valori dei parametri validati per tipo (NUMERO, BOOLEANO S/N, EMAIL, URL solo http(s)/astro?,
  TELEFONO, ELENCO normalizzato); `FILE.CARTELLA` mai `upload/` ne' `..`; chiavi solo esistenti.
- Nessuno puo' togliere il proprio profilo da `ACCESSO.PROFILI` / `CONFIGURAZIONE.PROFILI` (409).
- Ogni modifica -> evento CONFIGURAZIONE con il dettaglio.

## QA (laptop, /wera2026 profilo test, DB locale WERA-TEST)
- [x] build, WAR test, deploy a caldo; DDL rilanciato (exit 0).
- [x] curl ADM: tipi (6), crea controllo efficienza (IN_SCADENZA) e INAIL (SCADUTA), riepilogo
  (prossima = scaduta), data 31/02 e periodicita' 999 rifiutate; chiudi con documento -> successiva
  20/10/2028 (+24 mesi); richiudi 409; condominio errato 404; annulla con nota; storico (2); GET 405.
- [x] curl AMM: crea 403, config 403, salva 403, pagina config -> redirect, PUO_CONFIGURARE=false.
- [x] curl configurazione ADM: dati (21 parametri, 15 categorie, aziende); eccezione per azienda ->
  valore effettivo cambiato -> eliminata; rifiuti: numero "abc", URL `javascript:`, auto-esclusione 409,
  `upload/`, chiave inesistente 404; estensioni normalizzate; sezione nuova + doppione 409 + modifica +
  permessi/tipo/icona non validi; tipo nuovo + modifica (periodicita' vuota = una tantum) + sezione
  inesistente; categoria spostata e rimessa, inesistente 404.
- [x] Browser (pannello stretto, tablet): Nuova scadenza -> riga "In scadenza" e sintesi aggiornata;
  Eseguita -> "Prossima scadenza: 25/10/2027"; Storico (eseguite, annullata, allegato scaricabile);
  pagina configurazione (percorso, voce attiva, 4 schede); modifica sezione; eccezione parametro
  aggiunta e tolta dall'interfaccia.
- [x] Pulizia: hash password ripristinati identici; sezione e tipo di prova eliminati; parametri e
  categorie come prima. Restano (non cancellabili, DB sacrificabile) le scadenze e i documenti di prova
  del condominio 34.
- [ ] INS non provato (assente nel DB locale).

## Rischi / punti aperti
- I campi di sessione del menu (titolo, accesso) si aggiornano al login successivo.
- Le periodicita' iniziali dei tipi restano da verificare con SIRBI (ora modificabili dalla pagina).
- Nessuna mail di avviso scadenze (Quartz spento in wera2026): eventuale ondata futura.
