
package net.project.db;

/*
 DBConnection_itf.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 27/05/1999

 Autore: Rosella V.

 Note:

 Modifiche:

 */

import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLWarning;
import java.sql.Statement;

import net.project.errors.AppCrash;

/**
 * Questa interfaccia rappresenta una connessione alla base dati come viene utilizzata dalle applicazioni. E'
 * sostanzialmente un'interfaccia che restringe la JDBC Connection
 */
public interface DBConnection_itf {

    /**
     * Ritorna null fino a che un nuovo warning viene riportato per questa connessione.
     * 
     * @exception AppCrash.
     */
    public void clearWarnings() throws AppCrash;

    /**
     * Rende effettive le esecuzioni degli statement prima della sua chiamata.
     * 
     * @exception AppCrash.
     */
    public void commit() throws AppCrash;

    /**
     * Rende effettive le esecuzioni degli statement prima della sua chiamata.
     * 
     * @return java.sql.Statement.
     * @exception AppCrash.
     */
    public Statement createStatement() throws AppCrash;

    /**
     * Imposta il flag isBusy a false. La connessione viene resa diponibile per altre operazioni.
     */
    public void free();

    /**
     * Ritorna lo stato corrente del fag auto-commit.
     * 
     * @return boolean.
     * @exception AppCrash.
     */
    public boolean getAutoCommit() throws AppCrash;

    /**
     * Ritorna il catalog name corrente delle connessioni.
     * 
     * @return java.lang.String
     * @exception AppCrash
     */
    public String getCatalog() throws AppCrash;

    /**
     * Ritorna informazioni relative alla tabelle del database, alla grammatica SQL supportata, ecc.
     * 
     * @return java.sql.DatabaseMetaData.
     * @exception AppCrash.
     */
    public DatabaseMetaData getMetaData() throws AppCrash;

    /**
     * Ritorna lo stato attuale del transaction isolation mode.
     * 
     * @return int
     * @exception AppCrash
     */
    public int getTransactionIsolation() throws AppCrash;

    /**
     * Ritorna il primo warning generato dalla connessione.
     * 
     * @return SQLWarning.
     * @exception AppCrash.
     */
    public SQLWarning getWarnings() throws AppCrash;

    /**
     * Viene testata la chiusura della connesione.
     * 
     * @return boolean.
     * @exception AppCrash.
     */
    public boolean isClosed() throws AppCrash;

    /**
     * Viene testato se la connessione è in modalità di sola lettura.
     * 
     * @return boolean.
     * @exception AppCrash.
     */
    public boolean isReadOnly() throws AppCrash;

    /**
     * Ritorna la forma 'native' dello statement che il driver potrebbe aver inviato.
     * 
     * @param java.lang.String stringa che definisce la query.
     * @return java.lang.String.
     * @exception AppCrash.
     */
    public String nativeSQL(String query) throws AppCrash;

    /**
     * Consente l'impostazione dei parametri di input/output e la gestione dei metodi per eseguire una chiamata di
     * procedura SQL.
     * 
     * @param java.lang.String uno statement SQL.
     * @return CallableStatement.
     * @exception AppCrash.
     */
    public CallableStatement prepareCall(String sql) throws AppCrash;

    /**
     * Uno statement SQL, con o senza parametri di input può essere precompilato e salvato in un oggetto
     * PreparatedStatement. Questo oggetto può quindi essere usato per eseguire in modo più efficiente tale statement
     * più volte.
     * 
     * @param java.lang.String uno statement SQL.
     * @return PreparatedStatement.
     * @exception AppCrash.
     */
    public PreparedStatement prepareStatement(String sql) throws AppCrash;

    /**
     * Consente il ritorno allo stato del database dopo l'ultima rollback
     * 
     * @exception AppCrash.
     */
    public void rollback() throws AppCrash;

    /**
     * Se il flag di autoCommit è impostato a true ogni statement viene eseguito e considerato come una ttransazione.
     * 
     * @param boolean valore di autoCommit.
     * @exception AppCrash.
     */
    public void setAutoCommit(boolean enableAutoCommit) throws AppCrash;

    /**
     * Imposta un nome di catalogo.
     * 
     * @param java.lang.String nome del catalogo.
     * @exception AppCrash.
     */
    public void setCatalog(String catalog) throws AppCrash;

    /**
     * Una connessione può essere impostata per sola lettura per ottimizzare la gestione del database.
     * 
     * @param boolean valore del flag per sola lettura.
     * @exception AppCrash.
     */
    public void setReadOnly(boolean readOnly) throws AppCrash;

    /**
     * Cambia il livello di isolamento di una transazione.
     * 
     * @param int livello di isolamento.
     * @exception AppCrash.
     */
    public void setTransactionIsolation(int level) throws AppCrash;
}
