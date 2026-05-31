
package net.projectsrl.wera.importazioni.db;

import java.math.BigDecimal;
import java.sql.Timestamp;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.WeraDAO_base;

public class DatiRilevatoriDAO extends WeraDAO_base {

	private static final String TABLE_NAME = "DATI_RILEVATORI";

	
	public static final String NOME_FILE_DR = "NOME_FILE_DR";
	public static final String RILEVATORE = "RILEVATORE";
	public static final String LETTURA = "LETTURA";
	public static final String LETTURA_ACS = "LETTURA_ACS";
	public static final String MESE = "MESE";
	public static final String ANNO = "ANNO";
	public static final String DATA_IMPORT = "DATA_IMPORT";
	public static final String ORA_IMPORT = "ORA_IMPORT";
	public static final String DATA_DI_LETTURA = "DATA_DI_LETTURA";
	public static final String ORA_DI_LETTURA = "ORA_DI_LETTURA";
	public static final String ANOMALIA = "ANOMALIA";
	public static final String DATA_ANOMALIA = "DATA_ANOMALIA";
	public static final String ORA_ANOMALIA = "ORA_ANOMALIA";
	public static final String LETTURA_ATTUALE = "LETTURA_ATTUALE";
	public static final String UNITA_DI_MISURA = "UNITA_DI_MISURA";
	public static final String DATA_LETTURA_ATTUALE = "DATA_LETTURA_ATTUALE";
	public static final String UNIT_OF_STAT = "UNIT_OF_STAT";
	public static final String ID_CONDOMINIO = "ID_CONDOMINIO";
	public static final String VOLUME_ATTUALE = "VOLUME_ATTUALE";
	public static final String LETTURA_PREC = "LETTURA_PREC";
	public static final String LETTURA_ACS_PREC = "LETTURA_ACS_PREC";
	public static final String FATTORE_ENERGIA = "FATTORE_ENERGIA";
	public static final String LETTURA_AFS = "LETTURA_AFS";
	public static final String LETTURA_AFS_PREC = "LETTURA_AFS_PREC";

	public DatiRilevatoriDAO() throws AppCrash {

		super(TABLE_NAME);
	}

	public DatiRilevatoriDAO(DBTransaction transact) throws AppCrash {

		super(transact, TABLE_NAME);
	}

	public DatiRilevatoriDAO(DBTransaction transact, String tableName) throws AppCrash {

		super(transact, tableName);
	}

	@Override
	protected void init() throws AppCrash {

		super.init();
		
		addNoStringField(ID_CONDOMINIO, Integer.class);
		addNoStringField(LETTURA, BigDecimal.class);
		addNoStringField(LETTURA_ACS, BigDecimal.class);
		addNoStringField(LETTURA_PREC, BigDecimal.class);
		addNoStringField(LETTURA_ACS_PREC, BigDecimal.class);
		addNoStringField(LETTURA_ATTUALE, BigDecimal.class);
		addNoStringField(VOLUME_ATTUALE, BigDecimal.class);
		addNoStringField(TS_INS, Timestamp.class);
		addNoStringField(ID_UTENTE_INS, Integer.class);
		addNoStringField(TS_MOD, Timestamp.class);
		addNoStringField(ID_UTENTE_MOD, Integer.class);
		addNoStringField(LETTURA_AFS, BigDecimal.class);
		addNoStringField(LETTURA_AFS_PREC, BigDecimal.class);
	}

	@Override
	protected WhereCondition whereCondition() throws AppCrash {

		WhereCondition whereCondition = new WhereCondition(this);

		if (Util.IsNotEmpty(getAttribute(ID_MODULO))) {
			appendField(ID_MODULO, whereCondition);
		} 

		return whereCondition;
	}

}
