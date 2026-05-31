
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

public class NewsAziendeDAO extends PjNDAO_base implements ForeingnKeysRows_itf {

    private static final String TABLE_NAME            = "NEWS_AZIENDE";
    private static final String DATA_SET_AZIENDE_NEWS = "DataSetAziendeNews";

    public static final String  ID_NEWS_AZIENDA       = "ID_NEWS_AZIENDA";
    public static final String  ID_NEWS               = "ID_NEWS";
    public static final String  ID_AZIENDA            = "ID_AZIENDA";

    public NewsAziendeDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public NewsAziendeDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public NewsAziendeDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);

    }

    private class DeleteAziendeNews extends DeleteQueryRows<NewsAziendeDAO, Integer> {

        public DeleteAziendeNews() {
            super(DATA_SET_AZIENDE_NEWS, ID_NEWS, ID_NEWS_AZIENDA);

        }

    }

    private class InsertNewsAziende extends InsertMultipleReferences<NewsAziendeDAO> {

        public InsertNewsAziende() {
            super(DATA_SET_AZIENDE_NEWS, ID_NEWS, ID_AZIENDA);
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

        if (Util.IsNotEmpty(getAttribute(ID_NEWS_AZIENDA))) {
            appendField(ID_NEWS_AZIENDA, whereCondition);
        }

        return whereCondition;

    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_NEWS_AZIENDA, Integer.class);
        addNoStringField(ID_NEWS, Integer.class);
        addNoStringField(ID_AZIENDA, Integer.class);

    }

    public void insert(String aziende, Integer idNews) throws AppCrash {

        new InsertNewsAziende().insert(aziende, idNews);

    }

    public String getSelectedCodeList(String idNews) throws AppCrash {

        return new InsertNewsAziende().getSelectedCodeList(idNews);
    }

    @Override
    public void delete(String idNews) throws AppCrash {

        new DeleteAziendeNews().delete(idNews);

    }

}