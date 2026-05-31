/*

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore:

  Note:

*/

package net.projectsrl.db;

import java.util.ArrayList;
import java.util.HashSet;

import net.project.db.ConnectionPool_base;
import net.project.db.ConnectionPool_itf;
import net.project.db.DBConnection;
import net.project.db.DBConnection_itf;
import net.project.db.DBFactory;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.misc.Config;

/**
 * La classe TimedConnectionPool rappresenta un pool di connessioni al DB temporizzate: se le connessioni rimangono
 * inattive per un determintato intervallo di tempo vengono chiuse
 *
 */
public class PjTimedConnectionPool extends ConnectionPool_base implements ConnectionPool_itf, Runnable {

    private ArrayList<ConnectionContainer> _available;
    private HashSet<DBConnection>          _busy = new HashSet<DBConnection>();
    private String                         _JDBCDriver;
    private String                         _connectionURL;
    private String                         _dbUser;
    private String                         _dbPassword;
    private String                         _configName;
    private int                            _connectionNum;
    private int                            _connectionMax;
    private int                            _connectionTimeout;
    private Thread                         _timer;

    /**
     * Costruttore. Crea un pool di connessioni recuperando i parametri necessari dalla configurazione passata i nomi
     * delle proprieta' da leggere sono:
     *
     * <P>
     * DB.ConnectionURL
     * </p>
     *
     * <P>
     * </p>
     *
     * <P>
     * DB.ConnectionUser
     * </p>
     *
     * <P>
     * </p>
     *
     * <P>
     * DB.ConnectionPassword
     * </p>
     *
     * <P>
     * </p>
     *
     * <P>
     * DB.JDBCDriver
     * </p>
     *
     * <P>
     * </p>
     *
     * <P>
     * DB.ConnectionNum
     * </p>
     *
     * <P>
     * </p>
     *
     * <P>
     * DB.ConnectionTimeout
     * </p>
     *
     * <P>
     * </p>
     *
     * @param name java.lang.String nome del file di configurazione da utilizzare
     * @throws AppCrash in caso di eccezione
     */
    public PjTimedConnectionPool(String name) throws AppCrash {

        try {
            _configName = name;

            // Ricavo dal file di configurazione i parametri di connessione
            _connectionURL = Config.GetInstance(name).getProperty("DB.ConnectionURL");
            _dbUser = Config.GetInstance(name).getProperty("DB.ConnectionUser");
            _dbPassword = Config.GetInstance(name).getProperty("DB.ConnectionPassword");
            _JDBCDriver = Config.GetInstance(name).getProperty("DB.JDBCDriver");

            // Ricavo il numero di connessioni massimo da poter creare nell pool
            _connectionNum = (new Integer(Config.GetInstance(name).getProperty("DB.ConnectionNum"))).intValue();
            _connectionTimeout = (new Integer(Config.GetInstance(name).getProperty("DB.ConnectionTimeout"))).intValue();

            _connectionMax = _connectionNum + 5;

            ErrDetector.GetInstance().invariant((_connectionNum != 0) && (_dbPassword != null) && (_dbUser != null)
                    && (_connectionURL != null) && (_connectionTimeout != 0) && (_JDBCDriver != null));

            _available = new ArrayList<ConnectionContainer>(_connectionNum);

            // Carica il driver JDBC
            Class.forName(_JDBCDriver);

            // Istanzia il timer
            _timer = new Thread(this);
            // Imposta il timer come deamon in modo che non impedisca alla JVM di terminare nel caso
            // in cui non vi siano user Thread attivi.
            _timer.setDaemon(true);
            _timer.start();
        } catch (AppCrash err) {
            err.logContext("TimedConnectionPool", this.toString());
            throw err;
        } catch (Exception e) {

            AppCrash err = new AppCrash(e);
            err.logContext("TimedConnectionPool", this.toString());
            throw err;
        }
    }

    /**
     * Ciclo per la verifica dei tempi di inattivita' delle connessioni e per la loro chiusura
     */
    public void run() {

        while (true) {

            try {
                Thread.sleep(_connectionTimeout / 5);
                synchronized (this) {

                    ArrayList<ConnectionContainer> newAvailable = new ArrayList<ConnectionContainer>(_connectionNum);

                    while (_available.size() > 0) {

                        ConnectionContainer container = (ConnectionContainer) _available.remove(0);
                        container.touch();

                        if (container.getState() >= 5) {

                            try {
                                container.getConnection().close();
                            } catch (AppCrash err) {
                                // L'eccezione viene ignorata
                            }
                        } else {
                            newAvailable.add(container);
                        }
                    }

                    _available = newAvailable;

                    Logger.GetInstance()
                            .log0("TimedConnectionPool available " + _available.size() + " busy " + _busy.size());
                }
            } catch (Throwable err) {
                // Le eventuali eccezioni vengono ignorate
            } finally {
                Logger.GetInstance().flush();
            }
        }
    }

    /**
     * Ritorna una connessione a un database
     * 
     * @return la connessione
     * @throws AppCrash in caso di eccezione
     */
    public DBConnection_itf getAConnection() throws AppCrash {

        try {

            synchronized (this) {

                _connectionURL = Config.GetInstance().getProperty("DB.ConnectionURL");

                ErrDetector.GetInstance().invariant(_busy.size() < _connectionMax,
                        "Raggiunto limite connessione DB in uso");

                DBConnection conn = DBFactory.makeTimedDBConnection(_configName, _connectionURL, _dbUser, _dbPassword);
                _busy.add(conn);

                return conn;
            }

        } catch (AppCrash err) {
            err.logContext("TimedConnectionPool", "Errore recuperando connessione" + toString());
            throw err;
        } catch (Throwable ex) {

            AppCrash err = new AppCrash(ex);
            err.logContext("TimedConnectionPool", "Errore recuperando connessione" + toString());
            throw err;
        }
    }

    /**
     * Chiude tutte le connessione del pool.
     * 
     * @throws AppCrash DOCUMENT ME!
     */
    public void shutdown() throws AppCrash {

        synchronized (this) {

            while (_available.size() > 0) {

                ConnectionContainer container = (ConnectionContainer) _available.remove(0);

                try {
                    container.getConnection().close();
                } catch (AppCrash err) {
                    // Le eccezioni sono ignorate
                }
            }
        }
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
        temp.append(" Num: ").append(_connectionNum);
        temp.append(" Timeout: ").append(_connectionTimeout);
        temp.append(" Driver: ").append(_JDBCDriver);

        return temp.toString();
    }

    /**
     * Questa classe serve come contenitore della connessione che viene inserita nel pool delle connessioni libere.
     * Tiene traccia del tempo trascorso come inutilizzata
     */
    private static class ConnectionContainer {

        private int          _state;
        private DBConnection _value;

        public ConnectionContainer(DBConnection value) {
            _state = 0;
            _value = value;
        }

        public DBConnection getConnection() {

            _state = 0;

            return _value;
        }

        public void touch() {

            _state = _state + 1;
        }

        public int getState() {

            return _state;
        }
    }

    /**
     * Override del metodo base chiamato dalla free di una connection. Rimette la connection nel pool di quelle
     * disponibili.
     *
     * @param dbConn la connessione liberata
     *
     * @see net.projectsrl.db.ConnectionPool_base#freeNotify(net.projectsrl.db.DBConnection_itf)
     */
    public void freeNotify(DBConnection_itf dbConn) {

        DBConnection conn = (DBConnection) dbConn;
        try {

            synchronized (this) {
                _busy.remove(conn);
                int dimensioni = _available.size() + _busy.size();

                // Se vi sono gia' abbastanza connessioni attive chiudo la connessione e basta
                if (dimensioni >= _connectionNum) {
                    conn.close();
                    Logger.GetInstance().log0(
                            "TimedConnectionPoolFN available " + _available.size() + " busy " + (_busy.size() + 1));
                } else {

                    ConnectionContainer container = new ConnectionContainer(conn);
                    // inserisco in testa alla lista
                    _available.add(0, container);
                }
            }
        } catch (AppCrash err) {
            err.logContext("TimedConnectionPool", "Errore liberando connessione" + toString());
        } catch (Throwable ex) {

            AppCrash err = new AppCrash(ex);
            err.logContext("TimedConnectionPool", "Errore liberando connessione" + toString());
        }

    }

}