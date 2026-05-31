/*
  Sms_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 10/7/2002

  Autore: Luca M.

  Note:

  Modifiche:	

 */

package net.project.misc;

import net.project.errors.AppCrash;

/**
 * Interfaccia che rappresenta un sms.
 */
public interface Sms_itf {

    /**
     * Specifica il mittente.
     * 
     * @param java.lang.String from Il mittente.
     */
    public void setFrom(String from);

    /**
     * Specifica il destinatario.
     * 
     * @param java.lang.String to Il destinatario.
     */
    public void setTo(String to);

    /**
     * Specifica il messaggio.
     * 
     * @param java.lang.String message Il messaggio.
     */
    public void setMessage(String message);

    /**
     * Invia l'sms.
     * 
     * @exception net.project.errors.AppCrash in caso di errori nell'invio dell'sms.
     */
    public void send() throws AppCrash;

}
