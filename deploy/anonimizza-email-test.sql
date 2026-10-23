-- anonimizza-email-test.sql
-- Riscrive TUTTI gli indirizzi email del DB di TEST verso un'unica casella dell'operatore,
-- cosi nessuna mail di collaudo (reset, registrazione, workflow) raggiunge persone reali.
--
-- USO (solo DB locale; l'indirizzo di destinazione si passa a riga di comando, non sta nel repo):
--   set PGPASSWORD=...
--   "D:\Programmi\PostgreSQL\9.4\bin\psql.exe" -h 127.0.0.1 -U postgres -d wera -X ^
--       -v ON_ERROR_STOP=1 -v dest=nome.cognome@dominio.it -f deploy\anonimizza-email-test.sql
--
-- Regole:
--   * utenti.email     -> nome.cognome+u<ID_UTENTE>@dominio.it (plus-addressing): l'indirizzo
--                         resta UNICO per utente perche il reset password cerca l'utente per
--                         EMAIL e richiede che sia univoca (FunctionResetPassword.isUniqueEmail).
--   * utenti.username  -> NON toccato: e la login (indice unico k2_utenti), non un destinatario.
--   * ogni altra colonna di testo di ogni tabella (distribution_list, email_log, mail_config,
--     e qualunque tabella presente nel dump): ogni indirizzo trovato -> dest.
--   * Rilanciabile: una seconda esecuzione non cambia nulla.
--   * Rifiuta di girare se il server non e su loopback.
--   * Rifiuta di girare se il DB NON e marcato come copia di test. Il nome non basta: la
--     produzione si chiama "wera" come molte copie locali. Il marcatore e il commento del
--     database, da impostare UNA volta, a mano, SOLO su una copia ripristinata:
--       COMMENT ON DATABASE <nome> IS 'WERA-TEST: copia di prova, dati sacrificabili';
--     Sul DB di produzione quel commento non deve MAI esistere.

\set QUIET on
BEGIN;

SET LOCAL wera.dest = :'dest';

DO $$
DECLARE
    dest   text := current_setting('wera.dest');
    re     text := '[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}';
    srv    text := host(inet_server_addr());
    marca  text := coalesce(shobj_description(
                       (SELECT oid FROM pg_database WHERE datname = current_database()),
                       'pg_database'), '');
    r      record;
    n      bigint;
BEGIN
    IF srv IS NOT NULL AND srv NOT IN ('127.0.0.1', '::1') THEN
        RAISE EXCEPTION 'server % non locale: script consentito solo sul DB di test in locale', srv;
    END IF;
    IF left(marca, 9) <> 'WERA-TEST' THEN
        RAISE EXCEPTION 'il database "%" non e marcato come copia di test (commento attuale: "%"). '
            'Se e DAVVERO una copia sacrificabile: COMMENT ON DATABASE % IS ''WERA-TEST: ...''; '
            'sul DB di produzione NON farlo mai.', current_database(), marca, current_database();
    END IF;
    IF dest !~ ('^' || re || '$') OR position('+' in dest) > 0 THEN
        RAISE EXCEPTION 'dest non valido (serve un indirizzo semplice, senza +): %', dest;
    END IF;

    UPDATE utenti
       SET email = split_part(dest, '@', 1) || '+u' || id_utente || '@' || split_part(dest, '@', 2)
     WHERE email ~ re
       AND email <> (split_part(dest, '@', 1) || '+u' || id_utente || '@' || split_part(dest, '@', 2));
    GET DIAGNOSTICS n = ROW_COUNT;
    RAISE NOTICE 'public.utenti.email: % righe', n;

    FOR r IN
        SELECT c.table_schema AS s, c.table_name AS t, c.column_name AS col
          FROM information_schema.columns c
          JOIN information_schema.tables tb
            ON tb.table_schema = c.table_schema AND tb.table_name = c.table_name
         WHERE tb.table_type = 'BASE TABLE'
           AND c.table_schema NOT IN ('pg_catalog', 'information_schema')
           AND c.data_type IN ('character varying', 'text', 'character')
           AND NOT (c.table_schema = 'public' AND c.table_name = 'utenti'
                    AND c.column_name IN ('email', 'username'))
         ORDER BY 1, 2, 3
    LOOP
        EXECUTE format('UPDATE %I.%I SET %I = regexp_replace(%I, %L, %L, ''g'') '
                       'WHERE %I ~ %L AND regexp_replace(%I, %L, %L, ''g'') <> %I',
                       r.s, r.t, r.col, r.col, re, dest,
                       r.col, re, r.col, re, dest, r.col);
        GET DIAGNOSTICS n = ROW_COUNT;
        IF n > 0 THEN
            RAISE NOTICE '%.%.%: % righe', r.s, r.t, r.col, n;
        END IF;
    END LOOP;
END $$;

COMMIT;
