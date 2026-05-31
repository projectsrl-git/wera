/*
  WindowDataSetDecorator.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 03/03/2003

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.dataset;

import java.util.Map;
import java.util.NoSuchElementException;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;

/**
 * Rappresenta un dataset con possibilità di suddividere i dati in pagine.
 */
public class WindowDataSetDecorator implements WindowDataSet_itf {

    private SizeableDataSet_itf _sizeableDataset          = null;
    private int                 _windowSize               = 0;

    private int                 _currentWindow            = -1;
    private int                 _numberOfWindows          = -1;

    private int                 _rowsCounter              = 0;
    private int                 _latestRowOfCurrentWindow = 0;

    private boolean             _winDsOpened              = false;
    private boolean             _dsOpened                 = false;
    private boolean             _nextExecuted             = false;

    /**
     * Costruttore.
     */
    public WindowDataSetDecorator(SizeableDataSet_itf sizeableDataset, int size) throws AppCrash {

        // controllo formale dei parametri in ingresso
        ErrDetector.GetInstance().invariant(sizeableDataset != null);
        ErrDetector.GetInstance().param(size > 0);

        _sizeableDataset = sizeableDataset;
        _windowSize = size;
    }

    /**
     * Controlla se il dataset contiene altri elementi.
     * 
     * @return boolean true se il dataset contiene altri elementi, false altrimenti
     * @exception RuntimeException: in caso di Exception
     */
    @Override
    public boolean hasMoreElements() {

        if (_rowsCounter < _latestRowOfCurrentWindow) {
            return _sizeableDataset.hasMoreElements();
        }
        return false;
    }

    /**
     * Restituisce l'elemento del dataset successivo a quello corrente.
     * 
     * @return java.lang.Object il successivo elemento del DataSet
     * @exception NoSuchElementException se non ci sono più elementi nel DataSet, o in caso di Exception
     */
    @Override
    public Object nextElement() throws NoSuchElementException {

        _nextExecuted = true;
        _rowsCounter++;
        if (_rowsCounter <= _latestRowOfCurrentWindow) {
            return _sizeableDataset.nextElement();
        }

        throw new NoSuchElementException();

    }

    /**
     * Rende disponibile il DataSet.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void open() throws AppCrash {

        ErrDetector.GetInstance().preCond(_winDsOpened,
                "Class: WindowDataSetDecorator - open() non possibile prima di openWindowDataSet()");
        if (_dsOpened) {
            return;
        }
        _nextExecuted = true; // per eseguire la rewind() del dataset
        _dsOpened = true;
        rewind();
    }

    /**
     * Posiziona il 'cursore' del DataSet in corrispondenza del primo elemento della finestra corrente.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void rewind() throws AppCrash {

        _rowsCounter = 0;
        if (!_nextExecuted) {
            return;
        }
        _sizeableDataset.rewind();
        _nextExecuted = false;
        setWindow(_currentWindow);
    }

    /**
     * Rilascia il DataSet.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void close() throws AppCrash {

        _sizeableDataset.close();
        _dsOpened = false;
    }

    /**
     * Imposta i parametri necessari per l'apertura del DataSet.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void setParam(Map parametri) throws AppCrash {

        _sizeableDataset.setParam(parametri);
    }

    /**
     * Conta il numero di colonne del DataSet
     * 
     * @return int numero di colonne del DataSet
     * @exception net.project.errors.AppCrash
     */
    @Override
    public int getColumnNo() throws AppCrash {

        return _sizeableDataset.getColumnNo();
    }

    /**
     * Ritorna i nomi delle colonne del DataSet
     * 
     * @return String[] nomi delle colonne del DataSet
     * @exception net.project.errors.AppCrash
     */
    @Override
    public String[] getColumnNames() throws AppCrash {

        return _sizeableDataset.getColumnNames();
    }

    /**
     * Apre il WindowDataSet.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void openWindowDataSet() throws AppCrash {

        if (!_winDsOpened) {
            _sizeableDataset.open();
            _nextExecuted = false;
            _winDsOpened = true;
            _dsOpened = true;
            setWindow(1);
        }
    }

    /**
     * Chiude il WindowDataSet.
     */
    @Override
    public void closeWindowDataSet() {

        _winDsOpened = false;
    }

    /**
     * Ritorna il numero di righe presenti nel dataset corrente.
     * 
     * @return int
     * @exception net.project.errors.AppCrash
     */
    @Override
    public int getNumberOfRows() throws AppCrash {

        return _sizeableDataset.getNumberOfRows();
    }

    /**
     * Sposta la posizione della finestra di dati nel dataset.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void setWindow(int pageNumber) throws AppCrash {

        if (getNumberOfRows() == 0) {
            return;
        }
        ErrDetector.GetInstance().param(pageNumber > 0 && pageNumber <= getNumberOfWindows());
        if (_nextExecuted) {
            _sizeableDataset.rewind();
            _nextExecuted = false;
            _rowsCounter = 0;
        }
        _latestRowOfCurrentWindow = setLatestRow(pageNumber);
        for (int i = 1; i <= (_windowSize * (pageNumber - 1)); i++) {
            nextElement();
        }
        _currentWindow = pageNumber;
    }

    /**
     * Calcola la posizione dell'ultima riga della pagina richiesta.
     * 
     * @param int pageNumber Il numero della pagina richiesta (prima pagina = 1)
     * @return int La posizione dell'ultima riga della pagina richiesta.
     * @exception net.project.errors.AppCrash
     */
    private int setLatestRow(int pageNumber) throws AppCrash {

        int latestRow = _windowSize * pageNumber;
        if (latestRow > getNumberOfRows()) {
            return getNumberOfRows();
        }
        return latestRow;
    }

    /**
     * Ritorna il numero della pagina corrente.
     * 
     * @return int
     */
    @Override
    public int getCurrentWindow() {

        return _currentWindow;
    }

    /**
     * Ritorna il numero della prossima pagina. Qualora si sia posizionati sull'ultima pagina, oppure il dataset sia
     * vuoto, allora restituisce -1.
     * 
     * @return int
     * @exception net.project.errors.AppCrash
     */
    @Override
    public int getNextWindow() throws AppCrash {

        boolean currentWindowIsLastWindow = ((_currentWindow + 1) > getNumberOfWindows());
        boolean datasetIsEmpty = (_currentWindow == -1);

        if (currentWindowIsLastWindow || datasetIsEmpty) {
            return -1;
        }
        return _currentWindow + 1;

    }

    /**
     * Ritorna il numero della pagina precedente. Qualora si sia posizionati sulla prima pagina, oppure il dataset sia
     * vuoto, allora restituisce -1.
     * 
     * @return int
     */
    @Override
    public int getPrevWindow() {

        boolean currentWindowIsFirstWindow = (_currentWindow == 1);
        boolean datasetIsEmpty = (_currentWindow == -1);

        if (currentWindowIsFirstWindow || datasetIsEmpty) {
            return -1;
        }
        return _currentWindow - 1;

    }

    /**
     * Ritorna il numero delle righe presenti nella finestra corrente.
     * 
     * @return int
     * @exception net.project.errors.AppCrash
     */
    @Override
    public int getWindowSize() throws AppCrash {

        int resto = getNumberOfRows() % _windowSize;
        if (_currentWindow == getNumberOfWindows() && resto != 0) {
            return resto;
        }
        return _windowSize;
    }

    /**
     * Testa la presenza di pagine successive nel dataset
     * 
     * @return boolean true se ci sono pagine successive alla corrente, false se la finestra corrente è l'ultima
     * @exception net.project.errors.AppCrash
     */
    @Override
    public boolean existsNextWindow() throws AppCrash {

        return (getNextWindow() == -1 ? false : true);
    }

    /**
     * Testa la presenza di pagine precedenti nel dataset
     * 
     * @return boolean true se ci sono pagine precedenti alla corrente, false se la finestra corrente è la prima
     */
    @Override
    public boolean existsPrevWindow() {

        return (getPrevWindow() == -1 ? false : true);
    }

    /**
     * Ritorna il numero di pagine in cui è suddiviso il dataset.
     * 
     * @return int
     * @exception net.project.errors.AppCrash
     */
    @Override
    public int getNumberOfWindows() throws AppCrash {

        if (_numberOfWindows == -1) {
            _numberOfWindows = countWindows();
        }
        return _numberOfWindows;
    }

    /**
     * Calcola il numero di pagine in cui suddividere il dataset.
     * 
     * @return int
     * @exception net.project.errors.AppCrash
     */
    private int countWindows() throws AppCrash {

        int quoziente = getNumberOfRows() / _windowSize;
        int resto = getNumberOfRows() % _windowSize;
        if (resto != 0) {
            ++quoziente;
        }
        return quoziente;
    }
}
