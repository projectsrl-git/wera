package net.projectsrl.dafne.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class StraordinariDAO extends PjNDAO_base {
	
	private static final String TABLE_NAME = "STRAORDINARI";

	public static final String	ID_STRAORDINARIO = "ID_STRAORDINARIO";
	public static final String	DIPENDENTE = "DIPENDENTE";
	public static final String	TIPO_PERMESSO = "TIPO_PERMESSO";
	public static final String	DATA_DAL = "DATA_DAL";
	public static final String	DATA_AL = "DATA_AL";
	public static final String	ORE_DAL = "ORE_DAL";
	public static final String	ORE_AL = "ORE_AL";
	public static final String	ORE = "ORE";
	public static final String	AZIENDA_TENDINA = "AZIENDA_TENDINA";
	public static final String	APPROVAZIONE = "APPROVAZIONE";
	public static final String	CAUSALE = "CAUSALE";

	public StraordinariDAO() throws AppCrash {

		super(TABLE_NAME);
	}

	public StraordinariDAO(DBTransaction transact) throws AppCrash {

		super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY)
				+ TABLE_NAME);
	}

	public StraordinariDAO(DBTransaction transact, String tableName)
			throws AppCrash {

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

		if (Util.IsNotEmpty(getAttribute(ID_STRAORDINARIO))) {
			appendField(ID_STRAORDINARIO, whereCondition);
		}

		return whereCondition;
	}
	
	@Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_STRAORDINARIO, Integer.class);
 }
}