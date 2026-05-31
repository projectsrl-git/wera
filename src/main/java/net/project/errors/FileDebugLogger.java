/*
  FileDebugLogger.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 09/10/1999

  Autore: Simone Z.

  Note:

  Modifiche:

 */

package net.project.errors;

public class FileDebugLogger extends FileLogWriter {

    private String _nomeLog;

    /**
     * Constructor . Legge il nome del file di debug dalla proprieta' "Logger.DebugFile"
     */
    public FileDebugLogger(String tipo, String confName) {

        super(tipo, confName);
        _nomeLog = ErrorConfig.GetInstance(getConfName()).getProperty("Logger.DebugFile");
    }

    @Override
    public String getFileName() {

        return _nomeLog;
    }

}
