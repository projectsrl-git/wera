/*
  HashtableRow.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione:

  Autore: Simone Z.

  Note:

  Modifiche:	Aggiunto costruttore con Row_itf.
  				(22/11/2002 - Luca M.)

 */

package net.project.dataset;

import java.util.HashMap;
import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.ErrDetector_itf;

/**
 * Classe che rappresenta una riga di un oggetto DataSet_itf. La riga è realizzata tenendo in un array di String le
 * coppie numeroColonna - nomeColonna di un dataset, e in una Map le coppie nomeColonna - valoreColonna del medesimo
 * dataset. La classe è case insensitive rispetto al nome delle colonne.
 * 
 */
public class HashtableRow implements Row_itf {

    private HashMap  _row         = null;
    private String[] _columnNames = null;

    /**
     * Costruttore. Costruisce un oggetto HashtableRow a partire da un qualsiasi oggetto Row_itf.
     * 
     * @param row net.project.dataset.Row_itf La riga dalla quale costruire il presente oggetto. NON PUO' essere NULL.
     * @param columnNames java.lang.String[] I nomi delle colonne del dataset. NON PUO' essere NULL.
     * @exception net.project.errors.AppCrash In caso di parametri in ingresso NULL o non validi.
     * 
     */
    public HashtableRow(Row_itf row, String[] columnNames) throws AppCrash {

        ErrDetector.GetInstance().param(row);
        doConstruction(row, columnNames);

    }

    /**
     * Costruttore. Costruisce un oggetto HashtableRow a partire da un qualsiasi oggetto Map.
     * 
     * @param set java.util.Map La map da utilizzare per costruire il presente oggetto. NON PUO' essere NULL, né VUOTA.
     * @param columnNames java.lang.String[] I nomi delle colonne del dataset. NON PUO' essere NULL.
     * @exception net.project.errors.AppCrash In caso di parametri in ingresso NULL o non validi.
     * 
     */
    public HashtableRow(Map set, String[] columnNames) throws AppCrash {

        ErrDetector.GetInstance().param(set);
        ErrDetector.GetInstance().param(!set.isEmpty());
        doConstruction(set, columnNames);

    }

    /**
     * Costruttore. Costruisce un oggetto HashtableRow del quale si conoscono solo i nomi delle colonne. L'oggetto sarà
     * popolato successivamente tramite il metodo put(Object, Object).
     * 
     * @param columnNames java.lang.String[] I nomi delle colonne del dataset. NON PUO' essere NULL.
     * @exception net.project.errors.AppCrash In caso di parametri in ingresso NULL o non validi.
     * 
     */
    public HashtableRow(String[] columnNames) throws AppCrash {

        checkColumnNames(columnNames);
        _columnNames = toUpperCase(columnNames);
        _row = new HashMap(_columnNames.length);

    }

    private void doConstruction(Object row, String[] columnNames) throws AppCrash {

        checkColumnNames(columnNames);
        _columnNames = toUpperCase(columnNames);
        _row = new HashMap(_columnNames.length);
        for (int i = 0; i < _columnNames.length; i++) {
            Object value = getColumnValue(row, columnNames[i]);
            _row.put(_columnNames[i], formatIfNull(value));
        }

    }

    private String[] toUpperCase(String[] what) {

        String[] upperCaseWords = new String[what.length];
        for (int i = 0; i < what.length; i++) {
            upperCaseWords[i] = what[i].toUpperCase();
        }
        return upperCaseWords;

    }

    private Object getColumnValue(Object row, String columnName) throws AppCrash {

        if (row instanceof Row_itf) {
            return formatIfNull(((Row_itf) row).getField(columnName));
        }
        if (row instanceof Map) {
            return ((Map) row).get(columnName);
        }
        throw new AppCrash("Parametro row di classe non valida: -" + row.getClass().getName() + "-");

    }

    private Object formatIfNull(Object what) {

        if (what == null) {
            return new SqlNull();
        }
        return what;

    }

    private void checkColumnNames(String[] columnNames) throws AppCrash {

        ErrDetector_itf ed = ErrDetector.GetInstance();
        ed.param(columnNames);
        for (int i = 0; i < columnNames.length; i++) {
            ed.param(columnNames[i]);
            ErrDetector.GetInstance().preCond(
                    isNotEmpty(columnNames[i]),
                    "L'elemento n." + i + " nel parametro columnNames del costruttore ha un valore non accettabile: -"
                            + columnNames[i] + "-");
        }

    }

    /**
     * Restituisce il valore di un campo della riga.
     * 
     * @param java.lang String fieldName il nome del campo
     * @return java.lang.Object il valore del campo Può essere NULL.
     * @exception net.project.errors.ParamCrash se il parametro in ingresso è NULL
     * @exception net.project.errors.AppCrash in caso di errore nel recupero del valore del campo
     */
    @Override
    public Object getField(String fieldName) throws AppCrash {

        // Controllo formale del parametro in ingresso.
        try {
            ErrDetector.GetInstance().param(fieldName);
            String nome = fieldName.toUpperCase();
            ErrDetector.GetInstance().preCond(isListedInColumnNames(nome));

            Object resp = _row.get(nome);
            return checkResp(resp);
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("HashTableRow", buildKeyNotListedLog(fieldName));
            throw ac;
        }

    }

    /**
     * Restituisce il valore di un campo della riga.
     * 
     * @param int fieldNo il numero del campo
     * @return java.lang.Object il valore del campo Può essere NULL.
     * @exception net.project.errors.ParamCrash se il parametro in ingresso è zero o un numero negativo
     * @exception net.project.errors.AppCrash in caso di errore nel recupero del valore del campo
     */
    @Override
    public Object getField(int fieldNo) throws AppCrash {

        try {
            // Controllo formale del parametro in ingresso.
            ErrDetector.GetInstance().preCond(fieldNo > 0 && fieldNo <= _columnNames.length);

            Object resp = _row.get(_columnNames[fieldNo - 1]);
            return checkResp(resp);
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("HashTableRow", "FieldNo: " + fieldNo);
            throw ac;
        }

    }

    private Object checkResp(Object resp) {

        if (resp instanceof SqlNull) {
            return null;
        }
        return resp;

    }

    // Metodo con visibilità di package
    // da utilizzarsi per riempire il presente oggetto
    // costruito col costruttore HashtableRow(String[]).
    // Se il valore e' null, al suo posto viene
    // inserita un'istanza di SqlNull.
    // Lancia un AppCrash nei seguenti casi:
    // - la chiave e' null;
    // - la chiave non e' un oggetto String;
    // - la chiave non e' una stringa elencata tra i nomi delle colonne.
    Object put(Object key, Object value) throws AppCrash {

        try {
            ErrDetector_itf ed = ErrDetector.GetInstance();
            ed.preCond(key != null, "La chiave di put(Object, Object) è NULL!)");
            ed.preCond(key instanceof String);
            key = ((String) key).toUpperCase();
            ed.preCond(isListedInColumnNames((String) key));

            if (value == null) {
                value = new SqlNull();
            }
            return _row.put(key, value);
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("HashTableRow", buildKeyNotListedLog(key));
            throw ac;
        }

    }

    private boolean isListedInColumnNames(String key) {

        for (int i = 0; i < _columnNames.length; i++) {
            if (key.equals(_columnNames[i])) {
                return true;
            }
        }
        return false;

    }

    private String buildKeyNotListedLog(Object key) {

        StringBuffer log = new StringBuffer();
        log.append("La chiave ");
        log.append("non è elencata in _columnNames! \n");
        log.append("Chiave: -");
        log.append(key);
        log.append("- \n");
        log.append("_columnNames:");
        for (int i = 0; i < _columnNames.length; i++) {
            log.append(" -");
            log.append(_columnNames[i]);
            log.append("-;");
        }
        return log.toString();

    }

    /**
     * Conta il numero di colonne della riga.
     * 
     * @return int numero di colonne del dataset
     */
    @Override
    public int getColumnNo() {

        return _columnNames.length;
    }

    /**
     * Restituisce i nomi delle colonne della riga.
     * 
     * @return java.lang.String[] nomi delle colonne della riga.
     */
    public String[] getColumnNames() {

        return _columnNames;
    }

    private boolean isNotEmpty(String what) {

        return ((what != null) && (what.trim().length() > 0));
    }

    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < _columnNames.length; i++) {
            buffer.append("colonna ");
            buffer.append(i + 1);
            buffer.append(": nome: -");
            buffer.append(_columnNames[i]);
            buffer.append("-; valore: -");
            Object valore = _row.get(_columnNames[i]);
            if (valore != null) {
                buffer.append(valore.toString());
            } else {
                buffer.append(valore);
            }
            buffer.append("-; ");
        }
        return buffer.toString();

    }

    /**
     * Classe statica che rappresenta SQL NULL.
     */
    protected static class SqlNull {
    }

}
