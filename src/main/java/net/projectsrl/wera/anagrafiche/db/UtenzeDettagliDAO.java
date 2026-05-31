
package net.projectsrl.wera.anagrafiche.db;

import java.math.BigDecimal;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDettagliDAO_base;

public class UtenzeDettagliDAO extends AliModDettagliDAO_base {

    //private static final String TABLE_PARENT_NAME = "UTENZE";

    private static final String TABLE_NAME          = "UTENZE_DETTAGLIO";

    public static final String   TIPO_RILEVATORE         = "TIPO_RILEVATORE";
    public static final String   STANZA = "STANZA";
    public static final String   TIPO = "TIPO";
    public static final String   MARCA = "MARCA";
    public static final String   LARGHEZZA = "LARGHEZZA";
    public static final String   ALTEZZA = "ALTEZZA";
    public static final String   PROFONDITA = "PROFONDITA";
    public static final String   ELEMENTI = "ELEMENTI";
    public static final String   POTENZA = "POTENZA";
    public static final String   ESP = "ESP";
    public static final String   ESP_2 = "ESP_2";
	public static final String   RILEVATORE           = "RILEVATORE";
	public static final String   N_PROG           = "N_PROG";
	public static final String   COEFF           = "COEFF";
	public static final String   POS           = "POS";
	public static final String   DIAMETRO           = "DIAMETRO";
	public static final String   MAT_TUBO           = "MAT_TUBO";
	public static final String   PREREG           = "PREREG";
	public static final String   TIPO_VALVOLA           = "TIPO_VALVOLA";
    public static final String   TABELLA_PARENT         = "TABELLA_PARENT";
    public static final String   ID_DETTAGLIO_PARENT    = "ID_DETTAGLIO_PARENT";
    

    public UtenzeDettagliDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public UtenzeDettagliDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public UtenzeDettagliDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(LARGHEZZA, BigDecimal.class);
        addNoStringField(ALTEZZA, BigDecimal.class);
        addNoStringField(PROFONDITA, BigDecimal.class);
        addNoStringField(POTENZA, BigDecimal.class);
        addNoStringField(ESP, BigDecimal.class);
        addNoStringField(ESP_2, BigDecimal.class);
                
        addNoStringField(N_PROG, Integer.class);
        addNoStringField(COEFF, Integer.class);
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
        UtenzeDettagliDAO daoOld=new UtenzeDettagliDAO();
        daoOld.setAttribute(ID_DETTAGLIO, idDettaglio);
        daoOld.retrieve();
        String nr=(String) daoOld.getAttribute(NR_DETTAGLIO);
                
        
        if (Util.IsEmpty((String) getAttribute(NR_DETTAGLIO))) {
            setAttribute(NR_DETTAGLIO, nr);
        }
        
        super.update();
    }
}
