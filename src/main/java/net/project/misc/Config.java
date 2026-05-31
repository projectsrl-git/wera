/*
  Config.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione: 06/06/2000

  Autore: Simone Z.

  Note:

  Modifiche:

 */

package net.project.misc;

import java.io.IOException;
import java.util.Hashtable;

import net.project.errors.ErrorConfig;

public class Config {

    private static Hashtable  _Configuration = new Hashtable();
    private static Config_itf _DefaultConf;

    /**
     * Costruttore. La classe non e' istanziabile
     * 
     * @exception java.io.IOException.
     */
    private Config() {

    };

    private static void makeConf(String name, String configFile) throws IOException {

        Config_itf conf = new Configuration(configFile);
        if (name.equals("default")) {
            _DefaultConf = conf;
            ErrorConfig.setConfig("", ((Configuration) conf).getProperties());
        } else {
            _Configuration.put(name, conf);
            ErrorConfig.setConfig(name, ((Configuration) conf).getProperties());
        }
    }

    /**
     * Restituisce l'interfaccia che rappresenta la configurazione di default.
     */
    public static Config_itf GetInstance() {

        return _DefaultConf;
    }

    /**
     * Restituisce l'istanza dell'oggetto che rappresenta la configurazione con il nome passato.
     */
    public static Config_itf GetInstance(String name) {

        if (name.equals("")) return _DefaultConf;
        else
            return (Config_itf) _Configuration.get(name);
    }

    /**
     * Inizializza una configurazione in base al nome ed al file di configurazione.
     * 
     * @param configFile java.lang.String nome del file di configurazione
     * @param name java.lang.String nome della configurazione
     * @exception java.io.IOException.
     */
    static public synchronized void InitInstance(String name, String configFile) throws IOException {

        if (_Configuration.get(name) == null) {
            makeConf(name, configFile);
        }
    }

    /**
     * Inizializza la configurazione di default in base al file di configurazione.
     * 
     * @param configFile java.lang.String nome del file di configurazione
     * @exception java.io.IOException.
     */
    static public synchronized void InitInstance(String configFile) throws IOException {

        if (_DefaultConf == null) {
            InitInstance("default", configFile);
        }

        // Inizializzazioni delle configurazioni alternative
        int j = 0;
        while (true) {
            String altConfig = _DefaultConf.getProperty("Config.alternate" + j + ".name");
            if (altConfig == null) break;

            String altFile = _DefaultConf.getProperty("Config.alternate" + j + ".file");
            altFile = Config.GetInstance().makeAbsolutePath(altFile);

            InitInstance(altConfig, altFile);
            j = j + 1;
        }
    }

}
