/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.misc;

import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;

/**
 * Questa classe fornisce le funzionalità di un semplice messaggio SMTP.
 */
public class SimpleSmtpEMail implements SimpleEMail_itf {

    private final String _contentType = "text/plain";
    private String       _host        = null;
    private String       _message     = null;
    private String       _to          = null;
    private String       _cc          = null;
    private String       _bcc         = null;
    private String       _from        = null;
    private String       _subject     = null;
    private String       _replayto    = null;
    private Session      _session     = null;

    /**
     * Costruttore.
     *
     * @exception net.project.errors.AppCrash. Se si è verificato un errore di input/output.
     */
    public SimpleSmtpEMail() throws AppCrash {

        _host = Config.GetInstance().getProperty("mail.SMTPHost", "fw2.ssb.net");

        // Set the host smtp address
        Properties props = new Properties();
        props.put("mail.smtp.host", _host);

        // create some properties and get the default Session
        _session = Session.getDefaultInstance(props, null);
        _session.setDebug(false);
    }

    /**
     * Imposta l'host.
     *
     * @param host java.lang.String host.
     */
    @Override
    public void setHost(String host) {

        _host = host;
    }

    /**
     * Ritorna l'host.
     *
     * @return java.lang.String L'host.
     */
    private String getHost() {

        return _host;
    }

    /**
     * Imposta il messaggio da spedire.
     *
     * @param message java.lang.String messaggio da spedire.
     */
    @Override
    public void setMessage(String message) {

        _message = message;
    }

    /**
     * Ritorna il messaggio da spedire.
     *
     * @return java.lang.String Il messaggio da spedire.
     */
    private String getMessage() {

        return _message;
    }

    /**
     * Imposta il destinatario del messaggio.
     *
     * @param to java.lang.String destinatario del messaggio.
     */
    @Override
    public void setTo(String to) {

        _to = to;
    }

    /**
     * Ritorna il destinatario del messaggio.
     *
     * @return java.lang.String Il destinatario del messaggio.
     */
    private String getTo() {

        return _to;
    }

    /**
     * Questo metodo
     *
     * @param cc DOCUMENT ME!
     */
    @Override
    public void setCc(String cc) {

        _cc = cc;
    }

    /**
     * Questo metodo
     *
     * @param bcc DOCUMENT ME!
     */
    @Override
    public void setBcc(String bcc) {

        _bcc = bcc;
    }

    /**
     * Imposta il mittente del messaggio.
     *
     * @param from java.lang.String mittente del messaggio.
     */
    @Override
    public void setFrom(String from) {

        _from = from;
    }

    /**
     * Ritorna il mittente del messaggio.
     *
     * @return java.lang.String Il mittente del messaggio.
     */
    private String getFrom() {

        return _from;
    }

    /**
     * Imposta l'indirizzo da usare come "Replay to:"
     *
     * @param replayto java.lang.String indirizzo.
     */
    @Override
    public void setReplayTo(String replayto) {

        _replayto = replayto;
    }

    /**
     * Ottiene l'indirizzo da usare come "Replay to:"
     *
     * @return java.lang.String indirizzo usato come "Replay to:".
     */
    @Override
    public String getReplayTo() {

        return _replayto;
    }

    /**
     * Imposta il subject.
     *
     * @param subject java.lang.String soggetto dell'e-mail.
     */
    @Override
    public void setSubject(String subject) {

        _subject = subject;
    }

    /**
     * Ritorna il subject.
     *
     * @return java.lang.String Il soggetto dell'e-mail.
     */
    private String getSubject() {

        return _subject;
    }

    /**
     * Invia il messaggio specificato all'host sulla porta 25.
     *
     * @param message java.lang.String messaggio da spedire.
     *
     * @exception net.project.errors.AppCrash Se si è verificato un errore durante l'invio del messaggio.
     */
    @Override
    public void send(String message) throws AppCrash {

        String t = null;

        try {
            ErrDetector.GetInstance().param((_host != null) && (_from != null) && (_to != null) && (message != null));
            _message = message;

            // crea il messaggio
            MimeMessage msg = new MimeMessage(_session);

            // set the from and to address
            InternetAddress addressFrom = new InternetAddress(_from);
            msg.setFrom(addressFrom);

            setAddresses(_to, msg, Message.RecipientType.TO);

            if ((_cc != null) && !_cc.equals("")) {
                setAddresses(_cc, msg, Message.RecipientType.CC);
            }

            if ((_bcc != null) && !_bcc.equals("")) {
                setAddresses(_bcc, msg, Message.RecipientType.BCC);
            }

            // Setting the Subject and Content Type
            msg.setSubject(_subject);
            msg.setText(getMessage(), "ISO-8859-1");

            String replayto = getReplayTo();

            if (replayto != null) {
                InternetAddress addressReplayTo = new InternetAddress(replayto);
                msg.setFrom(addressReplayTo);
            }

            Transport.send(msg);
        } catch (Throwable e) {
            AppCrash err = new AppCrash(e);
            err.logContext("EMail", toString());
            throw err;
        }
    }

    /**
     * Questo metodo
     *
     * @param dest DOCUMENT ME!
     * @param msg DOCUMENT ME!
     * @param rt DOCUMENT ME!
     *
     * @throws AppCrash DOCUMENT ME!
     */
    private void setAddresses(String dest, MimeMessage msg, Message.RecipientType rt) throws AppCrash {

        StringTokenizer parser = new StringTokenizer(dest, ",");

        while (parser.hasMoreTokens()) {
            try {
                InternetAddress address = new InternetAddress(parser.nextToken().trim());
                msg.addRecipient(rt, address);
            } catch (AddressException err) {
            } catch (MessagingException e) {
                throw new AppCrash(e);
            }
        }
    }

    /**
     * Ritorna una stringa riportante host, mittente, destinatario e messaggio dell'e-mail.
     *
     * @return java.lang.String.
     */
    @Override
    public String toString() {

        return new String("Host: " + _host + " Sender: " + _from + "Receiver: " + _to + "Message: " + _message);
    }
}
