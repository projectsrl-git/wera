
package net.projectsrl.wm.db;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.projectsrl.db.PjDAO_base;

/**
 * Classe che rappresenta la tabella VISITATORI Proprietà lette dal file di configurazione:
 * <p>
 * DATABASE.NAME = nome del database
 */
public class ImpostazioniBaseDAO extends PjDAO_base {

    private static final String NOME_TABELLA        = "IMPOSTAZIONI_BASE";

    public static final String  AZIENDA            = "AZIENDA";
    public static final String  COMMESSE            = "COMMESSE";
    public static final String  T_STRAORD     = "T_STRAORD";

    public ImpostazioniBaseDAO() throws AppCrash {

        super(NOME_TABELLA);
        setUniqueIdentifier(AZIENDA);
    }

    public ImpostazioniBaseDAO(DBTransaction transact) throws AppCrash {

        super(transact, NOME_TABELLA);
        setUniqueIdentifier(AZIENDA);
    }

    public ImpostazioniBaseDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
        setUniqueIdentifier(AZIENDA);

    }

}
