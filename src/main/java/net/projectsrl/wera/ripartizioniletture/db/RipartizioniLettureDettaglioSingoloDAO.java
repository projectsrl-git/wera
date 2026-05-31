
package net.projectsrl.wera.ripartizioniletture.db;

import java.math.BigDecimal;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDettagliDAO_base;

public class RipartizioniLettureDettaglioSingoloDAO extends AliModDettagliDAO_base {

    //private static final String TABLE_PARENT_NAME = "RIPARTIZIONI_LETTURE";

    private static final String TABLE_NAME          = "RIPARTIZIONI_LETTURE_DETTAGLIO_SINGOLO";

    public static final String   DENOMINAZIONE = "DENOMINAZIONE";
    public static final String   LETTURA = "LETTURA";
    public static final String   LETTURA_ACS = "LETTURA_ACS";
    public static final String   STANZA = "STANZA";
    public static final String   RILEVATORE = "RILEVATORE";
    public static final String  NUMERO_RIPARTITORI        = "NUMERO_RIPARTITORI";
    
    public static final String   ID_CONDOMINO    = "ID_CONDOMINO";
    
    public static final String   LETTURA_AFS = "LETTURA_AFS";
    

    public RipartizioniLettureDettaglioSingoloDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public RipartizioniLettureDettaglioSingoloDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public RipartizioniLettureDettaglioSingoloDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_CONDOMINO, Integer.class);
        addNoStringField(LETTURA, Integer.class);
        addNoStringField(LETTURA_ACS, BigDecimal.class);
        addNoStringField(LETTURA_AFS, BigDecimal.class);
                     
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
        RipartizioniLettureDettaglioSingoloDAO daoOld=new RipartizioniLettureDettaglioSingoloDAO();
        daoOld.setAttribute(ID_DETTAGLIO, idDettaglio);
        daoOld.retrieve();
        String nr=(String) daoOld.getAttribute(NR_DETTAGLIO);
                
        
        if (Util.IsEmpty((String) getAttribute(NR_DETTAGLIO))) {
            setAttribute(NR_DETTAGLIO, nr);
        }
        
        super.update();
    }
}
