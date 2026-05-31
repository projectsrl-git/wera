
package net.projectsrl.wera.importazioni.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.qhse.moduli.core.FunctionInserimentoALIMOD_base;
import net.projectsrl.wera.anagrafiche.db.AntenneDAO;
import net.projectsrl.wera.importazioni.db.ElencoFileImportatiRilevatoriDAO;

public class FunctionInserimentoArchivioFileImportati extends FunctionInserimentoALIMOD_base<ElencoFileImportatiRilevatoriDAO> {

    public FunctionInserimentoArchivioFileImportati(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(ElencoFileImportatiRilevatoriDAO.ID_MODULO, "");

            templateData.put(ElencoFileImportatiRilevatoriDAO.ID_UTENTE_INS, getSpecificUserInfo(userInfo).getIdUtente());

        } else {
            PjNDAO_base rowToUpdate = new AntenneDAO();
            String idRow = req.getField(ElencoFileImportatiRilevatoriDAO.ID_MODULO);
            rowToUpdate.setAttribute(ElencoFileImportatiRilevatoriDAO.ID_MODULO, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), ElencoFileImportatiRilevatoriDAO.ID_MODULO + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

}
