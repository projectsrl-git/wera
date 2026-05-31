update MENU_LINGUE set descrizione = 'Gestione Siti' where id_menu =(select id_menu from menu where alias ='aziende') and id_lingue_iso = (select id_lingue_iso from lingue_iso where codice_iso='it');
update MENU_LINGUE set descrizione = 'Siti' where id_menu =(select id_menu from menu where alias ='dati_azienda') and id_lingue_iso = (select id_lingue_iso from lingue_iso where codice_iso='it');

delete from MENU where alias in ('ditte_terze','InserimentoDitteTerze','RicercaDitteTerze','stato_permesso','InserimentoStatoPermesso','RicercaStatoPermesso','ferie_permessi','InserimentoFeriePermessi','RicercaFeriePermessi');

-- ditte_terze
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='anagrafiche'),70,'ditte_terze','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='ditte_terze'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ditte terze');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='ditte_terze'),1,'InserimentoDitteTerze','astro?FUNCTIONID=InserimentoDitteTerze','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoDitteTerze'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='ditte_terze'),2,'RicercaDitteTerze','astro?FUNCTIONID=RicercaDitteTerze','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaDitteTerze'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');



-- assegna menu a ADM
DELETE FROM MENU_PROFILI where id_profilo = (select id_profilo from PROFILI where codice='ADM') ;
insert into menu_profili (id_menu,id_profilo) select id_menu,(select id_profilo from PROFILI where codice='ADM') from MENU;