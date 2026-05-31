/*
  Importo.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 04/01/2000

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.misc;

/**
 Questa classe implementa un importo formattato con valuta
 */

import java.util.StringTokenizer;

import net.project.errors.ErrDetector;
import net.project.errors.ErrDetector_itf;
import net.project.errors.Logger;
import net.project.errors.ParamCrash;

public class Importo {

    private String _importo      = "";
    private String _codiceValuta = "";

    /**
     * Costruttore
     *
     * @param cifra java.lang.String la cifra dell'importo
     * @param valuta java.lang.String la valuta dell'importo
     * @exception net.project.errors.ParamCrash.
     */
    public Importo(String cifra, String divisa) throws ParamCrash {

        try {
            ErrDetector_itf ed = ErrDetector.GetInstance();
            ed.param(cifra);
            ed.param(divisa);

            if (cifra.indexOf(",") != -1) {
                _importo = cifra.replace(',', '.');
            } else {
                _importo = cifra;
            }
            _codiceValuta = divisa;
        } catch (ParamCrash ex) {
            ex.logContext("Importo", "Errore nel costruttore: parametri errati");
            throw ex;
        }
    }

    /**
     * Decodifica il codice della valuta
     *
     * @return java.lang.String la valuta decodificata (recuperata dal file di configurazione)
     */
    public String getValuta() {

        String val = "";
        if (_codiceValuta.equals("380")) {
            val = Config.GetInstance().getProperty("Importo.Valuta.Lire");
        } else {
            val = Config.GetInstance().getProperty("Importo.Valuta.Euro");
        }
        return val;
    }

    /**
     * Ritorna l'importo formattato
     *
     * @return java.lang.String
     */
    public String getImporto() {

        return formattaImporto();
    }

    /**
     * Ritorna l'importo formattato per host
     *
     * @return java.lang.String
     */
    public String getHostImporto() {

        return formattaHostImporto();
    }

    /**
     * Ritorna la valuta e l'importo formattato
     *
     * @return java.lang.String
     */
    @Override
    public String toString() {

        return getValuta() + " " + getImporto();
    }

    /**
     * Formatta una stringa rappresentante un importo
     *
     * @return java.lang.String l'importo formattato in base alla divisa
     */
    private String formattaImporto() {

        int lung = _importo.length();

        if (_codiceValuta.equals("380")) {
            return puntiDecimali(_importo);
        } else {
            // In caso di euro, l'importo è espresso in centesimi di euro
            if (lung == 1) {
                return "0,0" + _importo;
            } else {
                if (lung == 2) {
                    return "0," + _importo;
                } else {
                    return (puntiDecimali(_importo.substring(0, (lung - 2))) + "," + _importo.substring(lung - 2));
                }
            }
        }
    }

    /**
     * Formatta una stringa rappresentante un importo su host
     *
     * @return java.lang.String l'importo formattato in base alla divisa
     */
    private String formattaHostImporto() {

        int lung = _importo.length();

        if (_codiceValuta.equals("380")) {
            return _importo;
        } else {
            // In caso di euro, l'importo è espresso in centesimi di euro
            if (lung == 1) {
                return "0.0" + _importo;
            } else {
                if (lung == 2) {
                    return "0." + _importo;
                } else {
                    return (_importo.substring(0, (lung - 2)) + "." + _importo.substring(lung - 2));
                }
            }
        }
    }

    /**
     * Inserisce i punti decimali in una stringa rappresentante un numero
     *
     * @param numero java.lang.String la stringa rappresentante il numero
     * @return java.lang.String la stringa rappresentante il numero con i punti decimali nella corretta posizione
     */
    private String puntiDecimali(String numero) {

        int lung = numero.length();
        int cifre = 3;

        while ((lung - cifre) > 0) {

            numero = numero.substring(0, (lung - cifre)) + "." + numero.substring(lung - cifre);
            cifre = cifre + 3;
        }
        return numero;
    }

    /**
     * Elimina le virgole dagli importi ottenuti dal db su host.
     *
     * @return java.lang.String L'importo in centesimi.
     */
    public String getCentesimi() {

        if (Float.valueOf(_importo).equals(new Float("0.0"))) {
            return "0";
        }
        StringBuffer importoInCentesimi = new StringBuffer();
        StringTokenizer st = new StringTokenizer(_importo, ".");
        String parteIntera = st.nextToken();
        if (!parteIntera.equals("0")) {
            importoInCentesimi.append(parteIntera);
        }
        if (_codiceValuta.equals("978")) {
            importoInCentesimi.append(st.nextToken().substring(0, 2));
        }
        return importoInCentesimi.toString();
    }

    public static void selfTest() {

        String importo = "2";
        String valuta = "978"; // 380 = lire 978 = euro
        try {
            Importo prova = new Importo(importo, valuta);

            Logger.GetInstance().log3("Importo selftest: " + prova.getValuta());
            Logger.GetInstance().log3("Importo selftest: " + prova.getImporto());
            Logger.GetInstance().log3("Importo selftest: " + prova.toString());
        } catch (ParamCrash ex) {
        }
    }
}
