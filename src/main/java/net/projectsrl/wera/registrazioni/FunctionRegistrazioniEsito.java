package net.projectsrl.wera.registrazioni;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.WebAppConstants_itf;

/**
 * FUNCTIONID=RegistrazioniEsito (POST, JSON; solo profili di APPROVAZIONE.PROFILI).
 * <ul>
 * <li>OPERAZIONE=verifica: esito della verifica ripartitori e note (richiesta in attesa o approvata);</li>
 * <li>OPERAZIONE=approva: crea l'utente di /wera in UNA transazione (utenti, utenti_lingue, utenti_profili,
 * utenti_aziende, password con l'hash scelto dal richiedente) e chiude la richiesta; poi la mail di esito;</li>
 * <li>OPERAZIONE=rifiuta (MOTIVO obbligatorio, va al richiedente) e annulla (spam o doppione, nessuna mail).</li>
 * </ul>
 * L'hash della password viene azzerato a ogni esito. Il legame ai condomini resta nell'anagrafica condominio.
 */
public class FunctionRegistrazioniEsito extends FunctionRegistrazioni_base {

    private static final String SQL_BLOCCA         = "SELECT * FROM registrazioni.richieste WHERE id_richiesta = ? FOR UPDATE";
    private static final String SQL_VERIFICA       = "UPDATE registrazioni.richieste SET esito_compatibilita = ?, "
            + "nota_compatibilita = ?, nota_interna = ?, ts_mod = current_timestamp, id_utente_mod = ? WHERE id_richiesta = ?";
    private static final String SQL_UTENTE_ESISTE  = "SELECT count(*) FROM public.utenti "
            + "WHERE lower(username) = lower(?) OR lower(email) = lower(?)";
    private static final String SQL_AZIENDA        = "SELECT id_azienda FROM public.aziende WHERE codice = ? ORDER BY id_azienda LIMIT 1";
    private static final String SQL_PROFILO        = "SELECT id_profilo FROM public.profili WHERE trim(codice) = ? LIMIT 1";
    private static final String SQL_LINGUA         = "SELECT id_lingue_iso FROM public.lingue_iso WHERE trim(codice_iso) = ? LIMIT 1";
    private static final String SQL_UTENTE         = "INSERT INTO public.utenti (username, cognome, nome, email, id_utente_ins, "
            + "fl_disattivo, provincia, cap, codfisc, telefono, indirizzo, localita) "
            + "VALUES (?, ?, ?, ?, ?, false, ?, ?, ?, ?, ?, ?) RETURNING id_utente";
    private static final String SQL_UTENTE_LINGUA  = "INSERT INTO public.utenti_lingue (id_utente, id_lingue_iso, fl_default) "
            + "VALUES (?, ?, true)";
    private static final String SQL_UTENTE_PROFILO = "INSERT INTO public.utenti_profili (id_utente, id_profilo) VALUES (?, ?)";
    private static final String SQL_UTENTE_AZIENDA = "INSERT INTO public.utenti_aziende (id_utente, id_azienda) VALUES (?, ?)";
    private static final String SQL_PASSWORD       = "INSERT INTO public.password (id_utente, password, dt_scadenza, fl_valida) "
            + "VALUES (?, ?, ?, true)";
    private static final String SQL_APPROVATA      = "UPDATE registrazioni.richieste SET stato = 'APPROVATA', id_utente = ?, "
            + "id_azienda = ?, hash_password = NULL, ts_esito = current_timestamp, id_utente_esito = ?, "
            + "ts_mod = current_timestamp, id_utente_mod = ? WHERE id_richiesta = ?";
    private static final String SQL_CHIUSA         = "UPDATE registrazioni.richieste SET stato = ?, motivo_rifiuto = ?, "
            + "nota_interna = COALESCE(?, nota_interna), hash_password = NULL, ts_esito = current_timestamp, "
            + "id_utente_esito = ?, ts_mod = current_timestamp, id_utente_mod = ? WHERE id_richiesta = ?";

    private static final String[] ESITI = { "DA_VERIFICARE", "COMPATIBILE", "PARZIALE", "NON_COMPATIBILE" };

    public FunctionRegistrazioniEsito(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        rispondiErrore(res, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Usare POST");
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Parametri p = Parametri.carica();
        Approvatore a = new Approvatore(getSpecificUserInfo(userInfo), p);
        String operazione = req.getParameter("OPERAZIONE");
        if (!a.isAbilitato()) {
            Eventi.registra(RegistrazioniCostanti.EVENTO_ACCESSO_NEGATO, null, a.getIdUtente(), a.getProfilo(),
                    req.getRemoteAddr(), "esito richiesta: " + operazione);
            rispondiErrore(res, HttpServletResponse.SC_FORBIDDEN, "Profilo non abilitato alle richieste di registrazione");
            return;
        }
        Integer id = FunctionRegistrazioniDati.intero(req.getParameter("ID_RICHIESTA"));
        if (id == null) {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "ID_RICHIESTA mancante");
            return;
        }
        String esito = pulito(req.getParameter("ESITO_COMPATIBILITA"), 20);
        if (esito != null && !java.util.Arrays.asList(ESITI).contains(esito)) {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "Esito della verifica non valido");
            return;
        }
        String notaCompatibilita = pulito(req.getParameter("NOTA_COMPATIBILITA"), 2000);
        String notaInterna = pulito(req.getParameter("NOTA_INTERNA"), 2000);

        if ("verifica".equals(operazione)) {
            verifica(req, res, a, id, esito, notaCompatibilita, notaInterna);
        } else if ("approva".equals(operazione)) {
            approva(req, res, p, a, id, esito, notaCompatibilita, notaInterna);
        } else if ("rifiuta".equals(operazione) || "annulla".equals(operazione)) {
            chiudi(req, res, p, a, id, "rifiuta".equals(operazione), pulito(req.getParameter("MOTIVO"), 2000), notaInterna);
        } else {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "OPERAZIONE non valida");
        }
    }

    // ------------------------------------------------------------------------------------------------ verifica

    private void verifica(SsbServletRequest req, SsbServletResponse res, Approvatore a, Integer id, String esito,
            String notaCompatibilita, String notaInterna) throws AppCrash {

        DBTransaction tx = new DBTransaction();
        try {
            Map<String, Object> r = blocca(tx, id);
            if (r == null || !statoModificabile(r)) {
                tx.rollBack();
                rispondiErrore(res, HttpServletResponse.SC_CONFLICT, r == null ? "Richiesta non trovata"
                        : "La richiesta e' " + r.get("stato") + ": non si modifica piu'");
                return;
            }
            aggiornaVerifica(tx, a, id, r, esito, notaCompatibilita, notaInterna);
            tx.commit();
        } catch (Throwable t) {
            annulla(tx);
            throw Db.errore(t, "Verifica richiesta " + id);
        } finally {
            tx.end();
        }
        Eventi.registra(RegistrazioniCostanti.EVENTO_MODIFICA, id, a.getIdUtente(), a.getProfilo(), req.getRemoteAddr(),
                "verifica ripartitori: " + (esito == null ? "invariata" : esito));
        ok(res, null);
    }

    // ------------------------------------------------------------------------------------------------ approva

    private void approva(SsbServletRequest req, SsbServletResponse res, Parametri p, Approvatore a, Integer id,
            String esito, String notaCompatibilita, String notaInterna) throws AppCrash {

        String codiceAzienda = p.testo(RegistrazioniCostanti.PAR_AZIENDA_CODICE);
        String codiceProfilo = p.testo(RegistrazioniCostanti.PAR_PROFILO, "AMM");
        int giorni = p.intero(RegistrazioniCostanti.PAR_PASSWORD_GIORNI, 60);
        Map<String, Object> r;
        Integer idUtente;
        Integer idAzienda;
        DBTransaction tx = new DBTransaction();
        try {
            r = blocca(tx, id);
            if (r == null || !RegistrazioniCostanti.STATO_IN_ATTESA.equals(r.get("stato"))) {
                tx.rollBack();
                rispondiErrore(res, HttpServletResponse.SC_CONFLICT, r == null ? "Richiesta non trovata"
                        : "La richiesta e' gia' " + r.get("stato"));
                return;
            }
            String email = (String) r.get("email");
            if (Db.intero(tx, SQL_UTENTE_ESISTE, email, email).intValue() > 0) {
                tx.rollBack();
                rispondiErrore(res, HttpServletResponse.SC_CONFLICT, "Esiste gia' un utente con l'email " + email
                        + ": la richiesta non si puo' approvare (rifiutala o annullala indicando il motivo).");
                return;
            }
            if (r.get("hash_password") == null) {
                tx.rollBack();
                rispondiErrore(res, HttpServletResponse.SC_CONFLICT, "Manca la password scelta dal richiedente.");
                return;
            }
            idAzienda = Db.intero(tx, SQL_AZIENDA, codiceAzienda);
            Integer idProfilo = Db.intero(tx, SQL_PROFILO, codiceProfilo);
            Integer idLingua = Db.intero(tx, SQL_LINGUA, WebAppConstants_itf.DEFAULT_ISO_LANGUAGE);
            if (idAzienda == null || idProfilo == null || idLingua == null) {
                tx.rollBack();
                rispondiErrore(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Configurazione incompleta: "
                        + (idAzienda == null ? "azienda '" + codiceAzienda + "' (ATTIVAZIONE.AZIENDA_CODICE) " : "")
                        + (idProfilo == null ? "profilo '" + codiceProfilo + "' (ATTIVAZIONE.PROFILO) " : "")
                        + (idLingua == null ? "lingua di default " : "") + "non trovati.");
                return;
            }
            aggiornaVerifica(tx, a, id, r, esito, notaCompatibilita, notaInterna);

            String cf = (String) r.get("codice_fiscale");
            idUtente = Db.intero(tx, SQL_UTENTE, email, r.get("referente_cognome"), r.get("referente_nome"), email,
                    Db.intero(a.getIdUtente()), r.get("provincia"), r.get("cap"), Db.testo(cf), r.get("telefono"),
                    r.get("indirizzo"), r.get("citta"));
            Db.esegui(tx, SQL_UTENTE_LINGUA, idUtente, idLingua);
            Db.esegui(tx, SQL_UTENTE_PROFILO, idUtente, idProfilo);
            Db.esegui(tx, SQL_UTENTE_AZIENDA, idUtente, idAzienda);
            Db.esegui(tx, SQL_PASSWORD, idUtente, r.get("hash_password"), scadenzaPassword(giorni));
            Db.esegui(tx, SQL_APPROVATA, idUtente, idAzienda, Db.intero(a.getIdUtente()), Db.intero(a.getIdUtente()), id);
            tx.commit();
        } catch (Throwable t) {
            annulla(tx);
            if (Db.violazioneUnivoca(t)) {
                rispondiErrore(res, HttpServletResponse.SC_CONFLICT, "Nome utente gia' esistente: approvazione annullata.");
                return;
            }
            AppCrash ac = Db.errore(t, "Approvazione richiesta " + id);
            Eventi.registra(RegistrazioniCostanti.EVENTO_ERRORE, id, a.getIdUtente(), a.getProfilo(), req.getRemoteAddr(),
                    "approvazione fallita: " + ac.getMessage());
            rispondiErrore(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Approvazione non riuscita: nessuna modifica.");
            return;
        } finally {
            tx.end();
        }

        Eventi.registra(RegistrazioniCostanti.EVENTO_APPROVAZIONE, id, a.getIdUtente(), a.getProfilo(),
                req.getRemoteAddr(), "utente " + idUtente + " (" + r.get("email") + "), profilo " + codiceProfilo
                        + ", azienda " + codiceAzienda);

        String esitoEffettivo = esito != null ? esito : (String) r.get("esito_compatibilita");
        String notaEffettiva = notaCompatibilita != null ? notaCompatibilita : (String) r.get("nota_compatibilita");
        Map<String, String> s = segnaposto(p, r, id);
        s.put("USERNAME", (String) r.get("email"));
        String riga = p.testo("ESITO." + esitoEffettivo + ".TESTO");
        s.put("ESITO_COMPATIBILITA", riga + (notaEffettiva == null ? "" : (riga.length() > 0 ? "\n" : "") + notaEffettiva));
        String mail = Posta.invia(p, RegistrazioniCostanti.MAIL_APPROVATA, (String) r.get("email"), s, id,
                a.getIdUtente(), req.getRemoteAddr());

        Map<String, Object> dati = new LinkedHashMap<String, Object>();
        dati.put("id_utente", idUtente);
        dati.put("username", r.get("email"));
        dati.put("mail", mail);
        ok(res, dati);
    }

    // ------------------------------------------------------------------------------------------------ rifiuta / annulla

    private void chiudi(SsbServletRequest req, SsbServletResponse res, Parametri p, Approvatore a, Integer id,
            boolean rifiuto, String motivo, String notaInterna) throws AppCrash {

        if (rifiuto && motivo == null) {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "Indica il motivo del rifiuto: va nella mail al richiedente.");
            return;
        }
        String stato = rifiuto ? RegistrazioniCostanti.STATO_RIFIUTATA : RegistrazioniCostanti.STATO_ANNULLATA;
        Map<String, Object> r;
        DBTransaction tx = new DBTransaction();
        try {
            r = blocca(tx, id);
            if (r == null || !RegistrazioniCostanti.STATO_IN_ATTESA.equals(r.get("stato"))) {
                tx.rollBack();
                rispondiErrore(res, HttpServletResponse.SC_CONFLICT, r == null ? "Richiesta non trovata"
                        : "La richiesta e' gia' " + r.get("stato"));
                return;
            }
            Db.esegui(tx, SQL_CHIUSA, stato, Db.testo(motivo), Db.testo(notaInterna), Db.intero(a.getIdUtente()),
                    Db.intero(a.getIdUtente()), id);
            tx.commit();
        } catch (Throwable t) {
            annulla(tx);
            throw Db.errore(t, "Chiusura richiesta " + id);
        } finally {
            tx.end();
        }
        Eventi.registra(rifiuto ? RegistrazioniCostanti.EVENTO_RIFIUTO : RegistrazioniCostanti.EVENTO_ANNULLAMENTO, id,
                a.getIdUtente(), a.getProfilo(), req.getRemoteAddr(), motivo);

        String mail = null;
        if (rifiuto) {
            Map<String, String> s = segnaposto(p, r, id);
            s.put("MOTIVO", motivo);
            mail = Posta.invia(p, RegistrazioniCostanti.MAIL_RIFIUTATA, (String) r.get("email"), s, id, a.getIdUtente(),
                    req.getRemoteAddr());
        }
        Map<String, Object> dati = new LinkedHashMap<String, Object>();
        dati.put("mail", mail);
        ok(res, dati);
    }

    // ------------------------------------------------------------------------------------------------ comuni

    private static Map<String, Object> blocca(DBTransaction tx, Integer id) throws Exception {

        List<Map<String, Object>> righe = Db.righe(tx, SQL_BLOCCA, id);
        return righe.isEmpty() ? null : righe.get(0);
    }

    private static boolean statoModificabile(Map<String, Object> r) {

        return RegistrazioniCostanti.STATO_IN_ATTESA.equals(r.get("stato"))
                || RegistrazioniCostanti.STATO_APPROVATA.equals(r.get("stato"));
    }

    /** Aggiorna esito e note solo se arrivano dal form (null = lascia il valore che c'e'). */
    private static void aggiornaVerifica(DBTransaction tx, Approvatore a, Integer id, Map<String, Object> r, String esito,
            String notaCompatibilita, String notaInterna) throws Exception {

        if (esito == null && notaCompatibilita == null && notaInterna == null) {
            return;
        }
        Db.esegui(tx, SQL_VERIFICA, esito != null ? esito : (String) r.get("esito_compatibilita"),
                Db.testo(notaCompatibilita != null ? notaCompatibilita : (String) r.get("nota_compatibilita")),
                Db.testo(notaInterna != null ? notaInterna : (String) r.get("nota_interna")), Db.intero(a.getIdUtente()), id);
    }

    private static Map<String, String> segnaposto(Parametri p, Map<String, Object> r, Integer id) {

        Map<String, String> s = new HashMap<String, String>();
        s.put("NOME", (String) r.get("referente_nome"));
        s.put("COGNOME", (String) r.get("referente_cognome"));
        s.put("DENOMINAZIONE", (String) r.get("denominazione"));
        s.put("NUMERO", String.valueOf(id));
        s.put("LINK", p.testo(RegistrazioniCostanti.PAR_LINK_ACCESSO));
        return s;
    }

    /** Scadenza della password come la scrive NewPassword: AAAA/MM/GG fra N giorni. */
    private static String scadenzaPassword(int giorni) {

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, giorni);
        return new SimpleDateFormat("yyyy/MM/dd", Locale.ITALY).format(c.getTime());
    }

    private static void annulla(DBTransaction tx) {

        try {
            tx.rollBack();
        } catch (AppCrash ignorata) {
            // gia' annullata
        }
    }

    private void ok(SsbServletResponse res, Map<String, Object> dati) throws AppCrash {

        Map<String, Object> esito = new LinkedHashMap<String, Object>();
        esito.put("esito", "OK");
        if (dati != null) {
            esito.putAll(dati);
        }
        rispondiJson(res, HttpServletResponse.SC_OK, esito);
    }
}
