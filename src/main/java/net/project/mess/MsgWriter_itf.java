/*
  MsgWriter_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 01/06/1999

  Autore: Rosella V.

  Note:

  Modifiche:		Vedi ClearCase history

 */

package net.project.mess;

import net.project.errors.AppCrash;

/**
 * L'interfaccia MsgWriter_itf definisce le capacita' di un generico creatore di messaggi (scrittore). Un messaggio e'
 * visto come un insieme logico di campi i quali sono identificati con un nome simbolico. I valori dei campi messaggi
 * sono rappresentati da stringhe ASCII
 *
 * @author Rosella Vergani
 */
public interface MsgWriter_itf extends MsgReader_itf {

    /**
     * Questo metodo serve per impostare il valore di un campo del messaggio
     *
     * @param java.lang.String name nome del campo.
     * @param java.lang.String value valore del campo.
     * @exception net.project.errors.AppCrash.
     */
    public void setField(String name, String value) throws AppCrash;

    /**
     * Questo metodo serve per recuperare il messaggio che e' stato composto.
     *
     * @return byte[] byte array contenente il messaggio
     * @exception net.project.errors.AppCrash.
     */
    public byte[] getMessage() throws AppCrash;
}
