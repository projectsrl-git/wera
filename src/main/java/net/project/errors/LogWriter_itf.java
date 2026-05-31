/*
  LogWriter_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 09/10/1999

  Autore: Simone Z.

  Note:

  Modifiche: Ilaria/ aggiunti metodi flush e dump e modificati quelli esistenti

 */

package net.project.errors;

import java.util.Hashtable;

public interface LogWriter_itf {

    public void WriteLog(String message, Hashtable info);

    public void WriteLog(Exception ex, Hashtable info);

    public void WriteLog(Throwable err, Hashtable info);

    public void WriteLog(String type, String message, Hashtable info);

    public void WriteLog(Hashtable info);

    public void dumpInfo(Hashtable info);

    public void flush();
}
