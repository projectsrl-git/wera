
package net.project.db;

/*
 TimedIfxDBConnection.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 7/10/2000

 Autore: Simone Z.

 Note:

 Modifiche:

 */

import java.sql.SQLException;
import java.sql.Statement;

import net.project.errors.AppCrash;
import net.project.errors.DBCrash;

/**
 * Questa classe rappresenta una connessione ad un database generico che viene creata solo quando occorre e che quando
 * viene rilasciata si chiude.
 */
public class TimedIfxDBConnection extends TimedDBConnection {

    /**
     * Costruttore.
     * 
     * @param URL java.lang.String database
     * @param user java.lang.String nome utente
     * @param password java.lang.String password
     * @exception AppCrash
     */
    public TimedIfxDBConnection(String url, String user, String password) throws AppCrash {

        super(url, user, password);
        try {
            Statement st = createStatement();
            st.executeUpdate("SET LOCK MODE TO WAIT 5");
            st.close();
        } catch (SQLException e) {
            DBCrash err = new DBCrash(e);
            throw err;
        }
    }

    @Override
    public void free() {

        super.free();

    }

}
