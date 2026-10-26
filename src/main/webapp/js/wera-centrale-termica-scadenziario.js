/* ============================================================
   WERA 2026 - Centrale termica, scadenziario (centraletermicascadenziario.html)
   ------------------------------------------------------------
   Mockup wera_demo "Scadenziario / Documentazione Centrale Termica": tre schede (Scadenze e documenti,
   Calendario, Alerting e storico invii) = tre sottovoci del menu. Dati da CentraleTermicaDati
   (scadenziario, avvisi, tipi_scadenza); scritture: CentraleTermicaUpload, CentraleTermicaScadenza,
   CentraleTermicaConfigurazioneSalva, CentraleTermicaAvvisiInvio. Il server controlla perimetro e
   permessi. ES5 + jQuery 2, ASCII.
   ============================================================ */
(function ($) {
    'use strict';

    var API = 'astro';
    var CHIAVE = 'wera.ct.scadenziario.condominio';
    var FUNZIONI = { scadenze: 'CentraleTermicaScadenze', calendario: 'CentraleTermicaCalendario', avvisi: 'CentraleTermicaAvvisi' };
    var MESI = ['Gennaio', 'Febbraio', 'Marzo', 'Aprile', 'Maggio', 'Giugno', 'Luglio', 'Agosto', 'Settembre', 'Ottobre', 'Novembre', 'Dicembre'];
    var SOGLIE_STANDARD = [90, 60, 30, 15, 7];

    var st = { condomini: [], id: null, scadenze: [], scheda: 'scadenze', anno: 0, mese: 0, avvisi: null, tipi: [], scrive: false };

    function esc(v) {
        if (v === null || v === undefined) { return ''; }
        return String(v).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }
    function data(iso) {
        var m = /^(\d{4})-(\d{2})-(\d{2})/.exec(iso || '');
        return m ? m[3] + '/' + m[2] + '/' + m[1] : '';
    }
    function numero(v, def) { var n = parseInt(v, 10); return isNaN(n) ? def : n; }
    function iso(d) {
        var m = d.getMonth() + 1, g = d.getDate();
        return d.getFullYear() + '-' + (m < 10 ? '0' : '') + m + '-' + (g < 10 ? '0' : '') + g;
    }
    function piuMesi(isoData, mesi) {
        var p = /^(\d{4})-(\d{2})-(\d{2})$/.exec(isoData || '');
        if (!p || !mesi) { return ''; }
        var d = new Date(Number(p[1]), Number(p[2]) - 1 + Number(mesi), Number(p[3]));
        return iso(d);
    }
    function periodicita(m) {
        m = numero(m, 0);
        if (!m) { return 'una tantum'; }
        if (m % 12 === 0) { return 'ogni ' + (m / 12) + (m === 12 ? ' anno' : ' anni'); }
        return 'ogni ' + m + ' mesi';
    }
    function sessioneScaduta() { window.location.href = API + '?FUNCTIONID=Logout'; }
    function avviso(t) { $('#ct-avviso').toggleClass('hidden', !t).removeClass('ct-avviso-ok').addClass('ct-avviso-ko').text(t || ''); }

    function dati(op, extra) {
        var d = $.Deferred();
        $.ajax({ url: API, data: $.extend({ FUNCTIONID: 'CentraleTermicaDati', OPERAZIONE: op }, extra || {}), dataType: 'text', cache: false })
            .done(function (t) {
                var j;
                try { j = JSON.parse(t); } catch (e) { sessioneScaduta(); d.reject('sessione'); return; }
                if (j.esito === 'OK') { d.resolve(j); } else { d.reject(j.messaggio || 'Errore'); }
            })
            .fail(function (xhr) {
                var m = 'Errore di comunicazione';
                try { m = JSON.parse(xhr.responseText).messaggio || m; } catch (e) { /* non JSON */ }
                d.reject(m);
            });
        return d.promise();
    }

    function post(funzione, param) {
        var d = $.Deferred();
        $.ajax({ url: API + '?FUNCTIONID=' + funzione, type: 'POST', data: param, dataType: 'text' })
            .done(function (t) {
                var j;
                try { j = JSON.parse(t); } catch (e) { sessioneScaduta(); d.reject('sessione'); return; }
                if (j.esito === 'OK') { d.resolve(j); } else { d.reject(j.messaggio || 'Errore'); }
            })
            .fail(function (xhr) {
                var m = 'Operazione non riuscita';
                try { m = JSON.parse(xhr.responseText).messaggio || m; } catch (e) { /* non JSON */ }
                d.reject(m);
            });
        return d.promise();
    }

    function stato(s) {
        if (s === 'SCADUTA') { return { cls: 'ct-chip-ko', txt: 'Scaduto', k: 'ko' }; }
        if (s === 'IN_SCADENZA') { return { cls: 'ct-chip-warn', txt: 'In scadenza', k: 'warn' }; }
        return { cls: 'ct-chip-ok', txt: 'Valido', k: 'ok' };
    }

    function urlDownload(param) { return API + '?' + $.param($.extend({ FUNCTIONID: 'CentraleTermicaDownload' }, param)); }

    // ------------------------------------------------------------------ avvio

    $(function () {
        var $p = $('#cts-page');
        st.scheda = FUNZIONI[$p.attr('data-scheda')] ? $p.attr('data-scheda') : 'scadenze';
        var oggi = new Date();
        st.anno = oggi.getFullYear();
        st.mese = oggi.getMonth();

        $('.cts-schede .nav-tabs a').on('click', function (e) { e.preventDefault(); mostraScheda($(this).attr('data-scheda')); });
        $('#cts-condominio').on('change', function () { scegli(numero($(this).val(), null)); });
        $('#cts-cerca').on('input', disegnaTabella);
        $('#cts-mese-prec').on('click', function () { spostaMese(-1); });
        $('#cts-mese-succ').on('click', function () { spostaMese(1); });
        $('#cts-righe').on('click', '[data-azione]', azioneRiga);
        $('#cts-btn-carica').on('click', function () { apriModale('carica', null); });
        $('#cts-nuova').on('click', function () { apriModale('crea', null); });
        $('#cts-m-condominio').on('change', function () { caricaTipi(numero($(this).val(), null), null); });
        $('#cts-m-tipo, #cts-m-data').on('change', proponiScadenza);
        $('#cts-form').on('submit', function (e) { e.preventDefault(); salvaModale(); });
        $('#cts-salva-alert').on('click', salvaAlert);
        $('#cts-controlla').on('click', controllaOra);
        $('#cts-calgrid').on('click', '[data-condominio]', function () {
            window.location.href = API + '?FUNCTIONID=CentraleTermica&ID_CONDOMINIO=' + encodeURIComponent($(this).attr('data-condominio'));
        });

        mostraScheda(st.scheda);
        dati('condomini').done(function (j) {
            st.condomini = j.condomini || [];
            var $s = $('#cts-condominio').empty().append($('<option>').val('').text('Tutti i condomini'));
            $.each(st.condomini, function (i, c) { $s.append($('<option>').val(c.ID_CONDOMINIO).text(c.DENOMINAZIONE + (c.LOCALITA ? ' - ' + c.LOCALITA : ''))); });
            if ($.fn.select2) { $s.select2({ width: '100%' }); }
            var scelto = numero($p.attr('data-id-condominio'), null), ultimo = null;
            try { ultimo = numero(window.localStorage.getItem(CHIAVE), null); } catch (e) { /* storage assente */ }
            var ids = $.map(st.condomini, function (c) { return c.ID_CONDOMINIO; });
            if ($.inArray(scelto, ids) < 0) { scelto = $.inArray(ultimo, ids) >= 0 ? ultimo : null; }
            $s.val(scelto ? String(scelto) : '').trigger('change.select2');
            scegli(scelto);
        }).fail(function (m) { if (m !== 'sessione') { avviso(m); } });
    });

    function mostraScheda(s) {
        st.scheda = s;
        $('.cts-schede .nav-tabs li').removeClass('active');
        $('.cts-schede .nav-tabs a[data-scheda="' + s + '"]').parent().addClass('active');
        $('.cts-schede .tab-pane').removeClass('active');
        $('#cts-' + s).addClass('active');
        // sottovoce attiva nel menu e URL coerente con la scheda
        $('#ct-voce-menu .nav-second-level li').removeClass('active');
        $('#ct-voce-menu .nav-second-level a[href$="FUNCTIONID=' + FUNZIONI[s] + '"]').parent().addClass('active');
        aggiornaUrl();
        if (s === 'calendario') { disegnaCalendario(); }
        if (s === 'avvisi' && !st.avvisi && st.condomini.length) { caricaAvvisi(); }
    }

    function aggiornaUrl() {
        if (window.history && window.history.replaceState) {
            window.history.replaceState(null, '', API + '?FUNCTIONID=' + FUNZIONI[st.scheda] + (st.id ? '&ID_CONDOMINIO=' + st.id : ''));
        }
    }

    function scegli(id) {
        st.id = id;
        st.avvisi = null;
        try { window.localStorage.setItem(CHIAVE, id ? String(id) : ''); } catch (e) { /* storage assente */ }
        aggiornaUrl();
        carica();
        if (st.scheda === 'avvisi') { caricaAvvisi(); }
    }

    function carica() {
        var p = st.id ? { ID_CONDOMINIO: st.id } : {};
        dati('scadenziario', p).done(function (j) {
            st.scadenze = j.scadenze || [];
            st.scrive = $.grep(st.scadenze, function (s) { return s.FL_SCRITTURA === true; }).length > 0;
            var v = 0, w = 0, k = 0;
            $.each(st.scadenze, function (i, s) { var x = stato(s.SEMAFORO).k; if (x === 'ok') { v++; } else if (x === 'warn') { w++; } else { k++; } });
            $('#cts-validi').text(v);
            $('#cts-in-scadenza').text(w);
            $('#cts-scaduti').text(k);
            if (st.scadenze.length) { $('#cts-preavviso').text(st.scadenze[0].GIORNI_PREAVVISO); }
            disegnaTabella();
            disegnaCalendario();
            // caricamento e nuova scadenza: solo se il profilo scrive in almeno una sezione con tipi di scadenza
            permessoScrittura();
        }).fail(function (m) { if (m !== 'sessione') { avviso(m); } });
    }

    function permessoScrittura() {
        var id = st.id || (st.condomini[0] && st.condomini[0].ID_CONDOMINIO);
        if (!id) { return; }
        dati('tipi_scadenza', { ID_CONDOMINIO: id }).done(function (j) {
            var ok = $.grep(j.tipi || [], function (t) { return t.FL_SCRITTURA === true; }).length > 0;
            $('#cts-carica, #cts-nuova').toggleClass('hidden', !ok);
        });
    }

    // ------------------------------------------------------------------ scadenze e documenti

    function disegnaTabella() {
        var q = $.trim($('#cts-cerca').val() || '').toLowerCase(), h = '';
        $.each(st.scadenze, function (i, s) {
            var testo = (s.SIGLA + ' ' + s.DESCRIZIONE + ' ' + s.DENOMINAZIONE + ' ' + (s.ENTE || '')).toLowerCase();
            if (q && testo.indexOf(q) < 0) { return; }
            var x = stato(s.SEMAFORO);
            var az = '';
            if (s.ID_DOCUMENTO_ULTIMO) {
                az += '<a class="btn btn-white btn-xs" title="Scarica l\'ultimo documento" href="' + esc(urlDownload({ ID_DOCUMENTO: s.ID_DOCUMENTO_ULTIMO })) +
                    '"><i class="fa fa-file-pdf-o" aria-hidden="true"></i></a> ';
            }
            if (s.FL_SCRITTURA === true) {
                az += '<button type="button" class="btn btn-white btn-xs" data-azione="rinnova" data-id="' + esc(s.ID_SCADENZA) +
                    '" title="Registra esecuzione e rinnovo"><i class="fa fa-refresh" aria-hidden="true"></i></button> ' +
                    '<button type="button" class="btn btn-white btn-xs" data-azione="annulla" data-id="' + esc(s.ID_SCADENZA) +
                    '" title="Annulla la scadenza"><i class="fa fa-ban" aria-hidden="true"></i></button>';
            }
            h += '<tr><td><b>' + esc(s.SIGLA) + '</b> \u00b7 ' + esc(s.DESCRIZIONE) + '</td>' +
                '<td><a href="' + API + '?FUNCTIONID=CentraleTermica&ID_CONDOMINIO=' + esc(s.ID_CONDOMINIO) + '">' + esc(s.DENOMINAZIONE) + '</a></td>' +
                '<td class="hidden-xs">' + esc(s.ENTE) + '</td><td class="hidden-xs ct-nowrap">' + esc(periodicita(s.PERIODICITA_MESI)) + '</td>' +
                '<td class="hidden-xs ct-nowrap">' + esc(data(s.ULTIMA_ESECUZIONE)) + '</td><td class="ct-nowrap"><b>' + esc(data(s.DATA_SCADENZA)) + '</b></td>' +
                '<td><span class="ct-chip ' + x.cls + '"><i class="cts-punto cts-' + x.k + '"></i> ' + x.txt + '</span></td>' +
                '<td class="text-right ct-nowrap">' + az + '</td></tr>';
        });
        $('#cts-righe').html(h || '<tr><td colspan="8" class="ct-vuoto">' + (st.scadenze.length ? 'Nessuna scadenza corrisponde al filtro.' :
            'Nessuna scadenza registrata. Usa "Carica documento" o "Nuova scadenza" per iniziare.') + '</td></tr>');
    }

    function scadenzaPerId(id) {
        for (var i = 0; i < st.scadenze.length; i++) { if (st.scadenze[i].ID_SCADENZA === id) { return st.scadenze[i]; } }
        return null;
    }

    function azioneRiga(e) {
        var $b = $(e.currentTarget), s = scadenzaPerId(numero($b.attr('data-id'), null));
        if (!s) { return; }
        if ($b.attr('data-azione') === 'rinnova') { apriModale('rinnova', s); }
        if ($b.attr('data-azione') === 'annulla') {
            var nota = window.prompt('Annullare la scadenza ' + s.SIGLA + ' di ' + s.DENOMINAZIONE + '? Motivo (facoltativo):', '');
            if (nota === null) { return; }
            post('CentraleTermicaScadenza', { AZIONE: 'annulla', ID_CONDOMINIO: s.ID_CONDOMINIO, ID_SCADENZA: s.ID_SCADENZA, NOTA: nota })
                .done(carica).fail(function (m) { if (m !== 'sessione') { avviso(m); } });
        }
    }

    // ------------------------------------------------------------------ finestra: carica / rinnova / nuova

    function apriModale(modo, s) {
        st.modo = modo;
        $('#cts-id-scadenza').val(s ? s.ID_SCADENZA : '');
        $('#cts-m-esito, #cts-progresso').addClass('hidden');
        $('#cts-m-file, #cts-m-nota, #cts-m-prossima').val('');
        $('#cts-m-data').val(iso(new Date()));
        var titoli = { carica: 'Carica un documento della centrale termica', rinnova: 'Registra esecuzione e rinnovo', crea: 'Nuova scadenza' };
        $('#cts-modale-titolo').text(titoli[modo]);
        $('#cts-modale-sotto').text(s ? s.SIGLA + ' \u00b7 ' + s.DESCRIZIONE + ' \u00b7 ' + s.DENOMINAZIONE : '');
        $('#cts-m-file, #cts-m-data').closest('.form-group').toggle(modo !== 'crea');
        $('label[for="cts-m-prossima"]').text(modo === 'crea' ? 'Data di scadenza' : 'Nuova data di scadenza');
        $('#cts-m-condominio, #cts-m-tipo').prop('disabled', modo === 'rinnova');
        var $c = $('#cts-m-condominio').empty();
        $.each(st.condomini, function (i, c) { $c.append($('<option>').val(c.ID_CONDOMINIO).text(c.DENOMINAZIONE)); });
        var idc = s ? s.ID_CONDOMINIO : (st.id || (st.condomini[0] && st.condomini[0].ID_CONDOMINIO));
        $c.val(String(idc));
        caricaTipi(idc, s ? s.ID_TIPO_SCADENZA : null);
        $('#cts-m-invia').prop('disabled', false).text(modo === 'crea' ? 'Registra' : 'Salva');
        $('#cts-modale').modal('show');
    }

    function caricaTipi(idCondominio, idTipo) {
        var $t = $('#cts-m-tipo').empty();
        if (!idCondominio) { return; }
        dati('tipi_scadenza', { ID_CONDOMINIO: idCondominio }).done(function (j) {
            st.tipi = $.grep(j.tipi || [], function (t) { return t.FL_SCRITTURA === true; });
            $.each(st.tipi, function (i, t) { $t.append($('<option>').val(t.ID_TIPO_SCADENZA).text(t.DESCRIZIONE + ' (' + t.TITOLO_SEZIONE + ')')); });
            if (idTipo) { $t.val(String(idTipo)); }
            proponiScadenza();
        });
    }

    function tipoScelto() {
        var id = numero($('#cts-m-tipo').val(), null), t = null;
        $.each(st.tipi, function (i, x) { if (x.ID_TIPO_SCADENZA === id) { t = x; } });
        return t;
    }

    /** Nuova data proposta = data del documento + periodicita' del tipo (modificabile). */
    function proponiScadenza() {
        var t = tipoScelto();
        $('#cts-m-tipo-info').text(t ? 'Sezione: ' + t.TITOLO_SEZIONE + ' \u00b7 ' + periodicita(t.PERIODICITA_MESI) +
            (t.RIFERIMENTO_NORMATIVO ? ' \u00b7 ' + t.RIFERIMENTO_NORMATIVO : '') : '');
        if (t && st.modo !== 'crea') { $('#cts-m-prossima').val(piuMesi($('#cts-m-data').val(), t.PERIODICITA_MESI)); }
    }

    function esito(testo, ok) {
        $('#cts-m-esito').removeClass('hidden ct-esito-ok ct-esito-ko').addClass(ok ? 'ct-esito-ok' : 'ct-esito-ko').text(testo);
        if (!ok) { $('#cts-m-invia').prop('disabled', false); }
    }

    function salvaModale() {
        var t = tipoScelto(), idc = numero($('#cts-m-condominio').val(), null);
        var file = $('#cts-m-file')[0].files[0], dataDoc = $('#cts-m-data').val(), prossima = $('#cts-m-prossima').val();
        if (!t || !idc) { esito('Scegli condominio e tipo documento.', false); return; }
        if (st.modo === 'crea' && !prossima) { esito('Indica la data di scadenza.', false); return; }
        $('#cts-m-invia').prop('disabled', true);

        if (st.modo === 'crea') {
            post('CentraleTermicaScadenza', { AZIONE: 'crea', ID_CONDOMINIO: idc, ID_TIPO_SCADENZA: t.ID_TIPO_SCADENZA, DATA_SCADENZA: prossima,
                NOTA: $('#cts-m-nota').val() }).done(fatto).fail(function (m) { if (m !== 'sessione') { esito(m, false); } });
            return;
        }
        var aperta = st.modo === 'rinnova' ? scadenzaPerId(numero($('#cts-id-scadenza').val(), null)) : null;
        if (!aperta) {
            $.each(st.scadenze, function (i, s) { if (s.ID_CONDOMINIO === idc && s.ID_TIPO_SCADENZA === t.ID_TIPO_SCADENZA) { aperta = s; } });
        }
        var dopoDocumento = function (idDocumento) {
            if (aperta) {
                post('CentraleTermicaScadenza', { AZIONE: 'chiudi', ID_CONDOMINIO: idc, ID_SCADENZA: aperta.ID_SCADENZA, DATA_ESECUZIONE: dataDoc,
                    ID_DOCUMENTO: idDocumento || '', DATA_PROSSIMA: prossima, NOTA: $('#cts-m-nota').val() })
                    .done(fatto).fail(function (m) { if (m !== 'sessione') { esito(m, false); } });
            } else if (idDocumento && dataDoc) {
                // primo documento di questo adempimento: si registra l'esecuzione (chiusa, con il documento)
                // e da li' la prossima scadenza, cosi' la riga mostra subito "Ultimo" e il PDF
                post('CentraleTermicaScadenza', { AZIONE: 'crea', ID_CONDOMINIO: idc, ID_TIPO_SCADENZA: t.ID_TIPO_SCADENZA, DATA_SCADENZA: dataDoc })
                    .done(function (c) {
                        post('CentraleTermicaScadenza', { AZIONE: 'chiudi', ID_CONDOMINIO: idc, ID_SCADENZA: c.id_scadenza, DATA_ESECUZIONE: dataDoc,
                            ID_DOCUMENTO: idDocumento, DATA_PROSSIMA: prossima, NOTA: $('#cts-m-nota').val() })
                            .done(fatto).fail(function (m) { if (m !== 'sessione') { esito(m, false); } });
                    }).fail(function (m) { if (m !== 'sessione') { esito(m, false); } });
            } else if (prossima) {
                post('CentraleTermicaScadenza', { AZIONE: 'crea', ID_CONDOMINIO: idc, ID_TIPO_SCADENZA: t.ID_TIPO_SCADENZA, DATA_SCADENZA: prossima,
                    NOTA: $('#cts-m-nota').val() }).done(fatto).fail(function (m) { if (m !== 'sessione') { esito(m, false); } });
            } else {
                fatto({});
            }
        };
        if (!file) { dopoDocumento(null); return; }

        var fd = new window.FormData();
        fd.append('ID_CONDOMINIO', idc);
        fd.append('ID_SEZIONE', t.ID_SEZIONE);
        fd.append('TITOLO', (t.DESCRIZIONE + ' ' + data(dataDoc)).substring(0, 200));
        fd.append('DATA_DOCUMENTO', dataDoc);
        fd.append('file', file, file.name);
        var $prog = $('#cts-progresso').removeClass('hidden'), $bar = $prog.find('i').css('width', 0);
        var xhr = new window.XMLHttpRequest();
        xhr.open('POST', API + '?FUNCTIONID=CentraleTermicaUpload');
        if (xhr.upload) { xhr.upload.onprogress = function (e) { if (e.lengthComputable) { $bar.css('width', Math.round(e.loaded * 100 / e.total) + '%'); } }; }
        xhr.onload = function () {
            var j;
            try { j = JSON.parse(xhr.responseText); } catch (e) { sessioneScaduta(); return; }
            var f = j.file && j.file[0];
            if (j.esito !== 'OK' || !f || f.esito !== 'OK') { esito((f && f.messaggio) || j.messaggio || 'Caricamento non riuscito', false); return; }
            dopoDocumento(f.id_documento);
        };
        xhr.onerror = function () { esito('Errore di comunicazione', false); };
        xhr.send(fd);
    }

    function fatto(j) {
        esito(j && j.data_scadenza_successiva ? 'Fatto. Prossima scadenza: ' + data(j.data_scadenza_successiva) : 'Fatto.', true);
        carica();
        window.setTimeout(function () { $('#cts-modale').modal('hide'); }, 1200);
    }

    // ------------------------------------------------------------------ calendario

    function spostaMese(d) {
        st.mese += d;
        if (st.mese < 0) { st.mese = 11; st.anno--; }
        if (st.mese > 11) { st.mese = 0; st.anno++; }
        disegnaCalendario();
    }

    function disegnaCalendario() {
        $('#cts-mese').text(MESI[st.mese] + ' ' + st.anno);
        var perGiorno = {};
        $.each(st.scadenze, function (i, s) { (perGiorno[s.DATA_SCADENZA] = perGiorno[s.DATA_SCADENZA] || []).push(s); });
        var primo = new Date(st.anno, st.mese, 1), inizio = (primo.getDay() + 6) % 7, giorni = new Date(st.anno, st.mese + 1, 0).getDate();
        var oggi = iso(new Date()), h = '';
        $.each(['L', 'M', 'M', 'G', 'V', 'S', 'D'], function (i, g) { h += '<div class="cts-dow">' + g + '</div>'; });
        for (var i = 0; i < inizio; i++) { h += '<div class="cts-giorno cts-vuoto"></div>'; }
        for (var g = 1; g <= giorni; g++) {
            var chiave = iso(new Date(st.anno, st.mese, g)), voci = '';
            $.each(perGiorno[chiave] || [], function (k, s) {
                var x = stato(s.SEMAFORO);
                voci += '<button type="button" class="cts-evento cts-' + x.k + '" data-condominio="' + esc(s.ID_CONDOMINIO) + '" title="' +
                    esc(s.DENOMINAZIONE + ' \u2014 ' + s.DESCRIZIONE + ' \u2014 scad. ' + data(s.DATA_SCADENZA)) + '">' +
                    esc(s.SIGLA) + ' \u00b7 ' + esc(s.DENOMINAZIONE) + '</button>';
            });
            h += '<div class="cts-giorno' + (chiave === oggi ? ' cts-oggi' : '') + '"><span class="cts-num">' + g + '</span>' + voci + '</div>';
        }
        $('#cts-calgrid').html(h);
    }

    // ------------------------------------------------------------------ alerting e storico invii

    function caricaAvvisi() {
        var p = st.id ? { ID_CONDOMINIO: st.id } : {};
        dati('avvisi', p).done(function (j) {
            st.avvisi = j.avvisi;
            var a = st.avvisi, c = a.config || {}, soglie = $.map(String(c['ALERT.GIORNI'] || '').split(','), function (x) { var n = numero(x, null); return n === null ? null : n; });
            var tutte = SOGLIE_STANDARD.slice();
            $.each(soglie, function (i, n) { if ($.inArray(n, tutte) < 0) { tutte.push(n); } });
            tutte.sort(function (x, y) { return y - x; });
            var h = '';
            $.each(tutte, function (i, n) {
                h += '<label class="cts-spunta"><input type="checkbox" class="cts-soglia" value="' + n + '"' + ($.inArray(n, soglie) >= 0 ? ' checked' : '') + '> ' + n + ' giorni prima</label>';
            });
            $('#cts-soglie').html(h);
            $('#cts-scadute').prop('checked', c['ALERT.SCADUTE'] === 'S');
            $('#cts-amministratore').prop('checked', c['ALERT.AMMINISTRATORE'] === 'S');
            $('#cts-cc').val(c['ALERT.CC'] || '');
            $('#cts-attivo').prop('checked', c['ALERT.ATTIVO'] === 'S');
            $('#cts-avvisi input').prop('disabled', a.puo_configurare !== true);
            $('#cts-salva-alert, #cts-controlla').toggle(a.puo_configurare === true);
            $('#cts-mail-prova').toggleClass('hidden', a.mail_di_prova !== true)
                .text('Server di posta di prova: gli avvisi vengono preparati e registrati, ma le mail sono raccolte sul server e non arrivano ai destinatari.');
            disegnaInvii(a.invii || []);
        }).fail(function (m) { if (m !== 'sessione') { avviso(m); } });
    }

    function disegnaInvii(invii) {
        var h = '';
        $.each(invii, function (i, r) {
            var x = r.ESITO === 'INVIATO' ? ['ct-chip-ok', 'Inviato'] : (r.ESITO === 'ERRORE' ? ['ct-chip-ko', 'Errore'] : ['ct-chip-neutro', 'Saltato']);
            var anticipo = numero(r.SOGLIA_GIORNI, 0) < 0 ? 'scaduto' : r.SOGLIA_GIORNI + ' gg';
            h += '<tr><td class="ct-nowrap">' + esc(data(r.TS_INVIO)) + '</td><td>' + esc(r.DENOMINAZIONE) + '</td><td>' + esc(r.SIGLA) + ' \u2013 ' +
                esc(r.DESCRIZIONE) + '</td><td class="ct-nowrap">' + esc(anticipo) + '</td><td class="hidden-xs cts-dest-txt">' + esc(r.DESTINATARI || r.DETTAGLIO) +
                '</td><td><span class="ct-chip ' + x[0] + '" title="' + esc(r.DETTAGLIO || '') + '">' + x[1] + '</span></td></tr>';
        });
        $('#cts-invii').html(h || '<tr><td colspan="6" class="ct-vuoto">Nessun avviso inviato finora.</td></tr>');
    }

    function messaggioAlert(testo, ok) {
        $('#cts-esito-alert').removeClass('hidden ct-esito-ok ct-esito-ko').addClass(ok ? 'ct-esito-ok' : 'ct-esito-ko').text(testo);
    }

    function salvaAlert() {
        var soglie = $('.cts-soglia:checked').map(function () { return this.value; }).get().join(',');
        var valori = [['ALERT.GIORNI', soglie], ['ALERT.SCADUTE', $('#cts-scadute').is(':checked') ? 'S' : 'N'],
            ['ALERT.AMMINISTRATORE', $('#cts-amministratore').is(':checked') ? 'S' : 'N'], ['ALERT.CC', $('#cts-cc').val()],
            ['ALERT.ATTIVO', $('#cts-attivo').is(':checked') ? 'S' : 'N']];
        var i = 0, prossimo = function () {
            if (i >= valori.length) { messaggioAlert('Configurazione salvata.', true); caricaAvvisi(); return; }
            var v = valori[i++];
            post('CentraleTermicaConfigurazioneSalva', { OGGETTO: 'parametro', CHIAVE: v[0], VALORE: v[1] }).done(prossimo)
                .fail(function (m) { if (m !== 'sessione') { messaggioAlert(m, false); } });
        };
        prossimo();
    }

    function controllaOra() {
        $('#cts-controlla').prop('disabled', true);
        post('CentraleTermicaAvvisiInvio', {}).done(function (j) {
            messaggioAlert('Controllo eseguito: ' + j.esaminate + ' scadenze esaminate, ' + j.inviati + ' avvisi inviati, ' + j.saltati +
                ' senza destinatario, ' + j.errori + ' errori' + (j.mail_di_prova ? ' (server di posta di prova).' : '.'), j.errori === 0);
            caricaAvvisi();
        }).fail(function (m) { if (m !== 'sessione') { messaggioAlert(m, false); } })
            .always(function () { $('#cts-controlla').prop('disabled', false); });
    }
}(window.jQuery));
