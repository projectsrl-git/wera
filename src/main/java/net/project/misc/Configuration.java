/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.misc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Questa classe fornisce l'implementazione concreta dell'interfaccia utilizzata per recuperare le proprieta' di
 * configurazione delle applicazioni. E' basata su un oggetto della classe java.util.Properties.
 */
public class Configuration implements Config_itf {

    private Properties      _configuration;
    private ResourceWatcher _resWatch = null;
    private File            _file;

    /**
     * Costruttore. Crea l'oggetto java.util.Properties e carica in esso il file di configurazione
     *
     * @param configFileName java.lang.String nome del file di configurazione.
     *
     * @exception java.io.IOException
     */
    public Configuration(String configFileName) throws IOException {

        _configuration = new Properties();
        _file = new File(configFileName);

        if (_file.exists()) {
            readFile();

            String reload = this.getProperty("Config.reloadDelay", "-1");

            if (!reload.equalsIgnoreCase("-1")) {
                _resWatch = new ResourceWatcher(_file, Integer.parseInt(reload));
            }
        } else {
            throw new FileNotFoundException(configFileName);
        }
    }

    /**
     * Consente la lettura di una proprietà dal file di configurazione associato a questo oggetto.
     *
     * @param propertyName java.lang.String nome della proprietà da leggere.
     *
     * @return java.lang.String Il valore della proprietà letta dal file di configurazione.
     */
    @Override
    public String getProperty(String propertyName) {

        String result = null;

        try {
            if ((_resWatch != null) && _resWatch.hasBeenModified()) {
                readFile();
            }

            result = _configuration.getProperty(propertyName);
        } catch (IOException e) {
            System.out.println("Errore IO rileggendo il file di configurazione");
        }

        return result;
    }

    /**
     * Consente la lettura di una proprietà dal file di configurazione indicando un default.
     *
     * @param propertyName java.lang.String nome della proprietà da leggere.
     * @param defaultVal java.lang.String valore di default nel caso non vi sia la proprieta'
     *
     * @return java.lang.String Il valore della proprietà letta dal file di configurazione o il valore di default
     */
    @Override
    public String getProperty(String propertyName, String defaultVal) {

        String result;
        result = _configuration.getProperty(propertyName, defaultVal);

        return result;
    }

    /**
     * Produce la scrittura di tutte le proprieta' elencate nell'oggetto sullo stream passato.
     *
     * @param out java.io.OutputStream stream sul quale scrivere
     * @param header java.lang.String intestazione da inserire prima del dump
     */
    @Override
    public void dump(OutputStream out, String header) {

        _configuration.save(out, header);
    }

    /**
     * Imposta il valore della proprieta' specificata
     */
    @Override
    public void setProperty(String propertyName, String val) {

        _configuration.setProperty(propertyName, val);
    }

    /**
     * Questo metodo
     *
     * @throws IOException DOCUMENT ME!
     */
    private void readFile() throws IOException {

        InputStream is = new BufferedInputStream(new FileInputStream(_file));
        _configuration.load(is);
        is.close();
    }

    /**
     * Ritorna la stringa passata con in testa il valore della proprieta' di configurazione Application.root se essa non
     * e' vuota e se la stringa passata non e' un path relativo. Non viene fatto nessun controllo della reale esistenza
     * del path.
     */
    @Override
    public String makeAbsolutePath(String path) {

        if (path == null) return null;
        if (path.equals("")) return "";

        String newPath = path;
        // Path relativo cerco di aggiungere in testa la root della applicazione
        if (!path.startsWith("/") && !path.startsWith("\\") && path.charAt(1) != ':') {
            String root = Config.GetInstance().getProperty("Application.root", "");

            if (!root.equals("")) {
                newPath = root + "/" + path;
            }

        }
        return newPath;

    }

    /**
     * Restituisce un oggetto di classe Properties con tutte le proprieta' definite nella configurazione
     *
     * @return Properties tutte le proprieta'
     */
    Properties getProperties() {

        return _configuration;
    }
}
