package net.projectsrl.wera.scheduled;

import java.util.HashMap;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.projectsrl.mail.DeferredMailSender;
import net.projectsrl.mail.SendSMTPMail;

public class JobMailCreditiScadenza implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		String primoAlert=Config.GetInstance().getProperty("AlertCrediti.Primo");
        String secondoAlert=Config.GetInstance().getProperty("AlertCrediti.Secondo");
        String terzoAlert=Config.GetInstance().getProperty("AlertCrediti.Terzo");
        String quartoAlert=Config.GetInstance().getProperty("AlertCrediti.Quarto");

        String sender = Config.GetInstance().getProperty("mail.from.service");

       
        String body= Config.GetInstance().getProperty("CreditiScadenza.Mail.Body");
        String object = Config.GetInstance().getProperty("CreditiScadenza.Mail.Subject");
        String recipients=getMailAziendaPrincipale();
        String recipientsCC="";
        String recipientsBCC="";
       
        
        mailAlert(sender, body, object, recipients, recipientsCC, recipientsBCC, primoAlert);
        mailAlert(sender, body, object, recipients, recipientsCC, recipientsBCC, secondoAlert);
        mailAlert(sender, body, object, recipients, recipientsCC, recipientsBCC, terzoAlert);
        mailAlert(sender, body, object, recipients, recipientsCC, recipientsBCC, quartoAlert);

	}



	private String getMailAziendaPrincipale() {
		String mail=""; 
		DataSet_itf dataSet = null;        
	        
	        try {

	            DataSetFactory dsFactory = DataSetFactory.getInstance();

	            dsFactory = DataSetFactory.getInstance();
	            dataSet = dsFactory.makeDataSet("", "DSAziendaPrincipale");
	            dataSet.open();
	            

	            while (dataSet.hasMoreElements()) {
	                Row_itf dbRow = (Row_itf) dataSet.nextElement();
	                mail=dbRow.getField("MAIL").toString().trim().toLowerCase();
	            }
	            
	            dataSet.close();

		        } catch (Throwable t) {
		            AppCrash ac = new AppCrash(t);
		            ac.logContext(this.getClass().getName(), "errore nell'esecuzione del Job");
		        } finally {
		            if (dataSet != null) {
		                try {
		                    dataSet.close();
		                } catch (AppCrash ac) {
		                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
		                }
		            }
		        }
	        return mail;
	}



	private void mailAlert(String sender, String body, String object, String recipients, String recipientsCC,
			String recipientsBCC, String alert) {
		
		String ragioneSociale = "";
	    String dataScadenzaCrediti = "";
	    String giorniScadenzaCrediti = "";
	    String creditiAcquistati = "";
	    String creditiUtilizzati= "";
	    String mail  ="";

		body+="<table><font face='Arial' size='2'><tr>" +
        		"<td bgcolor='#e7771e'><font color='white'><b>Ragione sociale</b></font></td>" +
        		"<td bgcolor='#e7771e'><font color='white'><b>Data scadenza crediti</b></font></td>" +
        		"<td bgcolor='#e7771e'><font color='white'><b>Giorni alla scadenza</b></font></td>" +
        		"<td bgcolor='#e7771e'><font color='white'><b>Crediti acquistati</b></font></td>" +
                "<td bgcolor='#e7771e'><font color='white'><b>Crediti utilizzati</b></font></td>" +
                "<td bgcolor='#e7771e'><font color='white'><b>Mail</b></font></td></tr>";
        
        DataSet_itf dataSet = null;        
        
        try {

            DataSetFactory dsFactory = DataSetFactory.getInstance();

            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", "DSCrediti");
            HashMap<String, String> params = new HashMap<String, String>();
            //params.put("WHERECONDITION_AZIENDE", getSpecificUserInfo(userInfo).getField(DafneCostanti_itf.WHERECONDITION_AZIENDE));
            params.put("WHERECONDITION_AZIENDE"," IS NOT NULL ");
            params.put("WHERECONDITION"," WHERE 1=1 AND DATE_PART('day',to_date(data_scadenza_crediti,'YYYY/MM/DD')::timestamp without time zone -now())::int ="+alert+" order by DATE_PART('day',to_date(data_scadenza_crediti,'YYYY/MM/DD')::timestamp without time zone -now())::int ,ragsoc");
            dataSet.setParam(params);
            dataSet.open();
            

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                
                ragioneSociale=dbRow.getField("RAGSOC").toString().trim().toUpperCase();
                dataScadenzaCrediti=dbRow.getField("DATA_SCADENZA_CREDITI").toString().trim();
                giorniScadenzaCrediti=dbRow.getField("SCADENZA_GIORNI").toString().trim();
                creditiAcquistati=dbRow.getField("CREDITI_ACQUISTATI").toString().trim();
                creditiUtilizzati=dbRow.getField("CREDITI_UTILIZZATI").toString().trim();
                mail=dbRow.getField("MAIL").toString().trim().toLowerCase();
                	
            	if (!ragioneSociale.isEmpty()){
                    body+="<tr>"+
                			"<td><font color='black'> "+ragioneSociale+" </font></td>" +
                			"<td><font color='black'> "+dataScadenzaCrediti+" </font></td>" +
                			"<td><font color='black'> "+giorniScadenzaCrediti+" </font></td>" +
                			"<td><font color='black'> "+creditiAcquistati+" </font></td>" +
                			"<td><font color='black'> "+creditiUtilizzati+" </font></td>" +
	                        "<td><font color='black'> "+mail+" </font></td></tr>";                    	
            	}
            
	            body+="</table>";
	            
	            if (!recipients.isEmpty() || !recipientsCC.isEmpty() || !recipientsBCC.isEmpty()){
	            	send( sender,  recipients,  recipientsCC,  recipientsBCC,  object,  body);
	            }
            }
            
            

            dataSet.close();

	        } catch (Throwable t) {
	            AppCrash ac = new AppCrash(t);
	            ac.logContext(this.getClass().getName(), "errore nell'esecuzione del Job");
	        } finally {
	            if (dataSet != null) {
	                try {
	                    dataSet.close();
	                } catch (AppCrash ac) {
	                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
	                }
	            }
	        }
	}
	
	
	
	 public void send(String sender, String recipients, String recipientsCC, String recipientsBCC, String subject, String body) throws AppCrash {

	        SendSMTPMail sendSMTPMail=null;
	        
	        try {

	            sendSMTPMail = new SendSMTPMail();
	            sendSMTPMail.setFrom(sender);
	            sendSMTPMail.setCc(recipientsCC);
	            sendSMTPMail.setBcc(recipientsBCC);
	            sendSMTPMail.setSubject(subject);
	            sendSMTPMail.setBody(body);
	            sendSMTPMail.setTo(recipients);
	            sendSMTPMail.setServer(Config.GetInstance().getProperty("mail.SMTPHost"));
	            sendSMTPMail.setUsername(Config.GetInstance().getProperty("mail.from.service"));

	            sendSMTPMail.prepareMail();
	            DeferredMailSender.getInstance().offer(sendSMTPMail);

	        } catch (Throwable e) {
	            AppCrash ac = new AppCrash(e);
	            ac.logContext(this.getClass().getName(),
	                    "error sending mail sendMailApprover - sendSMTPMail: " + sendSMTPMail.toString());
	        }
	    }
	


}
