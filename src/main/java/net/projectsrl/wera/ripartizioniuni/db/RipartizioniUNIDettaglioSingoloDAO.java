
package net.projectsrl.wera.ripartizioniuni.db;

import java.math.BigDecimal;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDettagliDAO_base;

public class RipartizioniUNIDettaglioSingoloDAO extends AliModDettagliDAO_base {

    //private static final String TABLE_PARENT_NAME = "RIPARTIZIONI_UNI";

    private static final String TABLE_NAME          = "RIPARTIZIONI_UNI_DETTAGLIO_SINGOLO";

    public static final String   DENOMINAZIONE = "DENOMINAZIONE";
    public static final String   LETTURA = "LETTURA";
    public static final String   LETTURA_ACS = "LETTURA_ACS";
    public static final String   STANZA = "STANZA";
    public static final String   RILEVATORE = "RILEVATORE";
    
    public static final String  CONSUMI_ENERGIA_TERMICA_RISCALDAMENTO           = "CONSUMI_ENERGIA_TERMICA_RISCALDAMENTO";
    public static final String  CONSUMI_ENERGIA_TERMICA_ACS            = "CONSUMI_ENERGIA_TERMICA_ACS";
    public static final String  CONSUMO_INVOLONTARIO_ENERGIA_TERMICA_ACS           = "CONSUMO_INVOLONTARIO_ENERGIA_TERMICA_ACS";
    
    public static final String  SPESA_ENERGIA_TERMICA_RISCALDAMENTO        = "SPESA_ENERGIA_TERMICA_RISCALDAMENTO";
    public static final String  SPESA_ENERGIA_TERMICA_ACS        = "SPESA_ENERGIA_TERMICA_ACS";
    public static final String  SPESA_POTENZA_TERMICA_RISCALDAMENTO        = "SPESA_POTENZA_TERMICA_RISCALDAMENTO";
    public static final String  SPESA_POTENZA_TERMICA_ACS        = "SPESA_POTENZA_TERMICA_ACS";
    public static final String  SPESA_TOTALE_RISCALDAMENTO        = "SPESA_TOTALE_RISCALDAMENTO";
    public static final String  SPESA_TOTALE_ACS        = "SPESA_TOTALE_ACS";
    public static final String  SPESA_TOTALE_APPARTAMENTO        = "SPESA_TOTALE_APPARTAMENTO";
    public static final String  NUMERO_RIPARTITORI        = "NUMERO_RIPARTITORI";
    
	
    public static final String   TABELLA_PARENT         = "TABELLA_PARENT";
    public static final String   ID_DETTAGLIO_PARENT    = "ID_DETTAGLIO_PARENT";
    public static final String   ID_CONDOMINO    = "ID_CONDOMINO";
    

    public RipartizioniUNIDettaglioSingoloDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public RipartizioniUNIDettaglioSingoloDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public RipartizioniUNIDettaglioSingoloDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_CONDOMINO, Integer.class);
        addNoStringField(LETTURA, Integer.class);
        addNoStringField(CONSUMI_ENERGIA_TERMICA_RISCALDAMENTO, BigDecimal.class);
        addNoStringField(CONSUMI_ENERGIA_TERMICA_ACS, BigDecimal.class);
        addNoStringField(CONSUMO_INVOLONTARIO_ENERGIA_TERMICA_ACS, BigDecimal.class);
        addNoStringField(LETTURA_ACS, BigDecimal.class);
        addNoStringField(SPESA_ENERGIA_TERMICA_RISCALDAMENTO,BigDecimal.class);
        addNoStringField(SPESA_ENERGIA_TERMICA_ACS, BigDecimal.class);
        addNoStringField(SPESA_POTENZA_TERMICA_RISCALDAMENTO, BigDecimal.class);
        addNoStringField(SPESA_POTENZA_TERMICA_ACS, BigDecimal.class);
        addNoStringField(SPESA_TOTALE_RISCALDAMENTO, BigDecimal.class);
        addNoStringField(SPESA_TOTALE_ACS, BigDecimal.class);
        addNoStringField(SPESA_TOTALE_APPARTAMENTO, BigDecimal.class);
                     
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
        RipartizioniUNIDettaglioSingoloDAO daoOld=new RipartizioniUNIDettaglioSingoloDAO();
        daoOld.setAttribute(ID_DETTAGLIO, idDettaglio);
        daoOld.retrieve();
        String nr=(String) daoOld.getAttribute(NR_DETTAGLIO);
                
        
        if (Util.IsEmpty((String) getAttribute(NR_DETTAGLIO))) {
            setAttribute(NR_DETTAGLIO, nr);
        }
        
        super.update();
    }
}
