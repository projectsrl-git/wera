/*

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore: 

  Note:

 */

package net.project.dataset;

import java.util.Iterator;

import net.project.db.ConnectionPool_itf;
import net.project.db.DBConnectionRegister;
import net.project.db.DBConnection_itf;
import net.project.db.ExConnectionPool_itf;
import net.project.errors.AppCrash;
import net.project.errors.Logger;

/**
 * Questa classe e' un decorator su ConnectionPool_itf che aggiunge al metodo getAConnection() un passo di
 * 'registrazione' dell'uso della conn. da parte del thread chiamante. Questo rende possibile implementare il metodo
 * freeAllThreadConnections che libera tutte le connessioni rimaste eventualmente occupate dal thread chiamante.
 * 
 * @author sim
 */
public class SafeConnectionPoolDecorator implements ExConnectionPool_itf {

    private ConnectionPool_itf   _thePool;
    private DBConnectionRegister _register = new DBConnectionRegister();

    /**
     * Crea un nuovo oggetto di tipo SafeConnectionPoolDecorator .
     * 
     * @param connPool il ConnectionPool_itf da decorare
     */
    public SafeConnectionPoolDecorator(ConnectionPool_itf connPool) {

        _thePool = connPool;
    }

    /**
     * @see net.project.db.ConnectionPool_itf#getAConnection()
     */
    @Override
    public DBConnection_itf getAConnection() throws AppCrash {

        DBConnection_itf dbcon = _thePool.getAConnection();
        DBConnectionRegister reg = (DBConnectionRegister) _register.get();
        reg.add(dbcon);

        return dbcon;
    }

    public ConnectionPool_itf getPool() {

        return _thePool;
    }

    /**
     * @see net.project.db.ConnectionPool_itf#shutdown()
     */
    @Override
    public void shutdown() throws AppCrash {

        _thePool.shutdown();
    }

    /**
     * Libera tutte le connessioni rimaste occupate per il thread chiamante
     * 
     * @see net.project.db.ConnectionPool_itf#freeAllThreadConnections()
     */
    @Override
    public void freeAllThreadConnections() {

        DataSetFactory.getInstance().closeAllThreadDataSets();

        DBConnectionRegister reg = (DBConnectionRegister) _register.get();
        Iterator connections = reg.getAllThreadConnections();

        while (connections.hasNext()) {
            Logger.GetInstance().log0("SafeConnectionPoolDecorator: connection closed in freeAllThreadConnections");

            DBConnection_itf dbcon = (DBConnection_itf) connections.next();
            connections.remove();
            dbcon.free();

        }
    }

    /**
     * Elimina del registro la connessione che e' stata liberata
     * 
     * @param dbConn la connessione liberata
     */
    @Override
    public void freeNotify(DBConnection_itf dbConn) {

        DBConnectionRegister reg = (DBConnectionRegister) _register.get();
        reg.remove(dbConn);
        _thePool.freeNotify(dbConn);

    }
}
