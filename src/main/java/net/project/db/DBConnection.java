
package net.project.db;

/*
 DBConnection.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 27/05/1999

 Autore: Rosella V.

 Note:

 Modifiche:

 */

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import net.project.errors.AppCrash;
import net.project.errors.DBCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;

/**
 * Questa classe e' rappresenta una connessione concreta alla base dati
 */
public class DBConnection implements DBConnection_itf {

    static private Map _JDBC2DataSourceMap = new HashMap();

    private boolean    _isBusy             = false;
    private Connection _conn;
    private String     _configName;

    /**
     * Costruttore.
     * 
     * @param URL java.lang.String database
     * @param user java.lang.String nome utente
     * @param password java.lang.String password
     * @exception AppCrash
     */
    public DBConnection(String URL, String user, String password) throws AppCrash {

        try {

            if (URL.startsWith("jdbc/") || URL.startsWith("java:")) {
                // Istanziazione JDBC 2.x

                if (_JDBC2DataSourceMap.get(URL) == null) {

                    Hashtable env = new Hashtable();
                    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.ibm.ejs.ns.jndi.CNInitialContextFactory");

                    Context ctx = null;
                    try {
                        ctx = new InitialContext(env);
                    } catch (Throwable t) {
                        ctx = new InitialContext();
                    }

                    _JDBC2DataSourceMap.put(URL, ctx.lookup(URL));
                }

                DataSource JDBC2DataSource = (DataSource) _JDBC2DataSourceMap.get(URL);

                if (user.toLowerCase().equals("nouser")) {
                    _conn = JDBC2DataSource.getConnection();
                } else {
                    _conn = JDBC2DataSource.getConnection(user, password);
                }

            } else {
                // Istanziazione JDBC 1.x
                if (user.toLowerCase().equals("nouser")) {
                    _conn = DriverManager.getConnection(URL);
                } else {
                    _conn = DriverManager.getConnection(URL, user, password);
                }
            }

            setAutoCommit(true);
        } catch (Throwable e) {
            AppCrash err = new AppCrash(e);
            throw err;
        }
    }

    /**
     * Ritorna null fino a che un nuovo warning viene riportato per questa connessione.
     * 
     * @exception AppCrash.
     */
    @Override
    public void clearWarnings() throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_conn != null);
            _conn.clearWarnings();
        } catch (SQLException e) {
            DBCrash appCrash = new DBCrash(e);
            throw appCrash;
        } catch (AppCrash e) {
            e.logContext("DBConnection", "clearWarnings");
            throw e;
        }
    }

    /**
     * Chiude una connessione al database.
     */
    public void close() throws AppCrash {

        try {
            _conn.close();
        } catch (SQLException e) {
            Logger.GetInstance().log3("eccezione in la close");
            DBCrash ex = new DBCrash(e);
            throw ex;
        }
    }

    /**
     * Rende effettive le esecuzioni degli statement prima della sua chiamata.
     * 
     * @exception AppCrash.
     */
    @Override
    public void commit() throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_conn != null);
            _conn.commit();
        } catch (SQLException e) {
            DBCrash appCrash = new DBCrash(e);
            throw appCrash;
        } catch (AppCrash e) {
            e.logContext("DBConnection", "_conn e' null");
            throw e;
        }
    }

    /**
     * Rende effettive le esecuzioni degli statement prima della sua chiamata.
     * 
     * @return java.sql.Statement.
     * @exception AppCrash.
     */
    @Override
    public Statement createStatement() throws AppCrash {

        Statement s = null;
        try {
            s = _conn.createStatement();
            ErrDetector.GetInstance().postCond(s != null);
        } catch (SQLException e) {
            DBCrash appCrash = new DBCrash(e);
            throw appCrash;
        } catch (AppCrash ex) {
            Logger.GetInstance().logError("Errore di creazione statement");
            throw ex;
        }
        return s;
    }

    /**
     * Imposta il flag _isBusy a false. La connessione viene resa diponibile per altre operazioni. Viene notificato il
     * connection pool di appartenenza che la connessione e' stata liberata
     */
    @Override
    public void free() {

        try {
            ConnectionPool_itf pool = ConnectionPool.GetInstance(getConfigName());
            pool.freeNotify(this);
            _isBusy = false;
        } catch (AppCrash ac) {
            // Eccezioni ingorate
        }
    }

    /**
     * Ritorna lo stato corrente del fag auto-commit.
     * 
     * @return boolean.
     * @exception AppCrash.
     */
    @Override
    public boolean getAutoCommit() throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_conn != null);
            return _conn.getAutoCommit();
        } catch (SQLException e) {
            DBCrash appCrash = new DBCrash(e);
            throw appCrash;
        } catch (AppCrash e) {
            e.logContext("DBConnection", "getAutoCommit");
            throw e;
        }

    }

    /**
     * Ritorna il catalog name corrente delle connessioni.
     * 
     * @return java.lang.String
     * @exception AppCrash
     */
    @Override
    public String getCatalog() throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_conn != null);
            return _conn.getCatalog();
        } catch (SQLException e) {
            DBCrash appCrash = new DBCrash(e);
            throw appCrash;
        } catch (AppCrash e) {
            e.logContext("DBConnection", "getCatalog");
            throw e;
        }
    }

    /**
     * Ritorna informazioni relative alla tabelle del database, alla grammatica SQL supportata, ecc.
     * 
     * @return java.sql.DatabaseMetaData.
     * @exception AppCrash.
     */
    @Override
    public DatabaseMetaData getMetaData() throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_conn != null);
            return _conn.getMetaData();
        } catch (SQLException e) {
            DBCrash appCrash = new DBCrash(e);
            throw appCrash;
        } catch (AppCrash e) {
            e.logContext("DBConnection", "getMetaData");
            throw e;
        }
    }

    /**
     * Ritorna lo stato attuale del transaction isolation mode.
     * 
     * @return int
     * @exception AppCrash
     */
    @Override
    public int getTransactionIsolation() throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_conn != null);
            return _conn.getTransactionIsolation();
        } catch (SQLException e) {
            DBCrash appCrash = new DBCrash(e);
            throw appCrash;
        } catch (AppCrash e) {
            e.logContext("DBConnection", "getTransactionIsolation");
            throw e;
        }
    }

    /**
     * Ritorna il primo warning generato dalla connessione.
     * 
     * @return SQLWarning.
     * @exception AppCrash.
     */
    @Override
    public SQLWarning getWarnings() throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_conn != null);
            return _conn.getWarnings();
        } catch (SQLException e) {
            DBCrash appCrash = new DBCrash(e);
            throw appCrash;
        } catch (AppCrash e) {
            e.logContext("DBConnection", "getWarnings");
            throw e;
        }
    }

    /**
     * Viene testata la chiusura della connesione.
     * 
     * @return boolean.
     * @exception AppCrash.
     */
    @Override
    public boolean isClosed() throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_conn != null);
            return _conn.isClosed();
        } catch (SQLException e) {
            DBCrash appCrash = new DBCrash(e);
            throw appCrash;
        } catch (AppCrash e) {
            e.logContext("DBConnection", "isClosed");
            throw e;
        }
    }

    /**
     * Viene testato se la connessione è in modalità di sola lettura.
     * 
     * @return boolean.
     * @exception AppCrash.
     */
    @Override
    public boolean isReadOnly() throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_conn != null);
            return _conn.isReadOnly();
        } catch (SQLException e) {
            DBCrash appCrash = new DBCrash(e);
            throw appCrash;
        } catch (AppCrash e) {
            e.logContext("DBConnection", "isReadOnly");
            throw e;
        }
    }

    /**
     * Ritorna la forma 'native' dello statement che il driver potrebbe aver inviato.
     * 
     * @param java.lang.String stringa che definisce la query.
     * @return java.lang.String.
     * @exception AppCrash.
     */
    @Override
    public String nativeSQL(String query) throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_conn != null);
            return _conn.nativeSQL(query);
        } catch (SQLException e) {
            DBCrash appCrash = new DBCrash(e);
            throw appCrash;
        } catch (AppCrash e) {
            e.logContext("DBConnection", "nativeSQL");
            throw e;
        }
    }

    /**
     * Consente l'impostazione dei parametri di input/output e la gestione dei metodi per eseguire una chiamata di
     * procedura SQL.
     * 
     * @param java.lang.String uno statement SQL.
     * @return CallableStatement.
     * @exception AppCrash.
     */
    @Override
    public CallableStatement prepareCall(String sql) throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_conn != null);
            return _conn.prepareCall(sql);
        } catch (SQLException e) {
            DBCrash appCrash = new DBCrash(e);
            throw appCrash;
        } catch (AppCrash e) {
            e.logContext("DBConnection", "prepareCall");
            throw e;
        }
    }

    /**
     * Uno statement SQL, con o senza parametri di input può essere precompilato e salvato in un oggetto
     * PreparatedStatement. Questo oggetto può quindi essere usato per eseguire in modo più efficiente tale statement
     * più volte.
     * 
     * @param java.lang.String uno statement SQL.
     * @return PreparatedStatement.
     * @exception AppCrash.
     */
    @Override
    public PreparedStatement prepareStatement(String sql) throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_conn != null);
            return _conn.prepareStatement(sql);
        } catch (SQLException e) {
            DBCrash appCrash = new DBCrash(e);
            throw appCrash;
        } catch (AppCrash e) {
            e.logContext("DBConnection", "prepareStatement");
            throw e;
        }
    }

    /**
     * Consente il ritorno allo stato del database dopo l'ultima commit.
     * 
     * @exception AppCrash.
     */
    @Override
    public void rollback() throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_conn != null);
            _conn.rollback();
        } catch (SQLException e) {
            DBCrash er = new DBCrash(e);
            throw er;
        } catch (AppCrash e) {
            e.logContext("DBConnection", "_conn e' null");
            throw e;
        }
    }

    /**
     * Se il flag di autoCommit è impostato a true ogni statement viene eseguito e considerato come una ttransazione.
     * 
     * @param boolean valore di autoCommit.
     * @exception AppCrash.
     */
    @Override
    public void setAutoCommit(boolean enableAutoCommit) throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_conn != null);
            _conn.setAutoCommit(enableAutoCommit);
        } catch (SQLException e) {
            DBCrash appCrash = new DBCrash(e);
            throw appCrash;
        } catch (AppCrash e) {
            e.logContext("DBConnection", "setAutoCommit");
            throw e;
        }
    }

    /**
     * Se la risorsa è disponibile viene impostato il flag _isBusy a true.
     * 
     * @return boolean
     */
    public synchronized boolean setBusy() {

        if (_isBusy == false) {
            _isBusy = true;
            return _isBusy;
        }
        return false;

    }

    /**
     * Imposta un nome di catalogo.
     * 
     * @param java.lang.String nome del catalogo.
     * @exception AppCrash.
     */
    @Override
    public void setCatalog(String catalog) throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_conn != null);
            _conn.setCatalog(catalog);
        } catch (SQLException e) {
            DBCrash appCrash = new DBCrash(e);
            throw appCrash;
        } catch (AppCrash e) {
            e.logContext("DBConnection", "setCatalog");
            throw e;
        }
    }

    /**
     * Una connessione può essere impostata per sola lettura per ottimizzare la gestione del database.
     * 
     * @param boolean valore del flag per sola lettura.
     * @exception AppCrash.
     */
    @Override
    public void setReadOnly(boolean readOnly) throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_conn != null);
            _conn.setReadOnly(readOnly);
        } catch (SQLException e) {
            DBCrash appCrash = new DBCrash(e);
            throw appCrash;
        } catch (AppCrash e) {
            e.logContext("DBConnection", "setReadOnly");
            throw e;
        }
    }

    /**
     * Cambia il livello di isolamento di una transazione.
     * 
     * @param int livello di isolamento.
     * @exception AppCrash.
     */
    @Override
    public void setTransactionIsolation(int level) throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_conn != null);
            _conn.setTransactionIsolation(level);
        } catch (SQLException e) {
            DBCrash appCrash = new DBCrash(e);
            throw appCrash;
        } catch (AppCrash e) {
            e.logContext("DBConnection", "setTransactionIsolation");
            throw e;
        }
    }

    void setConfigName(String config) {

        _configName = config;
    }

    String getConfigName() {

        return _configName;
    }

}
