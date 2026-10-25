/* ============================================================
   WERA 2026 - Centrale termica (pagina centraletermica.html)
   ------------------------------------------------------------
   Dati: astro?FUNCTIONID=CentraleTermicaDati (JSON). Caricamento: CentraleTermicaUpload (POST
   multipart, FUNCTIONID nella URL). File: CentraleTermicaDownload. Stato: CentraleTermicaStato.
   Perimetro e permessi sono controllati dal server: qui solo presentazione.
   ES5 + jQuery 2 (come il resto dell'app). Solo ASCII: accenti come \u00xx.
   ============================================================ */
(function ($) {
    'use strict';

    var API = 'astro';
    var MIME_APRIBILI = { pdf: 1, jpg: 1, jpeg: 1, png: 1, gif: 1 };
    var CHIAVE_ULTIMO = 'wera.ct.condominio';

    var st = {
        par: {},
        profilo: '',
        condomini: [],
        id: null,
        condominio: null,
        sezioni: [],
        ultimi: [],
        scadenze: [],
        selezione: {},
        pannello: null,
        file: []
    };

    // ------------------------------------------------------------------ utilita

    function esc(v) {
        if (v === null || v === undefined) { return ''; }
        return String(v).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }

    function data(iso) {
        if (!iso) { return ''; }
        var m = /^(\d{4})-(\d{2})-(\d{2})/.exec(iso);
        return m ? m[3] + '/' + m[2] + '/' + m[1] : '';
    }

    function dataOra(iso) {
        var d = data(iso), m = /T(\d{2}):(\d{2})/.exec(iso || '');
        return d + (m ? ' ' + m[1] + ':' + m[2] : '');
    }

    function dimensione(b) {
        if (b === null || b === undefined || b === '') { return ''; }
        b = Number(b);
        if (b < 1024) { return b + ' B'; }
        if (b < 1048576) { return Math.round(b / 102.4) / 10 + ' KB'; }
        return Math.round(b / 104857.6) / 10 + ' MB';
    }

    function estensione(nome) {
        var m = /\.([a-z0-9]+)$/i.exec(nome || '');
        return m ? m[1].toLowerCase() : '';
    }

    function icona(nome) {
        var e = estensione(nome);
        if (e === 'pdf' || e === 'p7m') { return 'fa-file-pdf-o'; }
        if (e === 'jpg' || e === 'jpeg' || e === 'png' || e === 'gif') { return 'fa-file-image-o'; }
        if (e === 'doc' || e === 'docx' || e === 'odt' || e === 'txt') { return 'fa-file-text-o'; }
        if (e === 'xls' || e === 'xlsx' || e === 'ods') { return 'fa-file-excel-o'; }
        if (e === 'zip') { return 'fa-file-archive-o'; }
        return 'fa-file-o';
    }

    function elenco(v) {
        var out = [];
        $.each(String(v || '').split(','), function (i, x) {
            x = $.trim(x).toLowerCase();
            if (x) { out.push(x); }
        });
        return out;
    }

    function numero(v, def) {
        var n = parseInt(v, 10);
        return isNaN(n) ? def : n;
    }

    /** Solo link http(s) o relativi: niente javascript: nei parametri configurabili. */
    function urlSicuro(u) {
        u = $.trim(u || '');
        if (!u) { return ''; }
        if (/^https?:\/\//i.test(u) || /^astro\?/i.test(u)) { return u; }
        return '';
    }

    function sessioneScaduta() {
        window.location.href = API + '?FUNCTIONID=Logout';
    }

    /** GET JSON su CentraleTermicaDati; una risposta non JSON e' la pagina di login (sessione scaduta). */
    function dati(operazione, extra) {
        var param = $.extend({ FUNCTIONID: 'CentraleTermicaDati', OPERAZIONE: operazione }, extra || {});
        var d = $.Deferred();
        $.ajax({ url: API, data: param, dataType: 'text', cache: false }).done(function (testo) {
            var j;
            try { j = JSON.parse(testo); } catch (e) { sessioneScaduta(); d.reject('sessione'); return; }
            if (j.esito === 'OK') { d.resolve(j); } else { d.reject(j.messaggio || 'Errore'); }
        }).fail(function (xhr) {
            var msg = 'Errore di comunicazione';
            try { msg = JSON.parse(xhr.responseText).messaggio || msg; } catch (e) { /* non JSON */ }
            d.reject(msg);
        });
        return d.promise();
    }

    function urlDownload(param) {
        return API + '?' + $.param($.extend({ FUNCTIONID: 'CentraleTermicaDownload' }, param));
    }

    function avviso(testo, tipo) {
        var $a = $('#ct-avviso');
        if (!testo) { $a.addClass('hidden').empty(); return; }
        $a.removeClass('hidden ct-avviso-ok ct-avviso-ko').addClass(tipo === 'ok' ? 'ct-avviso-ok' : 'ct-avviso-ko')
            .text(testo);
    }

    function semaforo(s) {
        switch (s) {
        case 'SCADUTA': return { cls: 'ct-chip-ko', txt: 'Scaduta' };
        case 'IN_SCADENZA': return { cls: 'ct-chip-warn', txt: 'In scadenza' };
        case 'REGOLARE': return { cls: 'ct-chip-ok', txt: 'In regola' };
        case 'DA_REGISTRARE': return { cls: 'ct-chip-neutro', txt: 'Scadenza da registrare' };
        default: return null;
        }
    }

    function chip(s) {
        var c = semaforo(s);
        return c ? '<span class="ct-chip ' + c.cls + '">' + esc(c.txt) + '</span>' : '';
    }

    function sezionePerId(id) {
        for (var i = 0; i < st.sezioni.length; i++) {
            if (st.sezioni[i].ID_SEZIONE === id) { return st.sezioni[i]; }
        }
        return null;
    }

    function sezioniScrivibili() {
        return $.grep(st.sezioni, function (s) { return s.FL_SCRITTURA === true; });
    }

    // codice di selezione per download/zip: D = documento nuovo (versione corrente), W = documento di /wera
    function codice(doc) {
        return doc.ORIGINE === 'WERA' ? 'W' + doc.ID_FILE_WERA : 'D' + doc.ID_DOCUMENTO;
    }

    function paramFile(doc) {
        return doc.ORIGINE === 'WERA' ? { ID_FILE_WERA: doc.ID_FILE_WERA } : { ID_DOCUMENTO: doc.ID_DOCUMENTO };
    }

    function azioniFile(param, nome) {
        var h = '';
        if (MIME_APRIBILI[estensione(nome)]) {
            h += '<a class="btn btn-white btn-xs" target="_blank" rel="noopener" href="' +
                esc(urlDownload($.extend({ APRI: 'S' }, param))) + '"><i class="fa fa-eye" aria-hidden="true"></i> Apri</a> ';
        }
        h += '<a class="btn btn-white btn-xs" href="' + esc(urlDownload(param)) +
            '"><i class="fa fa-download" aria-hidden="true"></i> Scarica</a>';
        return h;
    }

    function etichettaWera() {
        return st.par['DOCUMENTI_WERA.ETICHETTA'] || 'da WERA';
    }

    // ------------------------------------------------------------------ avvio

    function avvia() {
        $.when(dati('parametri'), dati('condomini')).done(function (p, c) {
            st.par = p.parametri || {};
            st.profilo = st.par.PROFILO || '';
            st.condomini = c.condomini || [];
            preparaCaricamento();
            riempiCondomini();
        }).fail(function (m) {
            if (m !== 'sessione') { avviso(m); }
        });

        $('#ct-condominio').on('change', function () {
            var id = numero($(this).val(), null);
            if (id) { scegliCondominio(id); }
        });
        $('#ct-btn-carica').on('click', function () { apriCaricamento(null, null); });
        $('#ct-btn-archivio').on('click', apriArchivio);
        $('#ct-btn-scarica-sel').on('click', scaricaSelezionati);
        $('#ct-sel-tutti').on('change', function () {
            var on = this.checked;
            $('#ct-ultimi input.ct-sel').each(function () { this.checked = on; segnaSelezione(this); });
            aggiornaSelezione();
        });
        $('#ct-ultimi').on('change', 'input.ct-sel', function () { segnaSelezione(this); aggiornaSelezione(); });
        $('#ct-cartelle, #ct-altre').on('click', '.ct-cartella', function () {
            apriSezione(numero($(this).attr('data-id'), null));
        });
        $('#ct-pannello-chiudi, #ct-velo').on('click', chiudiPannello);
        $(document).on('keydown', function (e) {
            if (e.keyCode === 27 && st.pannello) { chiudiPannello(); }
        });
        $('#ct-pannello-corpo').on('click', '[data-azione]', azionePannello);
        $('#ct-pannello-azioni').on('click', '[data-azione]', azionePannello);
    }

    function riempiCondomini() {
        var $s = $('#ct-condominio').empty();
        if (!st.condomini.length) {
            $('#ct-nome-condominio').text('Nessun condominio');
            $('#ct-indirizzo').text('Il tuo profilo non ha condomini associati.');
            return;
        }
        $.each(st.condomini, function (i, c) {
            $s.append($('<option>').val(c.ID_CONDOMINIO).text(c.DENOMINAZIONE +
                (c.LOCALITA ? ' - ' + c.LOCALITA : '')));
        });
        if ($.fn.select2) {
            $s.select2({ width: '100%', language: { noResults: function () { return 'Nessun condominio'; } } });
        }

        var scelto = numero($('#ct-page').attr('data-id-condominio'), null), ultimo = null;
        try { ultimo = numero(window.localStorage.getItem(CHIAVE_ULTIMO), null); } catch (e) { /* storage assente */ }
        var ids = $.map(st.condomini, function (c) { return c.ID_CONDOMINIO; });
        if ($.inArray(scelto, ids) < 0) { scelto = ($.inArray(ultimo, ids) >= 0) ? ultimo : ids[0]; }
        $s.val(String(scelto)).trigger('change.select2');
        scegliCondominio(scelto);
    }

    function scegliCondominio(id) {
        st.id = id;
        st.selezione = {};
        try { window.localStorage.setItem(CHIAVE_ULTIMO, String(id)); } catch (e) { /* storage assente */ }
        if (window.history && window.history.replaceState) {
            window.history.replaceState(null, '', API + '?FUNCTIONID=CentraleTermica&ID_CONDOMINIO=' + id);
        }
        ricarica();
    }

    function ricarica() {
        var id = st.id;
        avviso('');
        $('#ct-page').addClass('ct-carica-in-corso');
        $.when(dati('riepilogo', { ID_CONDOMINIO: id }), dati('sezioni', { ID_CONDOMINIO: id }),
            dati('documenti', { ID_CONDOMINIO: id, ULTIMI: 'S' }), dati('scadenze', { ID_CONDOMINIO: id }))
            .done(function (r, s, d, sc) {
                if (id !== st.id) { return; } // l'utente ha gia' cambiato condominio
                st.condominio = r.dati.condominio;
                st.sezioni = s.sezioni || [];
                st.ultimi = d.documenti || [];
                st.scadenze = sc.scadenze || [];
                disegnaTestata(r.dati.condominio);
                disegnaSintesi(r.dati.riepilogo || {});
                disegnaCartelle();
                disegnaUltimi();
                disegnaScadenze();
                disegnaAssistenza();
                $('#ct-btn-carica').toggleClass('hidden', sezioniScrivibili().length === 0);
                if (st.pannello) { st.pannello.ricarica(); }
            })
            .fail(function (m) { if (m !== 'sessione') { avviso(m); } })
            .always(function () { $('#ct-page').removeClass('ct-carica-in-corso'); });
    }

    // ------------------------------------------------------------------ disegno

    function disegnaTestata(c) {
        $('#ct-nome-condominio').text(c.DENOMINAZIONE || '');
        $('#ct-indirizzo').text([c.INDIRIZZO, c.LOCALITA, c.PROVINCIA ? '(' + c.PROVINCIA + ')' : '']
            .join(' ').replace(/\s+/g, ' '));
        var g = $.trim(st.par['ETICHETTA.GESTIONE'] || '');
        $('#ct-gestione').toggleClass('hidden', !g).text(g ? 'Gestione: ' + g : '');
        document.title = (st.par['ETICHETTA.TITOLO'] || 'Centrale termica') + ' - ' + (c.DENOMINAZIONE || '');
    }

    function disegnaSintesi(r) {
        $('#ct-kpi-documenti').text(r.NR_DOCUMENTI === null || r.NR_DOCUMENTI === undefined ? '0' : r.NR_DOCUMENTI);

        var $s = $('#ct-kpi-scadenza');
        if (r.PROSSIMA_SCADENZA) {
            $s.html(esc(data(r.PROSSIMA_SCADENZA)) + ' ' + chip(r.SEMAFORO_PROSSIMA_SCADENZA));
            $('#ct-kpi-scadenza-lbl').text(r.DESCR_PROSSIMA_SCADENZA || 'Prossima scadenza');
        } else {
            $s.html('<span class="ct-kpi-vuoto">Da verificare</span>');
            $('#ct-kpi-scadenza-lbl').text('Prossima scadenza');
        }

        $('#ct-kpi-intervento').html(r.ULTIMO_INTERVENTO ? esc(data(r.ULTIMO_INTERVENTO))
            : '<span class="ct-kpi-vuoto">Nessuno</span>');

        var tot = numero(r.NR_SEZIONI, 0), con = numero(r.NR_SEZIONI_CON_DOCUMENTI, 0);
        $('#ct-kpi-completezza').html(esc(con) + '<span class="ct-kpi-di">/' + esc(tot) + '</span>');
        $('#ct-kpi-barra').css('width', tot ? Math.round(con * 100 / tot) + '%' : '0');
    }

    function cartella(s, piccola) {
        var n = numero(s.NR_DOCUMENTI, 0), arch = numero(s.NR_ARCHIVIATI, 0);
        var meta = n === 1 ? '1 documento' : n + ' documenti';
        if (s.TS_ULTIMO_AGGIORNAMENTO) { meta += ' &middot; agg. ' + esc(data(s.TS_ULTIMO_AGGIORNAMENTO)); }
        var scad = '';
        if (s.PROSSIMA_SCADENZA) {
            scad = '<span class="ct-cartella-scad">' + chip(s.SEMAFORO) + ' ' + esc(data(s.PROSSIMA_SCADENZA)) + '</span>';
        } else if (s.SEMAFORO === 'DA_REGISTRARE') {
            scad = '<span class="ct-cartella-scad">' + chip('DA_REGISTRARE') + '</span>';
        }
        return '<button type="button" class="ct-cartella' + (piccola ? ' ct-cartella-piccola' : '') +
            (n === 0 ? ' ct-cartella-vuota' : '') + '" data-id="' + esc(s.ID_SEZIONE) + '">' +
            '<span class="ct-cartella-ico"><i class="fa ' + esc(s.ICONA || 'fa-folder') + '" aria-hidden="true"></i></span>' +
            '<span class="ct-cartella-testo"><span class="ct-cartella-titolo">' + esc(s.TITOLO) + '</span>' +
            (piccola ? '' : '<span class="ct-cartella-sotto">' + esc(s.SOTTOTITOLO) + '</span>') +
            '<span class="ct-cartella-meta">' + meta + (arch ? ' &middot; ' + arch + ' in archivio' : '') + '</span>' +
            scad + '</span><i class="fa fa-angle-right ct-cartella-freccia" aria-hidden="true"></i></button>';
    }

    function disegnaCartelle() {
        var principali = '', altre = '';
        $.each(st.sezioni, function (i, s) {
            if (s.TIPO === 'DOCUMENTI') { principali += cartella(s, false); } else { altre += cartella(s, true); }
        });
        $('#ct-cartelle').html(principali || '<p class="ct-vuoto">Nessuna sezione disponibile per il tuo profilo.</p>');
        $('#ct-altre').html(altre);
        $('#ct-altre-blocco').toggleClass('hidden', !altre);
    }

    function rigaDocumento(doc, conSelezione) {
        var cod = codice(doc), sel = st.selezione[cod] ? ' checked' : '';
        var badge = doc.ORIGINE === 'WERA' ? '<span class="ct-chip ct-chip-neutro">' + esc(etichettaWera()) + '</span>'
            : (numero(doc.NUMERO_VERSIONE, 1) > 1 ? '<span class="ct-chip ct-chip-ver">v' + esc(doc.NUMERO_VERSIONE) + '</span>' : '');
        var quando = doc.DATA_DOCUMENTO ? data(doc.DATA_DOCUMENTO) : data(doc.TS_CARICAMENTO);
        return '<tr>' +
            (conSelezione ? '<td class="ct-col-sel"><input type="checkbox" class="ct-sel" value="' + esc(cod) + '"' + sel +
                ' aria-label="Seleziona ' + esc(doc.TITOLO) + '"></td>' : '') +
            '<td><div class="ct-doc"><i class="fa ' + icona(doc.NOME_FILE) + ' ct-doc-ico" aria-hidden="true"></i><div>' +
            '<div class="ct-doc-titolo">' + esc(doc.TITOLO) + ' ' + badge + '</div>' +
            '<div class="ct-doc-file">' + esc(doc.NOME_FILE) + (doc.DIMENSIONE_BYTE ? ' &middot; ' + esc(dimensione(doc.DIMENSIONE_BYTE)) : '') +
            '</div></div></div></td>' +
            '<td class="hidden-xs">' + esc(doc.TITOLO_SEZIONE) + '</td>' +
            '<td class="hidden-xs ct-nowrap">' + esc(quando) + '</td>' +
            '<td class="text-right ct-nowrap">' + azioniFile(paramFile(doc), doc.NOME_FILE) + '</td></tr>';
    }

    function disegnaUltimi() {
        var h = '';
        $.each(st.ultimi, function (i, doc) { h += rigaDocumento(doc, true); });
        $('#ct-ultimi').html(h || '<tr><td colspan="5" class="ct-vuoto">Nessun documento caricato per questo condominio.</td></tr>');
        $('#ct-sel-tutti').prop('checked', false);
        aggiornaSelezione();
    }

    function segnaSelezione(cb) {
        if (cb.checked) { st.selezione[cb.value] = true; } else { delete st.selezione[cb.value]; }
    }

    function aggiornaSelezione() {
        var n = 0, k;
        for (k in st.selezione) { if (st.selezione.hasOwnProperty(k)) { n++; } }
        var max = numero(st.par['DOWNLOAD.ZIP_MAX_FILE'], 50);
        $('#ct-conta-sel').text(n ? '(' + n + ')' : '');
        $('#ct-btn-scarica-sel').prop('disabled', n === 0 || n > max)
            .attr('title', n > max ? 'Massimo ' + max + ' file per volta' : '');
    }

    function scaricaSelezionati() {
        var codici = [], k;
        for (k in st.selezione) { if (st.selezione.hasOwnProperty(k)) { codici.push(k); } }
        if (codici.length) { window.location.href = urlDownload({ SELEZIONE: codici.join(',') }); }
    }

    function disegnaScadenze() {
        var h = '';
        $.each(st.scadenze, function (i, s) {
            var g = numero(s.GIORNI_MANCANTI, 0);
            var quando = g < 0 ? 'scaduta da ' + (-g) + (g === -1 ? ' giorno' : ' giorni')
                : (g === 0 ? 'oggi' : 'tra ' + g + (g === 1 ? ' giorno' : ' giorni'));
            h += '<li class="ct-scad ct-scad-' + esc(String(s.SEMAFORO || '').toLowerCase()) + '">' +
                '<span class="ct-scad-punto" aria-hidden="true"></span><div>' +
                '<div class="ct-scad-titolo">' + esc(s.DESCRIZIONE) + '</div>' +
                '<div class="ct-scad-meta">' + esc(data(s.DATA_SCADENZA)) + ' &middot; ' + esc(quando) +
                ' &middot; ' + esc(s.TITOLO_SEZIONE) + '</div></div>' + chip(s.SEMAFORO) + '</li>';
        });
        $.each(st.sezioni, function (i, s) {
            if (s.SEMAFORO === 'DA_REGISTRARE') {
                h += '<li class="ct-scad ct-scad-da_registrare"><span class="ct-scad-punto" aria-hidden="true"></span><div>' +
                    '<div class="ct-scad-titolo">' + esc(s.TITOLO) + '</div>' +
                    '<div class="ct-scad-meta">Nessuna scadenza registrata per questa sezione</div></div>' +
                    chip('DA_REGISTRARE') + '</li>';
            }
        });
        $('#ct-scadenze').html(h || '<li class="ct-vuoto">Nessuna scadenza registrata.</li>');
    }

    function disegnaAssistenza() {
        var p = st.par, ufficio = $.trim(p['ASSISTENZA.UFFICIO']), email = $.trim(p['ASSISTENZA.EMAIL']),
            tel = $.trim(p['ASSISTENZA.TELEFONO']), orari = $.trim(p['ASSISTENZA.ORARI']),
            link = urlSicuro(p['ASSISTENZA.LINK_SEGNALAZIONE']);
        var contatti = '', azioni = '';
        if (tel) {
            contatti += '<li><i class="fa fa-phone" aria-hidden="true"></i> <a href="tel:' + esc(tel.replace(/[^0-9+]/g, '')) +
                '">' + esc(tel) + '</a></li>';
        }
        if (email) {
            contatti += '<li><i class="fa fa-envelope-o" aria-hidden="true"></i> ' + esc(email) + '</li>';
            var oggetto = (p['ETICHETTA.TITOLO'] || 'Centrale termica') + ' - ' + (st.condominio ? st.condominio.DENOMINAZIONE : '');
            azioni += '<a class="btn btn-primary btn-sm" href="mailto:' + esc(email) + '?subject=' +
                esc(encodeURIComponent(oggetto)) + '"><i class="fa fa-paper-plane" aria-hidden="true"></i> Contatta ufficio</a> ';
        }
        if (orari) { contatti += '<li><i class="fa fa-clock-o" aria-hidden="true"></i> ' + esc(orari) + '</li>'; }
        if (link) {
            azioni += '<a class="btn btn-white btn-sm" href="' + esc(link) + '" target="_blank" rel="noopener">' +
                '<i class="fa fa-flag-o" aria-hidden="true"></i> Apri segnalazione</a>';
        }
        $('#ct-titolo-assistenza').text(p['ASSISTENZA.TITOLO'] || 'Assistenza');
        $('#ct-ufficio').text(ufficio).toggleClass('hidden', !ufficio);
        $('#ct-contatti').html(contatti);
        $('#ct-assistenza-azioni').html(azioni);
        $('#ct-assistenza').toggleClass('hidden', !(ufficio || contatti || azioni));
    }

    // ------------------------------------------------------------------ pannello laterale

    function apriPannello(cfg) {
        st.pannello = cfg;
        $('#ct-pannello-sopra').text(cfg.sopra || '');
        $('#ct-pannello-titolo').text(cfg.titolo || '');
        $('#ct-pannello-sotto').text(cfg.sotto || '');
        $('#ct-pannello-azioni').html(cfg.azioni || '');
        $('#ct-pannello-corpo').html('<p class="ct-vuoto"><i class="fa fa-spinner fa-spin"></i> Caricamento...</p>');
        $('#ct-velo, #ct-pannello').removeAttr('hidden');
        $('body').addClass('ct-pannello-aperto');
        window.setTimeout(function () { $('#ct-pannello').addClass('ct-aperto'); $('#ct-pannello-chiudi').focus(); }, 10);
        cfg.ricarica();
    }

    function chiudiPannello() {
        var ritorno = st.pannello && st.pannello.ritorno;
        st.pannello = null;
        $('#ct-pannello').removeClass('ct-aperto');
        $('body').removeClass('ct-pannello-aperto');
        window.setTimeout(function () { $('#ct-velo, #ct-pannello').attr('hidden', 'hidden'); }, 220);
        if (ritorno) { $(ritorno).focus(); }
    }

    function voceDocumento(doc, sezione, archivio) {
        var nuovo = doc.ORIGINE !== 'WERA', scrive = sezione && sezione.FL_SCRITTURA === true;
        var badge = !nuovo ? '<span class="ct-chip ct-chip-neutro">' + esc(etichettaWera()) + '</span>'
            : '<span class="ct-chip ct-chip-ver">v' + esc(doc.NUMERO_VERSIONE || 1) + '</span>';
        var meta = [];
        if (doc.DATA_DOCUMENTO) { meta.push('del ' + data(doc.DATA_DOCUMENTO)); }
        meta.push('caricato il ' + dataOra(doc.TS_CARICAMENTO));
        if (doc.CARICATO_DA) { meta.push(doc.CARICATO_DA); }
        if (archivio && sezione) { meta.push(sezione.TITOLO); }

        var azioni = azioniFile(paramFile(doc), doc.NOME_FILE);
        if (nuovo) {
            azioni += ' <button type="button" class="btn btn-white btn-xs" data-azione="versioni" data-doc="' + esc(doc.ID_DOCUMENTO) +
                '"><i class="fa fa-history" aria-hidden="true"></i> Versioni</button>';
            if (scrive && doc.STATO === 'CORRENTE') {
                azioni += ' <button type="button" class="btn btn-white btn-xs" data-azione="versione" data-doc="' + esc(doc.ID_DOCUMENTO) +
                    '"><i class="fa fa-upload" aria-hidden="true"></i> Nuova versione</button>' +
                    ' <button type="button" class="btn btn-white btn-xs" data-azione="stato" data-stato="ARCHIVIATO" data-doc="' +
                    esc(doc.ID_DOCUMENTO) + '"><i class="fa fa-archive" aria-hidden="true"></i> Archivia</button>';
            }
            if (scrive && doc.STATO === 'ARCHIVIATO') {
                azioni += ' <button type="button" class="btn btn-white btn-xs" data-azione="stato" data-stato="CORRENTE" data-doc="' +
                    esc(doc.ID_DOCUMENTO) + '"><i class="fa fa-undo" aria-hidden="true"></i> Ripristina</button>';
            }
        }
        return '<li class="ct-voce" data-voce="' + esc(codice(doc)) + '">' +
            '<i class="fa ' + icona(doc.NOME_FILE) + ' ct-doc-ico" aria-hidden="true"></i>' +
            '<div class="ct-voce-corpo"><div class="ct-doc-titolo">' + esc(doc.TITOLO) + ' ' + badge + '</div>' +
            '<div class="ct-doc-file">' + esc(doc.NOME_FILE) + (doc.DIMENSIONE_BYTE ? ' &middot; ' + esc(dimensione(doc.DIMENSIONE_BYTE)) : '') + '</div>' +
            (doc.DESCRIZIONE && nuovo ? '<div class="ct-voce-descr">' + esc(doc.DESCRIZIONE) + '</div>' : '') +
            '<div class="ct-voce-meta">' + esc(meta.join(' \u00b7 ')) + '</div>' +
            '<div class="ct-voce-azioni">' + azioni + '</div>' +
            '<div class="ct-versioni hidden"></div></div></li>';
    }

    function apriSezione(idSezione) {
        var s = sezionePerId(idSezione);
        if (!s) { return; }
        apriPannello({
            sopra: st.condominio ? st.condominio.DENOMINAZIONE : '',
            titolo: s.TITOLO,
            sotto: s.SOTTOTITOLO,
            ritorno: $('.ct-cartella[data-id="' + idSezione + '"]')[0],
            azioni: s.FL_SCRITTURA === true ? '<button type="button" class="btn btn-primary btn-sm" data-azione="carica" data-sezione="' +
                esc(idSezione) + '"><i class="fa fa-cloud-upload" aria-hidden="true"></i> Carica in questa sezione</button>' : '',
            ricarica: function () {
                dati('documenti', { ID_CONDOMINIO: st.id, ID_SEZIONE: idSezione, STATO: 'TUTTI' }).done(function (j) {
                    var correnti = '', archiviati = '', na = 0;
                    $.each(j.documenti || [], function (i, doc) {
                        if (doc.STATO === 'ARCHIVIATO') { archiviati += voceDocumento(doc, s, false); na++; } else { correnti += voceDocumento(doc, s, false); }
                    });
                    var h = '<h3 class="ct-pannello-blocco">Documenti in evidenza</h3>' +
                        '<ul class="ct-voci">' + (correnti || '<li class="ct-vuoto">Nessun documento corrente.</li>') + '</ul>';
                    if (na) {
                        h += '<details class="ct-storico"><summary>Archivio storico (' + na + ')</summary><ul class="ct-voci">' +
                            archiviati + '</ul></details>';
                    }
                    $('#ct-pannello-corpo').html(h);
                }).fail(function (m) { if (m !== 'sessione') { $('#ct-pannello-corpo').html('<p class="ct-vuoto">' + esc(m) + '</p>'); } });
            }
        });
    }

    function apriArchivio() {
        apriPannello({
            sopra: st.condominio ? st.condominio.DENOMINAZIONE : '',
            titolo: 'Archivio storico',
            sotto: 'Documenti archiviati di tutte le sezioni. Le versioni precedenti dei documenti in evidenza si aprono da "Versioni".',
            ritorno: $('#ct-btn-archivio')[0],
            azioni: '<input type="search" class="form-control input-sm ct-filtro" id="ct-filtro-archivio" placeholder="Filtra per titolo o file" aria-label="Filtra archivio">',
            ricarica: function () {
                dati('documenti', { ID_CONDOMINIO: st.id, STATO: 'ARCHIVIATO' }).done(function (j) {
                    var h = '';
                    $.each(j.documenti || [], function (i, doc) { h += voceDocumento(doc, sezionePerId(doc.ID_SEZIONE), true); });
                    $('#ct-pannello-corpo').html('<ul class="ct-voci">' + (h || '<li class="ct-vuoto">Nessun documento in archivio.</li>') + '</ul>');
                }).fail(function (m) { if (m !== 'sessione') { $('#ct-pannello-corpo').html('<p class="ct-vuoto">' + esc(m) + '</p>'); } });
            }
        });
        $('#ct-pannello-azioni').off('input.ct').on('input.ct', '#ct-filtro-archivio', function () {
            var q = $.trim($(this).val()).toLowerCase();
            $('#ct-pannello-corpo .ct-voce').each(function () {
                $(this).toggle(!q || $(this).text().toLowerCase().indexOf(q) >= 0);
            });
        });
    }

    function azionePannello(e) {
        var $b = $(e.currentTarget), azione = $b.attr('data-azione'), idDoc = numero($b.attr('data-doc'), null);
        if (azione === 'carica') { apriCaricamento(numero($b.attr('data-sezione'), null), null); }
        if (azione === 'versione') { apriCaricamento(null, idDoc); }
        if (azione === 'versioni') { mostraVersioni($b, idDoc); }
        if (azione === 'stato') { cambiaStato(idDoc, $b.attr('data-stato')); }
    }

    function mostraVersioni($b, idDoc) {
        var $box = $b.closest('.ct-voce').find('.ct-versioni');
        if (!$box.hasClass('hidden')) { $box.addClass('hidden'); return; }
        $box.removeClass('hidden').html('<span class="ct-vuoto"><i class="fa fa-spinner fa-spin"></i></span>');
        dati('versioni', { ID_CONDOMINIO: st.id, ID_DOCUMENTO: idDoc }).done(function (j) {
            var h = '<ol class="ct-versioni-elenco">';
            $.each(j.versioni || [], function (i, v) {
                h += '<li' + (v.FL_CORRENTE ? ' class="ct-versione-corrente"' : '') + '><b>v' + esc(v.NUMERO_VERSIONE) + '</b> ' +
                    esc(v.NOME_FILE_ORIGINALE) + ' <span class="ct-voce-meta">' + esc(dataOra(v.TS_INS)) +
                    (v.CARICATO_DA ? ' \u00b7 ' + esc(v.CARICATO_DA) : '') + (v.NOTA ? ' \u00b7 ' + esc(v.NOTA) : '') +
                    (v.FL_CORRENTE ? ' \u00b7 corrente' : '') + '</span> ' +
                    azioniFile({ ID_VERSIONE: v.ID_VERSIONE }, v.NOME_FILE_ORIGINALE) + '</li>';
            });
            $box.html(h + '</ol>');
        }).fail(function (m) { if (m !== 'sessione') { $box.html('<span class="ct-vuoto">' + esc(m) + '</span>'); } });
    }

    function cambiaStato(idDoc, stato) {
        var domanda = stato === 'ARCHIVIATO' ? 'Spostare il documento nell\'archivio storico? Resta consultabile e si puo\' ripristinare.'
            : 'Riportare il documento fra quelli in evidenza?';
        if (!window.confirm(domanda)) { return; }
        $.ajax({
            url: API + '?FUNCTIONID=CentraleTermicaStato', type: 'POST', dataType: 'text',
            data: { ID_CONDOMINIO: st.id, ID_DOCUMENTO: idDoc, STATO: stato }
        }).done(function (t) {
            var j;
            try { j = JSON.parse(t); } catch (e) { sessioneScaduta(); return; }
            if (j.esito === 'OK') { ricarica(); } else { avviso(j.messaggio); }
        }).fail(function (xhr) {
            var m = 'Operazione non riuscita';
            try { m = JSON.parse(xhr.responseText).messaggio || m; } catch (e) { /* non JSON */ }
            window.alert(m);
        });
    }

    // ------------------------------------------------------------------ caricamento

    function preparaCaricamento() {
        var $drop = $('#ct-drop'), $file = $('#ct-carica-file');
        $drop.on('click keydown', function (e) {
            if (e.type === 'click' || e.keyCode === 13 || e.keyCode === 32) {
                if (e.target !== $file[0]) { e.preventDefault(); $file.trigger('click'); }
            }
        });
        $file.on('click', function (e) { e.stopPropagation(); });
        $file.on('change', function () { aggiungiFile(this.files); this.value = ''; });
        $drop.on('dragenter dragover', function (e) { e.preventDefault(); $drop.addClass('ct-drop-sopra'); });
        $drop.on('dragleave drop', function (e) { e.preventDefault(); $drop.removeClass('ct-drop-sopra'); });
        $drop.on('drop', function (e) {
            var dt = e.originalEvent && e.originalEvent.dataTransfer;
            if (dt && dt.files) { aggiungiFile(dt.files); }
        });
        $('#ct-file-scelti').on('click', '[data-togli]', function () {
            st.file.splice(numero($(this).attr('data-togli'), 0), 1);
            disegnaFile();
        });
        $('#ct-form-carica').on('submit', function (e) { e.preventDefault(); inviaCaricamento(); });
        $('#ct-modale-carica').on('hidden.bs.modal', function () { st.file = []; disegnaFile(); });
    }

    function regole() {
        return {
            est: elenco(st.par['FILE.ESTENSIONI']),
            mb: numero(st.par['FILE.DIMENSIONE_MAX_MB'], 25),
            max: numero(st.par['FILE.MAX_PER_CARICAMENTO'], 20)
        };
    }

    function apriCaricamento(idSezione, idDocumento) {
        var r = regole(), versione = !!idDocumento, scrivibili = sezioniScrivibili();
        if (!scrivibili.length) { return; }
        $('#ct-carica-documento').val(versione ? idDocumento : '');
        $('#ct-carica-titolo').text(versione ? 'Nuova versione' : 'Carica documenti');
        $('#ct-carica-sotto').text(st.condominio ? st.condominio.DENOMINAZIONE : '');
        var $sez = $('#ct-carica-sezione').empty();
        $.each(scrivibili, function (i, s) { $sez.append($('<option>').val(s.ID_SEZIONE).text(s.TITOLO)); });
        if (idSezione) { $sez.val(String(idSezione)); }
        $('#ct-gruppo-sezione, #ct-gruppo-titolo, #ct-gruppo-descrizione, #ct-gruppo-data').toggleClass('hidden', versione);
        $('#ct-gruppo-nota').toggleClass('hidden', !versione);
        $('#ct-carica-titolo-doc, #ct-carica-descrizione, #ct-carica-nota, #ct-carica-data').val('');
        $('#ct-carica-file').prop('multiple', !versione).attr('accept', $.map(r.est, function (x) { return '.' + x; }).join(','));
        $('#ct-drop-regole').text((versione ? 'Un solo file' : 'Fino a ' + r.max + ' file') + ', massimo ' + r.mb +
            ' MB ciascuno. Formati: ' + r.est.join(', ') + '.');
        $('#ct-esito').addClass('hidden').empty();
        $('#ct-progresso').addClass('hidden').find('i').css('width', 0);
        st.file = [];
        disegnaFile();
        $('#ct-modale-carica').modal('show');
    }

    function aggiungiFile(lista) {
        var r = regole(), versione = !!$('#ct-carica-documento').val();
        for (var i = 0; i < (lista ? lista.length : 0); i++) {
            var f = lista[i], errore = '';
            if ($.inArray(estensione(f.name), r.est) < 0) { errore = 'formato non ammesso'; }
            else if (f.size > r.mb * 1048576) { errore = 'oltre ' + r.mb + ' MB'; }
            st.file.push({ file: f, errore: errore });
        }
        if (versione && st.file.length > 1) { st.file = st.file.slice(-1); }
        if (st.file.length > r.max) { st.file = st.file.slice(0, r.max); }
        disegnaFile();
    }

    function disegnaFile() {
        var h = '', validi = 0;
        $.each(st.file, function (i, x) {
            if (!x.errore) { validi++; }
            h += '<li class="' + (x.errore ? 'ct-file-ko' : '') + '"><i class="fa ' + icona(x.file.name) + '" aria-hidden="true"></i> ' +
                '<span class="ct-file-nome">' + esc(x.file.name) + '</span> <span class="ct-voce-meta">' + esc(dimensione(x.file.size)) +
                (x.errore ? ' \u00b7 ' + esc(x.errore) : '') + '</span>' +
                '<button type="button" class="ct-togli" data-togli="' + i + '" aria-label="Togli ' + esc(x.file.name) + '">&times;</button></li>';
        });
        $('#ct-file-scelti').html(h);
        $('#ct-carica-invia').prop('disabled', validi === 0).text(validi > 1 ? 'Carica ' + validi + ' file' : 'Carica');
    }

    function inviaCaricamento() {
        var fd = new window.FormData(), idDoc = $('#ct-carica-documento').val(), n = 0;
        fd.append('ID_CONDOMINIO', st.id);
        if (idDoc) {
            fd.append('ID_DOCUMENTO', idDoc);
            fd.append('NOTA', $('#ct-carica-nota').val());
        } else {
            fd.append('ID_SEZIONE', $('#ct-carica-sezione').val());
            fd.append('TITOLO', $('#ct-carica-titolo-doc').val());
            fd.append('DESCRIZIONE', $('#ct-carica-descrizione').val());
            fd.append('DATA_DOCUMENTO', $('#ct-carica-data').val());
        }
        $.each(st.file, function (i, x) { if (!x.errore) { fd.append('file', x.file, x.file.name); n++; } });
        if (!n) { return; }

        var $prog = $('#ct-progresso').removeClass('hidden'), $bar = $prog.find('i').css('width', 0);
        $('#ct-carica-invia').prop('disabled', true);
        var xhr = new window.XMLHttpRequest();
        xhr.open('POST', API + '?FUNCTIONID=CentraleTermicaUpload');
        if (xhr.upload) {
            xhr.upload.onprogress = function (e) { if (e.lengthComputable) { $bar.css('width', Math.round(e.loaded * 100 / e.total) + '%'); } };
        }
        xhr.onload = function () {
            var j;
            try { j = JSON.parse(xhr.responseText); } catch (e) { sessioneScaduta(); return; }
            $bar.css('width', '100%');
            mostraEsito(j);
            if (j.esito === 'OK' || j.esito === 'PARZIALE') { ricarica(); }
            if (j.esito === 'OK') { window.setTimeout(function () { $('#ct-modale-carica').modal('hide'); }, 1200); }
            else { $('#ct-carica-invia').prop('disabled', false); }
        };
        xhr.onerror = function () { mostraEsito({ esito: 'KO', messaggio: 'Errore di comunicazione' }); $('#ct-carica-invia').prop('disabled', false); };
        xhr.send(fd);
    }

    function mostraEsito(j) {
        var h = '';
        if (j.messaggio) { h += '<p>' + esc(j.messaggio) + '</p>'; }
        if (j.file) {
            h += '<ul>';
            $.each(j.file, function (i, f) {
                h += '<li class="' + (f.esito === 'OK' ? 'ct-file-ok' : 'ct-file-ko') + '"><i class="fa ' +
                    (f.esito === 'OK' ? 'fa-check' : 'fa-times') + '" aria-hidden="true"></i> ' + esc(f.nome_file) +
                    (f.esito === 'OK' ? (f.numero_versione > 1 ? ' \u00b7 versione ' + esc(f.numero_versione) : ' \u00b7 caricato')
                        : ' \u00b7 ' + esc(f.messaggio)) + '</li>';
            });
            h += '</ul>';
        }
        $('#ct-esito').removeClass('hidden ct-esito-ok ct-esito-ko').addClass(j.esito === 'OK' ? 'ct-esito-ok' : 'ct-esito-ko').html(h);
    }

    $(avvia);
}(window.jQuery));
