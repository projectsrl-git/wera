/*
  RollingFileApplicationLogger.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 15/05/2001

  Autore: Anna L.

  Note: tratto da FileApplicationLogger.java

  Modifiche:

 */

package net.project.errors;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RollingFileApplicationLogger extends FileApplicationLogger {

    private SimpleDateFormat _formatter = new SimpleDateFormat("yyyy-MM-dd");

    public RollingFileApplicationLogger(String tipo, String confName) {

        super(tipo, confName);
    }

    @Override
    public String getFileName() {

        String nomeLog = super.getFileName();
        Date oggi = new Date();
        return nomeLog + "." + _formatter.format(oggi);
    }
}
