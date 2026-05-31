

DELETE FROM parametri WHERE DOMINIO ='ERR' OR (DOMINIO ='DOM' AND CODICE='ERR');
insert into parametri (dominio,codice,descrizione) values ('DOM','ERR','Errori rilevatori');
insert into parametri (dominio,codice,descrizione) values ('ERR','04','Batteria ha meno di 15 mesi di vita utile');
insert into parametri (dominio,codice,descrizione) values ('ERR','40','Hardware difettoso - errore permanente');
insert into parametri (dominio,codice,descrizione) values ('ERR','80','Fuori dalle soglie di funzionamento - errore temporaneo');
insert into parametri (dominio,codice,descrizione) values ('ERR','136','Errore permanente di comunicazione');
insert into parametri (dominio,codice,descrizione) values ('ERR','144','Errore temporaneo di comunicazione');
insert into parametri (dominio,codice,descrizione) values ('ERR','44','Batteria ha meno di 15 mesi di vita utile. Hardware difettoso - errore permanente');
insert into parametri (dominio,codice,descrizione) values ('ERR','84','Batteria ha meno di 15 mesi di vita utile. Fuori dalle soglie di funzionamento - errore temporaneo');
insert into parametri (dominio,codice,descrizione) values ('ERR','140','Batteria ha meno di 15 mesi di vita utile. Errore permanente di comunicazione');
insert into parametri (dominio,codice,descrizione) values ('ERR','148','Batteria ha meno di 15 mesi di vita utile. Errore temporaneo di comunicazione');
insert into parametri (dominio,codice,descrizione) values ('ERR','176','Hardware difettoso - errore permanente. Errore permanente di comunicazione');
insert into parametri (dominio,codice,descrizione) values ('ERR','184','Hardware difettoso - errore permanente. Errore temporaneo di comunicazione');
insert into parametri (dominio,codice,descrizione) values ('ERR','216','Fuori dalle soglie di funzionamento - errore temporaneo. Errore permanente di comunicazione');
insert into parametri (dominio,codice,descrizione) values ('ERR','208','Fuori dalle soglie di funzionamento - errore temporaneo. Errore temporaneo di comunicazione');
insert into parametri (dominio,codice,descrizione) values ('ERR','120','Ripartitore in status open');




DELETE FROM parametri WHERE DOMINIO ='TCS' OR (DOMINIO ='DOM' AND CODICE='TCS');
insert into parametri (dominio,codice,descrizione) values ('DOM','TCS','Corpo scaldante');
insert into parametri (dominio,codice,descrizione) values ('TCS','RG','Radiatore ghisa');
insert into parametri (dominio,codice,descrizione) values ('TCS','RS','Radiatore acciaio');
insert into parametri (dominio,codice,descrizione) values ('TCS','RR','Radiatore tubolare');
insert into parametri (dominio,codice,descrizione) values ('TCS','P','Piastre');
insert into parametri (dominio,codice,descrizione) values ('TCS','AL','Radiatore alluminio');
insert into parametri (dominio,codice,descrizione) values ('TCS','RK','Termoconvettore a piastre');
insert into parametri (dominio,codice,descrizione) values ('TCS','K','Termoconvettore');
insert into parametri (dominio,codice,descrizione) values ('TCS','ROR','Tubo alettato');
insert into parametri (dominio,codice,descrizione) values ('TCS','SC','Scalda salviette');





DELETE FROM parametri WHERE DOMINIO ='TCO' OR (DOMINIO ='DOM' AND CODICE='TCO');
insert into parametri (dominio,codice,descrizione) values ('DOM','TCO','Tipologia contatore');
insert into parametri (dominio,codice,descrizione) values ('TCO','A','Acqua calda');
insert into parametri (dominio,codice,descrizione) values ('TCO','R','Riscaldamento');
insert into parametri (dominio,codice,descrizione) values ('TCO','M','Metano');
insert into parametri (dominio,codice,descrizione) values ('TCO','E','Elettrico');
insert into parametri (dominio,codice,descrizione) values ('TCO','C','Cisterna');





DELETE FROM parametri WHERE DOMINIO ='TVA' OR (DOMINIO ='DOM' AND CODICE='TVA');
insert into parametri (dominio,codice,descrizione) values ('DOM','TVA','Tipo valvola');
insert into parametri (dominio,codice,descrizione) values ('TVA','DIR','Diritta');
insert into parametri (dominio,codice,descrizione) values ('TVA','SQ','Squadra');





DELETE FROM parametri WHERE DOMINIO ='POS' OR (DOMINIO ='DOM' AND CODICE='POS');
insert into parametri (dominio,codice,descrizione) values ('DOM','POS','Posizione valvola');
insert into parametri (dominio,codice,descrizione) values ('POS','DX','Destra');
insert into parametri (dominio,codice,descrizione) values ('POS','SX','Sinistra');





DELETE FROM parametri WHERE DOMINIO ='DTT' OR (DOMINIO ='DOM' AND CODICE='DTT');
insert into parametri (dominio,codice,descrizione) values ('DOM','DTT','Diametro tubo');
insert into parametri (dominio,codice,descrizione) values ('DTT','1/2','1/2"');
insert into parametri (dominio,codice,descrizione) values ('DTT','3/8','3/8"');
insert into parametri (dominio,codice,descrizione) values ('DTT','3/4','3/4"');
insert into parametri (dominio,codice,descrizione) values ('DTT','1','1"');
insert into parametri (dominio,codice,descrizione) values ('DTT','12','12"');
insert into parametri (dominio,codice,descrizione) values ('DTT','14','14"');
insert into parametri (dominio,codice,descrizione) values ('DTT','16','16"');
insert into parametri (dominio,codice,descrizione) values ('DTT','18','18"');
insert into parametri (dominio,codice,descrizione) values ('DTT','20','20"');





DELETE FROM parametri WHERE DOMINIO ='MAT' OR (DOMINIO ='DOM' AND CODICE='MAT');
insert into parametri (dominio,codice,descrizione) values ('DOM','MAT','Materiale valvola');
insert into parametri (dominio,codice,descrizione) values ('MAT','F','Ferro');
insert into parametri (dominio,codice,descrizione) values ('MAT','M','Multistrato');
insert into parametri (dominio,codice,descrizione) values ('MAT','R','Rame');





DELETE FROM parametri WHERE DOMINIO ='COL' OR (DOMINIO ='DOM' AND CODICE='COL');
insert into parametri (dominio,codice,descrizione) values ('DOM','COL','Colore evidenziatore');
insert into parametri (dominio,codice,descrizione) values ('COL','GIA','Giallo');
insert into parametri (dominio,codice,descrizione) values ('COL','ARA','Arancio');
insert into parametri (dominio,codice,descrizione) values ('COL','ROS','Rosso');
insert into parametri (dominio,codice,descrizione) values ('COL','VER','Verde');
insert into parametri (dominio,codice,descrizione) values ('COL','AZZ','Azzurro');





DELETE FROM parametri WHERE DOMINIO ='CCO' OR (DOMINIO ='DOM' AND CODICE='CCO');
insert into parametri (dominio,codice,descrizione) values ('DOM','CCO','Codifica HTML colore evidenziatore');
insert into parametri (dominio,codice,descrizione) values ('CCO','GIA','yellow');
insert into parametri (dominio,codice,descrizione) values ('CCO','ARA','orange');
insert into parametri (dominio,codice,descrizione) values ('CCO','ROS','red');
insert into parametri (dominio,codice,descrizione) values ('CCO','VER','green');
insert into parametri (dominio,codice,descrizione) values ('CCO','AZZ','lightskyblue');







DELETE FROM parametri WHERE DOMINIO ='STA' OR (DOMINIO ='DOM' AND CODICE='STA');
insert into parametri (dominio,codice,descrizione) values ('DOM','STA','Stato richiesta');
insert into parametri (dominio,codice,descrizione) values ('STA','DRA','Bozza');
insert into parametri (dominio,codice,descrizione) values ('STA','WAI','Da approvare');
insert into parametri (dominio,codice,descrizione) values ('STA','APP','Approvato');
insert into parametri (dominio,codice,descrizione) values ('STA','CLO','Chiuso');
insert into parametri (dominio,codice,descrizione) values ('STA','END','Completato');





DELETE FROM parametri WHERE DOMINIO ='DOC' OR (DOMINIO ='DOM' AND CODICE='DOC');
insert into parametri (dominio,codice,descrizione) values ('DOM','DOC','Documenti file manager');
insert into parametri (dominio,codice,descrizione) values ('DOC','001','Foto condominio');
insert into parametri (dominio,codice,descrizione) values ('DOC','002','Foto centrale termica');
insert into parametri (dominio,codice,descrizione) values ('DOC','003','Cartello centrale termica');
insert into parametri (dominio,codice,descrizione) values ('DOC','004','Contratto di nomina Terzo responsabile');
insert into parametri (dominio,codice,descrizione) values ('DOC','005','Libretto impianto');
insert into parametri (dominio,codice,descrizione) values ('DOC','006','Documenti caldaia');
insert into parametri (dominio,codice,descrizione) values ('DOC','007','Dichiarazioni di conformità');
insert into parametri (dominio,codice,descrizione) values ('DOC','008','Analisi dell''acqua');
insert into parametri (dominio,codice,descrizione) values ('DOC','009','Interventi ordinari');
insert into parametri (dominio,codice,descrizione) values ('DOC','010','Interventi straordinari');
insert into parametri (dominio,codice,descrizione) values ('DOC','011','Segnalazione amministratore');
insert into parametri (dominio,codice,descrizione) values ('DOC','012','Segnalazioni condomino');
insert into parametri (dominio,codice,descrizione) values ('DOC','013','Vigili del fuoco VVFF');
insert into parametri (dominio,codice,descrizione) values ('DOC','014','Stato pagamenti');
insert into parametri (dominio,codice,descrizione) values ('DOC','015','Consigli');




DELETE FROM parametri WHERE DOMINIO ='TPR' OR (DOMINIO ='DOM' AND CODICE='TPR');
insert into parametri (dominio,codice,descrizione) values ('DOM','TPR','Tipo ripartizione (lettura/completa)');
insert into parametri (dominio,codice,descrizione) values ('TPR','L','Sola lettura');
insert into parametri (dominio,codice,descrizione) values ('TPR','C','Completa');



DELETE FROM parametri WHERE DOMINIO ='TIC' OR (DOMINIO ='DOM' AND CODICE='TIC');
insert into parametri (dominio,codice,descrizione) values ('DOM','TIC','Tipo contabilizzazione');
insert into parametri (dominio,codice,descrizione) values ('TIC','1','Contatori di calore/Contatori volumetrici');
insert into parametri (dominio,codice,descrizione) values ('TIC','2','Ripartitori/Contatori volumetrici');
insert into parametri (dominio,codice,descrizione) values ('TIC','3','Ripartitori/NO ACS');


DELETE FROM parametri WHERE DOMINIO ='GIO' OR (DOMINIO ='DOM' AND CODICE='GIO');
insert into parametri (dominio,codice,descrizione) values ('DOM','GIO','Intervallo (giorni)');
insert into parametri (dominio,codice,descrizione) values ('GIO','1','1');
insert into parametri (dominio,codice,descrizione) values ('GIO','7','7');
insert into parametri (dominio,codice,descrizione) values ('GIO','15','15');
insert into parametri (dominio,codice,descrizione) values ('GIO','30','30');



DELETE FROM parametri WHERE DOMINIO ='CFG' OR (DOMINIO ='DOM' AND CODICE='CFG');
insert into parametri (dominio,codice,descrizione) values ('DOM','CFG','Tipo configurazione guidata');
insert into parametri (dominio,codice,descrizione) values ('CFG','000','Con importazione dati');
insert into parametri (dominio,codice,descrizione) values ('CFG','001','Manuale');





DELETE FROM parametri WHERE DOMINIO ='DUT' OR (DOMINIO ='DOM' AND CODICE='DUT');
insert into parametri (dominio,codice,descrizione) values ('DOM','DUT','Descrizione codici utenze, standard EN 1434-3 (M-Bus)');
insert into parametri (dominio,codice,descrizione) values ('DUT','14','Nodo di rete');
insert into parametri (dominio,codice,descrizione) values ('DUT','13','Calore / freddo');
insert into parametri (dominio,codice,descrizione) values ('DUT','12','Calore (mandata)');
insert into parametri (dominio,codice,descrizione) values ('DUT','11','Freddo (mandata)');
insert into parametri (dominio,codice,descrizione) values ('DUT','10','Freddo (ritorno)');
--insert into parametri (dominio,codice,descrizione) values ('DUT','8','Distributore costi di riscaldamento (HCA)');
insert into parametri (dominio,codice,descrizione) values ('DUT','8','Ripartitore HCA');
insert into parametri (dominio,codice,descrizione) values ('DUT','7','Acqua');
insert into parametri (dominio,codice,descrizione) values ('DUT','6','Contatore volumetrico (servizio ACS)');
insert into parametri (dominio,codice,descrizione) values ('DUT','4','Calore (ritorno)');
insert into parametri (dominio,codice,descrizione) values ('DUT','3','Gas');
insert into parametri (dominio,codice,descrizione) values ('DUT','2','Elettricità');
insert into parametri (dominio,codice,descrizione) values ('DUT','0','Altri');



DELETE FROM parametri WHERE DOMINIO ='UUT' OR (DOMINIO ='DOM' AND CODICE='UUT');
insert into parametri (dominio,codice,descrizione) values ('DOM','UUT','Unità di misura codici utenze, standard EN 1434-3 (M-Bus)');
insert into parametri (dominio,codice,descrizione) values ('UUT','14','%');
insert into parametri (dominio,codice,descrizione) values ('UUT','13','KWh');
insert into parametri (dominio,codice,descrizione) values ('UUT','12','KWh');
insert into parametri (dominio,codice,descrizione) values ('UUT','11','KWh');
insert into parametri (dominio,codice,descrizione) values ('UUT','10','KWh');
insert into parametri (dominio,codice,descrizione) values ('UUT','8','HCA');
insert into parametri (dominio,codice,descrizione) values ('UUT','7','m3');
insert into parametri (dominio,codice,descrizione) values ('UUT','6','m3');
insert into parametri (dominio,codice,descrizione) values ('UUT','4','KWh');
insert into parametri (dominio,codice,descrizione) values ('UUT','3','m3');
insert into parametri (dominio,codice,descrizione) values ('UUT','2','KWh');
insert into parametri (dominio,codice,descrizione) values ('UUT','0','-');


--ripartizioni 2018
DELETE FROM parametri WHERE DOMINIO ='TC8' OR (DOMINIO ='DOM' AND CODICE='TC8');
insert into parametri (dominio,codice,descrizione) values ('DOM','TC8','Tipo contabilizzazione 10200:2018');
insert into parametri (dominio,codice,descrizione) values ('TC8','1','Diretta: Contatori di calore/Contatori volumetrici');
insert into parametri (dominio,codice,descrizione) values ('TC8','2','Indiretta: Ripartitori/Contatori volumetrici');
insert into parametri (dominio,codice,descrizione) values ('TC8','3','Indiretta: Ripartitori/NO ACS');
insert into parametri (dominio,codice,descrizione) values ('TC8','4','Diretta/Indiretta: Compresenza di sistemi di contabilizzazione differenti');


DELETE FROM parametri WHERE DOMINIO ='COM' OR (DOMINIO ='DOM' AND CODICE='COM');
insert into parametri (dominio,codice,descrizione) values ('DOM','COM','Combustibile');
insert into parametri (dominio,codice,descrizione) values ('COM','1','Gasolio');
insert into parametri (dominio,codice,descrizione) values ('COM','2','Olio combustibile');
insert into parametri (dominio,codice,descrizione) values ('COM','3','Gas naturale');
insert into parametri (dominio,codice,descrizione) values ('COM','4','GPL 100% butano');
insert into parametri (dominio,codice,descrizione) values ('COM','5','GPL 30% butano e 70% propano');
insert into parametri (dominio,codice,descrizione) values ('COM','6','GPL 100% propano');
insert into parametri (dominio,codice,descrizione) values ('COM','7','Carbone');
insert into parametri (dominio,codice,descrizione) values ('COM','8','Coke');
insert into parametri (dominio,codice,descrizione) values ('COM','9','Legna da ardere (20% di umidità)');
insert into parametri (dominio,codice,descrizione) values ('COM','10','Pellet (10% di umidità)');
insert into parametri (dominio,codice,descrizione) values ('COM','11','Cippato (30% di umidità)');


DELETE FROM parametri WHERE DOMINIO ='PCI' OR (DOMINIO ='DOM' AND CODICE='PCI');
insert into parametri (dominio,codice,descrizione) values ('DOM','PCI','Valori del potere calorifico inferiore dei combustibili');
insert into parametri (dominio,codice,descrizione) values ('PCI','1','11,86');
insert into parametri (dominio,codice,descrizione) values ('PCI','2','11,47');
insert into parametri (dominio,codice,descrizione) values ('PCI','3','9,45');
insert into parametri (dominio,codice,descrizione) values ('PCI','4','32,25');
insert into parametri (dominio,codice,descrizione) values ('PCI','5','26,78');
insert into parametri (dominio,codice,descrizione) values ('PCI','6','24,44');
insert into parametri (dominio,codice,descrizione) values ('PCI','7','7,67');
insert into parametri (dominio,codice,descrizione) values ('PCI','8','8,2');
insert into parametri (dominio,codice,descrizione) values ('PCI','9','4');
insert into parametri (dominio,codice,descrizione) values ('PCI','10','4,6');
insert into parametri (dominio,codice,descrizione) values ('PCI','11','3,4');




ALTER TABLE parametri ALTER COLUMN descrizione TYPE character varying(200);

DELETE FROM parametri WHERE DOMINIO ='TDI' OR (DOMINIO ='DOM' AND CODICE='TDI');
insert into parametri (dominio,codice,descrizione,ordine) values ('DOM','TDI','Frazione del consumo involontario a piena utilizzazione - Tipo di impianto',1);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','1','Impianto a distribuzione verticale a colonne - Edificio ad un piano - stato isolamento: A',2);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','2','Impianto a distribuzione verticale a colonne - Edificio ad un piano - stato isolamento: B',3);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','3','Impianto a distribuzione verticale a colonne - Edificio ad un piano - stato isolamento: C',4);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','4','Impianto a distribuzione verticale a colonne - Edificio a due piani - stato isolamento: A',5);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','5','Impianto a distribuzione verticale a colonne - Edificio a due piani - stato isolamento: B',6);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','6','Impianto a distribuzione verticale a colonne - Edificio a due piani - stato isolamento: C',7);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','7','Impianto a distribuzione verticale a colonne - Edificio a tre piani - stato isolamento: A',8);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','8','Impianto a distribuzione verticale a colonne - Edificio a tre piani - stato isolamento: B',9);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','9','Impianto a distribuzione verticale a colonne - Edificio a tre piani - stato isolamento: C',10);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','10','Impianto a distribuzione verticale a colonne - Edificio a quattro piani ed oltre - stato isolamento: A',11);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','11','Impianto a distribuzione verticale a colonne - Edificio a quattro piani ed oltre - stato isolamento: B',12);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','12','Impianto a distribuzione verticale a colonne - Edificio a quattro piani ed oltre - stato isolamento: C',13);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','13','Impianto a distribuzione orizzontale con collettori complanari o monotubo',14);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','14','Impianto con satelliti di utenza con valvole a due vie modulanti e Delta T elevato',15);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','15','Impianto con satelliti di utenza con valvole a tre vie e regolazione on-off',16);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','16','Impianto con satelliti di utenza con valvole a due vie modulanti e Delta T elevato; produzione di acqua calda sanitaria con scambiatori collegati alla medesima rete',17);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','17','Impianto con satelliti di utenza con valvole a tre vie e regolazione on-off; produzione di acqua calda sanitaria con scambiatori collegati alla medesima rete',18);
insert into parametri (dominio,codice,descrizione,ordine) values ('TDI','18','Altro',19);



DELETE FROM parametri WHERE DOMINIO ='FHI' OR (DOMINIO ='DOM' AND CODICE='FHI');
insert into parametri (dominio,codice,descrizione) values ('DOM','FHI','Frazione del consumo involontario a piena utilizzazione - Tipo di impianto');
insert into parametri (dominio,codice,descrizione) values ('FHI','1','0.23');
insert into parametri (dominio,codice,descrizione) values ('FHI','2','0.25');
insert into parametri (dominio,codice,descrizione) values ('FHI','3','0.30');
insert into parametri (dominio,codice,descrizione) values ('FHI','4','0.22');
insert into parametri (dominio,codice,descrizione) values ('FHI','5','0.24');
insert into parametri (dominio,codice,descrizione) values ('FHI','6','0.28');
insert into parametri (dominio,codice,descrizione) values ('FHI','7','0.21');
insert into parametri (dominio,codice,descrizione) values ('FHI','8','0.23');
insert into parametri (dominio,codice,descrizione) values ('FHI','9','0.265');
insert into parametri (dominio,codice,descrizione) values ('FHI','10','0.20');
insert into parametri (dominio,codice,descrizione) values ('FHI','11','0.22');
insert into parametri (dominio,codice,descrizione) values ('FHI','12','0.25');
insert into parametri (dominio,codice,descrizione) values ('FHI','13','0.10');
insert into parametri (dominio,codice,descrizione) values ('FHI','14','0.10');
insert into parametri (dominio,codice,descrizione) values ('FHI','15','0.25');
insert into parametri (dominio,codice,descrizione) values ('FHI','16','0.35');
insert into parametri (dominio,codice,descrizione) values ('FHI','17','0.50');
insert into parametri (dominio,codice,descrizione) values ('FHI','18','0');

DELETE FROM parametri WHERE DOMINIO ='TPC' OR (DOMINIO ='DOM' AND CODICE='TPC');
insert into parametri (dominio,codice,descrizione) values ('DOM','TPC','Tipo ripartizione (previsionale/consuntiva)');
insert into parametri (dominio,codice,descrizione) values ('TPC','000','Previsionale');
insert into parametri (dominio,codice,descrizione) values ('TPC','001','Consuntiva');


DELETE FROM parametri WHERE DOMINIO ='TUT' OR (DOMINIO ='DOM' AND CODICE='TUT');
insert into parametri (dominio,codice,descrizione) values ('DOM','TUT','Classificazione utenza');
insert into parametri (dominio,codice,descrizione) values ('TUT','000','Locatario');
insert into parametri (dominio,codice,descrizione) values ('TUT','001','Locale uso collettivo');


DELETE FROM parametri WHERE DOMINIO ='TVC' OR (DOMINIO ='DOM' AND CODICE='TVC');
insert into parametri (dominio,codice,descrizione) values ('DOM','TVC','Tipo contabilizzazione (Generatore - Vettore)');
insert into parametri (dominio,codice,descrizione) values ('TVC','1','Contatore di combustibile');