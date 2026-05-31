/*
  FileDataSet.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione:

  Autore: Anna L.
  
  Note:

  Modifiche:

 */

package net.project.dataset;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;

/**
 * Rappresenta un DataSet ottenuto dalla lettura di un file sequenziale.
 *
 * <P>
 * Proprietà lette dal file di configurazione: DS.dsName.DimensioneRecord = la lunghezza del record
 * </p>
 *
 * <p>
 * DS.dsName.Path = proprieta' facoltativa. Se presente indica il path dove si trova il file sorgente
 * </p>
 * 
 */
public class FileDataSet implements DataSet_itf {

    private String              _configName        = null;
    private String              _dsName            = null;
    private FileInputStream     _inputStream       = null;
    private String              _fileName          = null;
    private int                 _dimRecord         = 0;
    private byte[]              _currentRecord     = null;

    private static final String NOME_FILE          = "nomeFile";
    private static final String NOME_ASSOLUTO_FILE = "nomeAssolutoFile";

    /**
     * Costruttore.
     *
     * @param configName java.lang.String Il nome della configurazione
     * @param dsName java.lang.String Il nome del dataset
     * @exception net.project.errors.AppCrash
     */
    public FileDataSet(String configName, String dsName) throws AppCrash {

        // controllo formale dei parametri in ingresso
        ErrDetector.GetInstance().invariant(configName != null);
        ErrDetector.GetInstance().param(dsName);

        _configName = configName;
        _dsName = dsName;
    }

    /**
     * Rende disponibile il DataSet.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void open() throws AppCrash {

        ErrDetector.GetInstance().preCond(_fileName != null, "setParam() non eseguito");
        try {
            // apre il file
            _inputStream = new FileInputStream(_fileName);
        } catch (FileNotFoundException fnfe) {
            AppCrash ap = new AppCrash(fnfe);
            throw (ap);
        }
        // Recupera da file di configurazione la dimensione in numero di bytes del record
        String dim = Config.GetInstance().getProperty("DS." + _dsName + ".DimensioneRecord");
        ErrDetector.GetInstance().param(dim);
        _dimRecord = Integer.parseInt(dim);
        // crea un byte array della dimensione richiesta
        _currentRecord = new byte[_dimRecord];
    }

    /**
     * Posiziona il 'cursore' del DataSet in corrispondenza del primo elemento.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void rewind() throws AppCrash {

        close();
        open();
    }

    /**
     * Rilascia il DataSet.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void close() throws AppCrash {

        try {
            _inputStream.close();
        } catch (IOException ioe) {
            AppCrash ap = new AppCrash(ioe);
            throw (ap);
        }
    }

    /**
     * Imposta i parametri necessari per l'apertura del DataSet. Nella Map ci deve essere uno dei due parametri
     * "nomeFile" e "nomeAssolutoFile".
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void setParam(Map parametri) throws AppCrash {

        // controllo formale del parametro in ingresso
        ErrDetector.GetInstance().param(parametri);
        _fileName = (String) parametri.get(NOME_FILE);
        if (_fileName != null) {
            String path = Config.GetInstance().getProperty("DS." + _dsName + ".Path");
            if (path != null) {
                _fileName = path + (String) parametri.get(NOME_FILE);
            }
        } else {
            _fileName = (String) parametri.get(NOME_ASSOLUTO_FILE);
            if (_fileName == null) {
                AppCrash ap = new AppCrash();
                throw (ap);
            }
        }
    }

    /**
     * Conta il numero di colonne del DataSet
     * 
     * @return int numero di colonne del DataSet
     * @exception net.project.errors.AppCrash
     */
    @Override
    public int getColumnNo() throws AppCrash {

        return 1;
    }

    /**
     * Ritorna i nomi delle colonne del DataSet
     * 
     * @return String[] nomi delle colonne del DataSet
     * @exception net.project.errors.AppCrash
     */
    @Override
    public String[] getColumnNames() throws AppCrash {

        String[] names = new String[1];
        names[0] = "RECORD";
        return names;
    }

    /**
     * Controlla se il file contiene altri record.
     *
     * @return boolean true se il file contiene altri record, false altrimenti
     */
    @Override
    public boolean hasMoreElements() {

        try {
            return (_inputStream.available() > 0 ? true : false);
        } catch (IOException ioe) {
            AppCrash ap = new AppCrash(ioe);
            return false;
        }
    }

    /**
     * Restituisce il record del file successivo a quello corrente.
     *
     * @return java.lang.Object il record successivo del FileDataSet, null in caso di errore nella creazione
     *         dell'oggetto Row_itf
     *
     * @exception NoSuchElementException se non ci sono più elementi nel FileDataSet
     */
    @Override
    public Object nextElement() throws NoSuchElementException {

        try {
            int byteRead = _inputStream.read(_currentRecord);
            if (byteRead == -1) {
                try {
                    close();
                } catch (AppCrash ex) {
                }
                throw new NoSuchElementException("NoSuchElementException - " + toString());
            }
            // considero l'eventuale assenza nell'ultimo record di un eventuale carriage return-line feed (CRLF)
            if ((byteRead != _dimRecord) && (byteRead != _dimRecord - 2)) {
                throw new NoSuchElementException("Letti meno di " + _dimRecord
                        + " bytes dal file sorgente -> Bytes letti = " + byteRead);
            }
            return createRow(_currentRecord);
        } catch (IOException ioe) {
            throw new NoSuchElementException("IOException - " + toString());
        } catch (AppCrash ap) {
            return null;
        }
    }

    /**
     * Metodo protected che crea un oggetto di tipo HashtableRow formato da una sola colonna di nome "record" contenente
     * la riga del file in formato String
     *
     * @param record byte[] L'array di byte contenente i dati per costruire la riga
     * @return net.project.dataset.Row_itf l'oggetto costruito
     * @exception net.project.errors.AppCrash in caso di errori nel reperimento dei dati o nella costruzione
     *                dell'oggetto HashtableRow
     */
    protected Row_itf createRow(byte[] record) throws AppCrash {

        ErrDetector.GetInstance().param(record);
        Map hashtable = new HashMap(1);
        hashtable.put("RECORD", new String(record));
        String[] columsName = new String[1];
        columsName[0] = "RECORD";
        HashtableRow hashRow = new HashtableRow(hashtable, columsName);
        return hashRow;
    }

    /**
     * Restituisce una stringa composta dai nomi di determinati attributi della classe, ognuno seguito dal proprio
     * valore corrente.
     *
     * @return java.lang.String la stringa restituita
     */
    @Override
    public String toString() {

        StringBuffer temp = new StringBuffer();
        temp.append("; _configName = ");
        temp.append(_configName);
        temp.append("; _dsName = ");
        temp.append(_dsName);
        temp.append("; _fileName = ");
        temp.append(_fileName);
        temp.append("; _dimRecord = ");
        temp.append(_dimRecord);
        temp.append("; _currentRecord = ");
        temp.append(new String(_currentRecord));
        return temp.toString();
    }
}
