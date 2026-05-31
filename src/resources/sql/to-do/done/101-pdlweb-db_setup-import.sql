/*
Priolo SMR
*/

--IMPIANTI
DELETE FROM IMPIANTI;
ALTER SEQUENCE IMPIANTI_ID_IMPIANTO_seq RESTART WITH 1;

insert into IMPIANTI (id_azienda,descr_impianto,id_utente_ins) 
SELECT ID_AZIENDA,'GASCO2',(select id_utente from utenti where username='assistenza') from AZIENDE where codice='PRISMR';

insert into IMPIANTI (id_azienda,descr_impianto,id_utente_ins) 
SELECT ID_AZIENDA,'SMR',(select id_utente from utenti where username='assistenza') from AZIENDE where codice='PRISMR';

--AREA_LAVORO
delete from AREA_LAVORO;
ALTER SEQUENCE AREA_LAVORO_id_AREA_seq RESTART WITH 1;
insert into AREA_LAVORO (id_impianto,descr_area,id_utente_ins) 
SELECT DISTINCT id_impianto,AREA, (select id_utente from utenti where username='assistenza')
FROM temp_lista_equipment as tle 
inner join impianti as imp on tle.impianto=imp.descr_impianto
;

--equipment
delete from equipment;
ALTER SEQUENCE equipment_id_equipment_seq RESTART WITH 1;
insert into equipment (id_area,descr_equipment,fl_eis,note)
select id_area,equipment as descr_equipment,case when eis='*' then true else false end as fl_eis,note from temp_lista_equipment left outer join AREA_LAVORO on area=descr_area
;





