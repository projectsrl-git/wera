/*
  DBCrash.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione: 06/10/1999

  Autore: Simone Z.

  Note:

 */

package net.project.errors;

import java.sql.SQLException;

public class DBCrash extends AppCrash {

    /**
     * Costruttore. Effettua il log di errori verificatisi durante l'accesso a database.
     * 
     * @param except java.lang.SQLException L'eccezione SQL verificatasi.
     */
    public DBCrash(SQLException except) {

        super(except);
        Logger.GetInstance().logError("Tipo: DBCrash");
        while (except != null) {
            Logger.GetInstance().logError("Messaggio: " + except.getMessage());
            Logger.GetInstance().logError("SQL state: " + except.getSQLState());
            Logger.GetInstance().logError("Err code: " + except.getErrorCode());
            except = except.getNextException();
        }
    }
}
