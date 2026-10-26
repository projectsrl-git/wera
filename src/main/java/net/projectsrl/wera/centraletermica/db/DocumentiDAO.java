package net.projectsrl.wera.centraletermica.db;

import java.sql.Timestamp;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

/**
 * DAO di CENTRALE_TERMICA.DOCUMENTI: il documento logico (titolo, sezione, data, stato).
 * I file stanno in VERSIONI. Le righe non si cancellano (trigger sul DB): si cambia lo stato.
 */
public class DocumentiDAO extends PjNDAO_base {

    private static final String TABLE_NAME           = "CENTRALE_TERMICA.DOCUMENTI";

    public static final String  ID_DOCUMENTO         = "ID_DOCUMENTO";
    public static final String  ID_AZIENDA           = "ID_AZIENDA";
    public static final String  ID_CONDOMINIO        = "ID_CONDOMINIO";
    public static final String  ID_SEZIONE           = "ID_SEZIONE";
    public static final String  TITOLO               = "TITOLO";
    public static final String  DESCRIZIONE          = "DESCRIZIONE";
    public static final String  DATA_DOCUMENTO       = "DATA_DOCUMENTO";
    public static final String  ID_VERSIONE_CORRENTE = "ID_VERSIONE_CORRENTE";
    public static final String  STATO                = "STATO";
    public static final String  TS_STATO             = "TS_STATO";
    public static final String  ID_UTENTE_STATO      = "ID_UTENTE_STATO";
    public static final String  NOTA_STATO           = "NOTA_STATO";
    public static final String  TS_INS               = "TS_INS";
    public static final String  ID_UTENTE_INS        = "ID_UTENTE_INS";
    public static final String  TS_MOD               = "TS_MOD";
    public static final String  ID_UTENTE_MOD        = "ID_UTENTE_MOD";

    public DocumentiDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public DocumentiDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_DOCUMENTO, Integer.class);
        addNoStringField(ID_AZIENDA, Integer.class);
        addNoStringField(ID_CONDOMINIO, Integer.class);
        addNoStringField(ID_SEZIONE, Integer.class);
        addNoStringField(DATA_DOCUMENTO, Timestamp.class);
        addNoStringField(ID_VERSIONE_CORRENTE, Integer.class);
        addNoStringField(TS_STATO, Timestamp.class);
        addNoStringField(ID_UTENTE_STATO, Integer.class);
        addNoStringField(TS_INS, Timestamp.class);
        addNoStringField(ID_UTENTE_INS, Integer.class);
        addNoStringField(TS_MOD, Timestamp.class);
        addNoStringField(ID_UTENTE_MOD, Integer.class);
    }

    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_DOCUMENTO))) {
            appendField(ID_DOCUMENTO, whereCondition);
        }

        return whereCondition;
    }
}
