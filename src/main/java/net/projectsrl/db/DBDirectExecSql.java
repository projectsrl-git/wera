package net.projectsrl.db;

import net.project.db.DBDirectQuery;
import net.project.db.DBTransaction;
import net.project.errors.AppCrash;


public class DBDirectExecSql extends DBDirectQuery {
    
    private String _query;


    public DBDirectExecSql(String queryName, DBTransaction transact) throws AppCrash {

        super(queryName, transact);
    }

    public DBDirectExecSql(String configName, String queryName) throws AppCrash {

        super(configName, queryName);
    }

    public DBDirectExecSql(String query) throws AppCrash {
        super("");
        _query =  query;
    }
    

    @Override
    protected String getQuery() {
        
        return _query;
    }
    
}
