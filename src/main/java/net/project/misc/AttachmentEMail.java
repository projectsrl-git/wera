/*
  AttachmentEMail.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 27/06/2003

  Autore: Fabio F. e Luca M.

  Note:

  Modifiche:

 */

package net.project.misc;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;

/**
 * Classe che implementa le funzionalità di invio di una email con allegato
 */
public class AttachmentEMail extends SimpleMultipartEMail implements AttachmentEMail_itf {

    public static final String CONTENT_TYPE              = "Content-Type";
    public static final String CONTENT_DISPOSITION       = "Content-Disposition";
    public static final String ATTACHMENT                = "attachment";
    public static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    public static final String BASE_64                   = "base64";

    private Vector             _mimeBodyPartVector       = null;

    /**
     * @throws AppCrash
     */
    public AttachmentEMail() throws AppCrash {

        super();
    }

    /**
     * Aggiunge un allegato all'email.
     * 
     * @param attachment byte[] L'allegato da inviare. Non può essere null.
     * @param mimeType java.lang.String Il mime type. Non può essere null né stringa vuota.
     * @param attachmentName java.lang.String Il nome dell'allegato. Può anche essere null o stringa vuota: in tal caso,
     *            l'allegato è inviato senza nome.
     * @exception net.project.errors.AppCrash Se uno dei parametri di ingresso è null o in caso di errore nell'aggiunta
     *                dell'allegato
     */
    @Override
    public void addAttachment(byte[] attachment, String mimeType, String attachmentName) throws AppCrash {

        ErrDetector.GetInstance().param(attachment);
        ErrDetector.GetInstance().param(mimeType);

        MimeBodyPart attBodyPart = new MimeBodyPart();

        try {
            // accodo l'allegato che viene convertito automaticamente in Base64
            attBodyPart.setText(new String(attachment));

            // faccio l'override delle proprietà relative all'allegato
            attBodyPart.setHeader(CONTENT_TYPE, mimeType);
            attBodyPart.setHeader(CONTENT_DISPOSITION, ATTACHMENT);
            if ((attachmentName != null) && !attachmentName.equals("")) {
                attBodyPart.setFileName(attachmentName);
            }
            attBodyPart.setHeader(CONTENT_TRANSFER_ENCODING, BASE_64);

            // crea il vettore se non esiste
            if (_mimeBodyPartVector == null) {
                _mimeBodyPartVector = new Vector();
            }

            _mimeBodyPartVector.addElement(attBodyPart);

        } catch (MessagingException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("AttachmentEMail", toString());
            throw err;
        }

    }

    /**
     * Aggiunge un allegato privo di nome all'email.
     * 
     * @param attachment byte[] L'allegato da inviare. Non può essere null.
     * @param mimeType java.lang.String Il mime type. Non può essere null né stringa vuota.
     * @exception net.project.errors.AppCrash Se uno dei parametri di ingresso è null o in caso di errore nell'aggiunta
     *                dell'allegato
     */
    @Override
    public void addAttachment(byte[] attachment, String mimeType) throws AppCrash {

        addAttachment(attachment, mimeType, null);
    }

    @Override
    protected void send(MimeMessage msg) throws AppCrash {

        try {
            if (_mimeBodyPartVector != null) {
                for (int i = 0; i < _mimeBodyPartVector.size(); i++) {
                    getMimeMultipart().addBodyPart((MimeBodyPart) _mimeBodyPartVector.elementAt(i));
                }
            }
        } catch (MessagingException e) {
            throw new AppCrash(e);
        }

        super.send(msg);
    }

    public static final void main(String[] argv) {

        try {
            System.out.println("Prima riga del main");
            Config.InitInstance("attach.cfg");
            System.out.println(Config.GetInstance().getProperty("mail.SMTPHost"));

            AttachmentEMail aem = new AttachmentEMail();
            aem.setFrom("super_email");
            aem.setTo("luca.mariani@ssb.it");
            aem.setSubject("prova ROD");

            FileInputStream fis = new FileInputStream(new File("dettagli.gif"));
            byte[] array = new byte[2048];
            int len = fis.read(array);
            byte[] arrayTagliato = new byte[len];
            System.arraycopy(array, 0, arrayTagliato, 0, len);

            aem.addAttachment(arrayTagliato, "image/gif", "immagine.gif");
            aem.addAttachment(arrayTagliato, "text/plain", "sbagliatto.gif");
            aem.addAttachment(arrayTagliato, "image/gif", "");
            aem.addAttachment(arrayTagliato, "image/gif", "subblime.gif");
            System.out.println("1");
            aem.send("Questa è una belissssima mail di PROV per ROD");
        } catch (AppCrash ac) {
            System.out.println(ac.getMessage());
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }

    }

}
