/*
  DataSetFactory.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 
 
  Data creazione:
 
  Autore: Pietro G. e Luca M.

  Note:

  Modifiche:

 */

package net.project.dataset;

import java.lang.reflect.Constructor;
import java.util.Iterator;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.errors.ParamCrash;
import net.project.misc.Config;

/**
 * Factory di classi che implementano l'interfaccia DataSet_itf.
 * 
 * <P>
 * Proprietà letta dal file di configurazione: DS.dsName.Class = classe da istanziare (che implementa DataSet_itf)
 * </p>
 */
public class DataSetFactory {

    private static DataSetFactory _dataSetFactory = null;
    private DataSetRegister       _register       = new DataSetRegister();
    private boolean               _registerNeeded = false;

    /**
     * Costruttore privato.Legge dal file di configurazione la proprieta' <code>DB.SafeDBConnection</code> per stabilire
     * se e' necessaria la registrazione dei dataset creati
     */
    private DataSetFactory() {

        // serve per verificare se la registrazione del dataset creato e' necessaria o meno.
        String safe = Config.GetInstance().getProperty("DB.SafeDBConnection", "false").trim();
        if (safe.equalsIgnoreCase("true")) {
            _registerNeeded = true;
        }
    }

    /**
     * Metodo di accesso al singleton.
     * 
     * @return net.project.dataset.DataSetFactory l'istanza della classe DataSetFactory
     */
    public static DataSetFactory getInstance() {

        if (_dataSetFactory == null) {
            synchronized (DataSetFactory.class) {
                if (_dataSetFactory == null) {
                    _dataSetFactory = new DataSetFactory();
                }
            }
        }

        return _dataSetFactory;
    }

    /**
     * Crea un DataSet.
     * 
     * @param configName configName nome della configurazione
     * @param dataSetName dataSetName nome del DataSet da creare
     * 
     * @return DOCUMENT ME!
     * @throws AppCrash DOCUMENT ME!
     */
    public DataSet_itf makeDataSet(String configName, String dataSetName) throws AppCrash {

        String className = null;

        try {
            // controllo formale dei parametri in ingresso
            ErrDetector.GetInstance().invariant(configName != null);
            ErrDetector.GetInstance().param(dataSetName);

            className = Config.GetInstance(configName).getProperty("DS." + dataSetName + ".Class");
            ErrDetector.GetInstance().param(className);

            Class pageClass = null;
            Constructor constructor = null;
            DataSet_itf dataSet = null;

            // Istanziazione della classe che implementa DataSet_itf.
            // la proprieta' letta puo' contenere un elenco di classi; quella piu' a destra e' la classe base che viene
            // istanziata per prima; tutte le altre devono essere dei decorator ovvere avere un constructor
            // che prende come parametro un DataSet_itf

            String[] classes = className.split(",");
            for (int j = classes.length - 1; j >= 0; j--) {
                pageClass = Class.forName(classes[j]);
                // Cerco un constructor con parametri (String, String): un dataset vero
                try {
                    constructor = pageClass.getConstructor(new Class[] { String.class, String.class });
                    // Istanzio la classe "base"
                    dataSet = (DataSet_itf) constructor.newInstance(new Object[] { configName, dataSetName });
                    continue;
                } catch (Throwable e) {
                    // Non ho trovato il constructor base, potrebbe essere un decorator, percio' proseguo
                }
                // Cerco il constructor del decorator con parametri (DataSet_itf)
                constructor = pageClass.getConstructor(new Class[] { DataSet_itf.class });
                dataSet = (DataSet_itf) constructor.newInstance(new Object[] { dataSet });

            }

            registerDataset(dataSet);

            return dataSet;
        } catch (ParamCrash pc) {
            pc.logContext("DataSetFactory", "configName = " + configName + "; dataSetName = " + dataSetName
                    + "; className = " + className);
            throw pc;
        } catch (Exception e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("DataSetFactory", "configName = " + configName + "; dataSetName = " + dataSetName
                    + "; className = " + className);
            throw ac;
        }
    }

    /**
     * Crea un WindowDataSet.
     * 
     * @param configName configName nome della configurazione
     * @param dataSetName dataSetName nome del DataSet da creare
     * 
     * @return net.project.dataset.WindowDataSet_itf
     * @throws AppCrash
     */
    public WindowDataSet_itf makeWindowDataSet(String configName, String dataSetName) throws AppCrash {

        int windowSize = 0;

        try {
            SizeableDataSet_itf sizeableDs = (SizeableDataSet_itf) makeDataSet(configName, dataSetName);
            windowSize = Integer.parseInt(Config.GetInstance(configName).getProperty(
                    "DS." + dataSetName + ".WindowSize"));
            ErrDetector.GetInstance().invariant(windowSize > 0);
            return makeWindowDatasetDecorator(sizeableDs, windowSize);

        } catch (ParamCrash pc) {
            pc.logContext("DataSetFactory", "configName = " + configName + "; dataSetName = " + dataSetName);
            throw pc;
        } catch (Exception e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("DataSetFactory", "configName = " + configName + "; dataSetName = " + dataSetName);
            throw ac;
        }
    }

    /**
     * Crea un WindowDataSetDecorator per gestire il dataset con pagine.
     *
     * @param sizeableDs net.project.dataset.WindowDataSetDecorator Il sizeable dataset da decorare.
     * @param size int La dimensione delle pagine in cui suddividere il dataset.
     * @return net.project.dataset.WindowDataSetDecorator Il dataset paginato.
     * @exception net.project.errors.AppCrash
     */
    public WindowDataSetDecorator makeWindowDatasetDecorator(SizeableDataSet_itf sizeableDs, int size) throws AppCrash {

        try {
            WindowDataSetDecorator windowDsDecor = new WindowDataSetDecorator(sizeableDs, size);
            registerDataset(windowDsDecor);
            return windowDsDecor;
        } catch (AppCrash ac) {
            ac.logContext("DataSetFactory", "Errore nella creazione del WindowDataSetDecorator");
            throw ac;
        }
    }

    /**
     * Questo metodo chiude tutti i dataset del Thread chiamante non esplicitamente chiusi dalla applicazione
     */
    public void closeAllThreadDataSets() {

        DataSetRegister reg = (DataSetRegister) _register.get();
        Iterator datas = reg.getAllThreadDataSets();

        while (datas.hasNext()) {
            DataSet_itf ds = (DataSet_itf) datas.next();

            try {
                ds.close();
            } catch (Throwable t) {
                Logger.GetInstance().log0("Errore nella closeAllThreadDataSets");
            } finally {
                datas.remove();
            }
        }
    }

    /**
     * Questo metodo serve per "registrare" un dataset come aperto
     */
    public void registerDataset(DataSet_itf ds) {

        if (_registerNeeded == false) return;

        DataSetRegister reg = (DataSetRegister) _register.get();
        reg.add(ds);
    }

}
