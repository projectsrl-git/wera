
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

public class UtentiUtenzeDAO extends PjNDAO_base implements ForeingnKeysRows_itf {

    private static final String TABLE_NAME              = "UTENTI_UTENZE";
    private static final String DATA_SET_UTENZE_UTENTE = "DataSetUtenzeUtente";

    public static final String  ID_UTENTE_UTENZA       = "ID_UTENTE_UTENZA";
    public static final String  ID_UTENTE               = "ID_UTENTE";
    public static final String  ID_UTENZA              = "ID_UTENZA";

    public UtentiUtenzeDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public UtentiUtenzeDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    private class DeleteUtenzeUtente extends DeleteQueryRows<UtentiUtenzeDAO, Integer> {

        public DeleteUtenzeUtente() {
            super(DATA_SET_UTENZE_UTENTE, UtentiUtenzeDAO.ID_UTENTE, UtentiUtenzeDAO.ID_UTENTE_UTENZA);

        }

    }

    private class InsertUtentiUtenze extends InsertMultipleReferences<UtentiUtenzeDAO> {

        public InsertUtentiUtenze() {
            super(DATA_SET_UTENZE_UTENTE, ID_UTENTE, ID_UTENZA);
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

        if (Util.IsNotEmpty(getAttribute(ID_UTENTE_UTENZA))) {
            appendField(ID_UTENTE_UTENZA, whereCondition);
        }

        return whereCondition;

    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_UTENTE_UTENZA, Integer.class);
        addNoStringField(ID_UTENTE, Integer.class);
        addNoStringField(ID_UTENZA, Integer.class);

    }

    public void insert(String codeList, Integer keyId) throws AppCrash {

        new InsertUtentiUtenze().insert(codeList, keyId);

    }

    @Override
    public void delete(String idUtente) throws AppCrash {

        new DeleteUtenzeUtente().delete(idUtente);

    }

    public Object getSelectedCodeList(String idUtente) throws AppCrash {

        return new InsertUtentiUtenze().getSelectedCodeList(idUtente);
    }

}