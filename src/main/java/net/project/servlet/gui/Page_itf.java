/*
  Page_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione:

  Autore: Pietro G. e Luca M.

  Note:

  Modifiche:

 */

package net.project.servlet.gui;

import java.io.PrintWriter;
import java.util.Map;

import net.project.dataset.DataSet_itf;
import net.project.errors.AppCrash;

/**
 * Interfaccia che definisce le funzionalità di una pagina.
 */
public interface Page_itf {

    /**
     * Restituisce il nome della pagina.
     * 
     * @return java.lang.String
     * @exception
     * @roseuid 3A6D432201F1
     */
    public String getName();

    /**
     * Valorizza i parametri di uno dei DataSet che serviranno a costruire la pagina.
     * 
     * @param param java.util.Map Map dei parametri del DataSet
     * @param dsNum int identificativo del DataSet
     * @return void
     * @exception net.project.errors.AppCrash
     * @roseuid 3A6D43890218
     */
    public void setDataSourceParam(Map param, int dsNum) throws AppCrash;

    /**
     * Se gli oggetti contenuti come valori nella hashtable sono istanze di DataSet_itf, allora richiama su ognuno di
     * essi il metodo setPageRootData(DataSet_itf, String) assumendo che le rispettive chiavi siano stringhe contenenti
     * il nome del nodo della _modelRoot; altrimenti carica nella _modelRoot tanti oggetti SimpleScalar quanti sono gli
     * elementi della hashtable stessa
     * 
     * @param param java.util.Hashtable paramHash hashtable contenente i parametri passati dal client
     * @return void
     * @exception net.project.errors.AppCrash
     */
    public void setPageRootData(Map paramHash) throws AppCrash;

    /**
     * Riceve un dataset e carica in un nodo della _modelRoot un oggetto DataSetToSimpleListAdapter (ottenuto dal
     * dataset stesso)
     * 
     * @param ds net.project.dataset.DataSet_itf il dataset in ingresso
     * @param name java.lang.String il nome del nodo della _modelRoot cui assegnare l'oggetto DataSetToSimpleListAdapter
     * @return void
     * @exception net.project.errors.ParamCrash
     */
    public void setPageRootData(DataSet_itf ds, String name) throws AppCrash;

    /**
     * Mostra la pagina creata.
     * 
     * @param out java.io.OutputStream l'oggetto sul quale mostrare la pagina
     * @return void
     * @exception
     * @roseuid 3A6D43F6039B
     */
    public void display(PrintWriter pw) throws AppCrash;

    /**
     * Questo metodo ritorna il nome della configurazione usata per creare la pagina
     *
     * @return String nome della configurazione
     */
    public String getConfigName();

}
