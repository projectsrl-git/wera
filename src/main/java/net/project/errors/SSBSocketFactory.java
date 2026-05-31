/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.errors;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * La classe SSBSocketFactory server per creare delle socket con un timeout di creazione ben preciso.
 *
 */
public class SSBSocketFactory {

    /**
     * Crea un nuovo oggetto di tipo Socket.
     *
     * @param addr destinazione
     * @param port porta
     * @param timeout timeout in millisecondi
     *
     * @return Socket la socket
     *
     * @throws InterruptedIOException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public static Socket MakeSocket(InetAddress addr, int port, int timeout) throws IOException {

        return getSocket(addr, port, null, timeout);

    }

    private static Socket getSocket(InetAddress addr, int port, InetAddress srcAddr, int timeout) throws IOException {

        Socket sock = null;

        sock = new Socket();

        if (srcAddr != null) {
            sock.bind(new InetSocketAddress(srcAddr, 0));
        } else {
            sock.bind(null);
        }

        sock.connect(new InetSocketAddress(addr, port), timeout);

        return sock;
    }

    /**
     * Crea un nuovo oggetto di tipo Socket facendo il bind specifico ad un src ip
     *
     * @param addr destinazione
     * @param srcAddr sorgente
     * @param port porta
     * @param timeout timeout in millisecondi
     *
     * @return Socket la socket
     *
     * @throws InterruptedIOException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public static Socket MakeSocket(InetAddress addr, int port, InetAddress srcAddr, int timeout) throws IOException {

        return getSocket(addr, port, srcAddr, timeout);

    }

}
