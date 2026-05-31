
package net.projectsrl.wera.gestionerilevatori.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDAO_base;

public class GestioneRilevatoriErroreDAO extends AliModDAO_base {

	private static final String TABLE_NAME       = "CONFIGURAZIONE_ERRORI";

	public static final String  ID_ERRORE     = "ID_ERRORE";
    public static final String  COLORE           = "COLORE";
    public static final String  MAIL           = "MAIL";
   
    
    
    
    public GestioneRilevatoriErroreDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public GestioneRilevatoriErroreDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public GestioneRilevatoriErroreDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();
        addNoStringField(ID_ERRORE, Integer.class);
        addNoStringField(MAIL, Boolean.class);
        
        
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
