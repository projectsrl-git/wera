
package net.projectsrl.wera.base.db;

import java.sql.Timestamp;
import java.util.HashMap;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public abstract class AliModDettagliDAO_base extends PjNDAO_base {

    private static final String DS_PROGRESSIVO_MODULO = "DSProgressivoALIMOD";
    
    private static final String PARAM_TABLE_NAME = "TABLE_NAME";
    private static final String PARAM_FIELD_NAME = "FIELD_NAME";

    public static final String  ID_DETTAGLIO     = "ID_DETTAGLIO";
    public static final String  NR_DETTAGLIO     = "NR_DETTAGLIO";
    public static final String  ID_MODULO        = "ID_MODULO";

    public static final String  TS_INS           = "TS_INS";
    public static final String  ID_UTENTE_INS    = "ID_UTENTE_INS";
    public static final String  TS_MOD           = "TS_MOD";
    public static final String  ID_UTENTE_MOD    = "ID_UTENTE_MOD";

    
    public AliModDettagliDAO_base(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    public AliModDettagliDAO_base(String tableName, String configName) throws AppCrash {

        super(tableName, configName);

    }

    public AliModDettagliDAO_base(String tableName) throws AppCrash {

        super(tableName);

    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_MODULO, Integer.class);
        addNoStringField(ID_DETTAGLIO, Integer.class);
        addNoStringField(TS_INS, Timestamp.class);
        addNoStringField(ID_UTENTE_INS, Integer.class);
        addNoStringField(TS_MOD, Timestamp.class);
        addNoStringField(ID_UTENTE_MOD, Integer.class);
    }

    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_DETTAGLIO))) {
            appendField(ID_DETTAGLIO, whereCondition);
        } else if (Util.IsNotEmpty(getAttribute(NR_DETTAGLIO))) {
            appendField(NR_DETTAGLIO, whereCondition);
        }

        return whereCondition;
    }


    @Override
    public void insert() throws AppCrash {

        String nr = getNextSequentialNumber();
        setAttribute(NR_DETTAGLIO, nr);

        super.insert();
        super.retrieve();
    }

    private String getNextSequentialNumber() throws AppCrash {

        String dsName = DS_PROGRESSIVO_MODULO;

        DataSet_itf dataSet = null;

        DataSetFactory dsFactory = DataSetFactory.getInstance();
        Integer lastSequentialNumeber = new Integer(0);

        try {
            dataSet = dsFactory.makeDataSet("", dsName);

            HashMap<String, String> param = new HashMap<String, String>();
            param.put(PARAM_TABLE_NAME, getTableName());
            param.put(PARAM_FIELD_NAME, NR_DETTAGLIO);
            dataSet.setParam(param);
            dataSet.open();

            if (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                if (dbRow != null) {
                    String strLast = (String) dbRow.getField("ultimo");
                    if (strLast != null && !strLast.trim().equals("")) {
                        lastSequentialNumeber = Integer.valueOf(strLast);
                        lastSequentialNumeber += 1;
                    }
                }
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),
                    "Errore nella ricerca dell'ultimo progressivo del dataset " + dsName);
            throw ac;
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + dsName);
                }
            }
        }

        if (lastSequentialNumeber == null || lastSequentialNumeber.intValue() == 0) {
            lastSequentialNumeber = new Integer(1);
        }

        return String.format("%05d", lastSequentialNumeber);

    }

}
