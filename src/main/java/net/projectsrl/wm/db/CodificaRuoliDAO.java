package net.projectsrl.wm.db;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.projectsrl.db.PjDAO_base;

/**
 * Classe che rappresenta la tabella PARA Proprietà lette dal file di
 * configurazione:
 * <p>
 * DATABASE.NAME = nome del database
 */
public class CodificaRuoliDAO extends PjDAO_base {

	private static final String NOME_TABELLA = "PARA";

	public static final String CODICE = "CODICE";
	public static final String DESCRI = "DESCRI";

	public CodificaRuoliDAO() throws AppCrash {
		super(NOME_TABELLA);
		setUniqueIdentifier(CODICE);
	}

	public CodificaRuoliDAO(DBTransaction transact) throws AppCrash {
		super(transact, Config.GetInstance().getProperty("DBEntity.NomeDB") + NOME_TABELLA);
		setUniqueIdentifier(CODICE);
	}

	public CodificaRuoliDAO(DBTransaction transact, String tableName) throws AppCrash {
		super(transact, tableName);
		setUniqueIdentifier(CODICE);

	}

}
