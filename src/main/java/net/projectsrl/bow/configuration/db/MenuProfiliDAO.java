
package net.projectsrl.bow.configuration.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public class MenuProfiliDAO extends PjNDAO_base {

    private static final String TABLE_NAME      = "MENU_PROFILI";

    public static final String  ID_MENU_PROFILI = "ID_MENU_PROFILI";
    public static final String  ID_MENU         = "ID_MENU";
    public static final String  ID_PROFILO      = "ID_PROFILO";


    public MenuProfiliDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public MenuProfiliDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public MenuProfiliDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);

    }
    
    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_MENU_PROFILI, Integer.class);
        addNoStringField(ID_MENU, Integer.class);
        addNoStringField(ID_PROFILO, Integer.class);

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

        if (Util.IsNotEmpty(getAttribute(ID_MENU_PROFILI))) {
            appendField(ID_MENU_PROFILI, whereCondition);
        }

        return whereCondition;

    }

}