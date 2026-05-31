update PASSWORD set FL_VALIDA = false;
INSERT INTO PASSWORD (ID_UTENTE,PASSWORD,DT_SCADENZA,FL_VALIDA) SELECT ID_UTENTE,'32ca9fc1a0f5b633e3f4c8c1bbecde9bedb9573','9999/12/31',TRUE FROM UTENTI;

update utenti set email='assistenza@projectsrl.net';
update DISTRIBUTION_LIST set lista_mail = 'assistenza@projectsrl.net' ;