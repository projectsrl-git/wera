
package net.project.mess.fixlen;

/*
 FixReadWrite.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 24/03/2000

 Autore: Rosella V.

 Note:

 Modifiche:

 */
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.mess.MsgFactory_base;
import net.project.mess.MsgReader_itf;
import net.project.mess.MsgWriter_itf;

/**
 * Classe per la lettura e scrittura di un messaggio con campi a lunghezza fissa
 */

public class FixReadWrite implements MsgWriter_itf {

    private String _type;
    String[]       _messageArray = null;

    /**
     * Costruttore presente solo per permettere la serializzazione/deserializzazione di oggetti di questa classe. <b>Non
     * deve essere utilizzato</b>
     */
    public FixReadWrite() {

        super();
    }

    /**
     * Costruttore.
     * 
     * @param type tipo del messaggio.
     */
    public FixReadWrite(String type) throws AppCrash {

        _type = type;
        // inizializzo l'array dei campi del messaggio
        int len = MessageSpec.GetInstance().getNumFields(type);
        _messageArray = new String[len + 1]; // lascio il primo elemento inutilizzato

        // inizializzo array dei campi
        Hashtable ht = MessageSpec.GetInstance().getSpec(getType());
        ErrDetector.GetInstance().invariant((ht != null), "Specifica non trovata per " + getType());

        Enumeration elementi = ht.elements();
        Vector elem = null;
        int position = 0;
        while (elementi.hasMoreElements()) {
            elem = (Vector) elementi.nextElement();
            position = Integer.parseInt((String) elem.elementAt(3));
            _messageArray[position] = MessageSpec.GetInstance().formatField(getType(), (String) elem.elementAt(0), "");
        }
    }

    /**
     * Costruttore.
     * 
     * @param type tipo del messaggio.
     * @param message messaggio.
     */
    public FixReadWrite(byte[] message) throws AppCrash {

        MsgReader_itf messageRead = MsgFactory_base.GetInstance().MakeMsgReader(message);
        _type = messageRead.getType();
        // inizializzo l'array dei campi del messaggio
        int len = MessageSpec.GetInstance().getNumFields(_type);
        _messageArray = new String[len + 1]; // lascio il primo elemento inutilizzato
        Hashtable ht = MessageSpec.GetInstance().getSpec(_type);
        Enumeration keys = ht.keys();

        while (keys.hasMoreElements()) {
            // inizializzo array dei campi
            String key = (String) keys.nextElement();
            setField(key, messageRead.getField(key));
        }
    }

    /**
     * Imposta il campo del messaggio
     * 
     * @param key chiave del campo.
     * @param value valore del campo.
     * @exception net.project.errors.AppCrash.
     */
    @Override
    public void setField(String key, String value) throws AppCrash {

        Hashtable ht = MessageSpec.GetInstance().getSpec(getType());
        ErrDetector.GetInstance().invariant((ht != null), "Specifica non trovata per " + getType());

        Vector v = (Vector) ht.get(key);
        ErrDetector.GetInstance().invariant((v != null), "Campo non trovato " + key + " in " + getType());

        _messageArray[new Integer((String) v.elementAt(3)).intValue()] = MessageSpec.GetInstance().formatField(
                getType(), key, value);
    }

    /**
     * Ritorna il messaggio.
     * 
     * @exception net.project.errors.AppCrash.
     * @return byte[]
     */
    @Override
    public byte[] getMessage() throws AppCrash {

        byte[] message = null;
        StringBuffer sb = new StringBuffer();
        // controllo che siano stati valorizzati tutti i campi del messaggio
        sb.append(_type);
        for (int i = 1; i < _messageArray.length; i++) {
            sb.append(_messageArray[i]);
        }
        message = sb.toString().getBytes();
        return message;
    }

    /**
     * Ritorna il tipo di messaggio.
     * 
     * @return String
     */
    @Override
    public String getType() {

        return _type;
    }

    /**
     * Ritorna il campo di nome keyName del messaggio.
     * 
     * @param keyName chiave del campo.
     * @exception net.project.errors.AppCrash.
     * @return java.lang.String
     */
    @Override
    public String getField(String keyName) throws AppCrash {

        String field = null;
        try {
            Hashtable ht = MessageSpec.GetInstance().getSpec(getType());
            ErrDetector.GetInstance().invariant((ht != null), "Specifica non trovata per " + getType());

            Vector v = (Vector) ht.get(keyName);
            ErrDetector.GetInstance().invariant((v != null), "Campo non trovato " + keyName + " in " + getType());

            int numFields = MessageSpec.GetInstance().getNumFields(_type);
            ErrDetector.GetInstance().param(1 <= new Integer((String) v.elementAt(3)).intValue());
            ErrDetector.GetInstance().param(new Integer((String) v.elementAt(3)).intValue() <= numFields);
            int key = new Integer((String) v.elementAt(3)).intValue();
            field = _messageArray[key];
        } catch (NumberFormatException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("FixReadWrite", "errore lettura campo: " + keyName);
            throw err;
        }
        return field;
    }
}
