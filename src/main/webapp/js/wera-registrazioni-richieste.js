/*
 * wera2026 - Richieste di registrazione (registrazionirichieste.html), solo profili di APPROVAZIONE.PROFILI.
 * Dati: RegistrazioniDati (elenco, dettaglio, foto). Azioni: RegistrazioniEsito (verifica, approva, rifiuta,
 * annulla). I permessi li controlla il server; qui solo la presentazione. ASCII: accenti come entita' HTML.
 */
(function ($) {
    'use strict';

    if (!$) {
        return;
    }
    var API = 'astro';
    var $pagina = $('#reg-page');
    if (!$pagina.length) {
        return;
    }

    var stato = 'IN_ATTESA';
    var aperta = parseInt($pagina.attr('data-id-richiesta'), 10) || null;

    var ETICHETTE_STATO = {
        IN_ATTESA: ['In attesa', 'ct-chip-warn'],
        APPROVATA: ['Approvata', 'ct-chip-ok'],
        RIFIUTATA: ['Rifiutata', 'ct-chip-ko'],
        ANNULLATA: ['Annullata', 'ct-chip-neutro']
    };
    var ESITI = [
        ['DA_VERIFICARE', 'Da verificare'],
        ['COMPATIBILE', 'Compatibili'],
        ['PARZIALE', 'Compatibili in parte'],
        ['NON_COMPATIBILE', 'Non compatibili']
    ];

    function esc(v) {
        return $('<div>').text(v == null ? '' : String(v)).html();
    }

    function data(v, conOra) {
        if (!v) {
            return '';
        }
        var m = /^(\d{4})-(\d{2})-(\d{2})(?:T(\d{2}):(\d{2}))?/.exec(v);
        if (!m) {
            return esc(v);
        }
        return m[3] + '/' + m[2] + '/' + m[1] + (conOra && m[4] ? ' ' + m[4] + ':' + m[5] : '');
    }

    function chipStato(s) {
        var e = ETICHETTE_STATO[s] || [s, 'ct-chip-neutro'];
        return '<span class="ct-chip ' + e[1] + '">' + esc(e[0]) + '</span>';
    }

    function etichettaEsito(codice) {
        for (var i = 0; i < ESITI.length; i++) {
            if (ESITI[i][0] === codice) {
                return ESITI[i][1];
            }
        }
        return codice || '';
    }

    function avviso(testo, tipo) {
        var $a = $('#reg-avviso');
        if (!testo) {
            $a.addClass('hidden').text('');
            return;
        }
        $a.removeClass('hidden ct-avviso-ok ct-avviso-ko ct-avviso-info').addClass('ct-avviso-' + (tipo || 'info')).html(testo);
        $('html, body').animate({ scrollTop: $a.offset().top - 90 }, 200);
    }

    function messaggioErrore(xhr) {
        try {
            var j = JSON.parse(xhr.responseText);
            if (j && j.messaggio) {
                return esc(j.messaggio);
            }
        } catch (e) {
            // risposta non JSON
        }
        return 'Operazione non riuscita (errore ' + xhr.status + ').';
    }

    // ------------------------------------------------------------------ elenco

    function caricaElenco() {
        $.ajax({ url: API, data: { FUNCTIONID: 'RegistrazioniDati', OPERAZIONE: 'elenco', STATO: stato }, dataType: 'json', cache: false })
            .done(function (j) {
                $.each(j.conteggi || {}, function (k, n) {
                    $('[data-conta="' + k + '"]').text(n);
                });
                $('#reg-menu-conta').text(j.conteggi && j.conteggi.IN_ATTESA ? j.conteggi.IN_ATTESA : '')
                    .toggle(!!(j.conteggi && j.conteggi.IN_ATTESA));
                disegnaElenco(j.richieste || []);
            })
            .fail(function (xhr) {
                $('#reg-voci').html('<li class="ct-vuoto">' + messaggioErrore(xhr) + '</li>');
            });
    }

    function disegnaElenco(righe) {
        var $ul = $('#reg-voci').empty();
        if (!righe.length) {
            $ul.append('<li class="ct-vuoto">Nessuna richiesta' + (stato === 'TUTTE' ? '' : ' in questo stato') + '.</li>');
            return;
        }
        $.each(righe, function (i, r) {
            var giorni = r.giorni_dalla_richiesta;
            var quando = giorni === 0 ? 'oggi' : (giorni === 1 ? 'ieri' : giorni + ' giorni fa');
            var $li = $('<li class="reg-voce" tabindex="0" role="button"></li>').attr('data-id', r.id_richiesta);
            $li.html(
                '<div class="reg-voce-testa"><b>' + esc(r.denominazione) + '</b>' + chipStato(r.stato) + '</div>'
                + '<div class="reg-voce-meta">n. ' + esc(r.id_richiesta) + ' &middot; ' + esc(r.referente_nome) + ' '
                + esc(r.referente_cognome) + ' &middot; ' + esc(r.citta) + ' (' + esc(r.provincia) + ')</div>'
                + '<div class="reg-voce-meta">' + (r.stato === 'IN_ATTESA' ? '<span class="reg-attesa'
                + (giorni >= 2 ? ' reg-attesa-lunga' : '') + '">' + quando + '</span>' : data(r.ts_esito || r.ts_ins))
                + (r.numero_allegati ? ' &middot; <i class="fa fa-camera" aria-hidden="true"></i> ' + esc(r.numero_allegati) : '')
                + ' &middot; ripartitori: ' + esc(etichettaEsito(r.esito_compatibilita)) + '</div>'
            );
            if (r.id_richiesta === aperta) {
                $li.addClass('on');
            }
            $ul.append($li);
        });
    }

    // ------------------------------------------------------------------ dettaglio

    function apri(id) {
        aperta = id;
        $('#reg-voci .reg-voce').removeClass('on').filter('[data-id="' + id + '"]').addClass('on');
        $('#reg-dettaglio').html('<p class="ct-vuoto">Caricamento&hellip;</p>');
        $.ajax({ url: API, data: { FUNCTIONID: 'RegistrazioniDati', OPERAZIONE: 'dettaglio', ID_RICHIESTA: id }, dataType: 'json', cache: false })
            .done(disegnaDettaglio)
            .fail(function (xhr) {
                $('#reg-dettaglio').html('<p class="ct-vuoto">' + messaggioErrore(xhr) + '</p>');
            });
        if (window.history && history.replaceState) {
            history.replaceState(null, '', API + '?FUNCTIONID=RegistrazioniRichieste&ID_RICHIESTA=' + id);
        }
    }

    function riga(etichetta, valore, html) {
        if (valore == null || valore === '') {
            return '';
        }
        return '<div class="reg-riga"><dt>' + etichetta + '</dt><dd>' + (html ? valore : esc(valore)) + '</dd></div>';
    }

    function disegnaDettaglio(j) {
        var r = j.richiesta;
        var c = j.controlli || {};
        var attesa = r.stato === 'IN_ATTESA';
        var studio = r.tipo_soggetto === 'STUDIO';
        var h = [];

        h.push('<div class="reg-det-testa"><div><div class="ct-label">Richiesta n. ' + esc(r.id_richiesta) + ' del '
            + data(r.ts_ins, true) + '</div><h2>' + esc(r.denominazione) + '</h2>'
            + '<div class="reg-det-sotto">' + (studio ? 'Studio o societ&agrave;' : 'Amministratore singolo') + '</div></div>'
            + chipStato(r.stato) + '</div>');

        // esito gia' dato
        if (r.stato === 'APPROVATA') {
            h.push('<div class="ct-avviso ct-avviso-ok">Approvata il ' + data(r.ts_esito, true)
                + (r.nome_utente_esito ? ' da ' + esc(r.nome_utente_esito) : '') + '. Utente <b>' + esc(r.username_utente)
                + '</b> (profilo ' + esc(c.profilo) + '). <a href="astro?FUNCTIONID=RicercaAnagraficaCondomini">Collega i suoi condom&igrave;ni</a>'
                + ' dall\'anagrafica condominio, campo Amministratore.</div>');
        } else if (r.stato === 'RIFIUTATA' || r.stato === 'ANNULLATA') {
            h.push('<div class="ct-avviso ct-avviso-ko">' + (r.stato === 'RIFIUTATA' ? 'Rifiutata' : 'Annullata') + ' il '
                + data(r.ts_esito, true) + (r.nome_utente_esito ? ' da ' + esc(r.nome_utente_esito) : '')
                + (r.motivo_rifiuto ? '. Motivo: ' + esc(r.motivo_rifiuto) : '') + '</div>');
        }

        // controlli automatici
        var allarmi = [];
        if (c.utenti_stessa_email && c.utenti_stessa_email.length && attesa) {
            allarmi.push('Esiste gi&agrave; un utente con questa email (' + esc(c.utenti_stessa_email[0].username)
                + '): l\'approvazione verrebbe rifiutata.');
        }
        if (c.richieste_stessa_piva && c.richieste_stessa_piva.length) {
            var altre = $.map(c.richieste_stessa_piva, function (x) {
                return 'n. ' + esc(x.id_richiesta) + ' (' + esc((ETICHETTE_STATO[x.stato] || [x.stato])[0].toLowerCase())
                    + (x.username_utente ? ', utente ' + esc(x.username_utente) : '') + ')';
            });
            allarmi.push('Altre richieste con la stessa partita IVA: ' + altre.join(', ') + '.');
        }
        if (allarmi.length) {
            h.push('<div class="ct-avviso ct-avviso-info"><i class="fa fa-exclamation-triangle" aria-hidden="true"></i> '
                + allarmi.join('<br>') + '</div>');
        }

        h.push('<div class="reg-blocchi">');
        h.push('<div class="reg-blocco"><h3>Dati fiscali</h3><dl>'
            + riga('Partita IVA', r.partita_iva) + riga('Codice fiscale', r.codice_fiscale)
            + riga('N. registro', r.numero_registro) + riga('PEC', r.pec ? '<a href="mailto:' + esc(r.pec) + '">'
                + esc(r.pec) + '</a>' : '', true)
            + riga('Codice SDI', r.codice_sdi)
            + riga('Indirizzo', r.indirizzo + ', ' + r.cap + ' ' + r.citta + ' (' + r.provincia + ')') + '</dl></div>');
        h.push('<div class="reg-blocco"><h3>Referente</h3><dl>'
            + riga('Nome', r.referente_nome + ' ' + r.referente_cognome)
            + riga('Email', '<a href="mailto:' + esc(r.email) + '">' + esc(r.email) + '</a> <span class="reg-nota">sar&agrave; il nome utente</span>', true)
            + riga('Telefono', '<a href="tel:' + esc(String(r.telefono).replace(/[^0-9+]/g, '')) + '">' + esc(r.telefono) + '</a>', true)
            + riga('Consenso', data(r.privacy_ts, true)) + '</dl></div>');
        h.push('</div>');

        h.push('<div class="reg-blocco"><h3>Condom&igrave;ni</h3><dl>'
            + riga('Gestiti (indicativo)', r.fascia_condomini || 'non indicato')
            + riga('Indicati', r.elenco_condomini ? esc(r.elenco_condomini).replace(/\n/g, '<br>') : '', true) + '</dl></div>');

        // ripartitori + foto
        var foto = '';
        $.each(j.allegati || [], function (i, a) {
            var url = API + '?FUNCTIONID=RegistrazioniDati&OPERAZIONE=foto&ID_ALLEGATO=' + a.id_allegato;
            var anteprima = /heic/.test(a.tipo_mime) ? '<span class="reg-foto-ph">HEIC</span>'
                : '<img src="' + url + '&ANTEPRIMA=S" alt="' + esc(a.nome_file_originale) + '">';
            foto += '<a class="reg-foto" href="' + url + '" target="_blank" rel="noopener" title="' + esc(a.nome_file_originale)
                + '">' + anteprima + '<span>' + esc(a.nome_file_originale) + '</span></a>';
        });
        h.push('<div class="reg-blocco"><h3>Ripartitori</h3><dl>' + riga('Marca e modello', r.marca_modello || 'non indicati')
            + '</dl>' + (foto ? '<div class="reg-fotos">' + foto + '</div>' : '<p class="reg-nota">Nessuna foto allegata.</p>') + '</div>');

        // verifica (modificabile finche' non e' rifiutata o annullata)
        var modificabile = attesa || r.stato === 'APPROVATA';
        var opzioni = $.map(ESITI, function (e) {
            return '<option value="' + e[0] + '"' + (e[0] === r.esito_compatibilita ? ' selected' : '') + '>' + e[1] + '</option>';
        }).join('');
        h.push('<form class="reg-blocco reg-verifica" id="reg-form-verifica"><h3>Verifica di compatibilit&agrave;</h3>'
            + '<div class="row"><div class="col-sm-5 form-group"><label for="reg-esito">Esito ripartitori</label>'
            + '<select class="form-control" id="reg-esito" name="ESITO_COMPATIBILITA"' + (modificabile ? '' : ' disabled') + '>' + opzioni + '</select></div>'
            + '<div class="col-sm-7 form-group"><label for="reg-nota-c">Nota per il richiedente <span class="reg-nota">(nella mail di approvazione)</span></label>'
            + '<textarea class="form-control" id="reg-nota-c" name="NOTA_COMPATIBILITA" rows="2" maxlength="2000"' + (modificabile ? '' : ' disabled') + '>'
            + esc(r.nota_compatibilita) + '</textarea></div></div>'
            + '<div class="form-group"><label for="reg-nota-i">Nota interna <span class="reg-nota">(non va mai al richiedente)</span></label>'
            + '<textarea class="form-control" id="reg-nota-i" name="NOTA_INTERNA" rows="2" maxlength="2000"' + (modificabile ? '' : ' disabled') + '>'
            + esc(r.nota_interna) + '</textarea></div>'
            + (modificabile ? '<button type="submit" class="btn btn-white btn-sm"><i class="fa fa-save" aria-hidden="true"></i> Salva la verifica</button>' : '')
            + '</form>');

        if (attesa) {
            h.push('<div class="reg-azioni">'
                + '<div class="reg-azione-ok"><button type="button" class="btn btn-primary" id="reg-approva"><i class="fa fa-check" aria-hidden="true"></i> Approva e crea l\'utente</button>'
                + '<p class="reg-nota">Crea l\'utente <b>' + esc(r.email) + '</b> con profilo ' + esc(c.profilo) + ' sull\'azienda '
                + esc(c.azienda_codice) + ', con la password scelta dal richiedente' + (c.mail_attiva ? ', e gli manda la mail di conferma.' : '. Mail spente: avvisalo tu.') + '</p></div>'
                + '<div class="reg-azione-no"><label for="reg-motivo">Motivo del rifiuto <span class="reg-nota">(va nella mail al richiedente)</span></label>'
                + '<textarea class="form-control" id="reg-motivo" rows="2" maxlength="2000"></textarea>'
                + '<div class="reg-azione-btn"><button type="button" class="btn btn-danger btn-outline btn-sm" id="reg-rifiuta"><i class="fa fa-times" aria-hidden="true"></i> Rifiuta</button>'
                + '<button type="button" class="btn btn-white btn-sm" id="reg-annulla" title="Per spam o doppioni: nessuna mail">Annulla (spam/doppione)</button></div></div>'
                + '</div>');
        }

        // registro
        var ev = $.map(j.eventi || [], function (e) {
            return '<li><span class="reg-ev-data">' + data(e.ts_evento, true) + '</span> <b>' + esc(e.tipo_evento) + '</b>'
                + (e.utente ? ' &middot; ' + esc(e.utente) : '') + (e.dettaglio ? ' &middot; ' + esc(e.dettaglio) : '') + '</li>';
        });
        h.push('<details class="reg-registro"><summary>Registro (' + ev.length + ')</summary><ul>' + ev.join('') + '</ul></details>');

        $('#reg-dettaglio').html(h.join(''));
    }

    // ------------------------------------------------------------------ azioni

    function esegui(param, conferma, fatto) {
        if (conferma && !window.confirm(conferma)) {
            return;
        }
        param.FUNCTIONID = 'RegistrazioniEsito';
        param.ID_RICHIESTA = aperta;
        $('#reg-dettaglio button').prop('disabled', true);
        $.ajax({ url: API + '?FUNCTIONID=RegistrazioniEsito', type: 'POST', data: param, dataType: 'json' })
            .done(function (j) {
                avviso(fatto(j), 'ok');
                caricaElenco();
                apri(aperta);
            })
            .fail(function (xhr) {
                avviso(messaggioErrore(xhr), 'ko');
                $('#reg-dettaglio button').prop('disabled', false);
            });
    }

    function esitoMail(m) {
        if (m === 'INVIATA') {
            return ' Mail inviata al richiedente.';
        }
        if (m === 'NON_INVIATA') {
            return ' Mail NON inviata (MAIL.ATTIVA = N): avvisa tu il richiedente.';
        }
        if (m === 'ERRORE') {
            return ' Invio della mail NON riuscito: avvisa tu il richiedente.';
        }
        return '';
    }

    function verificaCorrente() {
        return {
            ESITO_COMPATIBILITA: $('#reg-esito').val(),
            NOTA_COMPATIBILITA: $('#reg-nota-c').val(),
            NOTA_INTERNA: $('#reg-nota-i').val()
        };
    }

    $(document).on('submit', '#reg-form-verifica', function (ev) {
        ev.preventDefault();
        esegui($.extend({ OPERAZIONE: 'verifica' }, verificaCorrente()), null, function () {
            return 'Verifica salvata.';
        });
    });

    $(document).on('click', '#reg-approva', function () {
        esegui($.extend({ OPERAZIONE: 'approva' }, verificaCorrente()),
            'Approvare la richiesta e creare l\'utente amministratore?', function (j) {
                return 'Richiesta approvata: creato l\'utente <b>' + esc(j.username) + '</b>.' + esitoMail(j.mail)
                    + ' Ora collega i suoi condom&igrave;ni dall\'<a href="astro?FUNCTIONID=RicercaAnagraficaCondomini">anagrafica condominio</a>.';
            });
    });

    $(document).on('click', '#reg-rifiuta', function () {
        var motivo = $.trim($('#reg-motivo').val());
        if (!motivo) {
            avviso('Scrivi il motivo del rifiuto: va nella mail al richiedente.', 'ko');
            $('#reg-motivo').focus();
            return;
        }
        esegui({ OPERAZIONE: 'rifiuta', MOTIVO: motivo, NOTA_INTERNA: $('#reg-nota-i').val() },
            'Rifiutare la richiesta? Il richiedente ricevera\' il motivo.', function (j) {
                return 'Richiesta rifiutata.' + esitoMail(j.mail);
            });
    });

    $(document).on('click', '#reg-annulla', function () {
        esegui({ OPERAZIONE: 'annulla', MOTIVO: $.trim($('#reg-motivo').val()), NOTA_INTERNA: $('#reg-nota-i').val() },
            'Annullare la richiesta (spam o doppione)? Nessuna mail al richiedente.', function () {
                return 'Richiesta annullata.';
            });
    });

    $(document).on('click keydown', '#reg-voci .reg-voce', function (ev) {
        if (ev.type === 'keydown' && ev.which !== 13 && ev.which !== 32) {
            return;
        }
        ev.preventDefault();
        avviso('');
        apri(parseInt($(this).attr('data-id'), 10));
    });

    $('#reg-stati').on('click', '.reg-stato', function () {
        $('#reg-stati .reg-stato').removeClass('on');
        $(this).addClass('on');
        stato = $(this).attr('data-stato');
        caricaElenco();
    });

    $(function () {
        caricaElenco();
        if (aperta) {
            apri(aperta);
        }
    });
}(window.jQuery));
