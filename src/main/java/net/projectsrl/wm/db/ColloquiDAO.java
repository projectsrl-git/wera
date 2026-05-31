
package net.projectsrl.wm.db;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.projectsrl.db.PjDAO_base;



/**
 * Classe che rappresenta la tabella COLLOQUI 
 * Proprietà lette dal file di configurazione:
 * <p>
 * DATABASE.NAME = nome del database
 */
public class ColloquiDAO extends PjDAO_base {

	private static final String NOME_TABELLA = "COLLOQUI";

	// colonne
	public static final String DAGGANCIO = "DAGGANCIO";
	public static final String ID_COLLOQUIO = "ID_COLLOQUIO";


	public static final String IDTCONTATT = "IDTCONTATT";
	public static final String IDTIOPL = "IDTIOPL";
	public static final String IDTIPOCNTR = "IDTIPOCNTR";
	public static final String COMUNE = "COMUNE";
	public static final String D_DTCONTATT = "D_DTCONTATT";
	public static final String SINTESIESIT = "SINTESIESIT";
	public static final String QUALIFICLAV = "QUALIFICLAV";
	public static final String RETRIBUZION = "RETRIBUZION";
	public static final String PROFILOPROF = "PROFILOPROF";
	public static final String SPECIALIZZ = "SPECIALIZZ";
	public static final String SITUAZIONE = "SITUAZIONE";
	public static final String DISPONTRASF = "DISPONTRASF";
	public static final String ANNOTAZIONI = "ANNOTAZIONI";
	public static final String DISPONDAL = "DISPONDAL";
	public static final String RISULTATO = "RISULTATO";
	public static final String IMPRESVALUT = "IMPRESVALUT";
	public static final String ANNOTRISER = "ANNOTRISER";
	public static final String PROPDAFORM = "PROPDAFORM";
	public static final String AZIONIDAINT = "AZIONIDAINT";
	public static final String ALTREINDIC = "ALTREINDIC";
	public static final String ORDINE    = "?IORDINE"   ;
	
	public static final String IDRISUMANA   = "IDRISUMANA"  ;
	public static final String RISUMANA   = "RISUMANA"  ;
	public static final String DELTA_DISP = "?IDELTA_DISP";	
	
	
	public static final String RISCOLLOQUIO = "RISCOLLOQUIO";	
	public static final String RICHIESTASPEC = "RICHIESTASPEC";

	
	/**
	 * Costruttore
	 */
	public ColloquiDAO() throws AppCrash {
		super( NOME_TABELLA);
		setUniqueIdentifier(ID_COLLOQUIO);		
	}

	/**
	 * Costruttore con DBTransaction.
	 * 
	 * @param transact  transazione
	 * 
	 */
	public ColloquiDAO(DBTransaction transact) throws AppCrash {
		super(transact, Config.GetInstance().getProperty("DBEntity.NomeDB")
				+ NOME_TABELLA);
		setUniqueIdentifier(ID_COLLOQUIO);		
	}

	public ColloquiDAO(DBTransaction transact, String tableName)
			throws AppCrash {
		super(transact, tableName);
		setUniqueIdentifier(ID_COLLOQUIO);
	}



}
