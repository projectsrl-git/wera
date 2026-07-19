# WERA — Guida di stile (target del restyling)

Riferimento visivo: i due file `demo_riferimento_app.html` e `demo_riferimento_landing.html` allegati. I token esatti sono in `01_wera_design_tokens.css` (da riusare come sorgente unica di verità).

## Palette
- Sfondo app: `#f5f4f2` · superfici/card: `#ffffff` · superficie secondaria: `#faf9f7` · bordi: `#e8e6e1`
- Testo: `#16181d` (primario), `#737a86` (muted)
- Accento (brand "calore"): gradiente `linear-gradient(100deg,#f9a825,#fb6340 55%,#e8453c)`; tinta piena accento `#fb6340`, testo-accento `#b8331f`
- Sidebar/topbar scuri: `#1b1e24` / `#22262e`, testo sidebar `#aeb4bf`
- Stati: verde/valido `#1d9e75` (ink `#0f6e56`), ambra/in-scadenza `#e8821e`, rosso/scaduto-errore `#e24b4a`

## Tipografia
- Titoli/display: **Bricolage Grotesque** (700/800)
- Testo/UI: **Manrope** (400–800)
- Caricare via Google Fonts oppure self-host in `webapp/fonts/`.

## Forme
- Raggi: 9px (piccolo), 13px (base), 18px (grande), 24px (xl)
- Ombra card: `0 22px 48px -26px rgba(22,24,29,.4)` (usare con parsimonia)

## Mappatura classi esistenti → nuovo look (RE-SKIN CSS-FIRST)
L'app usa Bootstrap 3 + tema INSPINIA. Il 70–80% del restyling si ottiene **ridefinendo queste classi** nel nuovo foglio di stile, senza toccare i markup:

| Classe attuale (Bootstrap/INSPINIA) | Occorrenze | Nuovo aspetto |
|---|---|---|
| `.ibox` / `.ibox-title` / `.ibox-content` | ~320 | Card bianca, raggio 18px, bordo `--line`, header pulito senza barra colorata |
| `.btn-primary` | 211 | Bottone gradiente "pill" (`--grad`), testo bianco, raggio 9px |
| `.btn-white` / `.btn-default` | ~40 | Bottone ghost: bianco, bordo `--line`, testo `--ink-2` |
| `.form-control` | 578 | Input morbido: sfondo `--surface-2`, bordo `--line`, focus accento |
| `.form-group` | 339 | Label piccola maiuscoletta muted + spaziatura coerente |
| `.table` | 348 | Tabella pulita: header `--surface-2`, righe con bordo `--line`, hover leggero |
| `.nav-tabs` | 12 | Tab a "pill" con stato attivo su gradiente/accento |
| `.navbar` / topbar | — | Barra chiara compatta; brand a sinistra |
| menu laterale (`.nav-second-level`, `#side-menu`) | — | Rail scuro `#1b1e24`, voci con icona, attivo su gradiente |
| `.label` / `.badge` | — | Chip di stato (verde/ambra/rosso) come nel demo |
| `.footer` | 28 | Footer minimale, testo muted |
| `.wrapper` / `#page-wrapper` | 81 | Sfondo `--bg`, spaziatura contenuti |

Regola d'oro: **aggiungere** regole/override e classi, **non rinominare né rimuovere** le classi e gli id esistenti (li usano JS e framework).

## Componenti nuovi presenti nel demo (facoltativi, come riferimento)
Alberatura utenze navigabile, mappa condomìni, forecast, scadenziario Centrale Termica con calendario, badge crediti, switch white-label. Non sono richiesti dal restyling puramente estetico, ma i loro stili (card, tab, tabelle, chip) sono già nel foglio token e vanno riusati per coerenza.
