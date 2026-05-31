drop VIEW if exists V_UTENTI_PROFILI cascade;
DROP VIEW IF exists v_menu CASCADE;
DROP VIEW IF exists v_menu_tree;
DROP VIEW IF exists V_AZIENDE_TREE;
DROP VIEW IF exists V_DIPENDENTI;
DROP VIEW IF exists V_UTENTI;
DROP VIEW IF exists V_LOGIN;

ALTER TABLE utenti ALTER COLUMN username TYPE character varying(100);
ALTER TABLE utenti ALTER COLUMN nome TYPE character varying(100);
ALTER TABLE utenti ALTER COLUMN cognome TYPE character varying(100);


-- rilanciare tutte le viste 999-viste.sql