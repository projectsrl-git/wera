/*
  HashtableReadWrite.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione: 16/01/2001
  
  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.mess;

import java.util.Hashtable;
import java.util.Map;

import net.project.errors.AppCrash;

/**
 * Classe per la lettura e scrittura di una hashtable.
 */

public class HashtableReadWrite implements MsgWriter_itf {

    private Map _tabella;

    /**
     * Costruttore.
     */
    public HashtableReadWrite() throws AppCrash {

        _tabella = new Hashtable();
    }

    /**
     * Costruttore.
     * 
     * @param tabella java.lang.util.Hashtable L'hashtable.
     */
    public HashtableReadWrite(Map tabella) throws AppCrash {

        _tabella = tabella;
    }

    /**
     * Imposta il campo della hashtable.
     * 
     * @param key chiave del campo.
     * @param value valore del campo.
     */
    @Override
    public void setField(String key, String value) {

        _tabella.put(key, value);
    }

    /**
     * Ritorna il messaggio.
     * 
     * @exception net.project.errors.AppCrash.
     * @return byte[]
     */
    @Override
    public byte[] getMessage() throws AppCrash {

        return null;
    }

    /**
     * Ritorna il tipo di messaggio.
     * 
     * @return String
     */
    @Override
    public String getType() {

        return "HASHTABLE";
    }

    /**
     * Questo metodo serve per recuperare il valore di un campo della hashtable.
     * 
     * @param keyName chiave del campo.
     * @exception net.project.errors.AppCrash.
     * @return java.lang.String
     */
    @Override
    public String getField(String key) throws AppCrash {

        String value = (String) _tabella.get(key);
        if (value == null) {
            return "";
        }
        return value;
    }
}
