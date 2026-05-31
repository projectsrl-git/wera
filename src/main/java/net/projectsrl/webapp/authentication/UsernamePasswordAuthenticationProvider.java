
package net.projectsrl.webapp.authentication;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.misc.Config;
import net.project.misc.Util;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.security.AuthenticationProvider;
import net.project.servlet.security.AuthenticationProvider_itf;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.webapp.core.WebAppUtils;
import net.projectsrl.webapp.security.WebAppUserSecurityInfo;
import project.misc.Utils;

/**
 * Authentication provider per l'applicazione.
 */
public class UsernamePasswordAuthenticationProvider<U> extends AuthenticationProvider {

    private static final String LOGIN_SESSION_ATTRIBUTE  = "LOGIN";
    private static final String SERVLET_APPLICATION_NAME = Config.GetInstance().getProperty("Servlet.ApplicationName");
    private static final String USERNAME                 = "USERNAME";
    private static final String DATASET_LOGIN            = "DataSetLogin";
    private static final String PASSWORD                 = "PASSWORD";
    private static final String DT_SCADENZA              = "DT_SCADENZA";
    private static final String DATA_SCADENZA_CREDITI    = "DATA_SCADENZA_CREDITI";
    private static final String CREDITI_VALIDI           = "CREDITI_VALIDI";
    private static final String CREDITI              	 = "CREDITI";
    private static final String CREDITI_IN_SCADENZA      = "CREDITI_IN_SCADENZA";
    private String              _configName              = "";

    /**
     * Costruttore vuoto.
     */
    public UsernamePasswordAuthenticationProvider() {

        super();
    }

    /**
     * Costruttore.
     * 
     * @param configName java.lang.String nome della configurazione.
     */
    public UsernamePasswordAuthenticationProvider(String configName) {

        _configName = configName;
    }

    /**
     * Metodo di autenticazione.
     * 
     * @param userInfo net.ssb.servlet.security.UserSecurityInfo Oggetto contenente le informazioni di sicurezza
     *            dell'utente.
     * @param identity java.lang.Object Oggetto che identifica l'utente.
     * 
     * @exception net.ssb.errors.AppCrash.
     */
    @SuppressWarnings("unchecked")
    public void authenticate(UserSecurityInfo userInfo, Object identity) throws AppCrash {

        LoginData loginData = null;

        DataSet_itf dataSet = null;

        try {

            SsbServletRequest req = (SsbServletRequest) identity;
            ErrDetector.GetInstance().preCond(req != null, "req is null");

            if (isSessionUserValid(userInfo, req)) {
                return;
            }

            loginData = new LoginData(req);

            ErrDetector.GetInstance().postCond(Util.IsNotEmpty(loginData.getUser()), "user is empty");

            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet(_configName, DATASET_LOGIN);

            HashMap<String, String> params = new HashMap<String, String>();
            params.put(USERNAME, loginData.getUser());

            dataSet.setParam(params);
            dataSet.open();

            if (!dataSet.hasMoreElements()) {
                Logger.GetInstance().logApplication(
                        "UsernamePasswordAuthenticationProvider - user not found - user:" + loginData.getUser());
                userInfo.setUserId(null);
                userInfo.setRoleId(null);
                return;
            }

            Row_itf dbRow = (Row_itf) dataSet.nextElement();
            String dbPassword = (String) dbRow.getField(PASSWORD);
            if (dbPassword == null) {
                Logger.GetInstance().logApplication(
                        "UsernamePasswordAuthenticationProvider - null password " + dbPassword + " - user:"
                                + loginData.getUser());
                userInfo.setUserId(null);
                userInfo.setRoleId(null);
                return;
            }

            String reqPassword = req.getField(PASSWORD);
            String expireDate = (String) dbRow.getField(DT_SCADENZA);
            String role = (String) dbRow.getField(WebAppConstants_itf.PROFILO);

            if (!isValidPassword(dbPassword, reqPassword, expireDate)) {
                Logger.GetInstance().logApplication(
                        "UsernamePasswordAuthenticationProvider - invalid password " + reqPassword + " - user:"
                                + loginData.getUser());
                userInfo.setUserId(null);
                userInfo.setRoleId(null);
                return;
            }

            if (isPasswordExpired(expireDate)) {
                Logger.GetInstance().logApplication(
                        "UsernamePasswordAuthenticationProvider - password expired " + reqPassword + " - user:"
                                + loginData.getUser());
                userInfo.setUserId(null);
                userInfo.setRoleId(AuthenticationProvider_itf.ERRORE_PARAMETRI);
                return;
            }
            
            

            userInfo.setApplicationId(SERVLET_APPLICATION_NAME);
            userInfo.setIPAddress(WebAppUtils.getRemoteAddress(req));
            userInfo.setUserId(loginData.getUser());
            userInfo.setRoleId(role);

            ErrDetector.GetInstance().postCond(userInfo instanceof WebAppUserSecurityInfo,
                    "userInfo not instanceof WebAppUserSecurityInfo");

            setSpecificUserSecurityInfo((WebAppUserSecurityInfo<U>) userInfo, dbRow);

        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            StringBuffer userData = new StringBuffer();
            userData.append("identity");
            userData.append(": -");
            userData.append(identity);
            userData.append("-");
            ac.logContext(this.getClass().getName(), userData.toString());
            throw ac;
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (AppCrash ac) {
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
                }
            }
        }
    }

    private boolean isPasswordExpired(String expireDate) {

        String today = Utils.getStringDataOggiRibaltata();
        expireDate = Utils.ribaltaData(expireDate);

        return today.compareTo(expireDate) >= 0;
    }

    /**
     * Questo metodo
     * 
     * @param dbPassword
     * @param reqPassword
     * @throws AppCrash
     */
    protected boolean isValidPassword(String dbPassword, String reqPassword, String expireDate) throws AppCrash {

        boolean isValidPassword = dbPassword.equals(reqPassword)
                || dbPassword.equals(WebAppUtils.encryptSHA1(reqPassword));

        return isValidPassword;

    }

    /**
     * Questo metodo
     * 
     * @param userInfo
     * @param dbRow
     * @throws AppCrash
     */
    @SuppressWarnings("unchecked")
    protected void setSpecificUserSecurityInfo(WebAppUserSecurityInfo<U> userInfo, Row_itf dbRow) throws AppCrash {

        U idUtente = (U) dbRow.getField("ID_UTENTE");
        String isoLanguage = (String) dbRow.getField("CODICE_ISO");
        userInfo.setField(WebAppConstants_itf.CURRENT_SELECTED_ISO_LANGUAGE, isoLanguage);

        userInfo.setUserUniqueIdentifier(idUtente);
        userInfo.addMenuListToSessionMap(isoLanguage);
        userInfo.setLanguageLabels(isoLanguage);
        userInfo.setTemplateDataProperties();
        
        String username = (String) dbRow.getField(WebAppConstants_itf.USERNAME);
        String cognome = (String) dbRow.getField(WebAppConstants_itf.COGNOME_UTENTE);
        String nome = (String) dbRow.getField(WebAppConstants_itf.NOME_UTENTE);

        String profilo = (String) dbRow.getField(WebAppConstants_itf.PROFILO);
        String descrizioneProfilo = (String) dbRow.getField(WebAppConstants_itf.DESCRIZIONE_PROFILO);
        String scadenzaPassword = (String) dbRow.getField(WebAppConstants_itf.DT_SCADENZA);
        
        String validitaCrediti = dbRow.getField(CREDITI_VALIDI).toString();
        String dataScadenzaCrediti = (String) dbRow.getField(DATA_SCADENZA_CREDITI);
        String crediti = dbRow.getField(CREDITI).toString();
        String creditiInScadenza = dbRow.getField(CREDITI_IN_SCADENZA).toString();

        userInfo.setField(WebAppConstants_itf.ID_UTENTE, idUtente.toString());
        userInfo.setField(WebAppConstants_itf.USERNAME, username);
        userInfo.setField(WebAppConstants_itf.COGNOME_UTENTE, cognome);
        userInfo.setField(WebAppConstants_itf.NOME_UTENTE, nome);
        userInfo.setField(WebAppConstants_itf.PROFILO, profilo);
        userInfo.setField(WebAppConstants_itf.DESCRIZIONE_PROFILO, descrizioneProfilo);
        userInfo.setField(WebAppConstants_itf.DT_SCADENZA, scadenzaPassword);
        userInfo.setField(WebAppConstants_itf.TODAY_YYYYMMDD, Utils.getStringDataOggiRibaltata());
        userInfo.setField(WebAppConstants_itf.TODAY, Utils.getStringDataOggi());
        userInfo.setField(WebAppConstants_itf.START_OF_YEAR, "01/01/"+Utils.getAnnoOggi());
        
        userInfo.setField(WebAppConstants_itf.SOFTWARE_RELEASE, Config.GetInstance()
                .getProperty("Software.release", ""));
        
        userInfo.setField(WebAppConstants_itf.VALIDITA_CREDITI, validitaCrediti);
        userInfo.setField(WebAppConstants_itf.DATA_SCADENZA_CREDITI, dataScadenzaCrediti);
        userInfo.setField(WebAppConstants_itf.CREDITI, crediti);
        userInfo.setField(WebAppConstants_itf.CREDITI_IN_SCADENZA, creditiInScadenza);
    }

    private boolean isSessionUserValid(UserSecurityInfo userInfo, SsbServletRequest req) {

        // se sto passando l'utente non voglio considerare lo userinfo in sessione
        String user = req.getField(USERNAME);
        if (Util.IsNotEmpty(user)) {
            return false;
        }

        String remoteAddr = WebAppUtils.getRemoteAddress(req);
        UserSecurityInfo sessionUi = null;
        boolean sessionUIisValid = false;
        HttpSession session = req.getSession(false);
        if (session != null) {

            sessionUi = (UserSecurityInfo) session.getAttribute(LOGIN_SESSION_ATTRIBUTE);

            if (sessionUi != null && sessionUi.getRoleId() != null && sessionUi.getUserId() != null
                    && sessionUi.getApplicationId() != null
                    && sessionUi.getApplicationId().equals(SERVLET_APPLICATION_NAME)
                    && sessionUi.getIPAddress() != null && sessionUi.getIPAddress().equals(remoteAddr)) {

                userInfo.copy(sessionUi);

                sessionUIisValid = true;
            }
        }

        return sessionUIisValid;
    }

}
