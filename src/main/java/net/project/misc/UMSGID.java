/*
  UMSGID.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 28/09/2001

  Autore: Simone Z.

  Note:

  Modifiche:

 */

package net.project.misc;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Questa classe implementa l'identificatore univoco di un messaggio
 */

public class UMSGID {

    /**
     * InetAddress to make the UID globally unique
     */
    private static String _IPaddress;

    /**
     * a random number
     */
    private static String _AppUnique;

    /**
     * Usato per la sincronizzazione
     */
    private static Object _Mutex;

    private static long   _LastTime;
    private static long   _DELAY;

    static {
        _AppUnique = Integer.toString(Math.abs((new Object()).hashCode()), 16) + "000000000";
        _AppUnique = _AppUnique.substring(0, 8);
        _Mutex = new Object();
        _LastTime = System.currentTimeMillis();
        _DELAY = 10; // in milliseconds
        try {
            BigInteger ip = new BigInteger(InetAddress.getLocalHost().getAddress());
            ip = ip.abs();
            _IPaddress = ip.toString(16) + "000000000";
            _IPaddress = _IPaddress.substring(0, 8);
        } catch (UnknownHostException ex) {
            _IPaddress = _AppUnique;
        }
    }

    public UMSGID() {

    }

    /**
     * Ricava uno Unique Message Identifier . L'identificatore viene composto concatenando l'indirizzo IP della
     * macchina, il tempo corrente in millisecondi ed un identificativo random ricavato allo start della applicazione.
     *
     * @return java.lang.String Umsgid per la richiesta.
     */
    public String getValue() {

        synchronized (_Mutex) {
            boolean done = false;
            while (!done) {
                long time = System.currentTimeMillis();
                if (time < _LastTime + _DELAY) {
                    // pause for a second to wait for time to change
                    try {
                        Thread.currentThread();
                        Thread.sleep(_DELAY);
                    } catch (java.lang.InterruptedException e) {
                    } // ignore exception
                    continue;
                } else {
                    _LastTime = time;
                    done = true;
                }
            }
        }

        String time = Long.toString(_LastTime, 16) + "0000000000000000";
        String umsgid = _IPaddress + time.substring(0, 16) + _AppUnique;

        return umsgid;

    }

    /**
     * Ricava uno Unique Message Identifier . L'identificatore viene composto concatenando l'indirizzo IP della
     * macchina, il tempo corrente in millisecondi ed un identificativo random ricavato allo start della applicazione.
     * Il nome del Thread che richiama il metodo viene impostato con il valore ricavato
     *
     * @return java.lang.String Umsgid per la richiesta.
     */
    public String getValueSetThreadName() {

        String umsgid = getValue();

        // setta il nome del thread corrente con lo unique message id ricavato
        Thread.currentThread().setName(umsgid);

        return umsgid;
    }

}
