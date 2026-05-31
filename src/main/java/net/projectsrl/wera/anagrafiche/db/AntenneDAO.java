
package net.projectsrl.wera.anagrafiche.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDAO_base;

public class AntenneDAO extends AliModDAO_base {

	private static final String TABLE_NAME       = "NETWORK";

	public static final String  ID_CONDOMINIO     = "ID_CONDOMINIO";
    public static final String  ANTENNA           = "ANTENNA";
    public static final String  GATEWAY           = "GATEWAY";
    public static final String  TIPOLOGIA         = "TIPOLOGIA";
    
    
    
    public AntenneDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public AntenneDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public AntenneDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();
        addNoStringField(ID_CONDOMINIO, Integer.class);
        addNoStringField(GATEWAY, Boolean.class);
        
        
    }

    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_MODULO))) {
            appendField(ID_MODULO, whereCondition);
        }else if (Util.IsNotEmpty(getAttribute(NR_MODULO))) {
            appendField(NR_MODULO, whereCondition);
        }
        
        return whereCondition;
    }

}
