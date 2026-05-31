/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists ripartizioni_dettaglio cascade;

-- create tables section -------------------------------------------------


-- table ripartizioni_dettaglio

 create table ripartizioni_dettaglio(
 id_dettaglio serial not null,
 id_modulo integer not null,
 nr_dettaglio character varying(10) not null,

 	id_condomino integer not null,
 	denominazione 	character varying(200) not null,
	millesimi 			numeric(10,2) null,
	numero_ripartitori numeric(3) not null,
	lettura 			numeric(10,0) null,
	
	metano_fisso			numeric(10,2) null,
	metano_millesimi 		numeric(10,2) null,
	forza_motrice 			numeric(10,2) null,
	conduzione				numeric(10,2) null,
	
	conduzione_anticipo				numeric(10,2) null,
	conduzione_saldo				numeric(10,2) null,
	
	manutenzione_ordinaria	numeric(10,2) null,
	manutenzione_straordinaria	numeric(10,2) null,
	costi_letture	numeric(10,2) null,
	totale_senza_letture	numeric(10,2) null,
	totale_con_letture		numeric(10,2) null,
	
	metano		numeric(10,2) null,
	subtotale_costi_millesimi		numeric(10,2) null,
	
	tabella_parent character varying(30),
 id_dettaglio_parent integer ,
	
 ts_ins timestamp default current_timestamp not null,
 id_utente_ins integer not null,
 ts_mod timestamp,
 id_utente_mod integer
 
 
)
;

-- add keys for table ripartizioni_dettaglio

alter table ripartizioni_dettaglio add constraint pk_ripartizioni_dettaglio primary key (id_dettaglio)
;

alter table ripartizioni_dettaglio add constraint k1_ripartizioni_dettaglio unique (id_dettaglio)
;



