
package net.project.mess.iso8583;

/*
 ISO8583Read.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 18/05/1999

 Autore: Rosella V.

 Note:

 Modifiche:

 */

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.mess.MsgReader_itf;

/**
 * Classe per la lettura di un messaggio ISO8583
 */

public class ISO8583Read implements MsgReader_itf, FormatLengthType_itf {

    private byte[]            _message;
    private ISO8583Dictionary _fieldsSpecification;
    private NewBitMapISO8583  _bitMap;

    /**
     * Costruttore presente solo per permettere la serializzazione/deserializzazione di oggetti di questa classe. <b>Non
     * deve essere utilizzato</b>
     */
    public ISO8583Read() {

        super();
    }

    /**
     * Costruttore.
     * 
     * @param iso8583Init classe di gestione descrittori e bitMap.
     */
    public ISO8583Read(byte[] message) throws AppCrash {

        _message = message;
        String tipoDic = "";

        String tipo = getType();
        // I messaggi ISO8583 versione 93 iniziano tutti per "1": 1100-1110 1200-1210 1420-1430
        // invece i messaggi ISO87 iniziano con "0": 0100-0110 0420-0430
        if (tipo.substring(0, 1).equals("1")) {
            tipoDic = "93";
        }
        _fieldsSpecification = ISO8583Dictionary.GetInstance(tipoDic);
        _bitMap = new NewBitMapISO8583(message);
    }

    /**
     * Ritorna il campo di indice key del messaggio.
     * 
     * @param keyName indice del campo.
     * @return String
     */
    @Override
    public String getField(String keyName) throws AppCrash {

        int offset = 4 + 8;
        byte[] byteArrayLen = null;
        byte[] byteArrayField = null;
        int target = 0; // contiene il numero di byte di cui è formato il campo variabile
        String result = null;

        try {
            int key = Integer.parseInt(keyName.trim());

            ErrDetector.GetInstance().param(key > 0 && key <= 128);
            if (_bitMap.exist(key)) {
                for (int i = 1; i <= key; i++) {
                    if (_bitMap.exist(i)) {
                        if (_fieldsSpecification.getLengthType(i) == FIX) {
                            target = _fieldsSpecification.getMaxLength(i);
                            offset += target;
                        } else {
                            byteArrayLen = new byte[_fieldsSpecification.getLengthType(i)];
                            for (int j = 0; j < byteArrayLen.length; j++) {
                                byteArrayLen[j] = _message[offset];
                                offset++;
                            }

                            target = new Integer(new String(byteArrayLen)).intValue();
                            offset += target;
                        }
                    }
                }
                byteArrayField = new byte[target];
                for (int j = 0; j < target; j++) {
                    byteArrayField[j] = _message[offset + j - target];
                }
                result = new String(byteArrayField);

            }
        } catch (NumberFormatException e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("ISO8583Read", "errore lettura campo: " + keyName);
            throw ac;
        } catch (AppCrash e) {
            e.logContext("ISO8583Read", "errore lettura campo: " + keyName);
            throw e;
        }
        return result;
    }

    /**
     * Ritorna il tipo di messaggio.
     * 
     * @return String
     */
    @Override
    public String getType() {

        byte[] byteArray = new byte[4];
        for (int i = 0; i < 4; i++) {
            byteArray[i] = _message[i];
        }
        return new String(byteArray);
    }
}
