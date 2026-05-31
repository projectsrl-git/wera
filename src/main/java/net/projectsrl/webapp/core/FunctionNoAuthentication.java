
package net.projectsrl.webapp.core;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.security.UserSecurityInfo;

public class FunctionNoAuthentication extends FunctionProjectWebApp_base {

    public FunctionNoAuthentication(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }
    
    @Override
    protected boolean checkSession(SsbServletRequest req) {

        return true;
    }

    @Override
    protected Map<String, Object> createMapFromRequest(SsbServletRequest req, UserSecurityInfo userInfo)
            throws AppCrash {

        Map<String, Object> map = new HashMap<String, Object>();
        
        setMapFromRequest(map, req);

        setSessionProperties(req);

        return map;
    }

    private void setSessionProperties(SsbServletRequest req) throws AppCrash {

        HttpSession session = req.getSession(true);

        String nomeDbHostName = readDBHostName();
        String dbUrl = Config.GetInstance().getProperty("DB.ConnectionURL");
        dbUrl = dbUrl.replaceAll("#DB_HOST_NAME#", nomeDbHostName);
        Config.GetInstance().setProperty("DB.ConnectionURL", dbUrl);

        session.setAttribute("PAGE_TITLE", Config.GetInstance().getProperty("Servlet.ApplicationTitle", ""));
    }

}
