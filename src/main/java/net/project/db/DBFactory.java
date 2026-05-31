/*
  DBFactory.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 07/10/2000

  Autore: Simone Z.

  Note:

  Modifiche:

 */

package net.project.db;

import java.lang.reflect.Constructor;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;

/**
 * Questa classe ospita i factory method per la creazione dei vari oggetti del package net.project.db
 * 
 * @author Simone Zorzetti
 */
public abstract class DBFactory {

    /**
     * Metodo per la creazione di "interfacce" di tipo ConnectionPool_itf.
     * <P>
     * Per individuare quale classe istanziare viene letta dalla configurazione passata la proprieta' :
     * <P>
     * DB.ConnectionPoolClass
     * <P>
     *
     * @param config java.lang.String nome della configurazione da usare per leggere i parametri di connessione
     * @return ConnectionPool_itf
     * @exception net.project.errors.AppCrash Nel caso vi fossere problemi nell'istanziazione della classe concreta.
     */
    static public ConnectionPool_itf makeConnectionPool(String config) throws AppCrash {

        String poolClass = Config.GetInstance(config).getProperty("DB.ConnectionPoolClass");
        try {
            ErrDetector.GetInstance().param(poolClass);
            // istanzio la classe per il ConnectionPool
            Class tempClasse = Class.forName(poolClass);
            Constructor procBaseConstr = tempClasse.getConstructor(new Class[] { String.class });

            // Istanzio la classe tramite il costruttore ottenuto
            ConnectionPool_itf result = (ConnectionPool_itf) procBaseConstr.newInstance(new Object[] { config });

            if (isSafeDecorated(config)) {
                // Istanzio dinamicamente il SafeDecorator per non mettere un legame compile time tra i
                // package db e dataset che sarebbe ciclico ed utile solo in caso di decorazione
                Class classe = Class.forName("net.project.dataset.SafeConnectionPoolDecorator");
                Constructor constr = classe.getConstructor(new Class[] { ConnectionPool_itf.class });
                result = (ConnectionPool_itf) constr.newInstance(new Object[] { result });
            }

            return result;

        } catch (Exception e) {
            AppCrash err = new AppCrash(e);
            err.logContext("makeConnectionPool", "Config : " + config + " Classe: " + poolClass);
            throw err;
        }
    }

    /**
     * Metodo per la creazione delle classi che rappresentano le connessioni al DB. Queste sono tutte delle sottoclassi
     * di DBConnection. Per individuare quale classe istanziare viene letta dalla configurazione passata la proprieta' :
     * <P>
     * DB.ConnectionClass
     * <P>
     * 
     * @param config java.lang.String nome della configurazione da usare per leggere i parametri di connessione
     * @param URL java.lang.String URL per la connessione al DB tramite JDBC
     * @param user java.lang.String utente per la connessione
     * @param password java.lang.String password per la connessione
     * @return DBConnection
     * @exception net.project.errors.AppCrash Nel caso vi fossere problemi nell'istanziazione della classe concreta.
     */
    static public DBConnection makeDBConnection(String config, String URL, String user, String password)
            throws AppCrash {

        String connClass = Config.GetInstance(config).getProperty("DB.ConnectionClass");
        try {
            ErrDetector.GetInstance().param(connClass);
            // istanzio la classe per la Connection
            Class tempClasse = Class.forName(connClass);
            Constructor procBaseConstr = tempClasse.getConstructor(new Class[] { String.class, String.class,
                    String.class });
            // Istanzio la classe tramite il costruttore ottenuto
            DBConnection conn = (DBConnection) procBaseConstr.newInstance(new Object[] { URL, user, password });
            conn.setConfigName(config);
            return conn;
        } catch (Exception e) {
            AppCrash err = new AppCrash(e);
            err.logContext("makeConnection", "Config: " + config + "Url: " + URL + " User : " + user + " Pwd: "
                    + password + " Classe: " + connClass);
            throw err;
        }

    }

    /**
     * Metodo per la creazione delle classi che rappresentano le connessioni al DB che "scadono". Queste sono tutte
     * delle sottoclassi di TimedDBConnection. Per individuare quale classe istanziare viene letta dalla configurazione
     * passata la proprieta' :
     * <P>
     * DB.ConnectionClass
     * <P>
     * 
     * @param config java.lang.String nome della configurazione da usare per leggere i parametri di connessione
     * @param URL java.lang.String URL per la connessione al DB tramite JDBC
     * @param user java.lang.String utente per la connessione
     * @param password java.lang.String password per la connessione
     * @return DBConnection
     * @exception net.project.errors.AppCrash Nel caso vi fossere problemi nell'istanziazione della classe concreta.
     */
    static public DBConnection makeTimedDBConnection(String config, String URL, String user, String password)
            throws AppCrash {

        String connClass = Config.GetInstance(config).getProperty("DB.ConnectionClass");
        try {
            ErrDetector.GetInstance().param(connClass);
            // istanzio la classe per la Connection
            Class tempClasse = Class.forName(connClass);
            Constructor procBaseConstr = tempClasse.getConstructor(new Class[] { String.class, String.class,
                    String.class });

            // Istanzio la classe tramite il costruttore ottenuto
            TimedDBConnection conn = (TimedDBConnection) procBaseConstr
                    .newInstance(new Object[] { URL, user, password });
            conn.setConfigName(config);
            return (conn);

        } catch (Throwable e) {
            AppCrash err = new AppCrash(e);
            err.logContext("makeConnection", "Config: " + config + "Url: " + URL + " User : " + user + " Pwd: "
                    + password + " Classe: " + connClass);
            throw err;
        }

    }

    private static boolean isSafeDecorated(String config) {

        String safe = Config.GetInstance(config).getProperty("DB.SafeDBConnection", "false").trim();

        return (safe.equalsIgnoreCase("true") ? true : false);

    }

}
