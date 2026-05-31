
package net.projectsrl.wm.menu;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.wm.core.FunctionWebApp_base;

/**
 * FunctionLogout
 * 
 */
public class FunctionLogout extends FunctionWebApp_base {

	public FunctionLogout() {

		super();

	}

	public FunctionLogout(ApplicationServices_itf applServices, String functionID, String functionName) {

		super(applServices, functionID, functionName);
	}

	@Override
	public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

		elabora(req, res, userInfo);

	}

	@Override
	public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

		HttpSession session = req.getSession(false);
		session.invalidate();
		_applicationSrv.sessionExpired(req, res);
		
        try {
            res.sendRedirect("astro?FUNCTIONID=LoginInterno");
        } catch (IOException e) {
            throw new AppCrash(e);
        }
        
	}

}
