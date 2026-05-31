-- Table: email_log
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE if exists email_log;

CREATE TABLE email_log
(
  id_email_log uuid NOT NULL DEFAULT uuid_generate_v1mc(),
  sender character varying NOT NULL,
  recipients_to character varying NOT NULL,
  recipients_cc character varying NOT NULL,
  recipients_bcc character varying,
  subject character varying,
  body character varying,
  attachment_list character varying,
  attempts smallint NOT NULL,
  ts_send timestamp without time zone,
  email_data text NOT NULL,
  fl_sent boolean NOT NULL DEFAULT false,
  username character varying,
  ts_ins timestamp without time zone DEFAULT now(),
  fl_stop_send boolean NOT NULL DEFAULT false,
  CONSTRAINT key_email_log PRIMARY KEY (id_email_log)
);

CREATE INDEX idx_email_log_id_email_log
  ON email_log
  USING btree
  (id_email_log);

