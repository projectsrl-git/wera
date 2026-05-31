/*
  DataSetToSimpleListAdapter.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione:

  Autore: Simone Z.

  Note:

  Modifiche:

 */

package net.project.servlet.gui;

import java.util.NoSuchElementException;

import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;

/**
 * Questa classe fornisce un adapter tra un oggetto di interfaccia DataSet_itf e TemplateListModel
 */
public class DataSetToSimpleListAdapter implements TemplateCollectionModel, TemplateModelIterator {

    private DataSet_itf          _theDataSet;
    private RowToTemplateAdapter _theRowAdapter;
    private String               _configName     = null;
    private String               _formattingInfo = null;
    private String               _freemarkerOld  = null;

    /**
     * Costruttore.
     * 
     * @param net.project.dataset.DataSet_itf ds il DataSet da adattare
     * @param java.lang.String formattingInfo stringa formata da coppie nome-valore delle colonne da formattare
     * @param java.lang.String freemarkerOld compatibilita' con vecchio freemarker: se true tutti gli oggetti sono stati
     *            trasformati in stringhe
     * @param java.lang.String configName il nome della configurazione
     * @exception net.project.errors.AppCrash in caso di parametri in ingresso non inizializzati
     */
    public DataSetToSimpleListAdapter(DataSet_itf ds, String formattingInfo, String freemarkerOld, String configName)
            throws AppCrash {

        ErrDetector.GetInstance().param(ds);
        ErrDetector.GetInstance().preCond(formattingInfo != null);
        ErrDetector.GetInstance().preCond(configName != null);
        _theDataSet = ds;
        _configName = configName;
        _formattingInfo = formattingInfo;
        _freemarkerOld = freemarkerOld;
    }

    /**
     * Costruttore.
     * 
     * @param net.project.dataset.DataSet_itf ds il DataSet da adattare
     * @param java.lang.String formattingInfo stringa formata da coppie nome-valore delle colonne da formattare
     * @param java.lang.String freemarkerOld compatibilita' con vecchio freemarker: se true tutti gli oggetti sono stati
     *            trasformati in stringhe
     * @exception net.project.errors.AppCrash in caso di parametri in ingresso non inizializzati
     */
    public DataSetToSimpleListAdapter(DataSet_itf ds, String formattingInfo, String freemarkerOld) throws AppCrash {

        this(ds, formattingInfo, freemarkerOld, "");

    }

    /**
     * Resets the cursor to the beginning of the list.
     */
    public void rewind() throws TemplateModelException {

        try {
            _theDataSet.rewind();
            _theRowAdapter = null;
        } catch (AppCrash ac) {
            throw new TemplateModelException("AppCrash during rewind");
        }
    }

    /**
     * @return true if there is a next element.
     */
    @Override
    public boolean hasNext() throws TemplateModelException {

        try {
            return _theDataSet.hasMoreElements();

        } catch (RuntimeException re) {
            throw new TemplateModelException("Class: DataSetToSimpleListAdapter - " + toString());

        }
    }

    /**
     * @return the next element in the list.
     */
    @Override
    public TemplateModel next() throws TemplateModelException {

        try {

            if (_theDataSet.hasMoreElements() == false) return null;

            Row_itf nextRow = (Row_itf) _theDataSet.nextElement();
            if (_theRowAdapter == null) {
                _theRowAdapter = new RowToTemplateAdapter(nextRow, _formattingInfo, _freemarkerOld, _configName);
            } else {
                _theRowAdapter.setRow(nextRow);
            }
            return _theRowAdapter;

        } catch (NoSuchElementException noelement) {
            throw new TemplateModelException("dataset next out of bound");
        } catch (AppCrash ac) {
            throw new TemplateModelException("Class: DataSetToSimpleListAdapter - " + toString());
        }
    }

    /**
     * Questo metodo viene usato da Freemarker 2.0 per recuperare l'iterator da usare per scandire la lista. Viene
     * restituito un reference all'oggetto stesso dato che questo e' l'adapter.
     * 
     * @return TemplateModelIterator
     */
    @Override
    public TemplateModelIterator iterator() throws TemplateModelException {

        rewind();
        return this;
    }

    /**
     * @return true if this object is empty.
     */
    public boolean isEmpty() throws TemplateModelException {

        return (_theDataSet == null);
    }

    @Override
    public String toString() {

        return "_configName = " + _configName;
    }

}
