/*

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore:

  Note:

 */

package net.project.db;

import net.project.errors.AppCrash;

/**
 * Definisce l'interfaccia utilizzata per creare un pool di connessioni ad un database.
 */
public interface ConnectionPool_itf {

    /**
     * Ritorna una connessione a un database
     *
     * @return DBConnection_itf la connessione
     *
     * @throws AppCrash
     */
    public DBConnection_itf getAConnection() throws AppCrash;

    /**
     * Chiude tutte le connessione del pool.
     *
     * @throws AppCrash
     */
    public void shutdown() throws AppCrash;

    /**
     * Ritorna informazioni sulla classe.
     *
     * @return java.lang.String.
     */
    @Override
    public String toString();

    /**
     * Questo metodo viene invocato dalla DBConnection per notificare al ConnectionPool che la connessione in questione
     * e' stata liberata. Il metodo e' accedibile solo dal package net.project.db e tale deve rimanere.
     *
     * @param dbConn la connessione liberata
     */
    public void freeNotify(DBConnection_itf dbConn);
}
