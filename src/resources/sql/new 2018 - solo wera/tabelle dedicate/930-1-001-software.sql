/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists software cascade;

-- create tables section -------------------------------------------------


-- table software

 create table software(
 id_modulo serial not null,
 id_azienda bigint not null,
 denominazione character varying(100) not null,
 versione character varying(50)  null,
 produttore character varying(100) not null,
 
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

-- add keys for table software

alter table software add constraint pk_software primary key (id_modulo)
;

alter table software add constraint k1_software unique (id_modulo)
;
