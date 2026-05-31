/*
 Copyright (c) by SSB Spa Societa' per i Servizi Bancari

 Note:

 */

package net.project.mess.verify.datatype;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.project.errors.AppCrash;

/**
 * Questa classe e' un verifier che utilizza una generica espressione regolare per verificare la correttezza sintattica
 * di un campo.
 * <p>
 * Utilizza gli elementi RangeMin RangeMax e Format
 *
 */
public class RegExpVerifier extends DataType_base implements DiagnosiFieldVerifier_itf {

    public static final String TOOSHORT  = "RXSHORT";
    public static final String TOOLONG   = "RXLONG";
    public static final String DONTMATCH = "RXNOMATCH";

    private String             _regExp;
    private int                _min;
    private int                _max;
    private Pattern            _pattern;

    /**
     * @param param
     * @throws AppCrash
     */
    public RegExpVerifier(Hashtable param) throws AppCrash {

        super(param);
        // Formato Field
        setFormat(param.get(FORMAT_NODE).toString());
    }

    /**
     * @param param
     * @param fieldName
     * @throws AppCrash
     */
    public RegExpVerifier(Hashtable param, String fieldName) throws AppCrash {

        super(param, fieldName);

        // Formato Field
        setFormat(param.get(FORMAT_NODE).toString());

    }

    /**
     * Range di valori.Lunghezza Min - Max della stringa
     *
     * @param min Object Valore minimo che il campo può assumere.
     * @param max Object Valore massimo che il campo può assumere.
     * @return void
     */
    @Override
    public void setRange(Object min, Object max) throws AppCrash {

        // RangeMin e RangeMax sono facoltativi
        if (min == null || max == null) return;

        try {
            _min = Integer.parseInt(min.toString());
            _max = Integer.parseInt(max.toString());
        } catch (Throwable e) {
            AppCrash ex = new AppCrash(e);
            ex.logContext(this.getClass().getName(), "Errore setRange del campo " + getFieldName()
                    + " - Time non inferiore " + min + " - Time non inferiore " + max);
            throw (ex);
        }

    }

    /**
     * Questo metodo imposta il valore della regexp da usare per la validazione
     *
     * @param format
     *
     * @see net.project.mess.verify.datatype.DataType_base#setFormat(java.lang.String)
     */
    @Override
    public void setFormat(String format) {

        _regExp = format;
        _pattern = Pattern.compile(_regExp);
    }

    /**
     * Questo metodo esegue la verifica del campo passato: la lunghezza deve essere compresa fra min e max ed il campo
     * deve essere un match della regexp indicata nel formato.
     * <p>
     * Se min e max non sono stati specificati non viene fatto il controllo sulla lunghezza.
     *
     * @param value il campo da verificare
     * @return boolean true se ok false se ko
     * @throws AppCrash
     *
     * @see net.project.mess.verify.datatype.DataType_base#verify(java.lang.Object)
     */
    @Override
    public boolean verify(Object value) throws AppCrash {

        String field = value.toString().trim();

        if (_min != 0 && _max != 0) {
            if (field.length() < _min || field.length() > _max) return false;
        }

        Matcher match = _pattern.matcher(field);
        return match.matches();
    }

    /**
     * Questo metodo verifica il campo e restituisce una diagnosi ovvero un codice di errore
     *
     * @param value
     * @return
     *
     * @see net.project.mess.verify.datatype.DiagnosiFieldVerifier_itf#diagnose(java.lang.String)
     */
    @Override
    public String diagnose(String value) {

        String field = value.toString().trim();

        // se il controllo sulla lunghezza deve essere fatto
        if (_min != 0 && _max != 0) {
            if (field.length() < _min) return TOOSHORT;
            if (field.length() > _max) return TOOLONG;
        }

        // verifica match sulla regular expression
        Matcher match = _pattern.matcher(field);
        if (match.matches() == false) return DONTMATCH;

        // tutto ok
        return null;
    }

}
