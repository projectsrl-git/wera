
package net.project.mess.verify.datatype;

/*
 StringVerify.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 17/05/2001

 Autore: Assunta Ciervo

 Note:

 Modifiche:

 */
import java.util.Hashtable;

import net.project.errors.AppCrash;
import net.project.errors.Logger;

/**
 * Gestione Campo di tipo Alfanumerico
 * 
 */
public class StringVerify extends DataType_base {

    private int _min;
    private int _max;

    /**
     * StringVerify constructor.
     *
     * @param param Hashtable Hashtable contenente i parametri utili per la verifica del field
     */
    public StringVerify(Hashtable param) throws AppCrash {

        super(param);
    }

    /**
     * StringVerify constructor.
     *
     * @param param Hashtable Hashtable contenente i parametri utili per la verifica del field
     * @param fieldName String Nome del field
     */
    public StringVerify(Hashtable param, String fieldName) throws AppCrash {

        super(param, fieldName);
    }

    /**
     * Effettua controlli sulla lunghezza delle stringhe.
     *
     * @param value String Valore del campo da controllare.
     * @param min int Lunghezza minima consentita per il campo.
     * @param max int Lunghezza massima consentita per il campo.
     * @return true se il controllo ha dato esito positivo, false altrimenti.
     */
    private boolean checkString(String value, int min, int max) {

        boolean checkOK = true;
        if (value.length() < min || value.length() > max) {
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
            ex.logContext("StringVerify", "Errore setRange del campo " + getFieldName() + " - Time non inferiore "
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
            if (value.equals("")) return checkOK;

            checkOK = checkString(value.toString(), _min, _max);

            if (!checkOK) {
                Logger.GetInstance().log0(
                        "checkString : checking " + getFieldName() + " - Min " + _min + " - Max " + _max + " - Val "
                                + value);
            }

        } catch (Throwable e) {
            AppCrash ex = new AppCrash(e);
            ex.logContext("StringVerify", "Errore in verify");
            ex.logContext("StringVerify ", "Error checking " + getFieldName() + " - Min " + _min + " - Max " + _max
                    + " - Val " + value);
            throw (ex);
        }
        return checkOK;
    }
}