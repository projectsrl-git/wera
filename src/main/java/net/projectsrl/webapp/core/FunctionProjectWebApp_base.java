
package net.projectsrl.webapp.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.Function_base;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.authentication.LoginData;
import net.projectsrl.webapp.authentication.MenuItem;
import net.projectsrl.webapp.security.WebAppUserSecurityInfo;

public abstract class FunctionProjectWebApp_base extends Function_base {

    private static final String MESSAGE_LOGIN_FAILED           = "Messages.LoginFailed";
    private static final String MESSAGE_NEWPASSWORD_INFO       = "Messages.NewPassword.InfoMessage";
    private static final String LOGIN_PAGE                     = "login";
    private static final String NEW_PASSWORD                   = "newpassword";
    public static final String  TAG_INFO_MESSAGE               = "INFO_MESSAGE";
    public static final String  TAG_ERROR_MESSAGE              = "ERROR_MESSAGE";
    private static final String MESSAGES_DEFAULT_ERROR_MESSAGE = "Messages.DefaultErrorMessage";
    private static final String ERROR_PAGE                     = "ErrorPage";
    private static final String DB_PROPERTIES_FILENAME         = "db_connection.properties";

    public FunctionProjectWebApp_base() {

        super();
    }

    public FunctionProjectWebApp_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    /**
     * Verifica la necessit� di permessi per accedere alla funzione. Di default restituisce false percio', se non
     * ridefinito, fa si che l'accesso alla funzione non sia libero
     * 
     * @return true se non sono necessari permessi, false altrimenti.
     */
    @Override
    public boolean isAccessFree() {

        return true;
    }

    /**
     * Verifica la necessit� di effettuare l'autenticazione.
     * 
     * @return false, perche' questa funzione non richiede autenticazione.
     */
    @Override
    public boolean isAuthenticationRequired() {

        return true;
    }

    protected String getPageName() {

        return _functionName.toLowerCase();

    }


    protected Map<String, Object> createMapFromRequest(SsbServletRequest req, UserSecurityInfo userInfo)
            throws AppCrash {

        Map<String, Object> map = new HashMap<String, Object>();

        setMapFromRequest(map, req);

        map.put(WebAppConstants_itf.FUNCTIONID_PAGE_TAG, getMenuID());

        String includedHead = Config.GetInstance().getProperty("Page.Head.include");
        map.put(WebAppConstants_itf.INCLUDED_HEAD, includedHead);

        WebAppUserSecurityInfo<?> webAppUserInfo;
        // rilancio il ClassCastException
        try {
            webAppUserInfo = (WebAppUserSecurityInfo<?>) userInfo;
            manageWebAppUserSecurityInfo(webAppUserInfo, map);
        } catch (Throwable e) {
            throw new AppCrash(e);
        }

        detectLastActivity(req, map);

        return map;
    }

    protected void detectLastActivity(SsbServletRequest req, Map<String, Object> map) {

        if (req.getSession(false) == null) {
            return;
        }

        String functionId = "";
        if (getMenuID() != null) {
            functionId = getMenuID();
        }
        setSessionField(req, WebAppConstants_itf.FUNCTIONID_PAGE_TAG, functionId);

        String pathDescri = getPathDescription(map);
        setSessionField(req, WebAppConstants_itf.SELECTED_MENU_ITEM, pathDescri);
    }

    protected String getPathDescription(Map<String, Object> map) {

        String pathDescri = "";
        MenuItem selectedMenuItem = (MenuItem) map.get(WebAppConstants_itf.SELECTED_MENU_ITEM);
        if (selectedMenuItem != null) {
            if (selectedMenuItem.getPathDescri() != null) {
                pathDescri = selectedMenuItem.getPathDescri();
            }
        }
        return pathDescri;
    }

    protected void setMapFromRequest(Map<String, Object> templateData, SsbServletRequest req) {

        // legge tutti i parametri della request
        Enumeration<String> param = req.getParameterNames();

        while (param.hasMoreElements()) {
            String name = param.nextElement();
            String value = req.getField(name);
            if (value.contains("\"")) {
                value = value.replaceAll("\"", "&quot;");
            }

            templateData.put(name, value);

        }

    }

    private void manageWebAppUserSecurityInfo(WebAppUserSecurityInfo<?> userInfo, Map<String, Object> map)
            throws AppCrash {

        addSessionMap(map, userInfo);
        addMenu(userInfo, map);
    }

    /**
     * Add menu to page
     * 
     * @param WebAppUserSecurityInfo userInfo
     * @param Map<String, Object> map
     * @throws AppCrash
     */
    private void addMenu(WebAppUserSecurityInfo<?> userInfo, Map<String, Object> map) throws AppCrash {

        List<MenuItem> menuList = userInfo.getMenuList();

        if (menuList == null) {
            return;
        }

        WebAppUserSecurityInfo<?> webAppUserInfo = getSpecificUserInfo(userInfo);
        String currentLanguage = webAppUserInfo.getField(WebAppConstants_itf.CURRENT_SELECTED_ISO_LANGUAGE);

        MenuItem currentMenuItem=null;
        List<MenuItem> menuListInPage = new ArrayList<MenuItem>();

        // get current selected menu item
        for (MenuItem item : menuList) {

            if (!item.getIsoLanguage().equals(currentLanguage)) {
                continue;
            }

            if (item.getFunction().equals(getMenuID())) {
                currentMenuItem = item;
            }

            menuListInPage.add(item);

        }

        if (currentMenuItem == null) {
            return;
        }
        
        String path = webAppUserInfo.getSelectedPathTag(currentMenuItem);

        map.put(WebAppConstants_itf.SELECTED_MENU_ITEM, currentMenuItem);
        map.put(WebAppConstants_itf.PAGE_TITLE,currentMenuItem.getPathDescri());
        map.put(WebAppConstants_itf.SELECTED_PATH, path);

        map.put(WebAppUserSecurityInfo.MENU_LIST, menuListInPage);

        String includedMenu = Config.GetInstance().getProperty("Page.DefaultMenuName");
        map.put(WebAppConstants_itf.INCLUDED_MENU, includedMenu);

    }



    /**
     * restituisce la chiave usata come id univoco del menu per tracciare attivit� corrente
     * 
     * @return String attivit� corrente
     */
    protected String getMenuID() {

        return getName();
    }

    /**
     * Valorizza le Map dei parametri del dataset con le map dei dati del template
     * 
     * @param pageName
     * @param req
     * @param templateData
     * @return
     */
    protected Map<String, ?>[] setPageDatasetParam(String pageName, SsbServletRequest req,
            Map<String, ?> templateData) {

        // recupera il numero di dataset presenti nella pagina
        int dsNum = Integer.parseInt(Config.GetInstance().getProperty("Page." + pageName + ".DSNum", "0"));

        // crea un array di hashtable
        @SuppressWarnings("unchecked")
        Map<String, ?>[] dsArray = new HashMap[dsNum];

        // i parametri sono gli stessi per tutti i dataset
        for (int i = 0; i < dsNum; i++) {

            dsArray[i] = templateData;
        }

        return dsArray;

    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        doMethod(req, res, userInfo);
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        doMethod(req, res, userInfo);

    }

    private void doMethod(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

    private void addSessionMap(Map<String, Object> templateData, WebAppUserSecurityInfo<?> userInfo) {

        Map<String, Object> sessionMap = userInfo.getSessionMap();

        templateData.putAll(sessionMap);
    }

    @Override
    protected boolean checkField(SsbServletRequest req) {

        if (checkSession(req)) {
            String tokenString = req.getField(WebAppConstants_itf.SESSION_TOKEN);

            if (Util.IsNotEmpty(tokenString)) {
                return false;
            }
        }

        return true;

    }

    @Override
    protected void checkFieldFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        if (!checkSession(req)) {
            return;
        }

        String tokenString = req.getField(WebAppConstants_itf.SESSION_TOKEN);

        if (Util.IsEmpty(tokenString)) {
            return;
        }

        Map<String, String> dataMap = WebAppUtils.getSessionTokenMap(tokenString);

        String functionField = dataMap.get(Config.GetInstance().getProperty("Servlet.FunctionField"));

        if (Util.IsEmpty(functionField)) {
            return;
        }

        if (!(_applicationSrv instanceof ServletApplication)) {
            return;
        }

        Iterator<Entry<String, String>> iterator = dataMap.entrySet().iterator();
        while (iterator.hasNext()) {
            try {
                Map.Entry<String, String> pair = iterator.next();
                String key = pair.getKey();
                if (Util.IsEmpty(key)) {
                    continue;
                }
                String value = pair.getValue();
                if (Util.IsEmpty(value)) {
                    continue;
                }
                req.setField(key, value);
            } catch (Throwable e) {
                new AppCrash(e);
            }
        }

        req.setAttribute(WebAppConstants_itf.SESSION_TOKEN, "");

        ((ServletApplication) _applicationSrv).invokeProcessGet(functionField, req, res);

    }

    @Override
    protected void checkFieldAutFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        // password scaduta
        Map<String, Object> pageRootData = new HashMap<String, Object>();

        LoginData loginData = new LoginData(req);

        pageRootData.put(TAG_INFO_MESSAGE,
                Config.GetInstance().getProperty(MESSAGE_NEWPASSWORD_INFO) + " " + loginData.getUser());

        pageRootData.put(LoginData.USERNAME, loginData.getUser());
        pageRootData.put(LoginData.OLDPASSWORD, loginData.getPassword());

        _applicationSrv.displayPage(NEW_PASSWORD, pageRootData, res);
    }

    @Override
    protected void authenticationFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        Map<String, Object> pageRootData = new HashMap<String, Object>();
        pageRootData.put(TAG_ERROR_MESSAGE, Config.GetInstance().getProperty(MESSAGE_LOGIN_FAILED));
        _applicationSrv.displayPage(LOGIN_PAGE, pageRootData, res);
    }

    protected String readDBHostName() {

        String applicationPath = _applicationSrv.getRoot() + "WEB-INF";
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

    @Override
    protected boolean displayError(Throwable error, HttpServletRequest req, HttpServletResponse res) throws AppCrash {

        String page = ERROR_PAGE;
        Map<String, Object> pageRootData = new HashMap<String, Object>();
        pageRootData.put(TAG_ERROR_MESSAGE, Config.GetInstance().getProperty(MESSAGES_DEFAULT_ERROR_MESSAGE));
        _applicationSrv.displayPage(page, pageRootData, (SsbServletResponse) res);

        return true;
    }

    /**
     * Questo metodo restituisce l'oggetto di tipo USerSecurityInfo da utilizzare
     * 
     * @return
     */
    protected UserSecurityInfo makeUserSecurityInfo() throws AppCrash {

        String className = Config.GetInstance().getProperty("Servlet.userInfo.class",
                "net.projectsrl.webapp.security.WebAppUserSecurityInfo");

        UserSecurityInfo userInfo;

        try {

            userInfo = (UserSecurityInfo) (Class.forName(className)).newInstance();

        } catch (Throwable ex) {
            AppCrash err = new AppCrash(ex);
            err.logContext(this.getClass().getName(),
                    "Errore durante la creazione dell'oggetto " + className + " - " + ex.getMessage());
            throw (err);
        }

        return userInfo;
    }

    @Override
    protected boolean checkSession(SsbServletRequest req) {

        HttpSession session = req.getSession(false);
        if (session == null) {

            return false;
        }

        // controlla l'oggetto UserSecurityInfo
        if (session.getAttribute("LOGIN") == null) {
            return false;
        }

        return true;
    }

    @Override
    public void prepareForRequest(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

    }

    protected WebAppUserSecurityInfo<?> getSpecificUserInfo(UserSecurityInfo userInfo) {

        return (WebAppUserSecurityInfo<?>) userInfo;
    }

    /**
     * recupera un dato dalla mappa di sessione dell'oggetto UserSecurityInfo
     * 
     * @param UserSecurityInfo userInfo
     * @param String fieldName
     * @return String
     */
    protected String getSessionField(UserSecurityInfo userInfo, String fieldName) {

        WebAppUserSecurityInfo<?> webAppUserInfo = getSpecificUserInfo(userInfo);

        String fieldValue = webAppUserInfo.getField(fieldName);

        if (fieldValue == null) {
            return "";
        }

        return fieldValue;

    }

    protected void setSessionField(SsbServletRequest req, String fieldName, String fieldValue) {

        UserSecurityInfo userInfo = (UserSecurityInfo) req.getSession(false)
                .getAttribute(WebAppConstants_itf.SESSION_USER_SECURITY_INFO);

        if (userInfo == null) {
            return;
        }

        WebAppUserSecurityInfo<?> webAppUserInfo = getSpecificUserInfo(userInfo);

        if (fieldValue == null) {
            return;
        }
        webAppUserInfo.setField(fieldName, fieldValue);
    }

    protected void sendRedirect(SsbServletResponse res, String functionID) throws AppCrash {

        try {
            res.sendRedirect("astro?FUNCTIONID=" + functionID);
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getName().getClass().getName(), "errore nella sendRedirect - FUNCTIONID:" + functionID);
            throw ac;
        }
    }


}
