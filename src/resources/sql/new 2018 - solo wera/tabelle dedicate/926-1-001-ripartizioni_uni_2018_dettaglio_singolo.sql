-- Table: ripartizioni_uni_2018_dettaglio_singolo

DROP TABLE ripartizioni_uni_2018_dettaglio_singolo;

CREATE TABLE ripartizioni_uni_2018_dettaglio_singolo
(
  id_dettaglio serial NOT NULL,
  id_modulo integer NOT NULL,
  nr_dettaglio character varying(10) NOT NULL,
  id_condomino integer NOT NULL,

   denominazione character varying(200) not null,
    contatore_lettura_iniziale numeric(10,2) null,
    contatore_lettura_finale numeric(10,2) null,
    contatore_lettura numeric(10,2) null,
    
    stanza character varying(50) not null,
    rilevatore character varying(10) not null,
    progr character varying(1) null,
    k_c numeric(10,2) null,
    k_q numeric(10,2) null,
    k_t numeric(10,2) null,
    k numeric(10,2) null,
    
    ripartitore_lettura_iniziale integer null,
    ripartitore_lettura_finale integer null,
    ripartitore_lettura integer null,
    numero_ripartitori        integer null,
  
  tabella_parent character varying(30),
  id_dettaglio_parent integer,
  ts_ins timestamp without time zone NOT NULL DEFAULT now(),
  id_utente_ins integer NOT NULL,
  ts_mod timestamp without time zone,
  id_utente_mod integer,
  CONSTRAINT pk_ripartizioni_uni_2018_dettaglio_singolo PRIMARY KEY (id_dettaglio),
  CONSTRAINT k1_ripartizioni_uni_2018_dettaglio_singolo UNIQUE (id_dettaglio)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE ripartizioni_uni_2018_dettaglio_singolo
  OWNER TO postgres;
