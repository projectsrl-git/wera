/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.misc;

import net.project.errors.AppCrash;

/*
 Cache.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 03/12/2003

 Autore: Rosella V.

 Note:

 Modifiche:

 */

/**
 * Classe utilizzata per la gestione della cache
 */
public interface Cache {

    /**
     * Questo metodo
     *
     * @param key DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public abstract Object getData(CacheKey_itf key) throws AppCrash;

    /**
     * Questo metodo
     *
     * @param key DOCUMENT ME!
     * @param value DOCUMENT ME!
     */
    public abstract void putData(CacheKey_itf key, Object value) throws AppCrash;

    /**
     * Questo metodo
     *
     * @param key DOCUMENT ME!
     */
    public abstract void removeData(CacheKey_itf key) throws AppCrash;;

    /**
     * Questo metodo
     */
    public abstract void invalidate() throws AppCrash;;

    /**
     * Questo metodo
     *
     * @return DOCUMENT ME!
     */
    public abstract String getName();
}
