/*
  SetifDataSet.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione:

  Autore: Anna L.
  
  Note:

  Modifiche:

 */

package net.project.dataset;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.mess.fixlen.FixRead;
import net.project.mess.fixlen.FixReadEx;
import net.project.misc.Config;

/**
 * Rappresenta un DataSet ottenuto dalla lettura di un flusso Setif.
 *
 * <P>
 * Proprietà lette dal file di configurazione: DS.dsName.NomeSorgenteDati = il nome logico del dataset da usare come
 * sorgente dati
 * </p>
 *
 * <P>
 * Setif.causale.tipoRecord.multiplo = proprietà facoltativa, se presente può valere true oppure false
 * </p>
 */
public class SetifDataSet implements DataSet_itf {

    private String              _configName  = null;
    private String              _dsName      = null;
    private DataSet_itf         _dataSource  = null;
    private String              _sourceName  = null;

    private static final String RECORD_TESTA = "PA";
    private static final String RECORD_CODA  = "EF";
    private static final String RECORD_12    = "12";
    private static final String RECORD_70    = "70";

    private static final String TIPO_RECORD  = "TIPO_RECORD";
    private static final String CAUSALE      = "CAUSALE";

    /**
     * Costruttore.
     *
     * @param configName java.lang.String Il nome della configurazione
     * @param dsName java.lang.String Il nome del dataset
     * @exception net.project.errors.AppCrash
     */
    public SetifDataSet(String configName, String dsName) throws AppCrash {

        // controllo formale dei parametri in ingresso
        ErrDetector.GetInstance().invariant(configName != null);
        ErrDetector.GetInstance().param(dsName);
        // creo la sorgente dati del flusso setif
        _sourceName = Config.GetInstance().getProperty("DS." + dsName + ".NomeSorgenteDati");
        _dataSource = DataSetFactory.getInstance().makeDataSet(configName, _sourceName);

        _configName = configName;
        _dsName = dsName;
    }

    /**
     * Imposta i parametri necessari per l'apertura del DataSet.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void setParam(Map parametri) throws AppCrash {

        // controllo formale del parametro in ingresso
        ErrDetector.GetInstance().param(parametri);
        Logger.GetInstance().log3("Paramentri passati = " + parametri.toString());
        _dataSource.setParam(parametri);
    }

    /**
     * Rende disponibile il DataSet.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void open() throws AppCrash {

        _dataSource.open();
    }

    /**
     * Posiziona il 'cursore' del DataSet in corrispondenza del primo elemento.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void rewind() throws AppCrash {

        _dataSource.rewind();
    }

    /**
     * Rilascia il DataSet.
     * 
     * @return void
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void close() throws AppCrash {

        _dataSource.close();
    }

    /**
     * Conta il numero di colonne del DataSet
     * 
     * @return int numero di colonne del DataSet
     * @exception net.project.errors.AppCrash
     */
    @Override
    public int getColumnNo() throws AppCrash {

        return _dataSource.getColumnNo();
    }

    /**
     * Ritorna i nomi delle colonne del DataSet
     * 
     * @return String[] nomi delle colonne del DataSet
     * @exception net.project.errors.AppCrash
     */
    @Override
    public String[] getColumnNames() throws AppCrash {

        return _dataSource.getColumnNames();
    }

    /**
     * Controlla se ci sono altri record.
     *
     * @return boolean true se ci sono altri record, false altrimenti
     */
    @Override
    public boolean hasMoreElements() {

        return _dataSource.hasMoreElements();
    }

    /**
     * Restituisce il record successivo a quello corrente.
     *
     * @return java.lang.Object il record successivo, null in caso di errore nella creazione dell'oggetto Row_itf
     *
     * @exception NoSuchElementException se non ci sono più elementi nel dataset sorgente
     */
    @Override
    public Object nextElement() throws NoSuchElementException {

        String recordFromDataSource = null;
        String tipoRecord = null;
        String causale = null;
        Map disposizione = new HashMap();
        FixRead record = null;

        // Tipo record che inizia la disposizione. Deafaul 12 come SETIF
        String recordInizioDisposizione = Config.GetInstance().getProperty(
                "Setif." + _sourceName + ".inizioDisposizione", RECORD_12);
        try {
            Row_itf row = null;
            while (hasMoreElements()) {
                row = (Row_itf) _dataSource.nextElement();
                recordFromDataSource = (String) row.getField("RECORD");
                Logger.GetInstance().log3("Record=" + recordFromDataSource);

                // prefisso dei descrittori per i messaggi a lunghezza fissa. Default f-
                String msgPrefix = Config.GetInstance().getProperty("Setif." + _sourceName + ".messagePrefix", "f-");

                // creo un messaggio a lunghezza fissa con il record letto dalla sorgente dati
                record = creaRecord(recordFromDataSource, msgPrefix);

                tipoRecord = record.getField(TIPO_RECORD);
                if (tipoRecord.equals(RECORD_TESTA) || tipoRecord.equals(RECORD_CODA) || tipoRecord.equals(RECORD_70)) {
                    // i record di testa e coda sono unici, mentre il record 70 chiude la disposizione
                    disposizione.put(tipoRecord, record);
                    break;
                }

                if (tipoRecord.equals(recordInizioDisposizione)) {
                    // il record 12 contiene la causale
                    causale = record.getField(CAUSALE);
                }

                if (Config.GetInstance().getProperty("Setif." + causale + "." + tipoRecord + ".multiplo") == null
                        || Config.GetInstance().getProperty("Setif." + causale + "." + tipoRecord + ".multiplo")
                                .equals("false")) {
                    // record singolo
                    disposizione.put(tipoRecord, record);
                } else {
                    // record multipli
                    addRecordMultiplo(disposizione, tipoRecord, record);
                }
            }
            addCustomFields(disposizione, row);
            return createRow(disposizione);
        } catch (AppCrash ap) {
            return null;
        }
    }

    protected FixRead creaRecord(String recordFromDataSource, String msgPrefix) throws AppCrash {

        // creo un messaggio a lunghezza fissa con il record letto dalla sorgente dati
        FixRead record = new FixRead(
                (msgPrefix + recordFromDataSource.substring(1, 3) + recordFromDataSource.substring(1)).getBytes());

        return record;
    }

    /**
     * Gestisce i record multipli presenti in una disposizione memorizzandoli in un CollectionDataSet.
     *
     * @param disposizione java.util.Hashtable La disposizione contenente l'insieme dei record.
     * @param tipoRecord java.lang.String Il tipo del record multiplo corrente.
     * @param record net.project.mess.FixRead Il record corrente.
     * @exception net.project.errors.AppCrash
     */
    private void addRecordMultiplo(Map disposizione, String tipoRecord, FixRead record) throws AppCrash {

        CollectionDataSet recordMultipli;
        FixReadEx recordEx = new FixReadEx(record);
        String[] fieldList = getFieldList(recordEx);
        if (disposizione.get(tipoRecord) == null) {
            // creo un nuovo CollectionDataSet vuoto
            recordMultipli = new CollectionDataSet(fieldList);
        } else {
            // recupero il CollectionDataSet già esistente
            recordMultipli = ((CollectionDataSet) disposizione.get(tipoRecord));
        }
        // aggiungo una riga vuota al CollectionDataSet
        recordMultipli.addRow();
        // aggiungo i campi del record nel CollectionDataSet
        addRecord(recordMultipli, record, fieldList);
        // memorizzo il CollectionDataSet nella disposizione
        disposizione.put(tipoRecord, recordMultipli);
    }

    /**
     * Metodo protected che crea un oggetto di tipo Row_itf leggendo dal file di configurazione la specifica classe da
     * istanziare
     *
     * @param disposizione java.util.Map la Map contente i dati per costruire la riga
     * @return Row_itf l'oggetto costruito
     * @exception net.project.errors.AppCrash in caso di errori nel reperimento dei dati o nella costruzione
     *                dell'oggetto Row_itf
     */
    protected Row_itf createRow(Map disposizione) throws AppCrash {

        try {
            String className = Config.GetInstance().getProperty("DS." + _dsName + ".Row");
            ErrDetector.GetInstance().param(className);

            Class rowClass = Class.forName(className);
            Constructor constructor = rowClass.getConstructor(new Class[] { Map.class });
            Row_itf row = (Row_itf) constructor.newInstance(new Object[] { disposizione });

            return row;
        } catch (ClassNotFoundException cnfe) {
            AppCrash ac = new AppCrash(cnfe);
            ac.logContext("SetifDataSet", toString());
            throw ac;
        } catch (NoSuchMethodException nsme) {
            AppCrash ac = new AppCrash(nsme);
            ac.logContext("SetifDataSet", toString());
            throw ac;
        } catch (InstantiationException ie) {
            AppCrash ac = new AppCrash(ie);
            ac.logContext("SetifDataSet", toString());
            throw ac;
        } catch (InvocationTargetException ite) {
            AppCrash ac = new AppCrash(ite);
            ac.logContext("SetifDataSet", toString());
            throw ac;
        } catch (IllegalAccessException iae) {
            AppCrash ac = new AppCrash(iae);
            ac.logContext("SetifDataSet", toString());
            throw ac;
        }
    }

    /**
     * Ricava un array con i nomi dei campi del record.
     *
     * @param recordEx net.project.mess.fixlen.FixReadEx Il record.
     * @return String[] L'array con i nomi dei campi del record.
     */
    private String[] getFieldList(FixReadEx recordEx) throws AppCrash {

        try {
            Iterator fieldList = recordEx.iterator();
            Vector v = new Vector();
            while (fieldList.hasNext()) {
                v.add(fieldList.next());
            }
            Object[] obj = v.toArray();
            String[] stringArray = new String[obj.length];
            System.arraycopy(obj, 0, stringArray, 0, obj.length);
            return stringArray;
        } catch (AppCrash ap) {
            ap.logContext("SetifDataSet", "Errore recuperando lista di campi");
            throw ap;
        }
    }

    /**
     * Aggiunge i campi del record in coda al CollectionDataSet
     *
     * @param recordMultipli net.project.dataset.CollectionDataSet Il CollectionDataSet con i record multipli
     * @param record net.project.mess.FixRead Il record da aggiungere.
     * @param fieldList String[] L'array con i nomi dei campi del record.
     */
    private void addRecord(CollectionDataSet recordMultipli, FixRead record, String[] fieldList) throws AppCrash {

        String fieldName = "";
        try {
            int numberOfField = fieldList.length;
            for (int i = 0; i < numberOfField; i++) {
                fieldName = fieldList[i];
                recordMultipli.put(fieldName, record.getField(fieldName).trim());
            }
        } catch (AppCrash ap) {
            ap.logContext("SetifDataSet", "Errore su getField() del campo " + fieldName);
            throw ap;
        }
    }

    /**
     * Questo metodo permette alle sotto classi di aggiungere alla Map disposizione che verra' usata per creare la Row
     * di risposta eventuali campi aggiuntivi calcolati o di provenienza dalla Row_itf di base. La row passata e' quella
     * del record 70 di chiusura della disposizione. Questa versione ovviamente non fa nulla.
     *
     * @param disposizione la Map
     * @param row la Row_itf di base
     * @throws AppCrash in caso di eccezione
     */
    protected void addCustomFields(Map disposizione, Row_itf row) throws AppCrash {

        return;
    }
}
