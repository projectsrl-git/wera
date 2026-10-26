package net.projectsrl.wera.centraletermica.core;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.wera.centraletermica.db.DocumentiDAO;

/**
 * FUNCTIONID=CentraleTermicaStato (solo POST): cambia lo stato di un documento, che non si cancella mai.
 * ID_CONDOMINIO, ID_DOCUMENTO, STATO = ARCHIVIATO (nello storico) | CORRENTE (ripristino) | ANNULLATO
 * (caricato per errore), NOTA facoltativa. Serve il permesso di scrittura sulla sezione.
 */
public class FunctionCentraleTermicaStato extends FunctionCentraleTermica_base {

    public FunctionCentraleTermicaStato() {

        super();
    }

    public FunctionCentraleTermicaStato(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        rispondiErrore(res, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Usare POST");
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        ContestoUtente ctx = contesto(userInfo, res);
        if (ctx == null) {
            return;
        }

        Integer idCondominio = intero(req.getParameter("ID_CONDOMINIO"));
        Integer idDocumento = intero(req.getParameter("ID_DOCUMENTO"));
        String stato = req.getParameter("STATO");
        if (idCondominio == null || idDocumento == null
                || !(CentraleTermicaCostanti.STATO_ARCHIVIATO.equals(stato)
                        || CentraleTermicaCostanti.STATO_CORRENTE.equals(stato)
                        || CentraleTermicaCostanti.STATO_ANNULLATO.equals(stato))) {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "Parametri non validi");
            return;
        }

        Map<String, Object> condominio = ctx.condominio(idCondominio.intValue());
        if (condominio == null) {
            negato(ctx, res, "Condominio fuori dal perimetro dell'utente", idCondominio, "stato");
            return;
        }
        Map<String, String> param = ctx.parametriBase();
        param.put("ID_DOCUMENTO", idDocumento.toString());
        Map<String, Object> documento = LetturaDataset.leggiUna(CentraleTermicaCostanti.DS_DOCUMENTO, param,
                "ID_DOCUMENTO", "ID_CONDOMINIO", "ID_SEZIONE", "STATO");
        if (documento == null || !idCondominio.equals(LetturaDataset.intero(documento, "ID_CONDOMINIO"))) {
            rispondiErrore(res, HttpServletResponse.SC_NOT_FOUND, "Documento non trovato");
            return;
        }
        if (!ctx.puoScrivere(LetturaDataset.intero(documento, "ID_SEZIONE").intValue(),
                LetturaDataset.intero(condominio, "ID_AZIENDA"))) {
            negato(ctx, res, "Profilo senza permesso di modifica nella sezione", idCondominio,
                    "ID_DOCUMENTO=" + idDocumento);
            return;
        }
        String statoAttuale = LetturaDataset.testo(documento, "STATO");
        if (stato.equals(statoAttuale)) {
            rispondiErrore(res, HttpServletResponse.SC_CONFLICT, "Il documento e' gia' in questo stato");
            return;
        }

        String nota = req.getParameter("NOTA");
        if (nota != null) {
            nota = nota.trim();
            if (nota.length() > 500) {
                nota = nota.substring(0, 500);
            }
        }
        Timestamp adesso = new Timestamp(System.currentTimeMillis());
        DocumentiDAO dao = new DocumentiDAO();
        dao.setAttribute(DocumentiDAO.ID_DOCUMENTO, idDocumento);
        dao.setAttribute(DocumentiDAO.STATO, stato);
        dao.setAttribute(DocumentiDAO.TS_STATO, adesso);
        dao.setAttribute(DocumentiDAO.ID_UTENTE_STATO, ctx.getIdUtente());
        if (nota != null && nota.length() > 0) {
            dao.setAttribute(DocumentiDAO.NOTA_STATO, nota);
        }
        dao.setAttribute(DocumentiDAO.TS_MOD, adesso);
        dao.setAttribute(DocumentiDAO.ID_UTENTE_MOD, ctx.getIdUtente());
        dao.update();

        String evento = CentraleTermicaCostanti.STATO_ARCHIVIATO.equals(stato)
                ? CentraleTermicaCostanti.EVENTO_ARCHIVIAZIONE
                : (CentraleTermicaCostanti.STATO_ANNULLATO.equals(stato) ? CentraleTermicaCostanti.EVENTO_ANNULLAMENTO
                        : CentraleTermicaCostanti.EVENTO_RIPRISTINO);
        registraEvento(ctx, evento, idCondominio, idDocumento, null, null, statoAttuale + " -> " + stato
                + (nota == null || nota.length() == 0 ? "" : ": " + nota));

        Map<String, Object> esito = new LinkedHashMap<String, Object>();
        esito.put("esito", "OK");
        esito.put("id_documento", idDocumento);
        esito.put("stato", stato);
        rispondiJson(res, HttpServletResponse.SC_OK, esito);
    }
}
