
package net.project.db;

/*
 IfxDBConnection.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 7/10/2000

 Autore: Simone Z.

 Note:

 Modifiche:

 */

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import net.project.errors.AppCrash;
import net.project.errors.DBCrash;

/**
 * Questa classe rappresenta una connessione ad un database Informix. Quando viene creata esegue l'istruzione SQL
 * "SET LOCK MODE TO WAIT 5" proprietary Informix per settare il tempo di retry (5 secondi) in caso di concorrenza degli
 * accessi ad una risorsa DB.
 */
public class IfxDBConnection extends DBConnection {

    /**
     * Costruttore.
     * 
     * @param URL java.lang.String database
     * @param user java.lang.String nome utente
     * @param password java.lang.String password
     * @exception AppCrash
     */
    public IfxDBConnection(String url, String user, String password) throws AppCrash {

        super(url, user, password);
        try {
            setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            Statement st = createStatement();
            st.executeUpdate("SET LOCK MODE TO WAIT 15");
            st.close();
        } catch (SQLException e) {
            DBCrash err = new DBCrash(e);
            throw err;
        }
    }

}
