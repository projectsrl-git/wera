
package net.projectsrl.wm.db;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.projectsrl.db.PjDAO_base;

public class AziendeGruppiDAO extends PjDAO_base {

	private static final String NOME_TABELLA = "AZIENDE";

	public static final String	CODICE = "CODICE";
	public static final String	CODICE_PARENT = "CODICE_PARENT";
	public static final String	RAGSOC = "RAGSOC";
	public static final String	RAGRID = "RAGRID";
	public static final String	SETTORE = "SETTORE";
	public static final String	GRUPPI = "GRUPPI";


	public AziendeGruppiDAO() throws AppCrash {

		super(NOME_TABELLA);
		setUniqueIdentifier(CODICE);
	}

	public AziendeGruppiDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty("DBEntity.NomeDB") + NOME_TABELLA);
		setUniqueIdentifier(CODICE);
	}

	public AziendeGruppiDAO(DBTransaction transact, String tableName) throws AppCrash {

		super(transact, tableName);
		setUniqueIdentifier(CODICE);

	}

}
