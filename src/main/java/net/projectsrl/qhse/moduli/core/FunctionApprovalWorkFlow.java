
package net.projectsrl.qhse.moduli.core;

import java.io.PrintWriter;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.alibow.core.Constants_itf;
import net.projectsrl.alibow.core.StatiRichiesta;
import net.projectsrl.qhse.mail.SendMail;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.wera.base.db.AliModDAO_base;
import net.projectsrl.wera.ripartizioni.db.RipartizioniDAO;
import net.projectsrl.wera.ripartizioniletture.db.RipartizioniLettureDAO;
import net.projectsrl.wera.ripartizioniuni.db.RipartizioniUNIDAO;
import net.projectsrl.wera.ripartizioniuni2018.db.RipartizioniUNI2018DAO;
import project.misc.Utils;

public class FunctionApprovalWorkFlow extends FunctionProjectWebApp_base {

    public FunctionApprovalWorkFlow(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        boolean result = false;
        String title = "";
        String message = "";
        String lang = "";
        AliModDAO_base dao = null;

        String idModulo = null;

        try {

            String modulo = req.getField(Constants_itf.MODULO);
            ErrDetector.GetInstance().param(Utils.IsNotEmpty(modulo), "parameter modulo is empty");

            idModulo = req.getField("ID_MODULO");
            ErrDetector.GetInstance().param(Utils.IsNotEmpty(idModulo), "parameter idModulo is empty");

            String workFlowAction = req.getField(Constants_itf.WORKFLOW_ACTION);
            ErrDetector.GetInstance().param(Utils.IsNotEmpty(workFlowAction), "parameter workFlowAction is empty");

            String operationType = "UPDATE";

            try {
                lang = getSpecificUserInfo(userInfo).getField(WebAppConstants_itf.CURRENT_SELECTED_ISO_LANGUAGE);
            } catch (Throwable e) {
                lang = "it";
            }

            if (modulo.equals(Constants_itf.RIPARTIZIONI_UNI)) {
                dao = new RipartizioniUNIDAO();
            } else if (modulo.equals(Constants_itf.RIPARTIZIONI)) {
                dao = new RipartizioniDAO();
            } else if (modulo.equals(Constants_itf.RIPARTIZIONI_LETTURE)) {
                dao = new RipartizioniLettureDAO();
            } else if (modulo.equals(Constants_itf.RIPARTIZIONI_UNI_2018)) {
                dao = new RipartizioniUNI2018DAO();
            }

            dao.setAttribute(AliModDAO_base.ID_MODULO, idModulo);
            ErrDetector.GetInstance().preCond(dao.retrieve(), "no data found - idModulo:" + idModulo);

            workFlowManager(userInfo, dao, workFlowAction, modulo, idModulo);

            result = true;

            title = Config.GetInstance().getProperty(lang + ".SaveFormMessageTitle.Success." + operationType,
                    "SaveFormMessage - no message title");
            message = Config.GetInstance().getProperty(lang + ".SaveFormMessageText.Success." + operationType,
                    "SaveFormMessage - no message text");

        } catch (Throwable th) {

            result = false;
            title = Config.GetInstance().getProperty(lang + ".SaveFormMessageTitle.Error",
                    "SaveFormMessage - no message title");
            message = Config.GetInstance().getProperty(lang + ".SaveFormMessageText.Error",
                    "SaveFormMessage - no message text");

            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "errore salvataggio dati");

        } finally {

            sendResponseJSON(res, result, title, message, dao);
        }
    }

    private void workFlowManager(UserSecurityInfo userInfo, AliModDAO_base dao, String workFlowAction, String modulo, String idModulo)
            throws AppCrash {

        String statoIniziale = (String) dao.getAttribute(AliModDAO_base.STATO);
        String statoFinale = statoIniziale;

        SendMail mail = new SendMail(getSpecificUserInfo(userInfo));

        if (workFlowAction.equals(Constants_itf.APPROVE)) {

            if (statoIniziale.equals(StatiRichiesta.DRAFT.getCode())) {

                if (modulo.equals(Constants_itf.MODULO_CHECKLIST_AGENZIE)) {
                    statoFinale = StatiRichiesta.COMPLETED.getCode();
                } else {
                    statoFinale = StatiRichiesta.WAITNG_FOR_FIRST_APPROVAL.getCode();
                }

                dao.setAttribute(AliModDAO_base.STATO, statoFinale);
                dao.update();

                //mail.sendMailAliMOD(dao, workFlowAction, statoIniziale, statoFinale, idModulo);

            } else if (statoIniziale.equals(StatiRichiesta.WAITNG_FOR_FIRST_APPROVAL.getCode())) {

                statoFinale = StatiRichiesta.APPROVED.getCode();

                dao.setAttribute(AliModDAO_base.STATO, statoFinale);
                dao.update();

                
                //mail.sendMailAliMOD(dao, workFlowAction, statoIniziale, statoFinale, idModulo);
                //mail.sendMailAliMOD(dao, Constants_itf.NOTIFY, statoIniziale, statoFinale, idModulo);
           
            }

        } else if (workFlowAction.equals(Constants_itf.ROLLBACK)) {

            if (statoIniziale.equals(StatiRichiesta.WAITNG_FOR_FIRST_APPROVAL.getCode())) {

                statoFinale = StatiRichiesta.DRAFT.getCode();
                dao.setAttribute(AliModDAO_base.STATO, statoFinale);
                dao.update();

                //mail.sendMailAliMOD(dao, Constants_itf.ROLLBACK, statoIniziale, statoFinale, idModulo);
            } else if (statoIniziale.equals("WQP")) {
                statoFinale = "WAI";
                dao.setAttribute(AliModDAO_base.STATO, statoFinale);
                dao.update();
                
                //mail.sendMailAliMOD(dao, Constants_itf.ROLLBACK, statoIniziale, statoFinale, idModulo);

            } else if (statoIniziale.equals("APP")) {
                statoFinale = "DRA";
                dao.setAttribute(AliModDAO_base.STATO, statoFinale);
                dao.update();
                
                //mail.sendMailAliMOD(dao, Constants_itf.ROLLBACK, statoIniziale, statoFinale, idModulo);
            }

        } else  if (workFlowAction.equals(Constants_itf.COMPLETED)) {

            if (statoIniziale.equals(StatiRichiesta.DRAFT.getCode())) {
               
                statoFinale = StatiRichiesta.APPROVED.getCode();

                dao.setAttribute(AliModDAO_base.STATO, statoFinale);
                dao.update();

                //mail.sendMailAliMOD(dao, workFlowAction, statoIniziale, statoFinale, idModulo);

            }
            
            
            if (statoIniziale.equals(StatiRichiesta.WAITNG_FOR_FIRST_APPROVAL.getCode())) {
                
                statoFinale = StatiRichiesta.APPROVED.getCode();

                dao.setAttribute(AliModDAO_base.STATO, statoFinale);
                dao.update();

                //mail.sendMailAliMOD(dao, workFlowAction, statoIniziale, statoFinale, idModulo);

            }

        } 

    }

   
    protected void sendResponseJSON(SsbServletResponse res, boolean result, String title, String message,
            AliModDAO_base dao) {

        try {
            PrintWriter out = res.getWriter();

            Integer id = (Integer) dao.getAttribute(AliModDAO_base.ID_MODULO);
            String nr = (String) dao.getAttribute(AliModDAO_base.NR_MODULO);
            String stato = (String) dao.getAttribute(AliModDAO_base.STATO);

            String resultString = "{\"result\":" + result + ",\"title\":'" + title + "',\"message\":'" + message
                    + "',\"id\":" + id + ",\"nr\":'" + nr + "',\"stato\":'" + stato + "'}";
            out.println(resultString);
            out.close();

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "errore writing succesful response");
        }
    }

}
