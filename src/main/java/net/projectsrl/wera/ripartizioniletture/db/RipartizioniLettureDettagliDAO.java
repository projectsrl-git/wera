
package net.projectsrl.wera.ripartizioniletture.db;

import java.math.BigDecimal;
import java.sql.Timestamp;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDettagliDAO_base;
import net.projectsrl.wera.ripartizioni.db.RipartizioniDettaglioDAO;

public class RipartizioniLettureDettagliDAO extends AliModDettagliDAO_base {
	private static final String TABLE_NAME          = "RIPARTIZIONI_LETTURE_DETTAGLIO";
	public static final String  ID_AZIENDA = "ID_AZIENDA";
    public static final String  ID_CONDOMINO   = "ID_CONDOMINO";
    
	public static final String  DENOMINAZIONE = "DENOMINAZIONE";
    public static final String  LETTURA   = "LETTURA";
    public static final String  LETTURA_ACS   = "LETTURA_ACS";
    public static final String  MILLESIMI_CLIMA = "MILLESIMI_CLIMA";
    public static final String  MILLESIMI_ACS           = "MILLESIMI_ACS";
    public static final String  NUMERO_RIPARTITORI        = "NUMERO_RIPARTITORI";
    
    public static final String  TS_INS        = "TS_INS";
    public static final String  ID_UTENTE_INS        = "ID_UTENTE_INS";
    public static final String  TS_MOD        = "TS_MOD";
    public static final String  ID_UTENTE_MOD        = "ID_UTENTE_MOD";
    
    public static final String   LETTURA_AFS = "LETTURA_AFS";
    
 
    
    public RipartizioniLettureDettagliDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public RipartizioniLettureDettagliDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public RipartizioniLettureDettagliDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_MODULO, Integer.class);
        addNoStringField(ID_CONDOMINO, Integer.class);
        addNoStringField(ID_AZIENDA, Integer.class);
        
       
        addNoStringField(LETTURA, BigDecimal.class);
        addNoStringField(LETTURA_ACS, BigDecimal.class);
        addNoStringField(MILLESIMI_CLIMA, BigDecimal.class);
        addNoStringField(MILLESIMI_ACS, BigDecimal.class);
        
        addNoStringField(LETTURA_AFS, BigDecimal.class);
       
        addNoStringField(TS_INS, Timestamp.class);
        addNoStringField(ID_UTENTE_INS, Integer.class);
        addNoStringField(TS_MOD, Timestamp.class);
        addNoStringField(ID_UTENTE_MOD, Integer.class);

        
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
