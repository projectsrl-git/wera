
package net.project.mess.iso8583;

/*
 ISO8583ReadWrite.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 21/05/1999

 Autore: Rosella V.

 Note:

 Modifiche:
 25/11/99	TL17 ottimizzazione uso stringhe. Rosella V.

 */

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.mess.MsgWriter_itf;

/**
 * Classe per la costruzione di un messaggio ISO8583
 */
public class ISO8583ReadWrite implements MsgWriter_itf, FormatLengthType_itf {

    private String[]          _messagesArray;
    private ISO8583Dictionary _fieldsSpecification;
    private NewBitMapISO8583  _bitMap;
    private byte[]            _type;

    /**
     * Costruttore presente solo per permettere la serializzazione/deserializzazione di oggetti di questa classe. <b>Non
     * deve essere utilizzato</b>
     */
    public ISO8583ReadWrite() {

        super();
    }

    /**
     * Costruttore.
     * 
     * @param iso8583Init classe di gestione descrittori e bitMap.
     */
    public ISO8583ReadWrite(String type) throws AppCrash {

        String tipoDic = "";

        // I messaggi ISO8583 versione 93 iniziano tutti per "1": 1100-1110 1200-1210 1420-1430
        // invece i messaggi ISO87 iniziano con "0": 0100-0110 0420-0430
        if (type.substring(0, 1).equals("1")) {
            tipoDic = "93";
        }
        _fieldsSpecification = ISO8583Dictionary.GetInstance(tipoDic);

        _type = type.getBytes();
        _messagesArray = new String[129]; // 128 (1-128) è il massimo numero di campi presenti nel messaggio
        for (int i = 0; i < _messagesArray.length; i++) {
            _messagesArray[i] = null;
        }
        _bitMap = new NewBitMapISO8583();

    }

    /**
     * Ritorna il campo di indice key del messaggio.
     * 
     * @param keyName indice del campo.
     * @return String
     */
    @Override
    public String getField(String keyName) throws AppCrash {

        String field = null;
        try {
            int key = Integer.parseInt(keyName);
            ErrDetector.GetInstance().param((key >= 2) && (key <= 128));
            field = _messagesArray[key];
        } catch (AppCrash e) {
            Logger.GetInstance().logError(toString());
            throw e;
        } catch (NumberFormatException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("ISO8583ReadWrite", "errore lettura campo: " + keyName);
            throw err;
        }
        return field;
    }

    /**
     * Ritorna il messaggio creato.
     * 
     * @return byte[]
     */
    @Override
    public byte[] getMessage() {

        byte[] tempMessage = null;
        byte[] message = null;
        int offset = 0;
        byte[] temp = null;
        StringBuffer stringMessage = new StringBuffer();

        // inizio creazione messaggio
        try {
            if (_bitMap.exist(1)) {
                temp = new byte[20]; // 4+8+8
            } else {
                temp = new byte[12]; // 4+8
            }
            System.arraycopy(_type, 0, temp, offset, _type.length);
            offset += _type.length;
            if (_bitMap.exist(1)) {
                System.arraycopy(_bitMap.getBytes(), 0, temp, offset, _bitMap.getBytes().length);
                offset += _bitMap.getBytes().length;
            } else {
                System.arraycopy(_bitMap.getBytes(), 0, temp, offset, (_bitMap.getBytes().length) / 2);
                offset += (_bitMap.getBytes().length) / 2;
            }
            // inizio copia di tutti i campi
            for (int i = 1; i < _messagesArray.length; i++) {
                if (_messagesArray[i] != null) {
                    stringMessage.append(_fieldsSpecification.formatField(i, _messagesArray[i]));
                }
            }
            // fine copia di tutti i campi
            tempMessage = stringMessage.toString().getBytes();
            message = new byte[temp.length + tempMessage.length];
            for (int i = 0; i < temp.length; i++) {
                message[i] = temp[i];
            }
            for (int i = temp.length; i < message.length; i++) {
                message[i] = tempMessage[i - temp.length];
            }
        } catch (AppCrash e) {
            Logger.GetInstance().logError(toString());
        }
        // fine creazione messaggio
        return message;
    }

    /**
     * Ritorna il tipo di messaggio.
     * 
     * @return String
     */
    @Override
    public String getType() {

        return new String(_type);
    }

    /**
     * Imposta un campo.
     * 
     * @param field campo.
     * @param key indice del campo.
     */
    @Override
    public void setField(String keyName, String field) throws AppCrash {

        try {
            int key = Integer.parseInt(keyName);
            ErrDetector.GetInstance().param((key >= 2) && (key <= 128)); // controllo sul range per _messagesArray
            ErrDetector.GetInstance().param(field.length() <= _fieldsSpecification.getMaxLength(key)); // controllo
                                                                                                       // sulla massima
                                                                                                       // lunghezza del
                                                                                                       // campo
            _messagesArray[key] = field; // assegnamento del campo di indice key
            _bitMap.setBit(key, true);
            if (key > 64) {
                _bitMap.setBit(1, true);
            }
        } catch (AppCrash e) {
            Logger.GetInstance().logError(toString());
            throw e;
        } catch (NumberFormatException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("ISO8583ReadWrite", "errore scrittura campo: " + keyName);
            throw err;
        }
    }
}
