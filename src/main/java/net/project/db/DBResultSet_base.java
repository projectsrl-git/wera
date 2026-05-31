/*
  DBResultSet_base.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 10/09/2000

  Autore: Andrea R.

  Note:

  Modifiche:

 */

package net.project.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import net.project.errors.AppCrash;
import net.project.errors.DBCrash;

/**
 * Specializza la classe DBEntity_base consentendo la gestione di un result set e non di un singolo elemento
 */
public abstract class DBResultSet_base extends DBEntity_base {

    private ResultSet         _resultSet = null;
    private Statement         _statement = null;
    private ResultSetMetaData _meta      = null;
    private String            _join      = null;

    /**
     * Costruttore.
     */
    protected DBResultSet_base(String tableName) {

        super(tableName);
    }

    /**
     * Costruttore con nome file configurazione.
     */
    protected DBResultSet_base(String tableName, String configName) {

        super(tableName, configName);
    }

    /**
     * Costruttore con connessione.
     */
    protected DBResultSet_base(DBTransaction transact, String tableName) {

        super(transact, tableName);
    }

    /**
     * Ritorna true se viene recuperata una riga dal database altrimenti false.
     * 
     * @return boolean
     */
    @Override
    public boolean retrieve() throws AppCrash {

        boolean ret = false;
        StringBuffer query = new StringBuffer();

        try {
            if (_resultSet != null) _resultSet.close();
            if (_statement != null) _statement.close();

            _statement = null;
            _resultSet = null;
            _meta = null;

            if (_join == null) {
                query.append("select * from ").append(_tableName);
            } else {
                query.append("select * from ").append(_join);
            }
            query.append(whereCondition());

            _statement = getConnection().createStatement();
            _resultSet = _statement.executeQuery(query.toString());
            _meta = _resultSet.getMetaData();
            ret = true;
        } catch (SQLException e) {
            DBCrash ex = new DBCrash(e);
            ex.logContext(this.getClass().getName(), "Query: " + query.toString());
            throw ex;
        } catch (Throwable e) {
            AppCrash appCrash = new AppCrash(e.getMessage());
            appCrash.logContext(this.getClass().getName(), "Query: " + query.toString());
            throw (appCrash);
        }
        return (ret);
    }

    /**
     * Carica il record successivo
     *
     * @return boolean true se il record è stato caricato; false altrimenti
     */
    public boolean next() throws AppCrash {

        boolean ret = false;

        if (_resultSet == null) return (ret);

        try {
            if (_resultSet.next()) {
                for (int i = 1; i <= _meta.getColumnCount(); i++) {
                    setAttribute(_meta.getColumnLabel(i), _resultSet.getString(_meta.getColumnLabel(i)));
                }
                ret = true;
            } else {
                _resultSet.close();
                _statement.close();
                _resultSet = null;
                _statement = null;
                _meta = null;
            }
        } catch (Throwable e) {
            AppCrash appCrash = new AppCrash(e.getMessage());
            throw (appCrash);
        }
        return (ret);
    }

    /**
     * Imposta l'elenco delle tabelle su cui affettuare la join
     */
    public void setJoin(String tables) {

        _join = tables;
    }

}
