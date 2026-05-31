/*
  FullLogger.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 07/07/1999

  Autore: Simone Z.

  Note:

  Modifiche:	11/10/99	TL16 ristrutturazione eccezioni

 */

package net.project.errors;

public class FullLogger extends Logger {

    protected FullLogger() {

        super();
        setLogLevel(3);
    }

    public FullLogger(String confName) {

        super(confName);
    }

    @Override
    public void log1(String what) {

        logDebug(what);
    }

    @Override
    public void log2(String what) {

        logDebug(what);
    }

    @Override
    public void log3(String what) {

        logDebug(what);
    }

}
