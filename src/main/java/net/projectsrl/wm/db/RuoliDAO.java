
package net.projectsrl.wm.db;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.projectsrl.db.PjDAO_base;

public class RuoliDAO extends PjDAO_base {

	private static final String NOME_TABELLA = "MENU";

	public static final String CODICE = "CODICE";
	public static final String DESCRIZIONE = "DESCRIZIONE";
	public static final String FLAG = "FLAG";
	public static final String RUOLI = "RUOLI";
	public static final String LINK = "LINK";
	public static final String ORDINE = "ORDINE";
	public static final String UTENTE = "UTENTE";


	public RuoliDAO() throws AppCrash {

		super(NOME_TABELLA);
		setUniqueIdentifier(CODICE);
	}

	public RuoliDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty("DBEntity.NomeDB") + NOME_TABELLA);
		setUniqueIdentifier(CODICE);
	}

	public RuoliDAO(DBTransaction transact, String tableName) throws AppCrash {

		super(transact, tableName);
		setUniqueIdentifier(CODICE);

	}

}
