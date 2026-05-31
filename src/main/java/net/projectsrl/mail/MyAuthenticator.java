
package net.projectsrl.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import net.project.errors.Logger;
import net.project.misc.Config;

public class MyAuthenticator extends Authenticator {

    public MyAuthenticator() {

    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {

        Logger.GetInstance()
                .log0("MyAuthenticator - mail.SMTPHost.user:"
                        + Config.GetInstance().getProperty("mail.SMTPHost.user", "") + " - mail.SMTPHost.password:"
                        + Config.GetInstance().getProperty("mail.SMTPHost.password", ""));
        
        PasswordAuthentication passwordAutentication = new PasswordAuthentication(
                Config.GetInstance().getProperty("mail.SMTPHost.user", ""),
                Config.GetInstance().getProperty("mail.SMTPHost.password", ""));
        return passwordAutentication;
    }
}