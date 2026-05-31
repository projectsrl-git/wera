
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

public class StaffDirezioniDAO extends PjNDAO_base implements ForeingnKeysRows_itf {

    private static final String TABLE_NAME               = "STAFF_DIREZIONI";
    private static final String DATA_SET_STAFF_DIREZIONI = "DSStaffDirezioni";

    public static final String  ID_STAFF_DIREZIONE       = "ID_STAFF_DIREZIONE";
    public static final String  ID_DIREZIONE             = "ID_DIREZIONE";
    public static final String  ID_RISORSA_AZIENDA       = "ID_RISORSA_AZIENDA";

    public StaffDirezioniDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public StaffDirezioniDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public StaffDirezioniDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);

    }

    private class DeleteStaffDirezioni extends DeleteQueryRows<StaffDirezioniDAO, Integer> {

        public DeleteStaffDirezioni() {
            super(DATA_SET_STAFF_DIREZIONI, StaffDirezioniDAO.ID_DIREZIONE, StaffDirezioniDAO.ID_STAFF_DIREZIONE);

        }

    }

    private class InsertStaffDirezioni extends InsertMultipleReferences<StaffDirezioniDAO> {

        public InsertStaffDirezioni() {
            super(DATA_SET_STAFF_DIREZIONI, ID_DIREZIONE, ID_RISORSA_AZIENDA);
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

        if (Util.IsNotEmpty(getAttribute(ID_STAFF_DIREZIONE))) {
            appendField(ID_STAFF_DIREZIONE, whereCondition);
        }

        return whereCondition;

    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_STAFF_DIREZIONE, Integer.class);
        addNoStringField(ID_DIREZIONE, Integer.class);
        addNoStringField(ID_RISORSA_AZIENDA, Integer.class);

    }

    public void insert(String codeList, Integer keyId) throws AppCrash {

        new InsertStaffDirezioni().insert(codeList, keyId);

    }

    @Override
    public void delete(String idDirezione) throws AppCrash {

        new DeleteStaffDirezioni().delete(idDirezione);

    }

    public Object getSelectedCodeList(String idDirezione) throws AppCrash {

        return new InsertStaffDirezioni().getSelectedCodeList(idDirezione);
    }

}