DROP TABLE IF EXISTS DISTRIBUTION_LIST_TEMP;

CREATE TABLE DISTRIBUTION_LIST_TEMP
(
	modulo character varying(30) not null,
	id_azienda integer not null,
	lista_mail text not NULL
);

INSERT INTO DISTRIBUTION_LIST_TEMP (modulo,id_azienda,lista_mail) SELECT modulo,id_azienda,lista_mail FROM DISTRIBUTION_LIST;

DROP TABLE IF EXISTS DISTRIBUTION_LIST CASCADE;

CREATE TABLE DISTRIBUTION_LIST
(
	id_distribution_list serial not null,
	modulo character varying(30) not null,
	id_azienda integer not null,
	lista_mail text not NULL,
	id_utente_ins integer,
	ts_ins timestamp NOT NULL DEFAULT current_timestamp
);


INSERT INTO DISTRIBUTION_LIST (modulo,id_azienda,lista_mail,id_utente_ins) SELECT modulo,id_azienda,lista_mail,(SELECT id_utente FROM utenti WHERE username ='assistenza') FROM DISTRIBUTION_LIST_TEMP;

DROP TABLE IF EXISTS DISTRIBUTION_LIST_TEMP;


DROP TABLE IF EXISTS MAIL_CONFIG_TEMP CASCADE;

CREATE TABLE MAIL_CONFIG_TEMP
(
	modulo character varying(30) not null,
	workflow_action character varying(30) not null,
	stato_iniziale char(3) not null,
	stato_finale char(3) not null,
	oggetto text not null,
	testo_mail text not null,
	mittente text
);

insert into MAIL_CONFIG_TEMP (	
	modulo,
	workflow_action,
	stato_iniziale,
	stato_finale,
	oggetto,
	mittente,
	testo_mail
) 
SELECT
	modulo,
	workflow_action,
	stato_iniziale,
	stato_finale,
	oggetto,
	mittente,
	testo_mail
FROM mail_config
;

DROP TABLE IF EXISTS MAIL_CONFIG CASCADE;

CREATE TABLE MAIL_CONFIG
(
	ID_MAIL_CONFIG SERIAL,
	modulo character varying(30) not null,
	workflow_action character varying(30) not null,
	stato_iniziale char(3) not null,
	stato_finale char(3) not null,
	oggetto text not null,
	testo_mail text not null,
	mittente text,
	id_utente_ins integer,
	ts_ins timestamp NOT NULL DEFAULT current_timestamp
);

insert into MAIL_CONFIG (	
	modulo,
	workflow_action,
	stato_iniziale,
	stato_finale,
	oggetto,
	mittente,
	testo_mail,
	id_utente_ins
) 
SELECT
	modulo,
	workflow_action,
	stato_iniziale,
	stato_finale,
	oggetto,
	mittente,
	testo_mail,
	(SELECT id_utente FROM utenti WHERE username ='assistenza')
FROM MAIL_CONFIG_TEMP
;



DROP TABLE IF EXISTS MAIL_CONFIG_TEMP CASCADE;
