
insert into parametri (dominio,codice,descrizione) values ('DOM','TAZ','Tipo Azienda');
insert into parametri (dominio,codice,descrizione) values ('TAZ','HSE','Sito QHSE');
insert into parametri (dominio,codice,descrizione) values ('TAZ','PDL','Siti PDL');
insert into parametri (dominio,codice,descrizione) values ('TAZ','BIM','BL - Industrial Merchant');
insert into parametri (dominio,codice,descrizione) values ('TAZ','BLI','BL - Large Industries');
insert into parametri (dominio,codice,descrizione) values ('TAZ','BEL','BL - Elettronica');

insert into parametri (dominio,codice,descrizione) values ('DOM','BSN','Business Line');
insert into parametri (dominio,codice,descrizione) values ('BSN','BIM','BL - Industrial Merchant');
insert into parametri (dominio,codice,descrizione) values ('BSN','BLI','BL - Large Industries');
insert into parametri (dominio,codice,descrizione) values ('BSN','BEL','BL - Elettronica');

insert into parametri (dominio,codice,descrizione) values ('DOM','ACT','Tipo Azione');
insert into parametri (dominio,codice,descrizione) values ('ACT','COR','Corretiva');
insert into parametri (dominio,codice,descrizione) values ('ACT','MIG','Miglioramento');

insert into parametri (dominio,codice,descrizione) values ('DOM','CLA','Classificazione Azione');
insert into parametri (dominio,codice,ordine,descrizione) values ('CLA','SEC',1,'Sicurezza');
insert into parametri (dominio,codice,ordine,descrizione) values ('CLA','QUA',2,'Qualita''');
insert into parametri (dominio,codice,ordine,descrizione) values ('CLA','ENV',3,'Ambiente');

insert into parametri (dominio,codice,descrizione) values ('DOM','STA','Stato Richiesta');
insert into parametri (dominio,codice,ordine,descrizione) values ('STA','WAI',1,'In attesa');
insert into parametri (dominio,codice,ordine,descrizione) values ('STA','APP',2,'Approvata');
insert into parametri (dominio,codice,ordine,descrizione) values ('STA','REJ',3,'Rifiutata');

insert into parametri (dominio,codice,descrizione) values ('DOM','SAZ','Stato Azione');
insert into parametri (dominio,codice,ordine,descrizione) values ('SAZ','OPE',1,'Aperta');
insert into parametri (dominio,codice,ordine,descrizione) values ('SAZ','CLO',2,'Completata');
insert into parametri (dominio,codice,ordine,descrizione) values ('SAZ','CHK',3,'Verificata');

insert into parametri (dominio,codice,descrizione) values ('DOM','VER','Tipo Verifica HSE');
insert into parametri (dominio,codice,descrizione) values ('VER','AUT','AUTOVERIFICA HSE DI SITO');
insert into parametri (dominio,codice,descrizione) values ('VER','VER','VERIFICA HSE DI SITO');
