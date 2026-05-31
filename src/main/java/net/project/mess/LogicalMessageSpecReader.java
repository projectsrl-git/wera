/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.mess;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.misc.FileParser;

/**
 * Questa classe rappresenta la specifica logica di un messaggio. Per specifica si intende l'insieme dei campi che
 * formano il messaggio divisi in obbligatori e facoltativi. I nomi dei campi sono presi da un vocabolario. Tutte le
 * caratteristiche dei campi sono contenute nel vocabolario. Le specifiche logiche dei messaggi sono contenute in file
 * di testo che sono cosi' composti: colonna 1: nome del campo
 * <P>
 * colonna 2: O/F obbligatorio-facoltativo
 * <P>
 * 
 */
public class LogicalMessageSpecReader {

    private Hashtable _mandatoryFields   = new Hashtable();
    private Hashtable _facoltativeFields = new Hashtable();
    private String    _fileMessageFields;
    private String    _messageType;

    /**
     * Costruisce una specifica a partire dal nome (tipo) del messaggio. Legge la proprieta'
     * <code>verifier.messaggeDir</code> per recuperare la directory dove si trovano i file di specifica. Questa
     * proprieta' deve esistere e non essere vuota. Il nome del file della specifica per il messaggio viene letto dalla
     * proprieta' <code>verifier.(messageType).messagge</code>
     *
     * @param messageType String tipo messaggio
     *
     * @throws AppCrash
     */
    public LogicalMessageSpecReader(String messageType) throws AppCrash {

        String dir_filename = Config.GetInstance().getProperty("verifier.messageDir");
        ErrDetector.GetInstance().param(dir_filename);

        // Path relativo cerco di aggiungere in testa la root della applicazione
        dir_filename = Config.GetInstance().makeAbsolutePath(dir_filename);

        String filename = Config.GetInstance().getProperty("verifier." + messageType + ".message");
        ErrDetector.GetInstance().param(filename);

        _fileMessageFields = dir_filename + filename;

        // Leggere file
        readFile();

        _messageType = messageType;
    }

    /**
     * Ritorna il tipo del messaggio
     *
     * @return String _messageType
     */
    public String getMessageType() {

        return _messageType;
    }

    /**
     * Ritorna un Iterator (versione moderna di Enumeration) contenente i campi obbligatori
     *
     * @return Iterator campi obbligatori
     */
    public Iterator getMandatoryFields() {

        return _mandatoryFields.keySet().iterator();
    }

    /**
     * Ritorna un Iterator (versione moderna di Enumeration) contenente i campi facoltativi
     *
     * @return Iterator campi facoltativi
     */
    public Iterator getFacoltativeFields() {

        return _facoltativeFields.keySet().iterator();
    }

    /**
     * Lettura file per invividuare i field obbligatori/facoltativi
     *
     * @throws AppCrash
     */
    private void readFile() throws AppCrash {

        try {
            Vector parsering = FileParser.parse(_fileMessageFields);
            fillHashtables(parsering.elements()); // parsering è un vettore di vettori
        } catch (AppCrash ac) {
            ac.logContext("LogicalMessageSpec", "Errore lettura file per campi obbligatori/facoltativi: file "
                    + _fileMessageFields + " Tipo: " + _messageType);
            throw ac;
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("LogicalMessageSpec", "Errore lettura file per campi obbligatori/facoltativi: file "
                    + _fileMessageFields + " Tipo: " + _messageType);
            throw ac;
        }
    }

    /**
     * Riempie le hashtable con i dati ricavati dal file di specifica del messaggio
     *
     * @param components Enumeration L'enumeration dei componenti.
     *
     * @exception AppCrash
     */
    private void fillHashtables(Enumeration components) throws AppCrash {

        String field = "";
        String op = "";

        try {
            while (components.hasMoreElements()) {
                Vector el = (Vector) (components.nextElement());
                Enumeration element = el.elements();

                // Colonna 1 - Campo
                field = (String) element.nextElement();

                // Colonna 2 - Facoltativo/obbligatorio
                op = (String) element.nextElement();

                if (op.trim().equalsIgnoreCase("f")) {
                    _facoltativeFields.put(field, op);
                } else {
                    _mandatoryFields.put(field, op);
                }

                // Chiamata per permettere alle estensioni della classe di elaborare le informazioni
                // presenti nelle altre colonne della linea corrente
                processCols(element);
            }
        } catch (AppCrash ac) {
            throw ac;
        } catch (Exception ex) {
            AppCrash ap = new AppCrash(ex);
            ap.logContext("LogicalMessageSpec", "Errore in fillHashtable");
            throw ap;
        }
    }

    /**
     * Questo metodo serve alle sottoclassi per elaborare le colonne oltre la seconda del file di specifica del
     * messaggio
     *
     * @param element Enumeration con i primi due elementi gia' letti
     *
     * @throws AppCrash
     */
    protected void processCols(Enumeration element) throws AppCrash {

    }
}
