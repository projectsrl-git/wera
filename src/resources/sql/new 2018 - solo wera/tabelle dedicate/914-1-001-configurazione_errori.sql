/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists configurazione_errori cascade;

-- create tables section -------------------------------------------------


-- table configurazione_errori

 create table configurazione_errori(
 id_modulo serial not null,
 id_azienda bigint not null,
 id_errore bigint not null,
 colore character varying(20) not null,
 mail boolean not null default false,
  
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

-- add keys for table configurazione_errori

alter table configurazione_errori add constraint pk_configurazione_errori primary key (id_modulo)
;

alter table configurazione_errori add constraint k1_configurazione_errori unique (id_modulo)
;

