drop view v_rilevatori_errore;
drop view v_scarico_solo_antenne_gateway_censiti;
drop view v_scarico_solo_antenne_censite;

alter table network alter column antenna type character varying (10);