/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.misc;

/*
 CacheFactory.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari 

 Data creazione: 01/12/2003

 Autore: Rosella V.

 Note:

 Modifiche:

 */
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;

/**
 * Classe per la costruzione di oggetti Cache_itf
 */
public abstract class CacheFactory {

    /**
     * Ritorna una istanza di tipo Cache_itf.
     *
     * @param cacheName java.lang.String formato di trasmissione da eseguire.
     *
     * @return DOCUMENT ME!
     *
     * @exception AppCrash.
     */
    public static Cache MakeCache(String cacheName) throws AppCrash {

        Cache cache = null;
        String className = null;

        // Recupero dal file di configurazione la classe da istanziare
        try {
            className = Config.GetInstance().getProperty("Cache." + cacheName + ".class",
                    "net.project.misc.SimpleCache");
            ErrDetector.GetInstance().invariant(className != null);

            // istanzio la classe per Cache
            Class tempClasse = Class.forName(className);
            Constructor procBaseConstr = tempClasse.getConstructor(new Class[] { String.class });

            // Istanzio la classe tramite il costruttore ottenuto
            cache = (Cache) procBaseConstr.newInstance(new Object[] { cacheName });
        } catch (ClassNotFoundException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("CacheFactory", "Nome Cache : " + cacheName + " Classe: " + className);
            throw err;
        } catch (InstantiationException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("CacheFactory", "Nome Cache : " + cacheName + " Classe: " + className);
            throw err;
        } catch (NoSuchMethodException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("CacheFactory", "Nome Cache : " + cacheName + " Classe: " + className);
            throw err;
        } catch (InvocationTargetException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("CacheFactory", "Nome Cache : " + cacheName + " Classe: " + className);
            throw err;
        } catch (IllegalAccessException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("CacheFactory", "Nome Cache : " + cacheName + " Classe: " + className);
            throw err;
        } catch (AppCrash err) {
            err.logContext("CacheFactory", "Nome Cache : " + cacheName + " Classe: " + className);
            throw err;
        }

        ErrDetector.GetInstance().invariant(cache != null);

        return cache;
    }
}
