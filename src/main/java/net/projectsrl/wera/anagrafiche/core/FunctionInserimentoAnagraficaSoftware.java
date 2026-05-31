
package net.projectsrl.wera.anagrafiche.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.qhse.moduli.core.FunctionInserimentoALIMOD_base;
import net.projectsrl.wera.anagrafiche.db.SoftwareDAO;

public class FunctionInserimentoAnagraficaSoftware extends FunctionInserimentoALIMOD_base<SoftwareDAO> {

    public FunctionInserimentoAnagraficaSoftware(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(SoftwareDAO.ID_MODULO, "");
            templateData.put(SoftwareDAO.ID_UTENTE_INS, getSpecificUserInfo(userInfo).getIdUtente());

        } else {
            PjNDAO_base rowToUpdate = new SoftwareDAO();
            String idRow = req.getField(SoftwareDAO.ID_MODULO);
            rowToUpdate.setAttribute(SoftwareDAO.ID_MODULO, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), SoftwareDAO.ID_MODULO + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

}
