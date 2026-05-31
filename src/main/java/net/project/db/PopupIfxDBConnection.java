
package net.project.db;

/*
 PopupIfxDBConnection.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari 

 Data creazione: 7/10/2000

 Autore: Simone Z.

 Note:

 Modifiche:

 */

import net.project.errors.AppCrash;

/**
 * Questa classe rappresenta una connessione ad un database Informix che viene creata solo quando occorre e che quando
 * viene rilasciata si chiude.
 */
public class PopupIfxDBConnection extends IfxDBConnection {

    /**
     * Costruttore.
     * 
     * @param URL java.lang.String database
     * @param user java.lang.String nome utente
     * @param password java.lang.String password
     * @exception AppCrash
     */
    public PopupIfxDBConnection(String URL, String user, String password) throws AppCrash {

        super(URL, user, password);
    }

    @Override
    public void free() {

        try {
            super.close();
        } catch (AppCrash er) {
            // Niente da fare, posso proseguire perche' significa che la connessione era gia' chiusa
        } finally {
            super.free();
        }
    }

}
