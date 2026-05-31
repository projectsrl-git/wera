/*
CREATED: 12/11/2017
MODIFIED: 11/01/2018
MODEL: ALI-QHSE
DATABASE: POSTGRESQL 9.4
*/




-- DROP TABLES SECTION ---------------------------------------------------

DROP TABLE IF EXISTS RIPARTIZIONI_LETTURE CASCADE;

-- CREATE TABLES SECTION -------------------------------------------------


-- TABLE RIPARTIZIONI_LETTURE

 create table ripartizioni_letture(
 id_modulo serial not null,
 id_azienda integer not null,
 nr_modulo character varying(10) not null,
 dt_modulo character varying(10) not null,
 stato character varying(3) not null,
 lista_allegati text,
 
 id_condominio integer not null,
 
 data_dal character varying(10) not null,
 data_al character varying(10) not null,
 confermata boolean not null default false,
 inviata boolean not null default false,
 controllo_ok boolean not null default false,
 tipo_ripartizione character varying(1) not null,
 
 
	cc_ac_qta		     	numeric(10,2) null,
	cc_fm_qta		     	numeric(10,2) null,
	cc_si_qta 		    numeric(10,2) null,
	cc_ac_lordo		   numeric(10,2) null,
	cc_fm_lordo		   numeric(10,2) null,
	cc_si_lordo 	   	numeric(10,2) null,
	cc_ac_totale	   	numeric(10,2) null,
	cc_fm_totale	   	numeric(10,2) null,
	cc_si_totale 	  	numeric(10,2) null,
	cc_tc_totale 	  	numeric(10,2) null,
	                
	cmi_cis_qta 	   	numeric(10,0) null,
	cmi_cct_qta 	   	numeric(10,0) null,
	cmi_cl_qta		    numeric(10,0) null,
	cmi_si_qta		    numeric(10,0) null,
	cmi_cis_lordo 	 numeric(10,2) null,
	cmi_cct_lordo 	 numeric(10,2) null,
	cmi_cl_lordo	   	numeric(10,2) null,
	cmi_si_lordo	   	numeric(10,2) null,
	cmi_cis_totale 	numeric(10,2) null,
	cmi_cct_totale 	numeric(10,2) null,
	cmi_cl_totale	  	numeric(10,2) null,
	cmi_si_totale	  	numeric(10,2) null,
	                
	tc_cc_rip		     	numeric(3,0) null,
	tc_cct_rip		    numeric(3,0) null,
	tc_cfm_rip		    numeric(3,0) null,
	tc_cl_rip		     	numeric(3,0) null,
	tc_af_rip		     	numeric(3,0) null,
	tc_cc_totale	   	numeric(10,2) null,
	tc_cct_totale	  	numeric(10,2) null,
	tc_cfm_totale	  	numeric(10,2) null,
	tc_cl_totale	   	numeric(10,2) null,
	tc_tc_totale	   	numeric(10,2) null,
	tc_af_totale	   	numeric(10,2) null,
	                
	                
	cr_pf_totale	   	numeric(10,2) null,
	cr_pr_totale	   	numeric(10,2) null,
	cr_tc_totale	   	numeric(10,2) null,
	                
	tc_ac_rip		     	numeric(3,0) null,
	tc_ac_totale	   	numeric(10,2) null,
	                
	tc_cis_totale	  	numeric(10,2) null,
	
	
	
	spesa_totale_impianto		numeric(10,2) null,
	spesa_totale_gestione		numeric(10,2) null,
	perdite_impianto			numeric(10,2) null,
	fabbisogno_annuo_clima	numeric(10,2) null,
	fabbisogno_annuo_acs		numeric(10,2) null,
	
	v1_combustibile_lettura_iniziale		numeric(10,2) null,
	v1_combustibile_lettura_finale		numeric(10,2) null,
	v1_combustibile_consumo				numeric(10,2) null,
	v1_combustibile_fabbisogno_clima		numeric(10,2) null,
	v1_combustibile_fabbisogno_acs		numeric(10,2) null,
	v1_combustibile_costo_unitario		numeric(10,2) null,
	v2_energia_elettrica_lettura_iniziale		numeric(10,2) null,
	v2_energia_elettrica_lettura_finale		numeric(10,2) null,
	v2_energia_elettrica_consumo				numeric(10,2) null,
	v2_energia_elettrica_fabbisogno_clima		numeric(10,2) null,
	v2_energia_elettrica_fabbisogno_acs		numeric(10,2) null,
	v2_energia_elettrica_costo_unitario		numeric(10,2) null,
	
	generatore_lettura_iniziale		numeric(10,2) null,
	generatore_lettura_finale			numeric(10,2) null,
	generatore_consumo				numeric(10,2) null,
	generatore_consumo_acs				numeric(10,2) null,
	generatore_fabbisogno_clima		numeric(10,2) null,
	generatore_fabbisogno_acs			numeric(10,2) null,
	
	v1_coefficiente_ripartizione_riscaldamento		numeric(10,2) null,
	v1_coefficiente_ripartizione_acs					numeric(10,2) null,
	v1_consumo_riscaldamento							numeric(10,2) null,
	v1_consumo_acs									numeric(10,2) null,
	v2_coefficiente_ripartizione_riscaldamento		numeric(10,2) null,
	v2_coefficiente_ripartizione_acs					numeric(10,2) null,
	v2_consumo_riscaldamento							numeric(10,2) null,
	v2_consumo_acs									numeric(10,2) null,
	energia_termica_coefficiente_ripartizione_riscaldamento		numeric(10,2) null,
	energia_termica_coefficiente_ripartizione_acs		numeric(10,2) null,
	energia_termica_consumo_riscaldamento				numeric(10,0) null,
	energia_termica_consumo_acs						numeric(10,0) null,
	consumo_totale_riscaldamento						numeric(10,0) null,
	consumo_totale_acs								numeric(10,0) null,
	consumo_totale_riscaldamento_e_acs				numeric(10,0) null,
	
	spesa_riscaldamento								numeric(10,2) null,
	spesa_acs											numeric(10,2) null,
	coefficiente_ripartizione_riscaldamento			numeric(10,2) null,
	coefficiente_ripartizione_acs						numeric(10,2) null,
	spesa_conduzione_riscaldamento					numeric(10,2) null,
	spesa_conduzione_acs								numeric(10,2) null,
	spesa_gestione_riscaldamento						numeric(10,2) null,
	spesa_gestione_acs								numeric(10,2) null,
	spesa_totale_riscaldamento						numeric(10,2) null,
	spesa_totale_acs									numeric(10,2) null,
	spesa_totale_riscaldamento_acs					numeric(10,2) null,
	costo_unitario_energia_termica_riscaldamento		numeric(10,3) null,
	costo_unitario_energia_termica_acs				numeric(10,3) null,
	consumo_involontario								numeric(10,2) null,
	
	totale_energia_termica_riscaldamento				numeric(10,2) null,
	totale_energia_termica_acs						numeric(10,2) null,
	totale_potenza_termica_riscaldamento              numeric(10,2) null,
	totale_potenza_termica_acs                        numeric(10,2) null,
	totale_riscaldamento                              numeric(10,2) null,
	totale_acs                                        numeric(10,2) null,
	totale_appartamento                               numeric(10,2) null,
	lettura		numeric(10,0) null,
	lettura_acs		numeric(10,2) null,
	totale_millesimi_riscaldamento				numeric(10,2) null,
	totale_millesimi_acs						numeric(10,2) null,
	rendimento_caldaia						numeric(10,2) null,
	tipologia_ripartizione		character varying(1) ,
	tipo_contabilizzazione		character varying(1) ,
	
 ts_ins timestamp default current_timestamp not null,
 id_utente_ins integer not null,
 ts_mod timestamp,
 id_utente_mod integer
 
 
)
;

-- add keys for table ripartizioni_letture

alter table ripartizioni_letture add constraint pk_ripartizioni_letture primary key (id_modulo)
;

alter table ripartizioni_letture add constraint k1_ripartizioni_letture unique (id_modulo)
;





