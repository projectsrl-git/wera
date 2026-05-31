package net.projectsrl.dafne.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class RendicontazioniDAO extends PjNDAO_base {
	
	private static final String TABLE_NAME = "RENDICONTAZIONI";

	public static final String	ID_RENDICONTAZIONE =		  "ID_RENDICONTAZIONE";		          
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
	public static final String	SPESE_VIAGGIO_IMPORTO =		  "?ISPESE_VIAGGIO_IMPORTO";		    
	public static final String	SPESE_TRASPORTO_IMPORTO =	  "?ISPESE_TRASPORTO_IMPORTO";	  
	public static final String	SPESE_PARCHEGGIO_IMPORTO =	  "?ISPESE_PARCHEGGIO_IMPORTO";	 
	public static final String	SPESE_VITTO_IMPORTO =		  "?ISPESE_VITTO_IMPORTO";		        
	public static final String	SPESE_ALLOGGIO_IMPORTO =	  "?ISPESE_ALLOGGIO_IMPORTO";	    
	public static final String	SPESE_PEDAGGI_IMPORTO =		  "?ISPESE_PEDAGGI_IMPORTO";		    
	public static final String	SPESE_ALTRO_IMPORTO =		  "?ISPESE_ALTRO_IMPORTO";		        
	public static final String	SPESE_TOTALE_IMPORTO =		  "?ISPESE_TOTALE_IMPORTO";		      
	public static final String	SPESE_VIAGGIO_NOTE =		  "SPESE_VIAGGIO_NOTE";		          
	public static final String	SPESE_TRASPORTO_NOTE =		  "SPESE_TRASPORTO_NOTE";		      
	public static final String	SPESE_PARCHEGGIO_NOTE =		  "SPESE_PARCHEGGIO_NOTE";		    
	public static final String	SPESE_VITTO_NOTE =			  "SPESE_VITTO_NOTE";			            
	public static final String	SPESE_ALLOGGIO_NOTE =		  "SPESE_ALLOGGIO_NOTE";		        
	public static final String	SPESE_PEDAGGI_NOTE =		  "SPESE_PEDAGGI_NOTE";		          
	public static final String	SPESE_ALTRO_NOTE =			  "SPESE_ALTRO_NOTE";			            
	public static final String	SPESE_TOTALE_NOTE =			  "SPESE_TOTALE_NOTE";	
	
	public static final String	APPROVAZIONE =				  "APPROVAZIONE";
	public static final String	ADMIN =				 		  "ADMIN";	
	public static final String	SPESE_VIAGGIO_IMPORTO_A =	  "?ISPESE_VIAGGIO_IMPORTO_A";		    
	public static final String	SPESE_TRASPORTO_IMPORTO_A =	  "?ISPESE_TRASPORTO_IMPORTO_A";	  
	public static final String	SPESE_PARCHEGGIO_IMPORTO_A =  "?ISPESE_PARCHEGGIO_IMPORTO_A";	 
	public static final String	SPESE_VITTO_IMPORTO_A =		  "?ISPESE_VITTO_IMPORTO_A";		        
	public static final String	SPESE_ALLOGGIO_IMPORTO_A =	  "?ISPESE_ALLOGGIO_IMPORTO_A";	    
	public static final String	SPESE_PEDAGGI_IMPORTO_A =	  "?ISPESE_PEDAGGI_IMPORTO_A";		    
	public static final String	SPESE_ALTRO_IMPORTO_A =		  "?ISPESE_ALTRO_IMPORTO_A";		        
	public static final String	SPESE_TOTALE_IMPORTO_A =	  "?ISPESE_TOTALE_IMPORTO_A";	
	
	public static final String	SPESE_VIAGGIO_VALUTA =		  "SPESE_VIAGGIO_VALUTA";		    
	public static final String	SPESE_TRASPORTO_VALUTA =	  "SPESE_TRASPORTO_VALUTA";	  
	public static final String	SPESE_PARCHEGGIO_VALUTA =	  "SPESE_PARCHEGGIO_VALUTA";	 
	public static final String	SPESE_VITTO_VALUTA =		  "SPESE_VITTO_VALUTA";		        
	public static final String	SPESE_ALLOGGIO_VALUTA =	  	  "SPESE_ALLOGGIO_VALUTA";	    
	public static final String	SPESE_PEDAGGI_VALUTA =		  "SPESE_PEDAGGI_VALUTA";		    
	public static final String	SPESE_ALTRO_VALUTA =		  "SPESE_ALTRO_VALUTA";		        
	public static final String	SPESE_TOTALE_VALUTA =		  "SPESE_TOTALE_VALUTA";	

	public RendicontazioniDAO() throws AppCrash {

		super(TABLE_NAME);
	}

	public RendicontazioniDAO(DBTransaction transact) throws AppCrash {

		super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY)
				+ TABLE_NAME);
	}

	public RendicontazioniDAO(DBTransaction transact, String tableName)
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

		if (Util.IsNotEmpty(getAttribute(ID_RENDICONTAZIONE))) {
			appendField(ID_RENDICONTAZIONE, whereCondition);
		}

		return whereCondition;
	}
	
	
	@Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_RENDICONTAZIONE, Integer.class);
 }
}