
package net.project.mess.verify.datatype;

/*
 DateVerify.java

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
 * Classe per il controllo del formato del timestamp
 * 
 */
public class DateVerify extends DataType_base {

    private String _format;

    private String _dateMin;
    private String _dateMax;

    /**
     * StringVerify constructor.
     *
     * @param param Hashtable Hashtable contenente i parametri utili per la verifica del field
     */
    public DateVerify(Hashtable param) throws AppCrash {

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
    public DateVerify(Hashtable param, String fieldName) throws AppCrash {

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
    protected boolean checkDate(String value, String format) throws Throwable {

        boolean checkOK = true;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            formatter.setLenient(false);
            formatter.parse(value);
        } catch (ParseException ex) {
            checkOK = false;
        }
        return checkOK;
    }

    /**
     * Set Formato data
     *
     * @param format String Formato.
     * @return void
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
            _dateMin = min.toString();
            _dateMax = max.toString();
        } catch (Throwable e) {
            AppCrash ex = new AppCrash(e);
            ex.logContext("DateVerify", "Errore setRange del campo " + getFieldName() + " - Time non inferiore " + min
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

            checkOK = checkDate(value.toString(), _format);

            if (!checkOK) {
                Logger.GetInstance().log0(
                        "check Date : checking " + getFieldName() + " - Formato " + _format + " - Data non inferiore "
                                + _dateMin + " - Data non inferiore " + _dateMax + " - Valore " + value);
            }

        } catch (Throwable e) {
            AppCrash ex = new AppCrash(e);
            ex.logContext("DateVerify", "Errore in verify");
            ex.logContext("DateVerify ", "Error checking " + getFieldName() + " - Formato " + _format
                    + " - Data non inferiore " + _dateMin + " - Data non inferiore " + _dateMax + " - Valore " + value);
            throw (ex);
        }
        return checkOK;
    }
}
