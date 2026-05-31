/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists elenco_file_importati_rilevatori cascade;

-- create tables section -------------------------------------------------


-- table elenco_file_importati_rilevatori

 create table elenco_file_importati_rilevatori(
 id_modulo serial not null,
 id_azienda bigint not null,
 nome_file text not null,
 data_import character varying(10) not null,
 ora_import character varying(5) not null,
 
  nr_modulo character varying(10) ,
  dt_modulo character varying(10)  ,
  stato character varying(3)  ,
	lista_allegati text,

 ts_ins timestamp default current_timestamp not null,
 id_utente_ins integer not null,
 ts_mod timestamp,
 id_utente_mod integer
 
)
;

-- add keys for table elenco_file_importati_rilevatori

alter table elenco_file_importati_rilevatori add constraint pk_elenco_file_importati_rilevatori primary key (id_modulo)
;

alter table elenco_file_importati_rilevatori add constraint k1_elenco_file_importati_rilevatori unique (id_modulo)
;

