drop table if exists ATTRIBUTI_AZIENDA;
CREATE TABLE ATTRIBUTI_AZIENDA (
	id_attributo_azienda serial
	,id_azienda integer
	,codice_attributo varchar(30)
	,valore_attributo varchar 
)
;

insert into ATTRIBUTI_AZIENDA ( id_azienda,codice_attributo,valore_attributo)  
select id_azienda, 'MODULO_INTERFERENZE'
	, case 
		when codice ='PRISMR' then 'LI/RCSS/PR-MOD 89' 
		when codice ='PRIASU' then 'LI/SR-MOD 181' 
		when codice ='FEB' then 'LI-PV-MOD 86'
		WHEN codice ='SAR' then 'LI/CA-MOD135'
		WHEN codice ='CAS' then 'LI-VR MOD 50'
		WHEN codice ='PAD' then 'LI-PD MOD 66'
		WHEN codice ='CAR' then ''
		WHEN codice ='OSO' then 'LI-VSA MOD 27'
		WHEN codice ='LIM' then 'LI-MI MOD 155'
	end as descr_attributo
from aziende where tipo_azienda like '%PDL%';


