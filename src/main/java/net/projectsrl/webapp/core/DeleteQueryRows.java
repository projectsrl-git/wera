
package net.projectsrl.webapp.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.db.NDAO_base;
import net.project.errors.AppCrash;

public class DeleteQueryRows<D extends NDAO_base, K> implements ForeingnKeysRows_itf{

    private final String _dsName;
    private final String _searchKeyFieldName;
    private final String _deleteKeyFieldName;
    
    public DeleteQueryRows(String dsName, String searchKeyFieldName, String deleteKeyFieldName) {
        super();
        _dsName = dsName;
        _searchKeyFieldName = searchKeyFieldName;
        _deleteKeyFieldName = deleteKeyFieldName;
    }

    @SuppressWarnings("unchecked")
    public void delete(String searchKeyValue) throws AppCrash {

        DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();

        try {
            dataSet = dsFactory.makeDataSet("", _dsName);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put(_searchKeyFieldName, searchKeyValue);
            dataSet.setParam(params);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                K keyValue = (K) dbRow.getField(_deleteKeyFieldName);
                D rowDAO = (D) getTypeParameterClass().newInstance();

                rowDAO.setAttribute(_deleteKeyFieldName, keyValue);
                rowDAO.delete();

            }
        } catch (Throwable th) {
            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "error using dataset " + _dsName);
            throw ac;
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "error closing dataset " + _dsName);
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    private Class<D> getTypeParameterClass() {

        Type type = getClass().getGenericSuperclass();
        ParameterizedType paramType = (ParameterizedType) type;
        return (Class<D>) paramType.getActualTypeArguments()[0];
    }

}
