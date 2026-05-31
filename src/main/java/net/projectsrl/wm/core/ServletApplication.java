
package net.projectsrl.wm.core;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.servlet.frame.ServletApplication_base;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.AuthenticationProvider;
import net.project.servlet.security.AuthenticationProvider_itf;

public abstract class ServletApplication extends ServletApplication_base {

    private static final long   serialVersionUID  = 7021418236995095328L;

    private static final String PAGE_MESSAGE      = "Message";
    private static final String FUNCTION_LOGIN    = "Login";
    private static final String TAG_INFO_MESSAGE  = "INFO_MESSAGE";
    private static final String TAG_NEXT_FUNCTION = "NEXT_FUCNTION";

    @Override
    public void processGet(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        // TODO Auto-generated method stub

    }

    @Override
    public AuthenticationProvider_itf getAuthenticationProvider(String functionName) throws AppCrash {

        String className = Config.GetInstance().getProperty(
                new String("Servlet." + functionName + ".authentication.class"));
        if (className == null) {
            // Se la proprieta' servlet.authentication.class non e' trovata
            // viene passato al metodo MakeAuthenticationProvider la function "default"
            // per abbinara la property Servlet.default.authentication.class
            functionName = "default";
        }

        return AuthenticationProvider.MakeAuthenticationProvider("", functionName);

    }

    @Override
    public void sessionExpired(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("LOGIN") == null) {
            try {
                res.sendRedirect("astro?FUNCTIONID=LoginInterno");
                return;
            } catch (IOException e) {
                throw new AppCrash(e);
            }
        }

    }

   

    
    
}
