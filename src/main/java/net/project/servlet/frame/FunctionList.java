/*
  FunctionList.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 17/10/2000

  Autore: Anna L.

  Note:

  Modifiche:
  
 */

package net.project.servlet.frame;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.ParamCrash;
import net.project.misc.FileParser;

/**
 * Questa classe implementa la lista di tutte le funzioni.
 */

public class FunctionList {

    // Hashtable che fa da bind tra nome della funzione e nome della classe che
    // la implementa (contiene coppie del tipo: nome funzione - nome classe)
    private Hashtable _functionClassBind;

    // Hashtable che fa da bind tra nome della funzione e id della funzione
    // (contiene coppie del tipo: nome funzione - ID funzione)
    private Hashtable _functionIDBind;

    // Insieme delle functions main: sono quelle da disattivare per spegnere una applicazione
    private Set       _mainFunctions;

    /**
     * Costruttore dell'oggetto. L'elenco delle funzioni e delle rispettive classi viene letto dal file passato.
     *
     * @param file java.lang.String Il nome del file che contiene l'elenco delle funzioni e delle classi.
     * @exception net.project.errors.AppCrash.
     */
    public FunctionList(String file) throws AppCrash {

        Vector parsering = null;
        try {
            ErrDetector.GetInstance().param(file);
            parsering = FileParser.parse(file);
            fillHashtables(parsering.elements()); // parsering è un vettore di vettori
        } catch (ParamCrash pc) {
            pc.logContext("FunctionList", "Param error nel costruttore: " + toString());
            throw (pc);
        } catch (AppCrash ac) {
            ac.logContext("FunctionList", "Errore nel costruttore");
            throw (ac);
        }
    }

    /**
     * Ricava le coppie nome funzione-nome classe e nome funzione-id funzione e popola le relative hashtable. Popola
     * anche l'elenco delle main functions
     *
     * @param components java.util.Enumeration L'enumeration dei componenti.
     * @exception net.project.errors.AppCrash.
     */
    private void fillHashtables(Enumeration components) throws AppCrash {

        _functionClassBind = new Hashtable();
        _functionIDBind = new Hashtable();
        _mainFunctions = new HashSet();

        try {
            while (components.hasMoreElements()) {
                Enumeration element = ((Vector) (components.nextElement())).elements();
                String functionName = (String) element.nextElement();
                String className = (String) element.nextElement();
                String functionID = (String) element.nextElement();
                _functionClassBind.put(functionName, className);
                _functionIDBind.put(functionName, functionID);

                // Se esiste la 4 colonna verifico che sia una 'M' che indica che
                // la function e' una main function
                if (element.hasMoreElements()) {
                    String main = (String) element.nextElement();
                    if (main.equalsIgnoreCase("M")) {
                        _mainFunctions.add(functionName);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            AppCrash ap = new AppCrash(ex);
            ap.logContext("FunctionList", "Errore in fillHashtable");
            throw (ap);
        }
    }

    /**
     * Ricava la classe che implementa una funzione.
     *
     * @param functionName java.lang.String Nome della funzione.
     * @return java.lang.String. Il nome della classe che implementa la funzione.
     */
    public String findClass(String functionName) {

        return (String) _functionClassBind.get(functionName);
    }

    /**
     * Genera una lista con i nomi delle funzioni.
     *
     * @return java.util.Enumeration. La lista delle funzioni.
     */
    public Enumeration getFunctionList() {

        return _functionClassBind.keys();
    }

    /**
     * Ricava l'ID della funzione.
     *
     * @param functionName java.lang.String Il nome della funzione.
     * @return Ritorna l'id della funzione.
     */
    public String getFunctionID(String functionName) {

        return (String) _functionIDBind.get(functionName);
    }

    /**
     * Genera una lista con i nomi delle funzioni che sono main function.
     *
     * @return java.util.Iterator. La lista delle funzioni main.
     */
    public Iterator getMainFunctions() {

        return _mainFunctions.iterator();
    }

}
