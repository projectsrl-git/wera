
package net.project.mess.verify.datatype;

/*
 NumericVerify.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 17/05/2001

 Autore: Assunta Ciervo

 Note:

 Modifiche:

 */
import java.math.BigInteger;
import java.util.Hashtable;

import net.project.errors.AppCrash;
import net.project.errors.Logger;

/**
 * Gestione Campo di tipo Numerico
 * 
 */
public class NumericVerify extends DataType_base {

    private int _min;
    private int _max;

    /**
     * NumericVerify constructor.
     *
     * @param param Hashtable Hashtable contenente i parametri utili per la verifica del field
     */
    public NumericVerify(Hashtable param) throws AppCrash {

        super(param);
    }

    /**
     * NumericVerify constructor.
     *
     * @param param Hashtable Hashtable contenente i parametri utili per la verifica del field
     * @param fieldName String Nome del field
     */
    public NumericVerify(Hashtable param, String fieldName) throws AppCrash {

        super(param, fieldName);
    }

    /**
     * Effettua controlli sui numeri.
     *
     * @param value String Valore del campo da controllare.
     * @param min int Lunghezza minima consentita per il campo.
     * @param max int Lunghezza massima consentita per il campo.
     * @return true se il controllo ha dato esito positivo, false altrimenti.
     */
    private boolean checkNumber(String value, int min, int max) {

        boolean checkOK = true;
        try {
            BigInteger integer = new BigInteger(value);
            if ((value.length() < min) || (value.length() > max)) {
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
            _min = Integer.parseInt(min.toString());
            _max = Integer.parseInt(max.toString());
        } catch (Throwable e) {
            AppCrash ex = new AppCrash(e);
            ex.logContext("NumericVerify", "Errore setRange del campo " + getFieldName() + " - Time non inferiore "
                    + min + " - Time non inferiore " + max);
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
            ex.logContext("NumericVerify", "Errore in verify");
            ex.logContext("NumericVerify ", "Error checking " + getFieldName() + " - Min " + _min + " - Max " + _max
                    + " - Val " + value);
            throw (ex);
        }
        return checkOK;
    }
}