# WERA restyling — Ambito, vincoli e consegna

## Ambito: SOLO presentazione
Il restyling tocca esclusivamente il livello di presentazione:
1. Un nuovo foglio di stile `webapp/css/wera-theme.css` (dai token allegati) con gli override delle classi Bootstrap/INSPINIA.
2. I layout condivisi in `WEB-INF/template/include/` (head, header, menu, footer, box_title).
3. Ritocchi mirati ai singoli template `.html` solo dove il markup va ristrutturato.
4. Asset statici (font, eventuali icone/logo) in `webapp/css`, `webapp/fonts`, `webapp/img`.

## NON TOCCARE (romperebbe l'applicazione)
- Codice Java (`src/main/java/**`), `pom.xml`, dipendenze, servlet, framework ALI BOW/astro.
- `WEB-INF/config/config.cfg` e qualsiasi query SQL / `DBDataSet` / logica di business.
- Le direttive e le variabili FreeMarker: `${...}`, `<#if>`, `<#list>`, `<#include>`, ecc. → vanno **preservate identiche**.
- Il "cablaggio" dei form: attributi `name`, `id`, `action`, `method`, `FUNCTIONID`, `OPERATION_TYPE`, gli `onclick`/handler collegati al framework, gli `id`/classi usati da jQuery/plugin (datatables, select2, datepicker).
- Struttura dei campi e ordine dei parametri nei form.

Principio: **si aggiungono** classi/regole/wrapper estetici; non si rinominano né si eliminano id/classi/hook esistenti.

## PRECONDIZIONE DI SICUREZZA (bloccante)
Il repository è **pubblico** e contiene criticità già rilevate: credenziali SMTP in chiaro in `config.cfg`, dati reali di clienti nella history (`src/resources/docs/`), SQL injection, hashing SHA-1. **Prima** di dare l'intero repo a un modello (o a chiunque):
- rendere il repo **privato**, ruotare le credenziali SMTP, rimuovere i dati reali dalla history;
- oppure lavorare su un **clone sanificato** (rimossi `src/resources/docs/` e i segreti).
Se durante il lavoro emergono segreti o dati personali, vanno **segnalati e non riprodotti/estratti**.

## Consegna attesa
- Un branch `restyle/ui-2026` (oppure un `git diff`/patch se non c'è accesso in scrittura). **Nessun push forzato**, nessun commit di segreti.
- `CHANGES.md`: elenco file toccati, con per ciascun componente "prima → dopo" e le classi override introdotte.
- Note di build/QA per l'operatore umano (vedi sotto).

## Build & QA (a carico umano — il modello non esegue il build Java)
Ambiente: Java 7, Maven, packaging WAR, SQL Server (jTDS via JNDI).
1. `mvn clean package` → deploy del WAR su Tomcat.
2. Smoke test visivo delle pagine chiave in ondate (vedi inventario): login → home → una `ricerca*` → una `inserimento*` → `inserimentoripartizioni` (tab Costi per unità) → configurazione guidata → profilo.
3. Verificare che: i form si inviino ancora, i datatables/select2/datepicker funzionino, i grafici e i tab si aprano, non ci siano regressioni di layout su risoluzioni tipiche.

## Strategia consigliata: a ONDATE (non un unico commit)
1. Fondamenta: `wera-theme.css` + link in `included_head.include` + `included_menu`/`included_header`/`included_footer`.
2. Verifica visiva → poi override componenti (ibox/btn/form/table/tabs).
3. Ondate per gruppo di template (auth → dashboard → anagrafiche → ricerca → ripartizioni → config guidata → resto), validando a ogni ondata.

## Convenzione di documentazione ad ogni rilascio (obbligatoria)
Ogni rilascio/ondata produce SEMPRE tre artefatti versionati insieme al codice:
1. `.claude/<AAAA-MM-GG>-<ondata>.md` — nota di rilascio (usa `.claude/000-template-release.md`): cosa/perche, file toccati, "prima -> dopo", QA, rischi/aperti.
2. `CLAUDE.md` (radice) — aggiornato se cambia qualcosa di duraturo (convenzioni, struttura, design system, regole "non toccare", build/QA).
3. `COMMIT_MSG.txt` (radice) — messaggio di commit proposto (conventional), da usare con `git commit -F COMMIT_MSG.txt`.

Comando di commit tipo per ondata:
```
git add -A
git commit -F COMMIT_MSG.txt      # include codice + .claude/*.md + CLAUDE.md
git push
```
