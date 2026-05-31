
package net.projectsrl.webapp.core;

import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.PjNDAO_base;

public abstract class FunctionAjaxForm_base<D extends PjNDAO_base> extends FunctionProjectWebApp_base {


    public FunctionAjaxForm_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public boolean isAuthenticationRequired() {

        return false;
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

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

        D formDao = null;

        try {

            formDao = commitChanges(req, operationType, userInfo);

            result = true;

            title = Config.GetInstance().getProperty(lang + ".SaveFormMessageTitle.Success." + operationType,
                    "SaveFormMessage - no message title");
            message = Config.GetInstance().getProperty(lang + ".SaveFormMessageText.Success." + operationType,
                    "SaveFormMessage - no message text");
            
            onSuccess(req,  res,  userInfo, formDao);

        } catch (Throwable th) {

            result = false;
            title = Config.GetInstance().getProperty(lang + ".SaveFormMessageTitle.Error",
                    "SaveFormMessage - no message title");
            message = Config.GetInstance().getProperty(lang + ".SaveFormMessageText.Error",
                    "SaveFormMessage - no message text");

            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "errore salvataggio dati");

        } finally {

            sendResponseJSON(res, result, title, message, formDao);
        }

    }

    protected void onSuccess(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo, D formDao ) throws AppCrash {

        // override for your scope
        
    }

    /**
     * Questo metodo
     * 
     * @param res
     * @param formDao
     */
    protected void sendResponseJSON(SsbServletResponse res, boolean result, String title, String message, D formDao) {

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
    private D commitChanges(SsbServletRequest req, String operationType, UserSecurityInfo userInfo) throws AppCrash {

        try {

            D formDao = (D) getTypeParameterClass().newInstance();

            if (WebAppConstants_itf.OPERATION_TYPE_INSERT.equals(operationType)) {
                insert(req, formDao, userInfo);

            } else if (WebAppConstants_itf.OPERATION_TYPE_UPDATE.equals(operationType)) {
                update(req, formDao, userInfo);

            } else if (WebAppConstants_itf.OPERATION_TYPE_DELETE.equals(operationType)) {
                delete(req, formDao, userInfo);
            }

            return formDao;

        } catch (Throwable th) {
            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "error committing changes - operationType:" + operationType);
            throw ac;
        }

    }

    protected void delete(SsbServletRequest req, D formDao, UserSecurityInfo userInfo) throws AppCrash {

        String keyField = req.getField("keyField");
        String keyValue = req.getField("keyValue");
        formDao.setAttribute(keyField, keyValue);
        formDao.delete();
    }

    protected void update(SsbServletRequest req, D formDao, UserSecurityInfo userInfo) throws AppCrash {

        formDao.setAttributesFromRequest(req);
        formDao.update();
    }

    protected void insert(SsbServletRequest req, D formDao, UserSecurityInfo userInfo) throws AppCrash {

        formDao.setAttributesFromRequest(req);
        formDao.insert();
    }

    @SuppressWarnings("unchecked")
    private Class<D> getTypeParameterClass() {

        Type type = getClass().getGenericSuperclass();
        ParameterizedType paramType = (ParameterizedType) type;
        return (Class<D>) paramType.getActualTypeArguments()[0];
    }

    protected abstract boolean isAnInsert(SsbServletRequest req);

    @Override
    protected Map<String, Object> createMapFromRequest(SsbServletRequest req, UserSecurityInfo userInfo)
            throws AppCrash {

        Map<String, Object> map = super.createMapFromRequest(req, userInfo);

        boolean insert = isAnInsert(req);

        if (insert) {
            map.put(WebAppConstants_itf.OPERATION_TYPE, WebAppConstants_itf.OPERATION_TYPE_INSERT);

        } else {
            map.put(WebAppConstants_itf.OPERATION_TYPE, WebAppConstants_itf.OPERATION_TYPE_UPDATE);

        }

        return map;
    }

}
