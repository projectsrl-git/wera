
package net.projectsrl.bow.company.db;

import java.util.HashMap;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class AziendeDAO extends PjNDAO_base {

    private static final String TABLE_NAME        = "AZIENDE";

    public static final String  ID_AZIENDA        = "ID_AZIENDA";
    public static final String  ID_AZIENDA_PARENT = "ID_AZIENDA_PARENT";
    public static final String  CODICE            = "CODICE";
    public static final String  RAGSOC            = "RAGSOC";
    public static final String  ALIAS_AZIENDA     = "ALIAS_AZIENDA";
    public static final String  TIPO_AZIENDA      = "TIPO_AZIENDA";
    
    public static final String CODFISCALE_AZ  ="CODFISCALE_AZ";
	public static final String PARTITAIVA     ="PARTITAIVA";
	public static final String TELEFONO       ="TELEFONO";
	public static final String MAIL           ="MAIL";
	public static final String CREDITI        ="CREDITI";	                                                             
	public static final String NOME           ="NOME";
	public static final String COGNOME        ="COGNOME";
	public static final String SESSO          ="SESSO";
	public static final String D_NASCITA      ="D_NASCITA";
	public static final String LOCNASCITA     ="LOCNASCITA";
	public static final String PROVNASCITA    ="PROVNASCITA";
	public static final String CODFISCALE     ="CODFISCALE";
	public static final String DATA_SCADENZA_CREDITI     ="DATA_SCADENZA_CREDITI";
	

    public AziendeDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public AziendeDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public AziendeDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_AZIENDA, Integer.class);
        addNoStringField(ID_AZIENDA_PARENT, Integer.class);
        addNoStringField(CREDITI, Integer.class);

    }

    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_AZIENDA))) {
            appendField(ID_AZIENDA, whereCondition);

        }

        return whereCondition;
    }
    
    @Override
    public void insert() throws AppCrash {

        super.insert();
        
        Integer idAzienda=0;
        DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        try {
            dataSet = dsFactory.makeDataSet("", "DSAziendeConfigurazioneErrori");

            HashMap<String, String> param = new HashMap<String, String>();
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                idAzienda= (Integer) dbRow.getField("ID_AZIENDA");
                creaConfigurazioneErrori(idAzienda);
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),
                    "Errore nella ricerca dell'ultimo progressivo del dataset " + "DSDatiRipartizioni");
            throw ac;
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DSDatiRipartizioni");
                }
            }
        }

        
    }

	private void creaConfigurazioneErrori(Integer idAzienda) throws AppCrash {
		String queryInsert = "insert into configurazione_errori (id_azienda,id_errore,colore,mail,nr_modulo,dt_modulo,stato,ts_ins,id_utente_ins) (select "+idAzienda+",id_parametro,'',false,id_parametro,'2000/01/01','DRA','2018-01-01 12:00:00.001',1 from parametri where dominio='ERR')";
        net.projectsrl.wm.utils.WMUtils.executeQuery(queryInsert);
	}      
}
