
package net.projectsrl.core;

import java.util.HashMap;

import it.project.webapp.accesscontrol.UtentiDAOLogin;
import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.mail.MyAuthenticator;
import net.projectsrl.wm.mail.InvioMail;
import net.projectsrl.wm.utils.Utils;

/**
 * FunctionInvioMail
 * 
 */
public class FunctionInvioMail extends FunctionCrossover_base {

    public static final String  PAGE        = "invio_mail";

    private static final String _ESTENSIONE = ".pdf";

    public FunctionInvioMail() {

        super();
    }

    public FunctionInvioMail(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @SuppressWarnings("unchecked")
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        HashMap templateData = (HashMap) setCommonTags(req, userInfo);

        templateData = setTemplateDataFromRequest(templateData, req);

        templateData = assegnaOggettiTestiMail(templateData);

        templateData.put("EMAIL_INVIATA", "NO");

        String codiceRichiesta = req.getField("CODICE");
        String attachName = "";

        if (codiceRichiesta.contains("|")) {
            String[] arr = codiceRichiesta.split("\\;");
            attachName = "";

            for (int i = 0; i < arr.length; i++) {
                attachName += Utils.normalizeASCIIFilename(arr[i]) + ";";
            }

        } else {
            attachName = codiceRichiesta;
            attachName = Utils.normalizeASCIIFilename(attachName);

        }

        if (req.getField("ALLEGATO").equals("SI")) {
            templateData.put(ATTACHMENT, attachName);
        }
        _applicationSrv.displayPage(PAGE, templateData, res);
    }

    @SuppressWarnings("unchecked")
    private HashMap assegnaOggettiTestiMail(HashMap templateData) throws AppCrash {

        DataSet_itf dataSet = null;
        String dsName = "DataSetGruppiDestinatariMail";

        DataSetFactory dsFactory = DataSetFactory.getInstance();

        try {
            dataSet = dsFactory.makeDataSet("", dsName);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                if (dbRow != null) {
                    String gruppo = (String) dbRow.getField("GRUPPO");
                    String oggetto = (String) dbRow.getField("LIBERA");
                    String corpo = (String) dbRow.getField("NOTE");

                    templateData.put("OGGETTO" + gruppo, oggetto);
                    templateData.put("CORPO" + gruppo, corpo);
                }
            }
        } catch (AppCrash ac) {
            ac.logContext("FunctionInvioMail.assegnaOggettiTestiMail", "Errore nella ricerca del max del dataset "
                    + dsName);
            throw ac;
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext("FunctionInvioMail.assegnaOggettiTestiMail", "Errore nella close del dataset "
                            + dsName);
                }
            }
        }
        return templateData;
    }

    @SuppressWarnings("unchecked")
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        HashMap templateData = (HashMap) setCommonTags(req, userInfo);

        String user = getSessionUser(req);

        // sessione scaduta o utente non in sessione
        if (user.equals("")) {
            templateData.put("EMAIL_INVIATA", "OK");
            templateData.put(TAG_MESSAGE, Config.GetInstance().getProperty(
                    "Message.email_inviata_ko.utente_non_valido", NO_MESSAGE));
            _applicationSrv.displayPage(PAGE, templateData, res);
            return;
        }

        String from = Config.GetInstance().getProperty("mail.from", "marketing@project-online.it");

        UtentiDAOLogin utente = new UtentiDAOLogin();
        utente.setField(UtentiDAOLogin.USERID, user);
        if (utente.retrieve()) {
            String email = utente.getField(UtentiDAOLogin.EMAIL);
            if (!email.equals("")) {
                from = email;
            } else {
                templateData.put("EMAIL_INVIATA", "OK");
                templateData.put(TAG_MESSAGE, Config.GetInstance().getProperty(
                        "Message.email_inviata_ko.utente_non_valido", NO_MESSAGE));
                _applicationSrv.displayPage(PAGE, templateData, res);
                return;
            }

        } else {
            templateData.put("EMAIL_INVIATA", "OK");
            templateData.put(TAG_MESSAGE, Config.GetInstance().getProperty(
                    "Message.email_inviata_ko.utente_non_valido", NO_MESSAGE));
            _applicationSrv.displayPage(PAGE, templateData, res);
            return;
        }

        templateData = inviaMail(req, templateData, from, user);

        _applicationSrv.displayPage(PAGE, templateData, res);
    }

    @SuppressWarnings("unchecked")
    private HashMap inviaMail(SsbServletRequest req, HashMap templateData, String from, String user) throws AppCrash {

        String elencoDestinatari = req.getField("ID_DETTAGLI_DA_INSERIRE");

        InvioMail invioMail = new InvioMail();

        invioMail.setFrom(from);

        String oggetto = req.getField("OGGETTOINT");
        invioMail.setSubject(oggetto);

        String corpo = req.getField("CORPOINT");
        invioMail.setBody(corpo);

       /* invioMail.setFooter("\n\n" + Config.GetInstance().getProperty("mail.privacy"));*/

        String localFileName = Utils.leggeStringaElencoCampi("modulo_stampa", req.getField("modulo_stampa"))
                + _ESTENSIONE;

        String attachName = Utils.leggeStringaElencoCampi("nome_allegato", req.getField("modulo_stampa"));

        boolean hasAttachment = !attachName.equals("");

        if (hasAttachment) {

            if (attachName.contains("|")) {

                String[] arr = attachName.split("\\|");
                attachName = "";
                String tipoModulo = "_RIDOTTA_INTERNA";
                // _COMPLETA
                // _RIDOTTA_INTERNA
                // _RIDOTTA_ESTERNA

                for (int i = 0; i < arr.length; i++) {

                    if (arr[i].startsWith("_")) {

                    } else {
                        attachName = Utils.normalizeASCIIFilename(arr[i] + tipoModulo + _ESTENSIONE);

                        localFileName = "";

                        invioMail.addAttachmentName(attachName);
                        invioMail.addAttachment(localFileName);
                    }
                }

            } else {
                attachName = Utils.normalizeASCIIFilename(attachName + _ESTENSIONE);

                localFileName = "";

                // if ((new File(localFileName)).exists()) {
                invioMail.setLocalFileName(localFileName);
                invioMail.setAttachName(attachName);
                // }
            }
        }

        @SuppressWarnings("unused")
		String firmaDigitale = _applicationSrv.getRoot() + "img/logo_project.gif";

        // String firmaDigitale = user+".gif";

       /* invioMail.setSignFileName(firmaDigitale);*/

        invioMail.setTo(elencoDestinatari);

        invioMail.setServer(Config.GetInstance().getProperty("mail.SMTPHost"));

        try {
            MyAuthenticator auth = null;
            if (!Config.GetInstance().getProperty("mail.SMTPHost.user", "").equals("")) {
                auth = new MyAuthenticator();

            }

            invioMail.invioMail(auth, hasAttachment);
            templateData.put("EMAIL_INVIATA", "OK");
            templateData.put(TAG_MESSAGE, Config.GetInstance().getProperty("Message.email_inviata_ok", NO_MESSAGE));


        } catch (Throwable e) {
            templateData.put("EMAIL_INVIATA", "OK");
            templateData.put(TAG_MESSAGE, Config.GetInstance().getProperty("Message.email_inviata_ko", NO_MESSAGE));
            new AppCrash(e);
        }

        return templateData;
    }

}
