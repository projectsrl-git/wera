
package it.project.webapp.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.Function_base;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import project.db.PjDAO_base;
import project.misc.Utils;

@Deprecated
public abstract class FunctionWebApp_base extends Function_base implements Costanti_itf {

    public static final String WEB_INF                = "WEB-INF";
    public static final String DB_PROPERTIES_FILENAME = "db_connection.properties";

    public FunctionWebApp_base() {

        super();
    }

    public FunctionWebApp_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    protected boolean checkField(SsbServletRequest req) {

        if (req.getSession(false) != null) {
            SessionCounter.updateLastActivity(req.getSession(false), getName());

            String noLogFunctionList = Config.GetInstance().getProperty("LogOperation.NoLog.FunctionList", "");
            if (!noLogFunctionList.contains(getName())) {
                Utils.logOperation(getSessionUser(req), getSessionRole(req) + " - "
                        + req.getSession(false).getAttribute("USER_COGNOME") + " "
                        + req.getSession(false).getAttribute("USER_NOME"), getFunctionID() + " " + getName(),
                        Utils.getRequestParameters(req));
            }

        }

        return true;
    }

    protected String getPageTitle() {

        return readPageTitle();
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

    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        _applicationSrv.displayPage("ERRORE", setCommonTags(req, userInfo), res);

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

        if (req.getSession(false) != null) {

            Utils.logOperation(getSessionUser(req),
                    getSessionRole(req) + " - " + req.getSession(false).getAttribute("USER_COGNOME") + " "
                            + req.getSession(false).getAttribute("USER_NOME"), "Chiusura sessione di lavoro",
                    Utils.getRequestParameters(req));
        }
        req.getSession().invalidate();
    }

    protected boolean checkSession(SsbServletRequest req) {

        HttpSession session = req.getSession(false);
        if (session == null) {
            return false;
        } else {
            UserSecurityInfo userInfo = (UserSecurityInfo) session.getAttribute("LOGIN");
            if (userInfo == null) {
                return false;
            } else if (userInfo.getUserId().equals("")) {
                return false;
            }
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
     * Setta i tag di freemarker comuni alle Function figlie di questa _base
     * 
     * @param SsbServletRequest req
     * @param UserSecurityInfo userInfo
     * 
     * @return java.util.Hashtable
     */
    protected HashMap<String, Object> setCommonTags(SsbServletRequest req, UserSecurityInfo userInfo) throws AppCrash {

        try {
            HashMap<String, Object> templateData = new HashMap<String, Object>();

            String treeGridSerialCode = Config.GetInstance().getProperty("TreeGrid.SerialCode", "");

            if (treeGridSerialCode.equals("")) {
                templateData.put("PAGE_NAME", _functionName.toLowerCase());
            } else {
                templateData.put("PAGE_NAME", _functionName.toLowerCase() + "' Code='" + treeGridSerialCode);
            }

            String tagIncludedFrame = Config.GetInstance().getProperty("Page.DefaultMenuName");
            templateData.put("INCLUDED_FRAME", tagIncludedFrame);

            String tagIncludedFrameRicerche = Config.GetInstance().getProperty("Page.DefaultMenuNameRicerche");
            templateData.put("INCLUDED_FRAME_RICERCHE", tagIncludedFrameRicerche);
            
            String tagIncludedFrameGantt = Config.GetInstance().getProperty("Page.DefaultMenuNameGantt");
            templateData.put("INCLUDED_FRAME_GANTT", tagIncludedFrameGantt);

            String tagIncludedFrameTablet = Config.GetInstance().getProperty("Page.DefaultMenuTabletName");
            templateData.put("INCLUDED_FRAME_TABLET", tagIncludedFrameTablet);
            
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


            
            HttpSession session = req.getSession(false);

            String logo = "";
            String logo_in_elenchi = "";
            String societa = "";
            String pageTitle = "";

            if (session != null) {
                logo = (String) session.getAttribute("LOGO");
                logo_in_elenchi = (String) session.getAttribute("LOGO_IN_ELENCHI");

                societa = (String) session.getAttribute("NOME_SOCIETA");
                if (societa == null) {
                    societa = "";
                }

                pageTitle = (String) session.getAttribute("PAGE_TITLE");
                if (pageTitle == null) {
                    pageTitle = "";
                }
            }
            templateData.put("LOGO", logo);
            templateData.put("LOGO_IN_ELENCHI", logo_in_elenchi);
            templateData.put("NOME_SOCIETA", societa);
            templateData.put("PAGE_TITLE", pageTitle);

            templateData.put("BLOCCO", "NO");

            // controllo pressione dei tasti senza aver salvato
            //
            templateData.put("CONTROLLO_SALVATAGGIO", "SI");

            // gestione blocco
            //
            String sessionId = req.getRequestedSessionId();
            String sessioneModificato = Config.GetInstance().getProperty("SESSIONE_IN_MODIFICA", "");

            if (sessioneModificato.equals(sessionId)) {
                Config.GetInstance().setProperty("SESSIONE_IN_MODIFICA", "");
            }

            getMessages(req, templateData);

            loadDataFromSession(session, templateData);

            return templateData;

        } catch (Throwable t) {
            AppCrash ap = new AppCrash(t);
            throw ap;
        }
    }

    protected void loadDataFromSession(HttpSession session, HashMap<String, Object> templateData) throws AppCrash {

        loadMenu(session, templateData);
    }

    private void loadMenu(HttpSession session, HashMap<String, Object> templateData) throws AppCrash {

        String user = getSessionUser(session);
        String ruolo = getSessionRole(session);

        String subMenu = getFunctionID();

        templateData.put("USER", user);
        templateData.put("SUBMENU", subMenu);
        templateData.put("RUOLO", ruolo);
        templateData.put("USER_COGNOME", (session.getAttribute("USER_COGNOME")));
        templateData.put("USER_NOME", (session.getAttribute("USER_NOME")));

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

    private void getMessages(SsbServletRequest req, HashMap<String, Object> templateData) throws AppCrash {

        String messageCode = req.getField("MESSAGECODE").trim();
        String messageString = req.getField("MESSAGE").trim();

        if (!messageCode.equals("")) {
            messageString = Config.GetInstance().getProperty("Messages." + _functionName + "." + messageCode, "");
        }

        if (messageString.equals("")) {
            return;
        }

        templateData.put("MESSAGE", messageString);
    }

    protected HashMap<String, Object> setTemplateDataFromRequest(HashMap<String, Object> templateData,
            SsbServletRequest req) {

        // legge tutti i parametri della request
        Enumeration<?> param = req.getParameterNames();

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

        Enumeration param = req.getParameterNames(); // Collezione di parametri

        Hashtable<String, String> requestData = new Hashtable<String, String>();
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
    protected HashMap[] setPageDatasetParam(String page, SsbServletRequest req, HashMap templateData) throws AppCrash {

        HashMap<String, String> queryParameter = new HashMap<String, String>();

        // recupera il numero di dataset presenti nella pagina
        String strDsNum = Config.GetInstance().getProperty("Page." + page + ".DSNum");
        ErrDetector.GetInstance().invariant(strDsNum != null,
                "Non trovato in cunfigurazione numero di DataSet Page." + page + ".DSNum");
        int dsNum = 0;
        try {
            dsNum = Integer.parseInt(Config.GetInstance().getProperty("Page." + page + ".DSNum"));
        } catch (Throwable th) {
            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "Valore proprietà numero di DataSet Page." + page + ".DSNum="
                    + strDsNum + " non numerico");
        }

        // crea un array di hashtable
        HashMap[] dsArray = new HashMap[dsNum];

        // i parametri sono gli stessi per tutti i dataset
        for (int i = 0; i < dsNum; i++) {

            dsArray[i] = prepareWhereCondition(req, prepareWhereCondition(templateData, queryParameter));
        }

        return dsArray;

    }

    protected HashMap<String, String> prepareWhereCondition(SsbServletRequest req,
            HashMap<String, String> queryParameter) {

        return queryParameter;
    }

    protected HashMap<String, String> prepareWhereCondition(HashMap<String, String> templateData,
            HashMap<String, String> queryParameter) {

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

    /**
     * Recupera il ruolo utente dalla sessione
     * 
     * @param SsbServletRequest req
     * 
     * @return String user ruolo utente
     */
    protected String getSessionRole(SsbServletRequest req) {

        HttpSession session = req.getSession(false);
        return getSessionRole(session);

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
        ruolo = (String) session.getAttribute("RUOLO");
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

        return getSessionUser(session);
    }

    /**
     * Recupera il codice utente dalla sessione
     * 
     * @param HttpSession session
     * 
     * @return String user codice utente
     */
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

        String applicationPath = _applicationSrv.getRoot() + WEB_INF;
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

    protected String readPageTitle() {

        String applicationPath = _applicationSrv.getRoot() + WEB_INF;
        File file = new File(applicationPath + File.separator + DB_PROPERTIES_FILENAME);

        Properties properties = new Properties();
        String pageTitle = "";
        try {
            properties.load(new FileInputStream(file));
            pageTitle = properties.getProperty("page_title");
        } catch (IOException e) {
            pageTitle = "Progetto Iride";
        }

        return pageTitle;
    }

    protected String readSecondaryDBHostName() {

        String applicationPath = _applicationSrv.getRoot() + WEB_INF;
        File file = new File(applicationPath + File.separator + DB_PROPERTIES_FILENAME);

        Properties properties = new Properties();
        String dbHostName = "";
        try {
            properties.load(new FileInputStream(file));
            dbHostName = properties.getProperty("host_name2");
        } catch (IOException e) {
            dbHostName = "";
        }

        return dbHostName;
    }

    /**
     * Redirige l'applicazione alla stessa pagina corrente, facendo comparire però un messaggio <br>
     * <br>
     * La sintassi per la codifica del messaggio su file di configurazione è: <br>
     * <br>
     * Messages.<Function name come da functionList.txt>.<codice del messaggio>=<testo del messaggio in formato HTML> <br>
     * <br>
     * esempio: Messages.LoginInterno.1=Login failure: unknown user or password incorrect <br>
     * <br>
     * Attenzione! per far comparire nella pagina richiamata il testo del messaggio deve essere presente<br>
     * nel metodo "mostra" un richiamo alla setCommonTags per popolare la struttura della Map che definisce<br>
     * i dati della Page_itf costruita con freemarker <br>
     * <br>
     * esempio: nella mostra ci sarà un richiamo della displayPage di questo tipo<br>
     * <br>
     * _applicationSrv.displayPage(PAGE, setCommonTags(req, userInfo), res); <br>
     * 
     * @param SsbServletResponse res
     * @param String messageCode Codice del messaggio che si vuol far comparire
     * @throws AppCrash
     */
    protected void redirectToThisPageWithMessage(SsbServletResponse res, String messageCode) throws AppCrash {

        try {
            res.sendRedirect("astro?FUNCTIONID=" + this._functionName + "&MESSAGECODE=" + messageCode);
        } catch (IOException e) {
            throw new AppCrash(e);
        }

    }

}
