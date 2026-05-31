/*

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore: 

  Note:

 */

package net.project.mess.verify;

import net.project.errors.AppCrash;
import net.project.mess.MsgReader_itf;

/**
 * Questa interfaccia rappresenta un oggetto capace di effettuare un controllo semantico su un generico messaggio.
 * Oggetti di questo tipo vengono usati come strategy per paramentrizzare la verifica del contenuto dei messaggi dalla
 * classe SemantiLogicalMessageSpec
 * 
 * @author sim
 */
public interface SemanticCheck_itf {

    /**
     * Questo metodo esegue la verifica semantica del valore dei campi del messaggio passato.
     * <P>
     * Le classi che implementano questo metodo devono ritornare una stringa con la descrizione dell'errore o "" se
     * tutto e' OK.
     *
     * @param msg MsgReader_itf con il messaggio da verificare
     * @return String con la descrizione dell'errore o "" se tutto OK.
     *
     * @throws AppCrash DOCUMENT ME!
     */
    public String check(MsgReader_itf msg) throws AppCrash;
}