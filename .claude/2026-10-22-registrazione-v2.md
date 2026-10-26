# 2026-10-22 — Restyle r2, ondata 3: registrazione allineata al mockup

## Obiettivo
`registrazione.html` adotta il layout di `registrazione-inquilino.html` del mockup (giro 12):
topbar scura con "Torna alla home", intestazione centrata, due colonne con la guida
"Dove trovo il codice?" (3 passi numerati + illustrazione SVG del ripartitore sul radiatore)
e la card del form.

## File toccati
- `src/main/webapp/WEB-INF/template/registrazione.html` — riscritto sul markup del mockup.
  CABLAGGIO FORM PRESERVATO IDENTICO: `action="astro" method="post" id="Registrazione"`,
  FUNCTIONID=Registrazione, campi NOME/COGNOME/EMAIL/RILEVATORE (stessi name/id/type,
  STESSO ORDINE dei parametri nel POST), submit nativo. Pagina passata dallo stack
  bootstrap/wera-theme a wera-landing.css (stesso design system della landing).
  Etichetta del campo RILEVATORE resa "Codice ripartitore" (solo testo, name/id invariati),
  hint e placeholder dal mockup. Link "Accedi" -> `astro?FUNCTIONID=Login#login` cosi il
  dialog di login si apre direttamente.
- `src/main/webapp/css/wera-landing.css` — nuova sezione REGISTRAZIONE: .topbar/.wrap/.head/
  .cols/.guide/.formcard/.illu + passi numerati SCOPED come `.guide .steps` (il mockup usa
  `.steps` nudo ma nella landing esiste gia `.pcard .steps`: lo scope evita interferenze).

## Adattamenti mockup -> gestionale (deliberati)
- SALTATI i campi PASSWORD/PASSWORD2/PROFILO/PRIVACY del mockup: il backend reale
  (FUNCTIONID=Registrazione) riceve solo NOME/COGNOME/EMAIL/RILEVATORE e la regola e non
  toccare struttura/ordine dei campi. Aggiunta la frase "Riceverai le credenziali via email"
  per spiegare l'assenza della password. Guida: passo 3 adattato (niente "crea la password").
- SALTATO il rewardbox "Guadagna con noi" (stessa policy della landing: programma reward
  senza logica reale).
- Il mockup fa redirect JS finto: qui il form fa il POST reale ad astro come sempre.

## Verifica (preview statica servita in locale)
- Mobile: topbar, chip, guida con passi numerati a cerchio gradiente, SVG, form impilato.
- Desktop: due colonne affiancate, nome/cognome in grid-2, CTA gradiente.

## QA
- [ ] registrazione reale con un codice ripartitore valido: POST invariato, esito server ok
- [ ] codice non valido: comportamento d'errore del server invariato
- [ ] "Torna alla home" e "Accedi" (il dialog si apre su #login); mobile 375px senza overflow
- [ ] campi obbligatori: validazione nativa browser (come prima del restyle)

## Rischi / punti aperti
- Il backend non rimanda ERROR_MESSAGE su questa pagina (verificato anche nell'originale
  pre-restyle): l'esito arriva dalla risposta del server (message/errore). Invariato.
- Quando esistera un flusso di registrazione amministratore/installatore, il mockup ha gia
  le pagine di riferimento (registrazione-amministratore/installatore.html).
