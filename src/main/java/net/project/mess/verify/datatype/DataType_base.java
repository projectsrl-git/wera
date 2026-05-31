
package net.project.mess.verify.datatype;

/*
 DateVerify.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 17/05/2001

 Autore: Assunta Ciervo

 Note:

 Modifiche:

 */
import java.util.Hashtable;

import net.project.errors.AppCrash;

/**
 * Gestione Campo di tipo Data
 * 
 */
public class DataType_base implements FieldVerifier_itf, XMLField_itf {

    private String _name;

    /**
     * StringVerify constructor.
     */
    public DataType_base() {

        super();
    }

    /**
     * StringVerify constructor.
     *
     * @param param Hashtable Hashtable contenente i parametri utili per la verifica del field
     */
    public DataType_base(Hashtable param) throws AppCrash {

        super();

        // Imposta range del campo
        setRange(param.get(RANGEMIN_NODE), param.get(RANGEMAX_NODE));
    }

    /**
     * StringVerify constructor.
     *
     * @param param Hashtable Hashtable contenente i parametri utili per la verifica del field
     * @param fieldName String Nome del field
     */
    public DataType_base(Hashtable param, String fieldName) throws AppCrash {

        super();

        // Imposta nome del campo
        setFieldName(fieldName);

        // Imposta range del campo
        setRange(param.get(RANGEMIN_NODE), param.get(RANGEMAX_NODE));
    }

    /**
     * Get nome campo
     *
     * @return String nome del campo
     */
    public String getFieldName() {

        return _name;
    }

    /**
     * Set Nome field
     *
     * @param name String Nome field.
     * @return void
     */
    public void setFieldName(String name) {

        _name = name;
    }

    /**
     * Set Formato data
     *
     * @param format String Formato.
     * @return void
     */
    public void setFormat(String format) {

    }

    /**
     * Range di valori. Min - Max
     *
     * @param min Object Valore minimo che il campo può assumere.
     * @param max Object Valore massimo che il campo può assumere.
     * @return void
     */
    public void setRange(Object min, Object max) throws AppCrash {

    }

    /**
     * Verifica se il campo soddisfa i requisiti. Default true
     * 
     * @param value String Object del campo da verificare
     * @return boolean
     */
    @Override
    public boolean verify(Object value) throws AppCrash {

        return true;
    }
}
