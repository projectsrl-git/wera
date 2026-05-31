
package net.projectsrl.wm.db;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.projectsrl.db.PjDAO_base;

/**
 * Classe che rappresenta la tabella APPROVATORI Proprietà lette dal file di configurazione:
 * <p>
 * DATABASE.NAME = nome del database
 */
public class ApprovazioniDAO extends PjDAO_base {

    private static final String NOME_TABELLA        = "APPROVAZIONI";

    public static final String  ID_GRUPPO        = "ID_GRUPPO";
    public static final String  AZIENDA        = "AZIENDA";
    public static final String  LIVELLO_1    	 = "LIVELLO_1";
    public static final String  LIVELLO_2     	 = "LIVELLO_2";
    public static final String  LIVELLO_3     	 = "LIVELLO_3";
    public static final String  LIVELLO_4    	 = "LIVELLO_4";
    public static final String  LIVELLO_5    	 = "LIVELLO_5";

    public ApprovazioniDAO() throws AppCrash {

        super(NOME_TABELLA);
        setUniqueIdentifier(ID_GRUPPO);
    }

    public ApprovazioniDAO(DBTransaction transact) throws AppCrash {

        super(transact, NOME_TABELLA);
        setUniqueIdentifier(ID_GRUPPO);
    }

    public ApprovazioniDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
        setUniqueIdentifier(ID_GRUPPO);

    }

}
