/*
  Row_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione:

  Autore: Pietro G. e Luca M.

  Note:

  Modifiche:

 */

package net.project.dataset;

import net.project.errors.AppCrash;
import net.project.errors.DBCrash;

/**
 * Interfaccia che rappresenta, in forma di riga, un elemento di un oggetto DataSet.
 */
public interface Row_itf {

    /**
     * Restituisce il valore di un campo della riga.
     * 
     * @param java.lang String fieldName il nome del campo
     * @return java.lang.Object il valore del campo
     * @exception net.project.errors.AppCrash in caso di errore nel recupero del valore del campo
     */
    public Object getField(String fieldName) throws AppCrash;

    /**
     * Restituisce il valore di un campo della riga.
     * 
     * @param int fieldNo il numero del campo
     * @return java.lang.Object il valore del campo
     * @exception net.project.errors.ParamCrash se il parametro in ingresso è null
     * @exception net.project.errors.AppCrash in caso di errore nel recupero del valore del campo
     */
    public Object getField(int fieldNo) throws AppCrash;

    /**
     * Conta il numero di colonne del DataSet
     * 
     * @return int numero di colonne del DataSet
     * @exception net.project.errors.AppCrash
     */
    public int getColumnNo() throws DBCrash;

}
