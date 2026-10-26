package net.projectsrl.wera.registrazioni;

import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;

/**
 * Base delle funzioni delle registrazioni (wera2026): risposte JSON, lettura dei campi, IP del richiedente.
 */
public abstract class FunctionRegistrazioni_base extends FunctionProjectWebApp_base {

    private static final ObjectMapper JSON = new ObjectMapper();

    public FunctionRegistrazioni_base() {

        super();
    }

    public FunctionRegistrazioni_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    /** Testo ripulito: null se assente o vuoto, caratteri di controllo tolti (a capo ammessi), troncato. */
    static String pulito(String v, int max) {

        if (v == null) {
            return null;
        }
        String t = v.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "").trim();
        if (t.length() == 0) {
            return null;
        }
        return t.length() > max ? t.substring(0, max) : t;
    }

    /**
     * IP del richiedente. Dietro l'Apache di frontend l'IP vero e' l'ultimo di X-Forwarded-For, ma lo si
     * accetta solo se la connessione arriva da un proxy di ANTIABUSO.PROXY_FIDATI (altrimenti chiunque
     * potrebbe scegliersi l'IP e aggirare il limite).
     */
    static String ip(SsbServletRequest req, Parametri p) {

        String remoto = req.getRemoteAddr();
        String inoltrato = req.getHeader("X-Forwarded-For");
        if (inoltrato != null && remoto != null
                && p.elenco(RegistrazioniCostanti.PAR_ANTIABUSO_PROXY).contains(remoto)) {
            String[] voci = inoltrato.split(",");
            String ultimo = voci[voci.length - 1].trim();
            if (ultimo.matches("[0-9A-Fa-f:.]{3,45}")) {
                return ultimo;
            }
        }
        return remoto;
    }

    protected void rispondiErrore(SsbServletResponse res, int status, String messaggio) throws AppCrash {

        Map<String, Object> esito = new LinkedHashMap<String, Object>();
        esito.put("esito", "KO");
        esito.put("messaggio", messaggio);
        rispondiJson(res, status, esito);
    }

    protected void rispondiJson(SsbServletResponse res, int status, Object dati) throws AppCrash {

        try {
            byte[] corpo = JSON.writeValueAsBytes(normalizza(dati));
            res.setStatus(status);
            res.setContentType("application/json; charset=UTF-8");
            res.setHeader("Cache-Control", "no-store");
            res.setContentLength(corpo.length);
            OutputStream out = res.getOutputStream();
            out.write(corpo);
            out.flush();
            out.close();
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext(this.getClass().getName(), "Errore nella scrittura della risposta JSON");
            throw ac;
        }
    }

    /** Timestamp come testo AAAA-MM-GGThh:mm:ss, stringhe ripulite, mappe e liste in profondita'. */
    @SuppressWarnings("unchecked")
    private static Object normalizza(Object valore) {

        if (valore instanceof Map) {
            Map<String, Object> copia = new LinkedHashMap<String, Object>();
            for (Map.Entry<String, Object> e : ((Map<String, Object>) valore).entrySet()) {
                copia.put(e.getKey(), normalizza(e.getValue()));
            }
            return copia;
        }
        if (valore instanceof List) {
            List<Object> lista = new java.util.ArrayList<Object>();
            for (Object o : (List<Object>) valore) {
                lista.add(normalizza(o));
            }
            return lista;
        }
        if (valore instanceof Timestamp) {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format((Timestamp) valore);
        }
        if (valore instanceof String) {
            return ((String) valore).trim();
        }
        return valore;
    }
}
