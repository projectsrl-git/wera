
package net.projectsrl.webapp.authentication;

import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;

/**
 * FunctionLogin
 * 
 * Login standard per applicazione web
 * 
 */
public abstract class FunctionLogin_base extends FunctionProjectWebApp_base {

    public FunctionLogin_base() {

        super();
    }

    public FunctionLogin_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public boolean isAuthenticationRequired() {

        return false;
    }

    @Override
    protected boolean checkSession(SsbServletRequest req) {

        return true;
    }
    
}
