
package net.projectsrl.wera.ripartizioniletture.db;

import java.math.BigDecimal;
import java.sql.Timestamp;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDAO_base;

public class RipartizioniLettureDAO extends AliModDAO_base {
	private static final String TABLE_NAME = "RIPARTIZIONI_LETTURE";

	public static final String DATA_DAL = "DATA_DAL";
	public static final String DATA_AL = "DATA_AL";

	public static final String ID_CONDOMINIO = "ID_CONDOMINIO";
	public static final String CONFERMATA = "CONFERMATA";
	public static final String INVIATA = "INVIATA";
	public static final String CONTROLLO_OK = "CONTROLLO_OK";

	public static final String TIPO_RIPARTIZIONE = "TIPO_RIPARTIZIONE";
	
	public static final String LETTURA = "LETTURA";
	public static final String LETTURA_ACS = "LETTURA_ACS";
	public static final String LETTURA_AFS = "LETTURA_AFS";
	public static final String TIPOLOGIA_RIPARTIZIONE = "TIPOLOGIA_RIPARTIZIONE";
	public static final String TOTALE_MILLESIMI_RISCALDAMENTO        = "TOTALE_MILLESIMI_RISCALDAMENTO";
    public static final String TOTALE_MILLESIMI_ACS        = "TOTALE_MILLESIMI_ACS";
    public static final String TIPO_CONTABILIZZAZIONE = "TIPO_CONTABILIZZAZIONE";
    public static final String SALVATAGGIO_IN_CORSO = "SALVATAGGIO_IN_CORSO";
    
	public static final String TS_INS = "TS_INS";
	public static final String ID_UTENTE_INS = "ID_UTENTE_INS";
	public static final String TS_MOD = "TS_MOD";
	public static final String ID_UTENTE_MOD = "ID_UTENTE_MOD";

	public RipartizioniLettureDAO() throws AppCrash {

		super(TABLE_NAME);
	}

	public RipartizioniLettureDAO(DBTransaction transact) throws AppCrash {

		super(transact, TABLE_NAME);
	}

	public RipartizioniLettureDAO(DBTransaction transact, String tableName) throws AppCrash {

		super(transact, tableName);
	}

	@Override
	protected void init() throws AppCrash {

		super.init();

		addNoStringField(ID_MODULO, Integer.class);
		addNoStringField(ID_CONDOMINIO, Integer.class);

		addNoStringField(CONFERMATA, Boolean.class);
		addNoStringField(INVIATA, Boolean.class);
		addNoStringField(CONTROLLO_OK, Boolean.class);
		
		addNoStringField(LETTURA, Integer.class);
		addNoStringField(LETTURA_ACS, BigDecimal.class);
        addNoStringField(TOTALE_MILLESIMI_RISCALDAMENTO, BigDecimal.class);
        addNoStringField(TOTALE_MILLESIMI_ACS, BigDecimal.class);
        
        
        addNoStringField(LETTURA_AFS, BigDecimal.class);
		
		addNoStringField(TS_INS, Timestamp.class);
		addNoStringField(ID_UTENTE_INS, Integer.class);
		addNoStringField(TS_MOD, Timestamp.class);
		addNoStringField(ID_UTENTE_MOD, Integer.class);
		
		addNoStringField(SALVATAGGIO_IN_CORSO,Boolean.class);

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
	
	
	@Override
    public void delete() throws AppCrash {
		 super.delete();
	}

}
