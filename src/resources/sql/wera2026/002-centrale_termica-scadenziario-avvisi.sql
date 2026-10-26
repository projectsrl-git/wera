/*
created: 26/10/2026
model: wera2026 - Centrale termica, ondata 4 (scadenziario per adempimento, calendario, avvisi)
database: postgresql 9.4

Richiede 001-centrale_termica-ddl.sql. SOLO lo schema centrale_termica: nessun oggetto di public.
Idempotente (DO block per le colonne nuove: 9.4 non ha ADD COLUMN IF NOT EXISTS).

- tipi_scadenza: colonne sigla (CPI, INAIL, RCEE...) ed ente (Comando VV.F., INAIL...) del mockup
  wera_demo; nuovo tipo VERIFICA_TERRA (DPR 462/01).
- invii_avvisi: storico degli avvisi di scadenza (append-only, un solo INVIATO per scadenza e soglia).
- parametri ALERT.*: soglie, destinatari, attivazione, testo; token del trigger giornaliero.
- scelte dell'operatore del 2026-10-26, applicate SOLO se il parametro non e' mai stato modificato:
  SCADENZE.GIORNI_PREAVVISO 30 -> 90 (soglia "in scadenza" del mockup) e MENU.VOCE_WERA.PROFILI
  'ADM,INS' -> '' (vecchia voce "Centrali termiche" nascosta a chi ha la Centrale termica nuova).

Esecuzione: psql -v ON_ERROR_STOP=1 -f 002-centrale_termica-scadenziario-avvisi.sql
Rollback: DROP TABLE centrale_termica.invii_avvisi; ALTER TABLE centrale_termica.tipi_scadenza
DROP COLUMN sigla, DROP COLUMN ente; DELETE dei parametri ALERT.* (lo schema resta).
*/

SET client_encoding = 'UTF8';

BEGIN;

-- ---------------------------------------------------------------------------------------------
-- tipi_scadenza: sigla ed ente
-- ---------------------------------------------------------------------------------------------

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'centrale_termica' AND table_name = 'tipi_scadenza' AND column_name = 'sigla') THEN
        ALTER TABLE centrale_termica.tipi_scadenza ADD COLUMN sigla varchar(10);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'centrale_termica' AND table_name = 'tipi_scadenza' AND column_name = 'ente') THEN
        ALTER TABLE centrale_termica.tipi_scadenza ADD COLUMN ente varchar(100);
    END IF;
END
$$;

COMMENT ON COLUMN centrale_termica.tipi_scadenza.sigla IS 'Sigla breve mostrata nello scadenziario e nel calendario (es. CPI, INAIL, RCEE).';
COMMENT ON COLUMN centrale_termica.tipi_scadenza.ente IS 'Ente o soggetto che esegue o certifica (es. Comando VV.F., INAIL, Manutentore abilitato).';

UPDATE centrale_termica.tipi_scadenza t
   SET sigla = v.sigla, ente = v.ente
  FROM (VALUES
       ('MANUTENZIONE_ORDINARIA', 'MANUT', 'Manutentore abilitato'),
       ('CONTROLLO_EFFICIENZA',   'RCEE',  'Manutentore / Catasto regionale'),
       ('ANALISI_ACQUA',          'ACQUA', 'Laboratorio / manutentore'),
       ('NOMINA_TERZO_RESP',      'TR',    'Terzo responsabile'),
       ('CPI_VVFF',               'CPI',   'Comando VV.F.'),
       ('VERIFICA_INAIL',         'INAIL', 'INAIL (ex ISPESL)')
       ) AS v (codice, sigla, ente)
 WHERE t.id_azienda IS NULL AND t.codice = v.codice AND t.sigla IS NULL AND t.ente IS NULL;

INSERT INTO centrale_termica.tipi_scadenza
       (codice, sigla, descrizione, ente, id_sezione, periodicita_mesi, riferimento_normativo, ordine, id_utente_ins)
SELECT 'VERIFICA_TERRA', 'TERRA', 'Verifica impianti di messa a terra', 'Organismo abilitato', s.id_sezione, 24,
       'DPR 462/01', 70, 0
  FROM centrale_termica.sezioni s
 WHERE s.id_azienda IS NULL AND s.codice = 'SICUREZZA'
   AND NOT EXISTS (SELECT 1 FROM centrale_termica.tipi_scadenza t WHERE t.id_azienda IS NULL AND t.codice = 'VERIFICA_TERRA');


-- ---------------------------------------------------------------------------------------------
-- v_scadenze: stesse colonne di 001 + in fondo sigla, ente, esecuzione e documento precedenti
-- (CREATE OR REPLACE VIEW ammette solo colonne aggiunte in coda)
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
       t.riferimento_normativo,
       COALESCE(t.sigla, t.codice)::varchar(30)       AS sigla,
       t.ente,
       x.id_scadenza_precedente,
       pr.data_esecuzione                             AS ultima_esecuzione,
       pr.id_documento_chiusura                       AS id_documento_ultimo
  FROM centrale_termica.scadenze x
  JOIN centrale_termica.tipi_scadenza t ON t.id_tipo_scadenza = x.id_tipo_scadenza
  JOIN centrale_termica.sezioni s ON s.id_sezione = t.id_sezione
  LEFT JOIN centrale_termica.scadenze pr ON pr.id_scadenza = x.id_scadenza_precedente
 CROSS JOIN LATERAL (
       SELECT COALESCE(x.giorni_preavviso, t.giorni_preavviso,
                       CASE WHEN p.valore ~ '^[0-9]{1,4}$' THEN p.valore::integer END,
                       30) AS giorni_preavviso
         FROM (SELECT centrale_termica.valore_parametro('SCADENZE.GIORNI_PREAVVISO', x.id_azienda) AS valore) p
       ) g
 WHERE x.stato <> 'ANNULLATA'
   AND NOT s.fl_disattivo;

COMMENT ON VIEW centrale_termica.v_scadenze IS
'Scadenze APERTE e CHIUSE (non annullate) con giorni mancanti e semaforo SCADUTA / IN_SCADENZA / REGOLARE (solo aperte), sigla ed ente del tipo, esecuzione e documento della scadenza precedente (rinnovo). Preavviso: scadenza, tipo, parametro SCADENZE.GIORNI_PREAVVISO dell''azienda, 30. Nessun filtro di perimetro.';


-- ---------------------------------------------------------------------------------------------
-- invii_avvisi: storico degli avvisi di scadenza
-- ---------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS centrale_termica.invii_avvisi (
    id_invio                bigserial       NOT NULL,
    ts_invio                timestamp       NOT NULL DEFAULT current_timestamp,
    id_azienda              integer         NOT NULL,
    id_condominio           integer         NOT NULL,
    id_scadenza             integer         NOT NULL,
    soglia_giorni           integer         NOT NULL,
    destinatari             text,
    oggetto                 varchar(300),
    esito                   varchar(10)     NOT NULL,
    dettaglio               text,
    id_utente               integer,
    CONSTRAINT pk_invii_avvisi PRIMARY KEY (id_invio),
    CONSTRAINT ck_invii_avvisi_esito CHECK (esito IN ('INVIATO', 'ERRORE', 'SALTATO')),
    CONSTRAINT fk_invii_avvisi_scadenza FOREIGN KEY (id_scadenza)
        REFERENCES centrale_termica.scadenze (id_scadenza)
);

COMMENT ON TABLE centrale_termica.invii_avvisi IS
'Avvisi di scadenza: una riga per tentativo. soglia_giorni = anticipo (90, 60, 30...) oppure -1 per scadenza superata. Solo INSERT (trigger). Un solo INVIATO per scadenza e soglia (indice univoco parziale).';
COMMENT ON COLUMN centrale_termica.invii_avvisi.esito IS 'INVIATO = consegnato al server SMTP del profilo (in wera2026 PROD oggi il mailcatcher locale); ERRORE = invio fallito; SALTATO = nessun destinatario.';
COMMENT ON COLUMN centrale_termica.invii_avvisi.id_utente IS 'Chi ha lanciato il controllo dalla pagina; NULL = trigger giornaliero.';

DO $$
BEGIN
    IF to_regclass('centrale_termica.k1_invii_avvisi') IS NULL THEN
        CREATE UNIQUE INDEX k1_invii_avvisi ON centrale_termica.invii_avvisi (id_scadenza, soglia_giorni) WHERE esito = 'INVIATO';
    END IF;
    IF to_regclass('centrale_termica.idx_invii_avvisi_condominio') IS NULL THEN
        CREATE INDEX idx_invii_avvisi_condominio ON centrale_termica.invii_avvisi (id_condominio, ts_invio);
    END IF;
END
$$;

DROP TRIGGER IF EXISTS tr_invii_avvisi_immutabili ON centrale_termica.invii_avvisi;
CREATE TRIGGER tr_invii_avvisi_immutabili
    BEFORE UPDATE OR DELETE ON centrale_termica.invii_avvisi
    FOR EACH ROW EXECUTE PROCEDURE centrale_termica.fn_storico_immutabile();
DROP TRIGGER IF EXISTS tr_invii_avvisi_no_truncate ON centrale_termica.invii_avvisi;
CREATE TRIGGER tr_invii_avvisi_no_truncate
    BEFORE TRUNCATE ON centrale_termica.invii_avvisi
    FOR EACH STATEMENT EXECUTE PROCEDURE centrale_termica.fn_storico_immutabile();


-- ---------------------------------------------------------------------------------------------
-- parametri degli avvisi (solo se mancano)
-- ---------------------------------------------------------------------------------------------

INSERT INTO centrale_termica.parametri (chiave, valore, tipo_valore, descrizione, id_utente_ins)
SELECT v.chiave, v.valore, v.tipo_valore, v.descrizione, 0
  FROM (VALUES
       ('ALERT.ATTIVO',             'S',          'BOOLEANO', 'Avvisi di scadenza attivi (il controllo giornaliero li prepara e li invia).'),
       ('ALERT.GIORNI',             '90,60,30',   'ELENCO',   'Giorni di anticipo a cui mandare un avviso (es. 90,60,30,7).'),
       ('ALERT.SCADUTE',            'S',          'BOOLEANO', 'Manda anche un avviso quando la scadenza e'' superata.'),
       ('ALERT.AMMINISTRATORE',     'S',          'BOOLEANO', 'Destinatario A: l''amministratore del condominio (email dell''utente amministratore).'),
       ('ALERT.CC',                 '',           'ELENCO',   'Indirizzi in copia (Cc), separati da virgola (es. installatore).'),
       ('ALERT.OGGETTO',            'Scadenza {sigla} - {condominio}', 'TESTO', 'Oggetto della mail; segnaposto {sigla} {documento} {condominio} {data}.'),
       ('ALERT.LINK',               '',           'URL',      'Indirizzo della pagina da citare nella mail (es. https://weraweb.it/wera2026/). Vuoto = nessun link.'),
       ('ALERT.TOKEN',              md5(random()::text || clock_timestamp()::text), 'TESTO', 'Chiave del controllo giornaliero automatico (cron sul server, solo da localhost).')
       ) AS v (chiave, valore, tipo_valore, descrizione)
 WHERE NOT EXISTS (SELECT 1 FROM centrale_termica.parametri p WHERE p.id_azienda IS NULL AND p.chiave = v.chiave);

-- scelte dell'operatore del 2026-10-26, solo su valori mai modificati
UPDATE centrale_termica.parametri SET valore = '90'
 WHERE id_azienda IS NULL AND chiave = 'SCADENZE.GIORNI_PREAVVISO' AND valore = '30' AND ts_mod IS NULL;
UPDATE centrale_termica.parametri SET valore = ''
 WHERE id_azienda IS NULL AND chiave = 'MENU.VOCE_WERA.PROFILI' AND valore = 'ADM,INS' AND ts_mod IS NULL;

COMMIT;
