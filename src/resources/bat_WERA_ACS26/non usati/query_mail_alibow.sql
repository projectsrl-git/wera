COPY (
select sap,stato from checklist_agenzie order by sap asc, stato desc )
TO '/tmp/situazione_checklist_agenzie.xls' ;