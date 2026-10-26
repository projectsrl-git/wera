package net.projectsrl.wera.registrazioni;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.authentication.MenuItem;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.webapp.security.WebAppUserSecurityInfo;

/**
 * FUNCTIONID=RegistrazioniRichieste: pagina delle richieste di registrazione (template
 * registrazionirichieste.html), solo per i profili di APPROVAZIONE.PROFILI. I dati arrivano in JSON da
 * RegistrazioniDati, le azioni vanno a RegistrazioniEsito. La voce non e' nel menu del DB (condiviso con
 * /wera): percorso e voce attiva si costruiscono qui, come per la Centrale termica.
 */
public class FunctionRegistrazioniRichieste extends FunctionRegistrazioni_base {

    static final String PATH_REGISTRAZIONI = "REG";
    static final String TITOLO             = "Richieste di registrazione";

    public FunctionRegistrazioniRichieste(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        mostra(req, res, userInfo);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Parametri p = Parametri.carica();
        Approvatore a = new Approvatore(getSpecificUserInfo(userInfo), p);
        memorizzaAccesso(req, a);
        if (!a.isAbilitato()) {
            Eventi.registra(RegistrazioniCostanti.EVENTO_ACCESSO_NEGATO, null, a.getIdUtente(), a.getProfilo(),
                    req.getRemoteAddr(), "pagina richieste di registrazione");
            sendRedirect(res, "Home");
            return;
        }
        Map<String, Object> dati = createMapFromRequest(req, userInfo);
        dati.put("REG_PAGINA", "S");
        Integer id = FunctionRegistrazioniDati.intero(req.getParameter("ID_RICHIESTA"));
        dati.put("REG_ID_RICHIESTA", id == null ? "" : id.toString());
        dati.put("REG_MAIL_ATTIVA", p.vero(RegistrazioniCostanti.PAR_MAIL_ATTIVA, true) ? "S" : "N");
        _applicationSrv.displayPage(getPageName(), dati, res);
    }

    /** Percorso "Home / Richieste di registrazione" e menu della pagina (come FunctionCentraleTermica). */
    @Override
    protected String getPathDescription(Map<String, Object> map) {

        String pathDescri = "Home / " + TITOLO;
        Object selezionato = map.get(WebAppConstants_itf.SELECTED_MENU_ITEM);
        if (selezionato instanceof MenuItem) {
            return pathDescri;
        }
        String lingua = (String) map.get(WebAppConstants_itf.CURRENT_SELECTED_ISO_LANGUAGE);
        if (lingua == null) {
            lingua = "it";
        }
        MenuItem voce = new MenuItem(PATH_REGISTRAZIONI, "0", lingua, TITOLO, getMenuID(),
                "astro?FUNCTIONID=" + getMenuID(), 1, PATH_REGISTRAZIONI, pathDescri,
                "astro?FUNCTIONID=Home;astro?FUNCTIONID=" + getMenuID(), false, 0, "fa fa-user-plus");
        map.put(WebAppConstants_itf.SELECTED_MENU_ITEM, voce);
        map.put(WebAppConstants_itf.PAGE_TITLE, pathDescri);
        map.put(WebAppConstants_itf.SELECTED_PATH, PATH_REGISTRAZIONI);
        map.put(WebAppConstants_itf.INCLUDED_MENU, Config.GetInstance().getProperty("Page.DefaultMenuName"));
        Object menu = map.get(WebAppUserSecurityInfo.MENU_LIST);
        if (menu instanceof List) {
            List<MenuItem> menuInPagina = new ArrayList<MenuItem>();
            for (Object item : (List<?>) menu) {
                if (item instanceof MenuItem && lingua.equals(((MenuItem) item).getIsoLanguage())) {
                    menuInPagina.add((MenuItem) item);
                }
            }
            map.put(WebAppUserSecurityInfo.MENU_LIST, menuInPagina);
        }
        return pathDescri;
    }
}
