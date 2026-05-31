/*
 MsgProcessorFactory.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 01/06/1999

  Autore: Rosella V.

  Note:

  Modifiche:

 */

package net.project.mess;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;

/**
 * La classe astratta MsgProcessorFactory e' la factory per gli oggetti di elaborazione dei messaggi; essa ospita un
 * solo metodo statico che serve appunto allo scopo sopra indicato.
 *
 * @author Rosella Vergani
 */
public abstract class MsgProcessorFactory {

    /**
     * Questo e' il metodo che serve per la creazione degli oggetti di tipo MsgProcessor_itf. Esso ricava il tipo di
     * messaggio ed in base a questo istanzia la classe concreta opportuna. Per ricavare il nome della classe viene
     * letta la proprieta' di conficurazione <B>Mess.processor.$tipo$</b> dove $tipo$ e' il valore el tipo di messaggio.
     * Ad esempio per il messaggio ATDAT RCCAO il processor istanziato sara' un oggetto della classe letta dalla
     * proprieta' Mess.processor.RCCAO
     *
     * @param net.project.mess.MsgReader_itf source Lettore del messaggio da elaborare
     * @return net.project.mess.MsgProcessor_itf l'oggetto processor del messaggio
     * @exception net.project.errors.AppCrash Nel caso vi fossere problemi nell'istanziazione della classe per il
     *                processing.
     */
    public static MsgProcessor_itf MakeMsgProcessor(MsgReader_itf source) throws AppCrash {

        String tipo = null;
        String classe = null;

        MsgProcessor_itf procBase = null;

        // Leggo dal file di configurazione quale classe costituisce il processor
        // del messaggio ATDAT ricevuto
        try {
            tipo = source.getType();
            classe = Config.GetInstance().getProperty("Mess.processor." + tipo);
            ErrDetector.GetInstance().invariant(classe != null);
        } catch (AppCrash err) {
            err.logContext("MsgProcessorFactory", "Tipo : " + tipo + " Classe: " + classe);
            throw err;
        }

        // Istanzio un oggetto della classe letta
        try {
            // Recupero il costruttore della classe letta che riceve in ingresso un interprete MsgReader_itf
            // Es. RCCAO(MsgReader_itf interp)
            Class tempClasse = Class.forName(classe);
            Constructor procBaseConstr = tempClasse.getConstructor(new Class[] { MsgReader_itf.class });

            // Istanzio la classe tramite il costruttore ottenuto
            procBase = (MsgProcessor_itf) procBaseConstr.newInstance(new Object[] { source });

        } catch (ClassNotFoundException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("MsgProcessorFactory", "Tipo : " + tipo + " Classe: " + classe);
        } catch (IllegalAccessException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("MsgProcessorFactory", "Tipo : " + tipo + " Classe: " + classe);
        } catch (InstantiationException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("MsgProcessorFactory", "Tipo : " + tipo + " Classe: " + classe);
        } catch (NoSuchMethodException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("MsgProcessorFactory", "Tipo : " + tipo + " Classe: " + classe);
        } catch (InvocationTargetException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("MsgProcessorFactory", "Tipo : " + tipo + " Classe: " + classe);
        }

        return procBase;
    }
}
