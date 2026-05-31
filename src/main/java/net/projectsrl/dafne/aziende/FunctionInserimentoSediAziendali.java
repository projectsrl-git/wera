
package net.projectsrl.dafne.aziende;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.dafne.db.SediAziendaliDAO;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.webapp.core.FunctionAjaxForm_base;

/**
 * FunctionInserimentoSediAziendali
 */
public class FunctionInserimentoSediAziendali extends FunctionAjaxForm_base<SediAziendaliDAO> {

    public FunctionInserimentoSediAziendali(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(SediAziendaliDAO.CODICE, "");
        } else {
            PjNDAO_base sediaziendali = new SediAziendaliDAO();
            String idFeriePermesso = req.getField(SediAziendaliDAO.CODICE);
            sediaziendali.setAttribute(SediAziendaliDAO.CODICE, idFeriePermesso);
            ErrDetector.GetInstance().preCond(sediaziendali.retrieve(), SediAziendaliDAO.CODICE + " not found");
            sediaziendali.setMapFromAttributes(templateData);
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }


    @Override
    protected boolean isAnInsert(SsbServletRequest req) {

        return Util.IsEmpty(req.getField(SediAziendaliDAO.CODICE));
    }

}
