/*
  XMLSsbServletRequest.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 07/10/2001

  Autore: Simone Z.

  Note:

  Modifiche:

 */

package net.project.servlet.frame;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import net.project.errors.AppCrash;
import net.project.errors.Logger;
import net.project.mess.LogColloquio;
import net.project.mess.VerifiedMessage;
import net.project.misc.Config;
import net.project.misc.Converter;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Questa classe, che estende SsbServletRequest, permette di trattare una richiesta XML come se fosse una normale
 * richiesta HTTP o un insieme di campi come MsgWriter_itf. Questa classe fa eseguire il parsing dell'XML in ingresso ed
 * e' in grado di restituire i tags richiesti come se fossero appunto singoli campi di una request HTTP. L'assunto
 * generale che viene fatto e' che ogni tag sia univoco all'interno di un messaggio di richiesta e che vi sia la
 * necessita' di recuperare solo i valori dei tag foglia. Tutti questi assunti sono generalmente veri dato che i
 * messaggi li progettiamo noi.
 * <P>
 * Questo fatto ci permette di costruire Function sostanzialmente indipendenti dal formato del messaggio in ingresso
 * (HTTP o XML).
 * <p>
 * <p>
 * Alcuni metodi dell'interfaccia standard sono pero' necessariamente stati soppressi:
 * <p>
 * getAttribute()
 * <p>
 * getAttributeNames()
 * <p>
 * 
 */
public class XMLSsbServletRequest extends SsbServletRequest {

    private String              _XMLCommType;
    private SaxXMLRequestParser _XMLParser;
    private Document            _doc;
    private HttpServletRequest  _request;

    /**
     * Costruttore.
     * 
     * @param req Richiesta HTTP
     * @param XMLCommType Tipo di messaggio XML (POST letto dallo stream direttamente - GET letto da un parametro)
     * @param parser Parser da usare per interpretare il messaggio XML
     */
    public XMLSsbServletRequest(HttpServletRequest req, String XMLCommType) throws AppCrash {

        super(req);
        _XMLCommType = XMLCommType;
        _request = req;
        _XMLParser = new SaxXMLRequestParser();
    }

    /**
     * Questo metodo esegue il parsing della richiesta XML pervenuta in modo da poterne estrarre i vari elementi come
     * fossero parametri ed attributi della richiesta. Questo metodo viene richiamato direttamente dal framework.
     * <p>
     * Viene messo nel thread local di Logger.GetInstance().getInfo() un oggetto di tipo VerifiedMessage che contiene la
     * diagnosi del parser XML. Tale oggetto e' associato alla chiave XML-IN.
     * 
     * @return String la diagnosi del parser XML; null significa tutto OK
     * @throws AppCrash
     */
    public String parseRequest() {

        String diagnosi = null;

        try {

            // 1) request POST include file XML
            // 2) oppure nella request GET/POST come PAR1=VAL1&XML=valueXML...
            InputStream inputStream = null;
            String logEnabled = Config.GetInstance().getProperty("Servlet.logtype", "NONE");

            if (_XMLCommType.equals("POST")) { // 1)
                InputStream requestStream = _request.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int currentByte;
                while (true) {
                    currentByte = requestStream.read();
                    if (currentByte == -1) {
                        break;
                    }
                    baos.write(currentByte);
                }
                requestStream.close();
                baos.flush();
                baos.close();
                byte[] inputStreamBytes = baos.toByteArray();
                if (logEnabled.equalsIgnoreCase("XML")) {
                    String requestAsString = new String(inputStreamBytes, "ISO-8859-1");
                    logRequest(requestAsString);
                }
                inputStream = new ByteArrayInputStream(inputStreamBytes);

            } else { // 2)
                String valueXML = super.getParameter(Config.GetInstance().getProperty("Richiesta.NomeParametroXML"));

                if (logEnabled.equalsIgnoreCase("XML")) {
                    logRequest(valueXML);
                }

                if (!valueXML.equals("")) {
                    String asciiValueXML = Converter.convertOut(valueXML, Converter.ASCII_ENCODING);
                    inputStream = new ByteArrayInputStream(asciiValueXML.getBytes());
                }
            }

            // Parser file XML
            if (inputStream != null) {
                _XMLParser.parse(inputStream);
            } else {
                diagnosi = "Empty XML";
            }

        } catch (IOException e) {
            diagnosi = "IOException " + e.getMessage();
        } catch (AppCrash e) {
            diagnosi = "AppCrash";
        } catch (SAXParseException e) {
            diagnosi = "SaxParse Line=" + e.getLineNumber() + ": " + e.getMessage();
        } catch (SAXException e) {
            diagnosi = "Sax: " + e.getMessage();
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            diagnosi = "Eccezione: " + t.getMessage();
        }

        VerifiedMessage vm = new VerifiedMessage(this, diagnosi);

        Logger.GetInstance().addInfo("XML-IN", vm);

        return diagnosi;
    }

    /**
     * Questo metodo non e' supportato.
     * 
     * @param name the name of the attribute whose value is required
     */

    @Override
    public Object getAttribute(String name) {

        AppCrash ac = new AppCrash("Call getAttribute() non possibile ....");
        ac.logContext("XMLSsbServletRequest", "");
        return "";
    }

    /**
     * Questo metodo non e' supportato.
     */

    @Override
    public Enumeration getAttributeNames() {

        AppCrash ac = new AppCrash("Call getAttributeNames() non possibile ....");
        ac.logContext("XMLSsbServletRequest", "");
        return _request.getAttributeNames();
    }

    /**
     * Returns an input stream for reading binary data in the request body.
     *
     * @see getReader
     * @exception IllegalStateException if getReader has been called on this same request.
     * @exception IOException on other I/O related errors.
     */

    @Override
    public ServletInputStream getInputStream() throws IOException {

        return _request.getInputStream();
    }

    /**
     * Ritorna il valore del parametro o dell'attributo richiesto: prima cerca un attributo con il nome indicato
     * direttamente nella request HTTP; se non lo trova cerca un tag con il nome passato nel messaggio XML.
     *
     * @param name java.lang.String Il nome del parametro richiesto.
     * @return Il valore dell'attributo o del parametro richiesto.
     */

    @Override
    public String getParameter(String name) {

        // Cerca un attributo con il nome indicato
        String value = (String) _request.getAttribute(name);
        if (value != null) {
            return value;
        }
        // Se non trova l'attributo, cerca un parametro
        try {
            value = _XMLParser.getTagValue(name);
            if (value == null) {
                return "";
            }
        } catch (AppCrash ac) {
            Logger.GetInstance().log0("XMLSsbServletRequest- metodo getParameter() - AppCrash su tag: " + name);
            return "";
        }

        return value;
    }

    /**
     * Questo metodo ritorna tutti i nomi dei tags presenti nel messaggio XML in ingresso
     */

    @Override
    public Enumeration getParameterNames() {

        try {
            Enumeration e = _XMLParser.getTags();
            return e;
        } catch (AppCrash ac) {
            Logger.GetInstance().log0("XMLSsbServletRequest - Exception in metodo getParameterNames");
            return null;
        }
    }

    /**
     * Returns the values of the specified parameter for the request as an array of strings, or null if the named
     * parameter does not exist. For example, in an HTTP servlet this method would return the values of the specified
     * query string or posted form as an array of strings.
     *
     * @param name the name of the parameter whose value is required.
     * @see javax.servlet.ServletRequest#getParameter
     */

    @Override
    public String[] getParameterValues(String name) {

        AppCrash ac = new AppCrash(
                "Metodo getParameterValues(String name) non supportato nella classe XMLSsbServletRequest, futuri sviluppi");
        return null;

    }

    /**
     * Returns a buffered reader for reading text in the request body. This translates character set encodings as
     * appropriate.
     *
     * @see getInputStream
     *
     * @exception UnsupportedEncodingException if the character set encoding is unsupported, so the text can't be
     *                correctly decoded.
     * @exception IllegalStateException if getInputStream has been called on this same request.
     * @exception IOException on other I/O related errors.
     */

    @Override
    public BufferedReader getReader() throws IOException {

        return _request.getReader();
    }

    /**
     * Questo metodo servirebbe per recuperare il messaggio che e' stato composto.<b> Viene sempre restituito null</b>
     *
     * @return byte[] byte array contenente il messaggio
     * @exception net.project.errors.AppCrash.
     */
    @Override
    public byte[] getMessage() throws AppCrash {

        return null;
    }

    /**
     * Ritorna il valore del campo corrispondente al nome passato.
     * 
     * @param nome java.lang.String nome del campo.
     * @return java.lang.String Il valore del campo oppure "" se il campo non esiste.
     */
    @Override
    public String getField(String nome) {

        String field = getParameter(nome);

        return field;
    }

    /**
     * Ritorna il protocollo di comunicazione.
     * 
     * @return String
     */
    public String getType(String functionField) {

        return _request.getProtocol();
    }

    private void logRequest(String valueXML) {

        LogColloquio log = LogColloquio.makeLogColloquio(LogColloquio.RQ_HTTP_ENTRANTE);
        log.setProtocollo(LogColloquio.MESSAGGI_XML);
        log.setTipoMsg("XML-IN");

        try {
            log.setSorgente(getRemoteAddr());

            String nomeProg = Config.GetInstance().getProperty("Logger.ProgramName");
            log.setDestinazione(nomeProg);
            if (!valueXML.equals("")) {
                log.setRichiesta(valueXML);
            } else {
                log.setRichiesta(toString());
            }

        } catch (Throwable e) {
            log.setRichiesta("Errore nella creazione di LogColloquio XML");
        }

        Logger.GetInstance().addInfo(LogColloquio.RQ_HTTP_ENTRANTE, log);

    }

}
