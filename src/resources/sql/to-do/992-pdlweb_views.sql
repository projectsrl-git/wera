/*======================== PDLWEB ===================================*/







DROP VIEW IF exists V_LIMOD70;
DROP VIEW IF exists V_LIMOD70_DETTAGLIO;
DROP VIEW IF exists V_LIMOD64;
DROP VIEW IF exists V_LIMOD64_DETTAGLIO;







-- view [V_IMPIANTI]
DROP VIEW IF exists V_IMPIANTI;

CREATE VIEW V_IMPIANTI
AS
SELECT 
	IMP.* 
	,AZI.RAGSOC AS DESCR_SITO
FROM IMPIANTI AS IMP
LEFT OUTER JOIN AZIENDE AS AZI
ON IMP.ID_AZIENDA=AZI.ID_AZIENDA
where IMP.FL_DISATTIVO = false 
;

-- view [V_AREA_LAVORO]
DROP VIEW IF exists V_AREA_LAVORO;

CREATE VIEW V_AREA_LAVORO
AS
SELECT 
	ARE.*
	,imp.descr_impianto
	,azi.id_azienda
	,azi.ragsoc	
FROM AREA_LAVORO AS ARE
left outer join impianti as imp
on imp.id_impianto=are.id_impianto
left outer join aziende as azi
on azi.id_azienda=imp.id_azienda
WHERE ARE.FL_DISATTIVO = false 
;


-- view [V_EQUIPMENT]
DROP VIEW IF exists V_EQUIPMENT;

CREATE VIEW V_EQUIPMENT
AS
SELECT 
	EQU.*
	,are.descr_area
	,imp.id_impianto
	,imp.descr_impianto
	,azi.id_azienda
	,azi.ragsoc
	,EQU.ID_EQUIPMENT::VARCHAR||'|'||FL_EIS::VARCHAR AS ID_EQUIPMENT_EIS
FROM EQUIPMENT AS EQU
left outer join area_lavoro as are
on equ.id_area=are.id_area
left outer join impianti as imp
on imp.id_impianto=are.id_impianto
left outer join aziende as azi
on azi.id_azienda=imp.id_azienda
WHERE EQU.FL_DISATTIVO = false 
;


-- view [V_SITIPDL]
DROP VIEW IF exists V_SITIPDL;

CREATE VIEW V_SITIPDL
AS
SELECT 
	AZI.* 
FROM AZIENDE AS AZI
WHERE AZI.TIPO_AZIENDA LIKE '%PDL%' 
;

-- view [V_PDL]
DROP VIEW IF exists V_PDL;

CREATE VIEW V_PDL
AS
SELECT 
	PDL.*
	,azi.RAGSOC
	,SPL.DESCRIZIONE AS DESCR_STATO
	,IMP.DESCR_IMPIANTO
	,ARE.DESCR_AREA
	,EQI.DESCR_EQUIPMENT
	,DESCRIZIONE_DELLA_SOSPENSIONE as MOTIVO_SOSPENSIONE
	,to_char(TS_ATTIVAZIONE,'yyyy/mm/dd') as DATA_ULTIMA_ATTIVAZIONE
	,to_char(TS_ATTIVAZIONE,'HH24:MI') as ORA_UTLIMA_ATTIVAZIONE 
	,initcap(UATT.nome::text)||' '||initcap(UATT.cognome::text) as UTENTE_ULTIMA_ATTIVAZIONE
	,MDI.valore_attributo as modulo_interferenze,CASE WHEN FLG_TA THEN 'In fermata' ELSE 'Non in fermata' END as stato_fermata
FROM PDL 
left outer join aziende as azi ON PDL.ID_AZIENDA=azi.ID_AZIENDA 
left outer join impianti as IMP ON PDL.ID_IMPIANTO=IMP.ID_IMPIANTO and  PDL.ID_AZIENDA= IMP.ID_AZIENDA
left outer join area_lavoro as ARE ON PDL.ID_AREA= ARE.ID_AREA AND PDL.ID_IMPIANTO= ARE.ID_IMPIANTO
left outer join equipment as EQI ON PDL.ID_EQUIPMENT= EQI.ID_EQUIPMENT AND PDL.ID_AREA= EQI.ID_AREA
left outer join utenti as UATT on PDL.ID_UTENTE_ATTIVAZIONE = UATT.ID_UTENTE
left outer join parametri as SPL ON PDL.STATO= SPL.CODICE and SPL.DOMINIO='SPL'
left outer join ATTRIBUTI_AZIENDA as MDI on PDL.ID_AZIENDA=MDI.ID_AZIENDA and  MDI.codice_attributo= 'MODULO_INTERFERENZE'
;




-- view [V_IMPRESE]
DROP VIEW IF exists V_IMPRESE;

CREATE VIEW V_IMPRESE
AS
SELECT 
	DISTINCT NOME_IMPRESA
FROM PREPOSTI_IMPRESA 
;

-- view [V_IMPRESE]
DROP VIEW IF exists V_IMPRESE;

CREATE VIEW V_IMPRESE
AS
SELECT 
	DISTINCT NOME_IMPRESA
	,ID_AZIENDA
FROM PREPOSTI_IMPRESA 
;


-- view [V_PREPOSTI_IMPRESA]
DROP VIEW IF exists V_PREPOSTI_IMPRESA;

CREATE VIEW V_PREPOSTI_IMPRESA
AS
SELECT 
	ID_PREPOSTO_IMPRESA
	,PI.ID_AZIENDA
	,AZ.RAGSOC
	,NOME_IMPRESA
	,NOME_COGNOME_PREPOSTO_IMPRESA
FROM PREPOSTI_IMPRESA as PI
left outer join AZIENDE as AZ
on PI.ID_AZIENDA = AZ.ID_AZIENDA
;

-- view [V_UTENTI_PROFILI_AZIENDE]
DROP VIEW IF exists V_UTENTI_PROFILI_AZIENDE;

CREATE VIEW V_UTENTI_PROFILI_AZIENDE
AS
SELECT 
	ut.id_utente
	,nome
	,cognome
	,nome||' '||cognome as NOME_COGNOME 
	,pr.codice as profilo
	,ua.id_azienda
FROM UTENTI as ut
inner join utenti_profili as up on up.id_utente= ut.id_utente 
inner join profili as pr on up.id_profilo=pr.id_profilo 
inner join utenti_aziende as ua on up.id_utente = ua.id_utente
inner join aziende as az on ua.id_azienda=az.id_azienda and tipo_azienda like '%PDL%';
;


-- view [V_STRUTTURA_TREE]
DROP VIEW IF exists V_STRUTTURA_TREE;

CREATE VIEW V_STRUTTURA_TREE
AS
select 
	1 as livello
	,'sito_'||id_azienda::varchar as id
	,'#' as parent
	,replace(ragsoc,'"','\"') as node_text
	,id_azienda
	,'fa fa-building fa-2x' as icon
	,null::integer as id_impianto
	,null::integer as id_area
	,null::integer as id_equipment
from aziende 
where tipo_azienda like '%PDL%'
union
select 
	2 as livello
	,'impianto_'||id_impianto::varchar as id
	,'sito_'||id_azienda::varchar as parent
	,replace(descr_impianto,'"','\"') as node_text
	,id_azienda
	,'fa fa-industry fa-2x' as icon
	,id_impianto
	,null::integer as id_area
	,null::integer as id_equipment
from impianti where fl_disattivo=false
union
select 
	3 as livello
	,'area_'||id_area::varchar as id
	,'impianto_'||are.id_impianto::varchar as parent
	,replace(descr_area,'"','\"') as node_text
	,imp.id_azienda
	,'fa fa-sitemap fa-2x' as icon
	,are.id_impianto
	,are.id_area
	,null::integer as id_equipment
from area_lavoro as are 
left outer join impianti as imp on are.id_impianto = imp.id_impianto 
where are.fl_disattivo=false
union
select 
	4 as livello
	,'id_'||imp.id_azienda::varchar||'_'||are.id_impianto::varchar||'_'||equ.id_area::varchar||'_'||id_equipment::varchar as id
	,'area_'||equ.id_area::varchar as parent
	,replace(descr_equipment,'"','\"') as node_text
	,imp.id_azienda	
	,'fa fa-cogs fa-2x' as icon
	,are.id_impianto
	,equ.id_area
	,equ.id_equipment
from equipment as equ
left outer join area_lavoro as are on are.id_area = equ.id_area 
left outer join impianti as imp on are.id_impianto = imp.id_impianto 
where equ.fl_disattivo=false
UNION
select 
	5 as livello
	,'iid_'||imp.id_azienda::varchar||'_'||are.id_impianto::varchar||'_'||equ.id_area::varchar||'_'||id_equipment::varchar as id
	,'id_'||imp.id_azienda::varchar||'_'||are.id_impianto::varchar||'_'||equ.id_area::varchar||'_'||id_equipment::varchar as parent
	,'Nuovo PdL' as node_text
	,imp.id_azienda	
	,'fa fa-file-o' as icon
	,are.id_impianto
	,equ.id_area
	,equ.id_equipment
from equipment as equ
left outer join area_lavoro as are on are.id_area = equ.id_area 
left outer join impianti as imp on are.id_impianto = imp.id_impianto 
where equ.fl_disattivo=false
UNION
select 
	5 as livello
	,'sid_'||imp.id_azienda::varchar||'_'||are.id_impianto::varchar||'_'||equ.id_area::varchar||'_'||id_equipment::varchar as id
	,'id_'||imp.id_azienda::varchar||'_'||are.id_impianto::varchar||'_'||equ.id_area::varchar||'_'||id_equipment::varchar as parent
	,'Ricerca PdL' as node_text
	,imp.id_azienda	
	,'fa fa-search' as icon
	,are.id_impianto
	,equ.id_area
	,equ.id_equipment
from equipment as equ
left outer join area_lavoro as are on are.id_area = equ.id_area 
left outer join impianti as imp on are.id_impianto = imp.id_impianto 
where equ.fl_disattivo=false
;




-- view V_GRAFICO1
DROP VIEW IF EXISTS V_GRAFICO1;
CREATE VIEW V_GRAFICO1 
AS
select dt_pdl,count(*) as totale from pdl where dt_pdl between 
to_char(cast(date_trunc('month', current_date) as date),'YYYY/mm/DD')
and 
to_char(cast(date_trunc('month', current_date)+'1month'::interval-'1day'::interval  as date),'YYYY/mm/DD') 
group by dt_pdl order by dt_pdl
;





CREATE VIEW V_LIMOD70
AS
		SELECT distinct
		MOD.*
		,RAGSOC  AS DESCR_SITO
		,DESCR_IMPIANTO
		,RAGSOC AS DESCR_AZIENDA
		,CODICE_IMPIANTO
		,descr_area
		,sta.DESCRIZIONE AS DESCR_STATO
		,VAL.DESCRIZIONE AS descr_VALUTAZIONE
		,nome||' '||cognome as firma
		,CASE distanza_rispetto WHEN 'S' THEN 'Si' WHEN 'N' THEN 'No' ELSE '' END as distanzarispetto
	FROM LIMOD70 AS MOD
	LEFT OUTER JOIN AZIENDE AS AZI
	ON MOD.ID_AZIENDA=AZI.ID_AZIENDA 
	LEFT OUTER JOIN IMPIANTI AS IMP 
	ON MOD.ID_IMPIANTO=IMP.ID_IMPIANTO AND  MOD.ID_AZIENDA= IMP.ID_AZIENDA
	LEFT OUTER JOIN PARAMETRI AS STA
	ON STA.CODICE = MOD.STATO AND STA.DOMINIO ='STA'
	LEFT OUTER JOIN PARAMETRI AS VAL
ON VAL.CODICE=MOD.VALUTAZIONE AND VAL.DOMINIO = 'VAL'
left outer join area_lavoro as ARE 
	ON mod.ID_AREA= ARE.ID_AREA 
	LEFT OUTER JOIN V_UTENTI_PROFILI_AZIENDE AS VUPR
	ON mod.ID_UTENTE_INS = VUPR.ID_UTENTE
;

-- view [V_LIMOD70_DETTAGLIO]

CREATE VIEW V_LIMOD70_DETTAGLIO
AS
SELECT distinct
	DET.* 
	,descr_area
	,pdl.id_impianto
	,pdl.id_azienda
	,li.id_area
	,id_equipment
	,nr_pdl
	,dt_pdl
	,odl
	,impresa_testo
	,nome_cognome_preposto_impresa
	,nome_cognome_delegato_lavori_al
	,NOME_COGNOME
	,descrizione_lavoro
	,to_char(DET.TS_INS,'HH24:MI:SS') as ORA_INS
	, valutazione
	,VAL.DESCRIZIONE AS descr_VALUTAZIONE
	,distanza_rispetto
	,CASE distanza_rispetto WHEN 'S' THEN 'Si' WHEN 'N' THEN 'No' ELSE '' END as distanzarispetto
FROM limod70_dettagli AS DET
LEFT OUTER JOIN V_PDL AS pdl
ON DET.id_pdl=pdl.id_pdl
LEFT OUTER JOIN V_UTENTI_PROFILI_AZIENDE AS VUPR
	ON det.ID_UTENTE_INS = VUPR.ID_UTENTE
	left outer join limod70 as li
	on li.id_modulo=det.id_modulo
	LEFT OUTER JOIN PARAMETRI AS VAL
ON VAL.CODICE=li.VALUTAZIONE AND VAL.DOMINIO = 'VAL'
	;












-- view [V_INTERFERENZE]
DROP VIEW IF exists V_INTERFERENZE_TEMP CASCADE;

CREATE VIEW V_INTERFERENZE_TEMP AS 


SELECT 
	lista_id_pdl,T2.ID_IMPIANTO
	
	
    ,NR_IMPRESE >1 AS FLG_POSSIBILE_INTERFERENZA
FROM (
	SELECT 
		ID_AZIENDA
		,ID_IMPIANTO
		
		,string_agg(NOME_IMPRESA, ',') as lista_imprese	
		,string_agg(lista_id_pdl, ',') as lista_id_pdl		
		,string_agg(lista_nr_pdl, ',') as lista_nr_pdl
		,string_agg(lista_dt_attivazione, ',') as lista_dt_attivazione	
		,string_agg(lista_descrizione_lavoro, ',') as lista_descrizione_lavoro
		,string_agg(lista_preposto_impresa, ',') as lista_preposto_impresa
        ,COUNT(*) AS NR_IMPRESE
	FROM (
		SELECT  
			ID_AZIENDA
			,ID_IMPIANTO
			
			,NOME_IMPRESA
			,string_agg(dt_attivazione, ',') as lista_dt_attivazione
			,string_agg(ID_PDL::varchar, ',') as lista_id_pdl		
			,string_agg(NR_PDL, ',') as lista_nr_pdl
			,string_agg(DESCRIZIONE_LAVORO, ',') as lista_descrizione_lavoro		
			,string_agg(NOME_COGNOME_PREPOSTO_IMPRESA, ',') as lista_preposto_impresa
		FROM V_IMPRESE_PDL_ATTIVI 
		GROUP BY 
			ID_AZIENDA
			,ID_IMPIANTO
			,ID_AREA
			,NOME_IMPRESA,id_pdl
			order by id_pdl
	) AS T1
	GROUP BY
		ID_AZIENDA
		,ID_IMPIANTO
		
) AS T2 
left outer join aziende as azi ON T2.ID_AZIENDA=azi.ID_AZIENDA 
left outer join impianti as IMP ON T2.ID_IMPIANTO=IMP.ID_IMPIANTO 
WHERE NR_IMPRESE>1
;










CREATE VIEW V_LIMOD64
AS
	SELECT 
		MOD.*
		,RAGSOC  AS DESCR_SITO
		,DESCR_IMPIANTO
		,RAGSOC AS DESCR_AZIENDA
		,CODICE_IMPIANTO
		,sta.DESCRIZIONE AS DESCR_STATO
		,descr_area
		, nome||' '||cognome as nome_cognome
		,to_char(mod.TS_INS,'HH24:MI:SS') as ORA
	FROM LIMOD64 AS MOD
	LEFT OUTER JOIN AZIENDE AS AZI
	ON MOD.ID_AZIENDA=AZI.ID_AZIENDA 
	LEFT OUTER JOIN IMPIANTI AS IMP 
	ON MOD.ID_IMPIANTO=IMP.ID_IMPIANTO AND  MOD.ID_AZIENDA= IMP.ID_AZIENDA
	LEFT OUTER JOIN PARAMETRI AS STA
	ON STA.CODICE = MOD.STATO AND STA.DOMINIO ='STA'
	left outer join area_lavoro as ARE 
	ON mod.ID_AREA= ARE.ID_AREA 
	left outer join UTENTI AS UT
	ON mod.id_coord_gest_int = UT.ID_UTENTE


;

-- view [V_LIMOD64_DETTAGLIO]

CREATE VIEW V_LIMOD64_DETTAGLIO
AS
SELECT 
	DET.* 
	,descr_area
	,id_impianto
	,id_azienda
	,id_area
	,id_equipment
	,nr_pdl
	,dt_pdl
	,nome_cognome_delegato_lavori_al
FROM limod64_dettagli AS DET
LEFT OUTER JOIN V_PDL AS pdl
ON DET.id_pdl=pdl.id_pdl
;
