
package net.projectsrl.bow.configuration.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class DistributionListDAO extends PjNDAO_base {

    private static final String TABLE_NAME           = "DISTRIBUTION_LIST";

    public static final String  ID_DISTRIBUTION_LIST = "ID_DISTRIBUTION_LIST";
    public static final String  MODULO               = "MODULO";
    public static final String  ID_AZIENDA           = "ID_AZIENDA";
    public static final String  LISTA_MAIL           = "LISTA_MAIL";

    public DistributionListDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public DistributionListDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public DistributionListDAO(DBTransaction transact, String tableName) throws AppCrash {

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

        if (Util.IsNotEmpty(getAttribute(ID_DISTRIBUTION_LIST))) {
            appendField(ID_DISTRIBUTION_LIST, whereCondition);
        }

        return whereCondition;

    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_DISTRIBUTION_LIST, Integer.class);
        addNoStringField(ID_AZIENDA, Integer.class);

    }

}