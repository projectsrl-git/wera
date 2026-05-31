
package net.project.db;

/*
 DBTransaction.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 29/05/1999

 Autore: Simone Z.

 Note:

 Modifiche:
 25/11/99	TL17 ottimizzazione uso stringhe. Rosella V.

 */

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Statement;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;

/**
 * Classe per la gestione delle transazioni
 */

public class DBTransaction {

    private DBConnection_itf _connection = null;

    /**
     * Costruttore. Recupera la connessione necessaria dal ConnectionPool di default
     * 
     * @exception AppCrash.
     */
    public DBTransaction() throws AppCrash {

        try {
            _connection = ConnectionPool.GetInstance().getAConnection();
            _connection.setAutoCommit(false);
        } catch (AppCrash e) {
            e.logContext("DBTransaction", "Impossibile recuperare connessione");
            throw e;
        }

    }

    /**
     * Costruttore. Recupera la connessione necessaria dal ConnectionPool indicato dal parametro passato
     * 
     * @param DBname java.lang.String nome simbolico del database al quale connettersi
     * @exception AppCrash.
     */
    public DBTransaction(String dBname) throws AppCrash {

        try {
            _connection = ConnectionPool.GetInstance(dBname).getAConnection();
            _connection.setAutoCommit(false);
        } catch (AppCrash e) {
            e.logContext("DBTransaction", "Impossibile recuperare connessione");
            throw e;
        }

    }

    /**
     * Gestione della commit sulla transazione.
     */
    public void commit() throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_connection != null);
            _connection.commit();
        } catch (AppCrash e) {
            e.logContext("DBTransaction", "Errore durante la commit: _connection e' null");
            throw e;
        }
    }

    /**
     * Fine della transazione. La connessione viene restituita al ConnectionPool
     */
    public void end() throws AppCrash {

        if (_connection == null) return;

        try {
            _connection.setAutoCommit(true);
            _connection.free();
            _connection = null;
        } catch (AppCrash e) {
            e.logContext("DBTransaction", "errore durante end");
            throw (e);
        }
    }

    /**
     * Implementa la visione della connessione che viene resa disponibile solo a DBEntity_base e DBUpdatableEntity_base.
     */
    DBConnection_itf getDBConnection() {

        return _connection;
    }

    /**
     * Gestione della rollback.
     */
    public void rollBack() throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(_connection != null);
            _connection.rollback();
        } catch (AppCrash e) {
            e.logContext("DBTransaction", "Errore durante la rollback: _connection e' null");
            throw e;
        }
    }

    /**
     * Serve per impostare il livello di isolazione che si desidera (vedi JDBC).
     * 
     * @param transactionIsolation int
     */
    public void setTransactionIsolation(int transactionIsolation) throws AppCrash {

        try {
            _connection.setTransactionIsolation(transactionIsolation);
        } catch (AppCrash e) {
            e.logContext("DBTransaction", "errore in setTransactionIsolation");
            throw (e);
        }
    }

    /**
     * Questo metodo serve per creare uno Statement JDBC appartenente alla transazione rappresentata dall'oggetto
     * DBTransaction
     */
    public Statement createStatement() throws AppCrash {

        Statement result = null;
        try {
            result = _connection.createStatement();
        } catch (AppCrash e) {
            e.logContext("DBTransaction", "createStatement");
            throw (e);
        }
        return result;
    }

    /**
     * Questo metodo serve per creare uno CallableStatement JDBC appartenente alla transazione rappresentata
     * dall'oggetto DBTransaction
     */
    public CallableStatement prepareCall(String sql) throws AppCrash {

        CallableStatement result = null;
        try {
            result = _connection.prepareCall(sql);
        } catch (AppCrash e) {
            e.logContext("DBTransaction", "prepareCall:" + sql);
            throw (e);
        }
        return result;
    }

    /**
     * Questo metodo serve per creare un PreparedStatement JDBC appartenente alla transazione rappresentata dall'oggetto
     * DBTransaction
     */
    public PreparedStatement prepareStatement(String sql) throws AppCrash {

        PreparedStatement result = null;
        try {
            result = _connection.prepareStatement(sql);
        } catch (AppCrash e) {
            e.logContext("DBTransaction", "prepareStatement:" + sql);
            throw (e);
        }
        return result;
    }

}
