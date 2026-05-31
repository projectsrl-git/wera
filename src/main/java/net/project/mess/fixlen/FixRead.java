
package net.project.mess.fixlen;

/*
 FixRead.java

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
import net.project.mess.MsgReader_itf;

/**
 * Classe per la lettura di un messaggio con campi a lunghezza fissa
 */

public class FixRead implements MsgReader_itf {

    private byte[] _message;

    /**
     * Costruttore presente solo per permettere la serializzazione/deserializzazione di oggetti di questa classe. <b>Non
     * deve essere utilizzato</b>
     */
    public FixRead() {

        super();
    }

    /**
     * Costruttore.
     * 
     * @param byte[] message con campi a lunghezza fissa.
     */
    public FixRead(byte[] message) throws AppCrash {

        _message = message;
    }

    /**
     * Ritorna il campo del messaggio dato il nome
     * 
     * @param java.lang.String key chiave del campo.
     * @exception net.project.errors.AppCrash.
     * @return java.lang.String
     */
    @Override
    public String getField(String key) throws AppCrash {

        String result = null;

        int len = 0;
        int offset = 4; // inizializzo offset al primo char del messaggio
        Vector temp = null;
        Vector tempVector = null;
        int pos = 0;
        byte[] field = null;
        Hashtable ht = MessageSpec.GetInstance().getSpec(getType());
        ErrDetector.GetInstance().invariant((ht != null), "Specifica non trovata per " + getType());

        tempVector = (Vector) ht.get(key);
        ErrDetector.GetInstance().invariant((tempVector != null), "Campo non trovato " + key + " in " + getType());

        pos = new Integer((String) tempVector.elementAt(3)).intValue();

        // calcolo offset per lettura del campo richiesto
        Enumeration enu = ht.elements();
        while (enu.hasMoreElements()) {
            temp = (Vector) enu.nextElement();
            if (new Integer((String) temp.elementAt(3)).intValue() < pos) {
                offset += (new Integer((String) temp.elementAt(2)).intValue()); // leggo e sommo la lunghezza del campo
                                                                                // i-esimo
            }
        }
        // leggo la lunghezza del campo da restituire
        len = (new Integer((String) tempVector.elementAt(2)).intValue());
        // leggo il valore del campo di indice key
        field = new byte[len];
        System.arraycopy(_message, offset, field, 0, len);
        result = new String(field);
        return result;
    }

    /**
     * Ritorna il tipo del messaggio
     * 
     * @return java.lang.String
     */
    @Override
    public String getType() {

        byte[] byteArray = new byte[4];
        for (int i = 0; i < 4; i++) {
            byteArray[i] = _message[i];
        }
        return new String(byteArray);
    }
}
