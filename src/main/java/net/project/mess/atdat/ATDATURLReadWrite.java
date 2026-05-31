
package net.project.mess.atdat;

/*
 ATDATURLReadWrite.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 19/05/1999

 Autore: Simone Z.

 Note:

 Modifiche:
 25/11/99	TL17 ottimizzazione uso stringhe. Rosella V.

 15/12/00	Andrea Ricci
 Fix errore metodo getField(). Se valore null viene restituito "".

 */

import java.util.Enumeration;
import java.util.Hashtable;

import net.project.mess.MsgWriter_itf;

/**
 * Classe per la creazione di un messaggio ATDAT
 */

public class ATDATURLReadWrite implements MsgWriter_itf {

    private Hashtable _campi;
    private String    _tipo;

    /**
     * Costruttore presente solo per permettere la serializzazione/deserializzazione di oggetti di questa classe. <b>Non
     * deve essere utilizzato</b>
     */
    public ATDATURLReadWrite() {

    }

    /**
     * Costruttore.
     */

    public ATDATURLReadWrite(String tipo) {

        _campi = new Hashtable(31, .5f);
        _tipo = tipo;
    }

    /**
     * Ritorna il messaggio.
     * 
     * @return String
     */
    @Override
    public byte[] getMessage() {

        StringBuffer messaggio = new StringBuffer();
        String campo = null;
        messaggio.append(_tipo);
        for (Enumeration campi = _campi.keys(); campi.hasMoreElements();) {
            campo = (String) campi.nextElement(); // campo è la chiave
            messaggio.append("&").append(campo).append("=").append((String) _campi.get(campo));
        }
        messaggio.append("&");
        return messaggio.toString().getBytes();
    }

    /**
     * Consente l'impostazione di un campo col corrispondente valore.
     * 
     * @param nome nome del campo.
     * @param valore valore del campo.
     */
    @Override
    public void setField(String nome, String valore) {

        _campi.put(nome, valore);
    }

    @Override
    public String getField(String nome) {

        String value = (String) _campi.get(nome);
        return (value == null ? "" : value);
    }

    /**
     * Ritorna il tipo di messaggio.
     * 
     * @return String
     */
    @Override
    public String getType() {

        return _tipo;
    }

}
