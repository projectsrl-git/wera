
package net.projectsrl.dafne.authentication;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.dafne.core.DafneCostanti_itf;
import net.projectsrl.dafne.db.UtentiAziendeDAO;
import net.projectsrl.dafne.db.UtentiDAO;
import net.projectsrl.dafne.db.UtentiProfiliDAO;
import net.projectsrl.dafne.db.UtentiUtenzeDAO;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.webapp.core.FunctionAjaxForm_base;

public class FunctionInserimentoUtenti extends FunctionAjaxForm_base<UtentiDAO> {



    public FunctionInserimentoUtenti(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(UtentiDAO.ID_UTENTE, "");
            templateData.put(UtentiDAO.USERNAME, "");
        } else {
            PjNDAO_base utente = new UtentiDAO();
            String idUtente = req.getField(UtentiDAO.ID_UTENTE);
            utente.setAttribute(UtentiDAO.ID_UTENTE, idUtente);
            ErrDetector.GetInstance().preCond(utente.retrieve(), UtentiDAO.ID_UTENTE + " not found");
            utente.setMapFromAttributes(templateData);

            templateData.put(DafneCostanti_itf.PROFILO_MULTIPLO, new UtentiProfiliDAO().getSelectedCodeList(idUtente));
            templateData.put(DafneCostanti_itf.AZIENDE_MULTIPLE, new UtentiAziendeDAO().getSelectedCodeList(idUtente));
            templateData.put(DafneCostanti_itf.UTENZE_MULTIPLE, new UtentiUtenzeDAO().getSelectedCodeList(idUtente));
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

    @Override
    protected boolean isAnInsert(SsbServletRequest req) {

        return Util.IsEmpty(req.getField(UtentiDAO.ID_UTENTE));
    }

}
