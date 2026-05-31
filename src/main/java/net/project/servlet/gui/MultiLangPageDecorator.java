/*

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore: 

  Note:

 */

package net.project.servlet.gui;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.misc.Config_itf;

/**
 * Questa classe e' una estensione della classe FreeMarkerPage. Ad essa aggiunge la capacita' di valorizzare
 * automaticamente una serie di tag, pagina per pagina, con i valori che essi assumono nelle varie lingue configurate.
 * Il meccanismo utilizzato e' il seguente:
 * 
 * <p>
 * nel file di configurazione standard viene definita una proprieta' LanguageTags per la pagina in oggetto es.
 * <code>Page.avvioMaster.LanguageTags=ordine negozio importo</code> che riporta i tag multilingua utilizzati dalla
 * pagina. In un file di config a parte letto nella configurazione con nome MULTILANGUAGE sono definiti i valori dei
 * vari tag nelle lingue desiderate.
 * </p>
 * 
 * <P>
 * Il metodo display aggiunge automaticamente ad ogni pagina i tags richiesti nella lingua opportuna. La lingua viene
 * fornita nel tag "LINGUA" o chiamando il metodo <code>setLanguage()</code>
 * </p>
 * 
 * @author sim
 */
public class MultiLangPageDecorator extends PageDecorator {

    private String _language = "it";

    /**
     * Costruttore.
     * 
     * @param java.lang.String name nome della pagina
     * @param java.lang.String cfName nome della configurazione
     * @exception net.project.errors.AppCrash
     * @roseuid 3A6D44EF00F0
     */
    public MultiLangPageDecorator(Page_itf page) throws AppCrash {

        super(page);
    }

    /**
     * Il metodo display di questa classe richiama display di FreeMarkerPage ma prima aggiunge tutti i tag indicati
     * nella proprieta' <code>Page.+getName()+.LanguageTags</code> nella lingua stabilita dal valore del tag "LINGUA".
     * Se il tag LINGUA non e' valorizzato si assume di default "it"
     * 
     * <p>
     * I valori dei tag sono recuperati dalla configurazione di nome MULTILANGUAGE che deve esistere. I tag devono
     * essere presenti nel formato <code>lingua.NOMETAG=valore</code>. Ad esempio
     * <code>it.negozio=Negozio del venditore</code> definisce i tag di nome "negozio" per la lingua "it".
     * </p>
     * 
     * @see net.project.servlet.gui.FreeMarkerPage#display(PrintWriter)
     */
    @Override
    public void display(PrintWriter pw) throws AppCrash {

        Config_itf languageConf = Config.GetInstance("MULTILANGUAGE");

        String languageTags = Config.GetInstance(getConfigName()).getProperty("Page." + getName() + ".LanguageTags");

        // Se ci sono tags in lingua specificati per la pagina li aggiungo al modelRoot
        if (languageTags != null) {

            String tag = null;
            String tagvalue = null;
            String lingua = getLanguage();
            Map tagPairs = new HashMap();

            // Metto i valori dei tag da aggiungere in una Hashtable
            StringTokenizer st = new StringTokenizer(languageTags);

            while (st.hasMoreTokens()) {
                tag = st.nextToken();
                tagvalue = languageConf.getProperty(lingua + "." + tag, "-");
                tagPairs.put(tag, tagvalue);
            }

            // Aggiungo la Hashtable al modelRoot
            setPageRootData(tagPairs);
        }

        super.display(pw);
    }

    /**
     * Riceve una Map e carica nella _modelRoot tanti oggetti SimpleScalar quanti sono gli elementi della Map stessa. Se
     * e' presente il campo LINGUA lo memorizza per usarlo nel metodo display.
     * 
     * @param paramHash java.util.Map paramHash hashtable contenente i parametri passati dal client
     * 
     * @exception AppCrash
     */
    @Override
    public void setPageRootData(Map paramHash) throws AppCrash {

        super.setPageRootData(paramHash);

        if (paramHash.containsKey("LINGUA")) {
            setLanguage((String) paramHash.get("LINGUA"));
        }
    }

    /**
     * Questo metodo imposta la lingua utilizzata dalla pagina
     *
     * @param lang String con la lingua da impostare
     */
    public void setLanguage(String lang) throws AppCrash {

        ErrDetector.GetInstance().param(lang);
        _language = lang;
    }

    /**
     * Questo metodo riporta la lingua utilizzata dalla pagina
     *
     * @return String lingua impostata
     */
    public String getLanguage() {

        return _language;
    }
}