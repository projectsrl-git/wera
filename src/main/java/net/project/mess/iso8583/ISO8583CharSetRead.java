
package net.project.mess.iso8583;

/*
 ISO8583CharSetRead.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 19/06/2001

 Autore: Rosella V.

 Note:

 Modifiche:

 */

import java.math.BigInteger;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.mess.MsgReader_itf;
import net.project.misc.Config;
import net.project.misc.Converter;

/**
 * Classe per la lettura e la codifica di un messaggio ISO8583. La codifica da applicare può essere letta da file di
 * configurazione (ISO8583CharSetRead.SourceCharSet) o può essere esplicitamente indicata nel costruttore. Tale
 * proprietà deve essere valorizzata con la costante relativa alla codifica voluta. Possibili valori sono ad esempio:
 * 8859_1,Cp1046.
 */

public class ISO8583CharSetRead implements MsgReader_itf, FormatLengthType_itf {

    // costante per la lettura della conversione da applicare
    public static final String ENCODING_TYPE         = "ISO8583CharSetRead.SourceCharSet";

    // codifica di default
    public static final String DEFAULT_ENCODING_TYPE = "8859_1";
    public static final String NONE_ENCODING         = "NONE";

    private byte[]             _message;
    private MsgReader_itf      _reader;
    private String             _encoding;

    /**
     * Costruttore presente solo per permettere la serializzazione/deserializzazione di oggetti di questa classe. <b>Non
     * deve essere utilizzato</b>
     */
    public ISO8583CharSetRead() {

        super();
    }

    /**
     * Costruttore. Effettua la codifica del messaggio in base al parametro letto da file. di configurazione.
     * 
     * @parammessage byte[] messaggio.
     */
    public ISO8583CharSetRead(byte[] message) throws AppCrash {

        String isoType = null;
        String isoMessage = null;
        int lenBitmap = 8;
        try {
            ErrDetector.GetInstance().param(message != null);
            _encoding = Config.GetInstance().getProperty(ENCODING_TYPE, DEFAULT_ENCODING_TYPE);

            if (_encoding.equals(NONE_ENCODING)) {
                _message = message;
            } else {
                byte[] type = new byte[4];
                // controllo la lunghezza della bitmap
                byte[] testPrimaryBitmap = new byte[1];
                System.arraycopy(message, 4, testPrimaryBitmap, 0, 1); // copio il primo byte della primary bitmap
                // controllo l'esistenza della secondary bitmap
                if (new BigInteger(testPrimaryBitmap).testBit(7)) {
                    // esiste la secondary bitmap
                    lenBitmap = 16;
                }
                byte[] bitmap = new byte[lenBitmap];
                byte[] mes = new byte[message.length - (lenBitmap + 4)];
                _message = new byte[message.length];
                System.arraycopy(message, 0, type, 0, 4);
                System.arraycopy(message, 4, bitmap, 0, lenBitmap);
                System.arraycopy(message, lenBitmap + 4, mes, 0, mes.length);
                type = Converter.convertIn(type, _encoding);
                isoType = new String(type);
                mes = Converter.convertIn(mes, _encoding);
                isoMessage = new String(mes);
                System.arraycopy(type, 0, _message, 0, isoType.length());
                System.arraycopy(bitmap, 0, _message, isoType.length(), bitmap.length);
                System.arraycopy(mes, 0, _message, bitmap.length + isoType.length(), mes.length);

            } // istanzio il reader
            _reader = new ISO8583Read(_message);
        } catch (AppCrash e) {
            e.logContext("ISO8583CharSetRead", "costruttore ISO8583CharSetRead(byte[] message) - encoding = "
                    + _encoding + ", tipo = " + isoType + ", messaggio = " + isoMessage);
            throw e;
        }
    }

    /**
     * Ritorna il campo di indice key del messaggio.
     * 
     * @param keyName indice del campo.
     * @return String
     */
    @Override
    public String getField(String keyName) throws AppCrash {

        return _reader.getField(keyName.trim());
    }

    /**
     * Ritorna il tipo di messaggio.
     * 
     * @return String
     */
    @Override
    public String getType() {

        return _reader.getType();
    }
}