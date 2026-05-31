/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists ripartizioni_dettaglio_singolo cascade;

-- create tables section -------------------------------------------------


-- table ripartizioni_dettaglio_singolo

 create table ripartizioni_dettaglio_singolo(
 id_dettaglio serial not null,
 id_modulo integer not null,
 nr_dettaglio character varying(10) not null,

 	id_condomino integer not null,
 	denominazione 	character varying(200) not null,
	lettura 			numeric(10,0) null,
	metano		numeric(10,2) null,
	stanza character varying(50) not null,
	rilevatore character varying(10) not null,
	
	tabella_parent character varying(30),
 id_dettaglio_parent integer ,
	
 ts_ins timestamp default current_timestamp not null,
 id_utente_ins integer not null,
 ts_mod timestamp,
 id_utente_mod integer
 
 
)
;

-- add keys for table ripartizioni_dettaglio_singolo

alter table ripartizioni_dettaglio_singolo add constraint pk_ripartizioni_dettaglio_singolo primary key (id_dettaglio)
;

alter table ripartizioni_dettaglio_singolo add constraint k1_ripartizioni_dettaglio_singolo unique (id_dettaglio)
;



