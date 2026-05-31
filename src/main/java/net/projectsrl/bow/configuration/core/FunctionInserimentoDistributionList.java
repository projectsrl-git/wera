
package net.projectsrl.bow.configuration.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.bow.configuration.db.DistributionListDAO;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.webapp.core.FunctionAjaxForm_base;

public class FunctionInserimentoDistributionList extends FunctionAjaxForm_base<DistributionListDAO> {

    public FunctionInserimentoDistributionList(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        // indispensabile
        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            
        } else {
            PjNDAO_base rowToUpdate = new DistributionListDAO();
            String idRow = req.getField(DistributionListDAO.ID_DISTRIBUTION_LIST);
            rowToUpdate.setAttribute(DistributionListDAO.ID_DISTRIBUTION_LIST, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), DistributionListDAO.ID_DISTRIBUTION_LIST + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
        }

        // indispensabile
        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

    @Override
    protected boolean isAnInsert(SsbServletRequest req) {

        return Util.IsEmpty(req.getField(DistributionListDAO.ID_DISTRIBUTION_LIST));
    }

}
