/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.misc;

/*
 SimpleCache.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 01/12/2003

 Autore: Rosella V.

 Note:

 Modifiche:

 */
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;

/**
 * Classe utilizzata per la gestione della cache
 */
public class SimpleCache implements Cache {

    private Hashtable  _data;
    private int        _reloadHour = 1; // indica l'ora di scadenza della cache nelle 24 ore. Viene indicata nel

    // file di configurazione impostando il valore "Cache."+nomeCache+".ReloadHour"
    private Loader_itf _loader;         // la classe da usare e che implementa tale interfaccia viene indicata nel
                                         // file di configurazione impostando il valore "Cache."+nomeCache+".Loader"
    private String     name        = "";
    private Date       _firstAccess;    // ora di primo accesso
    private Date       _expireDate;     // data di scadenza

    /**
     * Costruttore.
     */
    private SimpleCache() throws AppCrash {

        super();
    }

    /**
     * Costruttore.
     *
     * @param name java.lang.String identificativo della cache.
     */
    public SimpleCache(String name) throws AppCrash {

        this.name = name;

        // istanzio il loader
        String loaderName = Config.GetInstance().getProperty("Cache." + name + ".Loader");
        ErrDetector.GetInstance().param(loaderName != null);

        try {
            Class classObject = Class.forName(loaderName.trim());
            Constructor procBaseConstr = classObject.getConstructor(new Class[] {});

            // Istanzio la classe tramite il costruttore ottenuto
            _loader = (Loader_itf) procBaseConstr.newInstance(new Object[] {});
        } catch (ClassNotFoundException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("SimpleCache", "Nome cache: " + name + " ,Errore nella istanziazione del loader - Classe: "
                    + loaderName);
            throw err;
        } catch (InstantiationException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("SimpleCache", "Nome cache: " + name + " ,Errore nella istanziazione del loader - Classe: "
                    + loaderName);
            throw err;
        } catch (NoSuchMethodException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("SimpleCache", "Nome cache: " + name + " ,Errore nella istanziazione del loader - Classe: "
                    + loaderName);
            throw err;
        } catch (InvocationTargetException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("SimpleCache", "Nome cache: " + name + " ,Errore nella istanziazione del loader - Classe: "
                    + loaderName);
            throw err;
        } catch (IllegalAccessException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("SimpleCache", "Nome cache: " + name + " ,Errore nella istanziazione del loader - Classe: "
                    + loaderName);
            throw err;
        }

        // assegno tempo di reload se valorizzato altrimenti rimane impostato quello di default
        String reloadHour = Config.GetInstance().getProperty("Cache." + name + ".ReloadHour");

        if (reloadHour != null) {
            _reloadHour = Integer.parseInt(reloadHour);
            ErrDetector.GetInstance().postCond((_reloadHour >= 0) && (_reloadHour < 24));
        }

        // inizializzo la cache
        _data = new Hashtable();
    }

    /**
     * Restituisce l'oggetto corrispondente alla chiave key.
     *
     * @param key net.project.misc.CacheKey_itf chiave che identifica l'oggetto in cache.
     *
     * @return DOCUMENT ME!
     *
     * @exception net.project.errors.AppCrash.
     */
    @Override
    public Object getData(CacheKey_itf key) throws AppCrash {

        Object object = null;
        ErrDetector.GetInstance().param(key != null);

        if (_firstAccess != null) {
            if (expire()) { // caso cache scaduta
                _data.clear();
                _firstAccess = null;
                _expireDate = null;
                object = _loader.load(key);

                if (object != null) {
                    // aggiungo l'oggetto in cache solo se valorizzato
                    _data.put(key, object);
                }
            } else if ((object = _data.get(key)) != null) { // caso di cache attiva e oggetto già in cache

                return object;
            } else { // caso di cache attiva ma oggetto non ancora in cache
                object = _loader.load(key);

                if (object != null) {
                    // aggiungo l'oggetto in cache solo se valorizzato
                    _data.put(key, object);
                }

                return object;
            }
        } else {
            object = _loader.load(key);

            if (object != null) {
                // aggiungo l'oggetto in cache solo se valorizzato
                _data.put(key, object);
                _firstAccess = new Date();
                _expireDate = calculateExpireDate();
            }
        }

        return object;
    }

    /**
     * Restituisce il nome della cache.
     *
     * @return java.lang.String nome della cache
     */
    @Override
    public String getName() {

        return name;
    }

    /**
     * Controlla se deve essere fatto il refresh della cache.
     *
     * @return boolean true se il tempo di permanenza degli oggetti in cache è scaduto
     */
    private boolean expire() {

        boolean result = false;

        Calendar currentCalendar = new GregorianCalendar();

        if (currentCalendar.getTime().after(_expireDate)) {
            result = true;
        }

        return result;
    }

    /**
     * Calcola la data esatta di scadenza della cache.
     *
     * @return DOCUMENT ME!
     *
     * @exception net.project.errors.AppCrash.
     */
    private Date calculateExpireDate() {

        Calendar tempCalendar = new GregorianCalendar();
        tempCalendar.setTime(_firstAccess);

        int hour = tempCalendar.get(Calendar.HOUR_OF_DAY);
        Calendar c = new GregorianCalendar();

        if (hour >= _reloadHour) {
            c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + 1);
            c.set(Calendar.HOUR_OF_DAY, _reloadHour);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
        } else {
            c.set(Calendar.HOUR_OF_DAY, _reloadHour);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
        }

        return c.getTime();
    }

    /*
     * Invalida l'istanza di Cache dal repository.
     * 
     * @exception net.project.errors.AppCrash.
     */
    @Override
    public void invalidate() throws AppCrash {

        try {
            Repository.GetInstance().invalidate(getName());
        } catch (AppCrash e) {
            e.logContext("SimpleCache", "errore durante invalidate");
            throw e;
        }
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

        if (_firstAccess != null) {
            if (expire()) { // caso cache scaduta
                _data.clear();
                _firstAccess = null;
                _expireDate = null;

                if (object != null) {
                    // aggiungo l'oggetto in cache solo se valorizzato
                    _data.put(key, object);
                }
            } else { // caso di cache attiva ma oggetto non ancora in cache

                if (object != null) {
                    // aggiungo l'oggetto in cache solo se valorizzato
                    _data.put(key, object);
                }
            }
        } else {
            if (object != null) {
                // aggiungo l'oggetto in cache solo se valorizzato
                _data.put(key, object);
                _firstAccess = new Date();
                _expireDate = calculateExpireDate();
            }
        }
    }

    /**
     * Rimuove dalla cache la chiave key e il suo correspettivo oggetto.
     *
     * @param key net.project.misc.CacheKey_itf chiave che identifica l'oggetto in cache da eliminare.
     *
     * @exception net.project.errors.AppCrash.
     */
    @Override
    public void removeData(CacheKey_itf key) throws AppCrash {

        _data.remove(key);
    }

    /**
     * Aggiunge l'oggetto con chiave key in cache
     *
     * @param key net.project.misc.CacheKey_itf Chiave che identifica l'oggetto
     * @param object java.lang.Object Oggetto aggiunto in cache
     */
    protected void putElement(CacheKey_itf key, Object object) {

        _data.put(key, object);
    }

    /**
     * Ritorna l'oggetto con chiave key se in cache, altrimenti null
     *
     * @param key net.project.misc.CacheKey_itf Chiave che identifica l'oggetto
     *
     * @return java.lang.Object Oggetto recuperato dalla cache
     */
    protected Object getElement(CacheKey_itf key) {

        return _data.get(key);
    }

    /**
     * Questo metodo
     *
     * @return DOCUMENT ME!
     */
    protected Loader_itf getLoader() {

        return _loader;
    }
}
