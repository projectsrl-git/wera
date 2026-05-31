
package net.projectsrl.wm.mail;

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

public class InvioMail {

    private String    _subject            = "";

    private String    _server             = "";

    private String    _from               = "";

    private String    _to                 = "";

    private String    _body               = "";

    private String    _attachName         = "";

    private String    _localFileName      = "";

    private String    _signFileName       = "";

    private String    _footer             = "";

    @SuppressWarnings("unchecked")
	private ArrayList _attachmentList     = new ArrayList();
    @SuppressWarnings("unchecked")
	private ArrayList _attachmentNameList = new ArrayList();

    @SuppressWarnings("unchecked")
	public void invioMail(Authenticator auth, boolean hasAttachment) throws AppCrash {

        Properties props = new Properties();
        props.setProperty("mail.smtp.host", _server);

        Session session = null;
        
        if (auth != null) {
            props.setProperty("mail.smtp.auth", "true");
            session = Session.getDefaultInstance(props, auth);
        } else {
            props.setProperty("mail.smtp.auth", "false"); 
            session = Session.getDefaultInstance(props);
        }

        Message message = new MimeMessage(session);

        try {
            Address sender = new InternetAddress(_from);

            InternetAddress[] recipient = parseAddressList(_to);

            message.setFrom(sender);
            // message.setRecipient(Message.RecipientType.TO, sender);
            message.setRecipients(Message.RecipientType.BCC, recipient);
            message.setSubject(_subject);

            MimeMultipart multipartBody = new MimeMultipart("alternative");
            MimeMultipart multipart1 = new MimeMultipart("related");

            // Create the message part html
            MimeBodyPart messageBodyPart = new MimeBodyPart();

            _body = _body.replaceAll("\r\n", "<br>");
            _body = _body.replaceAll("\n", "<br>");
            _body = _body.replaceAll("/n", "</br>");
            

            // Fill the message html
            String body = "<html><body><p><font face='arial' size='2'>" + _body
                    + "</font></p></body></html>";

            if (!_footer.equals("")) {
                body = body + _footer;
            }
            
           // Create the message part text
            MimeBodyPart messageBodyPartText = new MimeBodyPart();

            _body = _body.replaceAll("\r\n", ".");
            _body = _body.replaceAll("\n", ".");
            _body = _body.replaceAll("/n", "");
            

            messageBodyPart.setContent(body, "text/html");
            multipartBody.addBodyPart(messageBodyPart);
         // Create the message part text
            messageBodyPartText.setContent(body, "text/plain");
            multipartBody.addBodyPart(messageBodyPartText);

            
            MimeBodyPart mbpBody = new MimeBodyPart();
            mbpBody.setContent(multipartBody);
            multipart1.addBodyPart(mbpBody);
            
            

            if (!_signFileName.equals("") && (new File(_signFileName)).exists()) {

                MimeBodyPart imagePart = new MimeBodyPart();
                DataSource fds = new FileDataSource(_signFileName);
                imagePart.setDataHandler(new DataHandler(fds));
                imagePart.setHeader("Content-ID", "<memememe>");
                imagePart.setDisposition("inline");
                multipart1.addBodyPart(imagePart);

            }

            if (hasAttachment) {

                if (_attachmentList.size() != 0) {

                    Iterator iterName = _attachmentNameList.iterator();

                    for (Iterator iter = _attachmentList.iterator(); iter.hasNext();) {
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

    public void setAttachName(String name) {

        _attachName = name;
    }

    public void setLocalFileName(String fileName) {

        _localFileName = fileName;
    }

/*    public void setSignFileName(String fileName) {

        _signFileName = fileName;
    }*/

  /*  public void setFooter(String footer) {

        _footer = footer;
    }*/

    @SuppressWarnings("unchecked")
	public void addAttachment(String name) {

        _attachmentList.add(name);
    }

    @SuppressWarnings("unchecked")
	public void addAttachmentName(String name) {

        _attachmentNameList.add(name);
    }

    @SuppressWarnings("unchecked")
    private InternetAddress[] parseAddressList(String list) throws AddressException {

        Vector v = new Vector();
        String token;
        for (StringTokenizer st = new StringTokenizer(list, ";"); st.hasMoreTokens();) {
            token = st.nextToken().trim();

            v.addElement(new InternetAddress(token));
        }

        InternetAddress array[] = new InternetAddress[v.size()];
        v.copyInto(array);

        return array;
    }

}
