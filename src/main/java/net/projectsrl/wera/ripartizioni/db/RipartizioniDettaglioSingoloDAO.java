
package net.projectsrl.wera.ripartizioni.db;

import java.math.BigDecimal;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDettagliDAO_base;

public class RipartizioniDettaglioSingoloDAO extends AliModDettagliDAO_base {

    //private static final String TABLE_PARENT_NAME = "RIPARTIZIONI";

    private static final String TABLE_NAME          = "RIPARTIZIONI_DETTAGLIO_SINGOLO";

    public static final String   DENOMINAZIONE = "DENOMINAZIONE";
    public static final String   LETTURA = "LETTURA";
    public static final String   LETTURA_ACS = "LETTURA_ACS";
    public static final String   METANO = "METANO";
    public static final String   STANZA = "STANZA";
    public static final String   RILEVATORE = "RILEVATORE";
	
    public static final String   TABELLA_PARENT         = "TABELLA_PARENT";
    public static final String   ID_DETTAGLIO_PARENT    = "ID_DETTAGLIO_PARENT";
    public static final String   ID_CONDOMINO    = "ID_CONDOMINO";
    

    public RipartizioniDettaglioSingoloDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public RipartizioniDettaglioSingoloDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public RipartizioniDettaglioSingoloDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_CONDOMINO, Integer.class);
        addNoStringField(LETTURA, Integer.class);
        addNoStringField(METANO, BigDecimal.class);
        addNoStringField(LETTURA_ACS, BigDecimal.class);
                     
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
        RipartizioniDettaglioSingoloDAO daoOld=new RipartizioniDettaglioSingoloDAO();
        daoOld.setAttribute(ID_DETTAGLIO, idDettaglio);
        daoOld.retrieve();
        String nr=(String) daoOld.getAttribute(NR_DETTAGLIO);
                
        
        if (Util.IsEmpty((String) getAttribute(NR_DETTAGLIO))) {
            setAttribute(NR_DETTAGLIO, nr);
        }
        
        super.update();
    }
}
