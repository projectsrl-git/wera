/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists ripartizioni cascade;

-- create tables section -------------------------------------------------


-- table ripartizioni

 create table ripartizioni(
 id_modulo serial not null,
 id_azienda integer not null,
 nr_modulo character varying(10) NOT NULL,
 dt_modulo character varying(10) NOT NULL,
 stato character varying(3) NOT NULL,
 lista_allegati text, 

 id_condominio bigint not null,
 
 data_dal character varying(10) not null,
 data_al character varying(10) not null,
 confermata boolean not null default false,
 inviata boolean not null default false,
 tipo_ripartizione character varying(1) not null,
 
 cc_ac_qta			numeric(10,2) not null,
 cc_fm_qta			numeric(10,2) not null,
 cc_si_qta 		numeric(10,2) not null,
 cc_ac_lordo		numeric(10,2) ,
 cc_fm_lordo		numeric(10,2) ,
 cc_si_lordo 		numeric(10,2) ,
 cc_ac_totale		numeric(10,2) not null,
 cc_fm_totale		numeric(10,2) not null,
 cc_si_totale 		numeric(10,2) not null,
 cc_tc_totale 		numeric(10,2) not null,
 
 cmi_cis_qta 		numeric(10,0) not null,
 cmi_cct_qta 		numeric(10,0) not null,
 cmi_cl_qta		numeric(10,0) not null,
 cmi_cis_um 		character varying(10) not null,
 cmi_cct_um 		character varying(10) not null,
 cmi_cl_um			character varying(10) not null,
 cmi_cis_lordo 	numeric(10,2) ,
 cmi_cct_lordo 	numeric(10,2) ,
 cmi_cl_lordo		numeric(10,2) ,
 cmi_cis_totale 	numeric(10,2) not null,
 cmi_cct_totale 	numeric(10,2) not null,
 cmi_cl_totale		numeric(10,2) not null,
 cmi_ct_totale		numeric(10,2) not null,
 
 tc_cc_rip			numeric(3,0) not null,
 tc_cct_rip		numeric(3,0) not null,
 tc_cfm_rip		numeric(3,0) not null,
 tc_cl_rip			numeric(3,0) not null,
 tc_cc_note		character varying(200) not null,
 tc_cct_note		character varying(200) not null,
 tc_cfm_note		character varying(200) not null,
 tc_cl_note		character varying(200) not null,
 tc_tc_note		character varying(200) not null,
 tc_cc_totale		numeric(10,2) not null,
 tc_cct_totale		numeric(10,2) not null,
 tc_cfm_totale		numeric(10,2) not null,
 tc_cl_totale		numeric(10,2) not null,
 tc_tc_totale		numeric(10,2) not null,
 
 cr_pf_rip			numeric(3,0) not null,
 cr_pr_rip			numeric(3,0) not null,
 cr_tc_rip			numeric(3,0) not null,
 cr_pf_note		character varying(200) not null,
 cr_pr_note		character varying(200) not null,
 cr_tc_note		character varying(200) not null,
 cr_pf_totale		numeric(10,2) not null,
 cr_pr_totale		numeric(10,2) not null,
 cr_tc_totale		numeric(10,2) not null,
 
 pu_cpf			numeric(10,0) not null,
 pu_cr				numeric(10,0) not null,
 pu_cpf_totale		numeric(10,2) not null,
 pu_cr_totale		numeric(10,2) not null,
 
 tc_ac_rip		numeric(10,2) not null,
 tc_ac_note		character varying(200) not null,
 tc_ac_totale		numeric(10,2) not null,
 
 
 cc_ac_mill		numeric(10,2) not null,
 cc_ac_cons		numeric(10,2) not null,
 cmi_cct_acconto		numeric(10,2) not null,
 cmi_cct_saldo		numeric(10,2) not null,
	
 ts_ins timestamp default current_timestamp not null,
 id_utente_ins integer not null,
 ts_mod timestamp,
 id_utente_mod integer
 
 
)
;

-- add keys for table ripartizioni

alter table ripartizioni add constraint pk_ripartizioni primary key (id_modulo)
;

alter table ripartizioni add constraint k1_ripartizioni unique (id_modulo)
;