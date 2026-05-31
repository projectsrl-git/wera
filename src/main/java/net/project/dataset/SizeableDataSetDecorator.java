
package net.project.dataset;

import net.project.errors.AppCrash;

/**
 * Questa classe e' un decorator per oggetti di tipo SizeableDataSet_itf. Tutte le chiamate ai suoi metodi vengono
 * inoltrate all'oggetto che decora. Serve come base per costruire decorator personalizzati
 */
public class SizeableDataSetDecorator extends DataSetDecorator implements SizeableDataSet_itf {

    public SizeableDataSetDecorator(DataSet_itf ds) {

        super(ds);
    }

    /**
     * Ritorna il numero di righe presenti nel dataset corrente.
     * 
     * @return int
     * @exception net.project.errors.AppCrash
     */
    @Override
    public int getNumberOfRows() throws AppCrash {

        return ((SizeableDataSet_itf) getOriginalDataset()).getNumberOfRows();
    }
}
