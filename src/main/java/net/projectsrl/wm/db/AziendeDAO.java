package net.projectsrl.wm.db;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.projectsrl.db.PjDAO_base;

/**
 * Classe che rappresenta la tabella AZIENDE Proprietà lette dal file di
 * configurazione:
 * <p>
 * DATABASE.NAME = nome del database
 */
public class AziendeDAO extends PjDAO_base {
	

	private static final String NOME_TABELLA = "AZIENDE";

	public static final String	CODICE = "CODICE";
	public static final String	CODICE_PARENT = "CODICE_PARENT";
	public static final String	RAGSOC = "RAGSOC";
	public static final String	RAGRID = "RAGRID";
	public static final String	SETTORE = "SETTORE";
	public static final String	GRUPPI = "GRUPPI";
	public static final String	TAGGANCIO = "TAGGANCIO";
	public static final String	CODFISCALE_AZ = "CODFISCALE_AZ";
	public static final String	PARTITAIVA = "PARTITAIVA";
	
	public static final String	COGNOME = "COGNOME";
	public static final String	NOME = "NOME";
	public static final String	SESSO = "SESSO";
	public static final String	CODFISCALE = "CODFISCALE"; 
	public static final String	LOCNASCITA = "LOCNASCITA"; 
	public static final String  D_NASCITA = "D_NASCITA";
	public static final String  INAIL = "INAIL";
	

	public AziendeDAO() throws AppCrash {
		super(NOME_TABELLA);
		setUniqueIdentifier(TAGGANCIO);
	}


	public AziendeDAO(DBTransaction transact) throws AppCrash {
		super(transact, NOME_TABELLA);
		setUniqueIdentifier(TAGGANCIO);
	}

	public AziendeDAO(DBTransaction transact, String tableName) throws AppCrash {
		super(transact, tableName);
		setUniqueIdentifier(TAGGANCIO);

	}
}
