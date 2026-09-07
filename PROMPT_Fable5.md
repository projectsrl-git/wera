# Prompt da incollare (Claude Fable 5 in chat, oppure Claude Code sul repo locale)

> Allega a questa conversazione: `01_wera_design_tokens.css`, `02_design_guide.md`, `03_scope_e_guardrail.md`, `04_inventario_template.md`, `CLAUDE.md`, `.claude/000-template-release.md`, `demo_riferimento_app.html`, `demo_riferimento_landing.html`.

---

Sei un ingegnere front-end senior. Devi eseguire il **restyling estetico** dell'applicazione web legacy **WERA** allineandola al nuovo design fornito negli allegati. L'app è Java (framework proprietario ALI BOW/astro), con template FreeMarker e UI basata su **Bootstrap 3 + tema INSPINIA**.

## Sorgenti di verità
- Design: `01_wera_design_tokens.css` (token) + `02_design_guide.md` (mappatura classi) + i due file `demo_riferimento_*.html` come riferimento visivo.
- Ambito, vincoli, sicurezza e consegna: `03_scope_e_guardrail.md` (**leggilo e rispettalo integralmente**).
- Superficie e ondate: `04_inventario_template.md`.
- Contesto di progetto e convenzioni: `CLAUDE.md` (**mantienilo aggiornato**).

## Precondizione (verifica e dichiara)
Il repo è pubblico e contiene segreti/dati reali. Conferma che stai lavorando su un repo **privato o su un clone sanificato**. Se trovi credenziali o dati personali, **segnalali e non riprodurli**. Non committare segreti.

## Passi
0. Clona `https://github.com/projectsrl-git/wera.git` e crea il branch `restyle/ui-2026`. Se non hai accesso in scrittura, produci un `git diff`/patch.
1. **CSS-first.** Crea `src/main/webapp/css/wera-theme.css` importando i token dell'allegato e aggiungendo gli **override** delle classi Bootstrap/INSPINIA (`.ibox*`, `.btn-primary`, `.btn-white/.btn-default`, `.form-control`, `.form-group`, `.table`, `.nav-tabs`, sidebar `#side-menu`/`.nav-second-level`, topbar/navbar, `.label/.badge`, `.footer`, `#page-wrapper/.wrapper`). Aggiungi il `<link>` a `wera-theme.css` **per ultimo** in `WEB-INF/template/include/included_head.include`, dopo `style.css`. Aggiungi i font (Bricolage Grotesque + Manrope).
2. Restylizza i **layout condivisi** in `WEB-INF/template/include/` (menu, header, footer, box_title).
3. **Ondate** per gruppi di template (vedi inventario). Intervieni solo sul markup estetico. **Preserva identici**: `${...}`, `<#...>`, `name`/`id`/`action`/`method`/`FUNCTIONID`/`OPERATION_TYPE`, gli `onclick`/handler, gli id/classi usati da jQuery e dai plugin. **Aggiungi** classi/wrapper, non rinominare ne rimuovere nulla di esistente.

## Vincoli assoluti
Non toccare: Java, `pom.xml`, `config.cfg`, SQL/`DBDataSet`, logica di business, cablaggio dei form, comportamento JS. Se un cambiamento estetico rischia di rompere la logica, **fermati e chiedi**.

## Documentazione a OGNI rilascio (obbligatoria)
Ogni rilascio di modifiche (ogni ondata / ogni set di commit) deve essere accompagnato da **tre artefatti**:
1. **Un `.md` sotto `.claude/`** — es. `.claude/2026-07-16-ondata-0.md`, seguendo il modello `.claude/000-template-release.md`: cosa e cambiato e perche, file toccati, componenti "prima -> dopo", note QA, rischi/punti aperti.
2. **Aggiornamento di `CLAUDE.md`** (radice del repo) se e cambiato qualcosa di duraturo: convenzioni, struttura, posizione del design system, regole "da non toccare", passi di build/QA.
3. **`COMMIT_MSG.txt`** (radice del repo) — messaggio di commit proposto in formato conventional, es. `restyle(ondata-0): fondamenta CSS + layout condivisi`, con corpo a punti. L'operatore committera con `git commit -F COMMIT_MSG.txt`.

## Consegna
- Il branch `restyle/ui-2026` (o patch), **senza** push forzato e senza segreti.
- I tre artefatti di documentazione qui sopra ad ogni ondata.
- `QA.md`: passi di build (`mvn clean package`, deploy WAR su Tomcat, Java 7) e checklist di smoke test, dato che non puoi eseguire tu il build Java.
- Procedi a **ondate**, non con un unico commit gigante: dopo l'ondata 0 fermati, produci i tre artefatti e mostrami un riepilogo prima di continuare.

Inizia dall'ondata 0 (CSS-first + layout condivisi), produci i tre artefatti di documentazione e poi attendi conferma.
