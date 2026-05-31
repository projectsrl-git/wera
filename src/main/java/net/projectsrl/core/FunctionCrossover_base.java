
package net.projectsrl.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;

import org.apache.fop.apps.FOPException;

import it.project.webapp.core.MenuItem;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.Function_base;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.gui.PageFactory;
import net.project.servlet.gui.Page_itf;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.PjDAO_base;
import net.projectsrl.wm.utils.Utils;


public abstract class FunctionCrossover_base extends Function_base implements Costanti_itf {

    public static final String WEB_INF                = "WEB-INF";
    public static final String DB_PROPERTIES_FILENAME = "db_connection.properties";

    public FunctionCrossover_base() {

        super();
    }

    public FunctionCrossover_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    protected boolean checkField(SsbServletRequest req) {

        return true;
    }

    @Override
    protected void authenticationFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

    }

    @Override
    protected void checkFieldAutFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

    }

    @Override
    protected void checkFieldFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

    }

    @SuppressWarnings("unchecked")
	@Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        HashMap templateData = (HashMap) setCommonTags(req, userInfo);

        _applicationSrv.displayPage("ERRORE", templateData, res);

    }

    /**
     * Verifica la necessità di permessi per accedere alla funzione. Di default restituisce false percio', se non
     * ridefinito, fa si che l'accesso alla funzione non sia libero
     * 
     * @return true se non sono necessari permessi, false altrimenti.
     */
    public boolean isAccessFree() {

        return true;
    }

    /**
     * Destroy della sessione
     * 
     * @param req SsbServletRequest Request.
     */
    protected void destroySession(SsbServletRequest req) {

        req.getSession().invalidate();
    }

    protected boolean checkSession(SsbServletRequest req) {

        HttpSession session = req.getSession(false);
        if (session == null || ((String) session.getAttribute("USER")).equals("")) {
            return false;
        }

        Config.GetInstance().setProperty("DB.ConnectionURL", getSessionDbUrl(req));

        return true;
    }

    /**
     * Verifica la necessità di effettuare l'autenticazione. Trattandosi della funzione di login, tramite la quale ci si
     * autentica, ovviamente non richiede autenticazione.
     * 
     * @return false, perche' questa funzione non richiede autenticazione.
     */
    public boolean isAuthenticationRequired() {

        return false;
    }

    /**
     * Setta i tag di freemarker comuni alle function figlie Creation date: (10/07/02 11.09.03)
     * 
     * @param req DOCUMENT ME!
     * @param userInfo DOCUMENT ME!
     * 
     * @return java.util.Hashtable
     */
    @SuppressWarnings("unchecked")
    protected HashMap setCommonTags(SsbServletRequest req, UserSecurityInfo userInfo) throws AppCrash {

        try {
            HashMap templateData = new HashMap();
            HttpSession session = req.getSession(false);

            String logo = "";
            String logo_in_elenchi = "";

            if (session != null) {
                logo = (String) session.getAttribute("LOGO");
                logo_in_elenchi = (String) session.getAttribute("LOGO_IN_ELENCHI");
            }
            templateData.put("LOGO", logo);
            templateData.put("LOGO_IN_ELENCHI", logo_in_elenchi);

            String tagIncludedFrame = Config.GetInstance().getProperty("Page.DefaultMenuName");
            templateData.put("INCLUDED_FRAME", tagIncludedFrame);
            
            String tagIncludedFrameRicerche = Config.GetInstance().getProperty("Page.DefaultMenuNameRicerche");
            templateData.put("INCLUDED_FRAME_RICERCHE", tagIncludedFrameRicerche);
            
            String tagIncludedFrameGantt = Config.GetInstance().getProperty("Page.DefaultMenuNameGantt");
            templateData.put("INCLUDED_FRAME_GANTT", tagIncludedFrameGantt);
            
            String tagIncludedFrameDialog = Config.GetInstance().getProperty("Page.DefaultMenuNameDialog");
            templateData.put("INCLUDED_FRAME_DIALOG", tagIncludedFrameDialog);
            
            String tagIncludedFrameDialogRicerche = Config.GetInstance().getProperty("Page.DefaultMenuNameDialogRicerche");
            templateData.put("INCLUDED_FRAME_DIALOG_RICERCHE", tagIncludedFrameDialogRicerche);
            
            String tagIncludedFrameNoMenu = Config.GetInstance().getProperty("Page.DefaultMenuNameNoMenu");
            templateData.put("INCLUDED_FRAME_NOMENU", tagIncludedFrameNoMenu);
            
            String tagIncludedFrameMetro = Config.GetInstance().getProperty("Page.DefaultMenuNameMetro");
            templateData.put("INCLUDED_FRAME_METRO", tagIncludedFrameMetro);
            
            String tagIncludedFrameMetroMenu = Config.GetInstance().getProperty("Page.DefaultMenuNameMetroMenu");
            templateData.put("INCLUDED_FRAME_METRO_MENU", tagIncludedFrameMetroMenu);
            
            String tagIncludedFrameMetroRicerche = Config.GetInstance().getProperty("Page.DefaultMenuNameMetroRicerche");
            templateData.put("INCLUDED_FRAME_METRO_RICERCHE", tagIncludedFrameMetroRicerche);
            
            String tagIncludedFrameMetroTabs = Config.GetInstance().getProperty("Page.DefaultMenuNameMetroTabs");
            templateData.put("INCLUDED_FRAME_METRO_TABS", tagIncludedFrameMetroTabs);
            
            String tagIncludedMetroFullCalendar = Config.GetInstance().getProperty("Page.DefaultMenuNameMetroFullCalendar");
            templateData.put("INCLUDED_FRAME_METRO_FULLCALENDAR", tagIncludedMetroFullCalendar);
            
            String tagIncludedFrameMetroPwd = Config.GetInstance().getProperty("Page.DefaultMenuNameMetroPwd");
            templateData.put("INCLUDED_FRAME_METRO_PWD", tagIncludedFrameMetroPwd);


                        // gestione altert eventi
            HttpSession sessione = req.getSession(false);
            String flagAttivaAlert = "";
            String flagAttivaAlertTicket = "";

            if (sessione != null) {
                flagAttivaAlert = (String) sessione.getAttribute("ATTIVA_ALERT");
                flagAttivaAlertTicket = (String) sessione.getAttribute("ATTIVA_ALERT_TICKET");

                if (flagAttivaAlert == null || !flagAttivaAlert.equals("NO")) {
                    flagAttivaAlert = "SI";
                }
                if (flagAttivaAlertTicket == null || !flagAttivaAlertTicket.equals("NO")) {
                    flagAttivaAlertTicket = "SI";
                }
            }
            templateData.put("ATTIVA_ALERT", flagAttivaAlert);
            templateData.put("ATTIVA_ALERT_TICKET", flagAttivaAlertTicket);

            // visualizza societa
            String societa = "";

            if (sessione != null) {
                societa = (String) sessione.getAttribute("NOME_SOCIETA");

                if (societa == null) {
                    societa = "";
                }
            }
            templateData.put("NOME_SOCIETA", societa);

            templateData.put("BLOCCO", "NO");

            // controllo pressione dei tasti senza aver salvato
            //
            templateData.put("CONTROLLO_SALVATAGGIO", "SI");

            // gestione blocco
            String sessionId = req.getRequestedSessionId();
            String sessioneModificato = Config.GetInstance().getProperty("SESSIONE_IN_MODIFICA", "");

            if (sessioneModificato.equals(sessionId)) {
                Config.GetInstance().setProperty("CODICE_CV", "");
                Config.GetInstance().setProperty("USER_CV", "");
                Config.GetInstance().setProperty("SESSIONE_IN_MODIFICA", "");
            }

            loadDataFromSession(session, templateData);

            return templateData;
        } catch (Throwable t) {
            AppCrash ap = new AppCrash(t);
            throw ap;
        }
    }

    @SuppressWarnings("unchecked")
    protected HashMap setTemplateDataFromRequest(HashMap templateData, SsbServletRequest req) {

        Enumeration param = req.getParameterNames(); // Collezione di
        // parametri

        while (param.hasMoreElements()) {
            String name = (String) param.nextElement();
            String value = req.getField(name);
            if (value.contains("\"")) {
                value = value.replaceAll("\"", "&quot;");
            }
            templateData.put(name, value);
        }

        return templateData;
    }

    @SuppressWarnings("unchecked")
    protected void putRequestDataInSession(SsbServletRequest req) {

        Enumeration param = req.getParameterNames(); // Collezione di
        // parametri

        Hashtable requestData = new Hashtable();
        while (param.hasMoreElements()) {
            String name = (String) param.nextElement();
            String value = req.getField(name);
            requestData.put(name, value);
        }

        HttpSession sessione = req.getSession(false);
        if (sessione != null) {
            sessione.setAttribute(PREVIOUS_REQUEST_HASH, requestData);
        }

    }

    @SuppressWarnings("unchecked")
	protected String concatPreviousRequestDataInString(SsbServletRequest req) {

        Hashtable requestData = new Hashtable();
        String stringRequestData = "";

        HttpSession sessione = req.getSession(false);
        if (sessione != null) {
            requestData = (Hashtable) sessione.getAttribute(PREVIOUS_REQUEST_HASH);
        }

        if (requestData != null) {
            stringRequestData = concatHashDataInRequestString(requestData);
        }

        return stringRequestData;
    }

    @SuppressWarnings("unchecked")
	protected String concatHashDataInRequestString(Hashtable requestData) {

        String stringRequestData = "";

        Enumeration param = requestData.keys();

        while (param.hasMoreElements()) {
            String name = (String) param.nextElement();
            String value = (String) requestData.get(name);

            if (name != null && !name.equals("") && value != null) {
                stringRequestData += "&" + name.trim() + "=" + value.trim();
            }

        }

        return stringRequestData;
    }

    @SuppressWarnings("unchecked")
    protected HashMap[] setPageDatasetParam(String page, SsbServletRequest req, HashMap templateData) {

        HashMap<String, String> queryParameter = new HashMap<String, String>();

        String dataAlertEventiInScadenzaIniziale = Utils.getStringDataOggiRibaltata();

        String dataAlertEventiInScadenzaFinale = Utils.getStringDataOggiRibaltata();

        queryParameter.put("DATA_ALERT_EVENTI_INIZIALE", dataAlertEventiInScadenzaIniziale);

        queryParameter.put("DATA_ALERT_EVENTI_FINALE", dataAlertEventiInScadenzaFinale);

        // recupera il numero di dataset presenti nella pagina
        int dsNum = Integer.parseInt(Config.GetInstance().getProperty("Page." + page + ".DSNum"));

        // crea un array di hashtable
        HashMap[] dsArray = new HashMap[dsNum];

        // i parametri sono gli stessi per tutti i dataset
        for (int i = 0; i < dsNum; i++) {

            dsArray[i] = prepareWhereCondition(req, prepareWhereCondition(templateData, queryParameter));
        }

        return dsArray;

    }

    @SuppressWarnings("unchecked")
	protected HashMap prepareWhereCondition(SsbServletRequest req, HashMap<String, String> queryParameter) {

        return queryParameter;
    }

    @SuppressWarnings("unchecked")
	protected HashMap prepareWhereCondition(HashMap<String, String> templateData, HashMap<String, String> queryParameter) {

        return queryParameter;
    }

    

    @SuppressWarnings("unchecked")
    protected boolean refresh(String page, SsbServletRequest req, HashMap templateData, SsbServletResponse res)
            throws AppCrash {

        String option = req.getField(Costanti_itf.OPZIONE_INSERIMENTO_MODIFICA);

        if (option != null
                && (option.equals(Costanti_itf.OPZIONE_REFRESH_INSERIMENTO) || option
                        .equals(Costanti_itf.OPZIONE_REFRESH_MODIFICA))) {

            // SE OPZIONE_INSERIMENTO_MODIFICA è un opzione di REFRESH
            // (REFRESH_INSERIMENTO o REFRESH_MODIFICA)
            // allora faccio REFRESH
            //
            templateData = setTemplateDataFromRequest(templateData, req);
            if (option.equals(Costanti_itf.OPZIONE_REFRESH_INSERIMENTO)) {
                templateData.put(Costanti_itf.OPZIONE_INSERIMENTO_MODIFICA, Costanti_itf.OPZIONE_INSERIMENTO);
            } else if (option.equals(Costanti_itf.OPZIONE_REFRESH_MODIFICA)) {
                templateData.put(Costanti_itf.OPZIONE_INSERIMENTO_MODIFICA, Costanti_itf.OPZIONE_MODIFICA);
            }

            templateData = modificaTemplateDataPerRefresh(templateData, req);

            _applicationSrv.displayPage(page, templateData, setPageDatasetParam(page, req, templateData), res);

            // ritorno TRUE per far sapere alla procedura che mi ha chiamato che
            // HO FATTO REFRESH
            //
            return true;
        } else {

            // altrimenti esco senza far nulla
            // ritorno FALSE per far sapere alla procedura che mi ha chiamato
            // che NON HO FATTO REFRESH
            return false;
        }
    }

    @SuppressWarnings("unchecked")
	protected HashMap modificaTemplateDataPerRefresh(HashMap templateData, SsbServletRequest req) throws AppCrash {

        // Da modificare con i campi che servono
        //
        return templateData;
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
    @SuppressWarnings("unchecked")
	protected PjDAO_base setDAOFieldsFromRequest(SsbServletRequest req, PjDAO_base dao) throws AppCrash {

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

   

   
   

   

    

    

    /**
     * Recupera l'azienda dalla sessione
     * 
     * @param SsbServletRequest req
     * 
     * @return String user Azienda
     */
    protected String getSessionAzienda(SsbServletRequest req) {

        HttpSession session = req.getSession(false);
        String azienda = "";
        if (session != null) {
            azienda = (String) session.getAttribute("NOME_SOCIETA");
        }
        return azienda;
    }

	// LUCA
	
    /**
     * Recupera il ruolo utente dalla sessione
     * 
	 * @param ProjectServletRequest
	 *            req
     * 
     * @return String user ruolo utente
     */
    protected String getSessionRole(SsbServletRequest req) {

        HttpSession session = req.getSession(false);
        String ruolo = "";

        UserSecurityInfo userInfo = (UserSecurityInfo) session.getAttribute("LOGIN");

        if (userInfo != null && userInfo.getRoleId() != null && !(userInfo.getRoleId().equals(""))) {
            ruolo = userInfo.getRoleId();
            return ruolo;
        }

        if (session != null) {
            ruolo = (String) session.getAttribute("RUOLO_SESSIONE");
	        System.out.println(" ruolo " + ruolo);
            if (ruolo == null) {
                ruolo = "";
            }
			
        }
			
        return ruolo;
    }

    /**
     * Recupera il ruolo utente dalla sessione
     * 
     * @param HttpSession session
     * 
     * @return String user ruolo utente
     */
    protected String getSessionRole(HttpSession session) {

        String ruolo = "";

        if (session == null) {
            return ruolo;
        }

        UserSecurityInfo userInfo = (UserSecurityInfo) session.getAttribute("LOGIN");

        if (userInfo != null && userInfo.getRoleId() != null && !(userInfo.getRoleId().equals(""))) {
            ruolo = userInfo.getRoleId();
            return ruolo;
        }

        // compatibilità con vecchia gestione ruolo AstroWeb
        ruolo = (String) session.getAttribute("RUOLO_SESSIONE");
        return ruolo;

    }
	
    protected String getSessionQuery(SsbServletRequest req) {

        HttpSession session = req.getSession(false);
        String query_ruolo = "";
        if (session != null) {
            query_ruolo = (String) session.getAttribute("QUERY_RUOLO");
        }
        return query_ruolo;
    }

	// FINE LUCA

	

	
    /**
     * Recupera il codice utente dalla sessione
     * 
     * @param SsbServletRequest req
     * 
     * @return String user codice utente
     */
    protected String getSessionUser(SsbServletRequest req) {

        HttpSession session = req.getSession(false);
        String user = "";
        if (session != null) {
            user = (String) session.getAttribute("USER");
        }
        return user;
    }

    protected String getSessionUser(HttpSession session) {

        String user = "";
        if (session != null) {
            user = (String) session.getAttribute("USER");
        }
        return user;
    }

    /**
     * Recupera la stringa di connessione dalla sessione
     * 
     * @param SsbServletRequest req
     * 
     * @return String DbUrl stringa di connessione
     */
    protected String getSessionDbUrl(SsbServletRequest req) {

        HttpSession session = req.getSession(false);
        String dbUrl = "";
        if (session != null) {
            dbUrl = (String) session.getAttribute("DBURL");
        }
        return dbUrl;
    }

    
    protected String readDBHostName() {

        // Read properties file.

        String className = this.getClass().getSimpleName() + ".class";

        URL url = this.getClass().getResource(className);
        String path = url.getFile();
        String applicationPath = path.substring(0, path.indexOf(WEB_INF)) + WEB_INF;
        File file = new File(applicationPath + File.separator + DB_PROPERTIES_FILENAME);

        Properties properties = new Properties();
        String dbHostName = "";
        try {
            properties.load(new FileInputStream(file));
            dbHostName = properties.getProperty("host_name");
        } catch (IOException e) {
            dbHostName = "---proprieta' non trovata---";
        }

        return dbHostName;
    }

    protected void loadDataFromSession(HttpSession session, HashMap<String, Object> templateData) throws AppCrash {

        templateData.put("DATA_ULTIMA_ELABORAZIONE", ((String) session.getAttribute("DATA_ULTIMA_ELABORAZIONE")));
        templateData.put("ORA_ULTIMA_ELABORAZIONE", ((String) session.getAttribute("ORA_ULTIMA_ELABORAZIONE")));
        loadMenu(session, templateData);
    }

    @SuppressWarnings("unchecked")
	private void loadMenu(HttpSession session, HashMap<String, Object> templateData) throws AppCrash {

        String user = getSessionUser(session);
        String ruolo = getSessionRole(session);

        String subMenu = getFunctionID();

        templateData.put("USER", user);
        templateData.put("SUBMENU", subMenu);
        templateData.put("RUOLO_SESSIONE", ruolo);
        templateData.put("USER_COGNOME", ((String) session.getAttribute("USER_COGNOME")));
        templateData.put("USER_NOME", ((String) session.getAttribute("USER_NOME")));
        templateData.put("ID_CODICE", ((String) session.getAttribute("ID_CODICE")));

        if (user.equals("")) {
            user = null;
            return;
        }

        ArrayList<MenuItem> vociMenu = (ArrayList<MenuItem>) session.getAttribute("voci_menu");
        if (vociMenu != null) {
            templateData.put("voci_menu", vociMenu);
        }

        ArrayList<MenuItem> dettVociMenu = (ArrayList<MenuItem>) session.getAttribute("dett_voci_menu");
        if (dettVociMenu != null) {
            templateData.put("dett_voci_menu", dettVociMenu);
        }

    }
    
    
    @SuppressWarnings("unchecked")
	protected String salvaFormatoPDF(String fileName, String page, HashMap templateData, Map[] dataSourceParam,
            String xmlName, String xslName) throws AppCrash {

        fileName = _applicationSrv.getRoot() + Config.GetInstance().getProperty("Page." + page + ".storePath", "./")
                + "/" + Utils.normalizeASCIIFilename(fileName) + ".pdf";
        
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

}
