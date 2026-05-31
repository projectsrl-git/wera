
package net.project.mess.verify.datatype;

/*
 TimeVerify.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 17/05/2001

 Autore: Assunta Ciervo

 Note:

 Modifiche:

 */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;

import net.project.errors.AppCrash;
import net.project.errors.Logger;

/**
 * Gestione Campo di tipo Time
 * 
 */
public class TimeVerify extends DataType_base {

    private String _format;

    private String _timeMin;
    private String _timeMax;

    /**
     * StringVerify constructor.
     *
     * @param param Hashtable Hashtable contenente i parametri utili per la verifica del field
     */
    public TimeVerify(Hashtable param) throws AppCrash {

        super(param);

        // Formato Field
        setFormat(param.get(FORMAT_NODE).toString());
    }

    /**
     * StringVerify constructor.
     *
     * @param param Hashtable Hashtable contenente i parametri utili per la verifica del field
     * @param fieldName String Nome del field
     */
    public TimeVerify(Hashtable param, String fieldName) throws AppCrash {

        super(param, fieldName);

        // Formato Field
        setFormat(param.get(FORMAT_NODE).toString());
    }

    /**
     * Effettua controlli sulla data.
     *
     * @param value String Valore del campo da controllare.
     * @param format Formato data.
     * @return true se il controllo ha dato esito positivo, false altrimenti.
     */
    private boolean checkTime(String value, String format) {

        boolean checkOK = true;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            formatter.parse(value);
        } catch (ParseException ex) {
            checkOK = false;
        }
        return checkOK;
    }

    /**
     * Formato time
     * 
     */
    @Override
    public void setFormat(String format) {

        _format = format;
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
            _timeMin = min.toString();
            _timeMax = max.toString();
        } catch (Throwable e) {
            AppCrash ex = new AppCrash(e);
            ex.logContext("TimeVerify", "Errore setRange del campo " + getFieldName() + " - Time non inferiore " + min
                    + " - Time non inferiore " + max);
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

            checkOK = checkTime(value.toString(), _format);

            if (!checkOK) {
                Logger.GetInstance().log0(
                        "verify Time: checking " + getFieldName() + " - Formato " + _format + " - Time non inferiore "
                                + _timeMin + " - Time non inferiore " + _timeMax + " - Valore " + value);
            }

        } catch (Throwable e) {
            AppCrash ex = new AppCrash(e);
            ex.logContext("TimeVerify", "Errore in verify");
            ex.logContext("TimeVerify ", "Error checking " + getFieldName() + " - Formato " + _format
                    + " - Time non inferiore " + _timeMin + " - Time non inferiore " + _timeMax + " - Valore " + value);
            throw (ex);
        }
        return checkOK;
    }
}