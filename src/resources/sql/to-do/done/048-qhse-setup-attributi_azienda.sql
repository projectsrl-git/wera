delete from ATTRIBUTI_AZIENDA where codice_attributo ='SITO_AIFA';

insert into ATTRIBUTI_AZIENDA ( id_azienda,codice_attributo,valore_attributo)  
select id_azienda, 'SITO_AIFA','S' from aziende where codice in ( 'PRIASU','FEB','SAR','CAS','PAD','LIM');

