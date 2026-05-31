/*
  XMLServletApplication_base.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 04/10/2001

  Autore: Assunta C.

  Note:

  Modifiche:

 */

package net.project.servlet.frame;

import javax.servlet.ServletConfig;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.project.errors.AppCrash;
import net.project.errors.Logger;
import net.project.misc.Config;

/**
 * E' la classe che rappresenta la servlet application per la gestione XML. Essa dovrebbe essere in grado di
 * visualizzare la Home page della applicazione con il metodo GET. Il metodo POST gestisce start e stop della app. ed
 * eventualmente di ogni singola funzione, display dello status, display dei servlet partecipanti e della
 * configurazione.
 */
public abstract class XMLServletApplication_base extends ServletApplication_base {

    private String _XMLCommType;

    /**
     * Costruttore.
     */
    public XMLServletApplication_base() {

    }

    /**
     * Esegue la GET
     * 
     * @param req HttpServletRequest che incapsula la richiesta al servlet.
     * @param res HttpServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     * @exception java.lang.Throwable.
     * @return void
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) {

        SsbServletResponse ssbRes = createSsbServletResponse(res);

        try {
            XMLSsbServletRequest ssbReq = (XMLSsbServletRequest) createSsbServletRequest(req);

            String dia = ssbReq.parseRequest();

            if (dia != null) {
                parseFailed(dia, req, ssbRes);
                return;
            }
            // Controllo applicazione attiva
            if (getStatus() == 0) {
                functionNotFound(ssbReq, ssbRes);
                return;
            }

            if (_isTickNeeded) {
                synchronized (ServletApplication_base.class) {
                    if (_isTickNeeded) {
                        _isTickNeeded = false;
                        tick();
                    }
                }
            }

            Function_itf function = getFunction(ssbReq);
            if (function == null) {
                processGet(ssbReq, ssbRes);
            } else {
                function.doGet(ssbReq, ssbRes);
            }
        } catch (AppCrash ap) {
            invokeDisplayError(ap, req, ssbRes);
            ap.logContext("ServletApplication_base", "Errore nella doPost dell'applicazione ");
        } catch (Throwable err) {
            invokeDisplayError(err, req, ssbRes);
            err.printStackTrace();
            AppCrash ex = new AppCrash(err);
            ex.logContext("ServletApplication_base", "Errore nella doPost dell'applicazione ");
        } finally {
            ssbRes.flushBuffer();
            Logger.GetInstance().dumpInfo();
            Logger.GetInstance().resetInfo();
            freeDBConnections();
            Logger.GetInstance().flush();
            Logger.GetInstance(getConfigName()).flush();
        }

    }

    /**
     * Esegue la POST
     * 
     * @param req HttpServletRequest che incapsula la richiesta al servlet.
     * @param res HttpServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     * @exception java.lang.Throwable.
     * @return void
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) {

        SsbServletResponse ssbRes = createSsbServletResponse(res);

        try {
            XMLSsbServletRequest ssbReq = (XMLSsbServletRequest) createSsbServletRequest(req);

            String dia = ssbReq.parseRequest();

            if (dia != null) {
                parseFailed(dia, req, ssbRes);
                return;
            }

            // Controllo applicazione attiva
            if (getStatus() == 0) {
                functionNotFound(ssbReq, ssbRes);
                return;
            }

            if (_isTickNeeded) {
                synchronized (ServletApplication_base.class) {
                    if (_isTickNeeded) {
                        _isTickNeeded = false;
                        tick();
                    }
                }
            }

            Function_itf function = getFunction(ssbReq);
            if (function == null) {
                processPost(ssbReq, ssbRes);
            } else {
                function.doPost(ssbReq, ssbRes);
            }
        } catch (AppCrash ap) {
            invokeDisplayError(ap, req, ssbRes);
            ap.logContext("ServletApplication_base", "Errore nella doPost dell'applicazione ");
        } catch (Throwable err) {
            invokeDisplayError(err, req, ssbRes);
            err.printStackTrace();
            AppCrash ex = new AppCrash(err);
            ex.logContext("ServletApplication_base", "Errore nella doPost dell'applicazione ");
        } finally {
            ssbRes.flushBuffer();
            Logger.GetInstance().dumpInfo();
            Logger.GetInstance().resetInfo();
            freeDBConnections();
            Logger.GetInstance().flush();
            Logger.GetInstance(getConfigName()).flush();
        }

    }

    /**
     * Crea la SsbServletRequest.
     * 
     * @param req javax.servlet.http.HttpServletRequest l'oggetto richiesta attorno al quale creare la SsbServletRequest
     * @return net.project.frame.SsbServletRequest l'oggetto creato.
     * @exception net.project.errors.AppCrash
     */
    @Override
    protected SsbServletRequest createSsbServletRequest(HttpServletRequest req) throws AppCrash {

        return createSsbServletRequest(req, _XMLCommType);
    }

    /**
     * Crea la SsbServletRequest.
     * 
     * @param req javax.servlet.http.HttpServletRequest l'oggetto richiesta attorno al quale creare la SsbServletRequest
     * @param xmlCommType java.lang.String la modalita' di trasmissione dell'xml (puo' valere GET o POST)
     * @return net.project.frame.SsbServletRequest l'oggetto creato.
     * @exception net.project.errors.AppCrash
     */
    protected SsbServletRequest createSsbServletRequest(HttpServletRequest req, String xmlCommType) throws AppCrash {

        return new XMLSsbServletRequest(req, _XMLCommType);
    }

    /**
     * Esegue l'operazione get http.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     * @return void
     */
    @Override
    public void processGet(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        functionNotFound(req, res);
    }

    /**
     * Esegue l'operazione get http.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     * @return void
     */
    @Override
    public void processPost(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

        super.processPost(req, res);

        if (!req.getField("APPLICATIONCOMMANDEXECUTED").equals("")) return;

        functionNotFound(req, res);
    }

    @Override
    public void init(ServletConfig config) throws UnavailableException {

        try {
            super.init(config);
            _XMLCommType = Config.GetInstance(getConfigName()).getProperty("Servlet.XMLcommtype");
        } catch (Exception e) {
            throw new UnavailableException(this, "Errore in init(ServletConfig config) ");
        }

    }

    /**
     * Metodo per richiamare la visualizzazione del messaggio di errore in caso di eccezioni nel framework.
     */
    protected void parseFailed(String diagnosi, HttpServletRequest req, HttpServletResponse res) {

        try {
            displayError(new Exception(diagnosi), req, res);
        } catch (Throwable err) {
            AppCrash ex = new AppCrash(err);
        }
    }

    /**
     * Questo metodo viene richiamato quando la funzione richiesta non e' stata trovata
     */
    public abstract void functionNotFound(SsbServletRequest req, SsbServletResponse res) throws AppCrash;

}
