package net.projectsrl.wera.registrazioni;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;

/**
 * FUNCTIONID=RegistrazioniDati (GET, JSON; solo profili di APPROVAZIONE.PROFILI tranne "accesso").
 * OPERAZIONE=accesso (voce di menu), elenco (STATO), dettaglio (ID_RICHIESTA: richiesta, foto, registro,
 * controlli), foto (ID_ALLEGATO: l'immagine, in linea). Nessun hash di password esce di qui.
 */
public class FunctionRegistrazioniDati extends FunctionRegistrazioni_base {

    private static final String   SQL_ELENCO          = "SELECT id_richiesta, tipo_soggetto, denominazione, partita_iva, "
            + "citta, provincia, referente_nome, referente_cognome, email, fascia_condomini, stato, esito_compatibilita, "
            + "numero_allegati, giorni_dalla_richiesta, ts_ins, ts_esito, username_utente "
            + "FROM registrazioni.v_richieste WHERE (? = 'TUTTE' OR stato = ?) ORDER BY ts_ins DESC LIMIT 500";
    private static final String   SQL_CONTEGGI        = "SELECT stato, count(*) AS numero FROM registrazioni.richieste "
            + "GROUP BY stato";
    private static final String   SQL_DETTAGLIO       = "SELECT * FROM registrazioni.v_richieste WHERE id_richiesta = ?";
    private static final String   SQL_ALLEGATI        = "SELECT id_allegato, nome_file_originale, tipo_mime, dimensione_byte, "
            + "ts_ins FROM registrazioni.allegati WHERE id_richiesta = ? ORDER BY id_allegato";
    private static final String   SQL_EVENTI          = "SELECT e.ts_evento, e.tipo_evento, e.dettaglio, "
            + "TRIM(COALESCE(u.nome, '') || ' ' || COALESCE(u.cognome, '')) AS utente FROM registrazioni.eventi e "
            + "LEFT JOIN public.utenti u ON u.id_utente = e.id_utente WHERE e.id_richiesta = ? ORDER BY e.id_evento";
    private static final String   SQL_UTENTE_EMAIL    = "SELECT id_utente, username FROM public.utenti "
            + "WHERE lower(username) = lower(?) OR lower(email) = lower(?) ORDER BY id_utente";
    private static final String   SQL_STESSA_PIVA     = "SELECT id_richiesta, stato, denominazione, username_utente "
            + "FROM registrazioni.v_richieste WHERE partita_iva = ? AND id_richiesta <> ? ORDER BY id_richiesta DESC";
    private static final String   SQL_ALLEGATO        = "SELECT id_richiesta, nome_file_originale, percorso_file, tipo_mime "
            + "FROM registrazioni.allegati WHERE id_allegato = ?";
    private static final String[] STATI               = { "IN_ATTESA", "APPROVATA", "RIFIUTATA", "ANNULLATA", "TUTTE" };

    public FunctionRegistrazioniDati(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        mostra(req, res, userInfo);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Parametri p = Parametri.carica();
        Approvatore a = new Approvatore(getSpecificUserInfo(userInfo), p);
        String operazione = req.getParameter("OPERAZIONE");

        if ("accesso".equals(operazione)) {
            memorizzaAccesso(req, a);
            Map<String, Object> accesso = new LinkedHashMap<String, Object>();
            accesso.put("abilitato", Boolean.valueOf(a.isAbilitato()));
            accesso.put("in_attesa", Integer.valueOf(a.isAbilitato() ? contaInAttesa() : 0));
            Map<String, Object> risposta = new LinkedHashMap<String, Object>();
            risposta.put("accesso", accesso);
            rispondiJson(res, HttpServletResponse.SC_OK, risposta);
            return;
        }
        if (!a.isAbilitato()) {
            Eventi.registra(RegistrazioniCostanti.EVENTO_ACCESSO_NEGATO, null, a.getIdUtente(), a.getProfilo(),
                    req.getRemoteAddr(), "dati richieste: " + operazione);
            rispondiErrore(res, HttpServletResponse.SC_FORBIDDEN, "Profilo non abilitato alle richieste di registrazione");
            return;
        }

        if ("elenco".equals(operazione)) {
            elenco(req, res);
        } else if ("dettaglio".equals(operazione)) {
            dettaglio(req, res, p);
        } else if ("foto".equals(operazione)) {
            foto(req, res, p, a);
        } else {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "OPERAZIONE non valida");
        }
    }

    private void elenco(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        String stato = "IN_ATTESA";
        for (String s : STATI) {
            if (s.equals(req.getParameter("STATO"))) {
                stato = s;
            }
        }
        Map<String, Object> conteggi = new LinkedHashMap<String, Object>();
        for (String s : STATI) {
            conteggi.put(s, Integer.valueOf(0));
        }
        int tutte = 0;
        for (Map<String, Object> r : Db.leggi(SQL_CONTEGGI)) {
            int n = ((Number) r.get("numero")).intValue();
            conteggi.put((String) r.get("stato"), Integer.valueOf(n));
            tutte += n;
        }
        conteggi.put("TUTTE", Integer.valueOf(tutte));
        Map<String, Object> risposta = new LinkedHashMap<String, Object>();
        risposta.put("stato", stato);
        risposta.put("conteggi", conteggi);
        risposta.put("richieste", Db.leggi(SQL_ELENCO, stato, stato));
        rispondiJson(res, HttpServletResponse.SC_OK, risposta);
    }

    private void dettaglio(SsbServletRequest req, SsbServletResponse res, Parametri p) throws AppCrash {

        Integer id = intero(req.getParameter("ID_RICHIESTA"));
        List<Map<String, Object>> righe = id == null ? null : Db.leggi(SQL_DETTAGLIO, id);
        if (righe == null || righe.isEmpty()) {
            rispondiErrore(res, HttpServletResponse.SC_NOT_FOUND, "Richiesta non trovata");
            return;
        }
        Map<String, Object> r = righe.get(0);
        Map<String, Object> controlli = new LinkedHashMap<String, Object>();
        controlli.put("utenti_stessa_email", Db.leggi(SQL_UTENTE_EMAIL, r.get("email"), r.get("email")));
        controlli.put("richieste_stessa_piva", Db.leggi(SQL_STESSA_PIVA, r.get("partita_iva"), id));
        controlli.put("azienda_codice", p.testo(RegistrazioniCostanti.PAR_AZIENDA_CODICE));
        controlli.put("profilo", p.testo(RegistrazioniCostanti.PAR_PROFILO, "AMM"));
        controlli.put("mail_attiva", Boolean.valueOf(p.vero(RegistrazioniCostanti.PAR_MAIL_ATTIVA, true)));

        Map<String, Object> risposta = new LinkedHashMap<String, Object>();
        risposta.put("richiesta", r);
        risposta.put("allegati", Db.leggi(SQL_ALLEGATI, id));
        risposta.put("eventi", Db.leggi(SQL_EVENTI, id));
        risposta.put("controlli", controlli);
        rispondiJson(res, HttpServletResponse.SC_OK, risposta);
    }

    private void foto(SsbServletRequest req, SsbServletResponse res, Parametri p, Approvatore a) throws AppCrash {

        Integer id = intero(req.getParameter("ID_ALLEGATO"));
        List<Map<String, Object>> righe = id == null ? null : Db.leggi(SQL_ALLEGATO, id);
        File file = null;
        if (righe != null && !righe.isEmpty()) {
            File cartella = Allegati.cartella(_applicationSrv.getRoot(), p.testo(RegistrazioniCostanti.PAR_FILE_CARTELLA));
            file = Allegati.risolvi(cartella, (String) righe.get(0).get("percorso_file"));
        }
        if (file == null) {
            rispondiErrore(res, HttpServletResponse.SC_NOT_FOUND, "Foto non trovata");
            return;
        }
        Map<String, Object> riga = righe.get(0);
        if (!"S".equals(req.getParameter("ANTEPRIMA"))) {
            Eventi.registra(RegistrazioniCostanti.EVENTO_DOWNLOAD, (Integer) riga.get("id_richiesta"), a.getIdUtente(),
                    a.getProfilo(), req.getRemoteAddr(), "foto " + riga.get("nome_file_originale"));
        }
        String nome = ((String) riga.get("nome_file_originale")).replaceAll("[^A-Za-z0-9._ -]", "_");
        InputStream in = null;
        try {
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType((String) riga.get("tipo_mime"));
            res.setHeader("Content-Disposition", "inline; filename=\"" + nome + "\"");
            res.setHeader("Cache-Control", "private, max-age=600");
            res.setHeader("X-Content-Type-Options", "nosniff");
            res.setContentLength((int) file.length());
            in = new FileInputStream(file);
            OutputStream out = res.getOutputStream();
            byte[] buffer = new byte[8192];
            int letti;
            while ((letti = in.read(buffer)) != -1) {
                out.write(buffer, 0, letti);
            }
            out.flush();
            out.close();
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext(this.getClass().getName(), "Foto non inviata: allegato " + id);
            throw ac;
        } finally {
            Allegati.chiudi(in);
        }
    }

    /** Intero positivo da un parametro, null se assente o non valido. */
    static Integer intero(String valore) {

        if (valore == null || !valore.trim().matches("[0-9]{1,9}")) {
            return null;
        }
        int n = Integer.parseInt(valore.trim());
        return n > 0 ? Integer.valueOf(n) : null;
    }
}
