package net.projectsrl.wera.centraletermica.db;

import java.sql.Timestamp;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

/**
 * DAO di CENTRALE_TERMICA.VERSIONI: un file caricato. Le righe sono immutabili (trigger sul DB):
 * si usano solo insert e retrieve.
 */
public class VersioniDAO extends PjNDAO_base {

    private static final String TABLE_NAME          = "CENTRALE_TERMICA.VERSIONI";

    public static final String  ID_VERSIONE         = "ID_VERSIONE";
    public static final String  ID_DOCUMENTO        = "ID_DOCUMENTO";
    public static final String  NUMERO_VERSIONE     = "NUMERO_VERSIONE";
    public static final String  NOME_FILE_ORIGINALE = "NOME_FILE_ORIGINALE";
    public static final String  PERCORSO_FILE       = "PERCORSO_FILE";
    public static final String  TIPO_MIME           = "TIPO_MIME";
    public static final String  DIMENSIONE_BYTE     = "DIMENSIONE_BYTE";
    public static final String  IMPRONTA_SHA256     = "IMPRONTA_SHA256";
    public static final String  NOTA                = "NOTA";
    public static final String  TS_INS              = "TS_INS";
    public static final String  ID_UTENTE_INS       = "ID_UTENTE_INS";

    public VersioniDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public VersioniDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_VERSIONE, Integer.class);
        addNoStringField(ID_DOCUMENTO, Integer.class);
        addNoStringField(NUMERO_VERSIONE, Integer.class);
        addNoStringField(DIMENSIONE_BYTE, Long.class);
        addNoStringField(TS_INS, Timestamp.class);
        addNoStringField(ID_UTENTE_INS, Integer.class);
    }

    @Override
    public void update() throws AppCrash {

        throw new AppCrash("CENTRALE_TERMICA.VERSIONI e' immutabile: update non ammesso");
    }

    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_VERSIONE))) {
            appendField(ID_VERSIONE, whereCondition);
        } else if (Util.IsNotEmpty(getAttribute(PERCORSO_FILE))) {
            appendField(PERCORSO_FILE, whereCondition);
        }

        return whereCondition;
    }
}
