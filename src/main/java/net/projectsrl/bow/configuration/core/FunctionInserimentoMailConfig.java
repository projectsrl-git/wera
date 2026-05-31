
package net.projectsrl.bow.configuration.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.bow.configuration.db.MailConfigDAO;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.webapp.core.FunctionAjaxForm_base;

public class FunctionInserimentoMailConfig extends FunctionAjaxForm_base<MailConfigDAO> {

    public FunctionInserimentoMailConfig(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        // indispensabile
        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            
        } else {
            PjNDAO_base rowToUpdate = new MailConfigDAO();
            String idRow = req.getField(MailConfigDAO.ID_MAIL_CONFIG);
            rowToUpdate.setAttribute(MailConfigDAO.ID_MAIL_CONFIG, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), MailConfigDAO.ID_MAIL_CONFIG + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
        }

        // indispensabile
        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

    @Override
    protected boolean isAnInsert(SsbServletRequest req) {

        return Util.IsEmpty(req.getField(MailConfigDAO.ID_MAIL_CONFIG));
    }

}
