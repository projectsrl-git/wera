/*
  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore: Simone Z.

  Note:


 */

package net.project.mess;

import net.project.errors.AppCrash;

/**
 * Quetsta classe rappresenta un'estensione del concetto di MsgWriter: essa aggiunge i metodi di copia
 * 
 * @author Simone
 */
public interface MsgWriterEx_itf extends MsgWriter_itf, MsgReaderEx_itf {

    /**
     * Questo metodo cerca di estrarre da un MsgReader tutti i campi che sono indicati come validi per il MsgWriterEx
     * del quale fa parte.
     * 
     * @param msg MsgReader_itf il MsgReader dal quale prelevare i campi
     * 
     * @throws AppCrash
     */
    public void copy(MsgReader_itf msg) throws AppCrash;

    /**
     * Questo metodo cerca di estrarre da un MsgReader tutti i campi che sono indicati nell'array passato e di copiarli
     * all'interno dell'oggetto del quale fa parte.
     * 
     * @param msg MsgReader_itf il MsgReader dal quale prelevare i campi
     * @param mapping matrice che contiene il mapping dei campi da copiare nel formato "destinazione" - "sorgente"
     * 
     * @throws AppCrash
     */
    public void copy(MsgReader_itf msg, String[][] mapping) throws AppCrash;

    /**
     * Questo metodo permette di settare i campi di un MsgWriter a partire dai campi di un array. Per eseguire il
     * mapping prende in ingresso una matrice che contiene per ogni riga una coppia "destinazione" - "valore"
     * 
     * @param mapping matrice "destinazione" - "valore" .
     * 
     * @throws AppCrash DOCUMENT ME!
     */
    public void copy(String[][] mapping) throws AppCrash;

}