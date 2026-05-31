-- Constraint: pk_pdl

ALTER TABLE pdl DROP CONSTRAINT pk_pdl
;

ALTER TABLE PDL alter column ID_EQUIPMENT DROP NOT NULL
;

ALTER TABLE pdl ADD CONSTRAINT pk_pdl PRIMARY KEY(id_pdl, id_area, id_impianto, id_azienda)
;

