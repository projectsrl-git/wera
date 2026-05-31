
package net.project.mess.atdat;

/*
 ATDATURLCharSetReadWrite.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 18/06/2001

 Autore: Rosella V.

 Note:

 Modifiche:

 */

import net.project.errors.AppCrash;
import net.project.mess.MsgWriter_itf;
import net.project.misc.Config;
import net.project.misc.Converter;

/**
 * Classe per la lettura e la codifica di un messaggio ATDAT. La codifica da applicare può essere letta da file di
 * configurazione (ATDATURLCharSetReadWrite.SourceCharSet) o può essere esplicitamente indicata nel metodo get message.
 * Tale proprietà deve essere valorizzata con la costante relativa alla codifica voluta. Possibili valori sono ad
 * esempio: 8859_1,Cp1046.
 */

public class ATDATURLCharSetReadWrite implements MsgWriter_itf {

    // costante per la lettura della conversione da applicare
    public static final String ENCODING_TYPE         = "ATDATURLCharSetReadWrite.SourceCharSet";

    // codifica di default
    public static final String DEFAULT_ENCODING_TYPE = "8859_1";
    public static final String NONE_ENCODING         = "NONE";
    private MsgWriter_itf      _writer;

    /**
     * Costruttore presente solo per permettere la serializzazione/deserializzazione di oggetti di questa classe. <b>Non
     * deve essere utilizzato</b>
     */
    public ATDATURLCharSetReadWrite() {

    }

    /**
     * Costruttore.
     * 
     * @param tipo java.lang.String tipo del messaggio.
     */

    public ATDATURLCharSetReadWrite(String tipo) {

        _writer = new ATDATURLReadWrite(tipo);
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
            e.logContext("ATDATURLCharSetReadWrite", "metodo getMessage() - encoding = " + encoding + ", messaggio = "
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
            e.logContext("ATDATURLCharSetReadWrite", "metodo setField - nome = " + nome + ", valore = " + valore);
            throw e;
        }
    }

    @Override
    public String getField(String nome) throws AppCrash {

        try {
            return _writer.getField(nome);
        } catch (AppCrash e) {
            e.logContext("ATDATURLCharSetReadWrite", "metodo getField - nome = " + nome);
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