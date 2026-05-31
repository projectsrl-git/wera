/*
  hash.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 25/10/2000

  Autore: Simone Z.

  Note: deriva dalla classe in TL

  Modifiche:


 */

package net.project.misc;

import java.security.MessageDigest;

import net.project.errors.AppCrash;
import sun.misc.BASE64Encoder;

/**
 * La classe esporta un metodo statico per il calcolo dell'hash MD5 su una stringa. Il risultato e' convertito in base
 * 64
 */
public class hash {

    /**
     * Metodo statico per il calcolo dell'hash MD5 su una stringa. Il risultato e' una stringa che contiene l'hash MD5
     * convertito in base 64
     * 
     * @param message java.lang.String stringa della quale calcolare l'hash MD5
     * @return java.lang.String stringa che contiene l'hash MD5 convertito in base 64
     * @throws AppCrash
     */
    public static String doMD5(String message) throws AppCrash {

        // Check arguments.

        try {
            // Obtain a message digest object.
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] buffer = message.getBytes();

            md.update(buffer, 0, message.length());
            byte[] raw = md.digest();

            // Trasforma il digest in base64.
            BASE64Encoder encoder = new BASE64Encoder();
            String base64 = encoder.encode(raw);
            return base64;
        } catch (Throwable e) {
            AppCrash ex = new AppCrash(e);
            ex.logContext("doMD5 messaggio : ", message);
            throw ex;
        }
    }

    /**
     * Metodo statico per il calcolo dell'hash MD5 su una stringa. Il risultato e' una stringa che contiene l'hash MD5
     * convertito in esadecimale
     * 
     * @param message java.lang.String stringa della quale calcolare l'hash MD5
     * @return java.lang.String stringa che contiene l'hash MD5 convertito in esadecimale
     * @throws AppCrash
     */
    public static String doMD5Hex(String message) throws AppCrash {

        // Check arguments.

        try {
            // Obtain a message digest object.
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] buffer = message.getBytes();

            md.update(buffer, 0, message.length());
            byte[] raw = md.digest();

            // Trasforma il digest in base64.
            return toHexString(raw);
        } catch (Throwable e) {
            AppCrash ex = new AppCrash(e);
            ex.logContext("doMD5Hex messaggio : ", message);
            throw ex;
        }

    }

    /**
     * Metodo statico per il calcolo dell'hash SHA-1 su una stringa. Il risultato e' una stringa che contiene l'hash
     * SHA-1 convertito in base 64
     * 
     * @param message java.lang.String stringa della quale calcolare l'hash SHA-1
     * @return java.lang.String stringa che contiene l'hash SHA-1 convertito in base 64
     * @throws AppCrash
     */
    public static String doSHA1(String message) throws AppCrash {

        // Check arguments.

        try {
            // Obtain a message digest object.
            MessageDigest md = MessageDigest.getInstance("SHA");

            byte[] buffer = message.getBytes();

            md.update(buffer, 0, message.length());
            byte[] raw = md.digest();

            // Trasforma il digest in base64.
            BASE64Encoder encoder = new BASE64Encoder();
            String base64 = encoder.encode(raw);
            return base64;
        } catch (Throwable e) {
            AppCrash ex = new AppCrash(e);
            ex.logContext("doSHA-1 messaggio : ", message);
            throw ex;
        }
    }

    /**
     * Metodo statico per il calcolo dell'hash SHA-1 su una stringa. Il risultato e' una stringa che contiene l'hash
     * SHA-1 convertito in esadecimale
     * 
     * @param message java.lang.String stringa della quale calcolare l'hash SHA-1
     * @return java.lang.String stringa che contiene l'hash SHA-1 convertito in esadecimale
     * @throws AppCrash
     */
    public static String doSHA1Hex(String message) throws AppCrash {

        // Check arguments.

        try {
            // Obtain a message digest object.
            MessageDigest md = MessageDigest.getInstance("SHA");

            byte[] buffer = message.getBytes();

            md.update(buffer, 0, message.length());
            byte[] raw = md.digest();

            // Trasforma il digest in base64.
            return toHexString(raw);
        } catch (Throwable e) {
            AppCrash ex = new AppCrash(e);
            ex.logContext("doSHA-1Hex messaggio : ", message);
            throw ex;
        }

    }

    /**
     * Metodo statico per il calcolo dell'hash SHA-1 su una stringa. Il risultato e' un arra di byte che contiene l'hash
     * SHA-1.
     * 
     * @param message java.lang.String stringa della quale calcolare l'hash SHA-1.
     * @return byte[] array di byte che contiene l'hash SHA-1.
     * @throws AppCrash
     */
    public static byte[] doSHA1Binary(String message) throws AppCrash {

        try {
            // Obtain a message digest object.
            MessageDigest md = MessageDigest.getInstance("SHA");

            byte[] buffer = message.getBytes();

            md.update(buffer, 0, message.length());
            byte[] raw = md.digest();
            return raw;
        } catch (Throwable e) {
            AppCrash ex = new AppCrash(e);
            ex.logContext("doSHA-1 messaggio : ", message);
            throw ex;
        }
    }

    private static String toHexString(byte[] data) {

        StringBuffer ret = new StringBuffer();
        final String stuff[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
        int l, r;

        for (int i = 0; i < data.length; i++) {
            l = ((data[i]) & 0xF0) >> 4;
            r = (data[i]) & 0x0F;
            ret.append(stuff[l]).append(stuff[r]);
        }
        return ret.toString();
    }

}
