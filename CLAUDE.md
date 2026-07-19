# CLAUDE.md — WERA (contesto di progetto per Claude)

## Cos'e
WERA / WebCredit WERA: web app Java per contabilizzazione calore e ripartizione spese in condominio.
Framework proprietario ALI BOW/astro (config-driven), template FreeMarker, UI Bootstrap 3 + tema INSPINIA.
Stack: Java 7, servlet, FreeMarker, SQL Server (jTDS via JNDI), Maven -> WAR, Tomcat.

## Struttura rilevante
- Template FreeMarker: `src/main/webapp/WEB-INF/template/*.html` (84) + layout condivisi in `.../template/include/`.
- CSS/JS statici: `src/main/webapp/{css,js,fonts,img,font-awesome}`.
- Config applicativa (SQL, FUNCTIONID, routing): `src/main/webapp/WEB-INF/config/config.cfg`.
- Le cartelle `template/non usate` e `css/non usati` sono codice morto: NON toccarle.

## Riferimenti design (in repo)
Tutto in `.claude/design/`: `01_wera_design_tokens.css` (token, sorgente di verita),
`02_design_guide.md` (mappatura classi), `03_scope_e_guardrail.md` (ambito/vincoli/consegna,
DA RISPETTARE INTEGRALMENTE), `04_inventario_template.md` (ondate), `demo_riferimento_app.html`
e `demo_riferimento_landing.html` (riferimento visivo).

## Restyling UI (branch `restyle/ui-2026`) — stato
- Design system: `src/main/webapp/css/wera-theme.css` (token + override INSPINIA/Bootstrap/plugin,
  sezioni numerate 1-26). Linkato per ULTIMO in `include/included_head.include`; le pagine
  standalone errore/errorpage/message lo linkano direttamente nell'head (+ Google Fonts).
  IMPORTANTE: a OGNI modifica del tema aggiornare il cache-buster `?AAAAMMGG` del link in
  `included_head.include` (e nelle standalone), altrimenti i browser servono la copia in cache.
- Landing/auth: `src/main/webapp/css/wera-landing.css` (solo login.html, pagina standalone).
- Font: Bricolage Grotesque (display) + Manrope (UI), via Google Fonts.
- COMPLETATE TUTTE le ondate 0-6 (2026-07-19). Ondate 2-6 quasi interamente CSS-only:
  sez. 21 anagrafiche (export ghost, legenda chip, banda titolo), sez. 22 ripartizioni
  (input compatti in griglia, alert stato), sez. 23 config guidata (jquery.steps pill,
  progressBar, dropzone), sez. 24 import/file (file-box, progress, FullCalendar #calendar),
  sez. 25 varie (summernote, pagine middle-box). Unici template toccati dopo l'ondata 1:
  errore/errorpage/message (soli <link> additivi + rimozione margin-left legacy dal logo).
- ESCLUSI di proposito: mail.html (template EMAIL con hack MSO: non riskinnare);
  recolor intestazioni Riscaldamento/ACS in inserimentoripartizioniuni2018 (color coding
  funzionale applicato anche via JS in rowCallback).
- Approccio CSS-first: si ridefiniscono le classi esistenti; si interviene sui template solo dove
  il markup va ristrutturato (stili inline in conflitto, wrapper).

## Regole "DA NON TOCCARE"
- Codice Java, `pom.xml`, `config.cfg`, SQL/`DBDataSet`, logica di business.
- Direttive/variabili FreeMarker: `${...}`, `<#...>` — preservate identiche.
- Cablaggio form: `name`, `id`, `action`, `method`, `FUNCTIONID`, `OPERATION_TYPE`, handler `onclick`,
  id/classi usati da jQuery e plugin (datatables, select2, datepicker, amcharts, jstree, metisMenu).
- Si AGGIUNGE (classi/wrapper), non si rinomina/rimuove nulla di esistente.
- Nei template di ripartizioni (ondata 3) e config guidata (ondata 4): massima prudenza,
  preferire override CSS puri; in dubbio saltare il punto, annotarlo nella nota di rilascio, proseguire.

## Encoding (IMPORTANTE)
File misti: `home.html` e gli `include/*.include` sono ISO-8859-1; login/registrazione/resetpassword/
newpassword/profilo sono UTF-8. Verificare con `file` PRIMA di editare e PRESERVARE l'encoding
originale (mai risalvare ISO-8859-1 come UTF-8: `git diff` mostrerebbe il file riscritto per intero).

## Sicurezza (aperto)
Repo storicamente pubblico con criticita note: credenziali SMTP in chiaro in `config.cfg`, dati reali
in history, SQL injection, hashing SHA-1. Rendere privato + ruotare credenziali + ripulire history
(`git filter-repo`). Non committare segreti; se emergono segreti o dati personali, segnalarli e non riprodurli.

## Build & QA
`mvn clean package` -> deploy WAR su Tomcat (Java 7). Il build Java e a carico dell'operatore umano.
Smoke test per ondata: login, home, una ricerca*, una inserimento*, inserimentoripartizioni
(tab Costi per unita), configurazione guidata, profilo. Checklist complete in `QA.md`.

## Convenzione di rilascio (obbligatoria, ogni ondata)
1. Nota in `.claude/<AAAA-MM-GG>-<ondata>.md` (modello: `.claude/000-template-release.md`).
2. Aggiornamento di questo `CLAUDE.md` se cambia qualcosa di duraturo.
3. `COMMIT_MSG.txt` (conventional) -> `git commit -F COMMIT_MSG.txt`.
Procedere a ondate, mai un unico commit gigante.

## Run autonomo in Claude Code (ondate 2-6)
Procedere SENZA attendere conferme: per OGNI ondata produrre i tre artefatti e committare
con `git commit -F COMMIT_MSG.txt` prima di passare alla successiva. Un commit per ondata.
NON pushare: il push lo fa l'operatore a fine run. Fermarsi SOLO se una modifica estetica
rischia di toccare logica/cablaggio: in quel caso saltare il punto, annotarlo nella nota di
rilascio e proseguire. A fine run: aggiornare `QA.md` e produrre un riepilogo unico
(file toccati per ondata, punti saltati, rischi).
