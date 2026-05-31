
package net.projectsrl.qhse.moduli.core;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.webapp.security.WebAppUserSecurityInfo;
import project.misc.Utils;

public class FunctionHtml2Pdf extends FunctionProjectWebApp_base {

    public FunctionHtml2Pdf(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        elabora(req, res, userInfo);
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        String fileName = req.getField("FILENAME");
        ErrDetector.GetInstance().param(Utils.IsNotEmpty(fileName), "filename is null");
        String urlToParse = "astro?FUNCTIONID=PlainHtml&FILENAME="+fileName;
        downloadPDFFile(req, res, urlToParse, fileName);
        
    }

    @Override
    protected Map<String, Object> createMapFromRequest(SsbServletRequest req, UserSecurityInfo userInfo)
            throws AppCrash {

        Map<String, Object> map = new HashMap<String, Object>();

        setMapFromRequest(map, req);

        String includedHead = Config.GetInstance().getProperty("Page.Head.include");
        map.put(WebAppConstants_itf.INCLUDED_HEAD, includedHead);

        WebAppUserSecurityInfo<?> webAppUserInfo;
        // rilancio il ClassCastException
        try {
            webAppUserInfo = (WebAppUserSecurityInfo<?>) userInfo;
            Map<String, Object> sessionMap = webAppUserInfo.getSessionMap();

            map.putAll(sessionMap);
        } catch (Throwable e) {
            throw new AppCrash(e);
        }

        return map;
    }

    private void downloadPDFFile(SsbServletRequest req, SsbServletResponse res, String urlToParse, String fileName)
            throws AppCrash {

        Document document = new Document();

        ServletOutputStream os = null;
        InputStream is = null;

        try {

            URL urlPDF = new URL(getCurrentContextUrl(req) + "/"+urlToParse);

            is = urlPDF.openStream();

            res.setContentType("application/pdf");
            res.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".pdf\"");
            res.setContentType("application/pdf");
            os = res.getOutputStream();

            PdfWriter writer = PdfWriter.getInstance(document, os);
            document.open();
            XMLWorkerHelper.getInstance().parseXHtml(writer, document, is);
            document.close();
            os.flush();
            os.close();
            is.close();

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            throw ac;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (Throwable e) {
                    new AppCrash(e);
                }
            }

            if (is != null) {
                try {
                    is.close();
                } catch (Throwable e) {
                    new AppCrash(e);
                }
            }
        }
    }

    public static String getCurrentContextUrl(HttpServletRequest request) {

        // Getting servlet request URL
        String url = request.getRequestURL().toString();

        // Getting servlet request query string.
        String queryString = request.getQueryString();

        // Getting request information without the hostname.
        String uri = request.getRequestURI();

        // Below we extract information about the request object path
        // information.
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int portNumber = request.getServerPort();
        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        queryString = request.getQueryString();

        Logger.GetInstance().log0("Url: " + url + "<br/>");
        Logger.GetInstance().log0("Uri: " + uri + "<br/>");
        Logger.GetInstance().log0("Scheme: " + scheme + "<br/>");
        Logger.GetInstance().log0("Server Name: " + serverName + "<br/>");
        Logger.GetInstance().log0("Port: " + portNumber + "<br/>");
        Logger.GetInstance().log0("Context Path: " + contextPath + "<br/>");
        Logger.GetInstance().log0("Servlet Path: " + servletPath + "<br/>");
        Logger.GetInstance().log0("Path Info: " + pathInfo + "<br/>");
        Logger.GetInstance().log0("Query: " + queryString);

        return url.substring(0, url.lastIndexOf(servletPath));
    }    

}
