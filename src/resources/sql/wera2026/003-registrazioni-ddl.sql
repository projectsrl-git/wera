/*
created: 26/10/2026
model: wera2026 - Registrazioni (richieste di registrazione da approvare: amministratore)
database: postgresql 9.4

Disegno: .claude/2026-10-26-disegno-registrazione-amministratore.md

SOLO OGGETTI NUOVI, tutti nello schema "registrazioni": nessuna tabella, vista o dato di /wera
viene modificato. All'APPROVAZIONE il Java di wera2026 inserisce righe nelle tabelle utenti di
/wera (utenti, utenti_lingue, utenti_profili, utenti_aziende, password), come fa la gestione
utenti esistente: e' l'unico modo perche' il login (V_LOGIN) e il perimetro AMM funzionino.

Idempotente: si puo rilanciare. Niente sintassi successiva alla 9.4 (no CREATE INDEX IF NOT
EXISTS, no ON CONFLICT): gli indici passano da blocchi DO. I parametri iniziali si inseriscono
solo se mancano: un rilancio non sovrascrive i valori cambiati.

Esecuzione: psql -v ON_ERROR_STOP=1 -f 003-registrazioni-ddl.sql
Rollback (distrugge anche le richieste!): DROP SCHEMA registrazioni CASCADE;
(gli utenti gia' attivati restano in public.utenti: si gestiscono come gli altri utenti)

Riferimenti verso tabelle di /wera SENZA foreign key (stessa scelta di centrale_termica):
  id_azienda    -> public.aziende.id_azienda    (gestore a cui viene legato l'utente)
  id_utente*    -> public.utenti.id_utente      (0 = script / sistema)
  codice_profilo-> public.profili.codice
*/

SET client_encoding = 'UTF8';

BEGIN;

CREATE SCHEMA IF NOT EXISTS registrazioni;

COMMENT ON SCHEMA registrazioni IS
'wera2026 - Registrazioni self-service da approvare (amministratore; in futuro installatore). Solo wera2026 lo usa.';


-- ---------------------------------------------------------------------------------------------
-- Funzione comune: il registro eventi non si modifica ne si cancella
-- ---------------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION registrazioni.fn_storico_immutabile()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'registrazioni.%: % non ammesso, lo storico non si modifica ne si cancella',
        TG_TABLE_NAME, TG_OP;
END;
$$;


-- ---------------------------------------------------------------------------------------------
-- richieste: una riga per richiesta di registrazione (dati del soggetto = anagrafica dello studio)
-- ---------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS registrazioni.richieste (
    id_richiesta            serial          NOT NULL,
    tipo_richiesta          varchar(20)     NOT NULL DEFAULT 'AMMINISTRATORE',
    tipo_soggetto           varchar(20)     NOT NULL,
    denominazione           varchar(150)    NOT NULL,
    partita_iva             varchar(11)     NOT NULL,
    codice_fiscale          varchar(16)     NOT NULL,
    numero_registro         varchar(50),
    pec                     varchar(100)    NOT NULL,
    codice_sdi              varchar(7),
    indirizzo               varchar(50)     NOT NULL,
    citta                   varchar(50)     NOT NULL,
    provincia               varchar(2)      NOT NULL,
    cap                     varchar(5)      NOT NULL,
    fascia_condomini        varchar(30),
    elenco_condomini        text,
    marca_modello           varchar(300),
    referente_nome          varchar(100)    NOT NULL,
    referente_cognome       varchar(100)    NOT NULL,
    email                   varchar(100)    NOT NULL,
    telefono                varchar(30)     NOT NULL,
    hash_password           varchar(50),
    privacy_ts              timestamp       NOT NULL,
    privacy_testo           text            NOT NULL,
    ip                      varchar(45),
    user_agent              varchar(300),
    stato                   varchar(15)     NOT NULL DEFAULT 'IN_ATTESA',
    esito_compatibilita     varchar(20)     NOT NULL DEFAULT 'DA_VERIFICARE',
    nota_compatibilita      text,
    nota_interna            text,
    motivo_rifiuto          text,
    id_azienda              integer,
    id_utente               integer,
    ts_esito                timestamp,
    id_utente_esito         integer,
    ts_ins                  timestamp       NOT NULL DEFAULT current_timestamp,
    ts_mod                  timestamp,
    id_utente_mod           integer,
    CONSTRAINT pk_richieste PRIMARY KEY (id_richiesta),
    CONSTRAINT ck_richieste_tipo CHECK (tipo_richiesta IN ('AMMINISTRATORE', 'INSTALLATORE')),
    CONSTRAINT ck_richieste_soggetto CHECK (tipo_soggetto IN ('PROFESSIONISTA', 'STUDIO')),
    CONSTRAINT ck_richieste_stato CHECK (stato IN ('IN_ATTESA', 'APPROVATA', 'RIFIUTATA', 'ANNULLATA')),
    CONSTRAINT ck_richieste_compatibilita CHECK (esito_compatibilita IN
        ('DA_VERIFICARE', 'COMPATIBILE', 'PARZIALE', 'NON_COMPATIBILE')),
    CONSTRAINT ck_richieste_piva CHECK (partita_iva ~ '^[0-9]{11}$'),
    CONSTRAINT ck_richieste_cap CHECK (cap ~ '^[0-9]{5}$'),
    CONSTRAINT ck_richieste_provincia CHECK (provincia ~ '^[A-Z]{2}$'),
    CONSTRAINT ck_richieste_approvata CHECK (stato <> 'APPROVATA' OR id_utente IS NOT NULL),
    CONSTRAINT ck_richieste_rifiutata CHECK (stato <> 'RIFIUTATA' OR motivo_rifiuto IS NOT NULL)
);

COMMENT ON TABLE registrazioni.richieste IS
'Richieste di registrazione self-service. L''utente di /wera nasce solo all''approvazione (id_utente). Dopo l''esito la riga resta come anagrafica del soggetto (P.IVA, PEC, SDI... che public.utenti non ha).';
COMMENT ON COLUMN registrazioni.richieste.hash_password IS
'Password scelta nel form, con l''hash del framework (WebAppUtils.encryptSHA1, come public.password). Copiata in public.password all''approvazione, poi azzerata (anche al rifiuto).';
COMMENT ON COLUMN registrazioni.richieste.privacy_testo IS 'Testo del consenso accettato (parametro PRIVACY.TESTO al momento dell''invio).';
COMMENT ON COLUMN registrazioni.richieste.nota_compatibilita IS 'Nota sull''esito della verifica ripartitori: va nella mail di esito al richiedente.';
COMMENT ON COLUMN registrazioni.richieste.nota_interna IS 'Nota interna di chi approva: non va mai al richiedente.';

DO $$
BEGIN
    -- una sola richiesta in attesa per email e per partita IVA (anche con invii contemporanei)
    IF to_regclass('registrazioni.k1_richieste_email_in_attesa') IS NULL THEN
        CREATE UNIQUE INDEX k1_richieste_email_in_attesa ON registrazioni.richieste (lower(email))
            WHERE stato = 'IN_ATTESA';
    END IF;
    IF to_regclass('registrazioni.k2_richieste_piva_in_attesa') IS NULL THEN
        CREATE UNIQUE INDEX k2_richieste_piva_in_attesa ON registrazioni.richieste (tipo_richiesta, partita_iva)
            WHERE stato = 'IN_ATTESA';
    END IF;
    IF to_regclass('registrazioni.idx_richieste_stato') IS NULL THEN
        CREATE INDEX idx_richieste_stato ON registrazioni.richieste (stato, ts_ins);
    END IF;
END
$$;


-- ---------------------------------------------------------------------------------------------
-- allegati: foto dei ripartitori (verifica compatibilita)
-- ---------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS registrazioni.allegati (
    id_allegato             serial          NOT NULL,
    id_richiesta            integer         NOT NULL,
    nome_file_originale     varchar(255)    NOT NULL,
    percorso_file           varchar(500)    NOT NULL,
    tipo_mime               varchar(100),
    dimensione_byte         bigint          NOT NULL,
    impronta_sha256         char(64)        NOT NULL,
    ts_ins                  timestamp       NOT NULL DEFAULT current_timestamp,
    CONSTRAINT pk_allegati PRIMARY KEY (id_allegato),
    CONSTRAINT k1_allegati_percorso UNIQUE (percorso_file),
    CONSTRAINT fk_allegati_richiesta FOREIGN KEY (id_richiesta)
        REFERENCES registrazioni.richieste (id_richiesta) ON DELETE CASCADE
);

COMMENT ON TABLE registrazioni.allegati IS
'File allegati alla richiesta. percorso_file e'' relativo alla cartella del parametro FILE.CARTELLA (sotto directory.external_files), con nome univoco generato dal Java.';

DO $$
BEGIN
    IF to_regclass('registrazioni.idx_allegati_richiesta') IS NULL THEN
        CREATE INDEX idx_allegati_richiesta ON registrazioni.allegati (id_richiesta);
    END IF;
END
$$;


-- ---------------------------------------------------------------------------------------------
-- parametri: chiave/valore con override per azienda (come centrale_termica.parametri)
-- ---------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS registrazioni.parametri (
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

COMMENT ON TABLE registrazioni.parametri IS
'Parametri delle registrazioni. Riga con id_azienda NULL = default; riga con id_azienda = override. Lettura: funzione valore_parametro(). Nei testi i segnaposto {NOME} ecc. li sostituisce il Java.';

DO $$
BEGIN
    IF to_regclass('registrazioni.k1_parametri') IS NULL THEN
        CREATE UNIQUE INDEX k1_parametri ON registrazioni.parametri (COALESCE(id_azienda, 0), chiave);
    END IF;
END
$$;

CREATE OR REPLACE FUNCTION registrazioni.valore_parametro(p_chiave varchar, p_id_azienda integer)
RETURNS text
LANGUAGE sql
STABLE
AS $$
    SELECT p.valore
      FROM registrazioni.parametri p
     WHERE p.chiave = p_chiave
       AND (p.id_azienda = p_id_azienda OR p.id_azienda IS NULL)
     ORDER BY p.id_azienda NULLS LAST
     LIMIT 1
$$;


-- ---------------------------------------------------------------------------------------------
-- eventi: registro append-only (anche i blocchi anti-abuso, usati per il limite per IP)
-- ---------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS registrazioni.eventi (
    id_evento               bigserial       NOT NULL,
    ts_evento               timestamp       NOT NULL DEFAULT current_timestamp,
    tipo_evento             varchar(20)     NOT NULL,
    id_richiesta            integer,
    id_utente               integer,
    codice_profilo          char(3),
    ip                      varchar(45),
    dettaglio               text,
    CONSTRAINT pk_eventi PRIMARY KEY (id_evento),
    CONSTRAINT ck_eventi_tipo CHECK (tipo_evento IN (
        'RICHIESTA', 'BLOCCO', 'APPROVAZIONE', 'RIFIUTO', 'ANNULLAMENTO', 'MODIFICA', 'MAIL',
        'DOWNLOAD', 'ACCESSO_NEGATO', 'ERRORE')),
    CONSTRAINT fk_eventi_richiesta FOREIGN KEY (id_richiesta)
        REFERENCES registrazioni.richieste (id_richiesta)
);

COMMENT ON TABLE registrazioni.eventi IS
'Registro delle operazioni: solo INSERT. BLOCCO = invio scartato dall''anti-abuso (campo trappola, troppo veloce, troppe richieste dallo stesso IP); MAIL = esito di ogni invio (anche NON_INVIATA se MAIL.ATTIVA=N).';

DO $$
BEGIN
    IF to_regclass('registrazioni.idx_eventi_richiesta') IS NULL THEN
        CREATE INDEX idx_eventi_richiesta ON registrazioni.eventi (id_richiesta, ts_evento);
    END IF;
    IF to_regclass('registrazioni.idx_eventi_ip') IS NULL THEN
        CREATE INDEX idx_eventi_ip ON registrazioni.eventi (ip, ts_evento);
    END IF;
END
$$;

DROP TRIGGER IF EXISTS tr_eventi_immutabili ON registrazioni.eventi;
CREATE TRIGGER tr_eventi_immutabili
    BEFORE UPDATE OR DELETE ON registrazioni.eventi
    FOR EACH ROW EXECUTE PROCEDURE registrazioni.fn_storico_immutabile();
DROP TRIGGER IF EXISTS tr_eventi_no_truncate ON registrazioni.eventi;
CREATE TRIGGER tr_eventi_no_truncate
    BEFORE TRUNCATE ON registrazioni.eventi
    FOR EACH STATEMENT EXECUTE PROCEDURE registrazioni.fn_storico_immutabile();


-- ---------------------------------------------------------------------------------------------
-- v_richieste: elenco per la pagina di approvazione
-- ---------------------------------------------------------------------------------------------

CREATE OR REPLACE VIEW registrazioni.v_richieste AS
SELECT r.id_richiesta, r.tipo_richiesta, r.tipo_soggetto, r.denominazione, r.partita_iva, r.codice_fiscale,
       r.numero_registro, r.pec, r.codice_sdi, r.indirizzo, r.citta, r.provincia, r.cap,
       r.fascia_condomini, r.elenco_condomini, r.marca_modello,
       r.referente_nome, r.referente_cognome, r.email, r.telefono,
       r.privacy_ts, r.stato, r.esito_compatibilita, r.nota_compatibilita, r.nota_interna, r.motivo_rifiuto,
       r.id_azienda, r.id_utente, r.ts_esito, r.id_utente_esito, r.ts_ins,
       (SELECT count(*) FROM registrazioni.allegati a WHERE a.id_richiesta = r.id_richiesta)::integer AS numero_allegati,
       (current_date - r.ts_ins::date)::integer AS giorni_dalla_richiesta,
       u.username AS username_utente,
       TRIM(COALESCE(e.nome, '') || ' ' || COALESCE(e.cognome, '')) AS nome_utente_esito
  FROM registrazioni.richieste r
  LEFT JOIN public.utenti u ON u.id_utente = r.id_utente
  LEFT JOIN public.utenti e ON e.id_utente = r.id_utente_esito;

COMMENT ON VIEW registrazioni.v_richieste IS
'Richieste con numero di allegati, giorni di attesa, username creato e nome di chi ha dato l''esito. Senza hash_password, ip e user_agent.';


-- ---------------------------------------------------------------------------------------------
-- Parametri iniziali (solo se mancano). Nessun dato personale: contatti e destinatari vuoti.
-- ---------------------------------------------------------------------------------------------

INSERT INTO registrazioni.parametri (chiave, valore, tipo_valore, descrizione, id_utente_ins)
SELECT v.chiave, v.valore, v.tipo_valore, v.descrizione, 0
  FROM (VALUES
       ('REGISTRAZIONE.ATTIVA',        'S',       'BOOLEANO', 'N = il form non accetta richieste e la pagina mostra REGISTRAZIONE.CHIUSA.TESTO.'),
       ('REGISTRAZIONE.CHIUSA.TESTO',  'Le registrazioni sono momentaneamente sospese. Riprova piu'' tardi o contatta l''assistenza.', 'TESTO', 'Messaggio della pagina con REGISTRAZIONE.ATTIVA = N.'),
       ('ATTIVAZIONE.AZIENDA_CODICE',  'SIRBI',   'TESTO',    'Codice (public.aziende.codice) del gestore a cui viene legato l''utente approvato.'),
       ('ATTIVAZIONE.PROFILO',         'AMM',     'TESTO',    'Codice profilo (public.profili.codice) dato all''utente approvato.'),
       ('APPROVAZIONE.PROFILI',        'ADM',     'ELENCO',   'Profili che vedono e gestiscono le richieste (pagina e voce di menu).'),
       ('PASSWORD.LUNGHEZZA_MIN',      '8',       'NUMERO',   'Lunghezza minima della password scelta nel form.'),
       ('PASSWORD.LUNGHEZZA_MAX',      '64',      'NUMERO',   'Lunghezza massima della password scelta nel form.'),
       ('PASSWORD.REGOLA',             '^(?=.*[0-9])(?=.*[^A-Za-z0-9]).*$', 'TESTO', 'Espressione regolare (Java) che la password deve rispettare. Vuoto = solo la lunghezza.'),
       ('PASSWORD.REGOLA.TESTO',       'almeno un numero e un carattere speciale', 'TESTO', 'Spiegazione della regola, mostrata nel form e negli errori.'),
       ('PASSWORD.GIORNI_VALIDITA',    '60',      'NUMERO',   'Giorni di validita della password all''attivazione (poi il login chiede di cambiarla, come NewPassword).'),
       ('CONDOMINI.FASCE',             '1-5,6-15,16-40,oltre 40', 'ELENCO', 'Voci della tendina "Condomini gestiti (indicativo)".'),
       ('FILE.CARTELLA',               'registrazioni/', 'TESTO', 'Cartella degli allegati, relativa a directory.external_files di config.cfg.'),
       ('FILE.ESTENSIONI',             'jpg,jpeg,png,webp,heic,heif', 'ELENCO', 'Estensioni ammesse per le foto (il Java controlla anche il contenuto).'),
       ('FILE.DIMENSIONE_MAX_MB',      '10',      'NUMERO',   'Dimensione massima di ogni foto, in MB.'),
       ('FILE.MAX_PER_RICHIESTA',      '6',       'NUMERO',   'Numero massimo di foto per richiesta.'),
       ('ANTIABUSO.SECONDI_MINIMI',    '5',       'NUMERO',   'Un invio arrivato prima di questi secondi dall''apertura del form viene scartato.'),
       ('ANTIABUSO.MAX_RICHIESTE_IP_24H', '5',    'NUMERO',   'Invii (validi o scartati) ammessi dallo stesso IP nelle ultime 24 ore.'),
       ('ANTIABUSO.PROXY_FIDATI',      '192.168.0.50', 'ELENCO', 'IP dei proxy di frontend: solo da questi si legge X-Forwarded-For per l''IP del richiedente.'),
       ('MAIL.ATTIVA',                 'S',       'BOOLEANO', 'N = nessuna mail parte (si registra NON_INVIATA). In PROD N finche'' l''SMTP e'' il mailcatcher.'),
       ('MAIL.MITTENTE',               '',        'EMAIL',    'Mittente delle mail. Vuoto = ResetPassword.Mail.Address.From di config.cfg.'),
       ('MAIL.NOTIFICA_DESTINATARI',   '',        'ELENCO',   'Chi riceve l''avviso di nuova richiesta (email separate da virgola). Vuoto = nessun avviso.'),
       ('MAIL.RICEVUTA.OGGETTO',       'WERA - abbiamo ricevuto la tua richiesta di registrazione', 'TESTO', 'Oggetto della ricevuta al richiedente.'),
       ('MAIL.RICEVUTA.TESTO',         E'Gentile {NOME},\n\nabbiamo ricevuto la richiesta di registrazione di {DENOMINAZIONE} come amministratore su WERA (richiesta n. {NUMERO}).\nVerifichiamo i dati e la compatibilita'' dei ripartitori e ti rispondiamo {TEMPI_RISPOSTA}.\n\nQuando la richiesta e'' approvata potrai accedere con la tua email e la password che hai scelto.\n\nSupporto WERA', 'TESTO', 'Segnaposto: {NOME} {COGNOME} {DENOMINAZIONE} {NUMERO} {TEMPI_RISPOSTA} {LINK}.'),
       ('MAIL.NOTIFICA.OGGETTO',       'WERA - nuova richiesta di registrazione amministratore n. {NUMERO}', 'TESTO', 'Oggetto dell''avviso a MAIL.NOTIFICA_DESTINATARI.'),
       ('MAIL.NOTIFICA.TESTO',         E'Nuova richiesta di registrazione amministratore n. {NUMERO}.\n\nSoggetto: {DENOMINAZIONE} ({TIPO_SOGGETTO})\nReferente: {NOME} {COGNOME}\nCitta'': {CITTA} ({PROVINCIA})\nCondomini gestiti: {FASCIA_CONDOMINI}\nRipartitori: {MARCA_MODELLO}\nFoto allegate: {NUMERO_FOTO}\n\nPer verificarla e approvarla: {LINK_APPROVAZIONE}', 'TESTO', 'Segnaposto: quelli della ricevuta + {TIPO_SOGGETTO} {CITTA} {PROVINCIA} {FASCIA_CONDOMINI} {MARCA_MODELLO} {NUMERO_FOTO} {LINK_APPROVAZIONE}.'),
       ('MAIL.APPROVATA.OGGETTO',      'WERA - il tuo account amministratore e'' attivo', 'TESTO', 'Oggetto della mail di approvazione.'),
       ('MAIL.APPROVATA.TESTO',        E'Gentile {NOME},\n\nla registrazione di {DENOMINAZIONE} su WERA e'' stata approvata.\nPuoi accedere da {LINK} con il nome utente {USERNAME} e la password che hai scelto in fase di registrazione.\n\n{ESITO_COMPATIBILITA}\n\nI tuoi condomini compariranno nel tuo profilo man mano che vengono associati.\n\nSupporto WERA', 'TESTO', 'Segnaposto: {NOME} {COGNOME} {DENOMINAZIONE} {NUMERO} {USERNAME} {LINK} {ESITO_COMPATIBILITA}.'),
       ('MAIL.RIFIUTATA.OGGETTO',      'WERA - esito della richiesta di registrazione', 'TESTO', 'Oggetto della mail di rifiuto.'),
       ('MAIL.RIFIUTATA.TESTO',        E'Gentile {NOME},\n\nla richiesta di registrazione di {DENOMINAZIONE} su WERA (n. {NUMERO}) non e'' stata accolta.\nMotivo: {MOTIVO}\n\nPer chiarimenti puoi contattare l''assistenza WERA.\n\nSupporto WERA', 'TESTO', 'Segnaposto: {NOME} {COGNOME} {DENOMINAZIONE} {NUMERO} {MOTIVO}.'),
       ('ESITO.COMPATIBILE.TESTO',     'Verifica dei ripartitori: compatibili con WERA.', 'TESTO', 'Riga {ESITO_COMPATIBILITA} della mail di approvazione.'),
       ('ESITO.PARZIALE.TESTO',        'Verifica dei ripartitori: compatibili in parte, ti contatteremo per i dettagli.', 'TESTO', 'Riga {ESITO_COMPATIBILITA} della mail di approvazione.'),
       ('ESITO.NON_COMPATIBILE.TESTO', 'Verifica dei ripartitori: non compatibili con WERA, ti contatteremo per le alternative.', 'TESTO', 'Riga {ESITO_COMPATIBILITA} della mail di approvazione.'),
       ('ESITO.DA_VERIFICARE.TESTO',   'La verifica dei ripartitori e'' ancora in corso: ti aggiorneremo.', 'TESTO', 'Riga {ESITO_COMPATIBILITA} della mail di approvazione.'),
       ('LINK.ACCESSO',                'https://weraweb.it/wera2026/', 'URL', 'Indirizzo di wera2026 nelle mail ({LINK}, e base di {LINK_APPROVAZIONE}). Vuoto = nessun link.'),
       ('TESTO.TEMPI_RISPOSTA',        'entro 2 giorni lavorativi', 'TESTO', 'Tempi di risposta citati in pagina e nella ricevuta.'),
       ('PAGINA.TITOLO',               'Registra il tuo studio di amministrazione', 'TESTO', 'Titolo della pagina di registrazione.'),
       ('PAGINA.SOTTOTITOLO',          'L''accesso amministratore e'' riservato a professionisti con partita IVA e a studi o societa'' di amministrazione condominiale. Dopo la verifica hai accesso a tutti i condomini che gestisci con WERA.', 'TESTO', 'Testo sotto il titolo.'),
       ('PAGINA.INFO_VERIFICA',        'Un nostro tecnico verifica i dati e la compatibilita'' dei ripartitori e ti ricontatta {TEMPI_RISPOSTA}.', 'TESTO', 'Riquadro informativo sotto le foto. Segnaposto: {TEMPI_RISPOSTA}.'),
       ('PRIVACY.TESTO',               'Dichiaro di esercitare l''attivita'' di amministrazione condominiale e accetto i termini di servizio e il trattamento dei dati personali.', 'TESTO', 'Testo della casella di consenso (salvato con la richiesta).'),
       ('PRIVACY.LINK',                '',        'URL',      'Link all''informativa privacy accanto al consenso. Vuoto = nessun link.'),
       ('ASSISTENZA.EMAIL',            '',        'EMAIL',    'Email di assistenza mostrata nella pagina. Vuoto = nascosta.'),
       ('ASSISTENZA.TELEFONO',         '',        'TELEFONO', 'Telefono di assistenza mostrato nella pagina. Vuoto = nascosto.')
       ) AS v (chiave, valore, tipo_valore, descrizione)
 WHERE NOT EXISTS (SELECT 1 FROM registrazioni.parametri p WHERE p.id_azienda IS NULL AND p.chiave = v.chiave);

-- Se l'utente del datasource non e il proprietario dello schema (oggi e postgres ovunque):
-- GRANT USAGE ON SCHEMA registrazioni TO <utente>;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA registrazioni TO <utente>;
-- GRANT USAGE ON ALL SEQUENCES IN SCHEMA registrazioni TO <utente>;

COMMIT;
