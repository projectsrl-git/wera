package net.projectsrl.wera.registrazioni;

import java.io.File;
import java.io.InputStream;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.WebAppUtils;

/**
 * FUNCTIONID=RegistrazioneAmministratore (pubblica, senza login).
 * <p>
 * GET: pagina registrazioneamministratore.html (stack wera-landing.css) con i testi e i limiti dai parametri.
 * POST multipart (FUNCTIONID nella URL, inviato dal JS con FormData): valida, salva la RICHIESTA e le foto
 * nello schema registrazioni e risponde JSON. Nessun utente viene creato qui: nasce all'approvazione
 * (FunctionRegistrazioniRichieste). Anti-abuso: campo trappola SITO_WEB, gettone con l'ora di apertura del
 * form firmato HMAC (tempo minimo), limite di invii per IP nelle 24 ore, una richiesta in attesa per email
 * e per P.IVA (anche indici univoci nel DB).
 */
public class FunctionRegistrazioneAmministratore extends FunctionRegistrazioni_base {

    static final String          CAMPO_TRAPPOLA      = "SITO_WEB";
    static final String          CAMPO_GETTONE       = "T";

    /** Chiave HMAC del gettone, nuova a ogni avvio: un gettone di prima del riavvio fa solo ripetere l'invio. */
    private static final byte[]  CHIAVE_GETTONE      = nuovaChiave();
    private static final long    GETTONE_MAX_MS      = 24L * 60L * 60L * 1000L;

    private static final String  SQL_CONTA_IP        = "SELECT count(*) FROM registrazioni.eventi WHERE ip = ? "
            + "AND tipo_evento IN ('RICHIESTA', 'BLOCCO') AND ts_evento > current_timestamp - interval '24 hours'";
    private static final String  SQL_UTENTE_ESISTE   = "SELECT count(*) FROM public.utenti "
            + "WHERE lower(username) = lower(?) OR lower(email) = lower(?)";
    private static final String  SQL_EMAIL_IN_ATTESA = "SELECT count(*) FROM registrazioni.richieste "
            + "WHERE lower(email) = lower(?) AND stato = 'IN_ATTESA'";
    private static final String  SQL_PIVA_IN_ATTESA  = "SELECT count(*) FROM registrazioni.richieste "
            + "WHERE tipo_richiesta = ? AND partita_iva = ? AND stato = 'IN_ATTESA'";
    private static final String  SQL_INSERISCI       = "INSERT INTO registrazioni.richieste (tipo_richiesta, "
            + "tipo_soggetto, denominazione, partita_iva, codice_fiscale, numero_registro, pec, codice_sdi, indirizzo, "
            + "citta, provincia, cap, fascia_condomini, elenco_condomini, marca_modello, referente_nome, "
            + "referente_cognome, email, telefono, hash_password, privacy_ts, privacy_testo, ip, user_agent) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp, ?, ?, ?) "
            + "RETURNING id_richiesta";
    private static final String  SQL_ALLEGATO        = "INSERT INTO registrazioni.allegati (id_richiesta, "
            + "nome_file_originale, percorso_file, tipo_mime, dimensione_byte, impronta_sha256) VALUES (?, ?, ?, ?, ?, ?)";

    public FunctionRegistrazioneAmministratore(ApplicationServices_itf applServices, String functionID,
            String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public boolean isAuthenticationRequired() {

        return false;
    }

    @Override
    protected boolean checkSession(SsbServletRequest req) {

        return true;
    }

    // ------------------------------------------------------------------------------------------------ pagina

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Parametri p = Parametri.carica();
        Map<String, String> segnaposto = new HashMap<String, String>();
        segnaposto.put("TEMPI_RISPOSTA", p.testo(RegistrazioniCostanti.PAR_TEMPI_RISPOSTA));

        Map<String, Object> dati = new HashMap<String, Object>();
        dati.put("RA_ATTIVA", p.vero(RegistrazioniCostanti.PAR_REGISTRAZIONE_ATTIVA, true) ? "S" : "N");
        dati.put("RA_CHIUSA_TESTO", p.testo(RegistrazioniCostanti.PAR_REGISTRAZIONE_CHIUSA));
        dati.put("RA_TITOLO", p.testo(RegistrazioniCostanti.PAR_PAGINA_TITOLO, "Registra il tuo studio di amministrazione"));
        dati.put("RA_SOTTOTITOLO", p.testo(RegistrazioniCostanti.PAR_PAGINA_SOTTOTITOLO));
        dati.put("RA_INFO_VERIFICA", Posta.sostituisci(p.testo(RegistrazioniCostanti.PAR_PAGINA_INFO_VERIFICA), segnaposto));
        dati.put("RA_TEMPI_RISPOSTA", p.testo(RegistrazioniCostanti.PAR_TEMPI_RISPOSTA));
        dati.put("RA_PRIVACY_TESTO", p.testo(RegistrazioniCostanti.PAR_PRIVACY_TESTO));
        dati.put("RA_PRIVACY_LINK", p.testo(RegistrazioniCostanti.PAR_PRIVACY_LINK));
        dati.put("RA_ASSISTENZA_EMAIL", p.testo(RegistrazioniCostanti.PAR_ASSISTENZA_EMAIL));
        dati.put("RA_ASSISTENZA_TELEFONO", p.testo(RegistrazioniCostanti.PAR_ASSISTENZA_TELEFONO));
        dati.put("RA_FASCE", p.elenco(RegistrazioniCostanti.PAR_FASCE_CONDOMINI));
        dati.put("RA_PASSWORD_MIN", Integer.toString(p.intero(RegistrazioniCostanti.PAR_PASSWORD_MIN, 8)));
        dati.put("RA_PASSWORD_MAX", Integer.toString(p.intero(RegistrazioniCostanti.PAR_PASSWORD_MAX, 64)));
        dati.put("RA_PASSWORD_REGOLA", p.testo(RegistrazioniCostanti.PAR_PASSWORD_REGOLA));
        dati.put("RA_PASSWORD_REGOLA_TESTO", p.testo(RegistrazioniCostanti.PAR_PASSWORD_REGOLA_TESTO));
        dati.put("RA_FILE_MAX_NUMERO", Integer.toString(p.intero(RegistrazioniCostanti.PAR_FILE_MAX_NUMERO, 6)));
        dati.put("RA_FILE_MAX_MB", Integer.toString(p.intero(RegistrazioniCostanti.PAR_FILE_MAX_MB, 10)));
        dati.put("RA_FILE_ESTENSIONI", p.testo(RegistrazioniCostanti.PAR_FILE_ESTENSIONI));
        dati.put("RA_GETTONE", gettone(System.currentTimeMillis()));
        _applicationSrv.displayPage(getPageName(), dati, res);
    }

    // ------------------------------------------------------------------------------------------------ invio

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Parametri p = Parametri.carica();
        String ip = ip(req, p);

        if (!p.vero(RegistrazioniCostanti.PAR_REGISTRAZIONE_ATTIVA, true)) {
            rispondiErrore(res, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    p.testo(RegistrazioniCostanti.PAR_REGISTRAZIONE_CHIUSA, "Registrazioni sospese"));
            return;
        }
        if (contaInviiIp(ip) >= p.intero(RegistrazioniCostanti.PAR_ANTIABUSO_MAX_IP, 5)) {
            Eventi.registra(RegistrazioniCostanti.EVENTO_BLOCCO, null, null, null, ip, "limite invii per IP nelle 24 ore");
            rispondiErrore(res, 429, "Troppe richieste da questa connessione nelle ultime 24 ore. Riprova domani "
                    + "o contatta l'assistenza.");
            return;
        }

        int maxMb = p.intero(RegistrazioniCostanti.PAR_FILE_MAX_MB, 10);
        int maxFile = p.intero(RegistrazioniCostanti.PAR_FILE_MAX_NUMERO, 6);
        Map<String, String> campi = new HashMap<String, String>();
        List<FileItem> files = new ArrayList<FileItem>();
        try {
            DiskFileUpload fu = new DiskFileUpload();
            fu.setHeaderEncoding("UTF-8");
            fu.setSizeMax(maxMb * RegistrazioniCostanti.MEGABYTE * maxFile + 2 * RegistrazioniCostanti.MEGABYTE);
            @SuppressWarnings("unchecked")
            List<FileItem> items = fu.parseRequest(req);
            for (FileItem item : items) {
                if (item.isFormField()) {
                    campi.put(item.getFieldName(), item.getString("UTF-8"));
                } else if (Util.IsNotEmpty(item.getName()) && item.getSize() > 0) {
                    files.add(item);
                } else {
                    item.delete();
                }
            }
        } catch (Throwable t) {
            for (FileItem item : files) {
                item.delete();
            }
            AppCrash ac = new AppCrash(t);
            ac.logContext(this.getClass().getName(), "Richiesta di registrazione non leggibile da " + ip);
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "Invio non valido o troppo grande (al massimo "
                    + maxFile + " foto da " + maxMb + " MB).");
            return;
        }

        try {
            registra(req, res, p, ip, campi, files, maxMb, maxFile);
        } finally {
            for (FileItem item : files) {
                item.delete();
            }
        }
    }

    private void registra(SsbServletRequest req, SsbServletResponse res, Parametri p, String ip,
            Map<String, String> campi, List<FileItem> files, int maxMb, int maxFile) throws AppCrash {

        // --- anti-abuso: campo trappola (si risponde OK per non insegnare nulla al bot) e tempo minimo
        if (Util.IsNotEmpty(campi.get(CAMPO_TRAPPOLA))) {
            Eventi.registra(RegistrazioniCostanti.EVENTO_BLOCCO, null, null, null, ip, "campo trappola compilato");
            Map<String, Object> finto = new LinkedHashMap<String, Object>();
            finto.put("esito", "OK");
            rispondiJson(res, HttpServletResponse.SC_OK, finto);
            return;
        }
        long aperto = verificaGettone(campi.get(CAMPO_GETTONE));
        if (aperto < 0) {
            // gettone mancante, scaduto o di prima di un riavvio: il JS ripete l'invio con quello nuovo
            Map<String, Object> riprova = new LinkedHashMap<String, Object>();
            riprova.put("esito", "RIPROVA");
            riprova.put("gettone", gettone(System.currentTimeMillis()));
            riprova.put("attesa_secondi", Integer.valueOf(p.intero(RegistrazioniCostanti.PAR_ANTIABUSO_SECONDI, 5)));
            rispondiJson(res, HttpServletResponse.SC_CONFLICT, riprova);
            return;
        }
        if (System.currentTimeMillis() - aperto < p.intero(RegistrazioniCostanti.PAR_ANTIABUSO_SECONDI, 5) * 1000L) {
            Eventi.registra(RegistrazioniCostanti.EVENTO_BLOCCO, null, null, null, ip, "invio troppo veloce");
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST,
                    "Invio troppo rapido: attendi qualche secondo e premi di nuovo il pulsante.");
            return;
        }

        // --- campi e validazioni (stessi controlli del JS della pagina)
        Richiesta r = new Richiesta(campi);
        Map<String, String> errori = r.valida();
        valida(p, r, files, maxMb, maxFile, errori);
        if (errori.isEmpty()) {
            duplicati(r, errori);
        }
        if (!errori.isEmpty()) {
            Map<String, Object> esito = new LinkedHashMap<String, Object>();
            esito.put("esito", "KO");
            esito.put("messaggio", "Controlla i campi evidenziati.");
            esito.put("errori", errori);
            rispondiJson(res, HttpServletResponse.SC_BAD_REQUEST, esito);
            return;
        }

        // --- richiesta + foto in una transazione; i file scritti si cancellano se qualcosa va storto
        File cartella = Allegati.cartella(_applicationSrv.getRoot(), p.testo(RegistrazioniCostanti.PAR_FILE_CARTELLA));
        List<File> scritti = new ArrayList<File>();
        Integer idRichiesta = null;
        DBTransaction tx = new DBTransaction();
        try {
            String userAgent = req.getHeader("User-Agent");
            idRichiesta = Db.intero(tx, SQL_INSERISCI, RegistrazioniCostanti.TIPO_AMMINISTRATORE, r.tipo, r.denominazione,
                    r.piva, r.cf, Db.testo(r.albo), r.pec, Db.testo(r.sdi), r.indirizzo, r.citta, r.provincia, r.cap,
                    Db.testo(r.fascia), Db.testo(r.condomini), Db.testo(r.modello), r.nome, r.cognome, r.email,
                    r.telefono, WebAppUtils.encryptSHA1(r.password), p.testo(RegistrazioniCostanti.PAR_PRIVACY_TESTO),
                    Db.testo(ip), Db.testo(pulito(userAgent, 300)));
            for (FileItem item : files) {
                String nome = Allegati.nomeSemplice(item.getName());
                Allegati.FileScritto fs;
                InputStream in = item.getInputStream();
                try {
                    fs = Allegati.scrivi(in, cartella, idRichiesta.intValue(), nome,
                            maxMb * RegistrazioniCostanti.MEGABYTE);
                } finally {
                    Allegati.chiudi(in);
                }
                scritti.add(fs.file);
                Db.esegui(tx, SQL_ALLEGATO, idRichiesta, nome, fs.percorsoRelativo, fs.tipoMime,
                        Long.valueOf(fs.dimensione), fs.sha256);
            }
            tx.commit();
        } catch (Throwable t) {
            annulla(tx, scritti);
            if (t instanceof Allegati.FileNonValido) {
                errori.put("FOTO", ((Allegati.FileNonValido) t).testo + ".");
                Map<String, Object> esito = new LinkedHashMap<String, Object>();
                esito.put("esito", "KO");
                esito.put("messaggio", "Una delle foto non e' valida.");
                esito.put("errori", errori);
                rispondiJson(res, HttpServletResponse.SC_BAD_REQUEST, esito);
                return;
            }
            if (Db.violazioneUnivoca(t)) {
                rispondiErrore(res, HttpServletResponse.SC_CONFLICT,
                        "C'e' gia' una richiesta in attesa con questa email o questa partita IVA.");
                return;
            }
            AppCrash ac = Db.errore(t, "Richiesta di registrazione non salvata (" + r.email + ")");
            Eventi.registra(RegistrazioniCostanti.EVENTO_ERRORE, null, null, null, ip,
                    "richiesta non salvata: " + ac.getMessage());
            rispondiErrore(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Non siamo riusciti a salvare la richiesta. Riprova tra qualche minuto.");
            return;
        } finally {
            tx.end();
        }

        Eventi.registra(RegistrazioniCostanti.EVENTO_RICHIESTA, idRichiesta, null, null, ip,
                RegistrazioniCostanti.TIPO_AMMINISTRATORE + " " + r.tipo + ", foto: " + files.size());

        // --- mail (non bloccano: la richiesta e' gia' salvata e visibile nella pagina di approvazione)
        Map<String, String> segnaposto = r.segnaposto(p, idRichiesta, files.size());
        String ricevuta = Posta.invia(p, RegistrazioniCostanti.MAIL_RICEVUTA, r.email, segnaposto, idRichiesta, null, ip);
        Posta.invia(p, RegistrazioniCostanti.MAIL_NOTIFICA, p.testo(RegistrazioniCostanti.PAR_MAIL_NOTIFICA_DESTINATARI),
                segnaposto, idRichiesta, null, ip);

        Map<String, Object> esito = new LinkedHashMap<String, Object>();
        esito.put("esito", "OK");
        esito.put("numero", idRichiesta);
        esito.put("email", r.email);
        esito.put("mail_ricevuta", ricevuta);
        esito.put("tempi_risposta", p.testo(RegistrazioniCostanti.PAR_TEMPI_RISPOSTA));
        rispondiJson(res, HttpServletResponse.SC_OK, esito);
    }

    /** Controlli che dipendono dai file e dalla regola password (parametri). */
    private static void valida(Parametri p, Richiesta r, List<FileItem> files, int maxMb, int maxFile,
            Map<String, String> errori) {

        int min = p.intero(RegistrazioniCostanti.PAR_PASSWORD_MIN, 8);
        int max = p.intero(RegistrazioniCostanti.PAR_PASSWORD_MAX, 64);
        String regola = p.testo(RegistrazioniCostanti.PAR_PASSWORD_REGOLA);
        if (r.password == null || r.password.length() < min || r.password.length() > max) {
            errori.put("PASSWORD", "La password deve avere da " + min + " a " + max + " caratteri.");
        } else if (regola.length() > 0 && !rispetta(regola, r.password)) {
            errori.put("PASSWORD", "La password deve contenere "
                    + p.testo(RegistrazioniCostanti.PAR_PASSWORD_REGOLA_TESTO, "i caratteri richiesti") + ".");
        } else if (r.email != null && r.password.equalsIgnoreCase(r.email)) {
            errori.put("PASSWORD", "La password non puo' essere uguale all'email.");
        }
        if (r.password != null && !r.password.equals(r.password2)) {
            errori.put("PASSWORD2", "Le due password non coincidono.");
        }

        List<String> fasce = p.elenco(RegistrazioniCostanti.PAR_FASCE_CONDOMINI);
        if (r.fascia != null && !fasce.contains(r.fascia)) {
            errori.put("NCOND", "Scegli una delle voci dell'elenco.");
        }

        if (files.size() > maxFile) {
            errori.put("FOTO", "Puoi allegare al massimo " + maxFile + " foto.");
        } else {
            List<String> estensioni = p.elencoMaiuscolo(RegistrazioniCostanti.PAR_FILE_ESTENSIONI);
            for (FileItem item : files) {
                String nome = Allegati.nomeSemplice(item.getName());
                if (!Allegati.estensioneAmmessa(nome, estensioni)) {
                    errori.put("FOTO", "\"" + nome + "\" non e' una foto ammessa (" + p.testo(
                            RegistrazioniCostanti.PAR_FILE_ESTENSIONI).replace(",", ", ") + ").");
                    break;
                }
                if (item.getSize() > maxMb * RegistrazioniCostanti.MEGABYTE) {
                    errori.put("FOTO", "\"" + nome + "\" supera " + maxMb + " MB.");
                    break;
                }
            }
        }
        if (r.modello == null && files.isEmpty()) {
            errori.put("MODELLO", "Indica marca e modello dei ripartitori oppure allega almeno una foto: ci serve "
                    + "per verificare la compatibilita' con WERA.");
        }
    }

    private static boolean rispetta(String regola, String valore) {

        try {
            return Pattern.compile(regola).matcher(valore).matches();
        } catch (Throwable t) {
            // regola scritta male nei parametri: vale solo la lunghezza
            AppCrash ac = new AppCrash(t);
            ac.logContext(FunctionRegistrazioneAmministratore.class.getName(), "PASSWORD.REGOLA non valida");
            return true;
        }
    }

    /** Email gia' di un utente o di una richiesta in attesa, P.IVA gia' in attesa: errori sul campo. */
    private static void duplicati(Richiesta r, Map<String, String> errori) throws AppCrash {

        DBTransaction tx = new DBTransaction();
        try {
            if (Db.intero(tx, SQL_UTENTE_ESISTE, r.email, r.email).intValue() > 0) {
                errori.put("EMAIL", "Esiste gia' un account con questa email. Per entrare usa \"Password "
                        + "dimenticata?\" dalla pagina di accesso.");
            } else if (Db.intero(tx, SQL_EMAIL_IN_ATTESA, r.email).intValue() > 0) {
                errori.put("EMAIL", "C'e' gia' una richiesta in attesa con questa email: la stiamo verificando.");
            }
            if (Db.intero(tx, SQL_PIVA_IN_ATTESA, RegistrazioniCostanti.TIPO_AMMINISTRATORE, r.piva).intValue() > 0) {
                errori.put("PIVA", "C'e' gia' una richiesta in attesa per questa partita IVA.");
            }
        } catch (Throwable t) {
            throw Db.errore(t, "Controllo duplicati");
        } finally {
            tx.end();
        }
    }

    private static int contaInviiIp(String ip) throws AppCrash {

        if (ip == null) {
            return 0;
        }
        List<Map<String, Object>> righe = Db.leggi(SQL_CONTA_IP, ip);
        Object n = righe.isEmpty() ? null : righe.get(0).get("count");
        return (n instanceof Number) ? ((Number) n).intValue() : 0;
    }

    private static void annulla(DBTransaction tx, List<File> scritti) {

        try {
            tx.rollBack();
        } catch (AppCrash ignorata) {
            // la transazione e' gia' annullata
        }
        for (File f : scritti) {
            Allegati.elimina(f);
        }
    }

    // ------------------------------------------------------------------------------------------------ gettone

    /** "<millisecondi>.<hmac>": dice quando e' stato aperto il form, senza sessione. */
    static String gettone(long millis) {

        String ts = Long.toString(millis);
        return ts + "." + firma(ts);
    }

    /** Millisecondi di apertura del form, o -1 se il gettone manca, e' alterato o e' troppo vecchio. */
    static long verificaGettone(String gettone) {

        if (gettone == null || !gettone.matches("[0-9]{10,15}\\.[0-9a-f]{64}")) {
            return -1;
        }
        String ts = gettone.substring(0, gettone.indexOf('.'));
        if (!java.security.MessageDigest.isEqual(firma(ts).getBytes(), gettone.substring(ts.length() + 1).getBytes())) {
            return -1;
        }
        long millis = Long.parseLong(ts);
        long eta = System.currentTimeMillis() - millis;
        return (eta < 0 || eta > GETTONE_MAX_MS) ? -1 : millis;
    }

    private static String firma(String testo) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(CHIAVE_GETTONE, "HmacSHA256"));
            byte[] h = mac.doFinal(testo.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(h.length * 2);
            for (byte b : h) {
                sb.append(String.format("%02x", Integer.valueOf(b & 0xff)));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("HMAC non disponibile", e);
        }
    }

    private static byte[] nuovaChiave() {

        byte[] k = new byte[32];
        new SecureRandom().nextBytes(k);
        return k;
    }

    // ------------------------------------------------------------------------------------------------ campi

    /** I campi del form, ripuliti e normalizzati. Nomi dei campi come nel mockup giro 12. */
    static final class Richiesta {

        final String tipo;
        final String denominazione;
        final String piva;
        final String cf;
        final String albo;
        final String pec;
        final String sdi;
        final String indirizzo;
        final String citta;
        final String provincia;
        final String cap;
        final String fascia;
        final String condomini;
        final String modello;
        final String nome;
        final String cognome;
        final String email;
        final String telefono;
        final String password;
        final String password2;
        final boolean privacy;

        Richiesta(Map<String, String> c) {

            String t = pulito(c.get("TIPO"), 20);
            tipo = RegistrazioniCostanti.SOGGETTO_STUDIO.equals(t) ? t
                    : (RegistrazioniCostanti.SOGGETTO_PROFESSIONISTA.equals(t) ? t : null);
            denominazione = pulito(c.get("DENOMINAZIONE"), 150);
            piva = Validazioni.partitaIvaNormalizzata(pulito(c.get("PIVA"), 20));
            cf = Validazioni.compatto(pulito(c.get("CF"), 20));
            // il numero di registro si chiede solo al professionista
            albo = RegistrazioniCostanti.SOGGETTO_PROFESSIONISTA.equals(tipo) ? pulito(c.get("ALBO"), 50) : null;
            pec = minuscolo(pulito(c.get("PEC"), 100));
            sdi = Validazioni.compatto(pulito(c.get("SDI"), 10));
            indirizzo = pulito(c.get("INDIRIZZO"), 60);
            citta = pulito(c.get("CITTA"), 60);
            provincia = Validazioni.compatto(pulito(c.get("PROVINCIA"), 5));
            cap = Validazioni.compatto(pulito(c.get("CAP"), 10));
            fascia = pulito(c.get("NCOND"), 30);
            condomini = pulito(c.get("CONDOMINI"), 2000);
            modello = pulito(c.get("MODELLO"), 300);
            nome = pulito(c.get("REF_NOME"), 100);
            cognome = pulito(c.get("REF_COGNOME"), 100);
            email = minuscolo(pulito(c.get("EMAIL"), 120));
            telefono = pulito(c.get("TELEFONO"), 40);
            // la password non si ripulisce: conta com'e' (solo il limite di lunghezza)
            password = c.get("PASSWORD") == null || c.get("PASSWORD").length() == 0 ? null : c.get("PASSWORD");
            password2 = c.get("PASSWORD2");
            privacy = Util.IsNotEmpty(c.get("PRIVACY"));
        }

        Map<String, String> valida() {

            Map<String, String> e = new LinkedHashMap<String, String>();
            boolean studio = RegistrazioniCostanti.SOGGETTO_STUDIO.equals(tipo);
            if (tipo == null) {
                e.put("TIPO", "Scegli il tipo di soggetto.");
            }
            obbligatorio(e, "DENOMINAZIONE", denominazione, studio ? "Indica la denominazione dello studio o della "
                    + "societa'." : "Indica nome e cognome.");
            if (piva == null) {
                e.put("PIVA", "Indica la partita IVA.");
            } else if (!Validazioni.partitaIva(piva)) {
                e.put("PIVA", "Partita IVA non valida: servono 11 cifre (controlla anche l'ultima, di controllo).");
            }
            if (cf == null) {
                e.put("CF", "Indica il codice fiscale.");
            } else if (!Validazioni.codiceFiscale(cf, studio)) {
                e.put("CF", studio ? "Codice fiscale non valido: 16 caratteri, oppure 11 cifre per le societa'."
                        : "Codice fiscale non valido: controlla i 16 caratteri.");
            }
            if (pec == null) {
                e.put("PEC", "Indica la PEC.");
            } else if (!Validazioni.email(pec)) {
                e.put("PEC", "Indirizzo PEC non valido.");
            }
            if (!Validazioni.codiceSdi(sdi)) {
                e.put("SDI", "Il codice destinatario SDI ha 7 caratteri, lettere o cifre.");
            }
            lunghezza(e, "INDIRIZZO", indirizzo, 50, "Indica l'indirizzo.");
            lunghezza(e, "CITTA", citta, 50, "Indica la citta'.");
            if (!Validazioni.provincia(provincia)) {
                e.put("PROVINCIA", "Sigla della provincia: 2 lettere (es. PV).");
            }
            if (!Validazioni.cap(cap)) {
                e.put("CAP", "Il CAP ha 5 cifre.");
            }
            obbligatorio(e, "REF_NOME", nome, "Indica il nome del referente.");
            obbligatorio(e, "REF_COGNOME", cognome, "Indica il cognome del referente.");
            if (email == null) {
                e.put("EMAIL", "Indica l'email: sara' il tuo nome utente.");
            } else if (!Validazioni.email(email)) {
                e.put("EMAIL", "Indirizzo email non valido.");
            }
            if (!Validazioni.telefono(telefono)) {
                e.put("TELEFONO", telefono == null ? "Indica un numero di telefono." : "Numero di telefono non valido.");
            }
            if (!privacy) {
                e.put("PRIVACY", "Per inviare la richiesta serve il consenso.");
            }
            // niente parentesi angolari nei testi liberi (vanno in pagine e mail)
            String[][] liberi = { { "DENOMINAZIONE", denominazione }, { "ALBO", albo }, { "INDIRIZZO", indirizzo },
                    { "CITTA", citta }, { "CONDOMINI", condomini }, { "MODELLO", modello }, { "REF_NOME", nome },
                    { "REF_COGNOME", cognome } };
            for (String[] l : liberi) {
                if (!e.containsKey(l[0]) && !Validazioni.testoPulito(l[1])) {
                    e.put(l[0], "Il testo non puo' contenere i caratteri < e >.");
                }
            }
            return e;
        }

        Map<String, String> segnaposto(Parametri p, Integer numero, int numeroFoto) {

            String link = p.testo(RegistrazioniCostanti.PAR_LINK_ACCESSO);
            Map<String, String> s = new HashMap<String, String>();
            s.put("NOME", nome);
            s.put("COGNOME", cognome);
            s.put("DENOMINAZIONE", denominazione);
            s.put("NUMERO", String.valueOf(numero));
            s.put("TEMPI_RISPOSTA", p.testo(RegistrazioniCostanti.PAR_TEMPI_RISPOSTA));
            s.put("LINK", link);
            s.put("TIPO_SOGGETTO", RegistrazioniCostanti.SOGGETTO_STUDIO.equals(tipo) ? "studio o societa'"
                    : "amministratore singolo");
            s.put("CITTA", citta);
            s.put("PROVINCIA", provincia);
            s.put("FASCIA_CONDOMINI", fascia == null ? "non indicato" : fascia);
            s.put("MARCA_MODELLO", modello == null ? "non indicati (vedi foto)" : modello);
            s.put("NUMERO_FOTO", String.valueOf(numeroFoto));
            s.put("LINK_APPROVAZIONE", linkApprovazione(link, numero));
            return s;
        }

        private static void obbligatorio(Map<String, String> e, String campo, String valore, String messaggio) {

            if (valore == null) {
                e.put(campo, messaggio);
            }
        }

        private static void lunghezza(Map<String, String> e, String campo, String valore, int max, String messaggio) {

            if (valore == null) {
                e.put(campo, messaggio);
            } else if (valore.length() > max) {
                e.put(campo, "Al massimo " + max + " caratteri.");
            }
        }

        private static String minuscolo(String v) {

            return v == null ? null : v.toLowerCase();
        }
    }

    /** Link diretto alla richiesta nella pagina di approvazione (vuoto se LINK.ACCESSO e' vuoto). */
    static String linkApprovazione(String link, Integer numero) {

        if (link == null || link.length() == 0) {
            return "";
        }
        return (link.endsWith("/") ? link : link + "/") + "astro?FUNCTIONID=RegistrazioniRichieste&ID_RICHIESTA=" + numero;
    }
}
