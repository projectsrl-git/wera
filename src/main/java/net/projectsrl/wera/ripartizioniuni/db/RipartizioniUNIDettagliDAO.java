
package net.projectsrl.wera.ripartizioniuni.db;

import java.math.BigDecimal;
import java.sql.Timestamp;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDettagliDAO_base;
import net.projectsrl.wera.ripartizioni.db.RipartizioniDettaglioDAO;

public class RipartizioniUNIDettagliDAO extends AliModDettagliDAO_base {
	private static final String TABLE_NAME          = "RIPARTIZIONI_UNI_DETTAGLIO";
	public static final String  ID_AZIENDA = "ID_AZIENDA";
    public static final String  ID_CONDOMINO   = "ID_CONDOMINO";
    
	public static final String  DENOMINAZIONE = "DENOMINAZIONE";
    public static final String  LETTURA   = "LETTURA";
    public static final String  LETTURA_ACS   = "LETTURA_ACS";
    public static final String  MILLESIMI_CLIMA = "MILLESIMI_CLIMA";

    public static final String  MILLESIMI_ACS           = "MILLESIMI_ACS";
    public static final String  CONSUMI_ENERGIA_TERMICA_RISCALDAMENTO           = "CONSUMI_ENERGIA_TERMICA_RISCALDAMENTO";
    public static final String  CONSUMI_ENERGIA_TERMICA_ACS            = "CONSUMI_ENERGIA_TERMICA_ACS";
    public static final String  CONSUMO_TOTALE_EDIFICIO_ENERGIA_TERMICA_RISCALDAMENTO          = "CONSUMO_TOTALE_EDIFICIO_ENERGIA_TERMICA_RISCALDAMENTO";
    public static final String  CONSUMO_TOTALE_EDIFICIO_ENERGIA_TERMICA_ACS           = "CONSUMO_TOTALE_EDIFICIO_ENERGIA_TERMICA_ACS";
    public static final String  CONSUMO_INVOLONTARIO_ENERGIA_TERMICA_ACS           = "CONSUMO_INVOLONTARIO_ENERGIA_TERMICA_ACS";
    public static final String  SPESA_TOTALE_EDIFICIO_ENERGIA_TERMICA_RISCALDAMENTO              = "SPESA_TOTALE_EDIFICIO_ENERGIA_TERMICA_RISCALDAMENTO";
    public static final String  SPESA_TOTALE_EDIFICIO_ENERGIA_TERMICA_ACS           = "SPESA_TOTALE_EDIFICIO_ENERGIA_TERMICA_ACS";
    public static final String  SPESA_TOTALE_EDIFICIO_POTENZA_TERMICA_RISCALDAMENTO             = "SPESA_TOTALE_EDIFICIO_POTENZA_TERMICA_RISCALDAMENTO";
    public static final String  SPESA_TOTALE_EDIFICIO_POTENZA_TERMICA_ACS             = "SPESA_TOTALE_EDIFICIO_POTENZA_TERMICA_ACS";
    public static final String  SPESA_ENERGIA_TERMICA_RISCALDAMENTO        = "SPESA_ENERGIA_TERMICA_RISCALDAMENTO";
    public static final String  SPESA_ENERGIA_TERMICA_ACS        = "SPESA_ENERGIA_TERMICA_ACS";
    public static final String  SPESA_POTENZA_TERMICA_RISCALDAMENTO        = "SPESA_POTENZA_TERMICA_RISCALDAMENTO";
    public static final String  SPESA_POTENZA_TERMICA_ACS        = "SPESA_POTENZA_TERMICA_ACS";
    public static final String  SPESA_TOTALE_RISCALDAMENTO        = "SPESA_TOTALE_RISCALDAMENTO";
    public static final String  SPESA_TOTALE_ACS        = "SPESA_TOTALE_ACS";
    public static final String  SPESA_TOTALE_APPARTAMENTO        = "SPESA_TOTALE_APPARTAMENTO";
    public static final String  NUMERO_RIPARTITORI        = "NUMERO_RIPARTITORI";
    
    
    public static final String  TS_INS        = "TS_INS";
    public static final String  ID_UTENTE_INS        = "ID_UTENTE_INS";
    public static final String  TS_MOD        = "TS_MOD";
    public static final String  ID_UTENTE_MOD        = "ID_UTENTE_MOD";
    
 
    
    public RipartizioniUNIDettagliDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public RipartizioniUNIDettagliDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public RipartizioniUNIDettagliDAO(DBTransaction transact, String tableName) throws AppCrash {

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
        addNoStringField(CONSUMI_ENERGIA_TERMICA_RISCALDAMENTO, BigDecimal.class);
        addNoStringField(CONSUMI_ENERGIA_TERMICA_ACS, BigDecimal.class);
        addNoStringField(CONSUMO_TOTALE_EDIFICIO_ENERGIA_TERMICA_RISCALDAMENTO, BigDecimal.class);
        addNoStringField(CONSUMO_TOTALE_EDIFICIO_ENERGIA_TERMICA_ACS, BigDecimal.class);
        addNoStringField(CONSUMO_INVOLONTARIO_ENERGIA_TERMICA_ACS, BigDecimal.class);
        addNoStringField(SPESA_TOTALE_EDIFICIO_ENERGIA_TERMICA_RISCALDAMENTO, BigDecimal.class);
        addNoStringField(SPESA_TOTALE_EDIFICIO_ENERGIA_TERMICA_ACS, BigDecimal.class);
        addNoStringField(SPESA_TOTALE_EDIFICIO_POTENZA_TERMICA_RISCALDAMENTO, BigDecimal.class);
        addNoStringField(SPESA_TOTALE_EDIFICIO_POTENZA_TERMICA_ACS, BigDecimal.class);
        addNoStringField(SPESA_ENERGIA_TERMICA_RISCALDAMENTO,BigDecimal.class);
        addNoStringField(SPESA_ENERGIA_TERMICA_ACS, BigDecimal.class);
        addNoStringField(SPESA_POTENZA_TERMICA_RISCALDAMENTO, BigDecimal.class);
        addNoStringField(SPESA_POTENZA_TERMICA_ACS, BigDecimal.class);
        addNoStringField(SPESA_TOTALE_RISCALDAMENTO, BigDecimal.class);
        addNoStringField(SPESA_TOTALE_ACS, BigDecimal.class);
        addNoStringField(SPESA_TOTALE_APPARTAMENTO, BigDecimal.class);
        
        
        

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
