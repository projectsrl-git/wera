/*
  Logger_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 29/09/2000

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.errors;

import java.util.Hashtable;

/**
 * Definisce l'interfaccia utilizzata per effettuare il log degli errori ed il log di debug delle applicazioni
 */
public interface Logger_itf {

    /**
     * Inizia il log di un errore. Vengono riportate nel file la data e la print della stack trace
     * 
     * @param err e' l'eccezione che costituisce l'errore.
     */
    public void logError(Exception err);

    /**
     * Inizia il log di un errore. Vengono riportate nel file la data e la print della stack trace
     * 
     * @param err e' l'errore.
     */
    public void logError(Throwable err);

    /**
     * Aggiunge al file di log degli errori una stringa passata.
     * 
     * @param what e' la stringa da aggiungere al log
     */
    public void logError(String what);

    public void logError(String type, String what);

    /**
     * Scrive sul syslog una stringa passata.
     * 
     * @param what e' la stringa da aggiungere al syslog
     */
    public void logSyslog(String what);

    public void logSyslog(String type, String what);

    /**
     * Scrive sul log applicativo una stringa passata.
     * 
     * @param what e' la stringa da aggiungere al log applicativo
     */
    public void logApplication(String type, String what);

    public void logApplication(String what);

    public void log0(String what);

    public void log1(String what);

    public void log2(String what);

    public void log3(String what);

    // Trattamento Info collection
    public void addInfo(String nome, Object valore);

    public void addInfo(Hashtable userInfo);

    public void dumpInfo(String tipo);

    public void dumpInfo();

    public void flush();

    public Hashtable getInfo();

    public void removeInfo(String nome);

    public void removeInfo(String[] infoNames);

    public void resetInfo();

    public int getLogLevel();

}
