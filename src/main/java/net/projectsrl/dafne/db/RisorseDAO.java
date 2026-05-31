
package net.projectsrl.dafne.db;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class RisorseDAO extends PjNDAO_base {

    private static final String DATASET_RISORSE_AZIENDA = "DSRisorseAzienda";
 
    private static final String TABLE_NAME             = "RISORSE";

    public static final String  ID_RISORSA             = "ID_RISORSA";
    public static final String  ID_CURRICUL            = "ID_CURRICUL";
    public static final String  COGNOME                = "COGNOME";
    public static final String  NOME                   = "NOME";
    public static final String  CODICE_FISCALE         = "CODICE_FISCALE";
    public static final String  DT_NASCITA             = "DT_NASCITA";
    public static final String  COD_CATASTALE_NASCITA  = "COD_CATASTALE_NASCITA";
    public static final String  COMUNE_NAZIONE_NASCITA = "COMUNE_NAZIONE_NASCITA";
    public static final String  PROV_NASCITA           = "PROV_NASCITA";
    public static final String  COD_CATASTALE_RES      = "COD_CATASTALE_RES";
    public static final String  INDIRIZZO_RES          = "INDIRIZZO_RES";
    public static final String  COMUNE_RES             = "COMUNE_RES";
    public static final String  PROV_RES               = "PROV_RES";
    public static final String  CAP_RES                = "CAP_RES";
    public static final String  TS_INS                 = "TS_INS";
    public static final String  ID_UTENTE_INS          = "ID_UTENTE_INS";

    public RisorseDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public RisorseDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public RisorseDAO(DBTransaction transact, String tableName) throws AppCrash {

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

        if (Util.IsNotEmpty(getAttribute(ID_RISORSA))) {
            appendField(ID_RISORSA, whereCondition);
        }

        return whereCondition;

    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_RISORSA, Integer.class);
        addNoStringField(ID_CURRICUL, Integer.class);
        addNoStringField(TS_INS, Timestamp.class);
        addNoStringField(ID_UTENTE_INS, Integer.class);

    }

    @Override
    public void setMapFromAttributes(Map<String, Object> map) throws AppCrash {

        super.setMapFromAttributes(map);
        
        RisorseAziendaDAO risorseAzienda=new RisorseAziendaDAO();
        risorseAzienda.setAttribute(RisorseAziendaDAO.ID_RISORSA, getAttribute(ID_RISORSA));
        risorseAzienda.setAttribute(RisorseAziendaDAO.FL_ULTIMO_RAPPORTO, true);
        ErrDetector.GetInstance().preCond(risorseAzienda.retrieve(),"ID_AZIENDA not fuond for ID_RISORSA"+getAttribute(ID_RISORSA));
        map.put(RisorseAziendaDAO.ID_AZIENDA, risorseAzienda.getAttribute(RisorseAziendaDAO.ID_AZIENDA));
        map.put(RisorseAziendaDAO.DT_INIZIO_RAPPORTO, risorseAzienda.getAttribute(RisorseAziendaDAO.DT_INIZIO_RAPPORTO));
        map.put(RisorseAziendaDAO.DT_FINE_RAPPORTO, risorseAzienda.getAttribute(RisorseAziendaDAO.DT_FINE_RAPPORTO));
        
    }

    private String getListaAziende() throws AppCrash {

        DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();

        String dsName=DATASET_RISORSE_AZIENDA;
        String listaAziende="";
        
        try {
            dataSet = dsFactory.makeDataSet("", dsName);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put(ID_RISORSA, getAttribute(ID_RISORSA));
            dataSet.setParam(params);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                Integer keyValue = (Integer) dbRow.getField(RisorseAziendaDAO.ID_AZIENDA);
                listaAziende+=keyValue;
            }
        } catch (Throwable th) {
            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "error using dataset " + dsName);
            throw ac;
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "error closing dataset " + dsName);
                }
            }
        }
        return listaAziende;
    }

}