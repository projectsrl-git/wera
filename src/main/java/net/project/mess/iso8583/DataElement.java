
package net.project.mess.iso8583;

/*
 DataElement.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 19/05/1999

 Autore: Rosella V.

 Note:

 Modifiche:	13/10/99	TL16 modifiche per ristrutturazione eccezioni
 25/11/99	TL17 ottimizzazione uso stringhe. Rosella V.

 */

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.InvariantCrash;

/**
 * Rappresenta un campo ISO8583.
 */

public class DataElement implements FormatLengthType_itf {

    private int    _maxLength  = 0;
    private int    _lengthType = 0;
    private String _filler     = null;

    /**
     * Imposta il formato della lunghezza, la massima lunghezza e il filling da applicare al campo.
     * 
     * @param formatLen formato della lunghezza (FIX, LLVAR, LLLVAR).
     * @param maxLen lunghezza massima del campo.
     * @param filler filling da applicare (con blank e allineato a sinistra se il campo non è numerico, con zero e
     *            allineato a destra se il campo è numerico).
     * @exception AppCrash.
     */
    DataElement(String formatLen, String maxLen, String filler) throws AppCrash {

        setLengthType(formatLen);
        _maxLength = Integer.parseInt(maxLen);
        setFiller(filler);
    }

    /**
     * Ritorna il tipo di lunghezza del campo.
     * 
     * @return tipo di lunghezza.
     * @exception AppCrash.
     * @see net.project.mess.iso8583.FormatLengthType_itf
     */
    public int getLengthType() {

        return _lengthType;
    }

    /**
     * Ritorna la massima lunghezza del campo.
     * 
     * @return massima lunghezza del campo.
     */
    public int getMaxLength() {

        return _maxLength;
    }

    /**
     * Imposta il formato della lunghezza.
     * 
     * @param formatLength formato della lunghezza (FIX, LLVAR o LLLVAR).
     */
    private void setLengthType(String formatLength) throws AppCrash {

        if (formatLength.toUpperCase().equals("LLLVAR")) {
            _lengthType = LLLVAR;
        } else if (formatLength.toUpperCase().equals("LLVAR")) {
            _lengthType = LLVAR;
        } else if (formatLength.toUpperCase().equals("FIX")) {
            _lengthType = FIX;
        } else {
            InvariantCrash err = new InvariantCrash("Format error: " + formatLength);
            throw err;
        }
        return;
    }

    /**
     * Ritorna il filler applicato al campo.
     * 
     * @return filler.
     */
    public String getFiller() {

        return _filler;
    }

    /**
     * Imposta il filler per il campo.
     * 
     * @param filler.
     * @exception AppCrash.
     */
    public void setFiller(String filler) throws AppCrash {

        ErrDetector.GetInstance().param(filler);
        _filler = filler;
    }

    /**
     * Ritorna il valore formattato del campo. Se il campo è di lunghezza variabile viene prefissato dalla lunghezza.
     * 
     * @param value valore del campo.
     * @return valore del campo fillato.
     * @exception AppCrash.
     */
    public String formatField(String value) throws AppCrash {

        String field = null;
        StringBuffer tempField = null;
        int target = 0;
        StringBuffer stringLenTemp = null;
        try {

            // controllo se il campo è a lunghezza fissa: se lo è deve essere fillato
            if (getLengthType() == FIX) {
                tempField = new StringBuffer();
                if ((getFiller().equals("n")) || (getFiller().equals("nP"))) {
                    for (int i = 0; i < (getMaxLength() - value.length()); i++) {
                        tempField.append("0");
                    }
                    tempField.append(value);
                } else {
                    tempField.append(value);
                    for (int i = 0; i < (getMaxLength() - value.length()); i++) {
                        tempField.append(" ");
                    }
                }

            } else {
                tempField = new StringBuffer();
                target = getLengthType();
                stringLenTemp = new StringBuffer();
                stringLenTemp.append(Integer.toString(value.length()));
                int count = stringLenTemp.length();
                for (int z = 0; z < target - count; z++) {
                    stringLenTemp.insert(0, 0);
                }
                tempField.append(stringLenTemp);
                tempField.append(value);
            }
            ErrDetector.GetInstance().postCond(tempField != null);
            field = tempField.toString(); // assegnamento del campo di indice key
        } catch (AppCrash e) {
            e.logContext("DataElement", "Data element : " + value);
            throw e;
        }
        return field;
    }

}
