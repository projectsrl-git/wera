
package net.projectsrl.wera.anagrafiche.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.dafne.richieste.core.StatiRichiesta;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.qhse.moduli.core.FunctionInserimentoALIMOD_base;
import net.projectsrl.wera.anagrafiche.db.UtenzeDAO;


public class FunctionInserimentoAnagraficaUtenze extends FunctionInserimentoALIMOD_base<UtenzeDAO>  {

    public FunctionInserimentoAnagraficaUtenze(ApplicationServices_itf applServices, String functionID,
            String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);
        
        if (isAnInsert(req)) {
            templateData.put(UtenzeDAO.ID_MODULO, "");
            templateData.put(UtenzeDAO.DT_MODULO, project.misc.Utils.getStringDataOggi());
            templateData.put(UtenzeDAO.STATO, StatiRichiesta.DRAFT.getCode());
        } else {
            PjNDAO_base rowToUpdate = new UtenzeDAO();
            String idRow = req.getField(UtenzeDAO.ID_MODULO);
            rowToUpdate.setAttribute(UtenzeDAO.ID_MODULO, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), UtenzeDAO.ID_MODULO + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
        }        
            
        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

}
