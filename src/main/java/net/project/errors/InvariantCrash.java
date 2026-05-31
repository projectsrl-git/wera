/*
  InvariantCrash.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione: 06/10/1999

  Autore: Simone Z.

  Note:

 */

package net.project.errors;

public class InvariantCrash extends AppCrash {

    /**
     * Costruttore.
     */
    public InvariantCrash() {

        super();
        Logger.GetInstance().logError("Tipo: InvariantCrash");
    }

    /**
     * Costruttore.
     * 
     * @param mess java.lang.String messaggio esplicativo del crash.
     */
    public InvariantCrash(String mess) {

        super(mess);
        Logger.GetInstance().logError("Tipo: InvariantCrash");
    }
}
