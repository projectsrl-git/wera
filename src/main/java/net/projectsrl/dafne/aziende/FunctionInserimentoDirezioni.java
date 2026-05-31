
package net.projectsrl.dafne.aziende;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.dafne.core.DafneCostanti_itf;
import net.projectsrl.dafne.db.DirezioniDAO;
import net.projectsrl.dafne.db.StaffDirezioniDAO;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.webapp.core.FunctionAjaxForm_base;

/**
 * FunctionInserimentoDirezioni
 */
public class FunctionInserimentoDirezioni extends FunctionAjaxForm_base<DirezioniDAO> {

    public FunctionInserimentoDirezioni(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(DirezioniDAO.ID_DIREZIONE, "");
        } else {
            PjNDAO_base rowToUpdate = new DirezioniDAO();
            String idRow = req.getField(DirezioniDAO.ID_DIREZIONE);
            rowToUpdate.setAttribute(DirezioniDAO.ID_DIREZIONE, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), DirezioniDAO.ID_DIREZIONE + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
            
            templateData.put(DafneCostanti_itf.RISORSE_MULTIPLE, new StaffDirezioniDAO().getSelectedCodeList(idRow));
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }


    @Override
    protected boolean isAnInsert(SsbServletRequest req) {

        return Util.IsEmpty(req.getField(DirezioniDAO.ID_DIREZIONE));
    }

}
