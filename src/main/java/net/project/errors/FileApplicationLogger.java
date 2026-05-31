/*
  FileApplicationLogger.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 15/05/2001

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.errors;

public class FileApplicationLogger extends FileLogWriter {

    private String _nomeLog;

    /**
     * Constructor. Legge il nome del log dell'applicazione dalla proprieta' "Logger.ApplicationLog"
     */
    public FileApplicationLogger(String tipo, String confName) {

        super(tipo, confName);
        _nomeLog = ErrorConfig.GetInstance(getConfName()).getProperty("Logger.ApplicationLog");
    }

    @Override
    public String getFileName() {

        return _nomeLog;
    }

}
