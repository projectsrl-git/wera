
package net.projectsrl.wm.core;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.projectsrl.wm.mail.DeferredMailSender;

public class WMServletApplication extends net.projectsrl.webapp.core.ServletApplication {

    private static final long serialVersionUID = -6545639111665119144L;

    @Override
    public void init(ServletConfig config) throws UnavailableException, ServletException {

        super.init(config);

        DeferredMailSender.getInstance().start();

    }

    @Override
    public void destroy() {

        DeferredMailSender.getInstance().exit();

        super.destroy();

    }
    
    @Override
    public void sessionExpired(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        if (Config.GetInstance().getProperty("Page.SessioneScaduta.Template.Name") == null) return;

        displayPage("SessioneScaduta", res);
    }
   
}
