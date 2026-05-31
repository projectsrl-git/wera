/*
  SizeableDataSet_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 28/02/2003

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.dataset;

import net.project.errors.AppCrash;

/**
 * Interfaccia che definisce le funzionalità di un DataSet sizeable
 */
public interface SizeableDataSet_itf extends DataSet_itf {

    /**
     * Ritorna il numero di righe presenti nel dataset corrente.
     * 
     * @return int
     * @exception net.project.errors.AppCrash
     */
    public int getNumberOfRows() throws AppCrash;

}
