/*
  MsgChecker.java 

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 31/07/2000 

  Autore: Anna L.

  Note:

  Modifiche:	
  
 */

package net.project.mess;

import net.project.errors.AppCrash;
import net.project.errors.Logger;

/**
 * La classe MessageChecker viene utilizzata per effettuare controlli sui campi dei messaggi.
 */
public class MsgChecker {

    MsgReader_itf _msgSource = null;

    /**
     * Costruttore.
     *
     * @param msgSource net.project.mess.MsgReader_itf Il messaggio da controllare.
     */
    public MsgChecker(MsgReader_itf msgSource) {

        _msgSource = msgSource;
    }

    /**
     * Effettua controlli sulla lunghezza delle stringhe.
     *
     * @param value java.lang.String Valore del campo da controllare.
     * @param min Lunghezza minima consentita per il campo.
     * @param max Lunghezza massima consentita per il campo.
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
     * Effettua controlli sulla presenza e sulla lunghezza delle stringhe.
     *
     * @param name java.lang.String Nome del campo da controllare.
     * @param min Lunghezza minima consentita per il campo.
     * @param max Lunghezza massima consentita per il campo.
     * @return true se il controllo ha dato esito positivo, false altrimenti.
     * @exception net.project.errors.AppCrash.
     */
    public boolean checkStringObblig(String name, int min, int max) throws AppCrash {

        boolean checkOK = true;
        String value = "noval";

        try {
            value = _msgSource.getField(name);
            if (!value.equals("")) {
                checkOK = checkString(value, min, max);
            } else {
                checkOK = false;
                Logger.GetInstance().log0(
                        "checkStringObblig : checking " + name + " - Min " + min + " - Max " + max + " - Val " + value);
            }
        } catch (AppCrash ex) {
            ex.logContext("MsgChecker", "Errore in checkStringObblig");
            ex.logContext("checkStringObblig ", "Error checking " + name + " - Min " + min + " - Max " + max
                    + " - Val " + value);
            throw (ex);
        }
        return checkOK;
    }

    /**
     * Effettua controlli sulla lunghezza delle stringhe.
     *
     * @param name java.lang.String Nome del campo da controllare.
     * @param min Lunghezza minima consentita per il campo.
     * @param max Lunghezza massima consentita per il campo.
     * @return true se il controllo ha dato esito positivo, false altrimenti.
     * @exception net.project.errors.AppCrash.
     */
    public boolean checkStringFacolt(String name, int min, int max) throws AppCrash {

        boolean checkOK = true;
        String value = "noval";

        try {
            value = _msgSource.getField(name);
            if (value.equals("")) return checkOK;

            checkOK = checkString(value, min, max);
            if (!checkOK) {
                Logger.GetInstance().log0(
                        "checkStringFacolt : checking " + name + " - Min " + min + " - Max " + max + " - Val " + value);
            }
        } catch (AppCrash ex) {
            ex.logContext("MsgChecker", "Errore in checkStringFacolt");
            ex.logContext("checkStringFacolt ", "Error checking " + name + " - Min " + min + " - Max " + max
                    + " - Val " + value);
            throw (ex);
        }
        return checkOK;
    }

    /**
     * Effettua controlli sulla presenza e sul valore della valuta.
     *
     * @param name java.lang.String Nome del campo da controllare.
     * @return true se il controllo ha dato esito positivo, false altrimenti.
     * @exception net.project.errors.AppCrash.
     */
    public boolean checkValuta(String name) throws AppCrash {

        boolean checkOK = true;
        String valuta = "noval";

        try {
            valuta = _msgSource.getField(name);
            if (!valuta.equals("")) {
                if (!(valuta.equals("380")) && (!valuta.equals("978"))) {
                    checkOK = false;
                }
            } else {
                checkOK = false;
                Logger.GetInstance().log0("checkValuta : checking " + name + " - Val " + valuta);
            }
        } catch (AppCrash ex) {
            ex.logContext("MsgChecker", "Errore in checkValuta");
            ex.logContext("checkValuta ", "Error checking " + name + " - Val " + valuta);
            throw (ex);
        }
        return checkOK;
    }

    /**
     * Effettua controlli sui numeri.
     *
     * @param value java.lang.String Valore del campo da controllare.
     * @param min Lunghezza minima consentita per il campo.
     * @param max Lunghezza massima consentita per il campo.
     * @return true se il controllo ha dato esito positivo, false altrimenti.
     */
    private boolean checkNumber(String value, int min, int max) {

        boolean checkOK = true;
        try {
            Integer integer = new Integer(value);
            if ((integer.intValue() < min) || (integer.intValue() > max)) {
                checkOK = false;
            }
        } catch (NumberFormatException e) {
            checkOK = false;
        }
        return checkOK;
    }

    /**
     * Effettua controlli sulla presenza e sulla lunghezza dei numeri.
     *
     * @param name java.lang.String Nome del campo da controllare.
     * @param min Lunghezza minima consentita per il campo.
     * @param max Lunghezza massima consentita per il campo.
     * @return true se il controllo ha dato esito positivo, false altrimenti.
     * @exception net.project.errors.AppCrash.
     */
    public boolean checkNumberObblig(String name, int min, int max) throws AppCrash {

        boolean checkOK = true;
        String value = "noval";

        try {
            value = _msgSource.getField(name);
            if (!value.equals("")) {
                checkOK = checkNumber(value, min, max);
            } else {
                checkOK = false;
            }
        } catch (AppCrash ex) {
            ex.logContext("MsgChecker", "Errore in checkNumberObblig");
            ex.logContext("checkNumberObblig ", "Error checking " + name + " - Min " + min + " - Max " + max
                    + " - Val " + value);
            throw (ex);
        }

        if (!checkOK) {
            Logger.GetInstance().log0(
                    "checkNumberObblig : checking " + name + " - Min " + min + " - Max " + max + " - Val " + value);
        }

        return checkOK;
    }
}
