/*

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore:

  Note:

 */

package net.project.db;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Questa classe rappresenta il registro delle connessioni a DB usate da un thread. Il suo utilizzo e' prevede che venga
 * chiamato add() ogni volta che viene richiamata getaConnection() e remove() dopo ogni free(). La classe viene usata
 * dal decorator SafeDBConnection
 *
 * @author sim
 */
public class DBConnectionRegister extends ThreadLocal {

    private Set _connections = new HashSet();

    /**
     * Crea un nuovo oggetto di tipo DBConnectionRegister .
     */
    public DBConnectionRegister() {

        super();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
    public Object initialValue() {

        return new DBConnectionRegister();
    }

    /**
     * Il metodo add mette nel Set delle connessioni usate dal thread chiamante l'oggetto DBConnection_itf passato.
     *
     * @param dbConn la connessione da aggiungere a quelle usate
     */
    public void add(DBConnection_itf dbConn) {

        _connections.add(dbConn);
    }

    /**
     * Il metodo remove cancella dal Set l'oggetto DBConnection_itf passato
     *
     * @param dbConn la connessione non piu' usata
     */
    public void remove(DBConnection_itf dbConn) {

        _connections.remove(dbConn);
    }

    /**
     * Questo metodo serve per ottenere un un iterator con tutte le connessioni che sono nel registro per il thread
     * chiamante
     *
     * @return Iterator un iterator con tutte le connessioni che sono nel registro
     */
    public Iterator getAllThreadConnections() {

        return _connections.iterator();
    }
}