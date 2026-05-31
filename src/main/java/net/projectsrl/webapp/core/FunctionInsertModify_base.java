
package net.projectsrl.webapp.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.security.UserSecurityInfo;

public abstract class FunctionInsertModify_base extends FunctionProjectWebApp_base {

    public FunctionInsertModify_base() {

        super();
    }

    public FunctionInsertModify_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }    

    @Override
    protected Map<String, Object> createMapFromRequest(SsbServletRequest req, UserSecurityInfo userInfo)
            throws AppCrash {

        Map<String, Object> map =super.createMapFromRequest(req, userInfo);
        
        boolean insert=isNew(req);
        
        if (insert) {
            map.put(WebAppConstants_itf.OPZIONE_INSERIMENTO_MODIFICA, WebAppConstants_itf.OPZIONE_INSERIMENTO);
            
        } else {
            map.put(WebAppConstants_itf.OPZIONE_INSERIMENTO_MODIFICA, WebAppConstants_itf.OPZIONE_MODIFICA);

        }
        
       
        return map;
    }

    protected abstract boolean isNew(SsbServletRequest req);


}
