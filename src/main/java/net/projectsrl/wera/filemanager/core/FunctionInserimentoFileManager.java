
package net.projectsrl.wera.filemanager.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.qhse.moduli.core.FunctionInserimentoALIMOD_base;
import net.projectsrl.wera.filemanager.db.ElencoFileImportatiFileManagerDAO;

public class FunctionInserimentoFileManager extends FunctionInserimentoALIMOD_base<ElencoFileImportatiFileManagerDAO> {

    public FunctionInserimentoFileManager(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(ElencoFileImportatiFileManagerDAO.ID_MODULO, "");

            templateData.put(ElencoFileImportatiFileManagerDAO.ID_UTENTE_INS, getSpecificUserInfo(userInfo).getIdUtente());

        } else {
            PjNDAO_base rowToUpdate = new ElencoFileImportatiFileManagerDAO();
            String idRow = req.getField(ElencoFileImportatiFileManagerDAO.ID_MODULO);
            rowToUpdate.setAttribute(ElencoFileImportatiFileManagerDAO.ID_MODULO, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), ElencoFileImportatiFileManagerDAO.ID_MODULO + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

}
