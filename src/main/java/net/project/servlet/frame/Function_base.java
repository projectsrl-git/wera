/*
  Function_base.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 05/10/2000

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.servlet.frame;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.project.errors.AppCrash;
import net.project.errors.DAOAudit;
import net.project.errors.DAOAuditUser;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.mess.verify.Verifier;
import net.project.mess.verify.datatype.FieldVerifier_itf;
import net.project.misc.Config;
import net.project.misc.Converter;
import net.project.servlet.security.AuthenticationProvider_itf;
import net.project.servlet.security.UserSecurityInfo;

/**
 * Questa classe è la rappresentazione astratta di una funzione. Ogni function di una servlet application deve estendere
 * questa classe ed implementare i metodi astratti. Il metodo mostra() viene richiamato dalle GET; il metodo elabora
 * dalle POST.
 * <p>
 * Prima di richiamare il metodo mostra() o elabora() della sua sottoclasse questa classe esegue le seguenti operazioni:
 * <p>
 * <ul>
 * <li>Richiama il metodo prepareForRequest(req,res)</li>
 * <li>Controlla checkAuthentication() se necessaria</li>
 * <li>Controlla checkStatusPermission() se necessaria</li>
 * <li>Controlla checkRequestParameter()</li>
 * </ul>
 * <p>
 * Se checkAuthentication() fallisce viene richiamato il metodo della sottoclasse checkAuthenticationFailed() o
 * checkFieldAutFailed() a seconda del tipo di errore incontrato.
 * <p>
 * Se checkStatusPermission fallisce viene richiamato il metodo permissionFailed() che puo' essere ridefinito
 * opportunamente dalle sottoclassi.
 * <p>
 * Se checkRequestParameter() fallisce viene richiamato il metodo checkFieldFailed() della sottoclasse.
 * <p>
 *
 */
public abstract class Function_base implements Function_itf {

    private int                          _stato;
    private boolean                      _firewall           = false;
    private boolean                      _firewallTraining   = false;
    private boolean                      _serversideCheck    = false;
    private boolean                      _logDefaultFieldUse = false;

    public final ApplicationServices_itf _applicationSrv;
    public final String                  _functionID;
    public final String                  _functionName;

    // Nome della pagina in caso errore
    private static final String          PAGE_ERROR          = "PageError";

    // Chiavi per valorizzazione parametri pagina errore
    private static final String          NAME                = "Name";
    private static final String          MSG                 = "Msg";

    // Valori possibili: true warning
    private boolean                      _strictVerify       = false;

    /**
     * Costruttore.
     */
    public Function_base() {

        _applicationSrv = null;
        _functionID = null;
        _functionName = null;

    }

    /**
     * Costruttore.
     *
     * @param applServices net.project.servlet.frame.ApplicationServices_itf.
     * @param functionID java.lang.String L'ID della funzione.
     */
    public Function_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        _applicationSrv = applServices;
        _functionID = functionID;
        _functionName = functionName;
    }

    /**
     * Viene richiamato dall'operazione get http.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @param userInfo net.project.servlet.security.UserSecurityInfo.
     * @exception net.project.errors.AppCrash.
     * @return void
     */
    public abstract void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo)
            throws AppCrash;

    /**
     * Viene richiamato dall'operazione post http.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @param userInfo net.project.servlet.security.UserSecurityInfo.
     * @exception net.project.errors.AppCrash.
     * @return void
     */
    public abstract void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo)
            throws AppCrash;

    /**
     * La doget base verifica che l'applicazione e/o la funzione siano attive (Stato), che l'utente possa accedere alla
     * funzione, la presenza e la validità dei campi della richiesta e l'autenticazione. Se tutto OK richiama il metodo
     * mostra().
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     * @exception java.lang.Throwable.
     * @return void
     */
    @Override
    public final void doGet(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        long start = System.currentTimeMillis();
        try {
            UserSecurityInfo userInfo = makeUserSecurityInfo();

            prepareForRequest(req, res);

            if (checkSession(req) == false) {
                _applicationSrv.sessionExpired(req, res);
                return;
            }

            // Se i controlli non sono lato server applico le regole firewall prima di tutti
            // i check applicativi dei parametri
            if (_serversideCheck == false) {
                if (applyRequestFirewall(req) == false && _firewallTraining == false) {
                    checkFieldFailed(req, res);
                    return;
                }
            }

            if (checkAuthentication(req, res, userInfo) && checkStatusPermission(req, res, userInfo)
                    && checkRequestParameter(req, res)) {
                UserSecurityInfo sessionUi = null;
                HttpSession session = req.getSession(false);
                if (session != null) {
                    sessionUi = (UserSecurityInfo) session.getAttribute("LOGIN");
                }
                if (sessionUi == null) {
                    sessionUi = userInfo;
                }
                if (sessionUi.getUserId() == null) {
                    Logger.GetInstance().addInfo(DAOAudit.INFOUSERKEY, new DAOAuditUser(UserSecurityInfo.NOUSER));
                } else {
                    Logger.GetInstance().addInfo(DAOAudit.INFOUSERKEY, new DAOAuditUser(sessionUi.getUserId()));
                }

                mostra(req, res, sessionUi);

            }
        } catch (AppCrash ap) {
            // Se il messaggio di errore non e' stato visualizzato rilancio l'eccezione
            if (!invokeDisplayError(ap, req, res)) {
                throw (ap);
            }
        } catch (Throwable err) {
            err.printStackTrace();
            AppCrash ex = new AppCrash(err);
            // Se il messaggio di errore non e' stato visualizzato rilancio l'eccezione
            if (!invokeDisplayError(err, req, res)) {
                throw (ex);
            }

        } finally {
            if (_applicationSrv.isTraceEnabled()) {
                long end = System.currentTimeMillis();
                StringBuffer log = new StringBuffer(100);
                log.append("#Tracef=").append(getName()).append("Mostra   ");
                log.append(this.getClass().getName()).append("   ").append(getFunctionID());
                log.append("   ").append(end - start);
                Logger.GetInstance().log0(log.toString());
            }
        }
    }

    /**
     * La doPost base verifica che l'applicazione e/o la funzione siano attive (Stato), che l'utente possa accedere alla
     * funzione, la presenza e la validità dei campi della richiesta e l'autenticazione. Se tutto OK richiama il metodo
     * elabora().
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     * @exception java.lang.Throwable.
     * @return void
     */
    @Override
    public final void doPost(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        long start = System.currentTimeMillis();
        try {
            UserSecurityInfo userInfo = makeUserSecurityInfo();

            prepareForRequest(req, res);

            if (checkSession(req) == false) {
                _applicationSrv.sessionExpired(req, res);
                return;
            }

            // Se i controlli non sono lato server applico le regole firewall prima di tutti
            // i check applicativi dei parametri
            if (_serversideCheck == false) {
                if (applyRequestFirewall(req) == false && _firewallTraining == false) {
                    checkFieldFailed(req, res);
                    return;
                }
            }

            if (checkAuthentication(req, res, userInfo) && checkStatusPermission(req, res, userInfo)
                    && checkRequestParameter(req, res)) {
                UserSecurityInfo sessionUi = null;
                HttpSession session = req.getSession(false);
                if (session != null) {
                    sessionUi = (UserSecurityInfo) session.getAttribute("LOGIN");
                }
                if (sessionUi == null) {
                    sessionUi = userInfo;
                }
                if (sessionUi.getUserId() == null) {
                    Logger.GetInstance().addInfo(DAOAudit.INFOUSERKEY, UserSecurityInfo.NOUSER);
                } else {
                    Logger.GetInstance().addInfo(DAOAudit.INFOUSERKEY, sessionUi.getUserId());
                }
                elabora(req, res, sessionUi);
            }
        } catch (AppCrash ap) {
            // Se il messaggio di errore non e' stato visualizzato rilancio l'eccezione
            if (!invokeDisplayError(ap, req, res)) {
                throw (ap);
            }
        } catch (Throwable err) {
            err.printStackTrace();
            AppCrash ex = new AppCrash(err);
            // Se il messaggio di errore non e' stato visualizzato rilancio l'eccezione
            if (!invokeDisplayError(err, req, res)) {
                throw (ex);
            }
        } finally {
            if (_applicationSrv.isTraceEnabled()) {
                long end = System.currentTimeMillis();
                StringBuffer log = new StringBuffer(100);
                log.append("#Tracef=").append(getName()).append("Elabora   ");
                log.append(this.getClass().getName()).append("   ").append(getFunctionID());
                log.append("   ").append(end - start);
                Logger.GetInstance().log0(log.toString());
            }

        }

    }

    /**
     * Questo metodo di default non fa nulla. <b>Viene richiamato prima di eseguire qualsiasi controllo su
     * autenticazione, permessi e parametri</b>. Permette, alle classi concrete che estendono questa classe, di
     * effettuare operazioni propedeutiche all'elaborazione della get o post.
     *
     * @param req net.project.servlet.frame.SsbServletRequest oggetto che incapsula la richiesta dal client alla servlet
     * @param res net.project.servlet.frame.SsbServletResponse oggetto che incapsula la risposta della servlet al
     *            client.
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void prepareForRequest(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

    }

    /**
     * Controlla che la funzione sia attiva e che l'utente abbia i permessi di accesso.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     * @return true se i controlli hanno dato esito positivo, false altrimenti.
     */
    private boolean checkStatusPermission(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo)
            throws AppCrash {

        // Controllo funzione attiva
        if (_stato == 0) {
            permissionFailed(req, res, "Funzione non attiva");
            return false;
        }
        // Controllo permessi utente
        if (isAccessFree()) {
            return true;
        }

        try {

            HttpSession session = req.getSession(false);

            // se la session non c'è (ossia l'utente non si è autenticato)
            if (session == null || session.getAttribute("LOGIN") == null) {
                if (_applicationSrv.isTraceEnabled()) {
                    String log = "#Trace=" + getName() + " #TPF1 Permission Failed: session not found";
                    Logger.GetInstance().log0(log);
                }

                permissionFailed(req, res, "Utente non autenticato o sessione scaduta");
                return false;
            }
            UserSecurityInfo sessionUi = (UserSecurityInfo) session.getAttribute("LOGIN");

            _applicationSrv.getSecurityProvider().logAudit();
            if (!_applicationSrv.getSecurityProvider().checkPermission(sessionUi, _functionID)) {
                if (_applicationSrv.isTraceEnabled()) {
                    String log = "#Trace=" + getName() + " #TPF2 Permission Failed";
                    Logger.GetInstance().log0(log);
                }
                permissionFailed(req, res, "Utente non autorizzato");
                return false;
            }

            return true;
        } catch (AppCrash ap) {
            ap.logContext("Function_base", "Errore in doCheck");
            throw (ap);
        }

    }

    /**
     * Questo metodo viene richiamato dal framework se il controllo sui permessi di accesso alla funzione fallisce. Di
     * default esegue la displayPage() di una pagina. La pagina si chiama 'PageError' e deve esistere nel file di
     * configurazione. In questa pagina vengono settati due parametri: 'Name' con il functionID della funzione e 'Msg'
     * con un messaggi del framework.
     * <P>
     * Le classi concrete che estendono function_base dovrebbero ridefinire questo metodo per dare alla applicazione un
     * comportamento consono alle sue necssita'.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @param message java.lang.String Il messaggio da visualizzare.
     * @exception net.project.errors.AppCrash.
     * @return void.
     */
    protected void permissionFailed(SsbServletRequest req, SsbServletResponse res, String message) throws AppCrash {

        Map parameters = new HashMap();
        parameters.put(NAME, _functionID);
        parameters.put(MSG, message);
        _applicationSrv.displayPage(PAGE_ERROR, parameters, res);
    }

    /**
     * Esegue l'autenticazione utilizzando la classe che implementa AuthenticationProvider_itf associata alla fuction.
     * Questa classe viene recuperata attraverso il framework.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @param userInfo net.project.servlet.security.UserSecurityInfo.
     * @exception net.project.errors.AppCrash. return true se l'autenticazione va a buon fine, false altrimenti.
     */
    private boolean checkAuthentication(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo)
            throws AppCrash {

        // controlla la necessità dell'autenticazione
        if (!isAuthenticationRequired()) {
            return true;
        }
        try {
            AuthenticationProvider_itf aut = _applicationSrv.getAuthenticationProvider(_functionName);
            aut.authenticate(userInfo, req);
            if (userInfo.getRoleId() == null) {
                if (_applicationSrv.isTraceEnabled()) {
                    String log = "#Trace=" + getName() + " #TAF1 Auth Failed  - " + aut.getClass().getName();
                    Logger.GetInstance().log0(log);
                }
                authenticationFailed(req, res);
                return false;
            }
            if (userInfo.getRoleId().equals(AuthenticationProvider_itf.ERRORE_PARAMETRI)) {
                if (_applicationSrv.isTraceEnabled()) {
                    String log = "#Trace=" + getName() + " #TAF2 Auth Field Failed  - " + aut.getClass().getName();
                    Logger.GetInstance().log0(log);
                }
                checkFieldAutFailed(req, res);
                return false;
            }

            HttpSession session = req.getSession(true);
            session.setAttribute("LOGIN", userInfo);
            return true;

        } catch (AppCrash e) {
            e.logContext("Function_base", "Errore in checkAuthentication");
            throw (e);
        }
    }

    /**
     * Controlla la presenza e la validità dei campi della richiesta. Per effettuare il compito richiama il metodo
     * checkField() che deve essere implementato da tutte le sue sottoclassi.
     * <p>
     * Se abilitato il firewall parametri allora va a verificare per tutti i parametri della request se esiste nel
     * vocabolario del messaggio HTTPREQUEST un verifier per quel campo. Se lo trova esegue la verifica, se non lo trova
     * usa il verifier del campo DEFAULTFIELD.
     * <p>
     * Il verifier per DEFAULTFIELD deve essere presente.
     * <p>
     * Il firewall parametri si attiva tramite la proprieta' Servlet.Firewall = true I controlli firewall vengono
     * effettuati prima della chiamata a checkField se
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash. return true se il controllo ha esito positivo, false altrimenti.
     */
    private boolean checkRequestParameter(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        if (!checkField(req)) {
            if (_applicationSrv.isTraceEnabled()) {
                String log = "#Trace=" + getName() + " #TCF 1 Check field Failed ";
                Logger.GetInstance().log0(log);
            }

            checkFieldFailed(req, res);
            return false;
        }

        // Una volta eseguiti i controlli applicativi se il check parametri e' server side
        // applico le regole firewall
        if (_serversideCheck == true) {
            if (applyRequestFirewall(req) == false && _firewallTraining == false) {
                checkFieldFailed(req, res);
                return false;
            }
        }
        return true;
    }

    /**
     * Attiva una funzione. Questo metodo viene richiamato una sola volta alla partenza della servlet application
     *
     * @return void.
     */
    @Override
    public void start() {

        readConfigParam();
        _stato = 1;

    }

    /**
     * Ferma una funzione.
     *
     * @return void.
     */
    @Override
    public void stop() {

        _stato = 0;
    }

    /**
     * Verifica la necessità di permessi per accedere alla funzione. Di default restituisce false percio', se non
     * ridefinito, fa si che l'accesso alla funzione non sia libero
     *
     * @return true se non sono necessari permessi, false altrimenti.
     */
    public boolean isAccessFree() {

        return false;
    }

    /**
     * Verifica la necessità di effettuare l'autenticazione. Di default restituisce true. Questo fa si che l'accesso
     * alla funzione sia condizionato da una precedente autenticazione.
     *
     * @return true se è necessaria l'autenticazione, false altrimenti.
     */
    public boolean isAuthenticationRequired() {

        return true;
    }

    /**
     * Controlla la presenza e la validità dei campi della richiesta. Ogni classe deve implementare questo metodo
     * astratto per eseguire la verifica dei campi in ingresso
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @return true se i campi sono formattati correttamente, false altrimenti.
     */
    protected abstract boolean checkField(SsbServletRequest req);

    /**
     * Esegue l'azione da compiere in caso di non corretta formattazione dei campi della richiesta.Deve essere
     * implementato dalle classi concrete
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     */
    protected abstract void checkFieldFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash;

    /**
     * Esegue l'azione da compiere in caso di non corretta formattazione dei campi necessari all'autenticazione.Deve
     * essere implementato dalle classi concrete
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     */
    protected abstract void checkFieldAutFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash;

    /**
     * Esegue l'azione da compiere in caso di fallimento dell'autenticazione.Deve essere implementato dalle classi
     * concrete
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     */
    protected abstract void authenticationFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash;

    /**
     * Metodo per richiamare la visualizzazione del messaggio di errore in caso di eccezioni nel framework. Ritorna true
     * se il messaggio e' stato effettivamente visualizzato. Ritorna false se la visualizzazione deve essere demandata
     * alla servlet application facendo la throw della eccezione.
     */
    private boolean invokeDisplayError(Throwable error, HttpServletRequest req, HttpServletResponse res) {

        try {
            return displayError(error, req, res);

        } catch (Throwable err) {
            new AppCrash(err);
        }
        return false;
    }

    /**
     * Metodo per la gestione della visualizzazione del messaggio di errore in caso di eccezioni non gestite dalle
     * implementazioni o generate nel framework stesso. Deve restituire true se ha effettivamente visualizzato il
     * messaggio. Se restituisce false l'eccezione viene rilanciata verso la ServletApplication_base che la 'gira' al su
     * metodo displayError. L'implementazione di default restituisce false
     */
    protected boolean displayError(Throwable error, HttpServletRequest req, HttpServletResponse res) throws AppCrash {

        return false;
    }

    /**
     * Questo metodo deve verificare la presenza della sessione, se necessaria. Se tutto ok deve ritornare true
     * altrimenti false. L'implementazione di default ritorna sempre true. Le varie servlet application devono
     * ridefinire questo metodo e fornire delle implementazioni adeguate. Per verificare semplicemente l'esistenza della
     * sessione dalle sottoclassi si puo' richiamare il metodo simpleCheckSession(). La verifica della presenza della
     * sessione dovrebbe includere il controllo dell'esistenza in sessione degli oggetti necessari.
     * <p>
     * Il framework richiama questo metodo subito dopo la prepareForRequest() e prima di checkAuthentication() e
     * checkStatusPermission().
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @return boolean true ok, false sessione scaduta.
     */
    protected boolean checkSession(SsbServletRequest req) {

        return true;
    }

    /**
     * Questo metodo verifica semplicemente la presenza della sessione. Se presente ritornare true altrimenti false.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @return boolean true ok, false sessione scaduta.
     */
    protected boolean simpleCheckSession(SsbServletRequest req) {

        HttpSession session = req.getSession(false);
        if (session == null) {
            return false;
        }
        return true;

    }

    /**
     * Questo metodo ritorna il nome della funzione
     *
     * @return String nome della funzione
     */
    @Override
    public String getName() {

        return _functionName;
    }

    /**
     * Questo metodo ritorna il function ID della funzione
     *
     * @return String function ID
     */
    @Override
    public String getFunctionID() {

        return _functionID;
    }

    /**
     * Questo metodo restituisce l'oggetto di tipo USerSecurityInfo da utilizzare
     *
     * @return
     */
    protected UserSecurityInfo makeUserSecurityInfo() throws AppCrash {

        return new UserSecurityInfo();
    }

    /**
     * Questo metodo applica le regole firewall sui parametri. Se abilitato il firewall parametri allora va a verificare
     * per tutti i parametri della request se esiste nel vocabolario del messaggio HTTPREQUEST un verifier per quel
     * campo. Se lo trova esegue la verifica, se non lo trova usa il verifier del campo DEFAULTFIELD.
     * <p>
     * Il verifier per DEFAULTFIELD deve essere presente.
     * <p>
     * Il firewall parametri si attiva tramite la proprieta' Servlet.Firewall = true
     * <p>
     * Se la verifica di un campo e' ok il fw provvede anche all'escaping SQL del campo stesso.
     *
     * @param req la request da vagliare
     * @return true se tutto ok, altrimenti false
     * @throws AppCrash in caso di violazione delle regole
     */
    protected boolean applyRequestFirewall(SsbServletRequest req) throws AppCrash {

        if (_firewall == false) return true;

        boolean result = true;
        Enumeration reqPar = req.getParameterNames();
        String name = null;
        boolean sqlEscape = isSqlEscapingAllowed(req);

        while (reqPar.hasMoreElements()) {
            name = (String) reqPar.nextElement();
            String value = req.getField(name);

            if (value.equals("")) continue;

            if (_strictVerify == false && value.length() > 0 && value.trim().length() == 0) {
                Logger.GetInstance().log0("#Trace=" + getName() + " #TFWSTRICT name " + name);
                continue;
            }

            FieldVerifier_itf verif = Verifier.GetInstance().getVocabulary("HTTPREQUEST").getFieldVerifier(name);
            if (verif == null) {
                verif = Verifier.GetInstance().getVocabulary("HTTPREQUEST").getFieldVerifier("DEFAULTFIELD");
                if (_logDefaultFieldUse) {
                    Logger.GetInstance().log0("#Trace=" + getName() + " #FWDEFAULT name " + name);
                }
            }
            ErrDetector.GetInstance().invariant(verif != null,
                    "Manca il campo DEFAULTFIELD nel vocabolario del messaggio HTTPREQUEST");

            if (verif.verify(value) == false) {
                result = false;
                Logger.GetInstance().log0("#Trace=" + getName() + " #TFW name " + name + " val " + req.getField(name));
            }

            // Se permesso esegue l'escape SQL del parametro
            if (sqlEscape) {
                req.setField(name, sqlEscape(value));
            }
        }

        return result;
    }

    /**
     * Questo metodo esegue l'escaping SQL DB2: converte gli ' in ''
     *
     * @param field la stringa da convertire
     * @return la stringa convertita
     * @throws AppCrash nel caso in cui il campo passato sia null
     */
    public String sqlEscape(String field) throws AppCrash {

        return Converter.sqlEscape(field);
    }

    /**
     * Questo metodo esegue l'unescaping SQL DB2: converte i '' in '
     *
     * @param field la stringa da convertire
     * @return la stringa convertita
     * @throws AppCrash nel caso in cui il campo passato sia null
     */
    public String sqlUnEscape(String field) throws AppCrash {

        return Converter.sqlUnEscape(field);
    }

    /**
     * Questo metodo verifica se l'escaping SQL e' permesso per questa funzione. Viene chiamato dal firewall parametri
     * se abilitato
     * <p>
     * Di default ritorna true. Le funzioni concrete possono ridefinirlo per impedire l'escamping SOLO IN CASI
     * ECCEZIONALI.
     *
     * @param req la request HTTP
     * @return true se permesso false in caso contrario
     */
    protected boolean isSqlEscapingAllowed(SsbServletRequest req) {

        return true;
    }

    /**
     * Questo metodo legge i parametri di configurazione
     *
     */
    private void readConfigParam() {

        if (Config.GetInstance().getProperty("Servlet.Firewall", "false").equalsIgnoreCase("true")) {
            _firewall = true;
        }
        if (Config.GetInstance().getProperty("Servlet.Firewall.Training", "false").equalsIgnoreCase("true")) {
            _firewallTraining = true;
        }
        if (Config.GetInstance().getProperty("Servlet.ServersideCheck", "false").equalsIgnoreCase("true")) {
            _serversideCheck = true;
        }
        if (Config.GetInstance().getProperty("Servlet.LogDefaultFieldUse", "false").equalsIgnoreCase("true")) {
            _logDefaultFieldUse = true;
        }

        if (Config.GetInstance().getProperty("verifier.strictVerify", "false").equalsIgnoreCase("true")) {
            _strictVerify = true;
        }

    }

}
