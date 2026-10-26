# 2026-10-22 — Fix di due bug preesistenti nei template (su richiesta dell'operatore)

## Obiettivo
Correggere i due difetti trovati nello smoke test superuser, presenti dal commit iniziale.
Toccano una direttiva FreeMarker e il markup di un form: fatto SOLO su richiesta esplicita
dell'operatore in chat (2026-10-22), in deroga alla regola "DA NON TOCCARE".

## File toccati
- `configurazioneguidata1.html` r.273 — `<div class="col-sm-4"` -> `<div class="col-sm-4">`.
  Prima l'input INDIRIZZO veniva letto come attributo del div: nello step 1 della
  configurazione guidata il campo Indirizzo non esisteva (ne visibile ne inviato).
- `inserimentoripartizioni.html` r.1689, `inserimentoripartizioniuni.html` r.1698,
  `inserimentoripartizioniuni2018.html` r.2955, `inserimentoripartizioniletture.html` r.1688 —
  `<#if SALVATAGGIO_IN_CORSO?exists && SALVATAGGIO_IN_CORSO!=''>` ->
  `<#if SALVATAGGIO_IN_CORSO?exists && ((SALVATAGGIO_IN_CORSO?is_boolean && SALVATAGGIO_IN_CORSO) || (SALVATAGGIO_IN_CORSO?is_string && SALVATAGGIO_IN_CORSO!=''))>`.
  Il Java passa un Boolean (false in inserimento, il campo DB in modifica): il confronto
  boolean != '' falliva, FreeMarker (DEBUG) stampava l'errore dentro lo `<script>` e il popup
  bloccante "Salvataggio in corso..." non compariva mai. Il ramo stringa conserva la
  semantica originale nel caso il valore arrivasse come testo.
Edit byte-level (perl, contatore di unicita), UTF-8 + CRLF preservati, 5 righe in tutto.

## Cosa NON e stato toccato (conferma)
Java, `config.cfg`, CSS, altri template. Nessun name/id/action modificato: l'input
INDIRIZZO esisteva gia nel sorgente, ora il browser lo riconosce come tale.

## QA (su /wera-test, DB locale, WAR ricostruito e ridistribuito)
- [x] `mvn clean package` + `make_test_war.bat`; deploy della sola webapp wera-test
- [x] 4 inserimento ripartizioni: nessun errore FreeMarker, tutti gli script inline validi
- [x] popup "Salvataggio in corso..." (spinner, nessun bottone) su ripartizioni con
      salvataggio_in_corso=true: Standard id 233, UNI id 35, Letture id 876; assente con false
      (id 1 per ciascuno) e in inserimento
- [x] wizard step 1: campo Indirizzo visibile e obbligatorio; salvataggio di un condominio di
      prova (id 125) con indirizzo scritto sul DB e ripresentato nel form
- [x] nessun nuovo crash in `D:\usr\share\logs\wera\crash`

## Rischi / punti aperti
- UNI2018: nessuna ripartizione nel DB locale, verificato solo il caso inserimento (stessa
  espressione degli altri tre).
- Il campo Indirizzo ora e `required` nel wizard come gia previsto dal markup originale: chi
  usava il wizard senza indirizzo ora deve compilarlo.
- Le ripartizioni con salvataggio_in_corso=true rimasto appeso nei dati (1 Standard, 1 UNI,
  2 Letture nel DB locale) ora mostrano il popup bloccante e non si possono modificare
  finche il flag non torna false: e il comportamento voluto dal codice, ma va verificato in
  produzione se ci sono flag rimasti a true per errore.
