/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists network cascade;

-- create tables section -------------------------------------------------


-- table network

 create table network(
 id_modulo serial not null,
 id_azienda bigint not null,
 id_condominio bigint not null,
 antenna character varying(8) not null,
 gateway boolean not null default false,
 tipologia character varying(100),
 
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

-- add keys for table network

alter table network add constraint pk_network primary key (id_modulo)
;

alter table network add constraint k1_network unique (id_modulo)
;

