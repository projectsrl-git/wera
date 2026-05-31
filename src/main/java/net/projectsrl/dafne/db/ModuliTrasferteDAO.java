package net.projectsrl.dafne.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class ModuliTrasferteDAO extends PjNDAO_base {
	
	private static final String TABLE_NAME = "MOD_TRASFERTE";

	public static final String	ID_MODTRASFERTA =		  	  "ID_MODTRASFERTA";		          
	public static final String	ID_RISORSA =				  "ID_RISORSA";
	public static final String	ID_DIREZIONE =				  "ID_DIREZIONE";
	public static final String	ID_AZIENDA =				  "ID_AZIENDA";
	public static final String	QUALITA =					  "QUALITA";					                          
	public static final String	DATA_DAL =					  "DATA_DAL";					                        
	public static final String	DATA_AL = 					  "DATA_AL"; 					                        
	public static final String	ORE_DAL = 					  "ORE_DAL"; 					                        
	public static final String	ORE_AL = 					  "ORE_AL"; 					                          
	public static final String	ORE =						  "ORE";						                                
	public static final String	INCARICO =					  "INCARICO";					                        
	public static final String	CLIENTE =					  "CLIENTE";					                          
	public static final String	COMMESSA =					  "COMMESSA";					                        
	public static final String	LUOGO =						  "LUOGO";						                            
	public static final String	MODELLO =					  "MODELLO";					                          
	public static final String	MARCA =						  "MARCA";						                                       
	public static final String	DOCUMENTAZIONE = 			  "DOCUMENTAZIONE"; 			              
	public static final String	NOMINATIVO =				  "NOMINATIVO";				                      
	public static final String	DATA_NASCITA =	        	  "DATA_NASCITA";	        	      
	public static final String	CITTA_NASCITA =	        	  "CITTA_NASCITA";	        	    
	public static final String	PROV_NASCITA =	        	  "PROV_NASCITA";	        	      
	public static final String	INDIR_RESIDENZA =	    	  "INDIR_RESIDENZA";	    	        
	public static final String	CITTA_RESIDENZA =       	  "CITTA_RESIDENZA";       	    
	public static final String	CAP_RESIDENZA =         	  "CAP_RESIDENZA";         	    
	public static final String	COD_FISCALE =				  "COD_FISCALE";				                    
	public static final String	MOTIVAZIONE =				  "MOTIVAZIONE";
	public static final String	MATRICOLA_DIPENDENTE =		  "MATRICOLA_DIPENDENTE";   
	public static final String	ALLEGATO =				      "ALLEGATO";
	public static final String	FILENAME =				      "FILENAME"; 

	public ModuliTrasferteDAO() throws AppCrash {

		super(TABLE_NAME);
	}

	public ModuliTrasferteDAO(DBTransaction transact) throws AppCrash {

		super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY)
				+ TABLE_NAME);
	}

	public ModuliTrasferteDAO(DBTransaction transact, String tableName)
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

		if (Util.IsNotEmpty(getAttribute(ID_MODTRASFERTA))) {
			appendField(ID_MODTRASFERTA, whereCondition);
		}

		return whereCondition;
	}
	
	@Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_MODTRASFERTA, Integer.class);
 }
}