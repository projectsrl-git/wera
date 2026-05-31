
package net.projectsrl.wera.configurazione.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDAO_base;

public class ConfigurazioneCsvDAO extends AliModDAO_base {

	private static final String TABLE_NAME       = "CONFIGURAZIONE_CSV";

    public static final String  RIGA_1           = "RIGA_1";
    public static final String  RIGA_2           = "RIGA_2";
    public static final String  RIGA_3         = "RIGA_3";
    
    
    
    public ConfigurazioneCsvDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public ConfigurazioneCsvDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public ConfigurazioneCsvDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();
      
        
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
