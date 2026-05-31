/*
  ServletApplication_base.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 09/10/2000

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.servlet.frame;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.project.db.ConnectionPool;
import net.project.db.ExConnectionPool_itf;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.errors.ParamCrash;
import net.project.misc.Clockable_itf;
import net.project.misc.Config;
import net.project.misc.Config_itf;
import net.project.misc.ResourceWatcher;
import net.project.servlet.gui.PageFactory;
import net.project.servlet.gui.Page_itf;
import net.project.servlet.security.AuthenticationProvider;
import net.project.servlet.security.AuthenticationProvider_itf;
import net.project.servlet.security.SecurityProvider_base;
import net.project.servlet.security.SecurityProvider_itf;

/**
 * E' la classe che rappresenta la servlet application. Essa dovrebbe essere in grado di visualizzare la Home page della
 * applicazione con il metodo GET. Il metodo POST gestisce start e stop della app. ed eventualmente di ogni singola
 * funzione, display dello status, display dei servlet partecipanti e della configurazione.
 */
public abstract class ServletApplication_base extends HttpServlet implements ApplicationServices_itf, Clockable_itf {

    protected int                _stato;
    // Init parameter
    private String               _configFile                 = null;         // Nome del file di configurazione
    private String               _configName                 = null;         // Nome della configurazione usata
    private String               _configDefault              = null;         // Nome della configurazione di default
    // Config parameter
    private String               _applName                   = null;         // Nome dell'applicazione
    private String               _functionField              = null;         // Nome del campo che contiene la funzione
                                                                              // richiesta
    private String               _functionDefaultField       = null;         // Nome di default del campo che contiene
                                                                              // la funzione richiesta

    private FunctionList         _functionList;
    private Config_itf           _config;
    private SecurityProvider_itf _securityProvider;

    // Hashtable che fa da bind tra nome della funzione e classe che
    // implementa l'authentication provider per quella funzione
    private Map                  _authenticationProviders    = new HashMap();

    // Hashtable che fa da bind tra nome della funzione e classe che
    // la implementa (contiene coppie del tipo: nome funzione - classe)
    private Map                  _activeFunction             = new HashMap();

    // Nome della pagina in caso di applicazione non attiva
    private static final String  PAGE_APPLICATION_NOT_ACTIVE = "PageError";

    // Chiavi per valorizzazione parametri pagina applicazione non attiva
    private static final String  NAME                        = "Name";
    private static final String  MSG                         = "Msg";

    private long                 _millisecond                = 0;
    private boolean              _traceEnabled               = false;

    protected boolean            _isTickNeeded               = false;
    private boolean              _isTickRunning              = false;

    private Thread               _clockThread                = null;
    private ServletClock         _clock                      = null;

    private ResourceWatcher      _functionListWatcher;

    /**
     * Costruttore.
     */
    public ServletApplication_base() {

        // per eventuale serializzazione/deserializzazione
    }

    /**
     * Metodo da eseguire ad ogni tick del clock. Da ridefinire a cura delle sottoclassi se necessarion
     */
    @Override
    public void tick() {

        // da ridefinire a cura delle sottoclassi se necessarion
    }

    void setTickNeeded() {

        _isTickNeeded = true;
    }

    /**
     * Inizializza la tabella contenente coppie del tipo nome funzione-nome classe.
     *
     * @exception net.project.errors.AppCrash. return void.
     */
    private void createFunctionList() throws AppCrash {

        String fileName = getRoot() + "/" + _config.getProperty("Servlet.FunctionListFile");
        try {
            Logger.GetInstance().log0("Reading functionlist:" + fileName);
            _functionList = new FunctionList(fileName);
            String reload = _config.getProperty("Config.reloadDelay", "-1");
            if (!reload.equalsIgnoreCase("-1")) {
                _functionListWatcher = new ResourceWatcher(fileName, Integer.parseInt(reload));
            }

        } catch (AppCrash ex) {
            ex.logContext("ServletApplication_base", "Errore in createFunctionList. Filename: " + fileName);
            throw (ex);
        }
    }

    /**
     * Istanzia le classi opportune per SecurityProvider.
     *
     * @exception net.project.errors.AppCrash.
     * @return void
     */
    private void createProviders() throws AppCrash {

        try {
            _securityProvider = SecurityProvider_base.MakeSecurityProvider(_configName);
        } catch (AppCrash ex) {
            ex.logContext("ServletApplication_base", "Errore in createProviders");
            throw (ex);
        }
    }

    /**
     * La doget verifica che l'applicazione sia attiva (Stato). Se tutto OK richiama la processGet().
     *
     * @param req HttpServletRequest che incapsula la richiesta al servlet.
     * @param res HttpServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     * @exception java.lang.Throwable.
     * @return void
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) {

        // creazione dei wrapper;
        // i wrapper sono creati con metodi protected
        // per permetterne l'override
        SsbServletResponse ssbRes = createSsbServletResponse(res);
        try {
            SsbServletRequest ssbReq = createSsbServletRequest(req);
            ssbRes.setRequest(ssbReq);
            if (_functionListWatcher != null && _functionListWatcher.hasBeenModified()) {
                _activeFunction.clear();
            }
            // Controllo applicazione attiva
            if (getStatus() == 0) {
                applicationNotActive(ssbReq, ssbRes);
                return;
            }

            if (_isTickNeeded && !_isTickRunning) {
                gestisciTick();
            }

            Function_itf function = getFunction(ssbReq);
            if (function == null) {
                processGet(ssbReq, ssbRes);
            } else {
                function.doGet(ssbReq, ssbRes);
            }
        } catch (AppCrash ap) {
            invokeDisplayError(ap, req, ssbRes);
        } catch (Throwable err) {
            err.printStackTrace();
            new AppCrash(err);
            invokeDisplayError(err, req, ssbRes);
        } finally {
            try {
                ssbRes.flushBuffer();
            } catch (Throwable t) {
                new AppCrash(t);
            }
            try {
                Logger.GetInstance().dumpInfo();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            try {
                Logger.GetInstance().resetInfo();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            try {
                freeDBConnections();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            try {
                Logger.GetInstance().flush();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            try {
                Logger.GetInstance(getConfigName()).flush();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * @param req HttpServletRequest che incapsula la richiesta al servlet.
     * @param res HttpServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     * @exception java.lang.Throwable.
     * @return void
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) {

        // creazione dei wrapper;
        // i wrapper sono creati con metodi protected
        // per permetterne l'override
        SsbServletResponse ssbRes = createSsbServletResponse(res);
        try {
            SsbServletRequest ssbReq = createSsbServletRequest(req);
            ssbRes.setRequest(ssbReq);
            if (_functionListWatcher != null && _functionListWatcher.hasBeenModified()) {
                _activeFunction.clear();
            }

            // Controllo applicazione attiva
            if (getStatus() == 0) {
                applicationNotActive(ssbReq, ssbRes);
                return;
            }

            if (_isTickNeeded && !_isTickRunning) {
                gestisciTick();
            }

            Function_itf function = getFunction(ssbReq);
            if (function == null) {
                processPost(ssbReq, ssbRes);
            } else {
                function.doPost(ssbReq, ssbRes);
            }

        } catch (AppCrash ap) {
            invokeDisplayError(ap, req, ssbRes);
        } catch (Throwable err) {
            err.printStackTrace();
            new AppCrash(err);
            invokeDisplayError(err, req, ssbRes);
        } finally {
            try {
                ssbRes.flushBuffer();
            } catch (Throwable t) {
                new AppCrash(t);
            }
            try {
                Logger.GetInstance().dumpInfo();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            try {
                Logger.GetInstance().resetInfo();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            try {
                freeDBConnections();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            try {
                Logger.GetInstance().flush();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            try {
                Logger.GetInstance(getConfigName()).flush();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

    }

    /**
     * Crea la SsbServletRequest.
     * 
     * @param javax.servlet.http.HttpServletRequest l'oggetto richiesta attorno al quale creare la SsbServletRequest
     * @return net.project.frame.SsbServletRequest l'oggetto creato.
     * @exception net.project.errors.AppCrash
     */
    protected SsbServletRequest createSsbServletRequest(HttpServletRequest req) throws AppCrash {

        return new SsbServletRequest(req);
    }

    /**
     * Crea la SsbServletRequest. Questo metodo non lancia alcun crash perché non è accettabile che la costruzione
     * dell'oggetto risposta fallisca.
     * 
     * @param javax.servlet.http.HttpServletResponse l'oggetto richiesta attorno al quale creare la SsbServletRequest
     * @return net.project.frame.SsbServletResponse l'oggetto creato.
     */
    protected SsbServletResponse createSsbServletResponse(HttpServletResponse req) {

        return new SsbServletResponse(req);
    }

    /**
     * Visualizza la pagina in caso di applicazione non attiva.
     *
     * @param ssbReq SsbServletRequest che incapsula la richiesta al servlet.
     * @param ssbRes SsbServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     * @return void
     */
    protected void applicationNotActive(SsbServletRequest ssbReq, SsbServletResponse ssbRes) throws AppCrash {

        Map parameters = new HashMap();
        parameters.put(NAME, _applName);
        parameters.put(MSG, "Applicazione non attiva");
        displayPage(PAGE_APPLICATION_NOT_ACTIVE, parameters, ssbRes);
        Logger.GetInstance().log0("applicationNotActive");
    }

    /**
     * Esegue l'operazione get http.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     * @return void
     */
    public abstract void processGet(SsbServletRequest req, SsbServletResponse res) throws AppCrash;

    /**
     * Questo metodo provvede ad eseguire i comandi start, stop per l'intera applicazione. Vengono controllati la
     * password passata e la provenienza del comando come indirizzo IP. La password viene letta dal file di config nella
     * proprieta' <code>Servlet.passphrase</code>; deve essere di almeno 10 caratteri. <BR>
     * Vengon letti dalla request i campi:
     * <P>
     * APPLICATIONCOMMAND che contiene il comando: stop o restart
     * <P>
     * APPLICATIONPASSWORD che contiene la password da confrontare con quella letta da config.
     * <P>
     * <BR>
     * Se l'esecuzione di un comando viene accettata viene impostato il campo APPLICATIONCOMMANDEXECUTED della request
     * con il valore del comando eseguito. <br>
     * 
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     * @return void
     */
    public void processPost(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        // Recupera il comando
        String command = req.getField("APPLICATIONCOMMAND");
        String pwd = req.getField("APPLICATIONPASSWORD");

        if (command.equals("") || pwd.equals("")) return;

        String passphrase = Config.GetInstance().getProperty("Servlet.passphrase");

        // La passphare deve esistere ed essere piu' lunga di 10 caratteri
        if (passphrase == null || passphrase.length() <= 10) return;

        // IP address deve essere fra quelli permessi
        if (!isIPAddressAllowed(req)) return;

        // La password inserita deve essere uguale alla passphrase in configurazione
        if (!passphrase.equals(pwd)) return;

        String message = "Nessun comando eseguito!";
        if (command.equals("stop")) {
            stopApplication();
            message = "Application stopped ";
            Logger.GetInstance().log0("Application stopped ");
            req.setField("APPLICATIONCOMMANDEXECUTED", "stop");
        } else if (command.equals("restart")) {
            restartApplication();
            message = "Application restarted ";
            Logger.GetInstance().log0("Application restarted ");
            req.setField("APPLICATIONCOMMANDEXECUTED", "restart");
        }

        try {
            PrintWriter pw = res.getWriter();
            pw.write("-----------------------------------------");
            pw.write("Risultato: " + message);
            pw.write("-----------------------------------------");
        } catch (IOException e) {
            // Se perdo il log non ha importanza
        }

    }

    /**
     * Istanzia la classe Config.
     *
     * @exception net.project.errors.AppCrash.
     * @return void
     */
    private void initializeConfig(ServletConfig config) throws AppCrash {

        _configFile = config.getInitParameter("configFile");
        _configName = config.getInitParameter("configName");
        _configDefault = config.getInitParameter("configDefault");

        if (_configFile == null || _configName == null || _configDefault == null) {
            throw new AppCrash(
                    "ServletApplication_base initializeConfig: nome file configurazione o nome configurazione mancante:"
                            + "ConfigFile: " + _configFile + "ConfigName:" + _configName + "-ConfigDefault:"
                            + _configDefault);
        }

        String path = getRoot();

        try {
            Config.InitInstance(_configName, path + "/" + _configFile);
            _config = Config.GetInstance(_configName);
            Config.InitInstance(path + "/" + _configDefault);

            // Imposta il valore della root della applicazione
            Config.GetInstance().setProperty("Application.root", path);
            Config.GetInstance(_configName).setProperty("Application.root", path);
        } catch (IOException ioe) {
            AppCrash ap = new AppCrash(ioe);
            ap.logContext("ServletApplication_base", "I/O error in Config.InitInstance");
            ap.logContext("ServletApplication_base", "ConfigFile: " + _configFile + "ConfigName:" + _configName
                    + "-ConfigDefault:" + _configDefault + " Root:" + path);
            throw (ap);
        }

        // alternativeLoggerConfiguration(config);
        // alternativeDBConfiguration(config);

    }

    private void alternativeDBConfiguration(ServletConfig config) throws AppCrash {

        // DB.ConnectionPoolClass=net.project.db.PjTimedConnectionPool
        // DB.ConnectionClass=net.project.db.TimedDBConnection
        // DB.ConnectionTimeout=20000
        // DB.SafeDBConnection=true
        //
        // DB.ConnectionURL=
        // DB.ConnectionUser=aquarius
        // DB.ConnectionPassword=12aquarius34
        //
        // DB.JDBCDriver=net.sourceforge.jtds.jdbc.Driver
        // DB.ConnectionNum=100
        //
        // DBEntity.NomeDB=aq2_CrossOverGroupSrl

        String path = getRoot();

        try {
            String altConfigFile = config.getInitParameter("dbConfigFile");
            altConfigFile = path + "/" + altConfigFile;
            Config.InitInstance("DB", altConfigFile);
            Config_itf altConfig = Config.GetInstance("DB");

            String property;

            property = "";
            property = altConfig.getProperty("DB.ConnectionPoolClass");
            if (property != null) {
                Config.GetInstance().setProperty("DB.ConnectionPoolClass", property);
            }

            property = "";
            property = altConfig.getProperty("DB.ConnectionClass");
            if (property != null) {
                Config.GetInstance().setProperty("DB.ConnectionClass", property);
            }

            property = "";
            property = altConfig.getProperty("DB.ConnectionTimeout");
            if (property != null) {
                Config.GetInstance().setProperty("DB.ConnectionTimeout", property);
            }

            property = "";
            property = altConfig.getProperty("DB.SafeDBConnection");
            if (property != null) {
                Config.GetInstance().setProperty("DB.SafeDBConnection", property);
            }

            property = "";
            property = altConfig.getProperty("DB.ConnectionURL");
            if (property != null) {
                Config.GetInstance().setProperty("DB.ConnectionURL", property);
            }

            property = "";
            property = altConfig.getProperty("DB.ConnectionUser");
            if (property != null) {
                Config.GetInstance().setProperty("DB.ConnectionUser", property);
            }

            property = "";
            property = altConfig.getProperty("DB.ConnectionPassword");
            if (property != null) {
                Config.GetInstance().setProperty("DB.ConnectionPassword", property);
            }

            property = "";
            property = altConfig.getProperty("DB.JDBCDriver");
            if (property != null) {
                Config.GetInstance().setProperty("DB.JDBCDriver", property);
            }

            property = "";
            property = altConfig.getProperty("DB.ConnectionNum");
            if (property != null) {
                Config.GetInstance().setProperty("DB.ConnectionNum", property);
            }

            property = "";
            property = altConfig.getProperty("DBEntity.NomeDB");
            if (property != null) {
                Config.GetInstance().setProperty("DBEntity.NomeDB", property);
            }

        } catch (IOException ioe) {
            AppCrash ap = new AppCrash(ioe);
            ap.logContext("ServletApplication_base", "I/O error in Config.InitInstance");
            throw (ap);
        }

    }

    private void alternativeLoggerConfiguration(ServletConfig config) throws AppCrash {

        String path = getRoot();

        try {
            String logConfigFile = config.getInitParameter("logConfigFile");
            logConfigFile = path + "/" + logConfigFile;
            Config.InitInstance("LOG", logConfigFile);
            Config_itf logConfig = Config.GetInstance("LOG");

            String propertyLoggerClass = logConfig.getProperty("LoggerClass");
            String propertyCrashDir = logConfig.getProperty("Logger.CrashDir");
            String propertyDebugFile = logConfig.getProperty("Logger.DebugFile");
            String propertyApplicationLog = logConfig.getProperty("Logger.ApplicationLog");

            Config.GetInstance().setProperty("LoggerClass", propertyLoggerClass);
            Config.GetInstance().setProperty("Logger.CrashDir", propertyCrashDir);
            Config.GetInstance().setProperty("Logger.DebugFile", propertyDebugFile);
            Config.GetInstance().setProperty("Logger.ApplicationLog", propertyApplicationLog);

        } catch (IOException ioe) {
            AppCrash ap = new AppCrash(ioe);
            ap.logContext("ServletApplication_base", "I/O error in Config.InitInstance");
            throw (ap);
        }

    }

    /**
     * Inizializza i parametri di configurazione propri dell'applicazione.
     *
     * @exception net.project.errors.AppCrash.
     * @return void
     */
    private void initializeServletApplication(ServletConfig config) throws AppCrash {

        try {
            // Recupera dal file di configurazione i parametri necessari
            _applName = _config.getProperty("Servlet.ApplicationName");
            _functionField = _config.getProperty("Servlet.FunctionField");
            _functionDefaultField = _config.getProperty("Servlet.FunctionDefaultField");

            String te = _config.getProperty("Servlet.Trace", "true");
            _traceEnabled = (te.equalsIgnoreCase("true") ? true : false);

        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("ServletApplication_base", "errore in initializeServletApplication");
            throw (ac);
        }
    }

    @Override
    public void init(ServletConfig config) throws UnavailableException, ServletException {

        super.init(config);

        String loadOnDemand = null;
        try {
            // Istanzia la classe Config
            initializeConfig(config);

            // Inizializza i parametri dell'applicazione
            initializeServletApplication(config);

            loadOnDemand = _config.getProperty("Servlet.LoadOnDemand");
            ErrDetector.GetInstance().param(loadOnDemand);

            createProviders();
            createFunctionList();
            Logger.GetInstance().log0("Load on demand:" + loadOnDemand);

            if (loadOnDemand.equals("N")) {
                loadAllFunctions();
            }
            _stato = 1;

            String time = _config.getProperty("AutoReload.Check");
            if (time != null) {
                _millisecond = Long.parseLong(time);
                _clock = new ServletClock(_millisecond, this);
                _clockThread = new Thread(_clock);
                _clockThread.setDaemon(true);
                _clockThread.start();
                _isTickNeeded = false;
            }

            Logger.GetInstance().flush();

        } catch (Exception e) {
            e.printStackTrace();
            throw new UnavailableException("Errore in init(ServletConfig config) CF=" + _configFile + " CN="
                    + _configName + " LoadOnDemand:" + loadOnDemand);
        }
    }

    /**
     * Istanzia tutte le funzioni dell'applicazioni.
     *
     * @exception net.project.errors.AppCrash.
     */
    private void loadAllFunctions() throws AppCrash {

        String functionName = null;
        String functionID = null;
        try {
            Logger.GetInstance().log0("Loading all functions");
            Enumeration list = _functionList.getFunctionList(); // lista delle funzioni dell'applicazione
            while (list.hasMoreElements()) {
                functionName = (String) list.nextElement();
                functionID = _functionList.getFunctionID(functionName);
                activateFunction(functionName, functionID);
            }
        } catch (AppCrash ex) {
            ex.logContext("ServletApplication_base", "Errore in loadAllFunctions");
            throw (ex);
        } catch (Throwable e) {
            AppCrash err = new AppCrash(e);
            err.logContext("ServletApplication_base", "Errore in loadAllFunctions");
            throw (err);
        }
    }

    /**
     * Attiva una funzione.
     *
     * @param functionName java.lang.String Il nome della funzione.
     * @param functionID java.lang.String L'id della funzione.
     */
    private void activateFunction(String functionName, String functionID) throws AppCrash {

        String className = null;
        try {
            if (_activeFunction.containsKey(functionName)) {
                return; // Funzione già istanziata
            }
            className = _functionList.findClass(functionName);
            if (className == null) {
                return; // Funzione inesistente
            }

            Logger.GetInstance().log0(
                    "Creating Function: " + functionName + " ID: " + functionID + " Classe: " + className);

            // istanzio la classe per la funzione
            Class tempClasse = Class.forName(className);
            Constructor procBaseConstr = tempClasse.getConstructor(new Class[] { ApplicationServices_itf.class,
                    String.class, String.class });
            // Istanzio la classe tramite il costruttore ottenuto
            Function_itf classObject = (Function_itf) procBaseConstr.newInstance(new Object[] { this, functionID,
                    functionName });
            Logger.GetInstance().log0(
                    "Starting Function: " + functionName + " ID: " + functionID + " Classe: " + className);
            classObject.start();
            // Inserisco la coppia nome funzione-classe nella hashtable
            _activeFunction.put(functionName, classObject);
            Logger.GetInstance().log0(
                    "Activated Function: " + functionName + " ID: " + functionID + " Classe: " + className);

        } catch (Throwable e) {
            AppCrash err = new AppCrash(e);
            err.logContext("ServletApplication_base", "Errore in activateFunction: " + functionName + " ID: "
                    + functionID + " Classe: " + className);
            throw (err);
        }
    }

    /**
     * Ritorna la funzione richiesta.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet. exception net.project.errors.AppCrash.
     * @return net.project.servlet.frame.Function_itf. La funzione richiesta.
     */

    protected Function_itf getFunction(SsbServletRequest req) throws AppCrash {

        String functionName = null;
        String functionID = null;
        try {
            // recupera il nome della funzione richiesta
            if (_functionField == null) {
                ErrDetector.GetInstance().param(_functionDefaultField);
                functionName = req.getParameter(_functionDefaultField);
            } else {
                functionName = req.getParameter(_functionField);
            }
            if (functionName == null) {
                return null;
            }
            // recupera l'id della funzione richiesta
            functionID = _functionList.getFunctionID(functionName);
            // controlla l'esistenza della funzione
            if (functionID == null) {
                return null;
            }
            // attiva la funzione
            activateFunction(functionName, functionID);
            return (Function_itf) _activeFunction.get(functionName);
        } catch (ParamCrash pc) {
            pc.logContext("ServletApplication_base", "Parametro mancante: function field = " + _functionField
                    + ", function default field = " + _functionDefaultField);
            throw (pc);
        } catch (AppCrash ex) {
            ex.logContext("ServletApplication_base", "Errore nella getFunction");
            throw (ex);
        }
    }

    /**
     * Ritorna lo stato dell'applicazione.
     */
    @Override
    public int getStatus() {

        return _stato;
    }

    /**
     * Ritorna il nome della configurazione.
     *
     * @return java.lang.String Il nome della configurazione.
     */
    @Override
    public String getConfigName() {

        return _configName;
    }

    /**
     * Ritorna un'istanza della classe SecurityProvider.
     *
     * @return net.project.servlet.security.SecurityProvider_itf.
     */
    @Override
    public SecurityProvider_itf getSecurityProvider() {

        return _securityProvider;
    }

    /**
     * Ritorna un'istanza della classe AuthenticationProvider.
     *
     * @param functionName java.lang.String Il nome della funzione.
     * @exception net.project.errors.AppCrash.
     * @return net.project.servlet.security.AuthenticationProvider_itf.
     */
    @Override
    public AuthenticationProvider_itf getAuthenticationProvider(String functionName) throws AppCrash {

        // ricava l'authentication provider della funzione dalla hashtable degli authentication provider
        AuthenticationProvider_itf authenticationProvider = (AuthenticationProvider_itf) _authenticationProviders
                .get(functionName);
        if (authenticationProvider == null) {
            try {
                // se l'authentication provider richiesto non è presente nell'hashtable, lo crea ...
                authenticationProvider = AuthenticationProvider.MakeAuthenticationProvider(_configName, functionName);
                // ... e lo inserisce nella hashtable
                _authenticationProviders.put(functionName, authenticationProvider);
            } catch (AppCrash ex) {
                ex.logContext("ServletApplication_base", "Errore in getAuthenticationProvider. FunctionName: "
                        + functionName);
                throw (ex);
            }
        }
        return authenticationProvider;
    }

    /**
     * Visualizza una pagina senza PageRootData e DataSourceParam.
     *
     * @param pageName java.lang.String Il nome della pagina da visualizzare.
     * @param resp net.project.servlet.frame.SsbServletResponse La response http.
     * @return void
     */
    @Override
    public void displayPage(String pageName, SsbServletResponse resp) throws AppCrash {

        try {
            setSsbResponseFlags(resp, pageName);
            Map data = new HashMap();
            addCustomDataToTemplate(data, resp.getRequest(), resp);
            PageFactory pf = PageFactory.getInstance();
            Page_itf page = pf.makePage(_configName, pageName);
            page.setPageRootData(data);
            page.display(resp.getWriter());
        } catch (IOException ioe) {
            AppCrash ap = new AppCrash(ioe);
            ap.logContext("ServletApplication_base", "Errore nella displayPage");
            throw (ap);
        } catch (AppCrash ex) {
            ex.logContext("ServletApplication_base", "Errore nella displayPage");
            throw (ex);
        }
    }

    /**
     * Visualizza una pagina con soli PageRootData.
     *
     * @param pageName java.lang.String Il nome della pagina da visualizzare.
     * @param pageRootData java.util.Hashtable I parametri che valorizzano la pagina.
     * @param resp net.project.servlet.frame.SsbServletResponse La response http.
     * @return void
     */
    @Override
    public void displayPage(String pageName, Map pageRootData, SsbServletResponse resp) throws AppCrash {

        try {
            setSsbResponseFlags(resp, pageName);
            addCustomDataToTemplate(pageRootData, resp.getRequest(), resp);
            PageFactory pf = PageFactory.getInstance();
            Page_itf page = pf.makePage(_configName, pageName);
            page.setPageRootData(pageRootData);
            page.display(resp.getWriter());
        } catch (IOException ioe) {
            AppCrash ap = new AppCrash(ioe);
            ap.logContext("ServletApplication_base", "Errore nella displayPage");
            throw (ap);
        } catch (AppCrash ex) {
            ex.logContext("ServletApplication_base", "Errore nella displayPage");
            throw (ex);
        }
    }

    /**
     * Visualizza una pagina con soli DataSourceParam.
     *
     * @param pageName java.lang.String Il nome della pagina da visualizzare.
     * @param dataSourceParam Array di hashtable contenenti i parametri per i singoli dataset. L'array deve avere tanti
     *            campi quanti sono i diversi dataset presenti nella pagina. L'indice dell'array corrisponde al numero
     *            del dataset. Se uno dei dataset non prevede parametri, la corrispondente posizione nell'array dovrà
     *            essere a null.
     * @param resp net.project.servlet.frame.SsbServletResponse La response http.
     * @return void
     */
    @Override
    public void displayPage(String pageName, Map dataSourceParam[], SsbServletResponse resp) throws AppCrash {

        try {
            setSsbResponseFlags(resp, pageName);
            Map data = new HashMap();
            addCustomDataToTemplate(data, resp.getRequest(), resp);
            PageFactory pf = PageFactory.getInstance();
            Page_itf page = pf.makePage(_configName, pageName);
            page.setPageRootData(data);
            for (int i = 0; i < dataSourceParam.length; i++) {
                if (dataSourceParam[i] != null) {
                    page.setDataSourceParam(dataSourceParam[i], i);
                }
            }
            page.display(resp.getWriter());
        } catch (IOException ioe) {
            AppCrash ap = new AppCrash(ioe);
            ap.logContext("ServletApplication_base", "Errore nella displayPage");
            throw (ap);
        } catch (AppCrash ex) {
            ex.logContext("ServletApplication_base", "Errore nella displayPage");
            throw (ex);
        }
    }

    /**
     * Visualizza una pagina con PageRootData e DataSourceParam.
     *
     * @param pageName java.lang.String Il nome della pagina da visualizzare.
     * @param pageRootData java.util.Hashtable I parametri che valorizzano la pagina.
     * @param dataSourceParam Array di hashtable contenenti i parametri per i singoli dataset. L'array deve avere tanti
     *            campi quanti sono i diversi dataset presenti nella pagina. L'indice dell'array corrisponde al numero
     *            del dataset. Se uno dei dataset non prevede parametri, la corrispondente posizione nell'array dovrà
     *            essere a null.
     * @param resp net.project.servlet.frame.SsbServletResponse La response http.
     * @return void
     */
    @Override
    public void displayPage(String pageName, Map pageRootData, Map dataSourceParam[], SsbServletResponse resp)
            throws AppCrash {

        try {
            setSsbResponseFlags(resp, pageName);
            addCustomDataToTemplate(pageRootData, resp.getRequest(), resp);

            PageFactory pf = PageFactory.getInstance();
            Page_itf page = pf.makePage(_configName, pageName);
            page.setPageRootData(pageRootData);
            for (int i = 0; i < dataSourceParam.length; i++) {
                if (dataSourceParam[i] != null) {
                    page.setDataSourceParam(dataSourceParam[i], i);
                }
            }
            page.display(resp.getWriter());
        } catch (IOException ioe) {
            AppCrash ap = new AppCrash(ioe);
            ap.logContext("ServletApplication_base", "Errore nella displayPage");
            throw (ap);
        } catch (AppCrash ex) {
            ex.logContext("ServletApplication_base", "Errore nella displayPage");
            throw (ex);
        }
    }

    /**
     * Questo metodo viene richiamato dalla doGet e doPost nel caso in cui il controllo sulla presenza della sessione
     * sia fallito. Le servlet application devono ridefire quel metodo in modo da ottenere il comportameno voluto in
     * caso di sessione scaduta. Di default il metodo esegue la displayPage() della pagina 'SessioneScaduta' se esiste.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     * @return void
     */
    @Override
    public void sessionExpired(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        if (Config.GetInstance().getProperty("Page.SessioneScaduta.Template.Name") == null) return;

        displayPage("SessioneScaduta", res);
    }

    /**
     * Metodo per richiamare la visualizzazione del messaggio di errore in caso di eccezioni nel framework.
     */
    protected void invokeDisplayError(Throwable error, HttpServletRequest req, HttpServletResponse res) {

        try {
            displayError(error, req, res);
        } catch (Throwable err) {
            new AppCrash(err);
        }
    }

    /**
     * Metodo per la gestione della visualizzazione del messaggio di errore in caso di eccezioni non gestite dalle
     * implementazioni o generate nel framework stesso. Viene invocato dalla doPost e doGet. L'implementazione di
     * default non mostra nulla.
     */
    protected void displayError(Throwable error, HttpServletRequest req, HttpServletResponse res) throws AppCrash {

        return;
    }

    protected void freeDBConnections() {

        try {
            // Connesioni file config default
            if (isConnectionPoolSafeDecorated("")) {
                ExConnectionPool_itf pool = (ExConnectionPool_itf) ConnectionPool.GetInstance();
                pool.freeAllThreadConnections();
            }

            // Le Connesioni tratte dall'eventuale pool con file config con nome applicazione
            // vengono ignorate perche' tale pool in pratica non viene usato e perche' potrebbero
            // esserci altre config che hanno istaziato loro pool. Da rivedere nel complesso

        } catch (Throwable ac) {
            Logger.GetInstance().log0("Errore nella freeDBConnection");
        }

    }

    private boolean isConnectionPoolSafeDecorated(String config) {

        String safe = Config.GetInstance(config).getProperty("DB.SafeDBConnection", "false").trim();

        return (safe.equalsIgnoreCase("true") ? true : false);

    }

    private void stopApplication() throws AppCrash {

        Iterator iter = _functionList.getMainFunctions();

        while (iter.hasNext()) {
            String func = (String) iter.next();
            stopFunction(func);
        }
    }

    private void restartApplication() throws AppCrash {

        Iterator iter = _functionList.getMainFunctions();

        while (iter.hasNext()) {
            String func = (String) iter.next();
            restartFunction(func);
        }

    }

    private void stopFunction(String functionName) throws AppCrash {

        try {
            Function_itf functionObject = (Function_itf) _activeFunction.get(functionName);
            functionObject.stop();
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("ServletApplication_base", "Stopping: " + functionName);
            throw ac;
        }
    }

    private void restartFunction(String functionName) throws AppCrash {

        try {
            Function_itf functionObject = (Function_itf) _activeFunction.get(functionName);
            functionObject.start();
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("ServletApplication_base", "Restarting: " + functionName);
            throw ac;
        }
    }

    /**
     * Questo metodo verifica se la richiesta proviene da un indirizzo IP autorizzato ad acedere alla funzione. Viene
     * letto il parametro di configurazione <code>Servlet.controllIP</code>. Questo contiene un elenco di IP separati da
     * punto e virgola ";". Gli IP possono essere completi o parziali. Un IP e'abilitato se inizia con uno degli IP
     * elencati
     *
     * @return boolean se l'accesso è consentito
     */
    private boolean isIPAddressAllowed(SsbServletRequest req) throws AppCrash {

        String allowedIP = Config.GetInstance().getProperty("Servlet.controllIP");
        ErrDetector.GetInstance().invariant(allowedIP != null,
                "manca la propietà 'monitor.allowedIP' nel file di configurazione");

        String ip = req.getRemoteAddr();
        Logger.GetInstance().log3(
                "Sto eseguendo il monitoring '"
                        + req.getField(Config.GetInstance().getProperty("Servlet.FunctionField", "PAGE"))
                        + "'. La richiesta arriva dal Host:'" + ip + "'. IP autorizzati:'" + allowedIP + "'");

        StringTokenizer ipList = new StringTokenizer(allowedIP, ";");

        while (ipList.hasMoreElements()) {
            String ipElement = ipList.nextToken();

            if (ip.startsWith(ipElement)) return true;
        }
        return false;
    }

    /**
     * Questo metodo ritorna la root nel file system della macchina dove e' avvenuto il deployment della servlet
     * application (modulo war che la contiene)
     *
     * @return String root del deployment
     */
    @Override
    public String getRoot() {

        String path = getServletContext().getRealPath("/");
        return path;
    }

    /**
     * Ritorna true se il meccanismo di trace della servlet application e' abilitato
     * 
     * @return boolean true se trace abilitata
     */
    @Override
    public boolean isTraceEnabled() {

        return _traceEnabled;
    }

    /**
     * Questo metodo viene richiamato prima della display della pagina per permettere di impostare, pagina per pagina, i
     * flag dell'oggetto SsbServletResponse usato per emettere la pagina.
     * <p>
     * <p>
     * 
     * @param res SsbServletResponse usato per emettere la pagina
     * @param pageName il nome della pagina che sis sta generando
     */
    protected void setSsbResponseFlags(SsbServletResponse res, String pageName) {

        String logEnabled = _config.getProperty("Page." + pageName + ".log", "true");

        if (logEnabled.equalsIgnoreCase("false")) {
            res.setLogging(false);
        }
    }

    /**
     * Questo metodo viene richiamata prima della display della pagina per permettere la personalizzazione 'a basso
     * livello' del contenuto della Map con i dati che viene passata al template engine.
     * <p>
     * Di suo aggiunge gli oggetti Request,Response, ServletContext e Servlet
     * <p>
     * 
     * @param data Map con la struttura dati passata alla displayPage
     * @param req SsbServletRequest la request
     * @param res SsbServletResponse la response
     * @return
     */
    protected void addCustomDataToTemplate(Map data, SsbServletRequest req, SsbServletResponse res) {

        ServletContext servContext = getServletContext();

        // Per compatibilita' con applicazioni esisteni inserisco la req solo se non null:
        // in condizioni di errore le applicazioni esistenti che ridefiniscono
        // displayError(Throwable, HttpServletRequest, HttpServletResponse) sicuramente non impostano
        // la req nella res percio' la req qua' non e' disponibile.
        if (req != null) {
            data.put(".ServletApplication_base.SSBRequest", req);
        }
        data.put(".ServletApplication_base.SSBResponse", res);
        data.put(".ServletApplication_base.SSBServlet", this);
        data.put(".ServletApplication_base.SSBContext", servContext);

    }

    private synchronized void gestisciTick() {

        try {
            if (_isTickNeeded && !_isTickRunning) {
                _isTickNeeded = false;
                _isTickRunning = true;
                tick();
            }
        } catch (Throwable t) {
            new AppCrash(t);
        } finally {
            _isTickRunning = false;
        }

    }

    /**
     * Questo metodo
     *
     *
     * @see javax.servlet.GenericServlet#destroy()
     */
    @Override
    public void destroy() {

        if (_clockThread != null && _clock != null) {
            _clock.ferma();
        }

        super.destroy();
    }
}
