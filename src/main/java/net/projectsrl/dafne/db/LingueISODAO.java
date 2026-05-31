
package net.projectsrl.dafne.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class LingueISODAO extends PjNDAO_base {

    private static final String TABLE_NAME    = "LINGUE_ISO";

    public static final String  ID_LINGUE_ISO = "ID_LINGUE_ISO";
    public static final String  CODICE_ISO    = "CODICE_ISO";
    public static final String  LINGUA        = "LINGUA";

    public LingueISODAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public LingueISODAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public LingueISODAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);

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

        if (Util.IsNotEmpty(getAttribute(ID_LINGUE_ISO))) {
            appendField(ID_LINGUE_ISO, whereCondition);
        } else if (Util.IsNotEmpty(getAttribute(CODICE_ISO))) {
            appendField(CODICE_ISO, whereCondition);
        }

        return whereCondition;

    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_LINGUE_ISO, Integer.class);

    }

}