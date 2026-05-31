
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

public class UtentiAziendeDAO extends PjNDAO_base implements ForeingnKeysRows_itf {

    private static final String TABLE_NAME              = "UTENTI_AZIENDE";
    private static final String DATA_SET_AZIENDE_UTENTE = "DataSetAziendeUtente";

    public static final String  ID_UTENTE_AZIENDA       = "ID_UTENTE_AZIENDA";
    public static final String  ID_UTENTE               = "ID_UTENTE";
    public static final String  ID_AZIENDA              = "ID_AZIENDA";

    public UtentiAziendeDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public UtentiAziendeDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public UtentiAziendeDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);

    }

    private class DeleteAziendeUtente extends DeleteQueryRows<UtentiAziendeDAO, Integer> {

        public DeleteAziendeUtente() {
            super(DATA_SET_AZIENDE_UTENTE, UtentiAziendeDAO.ID_UTENTE, UtentiAziendeDAO.ID_UTENTE_AZIENDA);

        }

    }

    private class InsertUtentiAziende extends InsertMultipleReferences<UtentiAziendeDAO> {

        public InsertUtentiAziende() {
            super(DATA_SET_AZIENDE_UTENTE, ID_UTENTE, ID_AZIENDA);
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

        if (Util.IsNotEmpty(getAttribute(ID_UTENTE_AZIENDA))) {
            appendField(ID_UTENTE_AZIENDA, whereCondition);
        }

        return whereCondition;

    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_UTENTE_AZIENDA, Integer.class);
        addNoStringField(ID_UTENTE, Integer.class);
        addNoStringField(ID_AZIENDA, Integer.class);

    }

    public void insert(String aziende, Integer idUtente) throws AppCrash {

        new InsertUtentiAziende().insert(aziende, idUtente);

    }

    public String getSelectedCodeList(String idUtente) throws AppCrash {

        return new InsertUtentiAziende().getSelectedCodeList(idUtente);
    }

    @Override
    public void delete(String idUtente) throws AppCrash {

        new DeleteAziendeUtente().delete(idUtente);

    }

}