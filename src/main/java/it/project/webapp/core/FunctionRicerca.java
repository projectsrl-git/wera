
package it.project.webapp.core;

import java.util.HashMap;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import freemarker.template.SimpleNumber;

/**
 * FunctionRicerca
 * 
 */
public class FunctionRicerca extends FunctionWebApp_base {

    private String _pageImposta   = "";
    private String _pageRisultati = "";

    public FunctionRicerca() {

        super();
    }

    public FunctionRicerca(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        HashMap templateData = setCommonTags(req, userInfo);
        templateData = setTemplateDataFromRequest(templateData, req);
        templateData.put("ESITORICERCA", new SimpleNumber(0));

        _applicationSrv.displayPage(_pageImposta, templateData, res);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        HashMap templateData = setCommonTags(req, userInfo);
        templateData = setTemplateDataFromRequest(templateData, req);
        templateData.put("ESITORICERCA", new SimpleNumber(1));

        String report_dettagliato = req.getField("REPORT_DETTAGLIATO").toUpperCase();
        // REPORT DETTAGLIATO
        if (report_dettagliato.trim().equals("ON")) {
            templateData.put("REPORT_DETTAGLIATO", "SI");
        } else {
            templateData.put("REPORT_DETTAGLIATO", "NO");
        }

        _applicationSrv.displayPage(_pageRisultati, templateData,
                setPageDatasetParam(_pageRisultati, req, templateData), res);

    }

    public void setPageImposta(String imposta) {

        _pageImposta = imposta;
    }

    public void setPageRisultati(String risultati) {

        _pageRisultati = risultati;
    }

}
