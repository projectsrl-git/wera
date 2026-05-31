
package net.projectsrl.webapp.core;

import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;

public class FunctionHome_base extends FunctionProjectWebApp_base {

    public FunctionHome_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    // alla pagina home arrivo dopo login, quindi deve essere accessibile senza controllo sessione
    protected boolean checkSession(SsbServletRequest req) {

        return true;
    }



}
