
package net.projectsrl.bow.core;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import net.projectsrl.mail.DeferredMailSender;
import net.projectsrl.webapp.core.ServletApplication;

public class BOWServletApplication extends ServletApplication {

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


}
