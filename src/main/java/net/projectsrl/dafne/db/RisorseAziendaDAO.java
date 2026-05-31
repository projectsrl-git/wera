
package net.projectsrl.dafne.db;

import java.util.Map;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class RisorseAziendaDAO extends PjNDAO_base {

    private static final String TABLE_NAME         = "RISORSE_AZIENDA";

    public static final String  ID_RISORSA_AZIENDA = "ID_RISORSA_AZIENDA";
    public static final String  ID_RISORSA         = "ID_RISORSA";
    public static final String  ID_AZIENDA         = "ID_AZIENDA";

    public static final String  MATRICOLA          = "MATRICOLA";
    public static final String  DT_INIZIO_RAPPORTO = "DT_INIZIO_RAPPORTO";
    public static final String  DT_FINE_RAPPORTO   = "DT_FINE_RAPPORTO";
    public static final String  FL_IN_CORSO        = "FL_IN_CORSO";
    public static final String  FL_ULTIMO_RAPPORTO = "FL_ULTIMO_RAPPORTO";

    public RisorseAziendaDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public RisorseAziendaDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public RisorseAziendaDAO(DBTransaction transact, String tableName) throws AppCrash {

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

        if (Util.IsNotEmpty(getAttribute(ID_RISORSA)) && Util.IsNotEmpty(getAttribute(FL_ULTIMO_RAPPORTO))) {
            appendField(ID_RISORSA, whereCondition);
            appendField(FL_ULTIMO_RAPPORTO, whereCondition);
        } else if (Util.IsNotEmpty(getAttribute(ID_RISORSA_AZIENDA))) {
            appendField(ID_RISORSA_AZIENDA, whereCondition);
        }

        return whereCondition;

    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_RISORSA_AZIENDA, Integer.class);
        addNoStringField(ID_RISORSA, Integer.class);
        addNoStringField(ID_AZIENDA, Integer.class);
        addNoStringField(FL_IN_CORSO, Boolean.class);
        addNoStringField(FL_ULTIMO_RAPPORTO, Boolean.class);

    }

    @Override
    public void setMapFromAttributes(Map<String, Object> map) throws AppCrash {

        super.setMapFromAttributes(map);

    }

}