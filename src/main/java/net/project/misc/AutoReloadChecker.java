/*
  AutoReloadChecker.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 12/03/2002

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.misc;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;

/**
 * Classe che effettua controlli temporali per verificare se è necessario un reload automatico, in base al primo accesso
 * effettuato e alla data di scadenza calcolata.
 */
public class AutoReloadChecker {

    private int  _reloadHour = 1;   // indica l'ora di scadenza della cache nelle 24 ore.

    private Date _expireDate = null; // data di scadenza

    /**
     * Costruttore.
     *
     * @exception net.project.errors.AppCrash.
     */
    public AutoReloadChecker(String reloadHour) throws AppCrash {

        try {
            _reloadHour = Integer.parseInt(reloadHour);
            ErrDetector.GetInstance().postCond((_reloadHour >= 0) && (_reloadHour < 24));
            _expireDate = calculateExpireDate();
            Logger.GetInstance().log0("AUTORELOAD CHECKER ATTIVO - Expire date = " + _expireDate.toString());
        } catch (AppCrash e) {
            e.logContext("AutoReloadChecker", "Errore nel costruttore");
            throw e;
        }
    }

    /**
     * Controlla se deve essere fatto il refresh della cache.
     * 
     * @return boolean true se il tempo di permanenza degli oggetti in cache è scaduto
     */
    public boolean isReloadNeeded() {

        Calendar currentCalendar = new GregorianCalendar();
        if (currentCalendar.getTime().after(_expireDate)) {
            _expireDate = calculateExpireDate();
            Logger.GetInstance().log0("AUTORELOAD CHECKER - New expire date = " + _expireDate.toString());
            return true;
        }
        return false;
    }

    /**
     * Calcola la data esatta di scadenza della cache.
     * 
     * @exception net.project.errors.AppCrash.
     */
    private Date calculateExpireDate() {

        Calendar tempCalendar = new GregorianCalendar();
        Date today = new Date();
        tempCalendar.setTime(today);
        int hour = tempCalendar.get(Calendar.HOUR_OF_DAY);
        Calendar c = new GregorianCalendar();
        if (hour >= _reloadHour) {
            c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + 1);
        }
        c.set(Calendar.HOUR_OF_DAY, _reloadHour);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
}
