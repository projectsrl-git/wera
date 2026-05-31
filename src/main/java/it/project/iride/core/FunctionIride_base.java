
package it.project.iride.core;

import it.project.webapp.core.FunctionWebApp_base;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;

public abstract class FunctionIride_base extends FunctionWebApp_base implements Costanti_itf {

    public FunctionIride_base() {

        super();
    }

    public FunctionIride_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    protected void loadDataFromSession(HttpSession session, HashMap<String, Object> templateData) throws AppCrash {

        super.loadDataFromSession(session, templateData);

        templateData.put("DEALER", getSessionDealer(session));
        templateData.put("UTENTE_EV", getSessionUser(session));
        templateData.put("DATA_ULTIMA_ELABORAZIONE", (session.getAttribute("DATA_ULTIMA_ELABORAZIONE")));
        templateData.put("ORA_ULTIMA_ELABORAZIONE", (session.getAttribute("ORA_ULTIMA_ELABORAZIONE")));
    }

    /**
     * Recupera il codice utente dalla sessione
     * 
     * @param SsbServletRequest req
     * 
     * @return String user codice utente
     * @throws AppCrash
     */
    protected String getSessionDealer(SsbServletRequest req) {

        HttpSession session = req.getSession(false);

        return getSessionDealer(session);
    }

    /**
     * Recupera il codice utente dalla sessione
     * 
     * @param SsbServletRequest req
     * 
     * @return String user codice utente
     * @throws AppCrash
     */
    protected String getSessionDealer(HttpSession session) {

        String codiceDealer = (String) session.getAttribute("DEALER");

        return codiceDealer;
    }

    /**
     * true se l'utente di sessione è un dealer
     * 
     * SsbServletRequest req
     * 
     * @return boolean
     */
    protected boolean isDealer(SsbServletRequest req) {

        return RUOLO_DEALER.equals(getSessionRole(req));
    }

}
