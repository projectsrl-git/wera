
package net.projectsrl.dafne.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.webapp.core.DeleteQueryRows;
import net.projectsrl.webapp.core.ForeingnKeysRows_itf;
import net.projectsrl.webapp.core.InsertMultipleReferences;

public class UtentiProfiliDAO extends PjNDAO_base implements ForeingnKeysRows_itf {

    private static final String TABLE_NAME              = "UTENTI_PROFILI";
    private static final String DATA_SET_PROFILI_UTENTE = "DataSetProfiliUtente";

    public static final String  ID_UTENTE_PROFILO       = "ID_UTENTE_PROFILO";
    public static final String  ID_UTENTE               = "ID_UTENTE";
    public static final String  ID_PROFILO              = "ID_PROFILO";

    public UtentiProfiliDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public UtentiProfiliDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    private class DeleteProfiliUtente extends DeleteQueryRows<UtentiProfiliDAO, Integer> {

        public DeleteProfiliUtente() {
            super(DATA_SET_PROFILI_UTENTE, UtentiProfiliDAO.ID_UTENTE, UtentiProfiliDAO.ID_UTENTE_PROFILO);

        }

    }

    private class InsertUtentiProfili extends InsertMultipleReferences<UtentiProfiliDAO> {

        public InsertUtentiProfili() {
            super(DATA_SET_PROFILI_UTENTE, ID_UTENTE, ID_PROFILO);
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

        if (Util.IsNotEmpty(getAttribute(ID_UTENTE_PROFILO))) {
            appendField(ID_UTENTE_PROFILO, whereCondition);
        }

        return whereCondition;

    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_UTENTE_PROFILO, Integer.class);
        addNoStringField(ID_UTENTE, Integer.class);
        addNoStringField(ID_PROFILO, Integer.class);

    }

    public void insert(String codeList, Integer keyId) throws AppCrash {

        new InsertUtentiProfili().insert(codeList, keyId);

    }

    @Override
    public void delete(String idUtente) throws AppCrash {

        new DeleteProfiliUtente().delete(idUtente);

    }

    public Object getSelectedCodeList(String idUtente) throws AppCrash {

        return new InsertUtentiProfili().getSelectedCodeList(idUtente);
    }

}