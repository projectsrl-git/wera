-- Drop views 

drop VIEW if exists V_UTENTI_PROFILI cascade;
DROP VIEW IF exists v_menu CASCADE;
DROP VIEW IF exists v_menu_tree;
DROP VIEW IF exists V_AZIENDE_TREE;
DROP VIEW IF exists V_DIPENDENTI;
DROP VIEW IF exists V_UTENTI;
DROP VIEW IF exists V_LOGIN;







-- view V_UTENTI_PROFILI
CREATE VIEW V_UTENTI_PROFILI 
AS
SELECT
	UPR.ID_UTENTE
	,PRF.CODICE
	,PRF.DESCRIZIONE
FROM UTENTI_PROFILI AS UPR
LEFT OUTER JOIN PROFILI AS PRF
ON UPR.ID_PROFILO = PRF.ID_PROFILO
;


-- view V_MENU
CREATE VIEW V_MENU AS 
 SELECT me.id_menu,
    me.id_menu_sup,
	me.alias,
    me.ordine,
    ml.descrizione,
    LI.codice_iso,
    me.link,
	me.icon
   FROM menu me
   INNER JOIN menu_lingue AS ML 
   ON me.id_menu = ml.id_menu
   INNER JOIN LINGUE_ISO AS LI
   ON ML.ID_LINGUE_ISO=LI.ID_LINGUE_ISO
;


-- view v_menu_tree
CREATE VIEW v_menu_tree AS 
WITH RECURSIVE v_menu_tree(
	id_menu_sup
	,descrizione_sup
	,link_sup
	,id_menu
	,alias
	,icon
	,descrizione
	,link
	,path
	,path_descri
	,link_chain
	,depth
	,codice_iso) AS (
	SELECT 
		r.id_menu_sup
		,p1.descrizione AS descrizione_sup
		,p1.link AS link_sup
		,r.id_menu
		,R.ALIAS 
		,R.ICON
		,p2.descrizione
		,p2.link
		,lpad(r.ordine::varchar, 4, '0') AS path
		,'Home / ' || r.descrizione::varchar AS path_descri
	        ,'astro?FUNCTIONID=Home;' || P2.LINK  as LINK_CHAIN		
        	,1 as DEPTH
		,r.codice_iso
	FROM v_menu as r
		,v_menu as p1
		,v_menu as p2
	WHERE r.id_menu_sup = (SELECT id_menu FROM menu WHERE id_menu_sup IS NULL) 
		AND p1.id_menu = r.id_menu_sup AND p1.codice_iso = r.codice_iso 
		AND p2.id_menu = r.id_menu AND p2.codice_iso = r.codice_iso 
		AND p1.codice_iso = p2.codice_iso
	UNION ALL
	SELECT 
		r.id_menu_sup
		,p1.descrizione AS descrizione_sup
		,p1.link AS link_sup
		,r.id_menu
		,R.ALIAS
		,R.ICON
		,p2.descrizione
		,p2.link
		,nd.path || lpad(r.ordine::varchar, 4, '0') as path
		,nd.path_descri|| ' / ' || r.descrizione AS path_descri
        	,LINK_CHAIN || ';' || P2.LINK  as LINK_CHAIN
		,nd.depth + 1
		,r.codice_iso
	FROM v_menu as r
		,v_menu as p1
		,v_menu as p2
		,v_menu_tree as nd
	WHERE 
		R.ID_MENU_SUP not in (SELECT ID_MENU FROM V_MENU WHERE ID_MENU_SUP IS  NULL) 
		and r.id_menu_sup = nd.id_menu AND r.codice_iso = nd.codice_iso 
		AND p1.id_menu = r.id_menu_sup AND p1.codice_iso = r.codice_iso 
		AND p2.id_menu = r.id_menu AND p2.codice_iso = r.codice_iso
)
SELECT DISTINCT * FROM v_menu_tree
UNION ALL
SELECT id_menu_sup
	,'' AS descrizione_sup
	,'' AS link_sup
	,id_menu
	,'Home' AS ALIAS
	,'fa fa-home' AS ICON
	,descrizione
	,link
	,'' AS path
	,'Home' AS path_descri
	,'astro?FUNCTIONID=Home' AS LINK_CHAIN
    	,0 AS depth
    	,codice_iso
FROM v_menu 
WHERE id_menu_sup IS NULL
;



-- view V_AZIENDE_TREE
CREATE VIEW V_AZIENDE_TREE AS 
WITH RECURSIVE V_AZIENDE_TREE(
		ID_AZIENDA_PARENT
		, RAGSOC_SUP
		, ID_AZIENDA
		, ALIAS_AZIENDA
		,CREDITI
		,DATA_SCADENZA_CREDITI
		, RAGSOC
		, PATH
		, DEPTH
	) as (
	SELECT
		ID_AZIENDA_PARENT
		, ''::varchar AS RAGSOC_SUP
		, ID_AZIENDA
		, ALIAS_AZIENDA
		,CREDITI
		,DATA_SCADENZA_CREDITI
		, RAGSOC
		, LPAD(ID_AZIENDA::varchar, 5, '0') as PATH
		, 0 AS DEPTH
		, TIPO_AZIENDA
		, CODICE
	FROM AZIENDE WHERE ID_AZIENDA_PARENT IS NULL
    UNION ALL
    SELECT
        R.ID_AZIENDA_PARENT, 
        P1.RAGSOC::varchar AS RAGSOC_SUP,
        R.ID_AZIENDA, 
		R.ALIAS_AZIENDA ,
		R.CREDITI,
		R.DATA_SCADENZA_CREDITI,
        P2.RAGSOC,
        PATH || '>' || LPAD(R.ID_AZIENDA::varchar, 5, '0') as PATH, 
        ND.DEPTH + 1
        , R.TIPO_AZIENDA
		, R.CODICE
    FROM AZIENDE AS R
		,AZIENDE AS P1
		,AZIENDE AS P2,
        V_AZIENDE_TREE AS ND
    WHERE R.ID_AZIENDA_PARENT IS NOT NULL
	AND R.ID_AZIENDA_PARENT = ND.ID_AZIENDA 
    AND P1.ID_AZIENDA = R.ID_AZIENDA_PARENT AND P2.ID_AZIENDA = R.ID_AZIENDA 
)
SELECT DISTINCT * FROM V_AZIENDE_TREE
;



-- view [V_UTENTI]
CREATE VIEW V_UTENTI AS
SELECT 	USR.ID_UTENTE 
	,USR.USERNAME 
	,LI.CODICE_ISO 
	,USR.COGNOME AS COGNOME_UTENTE 
	,USR.NOME AS NOME_UTENTE 
	,USR.NOME || ' ' || USR.COGNOME AS NOME_COGNOME
	,USR.EMAIL 
	,USR.FL_DISATTIVO 
	,USR.TS_LOGIN
	,to_char(ts_login,'yyyy/mm/dd') as DATA_ULTIMO_ACCESSO 
	,to_char(ts_login,'HH24:MI:SS') as ORA_ULTIMO_ACCESSO 	
	,PWD.PASSWORD 
	,PWD.DT_SCADENZA 
	,USR.ID_UTENTE_INS
	,USR.TS_INS
	,USR.PROVINCIA
	,USR.CAP
	,USR.CODFISC
	,USR.TELEFONO
	,USR.INDIRIZZO
	,USR.LOCALITA
	,(SELECT string_agg(CODICE, ',') FROM V_UTENTI_PROFILI AS EF WHERE ID_UTENTE=USR.ID_UTENTE ) AS PROFILO
	,(SELECT string_agg(DESCRIZIONE, ',') FROM V_UTENTI_PROFILI AS EF WHERE ID_UTENTE=USR.ID_UTENTE ) AS DESCRIZIONE_PROFILO
	,(SELECT string_agg(UA.ID_AZIENDA::varchar, ',') FROM UTENTI_AZIENDE AS UA WHERE UA.ID_UTENTE=USR.ID_UTENTE) AS AZIENDE_UTENTE
	
	,(SELECT string_agg(AZ.RAGSOC, ',') 
		FROM UTENTI_AZIENDE AS UA
		INNER JOIN AZIENDE AS AZ
		ON UA.ID_AZIENDA=AZ.ID_AZIENDA
		WHERE UA.ID_UTENTE=USR.ID_UTENTE 
		) AS RAGSOC_AZIENDE_UTENTE

	,(SELECT string_agg(SD.ID_DIREZIONE::varchar, ',') 
		FROM STAFF_DIREZIONI AS SD
		INNER JOIN UTENTI_AZIENDE AS UA 
		ON UA.ID_UTENTE_AZIENDA=SD.ID_UTENTE_AZIENDA 
		) AS LISTA_DIREZIONI
	--,(SELECT string_agg(denominazione, ', ') FROM CONDOMINI AS CO WHERE CO.ID_AMMINISTRATORE=USR.ID_UTENTE ) AS LISTA_CONDOMINI
	,(SELECT string_agg(distinct co.denominazione, ', ') FROM CONDOMINI AS CO left outer join utenze as ut on ut.id_condominio=co.id_modulo left outer join utenti_utenze as uu on ut.id_modulo=uu.id_utenza WHERE uu.id_utente=USR.ID_UTENTE or CO.ID_AMMINISTRATORE=USR.ID_UTENTE ) AS LISTA_CONDOMINI
	,(SELECT string_agg(denominazione, ', ') FROM UTENZE AS ut left outer join utenti_utenze as uu on ut.id_modulo=uu.id_utenza WHERE uu.id_utente=USR.ID_UTENTE ) AS LISTA_UTENZE
FROM UTENTI AS USR  
LEFT OUTER JOIN PASSWORD AS PWD  
ON USR.ID_UTENTE=PWD.ID_UTENTE AND PWD.FL_VALIDA=true 
INNER JOIN UTENTI_LINGUE AS UL 
ON USR.ID_UTENTE =UL.ID_UTENTE AND UL.FL_DEFAULT=true
INNER JOIN LINGUE_ISO AS LI 
ON UL.ID_LINGUE_ISO =LI.ID_LINGUE_ISO
;





-- view [V_LOGIN]
CREATE VIEW V_LOGIN
AS
SELECT 	USR.ID_UTENTE 
	,USR.USERNAME 
	,LI.CODICE_ISO 
	,USR.COGNOME AS COGNOME_UTENTE 
	,USR.NOME AS NOME_UTENTE 
	,PWD.PASSWORD 
	,PWD.DT_SCADENZA 
	,fl_disattivo
	,(select data_scadenza_crediti from aziende where id_azienda=UA.id_Azienda)
	,(select crediti from aziende where id_azienda=UA.id_Azienda)
	,(select case when DATE_PART('day', now()- to_date(data_scadenza_crediti,'YYYY/MM/DD')::timestamp without time zone )::int>0 then false else true end as crediti_validi from aziende where id_azienda=UA.id_Azienda)
	,(select case when DATE_PART('day', to_date(data_scadenza_crediti,'YYYY/MM/DD')::timestamp without time zone -now() )::int BETWEEN 1 AND 30 THEN true else false end as crediti_in_scadenza from aziende where id_azienda=UA.id_Azienda) 
	,(select DATE_PART('day', to_date(data_scadenza_crediti,'YYYY/MM/DD')::timestamp without time zone-now() )::int as scaduto_da_giorni from aziende where id_azienda=UA.id_Azienda)
	,(SELECT string_agg(rtrim(CODICE), ',') 
		FROM V_UTENTI_PROFILI 
		WHERE ID_UTENTE=USR.ID_UTENTE 
		)  AS PROFILO  

	,(SELECT string_agg(DESCRIZIONE, ',') 
		FROM V_UTENTI_PROFILI 
		WHERE ID_UTENTE=USR.ID_UTENTE 
		)  AS DESCRIZIONE_PROFILO  


	,(SELECT string_agg(ID_AZIENDA::varchar, ',') 
		FROM UTENTI_AZIENDE 
		WHERE ID_UTENTE=USR.ID_UTENTE 
		)  AS AZIENDE_UTENTE  


	,( SELECT ALIAS_AZIENDA FROM V_AZIENDE_TREE WHERE ID_AZIENDA IN (
			SELECT ID_AZIENDA 
			FROM UTENTI_AZIENDE AS UA 
			LEFT OUTER JOIN UTENTI AS UT 
			ON UA.ID_UTENTE=UT.ID_UTENTE 
			WHERE LOWER(UT.USERNAME)=LOWER(USR.USERNAME)) ORDER BY PATH limit 1 ) AS ALIAS_AZIENDA_UTENTE 
			
	,( SELECT AZ.CODICE 
			FROM V_AZIENDE_TREE AS AT 
			LEFT OUTER JOIN AZIENDE AS AZ 
			ON AZ.ID_AZIENDA = AT.ID_AZIENDA 
			WHERE AT.ID_AZIENDA IN (
			SELECT ID_AZIENDA 
			FROM UTENTI_AZIENDE AS UA 
			LEFT OUTER JOIN UTENTI AS UT 
			ON UA.ID_UTENTE=UT.ID_UTENTE 
			WHERE LOWER(UT.USERNAME)=LOWER(USR.USERNAME)) ORDER BY PATH limit 1 ) AS CODICE_AZIENDA_UTENTE 

	,(SELECT string_agg(SD.ID_DIREZIONE::varchar, ',') 
		FROM STAFF_DIREZIONI AS SD
		INNER JOIN UTENTI_AZIENDE AS UA 
		ON UA.ID_UTENTE_AZIENDA=SD.ID_UTENTE_AZIENDA 
		) AS DIREZIONI_UTENTE
		
FROM UTENTI AS USR  
LEFT OUTER JOIN PASSWORD AS PWD  
ON USR.ID_UTENTE=PWD.ID_UTENTE AND PWD.FL_VALIDA=true 
INNER JOIN UTENTI_LINGUE AS UL 
ON USR.ID_UTENTE =UL.ID_UTENTE AND UL.FL_DEFAULT=true
INNER JOIN LINGUE_ISO AS LI 
ON LI.ID_LINGUE_ISO =UL.ID_LINGUE_ISO  
INNER JOIN UTENTI_AZIENDE AS UA
ON UA.ID_UTENTE =USR.ID_UTENTE
;


-- view [V_PARAMETRI]
DROP VIEW IF exists V_PARAMETRI CASCADE;

CREATE VIEW V_PARAMETRI
AS
	select  
		PAR.*
		,DOM.DESCRIZIONE AS DESCR_DOMINIO
	FROM PARAMETRI AS PAR 
	INNER JOIN PARAMETRI AS DOM
	ON PAR.DOMINIO=DOM.CODICE AND DOM.DOMINIO ='DOM'
;






-- view [V_SCARICO_ALL]
DROP VIEW IF exists V_SCARICO_ALL CASCADE;

CREATE VIEW V_SCARICO_ALL
AS
	select 
	case when length(data_di_lettura)=10 and POSITION('/' in data_di_lettura)=5 then substring(data_di_lettura,9,2)||'/'||substring(data_di_lettura,6,2)||'/'||substring(data_di_lettura,1,4) 
	else case when length(data_di_lettura)>=10 then data_di_lettura else '' end  end as DATA_DI_LETTURA_PULITA,
	* from scarico 
;



-- view [V_SCARICO_NON_SCARICATI]
DROP VIEW IF exists V_SCARICO_NON_SCARICATI CASCADE;

CREATE VIEW V_SCARICO_NON_SCARICATI
AS
	select 
	case when length(data_di_lettura)=10 and POSITION('/' in data_di_lettura)=5 then substring(data_di_lettura,9,2)||'/'||substring(data_di_lettura,6,2)||'/'||substring(data_di_lettura,1,4) 
	else case when length(data_di_lettura)>=10 then data_di_lettura else '' end  end as DATA_DI_LETTURA_PULITA,
	* from scarico where n_fabbrica_dispositivo not in (select rilevatore from utenze_dettaglio)
;



-- view [V_SCARICO]
DROP VIEW IF exists V_SCARICO CASCADE;

CREATE VIEW V_SCARICO
AS
	select 
	case when length(data_di_lettura)=10 and POSITION('/' in data_di_lettura)=5 then substring(data_di_lettura,9,2)||'/'||substring(data_di_lettura,6,2)||'/'||substring(data_di_lettura,1,4) 
	else case when length(data_di_lettura)>=10 then data_di_lettura else '' end  end as DATA_DI_LETTURA_PULITA,
	* from scarico where n_fabbrica_dispositivo in (select rilevatore from utenze_dettaglio)
;



-- view [V_SCARICO_SOLO_RILEVATORI_CENSITI]
DROP VIEW IF exists V_SCARICO_SOLO_RILEVATORI_CENSITI CASCADE;

CREATE VIEW V_SCARICO_SOLO_RILEVATORI_CENSITI
AS
	select * from scarico where n_fabbrica_dispositivo in (select rilevatore from utenze_dettaglio)
;




-- view [V_SCARICO_SOLO_ANTENNE_CENSITE]
DROP VIEW IF exists V_SCARICO_SOLO_ANTENNE_CENSITE CASCADE;

CREATE VIEW V_SCARICO_SOLO_ANTENNE_CENSITE
AS
	select * from scarico where n_fabbrica_dispositivo in (select antenna from network where gateway=false)
;





-- view [V_SCARICO_SOLO_RILEVATORI]
DROP VIEW IF exists V_SCARICO_SOLO_RILEVATORI CASCADE;

CREATE VIEW V_SCARICO_SOLO_RILEVATORI
AS
	SELECT * FROM V_SCARICO_ALL WHERE UNITA_DI_MISURA<>'%' AND lower(LETTURA_ATTUALE)<>'ext. power'
;



-- view [V_SCARICO_SOLO_RILEVATORI_NON_SCARICATI]
DROP VIEW IF exists V_SCARICO_SOLO_RILEVATORI_NON_SCARICATI CASCADE;

CREATE VIEW V_SCARICO_SOLO_RILEVATORI_NON_SCARICATI
AS
	SELECT * FROM V_SCARICO_NON_SCARICATI WHERE UNITA_DI_MISURA<>'%' AND lower(LETTURA_ATTUALE)<>'ext. power'
;






-- view [V_SCARICO_SOLO_ANTENNE]
DROP VIEW IF exists V_SCARICO_SOLO_ANTENNE CASCADE;

CREATE VIEW V_SCARICO_SOLO_ANTENNE
AS
	SELECT * FROM SCARICO WHERE UNITA_DI_MISURA='%'
;




-- view [V_SCARICO_SOLO_GATEWAY]
DROP VIEW IF exists V_SCARICO_SOLO_GATEWAY CASCADE;

CREATE VIEW V_SCARICO_SOLO_GATEWAY
AS
	SELECT * FROM SCARICO WHERE lower(LETTURA_ATTUALE)='ext. power' AND (UNITA_DI_MISURA='' OR  UNITA_DI_MISURA IS NULL)
;




-- view [V_SCARICO_SOLO_ANTENNE_GATEWAY]
DROP VIEW IF exists V_SCARICO_SOLO_ANTENNE_GATEWAY CASCADE;

CREATE VIEW V_SCARICO_SOLO_ANTENNE_GATEWAY
AS
	SELECT * FROM SCARICO WHERE (lower(LETTURA_ATTUALE)='ext. power' AND (UNITA_DI_MISURA='' OR  UNITA_DI_MISURA IS NULL)) OR UNITA_DI_MISURA='%'
;




-- view [V_SCARICO_SOLO_ANTENNE_GATEWAY_CENSITI]
DROP VIEW IF exists V_SCARICO_SOLO_ANTENNE_GATEWAY_CENSITI CASCADE;

CREATE VIEW V_SCARICO_SOLO_ANTENNE_GATEWAY_CENSITI
AS
	SELECT * FROM SCARICO WHERE n_fabbrica_dispositivo in (select antenna from network ) and ((lower(LETTURA_ATTUALE)='ext. power' AND (UNITA_DI_MISURA='' OR  UNITA_DI_MISURA IS NULL)) OR UNITA_DI_MISURA='%')
;






-- view [V_ULTIMO_SCARICO_DATI]
DROP VIEW IF exists V_ULTIMO_SCARICO_DATI CASCADE;

CREATE VIEW V_ULTIMO_SCARICO_DATI
AS
	select n_fabbrica_dispositivo,data_di_lettura,data_anomalia,anomalia,id_azienda,id_condominio,visibile,case when visibile then 'Attivo' else 'Non attivo' end as descri_visibile, data_vis,errore_sistemato,ID_MODULO from scarico as vssr where to_timestamp(trim(replace(replace(data_di_lettura,'x',''),'--/--/--','')),'DD-MM-YYYY HH24:MI:SS')
	=(select MAX(to_timestamp(trim(replace(replace(data_di_lettura,'x',''),'--/--/--','')), 'DD-MM-YYYY HH24:MI:SS')) from scarico as vssr1 where vssr.n_fabbrica_dispositivo=vssr1.n_fabbrica_dispositivo group by n_fabbrica_dispositivo) group by n_fabbrica_dispositivo,data_di_lettura,anomalia,id_azienda,id_condominio,visibile,data_vis,errore_sistemato,ID_MODULO,data_anomalia
	order by n_fabbrica_dispositivo
;






-- view [V_ULTIMO_SCARICO_DATI_SOLO_RILEVATORI]
DROP VIEW IF exists V_ULTIMO_SCARICO_DATI_SOLO_RILEVATORI CASCADE;

CREATE VIEW V_ULTIMO_SCARICO_DATI_SOLO_RILEVATORI
AS
	select n_fabbrica_dispositivo,data_di_lettura,data_anomalia,anomalia,id_azienda,id_condominio,visibile,data_vis,errore_sistemato,ID_MODULO from v_scarico_solo_rilevatori as vssr where to_timestamp(data_di_lettura_PULITA,'DD-MM-YYYY HH24:MI:SS')
	=(select MAX(to_timestamp(data_di_lettura_PULITA, 'DD-MM-YYYY HH24:MI:SS')) from v_scarico_solo_rilevatori as vssr1 where vssr.n_fabbrica_dispositivo=vssr1.n_fabbrica_dispositivo group by n_fabbrica_dispositivo) group by n_fabbrica_dispositivo,data_di_lettura,anomalia,id_azienda,id_condominio,visibile,data_vis,errore_sistemato,ID_MODULO,data_anomalia
	order by n_fabbrica_dispositivo
;






-- view [V_ULTIMO_SCARICO_DATI_SOLO_ANTENNE]
DROP VIEW IF exists V_ULTIMO_SCARICO_DATI_SOLO_ANTENNE CASCADE;

CREATE VIEW V_ULTIMO_SCARICO_DATI_SOLO_ANTENNE
AS
	select n_fabbrica_dispositivo,data_di_lettura,data_anomalia,anomalia,id_azienda,id_condominio,visibile,data_vis,errore_sistemato,ID_MODULO from v_scarico_solo_antenne as vssr where to_timestamp(data_di_scarico||' '||ora_di_lettura,'DD-MM-YYYY HH24:MI:SS')
	=(select MAX(to_timestamp(data_di_scarico||' '||ora_di_lettura, 'DD-MM-YYYY HH24:MI:SS')) from v_scarico_solo_antenne as vssr1 where vssr.n_fabbrica_dispositivo=vssr1.n_fabbrica_dispositivo group by n_fabbrica_dispositivo) group by n_fabbrica_dispositivo,data_di_lettura,anomalia,id_azienda,id_condominio,visibile,data_vis,errore_sistemato,ID_MODULO,data_anomalia
	order by n_fabbrica_dispositivo
;





-- view [V_SCARICO_SOLO_RILEVATORI_DATA_PULITA]
DROP VIEW IF exists V_SCARICO_SOLO_RILEVATORI_DATA_PULITA CASCADE;

CREATE VIEW V_SCARICO_SOLO_RILEVATORI_DATA_PULITA
AS
	SELECT * FROM SCARICO WHERE UNITA_DI_MISURA<>'%' AND lower(LETTURA_ATTUALE)<>'ext. power'
;










DROP VIEW IF exists V_ULTIMO_SCARICO_DATI_NEW CASCADE;

CREATE VIEW V_ULTIMO_SCARICO_DATI_NEW
AS

SELECT DISTINCT ON (n_fabbrica_dispositivo)
n_fabbrica_dispositivo,data_di_lettura,data_anomalia,anomalia,id_azienda,id_condominio,visibile,case when visibile then 'Attivo' else 'Non attivo' end as descri_visibile, data_vis,errore_sistemato,ID_MODULO
FROM scarico WHERE ANOMALIA<>''
ORDER BY n_fabbrica_dispositivo, to_timestamp(trim(replace(replace(data_di_lettura,'x',''),'--/--/--','')),'DD-MM-YYYY HH24:MI:SS') DESC, to_timestamp(trim(replace(replace(DATA_ANOMALIA,'x',''),'--/--/--','')),'DD-MM-YYYY HH24:MI:SS') DESC;
;







-- view [V_ULTIMO_SCARICO_DATI_SOLO_RILEVATORI]
DROP VIEW IF exists V_ULTIMO_SCARICO_DATI_SOLO_RILEVATORI_NEW CASCADE;

CREATE VIEW V_ULTIMO_SCARICO_DATI_SOLO_RILEVATORI_NEW
AS
	
	SELECT DISTINCT ON (n_fabbrica_dispositivo)
	n_fabbrica_dispositivo,data_di_lettura,data_anomalia,anomalia,id_azienda,id_condominio,visibile,data_vis,errore_sistemato,ID_MODULO
	FROM v_scarico_solo_rilevatori where (ANOMALIA='' or anomalia is null)
	ORDER BY n_fabbrica_dispositivo, to_timestamp(trim(replace(replace(data_di_lettura,'x',''),'--/--/--','')),'DD-MM-YYYY HH24:MI:SS') DESC, to_timestamp(trim(replace(replace(DATA_ANOMALIA,'x',''),'--/--/--','')),'DD-MM-YYYY HH24:MI:SS') DESC;

;






-- view [V_ULTIMO_SCARICO_DATI_SOLO_ANTENNE]
DROP VIEW IF exists V_ULTIMO_SCARICO_DATI_SOLO_ANTENNE_NEW CASCADE;

CREATE VIEW V_ULTIMO_SCARICO_DATI_SOLO_ANTENNE_NEW
AS

	SELECT DISTINCT ON (n_fabbrica_dispositivo)
	n_fabbrica_dispositivo,data_di_lettura,data_anomalia,anomalia,id_azienda,id_condominio,visibile,data_vis,errore_sistemato,ID_MODULO 
	FROM v_scarico_solo_antenne_censite
	ORDER BY n_fabbrica_dispositivo, to_timestamp(trim(replace(replace(data_di_lettura,'x',''),'--/--/--','')),'DD-MM-YYYY HH24:MI:SS') DESC, to_timestamp(trim(replace(replace(DATA_ANOMALIA,'x',''),'--/--/--','')),'DD-MM-YYYY HH24:MI:SS') DESC;

;







-- view [V_RILEVATORI_ERRORE]
DROP VIEW IF exists V_RILEVATORI_ERRORE CASCADE;

CREATE VIEW V_RILEVATORI_ERRORE
AS
select distinct vusdsr.* 
,co.denominazione as condominio,co.indirizzo,co.localita,co.provincia,coalesce(co.sim,'') as sim
,ut.denominazione,ut.locatario,coalesce(ut.scala,'') as scala,coalesce(ut.piano,'') as piano,coalesce(ut.interno,'') as interno,coalesce(ut.n_telefono,'') as n_telefono
,coalesce(stanza,'') as stanza,case when gateway then 'Si' else 'No' end as gateway,nw.tipologia 
,co.data_prima_segnalazione,co.gg_segnalazione 
,COALESCE(pa.descrizione,'') as descri_anomalia,pa3.descrizione as colore_html 
from V_ULTIMO_SCARICO_DATI_NEW as vusdsr 
left outer join condomini as co on vusdsr.id_condominio=co.id_modulo 
left outer join utenze_dettaglio as ud on vusdsr.n_fabbrica_dispositivo=ud.rilevatore 
left outer join utenze as ut on ud.id_modulo=ut.id_modulo 
left outer join network as nw on vusdsr.n_fabbrica_dispositivo=nw.antenna 
left outer join parametri as pa on pa.codice=anomalia and pa.dominio='ERR' 
left outer join configurazione_errori as ce on ce.id_errore=pa.id_parametro 
left outer join parametri as pa2 on pa2.codice=ce.COLORE and pa2.dominio='COL' 
left outer join parametri as pa3 on pa3.codice=pa2.codice and pa3.dominio='CCO' 
;






-- view [V_DELAY_MAIL_ERRORI]
DROP VIEW IF exists V_DELAY_MAIL_ERRORI CASCADE;

CREATE VIEW V_DELAY_MAIL_ERRORI
AS
SELECT DATE_PART('day',  now() - to_date(data_prima_segnalazione,'YYYY/MM/DD'))::int as differenza,ID_MODULO,ID_AZIENDA,
CASE 
      WHEN TRENTA_GIORNI=0  THEN 30
     WHEN QUINDICI_GIORNI=0  THEN 15
     WHEN SETTE_GIORNI=0  THEN 7
     WHEN UN_GIORNO=0  THEN 1
END
AS DELAY
FROM (
select DATE_PART('day',  now() - to_date(data_prima_segnalazione,'YYYY/MM/DD'))::int as differenza,
DATE_PART('day',  now() - to_date(data_prima_segnalazione,'YYYY/MM/DD'))::int %1 as un_giorno,
DATE_PART('day',  now() - to_date(data_prima_segnalazione,'YYYY/MM/DD'))::int %7 as sette_giorni,
DATE_PART('day',  now() - to_date(data_prima_segnalazione,'YYYY/MM/DD'))::int %15 as quindici_giorni,
DATE_PART('day',  now() - to_date(data_prima_segnalazione,'YYYY/MM/DD'))::int %30 as trenta_giorni
,* from condomini
) AS TAB;






-- view [V_DELAY_MAIL_CENTRALI_TERMICHE]
DROP VIEW IF exists V_DELAY_MAIL_CENTRALI_TERMICHE CASCADE;

CREATE VIEW V_DELAY_MAIL_CENTRALI_TERMICHE
AS
SELECT DATE_PART('day',  now() - to_date(data_prima_segnalazione,'YYYY/MM/DD'))::int as differenza,ID_MODULO,ID_AZIENDA,
CASE 
      WHEN TRENTA_GIORNI=0  THEN 30
     WHEN QUINDICI_GIORNI=0  THEN 15
     WHEN SETTE_GIORNI=0  THEN 7
     WHEN UN_GIORNO=0  THEN 1
END
AS DELAY
FROM (
select DATE_PART('day',  now() - to_date(data_prima_segnalazione,'YYYY/MM/DD'))::int as differenza,
DATE_PART('day',  now() - to_date(data_prima_segnalazione,'YYYY/MM/DD'))::int %1 as un_giorno,
DATE_PART('day',  now() - to_date(data_prima_segnalazione,'YYYY/MM/DD'))::int %7 as sette_giorni,
DATE_PART('day',  now() - to_date(data_prima_segnalazione,'YYYY/MM/DD'))::int %15 as quindici_giorni,
DATE_PART('day',  now() - to_date(data_prima_segnalazione,'YYYY/MM/DD'))::int %30 as trenta_giorni
,* from ELENCO_FILE_IMPORTATI_FILE_MANAGER
) AS TAB;













-- view [V_SCARICO_1]
DROP VIEW IF exists V_SCARICO_1 CASCADE;

CREATE VIEW V_SCARICO_1
AS

select  n_fabbrica_dispositivo,anomalia,data_anomalia,DATA_DI_LETTURA,DATA_DI_LETTURA_PULITA,id_azienda,id_condominio,visibile,data_vis from v_scarico WHERE UNITA_DI_MISURA<>'%' AND lower(LETTURA_ATTUALE)<>'ext. power'
and date_part('year',now())-  date_part('year',to_date(case when length(data_di_lettura)=10 then data_di_lettura 
	else case when length(data_di_lettura)>=10 then data_di_lettura else '' end  end
,'YYYY/MM/DD'))<2	
group by n_fabbrica_dispositivo,data_di_lettura,data_anomalia,anomalia,id_azienda,id_condominio,visibile,data_vis,errore_sistemato,DATA_DI_LETTURA_PULITA;




-- view [V_SCARICO_2]
DROP VIEW IF exists V_SCARICO_2 CASCADE;

CREATE VIEW V_SCARICO_2
AS

select n_fabbrica_dispositivo,anomalia,id_azienda,id_condominio,visibile,data_vis
from v_scarico_1 as vssr where to_timestamp(data_di_lettura_PULITA,'DD-MM-YYYY HH24:MI:SS')

=(select MAX(to_timestamp(data_di_lettura_PULITA, 'DD-MM-YYYY HH24:MI:SS')) 
from v_scarico_1 as vssr1 where vssr.n_fabbrica_dispositivo=vssr1.n_fabbrica_dispositivo group by n_fabbrica_dispositivo) 
group by n_fabbrica_dispositivo,data_di_lettura,anomalia,id_azienda,id_condominio,visibile,data_vis,DATA_DI_LETTURA_PULITA
order by n_fabbrica_dispositivo;




-- view [V_DATI_RILEVATORI]
DROP VIEW IF exists V_DATI_RILEVATORI CASCADE;

CREATE VIEW V_DATI_RILEVATORI
AS

select '01/'|| LPAD(MESE::text, 2, '0') || '/' || ANNO as data_start,
ANNO || '/' || LPAD(MESE::text, 2, '0') || '/01' as data_start_ribaltata,
 * FROM DATI_RILEVATORI;
 
 
 
 



-- view [V_MONEY_1]
DROP VIEW IF exists V_MONEY_1 CASCADE;

CREATE VIEW V_MONEY_1
AS

select 
ri.id_dettaglio, 
ri.id_modulo, 
ri.id_condomino, 
ri.DENOMINAZIONE,
millesimi::numeric(10,2), 
NUMERO_RIPARTITORI, 
ri.lettura AS lettura, 
ri.lettura_acs AS lettura_acs, 
METANO_FISSO, 
METANO_MILLESIMI, 
FORZA_MOTRICE, 
CONDUZIONE, 
CONDUZIONE_ANTICIPO, 
CONDUZIONE_SALDO, 
MANUTENZIONE_ORDINARIA, 
MANUTENZIONE_STRAORDINARIA, 
COSTI_LETTURE, 
TOTALE_SENZA_LETTURE, 
TOTALE_CON_LETTURE, 
ri.METANO, 
SUBTOTALE_COSTI_MILLESIMI, 
rds.metano as metano_singolo, 
rds.lettura AS lettura_singolo, 
rds.rilevatore,rds.stanza 
from ripartizioni_dettaglio as ri 
left outer join ripartizioni_dettaglio_singolo as rds on ri.id_condomino=rds.id_condomino and ri.id_modulo=rds.id_modulo;










-- view [V_MONEY_2]
DROP VIEW IF exists V_MONEY_2 CASCADE;

CREATE VIEW V_MONEY_2
AS
select ID_CONDOMINO,DENOMINAZIONE, ri.id_modulo,
lettura, 
lettura_acs, 
millesimi_clima, 
millesimi_acs, 
CONSUMI_ENERGIA_TERMICA_RISCALDAMENTO, 
CONSUMI_ENERGIA_TERMICA_ACS, 
CONSUMO_TOTALE_EDIFICIO_ENERGIA_TERMICA_RISCALDAMENTO, 
CONSUMO_TOTALE_EDIFICIO_ENERGIA_TERMICA_ACS, 
CONSUMO_INVOLONTARIO_ENERGIA_TERMICA_ACS, 
SPESA_TOTALE_EDIFICIO_ENERGIA_TERMICA_RISCALDAMENTO, 
SPESA_TOTALE_EDIFICIO_ENERGIA_TERMICA_ACS, 
SPESA_TOTALE_EDIFICIO_POTENZA_TERMICA_RISCALDAMENTO, 
SPESA_TOTALE_EDIFICIO_POTENZA_TERMICA_ACS, 
SPESA_ENERGIA_TERMICA_RISCALDAMENTO, 
SPESA_ENERGIA_TERMICA_ACS, 
SPESA_POTENZA_TERMICA_RISCALDAMENTO, 
SPESA_POTENZA_TERMICA_ACS, 
SPESA_TOTALE_RISCALDAMENTO, 
SPESA_TOTALE_ACS, 
SPESA_TOTALE_APPARTAMENTO, 
NUMERO_RIPARTITORI::INT 
FROM RIPARTIZIONI_UNI_DETTAGLIO AS RI;






-- view [V_MONEY_3]
DROP VIEW IF exists V_MONEY_3 CASCADE;

CREATE VIEW V_MONEY_3
AS
select id_condomino,DENOMINAZIONE,ri.id_modulo,
lettura, 
lettura_acs,
lettura_afs,
millesimi_clima,
millesimi_acs,
NUMERO_RIPARTITORI::INT 
FROM RIPARTIZIONI_LETTURE_DETTAGLIO AS RI;


 


-- view [V_MONEY_3_CON_DETTAGLI]
DROP VIEW IF exists V_MONEY_3_CON_DETTAGLI CASCADE;

CREATE VIEW V_MONEY_3_CON_DETTAGLI
AS
select 
ri.id_dettaglio, 
ri.id_modulo, 
ri.id_condomino, 
ri.DENOMINAZIONE,
--substring(millesimi               ::numeric::money::text from '(([0-9]+.*)*[0-9]+)' ) AS millesimi, 
0 as millesimi,
RI.NUMERO_RIPARTITORI::int, 
1 as numero_ripartitori_per_totale,
coalesce(ri.lettura,0) as lettura, 
coalesce(ri.lettura_acs,0) as lettura_acs,
coalesce(ri.lettura_afs,0) as lettura_afs,
coalesce(rds.lettura,0) AS lettura_singolo, 
coalesce(rds.lettura_acs,0) AS lettura_acs_singolo, 
coalesce(rds.lettura_afs,0) AS lettura_afs_singolo, 
coalesce(rds.lettura,0) AS lettura_singolo_per_totale, 
coalesce(rds.lettura_acs,0) AS lettura_acs_singolo_per_totale, 
coalesce(rds.lettura_afs,0) AS lettura_afs_singolo_per_totale, 
rds.rilevatore,rds.stanza 
from ripartizioni_letture_dettaglio as ri 
left outer join ripartizioni_letture_dettaglio_singolo as rds on ri.id_condomino=rds.id_condomino and ri.id_modulo=rds.id_modulo;





-- view [V_MONEY_3_SENZA_DETTAGLI
DROP VIEW IF exists V_MONEY_3_SENZA_DETTAGLI CASCADE;

CREATE VIEW V_MONEY_3_SENZA_DETTAGLI
AS
select
ri.id_dettaglio, 
ri.id_modulo, 
ri.id_condomino, 
ri.DENOMINAZIONE,
--substring(millesimi               ::numeric::money::text from '(([0-9]+.*)*[0-9]+)' ) AS millesimi, 
0 as millesimi,
RI.NUMERO_RIPARTITORI::int, 
1 as numero_ripartitori_per_totale,
ri.lettura, 
ri.lettura_acs,
ri.lettura_afs,
sum(rds.lettura) AS lettura_singolo_per_totale
from ripartizioni_letture_dettaglio as ri 
left outer join ripartizioni_letture_dettaglio_singolo as rds on ri.id_condomino=rds.id_condomino and ri.id_modulo=rds.id_modulo
group by ri.id_dettaglio,ri.id_modulo, ri.id_condomino, ri.DENOMINAZIONE
;




















-- view [V_CONTATORI_ACQUA_CALDA]
DROP VIEW IF exists V_CONTATORI_ACQUA_CALDA CASCADE;

CREATE VIEW V_CONTATORI_ACQUA_CALDA
AS
	SELECT * FROM CONTATORI WHERE ID_TIPOLOGIA='A';



-- view [V_CONTATORI_ACQUA_CALDA]
DROP VIEW IF exists V_CONTATORI_RISCALDAMENTO CASCADE;

CREATE VIEW V_CONTATORI_RISCALDAMENTO
AS
	SELECT * FROM CONTATORI WHERE ID_TIPOLOGIA='R';


-- view [V_CONTATORI_ACQUA_CALDA]
DROP VIEW IF exists V_CONTATORI_ENERGIA_ELETTRICA CASCADE;

CREATE VIEW V_CONTATORI_ENERGIA_ELETTRICA
AS
	SELECT * FROM CONTATORI WHERE ID_TIPOLOGIA='E';


-- view [V_CONTATORI_ACQUA_CALDA]
DROP VIEW IF exists V_CONTATORI_METANO CASCADE;

CREATE VIEW V_CONTATORI_METANO
AS
	SELECT * FROM CONTATORI WHERE ID_TIPOLOGIA='M';


-- view [V_CONTATORI_ACQUA_CALDA]
DROP VIEW IF exists V_CONTATORI_CISTERNA CASCADE;

CREATE VIEW V_CONTATORI_CISTERNA
AS
	SELECT * FROM CONTATORI WHERE ID_TIPOLOGIA='C';

	
	
	
	
	
	
-- view [V_RILEVATORI_ERRORE_RIEPILOGO_MAIL]
DROP VIEW IF exists V_RILEVATORI_ERRORE_RIEPILOGO_MAIL CASCADE;

CREATE VIEW V_RILEVATORI_ERRORE_RIEPILOGO_MAIL
AS
select distinct vusdsr.* 
,co.denominazione as condominio,co.indirizzo,co.localita,co.provincia,co.sim 
,co.data_prima_segnalazione,co.gg_segnalazione 
,pa.descrizione as descri_anomalia,pa3.descrizione as colore_html 
from V_ULTIMO_SCARICO_DATI_NEW as vusdsr 
left outer join condomini as co on vusdsr.id_condominio=co.id_modulo 
left outer join parametri as pa on pa.codice=anomalia and pa.dominio='ERR' 
left outer join configurazione_errori as ce on ce.id_errore=pa.id_parametro 
left outer join parametri as pa2 on pa2.codice=ce.COLORE and pa2.dominio='COL' 
left outer join parametri as pa3 on pa3.codice=pa2.codice and pa3.dominio='CCO' 
;






-- view [V_SCARICO_SOLO_RILEVATORI_CENSITI]
DROP VIEW IF exists V_SCARICO_SOLO_RILEVATORI_CENSITI_COUNT CASCADE;

CREATE VIEW V_SCARICO_SOLO_RILEVATORI_CENSITI_COUNT
AS
	select DISTINCT(N_FABBRICA_DISPOSITIVO) as N_FABBRICA_DISPOSITIVO,ID_AZIENDA,ID_CONDOMINIO from scarico AS NUMERO where n_fabbrica_dispositivo in (select rilevatore from utenze_dettaglio) group by N_FABBRICA_DISPOSITIVO,id_azienda,id_condominio
;





DROP MATERIALIZED  VIEW IF EXISTS vista_scarico_data_massima CASCADE;

CREATE MATERIALIZED VIEW vista_scarico_data_massima AS
SELECT distinct
    --V_SCARICO_SOLO_RILEVATORI.id_modulo,
    id_azienda,
    id_condominio,
    nome_file,
    TO_CHAR(MAX(TO_DATE(data_di_lettura, 'YYYY/MM/DD')) 
        OVER (PARTITION BY id_condominio, nome_file), 'YYYY/MM/DD') AS data_di_lettura,
    n_fabbrica_dispositivo,
CASE WHEN replace(lettura_attuale, 'x x x', '0') ~ '^[0-9]+(\.[0-9]+)?$' THEN replace(lettura_attuale, 'x x x', '0')::float::int ELSE 0 END AS lettura_attuale,
CASE WHEN replace(volume_attuale, 'x x x', '0') ~ '^[0-9]+(\.[0-9]+)?$' THEN round(replace(volume_attuale, 'x x x', '0')::numeric, 2) ELSE 0.00 END AS volume_attuale, 
    CASE
        WHEN TRIM(unita_di_misura) <> '' THEN unita_di_misura
        WHEN COALESCE(NULLIF(replace(volume_attuale, 'x x x', '0'), '')::numeric, 0.00) > 0 THEN 'm3'
        WHEN COALESCE(NULLIF(replace(lettura_attuale, 'x x x', '0'), '')::numeric, 0.00) > 0 THEN 'HCA'
        ELSE NULL
    END AS unita_di_misura,
    fattore_energia,
    unita_di_misura2,
    unit_of_stat,
    volume1,
    volume2,
    date1,
    energy1,
    energy2,
    utenze_dettaglio.id_modulo as id_condomino,id_utente
FROM V_SCARICO_SOLO_RILEVATORI  
left outer join utenze_dettaglio on n_fabbrica_dispositivo=rilevatore
left outer join utenti_utenze on utenze_dettaglio.id_modulo=id_utenza
WHERE data_di_lettura ~ '^\d{4}/\d{2}/\d{2}$' and lower(nome_File) like '%.csv%';


CREATE UNIQUE INDEX idx_vista_scarico_unico ON vista_scarico_data_massima (id_condominio,nome_File,n_fabbrica_dispositivo,data_di_lettura);





-- view [V_SCARICO_SOLO_RILEVATORI_CENSITI]
DROP VIEW IF exists V_UTENZE_ID_CONDOMINO CASCADE;

CREATE VIEW V_UTENZE_ID_CONDOMINO
AS
	select id_modulo as id_condomino,id_condominio from utenze
;
