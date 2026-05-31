/*
  RequestReader.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 24/10/2000

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.servlet.frame;

import javax.servlet.http.HttpServletRequest;

import net.project.mess.MsgReader_itf;

/**
 * Questa classe e' un adapter per oggetti di tipo HttpServletRequest all'interfaccia MsgReader_itf della libreria SSB.
 * Permette la lettura di una request http come se fosse un messaggio.
 */
public class RequestReader implements MsgReader_itf {

    private HttpServletRequest _request;

    /**
     * Costruttore vuoto.
     */
    public RequestReader() {

    }

    /**
     * Costruttore. Prende in ingresso la request da adattare
     * 
     * @param req request da adattare
     */
    public RequestReader(HttpServletRequest req) {

        _request = req;
    }

    /**
     * Ritorna il campo corrispondente al nome richiesto. Esegue una getParameter() sulla request HTTP
     * 
     * @param nome java.lang.String nome del campo.
     * @return String
     */
    @Override
    public String getField(String nome) {

        String value = _request.getParameter(nome);
        if (value == null) {
            return "";
        } else {
            return value;
        }
    }

    /**
     * Ritorna il tipo di messaggio. Come tipo messaggio viene reso il protocollo delle request come estratto dalla
     * getProtocol()
     * 
     * @return String
     */
    @Override
    public String getType() {

        return _request.getProtocol();
    }
}
