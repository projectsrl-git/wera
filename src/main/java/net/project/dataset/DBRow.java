/*
  DBRow.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione:

  Autore: Pietro G. e Luca M.

  Note:

  Modifiche:

 */

package net.project.dataset;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.project.errors.AppCrash;
import net.project.errors.DBCrash;
import net.project.errors.ErrDetector;
import net.project.errors.ParamCrash;
import net.project.misc.Config;

/**
 * Classe che rappresenta una riga di un oggetto DBDataSet.
 */
public class DBRow implements Row_itf {

    // il ResultSet del quale la presente classe rappresenta una riga
    private ResultSet _resultSet = null;
    private int       _useShort  = 0;

    /**
     * Costruttore.
     * 
     * @param java.sql.ResultSet set valore dell'attributo _set
     * @exception net.project.errors.ParamCrash se il parametro in ingresso è null
     */
    public DBRow(ResultSet set) throws ParamCrash {

        // Controllo formale del parametro in ingresso.
        ErrDetector.GetInstance().param(set);

        _resultSet = set;
        _useShort = Integer.parseInt(Config.GetInstance().getProperty("dataset.DBRow.useShort", "0"));
    }

    /**
     * Costruttore.
     */
    DBRow() {

    }

    /**
     * Restituisce il valore di un campo della riga. Se la proprieta' di configurazione dataset.DBRow.useShort e'
     * definita e valorizzata con un valore diverso da 0 il mapping tra SMALLINT e Short e' abilitato. Se questa
     * proprieta' manca o vale 0 quando si presenta una colonna SMALLINT viene restituito un Integer come faceva la
     * versione 6.1 di DB2
     * 
     * @param java.lang String fieldName il nome del campo
     * @return java.lang.Object il valore del campo
     * @exception net.project.errors.ParamCrash se il parametro in ingresso è null
     * @exception net.project.errors.AppCrash in caso di errore nel recupero del valore del campo
     */
    @Override
    public Object getField(String fieldName) throws AppCrash {

        // Controllo formale del parametro in ingresso.
        ErrDetector.GetInstance().param(fieldName);

        try {
            Object obj = _resultSet.getObject(fieldName);

            // Se il mapping SMALLINT -> Short e' abilitato ritorno subito quanto ho ottenuto dal resultset
            if (_useShort != 0 || obj == null) return obj;

            // altrimenti controllo se obj e' uno Short, ovvero se la colonna e' uno SMALLINT;
            // in questo caso restiruisco un Integer come faceva la versione 6.1 di DB2
            if (obj instanceof java.lang.Short) {
                java.lang.Short shrt = (java.lang.Short) obj;
                return new java.lang.Integer(shrt.intValue());
            }

            // se obj non e' uno Short lo restituisco cosi' com'e'
            return obj;

        } catch (SQLException sqle) {
            DBCrash dbc = new DBCrash(sqle);
            dbc.logContext("DBRow", "fieldName = " + fieldName);
            throw dbc;
        }

    }

    /**
     * Restituisce il valore di un campo della riga. Se la proprieta' di configurazione dataset.DBRow.useShort e'
     * definita e valorizzata con un valore diverso da 0 il mapping tra SMALLINT e Short e' abilitato. Se questa
     * proprieta' manca o vale 0 quando si presenta una colonna SMALLINT viene restituito un Integer come faceva la
     * versione 6.1 di DB2
     * 
     * @param int fieldNo il numero del campo
     * @return java.lang.Object il valore del campo
     * @exception net.project.errors.ParamCrash se il parametro in ingresso è null
     * @exception net.project.errors.AppCrash in caso di errore nel recupero del valore del campo
     */
    @Override
    public Object getField(int fieldNo) throws AppCrash {

        // Controllo formale del parametro in ingresso.
        ErrDetector.GetInstance().preCond(fieldNo > 0);

        try {
            Object obj = _resultSet.getObject(fieldNo);

            // Se il mapping SMALLINT -> Short e' abilitato ritorno subito quanto ho ottenuto dal resultset
            if (_useShort != 0 || obj == null) return obj;

            // altrimenti controllo se obj e' uno Short, ovvero se la colonna e' uno SMALLINT;
            // in questo caso restiruisco un Integer come faceva la versione 6.1 di DB2
            if (obj instanceof java.lang.Short) {
                java.lang.Short shrt = (java.lang.Short) obj;
                return new java.lang.Integer(shrt.intValue());
            }

            // se obj non e' uno Short lo restituisco cosi' com'e'
            return obj;

        } catch (SQLException sqle) {
            DBCrash dbc = new DBCrash(sqle);
            dbc.logContext("DBRow", "fieldNo = " + fieldNo);
            throw dbc;
        }

    }

    /**
     * Conta il numero di colonne del DataSet
     * 
     * @return int numero di colonne del DataSet
     * @exception net.project.errors.DBCrash
     */
    @Override
    public int getColumnNo() throws DBCrash {

        try {
            return _resultSet.getMetaData().getColumnCount();

        } catch (SQLException sqle) {
            DBCrash dbc = new DBCrash(sqle);
            dbc.logContext("DBDataSet", toString());
            throw dbc;
        }

    }

    protected ResultSet getResultSet() {

        return _resultSet;
    }
}
