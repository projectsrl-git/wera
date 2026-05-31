
package it.project.webapp.core;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;

/**
 * FunctionHome: Homepage dell'applicazione
 * 
 */
public class FunctionHome extends FunctionWebApp_base {

    private static final String PAGE = "index";

    public FunctionHome() {

        super();
    }

    public FunctionHome(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }
    

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        HashMap<String, Object> templateData = setTemplateDataFromRequest(setCommonTags(req, userInfo), req);

        _applicationSrv.displayPage(PAGE, templateData, setPageDatasetParam(PAGE, req, templateData), res);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        HashMap<String, Object> templateData = setTemplateDataFromRequest(setCommonTags(req, userInfo), req);

        _applicationSrv.displayPage(PAGE, templateData, setPageDatasetParam(PAGE, req, templateData), res);
    }

    @Override
    protected HashMap<String, String> prepareWhereCondition(HashMap<String, String> templateData,
            HashMap<String, String> queryParameter) {

        return templateData;
    }

    @Override
    protected void loadDataFromSession(HttpSession session, HashMap<String, Object> templateData) throws AppCrash {

        super.loadDataFromSession(session, templateData);
        templateData.put("DATA_ULTIMA_ELABORAZIONE", (session.getAttribute("DATA_ULTIMA_ELABORAZIONE")));
        templateData.put("ORA_ULTIMA_ELABORAZIONE", (session.getAttribute("ORA_ULTIMA_ELABORAZIONE")));
    }

}
