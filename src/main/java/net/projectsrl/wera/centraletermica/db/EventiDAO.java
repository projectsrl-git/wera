package net.projectsrl.wera.centraletermica.db;

import java.sql.Timestamp;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

/**
 * DAO di CENTRALE_TERMICA.EVENTI: registro append-only (trigger sul DB contro update e delete).
 */
public class EventiDAO extends PjNDAO_base {

    private static final String TABLE_NAME                  = "CENTRALE_TERMICA.EVENTI";

    public static final String  ID_EVENTO                   = "ID_EVENTO";
    public static final String  TS_EVENTO                   = "TS_EVENTO";
    public static final String  TIPO_EVENTO                 = "TIPO_EVENTO";
    public static final String  ID_UTENTE                   = "ID_UTENTE";
    public static final String  CODICE_PROFILO              = "CODICE_PROFILO";
    public static final String  ID_AZIENDA                  = "ID_AZIENDA";
    public static final String  ID_CONDOMINIO               = "ID_CONDOMINIO";
    public static final String  ID_DOCUMENTO                = "ID_DOCUMENTO";
    public static final String  ID_VERSIONE                 = "ID_VERSIONE";
    public static final String  ID_SCADENZA                 = "ID_SCADENZA";
    public static final String  ID_FILE_WERA                = "ID_FILE_WERA";
    public static final String  DETTAGLIO                   = "DETTAGLIO";

    public EventiDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public EventiDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_EVENTO, Long.class);
        addNoStringField(TS_EVENTO, Timestamp.class);
        addNoStringField(ID_UTENTE, Integer.class);
        addNoStringField(ID_AZIENDA, Integer.class);
        addNoStringField(ID_CONDOMINIO, Integer.class);
        addNoStringField(ID_DOCUMENTO, Integer.class);
        addNoStringField(ID_VERSIONE, Integer.class);
        addNoStringField(ID_SCADENZA, Integer.class);
        addNoStringField(ID_FILE_WERA, Integer.class);
    }

    @Override
    public void update() throws AppCrash {

        throw new AppCrash("CENTRALE_TERMICA.EVENTI e' append-only: update non ammesso");
    }

    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_EVENTO))) {
            appendField(ID_EVENTO, whereCondition);
        }

        return whereCondition;
    }
}
