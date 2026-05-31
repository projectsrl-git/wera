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
 * La classe $objectType$ DOCUMENT ME!
 *
 * @author $author$
 */
public class FixSizeConnectionPool extends ConnectionPool_base implements ConnectionPool_itf {

    private DBConnection[] _pool;
    private String         _JDBCDriver;
    private String         _connectionURL;
    private String         _dbUser;
    private String         _dbPassword;
    private int            _connectionNum;

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
     * @param name java.lang.String nome del file di configurazione da utilizzare
     * @throws AppCrash DOCUMENT ME!
     */
    public FixSizeConnectionPool(String name) throws AppCrash {

        try {

            // Ricavo dal file di configurazione i parametri di connessione
            _connectionURL = Config.GetInstance(name).getProperty("DB.ConnectionURL");
            _dbUser = Config.GetInstance(name).getProperty("DB.ConnectionUser");
            _dbPassword = Config.GetInstance(name).getProperty("DB.ConnectionPassword");
            _JDBCDriver = Config.GetInstance(name).getProperty("DB.JDBCDriver");

            // Ricavo il numero di connessioni da creare nell pool
            _connectionNum = (new Integer(Config.GetInstance(name).getProperty("DB.ConnectionNum"))).intValue();

            ErrDetector.GetInstance().invariant(
                    (_connectionNum != 0) && (_dbPassword != null) && (_dbUser != null) && (_connectionURL != null)
                            && (_JDBCDriver != null));

            // Carica il driver JDBC
            Class.forName(_JDBCDriver);

            // Istanzia il pool ed inizializza le connessioni
            _pool = new DBConnection[_connectionNum];

            for (int j = 0; j < _connectionNum; j++) {
                _pool[j] = DBFactory.makeDBConnection(name, _connectionURL, _dbUser, _dbPassword);
            }
        } catch (AppCrash err) {
            err.logContext("FixSizeConnectionPool", this.toString());
            throw err;
        } catch (Exception e) {

            AppCrash err = new AppCrash(e);
            err.logContext("FixSizeConnectionPool", this.toString());
            throw err;
        }
    }

    /**
     * Ritorna una connessione a un database
     * 
     * @return DOCUMENT ME!
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    public synchronized DBConnection_itf getAConnection() throws AppCrash {

        int cont = 0;

        try {

            while (true) {

                for (int j = 0; j < _connectionNum; j++) {

                    if (_pool[j].setBusy() == true) {

                        // Se la connessione e' disgraziatamente chiusa la riapro
                        if (_pool[j].isClosed() == true) {
                            _pool[j] = new DBConnection(_connectionURL, _dbUser, _dbPassword);
                            _pool[j].setBusy();
                        }

                        return _pool[j];
                    }
                }

                if (cont < 10) {
                    Thread.sleep((long) ((Math.random() + 1) * 1000));
                    cont++;
                } else {

                    AppCrash dl = new AppCrash(
                            "Attenzione! Il sistema è sovraccarico oppure si è verificato un deadlock");
                    dl.logContext("FixSizeConnectionPool", toString());
                    throw dl;
                }
            }
        } catch (AppCrash err) {
            err.logContext("FixSizeConnectionPool", "Errore riaprendo connessione chiusa");
            throw err;
        } catch (InterruptedException ex) {

            AppCrash err = new AppCrash(ex);
            err.logContext("FixSizeConnectionPool", "Errore riaprendo connessione chiusa");
            throw err;
        }
    }

    /**
     * Chiude tutte le connessione del pool.
     * 
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    public void shutdown() throws AppCrash {

        for (int j = 0; j < _connectionNum; j++) {

            try {
                ErrDetector.GetInstance().invariant(!_pool[j].isClosed());
                _pool[j].close();
            } catch (AppCrash e) {
                Logger.GetInstance().logError(toString());
                throw e;
            }
        }
    }

    /**
     * Ritorna informazioni sulla classe.
     * 
     * @return java.lang.String.
     */
    @Override
    public String toString() {

        StringBuffer temp = new StringBuffer();
        temp.append("URL: ").append(_connectionURL);
        temp.append(" User: ").append(_dbUser);
        temp.append(" Password: ").append(_dbPassword);
        temp.append(" Num: ").append(_connectionNum);
        temp.append(" Driver: ").append(_JDBCDriver);

        return temp.toString();
    }

}