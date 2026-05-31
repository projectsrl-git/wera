
package net.projectsrl.webapp.core;

import java.io.PrintWriter;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.PjNDAO_base;

public class FunctionSaveForm extends FunctionProjectWebApp_base {

    private static final String INSERT    = "INSERT";
    private static final String UPDATE    = "UPDATE";
    private static final String DELETE    = "DELETE";

    private static final String DAO_CLASS = "daoClass";

    public FunctionSaveForm(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public boolean isAuthenticationRequired() {

        return false;
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        String daoClassName = req.getParameter(DAO_CLASS);

        boolean result = false;
        String title = "";
        String message = "";
        String lang = "";

        try {
            lang = getSpecificUserInfo(userInfo).getField(WebAppConstants_itf.CURRENT_SELECTED_ISO_LANGUAGE);
        } catch (Throwable e) {
            lang = "it";
        }

        String operationType = req.getField(WebAppConstants_itf.OPERATION_TYPE);

        PjNDAO_base formDao = null;

        try {

            ErrDetector.GetInstance().param(daoClassName != null, "daoClassName null");

            formDao = commitChanges(req, daoClassName, operationType, userInfo);

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
            ac.logContext(this.getClass().getName(), "errore salvataggio dati grid  - daoClassName:" + daoClassName);

        } finally {

            sendResponseJSON(res, result, title, message, formDao);
        }

    }

    /**
     * Questo metodo
     * 
     * @param res
     * @param formDao
     */
    protected void sendResponseJSON(SsbServletResponse res, boolean result, String title, String message,
            PjNDAO_base formDao) {

        try {
            PrintWriter out = res.getWriter();
            String resultString = "{\"result\":" + result + ",\"title\":'" + title + "',\"message\":'" + message + "'}";
            out.println(resultString);
            out.close();

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "errore writing succesful response");
        }
    }

    /*
     * salva le modifiche su db
     */
    private PjNDAO_base commitChanges(SsbServletRequest req, String daoClassName, String operationType,
            UserSecurityInfo userInfo) throws AppCrash {

        try {

            Class<?> daoClass = Class.forName(daoClassName);
            PjNDAO_base formDao = null;

            formDao = (PjNDAO_base) daoClass.newInstance();

            if (INSERT.equals(operationType)) {
                insert(req, formDao, userInfo);

            } else if (UPDATE.equals(operationType)) {
                update(req, formDao, userInfo);

            } else if (DELETE.equals(operationType)) {
                delete(req, formDao, userInfo);
            }

            return formDao;

        } catch (Throwable th) {
            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "errore nell'aggiornamento di datagrid - daoClassName:"
                    + daoClassName + " - operationType:" + operationType);
            throw ac;
        }

    }

    protected void delete(SsbServletRequest req, PjNDAO_base formDao, UserSecurityInfo userInfo) throws AppCrash {

        String keyField = req.getField("keyField");
        String id = req.getField("id");
        formDao.setAttribute(keyField, id);
        formDao.delete();
    }

    protected void update(SsbServletRequest req, PjNDAO_base formDao, UserSecurityInfo userInfo) throws AppCrash {

        formDao.setAttributesFromRequest(req);
        formDao.update();
    }

    protected void insert(SsbServletRequest req, PjNDAO_base formDao, UserSecurityInfo userInfo) throws AppCrash {

        formDao.setAttributesFromRequest(req);
        formDao.insert();
    }
}
