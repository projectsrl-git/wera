-- TABLE TIPI_RICHIESTA
DROP TABLE IF EXISTS TIPI_RICHIESTA CASCADE;

CREATE TABLE TIPI_RICHIESTA(
 ID_TIPO_RICHIESTA SERIAL NOT NULL,
 codice_tipo_richiesta CHARACTER VARYING(30) NOT NULL,
 DESCRIZIONE CHARACTER VARYING(100) NOT NULL
)
;

-- ADD KEYS FOR TABLE TIPI_RICHIESTA

ALTER TABLE TIPI_RICHIESTA ADD CONSTRAINT PK_TIPO_RICHIESTA PRIMARY KEY (ID_TIPO_RICHIESTA)
;

ALTER TABLE TIPI_RICHIESTA ADD CONSTRAINT ID_TIPO_RICHIESTA UNIQUE (ID_TIPO_RICHIESTA)
;



-- TABLE APPROVATORI_RICHIESTE
DROP TABLE IF EXISTS APPROVATORI_RICHIESTE CASCADE;

CREATE TABLE APPROVATORI_RICHIESTE(
 ID_APPROVATORE_RICHIESTE SERIAL NOT NULL,
 ID_TIPO_RICHIESTA INTEGER NOT NULL,
 ID_UTENTE INTEGER NOT NULL,
 ID_AZIENDA INTEGER NOT NULL,
 LIVELLO_APPROVAZIONE INTEGER NOT NULL
)
;

-- ADD KEYS FOR TABLE APPROVATORI_RICHIESTE

ALTER TABLE APPROVATORI_RICHIESTE ADD CONSTRAINT FK_APPROVATORI_RICHIESTE PRIMARY KEY (ID_APPROVATORE_RICHIESTE,ID_TIPO_RICHIESTA,ID_UTENTE,ID_AZIENDA)
;

ALTER TABLE APPROVATORI_RICHIESTE ADD CONSTRAINT K_APPROVATORI_RICHIESTE UNIQUE (ID_APPROVATORE_RICHIESTE)
;


DELETE FROM TIPI_RICHIESTA;
INSERT INTO TIPI_RICHIESTA (
	codice_tipo_richiesta
	,DESCRIZIONE
) VALUES (
	'ALIMOD20'
	,'Piani d''Azione'
);


INSERT INTO APPROVATORI_RICHIESTE (
 ID_TIPO_RICHIESTA
 ,ID_UTENTE
 ,ID_AZIENDA
 ,LIVELLO_APPROVAZIONE
)
SELECT 
	(SELECT id_tipo_richiesta FROM tipi_richiesta WHERE codice_tipo_richiesta ='ALIMOD20')
	,id_utente
	,id_azienda
	,1
FROM (
SELECT DISTINCT 
	ua.id_azienda
	,ut.id_utente
	,ut.username
	,pr.codice
FROM utenti AS ut
LEFT OUTER JOIN utenti_profili AS up ON up.id_utente = ut.id_utente 
LEFT OUTER JOIN profili AS pr ON pr.id_profilo = up.id_profilo 
LEFT OUTER JOIN utenti_aziende AS ua ON ua.id_utente = ut.id_utente 
WHERE codice='APP' AND username NOT IN ('domenico.santoro@airliquide.com')
ORDER BY id_azienda
) AS t1
;

UPDATE APPROVATORI_RICHIESTE AS ar
SET livello_approvazione = 2
FROM (
SELECT DISTINCT 
	ua.id_azienda
	,ut.id_utente
	,ut.username
	,pr.codice
FROM utenti AS ut
LEFT OUTER JOIN utenti_profili AS up ON up.id_utente = ut.id_utente 
LEFT OUTER JOIN profili AS pr ON pr.id_profilo = up.id_profilo 
LEFT OUTER JOIN utenti_aziende AS ua ON ua.id_utente = ut.id_utente 
WHERE codice='APP' AND username NOT IN ('domenico.santoro@airliquide.com')
ORDER BY id_azienda
) AS t1
WHERE 
ar.id_utente=t1.id_utente 
AND ar.id_azienda=t1.id_azienda
AND t1.username IN ('') 
;			

select 
	distinct
	piva
	,codice_prodotto
	,descrizione_prodotto
	,tipologia
	,settore_analisi
	,um
FROM (
  SELECT
	ROW_NUMBER() OVER (partition BY codice_prodotto ORDER BY piva,codice_prodotto) AS r,
	t.*
  FROM
	v_inv_completo t
) x
WHERE
  x.r <= 1
  and x.codice_prodotto NOT IN (SELECT codice FROM articoli) 
;
					