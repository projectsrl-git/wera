/*
  LowLogger.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 13/11/2000

  Autore: Simone Z.

  Note:

  Modifiche:

 */

package net.project.errors;

/**
 * Esegue il log delle sole chiamate a log0() e log1()
 */
public class LowLogger extends Logger {

    protected LowLogger() {

        super();
        setLogLevel(1);
    }

    public LowLogger(String confName) {

        super(confName);
    }

    @Override
    public void log1(String what) {

        logDebug(what);
    }

}
