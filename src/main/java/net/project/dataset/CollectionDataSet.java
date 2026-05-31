/*
  CollectionDataSet.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 26/11/2002

  Autore: Luca M.

  Note:

  Modifiche:

 */

package net.project.dataset;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;

/**
 * Classe che rappresenta un DataSet costruito attorno ad un List. E' possibile passare il List direttamente al
 * costruttore, oppure passare un oggetto Map al costruttore, oppure utilizzare il costruttore vuoto e quindi riempire
 * il DataSet, costruendone man mano il List. I nomi delle colonne del DataSet NON sono case sensitive.
 */
public class CollectionDataSet implements SizeableDataSet_itf {

    private boolean            _firstOpenExecuted = false;
    private boolean            _datasetOpened     = false;

    // Nome di default della 'colonna' del dataset
    // quando il dataset ne contiene una sola.
    public static final String VALUE              = "VALUE";

    private List               _theList           = null;
    private int                _index             = 0;
    private String[]           _columnNames       = null;

    /**
     * Costruttore.
     * 
     * @param list java.util.List L'oggetto attorno al quale è costruito il presente DataSet. NON PUO' essere NULL, né
     *            VUOTO. Se i suoi elementi NON sono né HashtableRow, né Map, allora sono considerati scalari.
     *            Altrimenti, sono considerati righe di scalari. In tal caso, il corretto funzionamento del dataset è
     *            garantito a patto che chiamando getColumnNames() su ogni elemento di list si ottenga sempre lo stesso
     *            String[].
     * @exception net.project.errors.AppCrash se il parametro in ingresso è NULL o VUOTO.
     */
    public CollectionDataSet(List list) throws AppCrash {

        ErrDetector.GetInstance()
                .invariant(list != null, "Il parametro list del costruttore CollectionDataSet è NULL!");
        ErrDetector.GetInstance().invariant(list.size() > 0,
                "Il parametro list del costruttore CollectionDataSet è VUOTO!");

        if (list.get(0) instanceof HashtableRow) {
            _theList = list;
            _columnNames = ((HashtableRow) list.get(0)).getColumnNames();

        } else {
            _theList = new Vector(list.size());

            if (list.get(0) instanceof Map) {
                Iterator mapKeys = ((Map) list.get(0)).keySet().iterator();
                _columnNames = new String[((Map) list.get(0)).size()];
                int columnNo = 0;
                while (mapKeys.hasNext()) {
                    _columnNames[columnNo] = mapKeys.next().toString();
                    columnNo++;
                }
                for (int rowNo = 0; rowNo < list.size(); rowNo++) {
                    HashtableRow row = new HashtableRow((Map) (list.get(rowNo)), _columnNames);
                    _theList.add(row);
                }

            } else {
                _columnNames = new String[] { VALUE };
                for (int rowNo = 0; rowNo < list.size(); rowNo++) {
                    Map hash = new HashMap(1);
                    hash.put(VALUE, list.get(rowNo));
                    HashtableRow row = new HashtableRow(hash, _columnNames);
                    _theList.add(row);
                }
            }

        }

    }

    /**
     * Costruttore.
     * 
     * @param map java.util.Map Unica riga del dataset NON PUO' essere NULL, né VUOTA.
     * @exception net.project.errors.AppCrash se il parametro in ingresso è NULL o VUOTO.
     */
    public CollectionDataSet(Map map) throws AppCrash {

        ErrDetector.GetInstance().invariant(map != null, "Il parametro map del costruttore CollectionDataSet è NULL!");
        ErrDetector.GetInstance().invariant(!map.isEmpty(),
                "Il parametro map del costruttore CollectionDataSet è VUOTO!");

        Iterator mapKeys = map.keySet().iterator();
        _columnNames = new String[map.size()];
        int columnNo = 0;
        while (mapKeys.hasNext()) {
            _columnNames[columnNo] = mapKeys.next().toString();
            columnNo++;
        }
        _theList = new Vector(1);
        _theList.add(new HashtableRow(map, _columnNames));

    }

    /**
     * Costruttore.
     * 
     * @param columnNames java.lang.String[] Elenco dei nomi delle colonne del dataset. NON PUO' essere NULL, NE'
     *            formato da stringhe VUOTE.
     * @exception net.project.errors.AppCrash Se il parametro in ingresso è NULL o formato da stringhe VUOTE.
     */
    public CollectionDataSet(String[] columnNames) throws AppCrash {

        ErrDetector.GetInstance().preCond(columnNames != null, "Il parametro columnNames nel costruttore è NULL!");
        for (int i = 0; i < columnNames.length; i++) {
            ErrDetector.GetInstance().preCond(
                    isNotEmpty(columnNames[i]),
                    "L'elemento n." + i + " nel parametro columnNames del costruttore ha un valore non accettabile: -"
                            + columnNames[i] + "-");
        }

        _columnNames = columnNames;
        _theList = new Vector();

    }

    /**
     * Costruttore vuoto. Costruisce un dataset con una sola colonna con un nome di default ('VALUE').
     * 
     * @exception net.project.errors.AppCrash MAI LANCIATO
     */
    public CollectionDataSet() throws AppCrash {

        this(new String[] { VALUE });
    }

    /**
     * Non fa nulla.
     */
    @Override
    public void open() {

        _firstOpenExecuted = true;
        _datasetOpened = true;
    }

    /**
     * Azzera l'indice.
     */
    @Override
    public void rewind() {

        _index = 0;
    }

    /**
     * Azzera l'indice.
     */
    @Override
    public void close() {

        _index = 0;
        _datasetOpened = false;
    }

    /**
     * Non fa nulla.
     */
    @Override
    public void setParam(Map columnNamesInfo) {

    }

    /**
     * Restituisce il numero di colonne.
     * 
     * @return int il numero di colonne.
     */
    @Override
    public int getColumnNo() {

        return _columnNames.length;
    }

    /**
     * Restituisce l'elenco dei nomi delle colonne.
     * 
     * @return java.lang.String[] l'elenco delle colonne.
     */
    @Override
    public String[] getColumnNames() {

        return _columnNames;
    }

    /**
     * Dice se il dataset ha altri elementi.
     */
    @Override
    public boolean hasMoreElements() {

        return _index < _theList.size();
    }

    /**
     * Restituisce il prossimo elemento del dataset.
     */
    @Override
    public Object nextElement() {

        try {
            Object nextElement = _theList.get(_index);
            _index++;
            return nextElement;

        } catch (ArrayIndexOutOfBoundsException aioobe) {
            new AppCrash(aioobe);
            throw new NoSuchElementException();
        }

    }

    /**
     * Aggiunge una riga vuota in coda al dataset. La riga è un oggetto HashtableRow.
     */
    public void addRow() throws AppCrash {

        // controllo che il dataset non sia aperto
        ErrDetector.GetInstance().preCond(!_datasetOpened,
                "Class: CollectionDataSet - metodo addRow() non possibile con dataset aperto");
        _theList.add(new HashtableRow(_columnNames));
    }

    /**
     * Aggiunge all'ultima riga del presente dataset una elemento, in forma di coppia chiave-valore.
     * 
     * @param key java.lang.Object La chiave dell'elemento da aggiungere: NON PUO' essere NULL; DEVE essere una stringa
     *            contenuta in _columnNames.
     * @param value java.lang.Object Il valore dell'elemento da aggiungere: NON PUO' essere NULL.
     * @return java.lang.Object Il valore che era già mappato sulla riga corrente alla chiave richiesta, o NULL se non
     *         ve ne era mappato alcuno.
     * @exception net.project.errors.AppCrash Se chiave o valore sono NULL, oppure se la chiave non è una stringa
     *                contenuta in _columnNames.
     */
    public Object put(Object key, Object value) throws AppCrash {

        return ((HashtableRow) (_theList.get(_theList.size() - 1))).put(key, value);
    }

    /**
     * Aggiunge uno scalare in coda al dataset.
     * 
     * @param value java.lang.Object L'oggetto da aggiungere. PUO' ESSERE NULL.
     * @return java.lang.Object Il valore che era già mappato sulla riga corrente alla chiave richiesta (cioè la chiave
     *         di default, 'VALUE'), o NULL se non ne era mappato alcuno.
     */
    public Object add(Object value) throws AppCrash {

        addRow();
        return put(VALUE, value);

    }

    private boolean isNotEmpty(String what) {

        return ((what != null) && (what.trim().length() > 0));
    }

    @Override
    public String toString() {

        if (_theList == null) {
            return "(CollectionDataSet non correttamente costruito)";
        }
        if (_theList.size() == 0) {
            return "(CollectionDataSet vuoto)";
        }
        StringBuffer description = new StringBuffer();
        for (int rowCont = 0; rowCont < _theList.size(); rowCont++) {
            description.append("Riga n. ");
            description.append(rowCont);
            description.append(" = -");
            description.append(_theList.get(rowCont));
            description.append("- \n");
        }
        return description.toString();

    }

    /**
     * Ritorna il numero di righe presenti nel dataset corrente.
     * 
     * @return int
     * @exception net.project.errors.AppCrash
     */
    @Override
    public int getNumberOfRows() throws AppCrash {

        // controllo che il dataset sia aperto
        ErrDetector.GetInstance().preCond(_firstOpenExecuted,
                "Class: CollectionDataSet - metodo getNumberOfRows() non possibile prima di open()");
        return _theList.size();
    }

}
