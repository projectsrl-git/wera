
package net.project.db;

/*
 DBUpdatableEntity_base.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 27/05/1999

 Autore: Rosella V.

 Note:

 Modifiche:
 25/11/99	TL17 ottimizzazione uso stringhe. Rosella V.
 18/02/2000	TL36	Anna L.		Correzione trattamento errato valori null
 01/03/2000	TL37	Anna L.		Sostituzione apici con virgolette nella costruzione di insert e update
 */

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;

import net.project.errors.AppCrash;
import net.project.errors.DBCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;

/**
 * Questa classe estende la entity base e rappresenta una entita' aggiornabile memorizzata in una singola riga di una
 * tabella DB.
 */
public abstract class DBUpdatableEntity_base extends DBEntity_base {

    protected String            _whereCondition    = null;
    private Hashtable           _dirtyFields       = new Hashtable();

    /** Prefissi che indicano i tipi numerici nel DB Q_ P_ M_ e V_ */
    private static final String _numericTypePrefix = "?IQ_P_M_V_";

    /**
     * Costruttore.
     */
    public DBUpdatableEntity_base(String tableName) {

        super(tableName);
    }

    /**
     * Costruttore con nome file configurazione.
     */
    public DBUpdatableEntity_base(String tableName, String configName) {

        super(tableName, configName);
    }

    /**
     * Costruttore con connessione.
     */
    public DBUpdatableEntity_base(DBTransaction transact, String tableName) {

        super(transact, tableName);
    }

    /**
     * Ritorna true se viene recuperata una riga dal database altrimenti false.
     * 
     * @return boolean
     */
    @Override
    public boolean retrieve() throws AppCrash {

        boolean result = false;

        result = super.retrieve();
        _whereCondition = whereCondition();

        return result;
    }

    /**
     * Esecuzione di una operazione di insert/update su una connessione.
     */
    protected void doWork(String query) throws AppCrash {

        Statement st = null;
        DBConnection_itf connection = null;

        try {
            if (Logger.GetInstance().getLogLevel() >= 3) {
                Logger.GetInstance().log3("Dowork " + query);
            }
            connection = super.getConnection();
            st = connection.createStatement();
            int row = st.executeUpdate(query);

            // rinnova la where condition in modo da riflettere la riga attuale
            _whereCondition = whereCondition();

        } catch (SQLException e) {
            DBCrash ex = new DBCrash(e);
            ex.logContext("DBUpdatableEntity_base", "Query: " + query);
            throw ex;
        } catch (AppCrash err) {
            err.logContext("DBUpdatableEntity_base", "Query: " + query);
            throw err;
        } finally {
            try {
                st.close();
                freeConnection();
            } catch (SQLException e) {
                DBCrash ex = new DBCrash(e);
                ex.logContext("DBUpdatableEntity_base", "Errore chiudendo lo statement");
                throw ex;
            }
        }

    }

    /**
     * Inserisce una riga nel database.
     */
    public void insert() throws AppCrash {

        StringBuffer tempBuffer = new StringBuffer();
        String insert = null;

        try {
            ErrDetector.GetInstance().invariant(!_tableColumns.isEmpty());
            tempBuffer.append("insert into ").append(_tableName);
            tempBuffer.append(createColumnsPart());
            tempBuffer.append("values ");
            tempBuffer.append(createValuesPart());
            insert = tempBuffer.toString();
            doWork(insert);
            clearDirtyFieldFlags();
        } catch (AppCrash e) {
            e.logContext("DBUpdatableEntity_base", "insert : HashTable vuota");
            throw e;
        }

    }

    /**
     * Aggiorna una riga del database.
     */
    public void update() throws AppCrash {

        String update = null;
        StringBuffer tempBuffer = new StringBuffer();

        try {
            ErrDetector.GetInstance().invariant(!_tableColumns.isEmpty());
            ErrDetector.GetInstance().invariant(_whereCondition != null);

            tempBuffer.append("update ").append(_tableName).append(" set ");
            tempBuffer.append(createColumnsPart());
            tempBuffer.append(" = ");
            tempBuffer.append(createValuesPart());

            tempBuffer.append(_whereCondition);
            update = tempBuffer.toString();
            doWork(update);
            clearDirtyFieldFlags();
        } catch (AppCrash e) {
            e.logContext("DBUpdatableEntity_base", "update : HashTable vuota");
            throw e;
        }

    }

    /**
     * Eliminazione dell'entita' rappresentata dall'oggetto dal DB
     */
    public void delete() throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(!_tableColumns.isEmpty());
            ErrDetector.GetInstance().invariant(_whereCondition != null);
        } catch (AppCrash e) {
            e.logContext("DBUpdatableEntity_base", "delete : HashTable vuota o wherecontion mancante");
            throw e;
        }

        StringBuffer tempBuffer = new StringBuffer();
        tempBuffer.append("delete from ").append(_tableName).append(_whereCondition);

        String query = tempBuffer.toString();
        doWork(query);

    }

    /**
     * Impostata una entry della hashtable.
     */
    @Override
    public void setAttribute(String attribID, Object value) {

        _dirtyFields.put(attribID.toUpperCase(), new Integer(1));
        super.setAttribute(attribID.toUpperCase(), value); // metto i valori in una Hashtable usando il nome della
                                                           // colonna come chiave
    }

    @Override
    public void clearDirtyFieldFlags() {

        _dirtyFields.clear();
    }

    protected String createColumnsPart() {

        String col = null;
        StringBuffer tempBuffer = new StringBuffer();
        tempBuffer.append(" (");
        for (Enumeration colonne = _dirtyFields.keys(); colonne.hasMoreElements();) {
            col = (String) colonne.nextElement();

            // Se il nome della colonna comprende il tipo di dato (?I o ?S), lo elimino
            if (col.startsWith("?")) {
                col = col.substring(2);
            }

            tempBuffer.append(col);
            if (colonne.hasMoreElements()) {
                tempBuffer.append(",");
            }
        }
        tempBuffer.append(") ");

        return tempBuffer.toString();
    }

    protected String createValuesPart() {

        String temp;
        Object colValue = new Object();
        StringBuffer tempBuffer = new StringBuffer();
        tempBuffer.append(" ( ");
        for (Enumeration colonne = _dirtyFields.keys(); colonne.hasMoreElements();) {
            colValue = colonne.nextElement();
            if ((_tableColumns.get(colValue) == null) || (((String) _tableColumns.get(colValue)).equals("#null#"))) {
                temp = "null";
                tempBuffer.append(temp);
                if (colonne.hasMoreElements()) {
                    tempBuffer.append(",");
                }
            } else {
                temp = (String) _tableColumns.get(colValue);
                temp = temp.replace('\'', ' ');

                // La naming convention per i campi del DB su DB2 ci permette di desumere il tipo di dato
                // della colonna dalle prime due lettere del suo nome
                String typeId = ((String) colValue).substring(0, 2);
                if (_numericTypePrefix.indexOf(typeId) >= 0) {
                    tempBuffer.append(temp);
                } else {
                    tempBuffer.append("'").append(temp).append("'");
                }

                if (colonne.hasMoreElements()) {
                    tempBuffer.append(",");
                }
            }

        }
        tempBuffer.append(") ");

        return tempBuffer.toString();
    }
}
