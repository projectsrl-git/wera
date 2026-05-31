
package net.project.mess.fixlen;

/*
 MessageSpec.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 27/03/2000

 Autore: Rosella V.

 Note:

 Modifiche:


 */
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.misc.FileParser;

/**
 * Questa classe rappresenta il contenitore delle specifiche dal punto di vista sintattico dei messaggi a lunghezza
 * fissa.
 * <p>
 *
 */
public class MessageSpec {

    private static MessageSpec _Instance;
    private Map                _deposito = new HashMap(31);

    /**
     * Creates a new MessageSpec object.
     *
     * @throws AppCrash eccezione
     */
    private MessageSpec() throws AppCrash {

        StringTokenizer parser = null;
        String type = null;
        String path = Config.GetInstance().getProperty("Fix.Path");
        ErrDetector.GetInstance().param(path);
        path = Config.GetInstance().makeAbsolutePath(path);

        String descrittori = Config.GetInstance().getProperty("Fix.Descrittori");
        ErrDetector.GetInstance().param(descrittori);

        try {
            parser = new StringTokenizer(descrittori);

            String file = null;
            Vector vectSpecifica = null;
            Vector temp = null;
            String fieldName = null;

            // per ogni file di descrittori
            for (int i = 0; parser.hasMoreTokens(); i++) {
                file = new StringBuffer().append(path).append(parser.nextToken()).toString();
                vectSpecifica = FileParser.parse(file);
                type = (String) ((Vector) vectSpecifica.elementAt(0)).elementAt(0);

                Map ht = new HashMap(13);

                // leggo le specifiche per tutti i campi del messaggio di tipo "type"
                for (int j = 1; j < vectSpecifica.size(); j++) {
                    temp = (Vector) vectSpecifica.elementAt(j);

                    // aggiungo la posizione del campo in coda al vettore
                    temp.addElement(new Integer(j).toString());
                    fieldName = (String) temp.elementAt(0); // nome del campo
                    ht.put(fieldName, temp); // temp=nomeCampo,n/an,lenCampo
                }

                _deposito.put(type, ht);
            }
        } catch (AppCrash e) {
            e.logContext("MessageSpec", "Path: " + path + " Descrittori: " + descrittori);
            throw e;
        }
    }

    /**
     * Ritorna l'istanza della classe.
     *
     * @return net.project.mess.fixLen.MessageSpec.
     *
     * @exception net.project.errors.AppCrash.
     */
    public static MessageSpec GetInstance() throws AppCrash {

        // questo e' un "Double checked lock" design pattern
        if (_Instance == null) {
            synchronized (MessageSpec.class) {
                if (_Instance == null) {
                    _Instance = new MessageSpec();
                }
            }
        }

        return _Instance;
    }

    /**
     * Ritorna il valore formattato del campo.
     *
     * @param type tipo del messaggio.
     * @param key chiave del campo.
     * @param value valore del campo.
     *
     * @return java.lang.String.
     *
     * @exception AppCrash.
     */
    public String formatField(String type, String key, String value) throws AppCrash {

        // n = campo numerico ; nx = campo numerico che mette spazi se non valorizzato
        // an = campo alfanumerico ; andx = campo alfanumerico allineato a dx
        String field = null;
        StringBuffer tempField = null;

        try {
            tempField = new StringBuffer();

            if (getFieldFormat(type, key).equals("n")) {
                // caso campo numerico
                int len = getFieldLen(type, key);
                ErrDetector.GetInstance().preCond(value.length() <= len);

                for (int i = 0; i < (len - value.length()); i++) {
                    tempField.append("0");
                }

                tempField.append(value);
            } else if (getFieldFormat(type, key).equals("nx")) {
                // caso campo numerico che mette spazi nel caso in cui il campo non ci sia
                int len = getFieldLen(type, key);
                ErrDetector.GetInstance().preCond(value.length() <= len);

                if (value.length() == 0) {
                    for (int i = 0; i < len; i++) {
                        tempField.append(" ");
                    }
                } else {
                    for (int i = 0; i < (len - value.length()); i++) {
                        tempField.append("0");
                    }

                    tempField.append(value);
                }
            } else if (getFieldFormat(type, key).equals("an")) {
                // caso campo alfanumerico
                int len = getFieldLen(type, key);
                ErrDetector.GetInstance().preCond(value.length() <= len);
                tempField.append(value);

                for (int i = 0; i < (len - value.length()); i++) {
                    tempField.append(" ");
                }
            } else if (getFieldFormat(type, key).equals("andx")) {
                // caso campo alfanumerico allineato a dx
                int len = getFieldLen(type, key);
                ErrDetector.GetInstance().preCond(value.length() <= len);

                for (int i = 0; i < (len - value.length()); i++) {
                    tempField.append(" ");
                }

                tempField.append(value);
            } else if (getFieldFormat(type, key).startsWith("andx_")) {
                // caso campo alfanumerico allineato a dx con fill generico
                int len = getFieldLen(type, key);
                ErrDetector.GetInstance().preCond(value.length() <= len);
                ErrDetector.GetInstance().invariant(getFieldFormat(type, key).length() == 5);

                String filler = getFieldFormat(type, key).substring(4);

                for (int i = 0; i < (len - value.length()); i++) {
                    tempField.append(filler);
                }

                tempField.append(value);
            } else if (getFieldFormat(type, key).equals("anT")) {
                // caso campo alfanumerico con troncamento se maggiore della lunghezza attesa
                int len = getFieldLen(type, key);
                if (value.length() > len) {
                    value = value.substring(0, len);
                }
                tempField.append(value);

                for (int i = 0; i < (len - value.length()); i++) {
                    tempField.append(" ");
                }
            }

            ErrDetector.GetInstance().postCond(tempField != null);
            field = tempField.toString(); // assegnamento del campo di indice key
        } catch (AppCrash e) {
            e.logContext("Message Spec: formatField", "Message type : " + type + " Field key : " + key
                    + " Field value : " + value);
            throw e;
        }

        return field;
    }

    /**
     * Ritorna il numero di campi di cui è composto un determinato messaggio.
     *
     * @param type tipo del messaggio.
     *
     * @return int.
     *
     * @exception net.project.errors.AppCrash.
     */
    public int getNumFields(String type) throws AppCrash {

        int num = 0;
        HashMap hm = (HashMap) _deposito.get(type);

        if (hm != null) {
            num = hm.size();
        }

        return num;
    }

    /**
     * Ritorna il formato del campo.
     *
     * @param type tipo del messaggio.
     * @param key chiave del campo.
     *
     * @return String.
     *
     * @exception net.project.errors.AppCrash.
     */
    public String getFieldFormat(String type, String key) throws AppCrash {

        String value = null;

        try {
            HashMap hm = (HashMap) _deposito.get(type);
            Vector v = (Vector) hm.get(key);
            value = (String) (v.elementAt(1));
        } catch (Exception e) {
            AppCrash appCrash = new AppCrash(e);
            appCrash.logContext("Message Spec: getFieldFormat", "Message type : " + type + " Field key : " + key);
            throw appCrash;
        }

        return value;
    }

    /**
     * Ritorna la lunghezza del campo.
     *
     * @param type tipo del messaggio.
     * @param key chiave del campo.
     *
     * @return int.
     *
     * @exception net.project.errors.AppCrash.
     */
    public int getFieldLen(String type, String key) throws AppCrash {

        int value = 0;

        try {
            HashMap hm = (HashMap) _deposito.get(type);
            Vector v = (Vector) hm.get(key);
            value = new Integer((String) (v.elementAt(2))).intValue();
        } catch (Exception e) {
            AppCrash appCrash = new AppCrash(e);
            appCrash.logContext("Message Spec: getFieldLen", "Message type : " + type + " Field key : " + key);
            throw appCrash;
        }

        return value;
    }

    /**
     * Ritorna le specifiche di un messaggio dato il tipo
     *
     * @param type tipo del messaggio.
     *
     * @return java.util.Hashtable.
     *
     * @exception net.project.errors.AppCrash.
     */
    public Hashtable getSpec(String type) throws AppCrash {

        Hashtable value = null;

        try {
            value = new Hashtable((HashMap) _deposito.get(type));
        } catch (Exception e) {
            AppCrash appCrash = new AppCrash(e);
            appCrash.logContext("Message Spec: getSpec", "Message type : " + type);
            throw appCrash;
        }

        return value;
    }
}
