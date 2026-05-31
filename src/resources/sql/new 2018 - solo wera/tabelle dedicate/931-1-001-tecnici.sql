/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists tecnici cascade;

-- create tables section -------------------------------------------------


-- table tecnici

 create table tecnici(
 id_modulo serial not null,
 id_azienda bigint not null,
 
 qualifica character varying(100) null,
 cognome character varying(50) not null,
 nome character varying(100) not null,
 indirizzo character varying(100) not null,
 cap character varying(5) not null,
 comune character varying(50) not null,
 prov character varying(2) not null,
 
 denominazione_studio character varying(100) null,
 indirizzo_studio character varying(100) null,
 cap_studio character varying(5)  null,
 comune_studio character varying(50)  null,
 prov_studio character varying(2)  null,
 
 nr_modulo character varying(10) NOT NULL,
 dt_modulo character varying(10) NOT NULL,
 stato character varying(3) NOT NULL,
lista_allegati text,
 
 ts_ins timestamp default current_timestamp not null,
 id_utente_ins integer not null,
 ts_mod timestamp,
 id_utente_mod integer
 
 
)
;

-- add keys for table tecnici

alter table tecnici add constraint pk_tecnici primary key (id_modulo)
;

alter table tecnici add constraint k1_tecnici unique (id_modulo)
;
