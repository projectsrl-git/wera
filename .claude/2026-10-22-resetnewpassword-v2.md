# 2026-10-22 — Restyle r2, ondata 4: resetpassword/newpassword sullo stack landing

## Obiettivo
Chiudere il percorso pubblico con un look coerente: le due pagine password passano dallo
stile `auth-*` di luglio (wera-theme + bootstrap) allo stack della landing/registrazione
(wera-landing.css): topbar scura con "Torna alla home", chip "Area riservata", titolo
Bricolage, card form a colonna singola centrata (`.onecol`).

## File toccati
- `resetpassword.html` — riscritto. CABLAGGIO INVARIATO: `astro` POST,
  FUNCTIONID=ResetPassword, campo EMAIL (stesso name/id/type). Link "Accedi" ->
  `astro?FUNCTIONID=Login#login` (apre il dialog).
- `newpassword.html` — riscritto. CABLAGGIO INVARIATO: FUNCTIONID=NewPassword, blocco
  `SESSION_TOKEN`, campi USERNAME/OLD_PASSWORD/NEW_PASSWORD/PASSWORD (stessi name/id/type e
  ORDINE; nuova/conferma affiancate in grid-2, ordine DOM identico), div
  `id=INFO_MESSAGE`/`id=ERROR_MESSAGE` con i rispettivi `<#if>` FreeMarker CONSERVATI.
  Lo script validatePassword/newPassword in head e CONSERVATO com'era: e codice morto da
  sempre (riferisce nomi campo inesistenti OLDPASSWORD/NEWPASSWORD e nessun handler lo
  invoca; il form fa submit nativo) ma la regola e non rimuovere.
  RIMOSSO SOLO il secondo blocco errore duplicato dentro il form (senza id, introdotto a
  luglio): mostrava lo stesso ERROR_MESSAGE due volte.
- `wera-landing.css` — aggiunte: `.onecol` (colonna singola 480px), `.auth-info` con
  `:empty{display:none}` (stile del div INFO_MESSAGE), topbar responsive (<420px: logo 26px,
  link no-wrap: il "Torna alla home" andava a capo).

## Verifica (preview statica servita in locale)
- Entrambe le pagine su mobile 350px e desktop: card centrata, alert nascosti quando vuoti,
  topbar su una riga.

## QA
- [ ] reset password: invio email -> esito server invariato
- [ ] newpassword: cambio password reale; messaggi INFO/ERROR del server visibili nella card
- [ ] i messaggi NON compaiono come box vuoti quando il server non li valorizza
- [ ] link: "Torna alla home" -> login; "Accedi" -> dialog; "Registrati" -> registrazione
- [ ] mobile 350-420px: topbar su una riga, campi nuova/conferma impilati

## Rischi / punti aperti
- La validazione client della nuova password resta non funzionante COME PRIMA (script morto
  conservato): le regole le applica il server. Se si vorra riattivarla, va corretta la
  mappatura dei nomi campo e agganciata al submit — intervento fuori dal perimetro estetico.
