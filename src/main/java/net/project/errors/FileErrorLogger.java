/*
  FileErrorLogger.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 09/10/1999

  Autore: Simone Z.

  Note:

  Modifiche:

 */

package net.project.errors;

public class FileErrorLogger extends FileLogWriter {

    private String _nomeLog;

    /**
     * Constructor . Legge il nome della directory di crash log dalla proprieta' "Logger.CrashDir"
     */
    public FileErrorLogger(String tipo, String confName) {

        super(tipo, confName);
        _nomeLog = ErrorConfig.GetInstance(getConfName()).getProperty("Logger.CrashDir");
    }

    @Override
    public String getFileName() {

        String threadId = Thread.currentThread().getName();
        return (_nomeLog + threadId);
    }

}
