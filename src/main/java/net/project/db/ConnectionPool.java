/*
  ConnectionPool.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 24/05/1999

  Autore: Simone Z.

  Note:

  Modifiche:

 */

package net.project.db;

import java.util.Hashtable;

import net.project.errors.AppCrash;

/**
 * Questa classe rappresenta il punto di ingresso per ottenere un oggetto di tipo ConnectionPool_itf per accedere ad un
 * database.
 */
public class ConnectionPool {

    static private ConnectionPool_itf _Instance;
    static private Hashtable          _MultiPool = new Hashtable();

    private ConnectionPool() {

        super();
    }

    /**
     * Implementazione del pattern Singleton. Restituisce l'istanza dell'oggetto.
     */
    public static ConnectionPool_itf GetInstance() throws AppCrash {

        // questo e' un "Double checked lock" design pattern
        if (_Instance == null) {
            synchronized (ConnectionPool.class) {
                if (_Instance == null) {
                    _Instance = DBFactory.makeConnectionPool("");
                }
            }
        }

        return _Instance;
    }

    /**
     * Implementazione del pattern Singleton su piu' oggetti. Restituisce l'istanza dell'oggetto ConnectionPool
     * associato al nome passato.
     * 
     * @param name java.lang.String nome del ConnectionPool da recuperare
     */
    public static ConnectionPool_itf GetInstance(String name) throws AppCrash {

        if (name.equals("")) return GetInstance();

        // questo e' un "Double checked lock" design pattern
        ConnectionPool_itf result = (ConnectionPool_itf) _MultiPool.get(name);
        if (result == null) {
            synchronized (ConnectionPool.class) {
                result = (ConnectionPool_itf) _MultiPool.get(name);
                if (result == null) {
                    result = DBFactory.makeConnectionPool(name);

                    _MultiPool.put(name, result);
                }
            }
        }
        return result;

    }

}
