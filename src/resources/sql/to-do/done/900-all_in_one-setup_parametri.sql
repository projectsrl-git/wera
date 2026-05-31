DROP VIEW  IF EXISTS v_parametri cascade;
DROP VIEW  IF EXISTS v_specifiche_allestimenti cascade;
DROP VIEW  IF EXISTS v_allestimenti cascade;
DROP VIEW  IF EXISTS v_alimod50 CASCADE;
DROP VIEW  IF EXISTS v_alimod20_dettaglio CASCADE;
DROP VIEW  IF EXISTS v_alimod20 CASCADE;

ALTER TABLE PARAMETRI ALTER COLUMN DESCRIZIONE TYPE VARCHAR(100);

delete from parametri;
ALTER SEQUENCE parametri_id_parametro_seq RESTART WITH 1;

insert into parametri (dominio,codice,descrizione) values ('DOM','GAS','Componenti GAS');

insert into parametri (dominio,codice,descrizione) values ('GAS','G01','C₂H₂');
insert into parametri (dominio,codice,descrizione) values ('GAS','G02','C₂H₄');
insert into parametri (dominio,codice,descrizione) values ('GAS','G03','CO₂');
insert into parametri (dominio,codice,descrizione) values ('GAS','G04','N₂');
insert into parametri (dominio,codice,descrizione) values ('GAS','G05','O₂');
insert into parametri (dominio,codice,descrizione) values ('GAS','G06','Ar');
insert into parametri (dominio,codice,descrizione) values ('GAS','G07','He');
insert into parametri (dominio,codice,descrizione) values ('GAS','G08','H₂');
insert into parametri (dominio,codice,descrizione) values ('GAS','G09','CH₄');
insert into parametri (dominio,codice,descrizione) values ('GAS','G10','C₃H₈');
insert into parametri (dominio,codice,descrizione) values ('GAS','G11','C₃H₆');
insert into parametri (dominio,codice,descrizione) values ('GAS','G12','P.C.I.');
insert into parametri (dominio,codice,descrizione) values ('GAS','G13','P.C.S.');
insert into parametri (dominio,codice,descrizione) values ('GAS','G14','I.W.I.');
insert into parametri (dominio,codice,descrizione) values ('GAS','G15','I.W.S.');
insert into parametri (dominio,codice,descrizione) values ('GAS','G16','Densità relativa');
insert into parametri (dominio,codice,descrizione) values ('GAS','G17','CH₄H₁₀ - n');
insert into parametri (dominio,codice,descrizione) values ('GAS','G18','Xe');
insert into parametri (dominio,codice,descrizione) values ('GAS','G19','N.A.');
insert into parametri (dominio,codice,descrizione) values ('GAS','G20','N₂O');
insert into parametri (dominio,codice,descrizione) values ('GAS','G21','R134a');
insert into parametri (dominio,codice,descrizione) values ('GAS','G22','R143a');
insert into parametri (dominio,codice,descrizione) values ('GAS','G23','R125');
insert into parametri (dominio,codice,descrizione) values ('GAS','G24','R32');
insert into parametri (dominio,codice,descrizione) values ('GAS','G25','R218');
insert into parametri (dominio,codice,descrizione) values ('GAS','G26','R600');
insert into parametri (dominio,codice,descrizione) values ('GAS','G27','Pentano');
insert into parametri (dominio,codice,descrizione) values ('GAS','G28','ETO');

insert into parametri (dominio,codice,descrizione) values ('DOM','STR','Strumento di Analisi');

insert into parametri (dominio,codice,descrizione) values ('STR','S01','Buretta Claude');
insert into parametri (dominio,codice,descrizione) values ('STR','S02','EL');
insert into parametri (dominio,codice,descrizione) values ('STR','S03','IR');
insert into parametri (dominio,codice,descrizione) values ('STR','S04','PM');
insert into parametri (dominio,codice,descrizione) values ('STR','S05','EL / ZR');
insert into parametri (dominio,codice,descrizione) values ('STR','S06','DID');
insert into parametri (dominio,codice,descrizione) values ('STR','S07','AID');
insert into parametri (dominio,codice,descrizione) values ('STR','S08','EC');
insert into parametri (dominio,codice,descrizione) values ('STR','S09','IG');
insert into parametri (dominio,codice,descrizione) values ('STR','S10','TCD');
insert into parametri (dominio,codice,descrizione) values ('STR','S11','PM / ZR');
insert into parametri (dominio,codice,descrizione) values ('STR','S12','GC - TCD');
insert into parametri (dominio,codice,descrizione) values ('STR','S13','GC');
insert into parametri (dominio,codice,descrizione) values ('STR','S14','THC');

insert into parametri (dominio,codice,descrizione) values ('DOM','FQA','Frequenza di Analisi');

insert into parametri (dominio,codice,descrizione) values ('FQA','F01','1 analisi / lotto carburo');
insert into parametri (dominio,codice,descrizione) values ('FQA','F02','1 bombola / lotto');
insert into parametri (dominio,codice,descrizione) values ('FQA','F03','1 bombola / lotto solo 1°riempimento');
insert into parametri (dominio,codice,descrizione) values ('FQA','F04','1 bombola / cambio cisterna');
insert into parametri (dominio,codice,descrizione) values ('FQA','F05','Tutte le bombole');
insert into parametri (dominio,codice,descrizione) values ('FQA','F06','Tutti i pacchi');
insert into parametri (dominio,codice,descrizione) values ('FQA','F07','1 bombola /trimestre');
insert into parametri (dominio,codice,descrizione) values ('FQA','F08','1 bombola / mese');
insert into parametri (dominio,codice,descrizione) values ('FQA','F09','1 contenitore / lotto');

insert into parametri (dominio,codice,descrizione) values ('DOM','IMP','Impurezze garantite');

insert into parametri (dominio,codice,descrizione) values ('IMP','I01','N₂');
insert into parametri (dominio,codice,descrizione) values ('IMP','I02','AsH₃');
insert into parametri (dominio,codice,descrizione) values ('IMP','I03','H₂O (5 bar)');
insert into parametri (dominio,codice,descrizione) values ('IMP','I04','H₂O');
insert into parametri (dominio,codice,descrizione) values ('IMP','I05','O₂');
insert into parametri (dominio,codice,descrizione) values ('IMP','I06','CO');
insert into parametri (dominio,codice,descrizione) values ('IMP','I07','NO+NO₂');
insert into parametri (dominio,codice,descrizione) values ('IMP','I08','CnHm');
insert into parametri (dominio,codice,descrizione) values ('IMP','I09','S Totale');
insert into parametri (dominio,codice,descrizione) values ('IMP','I10','Oli');
insert into parametri (dominio,codice,descrizione) values ('IMP','I11','NVOC');
insert into parametri (dominio,codice,descrizione) values ('IMP','I12','H₂');
insert into parametri (dominio,codice,descrizione) values ('IMP','I13','CO₂');
insert into parametri (dominio,codice,descrizione) values ('IMP','I14','NO / NOx');
insert into parametri (dominio,codice,descrizione) values ('IMP','I15','Nox');
insert into parametri (dominio,codice,descrizione) values ('IMP','I16','SO₂');
insert into parametri (dominio,codice,descrizione) values ('IMP','I17','Gas rari');
insert into parametri (dominio,codice,descrizione) values ('IMP','I18','H₂S');
insert into parametri (dominio,codice,descrizione) values ('IMP','I19','CH₄');
insert into parametri (dominio,codice,descrizione) values ('IMP','I20','Kr');
insert into parametri (dominio,codice,descrizione) values ('IMP','I21','Ne');
insert into parametri (dominio,codice,descrizione) values ('IMP','I22','Incondensabili in fase vapore');
insert into parametri (dominio,codice,descrizione) values ('IMP','I23','Impurezza alto bollenti');
insert into parametri (dominio,codice,descrizione) values ('IMP','I24','Acidità');
insert into parametri (dominio,codice,descrizione) values ('IMP','I25','Alogenuri');
insert into parametri (dominio,codice,descrizione) values ('IMP','I27','Fosfuri, Solfuri, Sost. Riducenti');
insert into parametri (dominio,codice,descrizione) values ('IMP','I28','Residuo dell''evaporazione');

insert into parametri (dominio,codice,descrizione) values ('DOM','SIT','Siti');

insert into parametri (dominio,codice,descrizione) values ('SIT','GRU','Grugliasco (TO)');
insert into parametri (dominio,codice,descrizione) values ('SIT','SAN','Sannazzaro (PV)');
insert into parametri (dominio,codice,descrizione) values ('SIT','ROD','Rodano (MI)');
insert into parametri (dominio,codice,descrizione) values ('SIT','RMO','Rodano MOD');
insert into parametri (dominio,codice,descrizione) values ('SIT','CHI','Chieve (CR)');
insert into parametri (dominio,codice,descrizione) values ('SIT','TRE','Trevenzuolo (VR)');
insert into parametri (dominio,codice,descrizione) values ('SIT','TRN','Trevenzuolo NAZIONALE - ITKF');
insert into parametri (dominio,codice,descrizione) values ('SIT','PDV','Padova');
insert into parametri (dominio,codice,descrizione) values ('SIT','MOD','Modena');
insert into parametri (dominio,codice,descrizione) values ('SIT','RAV','Ravenna');
insert into parametri (dominio,codice,descrizione) values ('SIT','POR','Porto Recanati (MC)');
insert into parametri (dominio,codice,descrizione) values ('SIT','LUC','Lucca');
insert into parametri (dominio,codice,descrizione) values ('SIT','LAN','Lanuvio (RM)');
insert into parametri (dominio,codice,descrizione) values ('SIT','CAS','Caserta (CE)');
insert into parametri (dominio,codice,descrizione) values ('SIT','OST','Ostuni (BR)');
insert into parametri (dominio,codice,descrizione) values ('SIT','PRI','Priolo (SR)');
insert into parametri (dominio,codice,descrizione) values ('SIT','ASS','Assemini (CA)');

insert into parametri (dominio,codice,descrizione) values ('DOM','FAM','Famiglia Allestimento');

insert into parametri (dominio,codice,descrizione) values ('FAM','F01','OSSIGENO Industriale ');
insert into parametri (dominio,codice,descrizione) values ('FAM','F02','ACETILENE Industriale');
insert into parametri (dominio,codice,descrizione) values ('FAM','F03','DIOSSIDO DI CARBONIO industriale');
insert into parametri (dominio,codice,descrizione) values ('FAM','F04','ELIO Industriale');
insert into parametri (dominio,codice,descrizione) values ('FAM','F05','AZOTO Industriale');
insert into parametri (dominio,codice,descrizione) values ('FAM','F06','IDROGENO industriale');
insert into parametri (dominio,codice,descrizione) values ('FAM','F07','NOXAL Infiammabile');
insert into parametri (dominio,codice,descrizione) values ('FAM','F08','NOXAL Inerte');
insert into parametri (dominio,codice,descrizione) values ('FAM','F09','ARIA Industriale NON RESPIRABILE');
insert into parametri (dominio,codice,descrizione) values ('FAM','F10','ATAL industriale');
insert into parametri (dominio,codice,descrizione) values ('FAM','F11','Argon Industriale');
insert into parametri (dominio,codice,descrizione) values ('FAM','F12','SIOETIL');
insert into parametri (dominio,codice,descrizione) values ('FAM','F13','MIX AZOTO - IDROGENO INERTE');
insert into parametri (dominio,codice,descrizione) values ('FAM','F14','ALIGAL 1');
insert into parametri (dominio,codice,descrizione) values ('FAM','F15','ALIGAL 2 ');
insert into parametri (dominio,codice,descrizione) values ('FAM','F16','ALIGAL 3');
insert into parametri (dominio,codice,descrizione) values ('FAM','F17','ALIGAL 6');
insert into parametri (dominio,codice,descrizione) values ('FAM','F18','ALIGAL MIX');
insert into parametri (dominio,codice,descrizione) values ('FAM','F19','ALIGAL 49');
insert into parametri (dominio,codice,descrizione) values ('FAM','F20','ALIGAL 62');
insert into parametri (dominio,codice,descrizione) values ('FAM','F21','ARCAL 1');
insert into parametri (dominio,codice,descrizione) values ('FAM','F22','ARCAL MIX');
insert into parametri (dominio,codice,descrizione) values ('FAM','F23','ARCA MIX TP');
insert into parametri (dominio,codice,descrizione) values ('FAM','F24','ACETILENE  N26');
insert into parametri (dominio,codice,descrizione) values ('FAM','F25','OSSIGENO ALTOP');
insert into parametri (dominio,codice,descrizione) values ('FAM','F26','LASAL MIX/81');
insert into parametri (dominio,codice,descrizione) values ('FAM','F27','LASAL 1');
insert into parametri (dominio,codice,descrizione) values ('FAM','F28','LASAL 2');
insert into parametri (dominio,codice,descrizione) values ('FAM','F29','LASAL 2003');
insert into parametri (dominio,codice,descrizione) values ('FAM','F30','LASAL 83');
insert into parametri (dominio,codice,descrizione) values ('FAM','F31','N2O PURO');
insert into parametri (dominio,codice,descrizione) values ('FAM','F32','ALPHAGAZ ELIO');
insert into parametri (dominio,codice,descrizione) values ('FAM','F33','ALPHAGAZ  ARGON');
insert into parametri (dominio,codice,descrizione) values ('FAM','F34','ALPHAGAZ ARIA');
insert into parametri (dominio,codice,descrizione) values ('FAM','F35','ALPHAGAZ OSSIGENO');
insert into parametri (dominio,codice,descrizione) values ('FAM','F36','ALPHAGAZ AZOTO');
insert into parametri (dominio,codice,descrizione) values ('FAM','F37','AHG MIX Inerte');
insert into parametri (dominio,codice,descrizione) values ('FAM','F38','AHG MIX Infiammabile');
insert into parametri (dominio,codice,descrizione) values ('FAM','F39','CO₂ PURO senza TP');
insert into parametri (dominio,codice,descrizione) values ('FAM','F40','CO₂ PURO con TP');

insert into parametri (dominio,codice,descrizione) values ('DOM','ADR','Etichetta ADR');

insert into parametri (dominio,codice,descrizione) values ('ADR','BOM','Etichetta per Bombole');
insert into parametri (dominio,codice,descrizione) values ('ADR','PAC','Etichetta per pacchi');

insert into parametri (dominio,codice,descrizione) values ('DOM','ALS','Tipologia Allestimento');

insert into parametri (dominio,codice,descrizione) values ('ALS','ALT','ALTOP');
insert into parametri (dominio,codice,descrizione) values ('ALS','REG','REGULAR');
insert into parametri (dominio,codice,descrizione) values ('ALS','SMA','SMARTOP');
insert into parametri (dominio,codice,descrizione) values ('ALS','ALB','ALBEE');
insert into parametri (dominio,codice,descrizione) values ('ALS','PRE','PRESTOP');
insert into parametri (dominio,codice,descrizione) values ('ALS','PAC','PACCO');
insert into parametri (dominio,codice,descrizione) values ('ALS','SCA','SCARABEO');

delete from parametri where dominio='SPL';
insert into parametri (dominio,codice,descrizione) values ('DOM','SPL','Stato Permessi di Lavoro');
insert into parametri (dominio,codice,descrizione,ordine) values ('SPL','OPE','Aperto',1);
insert into parametri (dominio,codice,descrizione,ordine) values ('SPL','ACT','Attivo',2);
insert into parametri (dominio,codice,descrizione,ordine) values ('SPL','SUS','Sospeso',3);
insert into parametri (dominio,codice,descrizione,ordine) values ('SPL','EXP','Scaduto',4);
insert into parametri (dominio,codice,descrizione,ordine) values ('SPL','CLO','Chiuso',5);
