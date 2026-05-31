package net.projectsrl.dafne.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class SediAziendaliDAO extends PjNDAO_base {
	
	private static final String TABLE_NAME = "SEDI_AZIENDALI";

	public static final String	CODICE = "CODICE";
	public static final String	CODICE_PARENT = "CODICE_PARENT";
	public static final String	RAGSOC = "RAGSOC";
	public static final String	INDIRIZZO = "INDIRIZZO";
	public static final String	CAP = "CAP";
	public static final String	CITTA = "CITTA";
	public static final String	PROVINCIA = "PROVINCIA";
	public static final String	TELEFONO = "TELEFONO";
	public static final String	EMAIL = "EMAIL";
	public static final String	SEDE_LEGALE = "SEDE_LEGALE";
	public static final String  SEDE_OPERATIVA = "SEDE_OPERATIVA";
	public static final String  COD_CATASTO = "COD_CATASTO";
	public static final String  CIVICO = "CIVICO";

	public SediAziendaliDAO() throws AppCrash {

		super(TABLE_NAME);
	}

	public SediAziendaliDAO(DBTransaction transact) throws AppCrash {

		super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY)
				+ TABLE_NAME);
	}

	public SediAziendaliDAO(DBTransaction transact, String tableName)
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

		if (Util.IsNotEmpty(getAttribute(CODICE))) {
			appendField(CODICE, whereCondition);
		}

		return whereCondition;
	}
}