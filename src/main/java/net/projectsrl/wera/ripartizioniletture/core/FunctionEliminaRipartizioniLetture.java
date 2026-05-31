
package net.projectsrl.wera.ripartizioniletture.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.qhse.moduli.core.FunctionInserimentoALIMOD_base;
import net.projectsrl.wera.ripartizioniletture.db.RipartizioniLettureDAO;

public class FunctionEliminaRipartizioniLetture extends FunctionInserimentoALIMOD_base<RipartizioniLettureDAO> {
	

    public FunctionEliminaRipartizioniLetture(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(RipartizioniLettureDAO.ID_MODULO, "");
            templateData.put(RipartizioniLettureDAO.DT_MODULO, project.misc.Utils.getStringDataOggi());
            templateData.put(RipartizioniLettureDAO.ID_UTENTE_INS, getSpecificUserInfo(userInfo).getIdUtente());
         
           
        } else {
            PjNDAO_base rowToUpdate = new RipartizioniLettureDAO();
            String idRow = req.getField(RipartizioniLettureDAO.ID_MODULO);
            rowToUpdate.setAttribute(RipartizioniLettureDAO.ID_MODULO, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), RipartizioniLettureDAO.ID_MODULO + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

    
}
