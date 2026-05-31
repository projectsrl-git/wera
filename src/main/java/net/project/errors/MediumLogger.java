/*
  MediumLogger.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 13/11/2000

  Autore: Simone Z.

  Note:

  Modifiche:

 */

package net.project.errors;

/**
 * Esegue il log delle chiamate a log0(), log1() e log2()
 */
public class MediumLogger extends Logger {

    protected MediumLogger() {

        super();
        setLogLevel(2);
    }

    public MediumLogger(String confName) {

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

}
