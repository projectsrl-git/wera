
package net.projectsrl.wm.menu;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.security.UserSecurityInfo;

public class FunctionHome extends it.project.webapp.core.FunctionHome {

    public FunctionHome() {

        super();
    }

    public FunctionHome(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }
    

    @Override
    protected HashMap<String, Object> setCommonTags(SsbServletRequest req, UserSecurityInfo userInfo) throws AppCrash {

        HashMap<String, Object> templateData = super.setCommonTags(req, userInfo);

        templateData.put("INCLUDED_FRAME_METRO_MENU", Config.GetInstance().getProperty("Page.DefaultMenuNameMetroMenu"));
    	templateData.put("RUOLO_SESSIONE", getSessionRole(req));
    	
        return templateData;
    }

    @Override
    protected void loadDataFromSession(HttpSession session, HashMap<String, Object> templateData) throws AppCrash {

        super.loadDataFromSession(session, templateData);
        templateData.put("ID_DIPENDENTE_SESSIONE", (String) session.getAttribute("ID_DIPENDENTE_SESSIONE"));
        templateData.put("AZIENDA_SESSIONE", (String) session.getAttribute("AZIENDA_SESSIONE"));
        templateData.put("DATA_ULTIMA_ELABORAZIONE", ((String) session.getAttribute("DATA_ULTIMA_ELABORAZIONE")));
        templateData.put("ORA_ULTIMA_ELABORAZIONE", ((String) session.getAttribute("ORA_ULTIMA_ELABORAZIONE")));
    }

}
