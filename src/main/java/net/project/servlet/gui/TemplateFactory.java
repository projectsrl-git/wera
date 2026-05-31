/*
  TemplateFactory.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione:

  Autore: Pietro G. e Luca M.

  Note:

  Modifiche:

 */

package net.project.servlet.gui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.misc.Config;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * Classe che costruisce Template. E' un singleton.
 * <P>
 * Proprietà lette dal file di configurazione: Page.TemplateRoot = nome della directory contenente i template
 * Page.FilenameSuffix = suffisso dei file che sono template (opzionale) Page.Delay = secondi che intercorrono tra due
 * aggiornamenti consecutivi della cache (opzionle: default = 5) Page.Encoding = character encoding (opzionale)
 * Page.NumberFormatOption = formattazione dei numeri tramite separatore delle migliaia del locale corrente: e'
 * opzionale, e puo' valere true (formattazione eseguita) oppure false (formattazione non eseguita); qualsiasi altro
 * valore e' ignorato ed e' pari al default (false); NON e' case sensitive Page.StrictSyntaxOption = richiesta sintassi
 * piu' restrittiva di freemarker (esempio: tag if preceduto da #) e' opzionale, e puo' valere true (sintassi
 * restrittiva richiesta) oppure false (sintassi restrittiva non richiesta); qualsiasi altro valore e' ignorato ed e'
 * pari al default (false); NON e' case sensitive. Page.TemplateException = se ugiale a 'prod' usa un handler che non
 * stampa lo stack trace a video in questo modo l'utente finale non lo vede
 *
 */

public class TemplateFactory implements TemplateFactory_itf {

    private static Map         _TemplateFactoryRepository = new HashMap();

    // Valori costanti di proprietà del file di configurazione
    public static final String PAGE                       = "Page";
    public static final String TEMPLATE_ROOT              = "TemplateRoot";
    public static final String DELAY                      = "Delay";
    public static final String ENCODING                   = "Encoding";
    public static final String FILENAME_SUFFIX            = "FilenameSuffix";
    public static final String TRUE                       = "true";
    public static final String FALSE                      = "false";
    public static final String NO_NUMBER_FORMAT           = "0.#########";

    // Valore costante del numero dei secondi di delay (tra due aggiornamenti consecutivi della cache) di default
    public static final String DEFAULT_DELAY              = "5";

    private String             _filenameSuffix            = "";
    private Configuration      _fremarkerConfig           = null;

    /**
     * Costruttore privato.
     * 
     * @param java.lang.String cf nome della configurazione
     * @exception net.project.errors.AppCrash
     */
    private TemplateFactory(String cf) throws AppCrash {

        // Recupero della templateRoot dal file di configurazione
        String tpProp = PAGE + "." + TEMPLATE_ROOT;
        String templateRoot = Config.GetInstance(cf).getProperty(tpProp);

        // Se path relativo cerco di aggiungere in testa la root della applicazione
        templateRoot = Config.GetInstance(cf).makeAbsolutePath(templateRoot);

        boolean templateRootEsistente = (templateRoot != null) && (templateRoot.length() > 0);
        ErrDetector.GetInstance().postCond(templateRootEsistente,
                "proprietà " + tpProp + " mancante nel file di configurazione");

        String classic = Config.GetInstance(cf).getProperty("Page.classic", "false");
        // Istanziazione di un TemplateCache
        try {
            _fremarkerConfig = new Configuration();
            File templateRootFile = new File(templateRoot);
            _fremarkerConfig.setDirectoryForTemplateLoading(templateRootFile);

            if (!classic.equalsIgnoreCase("false")) {
                _fremarkerConfig.setClassicCompatible(true);
            }
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("Templatefactory", t.getMessage() + "; " + toString());
            throw ac;
        }

        ErrDetector.GetInstance().postCond(_fremarkerConfig != null, "errore nell'istanziazione del TemplateCache");

        // Impostazione delle proprietà dell'oggetto FileTemplateCache:

        // Impostazione dei secondi che intercorrono tra due aggiornamenti consecutivi della cache
        String dProp = PAGE + "." + DELAY;
        String delayProperty = Config.GetInstance(cf).getProperty(dProp, DEFAULT_DELAY);
        try {
            int delay = Integer.parseInt(delayProperty);
            if (delay <= 0) {
                AppCrash ac = new AppCrash();
                ac.logContext("TemplateFactory", "cf = " + cf + "; " + dProp + " = " + delayProperty);
                throw ac;
            }
            _fremarkerConfig.setTemplateUpdateDelay(delay);
            Logger.GetInstance()
                    .log3("Class: TemplateFactory; Method: TemplateFactory; "
                            + " impostato delay: secondi che intercorrono tra due aggiornamenti consecutivi della cache = "
                            + delayProperty);

        } catch (NumberFormatException nfe) {
            AppCrash ac = new AppCrash(nfe);
            ac.logContext("TemplateFactory", "cf = " + cf + "; " + dProp + " = " + delayProperty);
            throw ac;
        }

        // Eventuale impostazione del character encoding
        String encProp = PAGE + "." + ENCODING;
        String encoding = Config.GetInstance(cf).getProperty(encProp);
        if ((encoding != null) && (encoding.length() > 0)) {
            _fremarkerConfig.setDefaultEncoding(encoding);
            Logger.GetInstance().log3(
                    "Class: TemplateFactory; Method: TemplateFactory; impostato character encoding: " + encoding);
        }

        // Eventuale impostazione del suffisso dei file
        String fsProp = PAGE + "." + FILENAME_SUFFIX;
        _filenameSuffix = Config.GetInstance(cf).getProperty(fsProp);
        Logger.GetInstance().log3(
                "Class: TemplateFactory; Method: TemplateFactory; impostato filenameSuffix: " + _filenameSuffix);

        if (_filenameSuffix == null) {
            _filenameSuffix = "";
        }

        String numberFormatOption = Config.GetInstance(cf).getProperty("Page.NumberFormatOption", "false");
        if (!TRUE.equalsIgnoreCase(numberFormatOption)) {
            _fremarkerConfig.setNumberFormat(NO_NUMBER_FORMAT);
        }

        String strictSyntaxOption = Config.GetInstance(cf).getProperty("Page.StrictSyntaxOption", "false");
        if (!TRUE.equalsIgnoreCase(strictSyntaxOption)) {
            _fremarkerConfig.setStrictSyntaxMode(false);
        }

        String exceptionHandler = Config.GetInstance(cf).getProperty("Page.TemplateException", "debug");
        if (exceptionHandler.equals("prod")) {
            _fremarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        }
    }

    /**
     * Metodo di accesso al singleton.
     * 
     * @param java.lang.String cfName nome della configurazione
     * @return net.project.servlet.gui.TemplateFactory il singleton
     * @exception net.project.errors.AppCrash
     * @roseuid 3A6D45E20135
     */
    public static TemplateFactory_itf GetInstance(String cfName) throws AppCrash {

        // Controllo formale del parametro in ingresso
        ErrDetector.GetInstance().preCond(cfName != null, "cfName = null");

        TemplateFactory_itf result = (TemplateFactory_itf) _TemplateFactoryRepository.get(cfName);
        if (result == null) {
            // questo e' un "Double checked lock" design pattern
            synchronized (TemplateFactory.class) {
                result = (TemplateFactory_itf) _TemplateFactoryRepository.get(cfName);
                if (result == null) {
                    result = new TemplateFactory(cfName);
                    _TemplateFactoryRepository.put(cfName, result);
                }
            }

        }

        return result;

    }

    /**
     * Metodo di accesso al singleton (ridefinisce il metodo omonimo)
     * 
     * @return net.project.servlet.gui.TemplateFactory il singleton
     * @exception net.project.errors.AppCrash
     * @roseuid 3A6D45E20135
     */
    public static TemplateFactory_itf GetInstance() throws AppCrash {

        return GetInstance("");
    }

    @Override
    public Template getTemplate(String name) throws AppCrash {

        // Controllo formale del parametro in ingresso
        ErrDetector.GetInstance().param(name);

        try {
            Template template = _fremarkerConfig.getTemplate(name + _filenameSuffix);
            if (template == null) {
                throw new AppCrash("Errore nell'istanziazione del template " + name + _filenameSuffix);
            }
            return template;

        } catch (Throwable re) {
            AppCrash ac = new AppCrash(re);
            ac.logContext("TemplateFactory", toString() + "; name = " + name + _filenameSuffix);
            throw ac;
        }

    }

    @Override
    public String toString() {

        return "_filenameSuffix = " + _filenameSuffix;
    }

}
