
package net.projectsrl.wera.ripartizioni.db;

import java.math.BigDecimal;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDettagliDAO_base;

public class RipartizioniDettaglioDAO extends AliModDettagliDAO_base {

    //private static final String TABLE_PARENT_NAME = "RIPARTIZIONI";

    private static final String TABLE_NAME          = "RIPARTIZIONI_DETTAGLIO";

    public static final String   DENOMINAZIONE = "DENOMINAZIONE";
    public static final String   MILLESIMI = "MILLESIMI";
    public static final String   NUMERO_RIPARTITORI = "NUMERO_RIPARTITORI";
    public static final String   LETTURA = "LETTURA";
    public static final String   LETTURA_ACS = "LETTURA_ACS";
    public static final String   METANO_FISSO = "METANO_FISSO";
    public static final String   METANO_MILLESIMI = "METANO_MILLESIMI";
    public static final String   FORZA_MOTRICE = "FORZA_MOTRICE";
    public static final String   CONDUZIONE = "CONDUZIONE";
    public static final String   CONDUZIONE_ANTICIPO = "CONDUZIONE_ANTICIPO";
    public static final String   CONDUZIONE_SALDO = "CONDUZIONE_SALDO";
	public static final String   MANUTENZIONE_ORDINARIA           = "MANUTENZIONE_ORDINARIA";
	public static final String   MANUTENZIONE_STRAORDINARIA           = "MANUTENZIONE_STRAORDINARIA";
	public static final String   COSTI_LETTURE           = "COSTI_LETTURE";
	public static final String   TOTALE_SENZA_LETTURE           = "TOTALE_SENZA_LETTURE";
	public static final String   TOTALE_CON_LETTURE           = "TOTALE_CON_LETTURE";
	
	public static final String   METANO = "METANO";
	public static final String   SUBTOTALE_COSTI_MILLESIMI = "SUBTOTALE_COSTI_MILLESIMI";
	
    public static final String   TABELLA_PARENT         = "TABELLA_PARENT";
    public static final String   ID_DETTAGLIO_PARENT    = "ID_DETTAGLIO_PARENT";
    public static final String   ID_CONDOMINO    = "ID_CONDOMINO";
    

    public RipartizioniDettaglioDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public RipartizioniDettaglioDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public RipartizioniDettaglioDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_CONDOMINO, Integer.class);
        addNoStringField(MILLESIMI, BigDecimal.class);
        addNoStringField(NUMERO_RIPARTITORI, Integer.class);
        addNoStringField(LETTURA, Integer.class);
        addNoStringField(LETTURA_ACS, BigDecimal.class);
        addNoStringField(METANO_FISSO, BigDecimal.class);
        addNoStringField(METANO_MILLESIMI, BigDecimal.class);
        addNoStringField(FORZA_MOTRICE, BigDecimal.class);
        addNoStringField(CONDUZIONE, BigDecimal.class);
        addNoStringField(CONDUZIONE_ANTICIPO, BigDecimal.class);
        addNoStringField(CONDUZIONE_SALDO, BigDecimal.class);
        addNoStringField(MANUTENZIONE_ORDINARIA, BigDecimal.class);
        addNoStringField(MANUTENZIONE_STRAORDINARIA, BigDecimal.class);
        addNoStringField(COSTI_LETTURE, BigDecimal.class);
        addNoStringField(TOTALE_SENZA_LETTURE, BigDecimal.class);
        addNoStringField(TOTALE_CON_LETTURE, BigDecimal.class);
        
        addNoStringField(METANO, BigDecimal.class);
        addNoStringField(SUBTOTALE_COSTI_MILLESIMI, BigDecimal.class);
                     
        addNoStringField(ID_DETTAGLIO_PARENT, Integer.class);
    }
    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_DETTAGLIO))) {
            appendField(ID_DETTAGLIO, whereCondition);
        } else if (Util.IsNotEmpty(getAttribute(NR_DETTAGLIO))) {
            appendField(NR_DETTAGLIO, whereCondition);
        }

        return whereCondition;
    }
    
    

    @Override
    public void update() throws AppCrash {
        
        Integer idDettaglio=(Integer) getAttribute(ID_DETTAGLIO);
        RipartizioniDettaglioDAO daoOld=new RipartizioniDettaglioDAO();
        daoOld.setAttribute(ID_DETTAGLIO, idDettaglio);
        daoOld.retrieve();
        String nr=(String) daoOld.getAttribute(NR_DETTAGLIO);
                
        
        if (Util.IsEmpty((String) getAttribute(NR_DETTAGLIO))) {
            setAttribute(NR_DETTAGLIO, nr);
        }
        
        super.update();
    }
}
