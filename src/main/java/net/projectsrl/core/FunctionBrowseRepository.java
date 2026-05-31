package net.projectsrl.core;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;


public class FunctionBrowseRepository extends FunctionCrossover_base {
    public static final String PAGE = "browse_folder";

    public FunctionBrowseRepository() {
        super();
    }
    
    protected boolean checkField(SsbServletRequest req) {
        return true;
    }

    public FunctionBrowseRepository(ApplicationServices_itf applServices, String functionID, String functionName) {
        super(applServices, functionID, functionName);
    }

    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo)
        throws AppCrash {
        elabora(req, res, userInfo);
    }

    @SuppressWarnings("unchecked")
	public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo)
        throws AppCrash {
        GestioneFileSystem fileSystem   = new GestioneFileSystem();
        Hashtable          templateData = new Hashtable();
        Vector             Hfile        = new Vector();

        File               file  = new File(req.getParameter("file"));
        int                level = Integer.parseInt(req.getParameter("level"));

        if (req.getParameter("level").equals("0") && req.getParameter("direction").equalsIgnoreCase("NULL")) {
            Hfile = fileSystem.directoryRootLevel();
        } else if (req.getParameter("level").equals("1") && req.getParameter("direction").equalsIgnoreCase("BACKWARD")) {
            level -= 1;
            Hfile = fileSystem.directoryRootLevel();
        } else if (req.getParameter("direction").equalsIgnoreCase("FORWARD")) {
            Hfile = fileSystem.directoryLevel(file);
            level += 1;
        } else if (!req.getParameter("level").equals("1") &&
                req.getParameter("direction").equalsIgnoreCase("BACKWARD")) {
            file = new File(file.getParent());
            level -= 1;
            Hfile = fileSystem.directoryLevel(file);
        } else if (!req.getParameter("level").equals("0") &&
                req.getParameter("direction").equalsIgnoreCase("NULL")) {
            Hfile = fileSystem.directoryLevel(file);
        }
        templateData.put("folder", req.getParameter("folder"));
        templateData.put("hf", Hfile);
        templateData.put("level", String.valueOf(level));
        _applicationSrv.displayPage(PAGE, templateData, res);
    }
}
