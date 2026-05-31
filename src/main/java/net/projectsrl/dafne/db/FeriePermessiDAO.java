
package net.projectsrl.dafne.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class FeriePermessiDAO extends PjNDAO_base {

    private static final String TABLE_NAME       = "FERIE_PERMESSI";

    public static final String  ID_FERIEPERMESSO = "ID_FERIEPERMESSO";
    public static final String  ID_RISORSA       = "ID_RISORSA";
    public static final String  ID_APPROVATORE   = "ID_APPROVATORE";
    public static final String  ID_AZIENDA       = "ID_AZIENDA";
    public static final String  DIPENDENTE       = "DIPENDENTE";
    public static final String  TIPO_PERMESSO    = "TIPO_PERMESSO";
    public static final String  DATA_DAL         = "DATA_DAL";
    public static final String  DATA_AL          = "DATA_AL";
    public static final String  ORE_DAL          = "ORE_DAL";
    public static final String  ORE_AL           = "ORE_AL";
    public static final String  ORE              = "ORE";
    public static final String  REVOCATO         = "REVOCATO";
    public static final String  AZIENDA_TENDINA  = "AZIENDA_TENDINA";
    public static final String  APPROVAZIONE     = "APPROVAZIONE";
    public static final String  NOTE             = "NOTE";
    public static final String  DOCUMENTAZIONE   = "DOCUMENTAZIONE";
    public static final String  ADMIN            = "ADMIN";

    public FeriePermessiDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public FeriePermessiDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public FeriePermessiDAO(DBTransaction transact, String tableName) throws AppCrash {

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

        if (Util.IsNotEmpty(getAttribute(ID_FERIEPERMESSO))) {
            appendField(ID_FERIEPERMESSO, whereCondition);
        }

        return whereCondition;
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_FERIEPERMESSO, Integer.class);
        addNoStringField(ID_RISORSA, Integer.class);
        addNoStringField(ID_APPROVATORE, Integer.class);
        addNoStringField(ID_AZIENDA, Integer.class);
    }
}