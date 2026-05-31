package net.projectsrl.wm.cambiopassword.core;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.core.FunctionInserimentoSenzaControlloPreEsistenza;
import net.projectsrl.mail.MyAuthenticator;
import net.projectsrl.wm.mail.SendSMTPMail;
import net.projectsrl.wm.mail.DeferredMailSender;
import net.projectsrl.wm.utils.Utils;

/**
 * FunctionCambioPassword
 * 
 */
public class FunctionCambioPassword extends FunctionInserimentoSenzaControlloPreEsistenza {

	private static final String PAGE = "cambio_password";

	public FunctionCambioPassword() {
		super();
		setPageMostra(PAGE);
		setPageElabora(PAGE);
		setDatasetTestata("DataSetCambioPassword");
	}

	public FunctionCambioPassword(ApplicationServices_itf applServices, String functionID, String functionName) {

		super(applServices, functionID, functionName);
		setPageMostra(PAGE);
		setPageElabora(PAGE);		
		setDatasetTestata("DataSetCambioPassword");
	}

	@SuppressWarnings("unchecked")
	protected HashMap loadVar(HashMap templateData, SsbServletRequest req) throws AppCrash {
		HttpSession session = req.getSession(true);

		templateData.put(OPZIONE_INSERIMENTO_MODIFICA, OPZIONE_INSERIMENTO);
		templateData.put("USERID", session.getAttribute("USER"));
		templateData.put("NOME", session.getAttribute("USER_NOME"));
		templateData.put("COGNOME", session.getAttribute("USER_COGNOME"));
		templateData.put("EMAIL", session.getAttribute("USER_MAIL"));
		templateData.put("RUOLO_SESSIONE", session.getAttribute("RUOLO"));
		templateData.put("RUOLO_SESSIONE", session.getAttribute("RUOLO_SESSIONE"));
		templateData.put("DATA",Utils.getStringDataOggi());	
		templateData.put("ATTIVO", session.getAttribute("ATTIVO"));
		templateData.put("ID_CODICE", session.getAttribute("ID_CODICE"));	
		templateData.put("AZIENDA", session.getAttribute("AZIENDA_SESSIONE"));		
		templateData.put("ID_DIPENDENTE", session.getAttribute("ID_DIPENDENTE_SESSIONE"));		
		templateData.put("PWD_SCADUTA", session.getAttribute("PWD_SCADUTA"));		
		return templateData;
	}
	
	public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {
        HashMap<String, Object> templateData = setTemplateDataFromRequest(setCommonTags(req, userInfo), req);
        saveVarStandard(templateData, req,res);
        String destinatari_mail = req.getField("EMAIL");
        String nominativo = req.getField("NOME")+" "+req.getField("COGNOME");
        String new_pwd = req.getField("PASSWORD");
        templateData.put("SALVATO", "Salvataggio effettuato con successo");
	    templateData.put("SALVATO_REMINDER", "Ultimo salvataggio effettuato alle ore "+Utils.getOrario());
        String from = Config.GetInstance().getProperty("mail.from", "noreply@projectsrl.net");
        templateData = inviaMail(req, templateData, from, new_pwd, destinatari_mail, nominativo);
        _applicationSrv.displayPage(PAGE, templateData, setPageDatasetParam(PAGE, req, templateData), res);
	}
	
	
	@SuppressWarnings({ "unchecked" })
    private HashMap inviaMail(SsbServletRequest req, HashMap templateData, String from, String new_pwd, String destinatari_mail, String nominativo) throws AppCrash {
	    String portale = Config.GetInstance().getProperty("indirizzo.portale");
        String elencoDestinatari = destinatari_mail;
        
        String oggetto = "Nuova password DAFNE";
        
        String corpo = "Gentile "+nominativo+",\n/nQuesta è una mail inviata automaticamente da DAFNE.\n/n";
    	corpo += "E' stato aggiornata la sua password per l'accesso a <a href='"+portale+"'>"+portale+"</a>, qui di seguito le credenziali per l'accesso al portale:\n" +
		"/n";
	    corpo += "<i>Userid</i>: " + destinatari_mail + "\n/n";
	    corpo += "<i>Password</i>: " + new_pwd + "\n/n";
	    corpo += "\n/nAcceda al sistema utilizzando la nuova password. \n\n/n/nCordiali Saluti\n/n<i>Il Team DAFNE</i>";

        
	    System.out.println(elencoDestinatari);
        SendSMTPMail sendSMTPMail = new SendSMTPMail();
        sendSMTPMail.setFrom(from);
        sendSMTPMail.setSubject(oggetto);
        sendSMTPMail.setBody(corpo);
        sendSMTPMail.setTo(elencoDestinatari);
        sendSMTPMail.setServer(Config.GetInstance().getProperty("mail.SMTPHost"));

        try {
            MyAuthenticator auth = null;
            if (!Config.GetInstance().getProperty("mail.SMTPHost.user", "").equals("")) {
                auth = new MyAuthenticator();
            }
            sendSMTPMail.prepareMail(auth, false, "", "", "S");

            DeferredMailSender.getInstance().offer(sendSMTPMail);
            templateData.put("EMAIL_INVIATA", "OK");
            templateData.put("EMAIL_INVIATA_MESSAGE",
                    Config.GetInstance().getProperty("Message.email_inviata_ok", NO_MESSAGE));

        } catch (Throwable e) {
            templateData.put("EMAIL_INVIATA", "KO");
            templateData.put("EMAIL_INVIATA_MESSAGE",
                    Config.GetInstance().getProperty("Message.email_inviata_ko", NO_MESSAGE));
            new AppCrash(e);
        }
        
        return templateData;
    }

}

