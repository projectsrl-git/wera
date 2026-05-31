
package net.projectsrl.mail;

import java.sql.Timestamp;
import java.util.UUID;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class EmailLogDAO extends PjNDAO_base {

    private static final String TABLE_NAME      = "EMAIL_LOG";

    public static final String  ID_EMAIL_LOG    = "ID_EMAIL_LOG";
    public static final String  SENDER          = "SENDER";
    public static final String  RECIPIENTS_TO   = "RECIPIENTS_TO";
    public static final String  RECIPIENTS_CC   = "RECIPIENTS_CC";
    public static final String  RECIPIENTS_BCC  = "RECIPIENTS_BCC";
    public static final String  SUBJECT         = "SUBJECT";
    public static final String  BODY            = "BODY";
    public static final String  ATTACHMENT_LIST = "ATTACHMENT_LIST";
    public static final String  ATTEMPTS        = "ATTEMPTS";
    public static final String  FL_SENT         = "FL_SENT";
    public static final String  FL_STOP_SEND    = "FL_STOP_SEND";
    public static final String  TS_SEND         = "TS_SEND";
    public static final String  EMAIL_DATA      = "EMAIL_DATA";
    public static final String  USERNAME        = "USERNAME";
    public static final String  TS_INS          = "TS_INS";

    public EmailLogDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public EmailLogDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public EmailLogDAO(DBTransaction transact, String tableName) throws AppCrash {

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

        if (Util.IsNotEmpty(getAttribute(ID_EMAIL_LOG))) {
            appendField(ID_EMAIL_LOG, whereCondition);
        }

        return whereCondition;
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_EMAIL_LOG, UUID.class);
        addNoStringField(ATTEMPTS, Integer.class);
        addNoStringField(FL_SENT, Boolean.class);
        addNoStringField(FL_STOP_SEND, Boolean.class);
        addNoStringField(TS_SEND, Timestamp.class);
        addNoStringField(TS_INS, Timestamp.class);

    }

}