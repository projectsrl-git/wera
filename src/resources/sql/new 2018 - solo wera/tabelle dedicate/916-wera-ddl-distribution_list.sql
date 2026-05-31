-- Table: DISTRIBUTION_LIST

DROP TABLE IF EXISTS DISTRIBUTION_LIST CASCADE;

CREATE TABLE DISTRIBUTION_LIST
(
	id_distribution_list serial not null,
	modulo character varying(30) not null,
	id_azienda integer not null,
	lista_mail text not null
);

INSERT INTO DISTRIBUTION_LIST (MODULO,ID_AZIENDA,LISTA_MAIL) SELECT 'RIPARTIZIONI_UNI_2018' AS MODULO, ID_AZIENDA,'luca.remiddi@projectsrl.net' FROM AZIENDE WHERE CODICE='WER';
INSERT INTO DISTRIBUTION_LIST (MODULO,ID_AZIENDA,LISTA_MAIL) SELECT 'RIPARTIZIONI_UNI' AS MODULO, ID_AZIENDA,'luca.remiddi@projectsrl.net' FROM AZIENDE WHERE CODICE='WER';
INSERT INTO DISTRIBUTION_LIST (MODULO,ID_AZIENDA,LISTA_MAIL) SELECT 'RIPARTIZIONI' AS MODULO, ID_AZIENDA,'luca.remiddi@projectsrl.net' FROM AZIENDE WHERE CODICE='WER';

-- Table: MAIL_CONFIG

DROP TABLE IF EXISTS MAIL_CONFIG CASCADE;

CREATE TABLE MAIL_CONFIG
(
	id_mail_config serial not null,
	modulo character varying(30) not null,
	workflow_action character varying(30) not null,
	stato_iniziale char(3) not null,
	stato_finale char(3) not null,
	oggetto text not null,
	testo_mail text not null,
	mittente text
);




insert into MAIL_CONFIG (	
	modulo,
	workflow_action,
	stato_iniziale,
	stato_finale,
	oggetto,
	mittente,
	testo_mail
) values (
'RIPARTIZIONI_UNI'
,'APPROVE'
,'DRA'
,'WAI'
,'WebCredit WERA - Ripartizione UNI nr. #NR_MODULO# del #DT_MODULO#'
,'account@wera.club'
,'Gentile Utente,<br><br>è stata creata la ripartizione in oggetto in attesa della sua approvazione.<br>Per consultarla e procedere con l''approvazione può accedere al portale cliccando sul seguente collegamento:<br><br><a href="https://www.webcredit.wera.club/wera/astro?FUNCTIONID=InserimentoRipartizioniUNI&OPERATION_TYPE=UPDATE&READONLY=Y&ID_MODULO=#ID_MODULO#">Cliccare qui</a><br><br>Cordiali saluti,<br><br>Team WebCredit WERA<br>'
)
;

insert into MAIL_CONFIG (	
	modulo,
	workflow_action,
	stato_iniziale,
	stato_finale,
	oggetto,
	mittente,
	testo_mail
) values (
'RIPARTIZIONI_UNI'
,'APPROVE'
,'WAI'
,'APP'
,'WebCredit WERA - Ripartizione UNI nr. #NR_MODULO# del #DT_MODULO# - APPROVATO'
,'account@wera.club'
,'Gentile Utente,<br><br>la ripartizione in oggetto è stata approvata.<br>Per consultarla può accedere al portale cliccando sul seguente collegamento:<br><br><a href="https://www.webcredit.wera.club/wera/astro?FUNCTIONID=InserimentoRipartizioniUNI&OPERATION_TYPE=UPDATE&READONLY=Y&ID_MODULO=#ID_MODULO#">Cliccare qui</a><br><br>Cordiali saluti,<br><br>Team WebCredit WERA<br>'
)
;


insert into MAIL_CONFIG (	
	modulo,
	workflow_action,
	stato_iniziale,
	stato_finale,
	oggetto,
	mittente,
	testo_mail
) values (
'RIPARTIZIONI_UNI'
,'COMPLETED'
,'DRA'
,'APP'
,'WebCredit WERA - Ripartizione UNI nr. #NR_MODULO# del #DT_MODULO# - APPROVATO'
,'account@wera.club'
,'Gentile Utente,<br><br>la ripartizione in oggetto è stata pubblicata.<br>Per consultarla può accedere al portale cliccando sul seguente collegamento:<br><br><a href="https://www.webcredit.wera.club/wera/astro?FUNCTIONID=InserimentoRipartizioniUNI&OPERATION_TYPE=UPDATE&READONLY=Y&ID_MODULO=#ID_MODULO#">Cliccare qui</a><br><br>Cordiali saluti,<br><br>Team WebCredit WERA<br>'
)
;



insert into MAIL_CONFIG (	
	modulo,
	workflow_action,
	stato_iniziale,
	stato_finale,
	oggetto,
	mittente,
	testo_mail
) values (
'RIPARTIZIONI_UNI'
,'ROLLBACK'
,'WAI'
,'DRA'
,'WebCredit WERA - Ripartizione UNI nr. #NR_MODULO# del #DT_MODULO# - ROLLBACK'
,'account@wera.club'
,'Gentile Utente,<br><br>la ripartizione in oggetto è stato riportata allo stato di bozza.<br>Per consultarla può accedere al portale cliccando sul seguente collegamento:<br><br><a href="https://www.webcredit.wera.club/wera/astro?FUNCTIONID=InserimentoRipartizioniUNI&OPERATION_TYPE=UPDATE&READONLY=Y&ID_MODULO=#ID_MODULO#">Cliccare qui</a><br><br>Cordiali saluti,<br><br>Team WebCredit WERA<br>'
)
;

insert into MAIL_CONFIG (	
	modulo,
	workflow_action,
	stato_iniziale,
	stato_finale,
	oggetto,
	mittente,
	testo_mail
) values (
'RIPARTIZIONI_UNI'
,'NOTIFY'
,'WAI'
,'APP'
,'WebCredit WERA - Ripartizione UNI nr. #NR_MODULO# del #DT_MODULO#'
,'account@wera.club'
,'Gentile Utente,<br><br>la informiamo che la ripartizione in oggetto è stato registrata nel portale WebCredit WERA.<br>Per consultarla può accedere al portale cliccando sul seguente collegamento:<br><br><a href="https://www.webcredit.wera.club/wera/astro?FUNCTIONID=InserimentoRipartizioniUNI&OPERATION_TYPE=UPDATE&READONLY=Y&ID_MODULO=#ID_MODULO#">Cliccare qui</a><br><br>Cordiali saluti,<br><br>Team WebCredit WERA<br>'
)
;



insert into MAIL_CONFIG 
(
	modulo
	,workflow_action
	,stato_iniziale
	,stato_finale
	,oggetto
	,mittente
	,testo_mail
) 
select 	
	'RIPARTIZIONI' modulo
	,workflow_action
	,stato_iniziale
	,stato_finale
	,replace(oggetto,'Ripartizione UNI','Ripartizione')
	,mittente
	,replace(testo_mail,'RipartizioniUNI','Ripartizioni')
from MAIL_CONFIG
where modulo = 'RIPARTIZIONI_UNI'
;	





insert into MAIL_CONFIG 
(
	modulo
	,workflow_action
	,stato_iniziale
	,stato_finale
	,oggetto
	,mittente
	,testo_mail
) 
select 	
	'RIPARTIZIONI_LETTURE' modulo
	,workflow_action
	,stato_iniziale
	,stato_finale
	,replace(oggetto,'Ripartizione UNI','Ripartizione sola lettura')
	,mittente
	,replace(testo_mail,'RipartizioniUNI','RipartizioniLetture')
from MAIL_CONFIG
where modulo = 'RIPARTIZIONI_UNI'
;	




insert into MAIL_CONFIG 
(
	modulo
	,workflow_action
	,stato_iniziale
	,stato_finale
	,oggetto
	,mittente
	,testo_mail
) 
select 	
	'RIPARTIZIONI_UNI_2018' modulo
	,workflow_action
	,stato_iniziale
	,stato_finale
	,replace(oggetto,'Ripartizione UNI','Ripartizione UNI 10200:2018')
	,mittente
	,replace(testo_mail,'RipartizioniUNI','RipartizioniUNI2018')
from MAIL_CONFIG
where modulo = 'RIPARTIZIONI_UNI'
;	


drop view if exists V_DISTRIBUTION_LIST;
create view V_DISTRIBUTION_LIST as 
select 
	dl.modulo
	,dl.workflow_action
	,dl.stato_iniziale
	,dl.stato_finale
	,dl.oggetto
	,dl.mittente
	,dl.testo_mail
	,t1.id_azienda
	,t1.codice_profilo
	,t1.destinatari_to
	,'' as destinatari_cc
	,'' as destinatari_bcc
from MAIL_CONFIG as dl, (select 
	ua.id_azienda
	,pr.codice as codice_profilo
	,string_agg(ut.email,';') as destinatari_to
from utenti_aziende as ua
inner join utenti as ut
on ut.id_utente = ua.id_utente 
inner join utenti_profili as up
on ua.id_utente = up.id_utente 
inner join profili as pr
on up.id_profilo = pr.id_profilo and pr.codice in ('ADM')
inner join aziende as az 
on ua.id_azienda=az.id_azienda
group by 
	ua.id_azienda
	,pr.codice 
) as t1
where dl.workflow_action='APPROVE'
	and dl.stato_iniziale='DRA'
	AND dl.stato_finale='WAI'
	
UNION

select 
	dl.modulo
	,dl.workflow_action
	,dl.stato_iniziale
	,dl.stato_finale
	,dl.oggetto
	,dl.mittente
	,dl.testo_mail	
	,null as id_azienda
	,'' as codice_profilo
	,'' as destinatari_to
	,'' as destinatari_cc
	,'' as destinatari_bcc
from MAIL_CONFIG as dl
where dl.workflow_action='APPROVE'
	and dl.stato_iniziale='WAI'
	AND dl.stato_finale='APP'
	
UNION

select 
	dl.modulo
	,dl.workflow_action
	,dl.stato_iniziale
	,dl.stato_finale
	,dl.oggetto
	,dl.mittente
	,dl.testo_mail	
	,null as id_azienda
	,'' as codice_profilo
	,'' as destinatari_to
	,'' as destinatari_cc
	,'' as destinatari_bcc
from MAIL_CONFIG as dl
where dl.workflow_action='COMPLETED'
	and dl.stato_iniziale='DRA'
	AND dl.stato_finale='APP'
	
UNION

select 
	dl.modulo
	,dl.workflow_action
	,dl.stato_iniziale
	,dl.stato_finale
	,dl.oggetto
	,dl.mittente
	,dl.testo_mail	
	,null as id_azienda
	,'' as codice_profilo
	,'' as destinatari_to
	,'' as destinatari_cc
	,'' as destinatari_bcc
from MAIL_CONFIG as dl
where dl.workflow_action='ROLLBACK'
	and dl.stato_iniziale='WAI'
	AND dl.stato_finale='DRA'
	
UNION

select 
	dl.modulo
	,mc.workflow_action
	,mc.stato_iniziale
	,mc.stato_finale
	,mc.oggetto
	,mc.mittente
	,mc.testo_mail	
	,dl.id_azienda
	,'' as codice_profilo
	,dl.lista_mail as destinatari_to
	,'' as destinatari_cc
	,'' as destinatari_bcc
from MAIL_CONFIG as mc
inner join DISTRIBUTION_LIST as dl
on mc.modulo=dl.modulo
where mc.workflow_action='NOTIFY'
	and mc.stato_iniziale='WAI'
	AND mc.stato_finale='APP'
;






drop view if exists V_MAIL_CONFIG;
create view V_MAIL_CONFIG as 
SELECT 
	mc.*
	,s1.descrizione AS descr_stato_iniziale
	,s2.descrizione AS descr_stato_finale
FROM mail_config AS mc
LEFT OUTER JOIN parametri AS s1 on s1.dominio ='STA' AND s1.codice=mc.stato_iniziale
LEFT OUTER JOIN parametri AS s2 on s2.dominio ='STA' AND s2.codice=mc.stato_finale
;

drop view if exists V_DISTRIBUTION_LIST_RICERCA;
create view V_DISTRIBUTION_LIST_RICERCA as 
SELECT 
	dl.*
	,si.ragsoc
FROM distribution_list AS dl
LEFT OUTER JOIN aziende AS si ON si.id_azienda=dl.id_azienda
;
