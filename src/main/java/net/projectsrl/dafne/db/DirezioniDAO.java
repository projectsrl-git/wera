
package net.projectsrl.dafne.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.project.servlet.frame.SsbServletRequest;
import net.projectsrl.dafne.core.DafneCostanti_itf;
import net.projectsrl.db.PjNDAO_base;

public class DirezioniDAO extends PjNDAO_base {

    private static final String TABLE_NAME           = "DIREZIONI";

    public static final String  ID_DIREZIONE         = "ID_DIREZIONE";
    public static final String  NOME_DIREZIONE       = "NOME_DIREZIONE";
    public static final String  NOME_DIREZIONE_BREVE = "NOME_DIREZIONE_BREVE";
    public static final String  ID_AZIENDA           = "ID_AZIENDA";
    public static final String  ID_DIREZIONE_PARENT  = "ID_DIREZIONE_PARENT";
    
    private String              _staff          = null;

    public DirezioniDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public DirezioniDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public DirezioniDAO(DBTransaction transact, String tableName) throws AppCrash {

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

        if (Util.IsNotEmpty(getAttribute(ID_DIREZIONE))) {
            appendField(ID_DIREZIONE, whereCondition);
        } else if (Util.IsNotEmpty(getAttribute(NOME_DIREZIONE))) {
            appendField(NOME_DIREZIONE, whereCondition);
        } else if (Util.IsNotEmpty(getAttribute(NOME_DIREZIONE_BREVE))) {
            appendField(NOME_DIREZIONE_BREVE, whereCondition);
        }

        return whereCondition;
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_DIREZIONE, Integer.class);
        addNoStringField(ID_DIREZIONE_PARENT, Integer.class);
        addNoStringField(ID_AZIENDA, Integer.class);
    }
    
    
    
    
    
    
    
    
    
    @Override
    public void insert() throws AppCrash {

        super.insert();
        
       
        Integer idDirezione = new TableIdentity(TABLE_NAME).getTableIdentity();

        // inserisce staff
        new StaffDirezioniDAO().insert(_staff, idDirezione);

    }

    @Override
    public void delete() throws AppCrash {

        String idDirezione = getAttributeAsString(ID_DIREZIONE);
        new StaffDirezioniDAO().delete(idDirezione);
        super.delete();

    }

    @Override
    public void update() throws AppCrash {

        super.update();

        Integer idDirezione = (Integer) getAttribute(ID_DIREZIONE);
        String idDirezioneString = idDirezione.toString();

        if (Util.IsNotEmpty(_staff)) {
            new StaffDirezioniDAO().delete(idDirezioneString);
            new StaffDirezioniDAO().insert(_staff, idDirezione);
        }

    }

    @Override
    public void setAttributesFromRequest(SsbServletRequest req) throws AppCrash {

        super.setAttributesFromRequest(req);

        _staff = req.getField(DafneCostanti_itf.RISORSE_MULTIPLE);

    }
    
    
    
    
}