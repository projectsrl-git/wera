package net.projectsrl.wera.centraletermica.core;

import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.wera.centraletermica.db.EventiDAO;

/**
 * Base delle funzioni della Centrale termica (wera2026). Non visualizza pagine FreeMarker: risponde
 * JSON o file. Ogni richiesta passa da contesto(): profilo abilitato (parametro ACCESSO.PROFILI) e
 * perimetro dei condomini sono verificati qui, lato server.
 */
public abstract class FunctionCentraleTermica_base extends FunctionProjectWebApp_base {

    private static final ObjectMapper JSON                        = new ObjectMapper();

    static final String               CAMPO_SESSIONE_ACCESSO      = "CT_ACCESSO";
    static final String               CAMPO_SESSIONE_TITOLO       = "CT_TITOLO_MENU";
    static final String               CAMPO_SESSIONE_NASCONDI_WERA = "CT_NASCONDI_VOCE_WERA";

    public FunctionCentraleTermica_base() {

        super();
    }

    public FunctionCentraleTermica_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    /** Contesto dell'utente, o null (risposta 403 gia' inviata) se il profilo non e' abilitato. */
    protected ContestoUtente contesto(UserSecurityInfo userInfo, SsbServletResponse res) throws AppCrash {

        ContestoUtente ctx = new ContestoUtente(getSpecificUserInfo(userInfo));
        if (!ctx.isAbilitato()) {
            negato(ctx, res, "Profilo non abilitato alla Centrale termica", null, null);
            return null;
        }
        return ctx;
    }

    /**
     * Salva in sessione cio' che serve al menu di wera2026 (included_menu.include), che legge i campi di
     * sessione su ogni pagina senza interrogare il DB: CT_ACCESSO (S/N), CT_TITOLO_MENU e
     * CT_NASCONDI_VOCE_WERA (S se il profilo e' abilitato alla Centrale termica nuova e non e' in
     * MENU.VOCE_WERA.PROFILI).
     */
    protected void memorizzaAccesso(SsbServletRequest req, ContestoUtente ctx, boolean abilitato) throws AppCrash {

        setSessionField(req, CAMPO_SESSIONE_ACCESSO, abilitato ? "S" : "N");
        String titolo = ctx.parametro(CentraleTermicaCostanti.PAR_ETICHETTA_TITOLO, ctx.getIdAzienda());
        setSessionField(req, CAMPO_SESSIONE_TITOLO, titolo.length() > 0 ? titolo : "Centrale termica");
        boolean voceWera = ctx.getProfilo() != null && ContestoUtente.elenco(ctx.parametro(
                CentraleTermicaCostanti.PAR_MENU_VOCE_WERA_PROFILI, ctx.getIdAzienda())).contains(ctx.getProfilo());
        // la vecchia voce si nasconde solo a chi ha la nuova (per gli altri, es. CON, resta com'e')
        setSessionField(req, CAMPO_SESSIONE_NASCONDI_WERA, abilitato && !voceWera ? "S" : "N");
    }

    /** Intero positivo da un parametro della richiesta, null se assente o non valido. */
    protected static Integer intero(String valore) {

        if (valore == null) {
            return null;
        }
        String v = valore.trim();
        if (!v.matches("[0-9]{1,9}")) {
            return null;
        }
        int n = Integer.parseInt(v);
        return n > 0 ? Integer.valueOf(n) : null;
    }

    protected void negato(ContestoUtente ctx, SsbServletResponse res, String motivo, Integer idCondominio,
            String dettaglio) throws AppCrash {

        registraEvento(ctx, CentraleTermicaCostanti.EVENTO_ACCESSO_NEGATO, idCondominio, null, null, null,
                motivo + (dettaglio == null ? "" : " - " + dettaglio));
        rispondiErrore(res, HttpServletResponse.SC_FORBIDDEN, motivo);
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

    /** Date come testo (AAAA-MM-GG, AAAA-MM-GGThh:mm:ss) nel fuso del server, invece dei millisecondi. */
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
        if (valore instanceof java.sql.Date) {
            return valore.toString();
        }
        if (valore instanceof String) {
            return ((String) valore).trim();
        }
        return valore;
    }

    /**
     * Scrive una riga nel registro eventi, fuori dalla transazione del chiamante (cosi' anche un
     * accesso negato o un caricamento fallito restano registrati). Un errore qui non blocca l'operazione.
     */
    protected void registraEvento(ContestoUtente ctx, String tipo, Integer idCondominio, Integer idDocumento,
            Integer idVersione, Integer idFileWera, String dettaglio) {

        try {
            EventiDAO evento = new EventiDAO();
            evento.setAttribute(EventiDAO.TIPO_EVENTO, tipo);
            if (ctx != null && ctx.getIdUtente() != null) {
                evento.setAttribute(EventiDAO.ID_UTENTE, ctx.getIdUtente());
            }
            if (ctx != null && ctx.getProfilo() != null) {
                evento.setAttribute(EventiDAO.CODICE_PROFILO, ctx.getProfilo());
            }
            if (ctx != null && ctx.getIdAzienda() != null) {
                evento.setAttribute(EventiDAO.ID_AZIENDA, ctx.getIdAzienda());
            }
            if (idCondominio != null) {
                evento.setAttribute(EventiDAO.ID_CONDOMINIO, idCondominio);
            }
            if (idDocumento != null) {
                evento.setAttribute(EventiDAO.ID_DOCUMENTO, idDocumento);
            }
            if (idVersione != null) {
                evento.setAttribute(EventiDAO.ID_VERSIONE, idVersione);
            }
            if (idFileWera != null) {
                evento.setAttribute(EventiDAO.ID_FILE_WERA, idFileWera);
            }
            if (dettaglio != null) {
                evento.setAttribute(EventiDAO.DETTAGLIO, dettaglio.length() > 4000 ? dettaglio.substring(0, 4000)
                        : dettaglio);
            }
            evento.insert();
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext(this.getClass().getName(), "Registro eventi non scritto: " + tipo);
        }
    }
}
