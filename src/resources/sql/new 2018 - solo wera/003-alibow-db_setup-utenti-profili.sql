-- AZIENDE
DELETE FROM AZIENDE;
ALTER SEQUENCE AZIENDE_id_azienda_seq RESTART WITH 1;
insert into aziende (codice,ragsoc,partita_iva,codice_fiscale,alias_azienda) values ('WER','WERA','0','0','wera');

-- PROFILI 
DELETE FROM PROFILI;
ALTER SEQUENCE PROFILI_id_profilo_seq RESTART WITH 1;


INSERT INTO PROFILI (CODICE,DESCRIZIONE) VALUES ('ADM','SupeAdmin');
INSERT INTO PROFILI (CODICE,DESCRIZIONE) VALUES ('INS','Gestore impianto');
INSERT INTO PROFILI (CODICE,DESCRIZIONE) VALUES ('AMM','Amministratore condominio');
INSERT INTO PROFILI (CODICE,DESCRIZIONE) VALUES ('CON','Condomino');
INSERT INTO PROFILI (CODICE,DESCRIZIONE) VALUES ('CEN','Centrali termiche');

-- utenti 
delete from utenti;          
ALTER SEQUENCE utenti_id_utente_seq RESTART WITH 1;

INSERT INTO utenti(username,cognome,nome,email) VALUES ('support@wera.club','ADM','Assistenza','assistenza@projectsrl.net');

-- password
DELETE FROM PASSWORD;
ALTER SEQUENCE PASSWORD_id_password_seq RESTART WITH 1;
INSERT INTO PASSWORD (ID_UTENTE,PASSWORD,DT_SCADENZA,FL_VALIDA) SELECT ID_UTENTE,'32ca9fc1a0f5b633e3f4c8c1bbecde9bedb9573','2999/12/31',TRUE FROM UTENTI;

--- UTENTI_LINGUE
DELETE FROM UTENTI_LINGUE;
ALTER SEQUENCE UTENTI_LINGUE_id_utenti_lingue_seq RESTART WITH 1;
INSERT INTO UTENTI_LINGUE (ID_UTENTE,id_lingue_iso,FL_DEFAULT) SELECT ID_UTENTE,(select id_lingue_iso from LINGUE_ISO where codice_iso='it') as id_lingue_iso,true FROM UTENTI;

--- UTENTI_AZIENDE
DELETE FROM UTENTI_AZIENDE;
ALTER SEQUENCE UTENTI_AZIENDE_id_utente_azienda_seq RESTART WITH 1;
INSERT INTO UTENTI_AZIENDE (ID_UTENTE,ID_AZIENDA) select id_utente,(select id_azienda from AZIENDE where alias_azienda='wera') from utenti ;


/****************** UTENTI_PROFILI *******************/
DELETE FROM UTENTI_PROFILI;
ALTER SEQUENCE UTENTI_PROFILI_id_utente_profilo_seq RESTART WITH 1;


INSERT INTO UTENTI_PROFILI (ID_UTENTE,ID_PROFILO) VALUES ((SELECT ID_UTENTE FROM UTENTI WHERE USERNAME='support@wera.club'),(SELECT ID_PROFILO FROM PROFILI WHERE CODICE='ADM'));


/****************** MENU_PROFILI *******************/
DELETE FROM MENU_PROFILI;
ALTER SEQUENCE MENU_PROFILI_id_menu_profili_seq RESTART WITH 1;

DELETE FROM MENU_PROFILI where id_profilo = (select id_profilo from PROFILI where codice='ADM') ;
insert into menu_profili (id_menu,id_profilo) select id_menu,(select id_profilo from PROFILI where codice='ADM') from MENU;