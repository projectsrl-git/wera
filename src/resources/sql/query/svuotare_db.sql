delete from aziende WHERE ID_AZIENDA>1; 
UPDATE AZIENDE SET CODICE='WER', RAGSOC='RAGIONE SOCIALE AZIENDA',PARTITA_IVA='',CODICE_FISCALE='',TIPO_AZIENDA='',CODFISCALE_AZ='',PARTITAIVA='',TELEFONO='',MAIL='',NOME='',COGNOME='',SESSO='',D_NASCITA='',LOCNASCITA='',PROVNASCITA='',CODFISCALE='',CREDITI=0;

delete from condomini;
delete from configurazione_errori WHERE ID_AZIENDA>1;
UPDATE CONFIGURAZIONE_ERRORI SET MAIL=false,COLORE='';

delete from contatori;
delete from dati_rilevatori;
UPDATE DISTRIBUTION_LIST SET LISTA_MAIL='';
delete from elenco_file_importati_file_manager;
delete from elenco_file_importati_rilevatori;
delete from email_log;
delete from network;
delete from news_aziende;
delete from news_profili;
delete from news;
delete from password WHERE ID_UTENTE>1;
delete from profili_azienda where id_azienda>1;
delete from rilevatori_eliminati;
delete from ripartizioni;
delete from ripartizioni_dettaglio;
delete from ripartizioni_dettaglio_singolo;
delete from ripartizioni_letture;
delete from ripartizioni_letture_dettaglio;
delete from ripartizioni_letture_dettaglio_singolo;
delete from ripartizioni_uni;
delete from ripartizioni_uni_dettaglio;
delete from ripartizioni_uni_dettaglio_singolo;
delete from scarico;
delete from utenti WHERE ID_UTENTE>1;
delete from utenti_aziende WHERE ID_UTENTE>1;
delete from utenti_lingue WHERE ID_UTENTE>1;
delete from utenti_profili WHERE ID_UTENTE>1;
delete from utenti_utenze WHERE ID_UTENTE>1;
delete from utenze;
delete from utenze_dettaglio;


--ALTER SEQUENCE aziende RESTART WITH 2;
--ALTER SEQUENCE condomini RESTART WITH 1;
--SELECT setval(pg_get_serial_sequence('configurazione_errori', 'id_modulo'), count(*)+1) FROM configurazione_errori;
--ALTER SEQUENCE contatori RESTART WITH 1;
--ALTER SEQUENCE dati_rilevatori RESTART WITH 1;

--ALTER SEQUENCE elenco_file_importati_file_manager RESTART WITH 1;
--ALTER SEQUENCE elenco_file_importati_rilevatori RESTART WITH 1;
--ALTER SEQUENCE email_log RESTART WITH 1;
--ALTER SEQUENCE network RESTART WITH 1;
--ALTER SEQUENCE news_aziende RESTART WITH 1;
--ALTER SEQUENCE news_profili RESTART WITH 1;
--ALTER SEQUENCE news RESTART WITH 1;
--SELECT setval(pg_get_serial_sequence('password', 'id_password'), count(*)+1) FROM password;
--SELECT setval(pg_get_serial_sequence('profili_azienda', 'id_profilo_azienda'), count(*)+1) FROM profili_azienda;
--ALTER SEQUENCE rilevatori_eliminati RESTART WITH 1;
--ALTER SEQUENCE ripartizioni RESTART WITH 1;
--ALTER SEQUENCE ripartizioni_dettaglio RESTART WITH 1;
--ALTER SEQUENCE ripartizioni_dettaglio_singolo RESTART WITH 1;
--ALTER SEQUENCE ripartizioni_letture RESTART WITH 1;
--ALTER SEQUENCE ripartizioni_letture_dettaglio RESTART WITH 1;
--ALTER SEQUENCE ripartizioni_letture_dettaglio_singolo RESTART WITH 1;
--ALTER SEQUENCE ripartizioni_uni RESTART WITH 1;
--ALTER SEQUENCE ripartizioni_uni_dettaglio RESTART WITH 1;
--ALTER SEQUENCE ripartizioni_uni_dettaglio_singolo RESTART WITH 1;
--ALTER SEQUENCE scarico RESTART WITH 1;
--SELECT setval(pg_get_serial_sequence('utenti', 'id_utente'), count(*)+1) FROM utenti;
--SELECT setval(pg_get_serial_sequence('utenti_aziende', 'id_utente_azienda'), count(*)+1) FROM utenti_aziende;
--SELECT setval(pg_get_serial_sequence('utenti_lingue', 'id_utenti_lingue'), count(*)+1) FROM utenti_lingue;
--SELECT setval(pg_get_serial_sequence('utenti_profili', 'id_utente_profilo'), count(*)+1) FROM utenti_profili;
--SELECT setval(pg_get_serial_sequence('utenti_utenze', 'id_utente_utenza'), count(*)+1) FROM utenti_utenze;
--ALTER SEQUENCE utenze RESTART WITH 1;
--ALTER SEQUENCE utenze_dettaglio RESTART WITH 1;







SELECT setval(pg_get_serial_sequence('aziende', 'id_azienda'), count(*)+1) FROM aziende;
SELECT setval(pg_get_serial_sequence('condomini', 'id_modulo'), count(*)+1) FROM condomini;
SELECT setval(pg_get_serial_sequence('configurazione_errori', 'id_modulo'), count(*)+1) FROM configurazione_errori;
SELECT setval(pg_get_serial_sequence('contatori', 'id_modulo'), count(*)+1) FROM contatori;
SELECT setval(pg_get_serial_sequence('dati_rilevatori', 'id_modulo'), count(*)+1) FROM dati_rilevatori;
SELECT setval(pg_get_serial_sequence('elenco_file_importati_file_manager', 'id_modulo'), count(*)+1) FROM elenco_file_importati_file_manager;
SELECT setval(pg_get_serial_sequence('elenco_file_importati_rilevatori', 'id_modulo'), count(*)+1) FROM elenco_file_importati_rilevatori;
SELECT setval(pg_get_serial_sequence('email_log', 'id_email_log'), count(*)+1) FROM email_log;
SELECT setval(pg_get_serial_sequence('network', 'id_modulo'), count(*)+1) FROM network;
SELECT setval(pg_get_serial_sequence('news_aziende', 'id_news_azienda'), count(*)+1) FROM news_aziende;
SELECT setval(pg_get_serial_sequence('news_profili', 'id_news_profilo'), count(*)+1) FROM news_profili;
SELECT setval(pg_get_serial_sequence('news', 'id_news'), count(*)+1) FROM news;
SELECT setval(pg_get_serial_sequence('password', 'id_password'), count(*)+1) FROM password;
SELECT setval(pg_get_serial_sequence('profili_azienda', 'id_profilo_azienda'), count(*)+1) FROM profili_azienda;

SELECT setval(pg_get_serial_sequence('rilevatori_eliminati', 'id_elimina'), count(*)+1) FROM rilevatori_eliminati;
SELECT setval(pg_get_serial_sequence('ripartizioni', 'id_modulo'), count(*)+1) FROM ripartizioni;
SELECT setval(pg_get_serial_sequence('ripartizioni_dettaglio', 'id_dettaglio'), count(*)+1) FROM ripartizioni_dettaglio;
SELECT setval(pg_get_serial_sequence('ripartizioni_dettaglio_singolo', 'id_dettaglio'), count(*)+1) FROM ripartizioni_dettaglio_singolo;
SELECT setval(pg_get_serial_sequence('ripartizioni_letture', 'id_modulo'), count(*)+1) FROM ripartizioni_letture;
SELECT setval(pg_get_serial_sequence('ripartizioni_letture_dettaglio', 'id_dettaglio'), count(*)+1) FROM ripartizioni_letture_dettaglio;
SELECT setval(pg_get_serial_sequence('ripartizioni_letture_dettaglio_singolo', 'id_dettaglio'), count(*)+1) FROM ripartizioni_letture_dettaglio_singolo;
SELECT setval(pg_get_serial_sequence('ripartizioni_uni', 'id_modulo'), count(*)+1) FROM ripartizioni_uni;
SELECT setval(pg_get_serial_sequence('ripartizioni_uni_dettaglio', 'id_dettaglio'), count(*)+1) FROM ripartizioni_uni_dettaglio;
SELECT setval(pg_get_serial_sequence('ripartizioni_uni_dettaglio_singolo', 'id_dettaglio'), count(*)+1) FROM ripartizioni_uni_dettaglio_singolo;
SELECT setval(pg_get_serial_sequence('scarico', 'id_modulo'), count(*)+1) FROM scarico;


SELECT setval(pg_get_serial_sequence('utenti', 'id_utente'), count(*)+1) FROM utenti;
SELECT setval(pg_get_serial_sequence('utenti_aziende', 'id_utente_azienda'), count(*)+1) FROM utenti_aziende;
SELECT setval(pg_get_serial_sequence('utenti_lingue', 'id_utenti_lingue'), count(*)+1) FROM utenti_lingue;
SELECT setval(pg_get_serial_sequence('utenti_profili', 'id_utente_profilo'), count(*)+1) FROM utenti_profili;
SELECT setval(pg_get_serial_sequence('utenti_utenze', 'id_utente_utenza'), count(*)+1) FROM utenti_utenze;

SELECT setval(pg_get_serial_sequence('utenze', 'id_modulo'), count(*)+1) FROM utenze;
SELECT setval(pg_get_serial_sequence('utenze_dettaglio', 'id_dettaglio'), count(*)+1) FROM utenze_dettaglio;








SELECT setval(pg_get_serial_sequence('aziende', 'id_azienda'), (SELECT MAX(id_azienda) FROM aziende)+1);
SELECT setval(pg_get_serial_sequence('condomini', 'id_modulo'), (SELECT MAX(id_modulo) FROM condomini)+1);
SELECT setval(pg_get_serial_sequence('configurazione_errori', 'id_modulo'), (SELECT MAX(id_modulo) FROM configurazione_errori)+1);
SELECT setval(pg_get_serial_sequence('contatori', 'id_modulo'), (SELECT MAX(id_modulo) FROM contatori)+1);
SELECT setval(pg_get_serial_sequence('dati_rilevatori', 'id_modulo'), (SELECT MAX(id_modulo) FROM dati_rilevatori)+1);
--SELECT setval(pg_get_serial_sequence('elenco_file_importati_file_manager', 'id_modulo'), (SELECT MAX(id_modulo) FROM elenco_file_importati_file_manager)+1);
SELECT setval(pg_get_serial_sequence('elenco_file_importati_rilevatori', 'id_modulo'), (SELECT MAX(id_modulo) FROM elenco_file_importati_rilevatori)+1);
--SELECT setval(pg_get_serial_sequence('email_log', 'id_email_log'), (SELECT MAX(id_email_log) FROM email_log)+1);
SELECT setval(pg_get_serial_sequence('network', 'id_modulo'), (SELECT MAX(id_modulo) FROM network)+1);
--SELECT setval(pg_get_serial_sequence('news_aziende', 'id_news_azienda'), (SELECT MAX(id_news_azienda) FROM news_aziende)+1);
--SELECT setval(pg_get_serial_sequence('news_profili', 'id_news_profilo'), (SELECT MAX(id_news_profilo) FROM news_profili)+1);
--SELECT setval(pg_get_serial_sequence('news', 'id_news'), (SELECT MAX(id_news) FROM news)+1);
SELECT setval(pg_get_serial_sequence('password', 'id_password'), (SELECT MAX(id_password) FROM password)+1);
SELECT setval(pg_get_serial_sequence('profili_azienda', 'id_profilo_azienda'), (SELECT MAX(id_profilo_azienda) FROM profili_azienda)+1);

--SELECT setval(pg_get_serial_sequence('rilevatori_eliminati', 'id_elimina'), (SELECT MAX(id_elimina) FROM rilevatori_eliminati)+1);
SELECT setval(pg_get_serial_sequence('ripartizioni', 'id_modulo'), (SELECT MAX(id_modulo) FROM ripartizioni)+1);
SELECT setval(pg_get_serial_sequence('ripartizioni_dettaglio', 'id_dettaglio'), (SELECT MAX(id_dettaglio) FROM ripartizioni_dettaglio)+1);
SELECT setval(pg_get_serial_sequence('ripartizioni_dettaglio_singolo', 'id_dettaglio'), (SELECT MAX(id_dettaglio) FROM ripartizioni_dettaglio_singolo)+1);
SELECT setval(pg_get_serial_sequence('ripartizioni_letture', 'id_modulo'), (SELECT MAX(id_modulo) FROM ripartizioni_letture)+1);
SELECT setval(pg_get_serial_sequence('ripartizioni_letture_dettaglio', 'id_dettaglio'), (SELECT MAX(id_dettaglio) FROM ripartizioni_letture_dettaglio)+1);
SELECT setval(pg_get_serial_sequence('ripartizioni_letture_dettaglio_singolo', 'id_dettaglio'), (SELECT MAX(id_dettaglio) FROM ripartizioni_letture_dettaglio_singolo)+1);
SELECT setval(pg_get_serial_sequence('ripartizioni_uni', 'id_modulo'), (SELECT MAX(id_modulo) FROM ripartizioni_uni)+1);
SELECT setval(pg_get_serial_sequence('ripartizioni_uni_dettaglio', 'id_dettaglio'), (SELECT MAX(id_dettaglio) FROM ripartizioni_uni_dettaglio)+1);
SELECT setval(pg_get_serial_sequence('ripartizioni_uni_dettaglio_singolo', 'id_dettaglio'), (SELECT MAX(id_dettaglio) FROM ripartizioni_uni_dettaglio_singolo)+1);
SELECT setval(pg_get_serial_sequence('scarico', 'id_modulo'), (SELECT MAX(id_modulo) FROM scarico)+1);


SELECT setval(pg_get_serial_sequence('utenti', 'id_utente'), (SELECT MAX(id_utente) FROM utenti)+1);
SELECT setval(pg_get_serial_sequence('utenti_aziende', 'id_utente_azienda'), (SELECT MAX(id_utente_azienda) FROM utenti_aziende)+1);
SELECT setval(pg_get_serial_sequence('utenti_lingue', 'id_utenti_lingue'), (SELECT MAX(id_utenti_lingue) FROM utenti_lingue)+1);
SELECT setval(pg_get_serial_sequence('utenti_profili', 'id_utente_profilo'), (SELECT MAX(id_utente_profilo) FROM utenti_profili)+1);
--SELECT setval(pg_get_serial_sequence('utenti_utenze', 'id_utente_utenza'), (SELECT MAX(id_utente_utenza) FROM utenti_utenze)+1);

SELECT setval(pg_get_serial_sequence('utenze', 'id_modulo'), (SELECT MAX(id_modulo) FROM utenze)+1);
SELECT setval(pg_get_serial_sequence('utenze_dettaglio', 'id_dettaglio'), (SELECT MAX(id_dettaglio) FROM utenze_dettaglio)+1);