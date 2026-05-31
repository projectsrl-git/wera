
package net.projectsrl.bow.parameters;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class ParametriAziendaDAO extends PjNDAO_base {

    private static final String TABLE_NAME           = "PARAMETRI_AZIENDA";

    public static final String  ID_PARAMETRO_AZIENDA = "ID_PARAMETRO_AZIENDA";
    public static final String  ID_PARAMETRO         = "ID_PARAMETRO";
    public static final String  ID_AZIENDA           = "ID_AZIENDA";

    public ParametriAziendaDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public ParametriAziendaDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public ParametriAziendaDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_PARAMETRO_AZIENDA, Integer.class);
        addNoStringField(ID_PARAMETRO, Integer.class);
        addNoStringField(ID_AZIENDA, Integer.class);
    }

    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_PARAMETRO_AZIENDA))) {
            appendField(ID_PARAMETRO_AZIENDA, whereCondition);
        }

        return whereCondition;
    }
}
