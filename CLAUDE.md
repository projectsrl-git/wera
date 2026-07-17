# CLAUDE.md — WERA (contesto di progetto per Claude)

## Cos'e
WERA / WebCredit WERA: web app Java per contabilizzazione calore e ripartizione spese in condominio.
Framework proprietario ALI BOW/astro (config-driven), template FreeMarker, UI Bootstrap 3 + tema INSPINIA.
Stack: Java 7, servlet, FreeMarker, SQL Server (jTDS via JNDI), Maven -> WAR, Tomcat.

## Struttura rilevante
- Template FreeMarker: `src/main/webapp/WEB-INF/template/*.html` (84) + layout condivisi in `.../template/include/`.
- CSS/JS statici: `src/main/webapp/{css,js,fonts,img,font-awesome}`.
- Config applicativa (SQL, FUNCTIONID, routing): `src/main/webapp/WEB-INF/config/config.cfg`.

## Lavoro in corso: restyling UI (branch `restyle/ui-2026`)
- Design system: `src/main/webapp/css/wera-theme.css` (token + override classi INSPINIA/Bootstrap). Linkato per ultimo in `include/included_head.include`.
- Font: Bricolage Grotesque (display) + Manrope (UI).
- Approccio CSS-first: si ridefiniscono le classi esistenti; si intervengono i template solo dove il markup va ristrutturato.

## Regole "DA NON TOCCARE"
- Codice Java, `pom.xml`, `config.cfg`, SQL/`DBDataSet`, logica di business.
- Direttive/variabili FreeMarker: `${...}`, `<#...>`.
- Cablaggio form: `name`, `id`, `action`, `method`, `FUNCTIONID`, `OPERATION_TYPE`, handler `onclick`, id/classi usati da jQuery e plugin (datatables, select2, datepicker).
- Si AGGIUNGE (classi/wrapper), non si rinomina/rimuove nulla di esistente.

## Sicurezza (aperto)
Repo storicamente pubblico con criticita note: credenziali SMTP in chiaro in `config.cfg`, dati reali in history, SQL injection, hashing SHA-1. Rendere privato + ruotare credenziali + ripulire history. Non committare segreti.

## Build & QA
`mvn clean package` -> deploy WAR su Tomcat (Java 7). Smoke test: login, home, una ricerca*, una inserimento*, inserimentoripartizioni (tab Costi per unita), configurazione guidata, profilo.

## Convenzione di rilascio
Ogni ondata: nota in `.claude/<data>-<ondata>.md`, aggiornamento di questo `CLAUDE.md` se serve, `COMMIT_MSG.txt`. Procedere a ondate, mai un unico commit gigante.

## Stato restyling (agg. 2026-07-16 — ondata 0 completata)
- `wera-theme.css` creato con token + override (Bootstrap/INSPINIA/plugin). Linkato per ULTIMO in `included_head.include` (dopo `bow.css`), con font Google (Bricolage Grotesque + Manrope) e cache-busting `?20260716`.
- `included_header.include`: stili inline sostituiti dalle classi `topbar-user`/`topbar-exit` (definite nel tema).
- ATTENZIONE: `included_head.include` è codificato ISO-8859-1 — preservare l'encoding negli edit.
- Prossima: ondata 1 (auth & dashboard — login, registrazione, reset password, profilo, home).

## Ondata 1a (2026-07-17): login
`login.html` riscritto (solo presentazione, form/direttive identici). Stili `.auth-*` in wera-theme.css sez. 19, riusabili per registrazione/resetpassword/newpassword. login.html è UTF-8 e ha un head autonomo: il link al tema va mantenuto anche lì.
