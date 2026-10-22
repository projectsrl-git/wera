# 2026-10-22 — Smoke test superuser ondate 0-6 su /wera-test (DB locale)

## Obiettivo
Punto 3 dell'handoff r2: collaudare l'app interna completa (fase 1 + r2) su questa istanza,
mai fatto prima con il merge completo.

## File toccati
- `QA.md` — sezione "Smoke test superuser ondate 0-6" con esiti, bug preesistenti, limiti.
- Nessun file applicativo modificato.

## Metodo
1. Giro HTTP di tutte le 53 voci di menu del superuser + Profilo + ScaricoDatiManuale:
   stato, titolo, presenza di wera-theme.css, testo di errore FreeMarker nell'HTML.
2. Verifiche interattive nel browser per ondata (tab, select2, datepicker, ricerca in linea,
   ricalcolo totali, summernote, calendario, dropzone) e due salvataggi reali in UPDATE
   (condominio, parametro) sul DB locale di pasticcio.
3. Layout a 1366x768 su home e inserimento ripartizioni.
4. Crash log in `D:\usr\share\logs\wera\crash` controllato dopo ogni blocco.

## Esito
Nessuna regressione attribuibile al restyle. Dettaglio in QA.md.

## Bug preesistenti trovati (non corretti, da decidere con l'operatore)
- `configurazioneguidata1.html` r.273: `<div class="col-sm-4"` senza `>`: il campo Indirizzo
  dello step 1 del wizard non esiste. Fix banale ma attiva un campo `required` (ondata 4:
  massima prudenza) -> saltato.
- `SALVATAGGIO_IN_CORSO!=''` (booleano vs stringa) nei 4 template inserimento ripartizioni:
  FreeMarker DEBUG stampa l'errore dentro lo script e il popup "Salvataggio in corso..."
  non compare mai. Fix = cambiare una direttiva FreeMarker (DA NON TOCCARE senza ok).

## Non verificato
- Intestazioni Riscaldamento/ACS UNI2018 (0 ripartizioni UNI2018 nel DB locale).
- Letture consumi e storico letture (vista `vista_scarico_data_massima` assente nel DB locale).
- Import reali da dropzone e ScaricoDatiManuale (non inviati: riscrivono file di configurazione).
- Larghezza 1920x1080 (pannello di test limitato).

## Rischi / punti aperti
- DB locale: query lente (home ~10 s, rilevatori non scaricati ~50 s): non indicative della
  produzione.
