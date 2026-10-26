package net.projectsrl.wera.centraletermica.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.mail.SendSMTPMail;

/**
 * FUNCTIONID=CentraleTermicaAvvisiInvio: controllo delle scadenze e invio degli avvisi.
 * <ul>
 * <li>POST da un utente di CONFIGURAZIONE.PROFILI (pulsante "Controlla ora" della pagina)</li>
 * <li>GET/POST con TOKEN = parametro ALERT.TOKEN, solo da localhost (cron giornaliero sul server:
 * <code>curl -s 'http://localhost/wera2026/astro?FUNCTIONID=CentraleTermicaAvvisiInvio&amp;TOKEN=...'</code>)</li>
 * </ul>
 * Per ogni scadenza APERTA: soglia = la piu' piccola fra ALERT.GIORNI che sia >= ai giorni mancanti
 * (oppure -1 se superata e ALERT.SCADUTE=S); se per quella scadenza e soglia non c'e' gia' un invio (o un
 * SALTATO) manda la mail all'amministratore del condominio (ALERT.AMMINISTRATORE) con ALERT.CC in copia,
 * tramite l'SMTP del profilo (in wera2026 PROD oggi il mailcatcher: nessuna mail reale), e registra
 * l'esito in INVII_AVVISI. Parametri letti per l'azienda del condominio.
 */
public class FunctionCentraleTermicaAvvisiInvio extends FunctionCentraleTermica_base {

    private static final String SQL_REGISTRA = "INSERT INTO CENTRALE_TERMICA.INVII_AVVISI (ID_AZIENDA, ID_CONDOMINIO, "
            + "ID_SCADENZA, SOGLIA_GIORNI, DESTINATARI, OGGETTO, ESITO, DETTAGLIO, ID_UTENTE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public FunctionCentraleTermicaAvvisiInvio() {

        super();
    }

    public FunctionCentraleTermicaAvvisiInvio(ApplicationServices_itf applServices, String functionID,
            String functionName) {

        super(applServices, functionID, functionName);
    }

    /** Il cron non ha sessione: l'autorizzazione e' nel metodo (token + localhost, oppure utente abilitato). */
    @Override
    public boolean isAuthenticationRequired() {

        return false;
    }

    @Override
    protected boolean checkSession(SsbServletRequest req) {

        return true;
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        elabora(req, res, userInfo);
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        LetturaParametri par = new LetturaParametri();
        Integer idUtente = null;
        String token = req.getParameter("TOKEN");

        if (Util.IsNotEmpty(token)) {
            String atteso = par.valore("ALERT.TOKEN", null);
            if (!locale(req) || atteso.length() < 16 || !atteso.equals(token)) {
                registraEvento(null, CentraleTermicaCostanti.EVENTO_ACCESSO_NEGATO, null, null, null, null,
                        "controllo avvisi: token o indirizzo non validi");
                rispondiErrore(res, HttpServletResponse.SC_FORBIDDEN, "Non autorizzato");
                return;
            }
        } else {
            boolean ammesso = false;
            if (userInfo != null && req.getSession(false) != null && req.getSession(false).getAttribute("LOGIN") != null
                    && "POST".equalsIgnoreCase(req.getMethod())) {
                ContestoUtente ctx = new ContestoUtente(getSpecificUserInfo(userInfo));
                ammesso = ctx.puoConfigurare();
                idUtente = ctx.getIdUtente();
                if (!ammesso) {
                    negato(ctx, res, "Profilo non abilitato al controllo degli avvisi", null, "avvisi");
                    return;
                }
            }
            if (!ammesso) {
                rispondiErrore(res, HttpServletResponse.SC_FORBIDDEN, "Non autorizzato");
                return;
            }
        }

        rispondiJson(res, HttpServletResponse.SC_OK, esegui(par, idUtente));
    }

    /** Indirizzo del socket (non quello dichiarato dagli header dei proxy) di loopback. */
    private static boolean locale(SsbServletRequest req) {

        ServletRequest originale = req.getRequest();
        String ip = originale != null ? originale.getRemoteAddr() : null;
        return "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip);
    }

    private Map<String, Object> esegui(LetturaParametri par, Integer idUtente) throws AppCrash {

        int esaminate = 0, inviati = 0, errori = 0, saltati = 0;
        List<Map<String, Object>> dettaglio = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> scadenze = LetturaDataset.leggi(CentraleTermicaCostanti.DS_AVVISI_DA_INVIARE,
                new HashMap<String, String>(), "ID_SCADENZA", "ID_AZIENDA", "ID_CONDOMINIO", "DENOMINAZIONE",
                "INDIRIZZO", "LOCALITA", "SIGLA", "DESCRIZIONE", "ENTE", "DATA_SCADENZA", "GIORNI_MANCANTI",
                "EMAIL_AMMINISTRATORE", "SOGLIE_REGISTRATE");

        for (Map<String, Object> s : scadenze) {
            Integer idAzienda = LetturaDataset.intero(s, "ID_AZIENDA");
            if (!par.vero("ALERT.ATTIVO", idAzienda)) {
                continue;
            }
            esaminate++;
            int giorni = LetturaDataset.intero(s, "GIORNI_MANCANTI").intValue();
            Integer soglia = soglia(giorni, par.interi("ALERT.GIORNI", idAzienda), par.vero("ALERT.SCADUTE", idAzienda));
            if (soglia == null || ("," + LetturaDataset.testo(s, "SOGLIE_REGISTRATE") + ",").contains("," + soglia + ",")) {
                continue;
            }

            List<String> a = new ArrayList<String>();
            String email = LetturaDataset.testo(s, "EMAIL_AMMINISTRATORE");
            if (par.vero("ALERT.AMMINISTRATORE", idAzienda) && valida(email)) {
                a.add(email);
            }
            List<String> cc = new ArrayList<String>();
            for (String c : par.valore("ALERT.CC", idAzienda).split(",")) {
                if (valida(c.trim()) && !cc.contains(c.trim())) {
                    cc.add(c.trim());
                }
            }
            if (a.isEmpty() && !cc.isEmpty()) {
                a.add(cc.remove(0));
            }

            String oggetto = testo(par.valore("ALERT.OGGETTO", idAzienda), s);
            if (oggetto.length() == 0) {
                oggetto = testo("Scadenza {sigla} - {condominio}", s);
            }
            String destinatari = join(a) + (cc.isEmpty() ? "" : " (Cc: " + join(cc) + ")");
            String esito;
            String nota = null;
            if (a.isEmpty()) {
                esito = "SALTATO";
                nota = "nessun destinatario (email dell'amministratore assente e ALERT.CC vuoto)";
                saltati++;
            } else {
                try {
                    invia(join(a), join(cc), oggetto, corpo(s, soglia.intValue(), par.valore("ALERT.LINK", idAzienda)));
                    esito = "INVIATO";
                    inviati++;
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Avviso non inviato, scadenza " + s.get("ID_SCADENZA"));
                    esito = "ERRORE";
                    nota = String.valueOf(t.getMessage());
                    errori++;
                }
            }
            registra(s, soglia, destinatari, oggetto, esito, nota, idUtente);

            Map<String, Object> riga = new LinkedHashMap<String, Object>();
            riga.put("condominio", s.get("DENOMINAZIONE"));
            riga.put("sigla", s.get("SIGLA"));
            riga.put("soglia", soglia);
            riga.put("esito", esito);
            dettaglio.add(riga);
        }

        Map<String, Object> out = new LinkedHashMap<String, Object>();
        out.put("esito", "OK");
        out.put("esaminate", Integer.valueOf(esaminate));
        out.put("inviati", Integer.valueOf(inviati));
        out.put("errori", Integer.valueOf(errori));
        out.put("saltati", Integer.valueOf(saltati));
        out.put("mail_di_prova", Boolean.valueOf(mailDiProva()));
        out.put("dettaglio", dettaglio);
        return out;
    }

    /** Piu' piccola soglia >= giorni mancanti; -1 per scadenza superata (se previsto); null = nessun avviso. */
    static Integer soglia(int giorni, List<Integer> soglie, boolean scadute) {

        if (giorni < 0) {
            return scadute ? Integer.valueOf(-1) : null;
        }
        for (Integer t : soglie) {
            if (giorni <= t.intValue()) {
                return t;
            }
        }
        return null;
    }

    /** Vero se l'SMTP del profilo e' locale (mailcatcher): nessuna mail esce davvero. */
    static boolean mailDiProva() {

        String host = Config.GetInstance().getProperty("mail.SMTPHost", "");
        return host.equals("127.0.0.1") || host.equalsIgnoreCase("localhost");
    }

    private static boolean valida(String email) {

        return email != null && email.matches("[^@\\s,;]+@[^@\\s,;]+\\.[^@\\s,;]+");
    }

    private static String join(List<String> l) {

        StringBuilder sb = new StringBuilder();
        for (String s : l) {
            sb.append(sb.length() == 0 ? "" : ",").append(s);
        }
        return sb.toString();
    }

    private static String data(Object d) {

        return d instanceof java.util.Date ? new SimpleDateFormat("dd/MM/yyyy").format((java.util.Date) d)
                : String.valueOf(d);
    }

    private static String testo(String modello, Map<String, Object> s) {

        return modello.replace("{sigla}", String.valueOf(s.get("SIGLA")))
                .replace("{documento}", String.valueOf(s.get("DESCRIZIONE")))
                .replace("{condominio}", String.valueOf(s.get("DENOMINAZIONE")))
                .replace("{data}", data(s.get("DATA_SCADENZA"))).trim();
    }

    private static String corpo(Map<String, Object> s, int soglia, String link) {

        int giorni = LetturaDataset.intero(s, "GIORNI_MANCANTI").intValue();
        StringBuilder b = new StringBuilder();
        b.append("Gentile amministratore,\n\n");
        b.append("per il condominio ").append(s.get("DENOMINAZIONE")).append(" (").append(s.get("INDIRIZZO")).append(", ")
                .append(s.get("LOCALITA")).append(")\n");
        b.append(soglia < 0 ? "e' SCADUTO" : "e' in scadenza").append(" l'adempimento:\n\n");
        b.append(s.get("SIGLA")).append(" - ").append(s.get("DESCRIZIONE")).append("\n");
        if (s.get("ENTE") != null) {
            b.append("Ente: ").append(s.get("ENTE")).append("\n");
        }
        b.append("Scadenza: ").append(data(s.get("DATA_SCADENZA")));
        b.append(giorni < 0 ? " (superata da " + (-giorni) + " giorni)" : " (tra " + giorni + " giorni)").append("\n\n");
        if (link != null && link.trim().length() > 0) {
            b.append("Documenti e scadenze della centrale termica: ").append(link.trim()).append("\n\n");
        }
        b.append("Messaggio automatico della Centrale termica WERA.");
        return b.toString();
    }

    private static void invia(String a, String cc, String oggetto, String corpo) throws AppCrash {

        String mittente = Config.GetInstance().getProperty("mail.from.service");
        SendSMTPMail mail = new SendSMTPMail();
        mail.setFrom(mittente);
        mail.setTo(a);
        mail.setCc(cc);
        mail.setBcc("");
        mail.setSubject(oggetto);
        mail.setBody(corpo);
        mail.setServer(Config.GetInstance().getProperty("mail.SMTPHost"));
        mail.setUsername(mittente);
        mail.prepareMail();
        mail.sendMail();
    }

    private void registra(Map<String, Object> s, Integer soglia, String destinatari, String oggetto, String esito,
            String nota, Integer idUtente) throws AppCrash {

        DBTransaction tx = new DBTransaction();
        try {
            Sql.esegui(tx, SQL_REGISTRA, LetturaDataset.intero(s, "ID_AZIENDA"), LetturaDataset.intero(s, "ID_CONDOMINIO"),
                    LetturaDataset.intero(s, "ID_SCADENZA"), soglia, Sql.testo(destinatari),
                    Sql.testo(oggetto.length() > 300 ? oggetto.substring(0, 300) : oggetto), esito, Sql.testo(nota),
                    Sql.intero(idUtente));
            tx.commit();
        } catch (Throwable t) {
            tx.rollBack();
            AppCrash ac = new AppCrash(t);
            ac.logContext(this.getClass().getName(), "Registro avvisi non scritto, scadenza " + s.get("ID_SCADENZA"));
        } finally {
            tx.end();
        }
    }
}
