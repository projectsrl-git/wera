
package net.projectsrl.webapp.authentication;

import javax.servlet.http.HttpSession;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.Function_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.AuthenticationProvider_itf;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.webapp.core.ServletApplication;

/**
 * FunctionNewPassword_base
 * 
 */
public abstract class FunctionNewPassword_base extends FunctionProjectWebApp_base {



    public FunctionNewPassword_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public boolean isAuthenticationRequired() {

        return false;
    }


    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        LoginData loginData = new LoginData(req);

        String encryptedPassword = encryptPassword(loginData.getPassword());

        storeNewPassword(loginData, encryptedPassword);

        authenticate(req, userInfo);

        Function_itf function = ((ServletApplication)_applicationSrv).changeFunction(getHomeFunction(), req);
        
        function.doPost(req, res);

    }

    protected void authenticate(SsbServletRequest req, UserSecurityInfo userInfo) throws AppCrash {

        AuthenticationProvider_itf aut = _applicationSrv.getAuthenticationProvider(_functionName);
        aut.authenticate(userInfo, req);
        HttpSession session = req.getSession(true);
        session.setAttribute("LOGIN", userInfo);
    }
    
    @Override
    protected boolean checkSession(SsbServletRequest req) {

        return true;
    }

    protected abstract void storeNewPassword(LoginData loginData, String encryptedPassword) throws AppCrash;

    protected abstract String getHomeFunction();

    protected abstract String encryptPassword(String password) throws AppCrash;

}
