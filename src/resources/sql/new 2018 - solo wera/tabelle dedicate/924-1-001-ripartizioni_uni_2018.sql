/*
CREATED: 12/11/2017
MODIFIED: 11/01/2018
MODEL: ALI-QHSE
DATABASE: POSTGRESQL 9.4
*/




-- DROP TABLES SECTION ---------------------------------------------------

DROP TABLE IF EXISTS RIPARTIZIONI_UNI_2018 CASCADE;

-- CREATE TABLES SECTION -------------------------------------------------


-- TABLE RIPARTIZIONI_UNI_2018

create table RIPARTIZIONI_UNI_2018(
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
totale_millesimi_riscaldamento				numeric(10,2) null,
totale_millesimi_acs						numeric(10,2) null,
rendimento_caldaia						numeric(10,2) null,
tipologia_ripartizione		character varying(1) not null,
tipo_contabilizzazione		character varying(1) not null,
tipo_ripartizione_pc		character varying(3) not null,

fabbisogno_annuo_clima numeric(10,2) null,
fabbisogno_annuo_acs numeric(10,2) null,
perdite_impianto numeric(10,2) null,
 
sg_scm_totale_prev numeric(10,2) null,
sg_scr_qta_prev integer null,
sg_scr_importo_prev numeric(10,2) null,
sg_scr_totale_prev numeric(10,2) null,
sg_sg_totale_prev numeric(10,2) null,
sg_scm_totale_cons numeric(10,2) null,
sg_scr_qta_cons numeric(10,2) null,
sg_scr_importo_cons numeric(10,2) null,
sg_scr_totale_cons numeric(10,2) null,
sg_sg_totale_cons numeric(10,2) null,

vet_comb_um character varying(3) not null,
vet_comb_q_ve_h_prev numeric(10,2) null,
vet_comb_q_ve_w_prev numeric(10,2) null,
vet_comb_c_ve_prev numeric(10,2) null,
vet_comb_q_ve_h_cons numeric(10,2) null,
vet_comb_q_ve_w_cons numeric(10,2) null,
vet_comb_c_ve_cons numeric(10,2) null,
vet_elettr_um character varying(3) not null,
vet_elettr_q_ve_h_prev numeric(10,2) null,
vet_elettr_q_ve_w_prev numeric(10,2) null,
vet_elettr_c_ve_prev numeric(10,2) null,
vet_elettr_q_ve_h_cons numeric(10,2) null,
vet_elettr_q_ve_w_cons numeric(10,2) null,
vet_elettr_c_ve_cons numeric(10,2) null,

st_risc_se_prev numeric(10,2) null,
st_risc_sg_prev numeric(10,2) null,
st_risc_st_prev numeric(10,2) null,
st_risc_se_cons numeric(10,2) null,
st_risc_sg_cons numeric(10,2) null,
st_risc_st_cons numeric(10,2) null,
st_acs_se_prev numeric(10,2) null,
st_acs_sg_prev numeric(10,2) null,
st_acs_st_prev numeric(10,2) null,
st_acs_se_cons numeric(10,2) null,
st_acs_sg_cons numeric(10,2) null,
st_acs_st_cons numeric(10,2) null,

st_se_totale_prev numeric(10,2) null,
st_sg_totale_prev numeric(10,2) null,
st_st_totale_prev numeric(10,2) null,
st_se_totale_cons numeric(10,2) null,
st_sg_totale_cons numeric(10,2) null,
st_st_totale_cons numeric(10,2) null,
	
gen_tipo character varying(50) not null,
gen_qgn_h_prev numeric(10,2) null,
gen_qgn_w_prev numeric(10,2) null,
gen_qgn_h_cons numeric(10,2) null,
gen_qgn_w_cons numeric(10,2) null,

consumi_risc_q_vol numeric(10,2) null,
consumi_risc_q_inv numeric(10,2) null,
consumi_risc_q_obb numeric(10,2) null,
consumi_risc_q_tot numeric(10,2) null,
consumi_risc_f_inv numeric(10,2) null,
consumi_acs_q_vol numeric(10,2) null,
consumi_acs_q_inv numeric(10,2) null,
consumi_acs_q_obb numeric(10,2) null,
consumi_acs_q_tot numeric(10,2) null,
consumi_acs_f_inv numeric(10,2) null,

costi_etu_clima_invern_ch numeric(10,4) null,
costi_etu_acs_cw numeric(10,4) null,

spese_risc_s_vol numeric(10,2) null,
spese_risc_s_inv numeric(10,2) null,
spese_risc_s_obb numeric(10,2) null,
spese_risc_s_e numeric(10,2) null,
spese_risc_s_g numeric(10,2) null,
spese_risc_s_c numeric(10,2) null,
spese_risc_s_p numeric(10,2) null,
spese_risc_s_uc numeric(10,2) null,
spese_risc_s_tot numeric(10,2) null,
spese_acs_s_vol numeric(10,2) null,
spese_acs_s_inv numeric(10,2) null,
spese_acs_s_obb numeric(10,2) null,
spese_acs_s_e numeric(10,2) null,
spese_acs_s_g numeric(10,2) null,
spese_acs_s_c numeric(10,2) null,
spese_acs_s_p numeric(10,2) null,
spese_acs_s_uc numeric(10,2) null,
spese_acs_s_tot numeric(10,2) null,
spese_totali_s_gl_tot numeric(10,2) null,


committente_cognome character varying(50) null,
committente_nome character varying(50) null,
id_tecnico integer null,
id_amministratore integer null,
responsabile_impianto character varying(150) null,
id_software_calcolo character varying(50) null,

mill character varying(50) null,	
servizio_riscaldamento_utenza character varying(50) null,	
servizio_acs_utenza character varying(50) null,	
servizio_riscaldamento_centrale character varying(50) null,	
servizio_acs_centrale character varying(50) null,	
servizio_riscaldamento_edifici character varying(50) null,	
servizio_acs_edifici character varying(50) null,	

ts_ins timestamp default current_timestamp not null,
id_utente_ins integer not null,
ts_mod timestamp,
id_utente_mod integer


)
;

-- add keys for table RIPARTIZIONI_UNI_2018

alter table RIPARTIZIONI_UNI_2018 add constraint pk_RIPARTIZIONI_UNI_2018 primary key (id_modulo)
;

alter table RIPARTIZIONI_UNI_2018 add constraint k1_RIPARTIZIONI_UNI_2018 unique (id_modulo)
;





