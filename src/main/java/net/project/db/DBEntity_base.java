
package net.project.db;

/*
 DBEntity_base.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 27/05/1999

 Autore: Rosella V.

 Note:

 Modifiche:	07/10/99	TL16 revisione eccezioni
 25/11/99	TL17 ottimizzazione uso stringhe. Rosella V.
 18/02/2000	TL36	Anna L.		Correzione trattamento errato valori null
 02/10/2000	Anna L.		Aggiunto nuovo costruttore con indicazione del ConnectionPool da utilizzare.

 */

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;

import net.project.errors.AppCrash;
import net.project.errors.DBCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.misc.Config;

/**
 * Questa classe rappresenta una entita' leggibile memorizzata nella base dati in una singola riga di una tabella.
 */
public abstract class DBEntity_base {

    protected final static int FREE_CONNECTION = 1;
    protected final static int KEEP_CONNECTION = 2;

    private DBConnection_itf   _connection     = null;
    protected Hashtable        _tableColumns   = null;
    private int                _mode           = 0;
    String                     _tableName      = null;
    String                     _configName     = "";

    /**
     * Costruttore. Se il nome della tabella non contiene gia' il prefisso con il sottosistema DB2 allora questo viene
     * aggiunto leggendo la proprieta' di configurazione DBEntity.nomeDB (se esiste)
     * 
     * @param tableName nome tabella
     */
    protected DBEntity_base(String tableName) {

        if (tableName.indexOf(".") > 4) {
            String prefix = Config.GetInstance().getProperty("DBEntity.nomeDB");
            if (prefix != null) {
                tableName = prefix + "." + tableName;
            }
        }
        _tableColumns = new Hashtable(101, 0.75f);
        _mode = FREE_CONNECTION;
        _tableName = tableName;
    }

    /**
     * Costruttore con nome file configurazione. Se il nome della tabella non contiene gia' il prefisso con il
     * sottosistema DB2 allora questo viene aggiunto leggendo la proprieta' di configurazione DBEntity.nomeDB (se
     * esiste)
     * 
     * @param tableName nome tabella
     * @param configName nome configurazione
     */
    protected DBEntity_base(String tableName, String configName) {

        this(tableName);
        _configName = configName;
        if (tableName.indexOf(".") > 4) {
            String prefix = Config.GetInstance(_configName).getProperty("DBEntity.nomeDB");
            if (prefix != null) {
                tableName = prefix + "." + tableName;
            }
        }
    }

    /**
     * Costruttore con connessione.
     */
    protected DBEntity_base(DBTransaction transact, String tableName) {

        this(tableName);
        _connection = transact.getDBConnection();
        _mode = KEEP_CONNECTION;
    }

    /**
     * Ritorna il valore dell'attributo identificato da attribID.
     * 
     * @param attribID java.lang.String
     * @return java.lang.Object
     */
    public Object getAttribute(String attribID) throws AppCrash {

        Object attribute = null;
        try {
            ErrDetector.GetInstance().preCond(_tableColumns != null);
            attribute = _tableColumns.get(attribID.toUpperCase());
            if (attribute == null) return null;

            if (attribute.equals("#null#")) {
                attribute = null;
            }
        } catch (AppCrash e) {
            e.logContext(this.getClass().getName(), "Nome attributo " + attribID);
            throw e;
        }

        return attribute;
    }

    /**
     * Metodo
     * 
     * @return DBConnection_itf
     */
    protected DBConnection_itf getConnection() throws AppCrash {

        try {
            if (_connection == null) {
                _connection = ConnectionPool.GetInstance(_configName).getAConnection();
            }
        } catch (AppCrash e) {
            e.logContext("DBEntity_base", "errore di recupero connessione da ConnectionPool");
            throw e;
        }
        return _connection;
    }

    void freeConnection() {

        if (_mode != KEEP_CONNECTION && _connection != null) {
            _connection.free();
            _connection = null;
        }
    }

    /**
     * Controlla l'impostazione come FREE_CONNECTION o KEEP_CONNECTION.
     * 
     * @return int
     */
    protected int getMode() {

        return _mode;
    }

    /**
     * Ritorna true se viene recuperata una riga dal database altrimenti false.
     * 
     * @return boolean
     */
    public boolean retrieve() throws AppCrash {

        ResultSetMetaData meta = null;
        boolean ret = false;
        Statement st = null;
        StringBuffer query = new StringBuffer();

        try {
            query.append(getQuery());

            _connection = getConnection();
            long start = System.currentTimeMillis();
            st = _connection.createStatement();
            String completeQuery = query.toString();

            if (Logger.GetInstance().getLogLevel() >= 3) {
                Logger.GetInstance().log3("Retrieve " + completeQuery);
            }
            ResultSet rs = st.executeQuery(completeQuery);

            long end = System.currentTimeMillis();
            if (end - start > 2000) {
                int startWhere = completeQuery.toLowerCase().indexOf("where");
                Logger.GetInstance().log0(
                        "TIMEretrieve;" + (end - start) + ";" + this.getClass().getName() + ";"
                                + completeQuery.substring(startWhere));
            }

            meta = rs.getMetaData(); // accesso a meta data

            if (rs.next()) {
                // per ogni colonna in result set
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    String columnLabel = meta.getColumnLabel(i);
                    String columnValue = rs.getString(meta.getColumnLabel(i));
                    setAttribute(columnLabel, columnValue);
                }
                ret = true;

                // Resetto i flag dirty per i campi
                clearDirtyFieldFlags();

                // controllo che sia stata recuperata una sola riga
                ErrDetector.GetInstance().invariant(!rs.next());
            }

        } catch (AppCrash e) {
            e.logContext(this.getClass().getName(), "Query: " + query.toString());
            throw e;
        } catch (SQLException e) {
            DBCrash ex = new DBCrash(e);
            ex.logContext(this.getClass().getName(), "Query: " + query.toString());
            throw ex;
        } finally {
            try {
                if (st != null) st.close();
                freeConnection();
            } catch (SQLException e) {
                DBCrash ex = new DBCrash(e);
                ex.logContext(this.getClass().getName(), "Errore chiudendo lo statement");
                throw ex;
            }
        }

        return ret;
    }

    /**
     * Restiruisce la condizione where che identifica univocamente la riga.
     */
    protected abstract String whereCondition() throws AppCrash;

    /**
     * Impostata una entry della hashtable.
     */
    public void setAttribute(String attribID, Object value) {

        Object o = null;
        if (value == null) {
            o = "#null#";
        } else {
            o = value;
        }
        String val = ((String) o).trim();
        _tableColumns.put(attribID.toUpperCase(), val); // metto i valori in una Hashtable usando il nome della colonna
                                                        // come chiave
    }

    /**
     * Ritorna informazioni sulla classe.
     * 
     * @return java.lang.String.
     */
    @Override
    public String toString() {

        StringBuffer temp = new StringBuffer();
        temp.append("Valori colonne: ");
        String col = null;
        for (Enumeration colonne = _tableColumns.keys(); colonne.hasMoreElements();) {
            col = (String) colonne.nextElement();
            temp.append(col).append("=").append((String) _tableColumns.get(col));
            if (colonne.hasMoreElements()) {
                temp.append(",");
            }
        }
        return temp.toString();
    }

    public void clearDirtyFieldFlags() {

        // nulla da fare
    }

    /**
     * Questo metodo protetto viene utilizzato per recuperare la query da eseguire. Di default viene restituito
     * "select * from " + whereCondition(). Le sottoclassi possono ridefinirlo per modificare questo comportamento (ad
     * esempio per definire query complesse con join) .
     */
    protected String getQuery() throws AppCrash {

        String que = "select * from " + _tableName + " " + whereCondition();
        return que;
    }

}
