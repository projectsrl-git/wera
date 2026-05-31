/*
  XmlRead.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 3/7/2003

  Autore: Luca M.
 
  Note:

  Modifiche:	Aggiunta gestione di attributi e sottotag. 
  				(23/2/2004 - Luca M.)

 */

package net.project.mess.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.mess.MsgReader_itf;

import org.xml.sax.SAXException;

/**
 * Lettore di messaggi xml.
 */
public class XmlRead implements MsgReader_itf {

    // Tipo del messaggio.
    public static final String XML_MSG_TYPE = "xml";

    private byte[]             _message     = null;
    private SaxXMLParser       _XMLParser   = null;
    private String             _configName  = null;

    /**
     * Costruttore presente solo per permettere la serializzazione/deserializzazione di oggetti di questa classe. <b>Non
     * deve essere utilizzato</b>
     */
    public XmlRead() {

    }

    /**
     * Costruttore.
     * 
     * @param configName java.lang.String Nome della configurazione. NON PUO' ESSERE NULL.
     * @param message byte[] Messaggio xml. NON PUO' ESSERE NULL.
     * @exception net.project.errors.AppCrash Se un parametro in ingresso, e' NULL, o in caso di errori nel parsing del
     *                messaggio xml.
     */
    public XmlRead(String configName, byte[] message) throws AppCrash {

        ErrDetector.GetInstance().preCond(configName != null, "Il parametro configName non puo' essere null!");
        ErrDetector.GetInstance().preCond(message != null, "Il parametro message non puo' essere null!");

        _configName = configName;
        _message = message;
        parseRequest();

    }

    /**
     * Costruttore.
     * 
     * @param message byte[] Messaggio xml. NON PUO' ESSERE NULL.
     * @exception net.project.errors.AppCrash Se il parametro in ingresso, e' NULL, o in caso di errori nel parsing del
     *                messaggio xml.
     */
    public XmlRead(byte[] message) throws AppCrash {

        this("", message);
    }

    /**
     * @param name java.lang.String Il nome del tag o dell'attributo. NON PUO' ESSERE NULL.
     * @return java.lang.String Il valore del tag o dell'attributo. NON E' MAI NULL.
     * @exception net.project.errors.AppCrash Se il parametro in ingresso e' NULL, o in caso di errori nel recupero del
     *                valore del tag o dell'attributo.
     */
    @Override
    public String getField(String name) throws AppCrash {

        ErrDetector.GetInstance().preCond(name != null, "Invocato getField(java.lang.String) con argomento null!");
        String fieldValue = _XMLParser.getTagValue(name);
        return (fieldValue != null ? fieldValue : "");

    }

    /**
     * Restituisce il nome del tipo di messaggio.
     * 
     * @return java.lang.String il tipo di messaggio
     */
    @Override
    public String getType() {

        return XML_MSG_TYPE;
    }

    // Questo metodo e' definito protected
    // per permetterne l'override in caso di
    // estensione della presente classe.
    protected void parseRequest() throws AppCrash {

        ByteArrayInputStream messageAsInputStream = new ByteArrayInputStream(_message);
        _XMLParser = new SaxXMLParser();
        try {
            _XMLParser.parse(messageAsInputStream);

        } catch (SAXException se) {
            throw new AppCrash(se);
        } catch (IOException ioe) {
            throw new AppCrash(ioe);
        }

    }

}
