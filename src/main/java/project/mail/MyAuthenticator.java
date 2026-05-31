
package project.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import net.project.misc.Config;

public class MyAuthenticator extends Authenticator {

    public MyAuthenticator() {

    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {

        PasswordAuthentication passwordAutentication = new PasswordAuthentication(Config.GetInstance().getProperty(
                "mail.SMTPHost.user", "noreply@projectsrl.net"), Config.GetInstance().getProperty(
                "mail.SMTPHost.password", "automatica"));
        return passwordAutentication;
    }
}