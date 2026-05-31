
package net.projectsrl.alibow.core;

import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.servlet.frame.Function_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.projectsrl.servlet.scheduler.SchedulerServletApplication;

public class AliBOWServletApplication extends SchedulerServletApplication {

    private static final String FUNCTION_HOME_PDLWEB = "HomePdLWeb";

    private static final String FUNCTION_HOME_ALIREP = "HomeAliRep";

    private static final String PDLWEB_URL = "pdlweb";

    private static final String ALIREP_URL = "alirep";

    private static final String FUNCTION_HOME = "Home";
    
    private static final long serialVersionUID = -2206462153765690795L;

    @Override
    protected Function_itf getFunction(SsbServletRequest req) throws AppCrash {

        
        String functionId = req.getField(Config.GetInstance().getProperty("Servlet.FunctionField"));
        
        if (!functionId.equals(FUNCTION_HOME)) {
            return super.getFunction(req);
        }
        
        StringBuffer requestuURl=req.getRequestURL();
        
        if (requestuURl.indexOf(ALIREP_URL) != -1){
            functionId=FUNCTION_HOME_ALIREP;
        } else if (requestuURl.indexOf(PDLWEB_URL) != -1){
            functionId=FUNCTION_HOME_PDLWEB;
        } else {
            return super.getFunction(req);
        }
        
        Function_itf actualHome=changeFunction(functionId, req);
        
        return actualHome;
    }


}
