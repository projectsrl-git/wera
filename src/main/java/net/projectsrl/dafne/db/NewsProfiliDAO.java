
package net.projectsrl.dafne.db;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.webapp.core.DeleteQueryRows;
import net.projectsrl.webapp.core.ForeingnKeysRows_itf;
import net.projectsrl.webapp.core.InsertMultipleReferences;

public class NewsProfiliDAO extends PjNDAO_base implements ForeingnKeysRows_itf {

    private static final String TABLE_NAME              = "NEWS_PROFILI";
    private static final String DATA_SET_PROFILI_NEWS = "DataSetProfiliNews";

    public static final String  ID_NEWS_PROFILO         = "ID_NEWS_PROFILO";
    public static final String  ID_NEWS                 = "ID_NEWS";
    public static final String  ID_PROFILO              = "ID_PROFILO";

    public NewsProfiliDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public NewsProfiliDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public NewsProfiliDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);

    }

    private class DeleteProfiliNews extends DeleteQueryRows<NewsProfiliDAO, Integer> {

        public DeleteProfiliNews() {
            super(DATA_SET_PROFILI_NEWS, NewsProfiliDAO.ID_NEWS, NewsProfiliDAO.ID_NEWS_PROFILO);
        }

    }

    private class InsertProfiliNews extends InsertMultipleReferences<NewsProfiliDAO> {

        public InsertProfiliNews() {
            super(DATA_SET_PROFILI_NEWS,ID_NEWS, ID_PROFILO);
        }

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

        if (Util.IsNotEmpty(getAttribute(ID_NEWS_PROFILO))) {
            appendField(ID_NEWS_PROFILO, whereCondition);
        }

        return whereCondition;

    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_NEWS_PROFILO, Integer.class);
        addNoStringField(ID_NEWS, Integer.class);
        addNoStringField(ID_PROFILO, Integer.class);

    }

    public void insert(String codeList, Integer keyId) throws AppCrash {

        new InsertProfiliNews().insert(codeList, keyId);

    }

    @Override
    public void delete(String idNews) throws AppCrash {

        new DeleteProfiliNews().delete(idNews);

    }

    public Object getSelectedCodeList(String idNews) throws AppCrash {

        return new InsertProfiliNews().getSelectedCodeList(idNews);
    }

}