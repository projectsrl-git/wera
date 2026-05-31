
package net.projectsrl.alibow.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class AttributiAziendaDAO extends PjNDAO_base {
    
    private static final String TABLE_NAME           = "ATTRIBUTI_AZIENDA";

    public static final String  ID_ATTRIBUTO_AZIENDA = "ID_ATTRIBUTO_AZIENDA";
    public static final String  ID_AZIENDA           = "ID_AZIENDA";
    public static final String  CODICE_ATTRIBUTO     = "CODICE_ATTRIBUTO";
    public static final String  VALORE_ATTRIBUTO     = "VALORE_ATTRIBUTO";

    public AttributiAziendaDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public AttributiAziendaDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public AttributiAziendaDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_ATTRIBUTO_AZIENDA, Integer.class);
        addNoStringField(ID_AZIENDA, Integer.class);

    }

    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_ATTRIBUTO_AZIENDA))) {
            appendField(ID_ATTRIBUTO_AZIENDA, whereCondition);

        } else if (Util.IsNotEmpty(getAttribute(ID_AZIENDA)) && Util.IsNotEmpty(getAttribute(CODICE_ATTRIBUTO))) {
            appendField(ID_AZIENDA, whereCondition);
            appendField(CODICE_ATTRIBUTO, whereCondition);
        }

        return whereCondition;
    }
}
