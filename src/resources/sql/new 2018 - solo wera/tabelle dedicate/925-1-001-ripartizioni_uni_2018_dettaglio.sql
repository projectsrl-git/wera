/*
created: 12/11/2017
modified: 11/01/2018
model: ali-qhse
database: postgresql 9.4
*/




-- drop tables section ---------------------------------------------------

drop table if exists RIPARTIZIONI_UNI_2018_DETTAGLIO cascade;

-- create tables section -------------------------------------------------


-- table ripartizioni_uni_2018_dettaglio

 create table RIPARTIZIONI_UNI_2018_DETTAGLIO(
id_dettaglio serial not null,
id_modulo integer not null,
nr_dettaglio character varying(10) not null,
id_azienda integer not null,
id_condomino integer not null,
denominazione 	character varying(200) not null,

tabella_parent character varying(30),
id_dettaglio_parent integer ,
millesimi        							numeric(10,2) null,
millesimi_clima        							numeric(10,2) null,
millesimi_acs        							numeric(10,2) null,
lettura        							numeric(10,2) null,
lettura_acs        							numeric(10,2) null,

 	q_h        							numeric(10,2) null,
    q_w       						 	numeric(10,2) null,
    ur        							numeric(10,2) null,
    consumo_riscaldamento_q_vol        	numeric(10,2) null,
    consumo_riscaldamento_q_inv        	numeric(10,2) null,
    consumo_riscaldamento_q_obb        	numeric(10,2) null,
    consumo_riscaldamento_q_tot        	numeric(10,2) null,
    consumo_acs_q_vol        			numeric(10,2) null,
    consumo_acs_q_inv        			numeric(10,2) null,
    consumo_acs_q_obb        			numeric(10,2) null,
    consumo_acs_q_tot        			numeric(10,2) null,
                                                                   
    spese_riscaldamento_s_vol      		numeric(10,2) null,
    spese_riscaldamento_s_inv      		numeric(10,2) null,
    spese_riscaldamento_s_obb      		numeric(10,2) null,
    spese_riscaldamento_s_e        		numeric(10,2) null,
    spese_riscaldamento_s_g        		numeric(10,2) null,
    spese_riscaldamento_s_c        		numeric(10,2) null,
    spese_riscaldamento_s_p        		numeric(10,2) null,
    spese_riscaldamento_s_uc       		numeric(10,2) null,
    spese_riscaldamento_s_tot      		numeric(10,2) null,
    spese_acs_s_vol        				numeric(10,2) null,
    spese_acs_s_inv        				numeric(10,2) null,
    spese_acs_s_obb        				numeric(10,2) null,
    spese_acs_s_e        				numeric(10,2) null,
    spese_acs_s_g        				numeric(10,2) null,
    spese_acs_s_c        				numeric(10,2) null,
    spese_acs_s_p        				numeric(10,2) null,
    spese_acs_s_uc       				numeric(10,2) null,
    spese_acs_s_tot        				numeric(10,2) null,
    spese_totali_s_h_tot    			numeric(10,2) null,
    spese_totali_s_w_tot    			numeric(10,2) null,
    spese_totali_s_gl_tot   			numeric(10,2) null,
    numero_ripartitori integer not null,
    tipo_utenza character varying (3),
    

ts_ins timestamp default current_timestamp not null,
id_utente_ins integer not null,
ts_mod timestamp,
id_utente_mod integer
 
 
)
;

-- add keys for table ripartizioni_uni_2018_dettaglio

alter table ripartizioni_uni_2018_dettaglio add constraint pk_ripartizioni_uni_2018_dettaglio primary key (id_dettaglio)
;

alter table ripartizioni_uni_2018_dettaglio add constraint k1_ripartizioni_uni_2018_dettaglio unique (id_dettaglio)
;



