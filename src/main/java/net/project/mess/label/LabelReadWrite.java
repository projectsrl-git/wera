
package net.project.mess.label;

/*
 LabelReadWrite.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 25/09/2001

 Autore: Assunta C.

 Note:

 Modifiche:

 */

import java.util.Hashtable;
import java.util.Vector;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.mess.MsgWriter_itf;

/**
 * Classe per lettura e scrittura di un messaggio LABEL. Il messaggio ha il seguente formato: 4bytes Type messaggio
 * 4bytes Label1-Nome Campo, 4bytes Lunghezza Label1 in ascii, nbytes Label1-Dato ....... 4bytes Labeln-Nome Campo,
 * 4bytes Lunghezza Labeln in ascii, nbytes Labeln-Dato
 */

public class LabelReadWrite implements MsgWriter_itf {

    private Hashtable _labels;
    private Vector    _names;
    private String    _typeMessaggio;

    /**
     * Costruttore presente solo per permettere la serializzazione/deserializzazione di oggetti di questa classe. <b>Non
     * deve essere utilizzato</b>
     */
    public LabelReadWrite() {

        super();
    }

    /**
     * Costruttore.
     * 
     * @param tipo String tipo del messaggio.
     */

    public LabelReadWrite(String tipo) throws AppCrash {

        ErrDetector.GetInstance().invariant(!(tipo.length() > 4),
                "LabelReadWrite - Lunghezza parametro tipo Messaggio non valido. tipo=" + tipo);
        _typeMessaggio = tipo;
        _labels = new Hashtable();
        _names = new Vector();
    }

    /**
     * Ritorna il messaggio.
     * 
     * @return byte[]
     */
    @Override
    public byte[] getMessage() throws AppCrash {

        byte[] message = null;
        String zeri = "0000";
        String blank = "    ";
        try {
            /*
             * Il messaggio deve rispettare il seguente formato: 4bytes Type messaggio 4bytes Label1-Nome Campo, 4bytes
             * Lunghezza Label1 in ascii, nbytes Label1-Dato ....... 4bytes Labeln-Nome Campo, 4bytes Lunghezza Labeln
             * in ascii, nbytes Labeln-Dato
             */
            int dst = 0;

            /*
             * Calocolo lunghezza array messaggio len tipo 4 +(len campo 4 + lendato 4 + campo n)*nlabel
             */
            int lt = 4;
            String[] keys = new String[_labels.size()];
            String[] labelDato = new String[_labels.size()];

            /*
             * Enumeration k = _labels.keys(); int i = 0; while (k.hasMoreElements()) { lt += 8; // nome campo + len
             * dato campo keys[i] = (String) (k.nextElement()); labelDato[i] = (String) _labels.get(keys[i]); lt +=
             * labelDato[i].length(); i += 1; }
             */

            for (int i = 0; i < _names.size(); i++) {
                lt += 8; // nome campo + len dato campo
                keys[i] = (String) _names.elementAt(i);
                labelDato[i] = (String) _labels.get(keys[i]);
                lt += labelDato[i].length();
            }

            message = new byte[lt];
            // Tipo messaggio
            System.arraycopy(_typeMessaggio.getBytes(), 0, message, dst, 4);
            dst += 4;
            for (int i = 0; i < keys.length; i++) {
                // Nome campo n-esimo
                String key = (keys[i]);
                System.arraycopy(key.concat(blank.substring(0, 4 - key.length())).getBytes(), 0, message, dst, 4);
                dst += 4;
                // Lunghezza dato del campo n-esimo
                int n = labelDato[i].length();
                String len = Integer.toString(n);
                System.arraycopy((zeri.substring(0, 4 - len.length())).concat(len).getBytes(), 0, message, dst, 4);
                dst += 4;
                // Dato del campo n-esimo
                System.arraycopy(labelDato[i].getBytes(), 0, message, dst, n);
                dst += n;
            }

        } catch (Throwable ex) {
            AppCrash e = new AppCrash(ex);
            e.logContext("LabelReadWrite", "metodo getMessage() - messaggio = " + new String(message));
            throw e;
        }
        return message;
    }

    /**
     * Consente l'impostazione di un campo col corrispondente valore.
     * 
     * @param nome nome del campo.
     * @param valore valore del campo.
     */
    @Override
    public void setField(String nome, String valore) throws AppCrash {

        ErrDetector.GetInstance().invariant(!(nome.length() > 4),
                "LabelReadWrite - Lunghezza nome Campo non valido. nome=" + nome);
        _labels.put(nome, valore);
        if (!_names.contains(nome)) {
            _names.addElement(nome);
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

        return _typeMessaggio;
    }

}
