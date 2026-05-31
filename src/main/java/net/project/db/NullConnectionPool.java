/*

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore: 

  Note:

 */

package net.project.db;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.misc.Config;

/**
 * Questa classe rappresenta un finto ConnectionPool: quando viene richiesta una connessione questa viene creata al
 * momento. Quando la connessione viene rilasciata essa deve chiudersi; per tale motivo con questa connectionPool si
 * devono utilizzare connessioni di tipo PopupDBConnection o derivanti da essa.
 */
public class NullConnectionPool extends ConnectionPool_base implements ConnectionPool_itf {

    private final String _JDBCDriver;
    private final String _connectionURL;
    private final String _dbUser;
    private final String _dbPassword;
    private final String _configName;
    private final String _statsdName;

    /**
     * Costruttore. Crea un finto pool di connessioni recuperando i parametri necessari dal file di configurazione
     * utilizzando il nome del pool passato per costruire i nomi delle proprieta' da leggere:
     * 
     * <P>
     * DB.$nomeConnectionURL
     * </p>
     * 
     * <P>
     * </p>
     * 
     * <P>
     * DB.$nomeConnectionUser
     * </p>
     * 
     * <P>
     * </p>
     * 
     * <P>
     * DB.$nomeConnectionPassword
     * </p>
     * 
     * <P>
     * </p>
     * 
     * <P>
     * DB.$nomeJDBCDriver
     * </p>
     * 
     * <P>
     * Ad ogni chiamata a getAConnection viene stabilita una nuova connessione al DB; ad ogni free sulla connessione la
     * connessione deve chiudersi. Questa classe deve quindi essere utilizzata congiuntamente alle connesioni tipo
     * PopUpDBConnection
     * </p>
     * 
     * @param name java.lang.String nome del file di configurazione da utilizzare
     * @throws AppCrash DOCUMENT ME!
     */
    public NullConnectionPool(String name) throws AppCrash {

        try {
            _configName = name;
            if (name == null || name.length() == 0) {
                _statsdName = "LIB.DB.default.get";
            } else {
                _statsdName = "LIB.DB." + name + ".get";
            }
            // Ricavo dal file di configurazione i parametri di connessione
            _connectionURL = Config.GetInstance(name).getProperty("DB.ConnectionURL");
            _dbUser = Config.GetInstance(name).getProperty("DB.ConnectionUser", "nouser");
            _dbPassword = Config.GetInstance(name).getProperty("DB.ConnectionPassword");
            ErrDetector.GetInstance().param(_connectionURL);

            if (_connectionURL.startsWith("jdbc/") || _connectionURL.startsWith("java:")) {
                _JDBCDriver = null;
                return;
            }

            _JDBCDriver = Config.GetInstance(name).getProperty("DB.JDBCDriver");

            ErrDetector.GetInstance().invariant((_dbUser != null) && (_connectionURL != null) && (_JDBCDriver != null));

            // Carica il driver JDBC
            Class.forName(_JDBCDriver);
        } catch (AppCrash err) {
            err.logContext("NullConnectionPool", this.toString());
            throw err;
        } catch (Exception e) {

            AppCrash err = new AppCrash(e);
            err.logContext("NullConnectionPool", this.toString());
            throw err;
        }
    }

    /**
     * Ritorna una connessione a un database
     * 
     * @return DOCUMENT ME!
     * @throws AppCrash DOCUMENT ME!
     */
    public DBConnection_itf getAConnection() throws AppCrash {

        try {

            long startTime = System.currentTimeMillis();

            DBConnection conn = DBFactory.makeDBConnection(_configName, _connectionURL, _dbUser, _dbPassword);

            long endTime = System.currentTimeMillis();
            int execTime = (int) (endTime - startTime);

            // Log delle operazioni "lente"
            if (execTime > 300) {
                Logger.GetInstance().log0("TIMEDowork;" + execTime + ";Pool;" + _connectionURL);
            }
            //Logger.GetInstance().recordExecutionTime(_statsdName, execTime, StatsDClient_itf.STATSD_LIB_DB);
            return conn;
        } catch (AppCrash err) {
            err.logContext("NullConnectionPool", "Errore recuperando connessione da: " + _connectionURL);
            throw err;
        }
    }

    /**
     * Chiude tutte le connessione del pool.
     * 
     * @throws AppCrash DOCUMENT ME!
     */
    public void shutdown() throws AppCrash {

        // Nulla da fare dato che non vi e' un vero pool di connessioni
    }

    /**
     * Ritorna informazioni sulla classe.
     * 
     * @return java.lang.String.
     */
    public String toString() {

        StringBuffer temp = new StringBuffer();
        temp.append("URL: ").append(_connectionURL);
        temp.append(" User: ").append(_dbUser);
        temp.append(" Password: ").append(_dbPassword);
        temp.append(" Driver: ").append(_JDBCDriver);

        return temp.toString();
    }

}