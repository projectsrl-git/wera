/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists condomini cascade;

-- create tables section -------------------------------------------------


-- table condomini

 create table condomini(
 id_modulo serial not null,
 id_azienda bigint not null,
 denominazione character varying(200) not null,
 indirizzo character varying(200) not null,
 localita character varying(200) not null,
 provincia char(2),
 cap char(5),
 sim char(10),
 
 scarico int,
 id_amministratore bigint,
 codfisc char(16),
 data_primo_scarico char(10),
 gen boolean not null default false,
 feb boolean not null default false,
 mar boolean not null default false,
 apr boolean not null default false,
 mag boolean not null default false,
 giu boolean not null default false,
 lug boolean not null default false,
 ago boolean not null default false,
 sett boolean not null default false,
 ott boolean not null default false,
 nov boolean not null default false,
 dic boolean not null default false,
 
 
 
 perdite_impianto numeric(10,2) ,
 fabbisogno_annuo_clima numeric(10,2) ,
 fabbisogno_annuo_acs numeric(10,2) ,
 rendimento_caldaia numeric(10,2) ,
 tipo char(1),
 
 
 
 
    nr_modulo character varying(10) NOT NULL,
  dt_modulo character varying(10) NOT NULL,
  stato character varying(3) NOT NULL,
lista_allegati text,
 
 ts_ins timestamp default current_timestamp not null,
 id_utente_ins integer not null,
 ts_mod timestamp,
 id_utente_mod integer,
 gg_segnalazione int,
 data_prima_segnalazione char(10),
 
 tipo_contabilizzazione		character varying(1)
 
 
)
;

-- add keys for table condomini

alter table condomini add constraint pk_condomini primary key (id_modulo)
;

alter table condomini add constraint k1_condomini unique (id_modulo)
;










