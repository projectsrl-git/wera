package net.projectsrl.dafne.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class TrasferteDAO extends PjNDAO_base {
	
	private static final String TABLE_NAME = "TRASFERTE";

	public static final String	ID_TRASFERTA = "ID_TRASFERTA";
	public static final String	DESCRIZIONE = "DESCRIZIONE";
	public static final String	NOTE = "NOTE";
	public static final String	STATO_APPROVAZIONE = "STATO_APPROVAZIONE";
	public static final String	ID_RISORSA =				  "ID_RISORSA";
	public static final String	ID_DIREZIONE =				  "ID_DIREZIONE";
	public static final String	ID_AZIENDA =				  "ID_AZIENDA";
	public static final String	DATA_DAL = "DATA_DAL";
	public static final String	DATA_AL = "DATA_AL";
	public static final String	ORE_DAL = "ORE_DAL";
	public static final String	ORE_AL = "ORE_AL";
	public static final String	ORE = "ORE";
	public static final String	REVOCATO = "REVOCATO";
	public static final String	APPROVAZIONE = "APPROVAZIONE";
	public static final String	CLIENTE = "CLIENTE";
	public static final String	COMMESSA = "COMMESSA";
	public static final String	LUOGO = "LUOGO";
	public static final String	TIPO = "TIPO";

	public TrasferteDAO() throws AppCrash {

		super(TABLE_NAME);
	}

	public TrasferteDAO(DBTransaction transact) throws AppCrash {

		super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY)
				+ TABLE_NAME);
	}

	public TrasferteDAO(DBTransaction transact, String tableName)
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

		if (Util.IsNotEmpty(getAttribute(ID_TRASFERTA))) {
			appendField(ID_TRASFERTA, whereCondition);
		}

		return whereCondition;
	}
	
	@Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_TRASFERTA, Integer.class);
 }
}