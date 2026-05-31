
package net.projectsrl.wera.filemanager.db;

import java.util.HashMap;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDAO_base;

public class ElencoFileImportatiFileManagerDAO extends AliModDAO_base {

	private static final String DS_PROGRESSIVO_MODULO = "DSProgressivoALIMOD";
    private static final String PARAM_TABLE_NAME      = "TABLE_NAME";
    private static final String PARAM_FIELD_NAME      = "FIELD_NAME";
    
	private static final String TABLE_NAME = "ELENCO_FILE_IMPORTATI_FILE_MANAGER";
	public static final String ID_CONDOMINIO = "ID_CONDOMINIO";
	public static final String ID_MODULO = "ID_MODULO";
	public static final String ID_CATEGORIA = "ID_CATEGORIA";
	public static final String NOME_FILE = "NOME_FILE";
	public static final String DATA_IMPORT = "DATA_IMPORT";
	public static final String ORA_IMPORT = "ORA_IMPORT";
	public static final String DATA_PRIMA_SEGNALAZIONE = "DATA_PRIMA_SEGNALAZIONE";
	public static final String GG_SEGNALAZIONE = "GG_SEGNALAZIONE";
	
	
	
	public ElencoFileImportatiFileManagerDAO() throws AppCrash {

		super(TABLE_NAME);
	}

	public ElencoFileImportatiFileManagerDAO(DBTransaction transact) throws AppCrash {

		super(transact, TABLE_NAME);
	}

	public ElencoFileImportatiFileManagerDAO(DBTransaction transact, String tableName) throws AppCrash {

		super(transact, tableName);
	}

	@Override
	protected void init() throws AppCrash {

		super.init();
		addNoStringField(ID_MODULO, Integer.class);
		addNoStringField(ID_CONDOMINIO, Integer.class);
		addNoStringField(GG_SEGNALAZIONE, Integer.class);

	}
	
	 @Override
	    public void insert() throws AppCrash {

	        String nrModulo = getNextSequentialNumber();
	        setAttribute(STATO, "DRA");
	        setAttribute(NR_MODULO, nrModulo);
	        setAttribute(ID_MODULO, nrModulo);

	        super.insert();
	        super.retrieve();
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
	
	
	@Override
    public void delete() throws AppCrash {

        String idModulo = getAttributeAsString(ID_MODULO);
        String queryDelete = "delete from scarico where lower(nome_file) = (select lower(nome_file) from elenco_file_importati_rilevatori where id_modulo="+idModulo+")";
        net.projectsrl.wm.utils.WMUtils.executeQuery(queryDelete);
        String queryDelete1 = "delete from dati_rilevatori where lower(nome_file_dr) = (select lower(nome_file) from elenco_file_importati_rilevatori where id_modulo="+idModulo+")";
        net.projectsrl.wm.utils.WMUtils.executeQuery(queryDelete1);
        super.delete();

    }
	
	
	
	
	 private String getNextSequentialNumber() throws AppCrash {

	        String dsName = DS_PROGRESSIVO_MODULO;

	        DataSet_itf dataSet = null;

	        DataSetFactory dsFactory = DataSetFactory.getInstance();
	        Integer lastSequentialNumeber = new Integer(0);

	        try {
	            dataSet = dsFactory.makeDataSet("", dsName);

	            HashMap<String, String> param = new HashMap<String, String>();
	            param.put(PARAM_TABLE_NAME, getTableName());
	            param.put(PARAM_FIELD_NAME, NR_MODULO);
	            dataSet.setParam(param);
	            dataSet.open();

	            if (dataSet.hasMoreElements()) {
	                Row_itf dbRow = (Row_itf) dataSet.nextElement();

	                if (dbRow != null) {
	                    String strLast = (String) dbRow.getField("ultimo");
	                    if (strLast != null && !strLast.trim().equals("")) {
	                        lastSequentialNumeber = Integer.valueOf(strLast);
	                        lastSequentialNumeber += 1;
	                    }
	                }
	            }
	        } catch (AppCrash ac) {
	            ac.logContext(this.getClass().getName(),
	                    "Errore nella ricerca dell'ultimo progressivo del dataset " + dsName);
	            throw ac;
	        } finally {
	            // chiude il dataset per il conteggio degli elementi trovati
	            if (dataSet != null) {
	                try {
	                    dataSet.close();
	                } catch (Throwable t) {
	                    AppCrash ac = new AppCrash(t);
	                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + dsName);
	                }
	            }
	        }

	        if (lastSequentialNumeber == null || lastSequentialNumeber.intValue() == 0) {
	            lastSequentialNumeber = new Integer(1);
	        }

	        return "" + lastSequentialNumeber;

	    }
	 

}
