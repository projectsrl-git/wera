/*
 MsgProcessor_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 01/06/1999

  Autore: Rosella V.

  Note:

  Modifiche:

 */

package net.project.mess;

import net.project.errors.AppCrash;

/**
 * L'interfaccia MsgProcessor_itf definisce un generico oggetto capace di elaborare un messaggio in un protocollo di
 * tipo request/response.
 *
 * @author Rosella Vergani
 */
public interface MsgProcessor_itf {

    /**
     * Questo metodo serve per recuperare il messaggio di esito prodotto dalla elaborazione del messaggio
     *
     * @return byte[] retorna un byte array contenete il messaggio applicativo di risposta
     * @exception net.project.errors.AppCrash Nel caso vi fossere stati problemi durante la lettura della risposta
     */
    public byte[] getResponse() throws AppCrash;

    /**
     * Questo metodo serve per invocare l'elaborazione del messaggio
     *
     * @exception net.project.errors.AppCrash Nel caso vi fossere stati problemi durante il processing
     */
    public void process() throws AppCrash;
}
