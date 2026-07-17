# <AAAA-MM-GG> — <ondata / titolo rilascio>

## Obiettivo
Cosa fa questo rilascio e perche (1-3 righe).

## File toccati
- `percorso/file` — descrizione sintetica della modifica
- ...

## Componenti: prima -> dopo
- `.ibox` : barra titolo colorata -> card pulita raggio 18px
- `.btn-primary` : blu Bootstrap -> pill gradiente brand
- ...

## Cosa NON e stato toccato (conferma)
Logica, `config.cfg`, SQL, FreeMarker `${...}`/`<#...>`, cablaggio form: invariati.

## QA
- [ ] `mvn clean package` ok
- [ ] pagine chiave verificate: login / home / ricerca* / inserimento* / ripartizioni / config guidata / profilo
- [ ] form, datatables, select2, datepicker funzionanti
- Note:

## Rischi / punti aperti
- ...
