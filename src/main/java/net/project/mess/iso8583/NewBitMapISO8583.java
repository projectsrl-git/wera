/*
  NewBitMapISO8583.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 26/07/2000

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.mess.iso8583;

import java.math.BigInteger;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;

/**
 * Classe per la gestione delle due bitmap (primary e secondary bitmap). Ciascuna bitmap comprende 64 campi. La
 * variabile _bitMap rappresenta entranbe le bitmap. Utilizza la classe java.math.BigInteger per la memorizzazione dei
 * valori degli elementi delle bitmap.
 */

public class NewBitMapISO8583 {

    // bitmap rappresentante i valori della primary e della secondary bitmap
    private BigInteger _bitMap;

    /**
     * Costruttore. Inizializzazione della bitmap di 128 bit (64 per la primary e altrettanti per la secondary bitmap).
     */
    public NewBitMapISO8583() {

        _bitMap = new BigInteger("0");
    }

    /**
     * Costruttore. Inizializzazione della bitmap di 128 bit (64 per la primary e altrettanti per la secondary bitmap).
     *
     * @param message byte[]
     * @exception net.project.errors.AppCrash.
     */
    public NewBitMapISO8583(byte[] message) throws AppCrash {

        byte[] arrayMsg = new byte[16];
        byte[] secondaryBitmap = new byte[8];

        // Elimina i primi 4 bytes del messaggio (identificativi del tipo di msg)
        System.arraycopy(message, 4, arrayMsg, 0, 16);
        _bitMap = new BigInteger(arrayMsg);
        try {
            // Testa la presenza della secondary bitmap
            if (!exist(1)) {
                System.arraycopy(secondaryBitmap, 0, arrayMsg, 8, 8);
                _bitMap = new BigInteger(arrayMsg);

            }
        } catch (AppCrash e) {
            e.logContext("NewBitMapISO8583", "Data element inesistente: 1");
            throw e;
        }
    }

    /**
     * Converte la BitMap in un array di byte.
     *
     * @return byte[].
     */
    public byte[] getBytes() throws AppCrash {

        // Crea un array vuoto di 16 byte
        byte[] byteArray = new byte[16];
        // Ricava l'array di byte corrispondente alla bitmap
        byte[] byteBitMap = _bitMap.toByteArray();
        // Copia il secondo sul primo
        int len = 8;
        int offset = 0;
        if (exist(1)) {
            len = byteBitMap.length - 1;
            offset = 1;
        }
        // System.arraycopy(byteBitMap, 1, byteArray, 0, byteBitMap.length - 1);
        System.arraycopy(byteBitMap, offset, byteArray, 0, len);
        return byteArray;
    }

    /**
     * Verifica se un Data Element è presente nel messaggio.
     *
     * @param key indice del campo.
     * @exception net.project.errors.AppCrash.
     * @return true se il Data Element è presente, false altrimenti.
     */
    public boolean exist(int key) throws AppCrash {

        boolean exist = false;
        try {
            ErrDetector.GetInstance().param(key > 0 && key <= 128);
            exist = _bitMap.testBit(127 - (key - 1));
        } catch (AppCrash e) {
            e.logContext("NewBitMapISO8583", "Data element inesistente: " + key);
            throw e;
        }
        return exist;
    }

    /**
     * Imposta un bit della BitMap.
     *
     * @param key indice del campo.
     * @param value se true il bit è impostato a 1. 0 viceversa.
     * @exception net.project.errors.AppCrash.
     */
    public void setBit(int key, boolean value) throws AppCrash {

        try {
            ErrDetector.GetInstance().param(key > 0 && key <= 128);
            if (value) {
                _bitMap = _bitMap.setBit(127 - (key - 1)); // il campo esiste
            } else {
                _bitMap = _bitMap.clearBit(127 - (key - 1)); // il campo non esiste
            }
        } catch (AppCrash e) {
            e.logContext("NewBitMapISO8583", "Data element inesistente: " + key);
            throw e;
        }
    }
}
