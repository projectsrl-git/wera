
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
import net.project.misc.Util;

public class InsertMultipleReferences<R extends NDAO_base> {

    private static final String LIST_SEPARATOR = ";";

    private final String _dsName;

    private final String _foreingKeyFieldName, _referenceFieldName;

    public InsertMultipleReferences(String dsName, String foreingKeyFieldName, String referenceFieldName) {
        super();

        _dsName = dsName;
        _foreingKeyFieldName = foreingKeyFieldName;
        _referenceFieldName = referenceFieldName;

    }

    public void insert(String codeList, Integer keyId) throws AppCrash {

        String[] selected = codeList.split(LIST_SEPARATOR);
        for (int i = 0; i < selected.length; i++) {
            Integer attributeId = (Integer) new Integer(selected[i]);

            R referenceDAO;
            try {
                referenceDAO = (R) getTypeParameterClass().newInstance();
                referenceDAO.setAttribute(_referenceFieldName, attributeId);
                referenceDAO.setAttribute(_foreingKeyFieldName, keyId);
                referenceDAO.insert();
            } catch (Throwable e) {
                AppCrash ac = new AppCrash();
                ac.logContext(this.getClass().getName(), "foreingKeyFieldName:" + _foreingKeyFieldName + " - codeList:"
                        + codeList + " - referenceFieldName:" + _referenceFieldName + " - keyId:" + keyId);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Class<R> getTypeParameterClass() {

        Type type = getClass().getGenericSuperclass();
        ParameterizedType paramType = (ParameterizedType) type;
        return (Class<R>) paramType.getActualTypeArguments()[0];
    }

    public String getSelectedCodeList(String keyId) throws AppCrash {

        DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();

        String selectedList = "";
        try {
            dataSet = dsFactory.makeDataSet("", _dsName);

            Map<String, Object> parametri = new HashMap<String, Object>();
            parametri.put(_foreingKeyFieldName, keyId);
            dataSet.setParam(parametri);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                if (Util.IsNotEmpty(selectedList)) {
                    selectedList += LIST_SEPARATOR;
                }

                selectedList += dbRow.getField(_referenceFieldName);
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(), "Errore nella ricerca del max del dataset " + _dsName);
            throw ac;
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext("Utils.getProssimoCodice", "Errore nella close del dataset " + _dsName);
                }
            }
        }
        return selectedList;
    }

}
