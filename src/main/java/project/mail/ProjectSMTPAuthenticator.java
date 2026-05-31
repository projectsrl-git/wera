
package project.mail;

import net.project.misc.Config;

public class ProjectSMTPAuthenticator extends SmtpUserPasswordAuthenticator_base {

    public static final String  DEFAULT_MAIL_SMTP_HOST     = "smtp.projectsrl.net";
    public static final String  PROP_MAIL_SMTP_HOST        = "mail.SMTPHost";
    public static final String  MAIL_SMTP_HOST             = Config.GetInstance().getProperty(PROP_MAIL_SMTP_HOST,
                                                                   DEFAULT_MAIL_SMTP_HOST);

    private static final String DEFAULT_MAIL_SMTP_PASSWORD = "automatica";
    private static final String PROP_MAIL_SMTP_PASSWORD    = "mail.SMTPHost.password";
    private static final String MAIL_SMTP_PASSWORD         = Config.GetInstance().getProperty(PROP_MAIL_SMTP_PASSWORD,
                                                                   DEFAULT_MAIL_SMTP_PASSWORD);
    private static final String DEFAULT_MAIL_SMTP_USER     = "noreply@projectsrl.net";
    private static final String PROP_MAIL_SMTP_USER        = "mail.SMTPHost.user";
    private static final String MAIL_SMTP_USER             = Config.GetInstance().getProperty(PROP_MAIL_SMTP_USER,
                                                                   DEFAULT_MAIL_SMTP_USER);

    @Override
    public String getSMTPHost() {

        return MAIL_SMTP_HOST;
    }

    @Override
    public String getSMTPPassword() {

        return MAIL_SMTP_PASSWORD;
    }

    @Override
    public String getSMTPUser() {

        return MAIL_SMTP_USER;
    }
}
