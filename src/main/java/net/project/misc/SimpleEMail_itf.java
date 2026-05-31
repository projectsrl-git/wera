/*
  SimpleEMail_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 23/10/2001

  Autore: Simone Z.

  Note:

  Modifiche:	

 */

package net.project.misc;

import net.project.errors.AppCrash;

/**
 * Questa classe rappresenta le funzionalità di un semplice messaggio di posta elettronica senza attachment.
 */
public interface SimpleEMail_itf {

    /**
     * Imposta l'host.
     * 
     * @param host java.lang.String host.
     */
    public void setHost(String host);

    /**
     * Imposta il messaggio da spedire.
     * 
     * @param message java.lang.String messaggio da spedire.
     */
    public void setMessage(String message);

    /**
     * Imposta il destinatario del messaggio.
     * 
     * @param to java.lang.String destinatario del messaggio.
     */
    public void setTo(String to);

    /**
     * Imposta il destinatario in copia del messaggio.
     * 
     * @param to java.lang.String destinatario in copia del messaggio.
     */
    public void setCc(String cc);

    /**
     * Imposta il destinatario in copia nascosta del messaggio.
     * 
     * @param to java.lang.String destinatario in copia nascosta del messaggio.
     */
    public void setBcc(String bcc);

    /**
     * Imposta il mittente del messaggio.
     * 
     * @param to java.lang.String mittente del messaggio.
     */
    public void setFrom(String from);

    /**
     * Imposta l'indirizzo da usare come "Replay to:"
     * 
     * @param replayto java.lang.String indirizzo.
     */
    public void setReplayTo(String replayto);

    /**
     * Ottiene l'indirizzo da usare come "Replay to:"
     * 
     * @return java.lang.String indirizzo usato come "Replay to:".
     */
    public String getReplayTo();

    /**
     * Imposta il subject.
     * 
     * @param subject java.lang.String soggetto dell'e-mail.
     */
    public void setSubject(String subject);

    /**
     * Invia il messaggio specificato all'host sulla porta 25.
     * 
     * @param message java.lang.String messaggio da spedire.
     * @exception net.project.errors.AppCrash Se si è verificato un errore di I/O durante l'invio del messaggio.
     */
    public void send(String message) throws AppCrash;

}
