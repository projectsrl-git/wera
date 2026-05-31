
package net.projectsrl.wm.core;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.gui.PageFactory;
import net.project.servlet.gui.Page_itf;
import net.projectsrl.core.PDFCreator;

import org.apache.fop.apps.FOPException;

import project.misc.Utils;

public abstract class FunctionWM_base extends FunctionWebApp_base {

    public FunctionWM_base() {
        super();
        // TODO Auto-generated constructor stub
    }

    public FunctionWM_base(ApplicationServices_itf applServices, String functionID, String functionName) {
        super(applServices, functionID, functionName);
        // TODO Auto-generated constructor stub
    }

    /**
     * Recupera dalla sessione
     * 
     * @param SsbServletRequest req
     * 
     * @return String user Azienda
     */
    protected String getSessionApprovaFPS(SsbServletRequest req) {

        HttpSession session = req.getSession(false);
        String approvaFPS = "";
        if (session != null) {
            approvaFPS = (String) session.getAttribute("APPROVA_FPS_SESSIONE");
        }
        return approvaFPS;
    }

    /**
     * Recupera dalla sessione
     * 
     * @param SsbServletRequest req
     * 
     * @return String user Azienda
     */
    protected String getSessionApprovaRendicontazioni(SsbServletRequest req) {

        HttpSession session = req.getSession(false);
        String approvaR = "";
        if (session != null) {
            approvaR = (String) session.getAttribute("APPROVA_RENDICONTAZIONI_SESSIONE");
        }
        return approvaR;
    }

    protected String leggiHtml() throws IOException {

        String everything = "";
        String template = _applicationSrv.getRoot() + Config.GetInstance().getProperty("mail.template");
        BufferedReader br = new BufferedReader(new FileReader(template));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
                System.out.println(line);
            }
            everything = sb.toString();
            System.out.println(everything);
        } finally {
            br.close();
        }
        return everything;
    }

    protected String salvaFormatoPDF(String fileName, String page, HashMap templateData, Map[] dataSourceParam,
            String xmlName, String xslName) throws AppCrash {

        if (fileName.contains("Modulitrasferte")) {
            String allegato = templateData.get("AZIENDA_TENDINA") + "/" + templateData.get("DIPENDENTE") + "/"
                    + templateData.get("ID_MODTRASFERTA") + "/" + Utils.normalizeASCIIFilename(fileName) + ".pdf";
            String queryUpdate = "UPDATE MOD_TRASFERTE SET ALLEGATO='" + allegato + "', FILENAME='"
                    + Utils.normalizeASCIIFilename(fileName) + "' WHERE ID_MODTRASFERTA='"
                    + templateData.get("ID_MODTRASFERTA") + "'";
            net.projectsrl.wm.utils.WMUtils.executeQuery(queryUpdate);
            fileName = _applicationSrv.getRoot() + Config.GetInstance().getProperty("cartella.moduli.trasferte")
                    + allegato;
        }

        if (fileName.contains("Rendicontazione")) {
            String allegato = templateData.get("AZIENDA_TENDINA") + "/" + templateData.get("DIPENDENTE") + "/"
                    + templateData.get("ID_RENDICONTAZIONE") + "/" + Utils.normalizeASCIIFilename(fileName) + ".pdf";
            String queryUpdate = "UPDATE RENDICONTAZIONI SET ALLEGATO='" + allegato + "', FILENAME='"
                    + Utils.normalizeASCIIFilename(fileName) + "' WHERE ID_RENDICONTAZIONE='"
                    + templateData.get("ID_RENDICONTAZIONE") + "'";
            net.projectsrl.wm.utils.WMUtils.executeQuery(queryUpdate);
            fileName = _applicationSrv.getRoot() + Config.GetInstance().getProperty("cartella.moduli.rendicontazioni")
                    + allegato;

        }

        if (fileName.contains("InfortunioINAIL")) {
            fileName = _applicationSrv.getRoot() + Config.GetInstance().getProperty("Page." + page + ".storePath", "./")
                    + "/" + templateData.get("AZIENDA_TENDINA") + "/Infortuni_INAIL/"
                    + Utils.normalizeASCIIFilename(fileName) + ".pdf";

            String queryUpdate = "UPDATE INFORTUNIO_INAIL SET ALLEGATO='" + "areadocumenti/"
                    + templateData.get("AZIENDA_TENDINA") + "/Infortuni_INAIL/InfortunioINAIL_"
                    + templateData.get("TAGGANCIO") + ".pdf" + "', FILENAME='InfortunioINAIL_"
                    + templateData.get("TAGGANCIO") + ".pdf' WHERE TAGGANCIO='" + templateData.get("TAGGANCIO") + "'";
            net.projectsrl.wm.utils.WMUtils.executeQuery(queryUpdate);
        }

        if (!fileName.contains("Modulitrasferte") && !fileName.contains("Rendicontazione")
                && !fileName.contains("InfortunioINAIL")) {
            fileName = _applicationSrv.getRoot() + Config.GetInstance().getProperty("Page." + page + ".storePath", "./")
                    + "/" + Utils.normalizeASCIIFilename(fileName) + ".pdf";
        }

        File baseDir = new File(_applicationSrv.getRoot());
        File xsltfile = new File(baseDir, "xsl/" + xslName);
        File xmltfile = new File(baseDir, "WEB-INF/template/" + xmlName);

        PageFactory pf = PageFactory.getInstance();

        Page_itf template = pf.makePage(page);

        template.setPageRootData(templateData);
        for (int i = 0; i < dataSourceParam.length; i++) {
            if (dataSourceParam[i] != null) {
                template.setDataSourceParam(dataSourceParam[i], i);
            }
        }

        writeByteArrayOutputStreamToPDFFile(fileName, template, xmltfile, xsltfile);
        return fileName;
    }

    private void writeByteArrayOutputStreamToPDFFile(String fileName, Page_itf template, File xmlFile, File xslFileName)
            throws AppCrash {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        String xmlFileName = xmlFile.getName() + ".tmp";
        // Creazione file XML
        try {
            PrintWriter pw = new PrintWriter(baos);
            template.display(pw);
            pw.flush();

            FileOutputStream fos = new FileOutputStream(xmlFileName, false);
            OutputStreamWriter wrtout = new OutputStreamWriter(fos);

            String s = baos.toString("UTF-8");

            wrtout.write(s);

            wrtout.flush();
            wrtout.close();

        } catch (Throwable e) {
            throw new AppCrash(e);
        }

        File pdfFile = new File(fileName);

        // Creazione file PDF
        try {
            PDFCreator.convertXML2PDF(xmlFileName, xslFileName, pdfFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FOPException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    protected void readPDFFile(String fileName, String setHeaderElenco, String functionName, SsbServletResponse res)
            throws AppCrash {

        res.setContentType("application/donwload");
        if (setHeaderElenco.contains("Modulitrasferte")) {
            res.setHeader("Content-Disposition", "attachment; filename=\"" + setHeaderElenco + ".pdf\"");
        } else {
            res.setHeader("Content-Disposition",
                    "attachment; filename=\"" + setHeaderElenco + "_" + Utils.getUnique() + ".pdf\"");
        }

        InputStream ist = null;

        String text = "";
        try {
            ist = new FileInputStream(_applicationSrv.getRoot() + fileName);

            byte[] buffer = new byte[1024];
            int length;

            ServletOutputStream op = res.getOutputStream();
            while ((ist != null) && ((length = ist.read(buffer)) != -1)) {
                op.write(buffer, 0, length);
            }
            ist.close();

        } catch (Throwable e) {

            new AppCrash(e).logContext(functionName, "CRASH " + text);

        } finally {

            if (ist != null) {
                try {
                    ist.close();
                } catch (Throwable th) {
                    new AppCrash(_functionName + " errore nella chiusura di InputStream ist");
                }
            }

        }
    }

    protected void readPDFFileTimesheet(String fileName, String setHeaderElenco, String functionName,
            SsbServletResponse res, String anno, String mese, String risorsa) throws AppCrash {

        res.setContentType("application/donwload");
        res.setHeader("Content-Disposition",
                "attachment; filename=\"" + setHeaderElenco + "_" + risorsa + "_" + mese + "_" + anno + ".pdf\"");

        InputStream ist = null;

        String text = "";
        try {
            ist = new FileInputStream(_applicationSrv.getRoot() + fileName);

            byte[] buffer = new byte[1024];
            int length;

            ServletOutputStream op = res.getOutputStream();
            while ((ist != null) && ((length = ist.read(buffer)) != -1)) {
                op.write(buffer, 0, length);
            }
            ist.close();

        } catch (Throwable e) {

            new AppCrash(e).logContext(functionName, "CRASH " + text);

        } finally {

            if (ist != null) {
                try {
                    ist.close();
                } catch (Throwable th) {
                    new AppCrash(_functionName + " errore nella chiusura di InputStream ist");
                }
            }

        }
    }

    protected String salvaFormatoWord(String fileName, String page, HashMap templateData) throws AppCrash {

        fileName = _applicationSrv.getRoot() + Config.GetInstance().getProperty("Page." + page + ".storePath", "./")
                + "/" + Utils.normalizeASCIIFilename(fileName) + ".doc";

        PageFactory pf = PageFactory.getInstance();

        Page_itf template = pf.makePage(page);

        template.setPageRootData(templateData);

        writeByteArrayOutputStreamToWordFile(fileName, template);

        return fileName;
    }

    private void writeByteArrayOutputStreamToWordFile(String fileName, Page_itf template) throws AppCrash {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PrintWriter pw = new PrintWriter(baos);
            template.display(pw);
            pw.flush();

            FileOutputStream fos = new FileOutputStream(fileName, false);
            OutputStreamWriter wrtout = new OutputStreamWriter(fos);

            String s = baos.toString("UTF-8");

            wrtout.write(s);

            wrtout.flush();
            wrtout.close();

        } catch (Throwable e) {
            throw new AppCrash(e);
        }

    }

    protected String salvaFormatoWord(String fileName, String page, HashMap templateData, Map[] dataSourceParam)
            throws AppCrash {

        fileName = _applicationSrv.getRoot() + Config.GetInstance().getProperty("Page." + page + ".storePath", "./")
                + "/" + Utils.normalizeASCIIFilename(fileName) + ".doc";

        PageFactory pf = PageFactory.getInstance();

        Page_itf template = pf.makePage(page);

        template.setPageRootData(templateData);
        for (int i = 0; i < dataSourceParam.length; i++) {
            if (dataSourceParam[i] != null) {
                template.setDataSourceParam(dataSourceParam[i], i);
            }
        }

        writeByteArrayOutputStreamToWordFile(fileName, template);
        return fileName;
    }

    /**
     * Popola il PjDAO_base dao in input con tuti i campi/valori provenienti dalla SsbServletRequest req (in input)
     * aventi gli stessi nomi dei campi del dao
     * 
     * @param SsbServletRequest req
     * @param PjDAO_base dao
     * @return PjDAO_base dao
     * @throws AppCrash
     */
    protected net.projectsrl.db.PjDAO_base setDAOFieldsFromRequestPrivate(SsbServletRequest req, net.projectsrl.db.PjDAO_base dao)
            throws AppCrash {

        // condizioni iniziali sui parametri
        //
        ErrDetector.GetInstance().preCond(req != null, "setDAOFieldsFromRequest - req!=null");
        ErrDetector.GetInstance().preCond(dao != null, "setDAOFieldsFromRequest - dao!=null");

        Iterator daoFields = dao.iterator();

        while (daoFields != null && daoFields.hasNext()) {
            String element = (String) daoFields.next();
            String reqElement = element;
            if (element.startsWith("?")) {
                reqElement = element.substring(2);
            }
            dao.setField(element, req.getField(reqElement));
        }

        return dao;
    }

}
