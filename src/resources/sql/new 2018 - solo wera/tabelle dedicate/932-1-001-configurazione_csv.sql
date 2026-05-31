/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists configurazione_csv cascade;

-- create tables section -------------------------------------------------


-- table configurazione_csv

 create table configurazione_csv(
 id_modulo serial not null,
 id_azienda bigint not null,
 riga_1 text,
 riga_2 text,
 riga_3 text,
 
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

-- add keys for table configurazione_csv

alter table configurazione_csv add constraint pk_configurazione_csv primary key (id_modulo)
;

alter table configurazione_csv add constraint k1_configurazione_csv unique (id_modulo)
;

