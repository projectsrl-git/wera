package net.projectsrl.wera.centraletermica.core;

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
 * FUNCTIONID=CentraleTermica: pagina della Centrale termica (template centraletermica.html).
 * I dati arrivano in JSON da CentraleTermicaDati; qui solo il controllo di accesso e la cornice.
 * <p>
 * La voce NON e' nel menu del DB (condiviso con /wera): percorso, menu e voce attiva si costruiscono
 * qui, cosi' non scatta la ClassCastException delle funzioni fuori menu (SELECTED_MENU_ITEM in
 * sessione e' una stringa).
 */
public class FunctionCentraleTermica extends FunctionCentraleTermica_base {

    static final String PATH_CENTRALE_TERMICA = "CT";

    protected String    _titolo               = "Centrale termica";

    public FunctionCentraleTermica() {

        super();
    }

    public FunctionCentraleTermica(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        mostra(req, res, userInfo);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        ContestoUtente ctx = new ContestoUtente(getSpecificUserInfo(userInfo));
        boolean abilitato = ctx.isAbilitato();
        memorizzaAccesso(req, ctx, abilitato);
        if (!abilitato) {
            registraEvento(ctx, CentraleTermicaCostanti.EVENTO_ACCESSO_NEGATO, null, null, null, null,
                    "pagina Centrale termica");
            sendRedirect(res, "Home");
            return;
        }

        String titolo = ctx.parametro(CentraleTermicaCostanti.PAR_ETICHETTA_TITOLO, ctx.getIdAzienda());
        if (titolo.length() > 0) {
            _titolo = titolo;
        }

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);
        templateData.put("CT_PAGINA", "S");
        templateData.put("CT_SCHEDA", scheda());
        templateData.put("CT_TITOLO", _titolo);
        Integer idCondominio = intero(req.getParameter("ID_CONDOMINIO"));
        templateData.put("CT_ID_CONDOMINIO", idCondominio == null ? "" : idCondominio.toString());

        _applicationSrv.displayPage(getPageName(), templateData, res);
    }

    /** Sottovoce del menu "Centrale termica" evidenziata (CT_SCHEDA nel template). */
    protected String scheda() {

        return "panoramica";
    }

    /** Percorso nella topbar (le voci sono separate da "/"). */
    protected String percorso() {

        return "Home / " + _titolo;
    }

    /** Link delle voci del percorso, separati da ";" (l'ultima voce non e' un link). */
    protected String catenaLink() {

        return "astro?FUNCTIONID=Home;astro?FUNCTIONID=" + getMenuID();
    }

    /** Numero di voci del percorso con link. */
    protected int livelloPercorso() {

        return 1;
    }

    /**
     * Percorso "Home / Centrale termica" e menu della pagina, costruiti come FunctionProjectWebApp_base.addMenu
     * farebbe per una voce del DB.
     */
    @Override
    protected String getPathDescription(Map<String, Object> map) {

        String pathDescri = percorso();
        Object selezionato = map.get(WebAppConstants_itf.SELECTED_MENU_ITEM);
        if (selezionato instanceof MenuItem) {
            return pathDescri;
        }

        String lingua = (String) map.get(WebAppConstants_itf.CURRENT_SELECTED_ISO_LANGUAGE);
        if (lingua == null) {
            lingua = "it";
        }
        MenuItem voce = new MenuItem("CT", "0", lingua, _titolo, getMenuID(), "astro?FUNCTIONID=" + getMenuID(),
                livelloPercorso(), PATH_CENTRALE_TERMICA, pathDescri, catenaLink(), false, 0, "fa fa-fire");
        map.put(WebAppConstants_itf.SELECTED_MENU_ITEM, voce);
        map.put(WebAppConstants_itf.PAGE_TITLE, pathDescri);
        map.put(WebAppConstants_itf.SELECTED_PATH, PATH_CENTRALE_TERMICA);
        map.put(WebAppConstants_itf.INCLUDED_MENU, Config.GetInstance().getProperty("Page.DefaultMenuName"));

        // menu nella lingua corrente, come addMenu (in sessione ci sono tutte le lingue)
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
