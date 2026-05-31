
package net.projectsrl.dafne.db;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;

public class TableIdentity {

    private static final String ROW_ID                 = "ROW_ID";
    private final String        _tableName;
    private static final String DATASET_TABLE_IDENTITY = "DSTableIdentity";
    private static final String PARAM_TABLE            = "TABLE";

    public TableIdentity(String _tableName) {
        super();
        this._tableName = _tableName;
    }

    public synchronized Integer getTableIdentity() throws AppCrash {

        DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        Integer keyValue = null;

        try {
            dataSet = dsFactory.makeDataSet("", DATASET_TABLE_IDENTITY);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put(PARAM_TABLE, _tableName);
            dataSet.setParam(params);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                keyValue = (Integer) dbRow.getField(ROW_ID);

            }
        } catch (Throwable th) {
            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "error using dataset " + DATASET_TABLE_IDENTITY);
            throw ac;
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "error closing dataset " + DATASET_TABLE_IDENTITY);
                }
            }
        }
        return keyValue.intValue();
    }

}
