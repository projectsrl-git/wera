
package net.projectsrl.dafne.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.webapp.core.DeleteQueryRows;
import net.projectsrl.webapp.core.ForeingnKeysRows_itf;

public class UtentiLingueDAO extends PjNDAO_base implements ForeingnKeysRows_itf {

    private static final String TABLE_NAME       = "UTENTI_LINGUE";

    public static final String  ID_UTENTI_LINGUE = "ID_UTENTI_LINGUE";
    public static final String  ID_UTENTE        = "ID_UTENTE";
    public static final String  ID_LINGUE_ISO    = "ID_LINGUE_ISO";
    public static final String  FL_DEFAULT       = "FL_DEFAULT";

    public UtentiLingueDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public UtentiLingueDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public UtentiLingueDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);

    }

    private class DeleteLingueUtente extends DeleteQueryRows<UtentiLingueDAO, Integer> {

        public DeleteLingueUtente() {

            super("DataSetLingueUtente", UtentiLingueDAO.ID_UTENTE, UtentiLingueDAO.ID_UTENTI_LINGUE);
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

        if (Util.IsNotEmpty(getAttribute(ID_UTENTI_LINGUE))) {
            appendField(ID_UTENTI_LINGUE, whereCondition);
        }

        return whereCondition;

    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_UTENTI_LINGUE, Integer.class);
        addNoStringField(ID_UTENTE, Integer.class);
        addNoStringField(ID_LINGUE_ISO, Integer.class);
        addNoStringField(FL_DEFAULT, Boolean.class);

    }

    @Override
    public void delete(String idUtente) throws AppCrash {

        new DeleteLingueUtente().delete(idUtente);

    }

}