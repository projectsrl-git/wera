
package project.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
import net.project.misc.Config;

public class InvioMail {

    private String            _subject            = "";

    private String            _server             = "";

    private String            _from               = "";

    private String            _to                 = "";

    private String            _cc                 = "";

    private String            _bcc                = "";

    private String            _body               = "";

    private String            _attachName         = "";

    private String            _localFileName      = "";

    private String            _signFileName       = "";

    private String            _footer             = "";

    private ArrayList<String> _attachmentList     = new ArrayList<String>();
    
    private ArrayList<String> _attachmentNameList = new ArrayList<String>();

    public void invioMail(Authenticator auth, boolean hasAttachment) throws AppCrash {

        Properties props = new Properties();
        props.setProperty("mail.smtp.host", _server);
        props.setProperty("mail.smtp.port", Config.GetInstance().getProperty("mail.SMTPHost.port", "465"));
        props.setProperty("mail.smtp.user", Config.GetInstance().getProperty("mail.SMTPHost", ""));
        props.setProperty("mail.smtp.password", Config.GetInstance().getProperty("mail.SMTPHost.password", ""));

        Session session = null;

        if (auth != null) {
            props.setProperty("mail.smtp.auth", "true");
            session = Session.getDefaultInstance(props, auth);
        } else {
            props.setProperty("mail.smtp.auth", "false");
            session = Session.getDefaultInstance(props);
        }
        
        props.setProperty("mail.smtp.starttls.enable", Config.GetInstance().getProperty("mail.SMTPHost.starttls.enable", "true"));
        props.setProperty("mail.smtp.ssl.enable", Config.GetInstance().getProperty("mail.SMTPHost.ssl.enable", "false"));
        props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");

        Message message = new MimeMessage(session);

        try {
            Address sender = new InternetAddress(_from);

            InternetAddress[] to = parseAddressList(_to);
            InternetAddress[] cc = parseAddressList(_cc);
            InternetAddress[] bcc = parseAddressList(_bcc);

            message.setFrom(sender);
            message.setRecipients(Message.RecipientType.TO, to);
            message.setRecipients(Message.RecipientType.CC, cc);
            message.setRecipients(Message.RecipientType.BCC, bcc);
            message.setSubject(_subject);

            MimeMultipart multipartBody = new MimeMultipart("alternative");
            MimeMultipart multipart1 = new MimeMultipart("related");
            
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText( _body, "utf-8" );

            // Create the message part
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            MimeBodyPart messageBodyPartText = new MimeBodyPart();

            _body = _body.replaceAll("\r\n", "<br>");
            _body = _body.replaceAll("\n", "<br>");

            // Fill the message
            //String body = "<html><body><p><font face='arial' size='2'>" + _body+ "</font></p><p><img src=\"cid:memememe\"><br/>";
            String body = "<html><body><p><font face='arial' size='2'>" + _body
                    + "</font></p><br/></body></html>";

            if (!_footer.equals("")) {
                body = body + _footer;
            }
            messageBodyPartText.setContent(body, "text/plain");
            messageBodyPart.setContent(body, "text/html");
            multipartBody.addBodyPart(messageBodyPartText);
            multipartBody.addBodyPart(messageBodyPart);

            MimeBodyPart mbpBody = new MimeBodyPart();
            mbpBody.setContent(multipartBody);
            multipart1.addBodyPart(mbpBody);

           /* if (!_signFileName.equals("") && (new File(_signFileName)).exists()) {

                MimeBodyPart imagePart = new MimeBodyPart();
                DataSource fds = new FileDataSource(_signFileName);
                imagePart.setDataHandler(new DataHandler(fds));
                imagePart.setHeader("Content-ID", "<memememe>");
                imagePart.setDisposition("inline");
                multipart1.addBodyPart(imagePart);

            }*/

            if (hasAttachment) {

                if (_attachmentList.size() != 0) {

                    Iterator<String> iterName = _attachmentNameList.iterator();

                    for (Iterator<String> iter = _attachmentList.iterator(); iter.hasNext();) {
                        String element = (String) iter.next();
                        String name = (String) iterName.next();

                        // Part two is attachment
                        messageBodyPart = new MimeBodyPart();

                        DataSource source = new FileDataSource(new File(element));
                        messageBodyPart.setDataHandler(new DataHandler(source));
                        messageBodyPart.setFileName(name);
                        multipart1.addBodyPart(messageBodyPart);
                    }

                } else {
                    // Part two is attachment
                    messageBodyPart = new MimeBodyPart();

                    DataSource source = new FileDataSource(new File(_localFileName));
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(_attachName);
                    multipart1.addBodyPart(messageBodyPart);

                }

            }

            message.setContent(multipart1);
            message.setSentDate(new Date());

            Transport.send(message);

        } catch (Throwable e) {
            e.printStackTrace();
            AppCrash ac = new AppCrash(e);
            throw ac;
        }
    }

    public void setBody(String _body) {

        this._body = _body;
    }

    public void setFrom(String _from) {

        this._from = _from;
    }

    public void setServer(String _server) {

        this._server = _server;
    }

    public void setSubject(String _subject) {

        this._subject = _subject;
    }

    public void setTo(String _to) {

        this._to = _to;
    }

    public void setCc(String cc) {

        this._cc = cc;
    }

    public void setBcc(String bcc) {

        this._bcc = bcc;
    }

    public void setAttachName(String name) {

        _attachName = name;
    }

    public void setLocalFileName(String fileName) {

        _localFileName = fileName;
    }

    public void setSignFileName(String fileName) {

        _signFileName = fileName;
    }

    public void setFooter(String footer) {

        _footer = footer;
    }

    public void addAttachment(String name) {

        _attachmentList.add(name);
    }

    public void addAttachmentName(String name) {

        _attachmentNameList.add(name);
    }


    private InternetAddress[] parseAddressList(String list) throws AddressException {

        Vector<InternetAddress> internetAddressVector = new Vector<InternetAddress>();
        String token;
        for (StringTokenizer st = new StringTokenizer(list, ";"); st.hasMoreTokens();) {
            token = st.nextToken().trim();

            internetAddressVector.addElement(new InternetAddress(token));
        }

        InternetAddress internetAddressArray[] = new InternetAddress[internetAddressVector.size()];
        internetAddressVector.copyInto(internetAddressArray);

        return internetAddressArray;
    }

}
