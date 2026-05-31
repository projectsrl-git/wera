/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists dati_rilevatori cascade;

-- create tables section -------------------------------------------------


-- table dati_rilevatori

 create table dati_rilevatori(
 id_modulo serial not null,
 id_azienda bigint not null,
id_condominio bigint not null,
 nome_file_dr character varying(200) not null,
 rilevatore character varying(10) not null,
 lettura numeric(10,3) ,
 lettura_acs numeric(10,3) ,
 mese character varying(2) not null,
 anno character varying(4) not null,
 data_import character varying(10) not null,
 ora_import character varying(10) not null,
 data_di_lettura character varying(10) ,
 ora_di_lettura character varying(10) ,
 anomalia character varying(10) ,
 data_anomalia character varying(10) ,
 ora_anomalia character varying(10) ,
 lettura_attuale numeric(10,3) ,
 unita_di_misura character varying(10) ,
 data_lettura_attuale character varying(10) ,
 unit_of_stat character varying(10) ,
 volume_attuale numeric(10,3) ,
 lettura_prec numeric(10,3) ,
 lettura_acs_prec numeric(10,3) ,
 
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

-- add keys for table dati_rilevatori

alter table dati_rilevatori add constraint pk_dati_rilevatori primary key (id_modulo)
;

alter table dati_rilevatori add constraint k1_dati_rilevatori unique (id_modulo)
;

