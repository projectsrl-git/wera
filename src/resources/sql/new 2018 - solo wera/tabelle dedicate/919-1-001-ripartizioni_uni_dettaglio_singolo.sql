/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists ripartizioni_uni_dettaglio_singolo cascade;

-- create tables section -------------------------------------------------


-- table ripartizioni_uni_dettaglio_singolo

 create table ripartizioni_uni_dettaglio_singolo(
 id_dettaglio serial not null,
 id_modulo integer not null,
 nr_dettaglio character varying(10) not null,

 	id_condomino integer not null,
 	denominazione 	character varying(200) not null,
	stanza character varying(50) not null,
	rilevatore character varying(10) not null,
	
	lettura 			numeric(10,2) null,
	lettura_acs 			numeric(10,2) null,
	
	consumi_energia_termica_riscaldamento			numeric(10,2) null,
	consumi_energia_termica_acs			numeric(10,2) null,
	consumo_involontario_energia_termica_acs		numeric(10,2) null,
	
	spesa_energia_termica_riscaldamento 		numeric(10,2) null,
	spesa_energia_termica_acs 		numeric(10,2) null,
	spesa_potenza_termica_riscaldamento 		numeric(10,2) null,
	spesa_potenza_termica_acs		numeric(10,2) null,
	spesa_totale_riscaldamento		numeric(10,2) null,
	spesa_totale_acs 	numeric(10,2) null,
	spesa_totale_appartamento 	numeric(10,2) null,
	
	numero_ripartitori character varying(5) not null,
	
	
	tabella_parent character varying(30),
 id_dettaglio_parent integer ,
	
 ts_ins timestamp default current_timestamp not null,
 id_utente_ins integer not null,
 ts_mod timestamp,
 id_utente_mod integer
 
 
)
;

-- add keys for table ripartizioni_uni_dettaglio_singolo

alter table ripartizioni_uni_dettaglio_singolo add constraint pk_ripartizioni_uni_dettaglio_singolo primary key (id_dettaglio)
;

alter table ripartizioni_uni_dettaglio_singolo add constraint k1_ripartizioni_uni_dettaglio_singolo unique (id_dettaglio)
;



