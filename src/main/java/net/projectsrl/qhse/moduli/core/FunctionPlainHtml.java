
package net.projectsrl.qhse.moduli.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import project.misc.Utils;

public class FunctionPlainHtml extends FunctionProjectWebApp_base {

    public FunctionPlainHtml(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }
    
    @Override
    protected boolean checkSession(SsbServletRequest req) {
        return true;
    }

    @Override
    public boolean isAuthenticationRequired() {
        return false;
    }


    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        String fileName = req.getField("FILENAME");
        ErrDetector.GetInstance().param(Utils.IsNotEmpty(fileName), "filename is null");
        
        _applicationSrv.displayPage(fileName, templateData, setPageDatasetParam(fileName, req, templateData),
                res);
    }

    

}
