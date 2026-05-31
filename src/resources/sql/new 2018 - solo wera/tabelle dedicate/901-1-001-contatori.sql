/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists contatori cascade;

-- create tables section -------------------------------------------------


-- table contatori

 create table contatori(
 id_modulo serial not null,
 id_azienda bigint not null,
 id_condominio bigint not null,
 contatore character varying(8) not null,
 id_tipologia character varying(3) not null,
 
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

-- add keys for table contatori

alter table contatori add constraint pk_contatori primary key (id_modulo)
;

alter table contatori add constraint k1_contatori unique (id_modulo)
;