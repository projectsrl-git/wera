
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
 * FunctionInvioMailLink
 * 
 */
public class FunctionInvioMailLink extends FunctionCrossover_base {

    public static final String PAGE = "invio_mail_link";

    public FunctionInvioMailLink() {

        super();
    }

    public FunctionInvioMailLink(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @SuppressWarnings("unchecked")
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        HashMap templateData = (HashMap) setCommonTags(req, userInfo);

        templateData = setTemplateDataFromRequest(templateData, req);

        templateData = assegnaOggettiTestiMail(templateData, req);

        templateData.put("EMAIL_INVIATA", "NO");

        String codiceRichiesta = req.getField("CODICE");
        String attachName = codiceRichiesta;
        attachName = Utils.normalizeASCIIFilename(attachName);

        if (req.getField("ALLEGATO").equals("SI")) {
            templateData.put(ATTACHMENT, attachName);
        }

        _applicationSrv.displayPage(PAGE, templateData, res);
    }

    @SuppressWarnings("unchecked")
    private HashMap assegnaOggettiTestiMail(HashMap templateData, SsbServletRequest req) throws AppCrash {

        @SuppressWarnings("unused")
		String linkDaInviare = "";
        linkDaInviare = componeLinkDaInviare(templateData, req);

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
                    @SuppressWarnings("unused")
					String oggetto = (String) dbRow.getField("LIBERA");
                    @SuppressWarnings("unused")
					String corpo = (String) dbRow.getField("NOTE");

                    templateData.put("OGGETTO" + gruppo, "Tracciato Utenti Campagne");
                    templateData.put("CORPO" + gruppo,
                            "Cliccare sul collegamento sottostante per visualizzare la ricerca impostata\n\n");
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

    @SuppressWarnings({ "unchecked", "null" })
	private String componeLinkDaInviare(HashMap templateData, SsbServletRequest req) {

        // Ricavo dalla SsbServletRequest req il parametro LINK_DA_INVIARE
        // contenente il link che voglio inviare via mail.
        // Devo però BUCARE l'autenticazione e passare anche l'utente all'accesso dell'applicazione.
        // Per questo prevedo un parametro USERID in cui metterò l'utente di ASTRO desunto dalla e-mail
        // del destinatario + parametro TIPO_LOGIN=LINK per bucare la login ed eseguire direttamente il link
        // ---------------------
        // DOVREBBE ESSERE COSI' però per ora lasciamo che la persona acceda come utente che ha inviato

        String linkDaInviare = req.getField("LINK_DA_INVIARE");
        if (linkDaInviare == null && linkDaInviare.equals("")) {
            return "";
        }

        linkDaInviare = linkDaInviare + "&TIPO_LOGIN=LINK";

        String userDestinatario = getSessionUser(req);

        linkDaInviare = linkDaInviare + "&USERID=" + userDestinatario;

        return linkDaInviare;
    }

    @SuppressWarnings("unchecked")
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        HashMap templateData = (HashMap) setCommonTags(req, userInfo);

        String user = getSessionUser(req);

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

        String elencoDestinatari = req.getField("ID_DETTAGLI_DA_INSERIRE");

        InvioMail invioMail = new InvioMail();

        invioMail.setFrom(from);

        String oggetto = req.getField("OGGETTOINT");
        invioMail.setSubject(oggetto);

        String corpo = req.getField("CORPOINT");
        // invioMail.setBody(corpo+
        // " <a href='http://www.crossoverweb.eu:8080/astroweb/astro?FUNCTIONID=BrowseRichieste&codice=&RICALTER=&d_datarich_da=&d_datarich_a=&d_scadenza_da=&d_scadenza_a=&Sedelavor=&Anaaziric=&anaaziRefi=&ANAAZCLIF=&data_evento_da=&data_evento_a=&data_prossimo_evento_da=&data_prossimo_evento_a=&RICHIESTE_APERTE=on' >ESITO RICERCA</a>"
        // );
        String getRicerca = concatPreviousRequestDataInString(req);
        invioMail.setBody(corpo + " <a href='http://webserver:8080/astroweb/astro?" + getRicerca
                + "' >Tracciato Utenti Campagne</a>");

        /*invioMail.setFooter("\n\n" + Config.GetInstance().getProperty("mail.privacy"));*/

        @SuppressWarnings("unused")
		String firmaDigitale = _applicationSrv.getRoot() + "img/logo_project.gif";

       /* invioMail.setSignFileName(firmaDigitale);
*/
        invioMail.setTo(elencoDestinatari);

        invioMail.setServer(Config.GetInstance().getProperty("mail.SMTPHost"));

        try {
            MyAuthenticator auth = null;
            if (!Config.GetInstance().getProperty("mail.SMTPHost.user", "").equals("")) {
                auth = new MyAuthenticator();

            }
            boolean hasAttachment = false;
            invioMail.invioMail(auth, hasAttachment);

            templateData.put("EMAIL_INVIATA", "OK");
            templateData.put(TAG_MESSAGE, Config.GetInstance().getProperty("Message.email_inviata_ok", NO_MESSAGE));

        } catch (Throwable e) {
            templateData.put("EMAIL_INVIATA", "OK");
            templateData.put(TAG_MESSAGE, Config.GetInstance().getProperty("Message.email_inviata_ko", NO_MESSAGE));
            new AppCrash(e);
        } finally {

            _applicationSrv.displayPage(PAGE, templateData, res);
        }
    }

}
