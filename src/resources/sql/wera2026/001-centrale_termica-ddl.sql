/*
created: 25/10/2026
model: wera2026 - Centrale termica (gestione documentale parallela a /wera)
database: postgresql 9.4

Disegno: .claude/2026-10-25-disegno-db-centrale-termica.md

SOLO OGGETTI NUOVI, tutti nello schema "centrale_termica": nessuna tabella, vista o dato di
/wera viene modificato. Le uniche letture di oggetti esistenti sono nella vista
v_documenti_wera (public.elenco_file_importati_file_manager e public.parametri).

Idempotente: si puo rilanciare. Niente sintassi successiva alla 9.4 (no CREATE INDEX IF NOT
EXISTS, no ON CONFLICT): indici e vincoli aggiunti dopo la creazione passano da blocchi DO.
I dati iniziali (sezioni, tipi di scadenza, parametri, corrispondenze) si inseriscono solo se
mancano: un rilancio non sovrascrive le modifiche fatte dalla configurazione.

Esecuzione: psql -v ON_ERROR_STOP=1 -f 001-centrale_termica-ddl.sql
Rollback (distrugge anche i dati caricati!): DROP SCHEMA centrale_termica CASCADE;

Riferimenti verso tabelle di /wera SENZA foreign key (scelta voluta: una FK impedirebbe a /wera
di cancellare un condominio con documenti e prende un lock sulla tabella referenziata):
  id_azienda    -> public.aziende.id_azienda
  id_condominio -> public.condomini.id_modulo
  id_utente*    -> public.utenti.id_utente   (0 = script / sistema)
  codice_profilo-> public.profili.codice     (ADM, INS, AMM, CON)
Convenzione: id_azienda NULL = valore di default per tutte le aziende, valorizzato = override.
*/

SET client_encoding = 'UTF8';

BEGIN;

CREATE SCHEMA IF NOT EXISTS centrale_termica;

COMMENT ON SCHEMA centrale_termica IS
'wera2026 - Centrale termica: gestione documentale nuova e parallela a quella di /wera (elenco_file_importati_file_manager). Solo wera2026 la usa.';


-- ---------------------------------------------------------------------------------------------
-- Funzione comune: lo storico non si cancella e le righe immutabili non si modificano
-- ---------------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION centrale_termica.fn_storico_immutabile()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'centrale_termica.%: % non ammesso, lo storico non si modifica ne si cancella (usare lo stato)',
        TG_TABLE_NAME, TG_OP;
END;
$$;

COMMENT ON FUNCTION centrale_termica.fn_storico_immutabile() IS
'Trigger: rifiuta DELETE/TRUNCATE (e UPDATE dove previsto) sulle tabelle di storico. Archiviare o annullare cambiando lo stato.';


-- ---------------------------------------------------------------------------------------------
-- sezioni: le cartelle della Centrale termica
-- ---------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS centrale_termica.sezioni (
    id_sezione              serial          NOT NULL,
    id_azienda              integer,
    codice                  varchar(30)     NOT NULL,
    titolo                  varchar(100)    NOT NULL,
    sottotitolo             varchar(200),
    icona                   varchar(50),
    tipo                    varchar(10)     NOT NULL DEFAULT 'DOCUMENTI',
    ordine                  smallint        NOT NULL DEFAULT 0,
    fl_scadenza_richiesta   boolean         NOT NULL DEFAULT false,
    fl_disattivo            boolean         NOT NULL DEFAULT false,
    ts_ins                  timestamp       NOT NULL DEFAULT current_timestamp,
    id_utente_ins           integer         NOT NULL,
    ts_mod                  timestamp,
    id_utente_mod           integer,
    CONSTRAINT pk_sezioni PRIMARY KEY (id_sezione),
    CONSTRAINT ck_sezioni_tipo CHECK (tipo IN ('DOCUMENTI', 'FOTO', 'RISERVATA')),
    CONSTRAINT ck_sezioni_codice CHECK (codice ~ '^[A-Z0-9_]+$')
);

COMMENT ON TABLE centrale_termica.sezioni IS
'Sezioni (cartelle) della Centrale termica, configurabili. id_azienda NULL = sezione valida per tutte le aziende; valorizzato = sezione propria di quell''azienda.';
COMMENT ON COLUMN centrale_termica.sezioni.codice IS 'Codice stabile usato dal codice e dalle corrispondenze (es. LIBRETTO). Maiuscolo, cifre, _.';
COMMENT ON COLUMN centrale_termica.sezioni.icona IS 'Classe Font Awesome 4 (es. fa-book).';
COMMENT ON COLUMN centrale_termica.sezioni.tipo IS 'DOCUMENTI = cartella fra i documenti; FOTO = mostrata nella scheda impianto; RISERVATA = fuori dalle cartelle, solo ai profili di sezioni_profili.';
COMMENT ON COLUMN centrale_termica.sezioni.fl_scadenza_richiesta IS 'Se vero, il riquadro scadenze segnala i condomini senza una scadenza aperta di un tipo collegato alla sezione.';

DO $$
BEGIN
    IF to_regclass('centrale_termica.k1_sezioni') IS NULL THEN
        CREATE UNIQUE INDEX k1_sezioni ON centrale_termica.sezioni (COALESCE(id_azienda, 0), codice);
    END IF;
END
$$;


-- ---------------------------------------------------------------------------------------------
-- sezioni_profili: chi vede e chi carica in ogni sezione (come MENU_PROFILI, ma per codice)
-- ---------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS centrale_termica.sezioni_profili (
    id_sezione_profilo      serial          NOT NULL,
    id_sezione              integer         NOT NULL,
    codice_profilo          char(3)         NOT NULL,
    fl_lettura              boolean         NOT NULL DEFAULT true,
    fl_scrittura            boolean         NOT NULL DEFAULT false,
    ts_ins                  timestamp       NOT NULL DEFAULT current_timestamp,
    id_utente_ins           integer         NOT NULL,
    ts_mod                  timestamp,
    id_utente_mod           integer,
    CONSTRAINT pk_sezioni_profili PRIMARY KEY (id_sezione_profilo),
    CONSTRAINT k1_sezioni_profili UNIQUE (id_sezione, codice_profilo),
    CONSTRAINT fk_sezioni_profili_sezione FOREIGN KEY (id_sezione)
        REFERENCES centrale_termica.sezioni (id_sezione)
);

COMMENT ON TABLE centrale_termica.sezioni_profili IS
'Permessi per sezione e profilo. Un profilo senza riga non vede la sezione. Il perimetro dei condomini (AMM = i suoi) resta applicato dal Java.';
COMMENT ON COLUMN centrale_termica.sezioni_profili.codice_profilo IS 'public.profili.codice (ADM, INS, AMM, CON): codice e non id perche i serial differiscono fra DB.';
COMMENT ON COLUMN centrale_termica.sezioni_profili.fl_scrittura IS 'Puo caricare, versionare, archiviare documenti della sezione.';


-- ---------------------------------------------------------------------------------------------
-- documenti: il documento logico (titolo, sezione, data, stato); i file stanno in versioni
-- ---------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS centrale_termica.documenti (
    id_documento            serial          NOT NULL,
    id_azienda              integer         NOT NULL,
    id_condominio           integer         NOT NULL,
    id_sezione              integer         NOT NULL,
    titolo                  varchar(200)    NOT NULL,
    descrizione             text,
    data_documento          date,
    id_versione_corrente    integer,
    stato                   varchar(10)     NOT NULL DEFAULT 'CORRENTE',
    ts_stato                timestamp,
    id_utente_stato         integer,
    nota_stato              varchar(500),
    ts_ins                  timestamp       NOT NULL DEFAULT current_timestamp,
    id_utente_ins           integer         NOT NULL,
    ts_mod                  timestamp,
    id_utente_mod           integer,
    CONSTRAINT pk_documenti PRIMARY KEY (id_documento),
    CONSTRAINT ck_documenti_stato CHECK (stato IN ('CORRENTE', 'ARCHIVIATO', 'ANNULLATO')),
    CONSTRAINT fk_documenti_sezione FOREIGN KEY (id_sezione)
        REFERENCES centrale_termica.sezioni (id_sezione)
);

COMMENT ON TABLE centrale_termica.documenti IS
'Documento logico per condominio e sezione. Non si cancella mai (trigger): CORRENTE = in evidenza, ARCHIVIATO = nello storico, ANNULLATO = caricato per errore, visibile solo a chi gestisce.';
COMMENT ON COLUMN centrale_termica.documenti.id_condominio IS 'public.condomini.id_modulo (nessuna FK, vedi intestazione).';
COMMENT ON COLUMN centrale_termica.documenti.data_documento IS 'Data del documento (es. data del rapportino), non quella di caricamento.';
COMMENT ON COLUMN centrale_termica.documenti.id_versione_corrente IS 'Versione mostrata e scaricata di default; deve appartenere allo stesso documento (FK composta).';
COMMENT ON COLUMN centrale_termica.documenti.ts_stato IS 'Quando e da chi (id_utente_stato) e stato cambiato lo stato, con la nota.';

DO $$
BEGIN
    IF to_regclass('centrale_termica.idx_documenti_condominio') IS NULL THEN
        CREATE INDEX idx_documenti_condominio ON centrale_termica.documenti (id_condominio, id_sezione, stato);
    END IF;
    IF to_regclass('centrale_termica.idx_documenti_azienda') IS NULL THEN
        CREATE INDEX idx_documenti_azienda ON centrale_termica.documenti (id_azienda, ts_ins);
    END IF;
END
$$;

DROP TRIGGER IF EXISTS tr_documenti_no_delete ON centrale_termica.documenti;
CREATE TRIGGER tr_documenti_no_delete
    BEFORE DELETE ON centrale_termica.documenti
    FOR EACH ROW EXECUTE PROCEDURE centrale_termica.fn_storico_immutabile();
DROP TRIGGER IF EXISTS tr_documenti_no_truncate ON centrale_termica.documenti;
CREATE TRIGGER tr_documenti_no_truncate
    BEFORE TRUNCATE ON centrale_termica.documenti
    FOR EACH STATEMENT EXECUTE PROCEDURE centrale_termica.fn_storico_immutabile();


-- ---------------------------------------------------------------------------------------------
-- versioni: i file, immutabili; ogni caricamento ha un nome salvato univoco
-- ---------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS centrale_termica.versioni (
    id_versione             serial          NOT NULL,
    id_documento            integer         NOT NULL,
    numero_versione         smallint        NOT NULL,
    nome_file_originale     varchar(255)    NOT NULL,
    percorso_file           varchar(500)    NOT NULL,
    tipo_mime               varchar(100),
    dimensione_byte         bigint,
    impronta_sha256         char(64),
    nota                    varchar(500),
    ts_ins                  timestamp       NOT NULL DEFAULT current_timestamp,
    id_utente_ins           integer         NOT NULL,
    CONSTRAINT pk_versioni PRIMARY KEY (id_versione),
    CONSTRAINT k1_versioni UNIQUE (id_documento, numero_versione),
    CONSTRAINT k2_versioni UNIQUE (percorso_file),
    CONSTRAINT k3_versioni UNIQUE (id_documento, id_versione),
    CONSTRAINT ck_versioni_numero CHECK (numero_versione > 0),
    CONSTRAINT ck_versioni_dimensione CHECK (dimensione_byte IS NULL OR dimensione_byte >= 0),
    CONSTRAINT ck_versioni_impronta CHECK (impronta_sha256 IS NULL OR impronta_sha256 ~ '^[0-9a-f]{64}$'),
    CONSTRAINT fk_versioni_documento FOREIGN KEY (id_documento)
        REFERENCES centrale_termica.documenti (id_documento)
);

COMMENT ON TABLE centrale_termica.versioni IS
'File caricati: una riga per caricamento, mai modificata ne cancellata (trigger). Nuova versione = nuova riga + documenti.id_versione_corrente aggiornato.';
COMMENT ON COLUMN centrale_termica.versioni.nome_file_originale IS 'Nome del file come l''ha caricato l''utente: si usa solo per il download.';
COMMENT ON COLUMN centrale_termica.versioni.percorso_file IS 'Percorso UNIVOCO relativo alla cartella del parametro FILE.CARTELLA (es. <azienda>/<condominio>/<anno>/<uuid>.pdf): niente sovrascritture.';
COMMENT ON COLUMN centrale_termica.versioni.impronta_sha256 IS 'SHA-256 esadecimale minuscolo del contenuto: integrita e riconoscimento dei doppioni.';

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint
                   WHERE conname = 'fk_documenti_versione_corrente'
                     AND conrelid = 'centrale_termica.documenti'::regclass) THEN
        ALTER TABLE centrale_termica.documenti
            ADD CONSTRAINT fk_documenti_versione_corrente
            FOREIGN KEY (id_documento, id_versione_corrente)
            REFERENCES centrale_termica.versioni (id_documento, id_versione);
    END IF;
    IF to_regclass('centrale_termica.idx_versioni_impronta') IS NULL THEN
        CREATE INDEX idx_versioni_impronta ON centrale_termica.versioni (impronta_sha256);
    END IF;
END
$$;

DROP TRIGGER IF EXISTS tr_versioni_immutabili ON centrale_termica.versioni;
CREATE TRIGGER tr_versioni_immutabili
    BEFORE UPDATE OR DELETE ON centrale_termica.versioni
    FOR EACH ROW EXECUTE PROCEDURE centrale_termica.fn_storico_immutabile();
DROP TRIGGER IF EXISTS tr_versioni_no_truncate ON centrale_termica.versioni;
CREATE TRIGGER tr_versioni_no_truncate
    BEFORE TRUNCATE ON centrale_termica.versioni
    FOR EACH STATEMENT EXECUTE PROCEDURE centrale_termica.fn_storico_immutabile();


-- ---------------------------------------------------------------------------------------------
-- tipi_scadenza: catalogo degli adempimenti periodici
-- ---------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS centrale_termica.tipi_scadenza (
    id_tipo_scadenza        serial          NOT NULL,
    id_azienda              integer,
    codice                  varchar(30)     NOT NULL,
    descrizione             varchar(200)    NOT NULL,
    id_sezione              integer         NOT NULL,
    periodicita_mesi        smallint,
    giorni_preavviso        smallint,
    riferimento_normativo   varchar(200),
    ordine                  smallint        NOT NULL DEFAULT 0,
    fl_disattivo            boolean         NOT NULL DEFAULT false,
    ts_ins                  timestamp       NOT NULL DEFAULT current_timestamp,
    id_utente_ins           integer         NOT NULL,
    ts_mod                  timestamp,
    id_utente_mod           integer,
    CONSTRAINT pk_tipi_scadenza PRIMARY KEY (id_tipo_scadenza),
    CONSTRAINT ck_tipi_scadenza_codice CHECK (codice ~ '^[A-Z0-9_]+$'),
    CONSTRAINT ck_tipi_scadenza_periodicita CHECK (periodicita_mesi IS NULL OR periodicita_mesi > 0),
    CONSTRAINT ck_tipi_scadenza_preavviso CHECK (giorni_preavviso IS NULL OR giorni_preavviso >= 0),
    CONSTRAINT fk_tipi_scadenza_sezione FOREIGN KEY (id_sezione)
        REFERENCES centrale_termica.sezioni (id_sezione)
);

COMMENT ON TABLE centrale_termica.tipi_scadenza IS
'Catalogo degli adempimenti (prova di combustione, CPI VVFF, INAIL...). id_azienda NULL = per tutte le aziende. Valori iniziali modificabili dalla configurazione.';
COMMENT ON COLUMN centrale_termica.tipi_scadenza.id_sezione IS 'Sezione in cui va il documento che soddisfa la scadenza.';
COMMENT ON COLUMN centrale_termica.tipi_scadenza.periodicita_mesi IS 'Rinnovo automatico dopo N mesi dalla chiusura; NULL = una tantum. Sovrascrivibile per condominio in scadenze.';
COMMENT ON COLUMN centrale_termica.tipi_scadenza.giorni_preavviso IS 'NULL = parametro SCADENZE.GIORNI_PREAVVISO.';

DO $$
BEGIN
    IF to_regclass('centrale_termica.k1_tipi_scadenza') IS NULL THEN
        CREATE UNIQUE INDEX k1_tipi_scadenza ON centrale_termica.tipi_scadenza (COALESCE(id_azienda, 0), codice);
    END IF;
END
$$;


-- ---------------------------------------------------------------------------------------------
-- scadenze: scadenziario per condominio
-- ---------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS centrale_termica.scadenze (
    id_scadenza             serial          NOT NULL,
    id_azienda              integer         NOT NULL,
    id_condominio           integer         NOT NULL,
    id_tipo_scadenza        integer         NOT NULL,
    descrizione             varchar(200),
    data_scadenza           date            NOT NULL,
    periodicita_mesi        smallint,
    giorni_preavviso        smallint,
    stato                   varchar(10)     NOT NULL DEFAULT 'APERTA',
    data_esecuzione         date,
    id_documento_chiusura   integer,
    ts_chiusura             timestamp,
    id_utente_chiusura      integer,
    id_scadenza_precedente  integer,
    nota                    text,
    ts_ins                  timestamp       NOT NULL DEFAULT current_timestamp,
    id_utente_ins           integer         NOT NULL,
    ts_mod                  timestamp,
    id_utente_mod           integer,
    CONSTRAINT pk_scadenze PRIMARY KEY (id_scadenza),
    CONSTRAINT ck_scadenze_stato CHECK (stato IN ('APERTA', 'CHIUSA', 'ANNULLATA')),
    CONSTRAINT ck_scadenze_periodicita CHECK (periodicita_mesi IS NULL OR periodicita_mesi > 0),
    CONSTRAINT ck_scadenze_preavviso CHECK (giorni_preavviso IS NULL OR giorni_preavviso >= 0),
    CONSTRAINT fk_scadenze_tipo FOREIGN KEY (id_tipo_scadenza)
        REFERENCES centrale_termica.tipi_scadenza (id_tipo_scadenza),
    CONSTRAINT fk_scadenze_documento FOREIGN KEY (id_documento_chiusura)
        REFERENCES centrale_termica.documenti (id_documento),
    CONSTRAINT fk_scadenze_precedente FOREIGN KEY (id_scadenza_precedente)
        REFERENCES centrale_termica.scadenze (id_scadenza)
);

COMMENT ON TABLE centrale_termica.scadenze IS
'Scadenze per condominio. Chiusura = stato CHIUSA + documento che la soddisfa; se il tipo e periodico il Java crea la successiva (id_scadenza_precedente). Non si cancella (trigger): ANNULLATA.';
COMMENT ON COLUMN centrale_termica.scadenze.descrizione IS 'Facoltativa: distingue piu scadenze dello stesso tipo nello stesso condominio (es. due generatori). NULL = descrizione del tipo.';
COMMENT ON COLUMN centrale_termica.scadenze.periodicita_mesi IS 'NULL = periodicita del tipo. Serve per gli impianti con periodicita diversa (es. controllo di efficienza ogni 2 o 4 anni).';
COMMENT ON COLUMN centrale_termica.scadenze.data_esecuzione IS 'Data in cui l''adempimento e stato eseguito (base del rinnovo).';

DO $$
BEGIN
    IF to_regclass('centrale_termica.idx_scadenze_condominio') IS NULL THEN
        CREATE INDEX idx_scadenze_condominio ON centrale_termica.scadenze (id_condominio, stato, data_scadenza);
    END IF;
    IF to_regclass('centrale_termica.idx_scadenze_azienda') IS NULL THEN
        CREATE INDEX idx_scadenze_azienda ON centrale_termica.scadenze (id_azienda, stato, data_scadenza);
    END IF;
END
$$;

DROP TRIGGER IF EXISTS tr_scadenze_no_delete ON centrale_termica.scadenze;
CREATE TRIGGER tr_scadenze_no_delete
    BEFORE DELETE ON centrale_termica.scadenze
    FOR EACH ROW EXECUTE PROCEDURE centrale_termica.fn_storico_immutabile();
DROP TRIGGER IF EXISTS tr_scadenze_no_truncate ON centrale_termica.scadenze;
CREATE TRIGGER tr_scadenze_no_truncate
    BEFORE TRUNCATE ON centrale_termica.scadenze
    FOR EACH STATEMENT EXECUTE PROCEDURE centrale_termica.fn_storico_immutabile();


-- ---------------------------------------------------------------------------------------------
-- parametri: chiave/valore con override per azienda
-- ---------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS centrale_termica.parametri (
    id_parametro            serial          NOT NULL,
    id_azienda              integer,
    chiave                  varchar(60)     NOT NULL,
    valore                  text,
    tipo_valore             varchar(10)     NOT NULL DEFAULT 'TESTO',
    descrizione             varchar(300),
    ts_ins                  timestamp       NOT NULL DEFAULT current_timestamp,
    id_utente_ins           integer         NOT NULL,
    ts_mod                  timestamp,
    id_utente_mod           integer,
    CONSTRAINT pk_parametri PRIMARY KEY (id_parametro),
    CONSTRAINT ck_parametri_chiave CHECK (chiave ~ '^[A-Z0-9_.]+$'),
    CONSTRAINT ck_parametri_tipo CHECK (tipo_valore IN ('TESTO', 'NUMERO', 'BOOLEANO', 'ELENCO', 'EMAIL', 'TELEFONO', 'URL'))
);

COMMENT ON TABLE centrale_termica.parametri IS
'Parametri della Centrale termica. Riga con id_azienda NULL = default; riga con id_azienda = override per quell''azienda (anche vuoto). Lettura: funzione valore_parametro().';
COMMENT ON COLUMN centrale_termica.parametri.tipo_valore IS 'Guida la pagina di configurazione. BOOLEANO = S/N; ELENCO = valori separati da virgola.';

DO $$
BEGIN
    IF to_regclass('centrale_termica.k1_parametri') IS NULL THEN
        CREATE UNIQUE INDEX k1_parametri ON centrale_termica.parametri (COALESCE(id_azienda, 0), chiave);
    END IF;
END
$$;

CREATE OR REPLACE FUNCTION centrale_termica.valore_parametro(p_chiave varchar, p_id_azienda integer)
RETURNS text
LANGUAGE sql
STABLE
AS $$
    SELECT p.valore
      FROM centrale_termica.parametri p
     WHERE p.chiave = p_chiave
       AND (p.id_azienda = p_id_azienda OR p.id_azienda IS NULL)
     ORDER BY p.id_azienda NULLS LAST
     LIMIT 1
$$;

COMMENT ON FUNCTION centrale_termica.valore_parametro(varchar, integer) IS
'Valore effettivo di un parametro per un''azienda: override dell''azienda se esiste, altrimenti default. NULL se la chiave non esiste.';


-- ---------------------------------------------------------------------------------------------
-- categorie_wera: corrispondenza vecchie categorie (PARAMETRI DOMINIO DOC) -> sezioni
-- ---------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS centrale_termica.categorie_wera (
    id_categoria_wera       serial          NOT NULL,
    id_azienda              integer,
    codice_categoria        char(3)         NOT NULL,
    id_sezione              integer,
    ts_ins                  timestamp       NOT NULL DEFAULT current_timestamp,
    id_utente_ins           integer         NOT NULL,
    ts_mod                  timestamp,
    id_utente_mod           integer,
    CONSTRAINT pk_categorie_wera PRIMARY KEY (id_categoria_wera),
    CONSTRAINT fk_categorie_wera_sezione FOREIGN KEY (id_sezione)
        REFERENCES centrale_termica.sezioni (id_sezione)
);

COMMENT ON TABLE centrale_termica.categorie_wera IS
'In quale sezione mostrare i documenti caricati da /wera di una vecchia categoria (public.parametri DOMINIO=''DOC''). id_sezione NULL o categoria assente = non mostrati. id_azienda NULL = default.';

DO $$
BEGIN
    IF to_regclass('centrale_termica.k1_categorie_wera') IS NULL THEN
        CREATE UNIQUE INDEX k1_categorie_wera ON centrale_termica.categorie_wera (COALESCE(id_azienda, 0), codice_categoria);
    END IF;
END
$$;


-- ---------------------------------------------------------------------------------------------
-- eventi: registro append-only
-- ---------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS centrale_termica.eventi (
    id_evento               bigserial       NOT NULL,
    ts_evento               timestamp       NOT NULL DEFAULT current_timestamp,
    tipo_evento             varchar(20)     NOT NULL,
    id_utente               integer,
    codice_profilo          char(3),
    id_azienda              integer,
    id_condominio           integer,
    id_documento            integer,
    id_versione             integer,
    id_scadenza             integer,
    id_file_wera            integer,
    dettaglio               text,
    CONSTRAINT pk_eventi PRIMARY KEY (id_evento),
    CONSTRAINT ck_eventi_tipo CHECK (tipo_evento IN (
        'CARICAMENTO', 'NUOVA_VERSIONE', 'MODIFICA', 'ARCHIVIAZIONE', 'RIPRISTINO', 'ANNULLAMENTO',
        'DOWNLOAD', 'DOWNLOAD_ZIP', 'SCADENZA_CREATA', 'SCADENZA_CHIUSA', 'SCADENZA_ANNULLATA',
        'CONFIGURAZIONE', 'ACCESSO_NEGATO')),
    CONSTRAINT fk_eventi_documento FOREIGN KEY (id_documento)
        REFERENCES centrale_termica.documenti (id_documento),
    CONSTRAINT fk_eventi_versione FOREIGN KEY (id_versione)
        REFERENCES centrale_termica.versioni (id_versione),
    CONSTRAINT fk_eventi_scadenza FOREIGN KEY (id_scadenza)
        REFERENCES centrale_termica.scadenze (id_scadenza)
);

COMMENT ON TABLE centrale_termica.eventi IS
'Registro delle operazioni: solo INSERT (trigger su UPDATE/DELETE/TRUNCATE). ACCESSO_NEGATO = richiesta fuori perimetro rifiutata dal Java.';
COMMENT ON COLUMN centrale_termica.eventi.id_file_wera IS 'public.elenco_file_importati_file_manager.id_modulo, per i download dei documenti di /wera.';
COMMENT ON COLUMN centrale_termica.eventi.dettaglio IS 'Testo libero o JSON (es. elenco file dello zip, campi modificati).';

DO $$
BEGIN
    IF to_regclass('centrale_termica.idx_eventi_documento') IS NULL THEN
        CREATE INDEX idx_eventi_documento ON centrale_termica.eventi (id_documento);
    END IF;
    IF to_regclass('centrale_termica.idx_eventi_condominio') IS NULL THEN
        CREATE INDEX idx_eventi_condominio ON centrale_termica.eventi (id_condominio, ts_evento);
    END IF;
    IF to_regclass('centrale_termica.idx_eventi_ts') IS NULL THEN
        CREATE INDEX idx_eventi_ts ON centrale_termica.eventi (ts_evento);
    END IF;
END
$$;

DROP TRIGGER IF EXISTS tr_eventi_immutabili ON centrale_termica.eventi;
CREATE TRIGGER tr_eventi_immutabili
    BEFORE UPDATE OR DELETE ON centrale_termica.eventi
    FOR EACH ROW EXECUTE PROCEDURE centrale_termica.fn_storico_immutabile();
DROP TRIGGER IF EXISTS tr_eventi_no_truncate ON centrale_termica.eventi;
CREATE TRIGGER tr_eventi_no_truncate
    BEFORE TRUNCATE ON centrale_termica.eventi
    FOR EACH STATEMENT EXECUTE PROCEDURE centrale_termica.fn_storico_immutabile();


-- ---------------------------------------------------------------------------------------------
-- v_documenti_wera: documenti caricati da /wera, in sola lettura, smistati nelle sezioni
-- ---------------------------------------------------------------------------------------------
-- I file di /wera stanno tutti in un'unica cartella piatta (cartella.upload.filemanager) col nome
-- originale: lo stesso nome caricato per QUALSIASI condominio sovrascrive il file. Il numero di
-- riga e calcolato sull'intera tabella (prima dei filtri): fl_file_sovrascritto = su disco c'e il
-- file di un caricamento piu recente, quindi la riga NON va offerta al download.

CREATE OR REPLACE VIEW centrale_termica.v_documenti_wera AS
SELECT f.id_modulo                          AS id_file_wera,
       f.id_azienda::integer                AS id_azienda,
       f.id_condominio,
       f.id_categoria                       AS codice_categoria,
       p.descrizione                        AS descr_categoria,
       s.id_sezione,
       s.codice                             AS codice_sezione,
       s.titolo                             AS titolo_sezione,
       s.tipo                               AS tipo_sezione,
       f.nome_file,
       f.data_import,
       f.ora_import,
       f.ts_ins                             AS ts_caricamento,
       (f.nr_caricamento > 1)               AS fl_file_sovrascritto,
       f.data_prima_segnalazione,
       f.gg_segnalazione,
       CASE
           WHEN f.dt_prima IS NULL OR COALESCE(f.gg_segnalazione, 0) <= 0 THEN NULL
           WHEN f.dt_prima >= current_date THEN f.dt_prima
           ELSE f.dt_prima + (CEIL((current_date - f.dt_prima)::numeric / f.gg_segnalazione)
                              * f.gg_segnalazione)::integer
       END                                  AS data_prossima_segnalazione
  FROM (SELECT x.*,
               ROW_NUMBER() OVER (PARTITION BY LOWER(x.nome_file)
                                  ORDER BY x.ts_ins DESC, x.id_modulo DESC) AS nr_caricamento,
               CASE
                   WHEN x.data_prima_segnalazione ~ '^[0-9]{4}/(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])$'
                   THEN to_date(x.data_prima_segnalazione, 'YYYY/MM/DD')
               END AS dt_prima
          FROM public.elenco_file_importati_file_manager x) f
  JOIN LATERAL (SELECT c.id_sezione
                  FROM centrale_termica.categorie_wera c
                 WHERE c.codice_categoria = f.id_categoria
                   AND (c.id_azienda = f.id_azienda OR c.id_azienda IS NULL)
                 ORDER BY c.id_azienda NULLS LAST
                 LIMIT 1) m ON true
  JOIN centrale_termica.sezioni s
    ON s.id_sezione = m.id_sezione
   AND NOT s.fl_disattivo
  LEFT JOIN public.parametri p
    ON p.dominio = 'DOC'
   AND p.codice = f.id_categoria;

COMMENT ON VIEW centrale_termica.v_documenti_wera IS
'Documenti di /wera (elenco_file_importati_file_manager) in sola lettura, nella sezione data da categorie_wera (override azienda prima del default). Nessun filtro di perimetro: lo applica il Java.';


-- ---------------------------------------------------------------------------------------------
-- v_documenti: elenco unico (nuovi + /wera) per "Ultimi documenti" e archivio
-- ---------------------------------------------------------------------------------------------
-- Esclusi i documenti ANNULLATI (si leggono da documenti, solo per chi gestisce) e quelli di /wera
-- con file sovrascritto. Le colonne *_testo danno le date nel formato AAAA/MM/GG del resto di wera.

CREATE OR REPLACE VIEW centrale_termica.v_documenti AS
SELECT 'WERA2026'::varchar(8)                AS origine,
       d.id_documento,
       NULL::integer                        AS id_file_wera,
       d.id_azienda,
       d.id_condominio,
       d.id_sezione,
       s.codice                             AS codice_sezione,
       s.titolo                             AS titolo_sezione,
       s.tipo                               AS tipo_sezione,
       d.titolo,
       d.descrizione,
       d.stato,
       false                                AS fl_sola_lettura,
       d.data_documento,
       to_char(d.data_documento, 'YYYY/MM/DD') AS data_documento_testo,
       v.id_versione,
       v.numero_versione,
       v.nome_file_originale                AS nome_file,
       v.percorso_file,
       v.tipo_mime,
       v.dimensione_byte,
       COALESCE(v.ts_ins, d.ts_ins)         AS ts_caricamento,
       to_char(COALESCE(v.ts_ins, d.ts_ins), 'YYYY/MM/DD HH24:MI') AS ts_caricamento_testo,
       COALESCE(v.id_utente_ins, d.id_utente_ins) AS id_utente_caricamento
  FROM centrale_termica.documenti d
  JOIN centrale_termica.sezioni s ON s.id_sezione = d.id_sezione
  LEFT JOIN centrale_termica.versioni v ON v.id_versione = d.id_versione_corrente
 WHERE d.stato <> 'ANNULLATO'
UNION ALL
SELECT 'WERA'::varchar(8),
       NULL::integer,
       w.id_file_wera,
       w.id_azienda,
       w.id_condominio,
       w.id_sezione,
       w.codice_sezione,
       w.titolo_sezione,
       w.tipo_sezione,
       w.nome_file,
       w.descr_categoria,
       'CORRENTE'::varchar(10),
       true,
       NULL::date,
       NULL::text,
       NULL::integer,
       NULL::smallint,
       w.nome_file,
       NULL::varchar(500),
       NULL::varchar(100),
       NULL::bigint,
       w.ts_caricamento,
       to_char(w.ts_caricamento, 'YYYY/MM/DD HH24:MI'),
       NULL::integer
  FROM centrale_termica.v_documenti_wera w
 WHERE NOT w.fl_file_sovrascritto;

COMMENT ON VIEW centrale_termica.v_documenti IS
'Elenco unico dei documenti: origine WERA2026 (nuovi, versione corrente) e WERA (sola lettura, file non sovrascritto). Per i WERA il percorso si costruisce dal Java con cartella.upload.filemanager + nome_file. Nessun filtro di perimetro.';


-- ---------------------------------------------------------------------------------------------
-- v_scadenze: scadenze non annullate con semaforo (preavviso: scadenza, poi tipo, poi parametro)
-- ---------------------------------------------------------------------------------------------

CREATE OR REPLACE VIEW centrale_termica.v_scadenze AS
SELECT x.id_scadenza,
       x.id_azienda,
       x.id_condominio,
       x.id_tipo_scadenza,
       t.codice                                       AS codice_tipo,
       COALESCE(x.descrizione, t.descrizione)         AS descrizione,
       t.id_sezione,
       s.codice                                       AS codice_sezione,
       s.titolo                                       AS titolo_sezione,
       x.data_scadenza,
       x.stato,
       x.data_esecuzione,
       x.id_documento_chiusura,
       (x.data_scadenza - current_date)               AS giorni_mancanti,
       g.giorni_preavviso,
       CASE
           WHEN x.stato <> 'APERTA' THEN NULL
           WHEN x.data_scadenza < current_date THEN 'SCADUTA'
           WHEN x.data_scadenza - current_date <= g.giorni_preavviso THEN 'IN_SCADENZA'
           ELSE 'REGOLARE'
       END::varchar(12)                               AS semaforo,
       COALESCE(x.periodicita_mesi, t.periodicita_mesi) AS periodicita_mesi,
       t.riferimento_normativo
  FROM centrale_termica.scadenze x
  JOIN centrale_termica.tipi_scadenza t ON t.id_tipo_scadenza = x.id_tipo_scadenza
  JOIN centrale_termica.sezioni s ON s.id_sezione = t.id_sezione
 CROSS JOIN LATERAL (
       SELECT COALESCE(x.giorni_preavviso, t.giorni_preavviso,
                       CASE WHEN p.valore ~ '^[0-9]{1,4}$' THEN p.valore::integer END,
                       30) AS giorni_preavviso
         FROM (SELECT centrale_termica.valore_parametro('SCADENZE.GIORNI_PREAVVISO', x.id_azienda) AS valore) p
       ) g
 WHERE x.stato <> 'ANNULLATA'
   AND NOT s.fl_disattivo;

COMMENT ON VIEW centrale_termica.v_scadenze IS
'Scadenze APERTE e CHIUSE (non annullate) con giorni mancanti e semaforo SCADUTA / IN_SCADENZA / REGOLARE (solo aperte). Preavviso: scadenza, tipo, parametro SCADENZE.GIORNI_PREAVVISO dell''azienda, 30. Nessun filtro di perimetro.';


-- =============================================================================================
-- DATI INIZIALI (solo se mancano; id_utente_ins = 0 = script)
-- =============================================================================================

-- Sezioni: le 6 della richiesta del cliente + Foto impianto + Stato pagamenti
INSERT INTO centrale_termica.sezioni
       (codice, titolo, sottotitolo, icona, tipo, ordine, fl_scadenza_richiesta, id_utente_ins)
SELECT v.codice, v.titolo, v.sottotitolo, v.icona, v.tipo, v.ordine, v.fl_scadenza_richiesta, 0
  FROM (VALUES
       ('LIBRETTO',           'Libretto impianto',
        'Libretto di impianto per la climatizzazione e suoi aggiornamenti',            'fa-book',           'DOCUMENTI', 10, false),
       ('TERZO_RESPONSABILE', 'Nomina Terzo Responsabile',
        'Contratto e atto di nomina del Terzo responsabile',                           'fa-user',           'DOCUMENTI', 20, true),
       ('MANUTENZIONI',       'Manutenzioni e controlli',
        'Rapportini ordinari e straordinari, prove di combustione, analisi dell''acqua', 'fa-wrench',         'DOCUMENTI', 30, true),
       ('CONFORMITA',         'Dichiarazioni di conformità',
        'Dichiarazioni di conformità degli impianti',                                  'fa-check-square-o', 'DOCUMENTI', 40, false),
       ('SICUREZZA',          'Sicurezza',
        'Vigili del fuoco (VVFF) e INAIL, dove presenti',                              'fa-shield',         'DOCUMENTI', 50, true),
       ('DOC_TECNICA',        'Documentazione tecnica',
        'Manuali e documenti di caldaie e apparecchiature, cartello della centrale',    'fa-cogs',           'DOCUMENTI', 60, false),
       ('FOTO',               'Foto impianto',
        'Foto del condominio e della centrale termica (scheda impianto)',              'fa-camera',         'FOTO',      70, false),
       ('PAGAMENTI',          'Stato pagamenti',
        'Situazione dei pagamenti (sezione riservata)',                                'fa-eur',            'RISERVATA', 80, false)
       ) AS v (codice, titolo, sottotitolo, icona, tipo, ordine, fl_scadenza_richiesta)
 WHERE NOT EXISTS (SELECT 1 FROM centrale_termica.sezioni s
                    WHERE s.id_azienda IS NULL AND s.codice = v.codice);

-- Permessi: superuser e installatore leggono e scrivono ovunque; l'amministratore legge le
-- sezioni documentali e le foto; Stato pagamenti solo ADM/INS; il condomino nessuna sezione.
INSERT INTO centrale_termica.sezioni_profili
       (id_sezione, codice_profilo, fl_lettura, fl_scrittura, id_utente_ins)
SELECT s.id_sezione, v.codice_profilo, true, v.fl_scrittura, 0
  FROM centrale_termica.sezioni s
  JOIN (VALUES ('ADM', true), ('INS', true), ('AMM', false)) AS v (codice_profilo, fl_scrittura)
    ON (v.codice_profilo IN ('ADM', 'INS') OR s.tipo <> 'RISERVATA')
 WHERE s.id_azienda IS NULL
   AND NOT EXISTS (SELECT 1 FROM centrale_termica.sezioni_profili sp
                    WHERE sp.id_sezione = s.id_sezione AND sp.codice_profilo = v.codice_profilo);

-- Tipi di scadenza: valori iniziali, da verificare e modificabili dalla configurazione
INSERT INTO centrale_termica.tipi_scadenza
       (codice, descrizione, id_sezione, periodicita_mesi, riferimento_normativo, ordine, id_utente_ins)
SELECT v.codice, v.descrizione, s.id_sezione, v.periodicita_mesi, v.riferimento_normativo, v.ordine, 0
  FROM (VALUES
       ('MANUTENZIONE_ORDINARIA', 'Manutenzione ordinaria',                                   'MANUTENZIONI',       12, 'Istruzioni del costruttore / installatore', 10),
       ('CONTROLLO_EFFICIENZA',   'Controllo di efficienza energetica (prova di combustione)', 'MANUTENZIONI',       24, 'DPR 74/2013',                               20),
       ('ANALISI_ACQUA',          'Analisi dell''acqua dell''impianto',                       'MANUTENZIONI',       12, 'UNI 8065',                                  30),
       ('NOMINA_TERZO_RESP',      'Rinnovo nomina Terzo responsabile',                        'TERZO_RESPONSABILE', 12, NULL,                                        40),
       ('CPI_VVFF',               'Rinnovo periodico conformità antincendio (VVFF)',          'SICUREZZA',          60, 'DPR 151/2011',                              50),
       ('VERIFICA_INAIL',         'Verifica periodica INAIL',                                 'SICUREZZA',          60, 'DM 1/12/1975',                              60)
       ) AS v (codice, descrizione, codice_sezione, periodicita_mesi, riferimento_normativo, ordine)
  JOIN centrale_termica.sezioni s ON s.codice = v.codice_sezione AND s.id_azienda IS NULL
 WHERE NOT EXISTS (SELECT 1 FROM centrale_termica.tipi_scadenza t
                    WHERE t.id_azienda IS NULL AND t.codice = v.codice);

-- Parametri di default. I contatti dell'assistenza sono VUOTI di proposito (niente dati nel
-- repo): si valorizzano dalla configurazione; vuoto = voce nascosta nella pagina.
INSERT INTO centrale_termica.parametri (chiave, valore, tipo_valore, descrizione, id_utente_ins)
SELECT v.chiave, v.valore, v.tipo_valore, v.descrizione, 0
  FROM (VALUES
       ('ACCESSO.PROFILI',           'ADM,INS,AMM', 'ELENCO',   'Profili che vedono la Centrale termica (menu e pagine).'),
       ('MENU.VOCE_WERA.PROFILI',    'ADM,INS',     'ELENCO',   'Profili abilitati che in wera2026 vedono ancora la vecchia voce "Centrali termiche" (agli altri profili resta sempre).'),
       ('ETICHETTA.TITOLO',          'Centrale termica', 'TESTO', 'Titolo della pagina e della voce di menu.'),
       ('ETICHETTA.GESTIONE',        '',            'TESTO',    'Testo dopo "Gestione:" nella testata. Vuoto = riga nascosta.'),
       ('ASSISTENZA.TITOLO',         'Assistenza',  'TESTO',    'Titolo del riquadro assistenza.'),
       ('ASSISTENZA.UFFICIO',        '',            'TESTO',    'Nome dell''ufficio di assistenza. Vuoto = riquadro senza nome.'),
       ('ASSISTENZA.EMAIL',          '',            'EMAIL',    'Email per "Contatta ufficio". Vuoto = pulsante nascosto.'),
       ('ASSISTENZA.TELEFONO',       '',            'TELEFONO', 'Telefono dell''assistenza. Vuoto = nascosto.'),
       ('ASSISTENZA.ORARI',          '',            'TESTO',    'Orari dell''assistenza. Vuoto = nascosto.'),
       ('ASSISTENZA.LINK_SEGNALAZIONE', '',         'URL',      'Link di "Apri segnalazione". Vuoto = pulsante nascosto.'),
       ('SCADENZE.GIORNI_PREAVVISO', '30',          'NUMERO',   'Giorni prima della scadenza in cui diventa "in scadenza" (ambra).'),
       ('ULTIMI_DOCUMENTI.NUMERO',   '10',          'NUMERO',   'Righe nel riquadro "Ultimi documenti aggiornati".'),
       ('FILE.CARTELLA',             'centrale_termica/', 'TESTO', 'Sottocartella di directory.external_files per i file nuovi (mai upload/ di /wera).'),
       ('FILE.ESTENSIONI',           'pdf,p7m,jpg,jpeg,png,doc,docx,xls,xlsx,odt,ods,txt,zip', 'ELENCO', 'Estensioni ammesse al caricamento.'),
       ('FILE.DIMENSIONE_MAX_MB',    '25',          'NUMERO',   'Dimensione massima di un file caricato, in MB.'),
       ('FILE.MAX_PER_CARICAMENTO',  '20',          'NUMERO',   'Numero massimo di file in un solo caricamento.'),
       ('RIEPILOGO.SEZIONE_INTERVENTI', 'MANUTENZIONI', 'TESTO', 'Codice della sezione da cui si ricava "Ultimo intervento".'),
       ('DOWNLOAD.ZIP_MAX_FILE',     '50',          'NUMERO',   'Numero massimo di file in "Scarica selezionati".'),
       ('DOCUMENTI_WERA.VISIBILI',   'S',           'BOOLEANO', 'Mostra i documenti caricati da /wera (sola lettura).'),
       ('DOCUMENTI_WERA.ETICHETTA',  'da WERA',     'TESTO',    'Etichetta dei documenti caricati da /wera.')
       ) AS v (chiave, valore, tipo_valore, descrizione)
 WHERE NOT EXISTS (SELECT 1 FROM centrale_termica.parametri p
                    WHERE p.id_azienda IS NULL AND p.chiave = v.chiave);

-- Corrispondenza delle 16 categorie di /wera. NULL = non mostrata (segnalazioni, consigli).
INSERT INTO centrale_termica.categorie_wera (codice_categoria, id_sezione, id_utente_ins)
SELECT v.codice_categoria, s.id_sezione, 0
  FROM (VALUES
       ('001', 'FOTO'),               -- Foto condominio
       ('002', 'FOTO'),               -- Foto centrale termica
       ('003', 'DOC_TECNICA'),        -- Cartello centrale termica
       ('004', 'TERZO_RESPONSABILE'), -- Contratto di nomina Terzo responsabile
       ('005', 'LIBRETTO'),           -- Libretto impianto
       ('006', 'DOC_TECNICA'),        -- Documenti caldaia
       ('007', 'CONFORMITA'),         -- Dichiarazioni di conformita
       ('008', 'MANUTENZIONI'),       -- Analisi dell'acqua
       ('009', 'MANUTENZIONI'),       -- Interventi ordinari
       ('010', 'MANUTENZIONI'),       -- Interventi straordinari
       ('011', NULL),                 -- Segnalazione amministratore (funzione separata)
       ('012', NULL),                 -- Segnalazioni condomino (funzione separata)
       ('013', 'SICUREZZA'),          -- Vigili del fuoco VVFF
       ('014', 'PAGAMENTI'),          -- Stato pagamenti (riservata)
       ('015', NULL),                 -- Consigli
       ('016', 'SICUREZZA')           -- INAIL
       ) AS v (codice_categoria, codice_sezione)
  LEFT JOIN centrale_termica.sezioni s ON s.codice = v.codice_sezione AND s.id_azienda IS NULL
 WHERE NOT EXISTS (SELECT 1 FROM centrale_termica.categorie_wera c
                    WHERE c.id_azienda IS NULL AND c.codice_categoria = v.codice_categoria);

-- Se l'utente del datasource non e il proprietario dello schema (oggi e postgres ovunque):
-- GRANT USAGE ON SCHEMA centrale_termica TO <utente>;
-- GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA centrale_termica TO <utente>;
-- GRANT USAGE ON ALL SEQUENCES IN SCHEMA centrale_termica TO <utente>;

COMMIT;
