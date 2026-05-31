/*
 Copyright (c) by SSB Spa Societa' per i Servizi Bancari

 Note:

 */

package net.project.dataset;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.NoSuchElementException;

import net.project.db.ConnectionPool;
import net.project.db.ConnectionPool_itf;
import net.project.db.DBConnection_itf;
import net.project.errors.AppCrash;
import net.project.errors.DBCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.errors.ParamCrash;
import net.project.misc.Config;
import net.project.misc.TextTemplate;

/**
 * Rappresenta un DataSet ottenuto come risultato di una query fatta sul database.
 * 
 * <P>
 * Proprietà lette dal file di configurazione: DS.dsName.Query = query che genera il DBDataSet
 * </p>
 * 
 * <P>
 * DS.dsName.Row = classe implementante Row_itf che dev'essere istanziata per rappresentare le righe del DBDataSet
 * corrente
 * </p>
 * 
 * <p>
 * DS.dsName.DBcfg = proprieta' facoltativa. Se presente indica il nome della config da usare per recuperare il
 * ConnectionPool per eseguire la query. Permette di utilizzare diversi DB
 * </p>
 * 
 * <p>
 * DS.WITHUR = proprietà facoltativa. Se presente e valorizzata a true (TRUE) attiva l'append di WITH UR alla fine della
 * query letta dal config file
 * </p>
 */
public class DBDataSet implements DataSet_itf {

    private String             _sql             = null;
    private String             _configName      = null;
    private String             _dsName          = null;
    private ConnectionPool_itf _connectionPool  = null;
    private DBConnection_itf   _dbConnection    = null;
    private Statement          _statement       = null;
    private ResultSet          _resultSet       = null;
    private Row_itf            _row             = null;
    private boolean            _opened          = false;
    private boolean            _nextEseguita    = false;
    private boolean            _hasMoreElements = false;
    private String             _dbConfigName    = null;
    private Map                _parametri       = null;

    /**
     * Costruttore.
     * 
     * @param configName DOCUMENT ME!
     * @param dsName DOCUMENT ME!
     * 
     * @roseuid 3A5B327E0326
     */
    public DBDataSet(String configName, String dsName) throws AppCrash {

        // controllo formale dei parametri in ingresso
        ErrDetector.GetInstance().invariant(configName != null);
        ErrDetector.GetInstance().param(dsName);

        _configName = configName;
        _dsName = dsName;

        _dbConfigName = Config.GetInstance(_configName).getProperty("DS." + _dsName + ".DBcfg", "");
    }

    /**
     * Apre una connessione al database e recupera il ResultSet.
     * 
     * @exception net.project.errors.AppCrash in caso di errore nella connessione al database o nella parametrizzazione
     *                con TextTemplate
     * 
     * @roseuid 3A5D7D5200AC
     */
    @Override
    public void open() throws AppCrash {

        if (_opened) {
            return;
        }

        if (_sql == null) {
            _sql = getQuery();
            ErrDetector.GetInstance().param(_sql);

            setWithUr();
        }

        getDbConnection();

        long start = System.currentTimeMillis();
        try {
            _statement = _dbConnection.createStatement();
            if (Logger.GetInstance().getLogLevel() >= 3) {
                Logger.GetInstance().log3("sto per eseguire la seguente query: " + _sql);
            }
            _resultSet = _statement.executeQuery(_sql);

            long end = System.currentTimeMillis();
            if (end - start > 25000) {
                String query;
                if (end - start > 300000) {
                    query = _sql;
                } else {
                    query = _sql.substring(Math.max(0, _sql.length() - 150));
                }

                Logger.GetInstance().log0("TIMEquery;" + (end - start) + ";" + getDsName() + ";" + query);
            }

            _row = createRow(_resultSet);
            _opened = true;
            _nextEseguita = false;
        } catch (AppCrash ac) {
            ac.logContext("DBDataSet", toString());
            close();
            throw ac;
        } catch (SQLException sqle) {
            DBCrash dbc = new DBCrash(sqle);
            dbc.logContext("DBDataSet", toString());
            close();
            throw dbc;
        }
    }

    /**
     * Recupera la connessione dal database
     * 
     * @throws AppCrash
     */
    protected void getDbConnection() throws AppCrash {

        _connectionPool = ConnectionPool.GetInstance(getDBConfigName());
        _dbConnection = _connectionPool.getAConnection();
        _dbConnection.setAutoCommit(false);
    }

    // Metodo protected che crea un oggetto di tipo Row_itf leggendo dal file di configurazione
    // la specifica classe da istanziare
    // @param set java.sql.ResultSet il ResultSet contente i dati per costruire la riga
    // @return Row_itf l'oggetto costruito
    // @exception net.project.errors.AppCrash in caso di errori nel reperimento dei dati
    // o nella costruzione dell'oggetto Row_itf
    protected Row_itf createRow(ResultSet set) throws AppCrash {

        try {
            String className = Config.GetInstance(_configName).getProperty("DS." + _dsName + ".Row");
            ErrDetector.GetInstance().param(className);

            Class rowClass = Class.forName(className);
            Constructor constructor = rowClass.getConstructor(new Class[] { ResultSet.class });
            Row_itf row = (Row_itf) constructor.newInstance(new Object[] { set });

            return row;
        } catch (ClassNotFoundException cnfe) {
            AppCrash ac = new AppCrash(cnfe);
            ac.logContext("DBDataSet", toString());
            throw ac;
        } catch (NoSuchMethodException nsme) {
            AppCrash ac = new AppCrash(nsme);
            ac.logContext("DBDataSet", toString());
            throw ac;
        } catch (InstantiationException ie) {
            AppCrash ac = new AppCrash(ie);
            ac.logContext("DBDataSet", toString());
            throw ac;
        } catch (InvocationTargetException ite) {
            AppCrash ac = new AppCrash(ite);
            ac.logContext("DBDataSet", toString());
            throw ac;
        } catch (IllegalAccessException iae) {
            AppCrash ac = new AppCrash(iae);
            ac.logContext("DBDataSet", toString());
            throw ac;
        }
    }

    /**
     * Posiziona il cursore del ResultSet davanti alla prima riga.
     * 
     * @exception net.project.errors.AppCrash in caso di SQLException o di Resultset = null
     * 
     * @roseuid 3A5D7D5200D4
     */
    @Override
    public void rewind() throws AppCrash {

        if (!_opened) {
            open();

            return;
        }

        try {
            if (_resultSet != null) {
                _resultSet.close();
            }

            _resultSet = _statement.executeQuery(_sql);
            _nextEseguita = false;
            _row = createRow(_resultSet);
        } catch (SQLException sqle) {
            DBCrash dbc = new DBCrash(sqle);
            dbc.logContext("DBDataSet.java", toString());
            close();
            throw dbc;
        }
    }

    /**
     * Libera le risorse del database.
     * 
     * @exception net.project.AppCrash.errors lanciata dal metodo shutdown di ConnectionPool_itf oppure (come DBCrash)
     *                in caso di SQLException
     * 
     * @roseuid 3A5D7D520107
     */
    @Override
    public void close() throws AppCrash {

        try {
            if (_resultSet != null) {
                _resultSet.close();
                _resultSet = null;
            }

            if (_statement != null) {
                _statement.close();
                _statement = null;
            }

        } catch (SQLException sqle) {
            DBCrash dbc = new DBCrash(sqle);
            dbc.logContext("DBDataSet", toString());
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

            _opened = false;
            _hasMoreElements = false;
            _nextEseguita = true;

        }
    }

    /**
     * Valorizza l'attributo _sql, contenente la query da eseguire.
     * 
     * @param parametri parametri Map per valorizzare la parte parametrica della query scritta nel file di
     *            configurazione
     * 
     * @exception net.project.errors.ParamCrash
     * 
     * @roseuid 3A5D7D52016B
     */
    @Override
    public void setParam(Map parametri) throws AppCrash {

        // controllo formale del parametro in ingresso
        ErrDetector.GetInstance().param(parametri);
        _parametri = parametri;

        _sql = getQuery();
        ErrDetector.GetInstance().param(_sql);

        setWithUr();

        TextTemplate tt = null;

        try {
            tt = new TextTemplate(_sql);
        } catch (ParamCrash pc) {
            pc.logContext("DBDataSet", toString() + "; parametri = " + parametri);
            throw pc;
        }

        if (!tt.replace(parametri)) {
            AppCrash ac = new AppCrash();
            ac.logContext("DBDataSet", toString() + "; parametri = " + parametri);
            throw ac;
        }

        _sql = tt.getText();
    }

    /**
     * Recupera i parametri impostati nel dataset
     * 
     * @return
     */
    protected Map getParam() {

        return _parametri;
    }

    /**
     * Controlla se il ResultSet contiene altri elementi.
     * 
     * @return boolean true se il ResultSet contiene altri elementi, false altrimenti
     * 
     * @exception RuntimeException : in caso di SQLException N.B. viene lanciata una RuntimeException, anziché
     *                un'eccezione di net.project.errors, in quanto il presente metodo deriva dall'interfaccia
     *                Enumeration (implementata indirettamente tramite DataSet_itf), e pertanto la sua signature (che
     *                non prevede eccezioni) non può essere cambiata.
     */
    @Override
    public boolean hasMoreElements() {

        if (_nextEseguita) {
            return _hasMoreElements;
        }

        try {
            _hasMoreElements = _resultSet.next();
            _nextEseguita = true;

            return _hasMoreElements;
        } catch (SQLException sqle) {
            DBCrash dbc = new DBCrash(sqle);
            dbc.logContext("DBDataSet", "hasMoreElements" + toString());

            try {
                close();
            } catch (Throwable t) {
            }

            throw new RuntimeException("SQLException - " + toString());
        }
    }

    /**
     * Restituisce l'elemento del ResultSet successivo a quello corrente.
     * 
     * @return java.lang.Object il successivo elemento del DBDataSet
     * 
     * @exception NoSuchElementException se non ci sono più elementi nel DBDataSet, o in caso di SQLException N.B. in
     *                caso di SQLException viene lanciata una NoSuchElementException, anziché eccezioni di
     *                net.project.errors, in quanto il presente metodo deriva dall'interfaccia Enumeration (implementata
     *                indirettamente tramite DataSet_itf), e pertanto la sua signature (che prevede il lancio della sola
     *                NoSuchElementException) non può essere cambiata.
     */
    @Override
    public Object nextElement() throws NoSuchElementException {

        try {
            if (!_nextEseguita) {
                _hasMoreElements = _resultSet.next();
            }

            _nextEseguita = false;

            return _row;
        } catch (NoSuchElementException nsee) {
            try {
                close();
            } catch (Throwable t) {
            }

            throw nsee;
        } catch (SQLException sqle) {
            DBCrash dbc = new DBCrash(sqle);
            dbc.logContext("DBDataSet", "nextElement " + toString());

            try {
                close();
            } catch (Throwable t) {
            }

            throw new NoSuchElementException("SQLException - " + toString());
        }
    }

    /**
     * Restituisce una stringa composta dai nomi di determinati attributi della classe, ognuno seguito dal proprio
     * valore corrente.
     * 
     * @return java.lang.String la stringa restituita
     */
    @Override
    public String toString() {

        StringBuffer temp = new StringBuffer();
        temp.append("_sql = ");
        temp.append(_sql);
        temp.append("; _configName = ");
        temp.append(_configName);
        temp.append("; _dsName = ");
        temp.append(_dsName);
        temp.append("; _opened = ");
        temp.append(_opened);
        temp.append("; _hasMoreElements = ");
        temp.append(_hasMoreElements);
        temp.append("; _nextEseguita = ");
        temp.append(_nextEseguita);

        return temp.toString();
    }

    /**
     * Conta il numero di colonne del DataSet
     * 
     * @return int numero di colonne del DataSet
     * 
     * @exception net.project.errors.AppCrash
     */
    @Override
    public int getColumnNo() throws AppCrash {

        // Controllo di effettuata open (e dunque di _resultSet diverso da null)
        ErrDetector.GetInstance().preCond(_opened);
        ErrDetector.GetInstance().param(_resultSet);

        try {

            int count = _resultSet.getMetaData().getColumnCount();

            return count;
        } catch (SQLException sqle) {
            DBCrash dbc = new DBCrash(sqle);
            dbc.logContext("DBDataSet", toString());
            close();
            throw dbc;
        }
    }

    /**
     * Ritorna i nomi delle colonne.
     * 
     * @return java.lang.String[] nomu delle colonne.
     * 
     * @exception net.project.errors.AppCrash
     */
    @Override
    public String[] getColumnNames() throws AppCrash {

        String[] columnNames = null;

        try {
            // recupero il numero di colonne
            int colNum = getColumnNo();
            columnNames = new String[colNum];

            ResultSetMetaData metaData = _resultSet.getMetaData();

            for (int i = 1; i <= colNum; i++) {
                columnNames[i - 1] = metaData.getColumnName(i);
            }
        } catch (SQLException sqle) {
            DBCrash dbc = new DBCrash(sqle);
            dbc.logContext("Class DBDataSet: metodo getColumnNames", toString());
            close();
            throw dbc;
        } catch (AppCrash app) {
            app.logContext("Class DBDataSet: metodo getColumnNames", toString());
            close();
            throw app;
        }

        return columnNames;
    }

    /**
     * Questo metodo
     * 
     * @return DOCUMENT ME!
     */
    protected String getConfigName() {

        return _configName;
    }

    /**
     * Questo metodo
     * 
     * @return DOCUMENT ME!
     */
    protected String getDsName() {

        return _dsName;
    }

    /**
     * Questo metodo
     * 
     * @return DOCUMENT ME!
     */
    protected String getDBConfigName() throws AppCrash {

        return _dbConfigName;
    }

    public void setDBConfigName(String dbConfigName) {

        _dbConfigName = dbConfigName;
    }

    /**
     * Questo metodo serve per recuperare la stringa che rappresenta la query prima del replace dei parametri. Di
     * default legge la proprieta "DS." + _dsName + ".Query" dal file di configurazione Puo' essere ridefinito dalle
     * sottoclassi per modificarne il comportamento
     * 
     * @return DOCUMENT ME!
     */
    protected String getQuery() {

        return Config.GetInstance(_configName).getProperty("DS." + _dsName + ".Query");
    }

    /**
     * Ritorna la query ottenuta dopo la sostituzione dei parametri, oppure null se non è ancora stato chiamato il
     * metodo setParam()
     * 
     * @return DOCUMENT ME!
     */
    protected String getRuntimeQuery() throws AppCrash {

        ErrDetector.GetInstance().preCond((_sql != null),
                "Class: DBDataSet - metodo getRuntimeQuery() non possibile prima di setParam()");

        return _sql;
    }

    private void setWithUr() {

        String queryResult = _sql;
        int indice = 0;

        // controllo se la query termina con; in tal caso rimuovo dal punto e virgola in avanti
        indice = _sql.indexOf(';');
        if (indice != -1) {
            queryResult = _sql.substring(0, indice);
        }

        // verifico l'impostazione sul file di configurazione del parametro facoltativo DS.WITHUR
        // e controllo se WITH UR è già presente nella query
        String propWithUr = Config.GetInstance().getProperty("DS.WITHUR", "FALSE");
        String tmpSql = queryResult.toUpperCase();
        if ((propWithUr.equalsIgnoreCase("TRUE")) && (tmpSql.indexOf("WITH UR") == -1)) {
            queryResult += " WITH UR";
        }

        _sql = queryResult;
    }
}
