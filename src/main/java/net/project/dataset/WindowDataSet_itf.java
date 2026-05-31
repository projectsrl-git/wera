/*
  WindowDataSet_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 28/02/2003

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.dataset;

import net.project.errors.AppCrash;

/**
 * Interfaccia che definisce le funzionalità di un DataSet paginabile
 */
public interface WindowDataSet_itf extends SizeableDataSet_itf {

    /**
     * Sposta la posizione della finestra di dati nel dataset.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     */
    public void setWindow(int pageNumber) throws AppCrash;

    /**
     * Ritorna il numero della pagina corrente.
     * 
     * @return int
     */
    public int getCurrentWindow();

    /**
     * Ritorna il numero della prossima pagina oppure -1 se si è posizionati sull'ultima pagina.
     * 
     * @return int
     * @exception net.project.errors.AppCrash
     */
    public int getNextWindow() throws AppCrash;

    /**
     * Ritorna il numero della pagina precedente oppure -1 se si è posizionati sulla prima pagina.
     * 
     * @return int
     */
    public int getPrevWindow();

    /**
     * Ritorna il numero delle righe presenti nella finestra corrente.
     * 
     * @return int
     * @exception net.project.errors.AppCrash
     */
    public int getWindowSize() throws AppCrash;

    /**
     * Testa la presenza di pagine successive nel dataset
     * 
     * @return boolean true se ci sono pagine successive alla corrente, false se la finestra corrente è l'ultima
     * @exception net.project.errors.AppCrash
     */
    public boolean existsNextWindow() throws AppCrash;

    /**
     * Testa la presenza di pagine precedenti nel dataset
     * 
     * @return boolean true se ci sono pagine precedenti alla corrente, false se la finestra corrente è la prima
     */
    public boolean existsPrevWindow();

    /**
     * Ritorna il numero di pagine in cui è suddiviso il dataset.
     * 
     * @return int
     * @exception net.project.errors.AppCrash
     */
    public int getNumberOfWindows() throws AppCrash;

    /**
     * Apre il WindowDataSet.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     */
    public void openWindowDataSet() throws AppCrash;

    /**
     * Chiude il WindowDataSet.
     */
    public void closeWindowDataSet();

}
