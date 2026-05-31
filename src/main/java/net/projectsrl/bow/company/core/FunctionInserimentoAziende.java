
package net.projectsrl.bow.company.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.bow.company.db.AziendeDAO;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.webapp.core.FunctionAjaxForm_base;


public class FunctionInserimentoAziende extends FunctionAjaxForm_base<AziendeDAO> {

    public FunctionInserimentoAziende(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(AziendeDAO.ID_AZIENDA, "");
        } else {
            PjNDAO_base aziende = new AziendeDAO();
            String idAzienda = req.getField(AziendeDAO.ID_AZIENDA);
            aziende.setAttribute(AziendeDAO.ID_AZIENDA, idAzienda);
            ErrDetector.GetInstance().preCond(aziende.retrieve(), AziendeDAO.ID_AZIENDA + " not found");
            aziende.setMapFromAttributes(templateData);
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }


    @Override
    protected boolean isAnInsert(SsbServletRequest req) {

        return Util.IsEmpty(req.getField(AziendeDAO.ID_AZIENDA));
    }

}
