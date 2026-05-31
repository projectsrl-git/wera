/*
  CachedDBDataSet.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 14/11/2002

  Autore: Luca M.

  Note:

  Modifiche:

 */

package net.project.dataset;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;

/**
 * Classe che contiene un DataSet e ne memorizza le informazioni su un vettore privato. Quest'ultimo è poi utilizzato
 * per fornire le informazioni ai propri client.
 */
public class CachedDBDataSet implements SizeableDataSet_itf {

    private boolean     _firstOpenExecuted = false;

    private DataSet_itf _dataSet           = null;
    private Vector      _dataVector        = null;
    private int         _index             = 0;
    private int         _columnNo          = 0;
    private String[]    _columnNames       = null;

    /**
     * Costruttore. Costruisce un DBDataSet con i parametri in ingresso, ed un Vector dove memorizzare i dati del
     * DBDataSet.
     */
    public CachedDBDataSet(String configName, String datasetName) throws AppCrash {

        _dataSet = new DBDataSet(configName, datasetName);
        ErrDetector.GetInstance().postCond(_dataSet != null);
        _dataVector = new Vector();

    }

    /**
     * Accede al db, carica i dati su _dataVector e rilascia la connessione col db.
     */
    @Override
    public void open() throws AppCrash {

        if (_firstOpenExecuted) {
            rewind();
            return;
        }

        try {
            _index = 0;
            _dataSet.open();
            _columnNo = _dataSet.getColumnNo();
            _columnNames = _dataSet.getColumnNames();
            writeHead(_columnNames);
            while (_dataSet.hasMoreElements()) {
                Object nextElement = _dataSet.nextElement();
                if (nextElement instanceof Row_itf) {
                    store((Row_itf) nextElement, _columnNames);
                } else {
                    AppCrash ac = new AppCrash();
                    ac.logContext("CachedDBDataSet",
                            "Il dataset deve contenere righe di classe net.project.dataset.Row_itf"
                                    + " - Le righe sono invece di classe: " + nextElement.getClass().getName());
                    throw ac;
                }
            }
            writeTail(_columnNames);
            _firstOpenExecuted = true;

        } finally {
            _dataSet.close();
            _dataSet = null;
        }

    }

    // Metodo ridefinibile se si vogliono scrivere una o più righe di testa nel dataset.
    // Di default non fa nulla.
    protected void writeHead(String[] columnNames) throws AppCrash {

    }

    // Metodo protected che carica i dati di _dataSet.
    protected void store(Row_itf row, String[] columnNames) throws AppCrash {

        add(row, columnNames);
    }

    protected void add(Row_itf row, String[] columnNames) throws AppCrash {

        _dataVector.add(adaptRowToHashtableRow(row));
    }

    private HashtableRow adaptRowToHashtableRow(Row_itf row) throws AppCrash {

        if (row instanceof HashtableRow) {
            return (HashtableRow) row;
        } else {
            return new HashtableRow(row, _dataSet.getColumnNames());
        }

    }

    // Metodo ridefinibile se si vogliono scrivere una o più righe di coda nel dataset.
    // Di default non fa nulla.
    protected void writeTail(String[] columnNames) throws AppCrash {

    }

    /**
     * Posiziona l'indice utilizzato per _dataVector alla posizione 0.
     */
    @Override
    public void rewind() throws AppCrash {

        _index = 0;
    }

    /**
     * Non fa nulla, dato che la connessione al db è già rilasciata da open.
     */
    @Override
    public void close() throws AppCrash {

        _index = 0;
    }

    /**
     * Inoltra questo stesso metodo a _dataSet.
     */
    @Override
    public void setParam(Map parametri) throws AppCrash {

        _dataSet.setParam(parametri);
    }

    /**
     * Restituisce il numero di colonne del presente oggetto.
     */
    @Override
    public int getColumnNo() throws AppCrash {

        return _columnNo;
    }

    /**
     * Restituisce i nomi delle colonne del presente oggetto.
     */
    @Override
    public String[] getColumnNames() throws AppCrash {

        return _columnNames;
    }

    /**
     * Dice se ci sono altri elementi nell'oggetto.
     * 
     * @return boolean true se ci sono altri elementi, false altrimenti.
     */
    @Override
    public boolean hasMoreElements() {

        return _index < _dataVector.size();
    }

    /**
     * Restituisce il prossimo elemento dell'oggetto
     * 
     * @return java.lang.Object il prossimo elemento (classe: net.project.dataset.Row_itf)
     * @exception java.util.NoSuchElementException se non ci sono altri elementi
     */
    @Override
    public Object nextElement() {

        try {
            Object nextElement = _dataVector.elementAt(_index);
            _index++;
            return nextElement;

        } catch (ArrayIndexOutOfBoundsException aioobe) {
            new AppCrash(aioobe);
            throw new NoSuchElementException();
        }

    }

    protected DataSet_itf getDataSet() {

        return _dataSet;
    }

    protected Vector getDataVector() {

        return _dataVector;
    }

    /**
     * Ritorna il numero di righe presenti nel dataset corrente.
     * 
     * @return int
     * @exception net.project.errors.AppCrash
     */
    @Override
    public int getNumberOfRows() throws AppCrash {

        // controllo che il dataset sia già stato aperto
        ErrDetector.GetInstance().preCond(_firstOpenExecuted,
                "Class: CachedDBDataSet - metodo getNumberOfRows() non possibile prima di open()");
        return _dataVector.size();
    }

}
