
package it.project.iride.db;

import net.project.db.DAO_base;
import net.project.db.DBTransaction;
import net.project.errors.AppCrash;

/**
 * Classe che rappresenta la tabella LOG_OPER con il log delle operazioni eseguite dall'utente:
 * <p>
 * DATABASE.NAME = nome del database
 */
public class LogDAO extends DAO_base {

    private static final String NOME_TABELLA = "LOG_USER";

    public static final String  OPERATORE    = "OPERATORE";
    public static final String  DESCRI       = "DESCRI";
    public static final String  DATA         = "DATA";
    public static final String  ORA          = "ORA";
    public static final String  OPERAZIONE   = "OPERAZIONE";
    public static final String  HTTP_PARAMS  = "HTTP_PARAMS";

    public LogDAO() throws AppCrash {

        super(NOME_TABELLA);
    }

    public LogDAO(DBTransaction transact) throws AppCrash {

        super(transact, NOME_TABELLA);
    }

    public LogDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);

    }

    @Override
    protected String whereCondition() throws AppCrash {

        return null;
    }

}
