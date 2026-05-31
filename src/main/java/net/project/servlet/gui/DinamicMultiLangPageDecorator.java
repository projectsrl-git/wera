/*

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore:

  Note:

 */

package net.project.servlet.gui;

import java.util.Iterator;
import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.misc.Config_itf;

/**
 * Questa e' un decorator che aggiunge ad una Page_itf la capacita' di sostituire automaticamente ai valori dei tag che
 * iniziano con il prefisso IDMSG_ il valore che si trova nella configurazione MULTILANGUAGE per la lingua impostata per
 * la pagina. Il meccanismo di impostazione della lingua e' il medesimo di MultiLangFreeMarkerPage.
 *
 * @author sim
 */
public class DinamicMultiLangPageDecorator extends PageDecorator {

    private String _language = "it";

    /**
     * Costruttore
     * 
     * @param page pagina da decorare
     * @throws AppCrash
     */
    public DinamicMultiLangPageDecorator(Page_itf page) throws AppCrash {

        super(page);
    }

    /**
     * Riceve una Map e carica nella _modelRoot tanti oggetti SimpleScalar quanti sono gli elementi della Map stessa. Se
     * e' presente il campo LINGUA lo memorizza per usarlo nel metodo display. Prima di richiamare il metodo
     * setPageRootData della classe madre rimpiazza tutti i valori dei tag che iniziano per "IDMSG_" con il
     * corrispondente valore del messaggio nella lingua desiderata. Tali valori sono recuperati dalla configurazione
     * MULTILANGUAGE leggendo la proprieta' <code>lingua.valoreTag</code>; ad esempio il valore IDMSG_tuttoOK per la
     * lingua "usa" leggerebbe la proprieta' <code>usa.IDMSG_tuttoOk=All right!</code>
     *
     * @param paramHash java.util.Map paramHash hashtable contenente i parametri passati dal client
     *
     * @exception AppCrash
     */
    @Override
    public void setPageRootData(Map paramHash) throws AppCrash {

        Config_itf languageConf = Config.GetInstance("MULTILANGUAGE");

        if (paramHash.containsKey("LINGUA")) {
            setLanguage((String) paramHash.get("LINGUA"));
        }

        String lingua = getLanguage();
        Iterator keys = paramHash.entrySet().iterator();

        while (keys.hasNext()) {

            Map.Entry entry = (Map.Entry) keys.next();

            String key = (String) entry.getKey();
            Object obj = entry.getValue();

            if (obj instanceof String) {
                String value = (String) obj;

                if (value.startsWith("IDMSG_")) {

                    String textValue = languageConf.getProperty(lingua + "." + value, "-");
                    paramHash.put(key, textValue);
                }
            }
        }

        super.setPageRootData(paramHash);
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