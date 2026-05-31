
package net.project.mess.iso8583;

/*
 ISO8583Dictionary.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 18/05/1999

 Autore: Rosella V.

 Note:

 Modifiche:

 */
import java.util.Hashtable;
import java.util.Vector;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.misc.FileParser;

/**
 * Classe per la gestione del vettore dei descrittori ISO8583
 */
public class ISO8583Dictionary implements FormatLengthType_itf {

    private static Hashtable _Instances = new Hashtable();

    private DataElement[]    _elements;                   // array dei descrittori di messaggio

    /**
     * Costruttore
     * 
     * @param String Tipo tipo di dizionario ISO8583 da istanziare
     * @exception AppCrash.
     */
    private ISO8583Dictionary(String tipo) throws AppCrash {

        try {
            String fileName = Config.GetInstance().getProperty("FormatMessageFile" + tipo);
            fileName = Config.GetInstance().makeAbsolutePath(fileName);

            Vector v = FileParser.parse(fileName);

            _elements = new DataElement[129];

            // popolo l'array dall'elemento 1 al 128 (elemento 0 vuoto)
            for (int j = 1; j < v.size(); j++) {
                String lenType = (String) ((Vector) (v.elementAt(j))).elementAt(1);
                String maxLen = (String) ((Vector) (v.elementAt(j))).elementAt(2);
                String filler = (String) ((Vector) (v.elementAt(j))).elementAt(3);
                _elements[j] = new DataElement(lenType, maxLen, filler);
            }
        } catch (AppCrash e) {
            e.logContext("ISO8583Dictionary", toString());
            throw e;
        }

    }

    /**
     * Implementazione del pattern Singleton.
     * 
     * @exception AppCrash.
     */
    public static ISO8583Dictionary GetInstance() throws AppCrash {

        return GetInstance("");
    }

    /**
     * Implementazione del pattern Singleton.
     * 
     * @param String Tipo tipo di dizionario ISO8583 da istanziare
     * @exception AppCrash.
     */
    public static ISO8583Dictionary GetInstance(String tipo) throws AppCrash {

        ISO8583Dictionary result = null;
        try {
            // questo e' un "Double checked lock" design pattern
            result = (ISO8583Dictionary) _Instances.get(tipo);
            if (result == null) {
                synchronized (ISO8583Dictionary.class) {
                    result = (ISO8583Dictionary) _Instances.get(tipo);
                    if (result == null) {
                        result = new ISO8583Dictionary(tipo);
                        _Instances.put(tipo, result);
                    }
                }
            }
        } catch (AppCrash e) {
            throw e;
        }
        return result;
    }

    /**
     * Ritorna il tipo di lunghezza del campo identificato dal data element passato.
     * 
     * @return int.
     * @exception AppCrash.
     */
    public int getLengthType(int key) throws AppCrash {

        int value = 0;
        try {
            ErrDetector.GetInstance().param(((key) > 0) && ((key) <= 128));
            value = _elements[key].getLengthType();
        } catch (AppCrash e) {
            e.logContext("ISO8583Dictionary", "Data element inesistente: " + key);
            throw e;
        }
        return value;
    }

    /**
     * Ritorna la lunghezza massima del campo identificato dal data element passato.
     * 
     * @return int.
     * @exception AppCrash.
     */
    public int getMaxLength(int key) throws AppCrash {

        int value = 0;
        try {
            ErrDetector.GetInstance().param(((key) > 0) && ((key) <= 128));
            value = _elements[key].getMaxLength();
        } catch (AppCrash e) {
            e.logContext("ISO8583Dictionary", "Data element inesistente: " + key);
            throw e;
        }
        return value;
    }

    /**
     * Ritorna il valore formattato del campo. Se il campo è di lunghezza variabile viene prefissato dalla lunghezza.
     * 
     * @param int key.
     * @param String value.
     * @return String.
     * @exception AppCrash.
     */
    public String formatField(int key, String value) throws AppCrash {

        String field = null;
        try {
            ErrDetector.GetInstance().param(((key) > 0) && ((key) <= 128));
            field = _elements[key].formatField(value);
        } catch (AppCrash e) {
            e.logContext("ISO8583Dictionary", "Data element : " + key + "Valore : " + value);
            throw e;
        }
        return field;
    }

    /**
     * Ritorna il tipo di filler da usare.
     * 
     * @param int key.
     * @return String.
     * @exception AppCrash.
     */
    public String getFiller(int key) throws AppCrash {

        String value = null;
        try {
            ErrDetector.GetInstance().param(((key) > 0) && ((key) <= 128));
            value = _elements[key].getFiller();
        } catch (AppCrash e) {
            e.logContext("ISO8583Dictionary", "Data element inesistente: " + key);
            throw e;
        }
        return value;
    }
}
