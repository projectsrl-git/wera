
package net.project.db;

/*
 TimedDBConnection.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 7/10/2000

 Autore: Simone Z.

 Note:

 Modifiche:

 */

import java.sql.Connection;

import net.project.errors.AppCrash;

/**
 * Questa classe rappresenta una connessione ad un database generico che viene creata solo quando occorre e che quando
 * viene rilasciata si chiude.
 */
public class TimedDBConnection extends DBConnection {

    /**
     * Costruttore.
     * 
     * @param URL java.lang.String database
     * @param user java.lang.String nome utente
     * @param password java.lang.String password
     * @exception AppCrash
     */
    public TimedDBConnection(String url, String user, String password) throws AppCrash {

        super(url, user, password);

        setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

    }

    @Override
    public void free() {

        try {
            setAutoCommit(true);
        } catch (AppCrash er) {
            // Niente da fare, posso proseguire perche' significa che la connessione era gia' chiusa
        } finally {
            super.free();
        }
    }

}
