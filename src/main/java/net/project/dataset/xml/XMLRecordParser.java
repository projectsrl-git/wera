/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.dataset.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Questa classe serve per eseguire il parsing di un documento XML ottenendo un record alla volta attraverso chiamate al
 * metodo parseNext(). Il parsing avviene tramite SAX e non necessita della lettura di tutto il documento in memeoria.
 * <p>
 * Il documento e' suddiviso in record in base al set di nomi degli elementi da considerare come record che viene
 * passato al constructor. Un record e' il contenuto 'appiattito' di uno di questi elementi XML. Ogni chiamata a
 * parseNext() restituisce un oggetto di tipo Map che contiene il record letto.
 * <p>
 * Un record(elemento) XML viene tradotto in: - una chiave <code>recordtype</code> con il nome dell'elemento dal quale
 * e' stato ottenuto il record
 * <p>
 * - tante chiavi quanti sono gli attributi dell'elemento del tipo :nomeattributo con il loro rispettivo valore
 * <p>
 * - una chiave per ogni sottoelemento del tipo nomelemento con il valore
 * <p>
 * - una chiave per ogni attributo di ogni sottoelemento del tipo nomeelemento:nomeattrib con il suo valore
 * <p>
 * Il nome dei sotto elementi viene composto come un path es TestataRichiesta1/IDnegozio1.
 * <p>
 * Il nome di tutti i sotto elementi e' seguito da un numero La classe implementa il ContentHandler per il parser Sax ed
 * utilizza un thread interno per poter eseguire il parsing un record alla volta.
 *
 * @author s.zorzetti
 */
public class XMLRecordParser implements ContentHandler, Runnable {

    /** Stati possibili per il parser */
    public final static int NOTINITIALIZED = 0;
    public final static int STARTED        = 1;
    public final static int STOPPED        = 2;

    /** Il parser SAX usato per il parsing dell'XML */
    private XMLReader       _parser;
    /** finito di eseguire il parsing di un record */
    private boolean         _recordParsed  = false;
    /** documento finito */
    private boolean         _finished      = false;
    /** il parser e' in una sezione all'interno di un elemento considerato record */
    private boolean         _parsingRecord = false;
    /** e' il primo record */
    private boolean         _firstRow      = true;

    /** Set con i nomi degli elementi XML che costituiscono i record */
    private Set             _recordElementNames;
    /** buffer per accumulare i caratteri del valore dell'elemento corrente */
    private StringBuffer    _currentBuffer;
    /** stack utilizzato per costruire i nomi stile path dei campi del record */
    private Stack           _stack;

    private InputSource     _src;
    /** contenuto del record corrente in formazione */
    private Map             _currentRow;
    /** contatori degli elementi con lo stesso nome presenti in un elemento record */
    private Map             _counters;

    /** stato del record parser */
    private int             _state;

    /** thread usato per eseguire il parsing un record alla volta */
    private Thread          _parsingThread;

    /**
     * Constructor. Prende in ingresso l'insieme dei nomi degli elementi da considerare record e la sorgente dati di cui
     * fare il parsing
     */
    public XMLRecordParser(Set recordElementNames, InputSource src) throws AppCrash {

        _recordElementNames = recordElementNames;
        _src = src;

        _stack = new Stack();

        _currentRow = new HashMap();
        _counters = new HashMap();

        _state = NOTINITIALIZED;

        // Crea il parser XML SAX da utilizzare
        try {
            _parser = XMLReaderFactory.createXMLReader();
            _parser.setContentHandler(this);
        } catch (SAXException e) {
            AppCrash ac = new AppCrash(e);
            throw ac;
        }
    }

    /**
     * Avvia il parsing del documento. Deve essere chiamato prima di parseNext()
     *
     */
    public synchronized void start() {

        if (_state == NOTINITIALIZED) {
            _parsingThread = new Thread(this);
            _parsingThread.start();
            _state = STARTED;
        }

    }

    /**
     * Ferma il parsing del documento.
     *
     */
    public void stop() {

        if (_state == STARTED) {
            if (_parsingThread.isAlive()) {
                _parsingThread.interrupt();
            }
            _parsingThread = null;
            _state = STOPPED;
        }

    }

    /**
     * Esegue il parsing del documento XML per restituire il prossimo record. Un record e' il contenuto 'appiattito' di
     * un elemento XML che e' stato indicato come da considerarsi record
     * 
     * @return Map il contenuto del record letto
     * @throws AppCrash
     */
    synchronized Map parseNext() throws AppCrash {

        ErrDetector.GetInstance().invariant(_state == STARTED);

        try {
            _recordParsed = false;
            notify();

            while ((_recordParsed == false) && (_finished == false)) {
                wait();
            }

            if (_finished == false) {
                return (Map) ((HashMap) _currentRow).clone();
            } else {
                return null;
            }
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            throw ac;
        }
    }

    /**
     * Questo metodo serve durante il parsing del c
     */
    @Override
    synchronized public void run() {

        try {
            _recordParsed = false;
            _parser.parse(_src);

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            throw new RuntimeException("Crash in run durante il parsing");
        } finally {
            _finished = true;
            notify();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    @Override
    public void setDocumentLocator(Locator arg0) {

        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    @Override
    public synchronized void endDocument() throws SAXException {

        _finished = true;
        notify();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    @Override
    public void startPrefixMapping(String arg0, String arg1) throws SAXException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    @Override
    public void endPrefixMapping(String arg0) throws SAXException {

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

        // se non ho ancora letto record allora creo un record speciale con i soli
        // attributi dell'elemento piu' esterno del documento (se esistono)
        if (_firstRow && attributes.getLength() == 0) {
            _firstRow = false;
        }
        if (_firstRow) {
            for (int j = 0; j < attributes.getLength(); j++) {
                _currentRow.put("doc:" + attributes.getLocalName(j), attributes.getValue(j));
            }
            _firstRow = false;
            recordReady("doc");
        }

        if (_recordElementNames.contains(localName)) {
            _parsingRecord = true;
            _counters.clear();
            _currentRow.clear();
            _stack.push("");
        }

        // Ignoro gli element che non sono all'interno dei record che cerco
        if (_parsingRecord == false) {
            return;
        }

        _currentBuffer = new StringBuffer();

        String prefix = createFieldName(localName);

        // inserisco nel record corrente tutti gli attributi dell'elemento corrente con
        // la forma pathelem:nomeattrib
        for (int j = 0; j < attributes.getLength(); j++) {
            _currentRow.put(prefix + ":" + attributes.getLocalName(j), attributes.getValue(j));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public synchronized void endElement(String uri, String localName, String qName) throws SAXException {

        String prefix = null;

        if (_parsingRecord) {
            prefix = (String) _stack.pop();
            if (prefix.equals("")) {
                prefix = "val";
            }
        }

        if (_currentBuffer != null && _currentBuffer.length() > 0) {
            _currentRow.put(prefix, _currentBuffer.toString());
            _currentBuffer = null;
        }

        if (_recordElementNames.contains(localName)) {
            recordReady(localName);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] data, int start, int length) throws SAXException {

        if (_parsingRecord == false) {
            return;
        }
        if (_currentBuffer == null) {
            return;
        }

        _currentBuffer.append(data, start, length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    @Override
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {

        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    @Override
    public void processingInstruction(String arg0, String arg1) throws SAXException {

        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    @Override
    public void skippedEntity(String arg0) throws SAXException {

        // TODO Auto-generated method stub
    }

    private synchronized void recordReady(String elementName) {

        _recordParsed = true;
        _parsingRecord = false;
        _currentRow.put("recordtype", elementName);
        notify();

        while (_recordParsed == true) {
            try {
                wait();
            } catch (InterruptedException e) {
                AppCrash ac = new AppCrash(e);
            }
        }

    }

    /**
     * Crea il nome del campo da inserire nel record corrente.
     * 
     * @param elementName
     * @return
     */
    protected String createFieldName(String elementName) {

        // Assegno un ordinale all'elemento corrente ricavando il numero di elementi
        // di questo tipo presenti nel record corrente ed aggiungendo 1
        Integer elementCount = (Integer) _counters.get(elementName);
        if (elementCount != null) {
            elementCount = new Integer(elementCount.intValue() + 1);
        } else {
            elementCount = new Integer(1);
        }
        _counters.put(elementName, elementCount);

        // se l'elemento corrente non e' da cosiderarsi come record allora gli assegno
        // un nome nel record corrente costituendo il path relativo: elem1/sotto1/questo1
        if (_recordElementNames.contains(elementName) == false) {
            String newPrefix = (String) _stack.peek();
            if (newPrefix.equals("")) {
                newPrefix = elementName + elementCount.intValue();
            } else {
                newPrefix = newPrefix + "/" + elementName + elementCount.intValue();
            }
            _stack.push(newPrefix);
        }

        return (String) _stack.peek();

    }
}
