/*
  MsgFactory_base.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 14/05/1999

  Autore: Simone Z.

  Note:

  Modifiche:	vedi ClearCase history

 */

package net.project.mess;

import java.lang.reflect.Constructor;
import java.util.Hashtable;

import net.project.errors.AppCrash;
import net.project.misc.Config;

/**
 * MsgFactory_base e' la classe astratta che serve come "aggancio" per i clienti del package net.project.mess per la
 * creazione di oggetti di tipo MsgReader_itf e MsgWriter_itf Questa classe e' un singleton che istanzia una factory
 * concreta leggendo la proprieta' di configurazione <b>MsgFactoryClass</b>
 *
 * @author Simone Zorzetti
 */
public abstract class MsgFactory_base {

    private static Hashtable       _Factories = new Hashtable();
    private static MsgFactory_base _Instance  = null;

    /**
     * Questo metodo ritorna l'istanza della classe MsgFactory_base presente nel sistema per la configurazione di
     * default. Se un tale oggetto non esiste ancora ne crea uno. L'oggetto creato appartiene alla classe concreta letta
     * dalla proprieta' di configurazione <b>MsgFactoryClass</b>
     *
     * @return net.project.mess.MsgFactory_base L'istanza della classe MsgFactory_base
     * @exception net.project.errors.AppCrash Nel caso vi fossere problemi nell'istanziazione della classe concreta.
     */
    public final static MsgFactory_base GetInstance() throws AppCrash {

        // questo e' un "Double checked lock" design pattern
        if (_Instance == null) {
            synchronized (MsgFactory_base.class) {
                if (_Instance == null) {
                    String classe = Config.GetInstance().getProperty("MsgFactoryClass");
                    try {
                        _Instance = (MsgFactory_base) Class.forName(classe).newInstance();
                    } catch (ClassNotFoundException e) {
                        AppCrash err = new AppCrash(e);
                        err.logContext("MsgFactory", " class not found" + classe);
                    } catch (IllegalAccessException e) {
                        AppCrash err = new AppCrash(e);
                        err.logContext("MsgFactory", " error " + classe);
                    } catch (InstantiationException e) {
                        AppCrash err = new AppCrash(e);
                        err.logContext("MsgFactory", " error" + classe);
                    }
                }
            }
        }
        return _Instance;
    }

    /**
     * Questo metodo ritorna l'istanza della classe MsgFactory_base presente nel sistema per. la configurazione indicata
     * dal parametro passato. Se un tale oggetto non esiste ancora ne crea uno. L'oggetto creato appartiene alla classe
     * concreta letta dalla proprieta' di configurazione <b>MsgFactoryClass</b>
     *
     * @param confName java.lang.String nome della configurazione da usare
     * @return net.project.mess.MsgFactory_base L'istanza della classe MsgFactory_base
     * @exception net.project.errors.AppCrash Nel caso vi fossere problemi nell'istanziazione della classe concreta.
     */
    public final static MsgFactory_base GetInstance(String confName) throws AppCrash {

        if (confName.equals("")) return GetInstance();
        else {
            MsgFactory_base result = (MsgFactory_base) _Factories.get(confName);
            // questo e' un "Double checked lock" design pattern
            if (result == null) {
                synchronized (MsgFactory_base.class) {
                    result = (MsgFactory_base) _Factories.get(confName);
                    if (result == null) {
                        String classe = Config.GetInstance(confName).getProperty("MsgFactoryClass");
                        try {
                            result = (MsgFactory_base) Class.forName(classe).newInstance();
                            _Factories.put(confName, result);
                        } catch (ClassNotFoundException e) {
                            AppCrash err = new AppCrash(e);
                            err.logContext("MsgFactory", " class not found" + classe);
                        } catch (IllegalAccessException e) {
                            AppCrash err = new AppCrash(e);
                            err.logContext("MsgFactory", " error " + classe);
                        } catch (InstantiationException e) {
                            AppCrash err = new AppCrash(e);
                            err.logContext("MsgFactory", " error" + classe);
                        }
                    }
                }
            }
            return result;
        }
    }

    /**
     * Questo metodo serve per la creazioni di oggetti della classe MsgWriter_itf. L'oggetto creato dipende ovviamente
     * dalla factory concreta utilizzata dal sistema e dal parametro che viene passato
     *
     * @param java.lang.String type Tipo di messaggio da creare
     * @return net.project.mess.MsgWriter_itf il writer del messaggio
     * @exception net.project.errors.AppCrash
     */
    public abstract MsgWriter_itf MakeMsgWriter(String type) throws AppCrash;

    /**
     * Questo metodo serve per la modifica di oggetti della classe MsgWriter_itf. L'oggetto da modificare dipende
     * ovviamente dalla factory concreta utilizzata dal sistema e dai parametri che vengono passati
     *
     * @param byte[] message byte array contenente il messaggio che si vuole modificare con il writer
     * @return net.project.mess.MsgWriter_itf il writer del messaggio
     * @exception net.project.errors.AppCrash
     */
    public abstract MsgWriter_itf MakeMsgWriter(byte[] message) throws AppCrash;

    /**
     * Questo metodo serve per la creazioni di oggetti della classe MsgReader_itf. L'oggetto creato dipende ovviamente
     * dalla factory concreta utilizzata dal sistema e dal contenuto del byte array passato: la factory ispeziona il
     * byte array e ricava da esso il tipo di messaggio da utilizzare
     *
     * @param byte[] message byte array contenente il messaggio per il quale costruire il reader
     * @return net.project.mess.MsgReader_itf il reader del messaggio
     * @exception net.project.errors.AppCrash
     */
    public abstract MsgReader_itf MakeMsgReader(byte[] message) throws AppCrash;

    /**
     * Questo metodo protetto crea una istanza di MsgReader_itf dinamicamente a partire da una stringa con il nome della
     * classe da usare.
     * 
     * @param className
     * @param message
     * @return
     * @throws AppCrash eccezione
     */
    protected MsgReader_itf createReaderInstance(String className, byte[] message) throws AppCrash {

        try {
            Class classe = Class.forName(className);
            Constructor constr = classe.getConstructor(new Class[] { byte[].class });
            return (MsgReader_itf) constr.newInstance(new Object[] { message });
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            throw ac;
        }

    }

    /**
     * Questo metodo protetto crea una istanza di MsgWriter_itf dinamicamente a partire da una stringa con il nome della
     * classe da usare.
     * 
     * @param className
     * @param type
     * @return
     * @throws AppCrash
     */
    protected MsgWriter_itf createWriterInstance(String className, String type) throws AppCrash {

        try {
            Class classe = Class.forName(className);
            Constructor constr = classe.getConstructor(new Class[] { String.class });
            return (MsgWriter_itf) constr.newInstance(new Object[] { type });
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            throw ac;
        }

    }
}
