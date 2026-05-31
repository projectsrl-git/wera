/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists utenze cascade;

-- create tables section -------------------------------------------------


-- table utenze

 create table utenze(
 id_modulo serial not null,
 id_azienda bigint not null,
 id_condominio bigint not null,
 denominazione character varying(100) not null,
 locatario character varying(100) not null,
 scala character varying(10),
 piano character varying(3),
 interno character varying(10),
 n_telefono character varying(15),
 millesimi numeric(10,3) not null,
 millesimi_clima numeric(10,3) not null,
 millesimi_acs numeric(10,3) not null,
 
  nr_modulo character varying(10) NOT NULL,
  dt_modulo character varying(10) NOT NULL,
  stato character varying(3) NOT NULL,
  lista_allegati text,

  
  id_modulo_parent integer,
  tabella_parent character varying(30),
 
 ts_ins timestamp default current_timestamp not null,
 id_utente_ins integer not null,
 ts_mod timestamp,
 id_utente_mod integer
 
 
)
;

-- add keys for table utenze

alter table utenze add constraint pk_utenze primary key (id_modulo)
;

alter table utenze add constraint k1_utenze unique (id_modulo)
;
