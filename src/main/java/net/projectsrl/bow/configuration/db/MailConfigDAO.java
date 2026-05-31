
package net.projectsrl.bow.configuration.db;

import java.sql.Timestamp;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class MailConfigDAO extends PjNDAO_base {

    private static final String TABLE_NAME      = "MAIL_CONFIG";

    public static final String  ID_MAIL_CONFIG  = "ID_MAIL_CONFIG";
    public static final String  MODULO          = "MODULO";
    public static final String  WORKFLOW_ACTION = "WORKFLOW_ACTION";
    public static final String  STATO_INIZIALE  = "STATO_INIZIALE";
    public static final String  STATO_FINALE    = "STATO_FINALE";
    public static final String  OGGETTO         = "OGGETTO";
    public static final String  TESTO_MAIL      = "TESTO_MAIL";
    public static final String  MITTENTE        = "MITTENTE";

    public MailConfigDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public MailConfigDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public MailConfigDAO(DBTransaction transact, String tableName) throws AppCrash {

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

        if (Util.IsNotEmpty(getAttribute(ID_MAIL_CONFIG))) {
            appendField(ID_MAIL_CONFIG, whereCondition);
        }

        return whereCondition;

    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_MAIL_CONFIG, Integer.class);

    }

}