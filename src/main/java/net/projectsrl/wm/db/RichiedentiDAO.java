
package net.projectsrl.wm.db;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.projectsrl.db.PjDAO_base;

/**
 * Classe che rappresenta la tabella APPROVATORI Proprietà lette dal file di configurazione:
 * <p>
 * DATABASE.NAME = nome del database
 */
public class RichiedentiDAO extends PjDAO_base {

    private static final String NOME_TABELLA        = "RICHIEDENTI";

    public static final String  ID_GRUPPO            = "ID_GRUPPO";
    public static final String  AZIENDA            	 = "AZIENDA";
    public static final String  NOME_GRUPPO          = "NOME_GRUPPO";
   

    public RichiedentiDAO() throws AppCrash {

        super(NOME_TABELLA);
        setUniqueIdentifier(ID_GRUPPO);
    }

    public RichiedentiDAO(DBTransaction transact) throws AppCrash {

        super(transact, NOME_TABELLA);
        setUniqueIdentifier(ID_GRUPPO);
    }

    public RichiedentiDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
        setUniqueIdentifier(ID_GRUPPO);

    }

}
