
package net.project.mess.verify.datatype;

/*
 EnumerationVerify.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 17/05/2001

 Autore: Assunta Ciervo

 Note:

 Modifiche:

 */
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import net.project.errors.AppCrash;
import net.project.errors.Logger;

/**
 * Gestione Campo di tipo Enumeration
 * 
 */
public class EnumerationVerify extends DataType_base {

    private Vector _range;

    /**
     * StringVerify constructor.
     *
     * @param param Hashtable Hashtable contenente i parametri utili per la verifica del field
     */
    public EnumerationVerify(Hashtable param) throws AppCrash {

        super(param);
        setRangeEnumeration(param.get(RANGEENUM_NODE));
    }

    /**
     * StringVerify constructor.
     *
     * @param param Hashtable Hashtable contenente i parametri utili per la verifica del field
     * @param fieldName String Nome del field
     */
    public EnumerationVerify(Hashtable param, String fieldName) throws AppCrash {

        super(param, fieldName);
        setRangeEnumeration(param.get(RANGEENUM_NODE));
    }

    /**
     * Effettua controlli sulla lunghezza delle stringhe.
     *
     * @param value Enumeration Valore dell' Enumeration da controllare
     * @return true se il controllo ha dato esito positivo, false altrimenti.
     */
    private boolean checkEnumeration(Object value) {

        boolean checkOK = true;
        if (!_range.contains(value)) {
            checkOK = false;
        }

        return checkOK;
    }

    /**
     * Effettua controlli sulla lunghezza delle stringhe.
     *
     * @param value Enumeration Valore dell' Enumeration da controllare
     * @return true se il controllo ha dato esito positivo, false altrimenti.
     */
    private boolean checkEnumeration(Enumeration value) {

        boolean checkOK = true;
        int trovati = 0;
        int nEl = 0;
        while (value.hasMoreElements()) {
            Object o = value.nextElement();
            nEl = +1;
            if (_range.contains(o)) {
                trovati = +1;
            }
        }
        if (nEl != trovati) checkOK = false;
        return checkOK;
    }

    /**
     * Valori possibili
     *
     * @param min Object Valore minimo che il campo può assumere.
     * @param max Object Valore massimo che il campo può assumere.
     * @return void
     */
    public void setRangeEnumeration(Object range) throws AppCrash {

        try {

            StringTokenizer st = new StringTokenizer(range.toString(), "|");
            _range = new Vector();
            while (st.hasMoreTokens()) {
                _range.addElement(st.nextToken());
            }

        } catch (Throwable e) {
            AppCrash ex = new AppCrash(e);
            ex.logContext("EnumerationVerify", "Errore Enumeration " + getFieldName() + " - Range " + range.toString());
            throw (ex);
        }
    }

    /**
     * Verifica se il campo soddisfa i requisiti
     * 
     * @param value String Valore del campo da verificare
     * @return boolean
     */
    @Override
    public boolean verify(Object value) throws AppCrash {

        boolean checkOK = true;
        try {
            if (value.equals("")) return checkOK;

            checkOK = checkEnumeration(value);

            if (!checkOK) {
                Logger.GetInstance().log0(
                        "check Enumeration : checking " + getFieldName() + " Valore: " + value.toString());
            }

        } catch (Throwable e) {
            AppCrash ex = new AppCrash(e);
            ex.logContext("EnumerationVerify", "Errore in verify");
            ex.logContext("EnumerationVerify ", "Error checking " + getFieldName() + " Valore: " + value.toString());
            throw (ex);
        }
        return checkOK;
    }
}