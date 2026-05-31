/*
  ErrDetector_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione: 14/05/1999

  Autore: Simone Z.

  Note:

  Modifiche:	08/10/99	TL16 ristrutturazione eccezioni
				02/10/2000	Anna L.		Aggiunta interfaccia.  

 */

package net.project.errors;

/**
 * Definisce l'interfaccia utilizzata per la rilevazione degli errori.
 */

public interface ErrDetector_itf {

    /**
     * Controlla la validita' di un parametro.
     *
     * @param cond boolean La condizione booleana da verificare.
     * @exception net.project.errors.ParamCrash Se la condizione è falsa.
     */
    public void param(boolean cond) throws ParamCrash;

    /**
     * Controlla la validita' di un parametro.
     *
     * @param cond boolean La condizione booleana da verificare.
     * @param messaggio java.lang.String Il messaggio da loggare al verificarsi del crash.
     * @exception net.project.errors.ParamCrash Se la condizione è falsa.
     */
    public void param(boolean cond, String messaggio) throws ParamCrash;

    /**
     * Controlla che una oggetto non sia null
     *
     * @param obj java.lang.Object L'oggetto da controllare.
     * @exception net.project.errors.ParamCrash Se l'oggetto è null.
     */
    public void param(Object obj) throws ParamCrash;

    /**
     * Controlla che una stringa non sia vuota
     *
     * @param str java.lang.String La stringa da controllare.
     * @exception net.project.errors.ParamCrash Se la stringa è vuota.
     */
    public void param(String str) throws ParamCrash;

    /**
     * Controlla l'esistenza di una precondizione
     *
     * @param cond boolean La precondizione da verificare.
     * @exception net.project.errors.AppCrash Se la precondizione è falsa.
     */
    public void preCond(boolean cond) throws AppCrash;

    /**
     * Controlla l'esistenza di una precondizione
     *
     * @param cond boolean La precondizione da verificare.
     * @param messaggio java.lang.String Il messaggio di errore.
     * @exception net.project.errors.AppCrash Se la precondizione è falsa.
     */
    public void preCond(boolean cond, String messaggio) throws AppCrash;

    /**
     * Controlla l'esistenza di una postcondizione
     *
     * @param cond boolean La postcondizione da verificare.
     * @exception net.project.errors.AppCrash Se la postcondizione è falsa.
     */
    public void postCond(boolean cond) throws AppCrash;

    /**
     * Controlla l'esistenza di una postcondizione
     *
     * @param cond boolean La postcondizione da verificare.
     * @param messaggio java.lang.String Il messaggio di errore.
     * @exception net.project.errors.AppCrash Se la postcondizione è falsa.
     */
    public void postCond(boolean cond, String messaggio) throws AppCrash;

    /**
     * Controlla la validita' di un invariante
     *
     * @param cond boolean L'invariante da verificare.
     * @exception net.project.errors.AppCrash Se l'invariante è falsa.
     */
    public void invariant(boolean cond) throws AppCrash;

    /**
     * Controlla la validita' di un invariante
     *
     * @param cond boolean L'invariante da verificare.
     * @param messaggio java.lang.String Il messaggio di errore.
     * @exception net.project.errors.AppCrash Se l'invariante è falsa.
     */
    public void invariant(boolean cond, String messaggio) throws AppCrash;
}
