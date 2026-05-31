/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.misc;

import java.util.Calendar;
import java.util.GregorianCalendar;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;

/**
 * Classe che gestisce una cache di oggetti ciascuno dei quali è associato al tempo di caricamento
 */
public class TimedCache extends SimpleCache {

    /**
     * La classe
     *
     * @author $author$
     * @version $Revision: 1.1 $
     */
    private static class TimedCacheContainer {

        private GregorianCalendar _loadingTime;
        private Object            _cacheObjectValue;
        private int               _reloadingTime = 60; // intervallo di tempo di validità dell'oggetto espresso in
                                                       // minuti

        /**
         * Creates a new TimedCacheContainer object.
         *
         * @param value DOCUMENT ME!
         * @param cacheName DOCUMENT ME!
         */
        public TimedCacheContainer(Object value, String cacheName) {

            _loadingTime = new GregorianCalendar();
            _cacheObjectValue = value;
            _reloadingTime = Integer.valueOf(
                    Config.GetInstance().getProperty("TimedCache." + cacheName + ".ReloadingTime", "60")).intValue();

            net.project.errors.Logger.GetInstance().log3(cacheName + " : _reloadingTime" + _reloadingTime);
        }

        /**
         * Questo metodo
         *
         * @return DOCUMENT ME!
         */
        public Object getObject() {

            return _cacheObjectValue;
        }

        /**
         * Questo metodo
         *
         * @return DOCUMENT ME!
         */
        public GregorianCalendar getLoadingTime() {

            return _loadingTime;
        }

        /**
         * Questo metodo
         *
         * @return DOCUMENT ME!
         */
        public boolean isExpired() {

            boolean expired = false;
            GregorianCalendar calendarNow = new GregorianCalendar();
            GregorianCalendar tmp = (GregorianCalendar) _loadingTime.clone();
            tmp.add(Calendar.MINUTE, _reloadingTime);

            if (calendarNow.after(tmp)) {
                expired = true;
            }

            return expired;
        }
    }

    /**
     * Costruttore.
     *
     * @param name java.lang.String identificativo della cache.
     */
    public TimedCache(String name) throws AppCrash {

        super(name);
    }

    /**
     * Inserisce in cache l'oggetto legato alla chiave key.
     *
     * @param key net.project.misc.CacheKey_itf chiave che identifica l'oggetto in cache.
     * @param object Object Oggetto da inserire in cache.
     *
     * @exception net.project.errors.AppCrash.
     */
    @Override
    public void putData(CacheKey_itf key, Object object) throws AppCrash {

        ErrDetector.GetInstance().param(key != null);

        if (object != null) {
            // aggiungo l'oggetto in cache solo se valorizzato
            // verifico se l'oggetto è scaduto
            TimedCacheContainer tcc = (TimedCacheContainer) getElement(key);

            if (tcc.isExpired()) {
                TimedCacheContainer tmpTcc = new TimedCacheContainer(object, getName());
                putElement(key, tmpTcc);
            }
        }
    }

    /**
     * Restituisce l'oggetto corrispondente alla chiave key.
     *
     * @param key net.project.misc.CacheKey_itf chiave che identifica l'oggetto in cache.
     *
     * @return java.lang.Object Oggetto in cache.
     *
     * @exception net.project.errors.AppCrash.
     */
    @Override
    public Object getData(CacheKey_itf key) throws AppCrash {

        Object object = null;
        ErrDetector.GetInstance().param(key != null);

        if ((object = getElement(key)) != null) { // caso oggetto già in cache

            // verifico se l'oggetto è scaduto
            TimedCacheContainer tcc = (TimedCacheContainer) object;

            if (tcc.isExpired()) {
                Logger.GetInstance().log0("Timedcache scaduta: " + getName() + " Key: " + key.toString());

                // ricarico l'oggetto in cache
                Object tmpObject = loadObject(key);

                return tmpObject;
            } else {
                return tcc.getObject();
            }
        } else { // caso oggetto non ancora in cache
            Logger.GetInstance().log0("Timedcache manca: " + getName() + " Key: " + key.toString());
            Object tmpObject = loadObject(key);

            return tmpObject;
        }
    }

    /**
     * Questo metodo sincronizzato garantisce che se anche molti thread ritengono un oggetto scaduto, solo uno riesce a
     * ricaricarlo. Gli altri thread entrando in loadObject e ripetendo il controllo troveranno che non e' piu'
     * necessario caricare l'oggetto. E' una specie di "Double checked lock" design pattern.
     * 
     * @param key CacheKey_itf chive da caricare
     * @return Object l'oggetto caricato
     * @throws AppCrash in caso di crash del loader
     */
    protected synchronized Object loadObject(CacheKey_itf key) throws AppCrash {

        Logger.GetInstance().log0("Timedcache load: " + getName() + " Key: " + key.toString());
        Object object = getElement(key);

        // verifico se l'oggetto è scaduto o non c'e'
        TimedCacheContainer tcc = (TimedCacheContainer) object;

        if (tcc == null || tcc.isExpired()) {
            Logger.GetInstance().log0("Timedcache load1: " + getName() + " Key: " + key.toString());
            // carico l'oggetto in cache
            Object tmpObject = getLoader().load(key);

            TimedCacheContainer tmpTcc = new TimedCacheContainer(tmpObject, getName());
            putElement(key, tmpTcc);

            return tmpObject;
        } else {
            Logger.GetInstance().log0("Timedcache noload: " + getName() + " Key: " + key.toString());
            return tcc.getObject();
        }

    }

}
