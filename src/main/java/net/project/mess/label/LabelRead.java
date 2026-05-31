
package net.project.mess.label;

/*
 LabelRead.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 25/09/2001

 Autore: Assunta C.

 Note:

 Modifiche:

 */

import java.util.Hashtable;

import net.project.errors.AppCrash;
import net.project.mess.MsgReader_itf;

/**
 * Classe per la lettura di un messaggio LABEL. Il messaggio ha il seguente formato: 4bytes Type messaggio 4bytes
 * Label1-Nome Campo, 4bytes Lunghezza Label1 in ascii, nbytes Label1-Dato ....... 4bytes Labeln-Nome Campo, 4bytes
 * Lunghezza Labeln in ascii, nbytes Labeln-Dato
 */
public class LabelRead implements MsgReader_itf {

    private Hashtable _labels;
    private String    _typeMessaggio;

    /**
     * Costruttore presente solo per permettere la serializzazione/deserializzazione di oggetti di questa classe. <b>Non
     * deve essere utilizzato</b>
     */
    public LabelRead() {

        super();
    }

    /**
     * Costruttore.
     * 
     * @param messaggio byte[] messaggio.
     */
    public LabelRead(byte[] messaggio) throws AppCrash {

        // Analisi dell'array di bytes
        try {
            byte[] type = new byte[4];
            byte[] campo = new byte[4];
            byte[] lenDato = new byte[4];
            byte[] dato = null;
            int len = 0;
            String campoLabel;
            String datoLabel;

            // Costruisco hashtable
            _labels = new Hashtable();

            // Type messaggio
            int index = 0;
            System.arraycopy(messaggio, index, type, 0, 4);
            _typeMessaggio = new String(type);
            index += 4;

            while (index != messaggio.length) {

                // nome label n-esima - Nome Campo
                System.arraycopy(messaggio, index, campo, 0, 4);
                if ((new String(campo)).trim().equals("")) {
                    return;
                }
                campoLabel = new String(campo);
                index += 4;

                // lunghezza dato campo label n-esima
                System.arraycopy(messaggio, index, lenDato, 0, 4);
                index += 4;
                len = Integer.parseInt(new String(lenDato));

                // Dato
                dato = new byte[len];
                System.arraycopy(messaggio, index, dato, 0, len);
                datoLabel = new String(dato);
                index += len;

                // Inserimento dati in hashtable
                _labels.put(campoLabel, datoLabel);
            }
        } catch (NumberFormatException ne) {
            AppCrash e = new AppCrash(ne);
            e.logContext("LabelRead", "NumberFormatException nel messaggio = " + new String(messaggio));
            throw e;
        } catch (Throwable ex) {
            AppCrash e = new AppCrash(ex);
            e.logContext("LabelRead", "messaggio = " + new String(messaggio));
            throw e;
        }
    }

    /**
     * Ritorna il campo corrispondente alla n-esima label.
     * 
     * @param nome java.lang.String nome del campo.
     * @return String
     */
    @Override
    public String getField(String nome) throws AppCrash {

        String value = (String) _labels.get(nome);
        return (value == null ? "" : value.trim());
    }

    /**
     * Ritorna il tipo del messaggio.
     * 
     * @return String
     */
    @Override
    public String getType() {

        return _typeMessaggio.trim();
    }

}
