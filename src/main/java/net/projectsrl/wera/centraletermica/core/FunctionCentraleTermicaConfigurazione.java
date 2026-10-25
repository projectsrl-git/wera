package net.projectsrl.wera.centraletermica.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;

/**
 * FUNCTIONID=CentraleTermicaConfigurazione: pagina di configurazione (template
 * centraletermicaconfigurazione.html) per i profili di CONFIGURAZIONE.PROFILI (default: superuser).
 * Dati da CentraleTermicaDati&OPERAZIONE=config, salvataggi da CentraleTermicaConfigurazioneSalva.
 */
public class FunctionCentraleTermicaConfigurazione extends FunctionCentraleTermica {

    public FunctionCentraleTermicaConfigurazione() {

        super();
    }

    public FunctionCentraleTermicaConfigurazione(ApplicationServices_itf applServices, String functionID,
            String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        ContestoUtente ctx = new ContestoUtente(getSpecificUserInfo(userInfo));
        memorizzaAccesso(req, ctx, ctx.isAbilitato());
        if (!ctx.puoConfigurare()) {
            registraEvento(ctx, CentraleTermicaCostanti.EVENTO_ACCESSO_NEGATO, null, null, null, null,
                    "pagina configurazione Centrale termica");
            sendRedirect(res, ctx.isAbilitato() ? "CentraleTermica" : "Home");
            return;
        }
        String titolo = ctx.parametro(CentraleTermicaCostanti.PAR_ETICHETTA_TITOLO, ctx.getIdAzienda());
        if (titolo.length() > 0) {
            _titolo = titolo;
        }

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);
        templateData.put("CT_PAGINA", "S");
        templateData.put("CT_TITOLO", _titolo);
        _applicationSrv.displayPage(getPageName(), templateData, res);
    }

    @Override
    protected String percorso() {

        return "Home / " + _titolo + " / Configurazione";
    }

    @Override
    protected String catenaLink() {

        return "astro?FUNCTIONID=Home;astro?FUNCTIONID=CentraleTermica;astro?FUNCTIONID=" + getMenuID();
    }

    @Override
    protected int livelloPercorso() {

        return 2;
    }
}
