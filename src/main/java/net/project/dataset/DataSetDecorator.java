/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.dataset;

import java.util.Map;

import net.project.errors.AppCrash;

/**
 * Questa classe e' un decorator per oggetti di tipo DataSet_itf. Tutte le chiamate ai suoi metodi vengono inoltrate
 * all'oggetto che decora. Serve come base per costruire decorator personalizzati
 */
public class DataSetDecorator implements DataSet_itf {

    private DataSet_itf _theDataset = null;

    public DataSetDecorator(DataSet_itf ds) {

        _theDataset = ds;
    }

    /**
     * Questo metodo inoltra la chiamata al corrispondente metodo della classe decorata
     *
     * @throws AppCrash
     *
     * @see net.project.dataset.DataSet_itf#open()
     */
    @Override
    public void open() throws AppCrash {

        _theDataset.open();

    }

    /**
     * Questo metodo inoltra la chiamata al corrispondente metodo della classe decorata
     *
     * @throws AppCrash
     *
     * @see net.project.dataset.DataSet_itf#rewind()
     */
    @Override
    public void rewind() throws AppCrash {

        _theDataset.rewind();

    }

    /**
     * Questo metodo inoltra la chiamata al corrispondente metodo della classe decorata
     *
     * @throws AppCrash
     *
     * @see net.project.dataset.DataSet_itf#close()
     */
    @Override
    public void close() throws AppCrash {

        _theDataset.close();

    }

    /**
     * Questo metodo inoltra la chiamata al corrispondente metodo della classe decorata
     *
     * @param parametri
     * @throws AppCrash
     *
     * @see net.project.dataset.DataSet_itf#setParam(java.util.Map)
     */
    @Override
    public void setParam(Map parametri) throws AppCrash {

        _theDataset.setParam(parametri);

    }

    /**
     * Questo metodo inoltra la chiamata al corrispondente metodo della classe decorata
     *
     * @return
     * @throws AppCrash
     *
     * @see net.project.dataset.DataSet_itf#getColumnNo()
     */
    @Override
    public int getColumnNo() throws AppCrash {

        return _theDataset.getColumnNo();
    }

    /**
     * Questo metodo inoltra la chiamata al corrispondente metodo della classe decorata
     *
     * @return
     * @throws AppCrash
     *
     * @see net.project.dataset.DataSet_itf#getColumnNames()
     */
    @Override
    public String[] getColumnNames() throws AppCrash {

        return _theDataset.getColumnNames();
    }

    /**
     * Questo metodo inoltra la chiamata al corrispondente metodo della classe decorata
     *
     * @return
     *
     * @see java.util.Enumeration#hasMoreElements()
     */
    @Override
    public boolean hasMoreElements() {

        return _theDataset.hasMoreElements();
    }

    /**
     * Questo metodo inoltra la chiamata al corrispondente metodo della classe decorata
     *
     * @return
     *
     * @see java.util.Enumeration#nextElement()
     */
    @Override
    public Object nextElement() {

        return _theDataset.nextElement();
    }

    /**
     * Questo metodo ritorna l'oggetto che viene decorato
     *
     * @return DataSet_itf l'oggetto che viene decorato
     */
    protected DataSet_itf getOriginalDataset() {

        return _theDataset;
    }
}
