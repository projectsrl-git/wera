
package net.projectsrl.wm.db;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.projectsrl.db.PjDAO_base;

/**
 * Classe che rappresenta la tabella APPROVATORI Proprietà lette dal file di configurazione:
 * <p>
 * DATABASE.NAME = nome del database
 */
public class ApprovatoriDAO extends PjDAO_base {

    private static final String NOME_TABELLA        = "APPROVATORI";

    public static final String  ID_GRUPPO            = "ID_GRUPPO";
    public static final String  AZIENDA            	 = "AZIENDA";
    public static final String  NOME_GRUPPO          = "NOME_GRUPPO";
    public static final String  A_1     = "A_1";
    public static final String  A_2     = "A_2";
    public static final String  A_3     = "A_3";
    public static final String  A_4     = "A_4";
    public static final String  A_5     = "A_5";

    public ApprovatoriDAO() throws AppCrash {

        super(NOME_TABELLA);
        setUniqueIdentifier(ID_GRUPPO);
    }

    public ApprovatoriDAO(DBTransaction transact) throws AppCrash {

        super(transact, NOME_TABELLA);
        setUniqueIdentifier(ID_GRUPPO);
    }

    public ApprovatoriDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
        setUniqueIdentifier(ID_GRUPPO);

    }

}
