/*
  AppCrash.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione: 14/05/1999

  Autore: Simone Z.

  Note:

  Modifiche:	06/10/99	TL16 ristrutturazione

 */

package net.project.errors;

public class AppCrash extends Exception {

    private Throwable _originalException;

    /**
     * Costruttore.
     */
    public AppCrash() {

        Logger.GetInstance().logError(this);
    }

    /**
     * Costruttore.
     * 
     * @param mess java.lang.String Il messaggio d'errore da loggare.
     */
    public AppCrash(String mess) {

        this();
        logContext("AppCrash ", mess);
    }

    /**
     * Costruttore.
     *
     * @param except java.lang.Exception L'eccezione verificatasi.
     */
    public AppCrash(Exception except) {

        _originalException = except;

        // Stampo lo stack trace se l'eccezione non e' di tipo AppCrash, altrimenti significa che e
        // gia' stato stampato
        if (!(except instanceof AppCrash)) {
            Logger.GetInstance().logError(except);
            Logger.GetInstance().logError("Tipo: JVM Exception");
        }
    }

    /**
     * Costruttore.
     *
     * @param error java.lang.Throwable. L'errore verificatisi.
     */
    public AppCrash(Throwable error) {

        _originalException = error;

        // Stampo lo stack trace se l'eccezione non e' di tipo AppCrash, altrimenti significa che e
        // gia' stato stampato
        if (!(error instanceof AppCrash)) {
            Logger.GetInstance().logError(error);
            Logger.GetInstance().logError("Tipo: JVM Error");
        }
    }

    /**
     * Effettua il log degli errori verificatisi.
     *
     * @param classe java.lang.String La classe in cui si è verificata l'eccezione.
     * @param info java.lang.String Informazioni sull'eccezione verificatasi.
     */
    public void logContext(String classe, String info) {

        Logger.GetInstance().logError("----------");
        Logger.GetInstance().logError("Classe: " + classe);
        Logger.GetInstance().logError(info);
    }

    /**
     * Ritorna l'eccezione che ha generato l'AppCrash.
     *
     * @return java.lang.Throwable L'eccezione originale.
     */
    public Throwable getOriginalException() {

        return _originalException;
    }
}
