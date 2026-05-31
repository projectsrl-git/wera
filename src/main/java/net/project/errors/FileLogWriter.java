/*
  FileLogWriter.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 15/05/2001

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.errors;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileLogWriter extends StreamLogWriter_base {

    private String           _nomeLog;
    private SimpleDateFormat _formatter = new SimpleDateFormat("yyyy-MM-dd");
    private boolean          _rolling   = false;

    /**
     * Constructor. Legge il nome del log dell'applicazione dalla proprieta' "Logger.FileLogger"
     */
    public FileLogWriter(String tipo, String confName) {

        super(tipo, confName);

        String nomeFile = new String("Logger.file." + getLogType() + ".filename");
        _nomeLog = ErrorConfig.GetInstance(getConfName()).getProperty(nomeFile);

        if (_nomeLog == null) return;

        int k = _nomeLog.indexOf("%%");
        if (k == -1) return;

        _formatter = new SimpleDateFormat(_nomeLog.substring(k + 2));
        _nomeLog = _nomeLog.substring(0, k);
        _rolling = true;

    }

    protected String getFileName() {

        if (_rolling) {
            Date oggi = new Date();
            return _nomeLog + "." + _formatter.format(oggi);
        }
        return _nomeLog;
    }

    @Override
    protected void writeToStream(String message) {

        try {
            PrintWriter out = new PrintWriter(new FileWriter(getFileName(), true));
            out.println(message);
            out.close();
        } catch (Throwable t) {
            t.printStackTrace(System.out);
        }
    }

}
