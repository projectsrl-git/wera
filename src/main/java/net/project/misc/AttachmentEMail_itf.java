/*
  AttachmentEMail_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 27/06/2003

  Autore: Fabio F. e Luca M.

  Note:

  Modifiche:	

 */

package net.project.misc;

import net.project.errors.AppCrash;

/**
 * Interfaccia che definisce le funzionalità di invio di una email con allegato
 */
public interface AttachmentEMail_itf extends SimpleEMail_itf {

    /**
     * Aggiunge un allegato privo di nome all'email.
     * 
     * @param attachment byte[] L'allegato da inviare. Non può essere null.
     * @param mimeType java.lang.String Il mime type. Non può essere null né stringa vuota.
     * @exception net.project.errors.AppCrash Se uno dei parametri di ingresso è null o in caso di errore nell'aggiunta
     *                dell'allegato
     */
    public void addAttachment(byte[] attachment, String mimeType) throws AppCrash;

    /**
     * Aggiunge un allegato all'email.
     * 
     * @param attachment byte[] L'allegato da inviare. Non può essere null.
     * @param mimeType java.lang.String Il mime type. Non può essere null né stringa vuota.
     * @param attachmentName java.lang.String Il nome dell'allegato. Può anche essere null o stringa vuota.
     * @exception net.project.errors.AppCrash Se uno dei parametri di ingresso è null o in caso di errore nell'aggiunta
     *                dell'allegato
     */
    public void addAttachment(byte[] attachment, String mimeType, String attachmentName) throws AppCrash;

}
