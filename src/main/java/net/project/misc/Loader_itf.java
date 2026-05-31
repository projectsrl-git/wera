/*
  Loader_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 08/07/2001

  Autore: Rosella V.

  Note:

  Modifiche:

 */

package net.project.misc;

import net.project.errors.AppCrash;

/**
 * Definisce l'interfaccia utilizzata per la gestione del caricamento di un certo numero di oggetti ciascuno
 * identificato da una chiave.
 */
public interface Loader_itf {

    /**
     * Restituisce una Hashtable contenente tutti gli oggetti da mettere in cache.
     * 
     * @param key java.lang.Object chiave che identifica l'oggetto in cache.
     * @return java.lang.Object.
     * @exception net.project.errors.AppCrash.
     */
    public Object load(Object key) throws AppCrash;

}