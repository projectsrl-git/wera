/* ============================================================
   WERA 2026 - Centrale termica, configurazione (centraletermicaconfigurazione.html)
   ------------------------------------------------------------
   Dati: astro?FUNCTIONID=CentraleTermicaDati&OPERAZIONE=config. Salvataggi: POST
   CentraleTermicaConfigurazioneSalva (OGGETTO = sezione | tipo | parametro | categoria).
   Il server controlla profilo, valori e aziende: qui solo presentazione. ES5 + jQuery 2, ASCII.
   ============================================================ */
(function ($) {
    'use strict';

    var API = 'astro';
    var TIPI_SEZIONE = [['DOCUMENTI', 'Documenti (cartella)'], ['FOTO', 'Foto (scheda impianto)'], ['RISERVATA', 'Riservata']];
    var cfg = null, salvataggio = null;

    function esc(v) {
        if (v === null || v === undefined) { return ''; }
        return String(v).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }

    function avviso(testo, ok) {
        var $a = $('#ct-avviso');
        if (!testo) { $a.addClass('hidden').empty(); return; }
        $a.removeClass('hidden ct-avviso-ok ct-avviso-ko').addClass(ok ? 'ct-avviso-ok' : 'ct-avviso-ko').text(testo);
        if (ok) { window.setTimeout(function () { $a.addClass('hidden'); }, 2500); }
    }

    function sessioneScaduta() { window.location.href = API + '?FUNCTIONID=Logout'; }

    function carica() {
        $.ajax({ url: API, data: { FUNCTIONID: 'CentraleTermicaDati', OPERAZIONE: 'config' }, dataType: 'text', cache: false })
            .done(function (t) {
                var j;
                try { j = JSON.parse(t); } catch (e) { sessioneScaduta(); return; }
                if (j.esito !== 'OK') { avviso(j.messaggio); return; }
                cfg = j.config;
                disegnaSezioni();
                disegnaTipi();
                disegnaParametri();
                disegnaCategorie();
            })
            .fail(function (xhr) {
                var m = 'Errore di comunicazione';
                try { m = JSON.parse(xhr.responseText).messaggio || m; } catch (e) { /* non JSON */ }
                avviso(m);
            });
    }

    /** POST di salvataggio; successo: messaggio, ricarica e callback. */
    function salva(param, fatto, errore) {
        $.ajax({ url: API + '?FUNCTIONID=CentraleTermicaConfigurazioneSalva', type: 'POST', data: param, dataType: 'text' })
            .done(function (t) {
                var j;
                try { j = JSON.parse(t); } catch (e) { sessioneScaduta(); return; }
                if (j.esito === 'OK') { avviso('Salvato.', true); carica(); if (fatto) { fatto(j); } }
                else if (errore) { errore(j.messaggio); } else { avviso(j.messaggio); }
            })
            .fail(function (xhr) {
                var m = 'Salvataggio non riuscito';
                try { m = JSON.parse(xhr.responseText).messaggio || m; } catch (e) { /* non JSON */ }
                if (errore) { errore(m); } else { avviso(m); }
            });
    }

    function si(v) { return v === true ? '<i class="fa fa-check ct-ico-ok" aria-label="si"></i>' : ''; }

    function aziendaTxt(r) { return r.ID_AZIENDA ? esc(r.RAGSOC || ('azienda ' + r.ID_AZIENDA)) : '<span class="ct-muto">tutte</span>'; }

    function opzioni(lista, valore) {
        var h = '';
        $.each(lista, function (i, o) {
            h += '<option value="' + esc(o[0]) + '"' + (String(o[0]) === String(valore === null || valore === undefined ? '' : valore) ? ' selected' : '') +
                '>' + esc(o[1]) + '</option>';
        });
        return h;
    }

    function opzioniSezioni(conVuota, idAzienda) {
        var l = conVuota ? [['', 'Non mostrare']] : [];
        $.each(cfg.sezioni, function (i, s) {
            if (!s.ID_AZIENDA || !idAzienda || s.ID_AZIENDA === idAzienda) {
                l.push([s.ID_SEZIONE, s.TITOLO + (s.FL_DISATTIVO ? ' (disattiva)' : '')]);
            }
        });
        return l;
    }

    function opzioniAziende(conTutte) {
        var l = conTutte ? [['', 'Tutte le aziende']] : [];
        $.each(cfg.aziende, function (i, a) { l.push([a.ID_AZIENDA, a.RAGSOC]); });
        return l;
    }

    // ------------------------------------------------------------------ tabelle

    function disegnaSezioni() {
        var testa = '<thead><tr><th>Sezione</th><th class="hidden-xs">Tipo</th><th class="hidden-xs">Azienda</th>';
        $.each(cfg.profili, function (i, p) { testa += '<th class="text-center" title="' + esc(p.DESCRIZIONE) + '">' + esc(p.CODICE) + '</th>'; });
        testa += '<th class="text-center hidden-xs">Scad.</th><th class="text-center hidden-xs">Doc.</th><th></th></tr></thead>';
        var corpo = '<tbody>';
        $.each(cfg.sezioni, function (i, s) {
            corpo += '<tr' + (s.FL_DISATTIVO ? ' class="ctc-spenta"' : '') + '><td><div class="ct-doc"><i class="fa ' + esc(s.ICONA || 'fa-folder') +
                ' ct-doc-ico" aria-hidden="true"></i><div><div class="ct-doc-titolo">' + esc(s.TITOLO) +
                (s.FL_DISATTIVO ? ' <span class="ct-chip ct-chip-neutro">disattiva</span>' : '') + '</div><div class="ct-doc-file">' +
                esc(s.CODICE) + ' \u00b7 ordine ' + esc(s.ORDINE) + '</div></div></div></td>' +
                '<td class="hidden-xs">' + esc(s.TIPO) + '</td><td class="hidden-xs">' + aziendaTxt(s) + '</td>';
            $.each(cfg.profili, function (k, p) {
                var l = (s.PERMESSI || {})[p.CODICE] || '-';
                corpo += '<td class="text-center"><span class="ctc-perm ctc-perm-' + (l === '-' ? 'no' : l.toLowerCase()) + '">' +
                    (l === 'RW' ? 'LS' : (l === 'R' ? 'L' : '\u2013')) + '</span></td>';
            });
            corpo += '<td class="text-center hidden-xs">' + si(s.FL_SCADENZA_RICHIESTA) + '</td><td class="text-center hidden-xs">' +
                esc(s.NR_DOCUMENTI) + '</td><td class="text-right"><button type="button" class="btn btn-white btn-xs" data-sezione="' +
                esc(s.ID_SEZIONE) + '"><i class="fa fa-pencil" aria-hidden="true"></i> Modifica</button></td></tr>';
        });
        $('#ctc-tab-sezioni').html(testa + corpo + '</tbody>');
    }

    function disegnaTipi() {
        var h = '<thead><tr><th>Tipo</th><th class="hidden-xs">Sezione</th><th class="text-center">Ogni (mesi)</th>' +
            '<th class="text-center hidden-xs">Preavviso</th><th class="hidden-xs">Azienda</th><th class="text-center hidden-xs">Scad.</th><th></th></tr></thead><tbody>';
        $.each(cfg.tipi, function (i, t) {
            h += '<tr' + (t.FL_DISATTIVO ? ' class="ctc-spenta"' : '') + '><td><div class="ct-doc-titolo">' + esc(t.DESCRIZIONE) +
                (t.FL_DISATTIVO ? ' <span class="ct-chip ct-chip-neutro">disattivo</span>' : '') + '</div><div class="ct-doc-file">' +
                esc(t.CODICE) + (t.RIFERIMENTO_NORMATIVO ? ' \u00b7 ' + esc(t.RIFERIMENTO_NORMATIVO) : '') + '</div></td>' +
                '<td class="hidden-xs">' + esc(t.TITOLO_SEZIONE) + '</td><td class="text-center">' + (t.PERIODICITA_MESI ? esc(t.PERIODICITA_MESI) : '<span class="ct-muto">una tantum</span>') +
                '</td><td class="text-center hidden-xs">' + (t.GIORNI_PREAVVISO !== null && t.GIORNI_PREAVVISO !== undefined ? esc(t.GIORNI_PREAVVISO) + ' gg' : '<span class="ct-muto">param.</span>') +
                '</td><td class="hidden-xs">' + aziendaTxt(t) + '</td><td class="text-center hidden-xs">' + esc(t.NR_SCADENZE) +
                '</td><td class="text-right"><button type="button" class="btn btn-white btn-xs" data-tipo="' + esc(t.ID_TIPO_SCADENZA) +
                '"><i class="fa fa-pencil" aria-hidden="true"></i> Modifica</button></td></tr>';
        });
        $('#ctc-tab-tipi').html(h + '</tbody>');
    }

    function disegnaParametri() {
        var h = '<thead><tr><th>Parametro</th><th>Valore predefinito</th><th class="hidden-xs">Eccezioni per azienda</th><th></th></tr></thead><tbody>';
        $.each(cfg.parametri, function (i, p) {
            var ecc = '';
            $.each(p.ECCEZIONI || [], function (k, e) {
                ecc += '<span class="ct-chip ct-chip-ver" title="' + esc(e.RAGSOC) + '">' + esc(e.RAGSOC) + ': ' +
                    (e.VALORE === '' ? '<i>vuoto</i>' : esc(e.VALORE)) + '</span> ';
            });
            h += '<tr data-cerca="' + esc((p.CHIAVE + ' ' + (p.DESCRIZIONE || '')).toLowerCase()) + '"><td><div class="ct-doc-titolo ctc-chiave">' +
                esc(p.CHIAVE) + '</div><div class="ct-doc-file">' + esc(p.DESCRIZIONE) + '</div></td><td class="ctc-valore">' +
                (p.VALORE === '' ? '<span class="ct-muto">(vuoto)</span>' : esc(p.VALORE)) + '</td><td class="hidden-xs">' + ecc +
                '</td><td class="text-right"><button type="button" class="btn btn-white btn-xs" data-parametro="' + esc(p.CHIAVE) +
                '"><i class="fa fa-pencil" aria-hidden="true"></i> Modifica</button></td></tr>';
        });
        $('#ctc-tab-parametri').html(h + '</tbody>');
        $('#ctc-filtro-parametri').trigger('input');
    }

    function disegnaCategorie() {
        var h = '<thead><tr><th>Categoria di WERA</th><th class="text-center hidden-xs">File</th><th>Sezione nella Centrale termica</th></tr></thead><tbody>';
        $.each(cfg.categorie, function (i, c) {
            h += '<tr><td><div class="ct-doc-titolo">' + esc(c.DESCRIZIONE) + '</div><div class="ct-doc-file">codice ' +
                esc(c.CODICE_CATEGORIA) + '</div></td><td class="text-center hidden-xs">' + esc(c.NR_FILE) + '</td><td>' +
                '<select class="form-control input-sm" data-categoria="' + esc(c.CODICE_CATEGORIA) + '" aria-label="Sezione per ' +
                esc(c.DESCRIZIONE) + '">' + opzioni(opzioniSezioni(true, null), c.ID_SEZIONE) + '</select></td></tr>';
        });
        $('#ctc-tab-categorie').html(h + '</tbody>');
    }

    // ------------------------------------------------------------------ finestra di modifica

    function campo(id, etichetta, html, aiuto, classe) {
        return '<div class="form-group ' + (classe || '') + '"><label for="' + id + '">' + etichetta + '</label>' + html +
            (aiuto ? '<p class="ct-aiuto">' + aiuto + '</p>' : '') + '</div>';
    }

    function input(id, valore, attr) {
        return '<input class="form-control" id="' + id + '" value="' + esc(valore === null || valore === undefined ? '' : valore) + '" ' + (attr || '') + '>';
    }

    function apriModale(titolo, sotto, corpo, alSalvataggio) {
        $('#ctc-modale-titolo').text(titolo);
        $('#ctc-modale-sotto').text(sotto || '');
        $('#ctc-modale-corpo').html(corpo + '<div class="ct-esito ct-esito-ko hidden" id="ctc-errore" role="alert"></div>');
        $('#ctc-salva').prop('disabled', false).toggle(!!alSalvataggio);
        salvataggio = alSalvataggio;
        $('#ctc-modale').modal('show');
    }

    function erroreModale(m) {
        $('#ctc-errore').removeClass('hidden').text(m);
        $('#ctc-salva').prop('disabled', false);
    }

    function trovaPer(lista, chiave, valore) {
        for (var i = 0; i < lista.length; i++) { if (lista[i][chiave] === valore) { return lista[i]; } }
        return null;
    }

    function modificaSezione(id) {
        var s = id ? trovaPer(cfg.sezioni, 'ID_SEZIONE', id) : { TIPO: 'DOCUMENTI', ORDINE: 100, PERMESSI: { ADM: 'RW', INS: 'RW', AMM: 'R' } };
        var permessi = '<div class="ctc-permessi">';
        $.each(cfg.profili, function (i, p) {
            permessi += '<label class="ctc-permesso"><span>' + esc(p.CODICE) + ' <small>' + esc(p.DESCRIZIONE) + '</small></span>' +
                '<select class="form-control input-sm" data-profilo="' + esc(p.CODICE) + '">' +
                opzioni([['-', 'Nessun accesso'], ['R', 'Vede'], ['RW', 'Vede e carica']], (s.PERMESSI || {})[p.CODICE] || '-') + '</select></label>';
        });
        permessi += '</div>';
        var corpo = (id ? '' :
                '<div class="row">' + campo('ctc-codice', 'Codice', input('ctc-codice', '', 'maxlength="30" placeholder="ES. CANNA_FUMARIA"'), 'Maiuscole, cifre e _; non si cambia pi\u00f9.', 'col-sm-6') +
                campo('ctc-azienda', 'Azienda', '<select class="form-control" id="ctc-azienda">' + opzioni(opzioniAziende(true), '') + '</select>', '', 'col-sm-6') + '</div>') +
            campo('ctc-titolo', 'Titolo', input('ctc-titolo', s.TITOLO, 'maxlength="100" required')) +
            campo('ctc-sottotitolo', 'Sottotitolo', input('ctc-sottotitolo', s.SOTTOTITOLO, 'maxlength="200"')) +
            '<div class="row">' +
            campo('ctc-icona', 'Icona', '<div class="input-group"><span class="input-group-addon"><i class="fa ' + esc(s.ICONA || 'fa-folder') +
                '" id="ctc-icona-anteprima"></i></span>' + input('ctc-icona', s.ICONA, 'maxlength="50" placeholder="fa-book"') + '</div>', 'Classe Font Awesome 4.', 'col-sm-6') +
            campo('ctc-tipo', 'Tipo', '<select class="form-control" id="ctc-tipo">' + opzioni(TIPI_SEZIONE, s.TIPO) + '</select>', '', 'col-sm-6') + '</div>' +
            '<div class="row">' + campo('ctc-ordine', 'Ordine', input('ctc-ordine', s.ORDINE, 'type="number" min="0" max="9999"'), '', 'col-sm-4') +
            '<div class="col-sm-8 ctc-spunte"><label><input type="checkbox" id="ctc-scadenza"' + (s.FL_SCADENZA_RICHIESTA ? ' checked' : '') +
            '> Richiede una scadenza registrata</label><label><input type="checkbox" id="ctc-disattivo"' + (s.FL_DISATTIVO ? ' checked' : '') +
            '> Disattivata</label></div></div>' +
            '<h4 class="ctc-sotto">Permessi per profilo</h4>' + permessi;

        apriModale(id ? 'Modifica sezione' : 'Nuova sezione', id ? s.CODICE : '', corpo, function () {
            var perm = [];
            $('#ctc-modale-corpo [data-profilo]').each(function () { perm.push($(this).attr('data-profilo') + ':' + $(this).val()); });
            var p = {
                OGGETTO: 'sezione', ID_SEZIONE: id || '', TITOLO: $('#ctc-titolo').val(), SOTTOTITOLO: $('#ctc-sottotitolo').val(),
                ICONA: $('#ctc-icona').val(), TIPO: $('#ctc-tipo').val(), ORDINE: $('#ctc-ordine').val(),
                FL_SCADENZA_RICHIESTA: $('#ctc-scadenza').is(':checked') ? 'S' : 'N', FL_DISATTIVO: $('#ctc-disattivo').is(':checked') ? 'S' : 'N',
                PERMESSI: perm.join(',')
            };
            if (!id) { p.CODICE = $('#ctc-codice').val(); p.ID_AZIENDA = $('#ctc-azienda').val(); }
            return p;
        });
        $('#ctc-icona').on('input', function () {
            var v = $(this).val();
            $('#ctc-icona-anteprima').attr('class', 'fa ' + (/^fa-[a-z0-9-]+$/.test(v) ? v : 'fa-question'));
        });
    }

    function modificaTipo(id) {
        var t = id ? trovaPer(cfg.tipi, 'ID_TIPO_SCADENZA', id) : { ORDINE: 100 };
        var corpo = (id ? '' :
                '<div class="row">' + campo('ctc-codice', 'Codice', input('ctc-codice', '', 'maxlength="30" placeholder="ES. PROVA_FUMI"'), 'Maiuscole, cifre e _.', 'col-sm-6') +
                campo('ctc-azienda', 'Azienda', '<select class="form-control" id="ctc-azienda">' + opzioni(opzioniAziende(true), '') + '</select>', '', 'col-sm-6') + '</div>') +
            campo('ctc-descrizione', 'Descrizione', input('ctc-descrizione', t.DESCRIZIONE, 'maxlength="200" required')) +
            campo('ctc-sezione', 'Sezione del documento che la soddisfa', '<select class="form-control" id="ctc-sezione">' +
                opzioni(opzioniSezioni(false, t.ID_AZIENDA), t.ID_SEZIONE) + '</select>') +
            '<div class="row">' + campo('ctc-periodicita', 'Rinnovo ogni (mesi)', input('ctc-periodicita', t.PERIODICITA_MESI, 'type="number" min="1" max="240"'), 'Vuoto = una tantum.', 'col-sm-4') +
            campo('ctc-preavviso', 'Preavviso (giorni)', input('ctc-preavviso', t.GIORNI_PREAVVISO, 'type="number" min="0" max="365"'), 'Vuoto = parametro.', 'col-sm-4') +
            campo('ctc-ordine', 'Ordine', input('ctc-ordine', t.ORDINE, 'type="number" min="0" max="9999"'), '', 'col-sm-4') + '</div>' +
            campo('ctc-riferimento', 'Riferimento normativo', input('ctc-riferimento', t.RIFERIMENTO_NORMATIVO, 'maxlength="200"')) +
            '<div class="ctc-spunte"><label><input type="checkbox" id="ctc-disattivo"' + (t.FL_DISATTIVO ? ' checked' : '') + '> Disattivato</label></div>';
        apriModale(id ? 'Modifica tipo di scadenza' : 'Nuovo tipo di scadenza', id ? t.CODICE : '', corpo, function () {
            var p = {
                OGGETTO: 'tipo', ID_TIPO_SCADENZA: id || '', DESCRIZIONE: $('#ctc-descrizione').val(), ID_SEZIONE: $('#ctc-sezione').val(),
                PERIODICITA_MESI: $('#ctc-periodicita').val(), GIORNI_PREAVVISO: $('#ctc-preavviso').val(), ORDINE: $('#ctc-ordine').val(),
                RIFERIMENTO_NORMATIVO: $('#ctc-riferimento').val(), FL_DISATTIVO: $('#ctc-disattivo').is(':checked') ? 'S' : 'N'
            };
            if (!id) { p.CODICE = $('#ctc-codice').val(); p.ID_AZIENDA = $('#ctc-azienda').val(); }
            return p;
        });
    }

    function campoValore(id, tipo, valore) {
        if (tipo === 'BOOLEANO') { return '<select class="form-control" id="' + id + '">' + opzioni([['S', 'Si'], ['N', 'No']], valore) + '</select>'; }
        if (tipo === 'NUMERO') { return input(id, valore, 'type="number" min="0"'); }
        if (tipo === 'EMAIL') { return input(id, valore, 'type="email" maxlength="200"'); }
        if (tipo === 'URL') { return input(id, valore, 'type="url" maxlength="500" placeholder="https://..."'); }
        if (tipo === 'TELEFONO') { return input(id, valore, 'type="tel" maxlength="30"'); }
        return input(id, valore, 'maxlength="2000"');
    }

    function modificaParametro(chiave) {
        var p = trovaPer(cfg.parametri, 'CHIAVE', chiave);
        if (!p) { return; }
        var ecc = '<ul class="ctc-eccezioni">';
        $.each(p.ECCEZIONI || [], function (i, e) {
            ecc += '<li><span class="ctc-ecc-az">' + esc(e.RAGSOC) + '</span>' + campoValore('ctc-ecc-' + e.ID_AZIENDA, p.TIPO_VALORE, e.VALORE) +
                '<button type="button" class="btn btn-white btn-xs" data-ecc-salva="' + esc(e.ID_AZIENDA) + '">Salva</button>' +
                '<button type="button" class="btn btn-white btn-xs" data-ecc-via="' + esc(e.ID_AZIENDA) + '" aria-label="Elimina eccezione">' +
                '<i class="fa fa-trash-o" aria-hidden="true"></i></button></li>';
        });
        ecc += '</ul>';
        var libere = $.grep(opzioniAziende(false), function (a) { return !trovaPer(p.ECCEZIONI || [], 'ID_AZIENDA', a[0]); });
        var nuova = libere.length ? '<div class="ctc-nuova-ecc"><select class="form-control input-sm" id="ctc-ecc-azienda">' + opzioni(libere, '') +
            '</select>' + campoValore('ctc-ecc-nuovo', p.TIPO_VALORE, p.VALORE) +
            '<button type="button" class="btn btn-white btn-xs" id="ctc-ecc-aggiungi"><i class="fa fa-plus" aria-hidden="true"></i> Aggiungi</button></div>' : '';
        var corpo = '<p class="ct-aiuto">' + esc(p.DESCRIZIONE) + ' <span class="ct-chip ct-chip-neutro">' + esc(p.TIPO_VALORE) + '</span></p>' +
            campo('ctc-valore', 'Valore predefinito (tutte le aziende)', campoValore('ctc-valore', p.TIPO_VALORE, p.VALORE)) +
            '<h4 class="ctc-sotto">Eccezioni per azienda</h4>' + ecc + nuova;
        apriModale('Parametro', chiave, corpo, function () {
            return { OGGETTO: 'parametro', CHIAVE: chiave, VALORE: $('#ctc-valore').val() };
        });
        var dopo = function () { modificaParametroDopo(chiave); };
        $('#ctc-modale-corpo').off('click.ecc')
            .on('click.ecc', '[data-ecc-salva]', function () {
                var az = $(this).attr('data-ecc-salva');
                salva({ OGGETTO: 'parametro', CHIAVE: chiave, ID_AZIENDA: az, VALORE: $('#ctc-ecc-' + az).val() }, dopo, erroreModale);
            })
            .on('click.ecc', '[data-ecc-via]', function () {
                var az = $(this).attr('data-ecc-via');
                salva({ OGGETTO: 'parametro', CHIAVE: chiave, ID_AZIENDA: az, AZIONE: 'elimina' }, dopo, erroreModale);
            })
            .on('click.ecc', '#ctc-ecc-aggiungi', function () {
                salva({ OGGETTO: 'parametro', CHIAVE: chiave, ID_AZIENDA: $('#ctc-ecc-azienda').val(), VALORE: $('#ctc-ecc-nuovo').val() }, dopo, erroreModale);
            });
    }

    /** Riapre la finestra del parametro coi dati ricaricati (dopo un'eccezione salvata o tolta). */
    function modificaParametroDopo(chiave) {
        var tentativi = 0, t = window.setInterval(function () {
            tentativi++;
            if (cfg && !$.active) { window.clearInterval(t); modificaParametro(chiave); }
            if (tentativi > 20) { window.clearInterval(t); }
        }, 150);
    }

    // ------------------------------------------------------------------ avvio

    $(function () {
        carica();
        $('#ctc-nuova-sezione').on('click', function () { modificaSezione(null); });
        $('#ctc-nuovo-tipo').on('click', function () { modificaTipo(null); });
        $('#ctc-tab-sezioni').on('click', '[data-sezione]', function () { modificaSezione(Number($(this).attr('data-sezione'))); });
        $('#ctc-tab-tipi').on('click', '[data-tipo]', function () { modificaTipo(Number($(this).attr('data-tipo'))); });
        $('#ctc-tab-parametri').on('click', '[data-parametro]', function () { modificaParametro($(this).attr('data-parametro')); });
        $('#ctc-tab-categorie').on('change', '[data-categoria]', function () {
            salva({ OGGETTO: 'categoria', CODICE_CATEGORIA: $(this).attr('data-categoria'), ID_SEZIONE: $(this).val() });
        });
        $('#ctc-filtro-parametri').on('input', function () {
            var q = $.trim($(this).val()).toLowerCase();
            $('#ctc-tab-parametri tbody tr').each(function () { $(this).toggle(!q || ($(this).attr('data-cerca') || '').indexOf(q) >= 0); });
        });
        $('#ctc-form').on('submit', function (e) {
            e.preventDefault();
            if (!salvataggio) { return; }
            $('#ctc-salva').prop('disabled', true);
            salva(salvataggio(), function () { $('#ctc-modale').modal('hide'); }, erroreModale);
        });
    });
}(window.jQuery));
