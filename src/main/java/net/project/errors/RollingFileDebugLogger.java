/*
  RollingFileDebugLogger.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 09/10/1999

  Autore: Simone Z.

  Note:

  Modifiche:

 */

package net.project.errors;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RollingFileDebugLogger extends FileDebugLogger {

    private SimpleDateFormat _formatter = new SimpleDateFormat("yyyy-MM-dd");

    public RollingFileDebugLogger(String tipo, String confName) {

        super(tipo, confName);
    }

    @Override
    public String getFileName() {

        String nomeLog = super.getFileName();
        Date oggi = new Date();
        return nomeLog + "." + _formatter.format(oggi);
    }

}
