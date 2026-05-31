
package net.projectsrl.qhse.mail;

import java.util.HashMap;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.projectsrl.alibow.core.Constants_itf;
import net.projectsrl.dafne.db.UtentiDAO;
import net.projectsrl.dafne.richieste.core.StatiRichiesta;
import net.projectsrl.mail.DeferredMailSender;
import net.projectsrl.mail.SendSMTPMail;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.webapp.security.WebAppUserSecurityInfo;
import net.projectsrl.wera.base.db.AliModDAO_base;
import net.projectsrl.wera.ripartizioni.db.RipartizioniDAO;
import net.projectsrl.wera.ripartizioniletture.db.RipartizioniLettureDAO;
import net.projectsrl.wera.ripartizioniuni.db.RipartizioniUNIDAO;
import net.projectsrl.wera.ripartizioniuni2018.db.RipartizioniUNI2018DAO;

public class SendMail {

    private static final String DATASET_DISTRIBUTION_LIST = "DSDistributionList";
    private static final String DATASET_DESTINATARI = "DSDestinatariMailRipartizioni";
    
    
    private WebAppUserSecurityInfo<?> _userInfo;

    public SendMail(WebAppUserSecurityInfo<?>  userInfo) {

        _userInfo = userInfo;
    }
    
    
    public void sendMailAliMOD(AliModDAO_base dao, String workFlowAction, String statoIniziale, String statoFinale, String idModulo) throws AppCrash {

        DataSet_itf dataSet = null;

        try {

            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_DISTRIBUTION_LIST);

            HashMap<String, String> params = new HashMap<String, String>();
            
            String modulo=dao.getTableName();
            
            String whereCondition="";
            
            if (workFlowAction.equals(Constants_itf.APPROVE) && statoFinale.equals(StatiRichiesta.WAITNG_FOR_FIRST_APPROVAL.getCode())) {
                whereCondition+=" WHERE ";
                whereCondition+=" MODULO ='"+modulo+"' ";
                whereCondition+=" AND ";
                whereCondition+=" WORKFLOW_ACTION ='"+workFlowAction+"' ";
                whereCondition+=" AND ";
                whereCondition+=" STATO_INIZIALE ='"+statoIniziale+"' ";
                whereCondition+=" AND ";
                whereCondition+=" STATO_FINALE ='"+statoFinale+"' ";
                whereCondition+=" AND ";
                whereCondition+=" ID_AZIENDA ="+dao.getAttribute(AliModDAO_base.ID_AZIENDA)+" ";
            } else if (workFlowAction.equals(Constants_itf.APPROVE) && statoFinale.equals(StatiRichiesta.APPROVED.getCode())) {
                whereCondition+=" WHERE ";
                whereCondition+=" MODULO ='"+modulo+"' ";
                whereCondition+=" AND ";
                whereCondition+=" WORKFLOW_ACTION ='"+workFlowAction+"' ";
                whereCondition+=" AND ";
                whereCondition+=" STATO_INIZIALE ='"+statoIniziale+"' ";
                whereCondition+=" AND ";
                whereCondition+=" STATO_FINALE ='"+statoFinale+"' ";
            } else if (workFlowAction.equals(Constants_itf.COMPLETED) && statoFinale.equals(StatiRichiesta.APPROVED.getCode())) {
                whereCondition+=" WHERE ";
                whereCondition+=" MODULO ='"+modulo+"' ";
                whereCondition+=" AND ";
                whereCondition+=" WORKFLOW_ACTION ='"+workFlowAction+"' ";
                whereCondition+=" AND ";
                whereCondition+=" STATO_INIZIALE ='"+statoIniziale+"' ";
                whereCondition+=" AND ";
                whereCondition+=" STATO_FINALE ='"+statoFinale+"' ";
            } else if (workFlowAction.equals(Constants_itf.ROLLBACK) && statoFinale.equals(StatiRichiesta.DRAFT.getCode())) {
                whereCondition+=" WHERE ";
                whereCondition+=" MODULO ='"+modulo+"' ";
                whereCondition+=" AND ";
                whereCondition+=" WORKFLOW_ACTION ='"+workFlowAction+"' ";
                whereCondition+=" AND ";
                whereCondition+=" STATO_INIZIALE ='"+statoIniziale+"' ";
                whereCondition+=" AND ";
                whereCondition+=" STATO_FINALE ='"+statoFinale+"' ";
            } else if (workFlowAction.equals(Constants_itf.NOTIFY) && statoFinale.equals(StatiRichiesta.APPROVED.getCode())) {
                whereCondition+=" WHERE ";
                whereCondition+=" MODULO ='"+modulo+"' ";
                whereCondition+=" AND ";
                whereCondition+=" WORKFLOW_ACTION ='"+workFlowAction+"' ";
                whereCondition+=" AND ";
                whereCondition+=" STATO_INIZIALE ='"+statoIniziale+"' ";
                whereCondition+=" AND ";
                whereCondition+=" STATO_FINALE ='"+statoFinale+"' ";
                whereCondition+=" AND ";
                whereCondition+=" ID_AZIENDA ="+dao.getAttribute(AliModDAO_base.ID_AZIENDA)+" ";
            }
            
            
            params.put(WebAppConstants_itf.WHERECONDITION, whereCondition);

            dataSet.setParam(params);
            dataSet.open();

            if (!dataSet.hasMoreElements()) {
                return;
            }
            
            Row_itf dbRow = (Row_itf) dataSet.nextElement();
            String subject = (String) dbRow.getField("OGGETTO");
            String body = (String) dbRow.getField("TESTO_MAIL");
            String sender=(String) dbRow.getField("MITTENTE");
            String recipients = (String) dbRow.getField("DESTINATARI_TO");
            String recipientsBCC=(String) dbRow.getField("DESTINATARI_BCC");
            String recipientsCC="";
            
            if (workFlowAction.equals(Constants_itf.APPROVE) && statoFinale.equals(StatiRichiesta.WAITNG_FOR_FIRST_APPROVAL.getCode())) {
                UtentiDAO utente= new UtentiDAO();
                utente.setAttribute(UtentiDAO.ID_UTENTE, dao.getAttribute(AliModDAO_base.ID_UTENTE_INS));
                ErrDetector.GetInstance().preCond(utente.retrieve(),"user does not exists");
                recipientsCC= (String) utente.getAttribute(UtentiDAO.EMAIL);
               
            } else if (workFlowAction.equals(Constants_itf.APPROVE) && statoFinale.equals(StatiRichiesta.APPROVED.getCode())) {
                UtentiDAO utente= new UtentiDAO();
                utente.setAttribute(UtentiDAO.ID_UTENTE, dao.getAttribute(AliModDAO_base.ID_UTENTE_INS));
                ErrDetector.GetInstance().preCond(utente.retrieve(),"user does not exists");
                
                
                if (modulo.equals(Constants_itf.RIPARTIZIONI_UNI) || modulo.equals(Constants_itf.RIPARTIZIONI) || modulo.equals(Constants_itf.RIPARTIZIONI_LETTURE) || modulo.equals(Constants_itf.RIPARTIZIONI_UNI_2018)) {
                	recipients= getDestinatari(modulo,idModulo);
                }else{
                	recipients= (String) utente.getAttribute(UtentiDAO.EMAIL);
                }
                
                
                utente= new UtentiDAO();
                utente.setAttribute(UtentiDAO.ID_UTENTE, _userInfo.getIdUtente());
                ErrDetector.GetInstance().preCond(utente.retrieve(),"user does not exists");
                recipientsCC= (String) utente.getAttribute(UtentiDAO.EMAIL);
            } else if (workFlowAction.equals(Constants_itf.COMPLETED) && statoFinale.equals(StatiRichiesta.APPROVED.getCode())) {
                UtentiDAO utente= new UtentiDAO();
                utente.setAttribute(UtentiDAO.ID_UTENTE, dao.getAttribute(AliModDAO_base.ID_UTENTE_INS));
                ErrDetector.GetInstance().preCond(utente.retrieve(),"user does not exists");
                
                if (modulo.equals(Constants_itf.RIPARTIZIONI_UNI) || modulo.equals(Constants_itf.RIPARTIZIONI) || modulo.equals(Constants_itf.RIPARTIZIONI_LETTURE) || modulo.equals(Constants_itf.RIPARTIZIONI_UNI_2018)) {
                	recipients= getDestinatari(modulo,idModulo);
                }else{
                	recipients= (String) utente.getAttribute(UtentiDAO.EMAIL);
                }
                
                utente= new UtentiDAO();
                utente.setAttribute(UtentiDAO.ID_UTENTE, _userInfo.getIdUtente());
                ErrDetector.GetInstance().preCond(utente.retrieve(),"user does not exists");
                recipientsCC= (String) utente.getAttribute(UtentiDAO.EMAIL);
            } else if (workFlowAction.equals(Constants_itf.ROLLBACK) && statoFinale.equals(StatiRichiesta.DRAFT.getCode())) {
                UtentiDAO utente= new UtentiDAO();
                utente.setAttribute(UtentiDAO.ID_UTENTE, dao.getAttribute(AliModDAO_base.ID_UTENTE_INS));
                ErrDetector.GetInstance().preCond(utente.retrieve(),"user does not exists");
                recipients= (String) utente.getAttribute(UtentiDAO.EMAIL);
                
                utente= new UtentiDAO();
                utente.setAttribute(UtentiDAO.ID_UTENTE, _userInfo.getIdUtente());
                ErrDetector.GetInstance().preCond(utente.retrieve(),"user does not exists");
                recipientsCC= (String) utente.getAttribute(UtentiDAO.EMAIL);
            }
            
            
            
            subject = subject.replace("#NR_MODULO#", (String) dao.getAttribute(AliModDAO_base.NR_MODULO));
            subject = subject.replace("#DT_MODULO#", (String)dao.getAttribute(AliModDAO_base.DT_MODULO));

            body = body.replace("#ID_MODULO#",dao.getAttributeAsString(AliModDAO_base.ID_MODULO));

            send( sender,  recipients,  recipientsCC,  recipientsBCC,  subject,  body);
            
            
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            throw ac;
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
            sendSMTPMail.setUsername(_userInfo.getUserId());

            sendSMTPMail.prepareMail();
            DeferredMailSender.getInstance().offer(sendSMTPMail);

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(),
                    "error sending mail sendMailApprover - sendSMTPMail: " + sendSMTPMail.toString());
        }
    }
	
	

    private String getDestinatari(String modulo, String idModulo) throws AppCrash {

        String dati = "";
        DataSet_itf dataSet = null;
        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_DESTINATARI);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("TABELLA_RIPARTIZIONI",  modulo );
            params.put("ID_MODULO", "'" + idModulo + "'");
            dataSet.setParam(params);
            dataSet.open();
            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
               	dati = dbRow.getField("EMAIL").toString().trim();
            }
            dataSet.close();
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            throw ac;
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (AppCrash ac) {
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
                }
            }
        }
        return dati;
    }


}
