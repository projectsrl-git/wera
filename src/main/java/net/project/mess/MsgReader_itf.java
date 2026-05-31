/*
  MsgReader_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 01/06/1999

  Autore: Rosella V.

  Note:

  Modifiche:		Vedi ClearCase history

 */

package net.project.mess;

import net.project.errors.AppCrash;

/**
 * L'interfaccia MsgReader_itf definisce le capacita' di un generico interprete di messaggi (lettore). Un messaggio e'
 * visto come un insieme logico di campi i quali sono identificati con un nome simbolico. Ad ogni messaggio e' sempre
 * associata una proprieta' detta <b>tipo</b> che distingue le varie classi di messaggi.
 * <P>
 * I valori dei campi messaggi sono rappresentati da stringhe ASCII
 *
 * @author Rosella Vergani
 */
public interface MsgReader_itf {

    /**
     * Questo metodo serve per recuperare il valore di un campo dal messaggio. Se il campo non e' presente nel messaggio
     * viene una ritornata stringa vuota
     *
     * @return java.lang.String valore del campo richiesto
     * @param java.lang.String Name nome del campo da leggere
     * @exception net.project.errors.AppCrash Nel caso vi fossere problemi nella lettura del campo.
     */
    public String getField(String Name) throws AppCrash;

    /**
     * Questo metodo restituisce il valore della proprieta' <b>tipo</b> del messaggio applicativo
     *
     * @return java.lang.String tipo del messaggio
     */
    public String getType();
}
