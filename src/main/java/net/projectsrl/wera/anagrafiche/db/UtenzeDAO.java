
package net.projectsrl.wera.anagrafiche.db;

import java.math.BigDecimal;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDAO_base;

public class UtenzeDAO extends AliModDAO_base {

	private static final String TABLE_NAME = "UTENZE";

	public static final String ID_CONDOMINIO = "ID_CONDOMINIO";    
	public static final String DENOMINAZIONE = "DENOMINAZIONE";    
	public static final String LOCATARIO = "LOCATARIO";            
                                                                   
	public static final String SCALA = "SCALA";                    
	public static final String PIANO = "PIANO";                    
	public static final String INTERNO = "INTERNO";                
	public static final String N_TELEFONO = "N_TELEFONO";          
	public static final String MILLESIMI = "MILLESIMI";            
	public static final String MILLESIMI_CLIMA = "MILLESIMI_CLIMA";
	public static final String MILLESIMI_ACS = "MILLESIMI_ACS";
	
	public static final String FABBISOGNO_CLIMA = "FABBISOGNO_CLIMA";  
	public static final String FABBISOGNO_ACS = "FABBISOGNO_ACS";
	
	public static final String TIPO_UTENZA = "TIPO_UTENZA";  
	                                                               
	public static final String  TABELLA_PARENT        = "TABELLA_PARENT";
    public static final String  ID_MODULO_PARENT      = "ID_MODULO_PARENT";

	public UtenzeDAO() throws AppCrash {

		super(TABLE_NAME);
	}

	public UtenzeDAO(DBTransaction transact) throws AppCrash {

		super(transact, TABLE_NAME);
	}

	public UtenzeDAO(DBTransaction transact, String tableName) throws AppCrash {

		super(transact, tableName);
	}

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_CONDOMINIO, Integer.class);
        addNoStringField(ID_MODULO_PARENT, Integer.class);
        addNoStringField(MILLESIMI, BigDecimal.class);
        addNoStringField(MILLESIMI_CLIMA, BigDecimal.class);
        addNoStringField(MILLESIMI_ACS, BigDecimal.class);
        
        addNoStringField(FABBISOGNO_CLIMA, BigDecimal.class);
        addNoStringField(FABBISOGNO_ACS, BigDecimal.class);
    }

    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_MODULO))) {
            appendField(ID_MODULO, whereCondition);
        } else if (Util.IsNotEmpty(getAttribute(NR_MODULO))) {
            appendField(NR_MODULO, whereCondition);
        } else if (Util.IsNotEmpty(getAttribute(TABELLA_PARENT)) && Util.IsNotEmpty(getAttribute(ID_MODULO_PARENT))) {
            appendField(TABELLA_PARENT, whereCondition);
            appendField(ID_MODULO_PARENT, whereCondition);
        }
        
        return whereCondition;
    }

}
