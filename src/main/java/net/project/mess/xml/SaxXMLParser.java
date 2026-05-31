/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.mess.xml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Questa classe viene utilizzata per eseguire il parsing di un documento XML via SAX in modo da trasformarlo in una
 * hashtable. Ogni element ed attributo viene convertito in una coppia chiave valore della hashtable. Presupposto e' che
 * non esistano element e/o attributi con il medesimo nome.
 *
 * @author zorzetti
 */
public class SaxXMLParser extends DefaultHandler {

    /** Il parser SAX usato per il parsing dell'XML */
    private XMLReader    _parser;

    /** La mappa che contiene i campi dopo il parsing */
    private Map          _campi = new HashMap();

    /** buffer per accumulare i caratteri del valore dell'elemento corrente */
    private StringBuffer _currentBuffer;

    /**
     * Costruttore. Crea il parser SAX
     */
    public SaxXMLParser() throws AppCrash {

        // Crea il parser XML SAX da utilizzare
        try {
            _parser = XMLReaderFactory.createXMLReader();
            _parser.setContentHandler(this);
            _parser.setEntityResolver(this);
            _parser.setErrorHandler(this);
            _parser.setDTDHandler(this);
        } catch (SAXException e) {
            AppCrash ac = new AppCrash(e);
            throw ac;
        }
    }

    /**
     * Il metodo recupera il nome dei tag xml (di tipo #PCDATA) e degli attributi dalla Map campi estratta dal documento
     * XML di cui e' stato fatto il parsing
     *
     * @return java.util.Iterator i nomi dei tags
     *
     * @exception net.project.errors.AppCrash in caso di errori nel reperimento delle tags xml
     */
    public Enumeration getTags() throws AppCrash {

        ErrDetector.GetInstance().preCond(_campi != null);

        return Collections.enumeration(_campi.keySet());
    }

    /**
     * Il metodo recupera il valore di un tag xml (di tipo #PCDATA) o di un attributo dalla Map campi estratta dal
     * documento XML di cui e' stato fatto il parsing
     *
     * @param tag java.lang.String nome del tag xml
     *
     * @return java.lang.String valore del tag xml
     *
     * @exception net.project.errors.AppCrash in caso di errori nel reperimento del tag xml
     */
    public String getTagValue(String tag) throws AppCrash {

        ErrDetector.GetInstance().param(tag);
        ErrDetector.GetInstance().preCond(_campi != null);

        return (String) _campi.get(tag);
    }

    /**
     * Il metodo esegue il parsing di uno stream XML e memorizza attributi ed elementi nella Map locale.
     *
     * @param xmlStream java.io.InputStream il file XML
     *
     * @exception net.project.errors.AppCrash in caso di errori nel parsing
     */
    public void parse(InputStream xmlStream) throws SAXException, IOException, AppCrash {

        // non c'e' la try catch perche' le eccezioni servono fuori alla classe XMLSsbServlet request
        ErrDetector.GetInstance().param(xmlStream);

        InputSource src = new InputSource(xmlStream);
        _parser.parse(src);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
     * org.xml.sax.Attributes)
     */
    @Override
    public synchronized void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {

        _currentBuffer = new StringBuffer();

        // inserisco nel record corrente tutti gli attributi dell'elemento corrente con
        // la forma pathelem:nomeattrib
        for (int j = 0; j < attributes.getLength(); j++) {
            _campi.put(attributes.getLocalName(j), attributes.getValue(j));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public synchronized void endElement(String uri, String localName, String qName) throws SAXException {

        // Il presupposto di tale parser e' che non esistano tag o attributi
        // con lo stesso nome.
        // Tuttavia, qualora esistano, a fronte di richiesta del valore
        // di tale tag o attributo ripetuto viene restituito il valore
        // del primo tag o attributo con tale nome nell'xml.
        if (_campi.containsKey(localName)) {
            _currentBuffer = null;
            return;
        }

        if ((_currentBuffer != null) && (_currentBuffer.length() > 0)) {
            _campi.put(localName, _currentBuffer.toString());
            _currentBuffer = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] data, int start, int length) throws SAXException {

        if (_currentBuffer == null) {
            return;
        }

        _currentBuffer.append(data, start, length);
    }

    /**
     * Crea un InputSource dal quale leggere l'entita' esterna. Si suppone che l'entita' sia su file system nella
     * directory indicata dalla proprieta' di configurazione ValidationURI. Se ValidationURI non e' presente viene preso
     * di default la directory root della applicazione
     */
    @Override
    public InputSource resolveEntity(java.lang.String publicId, java.lang.String systemId) throws SAXException {

        try {
            int last = systemId.lastIndexOf("/");
            String fileName = systemId.substring(last);

            String validationDir = Config.GetInstance().getProperty("ValidationURI", ".");

            String path = Config.GetInstance().makeAbsolutePath(validationDir) + "/" + fileName;

            InputStream is = new FileInputStream(path);
            return new InputSource(is);
        } catch (IOException io) {
            throw new SAXException(io);
        }
    }

}
