/*
  SsbServletResponse.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 30/03/2001

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.servlet.frame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import net.project.errors.AppCrash;
import net.project.errors.Logger;
import net.project.mess.LogColloquio;
import net.project.misc.Config;

/**
 * Questa classe e' un wrapper dell'interfaccia HttpServletResponse. E' stata introdotta per simmetria rispetto alla
 * SsbServletRequest. Una particolarita' e' che vengono aggiunte agli header HTTP le direttive no cache per impedire a
 * proxy e browser di memorizzare il contenuto delle pagine emesse dal framework.
 * <p>
 * La classe e' anche in grado di provvedere alle esigenze di logging del framework popolando opportunamente l'oggetto
 * LogColloquio. Attraverso il meccanismo di bufferizzazione e' altresi' in grado di generare l'header HTTP
 * Content-Length necessario sulle connessioni HTTP 1.1 per la lettura delle response da parte dei client.
 * <p>
 * In caso di logging abilitato viene anche automaticamente generato il Content-length. Per stabilire se il logging e'
 * abilitto viene letta la proprieta' <code>Servlet.logtype</code>. Se il valore e' diverso da NONE il logging e'
 * abilitato. Il tutto e' valido solo se si utilizza il metodo getWriter(), e non getOutputStream(), per generare
 * l'output.
 * 
 */
public class SsbServletResponse extends HttpServletResponseWrapper {

    protected HttpServletResponse   _response;
    protected SsbServletRequest     _request;
    protected boolean               _genContentLen = false;
    protected boolean               _logging       = false;

    protected ByteArrayOutputStream _bao           = null;
    protected PrintWriter           _localPW       = null;
    protected boolean               _flushed       = false;
    protected int                   _bufferSize    = 3500;

    /**
     * Costruttore. Aggiunge le direttive no cache agli header HTTP emessi. Legge la proprieta'
     * <code>Servlet.logtype</code> per stabilire se l'output deve essere loggato e la proprieta'
     * <code>Servlet.buffersize</code> per stabilire la dimensione iniziale del ByteArrayOutputStream che verra' usato
     * per bufferizzare l'output prodotto.
     * <p>
     * La bufferizzazione serve sia per il logging sia per la generazione dell header HTTP Content-Length
     * 
     */
    public SsbServletResponse(HttpServletResponse res) {

        super(res);

        _response = res;
        String logType = Config.GetInstance().getProperty("Servlet.logtype", "NONE");
        if (!logType.equalsIgnoreCase("NONE") && !(res instanceof SsbServletResponse)) {
            setLogging(true);
            String bufSize = Config.GetInstance().getProperty("Servlet.buffersize");
            if (bufSize != null) {
                _bufferSize = Integer.parseInt(bufSize);
            }
        }
        // Se sto riwrappando una SsbServletResponse "copio" la request originale
        if (res instanceof SsbServletResponse) {
            this.setRequest(((SsbServletResponse) res).getRequest());
        }

        setHeader("Cache-Control", "no-cache");
        setIntHeader("Expires", -1);
    }

    /**
     * @return Ritorna il campo response originale.
     */
    public HttpServletResponse getOriginalResponse() {

        return _response;
    }

    /**
     * Returns a print writer for writing formatted text responses. The MIME type of the response will be modified, if
     * necessary, to reflect the character encoding used, through the <em>charset=...</em> property. This means that the
     * content type must be set before calling this method.
     * 
     * @see getOutputStream
     * @see setContentType
     * 
     * @exception UnsupportedEncodingException if no such encoding can be provided
     * @exception IllegalStateException if getOutputStream has been called on this same request.
     * @exception IOException on other errors.
     */

    public PrintWriter getWriter() throws IOException {

        // Se devo generare Content-length o eseguire il log dell'output faccio scrivere
        // l'output su un byte array
        if (_genContentLen || _logging) {

            // Se avevo gia' consegnato un Writer lo chiudo
            if (_localPW != null) {
                try {
                    _localPW.close();
                    _bao.close();
                } catch (Throwable t) {
                }
            }

            _bao = new ByteArrayOutputStream(_bufferSize);
            _localPW = new PrintWriter(_bao);
            return _localPW;
        }

        return _response.getWriter();

    }

    /**
     * Forces any content in the buffer to be written to the client. A call to this method automatically commits the
     * response, meaning the status code and headers will be written.
     * <p>
     * La chiamata a questo metodo provoca la scrittura dell'header Content-Length ed il logging della risposta fin qui
     * prodotta nell'oggetto LogColloquio corrispondente a RQ_HTTP_ENTRANTE (se i flag sono a true). Dopo aver chiamato
     * questo metodo con logging abilitato o con Content-length generation abilitato non e' piu' possibile eseguire
     * alcun output.
     * 
     * @see #setBufferSize
     * @see #getBufferSize
     * @see #isCommitted
     * @see #reset
     * 
     */

    public void flushBuffer() {

        String output = null;

        // Se buffering locale abilitato e ho gia' eseguito flushBuffer() allora non posso piu'
        // fare nulla, percio' return immediato.
        if ((_genContentLen || _logging) && _flushed) {
            return;
        }

        try {
            if ((_genContentLen || _logging) && _localPW != null) {
                _localPW.flush();
                _localPW.close();

                output = _bao.toString();
                _bao.close();

                setContentLength(output.length());
                _response.getWriter().write(output);
            }

            try {
                _response.flushBuffer();
            } catch (IOException t) {
                // Non ho nulla da fare se non sono riuscito a fare flush
            }

            _flushed = true;

        } catch (IOException e) {
            new AppCrash(e);
            output = "IOError " + output;
        }

        if (_logging) {
            LogColloquio log = null;
            Map<String, Object> info = Logger.GetInstance().getInfo();

            if (info != null) {
                log = (LogColloquio) info.get(LogColloquio.RQ_HTTP_ENTRANTE);
                if (log != null) log.setRisposta(output);
            }

        }
    }

    /**
     * Questo metodo permette di impostare il flag di generazione dell'header Content-Length. Di default non viene
     * generato. La generazione di tale header comporta la bufferizzazione di tutto l'output prodotto prima dell'invio
     * al client. Deve essere utilizzata solo in caso l'output non sia di dimensioni eccessive.
     * 
     * @param genContentLen flag boolean
     */
    public void setContentLenGeneration(boolean genContentLen) {

        _genContentLen = genContentLen;
    }

    /**
     * Questo metodo permette di settare il flag di logging abilitato. Quando impostato a true fa si che tutto quello
     * che viene prodotto dal servlet viene bufferizzato e registrato nell'oggetto LogColloquio opportuno. Di default
     * non viene loggato nulla.
     * 
     * @param logging flag boolean
     */
    public void setLogging(boolean logging) {

        _logging = logging;

    }

    /**
     * @return Ritorna il campo request.
     */
    public SsbServletRequest getRequest() {

        return _request;
    }

    /**
     * Questo metodo permette di impostare il campo request
     * 
     * @param request SsbServletRequest la request da impostare.
     */
    public void setRequest(SsbServletRequest request) {

        _request = request;
    }

}
