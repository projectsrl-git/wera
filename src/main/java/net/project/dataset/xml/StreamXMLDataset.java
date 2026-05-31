/*
 * Created on 15-set-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

package net.project.dataset.xml;

import java.io.InputStream;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import net.project.dataset.DataSet_itf;
import net.project.dataset.HashtableRow;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;

import org.xml.sax.InputSource;

/**
 * Implementa l'astrazione dataset_itf per sorgenti di dati in XML
 * 
 * @author zorzetti
 */
public class StreamXMLDataset implements DataSet_itf {

    private XMLRecordParser _parser;

    private Map             _lastRowReturned;
    private Map             _currentRowContent;
    private Map             _nextRowContent;

    public StreamXMLDataset(Set recordElementNames, InputStream stream) throws AppCrash {

        InputSource src = new InputSource(stream);
        _parser = new XMLRecordParser(recordElementNames, src);
        _lastRowReturned = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.project.dataset.DataSet_itf#open()
     */
    @Override
    public void open() throws AppCrash {

        _parser.start();
        _currentRowContent = _parser.parseNext();
        _nextRowContent = _parser.parseNext();

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.project.dataset.DataSet_itf#rewind()
     */
    @Override
    public void rewind() throws AppCrash {

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.project.dataset.DataSet_itf#close()
     */
    @Override
    public void close() throws AppCrash {

        _parser.stop();

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.project.dataset.DataSet_itf#setParam(java.util.Hashtable)
     */
    @Override
    public void setParam(Map parametri) throws AppCrash {

    }

    /*
     * (non-Javadoc)
     * 
     * @see net.project.dataset.DataSet_itf#getColumnNo()
     */
    @Override
    public int getColumnNo() throws AppCrash {

        return _currentRowContent.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.project.dataset.DataSet_itf#getColumnNames()
     */
    @Override
    public String[] getColumnNames() throws AppCrash {

        if (_lastRowReturned != null) {
            return getColummnNames(_lastRowReturned);
        }

        return getColummnNames(_currentRowContent);

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Enumeration#hasMoreElements()
     */
    @Override
    public boolean hasMoreElements() {

        if (_lastRowReturned == null && _currentRowContent == null) return false;

        if (_nextRowContent == null && _currentRowContent != null) return true;

        if (_nextRowContent == null) {
            return false;
        }

        return true;

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Enumeration#nextElement()
     */
    @Override
    public Object nextElement() {

        Row_itf result;
        if (_currentRowContent == null) {
            throw new NoSuchElementException("");
        }
        try {
            _lastRowReturned = _currentRowContent;
            result = buildRow(_currentRowContent);
            _currentRowContent = _nextRowContent;
            _nextRowContent = _parser.parseNext();

        } catch (AppCrash e) {
            throw new RuntimeException("AppCrah");
        }

        return result;
    }

    private Row_itf buildRow(Map rowContent) throws AppCrash {

        String[] colNames = getColummnNames(rowContent);

        HashtableRow htr;
        try {
            htr = new HashtableRow(rowContent, colNames);
            return htr;
        } catch (AppCrash e) {
            e.printStackTrace();
            throw e;
        }

    }

    private String[] getColummnNames(Map content) {

        Set columns = content.keySet();
        Object[] objs = columns.toArray();

        String[] colNames = new String[objs.length];

        for (int j = 0; j < objs.length; j++) {
            colNames[j] = (String) objs[j];
        }
        return colNames;
    }
}
