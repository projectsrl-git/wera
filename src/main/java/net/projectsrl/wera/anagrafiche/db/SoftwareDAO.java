
package net.projectsrl.wera.anagrafiche.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDAO_base;

public class SoftwareDAO extends AliModDAO_base {

	private static final String TABLE_NAME       = "SOFTWARE";

    public static final String  ID_MODULO = "ID_MODULO";
    public static final String  DENOMINAZIONE   = "DENOMINAZIONE";
    public static final String  VERSIONE           = "VERSIONE";
    public static final String  PRODUTTORE           = "PRODUTTORE";
    
    
    public SoftwareDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public SoftwareDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public SoftwareDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

   

    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_MODULO))) {
            appendField(ID_MODULO, whereCondition);
        } else if (Util.IsNotEmpty(getAttribute(NR_MODULO))) {
            appendField(NR_MODULO, whereCondition);
        }
        
        return whereCondition;
    }
 

}
