
package net.projectsrl.bow.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;

public class FunctionHome extends FunctionProjectWebApp_base {

    public FunctionHome(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        mostra(req, res, userInfo);
    }

    @Override
    // alla pagina home arrivo dopo login, quindi deve essere accessibile senza controllo sessione
    protected boolean checkSession(SsbServletRequest req) {

        return true;
    }

    @Override
    protected String getMenuID() {

        return "Home";
    }        
    
}
