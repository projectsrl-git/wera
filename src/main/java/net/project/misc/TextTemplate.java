/*
  TextTemplate.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 31/12/1999

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import net.project.errors.ErrDetector;
import net.project.errors.ParamCrash;

/**
 * Questa classe personalizza un testo sostituendo ad ogni occorrenza del tag #x# il valore presente nella Map
 * all'indice x e ad ogni occorrenza del tag '&' un a capo
 */
public class TextTemplate {

    private String _text = "";

    /**
     * Costruttore
     *
     * @param text java.lang.String testo da personalizzare
     * @exception net.project.errors.ParamCrash.
     */
    public TextTemplate(String text) throws ParamCrash {

        try {
            ErrDetector.GetInstance().param(text);
            _text = text;
        } catch (ParamCrash ex) {
            ex.logContext("TextTemplate", "Errore nel costruttore: parametri errati");
            throw ex;
        }
    }

    /**
     * Sostituisce ad ogni occorrenza del tag #x# il valore presente nella hashtable all'indice x. Sostituisce ad ogni
     * occorrenza del carattere '&' un a capo
     *
     * @param table java.util.Hashtable la hashtable che contiene i valori con cui personalizzare il testo
     * @exception net.project.errors.ParamCrash.
     * @return true se ogni tag è stato sostituito correttamente (esiste cioè una corrispondente entry nella hashtable),
     *         false altrimenti
     */
    public boolean replace(Map table) throws ParamCrash {

        String newText = "";
        String tableValue = "";
        boolean tuttoOk = true;
        boolean startConCancelletto = false;

        try {
            ErrDetector.GetInstance().param(table);

            _text = _text.replace('&', '\n');
            if (_text.startsWith("#")) {
                startConCancelletto = true;
                _text = " " + _text;
            }

            StringTokenizer st = new StringTokenizer(_text, "#");
            while (st.hasMoreTokens()) {
                newText = newText + st.nextToken();
                // recupera il valore nella hashtable
                if (st.hasMoreTokens()) {
                    tableValue = (String) table.get(st.nextToken());
                    if (tableValue == null) {
                        // se non ha trovato corrispondenza nella hashtable ...
                        tuttoOk = false;
                    } else {
                        newText = newText + tableValue;
                    }
                }
            }
            _text = newText;
            if (startConCancelletto && tuttoOk) {
                _text = _text.substring(1);
            }

            return tuttoOk;
        } catch (ParamCrash ex) {
            ex.logContext("TextTemplate", "Errore in replace: parametri errati");
            throw ex;
        }
    }

    /**
     * Ritorna il testo personalizzato
     *
     * @return java.lang.String
     */
    public String getText() {

        return _text;
    }
    
    public void setText(String text) {

        _text=text;
    }    

    public static void main(String args[]) {

        String IMPORTO_VALUTA = "IMP_VAL";
        Map table = new HashMap();
        table.put(IMPORTO_VALUTA, "£ 120000");
        table.put("AUTORIZ", "50");
        table.put("1", "inizio");
        table.put("2", "fine");

        String testo = " #1#& Il pagamento di #IMP_VAL#& accettato con autorizzazione N. #AUTORIZ# è stato contabilizzato&&#2#";
        try {
            TextTemplate prova = new TextTemplate(testo);

            prova.replace(table);
            System.out.println("testo sostituito = " + prova.getText());

        } catch (ParamCrash ex) {
        }
    }
}
