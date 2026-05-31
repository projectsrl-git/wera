
package net.projectsrl.webapp.core;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.servlet.gui.FreeMarkerPage;

public class FreeMarkerPageNoTemplate extends FreeMarkerPage {

    public FreeMarkerPageNoTemplate() {
        super();
        // TODO Auto-generated constructor stub
    }

    public FreeMarkerPageNoTemplate(String name, String cfName) throws AppCrash {
        super(name, cfName);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected String getTemplateName() throws AppCrash {

        String tempProp = PAGE + "." + getName() + "." + TEMPLATE + "." + NAME;
        String templateName = Config.GetInstance(getConfigName()).getProperty(tempProp,
                getName().toLowerCase() + ".html");
        boolean templateNameEsistente = (templateName != null) && (templateName.length() > 0);
        ErrDetector.GetInstance().postCond(templateNameEsistente,
                "proprietà " + tempProp + " mancante nel file di configurazione");
        return templateName;
    }

}
