-- LINGUE_ISO
DELETE FROM LINGUE_ISO;
ALTER SEQUENCE LINGUE_ISO_id_lingue_iso_seq RESTART WITH 1;
INSERT INTO LINGUE_ISO (codice_iso,lingua) VALUES ('it','Italiano');
INSERT INTO LINGUE_ISO (codice_iso,lingua) VALUES ('en','English');

--  MENU
DELETE FROM MENU;
ALTER SEQUENCE MENU_id_menu_seq RESTART WITH 1;
-- NOTA IMPORTANTE: se la voce di menu corrisponde ad un link effettivo ad una function, usa come campo "alias" il nome stesso della function (es: InserimentoFeriePermessi)

-- MENU 0
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES (null,0,'Home','astro?FUNCTIONID=Home','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='Home'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Home');

-- MENU 1
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),10,'richieste','#','fa fa-pencil-square-o');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='richieste'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Richieste');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),15,'pdl','#','fa fa-pencil-square-o');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='pdl'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Permessi di Lavoro');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),16,'registri','#','fa fa-list-alt');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='registri'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Registri');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),19,'interferenze','#','fa fa-bolt');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='interferenze'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Gestione Interferenze');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),20,'documenti','#','fa fa-file-text-o');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='documenti'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Documenti');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),25,'Workbook','astro?FUNCTIONID=Workbook','fa fa-book');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='Workbook'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Workbook');

DELETE FROM MENU WHERE alias in ('ReportPDL');
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),27,'ReportPDL','astro?FUNCTIONID=ReportPDL','fa fa-columns');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='ReportPDL'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Report');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),30,'anagrafiche','#','fa fa-users');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='anagrafiche'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Anagrafiche');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),50,'aziende','#','fa fa-industry');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='aziende'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Siti-Aziende');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),60,'configurazione','#','fa fa-cogs');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='configurazione'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Configurazione');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),70,'comunicazioni','#','fa fa-envelope-o');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='comunicazioni'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Comunicazioni');



-- MENU 2 richieste > configurazione_tipi_richieste
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='richieste'),100,'configurazione_tipi_richieste','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='configurazione_tipi_richieste'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Tipi Richiesta');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione_tipi_richieste'),1,'InserimentoTipiRichiesta','astro?FUNCTIONID=InserimentoTipiRichiesta','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoTipiRichiesta'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento Tipi Richiesta');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione_tipi_richieste'),2,'RicercaTipiRichiesta','astro?FUNCTIONID=RicercaTipiRichiesta','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaTipiRichiesta'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca Tipi Richiesta');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione_tipi_richieste'),3,'InserimentoTipiRichiestaAzienda','astro?FUNCTIONID=InserimentoTipiRichiestaAzienda','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoTipiRichiestaAzienda'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Abbinamento Tipi Richiesta - Azienda');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione_tipi_richieste'),4,'RicercaTipiRichiestaAzienda','astro?FUNCTIONID=RicercaTipiRichiestaAzienda','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaTipiRichiestaAzienda'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca Tipi Richiesta - Azienda');

-- MENU 2 richieste > configurazione_approvatori_richieste
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='richieste'),110,'configurazione_approvatori_richieste','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='configurazione_approvatori_richieste'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Approvatori Richiesta');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione_approvatori_richieste'),1,'InserimentoApprovatoriRichiesta','astro?FUNCTIONID=InserimentoApprovatoriRichiesta','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoApprovatoriRichiesta'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento Approvatori Richiesta');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione_approvatori_richieste'),2,'RicercaApprovatoriRichiesta','astro?FUNCTIONID=RicercaApprovatoriRichiesta','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaApprovatoriRichiesta'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca Approvatori Richiesta');


-- MENU PDL
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='pdl'),10,'InserimentoPDL','astro?FUNCTIONID=InserimentoPDL','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoPDL'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Crea nuovo');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='pdl'),20,'RicercaPDL','astro?FUNCTIONID=RicercaPDL','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaPDL'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca - Modifica');
update MENU_LINGUE set descrizione='Ricerca - Modifica' where id_menu = (select id_menu from menu where alias ='RicercaPDL');

-- MENU REGISTRI
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='registri'),10,'RegistroGiornalieroLavori','astro?FUNCTIONID=RegistroGiornalieroLavori','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RegistroGiornalieroLavori'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Registro Giornaliero Lavori');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='registri'),20,'RegistroPermessiScaduti','astro?FUNCTIONID=RegistroPermessiScaduti','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RegistroPermessiScaduti'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Registro Permessi Scaduti');

-- MENU INTERFERENZE
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='interferenze'),10,'InserimentoInterferenze','astro?FUNCTIONID=InserimentoInterferenze','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoInterferenze'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Segnalazione possibile interferenza');


-- MENU Documenti
-- MENU Documenti / Cataloghi Prodotti Gas
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='documenti'),10,'SchedaProdotto','astro?FUNCTIONID=SchedaProdotto','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='SchedaProdotto'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Scheda Prodotto Gas');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='documenti'),30,'cataloghi_all_in_one','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='cataloghi_all_in_one'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Cataloghi Prodotti Gas');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='cataloghi_all_in_one'),1,'ManualeProdottiCondizionati','astro?FUNCTIONID=ManualeProdottiCondizionati','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='ManualeProdottiCondizionati'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Manuale Prodotti Condizionati');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='cataloghi_all_in_one'),2,'CatalogoAllestimento','astro?FUNCTIONID=CatalogoAllestimento','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='CatalogoAllestimento'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Catalogo Allestimento');


-- MENU 3 ALIMOD20 - Piani d'Azione
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='documenti'),40,'master_table','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='master_table'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Master Table');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='master_table'),1,'MasterTableALIMOD20','astro?FUNCTIONID=MasterTableALIMOD20','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='MasterTableALIMOD20'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'ALIMOD20 - Piani d''Azione');



-- MENU Anagrafiche


-- MENU Anagrafiche / Prodotti Gas
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='anagrafiche'),10,'prodotti_gas','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='prodotti_gas'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Prodotti Gas');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='prodotti_gas'),1,'InserimentoFamigliaProdottiGas','astro?FUNCTIONID=InserimentoFamigliaProdottiGas','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoFamigliaProdottiGas'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='prodotti_gas'),2,'RicercaProdottiGas','astro?FUNCTIONID=RicercaProdottiGas','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaProdottiGas'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


-- MENU Anagrafiche / Allestimenti
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='anagrafiche'),20,'allestimenti','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='allestimenti'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Allestimenti');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='allestimenti'),1,'InserimentoAllestimento','astro?FUNCTIONID=InserimentoAllestimento','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoAllestimento'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='allestimenti'),2,'RicercaAllestimenti','astro?FUNCTIONID=RicercaAllestimenti','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaAllestimenti'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');

-- capi_turno
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='anagrafiche'),30,'capi_turno','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='capi_turno'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Capi turno');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='capi_turno'),1,'InserimentoCapiTurno','astro?FUNCTIONID=InserimentoCapiTurno','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoCapiTurno'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='capi_turno'),2,'RicercaCapiTurno','astro?FUNCTIONID=RicercaCapiTurno','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaCapiTurno'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


-- delegato_lavori
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='anagrafiche'),31,'delegato_lavori','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='delegato_lavori'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Delegato lavori');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='delegato_lavori'),1,'InserimentoDelegatoLavori','astro?FUNCTIONID=InserimentoDelegatoLavori','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoDelegatoLavori'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='delegato_lavori'),2,'RicercaDelegatoLavori','astro?FUNCTIONID=RicercaDelegatoLavori','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaDelegatoLavori'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');

-- siti
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='anagrafiche'),32,'siti','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='siti'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Siti');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='siti'),1,'InserimentoSiti','astro?FUNCTIONID=InserimentoSiti','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoSiti'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='siti'),2,'RicercaSiti','astro?FUNCTIONID=RicercaSiti','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaSiti'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


-- impianti
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='anagrafiche'),35,'impianti','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='impianti'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Impianti');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='impianti'),1,'InserimentoImpianti','astro?FUNCTIONID=InserimentoImpianti','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoImpianti'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='impianti'),2,'RicercaImpianti','astro?FUNCTIONID=RicercaImpianti','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaImpianti'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


-- personale_interno
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='anagrafiche'),40,'personale_interno','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='personale_interno'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Personale interno');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='personale_interno'),1,'InserimentoPersonaleInterno','astro?FUNCTIONID=InserimentoPersonaleInterno','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoPersonaleInterno'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='personale_interno'),2,'RicercaPersonaleInterno','astro?FUNCTIONID=RicercaPersonaleInterno','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaPersonaleInterno'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');

-- responsabile_centrale
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='anagrafiche'),50,'responsabile_centrale','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='responsabile_centrale'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Responsabile Centrale/Delegato');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='responsabile_centrale'),1,'InserimentoResponsabileCentrale','astro?FUNCTIONID=InserimentoResponsabileCentrale','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoResponsabileCentrale'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='responsabile_centrale'),2,'RicercaResponsabileCentrale','astro?FUNCTIONID=RicercaResponsabileCentrale','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaResponsabileCentrale'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');

-- lista_equipment
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='anagrafiche'),60,'lista_equipment','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='lista_equipment'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Lista Equipment');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='lista_equipment'),1,'InserimentoListaEquipment','astro?FUNCTIONID=InserimentoListaEquipment','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoListaEquipment'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='lista_equipment'),2,'RicercaListaEquipment','astro?FUNCTIONID=RicercaListaEquipment','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaListaEquipment'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');

-- ditte_terze
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='anagrafiche'),70,'ditte_terze','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='ditte_terze'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ditte terze');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='ditte_terze'),1,'InserimentoDitteTerze','astro?FUNCTIONID=InserimentoDitteTerze','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoDitteTerze'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='ditte_terze'),2,'RicercaDitteTerze','astro?FUNCTIONID=RicercaDitteTerze','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaDitteTerze'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');




-- MENU Aziende
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='aziende'),1,'dati_azienda','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='dati_azienda'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Dati Sito-Azienda');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='dati_azienda'),1,'InserimentoAziende','astro?FUNCTIONID=InserimentoAziende','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoAziende'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='dati_azienda'),2,'RicercaAziende','astro?FUNCTIONID=RicercaAziende','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaAziende'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='aziende'),3,'aziende_direzioni','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='aziende_direzioni'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Direzioni');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='aziende_direzioni'),1,'InserimentoDirezioni','astro?FUNCTIONID=InserimentoDirezioni','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoDirezioni'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='aziende_direzioni'),2,'RicercaDirezioni','astro?FUNCTIONID=RicercaDirezioni','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaDirezioni'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


-- MENU 6 Configurazione

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione'),10,'configurazione_utenti','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='configurazione_utenti'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Utenti');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione_utenti'),1,'InserimentoUtenti','astro?FUNCTIONID=InserimentoUtenti','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoUtenti'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione_utenti'),2,'RicercaUtenti','astro?FUNCTIONID=RicercaUtenti','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaUtenti'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');

-- domini
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione'),19,'domini','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='domini'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Tabelle');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='domini'),1,'InserimentoDomini','astro?FUNCTIONID=InserimentoDomini','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoDomini'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='domini'),2,'RicercaDomini','astro?FUNCTIONID=RicercaDomini','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaDomini'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


-- Parametri (inizio)
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione'),20,'parametri','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='parametri'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Parametri');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='parametri'),1,'InserimentoParametri','astro?FUNCTIONID=InserimentoParametri','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoParametri'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='parametri'),2,'RicercaParametri','astro?FUNCTIONID=RicercaParametri','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaParametri'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');

-- Profili
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione'),30,'profili','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='profili'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Profili');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='profili'),1,'InserimentoProfili','astro?FUNCTIONID=InserimentoProfili','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoProfili'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='profili'),2,'RicercaProfili','astro?FUNCTIONID=RicercaProfili','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaProfili'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');

-- Lingue iso (inizio)
delete from menu where alias in ('lingue_iso','InserimentoLingueIso','RicercaLingueIso');
delete from MENU_LINGUE where id_menu in (select id_menu from menu where alias in ('lingue_iso','InserimentoLingueIso','RicercaLingueIso'));

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione'),40,'lingue_iso','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='lingue_iso'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Lingue ISO');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='lingue_iso'),1,'InserimentoLingueIso','astro?FUNCTIONID=InserimentoLingueIso','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoLingueIso'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='lingue_iso'),2,'RicercaLingueIso','astro?FUNCTIONID=RicercaLingueIso','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaLingueIso'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');
-- Lingue iso (fine)


-- MENU 7 Comunicazioni

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='comunicazioni'),1,'dati_news','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='dati_news'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'News');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='dati_news'),1,'InserimentoNews','astro?FUNCTIONID=InserimentoNews','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoNews'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='dati_news'),2,'RicercaNews','astro?FUNCTIONID=RicercaNews','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaNews'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');
