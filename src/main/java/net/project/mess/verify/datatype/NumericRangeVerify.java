
package net.project.mess.verify.datatype;

/*
 NumericRangeVerify.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 07/06/2004

 Autore: Anna Legi

 Note:

 Modifiche:

 */
import java.util.Hashtable;

import net.project.errors.AppCrash;
import net.project.errors.Logger;

/**
 * Gestione Campo di tipo Numerico: controlla che il numero rientri nel range specificato
 */
public class NumericRangeVerify extends DataType_base {

    private long _min;
    private long _max;

    /**
     * NumericVerify constructor.
     *
     * @param param Hashtable Hashtable contenente i parametri utili per la verifica del field
     */
    public NumericRangeVerify(Hashtable param) throws AppCrash {

        super(param);
    }

    /**
     * NumericVerify constructor.
     *
     * @param param Hashtable Hashtable contenente i parametri utili per la verifica del field
     * @param fieldName String Nome del field
     */
    public NumericRangeVerify(Hashtable param, String fieldName) throws AppCrash {

        super(param, fieldName);
    }

    /**
     * Effettua controlli sui numeri.
     *
     * @param value String Valore del campo da controllare.
     * @param min int Valore minimo consentito per il campo.
     * @param max int Valore massimo consentito per il campo.
     * @return true se il controllo ha dato esito positivo, false altrimenti.
     */
    private boolean checkNumber(String value, long min, long max) {

        boolean checkOK = true;
        if ((value.length() > 1) && (value.startsWith("0"))) {
            return false;
        }
        try {
            long integer = Long.parseLong(value);
            if ((integer < min) || (integer > max)) {
                checkOK = false;
            }
        } catch (NumberFormatException e) {
            checkOK = false;
        }
        return checkOK;
    }

    /**
     * Range di valori. Min - Max
     *
     * @param min Object Valore minimo che il campo può assumere.
     * @param max Object Valore massimo che il campo può assumere.
     * @return void
     */
    @Override
    public void setRange(Object min, Object max) throws AppCrash {

        try {
            _min = Long.parseLong(min.toString());
            _max = Long.parseLong(max.toString());
        } catch (Throwable e) {
            AppCrash ex = new AppCrash(e);
            ex.logContext("NumericRangeVerify", "Errore setRange del campo " + getFieldName()
                    + " - Time non inferiore " + min + " - Time non inferiore " + max);
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

            if (!value.equals("")) {
                checkOK = checkNumber(value.toString(), _min, _max);
            } else {
                checkOK = false;
            }

            if (!checkOK) {
                Logger.GetInstance().log0(
                        "checkNumeric : checking " + getFieldName() + " - Min " + _min + " - Max " + _max + " - Val "
                                + value);
            }

        } catch (Throwable e) {
            AppCrash ex = new AppCrash(e);
            ex.logContext("NumericRangeVerify", "Errore in verify");
            ex.logContext("NumericRangeVerify ", "Error checking " + getFieldName() + " - Min " + _min + " - Max "
                    + _max + " - Val " + value);
            throw (ex);
        }
        return checkOK;
    }
}