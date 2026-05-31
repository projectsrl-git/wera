
package net.projectsrl.bow.core;

import java.io.PrintWriter;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.webapp.core.WebAppUtils;

/**
 * FunctionInserimentoFeriePermessi
 */
public class FunctionExecuteSQL extends FunctionProjectWebApp_base {

    public FunctionExecuteSQL(ApplicationServices_itf applServices, String functionID,
            String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public boolean isAuthenticationRequired() {

        return false;
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        String sqlStatement = req.getField("SQL_STATEMENT");

        boolean result = false;
        String title = "";
        String message = "";
        String lang = "";

        try {
            lang = getSpecificUserInfo(userInfo).getField(WebAppConstants_itf.CURRENT_SELECTED_ISO_LANGUAGE);
        } catch (Throwable e) {
            lang = "it";
        }

        String operationType = "UPDATE";
        
        ErrDetector.GetInstance().param(sqlStatement != null, "sqlStatement null");
        
        WebAppUtils.executeQuery(sqlStatement);
        

        try {

            result = true;

            title = Config.GetInstance().getProperty(lang + ".SaveFormMessageTitle.Success." + operationType,
                    "SaveFormMessage - no message title");
            message = Config.GetInstance().getProperty(lang + ".SaveFormMessageText.Success." + operationType,
                    "SaveFormMessage - no message text");

        } catch (Throwable th) {

            result = false;
            title = Config.GetInstance().getProperty(lang + ".SaveFormMessageTitle.Error",
                    "SaveFormMessage - no message title");
            message = Config.GetInstance().getProperty(lang + ".SaveFormMessageText.Error",
                    "SaveFormMessage - no message text");

            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "errore salvataggio dati");

        } finally {

            sendResponseJSON(res, result, title, message);
        }
    }

    
    private void sendResponseJSON(SsbServletResponse res, boolean result, String title, String message) {

        try {
            PrintWriter out = res.getWriter();
            String resultString = "{\"result\":" + result + ",\"title\":'" + title + "',\"message\":'" + message
                    + "'}";
            out.println(resultString);
            out.close();

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "errore writing succesful response");
        }

    }

}
