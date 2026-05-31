/*
  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 15/10/2003

  Autore: Simone Z.

  Note: Questa classe e' tratta dalla classe   SimpleSmtpEMail nella sua versione 
        SimpleSmtpEMail.java@@\main\Libs4_main\6. E' stata separata perche' questa versione
        crea dei problemi ad alcuni client di posta. SimpleSmtpEMail e' stata ripostata alla 
        versione \main\Libs4_main\3

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
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;

/**
 * Questa classe fornisce le funzionalità di un semplice messaggio SMTP.
 */
public class SimpleMultipartEMail implements SimpleEMail_itf {

    private String        _host          = null;
    private String        _message       = null;
    private String        _to            = null;
    private String        _cc            = null;
    private String        _bcc           = null;
    private String        _from          = null;
    private String        _subject       = null;
    private String        _replayto      = null;
    private Session       _session       = null;
    private MimeMultipart _mimeMultipart = null;

    /**
     * Costruttore. Legge dal file di configurazione l'indirizzo dell'host che si desidera utilizzare per inviare le
     * email, tramite la proprietà mail.SMTPHost; se questa non è valorizzata, vale il default: fw2.ssb.net. L'indirizzo
     * dell'host che invia le email può essere successivamente modificato invocando, sul presente oggetto, il metodo
     * setHost(java.lang.String), che ha come parametro appunto tale indirizzo (in questo caso, l'eventuale proprietà
     * mail.SMTPHost del file di configurazione, oppure il default, saranno ignorati).
     */
    public SimpleMultipartEMail() {

        _host = Config.GetInstance().getProperty("mail.SMTPHost", "fw2.ssb.net");
        _mimeMultipart = new MimeMultipart();

    }

    @Override
    public void setHost(String host) {

        _host = host;
    }

    @Override
    public void setFrom(String from) {

        _from = from;
    }

    @Override
    public void setTo(String to) {

        _to = to;
    }

    @Override
    public void setCc(String cc) {

        _cc = cc;
    }

    @Override
    public void setBcc(String bcc) {

        _bcc = bcc;
    }

    @Override
    public void setReplayTo(String replayto) {

        _replayto = replayto;
    }

    @Override
    public void setSubject(String subject) {

        _subject = subject;
    }

    @Override
    public void setMessage(String message) {

        _message = message;
    }

    @Override
    public String getReplayTo() {

        return _replayto;
    }

    protected MimeMultipart getMimeMultipart() {

        return _mimeMultipart;
    }

    /**
     * Invia il messaggio specificato all'host sulla porta 25.
     * 
     * @param message java.lang.String Il messaggio da spedire.
     * @exception net.project.errors.AppCrash Se si è verificato un errore durante l'invio del messaggio.
     */
    @Override
    public void send(String message) throws AppCrash {

        try {
            ErrDetector.GetInstance().param((_host != null) && (_from != null) && (_to != null) && (message != null));
            _message = message;

            setHostAddress();

            // crea il messaggio
            MimeMessage msg = new MimeMessage(_session);
            ErrDetector.GetInstance().param(msg);

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

            String replayto = getReplayTo();

            if (replayto != null) {
                InternetAddress addressReplayTo = new InternetAddress(replayto);
                msg.setFrom(addressReplayTo);
            }

            MimeBodyPart msgBodyPart = new MimeBodyPart();
            msgBodyPart.setText(_message, Converter.ASCII_ENCODING);
            _mimeMultipart.addBodyPart(msgBodyPart);
            send(msg);
        } catch (Throwable e) {
            AppCrash err = new AppCrash(e);
            err.logContext("SimpleSmtpEMail", toString());
            throw err;
        } finally {
            _mimeMultipart = new MimeMultipart();
        }

    }

    private void setHostAddress() {

        // Set the host smtp address
        Properties props = new Properties();
        props.put("mail.smtp.host", _host);

        // create some properties and get the default Session
        _session = Session.getDefaultInstance(props, null);
        _session.setDebug(false);

    }

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

    protected void send(MimeMessage msg) throws AppCrash {

        try {
            msg.setContent(_mimeMultipart);
            Transport.send(msg);
        } catch (MessagingException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("SimpleSmtpEMail", toString());
            throw err;
        }

    }

    /**
     * Restituisce una stringa riportante host, mittente, destinatario e messaggio dell'e-mail.
     * 
     * @return java.lang.String.
     */
    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer();
        buffer.append("Host: -");
        buffer.append(_host);
        buffer.append("-; Sender: -");
        buffer.append(_from);
        buffer.append("-; Receiver: -");
        buffer.append(_to);
        buffer.append("-; CcReceiver: -");
        buffer.append(_cc);
        buffer.append("-; BccReceiver: -");
        buffer.append(_bcc);
        buffer.append("-; ReplyTo: -");
        buffer.append(_replayto);
        buffer.append("-; Subject: -");
        buffer.append(_subject);
        buffer.append("-; Message: -");
        buffer.append(_message);
        buffer.append("-");
        return buffer.toString();

    }

}
