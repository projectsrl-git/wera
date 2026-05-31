-- AZIENDE
DELETE FROM AZIENDE;
ALTER SEQUENCE AZIENDE_id_azienda_seq RESTART WITH 1;
insert into aziende (codice,ragsoc,partita_iva,codice_fiscale,alias_azienda) values ('ALI','Air Liquide Italia','11111111111','1111111111111111','airliquide');

-- PROFILI 
DELETE FROM PROFILI;
ALTER SEQUENCE PROFILI_id_profilo_seq RESTART WITH 1;

INSERT INTO PROFILI (CODICE,DESCRIZIONE) VALUES ('ADM','Administrator');
INSERT INTO PROFILI (CODICE,DESCRIZIONE) VALUES ('AIA','All In One Administrator');
INSERT INTO PROFILI (CODICE,DESCRIZIONE) VALUES ('VIE','Catalogs Viewer');
INSERT INTO PROFILI (CODICE,DESCRIZIONE) VALUES ('TEC','Testing Center');

INSERT INTO PROFILI (CODICE,DESCRIZIONE) VALUES ('QHS','Administrator QHSE');
INSERT INTO PROFILI (CODICE,DESCRIZIONE) VALUES ('CON','Contributor');
INSERT INTO PROFILI (CODICE,DESCRIZIONE) VALUES ('APP','Approvatore');

INSERT INTO PROFILI (CODICE,DESCRIZIONE) VALUES ('PLA','PdL Admin');
INSERT INTO PROFILI (CODICE,DESCRIZIONE) VALUES ('CPT','Capo turno');
INSERT INTO PROFILI (CODICE,DESCRIZIONE) VALUES ('DLV','Delegato lavori');

-- utenti 
delete from utenti;          
ALTER SEQUENCE utenti_id_utente_seq RESTART WITH 1;

INSERT INTO utenti(username,cognome,nome,email) VALUES ('assistenza','ALI','Assistenza','assistenza.ali@projectsrl.net');
INSERT INTO utenti(username,cognome,nome,email) VALUES ('armando.canto@airliquide.com','Canto','Armando','armando.canto@airliquide.com');
INSERT INTO utenti(username,cognome,nome,email) VALUES ('chiara.romanoni@airliquide.com','Romanoni','Chiara','chiara.romanoni@airliquide.com');
INSERT INTO utenti(username,cognome,nome,email) VALUES ('antonio.bertolino@airliquide.com','Bertolino','Antonio','antonio.bertolino@airliquide.com');
INSERT INTO utenti(username,cognome,nome,email) VALUES ('mariacarmela.chiuri@airliquide.com','Chiuri','Maria Carmela','mariacarmela.chiuri@airliquide.com');
INSERT INTO utenti(username,cognome,nome,email) VALUES ('marta.belle@airliquide.com','Belle','Marta','marta.belle@airliquide.com');
INSERT INTO utenti(username,cognome,nome,email) VALUES ('francesco.miduri@airliquide.com','Miduri','Francesco','francesco.miduri@airliquide.com');
INSERT INTO utenti(username,cognome,nome,email) VALUES ('giuseppe.greco@airliquide.com','Greco','Giuseppe','giuseppe.greco@airliquide.com');
INSERT INTO utenti(username,cognome,nome,email) VALUES ('luigi.oddo@airliquide.com','Oddo','Luigi','luigi.oddo@airliquide.com');

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
INSERT INTO UTENTI_AZIENDE (ID_UTENTE,ID_AZIENDA) select id_utente,(select id_azienda from AZIENDE where alias_azienda='airliquide') from utenti ;


/****************** UTENTI_PROFILI *******************/
DELETE FROM UTENTI_PROFILI;
ALTER SEQUENCE UTENTI_PROFILI_id_utente_profilo_seq RESTART WITH 1;


INSERT INTO UTENTI_PROFILI (ID_UTENTE,ID_PROFILO) VALUES ((SELECT ID_UTENTE FROM UTENTI WHERE USERNAME='assistenza'),(SELECT ID_PROFILO FROM PROFILI WHERE CODICE='ADM'));

INSERT INTO UTENTI_PROFILI (ID_UTENTE,ID_PROFILO) VALUES ((SELECT ID_UTENTE FROM UTENTI WHERE USERNAME='armando.canto@airliquide.com'),(SELECT ID_PROFILO FROM PROFILI WHERE CODICE='AIA'));
INSERT INTO UTENTI_PROFILI (ID_UTENTE,ID_PROFILO) VALUES ((SELECT ID_UTENTE FROM UTENTI WHERE USERNAME='chiara.romanoni@airliquide.com'),(SELECT ID_PROFILO FROM PROFILI WHERE CODICE='VIE'));
INSERT INTO UTENTI_PROFILI (ID_UTENTE,ID_PROFILO) VALUES ((SELECT ID_UTENTE FROM UTENTI WHERE USERNAME='antonio.bertolino@airliquide.com'),(SELECT ID_PROFILO FROM PROFILI WHERE CODICE='TEC'));

INSERT INTO UTENTI_PROFILI (ID_UTENTE,ID_PROFILO) VALUES ((SELECT ID_UTENTE FROM UTENTI WHERE USERNAME='mariacarmela.chiuri@airliquide.com'),(SELECT ID_PROFILO FROM PROFILI WHERE CODICE='QHS'));
INSERT INTO UTENTI_PROFILI (ID_UTENTE,ID_PROFILO) VALUES ((SELECT ID_UTENTE FROM UTENTI WHERE USERNAME='marta.belle@airliquide.com'),(SELECT ID_PROFILO FROM PROFILI WHERE CODICE='CON'));

INSERT INTO UTENTI_PROFILI (ID_UTENTE,ID_PROFILO) VALUES ((SELECT ID_UTENTE FROM UTENTI WHERE USERNAME='francesco.miduri@airliquide.com'),(SELECT ID_PROFILO FROM PROFILI WHERE CODICE='PLA'));
INSERT INTO UTENTI_PROFILI (ID_UTENTE,ID_PROFILO) VALUES ((SELECT ID_UTENTE FROM UTENTI WHERE USERNAME='giuseppe.greco@airliquide.com'),(SELECT ID_PROFILO FROM PROFILI WHERE CODICE='DLV'));
INSERT INTO UTENTI_PROFILI (ID_UTENTE,ID_PROFILO) VALUES ((SELECT ID_UTENTE FROM UTENTI WHERE USERNAME='luigi.oddo@airliquide.com'),(SELECT ID_PROFILO FROM PROFILI WHERE CODICE='CPT'));


/****************** MENU_PROFILI *******************/
DELETE FROM MENU_PROFILI;
ALTER SEQUENCE MENU_PROFILI_id_menu_profili_seq RESTART WITH 1;

DELETE FROM MENU_PROFILI where id_profilo = (select id_profilo from PROFILI where codice='ADM') ;
insert into menu_profili (id_menu,id_profilo) select id_menu,(select id_profilo from PROFILI where codice='ADM') from MENU;

/*
insert into menu_profili (id_menu,id_profilo) select id_menu,(select id_profilo from PROFILI where codice='AIA') from MENU;

insert into menu_profili (id_menu,id_profilo) select id_menu,(select id_profilo from PROFILI where codice='VIE') from MENU;

insert into menu_profili (id_menu,id_profilo) select id_menu,(select id_profilo from PROFILI where codice='TEC') from MENU;

insert into menu_profili (id_menu,id_profilo) select id_menu,(select id_profilo from PROFILI where codice='QHS') from MENU;

insert into menu_profili (id_menu,id_profilo) select id_menu,(select id_profilo from PROFILI where codice='CON') from MENU;

insert into menu_profili (id_menu,id_profilo) select id_menu,(select id_profilo from PROFILI where codice='PLA') from MENU;

insert into menu_profili (id_menu,id_profilo) select id_menu,(select id_profilo from PROFILI where codice='DLV') from MENU;

insert into menu_profili (id_menu,id_profilo) select id_menu,(select id_profilo from PROFILI where codice='CPT') from MENU;
*/


