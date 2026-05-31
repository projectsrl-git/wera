
package net.projectsrl.dafne.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.project.servlet.frame.SsbServletRequest;
import net.projectsrl.dafne.core.DafneCostanti_itf;
import net.projectsrl.db.PjNDAO_base;

/**
 * Classe che rappresenta la tabella DEALER Proprietà lette dal file di configurazione:
 * <p>
 * DATABASE.NAME = nome del database
 */
public class NewsDAO extends PjNDAO_base {

    private static final String TABLE_NAME             = "NEWS";

    public static final String  ID_NEWS                = "ID_NEWS";
    public static final String  TITOLO                 = "TITOLO";
    public static final String  DT_VDAL                = "DT_VDAL";
    public static final String  DT_VAL                 = "DT_VAL";
    public static final String  DESCRIZIONE            = "DESCRIZIONE";
    public static final String  ID_UTENTE_INS          = "ID_UTENTE_INS";

    private String              _profili               = null;
    private String              _aziende               = null;

    public NewsDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public NewsDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
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

        if (Util.IsNotEmpty(getAttribute(ID_NEWS))) {
            appendField(ID_NEWS, whereCondition);
        }

        return whereCondition;
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_NEWS, Integer.class);
    }

    @Override
    public void insert() throws AppCrash {

        super.insert();
        
        Integer rowId = new TableIdentity(TABLE_NAME).getTableIdentity();
        new NewsProfiliDAO().insert(_profili, rowId);
        
        // inserisce aziende
        new NewsAziendeDAO().insert(_aziende, rowId);

    }

    @Override
    public void delete() throws AppCrash {

        String idNews = getAttributeAsString(ID_NEWS);
        new NewsProfiliDAO().delete(idNews);
        new NewsAziendeDAO().delete(idNews);
        super.delete();
        
        

    }

    @Override
    public void update() throws AppCrash {

        super.update();

        Integer idNews = (Integer) getAttribute(ID_NEWS);
        String idNewsString = idNews.toString();

        if (Util.IsNotEmpty(_profili)) {
            new NewsProfiliDAO().delete(idNewsString);
            new NewsProfiliDAO().insert(_profili, idNews);
        }
        
        if (Util.IsNotEmpty(_aziende)) {
            new NewsAziendeDAO().delete(idNewsString);
            new NewsAziendeDAO().insert(_aziende,idNews);
        }
    }

    @Override
    public void setAttributesFromRequest(SsbServletRequest req) throws AppCrash {

        super.setAttributesFromRequest(req);

        _profili = req.getField(DafneCostanti_itf.PROFILO_MULTIPLO);
        _aziende = req.getField(DafneCostanti_itf.AZIENDE_MULTIPLE);

    }

}