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

public class JobMailErroriRiepilogo implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

        String sender = Config.GetInstance().getProperty("mail.from.service");

        String condominio = "";
        String indirizzo = "";
        String capLocProv = "";
        //String azienda= "";
        String totAnomalie  ="";
        String errore       ="";

        String body="";
        String recipients="";
        String recipientsCC="";
        String recipientsBCC="";
        String object = "WEBCREDIT WERA - Alert rilevatori in errore - riepilogo";
        
        DataSet_itf dataSet = null;
        
        
        body+="Gentile utente,\n/nQuesta è una mail inviata automaticamente da WERA.\n/n";
        body+="<b>RIEPILOGO ERRORI</b>\n";
        body+="<table><font face='Arial' size='2'><tr>" +
        		"<td bgcolor='#e7771e'><font color='white'><b>Condominio</b></font></td>" +
        		"<td bgcolor='#e7771e'><font color='white'><b>Indirizzo</b></font></td>" +
        		"<td bgcolor='#e7771e'><font color='white'><b>Località</b></font></td>" +
                "<td bgcolor='#e7771e'><font color='white'><b>Errore</b></font></td>" +
                "<td bgcolor='#e7771e'><font color='white'><b>Totale errori</b></font></td></tr>";
        
        
        
        try {

            DataSetFactory dsFactory = DataSetFactory.getInstance();

            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", "DSGestioneRilevatoriErroreRiepilogo");
            HashMap<String, String> params = new HashMap<String, String>();
            //params.put("WHERECONDITION_AZIENDE", getSpecificUserInfo(userInfo).getField(DafneCostanti_itf.WHERECONDITION_AZIENDE));
            params.put("WHERECONDITION_AZIENDE"," IS NOT NULL ");
            params.put("WHERECONDITION"," WHERE 1=1 ");
            dataSet.setParam(params);
            dataSet.open();
            

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                
                	condominio=dbRow.getField("CONDOMINIO").toString().trim().toUpperCase();
                	
                	if (!condominio.isEmpty()){
                		recipients = dbRow.getField("LISTA_MAIL").toString().toLowerCase();
                	    
                        indirizzo = dbRow.getField("INDIRIZZO").toString().trim().toUpperCase();
                        capLocProv = dbRow.getField("LOCALITA").toString().trim().toUpperCase()+" ("+dbRow.getField("PROVINCIA").toString().trim().toUpperCase()+")";
                            
                        errore = dbRow.getField("ANOMALIA").toString().trim()+" - "+dbRow.getField("DESCRI_ANOMALIA").toString().trim();
                        totAnomalie = dbRow.getField("TOTALE_ANOMALIE").toString().trim();
                         

                        body+="<tr>"+
                    			"<td><font color='black'> "+condominio+" </font></td>" +
                    			"<td><font color='black'> "+indirizzo+" </font></td>" +
                    			"<td><font color='black'> "+capLocProv+" </font></td>" +
    	                        "<td><font color='black'> "+errore+" </font></td>" +
    	                        "<td align='right'><font color='black'> "+totAnomalie+" </font></td></tr>";
                    	
                    	
                	}
                	
                    
            }
            
            body+="</table>";
            
            if (!recipients.isEmpty() || !recipientsCC.isEmpty() || !recipientsBCC.isEmpty()){
            	send( sender,  recipients,  recipientsCC,  recipientsBCC,  object,  body);
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
