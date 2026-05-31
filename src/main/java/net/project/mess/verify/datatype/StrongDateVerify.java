/*
  StrongDateVerify.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 14/02/2003

  Autore: Rosella V.

  Note:

  Modifiche:

 */

package net.project.mess.verify.datatype;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;

import net.project.errors.AppCrash;

/**
 * Classe per il controllo puntuale del formato del timestamp
 */
public class StrongDateVerify extends DateVerify {

    /**
     * StrongStringVerify constructor.
     *
     * @param param Hashtable Hashtable contenente i parametri utili per la verifica del field
     */
    public StrongDateVerify(Hashtable param) throws AppCrash {

        super(param);
    }

    /**
     * StrongStringVerify constructor.
     *
     * @param param Hashtable Hashtable contenente i parametri utili per la verifica del field
     * @param fieldName String Nome del field
     */
    public StrongDateVerify(Hashtable param, String fieldName) throws AppCrash {

        super(param, fieldName);
    }

    /**
     * Effettua controlli sulla data.
     *
     * @param value String Valore del campo da controllare.
     * @param format Formato data.
     * @return true se il controllo ha dato esito positivo, false altrimenti.
     */
    @Override
    protected boolean checkDate(String value, String format) throws Throwable {

        boolean checkOK = true;

        try {
            // controllo che la lunghezza del formato passato coincida con la lunghezza del timestamp
            if (format.length() != value.length()) {
                checkOK = false;

                return checkOK;
            }

            if (value.indexOf("T") == 10 && format.indexOf("T") == 10) {
                value = value.replaceFirst("T", " ");
                format = format.replaceFirst("T", " ");
            }
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            formatter.setLenient(false);
            formatter.parse(value);
        } catch (ParseException ex) {
            checkOK = false;
        }

        return checkOK;
    }
}