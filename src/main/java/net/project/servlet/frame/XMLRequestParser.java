/*
  XMLRequestParser.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 11/10/2001

  Autore: Assunta C.

  Note:

  Modifiche:

 */

package net.project.servlet.frame;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.misc.Config;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Questa classe esegue il parse di una richiesta pervenuta con un file XML. Proprietà lette dal file di configurazione
 * (tutte OPZIONALI: se assenti, vale il default): ValidationURI = URI dove sono situati i file .dtd (se assente, nel
 * file xml è necessario indicare il file .dtd con il path assoluto, oppure si deve fare a meno del file .dtd) N.B.
 * Questa proprietà è utilizzabile solo per il parsing di un oggetto InputStream (non di un oggetto String) Coalescing =
 * true se il parser deve convertire CDATA nodes in Text nodes ed accodarli ad evenutali Text nodes adiacenti; false
 * (DEFAULT) altrimenti ExpandEntityReferences = true (DEFAULT) se il parser deve espandere entity reference nodes;
 * false altrimenti IgnoringComments = true se il parser deve ignorare i commenti; false (DEFAULT) altrimenti
 * IgnoringElementContentWhitespace = true se il parser deve eliminare lo spazio nel contenuto degli elementi; false
 * (DEFAULT) altrimenti NamespaceAware = true se il parser deve fornire supporto per i namespaces xml; false (DEFAULT)
 * altrimenti Validating = true se il parser deve eseguire anche la validazione del documento; false (DEFAULT)
 * altrimenti
 */

public class XMLRequestParser {

    public static final int    CODICE_FORMATO_MESSAGGIO_ERRATO = -1;

    private DocumentBuilder    _db                             = null;
    private ParserErrorHandler _parserErrorHandler             = null;

    /**
     * Costruttore.
     */
    public XMLRequestParser() throws AppCrash {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        ErrDetector.GetInstance().param(dbf);
        _db = createDocumentBuilder(dbf);
        ErrDetector.GetInstance().param(_db);
        _parserErrorHandler = new ParserErrorHandler();
        _db.setErrorHandler(_parserErrorHandler);

    }

    private DocumentBuilder createDocumentBuilder(DocumentBuilderFactory dbf) throws AppCrash {

        try {
            if (isRequiredProperty("Coalescing")) {
                dbf.setCoalescing(getMode("Coalescing"));
            }
            if (isRequiredProperty("ExpandEntityReferences")) {
                dbf.setExpandEntityReferences(getMode("ExpandEntityReferences"));
            }
            if (isRequiredProperty("IgnoringComments")) {
                dbf.setIgnoringComments(getMode("IgnoringComments"));
            }
            if (isRequiredProperty("IgnoringElementContentWhitespace")) {
                dbf.setIgnoringElementContentWhitespace(getMode("IgnoringElementContentWhitespace"));
            }
            if (isRequiredProperty("NamespaceAware")) {
                dbf.setNamespaceAware(getMode("NamespaceAware"));
            }
            if (isRequiredProperty("Validating")) {
                dbf.setValidating(getMode("Validating"));
            }
            return dbf.newDocumentBuilder();

        } catch (ParserConfigurationException pce) {
            AppCrash ac = new AppCrash(pce);
            throw ac;
        }

    }

    // Restituisce true se una proprietà del file di configurazione
    // è valorizzata con 'true' o 'false' (case insensitive);
    // false altrimenti.
    // Se la proprietà non è vuota, né pari a 'true' o 'false',
    // allora scrive un log di warning (e restituisce false)
    private boolean isRequiredProperty(String property) {

        String value = Config.GetInstance().getProperty(property);
        if (!(isNotEmpty(value))) {
            return false;
        }
        if (!((value.trim().equalsIgnoreCase("true")) || (value.trim().equalsIgnoreCase("false")))) {
            Logger.GetInstance().log3(
                    "XMLRequestParser - isRequiredProperty - WARNING! Proprietà " + property + " valorizzata con: -"
                            + value + "- La proprietà è lasciata al valore di default.");
            return false;
        }
        return true;

    }

    // Restituisce il valore di una proprietà (String) come boolean
    private boolean getMode(String property) {

        return Boolean.valueOf(Config.GetInstance().getProperty(property).trim()).booleanValue();
    }

    /**
     * Metodo che recupera il nome delle tags xml (di tipo #PCDATA) da un oggetto Document
     * 
     * @param doc Document oggetto rappresentante il file xml da esplorare
     * @return java.util.Enumeration i valori delle tags
     * @exception net.project.errors.AppCrash in caso di errori nel reperimento delle tags xml
     */
    public Enumeration getTags(Document doc) throws AppCrash {

        try {
            NodeList nodeList = doc.getChildNodes();
            doc.getDocumentElement().getParentNode();
            int len = nodeList.getLength();
            Node parent = nodeList.item(0);
            NodeList childList = parent.getChildNodes();
            Vector v = new Vector(len);
            for (int i = 0; i < childList.getLength(); i++) {
                Node node = childList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (!v.contains(node.getNodeName())) v.addElement(node.getNodeName());
                }
            }

            return v.elements();

        } catch (DOMException de) {
            AppCrash ac = new AppCrash(de);
            throw ac;
        }

    }

    /**
     * Metodo che recupera il valore di un tag xml (di tipo #PCDATA) da un oggetto Document
     * 
     * @param doc Document oggetto rappresentante il file xml da esplorare
     * @param tag java.lang.String nome del tag xml
     * @return java.lang.String valore del tag xml
     * @exception net.project.errors.AppCrash in caso di errori nel reperimento del tag xml
     */
    public String getTagValue(Document doc, String tag) throws AppCrash {

        try {
            NodeList nodeList = doc.getElementsByTagName(tag);
            Node node = nodeList.item(0);
            if (node == null) return null;
            if (node.getFirstChild() == null) return null;
            String value = node.getFirstChild().getNodeValue();

            return value;

        } catch (DOMException de) {
            AppCrash ac = new AppCrash(de);
            ac.logContext("XMLRequestParser", "tag: -" + tag + "-");
            throw ac;
        }

    }

    /**
     * Metodo che recupera il valore delle tags xml (di tipo #PCDATA) da un oggetto Document
     * 
     * @param doc Document oggetto rappresentante il file xml da esplorare
     * @param tag java.lang.String nome del tag xml
     * @return java.lang.String[] valori della tag xml
     * @exception net.project.errors.AppCrash in caso di errori nel reperimento del tag xml
     */

    public String[] getValues(Document doc, String tag) throws AppCrash {

        try {
            NodeList nodeList = doc.getElementsByTagName(tag);
            int len = nodeList.getLength();
            String[] v = new String[len];
            for (int i = 0; i < len; i++) {
                Node node = nodeList.item(i);
                v[i] = node.getFirstChild().getNodeValue();
            }

            return v;

        } catch (DOMException de) {
            AppCrash ac = new AppCrash(de);
            ac.logContext("XMLRequestParser", "tag: -" + tag + "-");
            throw ac;
        }

    }

    /**
     * Metodo che esegue il parsing di un file XML
     * 
     * @param xmlStream java.io.InputStream il file XML
     * @return org.w3c.dom.Document l'oggetto ottenuto dal parsing
     * @exception net.project.errors.AppCrash in caso di errori nel parsing
     */
    public Document parse(InputStream xmlStream) throws SAXException, IOException, AppCrash {

        ErrDetector.GetInstance().param(xmlStream);
        Document doc = null;

        if (isValidationURISpecified()) {
            doc = _db.parse(xmlStream, getValidationURI());
        } else {
            doc = _db.parse(xmlStream);
        }

        return doc;

    }

    private boolean isValidationURISpecified() {

        return isNotEmpty(Config.GetInstance().getProperty("ValidationURI"));
    }

    private String getValidationURI() {

        return Config.GetInstance().getProperty("ValidationURI");
    }

    private boolean isNotEmpty(String what) {

        return ((what != null) && (what.trim().length() > 0));
    }

    /**
     * Metodo che esegue il parsing di un testo XML
     * 
     * @param xmlStream java.lang.String il testo XML
     * @return org.w3c.dom.Document l'oggetto ottenuto dal parsing
     * @exception net.project.errors.AppCrash in caso di errori nel parsing
     */
    public Document parse(String xmlStream) throws SAXException, IOException, AppCrash {

        ErrDetector.GetInstance().param(xmlStream);

        Document doc = null;

        doc = _db.parse(xmlStream);

        return doc;

    }

    // Error handler to report errors and warnings
    private class ParserErrorHandler implements ErrorHandler {

        /**
         * Costruttore.
         */
        ParserErrorHandler() {

        }

        /**
         * Returns a string describing parse exception details
         */
        private String getParseExceptionInfo(SAXParseException spe) {

            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "URI=" + systemId + " Line=" + spe.getLineNumber() + ": " + spe.getMessage();
            return info;

        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.
        @Override
        public void warning(SAXParseException spe) throws SAXException {

            Logger.GetInstance().log0(
                    "Class: ParserErrorHandler; Method: warning (SAXParserException); Warning: "
                            + getParseExceptionInfo(spe));

        }

        @Override
        public void error(SAXParseException spe) throws SAXException {

            String message = "Error: " + getParseExceptionInfo(spe);
            Logger.GetInstance().log0(
                    "Class: ParserErrorHandler; Method: error (SAXParserException); Error: "
                            + getParseExceptionInfo(spe));
            throw new SAXException(message);

        }

        @Override
        public void fatalError(SAXParseException spe) throws SAXException {

            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            Logger.GetInstance().log0(
                    "Class: ParserErrorHandler; Method: fatalError (SAXParserException); Fatal Error: "
                            + getParseExceptionInfo(spe));
            throw new SAXException(message);

        }

    }

}
