
package net.project.dataset;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * @author Simone
 */
public class DataSetRegister extends ThreadLocal {

    private Set _datasets = new HashSet();

    /**
     * Crea un nuovo oggetto di tipo DataSetRegister .
     */
    public DataSetRegister() {

    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public Object initialValue() {

        return new DataSetRegister();
    }

    /**
     * Il metodo add mette nel Set dei dataset aperti dal thread chiamante l'oggetto DataSet_itf passato.
     * 
     * @param dbConn la connessione da aggiungere a quelle usate
     */
    public void add(DataSet_itf ds) {

        _datasets.add(ds);
    }

    /**
     * Il metodo remove cancella dal Set l'oggetto DataSet_itf passato
     * 
     * @param ds la connessione non piu' usata
     */
    public void remove(DataSet_itf ds) {

        _datasets.remove(ds);
    }

    /**
     * Questo metodo serve per ottenere un un iterator con tutti i dataset che sono nel registro per il thread chiamante
     *
     * @return Iterator un iterator con tutte le connessioni che sono nel registro
     */
    public Iterator getAllThreadDataSets() {

        return _datasets.iterator();
    }

}
