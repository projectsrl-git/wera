
package it.project.iride.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.ServletApplication_base;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.gui.PageFactory;
import net.project.servlet.gui.Page_itf;

public class IrideApplication extends ServletApplication_base implements ApplicationServices_itf {

    private static final long serialVersionUID = 1L;

    @Override
    public void processGet(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

    }

    @Override
    public void init(ServletConfig config) throws UnavailableException, ServletException {

        super.init(config);
    }

    @Override
    public void sessionExpired(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("LOGIN") == null) {
            try {
                res.sendRedirect("astro?FUNCTIONID=LoginInterno");
                return;
            } catch (IOException e) {
                throw new AppCrash(e);
            }
        }

    }

    /**
     * Metodo per la gestione della visualizzazione del messaggio di errore in caso di eccezioni non gestite dalle
     * implementazioni o generate nel framework stesso. Viene invocato dalla doPost e doGet. L'implementazione di
     * default non mostra nulla.
     */
    @Override
    protected void displayError(Throwable error, HttpServletRequest req, HttpServletResponse res) throws AppCrash {

        String functionField = Config.GetInstance().getProperty("Servlet.FunctionField");
        String functionDefaultField = Config.GetInstance().getProperty("Servlet.FunctionDefaultField");
        String functionName = "";

        if (functionField == null) {
            ErrDetector.GetInstance().param(functionDefaultField);
            functionField = functionDefaultField;
        }

        functionName = req.getParameter(functionField);

        if (functionName == null) {
            return;
        }

        String errorMessage = error.getMessage();
        if (error.getMessage() == null || errorMessage.trim().equals("")) {
            errorMessage = Config.GetInstance().getProperty("Messages.DefaultErrorMessage", "A generic error occurred");
        }

        try {
            res.sendRedirect("astro?" + functionField + "=" + functionName + "&MESSAGE=" + errorMessage);
        } catch (Throwable e) {
            throw new AppCrash(e);
        }

        return;
    }

    public String createXmlPage(String pageName, Map<String, Object> pageRootData,
            Map<String, Object> dataSourceParam[], SsbServletResponse resp) throws AppCrash {

        String filename = pageName + "." + pageRootData.get("USER") + ".xml";

        return createFileFromPage(pageName, pageRootData, dataSourceParam, resp, filename);
    }

    public String createGenericPage(String pageName, String filename, String extension,
            Map<String, Object> pageRootData, Map<String, Object> dataSourceParam[], SsbServletResponse resp)
            throws AppCrash {

        filename = filename + "." + pageRootData.get("USER") + "." + extension;

        return createFileFromPage(pageName, pageRootData, dataSourceParam, resp, filename);
    }

    public String createFileFromPage(String pageName, Map<String, Object> pageRootData,
            Map<String, Object> dataSourceParam[], SsbServletResponse resp, String filename) throws AppCrash {

        try {

            setSsbResponseFlags(resp, pageName);
            addCustomDataToTemplate(pageRootData, resp.getRequest(), resp);

            PageFactory pf = PageFactory.getInstance();
            Page_itf page = pf.makePage(getConfigName(), pageName);
            page.setPageRootData(pageRootData);
            for (int i = 0; i < dataSourceParam.length; i++) {
                if (dataSourceParam[i] != null) {
                    page.setDataSourceParam(dataSourceParam[i], i);
                }
            }

            File oldFile = new File(getRoot() + filename);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            PrintWriter pw = new PrintWriter(new FileWriter(getRoot() + filename));

            page.display(pw);

            pw.flush();
            pw.close();

            return filename;

        } catch (IOException ioe) {
            AppCrash ap = new AppCrash(ioe);
            ap.logContext("ServletApplication_base", "Errore nella displayPage");
            throw (ap);
        } catch (AppCrash ex) {
            ex.logContext("ServletApplication_base", "Errore nella displayPage");
            throw (ex);
        }
    }

}
