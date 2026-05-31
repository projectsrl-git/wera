
package net.projectsrl.dafne.db;

import java.util.HashMap;
import java.util.Map;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.db.DBTransaction;
import net.project.db.NDAO_base;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.webapp.core.DeleteQueryRows;
import net.projectsrl.webapp.core.ForeingnKeysRows_itf;

public class PasswordDAO extends PjNDAO_base implements ForeingnKeysRows_itf {

    private static final String DATA_SET_PASSWORD_UTENTI = "DataSetPasswordUtenti";

    private static final String TABLE_NAME  = "PASSWORD";

    public static final String  ID_PASSWORD = "ID_PASSWORD";
    public static final String  ID_UTENTE   = "ID_UTENTE";
    public static final String  PASSWORD    = "PASSWORD";
    public static final String  DT_SCADENZA = "DT_SCADENZA";
    public static final String  FL_VALIDA   = "FL_VALIDA";
    public static final String  TS_INS      = "TS_INS";

    public PasswordDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public PasswordDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public PasswordDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);

    }
    
    private class DeletePasswordUtente extends DeleteQueryRows<PasswordDAO, Integer> {

        public DeletePasswordUtente() {

            super(DATA_SET_PASSWORD_UTENTI, ID_UTENTE, ID_PASSWORD);

        }

    }
    

    /**
     * Questo metodo
     * 
     * @return
     * @throws AppCrash
     * 
     * @see net.ssb.db.NDAO_base#whereCondition()
     */
    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_UTENTE))) {
            appendField(ID_UTENTE, whereCondition);
            setAttribute(FL_VALIDA, Boolean.TRUE);
            appendField(FL_VALIDA, whereCondition);
        } else {
            appendField(ID_PASSWORD, whereCondition);
        }

        return whereCondition;
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_PASSWORD, Integer.class);
        addNoStringField(ID_UTENTE, Integer.class);
        addNoStringField(FL_VALIDA, Boolean.class);

    }

    @Override
    public void insert() throws AppCrash {
        
        invalidOldPassword();

        setAttribute(FL_VALIDA, Boolean.TRUE);
        super.insert();

    }

    protected void invalidOldPassword() throws AppCrash {

        Integer idUtente = (Integer) getAttribute(ID_UTENTE);

        ErrDetector.GetInstance().preCond(Util.IsNotEmpty(idUtente), ID_UTENTE + " is empty");

        DataSet_itf dataSet = null;
        String dsName = DATA_SET_PASSWORD_UTENTI;

        DataSetFactory dsFactory = DataSetFactory.getInstance();

        try {
            dataSet = dsFactory.makeDataSet("", DATA_SET_PASSWORD_UTENTI);

            Map<String, Object> parametri = new HashMap<String, Object>();
            parametri.put(ID_UTENTE, idUtente.toString());
            dataSet.setParam(parametri);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                Integer idPassword = (Integer) dbRow.getField(ID_PASSWORD);
                NDAO_base password=new PasswordDAO();
                password.setAttribute(ID_PASSWORD, idPassword);
                password.setAttribute(FL_VALIDA, false);
                password.update();
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(), "Errore nella ricerca del max del dataset " + dsName);
            throw ac;
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext("Utils.getProssimoCodice", "Errore nella close del dataset " + dsName);
                }
            }
        }
    }

    @Override
    public void delete(String searchKeyValue) throws AppCrash {

        new DeletePasswordUtente().delete(searchKeyValue);
        
    }

}