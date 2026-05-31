
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

public class UtentiRisorseDAO extends PjNDAO_base implements ForeingnKeysRows_itf {

    private static final String TABLE_NAME              = "UTENTI_RISORSE";
    private static final String DATA_SET_RISORSE_UTENTE = "DataSetRisorseUtente";

    public static final String  ID_UTENTE_RISORSA       = "ID_UTENTE_RISORSA";
    public static final String  ID_UTENTE               = "ID_UTENTE";
    public static final String  ID_RISORSA              = "ID_RISORSA";

    public UtentiRisorseDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public UtentiRisorseDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public UtentiRisorseDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);

    }

    private class DeleteRisorseUtente extends DeleteQueryRows<UtentiRisorseDAO, Integer> {

        public DeleteRisorseUtente() {
            super(DATA_SET_RISORSE_UTENTE, UtentiRisorseDAO.ID_UTENTE, UtentiRisorseDAO.ID_UTENTE_RISORSA);

        }

    }

    private class InsertUtentiRisorse extends InsertMultipleReferences<UtentiRisorseDAO> {

        public InsertUtentiRisorse() {
            super(DATA_SET_RISORSE_UTENTE,ID_UTENTE, ID_RISORSA);
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

        if (Util.IsNotEmpty(getAttribute(ID_UTENTE_RISORSA))) {
            appendField(ID_UTENTE_RISORSA, whereCondition);
        }

        return whereCondition;

    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_UTENTE_RISORSA, Integer.class);
        addNoStringField(ID_UTENTE, Integer.class);
        addNoStringField(ID_RISORSA, Integer.class);

    }

    public void insert(String codeList, Integer keyId) throws AppCrash {

        new InsertUtentiRisorse().insert(codeList, keyId);

    }

    @Override
    public void delete(String idUtente) throws AppCrash {

        new DeleteRisorseUtente().delete(idUtente);

    }

    public Object getSelectedCodeList(String idUtente) throws AppCrash {

        return new InsertUtentiRisorse().getSelectedCodeList(idUtente);
    }

}