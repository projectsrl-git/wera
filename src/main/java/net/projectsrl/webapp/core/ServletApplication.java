
package net.projectsrl.webapp.core;

import java.util.HashMap;
import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.misc.Util;
import net.project.servlet.frame.Function_itf;
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

    public void displayMessageNextLogin(String message, SsbServletResponse resp) throws AppCrash {

        Map<String, String> pageRootData = new HashMap<String, String>();
        pageRootData.put(TAG_INFO_MESSAGE, message);
        pageRootData.put(TAG_NEXT_FUNCTION, FUNCTION_LOGIN);

        super.displayPage(PAGE_MESSAGE, pageRootData, resp);
    }

    @Override
    public void sessionExpired(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        Map<String, String> pageRootData = new HashMap<String, String>();

        createSessionToken(pageRootData, req);

        if (Config.GetInstance().getProperty("Page.SessioneScaduta.Template.Name") == null) return;

        displayPage("SessioneScaduta", pageRootData, res);
    }

    private void createSessionToken(Map<String, String> pageRootData, SsbServletRequest req) {

        String functionField = req.getField(Config.GetInstance().getProperty("Servlet.FunctionField"));

        if (Util.IsEmpty(functionField)) {
            return;
        }

        if (Config.GetInstance().getProperty("Servlet.Function.NoToken.List", "Login,Logout").contains(functionField)) {
            return;
        }

        String tokenString = WebAppUtils.createSessionToken(req);
        pageRootData.put(WebAppConstants_itf.SESSION_TOKEN, tokenString);
    }
    

    public void invokeProcessGet(String functionName, SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        String functionField = Config.GetInstance().getProperty("Servlet.FunctionField");
        req.setField(functionField, functionName);
        Function_itf function = getFunction(req);
        ErrDetector.GetInstance().preCond(function != null);
        function.doGet(req, res);

    }
    
    public void invokeProcessPost(String functionName, SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        String functionField = Config.GetInstance().getProperty("Servlet.FunctionField");
        req.setField(functionField, functionName);
        Function_itf function = getFunction(req);
        ErrDetector.GetInstance().preCond(function != null);
        function.doPost(req, res);

    }

    public Function_itf changeFunction(String functionId, SsbServletRequest req) throws AppCrash{
        
        String functionDefaultField = Config.GetInstance().getProperty("Servlet.FunctionField");
        req.setField(functionDefaultField, functionId);
        
        return getFunction(req);
        
    } 
}
