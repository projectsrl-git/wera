delete from menu where alias in ('moduli-qhse'
,'alimod5','InserimentoALIMOD5','RicercaALIMOD5'
,'alimod20','InserimentoALIMOD20','RicercaALIMOD20'
,'alimod50','InserimentoALIMOD50','RicercaALIMOD50'
,'alimod51','InserimentoALIMOD51','RicercaALIMOD51'
,'alimod52','InserimentoALIMOD52','RicercaALIMOD52'
,'alimod67','InserimentoALIMOD67','RicercaALIMOD67'
,'alimod80','InserimentoALIMOD80','RicercaALIMOD80'
,'alimod105','InserimentoALIMOD105','RicercaALIMOD105'
,'limod13','InserimentoLIMOD13','RicercaLIMOD13'
,'limod53','InserimentoLIMOD53','RicercaLIMOD53');
 

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),13,'moduli-qhse','#','fa fa-shield');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='moduli-qhse'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Moduli QHSE');

-- 5
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='moduli-qhse'),5,'alimod5','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='alimod5')
,(select id_lingue_iso from lingue_iso where codice_iso='it'),'ALI-MOD 5 - Non conformita''');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='alimod5'),1,'InserimentoALIMOD5','astro?FUNCTIONID=InserimentoALIMOD5','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoALIMOD5'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='alimod5'),2,'RicercaALIMOD5','astro?FUNCTIONID=RicercaALIMOD5','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaALIMOD5'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


--20
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='moduli-qhse'),20,'alimod20','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='alimod20')
,(select id_lingue_iso from lingue_iso where codice_iso='it'),'ALI-MOD 20 - Piani d''Azione');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='alimod20'),1,'InserimentoALIMOD20','astro?FUNCTIONID=InserimentoALIMOD20','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoALIMOD20'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='alimod20'),2,'RicercaALIMOD20','astro?FUNCTIONID=RicercaALIMOD20','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaALIMOD20'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='moduli-qhse'),50,'alimod50','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='alimod50')
,(select id_lingue_iso from lingue_iso where codice_iso='it'),'ALI-MOD 50 - Verifica HSE');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='alimod50'),1,'InserimentoALIMOD50','astro?FUNCTIONID=InserimentoALIMOD50','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoALIMOD50'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='alimod50'),2,'RicercaALIMOD50','astro?FUNCTIONID=RicercaALIMOD50','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaALIMOD50'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='moduli-qhse'),51,'alimod51','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='alimod51'),
(select id_lingue_iso from lingue_iso where codice_iso='it'),'ALI-MOD 51 - Verb. prevenzione e protezione rischi');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='alimod51'),1,'InserimentoALIMOD51','astro?FUNCTIONID=InserimentoALIMOD51','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoALIMOD51'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='alimod51'),2,'RicercaALIMOD51','astro?FUNCTIONID=RicercaALIMOD51','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaALIMOD51'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='moduli-qhse'),52,'alimod52','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='alimod52'),
(select id_lingue_iso from lingue_iso where codice_iso='it'),'ALI-MOD 52 - Verb. riunione sicurezza');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='alimod52'),1,'InserimentoALIMOD52','astro?FUNCTIONID=InserimentoALIMOD52','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoALIMOD52'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='alimod52'),2,'RicercaALIMOD52','astro?FUNCTIONID=RicercaALIMOD52','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaALIMOD52'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='moduli-qhse'),67,'alimod67','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='alimod67'),
(select id_lingue_iso from lingue_iso where codice_iso='it'),'ALI-MOD 67 - Visita Comportamentale di Sicurezza');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='alimod67'),1,'InserimentoALIMOD67','astro?FUNCTIONID=InserimentoALIMOD67','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoALIMOD67'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='alimod67'),2,'RicercaALIMOD67','astro?FUNCTIONID=RicercaALIMOD67','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaALIMOD67'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


--ALI-MOD 80
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='moduli-qhse'),80,'alimod80','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='alimod80')
,(select id_lingue_iso from lingue_iso where codice_iso='it'),'ALI-MOD 80 - Infortuni, incidenti');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='alimod80'),1,'InserimentoALIMOD80','astro?FUNCTIONID=InserimentoALIMOD80','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoALIMOD80'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='alimod80'),2,'RicercaALIMOD80','astro?FUNCTIONID=RicercaALIMOD80','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaALIMOD80'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');



--ALI-MOD 105
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='moduli-qhse'),105,'alimod105','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='alimod105'),
(select id_lingue_iso from lingue_iso where codice_iso='it'),'ALI-MOD 105 - Modifica');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='alimod105'),1,'InserimentoALIMOD105','astro?FUNCTIONID=InserimentoALIMOD105','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoALIMOD105'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='alimod105'),2,'RicercaALIMOD105','astro?FUNCTIONID=RicercaALIMOD105','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaALIMOD105'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');

-- LI-MOD 13
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='moduli-qhse'),913,'limod13','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='limod13'),
(select id_lingue_iso from lingue_iso where codice_iso='it'),'LI-MOD 13 Check list operazioni carico automezzi');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='limod13'),1,'InserimentoLIMOD13','astro?FUNCTIONID=InserimentoLIMOD13','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoLIMOD13'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='limod13'),2,'RicercaLIMOD13','astro?FUNCTIONID=RicercaLIMOD13','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaLIMOD13'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


-- LI-MOD 53
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='moduli-qhse'),953,'limod53','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='limod53'),
(select id_lingue_iso from lingue_iso where codice_iso='it'),'LI-MOD 13 Check list operazioni carico automezzi');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='limod53'),1,'InserimentoLIMOD53','astro?FUNCTIONID=InserimentoLIMOD53','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoLIMOD53'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='limod53'),2,'RicercaLIMOD53','astro?FUNCTIONID=RicercaLIMOD53','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaLIMOD53'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');



DELETE FROM MENU_PROFILI where id_profilo = (select id_profilo from PROFILI where codice='ADM') ;
insert into menu_profili (id_menu,id_profilo) select id_menu,(select id_profilo from PROFILI where codice='ADM') from MENU;

DELETE FROM MENU_PROFILI where id_profilo in (select id_profilo from PROFILI where codice in ('QHS','CON','APP') );

insert into menu_profili (id_menu,id_profilo) 
select id_menu, id_profilo 
from menu,profili 
where codice in ('QHS','CON','APP')
and alias in (
'Home'
,'moduli-qhse'
,'alimod5','InserimentoALIMOD5','RicercaALIMOD5'
,'alimod20','InserimentoALIMOD20','RicercaALIMOD20'
,'alimod50','InserimentoALIMOD50','RicercaALIMOD50'
,'alimod51','InserimentoALIMOD51','RicercaALIMOD51'
,'alimod52','InserimentoALIMOD52','RicercaALIMOD52'
,'alimod67','InserimentoALIMOD67','RicercaALIMOD67'
,'alimod80','InserimentoALIMOD80','RicercaALIMOD80'
,'alimod105','InserimentoALIMOD105','RicercaALIMOD105'
,'limod13','InserimentoLIMOD13','RicercaLIMOD13'
,'limod53','InserimentoLIMOD53','RicercaLIMOD53');


insert into menu_profili (id_menu,id_profilo) 
select id_menu, id_profilo 
from menu,profili 
where codice in ('QHS')
and alias in (
'aziende'
,'dati_azienda'
,'InserimentoAziende'
,'RicercaAziende'
,'configurazione'
,'configurazione_utenti'
,'InserimentoUtenti'
,'RicercaUtenti'
)
;

--select * from v_menu_tree order by path;
