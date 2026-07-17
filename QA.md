# QA — build e smoke test (a carico dell'operatore)

## Build
1. Ambiente: Java 7, Maven, SQL Server (jTDS via JNDI), Tomcat.
2. `mvn clean package` → deploy del WAR su Tomcat.
3. Verificare in rete che il client raggiunga fonts.googleapis.com/fonts.gstatic.com (altrimenti self-hostare i font in `webapp/fonts/`; i fallback Georgia/system-ui restano leggibili).

## Smoke test visivo (ondata 0)
Percorso: login → home → una `ricerca*` → una `inserimento*` → `inserimentoripartizioni` (tab "Costi per unità") → configurazione guidata → profilo.

Checklist:
- [ ] Il tema si applica: sfondo #f5f4f2, card bianche arrotondate, bottoni primari a gradiente, sidebar scura con voce attiva su gradiente.
- [ ] I form si inviano ancora (login, una ricerca, un inserimento).
- [ ] DataTables: ordinamento, ricerca, paginazione funzionano; paginazione attiva colorata.
- [ ] Select2 si apre e seleziona; focus ring corallo visibile.
- [ ] Datepicker si apre; giorno selezionato in accento, "oggi" in ambra.
- [ ] Tab (`.nav-tabs`) si aprono; attivo con underline accento.
- [ ] Toastr e alert leggibili con le nuove tinte.
- [ ] Nessuna regressione layout a 1366×768 e 1920×1080; menu collassabile (hamburger) funziona.
- [ ] Footer e breadcrumb corretti.

## Rollback rapido
Rimuovere (o commentare) le 5 righe aggiunte in fondo al blocco CSS di `included_head.include`: l'app torna esattamente all'aspetto precedente.
