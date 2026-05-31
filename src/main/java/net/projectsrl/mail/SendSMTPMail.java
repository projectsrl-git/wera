
package net.projectsrl.mail;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.misc.Config;
import net.project.misc.Util;

public class SendSMTPMail {

    private SerializableMail _mailMessage;

    public SendSMTPMail() {

        _mailMessage = new SerializableMail();
    }

    public SendSMTPMail(SerializableMail mailMessage) {

        _mailMessage = mailMessage;
    }

    private MimeMessage createMimeMessage() throws AppCrash {

        String smtpUser = Config.GetInstance().getProperty("mail.SMTPHost.user");

        ErrDetector.GetInstance().invariant(Util.IsNotEmpty(smtpUser), "mail.SMTPHost.user is empty");

        MimeMessage message = null;

        boolean hasAttachment = false;
        String documento = null;

        if (_mailMessage != null && _mailMessage.getAttachmentList() != null
                && _mailMessage.getAttachmentList().size() > 0) {
            hasAttachment = true;
            documento = _mailMessage.getAttachmentList().get(0);
        }

        Properties props = new Properties();
        props.setProperty("mail.smtp.host", _mailMessage.getHost());
        props.setProperty("mail.smtp.port", _mailMessage.getPort());

        String startTls = Config.GetInstance().getProperty("mail.SMTPHost.starttls.enable", "false");
        props.setProperty("mail.smtp.starttls.enable", startTls);
        props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = null;

        try {
            props.setProperty("mail.smtp.auth", "true");
            Authenticator auth = new MyAuthenticator();
            session = Session.getDefaultInstance(props, auth);
            message = new MimeMessage(session);

            Address sender = new InternetAddress(_mailMessage.getFrom());

            InternetAddress[] recipient = parseAddressList(_mailMessage.getTo());
            InternetAddress[] recipientBcc = parseAddressList(_mailMessage.getBcc());
            InternetAddress[] recipientCc = parseAddressList(_mailMessage.getCc());

            message.setFrom(sender);
            message.setRecipients(Message.RecipientType.TO, recipient);
            message.setRecipients(Message.RecipientType.BCC, recipientBcc);
            message.setRecipients(Message.RecipientType.CC, recipientCc);
            message.setSubject(_mailMessage.getSubject());

            MimeMultipart multipartBody = new MimeMultipart("alternative");
            MimeMultipart multipart1 = new MimeMultipart("related");

            // Create the message part html
            // MimeBodyPart messageBodyPart = new MimeBodyPart();

            String htmlBody = _mailMessage.getBody();

            htmlBody = htmlBody.replaceAll("\r\n", "<br>");
            htmlBody = htmlBody.replaceAll("\n", "<br>");
            htmlBody = htmlBody.replaceAll("/n", "</br>");

            // Fill the message html
            String body = "<html><body><p><font face='arial' size='2'>" + htmlBody + "</font></p></body></html>";

            if (!_mailMessage.getFooter().equals("")) {
                body = body + _mailMessage.getFooter();
            }

            // Create the message part text
            MimeBodyPart messageBodyPartText = new MimeBodyPart();
            MimeBodyPart messageBodyPartTextPlain = new MimeBodyPart();
            String textBody = htmlBody;

            textBody = textBody.replaceAll("\r\n", ".");
            textBody = textBody.replaceAll("\n", ".");
            textBody = textBody.replaceAll("/n", "");
            textBody = textBody.replaceAll("<br/>", "");
            textBody = textBody.replaceAll("<br>", "\r\n");
            textBody = textBody.replaceAll("<i>", "");
            textBody = textBody.replaceAll("</i>", "");
            textBody = textBody.replaceAll("<b>", "");
            textBody = textBody.replaceAll("</b>", "");

            String bodyPlain = _mailMessage.getBody();

            // messageBodyPart.setContent(body, "text/html");
            // multipartBody.addBodyPart(messageBodyPart);
            // Create the message part text
            messageBodyPartTextPlain.setContent(bodyPlain, "text/plain");
            messageBodyPartText.setContent(body, "text/html");
            multipartBody.addBodyPart(messageBodyPartTextPlain);
            multipartBody.addBodyPart(messageBodyPartText);
            

            MimeBodyPart mbpBody = new MimeBodyPart();
            mbpBody.setContent(multipartBody);
            multipart1.addBodyPart(mbpBody);

            if (!_mailMessage.getSignFileName().equals("") && (new File(_mailMessage.getSignFileName())).exists()) {

                MimeBodyPart imagePart = new MimeBodyPart();
                DataSource fds = new FileDataSource(_mailMessage.getSignFileName());
                imagePart.setDataHandler(new DataHandler(fds));
                imagePart.setHeader("Content-ID", "<memememe>");
                imagePart.setDisposition("inline");
                multipart1.addBodyPart(imagePart);

            }

            if (hasAttachment) {
                MimeBodyPart attachBodyPart = new MimeBodyPart();
                File att = new File(new File(documento.replaceAll("//", "/")) + "");
                attachBodyPart.attachFile(att);
                multipart1.addBodyPart(attachBodyPart);
            }

            message.setContent(multipart1);
            message.setSentDate(new Date());

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            throw ac;
        }

        return message;
    }

    public void prepareMail() throws AppCrash {

        String smtpUser = Config.GetInstance().getProperty("mail.SMTPHost.user");

        ErrDetector.GetInstance().invariant(Util.IsNotEmpty(smtpUser), "mail.SMTPHost.user is empty");

        Authenticator auth = new MyAuthenticator();

        boolean hasAttachment = false;
        String percorso = null;
        String documento = null;

        if (_mailMessage != null && _mailMessage.getAttachmentList() != null
                && _mailMessage.getAttachmentList().size() > 0) {
            hasAttachment = true;
            percorso = "";
            documento = _mailMessage.getAttachmentList().get(0);
        }

        prepareMail(auth, hasAttachment, percorso, documento);
    }

    public void prepareMail(String percorso, String documento) throws AppCrash {

        boolean hasAttachment = true;

        prepareMail(null, hasAttachment, percorso, documento);
    }

    public void prepareMail(Authenticator auth, boolean hasAttachment, String percorso, String documento)
            throws AppCrash {

        if (hasAttachment && Util.IsNotEmpty(documento)) {
            _mailMessage.addAttachment(documento);
        }

    }

    public void sendMail() throws AppCrash {

        MimeMessage message = createMimeMessage();

        try {
            _mailMessage.setLastAttemp(System.currentTimeMillis());
            Transport.send(message);
            Logger.GetInstance().log0("Email sent succesfully - " + toString());

        } catch (Throwable e) {
            e.printStackTrace();
            AppCrash ac = new AppCrash(e);

            ac.logContext(this.getClass().getName(), toString());

            throw ac;
        }
    }

    public void setBody(String body) {

        _mailMessage.setBody(body);
    }

    public void setFrom(String from) {

        _mailMessage.setFrom(from);
    }

    public void setServer(String server) {

        _mailMessage.setHost(server);
    }

    public void setSubject(String subject) {

        _mailMessage.setSubject(subject);
    }

    public void setTo(String to) {

        _mailMessage.setTo(to);
    }

    public void setBcc(String bcc) {

        _mailMessage.setBcc(bcc);
    }

    public void setCc(String cc) {

        _mailMessage.setCc(cc);
    }

    public void setUsername(String username) {

        _mailMessage.setUsername(username);
    }

    public void addAttachment(String name) {

        _mailMessage.addAttachment(name);
    }

    private InternetAddress[] parseAddressList(String list) throws AddressException {

        Vector<InternetAddress> v = new Vector<InternetAddress>();
        String token;
        for (StringTokenizer st = new StringTokenizer(list, ";"); st.hasMoreTokens();) {
            token = st.nextToken().trim();

            v.addElement(new InternetAddress(token));
        }

        InternetAddress array[] = new InternetAddress[v.size()];
        v.copyInto(array);

        return array;
    }

    public SerializableMail getMailMessage() {

        return _mailMessage;
    }

    @Override
    public String toString() {

        return _mailMessage.toString();

    }

}
