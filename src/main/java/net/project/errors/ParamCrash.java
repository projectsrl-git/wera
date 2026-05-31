/*
  ParamCrash.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione: 06/10/1999

  Autore: Simone Z.

  Note:

 */

package net.project.errors;

public class ParamCrash extends AppCrash {

    /**
     * Costruttore.
     */
    public ParamCrash() {

        super();
        Logger.GetInstance().logError("Tipo: ParamCrash");
    }

    /**
     * Costruttore.
     * 
     * @param mess java.lang.String messaggio esplicativo del crash.
     */
    public ParamCrash(String mess) {

        super(mess);
        Logger.GetInstance().logError("Tipo: ParamCrash");
    }
}
