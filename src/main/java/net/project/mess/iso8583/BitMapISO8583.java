
package net.project.mess.iso8583;

/*
 BitMapISO8583.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 18/05/1999

 Autore: Rosella V.

 Note:

 Modifiche:	13/10/99	TL16 ristrutturazione eccezioni

 */

import java.math.BigInteger;
import java.util.BitSet;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;

/**
 * Classe per la gestione delle due bitmap (primary e secondary bitmap). Ciascuna bitmap comprende 64 campi. La
 * variabile _bitMap rappresenta entranbe le bitmap lasciando inutilizzato il primo bit. Utilizza la classe @link
 * java.util.BitSet per la memorizzazione dei valori degli elementi delle bitmap.
 */

public class BitMapISO8583 {

    private BitSet _bitMap; // bitmap rappresentante i valori della primary e della secondary bitmap

    /**
     * Costruttore
     * 
     * @exception AppCrash.
     */
    public BitMapISO8583() {

        _bitMap = new BitSet(129); // inizializzazione della bitmap di 128 bit (64 per la primary e altrettanti per la
    } // secondary bitmap

    /**
     * Costruttore
     * 
     * @exception AppCrash.
     */
    public BitMapISO8583(byte[] message) throws AppCrash {

        _bitMap = new BitSet(129); // inizializzazione della bitmap di 128 bit (64 per la primary e altrettanti per la
        set(message);
    } // secondary bitmap

    /**
     * Converte 8 bit in un byte.
     * 
     * @return byte.
     * @param idx indice di inizio scansione della bitmap.
     */
    private byte bitToByte(int idx) {

        int result = 0;

        for (int i = idx; i < idx + 8; i++) {
            if (_bitMap.get(i) == true) {
                result = result | 0x01;
            }
            if (i == idx + 7) {
                break;
            }
            result <<= 1;
        }
        return (byte) result;
    }

    /**
     * Verifica se un Data Element è presente nel messaggio.
     * 
     * @param key indice del campo.
     * @return true se il Data Element è presente.
     * @exception AppCrash.
     */
    public boolean exist(int key) throws AppCrash {

        boolean in = false;

        try {
            ErrDetector.GetInstance().param(key > 0 && key <= 128);
            in = _bitMap.get(key);
        } catch (AppCrash e) {
            e.logContext("BitMapISO8583", "Data element inesistente: " + key);
            throw e;
        }
        return in;
    }

    /**
     * Converte la BitMap in un array byte.
     * 
     * @return byte[].
     * @exception AppCrash.
     */
    public byte[] getBytes() throws AppCrash {

        byte[] byteArray = new byte[16];

        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = bitToByte(i * 8 + 1);
        }
        return byteArray; // valore della bitMap convertito in array di byte
    }

    /**
     * Imposta le due bitmap in base al messaggio corrente. Se un campo esiste il corrispondente bit nella bitmap viene
     * imposta a 1.
     * 
     * @param message messaggio.
     * @exception AppCrash.
     */
    private void set(byte[] message) throws AppCrash {

        long a;
        byte[] bArray = new byte[4];
        byte[] primaryMap = new byte[8];
        byte[] mapTemp = new byte[8];
        byte[] secondaryMap = null;
        BigInteger bi = null;
        int offset = 0;

        try {
            ErrDetector.GetInstance().param(message != null);
            for (int i = 0; i < primaryMap.length; i++) {
                ErrDetector.GetInstance().preCond((4 + offset) < message.length);
                mapTemp[i] = message[4 + offset]; // viene copiata la primary bitmap dal messaggio
                offset++;
            }
            for (int i = 0; i < mapTemp.length; i++) {
                bArray[3] = mapTemp[i];
                bi = new BigInteger(bArray); // gestione del segno su ogni byte della bitmap
                a = bi.intValue();
                for (int j = 0; j < 8; j++) {
                    int b = new Double(Math.pow(2, 7 - j)).intValue();
                    if (((a) & (b)) != 0) {
                        setBit(i * 8 + j + 1, true);
                        // Logger.GetInstance().log3("settato bit : "+(i*8+j+1)); // impostazione a 1 del bit
                        // corrispondente a un campo esistente
                    }

                }
            }
            if (exist(1)) { // se esiste secondary bitmap
                secondaryMap = new byte[8];
                for (int i = 0; i < secondaryMap.length; i++) {
                    ErrDetector.GetInstance().preCond((4 + offset) < message.length);
                    mapTemp[i] = message[4 + offset]; // viene copiata la secondary bitmap dal messaggio
                    offset++;
                }
                for (int i = 0; i < mapTemp.length; i++) {
                    bArray[3] = mapTemp[i];
                    bi = new BigInteger(bArray); // gestione del segno su ogni byte della bitmap
                    a = bi.intValue();
                    for (int j = 0; j < 8; j++) {
                        int b = new Double(Math.pow(2, 7 - j)).intValue();
                        if (((a) & (b)) != 0) {
                            setBit(64 + i * 8 + j + 1, true); // impostazione a 1 del bit corrispondente a un campo
                                                              // esistente
                        }
                    }
                }
            }
            // Logger.GetInstance().log3(_bitMap.toString());
        } catch (AppCrash e) {
            Logger.GetInstance().logError(toString());
            throw e;
        }
    }

    /**
     * Imposta un bit della BitMap.
     * 
     * @param key indice del campo.
     * @param value se true il bit è impostato a 1. 0 viceversa.
     * @exception AppCrash.
     */
    public void setBit(int key, boolean value) throws AppCrash {

        try {
            ErrDetector.GetInstance().param(key > 0 && key <= 128);
            if (value) {
                _bitMap.set(key); // il campo esiste
            } else {
                _bitMap.clear(key); // il campo non esiste
            }
        } catch (AppCrash e) {
            e.logContext("BitMapISO8583", "Data element inesistente: " + key);
            throw e;
        }
    }
}
