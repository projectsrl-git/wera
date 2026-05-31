delete from menu_profili where id_menu in (select id_menu from menu where alias in ('mailconfig','InserimentoMailConfig','RicercaMailConfig','distributionlist','InserimentoDistributionList','RicercaDistributionList','masterdistributionlist','logemail'))
;

delete from menu where alias in ('mailconfig','InserimentoMailConfig','RicercaMailConfig','distributionlist','InserimentoDistributionList','RicercaDistributionList','masterdistributionlist','logemail')
;


-- MailConfig
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione'),50,'mailconfig','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='mailconfig')
,(select id_lingue_iso from lingue_iso where codice_iso='it'),'Mail Config');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='mailconfig'),1,'InserimentoMailConfig','astro?FUNCTIONID=InserimentoMailConfig','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoMailConfig'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='mailconfig'),2,'RicercaMailConfig','astro?FUNCTIONID=RicercaMailConfig','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaMailConfig'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');



-- DistributionList
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione'),60,'distributionlist','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='distributionlist')
,(select id_lingue_iso from lingue_iso where codice_iso='it'),'Distribution list');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='distributionlist'),1,'InserimentoDistributionList','astro?FUNCTIONID=InserimentoDistributionList','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoDistributionList'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='distributionlist'),2,'RicercaDistributionList','astro?FUNCTIONID=RicercaDistributionList','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaDistributionList'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


-- MasterDistributionList
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione'),70,'masterdistributionlist','astro?FUNCTIONID=MasterDistributionList','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='masterdistributionlist')
,(select id_lingue_iso from lingue_iso where codice_iso='it'),'Master distribution list');


-- LogEmail
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione'),80,'logemail','astro?FUNCTIONID=LogEmail','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='logemail')
,(select id_lingue_iso from lingue_iso where codice_iso='it'),'Log email');


insert into menu_profili (id_menu,id_profilo) select id_menu,(select id_profilo from PROFILI where codice='ADM') from MENU 
where alias in ('mailconfig','InserimentoMailConfig','RicercaMailConfig','distributionlist','InserimentoDistributionList','RicercaDistributionList','masterdistributionlist','logemail')
;


