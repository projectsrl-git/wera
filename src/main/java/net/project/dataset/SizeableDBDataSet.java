/*
  SizeableDBDataSet.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 03/03/2003

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.dataset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

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
 * <P>
 * Proprietà lette dal file di configurazione (oltre a quelle lette da DBDataSet): DS.dsName.Counter.Query = query per
 * il conteggio delle righe contenute nel SizeableDBDataSet
 */
public class SizeableDBDataSet extends DBDataSet implements SizeableDataSet_itf {

    private int    _numberOfRows = -1;
    private String _counterQuery = null;

    /**
     * Costruttore.
     */
    public SizeableDBDataSet(String configName, String dsName) throws AppCrash {

        super(configName, dsName);
    }

    /**
     * Ritorna il numero di righe presenti nel dataset corrente.
     * 
     * @return int
     * @exception net.project.errors.AppCrash
     */
    @Override
    public int getNumberOfRows() throws AppCrash {

        ErrDetector.GetInstance().preCond((_numberOfRows != -1),
                "Class: SizeableDBDataSet - metodo getNumberOfRows() non possibile prima di open()");
        return _numberOfRows;
    }

    /**
     * Apre una connessione al database, recupera il ResultSet ed esegue la query di conteggio.
     * 
     * @return void
     * @exception net.project.errors.AppCrash in caso di errore nella connessione al database o nella parametrizzazione
     *                con TextTemplate
     */
    @Override
    public void open() throws AppCrash {

        _numberOfRows = countRows();
        super.open();
    }

    /**
     * Valorizza l'attributo _sql, contenente la query da eseguire.
     * 
     * @param java.util.Map parametri Map per valorizzare la parte parametrica della query scritta nel file di
     *            configurazione
     * @return void
     * @exception net.project.errors.ParamCrash
     */
    @Override
    public void setParam(Map parametri) throws AppCrash {

        super.setParam(parametri);

        _counterQuery = getCounterQuery();
        if (_counterQuery == null) {
            _counterQuery = createCounterQuery();
        } else {
            TextTemplate tt = null;
            try {
                tt = new TextTemplate(_counterQuery);

            } catch (ParamCrash pc) {
                pc.logContext("SizeableDBDataSet", toString() + "; parametri = " + parametri);
                throw pc;
            }

            if (!tt.replace(parametri)) {
                AppCrash ac = new AppCrash();
                ac.logContext("SizeableDBDataSet", toString() + "; parametri = " + parametri);
                throw ac;
            }
            _counterQuery = tt.getText();
        }
    }

    /**
     * Questo metodo serve per recuperare la stringa che rappresenta la query di conteggio prima del replace dei
     * parametri. Di default legge la proprieta "DS." + _dsName + ".Counter.Query" dal file di configurazione. Puo'
     * essere ridefinito dalle sottoclassi per modificarne il comportamento
     */
    protected String getCounterQuery() {

        return Config.GetInstance(getConfigName()).getProperty("DS." + getDsName() + ".Counter.Query");
    }

    /**
     * Crea la query di conteggio a partire dalla query di estrazione dei dati.
     *
     * @exception net.project.errors.AppCrash
     * @return java.lang.String La query generata.
     */
    private String createCounterQuery() throws AppCrash {

        String sql = getRuntimeQuery().toUpperCase();
        StringBuffer counterQuery = new StringBuffer("SELECT COUNT(*) FROM ( ");
        if (sql.indexOf(" ORDER ") == -1) {
            if (sql.indexOf("WITH UR") == -1) {
                counterQuery.append(getRuntimeQuery());
            } else {
                counterQuery.append(getRuntimeQuery().substring(0, sql.indexOf("WITH UR")));
            }
        } else {
            counterQuery.append(getRuntimeQuery().substring(0, sql.indexOf(" ORDER ")));
        }

        counterQuery.append("  ) AS PIPPO");
        if (Config.GetInstance(getDBConfigName()).getProperty("DB.db2", "true").equals("true")) {
            counterQuery.append(" WITH UR");
        }
        return counterQuery.toString();
    }

    /**
     * Esegue la query di conteggio delle righe.
     * 
     * @return int
     * @exception net.project.errors.AppCrash
     */
    private int countRows() throws AppCrash {

        ConnectionPool_itf connectionPool = ConnectionPool.GetInstance(getDBConfigName());
        DBConnection_itf dbConnection = connectionPool.getAConnection();
        dbConnection.setAutoCommit(false);
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            long start = System.currentTimeMillis();
            statement = dbConnection.createStatement();
            if (Logger.GetInstance().getLogLevel() >= 3) {
                Logger.GetInstance().log3("sto per eseguire la seguente query: " + _counterQuery);
            }

            resultSet = statement.executeQuery(_counterQuery);

            long end = System.currentTimeMillis();
            if (end - start > 25000) {
                String query;
                if (end - start > 300000) {
                    query = _counterQuery;
                } else {
                    query = _counterQuery.substring(Math.max(0, _counterQuery.length() - 50));
                }

                Logger.GetInstance().log0("TIMEquery;" + (end - start) + ";" + getDsName() + ";" + query);
            }

            resultSet.next();
            int rows = resultSet.getInt(1);
            return rows;
        } catch (AppCrash ac) {
            ac.logContext("SizeableDBDataSet", toString());
            throw ac;
        } catch (SQLException sqle) {
            DBCrash dbc = new DBCrash(sqle);
            dbc.logContext("SizeableDBDataSet", toString());
            throw dbc;
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (dbConnection != null) {
                    dbConnection.free();
                }
            } catch (SQLException sqle) {
                DBCrash dbc = new DBCrash(sqle);
                dbc.logContext("SizeableDBDataSet", toString());
                throw dbc;
            }
        }
    }
}
