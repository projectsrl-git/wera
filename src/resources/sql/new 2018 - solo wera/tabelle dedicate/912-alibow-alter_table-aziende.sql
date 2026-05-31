alter table aziende add column codfiscale_az character varying(16);
alter table aziende add column partitaiva character varying(13);
alter table aziende add column telefono character varying(50);
alter table aziende add column mail character varying(100);
alter table aziende add column crediti integer not null default 0;

alter table aziende add column nome character varying(100);
alter table aziende add column cognome character varying(100);
alter table aziende add column sesso character varying(1);
alter table aziende add column d_nascita character varying(10);
alter table aziende add column locnascita character varying(100);
alter table aziende add column provnascita character varying(2);
alter table aziende add column codfiscale character varying(16);

