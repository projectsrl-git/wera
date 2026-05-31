
package net.projectsrl.dafne.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class AziendeDAO extends PjNDAO_base {

    private static final String TABLE_NAME    = "AZIENDE";

    public static final String  ID_AZIENDA    = "ID_AZIENDA";
    public static final String  CODICE        = "CODICE";
    public static final String  CODICE_PARENT = "CODICE_PARENT";
    public static final String  RAGSOC        = "RAGSOC";
    public static final String  RAGRID        = "RAGRID";
    public static final String  SETTORE       = "SETTORE";
    public static final String  GRUPPI        = "GRUPPI";
    public static final String  TAGGANCIO     = "TAGGANCIO";
    public static final String  CODFISCALE_AZ = "CODFISCALE_AZ";
    public static final String  PARTITAIVA    = "PARTITAIVA";

    public static final String  COGNOME       = "COGNOME";
    public static final String  NOME          = "NOME";
    public static final String  SESSO         = "SESSO";
    public static final String  CODFISCALE    = "CODFISCALE";
    public static final String  LOCNASCITA    = "LOCNASCITA";
    public static final String  D_NASCITA     = "D_NASCITA";
    public static final String  INAIL         = "INAIL";

    public AziendeDAO() throws AppCrash {
        super(TABLE_NAME);
    }

    public AziendeDAO(DBTransaction transact) throws AppCrash {
        super(transact, TABLE_NAME);
    }

    public AziendeDAO(DBTransaction transact, String tableName) throws AppCrash {
        super(transact, tableName);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_AZIENDA, Integer.class);

    }

    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_AZIENDA))) {
            appendField(ID_AZIENDA, whereCondition);
        } else if (Util.IsNotEmpty(getAttribute(CODICE))) {
            appendField(CODICE, whereCondition);
        }

        return whereCondition;
    }
}
