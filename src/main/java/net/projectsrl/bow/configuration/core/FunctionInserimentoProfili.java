
package net.projectsrl.bow.configuration.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.bow.configuration.db.ProfiliDAO;
import net.projectsrl.webapp.core.FunctionAjaxForm_base;

public class FunctionInserimentoProfili extends FunctionAjaxForm_base<ProfiliDAO> {

    public FunctionInserimentoProfili(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            
        } else {
            ProfiliDAO rowToUpdate = new ProfiliDAO();
            String idRow = req.getField(ProfiliDAO.ID_PROFILO);
            rowToUpdate.setAttribute(ProfiliDAO.ID_PROFILO, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), ProfiliDAO.ID_PROFILO + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
            
            String menuList=rowToUpdate.getMenuList();
            templateData.put("MENU_ABILITATI", menuList);

        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

    @Override
    protected void update(SsbServletRequest req, ProfiliDAO formDao, UserSecurityInfo userInfo) throws AppCrash {
        
        formDao.setMenuList(req.getField("MENU_ABILITATI"));
        super.update(req, formDao, userInfo);
    }
    
    

    @Override
    protected void insert(SsbServletRequest req, ProfiliDAO formDao, UserSecurityInfo userInfo) throws AppCrash {

        formDao.setMenuList(req.getField("MENU_ABILITATI"));
        super.insert(req, formDao, userInfo);
    }

    @Override
    protected boolean isAnInsert(SsbServletRequest req) {

        return Util.IsEmpty(req.getField(ProfiliDAO.ID_PROFILO));
    }

}
