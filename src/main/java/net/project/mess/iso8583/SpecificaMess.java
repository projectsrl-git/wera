
package net.project.mess.iso8583;

/*
 SpecificaMess.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 28/05/1999

 Autore: Simone Z.

 Note:

 Modifiche:
 25/11/99	TL17 ottimizzazione uso stringhe. Rosella V.

 */

import java.util.Enumeration;
import java.util.Vector;

import net.project.errors.AppCrash;

/**
 * Questa classe rappresenta la specifica di un messaggio ISO8583. La specifica e' un insieme di specifiche di campi
 */
public class SpecificaMess {

    private Vector _campi = new Vector();
    private String _tipoMess;

    SpecificaMess(Vector vettoreSpecifiche) throws AppCrash {

        try {
            boolean primoElem = true;
            Vector elemento = null;
            SpecificaCampo spec = null;
            // Ogni elemento del Vector vettoreSpecifiche e' Vector che contiene
            // una riga del file di descrizione
            for (Enumeration enumer = vettoreSpecifiche.elements(); enumer.hasMoreElements();) {
                elemento = (Vector) enumer.nextElement();
                if (primoElem) {
                    _tipoMess = (String) elemento.elementAt(0);
                    primoElem = false;
                    continue;
                }
                // Creo un oggetto SpecificaCampo con dataElement prima colonna (0) e
                // dataSource seconda colonna (1) della riga corrente del file di descrizione
                spec = new SpecificaCampo((String) elemento.elementAt(0), (String) elemento.elementAt(1));
                // Aggiungo la specifica appena creata all'insieme dei campi del messaggio
                _campi.addElement(spec);
            }
        } catch (AppCrash err) {
            err.logContext("SpecificaMess", "Tipo messaggio: " + _tipoMess);
            throw err;
        }
    }

    /**
     * Restituisce un Enumeration tramite la quale si possono ricavare tutti gli oggetti SpecificaCampo che compongono
     * il messaggio ISO8583 del quale l'oggetto costituisce la specifica
     * 
     * @return java.util.Enumeration
     */
    public Enumeration elements() {

        Enumeration enumer = _campi.elements();
        return enumer;
    }

    /**
     * Questo metodo ritorna il tipo di messaggio
     *
     * @return String tipo messaggio
     */
    public String getTipoMess() {

        return _tipoMess;
    }
}
