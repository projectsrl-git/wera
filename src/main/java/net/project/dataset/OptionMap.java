/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.misc.Config;

/**
 * La classe legge dal file di configurazione un elenco di dati e li restituisce sotto forma di DataSet<br>
 * Nel file di configurazione si possono speicificare i nomi delle colonne utilizzate per costruire il DataSet,<br>
 * oltre ai separatori (Regular Expression) utilizzati per interpretare l'elenco di dati di partenza.<br>
 * <br>
 * Proprietà lette dal file di configurazione:<br>
 * - DS.OptionMap.DefaultRecordSeparator Separatore di default dei record; se non specificato, vale ';'<br>
 * - DS.OptionMap.DefaultFieldSeparator Separatore di default dei campi; se non specificato, vale ','<br>
 * - DS.OptionMap.DefaultColumnName Nomi di colonna utilizzati per creare il dataset; se non specificato, vale
 * 'CODICE,DESCR'<br>
 * - DS.#NomeOpzione#.Class Questa classe<br>
 * - DS.#NomeOpzione#.Row Classe utilizzata per scorrere il dataset restituito da questa classe<br>
 * - DS.#NomeOpzione#.RecordSeparator Separatore dei record di questo OptionMap; se non specificato, utilizza il default<br>
 * - DS.#NomeOpzione#.FieldSeparator Separatore dei campi di questo OptionMap; se non specificato, utilizza il default<br>
 * - DS.#NomeOpzione#.Options Elenco di dati da interpretare<br>
 * <br>
 * ATTENZIONE: i separatori sono delle regular expression, eventuali caratteri speciali vanno quindi racchiusi tra
 * parentesi quadre.<br>
 */
public class OptionMap implements DataSet_itf {

    // campi del dataset
    private String    _configName             = null;
    private String    _dsName                 = null;
    private String    _dsOptionList           = null;
    private String[]  _nomeColonne            = null;
    private ArrayList _dataSource             = null;
    private String    _defaultColumnName      = null;
    private String    _defaultFieldSeparator  = null;
    private String    _defaultRecordSeparator = null;
    private String    _columnName             = null;
    private String    _fieldSeparator         = null;
    private String    _recordSeparator        = null;

    // indice all'interno dell'ArrayList dei dati
    private int       _posizione              = 0;

    /**
     * Creates a new OptionMap object.
     *
     * @param configName DOCUMENT ME!
     * @param dsName DOCUMENT ME!
     *
     * @throws AppCrash DOCUMENT ME!
     */
    public OptionMap(String configName, String dsName) throws AppCrash {

        ArrayList nomeColonne = null;
        int contaColonne = 0;

        // controllo formale dei parametri in ingresso
        ErrDetector.GetInstance().invariant(configName != null);
        ErrDetector.GetInstance().param(dsName);

        _configName = configName;
        _dsName = dsName;

        Logger.GetInstance().log3("OptionMap - Costruttore per " + dsName);

        // verifica che esistano le voci nel file di configurazione
        _dsOptionList = Config.GetInstance(_configName).getProperty("DS." + _dsName + ".Options", "");
        ErrDetector.GetInstance().postCond(_dsOptionList != null);

        // legge le proprietà di default
        _defaultFieldSeparator = Config.GetInstance(_configName).getProperty("DS.OptionMap.DefaultFieldSeparator", ",");
        _defaultRecordSeparator = Config.GetInstance(_configName).getProperty("DS.OptionMap.DefaultRecordSeparator",
                ";");

        // legge le proprietà dell'OptionMap
        _fieldSeparator = Config.GetInstance(_configName).getProperty("DS." + _dsName + ".FieldSeparator",
                _defaultFieldSeparator);
        _recordSeparator = Config.GetInstance(_configName).getProperty("DS." + _dsName + ".RecordSeparator",
                _defaultRecordSeparator);

        // legge la proprietà di default dei nomi colonna
        // non lo faccio prima in quanto utilizzo il separatore letto prima
        _defaultColumnName = Config.GetInstance(_configName).getProperty("DS.OptionMap.DefaultColumnName",
                "CODICE" + _fieldSeparator + "DESCR");
        _columnName = Config.GetInstance(_configName).getProperty("DS." + _dsName + ".ColumnName", _defaultColumnName);

        // determina i nomi delle colonne
        nomeColonne = new ArrayList();

        StringTokenizer stColonne = new StringTokenizer(_columnName, _fieldSeparator);

        while (stColonne.hasMoreTokens()) {
            String elemento = stColonne.nextToken();
            nomeColonne.add(elemento);
            contaColonne++;
        }

        // creo l'array di stringhe dall'ArrayList per poterlo passare al costruttore di HashtableRow
        _nomeColonne = new String[contaColonne];

        for (int i = 0; i < contaColonne; i++) {
            _nomeColonne[i] = (String) nomeColonne.get(i);
        }

        Logger.GetInstance().log3("OptionMap - Separatori: " + _fieldSeparator + " " + _recordSeparator);
        Logger.GetInstance().log3("OptionMap - Colonne: " + nomeColonne.toString());
    }

    /**
     * Questo metodo legge il parametro dal file di configurazione e lo suddivide nei singoli campi al fine di<br>
     * restituire un ArrayList composto dalle coppie chiave-valore.<br>
     * I nomi dei campi e i separatori dei valori sono definiti dalle apposite proprieta'.<br>
     *
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    public void open() throws AppCrash {

        _dataSource = new ArrayList();

        // legge le opzioni dal file di configurazione
        if ((_dsOptionList != null) && !_dsOptionList.equals("")) {
            // elabora le singole righe
            String[] splitString1 = _dsOptionList.split(_recordSeparator);

            for (int ind1 = 0; ind1 < splitString1.length; ind1++) {
                if ((splitString1[ind1] != null) && !splitString1[ind1].equals("")) {
                    // elabora i dati di ogni riga
                    String[] splitString2 = splitString1[ind1].split(_fieldSeparator);
                    Map elencoDati = new HashMap();

                    int ind2 = 0;

                    if (splitString2.length <= _nomeColonne.length) {
                        for (ind2 = 0; ind2 < splitString2.length; ind2++) {
                            elencoDati.put(_nomeColonne[ind2], splitString2[ind2].trim());
                        }
                    }

                    if (splitString2.length < _nomeColonne.length) {
                        Logger.GetInstance().log3(
                                "OptionMap " + _dsName + " - Dati insufficienti, utilizzo stringhe vuote per: "
                                        + splitString1[ind1]);

                        for (; ind2 < _nomeColonne.length; ind2++) {
                            elencoDati.put(_nomeColonne[ind2], "");
                        }
                    }

                    if (splitString2.length > _nomeColonne.length) {
                        Logger.GetInstance().log3("OptionMap - Troppi valori per : " + splitString1[ind1]);
                    }

                    _dataSource.add(new HashtableRow(elencoDati, _nomeColonne));
                    Logger.GetInstance().log3("OptionMap - Riga: " + elencoDati.toString());
                }
            }
        }

        _posizione = 0;
    }

    /**
     * Questo metodo
     *
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    public void rewind() throws AppCrash {

        _posizione = 0;
    }

    /**
     * Questo metodo non e' necessario per l'oggetto ArrayList, quindi non eseguo nessuna operazione
     *
     * @throws AppCrash Mai lanciato
     */
    @Override
    public void close() throws AppCrash {

        // metodo non necessario per l'oggetto ArrayList, quindi non eseguo nessuna operazione
    }

    /**
     * Questo metodo
     *
     * @param parametri DOCUMENT ME!
     *
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    public void setParam(Map parametri) throws AppCrash {

        // metodo non necessario per OptionMap, in quanto non prevede parametri variabili
    }

    /**
     * Questo metodo
     *
     * @return DOCUMENT ME!
     *
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    public int getColumnNo() throws AppCrash {

        return _nomeColonne.length;
    }

    /**
     * Questo metodo
     *
     * @return DOCUMENT ME!
     *
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    public String[] getColumnNames() throws AppCrash {

        return _nomeColonne;
    }

    /**
     * Questo metodo
     *
     * @return DOCUMENT ME!
     */
    @Override
    public boolean hasMoreElements() {

        return (_posizione < _dataSource.size());
    }

    /**
     * Questo metodo
     *
     * @return DOCUMENT ME!
     */
    @Override
    public Object nextElement() throws NoSuchElementException {

        if (_posizione >= _dataSource.size()) {
            throw new NoSuchElementException();
        }

        return _dataSource.get(_posizione++);
    }
}
