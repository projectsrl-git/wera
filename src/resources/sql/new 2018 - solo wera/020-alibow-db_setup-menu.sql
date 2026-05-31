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
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),10,'import_dati','astro?FUNCTIONID=ImportazioneDati','fa fa-file-excel-o');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='import_dati'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Importazione dati');


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),20,'ConfigurazioneGuidata','astro?FUNCTIONID=ConfigurazioneGuidata','fa fa-magic');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='ConfigurazioneGuidata'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Configurazione guidata');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),30,'anagrafiche','astro?FUNCTIONID=MenuAnagrafiche','fa fa-pencil-square-o');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='anagrafiche'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Anagrafiche');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),40,'RicercaLettureConsumi','astro?FUNCTIONID=RicercaLettureConsumi','fa fa-file-text-o');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaLettureConsumi'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Letture consumi');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),50,'ripartizioni','astro?FUNCTIONID=MenuRipartizioni','fa fa-calculator');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='ripartizioni'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ripartizioni');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),60,'storico','astro?FUNCTIONID=Storico','fa fa-archive');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='storico'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Storico');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),70,'gestione_rilevatori','astro?FUNCTIONID=GestioneRilevatori','fa fa-eye');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='gestione_rilevatori'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Gestione rilevatori');


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),80,'configurazione','astro?FUNCTIONID=MenuConfigurazione','fa fa-cogs');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='configurazione'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Configurazione');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),90,'utility','astro?FUNCTIONID=MenuUtility','fa fa-wrench');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='utility'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Utility');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='Home'),100,'file_manager','astro?FUNCTIONID=MenuFileManager','fa fa-list');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='file_manager'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Centrali termiche');





-- MENU 1 IMPORT DATI
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='import_dati'),1,'ImportDatiAnagraficaUtenze','astro?FUNCTIONID=ImportDatiAnagraficaUtenze','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='ImportDatiAnagraficaUtenze'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Anagrafica utenze');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='import_dati'),2,'ImportDatiRilevatori','astro?FUNCTIONID=ImportDatiRilevatori','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='ImportDatiRilevatori'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Scarico rilevatori');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='import_dati'),3,'ArchivioFileImportati','astro?FUNCTIONID=ArchivioFileImportati','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='ArchivioFileImportati'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Archivio file importati');

--INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='import_dati'),3,'RiepilogoDatiAnagraficaUtenze','astro?FUNCTIONID=RiepilogoDatiAnagraficaUtenze','');
--INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RiepilogoDatiAnagraficaUtenze'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Riepilogo importazione anagrafica utenze');





-- MENU 2 ANAGRAFICHE
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='anagrafiche'),1,'an_condomini','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='an_condomini'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Condomini');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='an_condomini'),1,'InserimentoAnagraficaCondomini','astro?FUNCTIONID=InserimentoAnagraficaCondomini','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoAnagraficaCondomini'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='an_condomini'),2,'RicercaAnagraficaCondomini','astro?FUNCTIONID=RicercaAnagraficaCondomini','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaAnagraficaCondomini'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='anagrafiche'),2,'an_antenne','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='an_antenne'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Antenne');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='an_antenne'),1,'InserimentoAnagraficaAntenne','astro?FUNCTIONID=InserimentoAnagraficaAntenne','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoAnagraficaAntenne'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='an_antenne'),2,'RicercaAnagraficaAntenne','astro?FUNCTIONID=RicercaAnagraficaAntenne','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaAnagraficaAntenne'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='anagrafiche'),3,'an_contatori','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='an_contatori'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Contatori');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='an_contatori'),1,'InserimentoAnagraficaContatori','astro?FUNCTIONID=InserimentoAnagraficaContatori','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoAnagraficaContatori'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='an_contatori'),2,'RicercaAnagraficaContatori','astro?FUNCTIONID=RicercaAnagraficaContatori','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaAnagraficaContatori'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='anagrafiche'),4,'an_utenze','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='an_utenze'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Utenze');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='an_utenze'),1,'InserimentoAnagraficaUtenze','astro?FUNCTIONID=InserimentoAnagraficaUtenze','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoAnagraficaUtenze'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='an_utenze'),2,'RicercaAnagraficaUtenze','astro?FUNCTIONID=RicercaAnagraficaUtenze','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaAnagraficaUtenze'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='anagrafiche'),5,'an_tecnici','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='an_tecnici'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Tecnici');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='an_tecnici'),1,'InserimentoAnagraficaTecnici','astro?FUNCTIONID=InserimentoAnagraficaTecnici','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoAnagraficaTecnici'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='an_tecnici'),2,'RicercaAnagraficaTecnici','astro?FUNCTIONID=RicercaAnagraficaTecnici','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaAnagraficaTecnici'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='anagrafiche'),6,'an_software','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='an_software'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Software');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='an_software'),1,'InserimentoAnagraficaSoftware','astro?FUNCTIONID=InserimentoAnagraficaSoftware','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoAnagraficaSoftware'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='an_software'),2,'RicercaAnagraficaSoftware','astro?FUNCTIONID=RicercaAnagraficaSoftware','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaAnagraficaSoftware'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');





-- MENU 3 LETTURE CONSUMI
--INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='letture_consumi'),1,'LettureConsumi','astro?FUNCTIONID=LettureConsumi','');
--INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='LettureConsumi'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Riscaldamento / ACS');

--INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='letture_consumi'),2,'LettureContatori','astro?FUNCTIONID=LettureContatori','');
--INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='LettureContatori'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Contatori');

--INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='letture_consumi'),3,'RicercaLettureConsumi','astro?FUNCTIONID=RicercaLettureConsumi','');
--INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaLettureConsumi'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Letture consumi');







-- MENU 5 RIPARTIZIONI
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='ripartizioni'),1,'ripartizioni_std','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='ripartizioni_std'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Standard');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='ripartizioni_std'),1,'InserimentoRipartizioni','astro?FUNCTIONID=InserimentoRipartizioni','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoRipartizioni'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='ripartizioni_std'),2,'RicercaRipartizioni','astro?FUNCTIONID=RicercaRipartizioni','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaRipartizioni'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='ripartizioni'),2,'ripartizioni_uni','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='ripartizioni_uni'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Uni 10200-2015');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='ripartizioni_uni'),1,'InserimentoRipartizioniUNI','astro?FUNCTIONID=InserimentoRipartizioniUNI','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoRipartizioniUNI'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='ripartizioni_uni'),2,'RicercaRipartizioniUNI','astro?FUNCTIONID=RicercaRipartizioniUNI','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaRipartizioniUNI'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');



INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='ripartizioni'),3,'ripartizioni_uni_2018','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='ripartizioni_uni_2018'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Uni 10200:2018');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='ripartizioni_uni_2018'),1,'InserimentoRipartizioniUNI2018','astro?FUNCTIONID=InserimentoRipartizioniUNI2018','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoRipartizioniUNI2018'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='ripartizioni_uni_2018'),2,'RicercaRipartizioniUNI2018','astro?FUNCTIONID=RicercaRipartizioniUNI2018','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaRipartizioniUNI2018'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');



INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='ripartizioni'),4,'ripartizioni_solo_letture','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='ripartizioni_solo_letture'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Solo letture');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='ripartizioni_solo_letture'),1,'InserimentoRipartizioniLetture','astro?FUNCTIONID=InserimentoRipartizioniLetture','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoRipartizioniLetture'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='ripartizioni_solo_letture'),2,'RicercaRipartizioniLetture','astro?FUNCTIONID=RicercaRipartizioniLetture','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaRipartizioniLetture'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');





-- MENU 6 STORICO

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='storico'),1,'RicercaLettureConsumiStorico','astro?FUNCTIONID=RicercaLettureConsumiStorico','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaLettureConsumiStorico'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Letture Consumi');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='storico'),2,'storico_ripartizioni','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='storico_ripartizioni'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ripartizioni');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='storico_ripartizioni'),1,'RicercaRipartizioniStorico','astro?FUNCTIONID=RicercaRipartizioniStorico','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaRipartizioniStorico'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Standard');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='storico_ripartizioni'),2,'RicercaRipartizioniUNIStorico','astro?FUNCTIONID=RicercaRipartizioniUNIStorico','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaRipartizioniUNIStorico'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Uni 10200-2015');

--INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='storico'),3,'RicercaScaricoStorico','astro?FUNCTIONID=RicercaScaricoStorico','');
--INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaScaricoStorico'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Scarico');



-- MENU 7 GESTIONE DATI RILEVATORI
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='gestione_rilevatori'),1,'GestioneDatiRilevatori','astro?FUNCTIONID=GestioneDatiRilevatori','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='GestioneDatiRilevatori'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Gestione dati rilevatori');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='gestione_rilevatori'),2,'GestioneRilevatoriErrore','astro?FUNCTIONID=GestioneRilevatoriErrore','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='GestioneRilevatoriErrore'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Rilevatori in errore');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='gestione_rilevatori'),3,'GestioneRilevatoriSenzaLettura','astro?FUNCTIONID=GestioneRilevatoriSenzaLettura','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='GestioneRilevatoriSenzaLettura'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Rilevatori senza lettura');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='gestione_rilevatori'),4,'GestioneRilevatoriNonCensiti','astro?FUNCTIONID=GestioneRilevatoriNonCensiti','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='GestioneRilevatoriNonCensiti'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Rilevatori non censiti');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='gestione_rilevatori'),5,'GestioneRilevatoriNonScaricati','astro?FUNCTIONID=GestioneRilevatoriNonScaricati','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='GestioneRilevatoriNonScaricati'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Rilevatori non scaricati');





-- MENU 8 CONFIGURAZIONE
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione'),1,'conf_aziende','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='conf_aziende'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Aziende');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='conf_aziende'),1,'InserimentoAziende','astro?FUNCTIONID=InserimentoAziende','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoAziende'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='conf_aziende'),2,'RicercaAziende','astro?FUNCTIONID=RicercaAziende','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaAziende'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione'),2,'conf_utenti','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='conf_utenti'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Utenti');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='conf_utenti'),1,'InserimentoUtenti','astro?FUNCTIONID=InserimentoUtenti','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoUtenti'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='conf_utenti'),2,'RicercaUtenti','astro?FUNCTIONID=RicercaUtenti','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaUtenti'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


-- domini
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione'),3,'domini','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='domini'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Tabelle');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='domini'),1,'InserimentoDomini','astro?FUNCTIONID=InserimentoDomini','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoDomini'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='domini'),2,'RicercaDomini','astro?FUNCTIONID=RicercaDomini','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaDomini'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');


-- Parametri (inizio)
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione'),4,'parametri','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='parametri'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Parametri');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='parametri'),1,'InserimentoParametri','astro?FUNCTIONID=InserimentoParametri','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoParametri'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='parametri'),2,'RicercaParametri','astro?FUNCTIONID=RicercaParametri','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaParametri'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');

-- Profili
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione'),5,'profili','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='profili'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Profili');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='profili'),1,'InserimentoProfili','astro?FUNCTIONID=InserimentoProfili','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoProfili'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='profili'),2,'RicercaProfili','astro?FUNCTIONID=RicercaProfili','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaProfili'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');






-- MailConfig
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione'),6,'mailconfig','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='mailconfig')
,(select id_lingue_iso from lingue_iso where codice_iso='it'),'Mail Config');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='mailconfig'),1,'InserimentoMailConfig','astro?FUNCTIONID=InserimentoMailConfig','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoMailConfig'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='mailconfig'),2,'RicercaMailConfig','astro?FUNCTIONID=RicercaMailConfig','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaMailConfig'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');



-- DistributionList
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione'),7,'distributionlist','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='distributionlist')
,(select id_lingue_iso from lingue_iso where codice_iso='it'),'Distribution list');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='distributionlist'),1,'InserimentoDistributionList','astro?FUNCTIONID=InserimentoDistributionList','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoDistributionList'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='distributionlist'),2,'RicercaDistributionList','astro?FUNCTIONID=RicercaDistributionList','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaDistributionList'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');



-- ConfigurazioneCsv
INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazione'),8,'configurazionecsv','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='configurazionecsv')
,(select id_lingue_iso from lingue_iso where codice_iso='it'),'Configurazione CSV');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazionecsv'),1,'InserimentoConfigurazioneCsv','astro?FUNCTIONID=InserimentoConfigurazioneCsv','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoConfigurazioneCsv'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='configurazionecsv'),2,'RicercaConfigurazioneCsv','astro?FUNCTIONID=RicercaConfigurazioneCsv','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaConfigurazioneCsv'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');











-- MENU 8 UTILITY


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='utility'),1,'ConfigurazioneErrori','astro?FUNCTIONID=ConfigurazioneErrori','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='ConfigurazioneErrori'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Configurazione Errori');


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='utility'),2,'PianificazioneScarico','astro?FUNCTIONID=PianificazioneScarico','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='PianificazioneScarico'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Pianificazione Scarico');


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='utility'),3,'uti_info_pubblicitarie','#','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='uti_info_pubblicitarie'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Informazioni pubblicitarie');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='uti_info_pubblicitarie'),1,'InserimentoNews','astro?FUNCTIONID=InserimentoNews','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='InserimentoNews'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Inserimento');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='uti_info_pubblicitarie'),2,'RicercaNews','astro?FUNCTIONID=RicercaNews','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaNews'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca');






-- MENU 10 FILE MANAGER (CENTRALI TERMICHE)


INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='file_manager'),1,'ImportFileManager','astro?FUNCTIONID=ImportFileManager','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='ImportFileManager'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Importazione file');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='file_manager'),2,'RicercaFileManager','astro?FUNCTIONID=RicercaFileManager','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='RicercaFileManager'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Ricerca file');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='file_manager'),3,'FileManager','astro?FUNCTIONID=FileManager','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='FileManager'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Visualizzazione');

INSERT INTO MENU (id_menu_sup,ordine,alias,link,icon) VALUES ((select id_menu from menu where alias ='file_manager'),4,'CalendarioFileManager','astro?FUNCTIONID=CalendarioFileManager','');
INSERT INTO MENU_LINGUE (id_menu,id_lingue_iso,descrizione) VALUES ((select id_menu from menu where alias ='CalendarioFileManager'),(select id_lingue_iso from lingue_iso where codice_iso='it'),'Calendario');







-- insert MENU_PROFILI
DELETE FROM MENU_PROFILI;


insert into menu_profili (id_menu,id_profilo) select id_menu,(select id_profilo from PROFILI where codice='ADM') from MENU;

-- menu INSTALLATORE
insert into menu_profili (id_menu,id_profilo) select id_menu,(select id_profilo from PROFILI where codice='INS') from MENU;
delete from menu_profili 
	where id_profilo=(select id_profilo from PROFILI where codice='INS')
		and 
		(
		ID_MENU=(select ID_MENU from MENU where alias='import_dati')
		or
		ID_MENU=(select ID_MENU from MENU where alias='scarico_dati')
		or
		ID_MENU=(select ID_MENU from MENU where alias='storico')
		or
		ID_MENU=(select ID_MENU from MENU where alias='anagrafiche')
		or
		ID_MENU=(select ID_MENU from MENU where alias='an_contatori')
		or
		ID_MENU=(select ID_MENU from MENU where alias='an_condomini')
		or
		ID_MENU=(select ID_MENU from MENU where alias='conf_utenti')
		or
		ID_MENU=(select ID_MENU from MENU where alias='ConfigurazioneGuidata')
		or
		ID_MENU=(select ID_MENU from MENU where alias='an_antenne')
		or
		ID_MENU=(select ID_MENU from MENU where alias='uti_info_pubblicitarie')
 		)
;

-- menu CONDOMINO
insert into menu_profili (id_menu,id_profilo) select id_menu,(select id_profilo from PROFILI where codice='CON') from MENU;
delete from menu_profili 
	where id_profilo=(select id_profilo from PROFILI where codice='CON')
		and 
		(
		ID_MENU=(select ID_MENU from MENU where alias='letture_consumi')
		or
		ID_MENU=(select ID_MENU from MENU where alias='storico')
		or
		ID_MENU=(select ID_MENU from MENU where alias='utility')				
 		)
 		
;

-- menu AMMINISTRATORE CONDOMINIO
insert into menu_profili (id_menu,id_profilo) select id_menu,(select id_profilo from PROFILI where codice='AMM') from MENU;
delete from menu_profili 
	where id_profilo=(select id_profilo from PROFILI where codice='AMM')
		and 
		(
		ID_MENU=(select ID_MENU from MENU where alias='ripartizioni')
		or
		ID_MENU=(select ID_MENU from MENU where alias='ripartizioni_std')
		or
		ID_MENU=(select ID_MENU from MENU where alias='ripartizioni_uni')
		or
		ID_MENU=(select ID_MENU from MENU where alias='ripartizioni_uni_2018')
		or
		ID_MENU=(select ID_MENU from MENU where alias='storico')
		or
		ID_MENU=(select ID_MENU from MENU where alias='an_utenze')
		or
		ID_MENU=(select ID_MENU from MENU where alias='anagrafiche')
		or
		ID_MENU=(select ID_MENU from MENU where alias='an_contatori')
		or
		ID_MENU=(select ID_MENU from MENU where alias='LettureConsumi')
		or
		ID_MENU=(select ID_MENU from MENU where alias='LettureContatori')
		or
		ID_MENU=(select ID_MENU from MENU where alias='file_manager')

 		)
;





--- UTENTI_LINGUE
DELETE FROM UTENTI_LINGUE;
ALTER SEQUENCE UTENTI_LINGUE_id_utenti_lingue_seq RESTART WITH 1;
INSERT INTO UTENTI_LINGUE (ID_UTENTE,id_lingue_iso,FL_DEFAULT) SELECT ID_UTENTE,(select id_lingue_iso from LINGUE_ISO where codice_iso='it') as id_lingue_iso,true FROM UTENTI;
