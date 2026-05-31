/*

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore: 

  Note:

 */

package net.project.db;

/**
 * Classe base per la costruzione dei connection pool; raggruppa i metodi comuni
 * 
 * @author sim
 */
public abstract class ConnectionPool_base {

    public void freeNotify(DBConnection_itf dbConn) {

        // nulla da fare
    }
}
