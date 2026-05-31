/*
  SmsFactory.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 10/7/2002

  Autore: Luca M.

  Note:

  Modifiche:	

 */

package net.project.misc;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.ErrDetector_itf;

/**
 * Factory per costruire oggetti rappresentanti messaggi sms.
 * <p>
 * Proprietà da valorizzare nel file di configurazione: Sms.Class = nome della classe implementante
 * net.project.misc.Sms_itf che si vuole istanziare
 */
public class SmsFactory {

    /**
     * Metodo statico che crea un oggetto Sms_itf.
     * 
     * @return net.project.misc.Sms_itf l'oggetto creato. Non è MAI NULL.
     * @exception net.project.errors.AppCrash se la proprietà Sms.Class non è valorizzata nel file di configurazione, o
     *                in caso di errori nell'istanziazione della classe implementante Sms_itf indicata nella suddetta
     *                proprietà.
     */
    public static Sms_itf MakeSms() throws AppCrash {

        String className = Config.GetInstance().getProperty("Sms.Class");
        ErrDetector_itf ed = ErrDetector.GetInstance();
        ed.preCond(isNotEmpty(className), "proprieta' Sms.Class non valorizzata" + " nel file di configurazione");
        try {
            Sms_itf sms = (Sms_itf) Class.forName(className).newInstance();
            ed.param(sms);
            return sms;

        } catch (ClassNotFoundException cnfe) {
            AppCrash ac = new AppCrash(cnfe);
            ac.logContext("SmsFactory", "className: -" + className + "-");
            throw ac;
        } catch (IllegalAccessException iae) {
            AppCrash ac = new AppCrash(iae);
            ac.logContext("SmsFactory", "className: -" + className + "-");
            throw ac;
        } catch (InstantiationException ie) {
            AppCrash ac = new AppCrash(ie);
            ac.logContext("SmsFactory", "className: -" + className + "-");
            throw ac;
        }

    }

    private static boolean isNotEmpty(String what) {

        return ((what != null) && (what.trim().length() > 0));
    }

}
