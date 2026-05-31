
package net.projectsrl.wera.ripartizioniuni2018.db;

import java.math.BigDecimal;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDettagliDAO_base;

public class RipartizioniUNI2018DettaglioSingoloDAO extends AliModDettagliDAO_base {

    //private static final String TABLE_PARENT_NAME = "RIPARTIZIONI_UNI_2018";

    private static final String TABLE_NAME          = "RIPARTIZIONI_UNI_2018_DETTAGLIO_SINGOLO";

    public static final String   DENOMINAZIONE = "DENOMINAZIONE";
    public static final String   CONTATORE_LETTURA_INIZIALE = "CONTATORE_LETTURA_INIZIALE";
    public static final String   CONTATORE_LETTURA_FINALE = "CONTATORE_LETTURA_FINALE";
    public static final String   CONTATORE_LETTURA = "CONTATORE_LETTURA";
    
    public static final String   STANZA = "STANZA";
    public static final String   RILEVATORE = "RILEVATORE";
    public static final String   PROGR = "PROGR";
    public static final String   K_C = "K_C";
    public static final String   K_Q = "K_Q";
    public static final String   K_T = "K_T";
    public static final String   K = "K";
    
    public static final String   RIPARTITORE_LETTURA_INIZIALE = "RIPARTITORE_LETTURA_INIZIALE";
    public static final String   RIPARTITORE_LETTURA_FINALE = "RIPARTITORE_LETTURA_FINALE";
    public static final String   RIPARTITORE_LETTURA = "RIPARTITORE_LETTURA";
    
    public static final String  NUMERO_RIPARTITORI        = "NUMERO_RIPARTITORI";
        
	
    public static final String   TABELLA_PARENT         = "TABELLA_PARENT";
    public static final String   ID_DETTAGLIO_PARENT    = "ID_DETTAGLIO_PARENT";
    public static final String   ID_CONDOMINO    = "ID_CONDOMINO";
    

    public RipartizioniUNI2018DettaglioSingoloDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public RipartizioniUNI2018DettaglioSingoloDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public RipartizioniUNI2018DettaglioSingoloDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_CONDOMINO, Integer.class);
        
        addNoStringField(CONTATORE_LETTURA_INIZIALE, BigDecimal.class);
        addNoStringField(CONTATORE_LETTURA_FINALE, BigDecimal.class);
        addNoStringField(CONTATORE_LETTURA, BigDecimal.class);

        addNoStringField(K_C, BigDecimal.class);
        addNoStringField(K_Q, BigDecimal.class);
        addNoStringField(K_T, BigDecimal.class);
        addNoStringField(K, BigDecimal.class);
        
        addNoStringField(RIPARTITORE_LETTURA_INIZIALE, Integer.class);
        addNoStringField(RIPARTITORE_LETTURA_FINALE, Integer.class);
        addNoStringField(RIPARTITORE_LETTURA, Integer.class);
        
        addNoStringField(NUMERO_RIPARTITORI, Integer.class);
                     
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
        RipartizioniUNI2018DettaglioSingoloDAO daoOld=new RipartizioniUNI2018DettaglioSingoloDAO();
        daoOld.setAttribute(ID_DETTAGLIO, idDettaglio);
        daoOld.retrieve();
        String nr=(String) daoOld.getAttribute(NR_DETTAGLIO);
                
        
        if (Util.IsEmpty((String) getAttribute(NR_DETTAGLIO))) {
            setAttribute(NR_DETTAGLIO, nr);
        }
        
        super.update();
    }
}
