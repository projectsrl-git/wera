/*
 * wera2026 - Registrazione amministratore (registrazioneamministratore.html).
 * Stessi controlli del server (FunctionRegistrazioneAmministratore): qui servono solo a dare l'errore
 * subito, la verifica vera resta lato server. Le foto stanno in un array (galleria, fotocamera,
 * trascinamento) e partono con FormData insieme ai campi; l'esito arriva in JSON.
 * ES5, nessuna dipendenza.
 */
(function () {
    'use strict';

    var form = document.getElementById('RegAmm');
    if (!form) {
        return;
    }

    var URL_INVIO = 'astro?FUNCTIONID=RegistrazioneAmministratore';
    var cfg = {
        pwdMin: parseInt(form.getAttribute('data-pwd-min'), 10) || 8,
        pwdMax: parseInt(form.getAttribute('data-pwd-max'), 10) || 64,
        pwdRegola: form.getAttribute('data-pwd-regola') || '',
        pwdRegolaTesto: form.getAttribute('data-pwd-regola-testo') || '',
        fileMax: parseInt(form.getAttribute('data-file-max'), 10) || 6,
        fileMb: parseInt(form.getAttribute('data-file-mb'), 10) || 10,
        fileExt: (form.getAttribute('data-file-ext') || 'jpg,jpeg,png').toLowerCase().split(/\s*,\s*/)
    };
    var regolaPwd = null;
    try {
        regolaPwd = cfg.pwdRegola ? new RegExp(cfg.pwdRegola) : null;
    } catch (e) {
        regolaPwd = null; // regola non valida in JS: decide il server
    }

    var FOTO = [];
    var inviato = false;
    var ripetuto = false;

    function $(id) {
        return document.getElementById(id);
    }

    function val(nome) {
        var el = form.elements[nome];
        return el && el.value ? el.value.replace(/^\s+|\s+$/g, '') : '';
    }

    function studio() {
        return form.querySelector('input[name=TIPO]:checked').value === 'STUDIO';
    }

    // ------------------------------------------------------------------ controlli (come Validazioni.java)

    function partitaIva(v) {
        if (!/^[0-9]{11}$/.test(v) || v === '00000000000') {
            return false;
        }
        var s = 0;
        for (var i = 0; i < 10; i++) {
            var c = v.charCodeAt(i) - 48;
            if (i % 2 === 1) {
                c = c * 2;
                if (c > 9) {
                    c -= 9;
                }
            }
            s += c;
        }
        return (10 - s % 10) % 10 === v.charCodeAt(10) - 48;
    }

    var CF_DISPARI = [1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20, 11, 3, 6, 8, 12, 14, 16, 10, 22, 25, 24, 23];

    function cfPersona(v) {
        if (!/^[A-Z]{6}[0-9LMNPQRSTUV]{2}[ABCDEHLMPRST][0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{3}[A-Z]$/.test(v)) {
            return false;
        }
        var s = 0;
        for (var i = 0; i < 15; i++) {
            var ch = v.charAt(i);
            var k = /[0-9]/.test(ch) ? ch.charCodeAt(0) - 48 : ch.charCodeAt(0) - 65;
            s += (i % 2 === 0) ? CF_DISPARI[k] : k;
        }
        return v.charCodeAt(15) === 65 + s % 26;
    }

    function email(v) {
        return v.length <= 100 && /^[^\s@<>"',;]+@[^\s@<>"',;]+\.[^\s@<>"',;.]{2,}$/.test(v);
    }

    function compatto(v) {
        return v.replace(/\s+/g, '').toUpperCase();
    }

    function pivaNorm(v) {
        var c = compatto(v);
        return (c.indexOf('IT') === 0 && c.length === 13) ? c.substring(2) : c;
    }

    function angolari(v) {
        return /[<>]/.test(v);
    }

    /** Errore del campo o '' (vuoto = campo a posto). */
    function controlla(nome) {
        var v = val(nome);
        var st = studio();
        switch (nome) {
        case 'DENOMINAZIONE':
            if (!v) {
                return st ? 'Indica la denominazione dello studio o della societ\u00e0.' : 'Indica nome e cognome.';
            }
            break;
        case 'PIVA':
            if (!v) {
                return 'Indica la partita IVA.';
            }
            if (!partitaIva(pivaNorm(v))) {
                return 'Partita IVA non valida: servono 11 cifre (controlla anche l\'ultima, di controllo).';
            }
            break;
        case 'CF':
            if (!v) {
                return 'Indica il codice fiscale.';
            }
            v = compatto(v);
            if (v.length === 11 ? !(st && partitaIva(v)) : !cfPersona(v)) {
                return st ? 'Codice fiscale non valido: 16 caratteri, oppure 11 cifre per le societ\u00e0.'
                    : 'Codice fiscale non valido: controlla i 16 caratteri.';
            }
            break;
        case 'PEC':
            if (!v) {
                return 'Indica la PEC.';
            }
            if (!email(v)) {
                return 'Indirizzo PEC non valido.';
            }
            break;
        case 'SDI':
            if (v && !/^[A-Z0-9]{7}$/.test(compatto(v))) {
                return 'Il codice destinatario SDI ha 7 caratteri, lettere o cifre.';
            }
            break;
        case 'INDIRIZZO':
            if (!v) {
                return 'Indica l\'indirizzo.';
            }
            break;
        case 'CITTA':
            if (!v) {
                return 'Indica la citt\u00e0.';
            }
            break;
        case 'PROVINCIA':
            if (!/^[A-Z]{2}$/.test(compatto(v))) {
                return 'Sigla della provincia: 2 lettere (es. PV).';
            }
            break;
        case 'CAP':
            if (!/^[0-9]{5}$/.test(v)) {
                return 'Il CAP ha 5 cifre.';
            }
            break;
        case 'MODELLO':
            if (!v && FOTO.length === 0) {
                return 'Indica marca e modello dei ripartitori oppure allega almeno una foto: ci serve per verificare '
                    + 'la compatibilit\u00e0 con WERA.';
            }
            break;
        case 'REF_NOME':
            if (!v) {
                return 'Indica il nome del referente.';
            }
            break;
        case 'REF_COGNOME':
            if (!v) {
                return 'Indica il cognome del referente.';
            }
            break;
        case 'EMAIL':
            if (!v) {
                return 'Indica l\'email: sar\u00e0 il tuo nome utente.';
            }
            if (!email(v)) {
                return 'Indirizzo email non valido.';
            }
            break;
        case 'TELEFONO':
            if (!v) {
                return 'Indica un numero di telefono.';
            }
            if (!/^\+?[0-9 .\/()-]{6,30}$/.test(v) || v.replace(/[^0-9]/g, '').length < 6) {
                return 'Numero di telefono non valido.';
            }
            break;
        case 'PASSWORD':
            v = form.elements.PASSWORD.value;
            if (v.length < cfg.pwdMin || v.length > cfg.pwdMax) {
                return 'La password deve avere da ' + cfg.pwdMin + ' a ' + cfg.pwdMax + ' caratteri.';
            }
            if (regolaPwd && !regolaPwd.test(v)) {
                return 'La password deve contenere ' + (cfg.pwdRegolaTesto || 'i caratteri richiesti') + '.';
            }
            if (v.toLowerCase() === val('EMAIL').toLowerCase()) {
                return 'La password non pu\u00f2 essere uguale all\'email.';
            }
            break;
        case 'PASSWORD2':
            if (form.elements.PASSWORD2.value !== form.elements.PASSWORD.value) {
                return 'Le due password non coincidono.';
            }
            break;
        case 'PRIVACY':
            if (!form.elements.PRIVACY.checked) {
                return 'Per inviare la richiesta serve il consenso.';
            }
            break;
        default:
            break;
        }
        if (v && angolari(v) && nome !== 'PASSWORD' && nome !== 'PASSWORD2') {
            return 'Il testo non pu\u00f2 contenere i caratteri < e >.';
        }
        return '';
    }

    var CAMPI = ['DENOMINAZIONE', 'PIVA', 'CF', 'ALBO', 'PEC', 'SDI', 'INDIRIZZO', 'CITTA', 'PROVINCIA', 'CAP',
        'MODELLO', 'CONDOMINI', 'REF_NOME', 'REF_COGNOME', 'EMAIL', 'TELEFONO', 'PASSWORD', 'PASSWORD2', 'PRIVACY'];
    // campi che contano per la barra "Modulo completato"
    var OBBLIGATORI = ['DENOMINAZIONE', 'PIVA', 'CF', 'PEC', 'INDIRIZZO', 'CITTA', 'PROVINCIA', 'CAP', 'MODELLO',
        'REF_NOME', 'REF_COGNOME', 'EMAIL', 'TELEFONO', 'PASSWORD', 'PASSWORD2', 'PRIVACY'];

    function mostraErrore(nome, testo) {
        var box = form.querySelector('.ra-ferr[data-for="' + nome + '"]');
        if (box) {
            box.textContent = testo || '';
        }
        var el = form.elements[nome];
        var campo = el && el.closest ? el.closest('.field') : null;
        if (campo) {
            campo.className = campo.className.replace(/\s*ra-bad/g, '') + (testo ? ' ra-bad' : '');
        }
        if (nome === 'FOTO') {
            $('raDrop').className = $('raDrop').className.replace(/\s*ra-bad/g, '') + (testo ? ' ra-bad' : '');
        }
    }

    function aggiornaBarra() {
        var ok = 0;
        for (var i = 0; i < OBBLIGATORI.length; i++) {
            var n = OBBLIGATORI[i];
            var pieno = n === 'PRIVACY' ? form.elements.PRIVACY.checked
                : (n === 'MODELLO' ? (val('MODELLO') || FOTO.length > 0) : val(n));
            if (pieno && !controlla(n)) {
                ok++;
            }
        }
        var perc = Math.round(ok * 100 / OBBLIGATORI.length);
        $('raPerc').textContent = perc + '%';
        $('raBar').style.width = perc + '%';
    }

    // ------------------------------------------------------------------ tipo di soggetto

    function setTipo() {
        var st = studio();
        $('lblDen').innerHTML = (st ? 'Denominazione studio o societ&agrave; ' : 'Nome e cognome ')
            + '<span class="req">*</span>';
        $('DENOMINAZIONE').placeholder = st ? 'es. Studio Rossi Amministrazioni Srl' : 'es. Mario Rossi';
        $('fldAlbo').style.display = st ? 'none' : '';
        var etichette = form.querySelectorAll('.ra-seg label');
        for (var i = 0; i < etichette.length; i++) {
            etichette[i].className = etichette[i].querySelector('input').checked ? 'on' : '';
        }
    }

    // ------------------------------------------------------------------ foto

    function estensione(nome) {
        var p = nome.lastIndexOf('.');
        return p < 0 ? '' : nome.substring(p + 1).toLowerCase();
    }

    function aggiungi(lista) {
        var scartate = [];
        for (var i = 0; i < lista.length; i++) {
            var f = lista[i];
            if (FOTO.length >= cfg.fileMax) {
                scartate.push('puoi allegare al massimo ' + cfg.fileMax + ' foto');
                break;
            }
            if (cfg.fileExt.indexOf(estensione(f.name)) < 0) {
                scartate.push('"' + f.name + '" non \u00e8 una foto ammessa');
                continue;
            }
            if (f.size > cfg.fileMb * 1024 * 1024) {
                scartate.push('"' + f.name + '" supera ' + cfg.fileMb + ' MB');
                continue;
            }
            FOTO.push(f);
        }
        mostraErrore('FOTO', scartate.length ? scartate.join('; ') + '.' : '');
        disegnaFoto();
        if (FOTO.length) {
            mostraErrore('MODELLO', '');
        }
        aggiornaBarra();
    }

    function disegnaFoto() {
        var box = $('raThumbs');
        while (box.firstChild) {
            if (box.firstChild.getAttribute && box.firstChild.getAttribute('data-url')) {
                URL.revokeObjectURL(box.firstChild.getAttribute('data-url'));
            }
            box.removeChild(box.firstChild);
        }
        FOTO.forEach(function (file, i) {
            var d = document.createElement('div');
            d.className = 'ra-thumb';
            var ext = estensione(file.name);
            if (ext === 'heic' || ext === 'heif') {
                var ph = document.createElement('div');
                ph.className = 'ra-thumb-ph';
                ph.textContent = ext.toUpperCase();
                d.appendChild(ph);
            } else {
                var url = URL.createObjectURL(file);
                d.setAttribute('data-url', url);
                var img = document.createElement('img');
                img.alt = file.name;
                img.src = url;
                d.appendChild(img);
            }
            var b = document.createElement('button');
            b.type = 'button';
            b.innerHTML = '&#10005;';
            b.setAttribute('aria-label', 'Rimuovi ' + file.name);
            b.onclick = function () {
                FOTO.splice(i, 1);
                mostraErrore('FOTO', '');
                disegnaFoto();
                aggiornaBarra();
            };
            var n = document.createElement('div');
            n.className = 'ra-thumb-nm';
            n.textContent = file.name;
            d.appendChild(b);
            d.appendChild(n);
            box.appendChild(d);
        });
    }

    // ------------------------------------------------------------------ invio

    function messaggio(testo) {
        var m = $('formMsg');
        m.textContent = testo || '';
        m.hidden = !testo;
    }

    function occupato(si, perc) {
        var b = $('raInvia');
        b.disabled = si;
        b.querySelector('.ra-lbl').textContent = si ? (perc != null && perc < 100 ? 'Invio in corso\u2026 ' + perc + '%'
            : 'Invio in corso\u2026') : 'Invia la richiesta';
        var p = $('raProg');
        p.hidden = !si;
        p.firstChild.style.width = (perc || 0) + '%';
    }

    function controllaTutto() {
        var primo = null;
        for (var i = 0; i < CAMPI.length; i++) {
            var e = controlla(CAMPI[i]);
            mostraErrore(CAMPI[i], e);
            if (e && !primo) {
                primo = CAMPI[i];
            }
        }
        return primo;
    }

    function vaiA(nome) {
        var el = nome === 'FOTO' ? $('raDrop') : form.elements[nome];
        if (el && el.scrollIntoView) {
            el.scrollIntoView({ block: 'center', behavior: 'smooth' });
        }
        if (el && el.focus && nome !== 'FOTO') {
            try {
                el.focus({ preventScroll: true });
            } catch (e) {
                el.focus();
            }
        }
    }

    function invia() {
        var dati = new FormData(form);
        FOTO.forEach(function (f) {
            dati.append('FOTO', f, f.name);
        });
        var xhr = new XMLHttpRequest();
        xhr.open('POST', URL_INVIO, true);
        xhr.upload.onprogress = function (ev) {
            if (ev.lengthComputable) {
                occupato(true, Math.round(ev.loaded * 100 / ev.total));
            }
        };
        xhr.onload = function () {
            var r = null;
            try {
                r = JSON.parse(xhr.responseText);
            } catch (e) {
                r = null;
            }
            esito(xhr.status, r);
        };
        xhr.onerror = function () {
            occupato(false);
            messaggio('Connessione non riuscita: controlla la rete e riprova.');
        };
        occupato(true, 0);
        xhr.send(dati);
    }

    function esito(status, r) {
        if (r && r.esito === 'RIPROVA' && r.gettone && !ripetuto) {
            // gettone scaduto o di prima di un riavvio del server: si ripete una volta da soli
            ripetuto = true;
            $('RA_T').value = r.gettone;
            setTimeout(invia, ((r.attesa_secondi || 5) + 1) * 1000);
            return;
        }
        occupato(false);
        if (r && r.esito === 'OK') {
            inviato = true;
            form.hidden = true;
            $('raNum').textContent = r.numero ? 'Richiesta n. ' + r.numero : '';
            $('raMail').textContent = r.email || 'la tua email';
            $('raMailNota').hidden = r.mail_ricevuta !== 'INVIATA';
            $('raOk').hidden = false;
            $('raOk').focus();
            window.scrollTo(0, 0);
            $('raPerc').textContent = '100%';
            $('raBar').style.width = '100%';
            return;
        }
        if (r && r.errori) {
            var primo = null;
            for (var nome in r.errori) {
                if (Object.prototype.hasOwnProperty.call(r.errori, nome)) {
                    mostraErrore(nome, r.errori[nome]);
                    primo = primo || nome;
                }
            }
            messaggio(r.messaggio || 'Controlla i campi evidenziati.');
            if (primo) {
                vaiA(primo);
            }
            return;
        }
        messaggio(r && r.messaggio ? r.messaggio : 'Non siamo riusciti a inviare la richiesta (errore ' + status
            + '). Riprova tra qualche minuto.');
    }

    // ------------------------------------------------------------------ eventi

    form.addEventListener('submit', function (ev) {
        ev.preventDefault();
        if (inviato) {
            return;
        }
        messaggio('');
        ripetuto = false;
        var primo = controllaTutto();
        if (primo) {
            messaggio('Controlla i campi evidenziati.');
            vaiA(primo);
            return;
        }
        invia();
    });

    CAMPI.forEach(function (nome) {
        var el = form.elements[nome];
        if (!el) {
            return;
        }
        el.addEventListener('blur', function () {
            if (el.type !== 'checkbox' && (val(nome) || el.type === 'password' && el.value)) {
                mostraErrore(nome, controlla(nome));
            }
            aggiornaBarra();
        });
        el.addEventListener(el.type === 'checkbox' ? 'change' : 'input', function () {
            var box = form.querySelector('.ra-ferr[data-for="' + nome + '"]');
            if (box && box.textContent) {
                mostraErrore(nome, controlla(nome));
            }
            if (nome === 'PASSWORD' && form.elements.PASSWORD2.value) {
                mostraErrore('PASSWORD2', controlla('PASSWORD2'));
            }
            aggiornaBarra();
        });
    });

    var maiuscoli = form.querySelectorAll('.ra-up');
    for (var i = 0; i < maiuscoli.length; i++) {
        maiuscoli[i].addEventListener('input', function () {
            var pos = this.selectionStart;
            this.value = this.value.toUpperCase();
            try {
                this.setSelectionRange(pos, pos);
            } catch (e) {
                // input senza selezione
            }
        });
    }

    var radio = form.querySelectorAll('input[name=TIPO]');
    for (var j = 0; j < radio.length; j++) {
        radio[j].addEventListener('change', function () {
            setTipo();
            ['DENOMINAZIONE', 'CF'].forEach(function (n) {
                if (val(n)) {
                    mostraErrore(n, controlla(n));
                }
            });
            aggiornaBarra();
        });
    }

    var occhi = form.querySelectorAll('.ra-eye');
    for (var k = 0; k < occhi.length; k++) {
        occhi[k].addEventListener('click', function () {
            var campo = $(this.getAttribute('data-target'));
            var vedi = campo.type === 'password';
            campo.type = vedi ? 'text' : 'password';
            this.textContent = vedi ? 'Nascondi' : 'Mostra';
            this.setAttribute('aria-label', vedi ? 'Nascondi la password' : 'Mostra la password');
        });
    }

    $('raGal').addEventListener('click', function () {
        $('FILEGAL').click();
    });
    $('raCam').addEventListener('click', function () {
        $('FILECAM').click();
    });
    ['FILEGAL', 'FILECAM'].forEach(function (id) {
        $(id).addEventListener('change', function () {
            aggiungi(this.files);
            this.value = '';
        });
    });

    var drop = $('raDrop');
    ['dragenter', 'dragover'].forEach(function (t) {
        drop.addEventListener(t, function (ev) {
            ev.preventDefault();
            drop.className = drop.className.replace(/\s*ra-over/g, '') + ' ra-over';
        });
    });
    ['dragleave', 'drop'].forEach(function (t) {
        drop.addEventListener(t, function (ev) {
            ev.preventDefault();
            drop.className = drop.className.replace(/\s*ra-over/g, '');
        });
    });
    drop.addEventListener('drop', function (ev) {
        if (ev.dataTransfer && ev.dataTransfer.files) {
            aggiungi(ev.dataTransfer.files);
        }
    });

    setTipo();
    aggiornaBarra();
}());
