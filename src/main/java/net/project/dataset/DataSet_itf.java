/*
  DataSet_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione:

  Autore: Pietro G. e Luca M.

  Note:

  Modifiche:

 */

package net.project.dataset;

import java.util.Enumeration;
import java.util.Map;

import net.project.errors.AppCrash;

/**
 * Interfaccia che definisce le funzionalita' di un DataSet * @author
 */
public interface DataSet_itf extends Enumeration {

    /**
     * Rende disponibile il DataSet.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     * @roseuid 3A59E6950388
     */
    public void open() throws AppCrash;

    /**
     * Posiziona il 'cursore' del DataSet in corrispondenza del primo elemento.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     * @roseuid 3A59E6BF0202
     */
    public void rewind() throws AppCrash;

    /**
     * Rilascia il DataSet.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     * @roseuid 3A59E6D20213
     */
    public void close() throws AppCrash;

    /**
     * Imposta i parametri necessari per l'apertura del DataSet.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     * @roseuid 3A59E718019B
     */
    public void setParam(Map parametri) throws AppCrash;

    /**
     * Conta il numero di colonne del DataSet
     * 
     * @return int numero di colonne del DataSet
     * @exception net.project.errors.AppCrash
     */
    public int getColumnNo() throws AppCrash;

    /**
     * Ritorna i nomi delle colonne del DataSet
     * 
     * @return String[] nomi delle colonne del DataSet
     * @exception net.project.errors.AppCrash
     */
    public String[] getColumnNames() throws AppCrash;

}
