/*
  RowToTemplateAdapter.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione:

  Autore: Simone Z.

  Note:

  Modifiche:

 */

package net.project.servlet.gui;

import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.mess.MsgReader_itf;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;

/**
 * Questa classe fornisce un adapter per gli oggetti Row_itf alle interfacce TemplateHashModel e TemplateListModel.
 */
public class RowToTemplateAdapter implements TemplateHashModel, TemplateSequenceModel {

    private Row_itf                     _theRow;
    private int                         _numOfCol;
    private int                         _iterationPointer;
    private MsgReader_itf               _reader         = null;

    // La stringa _currentField è inizializzata a "", anziché a null,
    // per permettere sempre di invocare su di essa il metodo equals
    private String                      _currentField   = "";

    private String                      _formattingInfo = null;
    private String                      _configName     = null;

    private String                      _freemarkerOld;

    private static DefaultObjectWrapper _ObjectWrapper  = new DefaultObjectWrapper();

    // Valori costanti del file di configurazione
    public static final String          AMOUNT          = "IMPORTO";
    public static final String          CURRENCY        = "VALUTA";
    public static final String          PAGE            = "Page";

    // Valori costanti indicanti le valute prima della formattazione
    public static final String          LIRE            = "380";
    public static final String          EURO            = "978";

    // Valori costanti indicanti le valute dopo la formattazione
    public static final String          LIT             = "LIT";
    public static final String          EUR             = "EUR";

    /**
     * Costruisce un adapter con l'oggetto Row_itf passato. Di questo oggetto viene memorizzato un reference. L'adapter
     * NON e' thread safe.
     * 
     * @param net.project.dataset.Row_itf row l'oggetto Row_itf da adattare
     * @param java.lang.String formattingInfo stringa formata da coppie nome-valore delle colonne da formattare
     * @param java.lang.String freemarkerOld compatibilita' con vecchio freemarker: se true tutti gli oggetti sono stati
     *            trasformati in stringhe
     * @param java.lang.String configName nome della configurazione
     * @exception net.project.errors.AppCrash in caso di parametri di ingresso non inizializzati o di errore
     *                nell'elaborazione dei dati
     */
    public RowToTemplateAdapter(Row_itf row, String formattingInfo, String freemarkerOld, String configName)
            throws AppCrash {

        // Controllo formale dei parametri in ingresso
        ErrDetector.GetInstance().param(row);
        ErrDetector.GetInstance().preCond(formattingInfo != null);
        ErrDetector.GetInstance().preCond(configName != null);

        _freemarkerOld = freemarkerOld;
        _iterationPointer = 0;
        _formattingInfo = formattingInfo;
        _configName = configName;
        try {
            if (formattingInfo.equals("")) {
                _theRow = row;
            } else {
                _theRow = new FormattedRow(formattingInfo, row);
            }
            _numOfCol = _theRow.getColumnNo();

        } catch (AppCrash ac) {
            ac.logContext("RowToTemplateAdapter", toString());
            throw ac;
        }

    }

    /**
     * Costruisce un adapter con l'oggetto Row_itf passato. Di questo oggetto viene memorizzato un reference. L'adapter
     * NON e' thread safe
     * 
     * @param net.project.dataset.Row_itf row l'oggetto Row_itf da adattare
     * @param java.lang.String formattingInfo stringa formata da coppie nome-valore delle colonne da formattare
     * @param java.lang.String freemarkerOld compatibilita' con vecchio freemarker: se true tutti gli oggetti sono stati
     *            trasformati in stringhe
     * @exception net.project.errors.AppCrash
     */
    public RowToTemplateAdapter(Row_itf row, String formattingInfo, String freemarkerOld) throws AppCrash {

        this(row, formattingInfo, freemarkerOld, "");
    }

    /**
     * Restituisce un <tt>TemplateModel</tt> recuperando un elemento della Row_itf.
     *
     * @param java.lang.String key nome dell'elemento da recuperare.
     * @return freemarker.template.TemplateModel il <tt>TemplateModel</tt> in wrap all'elemento o null se non esiste
     */
    @Override
    public TemplateModel get(String key) throws TemplateModelException {

        try {
            Object rowElement = null;
            try {
                int colNo = Integer.parseInt(key) + 1;
                _iterationPointer = colNo;
                rowElement = _theRow.getField(colNo);

            } catch (NumberFormatException nfe) {
                rowElement = _theRow.getField(key);
                _currentField = key;
            }

            if (rowElement == null) return new SimpleScalar("");

            if (rowElement instanceof net.project.dataset.DataSet_itf) {
                return new DataSetToSimpleListAdapter((DataSet_itf) rowElement, "", _freemarkerOld);
            }
            if (_freemarkerOld.equalsIgnoreCase("true")) {
                return new SimpleScalar(rowElement.toString());
            } else {
                return _ObjectWrapper.wrap(rowElement);
            }
        } catch (AppCrash ac) {
            throw new TemplateModelException("Class: RowToTemplateAdapter - " + toString() + "; key = " + key);
        }

    }

    /**
     * @return true if this object is empty.
     */
    @Override
    public boolean isEmpty() throws TemplateModelException {

        return (_theRow == null);
    }

    /**
     * Resets the cursor to the beginning of the list.
     */
    public void rewind() throws TemplateModelException {

        _iterationPointer = 0;
    }

    /**
     * @return true if the cursor is at the beginning of the list.
     */
    public boolean isRewound() throws TemplateModelException {

        return (_iterationPointer == 0);
    }

    /**
     * @return true if there is a next element.
     */
    public boolean hasNext() throws TemplateModelException {

        return (_iterationPointer < _numOfCol);
    }

    /**
     * @return the next element in the list.
     */
    public TemplateModel next() throws TemplateModelException {

        _iterationPointer++;
        try {
            Object rowElement = _theRow.getField(_iterationPointer);
            if (rowElement == null) return null;

            if (rowElement instanceof net.project.dataset.DataSet_itf) {
                return new DataSetToSimpleListAdapter((DataSet_itf) rowElement, "", _freemarkerOld);
            }
            if (_freemarkerOld.equalsIgnoreCase("true")) {
                return new SimpleScalar(rowElement.toString());
            } else {
                return _ObjectWrapper.wrap(rowElement);
            }

        } catch (AppCrash ac) {
            throw new TemplateModelException("Class: RowToTemplateAdapter - " + toString());
        }

    }

    /**
     * @return the size of the list as a TemplateModel
     */
    @Override
    public int size() throws TemplateModelException {

        // Viene ritornato il numero reale piu' uno perche' i dati nelle nostre Row_itf sono fra 1 ed N mentre per
        // Freemarker fra 0 ed N-1
        // Ritornando il numero reale Freemarker impedisce l'accesso all'elemento N in quanto per lui e' fuori dalla
        // collection
        return _numOfCol + 1;
    }

    /**
     * @return the specified index in the list
     */
    @Override
    public TemplateModel get(int i) throws TemplateModelException {

        _iterationPointer = i;
        try {
            Object rowElement = _theRow.getField(i);
            if (rowElement == null) return new SimpleScalar("");

            if (rowElement instanceof net.project.dataset.DataSet_itf) {
                return new DataSetToSimpleListAdapter((DataSet_itf) rowElement, _freemarkerOld, "");
            }
            if (_freemarkerOld.equalsIgnoreCase("true")) {
                return new SimpleScalar(rowElement.toString());
            } else {
                return _ObjectWrapper.wrap(rowElement);
            }

        } catch (AppCrash ac) {
            throw new TemplateModelException("Class: RowToTemplateAdapter - " + toString() + "; i = " + i);
        }

    }

    public void setRow(Row_itf row) {

        if (_theRow instanceof FormattedRow) {
            ((FormattedRow) _theRow).setRow(row);
        } else {
            _theRow = row;
        }

    }

    /**
     * @return java.lang.String stringa che descrive lo stato del sistema
     */
    @Override
    public String toString() {

        StringBuffer temp = new StringBuffer();
        temp.append("_numOfCol = ");
        temp.append(_numOfCol);
        temp.append("; _iterationPointer = ");
        temp.append(_iterationPointer);
        temp.append("_currentField = ");
        temp.append(_currentField);
        temp.append("_formattingInfo = ");
        temp.append(_formattingInfo);
        temp.append("_configName = ");
        temp.append(_configName);
        return temp.toString();

    }

}
