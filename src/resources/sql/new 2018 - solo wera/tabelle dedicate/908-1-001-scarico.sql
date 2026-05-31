/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists scarico cascade;

-- create tables section -------------------------------------------------


-- table scarico

 create table scarico(
 id_modulo serial not null,
 id_azienda bigint not null,
 id_condominio bigint not null,
 
  	nome_file							character varying(50) not null,
	data_import						character varying(10) not null,
	ora_import						character varying(5) not null,
	n_cliente_network					character varying(50) ,
	n_fabbrica_network 				character varying(50) ,
	versione_software_i 				character varying(50) ,
	data_di_lettura_i 				character varying(50) ,
	giorno_settimana 					character varying(50) ,
	ora_di_lettura_i					character varying(50) ,
	operat_hours 						character varying(50) ,
	anomalia_i 						character varying(50) ,
	data_anomalia_i 					character varying(50) ,
	ora_anomalia_i 					character varying(50) ,
	disp_radio_trovati 				character varying(50) ,
	modello_antenna 					character varying(50) ,
	
	n_progressivo_dispositivo 		character varying(50) ,
	antenna_di_riferimento 			character varying(50) ,
	data_di_lettura 					character varying(50) ,
	ora_di_lettura 					character varying(50) ,
	n_fabbrica_dispositivo 			character varying(50) not null,
	codice_di_produzione 				character varying(50) ,
	versione_software 				character varying(50) ,
	anomalia 							character varying(50) ,
	data_anomalia 					character varying(50) ,
	ora_anomalia 						character varying(50) ,
	lettura_attuale 					character varying(50) ,
	unita_di_misura 					character varying(50) ,
	volume_attuale 					character varying(50) ,
	unita_di_misura1 					character varying(50) ,
	fattore_energia 					character varying(50) ,
	lettura_a_data_di_scarico 		character varying(50) ,
	unita_di_misura2 					character varying(50) ,
	data_di_scarico 					character varying(10) ,
	data_inizio_statistica 			character varying(10) ,
	stat_value1 						character varying(20) ,
	stat_value2 						character varying(20) ,
	stat_value3 						character varying(20) ,
	stat_value4 						character varying(20) ,
	stat_value5 						character varying(20) ,
	stat_value6 						character varying(20) ,
	stat_value7 						character varying(20) ,
	stat_value8 						character varying(20) ,
	stat_value9 						character varying(20) ,
	stat_value10 						character varying(20) ,             
	stat_value11 						character varying(20) ,             
	stat_value12 						character varying(20) ,             
	stat_value13 						character varying(20) ,             
	stat_value14 						character varying(20) ,             
	stat_value15 						character varying(20) ,             
	stat_value16 						character varying(20) ,             
	stat_value17 						character varying(20) ,             
	stat_value18 						character varying(20) ,             
	unit_of_stat 						character varying(50) ,             
	currvalue_of_tariff1 				character varying(50) ,     
	unit_of_tariff1 					character varying(50) ,          
	set_day_val_of_tariff1 			character varying(50) ,   
	set_day_unit_of_tariff1 			character varying(50) ,
		
	kdevadr292 						character varying(50) ,
	zdat 								character varying(50) ,
	zuhr 								character varying(50) ,
	cbs104 							character varying(50) ,
	zfdauer251 		     			character varying(50) ,
	volumecumul 		    			character varying(50) ,
	unvolumecumul 	   				character varying(50) ,
	cvolstlval108 	   				character varying(50) ,
	cvolstldim108 	   				character varying(50) ,
	censtvlval111 	   				character varying(50) ,
	censtvldim111 	   				character varying(50) ,
	cvolstvlval112 	  				character varying(50) ,
	cvolstvldim112 	  				character varying(50) ,
	zdatstvl114 		    			character varying(50) ,
	cpqmwmaxval169 	  				character varying(50) ,
	cpqmwmaxdim169 	  				character varying(50) ,
	zdatmwmax170 		   				character varying(50) ,
	valcumultar1 		   				character varying(50) ,
	umvalcumultar1 	  				character varying(50) ,
	cvxtar2val194 	   				character varying(50) ,
	cvxtar2dim194 	   				character varying(50) ,
	cvxtar1stlval193 					character varying(50) ,
	cvxtar1stldim193 					character varying(50) ,
	cvxtar2stlval194 					character varying(50) ,
	cvxtar2stldim194 					character varying(50) ,
	visibile 	boolean not null default false,
	data_vis 	character varying(10) ,
	ora_import_completa 	character varying(8) not null,
	errore_sistemato 	boolean not null default false,
		
	
	 nr_modulo character varying(10) ,
  dt_modulo character varying(10)  ,
  stato character varying(3)  ,
lista_allegati text,

 ts_ins timestamp default current_timestamp not null,
 id_utente_ins integer not null,
 ts_mod timestamp,
 id_utente_mod integer
 
 
)
;

-- add keys for table scarico

alter table scarico add constraint pk_scarico primary key (id_modulo)
;

alter table scarico add constraint k1_scarico unique (id_modulo)
;



