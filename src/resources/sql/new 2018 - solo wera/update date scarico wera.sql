update scarico set data_di_lettura=substring(data_di_lettura,1,10);
update scarico set data_di_lettura= (select case when substring(data_di_lettura,3,1)='/' and length(data_di_lettura)=10 then substring(data_di_lettura,7)||'/'||substring(data_di_lettura,4,2)||'/'||substring(data_di_lettura,1,2) else data_di_lettura end);
update scarico set data_di_lettura='2016/12/24' where data_di_lettura='16 1/12/24';
update scarico set data_di_lettura='2017/01/18' where data_di_lettura='17 0/01/18';
update scarico set data_di_lettura='2017/01/19' where data_di_lettura='17 0/01/19';
update scarico set data_di_lettura='2017/01/20' where data_di_lettura='17 0/01/20';
update scarico set data_di_lettura='2017/01/19' where data_di_lettura='17 1/01/19';
update scarico set data_di_lettura='2017/01/20' where data_di_lettura='17 1/01/20';
update scarico set data_di_lettura='2017/01/18' where data_di_lettura='17 2/01/18';
update scarico set data_di_lettura='2017/01/19' where data_di_lettura='17 2/01/19';

update scarico set data_di_lettura='2017/09/30' where data_di_lettura='30.09.2017';
update scarico set data_di_lettura='2017/05/31' where data_di_lettura='31.05.2017';
update scarico set data_di_lettura='2018/05/31' where data_di_lettura='31.05.2018';



update scarico set DATA_INIZIO_STATISTICA=replace(DATA_INIZIO_STATISTICA,'.','/') where length(DATA_INIZIO_STATISTICA)=10;

update scarico set DATA_INIZIO_STATISTICA=substring(DATA_INIZIO_STATISTICA,1,10);
update scarico set DATA_INIZIO_STATISTICA= (select case when substring(DATA_INIZIO_STATISTICA,3,1)='/' and length(DATA_INIZIO_STATISTICA)=10 then substring(DATA_INIZIO_STATISTICA,7)||'/'||substring(DATA_INIZIO_STATISTICA,4,2)||'/'||substring(DATA_INIZIO_STATISTICA,1,2) else DATA_INIZIO_STATISTICA end);

update scarico set data_di_lettura='2015/09/30' where data_di_lettura='66.016';