package net.projectsrl.wera.centraletermica.core;

import net.project.servlet.frame.ApplicationServices_itf;

/**
 * Pagine dello scadenziario (template centraletermicascadenziario.html, mockup wera_demo), registrate in
 * functionList con tre nomi, uno per sottovoce del menu "Centrale termica":
 * CentraleTermicaScadenze (Scadenze e documenti), CentraleTermicaCalendario, CentraleTermicaAvvisi
 * (Alerting e storico invii). Stessa pagina, scheda iniziale diversa. Accesso e menu come la pagina
 * principale (FunctionCentraleTermica); i dati arrivano da CentraleTermicaDati.
 */
public class FunctionCentraleTermicaScadenziario extends FunctionCentraleTermica {

    public FunctionCentraleTermicaScadenziario() {

        super();
    }

    public FunctionCentraleTermicaScadenziario(ApplicationServices_itf applServices, String functionID,
            String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    protected String getPageName() {

        return "centraletermicascadenziario";
    }

    @Override
    protected String scheda() {

        String nome = getName();
        if ("CentraleTermicaCalendario".equals(nome)) {
            return "calendario";
        }
        if ("CentraleTermicaAvvisi".equals(nome)) {
            return "avvisi";
        }
        return "scadenze";
    }

    @Override
    protected String percorso() {

        String voce = "calendario".equals(scheda()) ? "Calendario"
                : ("avvisi".equals(scheda()) ? "Alerting e storico invii" : "Scadenze e documenti");
        return "Home / " + _titolo + " / " + voce;
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
