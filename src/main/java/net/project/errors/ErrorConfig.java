/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.errors;

import java.util.Hashtable;
import java.util.Properties;

/**
 * Questa classe sostituisce per il package errors il singleton Config. Viene usato per impedire una dipendenza ciclica
 * tra i package misc ed errors. La sua inizializzazione e' a carico del package misc
 *
 * @author zorzetti
 */
public class ErrorConfig {

    private static Hashtable  _Configuration = new Hashtable();
    private static Properties _DefaultConf;

    /**
     * Costruttore. La classe non e' istanziabile
     *
     * @exception java.io.IOException.
     */
    private ErrorConfig() {

    }

    /**
     * Restituisce l'oggetto Properties che rappresenta la configurazione di default.
     *
     * @return Properties configurazione di default
     */
    public static Properties GetInstance() {

        return _DefaultConf;
    }

    /**
     * Restituisce l'oggetto Properties che rappresenta la configurazione con il nome passato.
     *
     * @param name nome della config desiderata
     *
     * @return Properties configurazione name
     */
    public static Properties GetInstance(String name) {

        if (name.equals("")) {
            return _DefaultConf;
        } else {
            return (Properties) _Configuration.get(name);
        }
    }

    /**
     * Imposta il valore di una configurazione in base al nome.
     *
     * @param configName nome della config
     * @param conf oggetto Properties
     */
    public static synchronized void setConfig(String configName, Properties conf) {

        if (configName.equals("")) {
            _DefaultConf = conf;
        } else {
            _Configuration.put(configName, conf);
        }

    }
}
