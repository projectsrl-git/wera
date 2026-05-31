
package net.projectsrl.wera.anagrafiche.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDAO_base;

public class TecniciDAO extends AliModDAO_base {

	private static final String TABLE_NAME = "TECNICI";

	public static final String ID_MODULO = "ID_MODULO";
	public static final String QUALIFICA = "QUALIFICA";
	public static final String COGNOME = "COGNOME";
	public static final String NOME = "NOME";
	public static final String INDIRIZZO = "INDIRIZZO";
	public static final String CAP = "CAP";
	public static final String COMUNE = "COMUNE";
	public static final String PROV = "PROV";
	public static final String DENOMINAZIONE_STUDIO = "DENOMINAZIONE_STUDIO";
	public static final String INDIRIZZO_STUDIO = "INDIRIZZO_STUDIO";
	public static final String CAP_STUDIO = "CAP_STUDIO";
	public static final String COMUNE_STUDIO = "COMUNE_STUDIO";
	public static final String PROV_STUDIO = "PROV_STUDIO";

	public TecniciDAO() throws AppCrash {

		super(TABLE_NAME);
	}

	public TecniciDAO(DBTransaction transact) throws AppCrash {

		super(transact, TABLE_NAME);
	}

	public TecniciDAO(DBTransaction transact, String tableName) throws AppCrash {

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
