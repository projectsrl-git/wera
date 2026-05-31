
package net.projectsrl.wera.anagrafiche.core;

import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.FunctionAjaxForm_base;
import net.projectsrl.wera.anagrafiche.db.UtenzeDAO;
import net.projectsrl.wera.anagrafiche.db.UtenzeDettagliDAO;

public class FunctionInserimentoDettaglioAnagraficaUtenze extends FunctionAjaxForm_base<UtenzeDettagliDAO> {

    public FunctionInserimentoDettaglioAnagraficaUtenze(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    protected boolean isAnInsert(SsbServletRequest req) {

        return Util.IsEmpty(req.getField(UtenzeDettagliDAO.ID_DETTAGLIO));
    }

    @Override
    protected void insert(SsbServletRequest req, UtenzeDettagliDAO formDao, UserSecurityInfo userInfo) throws AppCrash {

        formDao.setAttributesFromRequest(req);
        formDao.insert();
        
        
        String rilevatore= req.getField(UtenzeDettagliDAO.RILEVATORE);
        String idCondominio="";
        UtenzeDAO utenze = new UtenzeDAO();
        utenze.setAttribute(UtenzeDAO.ID_MODULO, req.getField(UtenzeDAO.ID_MODULO));
        	
        if (utenze.retrieve()) {
        	idCondominio=utenze.getAttributeAsString("ID_CONDOMINIO");
        }

        
        
        String queryUpdate= "update scarico set id_condominio="+idCondominio+" where n_fabbrica_dispositivo='"+rilevatore+"' and id_condominio=0";
        net.projectsrl.wm.utils.WMUtils.executeQuery(queryUpdate);
        String queryUpdate1= "update dati_rilevatori set id_condominio="+idCondominio+" where rilevatore='"+rilevatore+"' and id_condominio=0";
        net.projectsrl.wm.utils.WMUtils.executeQuery(queryUpdate1);
    }    
}
