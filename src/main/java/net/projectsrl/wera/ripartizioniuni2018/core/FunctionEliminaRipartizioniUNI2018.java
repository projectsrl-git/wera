package net.projectsrl.wera.ripartizioniuni2018.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.qhse.moduli.core.FunctionInserimentoALIMOD_base;
import net.projectsrl.wera.ripartizioniuni2018.db.RipartizioniUNI2018DAO;

public class FunctionEliminaRipartizioniUNI2018 extends FunctionInserimentoALIMOD_base<RipartizioniUNI2018DAO> {
	

    public FunctionEliminaRipartizioniUNI2018(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(RipartizioniUNI2018DAO.ID_MODULO, "");
            templateData.put(RipartizioniUNI2018DAO.DT_MODULO, project.misc.Utils.getStringDataOggi());
            templateData.put(RipartizioniUNI2018DAO.ID_UTENTE_INS, getSpecificUserInfo(userInfo).getIdUtente());
            
         /*   templateData.put(RipartizioniUNI2018DAO.CC_AC_QTA, 1);
            templateData.put(RipartizioniUNI2018DAO.CC_FM_QTA, 1);
            templateData.put(RipartizioniUNI2018DAO.CC_SI_QTA, 1);
            templateData.put(RipartizioniUNI2018DAO.CMI_SI_QTA, 1);
            templateData.put(RipartizioniUNI2018DAO.CMI_CIS_QTA, 1);
            templateData.put(RipartizioniUNI2018DAO.CMI_CCT_QTA, 1);
            templateData.put(RipartizioniUNI2018DAO.CMI_CIS_QTA, 1);
            
            templateData.put(RipartizioniUNI2018DAO.TC_CC_RIP, 100);
            templateData.put(RipartizioniUNI2018DAO.TC_CFM_RIP, 100);
            templateData.put(RipartizioniUNI2018DAO.TC_AC_RIP, 100);
            templateData.put(RipartizioniUNI2018DAO.TC_AF_RIP, 100);
            templateData.put(RipartizioniUNI2018DAO.TC_CCT_RIP, 100);
            
            templateData.put(RipartizioniUNI2018DAO.CC_AC_LORDO,0);
            templateData.put(RipartizioniUNI2018DAO.CC_FM_LORDO,0);
            templateData.put(RipartizioniUNI2018DAO.CC_SI_LORDO,0);
            templateData.put(RipartizioniUNI2018DAO.CMI_SI_LORDO,0);
            templateData.put(RipartizioniUNI2018DAO.CMI_CIS_LORDO,0);
            templateData.put(RipartizioniUNI2018DAO.CMI_CCT_LORDO,0);
            templateData.put(RipartizioniUNI2018DAO.CMI_CL_LORDO,0);*/
           
        } else {
            PjNDAO_base rowToUpdate = new RipartizioniUNI2018DAO();
            String idRow = req.getField(RipartizioniUNI2018DAO.ID_MODULO);
            rowToUpdate.setAttribute(RipartizioniUNI2018DAO.ID_MODULO, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), RipartizioniUNI2018DAO.ID_MODULO + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

    
}
