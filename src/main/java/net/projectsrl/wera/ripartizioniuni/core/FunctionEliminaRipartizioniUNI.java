
package net.projectsrl.wera.ripartizioniuni.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.qhse.moduli.core.FunctionInserimentoALIMOD_base;
import net.projectsrl.wera.ripartizioniuni.db.RipartizioniUNIDAO;

public class FunctionEliminaRipartizioniUNI extends FunctionInserimentoALIMOD_base<RipartizioniUNIDAO> {
	

    public FunctionEliminaRipartizioniUNI(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(RipartizioniUNIDAO.ID_MODULO, "");
            templateData.put(RipartizioniUNIDAO.DT_MODULO, project.misc.Utils.getStringDataOggi());
            templateData.put(RipartizioniUNIDAO.ID_UTENTE_INS, getSpecificUserInfo(userInfo).getIdUtente());
            
            templateData.put(RipartizioniUNIDAO.CC_AC_QTA, 1);
            templateData.put(RipartizioniUNIDAO.CC_FM_QTA, 1);
            templateData.put(RipartizioniUNIDAO.CC_SI_QTA, 1);
            templateData.put(RipartizioniUNIDAO.CMI_SI_QTA, 1);
            templateData.put(RipartizioniUNIDAO.CMI_CIS_QTA, 1);
            templateData.put(RipartizioniUNIDAO.CMI_CCT_QTA, 1);
            templateData.put(RipartizioniUNIDAO.CMI_CIS_QTA, 1);
            
            templateData.put(RipartizioniUNIDAO.TC_CC_RIP, 100);
            templateData.put(RipartizioniUNIDAO.TC_CFM_RIP, 100);
            templateData.put(RipartizioniUNIDAO.TC_AC_RIP, 100);
            templateData.put(RipartizioniUNIDAO.TC_AF_RIP, 100);
            templateData.put(RipartizioniUNIDAO.TC_CCT_RIP, 100);
            
            templateData.put(RipartizioniUNIDAO.CC_AC_LORDO,0);
            templateData.put(RipartizioniUNIDAO.CC_FM_LORDO,0);
            templateData.put(RipartizioniUNIDAO.CC_SI_LORDO,0);
            templateData.put(RipartizioniUNIDAO.CMI_SI_LORDO,0);
            templateData.put(RipartizioniUNIDAO.CMI_CIS_LORDO,0);
            templateData.put(RipartizioniUNIDAO.CMI_CCT_LORDO,0);
            templateData.put(RipartizioniUNIDAO.CMI_CL_LORDO,0);
           
        } else {
            PjNDAO_base rowToUpdate = new RipartizioniUNIDAO();
            String idRow = req.getField(RipartizioniUNIDAO.ID_MODULO);
            rowToUpdate.setAttribute(RipartizioniUNIDAO.ID_MODULO, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), RipartizioniUNIDAO.ID_MODULO + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

    
}
