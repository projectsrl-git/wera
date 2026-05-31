/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists utenze_dettaglio cascade;

-- create tables section -------------------------------------------------


-- table utenze_dettaglio

 create table utenze_dettaglio(
 id_dettaglio serial not null,
 id_modulo integer not null,
 nr_dettaglio character varying(10) not null,
 
 stanza character varying(50) not null,
 tipo character varying(50) ,
 marca character varying(50) ,
 larghezza numeric(12,2) ,
 altezza numeric(12,2) ,
 profondita numeric(12,2) ,
 elementi character varying(5) ,
 potenza numeric(12,2) ,
 esp numeric(12,2) ,
 esp_2 numeric(12,2) ,
 rilevatore character varying(10) not null,
 n_prog integer ,
 coeff integer ,
 pos character varying(3) ,
 diametro character varying(5) ,
 mat_tubo character varying(3) ,
 prereg character varying(3) ,
 tipo_valvola character varying(3) ,
 
 tabella_parent character varying(30),
 id_dettaglio_parent integer ,
 
 ts_ins timestamp default current_timestamp not null,
 id_utente_ins integer not null,
 ts_mod timestamp,
 id_utente_mod integer
 
 
)
;

-- add keys for table utenze_dettaglio

alter table utenze_dettaglio add constraint pk_utenze_dettaglio primary key (id_dettaglio)
;

alter table utenze_dettaglio add constraint k1_utenze_dettaglio unique (id_dettaglio)
;





