/*
  Logger.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 14/05/1999

  Autore: Simone Z.

  Note:

  Modifiche:	29/09/2000	Anna L.		Aggiunta interfaccia Logger_itf
                10/03/2002	Ilaria		modificato meccanismo di logger

 */

package net.project.errors;

import java.lang.reflect.Constructor;
import java.util.Hashtable;

/**
 * E' un singleton che permette di eseguire il log di stringhe per due diversi tipi di motivi: debug ed errore. I due
 * output vengono messi su due diversi file letti da variabili di configurazione
 */
public class Logger implements Logger_itf {

    static private Logger_itf _DefaultInstance   = null;
    static private Hashtable  _Instances         = new Hashtable();

    private InfoCollection    _info              = new InfoCollection();
    private Hashtable         _field             = new Hashtable();
    private String            _nomeDump;

    private LogWriter_itf     _debugLogger       = null;
    private LogWriter_itf     _errorLogger       = null;
    private LogWriter_itf     _syslogLogger      = null;
    private LogWriter_itf     _applicationLogger = null;

    private int               _logLevel;

    /**
     * Constructor privato. Legge il nome del file di debug dalla proprieta' "DebugFile" ed il nome del file di errore
     * dalla proprieta' "CrashFile" le proprieta' sono lette dalla configurazione di default
     */
    protected Logger() {

        this("");
        setLogLevel(0);
    }

    /**
     * Constructor privato. Legge il nome del file di debug dalla proprieta' "DebugFile" ed il nome del file di errore
     * dalla proprieta' "CrashFile" le proprieta' sono lette dalla configurazione indicata
     */
    protected Logger(String confName) {

        String debugClass = ErrorConfig.GetInstance(confName).getProperty("Logger.DebugClass");
        String errorClass = ErrorConfig.GetInstance(confName).getProperty("Logger.ErrorClass");
        String syslogClass = ErrorConfig.GetInstance(confName).getProperty("Logger.SyslogClass");
        String applicationClass = ErrorConfig.GetInstance(confName).getProperty("Logger.ApplicationClass");
        try {
            setLogLevel(0);
            Class tempClassDeb = Class.forName(debugClass);
            Constructor procBaseConstrDeb = tempClassDeb.getConstructor(new Class[] { String.class, String.class });
            _debugLogger = (LogWriter_itf) procBaseConstrDeb.newInstance(new Object[] { "DEB", confName });

            Class tempClassErr = Class.forName(errorClass);
            Constructor procBaseConstrErr = tempClassErr.getConstructor(new Class[] { String.class, String.class });
            _errorLogger = (LogWriter_itf) procBaseConstrErr.newInstance(new Object[] { "ERR", confName });

            if (applicationClass != null) {
                Class tempClassApp = Class.forName(applicationClass);
                Constructor procBaseConstrApp = tempClassApp.getConstructor(new Class[] { String.class, String.class });
                _applicationLogger = (LogWriter_itf) procBaseConstrApp.newInstance(new Object[] { "APP", confName });

            }
            if (syslogClass != null) {
                Class tempClassSys = Class.forName(syslogClass);
                Constructor procBaseConstrSys = tempClassSys.getConstructor(new Class[] { String.class, String.class });
                _syslogLogger = (LogWriter_itf) procBaseConstrSys.newInstance(new Object[] { "SYS", confName });
            }

        } catch (Throwable e) {
            e.printStackTrace(System.out);
        }

    }

    /**
     * Istanzia la classe InfoCollection e gli passa il nome e il valore del campo da aggiungere nell'Hashtable
     * 
     * @param nome e' il nome della stringa da inserire nell'Hashtable
     * @param valore e' il valore della stringa da inserire nell'Hashtable
     */
    @Override
    public void addInfo(String nome, Object valore) {

        InfoCollection info = (InfoCollection) _info.get();
        info.add(nome, valore);
    }

    /**
     * Istanzia la classe InfoCollection e gli passa il nome e il valore del campo da aggiungere nell'Hashtable
     * 
     * @param infoNames e' il nome della stringa da inserire nell'Hashtable
     * @param info e' il valore della stringa da inserire nell'Hashtable
     */
    @Override
    public void addInfo(Hashtable userInfo) {

        InfoCollection info = (InfoCollection) _info.get();
        info.add(userInfo);

    }

    /**
     * Istanzia la classe InfoCollection e gli passa la chiave da togliere dall'Hashtable
     * 
     * @param nome e' il nome della chiave dall'Hashtable
     */
    @Override
    public void removeInfo(String nome) {

        InfoCollection info = (InfoCollection) _info.get();
        info.remove(nome);

    }

    /**
     * Istanzia la classe InfoCollection e gli passa la chiave da togliere dall'Hashtable
     * 
     * @param infoNames e' il nome della chiave dall'Hashtable
     */
    @Override
    public void removeInfo(String[] infoNames) {

        InfoCollection info = (InfoCollection) _info.get();
        info.remove(infoNames);

    }

    /**
     * Istanzia la classe InfoCollection e pulisce l'Hashtable
     */
    @Override
    public void resetInfo() {

        InfoCollection info = (InfoCollection) _info.get();
        info.resetInfo();

    }

    /**
     * Inizia il log di un errore. Vengono riportate nel file la data e la print della stack trace
     * 
     * @param err e' l'eccezione che costituisce l'errore.
     */
    @Override
    public void logError(Exception err) {

        _errorLogger.WriteLog(err, getInfo());
    }

    /**
     * Inizia il log di un errore. Vengono riportate nel file la data e la print della stack trace
     * 
     * @param err e' l'errore.
     */
    @Override
    public void logError(Throwable err) {

        _errorLogger.WriteLog(err, getInfo());
    }

    /**
     * Aggiunge al file di log degli errori una stringa passata.
     * 
     * @param what e' la stringa da aggiungere al log
     */
    @Override
    public void logError(String what) {

        _errorLogger.WriteLog(what, getInfo());
    }

    @Override
    public void logError(String type, String what) {

        _errorLogger.WriteLog(type, what, getInfo());
    }

    /**
     * Scrive sul syslog una stringa passata.
     * 
     * @param what e' la stringa da aggiungere al syslog
     */
    @Override
    public void logSyslog(String what) {

        if (_syslogLogger != null) {
            _syslogLogger.WriteLog(what, getInfo());
        }
    }

    @Override
    public void logSyslog(String type, String what) {

        if (_syslogLogger != null) {
            _syslogLogger.WriteLog(type, what, getInfo());
        }
    }

    /**
     * Scrive sul log applicativo una stringa passata.
     * 
     * @param what e' la stringa da aggiungere al log applicativo
     */
    @Override
    public void logApplication(String type, String what) {

        if (_applicationLogger != null) {
            _applicationLogger.WriteLog(type, what, getInfo());
        }
    }

    @Override
    public void logApplication(String what) {

        if (_applicationLogger != null) {
            _applicationLogger.WriteLog(what, getInfo());
        }
    }

    /**
     * Aggiunge al file di log del levello indicato di debug una stringa passata.
     * 
     * @param what e' la stringa da aggiungere al log
     */
    public void logDebug(String what) {

        _debugLogger.WriteLog(what, getInfo());
    }

    /**
     * Aggiunge al file di log del levello indicato di debug una stringa passata.
     * 
     * @param what e' la stringa da aggiungere al log
     */
    @Override
    public void dumpInfo(String tipo) {

        if (tipo.equals("APP")) {
            if (_applicationLogger == null) return;

            _applicationLogger.dumpInfo(getInfo());
        } else if (tipo.equals("ERR")) {
            _errorLogger.dumpInfo(getInfo());
        } else if (tipo.equals("SYS")) {
            if (_syslogLogger == null) return;

            _syslogLogger.dumpInfo(getInfo());
        } else if (tipo.equals("DEB")) {
            _debugLogger.dumpInfo(getInfo());
        }

    }

    /**
     * Aggiunge al file di log del levello indicato di debug una stringa passata.
     * 
     * @param what e' la stringa da aggiungere al log
     */
    @Override
    public void dumpInfo() {

        if (_applicationLogger != null) {
            _applicationLogger.dumpInfo(getInfo());
        }
    }

    /**
     * Aggiunge al file di log del levello indicato di debug una stringa passata.
     * 
     * @param what e' la stringa da aggiungere al log
     */
    @Override
    public Hashtable getInfo() {

        InfoCollection info = (InfoCollection) _info.get();
        Hashtable fields = info.getInfo();
        return fields;

    }

    /**
     * E' il metodo di accesso al singleton.Ritorna il logger della configurazione di default
     */
    public static Logger_itf GetInstance() {

        // questo e' un "Double checked lock" design pattern
        if (_DefaultInstance == null) {
            synchronized (Logger.class) {
                if (_DefaultInstance == null) {
                    String classe = ErrorConfig.GetInstance().getProperty("LoggerClass");

                    try {
                        _DefaultInstance = (Logger_itf) Class.forName(classe).newInstance();
                    } catch (ClassNotFoundException e) {
                        System.err.println("LoggerClass class not found" + classe);
                        System.exit(-1);
                    } catch (IllegalAccessException e) {
                        System.err.println("LoggerClass class access error" + classe);
                        System.exit(-1);
                    } catch (InstantiationException e) {
                        System.err.println("LoggerClass class instantiation" + classe);
                        System.exit(-1);
                    }
                }
            }
        }
        return _DefaultInstance;
    }

    /**
     * E' il metodo di accesso al singleton. Ritorna il logger della configurazione indicata dal parametro confName
     * 
     * @param confName java.lang.String e' il nome della configurazione da usare
     */
    public static Logger_itf GetInstance(String confName) {

        if (confName.equals("")) {
            return GetInstance();
        }
        // questo e' un "Double checked lock" design pattern
        Logger_itf result = (Logger_itf) _Instances.get(confName);
        if (result == null) {
            synchronized (Logger.class) {
                result = (Logger_itf) _Instances.get(confName);
                if (result == null) {
                    String classe = ErrorConfig.GetInstance(confName).getProperty("LoggerClass");

                    try {
                        Class tempClass = Class.forName(classe);
                        Constructor procBaseConstr = tempClass.getConstructor(new Class[] { String.class });
                        result = (Logger_itf) procBaseConstr.newInstance(new Object[] { confName });

                        _Instances.put(confName, result);
                    } catch (ClassNotFoundException e) {
                        System.err.println("LoggerClass class not found" + classe);
                        System.exit(-1);
                    } catch (IllegalAccessException e) {
                        System.err.println("LoggerClass class access error" + classe);
                        System.exit(-1);
                    } catch (InstantiationException e) {
                        System.err.println("LoggerClass class instantiation" + classe);
                        System.exit(-1);
                    } catch (Throwable t) {
                        System.err.println("LoggerClass class instantiation throwable" + classe);
                        System.exit(-1);
                    }
                }
            }
        }
        return result;

    }

    @Override
    public void log0(String what) {

        logDebug(what);
    }

    @Override
    public void log1(String what) {

    }

    @Override
    public void log2(String what) {

    }

    @Override
    public void log3(String what) {

    }

    @Override
    public void flush() {

        _debugLogger.flush();
        _errorLogger.flush();

        if (_syslogLogger != null) _syslogLogger.flush();
        if (_applicationLogger != null) _applicationLogger.flush();

    }

    /**
     * @param logLevel il logLevel da impostare.
     */
    protected void setLogLevel(int logLevel) {

        _logLevel = logLevel;
    }

    /**
     * Questo metodo ritorna li livello di log attuale. Di default e' zero
     *
     * @return int il livello di log
     *
     * @see net.project.errors.Logger_itf#getLogLevel()
     */
    @Override
    public int getLogLevel() {

        return _logLevel;
    }

}
