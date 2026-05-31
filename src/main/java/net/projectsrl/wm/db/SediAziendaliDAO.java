package net.projectsrl.wm.db;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.projectsrl.db.PjDAO_base;

/**
 * Classe che rappresenta la tabella SEDI_AZIENDALI Proprietà lette dal file di
 * configurazione:
 * <p>
 * DATABASE.NAME = nome del database
 */
public class SediAziendaliDAO extends PjDAO_base {
	

	private static final String NOME_TABELLA = "SEDI_AZIENDALI";

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
		super(NOME_TABELLA);
		setUniqueIdentifier(CODICE);
	}


	public SediAziendaliDAO(DBTransaction transact) throws AppCrash {
		super(transact, NOME_TABELLA);
		setUniqueIdentifier(CODICE);
	}

	public SediAziendaliDAO(DBTransaction transact, String tableName) throws AppCrash {
		super(transact, tableName);
		setUniqueIdentifier(CODICE);

	}
}
