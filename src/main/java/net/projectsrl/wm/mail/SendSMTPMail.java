
package net.projectsrl.wm.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import net.project.errors.AppCrash;
import net.project.misc.Config;

public class SendSMTPMail {

	private String _subject = "";

	private String _server = "";

	private String _from = "";

	private String _to = "";

	private String _bcc = "";

	private String _cc = "";

	private String _body = "";

	private String _attachName = "";

	private String _localFileName = "";

	private String _signFileName = "";

	private String _footer = "";

	private List<String> _attachmentList = new ArrayList<String>();

	private List<String> _attachmentNameList = new ArrayList<String>();

	Message _message = null;

	public void prepareMail(Authenticator auth, boolean hasAttachment, String percorso, String documento,
			String grafica) throws AppCrash {

		Properties props = new Properties();
		// props.setProperty("mail.smtp.host", _server);

		Session session = null;

		/*
		 * if (auth != null) { props.setProperty("mail.smtp.auth", "true");
		 * session = Session.getDefaultInstance(props, auth); } else {
		 * props.setProperty("mail.smtp.auth", "false"); session =
		 * Session.getDefaultInstance(props); }
		 * 
		 * session = Session.getDefaultInstance(props, auth);
		 */
		props.setProperty("mail.smtp.host", _server);
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(
						Config.GetInstance().getProperty("mail.SMTPHost.user", "noreply@projectsrl.net"),
						Config.GetInstance().getProperty("mail.SMTPHost.password", "automatica"));
			}
		});

		_message = new MimeMessage(session);

		try {
			Address sender = new InternetAddress(_from);

			InternetAddress[] recipient = parseAddressList(_to);
			InternetAddress[] recipientBcc = parseAddressList(_bcc);
			InternetAddress[] recipientCc = parseAddressList(_cc);

			_message.setFrom(sender);
			_message.setRecipients(Message.RecipientType.TO, recipient);
			_message.setRecipients(Message.RecipientType.BCC, recipientBcc);
			_message.setRecipients(Message.RecipientType.CC, recipientCc);
			_message.setSubject(_subject);

			MimeMultipart multipartBody = new MimeMultipart("alternative");
			MimeMultipart multipart1 = new MimeMultipart("related");

			// Create the message part html
			MimeBodyPart messageBodyPart = new MimeBodyPart();

			// Fill the message html
			String body = "";
			if (grafica.equals("")) {
				_body = _body.replaceAll("\r\n", "<br>");
				_body = _body.replaceAll("\n", "<br>");
				_body = _body.replaceAll("/n", "</br>");

				body = "<html><body><p><font face='arial' size='2'>" + _body + "</font></p></body></html>";
			} else {
				body = _body;
			}

			if (!_footer.equals("")) {
				body = body + _footer;
			}

			// Create the message part text
			MimeBodyPart messageBodyPartText = new MimeBodyPart();

			_body = _body.replaceAll("\r\n", ".");
			_body = _body.replaceAll("\n", ".");
			_body = _body.replaceAll("/n", "");
			_body = _body.replaceAll("<br/>", "");
			_body = _body.replaceAll("<i>", "");
			_body = _body.replaceAll("</i>", "");
			_body = _body.replaceAll("<b>", "");
			_body = _body.replaceAll("</b>", "");

			String bodyPlain = _body;

			messageBodyPart.setContent(body, "text/html");
			multipartBody.addBodyPart(messageBodyPart);
			// Create the message part text
			// messageBodyPartText.setContent(bodyPlain, "text/plain");
			messageBodyPartText.setContent(body, "text/html");
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
				File att = new File(new File(documento.replaceAll("//", "/")) + "");
				messageBodyPart.attachFile(att);
			}

			_message.setContent(multipart1);
			_message.setSentDate(new Date());

		} catch (Throwable e) {
			e.printStackTrace();
			AppCrash ac = new AppCrash(e);
			throw ac;
		}
	}

	public void sendMail() throws AppCrash {

		try {
			Transport.send(_message);

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

	public void setBcc(String _bcc) {

		this._bcc = _bcc;
	}

	public void setCc(String _cc) {

		this._cc = _cc;
	}

	public void setAttachName(String name) {

		_attachName = name;
	}

	public void setLocalFileName(String fileName) {

		_localFileName = fileName;
	}

	public void addAttachment(String name) {

		_attachmentList.add(name);
	}

	public void addAttachmentName(String name) {

		_attachmentNameList.add(name);
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

}
