/*
  PageFactory.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione:

  Autore: Pietro G. e Luca M.

  Note:

  Modifiche:

 */

package net.project.servlet.gui;

import java.lang.reflect.Constructor;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;

/**
 * Classe che istanzia le classi che implementano Page_itf.
 * <P>
 * Proprietà lette dal file di configurazione: Page.pageName.Class = nome della classe da istanziare alla richiesta di
 * makePage (vedi metodo makePage)
 * <P>
 * Page.DefaultClass = nome della classe di default da istanziare alla richiesta di makePage (qualora la proprietà
 * Page.pageName.Class non sia valorizzata)
 */
public class PageFactory {

    private static PageFactory    _pageFactory  = null;

    // Costanti indicanti i nomi delle proprietà del file di configurazione
    protected static final String PAGE          = "Page";
    protected static final String CLASS         = "Class";
    protected static final String DEFAULT_CLASS = "DefaultClass";

    /**
     * Costruttore privato.
     * 
     * @roseuid 3A6D460E0305
     */
    private PageFactory() {

    }

    /**
     * Metodo di accesso al singleton.
     * 
     * @return net.project.servlet.gui.PageFactory l'istanza della classe PageFactory
     * @roseuid 3A6D45E20135
     */
    public static PageFactory getInstance() {

        if (_pageFactory == null) {

            synchronized (PageFactory.class) {
                if (_pageFactory == null) {

                    _pageFactory = new PageFactory();
                }
            }
        }

        return _pageFactory;

    }

    /**
     * Crea una pagina. La creazione avviene tramite istanziazione dinamica con Class.forName.
     * <p>
     * Viene letta la proprieta' di configurazione Page.#nomepagina#.Class per determinare la classe da istanziare. La
     * proprieta' puo' contenere un elenco di classi; quella piu' a destra e' la classe base che viene istanziata per
     * prima; tutte le altre devono essere dei decorator ovvere avere un constructor che prende come parametro un
     * Page_itf
     * <p>
     * <p>
     * 
     * @param java.lang.String configName nome della configurazione
     * @param java.lang.String pageName nome della pagina
     * @return net.project.servlet.gui.Page_itf pagina creata
     * @exception net.project.errors.AppCrash
     */
    public Page_itf makePage(String configName, String pageName) throws AppCrash {

        // Controllo formale dei parametri in ingresso.
        ErrDetector.GetInstance().preCond(configName != null, "PageFactory - configName = null");
        boolean pageNameEsistente = (pageName != null) && (pageName.length() > 0);
        ErrDetector.GetInstance().preCond(pageNameEsistente, "PageFactory - pageName non valorizzato");

        // Recupero del nome della classe da istanziare dal file di configurazione.
        String pageClassString = Config.GetInstance(configName).getProperty(PAGE + "." + pageName + "." + CLASS);
        if ((pageClassString == null) || (pageClassString.length() == 0)) {
            pageClassString = Config.GetInstance(configName).getProperty(PAGE + "." + DEFAULT_CLASS);
        }
        boolean pageClassStringEsistente = (pageClassString != null) && (pageClassString.length() > 0);
        ErrDetector.GetInstance().postCond(pageClassStringEsistente,
                "PageFactory - proprietà " + PAGE + "." + DEFAULT_CLASS + " mancante nel file di configurazione");

        // Istanziazione della classe che implementa Page_itf.
        // la proprieta' puo' contenere un elenco di classi; quella piu' a destra e' la classe base che viene
        // istanziata per prima; tutte le altre devono essere dei decorator ovvere avere un constructor
        // che prende come parametro un Page_itf
        Class pageClass = null;
        Constructor constructor = null;
        Page_itf page = null;
        try {
            String[] classes = pageClassString.split(",");
            for (int j = classes.length - 1; j >= 0; j--) {
                pageClass = Class.forName(classes[j]);

                // Cerco un constructor con parametri (String, String)
                try {
                    constructor = pageClass.getConstructor(new Class[] { String.class, String.class });
                    // Istanzio la classe "base"
                    page = (Page_itf) constructor.newInstance(new Object[] { pageName, configName });
                    continue;
                } catch (Throwable e) {
                    // Non ho trovato il constructor per la pagina base; forse e' un
                    // decorator, percio' proseguo
                }
                // Cerco il constructor del decorator con parametri (Page_itf)
                constructor = pageClass.getConstructor(new Class[] { Page_itf.class });
                page = (Page_itf) constructor.newInstance(new Object[] { page });

            }
            return page;

        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("PageFactory", getMakePageStatus(configName, pageName, pageClass, constructor));
            throw ac;
        }

    }

    /**
     * Crea una pagina.
     * 
     * @param java.lang.String pageName nome della pagina
     * @return net.project.servlet.gui.Page_itf pagina creata
     * @exception net.project.errors.AppCrash
     */
    public Page_itf makePage(String pageName) throws AppCrash {

        return makePage("", pageName);
    }

    // Metodo privato che costruisce una stringa con i nomi ed i valori delle variabili locali di makePage
    private String getMakePageStatus(String configName, String page, Class pageClass, Constructor constructor) {

        return "configName = " + configName + "; page = " + page + "; pageClass = " + pageClass + "; constructor = "
                + constructor;

    }

}
