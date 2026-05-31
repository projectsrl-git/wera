
package net.projectsrl.webapp.core;

import java.io.PrintWriter;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.PjNDAO_base;

public class FunctionAjaxCheckUnique extends FunctionProjectWebApp_base {

    private static final String DAO_CLASS    = "DAO_CLASS";
    private static final String SEARCH_KEY   = "SEARCH_KEY";
    private static final String SEARCH_VALUE = "SEARCH_VALUE";
    private String              _daoClassName;

    public FunctionAjaxCheckUnique(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        boolean result = false;
        String message = req.getField(TAG_ERROR_MESSAGE);

        _daoClassName = req.getParameter(DAO_CLASS);

        String lang = WebAppConstants_itf.DEFAULT_ISO_LANGUAGE;

        try {
            lang = getSpecificUserInfo(userInfo).getField(WebAppConstants_itf.CURRENT_SELECTED_ISO_LANGUAGE);
        } catch (Throwable e) {
            lang = "it";
        }

        try {

            ErrDetector.GetInstance().param(Util.IsNotEmpty(message), "TAG_ERROR_MESSAGE empty");
            ErrDetector.GetInstance().param(Util.IsNotEmpty(_daoClassName), "DAO_CLASS empty");

            PjNDAO_base formDao = makeDAO(req);

            result = !exists(formDao);

        } catch (Throwable th) {

            result = false;

            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "error check unique ");
            message = Config.GetInstance().getProperty(lang + ".SaveFormMessageText.Error",
                    "SaveFormMessage - no message text");

        } finally {

            sendResponseJSON(res, result, message);
        }

    }

    /**
     * Questo metodo
     * 
     * @param res
     * @param formDao
     */
    protected void sendResponseJSON(SsbServletResponse res, boolean result, String message) {

        try {
            PrintWriter out = res.getWriter();
            String resultString = "{\"result\":" + result + ",\"message\":'" + message + "'}";
            out.println(resultString);
            out.close();

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "errore writing succesful response");
        }
    }

    /**
     * verifica esistenza della chiave
     */
    protected PjNDAO_base makeDAO(SsbServletRequest req) throws AppCrash {

        String searchKey = null;
        String searchValue = null;

        try {

            Class<?> daoClass = Class.forName(_daoClassName);
            PjNDAO_base formDao = null;

            formDao = (PjNDAO_base) daoClass.newInstance();

            searchKey = req.getField(SEARCH_KEY);
            ErrDetector.GetInstance().param(Util.IsNotEmpty(searchKey), "SEARCH_KEY empty");

            searchValue = req.getField(SEARCH_VALUE);
            ErrDetector.GetInstance().param(Util.IsNotEmpty(searchValue), "SEARCH_VALUE empty");

            formDao.setAttribute(searchKey, searchValue);

            return formDao;

        } catch (Throwable th) {
            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "error in instance daoClassName:" + _daoClassName
                    + " - SEARCH_KEY:" + searchKey + " - SEARCH_VALUE:" + searchValue);
            throw ac;
        }

    }

    /**
     * verifica esistenza della chiave
     * 
     * @param formDao
     */
    protected boolean exists(PjNDAO_base formDao) throws AppCrash {

        try {

            return formDao.retrieve();

        } catch (Throwable th) {
            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "error checking key exists " + formDao);
            throw ac;
        }

    }

}
