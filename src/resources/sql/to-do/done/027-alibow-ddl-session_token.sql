-- Table: session_token

DROP TABLE if exists session_token;

CREATE TABLE session_token
(
  id_token uuid NOT NULL DEFAULT uuid_generate_v1mc(),
  ipaddress character varying NOT NULL,
  ts_ins timestamp without time zone NOT NULL DEFAULT now(),
  datamap bytea NOT NULL,
  CONSTRAINT key2 PRIMARY KEY (id_token)
);

