/*
  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore: Simone Z.

  Note:


 */

package net.project.mess;

import java.util.Iterator;

import net.project.errors.AppCrash;

/**
 * Questa interfaccia rappresenta l'estensione di MsgReader che introduce la possibilita' di recuperare l'elenco dei
 * campi facenti parte della specifica del messaggio. In effetti vengono restituiti i nomi dei campi che possono fare
 * parte del messaggio, non quelli che effettivamente sono presenti nello stesso.
 * 
 * @author Simone
 */
public interface MsgReaderEx_itf extends MsgReader_itf {

    /**
     * Questo metodo serve per recuperare l'elenco dei nomi dei campi che fanno legalmente parte del messaggio
     * 
     * @return Iterator contiene i nomi dei campi
     * 
     * @throws AppCrash in caso di errore
     */
    public Iterator iterator() throws AppCrash;

    /**
     * Questo metodo serve per eseguire il log di tutti i campi del messaggio sul log di debug con il livello
     * specificato
     * 
     * @param level livello di debug da usare: 0-3
     * 
     * @throws AppCrash se non riesce a recuperare i campi
     */
    public void logDebug(int level) throws AppCrash;

}
