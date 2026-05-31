
package net.projectsrl.webapp.authentication;

import java.util.HashMap;
import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.webapp.core.ServletApplication;
import project.mail.InvioMail;
import project.mail.ProjectSMTPAuthenticator;
import project.mail.SmtpUserPasswordAuthenticator_base;

/**
 * FunctionLogin
 * 
 * Login standard per applicazione web
 * 
 */
public abstract class FunctionRegistrazione_base extends FunctionProjectWebApp_base {

    private static final String TAG_BODY_TEXT_RESET_PASSWORD_BODY_PASSWORD = "#RESET_PASSWORD_BODY_PASSWORD#";
    private static final String TAG_BODY_TEXT_RESET_PASSWORD_BODY_USERNAME = "#RESET_PASSWORD_BODY_USERNAME#";
    private static final String MESSAGES_RESET_PASSWORD_SUCCESS            = "Messages.Registrazione.Success";
    private static final String MESSAGES_RESET_PASSWORD_ERROR              = "Messages.ResetPassword.Error";
    private static final String MESSAGES_TWICE_SAME_EMAIL_ERROR            = "Messages.ResetPassword.Error.TwiceSameEmailCrash";
    private static final String TAG_INFO_MESSAGE                           = "INFO_MESSAGE";
    private static final String MESSAGES_RESET_PASSWORD_INFO               = "Messages.ResetPassword.Info";

    public static final String  PROP_RESET_PASSWORD_MAIL_ADDRESS_FROM      = "ResetPassword.Mail.Address.From";
    public static final String  RESET_PASSWORD_MAIL_ADDRESS_FROM           = Config.GetInstance()
                                                                                   .getProperty(
                                                                                           PROP_RESET_PASSWORD_MAIL_ADDRESS_FROM);
    public static final String  PROP_RESET_PASSWORD_MAIL_ADDRESS_BCC       = "ResetPassword.Mail.Address.BCC";
    public static final String  RESET_PASSWORD_MAIL_ADDRESS_BCC            = Config.GetInstance()
                                                                                   .getProperty(
                                                                                           PROP_RESET_PASSWORD_MAIL_ADDRESS_BCC);
    public static final String  PROP_RESET_PASSWORD_MAIL_SUBJECT           = "ResetPassword.Mail.Subject";
    public static final String  RESET_PASSWORD_MAIL_SUBJECT                = Config.GetInstance().getProperty(
                                                                                   PROP_RESET_PASSWORD_MAIL_SUBJECT);
    public static final String  PROP_RESET_PASSWORD_MAIL_BODY              = "ResetPassword.Mail.Body";
    public static final String  RESET_PASSWORD_MAIL_BODY                   = Config.GetInstance().getProperty(
                                                                                   PROP_RESET_PASSWORD_MAIL_BODY);
    public static final String  EMAIL_FIELD                                = "EMAIL";
    public static final String  NOME_FIELD                                = "NOME";
    public static final String  COGNOME_FIELD                                = "COGNOME";
    public static final String  RILEVATORE_FIELD							= "RILEVATORE";
    public static final String  PROP_MAIL_PRIVACY_DISCLAIMER               = "mail.Privacy.Disclaimer";
    public static final String  MAIL_PRIVACY_DISCLAIMER                    = Config.GetInstance().getProperty(
                                                                                   PROP_MAIL_PRIVACY_DISCLAIMER);

    public FunctionRegistrazione_base() {

        super();
    }

    public FunctionRegistrazione_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    /**
     * Verifica la necessità di effettuare l'autenticazione.
     * 
     * @return false, perche' questa funzione non richiede autenticazione.
     */
    @Override
    public boolean isAuthenticationRequired() {

        return false;
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = new HashMap<String, Object>();

        templateData.put(TAG_INFO_MESSAGE, Config.GetInstance().getProperty(MESSAGES_RESET_PASSWORD_INFO));

        _applicationSrv.displayPage(getPageName(), templateData, res);

    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = new HashMap<String, Object>();

        String email = req.getField(EMAIL_FIELD);
        String nome = req.getField(NOME_FIELD);
        String cognome = req.getField(COGNOME_FIELD);
        String rilevatore = req.getField(RILEVATORE_FIELD);

        try {
            ErrDetector.GetInstance().param(Util.IsNotEmpty(email), "field EMAIL_FIELD is empty");

            if (isUniqueEmail(email)) {

                String pwd = generateRandomPassword();

                String encrPwd = encryptPassword(pwd);
                
                generateUser(email,nome,cognome,rilevatore);

                storeEncryptedPassword(encrPwd, email);

                sendNewPassword(pwd, email);

                ((ServletApplication) _applicationSrv).displayMessageNextLogin(
                        Config.GetInstance().getProperty(MESSAGES_RESET_PASSWORD_SUCCESS), res);
                return;

            } else {
                templateData.put(TAG_ERROR_MESSAGE,
                        Config.GetInstance().getProperty(MESSAGES_TWICE_SAME_EMAIL_ERROR));
            }

        } catch (Throwable th) {
            templateData
                    .put(TAG_ERROR_MESSAGE, Config.GetInstance().getProperty(MESSAGES_RESET_PASSWORD_ERROR));

        }

        _applicationSrv.displayPage(getPageName(), templateData, res);

    }

    protected abstract boolean isUniqueEmail(String email) throws AppCrash;

    protected abstract String generateRandomPassword() throws AppCrash;

    protected abstract String encryptPassword(String password) throws AppCrash;
    
    protected abstract void generateUser(String email, String nome, String cognome, String rilevatore) throws AppCrash;

    protected abstract void storeEncryptedPassword(String encryptedPassword, String email) throws AppCrash;

    protected abstract String getUserNameFromEmailAddress(String email) throws AppCrash;

    protected void sendNewPassword(String password, String email) throws AppCrash {

        InvioMail invioMail = new InvioMail();

        ErrDetector.GetInstance().preCond(Util.IsNotEmpty(RESET_PASSWORD_MAIL_ADDRESS_FROM),
                PROP_RESET_PASSWORD_MAIL_ADDRESS_FROM + " is empty");

        invioMail.setFrom(RESET_PASSWORD_MAIL_ADDRESS_FROM);

        invioMail.setTo(email);
        if (Util.IsNotEmpty(RESET_PASSWORD_MAIL_ADDRESS_BCC)) {
            invioMail.setBcc(RESET_PASSWORD_MAIL_ADDRESS_BCC);
        }

        ErrDetector.GetInstance().preCond(Util.IsNotEmpty(RESET_PASSWORD_MAIL_SUBJECT),
                PROP_RESET_PASSWORD_MAIL_SUBJECT + " is empty");

        invioMail.setSubject(RESET_PASSWORD_MAIL_SUBJECT);

        invioMail.setBody(completeMailBody(password, email));

        if (Util.IsNotEmpty(MAIL_PRIVACY_DISCLAIMER)) {
            invioMail.setFooter("\n\n" + MAIL_PRIVACY_DISCLAIMER);
        }

        boolean hasAttachment = false;

        invioMail.setSignFileName(getSignFilename());

        SmtpUserPasswordAuthenticator_base auth = makeSmtpAutenticator();

        invioMail.setServer(auth.getSMTPHost());

        invioMail.invioMail(auth, hasAttachment);

    }

    protected String getSignFilename() {

        return _applicationSrv.getRoot() + "img/logo_header.png";
    }

    protected SmtpUserPasswordAuthenticator_base makeSmtpAutenticator() {

        return new ProjectSMTPAuthenticator();
    }

    protected String completeMailBody(String password, String email) throws AppCrash {

        String body = null;

        ErrDetector.GetInstance().preCond(Util.IsNotEmpty(RESET_PASSWORD_MAIL_BODY),
                PROP_RESET_PASSWORD_MAIL_BODY + " is empty");

        body = RESET_PASSWORD_MAIL_BODY;

        body = body.replaceAll(TAG_BODY_TEXT_RESET_PASSWORD_BODY_USERNAME, getUserNameFromEmailAddress(email));
        body = body.replaceAll(TAG_BODY_TEXT_RESET_PASSWORD_BODY_PASSWORD, password);

        return body;
    }

    @Override
    protected boolean checkSession(SsbServletRequest req) {

        return true;
    }
}
