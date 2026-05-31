/*
  EMailFactory.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 23/10/2001

  Autore: Simone Z.

  Note:

  Modifiche:	

 */

package net.project.misc;

import net.project.errors.AppCrash;

/**
 * Questa classe e' la factory utilizzata per costruire messaggi email.
 */
public class EMailFactory {

    static public SimpleEMail_itf MakeSimpleEmail() throws AppCrash {

        String classe = Config.GetInstance().getProperty("mail.SimpleEmail.class", "net.project.misc.SimpleSmtpEMail");
        try {
            SimpleEMail_itf email = (SimpleEMail_itf) Class.forName(classe).newInstance();
            return email;
        } catch (ClassNotFoundException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("EMailFactory", "SimpleEMail Not Found" + classe);
            throw err;
        } catch (IllegalAccessException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("EMailFactory", "SimpleEMail class access error" + classe);
            throw err;
        } catch (InstantiationException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("EMailFactory", "SimpleEMail class instantiation error" + classe);
            throw err;
        }
    }

    static public AttachmentEMail_itf MakeAttachmentEMail() throws AppCrash {

        String classe = Config.GetInstance().getProperty("mail.AttachmentEMail.class",
                "net.project.misc.AttachmentEMail");
        try {
            AttachmentEMail_itf email = (AttachmentEMail_itf) Class.forName(classe).newInstance();
            return email;
        } catch (ClassNotFoundException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("EMailFactory", "AttachmentEMail Not Found " + classe);
            throw err;
        } catch (IllegalAccessException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("EMailFactory", "AttachmentEMail class access error " + classe);
            throw err;
        } catch (InstantiationException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("EMailFactory", "AttachmentEMail class instantiation error " + classe);
            throw err;
        }
    }

}
