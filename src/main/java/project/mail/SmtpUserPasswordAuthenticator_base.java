
package project.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public abstract class SmtpUserPasswordAuthenticator_base extends Authenticator {


    public SmtpUserPasswordAuthenticator_base() {

    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {

        PasswordAuthentication passwordAutentication = new PasswordAuthentication(getSMTPUser(), getSMTPPassword());

        return passwordAutentication;
    }

    public abstract String getSMTPHost();

    public abstract String getSMTPPassword() ;

    public abstract String getSMTPUser();
}