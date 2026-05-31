/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists rilevatori_eliminati cascade;

-- create tables section -------------------------------------------------


-- table rilevatori_eliminati

 create table rilevatori_eliminati(
 id_elimina serial not null,
 id_azienda bigint not null,
 id_condominio bigint not null,
 
 rilevatore character varying(10),
 ripristinato boolean not null default false,
 tipologia character varying(100) not null,
 
 ts_ins timestamp default current_timestamp not null,
 id_utente_ins integer not null,
 ts_mod timestamp,
 id_utente_mod integer
 
 
)
;

-- add keys for table rilevatori_eliminati

alter table rilevatori_eliminati add constraint pk_rilevatori_eliminati primary key (id_elimina)
;

alter table rilevatori_eliminati add constraint k1_rilevatori_eliminati unique (id_elimina)
;