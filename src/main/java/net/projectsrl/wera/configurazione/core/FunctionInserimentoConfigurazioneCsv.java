
package net.projectsrl.wera.configurazione.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.qhse.moduli.core.FunctionInserimentoALIMOD_base;
import net.projectsrl.wera.configurazione.db.ConfigurazioneCsvDAO;

public class FunctionInserimentoConfigurazioneCsv extends FunctionInserimentoALIMOD_base<ConfigurazioneCsvDAO> {

    public FunctionInserimentoConfigurazioneCsv(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(ConfigurazioneCsvDAO.ID_MODULO, "");
            templateData.put(ConfigurazioneCsvDAO.ID_UTENTE_INS, getSpecificUserInfo(userInfo).getIdUtente());

        } else {
            PjNDAO_base rowToUpdate = new ConfigurazioneCsvDAO();
            String idRow = req.getField(ConfigurazioneCsvDAO.ID_MODULO);
            rowToUpdate.setAttribute(ConfigurazioneCsvDAO.ID_MODULO, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), ConfigurazioneCsvDAO.ID_MODULO + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

}
