
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
import net.projectsrl.wera.anagrafiche.db.TecniciDAO;

public class FunctionInserimentoAnagraficaTecnici extends FunctionInserimentoALIMOD_base<TecniciDAO> {

    public FunctionInserimentoAnagraficaTecnici(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(TecniciDAO.ID_MODULO, "");
            templateData.put(TecniciDAO.ID_UTENTE_INS, getSpecificUserInfo(userInfo).getIdUtente());

        } else {
            PjNDAO_base rowToUpdate = new TecniciDAO();
            String idRow = req.getField(TecniciDAO.ID_MODULO);
            rowToUpdate.setAttribute(TecniciDAO.ID_MODULO, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), TecniciDAO.ID_MODULO + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

}
