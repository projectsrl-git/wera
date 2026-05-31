
package net.projectsrl.bow.configuration.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class ProfiliAziendaDAO extends PjNDAO_base {

    private static final String TABLE_NAME         = "PROFILI_AZIENDA";

    public static final String  ID_PROFILO_AZIENDA = "ID_PROFILO_AZIENDA";
    public static final String  ID_PROFILO         = "ID_PROFILO";
    public static final String  ID_AZIENDA         = "ID_AZIENDA";

    public ProfiliAziendaDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public ProfiliAziendaDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public ProfiliAziendaDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_PROFILO_AZIENDA, Integer.class);
        addNoStringField(ID_PROFILO, Integer.class);
        addNoStringField(ID_AZIENDA, Integer.class);
    }

    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_PROFILO_AZIENDA))) {
            appendField(ID_PROFILO_AZIENDA, whereCondition);
        }

        return whereCondition;
    }
}
