
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
import net.projectsrl.wera.anagrafiche.db.CondominiDAO;

public class FunctionInserimentoAnagraficaCondomini extends FunctionInserimentoALIMOD_base<CondominiDAO> {

    public FunctionInserimentoAnagraficaCondomini(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(CondominiDAO.ID_MODULO, "");
            templateData.put(CondominiDAO.DATA_PRIMO_SCARICO, project.misc.Utils.getStringDataOggi());
            templateData.put(CondominiDAO.DATA_PRIMA_SEGNALAZIONE, project.misc.Utils.getStringDataOggi());
            templateData.put(CondominiDAO.ID_UTENTE_INS, getSpecificUserInfo(userInfo).getIdUtente());

        } else {
            PjNDAO_base rowToUpdate = new CondominiDAO();
            String idRow = req.getField(CondominiDAO.ID_MODULO);
            rowToUpdate.setAttribute(CondominiDAO.ID_MODULO, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), CondominiDAO.ID_MODULO + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

	

}
