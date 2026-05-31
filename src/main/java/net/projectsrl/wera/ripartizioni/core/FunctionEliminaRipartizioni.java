
package net.projectsrl.wera.ripartizioni.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.qhse.moduli.core.FunctionInserimentoALIMOD_base;
import net.projectsrl.wera.ripartizioni.db.RipartizioniDAO;

public class FunctionEliminaRipartizioni extends FunctionInserimentoALIMOD_base<RipartizioniDAO> {
	

    public FunctionEliminaRipartizioni(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
        	templateData.put(RipartizioniDAO.ID_MODULO, "");
            templateData.put(RipartizioniDAO.DT_MODULO, project.misc.Utils.getStringDataOggi());
            templateData.put(RipartizioniDAO.ID_UTENTE_INS, getSpecificUserInfo(userInfo).getIdUtente());
            
            
            templateData.put(RipartizioniDAO.CC_AC_QTA, 1);
            templateData.put(RipartizioniDAO.CC_FM_QTA, 1);
            templateData.put(RipartizioniDAO.CC_SI_QTA, 1);
            templateData.put(RipartizioniDAO.CMI_CIS_QTA, 1);
            templateData.put(RipartizioniDAO.CMI_CCT_QTA, 1);
            
            templateData.put(RipartizioniDAO.TC_CC_RIP, 100);
            templateData.put(RipartizioniDAO.TC_AC_RIP, 100);
            templateData.put(RipartizioniDAO.TC_CCT_RIP, 100);
            templateData.put(RipartizioniDAO.TC_CFM_RIP, 100);
            templateData.put(RipartizioniDAO.TC_CL_RIP, 100);
            
            templateData.put(RipartizioniDAO.CR_PF_RIP, 70);
            templateData.put(RipartizioniDAO.CR_PR_RIP, 30);
           
        } else {
            PjNDAO_base rowToUpdate = new RipartizioniDAO();
            String idRow = req.getField(RipartizioniDAO.ID_MODULO);
            rowToUpdate.setAttribute(RipartizioniDAO.ID_MODULO, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), RipartizioniDAO.ID_MODULO + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

    
}
