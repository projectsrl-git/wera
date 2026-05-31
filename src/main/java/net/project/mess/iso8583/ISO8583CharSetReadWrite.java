
package net.project.mess.iso8583;

/*
 ISO8583CharSetReadWrite.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 19/06/2001

 Autore: Rosella V.

 Note:

 Modifiche:

 */

import java.math.BigInteger;

import net.project.errors.AppCrash;
import net.project.mess.MsgWriter_itf;
import net.project.misc.Config;
import net.project.misc.Converter;

/**
 * Classe per la costruzione di un messaggio ISO8583
 */
public class ISO8583CharSetReadWrite implements MsgWriter_itf, FormatLengthType_itf {

    // costante per la lettura della conversione da applicare
    public static final String ENCODING_TYPE         = "ISO8583CharSetReadWrite.SourceCharSet";

    // codifica di default
    public static final String DEFAULT_ENCODING_TYPE = "8859_1";
    public static final String NONE_ENCODING         = "NONE";

    private MsgWriter_itf      _writer;

    /**
     * Costruttore presente solo per permettere la serializzazione/deserializzazione di oggetti di questa classe. <b>Non
     * deve essere utilizzato</b>
     */
    public ISO8583CharSetReadWrite() {

        super();
    }

    /**
     * Costruttore.
     * 
     * @param type java.lang.String tipo del messaggio.
     */
    public ISO8583CharSetReadWrite(String type) throws AppCrash {

        _writer = new ISO8583ReadWrite(type);
    }

    /**
     * Ritorna il campo di indice key del messaggio.
     * 
     * @param keyName indice del campo.
     * @return String
     */
    @Override
    public String getField(String keyName) throws AppCrash {

        try {
            return _writer.getField(keyName);
        } catch (AppCrash e) {
            e.logContext("ISO8583CharSetReadWrite", "metodo getField - nome = " + keyName);
            throw e;
        }
    }

    /**
     * Ritorna il messaggio creato. Legge da configurazione la codifica da applicare.
     * 
     * @return byte[]
     */
    @Override
    public byte[] getMessage() throws AppCrash {

        byte[] message = null;
        byte[] tempMessage = null;
        String encoding = null;
        String isoType = null;
        String isoMessage = null;
        int lenBitmap = 8;
        try {
            encoding = Config.GetInstance().getProperty(ENCODING_TYPE, DEFAULT_ENCODING_TYPE);
            tempMessage = _writer.getMessage();
            // conversione
            if (encoding.equals(NONE_ENCODING)) {
                message = tempMessage;
            } else {
                byte[] type = new byte[4];
                // controllo la lunghezza della bitmap
                byte[] testPrimaryBitmap = new byte[1];
                System.arraycopy(tempMessage, 4, testPrimaryBitmap, 0, 1); // copio il primo byte della primary bitmap
                // controllo l'esistenza della secondary bitmap
                if (new BigInteger(testPrimaryBitmap).testBit(7)) {
                    // esiste la secondary bitmap
                    lenBitmap = 16;
                }
                byte[] bitmap = new byte[lenBitmap];
                byte[] mes = new byte[tempMessage.length - (lenBitmap + 4)];
                message = new byte[tempMessage.length];
                System.arraycopy(tempMessage, 0, type, 0, 4);
                System.arraycopy(tempMessage, 4, bitmap, 0, lenBitmap);
                System.arraycopy(tempMessage, lenBitmap + 4, mes, 0, mes.length);
                type = Converter.convertOut(type, encoding);
                isoType = new String(type);
                byte[] buf = null;
                buf = Converter.convertOut(mes, encoding);
                isoMessage = new String(buf);
                System.arraycopy(type, 0, message, 0, isoType.length());
                System.arraycopy(bitmap, 0, message, isoType.length(), bitmap.length);
                System.arraycopy(buf, 0, message, bitmap.length + isoType.length(), mes.length);
            }
        } catch (AppCrash e) {
            e.logContext("Classe ISO8583CharSetReadWrite", " codifica applicata = " + encoding + ", tipo = " + isoType
                    + ", messaggio = " + isoMessage);
            throw e;
        }
        return message;
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

    /**
     * Imposta un campo.
     * 
     * @param field campo.
     * @param keyName nome del campo.
     */
    @Override
    public void setField(String keyName, String field) throws AppCrash {

        try {
            _writer.setField(keyName, field);
        } catch (AppCrash e) {
            e.logContext("ISO8583CharSetReadWrite", "metodo setField - nome = " + keyName + ", valore = " + field);
            throw e;
        }
    }

}