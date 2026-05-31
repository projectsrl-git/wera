
package project.db;

import net.project.dataset.DBDataSet;
import net.project.errors.AppCrash;
import net.project.misc.Config;

public class ProvvisorioDataSet extends DBDataSet {

    public ProvvisorioDataSet(String configName, String dsName) throws AppCrash {

        super(configName, dsName);
    }

    @Override
    protected void getDbConnection() throws AppCrash {

        String dburl = Config.GetInstance().getProperty("DB.ConnectionURL");
        Config.GetInstance().setProperty("DB.ConnectionURL", "jdbc:jtds:sqlserver://localhost/AQ_PROJECTSRL");

        super.getDbConnection();
        Config.GetInstance().setProperty("DB.ConnectionURL", dburl);
    }

}
