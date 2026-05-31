/*

Copyright (c) by SSB spa Societa' per i Servizi Bancari

Autore: Simone Z. 

Note:

 */

package net.project.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.DBCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.errors.ParamCrash;
import net.project.misc.Config;
import net.project.misc.TextTemplate;

/**
 * La classe serve per eseguire una query di insert o update direttamente senza utilizzare un DAO. Deve essere
 * utilizzata in casi del tutto particolare che non possono essere trattati con i DAO. Ad esempio per eseguire query di
 * insert multiple o di update multiple
 * <p>
 * <br>
 * La query da eseguire viene letta dalla proprieta "DirectQuery." + _queryName + ".Query" del file di configurazione.
 */
public class DBDirectQuery {

    private String           _sql          = null;
    private String           _configName   = "";
    private String           _queryName    = null;
    private DBConnection_itf _dbConnection = null;
    private Statement        _statement    = null;
    private DBTransaction    _transaction  = null;
    private String           _dbConfigName = null;

    /**
     * Costruttore che prende in ingresso il nome della DirectQuery da eseguire
     * 
     * @param queryName nome della DirectQuery da eseguire
     * @throws AppCrash
     */
    public DBDirectQuery(String queryName) throws AppCrash {

        ErrDetector.GetInstance().param(queryName);
        _queryName = queryName;

        _dbConfigName = Config.GetInstance(_configName).getProperty("DirectQuery." + _queryName + ".DBcfg", "");
    }

    /**
     * Costruttore che prende in ingresso il nome della DirectQuery da eseguire e la transaction da usare per inoltrare
     * la query al DB
     * 
     * @param queryName nome della DirectQuery da eseguire
     * @param transact transaction da usare
     * @throws AppCrash
     */
    public DBDirectQuery(String queryName, DBTransaction transact) throws AppCrash {

        this(queryName);
        ErrDetector.GetInstance().param(transact);
        _transaction = transact;
    }

    /**
     * Costruttore che prende in ingresso il nome della DirectQuery da eseguire ed il nome della configurazione da usare
     * per recuperare i parametri di connessione al DB e la query da eseguire
     * 
     * @param configName nome della configurazione
     * @param queryName nome della query da eseguire
     * @throws AppCrash
     */
    public DBDirectQuery(String configName, String queryName) throws AppCrash {

        this(queryName);
        ErrDetector.GetInstance().invariant(configName != null);
        _configName = configName;

        _dbConfigName = Config.GetInstance(_configName).getProperty("DirectQuery." + _queryName + ".DBcfg", "");
    }

    /**
     * Valorizza l'attributo _sql, contenente la query da eseguire rimpiazzando i tag #NOMEPARAM# nel testo della query
     * con i parametri passati
     *
     * @param parametri parametri hashtable per valorizzare la parte parametrica della query scritta nel file di
     *            configurazione
     *
     * @throws net.project.errors.ParamCrash
     *
     */
    public void setParam(Map parametri) throws AppCrash {

        // controllo formale del parametro in ingresso
        ErrDetector.GetInstance().param(parametri);

        _sql = getQuery();
        ErrDetector.GetInstance().param(_sql);

        TextTemplate tt = null;

        try {
            tt = new TextTemplate(_sql);
        } catch (ParamCrash pc) {
            pc.logContext("DirectQuery", toString() + "; parametri = " + parametri);
            throw pc;
        }

        if (!tt.replace(parametri)) {
            AppCrash ac = new AppCrash();
            ac.logContext("DirectQuery", toString() + "; parametri = " + parametri);
            throw ac;
        }

        _sql = tt.getText();
    }

    /**
     * Questo metodo esegue la query di insert/update; deve essere stato precedentemente chiamato il metodo setParam()in
     * modo che il testo della query da eseguire sia stato preparato.
     * 
     * @throws AppCrash
     */
    public void execute() throws AppCrash {

        // la variabile _sql deve essere stata valorizzata, ovvero deve essere
        // gia' stato chiamato il metodo setParam()
        ErrDetector.GetInstance().param(_sql);

        try {
            createStatement();
            Logger.GetInstance().log3("sto per eseguire la seguente insert query: " + _sql);
            _statement.executeUpdate(_sql);
        } catch (AppCrash ac) {
            ac.logContext("DirectQuery", toString());
            close();
            throw ac;
        } catch (SQLException sqle) {
            DBCrash dbc = new DBCrash(sqle);
            dbc.logContext("DirectQuery", toString());
            close();
            throw dbc;
        } finally {
            close();
        }
    }

    /**
     * Libera le risorse del database e si assicura che tutto sia correttamente chiuso
     *
     * @exception net.project.AppCrash.errors lanciata dal metodo shutdown di ConnectionPool_itf oppure (come DBCrash)
     *                in caso di SQLException
     *
     */
    private void close() throws AppCrash {

        try {
            if (_statement != null) {
                _statement.close();
                _statement = null;
            }
        } catch (SQLException sqle) {
            DBCrash dbc = new DBCrash(sqle);
            dbc.logContext("DirectQuery", toString());
            throw dbc;
        } finally {
            // Mi assicuro in ogni caso di rilasciare la connessione al DB
            if (_dbConnection != null) {
                try {
                    _dbConnection.free();
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                _dbConnection = null;
            }
        }
    }

    /**
     * Questo metodo serve per recuperare la stringa che rappresenta la query prima del replace dei parametri. Di
     * default legge la proprieta "DirectQuery." + _queryName + ".Query" dal file di configurazione Puo' essere
     * ridefinito dalle sottoclassi per modificarne il comportamento
     *
     * @return una stringa che rapresenta la query da eseguire
     */
    protected String getQuery() {

        return Config.GetInstance(_configName).getProperty("DirectQuery." + _queryName + ".Query");
    }

    /*
     * Crea lo statement da usare per eseguire la query.
     */
    private void createStatement() throws AppCrash {

        if (_transaction == null) {
            // Se non ho un oggetto DBTransaction recupero una connessione al DB e creo lo statement
            _dbConnection = ConnectionPool.GetInstance(_dbConfigName).getAConnection();
            _statement = _dbConnection.createStatement();
        } else {
            _statement = _transaction.createStatement();
        }

    }
}
