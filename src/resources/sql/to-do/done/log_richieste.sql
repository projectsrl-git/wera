
drop table if exists log_richieste;
CREATE table log_richieste (
	id_log_richieste serial,
	tipo_richiesta character varying(30),
	id_richiesta integer,
	id_tabella_parent character varying(30),
	stato character(3),
	fl_ultimo_stato boolean,
	ts_ins timestamp without time zone NOT NULL DEFAULT now(),
	id_utente_ins integer NOT NULL,
	ts_del timestamp without time zone,
	id_utente_del integer
 ) 

drop table if exists log_richieste_rollback;
CREATE table log_richieste_rollback (
	id_log_richieste serial,
	tipo_richiesta character varying(30),
	id_richiesta integer,
	id_tabella_parent character varying(30),
	stato character(3),
	ts_ins timestamp without time zone NOT NULL DEFAULT now(),
	id_utente_ins integer NOT NULL,
	ts_del timestamp without time zone,
	id_utente_del integer
 ) 