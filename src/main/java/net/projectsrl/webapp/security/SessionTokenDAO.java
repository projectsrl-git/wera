
package net.projectsrl.webapp.security;

import java.sql.Timestamp;
import java.util.UUID;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.projectsrl.db.PjNDAO_base;

public class SessionTokenDAO extends PjNDAO_base {

    private static final String TABLE_NAME = "SESSION_TOKEN";

    public static final String  ID_TOKEN   = "ID_TOKEN";
    public static final String  IPADDRESS  = "IPADDRESS";
    public static final String  TS_INS     = "TS_INS";
    public static final String  DATAMAP    = "DATAMAP";

    public SessionTokenDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public SessionTokenDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public SessionTokenDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);

    }

    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        appendField(ID_TOKEN, whereCondition);

        return whereCondition;
    }
    
    @Override
    protected void init() throws AppCrash {

        super.init();
        
        addNoStringField(ID_TOKEN, UUID.class);      
        addNoStringField(TS_INS, Timestamp.class);

    }    

}
