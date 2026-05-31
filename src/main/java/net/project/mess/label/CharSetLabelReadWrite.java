/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.mess.label;

/*
 CharSetLabelReadWrite.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 28/09/2004

 Autore: Simona T.

 Note:

 Modifiche:

 */
import net.project.errors.AppCrash;
import net.project.mess.MsgWriter_itf;
import net.project.misc.Config;
import net.project.misc.Converter;

/**
 * La classe implementa un reader/writer per i messaggi label in dipendenza dalla codifica stabilita dalla proprietà
 * CharSetLabelReadWrite.SourceCharSet in configurazione.
 */
public class CharSetLabelReadWrite implements MsgWriter_itf {

    // costante per la lettura della conversione da applicare
    public static final String ENCODING_TYPE         = "CharSetLabelReadWrite.SourceCharSet";

    // codifica di default
    public static final String DEFAULT_ENCODING_TYPE = "8859_1";                             // ASCII
    public static final String NONE_ENCODING         = "NONE";
    private MsgWriter_itf      _writer;

    /**
     * Costruttore.
     *
     * @param tipo java.lang.String tipo del messaggio.
     */
    public CharSetLabelReadWrite(String tipo) throws AppCrash {

        _writer = new LabelReadWrite(tipo);
    }

    /**
     * Ritorna il messaggio. Legge da configurazione la codifica da applicare.
     *
     * @return byte[]
     */
    @Override
    public byte[] getMessage() throws AppCrash {

        byte[] message = null;
        byte[] tempMessage = null;
        String encoding = null;

        try {
            encoding = Config.GetInstance().getProperty(ENCODING_TYPE, DEFAULT_ENCODING_TYPE);

            tempMessage = _writer.getMessage();

            if (encoding.equals(NONE_ENCODING)) {
                message = tempMessage;
            } else {
                message = Converter.convertOut(tempMessage, encoding);
            }
        } catch (AppCrash e) {
            e.logContext("CharSetLabelReadWrite", "metodo getMessage() - encoding = " + encoding + ", messaggio = "
                    + new String(message));
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

        try {
            _writer.setField(nome, valore);
        } catch (AppCrash e) {
            e.logContext("CharSetLabelReadWrite", "metodo setField - nome = " + nome + ", valore = " + valore);
            throw e;
        }
    }

    /**
     * Ritorna il campo corrispondente a nome.
     *
     * @param nome java.lang.String nome del campo.
     *
     * @return String
     */
    @Override
    public String getField(String nome) throws AppCrash {

        try {
            return _writer.getField(nome);
        } catch (AppCrash e) {
            e.logContext("CharSetLabelReadWrite", "metodo getField - nome = " + nome);
            throw e;
        }
    }

    /**
     * Ritorna il tipo di messaggio.
     *
     * @return String
     */
    @Override
    public String getType() {

        return _writer.getType();
    }
}
