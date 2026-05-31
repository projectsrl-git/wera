/*

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore: Simone Z.

  Note:

 */

package net.project.mess;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;

import net.project.errors.AppCrash;
import net.project.errors.ErrorConfig;
import net.project.errors.Logger;
import net.project.errors.StorableLog_itf;

/**
 * Questa classe rappresenta il log di un generico colloquio request/response che una applicazione sostiene con
 * l'esterno. Le informazioni catturate riguardano il messaggio entrante e quello uscente. Le classi della libreria dei
 * package servlet e comm sono in grado di creare oggetti di questo tipo (o della sottoclasse indicata) automaticamente
 * e di popolarli con le informazioni qui riportate. Questi oggetti sono conservati in memoria nel threadlocal definito
 * dalla classe InfoCollection ed accedibile tramite Logger.GetInstance().getInfo().
 * <P>
 * Attualmente sono definiti 4 diversi tipi di colloqui che possono essere contemporaneamente presenti per un thread:
 * <P>
 * <P>
 * RQ_HTTP_ENTRANTE la richiesta proviene dall'esterno in HTTP (XML compreso)
 * <p>
 * RQ_HTTP_USCENTE la nostra applicazione esegue la richiesta in HTTP verso l'esterno
 * <p>
 * RQ_TCP_ENTRANTE la richiesta proviene dall'esterno tramite il package comm
 * <p>
 * RQ_TCP_USCENTE la nostra applicazione esegue la richiesta verso l'esterno tramite il package comm
 * <p>
 * <P>
 * Queste costanti servono come chiavi per accedere agli oggetti di tipo LogColloquio nella hashtable restiruita da in
 * Logger.GetInstance().getInfo().
 * <P>
 * Tipicamente ogni applicazione definira' una sottoclasse di LogColloquio in grado di contenere le informazioni
 * applicative utilizzate poi come chiavi di ricerca ed il meccanismo di memorizzazione.
 * <P>
 * Dato che LogColloquio implementa StorableLog_itf ogni volta che viene chiamato il metodo Logger.GetInstance().flush()
 * il logger richiamera' il metodo store() di tutti gli oggetti LogColloquio presenti nell'InfoCollection del logger per
 * il thread corrente.
 * <P>
 * <P>
 * Per abilitare la creazione degli oggetti logcolloquio da parte del package servlet e' necessario definire nel file di
 * configurazione la proprieta' <b>Servlet.logtype<b> con il valore HTTP o XML a seconda del caso.
 * <P>
 * Per abilitare i logcolloquio del package comm in uscita tramite i Client_itf sul file di configurazione deve essere
 * aggiunta la proprieta' servizio.+ service + .logflag=true per il servizio da abilitare.
 * <P>
 * Per abiltare i logcolloquio dei messaggi TCP entranti tramite ServerPeer_itf bisogna aggiungere al file di
 * configurazione la proprieta' ServerPeer. +nome+ .logflag=true per il server peer di interesse
 *
 * @author Simone
 */
public class LogColloquio implements StorableLog_itf {

    // Costanti per la definizione dei tipi di colloquio: servono come chiavi per memorizzare
    // gli oggetti di tipo LogColloquio in Logger.Info
    static public final String RQ_HTTP_ENTRANTE = "1";
    static public final String RQ_HTTP_USCENTE  = "2";
    static public final String RQ_TCP_ENTRANTE  = "3";
    static public final String RQ_TCP_USCENTE   = "4";

    // Costanti per definire i valori validi per il campo Protocollo
    static public final String MESSAGGI_HTTP    = "HTTP";
    static public final String MESSAGGI_XML     = "XML";
    static public final String MESSAGGI_SOCKET  = "TCP";
    static private HashMap     _ConstructorSet  = new HashMap();
    private String             _protocollo;
    private String             _sorgente;
    private String             _destinazione;
    private Date               _tsRichiesta;
    private Date               _tsRisposta;
    private String             _richiesta;
    private String             _risposta;
    private String             _tipoColloquio;
    private String             _appMsgId;
    private String             _tipoMsg;

    // Oggetto originale della richiesta
    private Object             _requestObject;

    /**
     * Questo metodo costruisce un oggetto di tipo LogColloquio in base al tipo di colloquio indicato. La classe da
     * istanziare viene letta dalla proprieta' di configurazione <code> Logger.ColloquioClass.{TIPO_COLLOQUIO}<code><p>.
     * I valori assumibili da TIPO_COLLOQUIO sono:<p>
     * RQ_HTTP_ENTRANTE<p>
     * RQ_HTTP_USCENTE<p>
     * RQ_TCP_ENTRANTE<p>
     * RQ_TCP_USCENTE<p>
     * <p>
     * Se la proprieta' di configurazione non e' presente viene istanziato un oggetto di
     * tipo LogColloquio
     *
     * @param tipoColloquio il tipo di colloquio per il quale creare l'oggetto
     *
     * @return un oggetto LogColloquio o una sua sottoclasse
     */
    public static LogColloquio makeLogColloquio(String tipoColloquio) {

        Constructor logConstructor = (Constructor) _ConstructorSet.get(tipoColloquio);

        if (logConstructor == null) {
            String logColloquioClass = null;

            try {
                logColloquioClass = ErrorConfig.GetInstance().getProperty("Logger.ColloquioClass." + tipoColloquio,
                        "net.project.mess.LogColloquio");

                Class tempClass = Class.forName(logColloquioClass);
                logConstructor = tempClass.getConstructor(new Class[] { String.class });
                _ConstructorSet.put(tipoColloquio, logConstructor);
            } catch (Throwable t) {
                AppCrash ac = new AppCrash(t);
                ac.logContext("LogColloquio", "Classe: " + logColloquioClass);
            }
        }

        if (logConstructor == null) {
            return new LogColloquio(tipoColloquio);
        }

        LogColloquio log = null;

        try {
            log = (LogColloquio) logConstructor.newInstance(new Object[] { tipoColloquio });
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            log = new LogColloquio(tipoColloquio);
        }

        return log;
    }

    /**
     * Creates a new LogColloquio object.
     *
     * @param tipoColloquio DOCUMENT ME!
     */
    public LogColloquio(String tipoColloquio) {

        _tipoColloquio = tipoColloquio;
        _tsRichiesta = new Date();
    }

    /**
     * Ritorna appMsgId. Questo attributo e' l'application message identifier. Dovrebbe essere in grado di identificare
     * univocamente il messaggio applicativo inviato o ricevuto. La libreria <b>NON<b> valorizza questo campo per le
     * richieste entranti, ma si aspetta che venga valorizzato dalla applicazione. Quando viene emessa una richiesta TCP
     * se e' presente nel threadlocal una richiesta HTTP entrante o una richiesta TCP entrante la libreria preleva da
     * essa il campo appMsgId e lo imposta nel logcolloquio della richiesta TCP uscente. Questo permette di legare i
     * vari colloqui avvenuti in una singola transazione.
     *
     * @return String
     */
    public String getAppMsgId() {

        return _appMsgId;
    }

    /**
     * Ritorna il valore dell'attributo _destinazione. Per le richieste HTTP (anche XML) entranti viene valorizzato con
     * il contenuto della proprieta' di config <b>Logger.ProgramName<b>. Per le richieste TCP entranti tramite
     * ServerPeer_itf viene definito con il service name del server peer che accetta la richiesta.
     * <P>
     * Per le richieste TCP uscenti viene valorizzato con il valore del service del Client_itf che sta eseguendo la
     * trasmissione
     *
     * @return String
     */
    public String getDestinazione() {

        return _destinazione;
    }

    /**
     * Ritorna il protocollo applicativo del colloquio.Attualmente sono definiti tre tipo di protocolli applicativi:
     * <P>
     * HTTP per messaggi in HTTP-HTTPS semplici
     * <P>
     * XML per messaggi in HTTP-HTTPS che contengono tutta la richiesta in un campo in XML
     * <P>
     * TCP per messaggi scambiati tramite il package comm (via socket CICS etc)
     * <P>
     * Questo attributo viene settato dalla libreria
     *
     * @return String
     */
    public String getProtocollo() {

        return _protocollo;
    }

    /**
     * Returns the richiesta.
     *
     * @return String
     */
    public String getRichiesta() {

        return _richiesta;
    }

    /**
     * Returns the risposta.
     *
     * @return String
     */
    public String getRisposta() {

        return _risposta;
    }

    /**
     * Ritorna la sorgente. La sorgente e' impostata con l'indirizzo IP del chiamante per le richieste HTTP (e quindi
     * XML) entranti. Per le richieste TCP entranti viene definito a ?? . Per le richieste TCP uscenti viene impostato
     * al valore della proprieta' di configurazione <b>Logger.ProgramName<b>
     *
     * @return String il valore dell'attributo sorgente
     */
    public String getSorgente() {

        return _sorgente;
    }

    /**
     * Returns the tsRichiesta. E' il timestamp di creazione dell'oggetto logcolloquio da parte della libreria di basso
     * livello
     *
     * @return String
     */
    public Date getTsRichiesta() {

        return _tsRichiesta;
    }

    /**
     * Returns the tsRisposta. E' il timestamp di generazione(ricezione) della risposta verso il richiedente come
     * catturato dalla libreria di basso livello
     *
     * @return String
     */
    public Date getTsRisposta() {

        return _tsRisposta;
    }

    /**
     * Sets the appMsgId.
     *
     * @param appMsgId The appMsgId to set
     */
    public void setAppMsgId(String appMsgId) {

        _appMsgId = appMsgId;
    }

    /**
     * Sets the destinazione.
     *
     * @param destinazione The destinazione to set
     */
    public void setDestinazione(String destinazione) {

        _destinazione = destinazione;
    }

    /**
     * Sets the protocollo. Questo metodo e' utilizzato dalla libreria di basso livello
     *
     * @param protocollo The protocollo to set
     */
    public void setProtocollo(String protocollo) {

        _protocollo = protocollo;
    }

    /**
     * Sets the richiesta.
     *
     * @param richiesta The richiesta to set
     */
    public void setRichiesta(String richiesta) {

        _richiesta = richiesta;
    }

    /**
     * Sets the risposta.
     *
     * @param risposta The risposta to set
     */
    public void setRisposta(String risposta) {

        _risposta = risposta;
        _tsRisposta = new Date();
    }

    /**
     * Sets the sorgente.
     *
     * @param sorgente The sorgente to set
     */
    public void setSorgente(String sorgente) {

        _sorgente = sorgente;
    }

    /**
     * Sets the requestObject.
     *
     * @param request The requestObject to set
     */
    public void setRequestObject(Object request) {

        _requestObject = request;
    }

    /**
     * Sets the requestObject.
     *
     *
     * @return DOCUMENT ME!
     */
    public Object getRequestObject() {

        return _requestObject;
    }

    /**
     * Ritorna la rappresentazione XML dell'oggetto colloquio
     * 
     * @return DOCUMENT ME!
     */
    public String toXML() {

        return "XML";
    }

    /**
     * Questo metodo serve per memorizzare il presente log del colloquio. Le sottoclassi devono ridefinirlo in modo da
     * effettuare le operazioni opportune. Il framework lo invoca su tutti gli oggetti LogColloquio presenti in
     * Logger.Info.
     * <P>
     */
    @Override
    public void store() {

    }

    /**
     * Ritorna tipoMsg.
     * 
     * @return String
     */
    public String getTipoMsg() {

        return _tipoMsg;
    }

    /**
     * Setta tipoMsg. La libreria di basso livello non chiama questo metodo
     * 
     * @param tipoMsg The tipoMsg to set
     */
    public void setTipoMsg(String tipoMsg) {

        _tipoMsg = tipoMsg;
    }

    /**
     * Questo metodo ritorna il VerifiedMessage presente nella Logger.Info corrispondente al tipo passato. Come tipo si
     * deve indicare il tipo della LogicalMessageSpec che viene usata per verificare il messaggio
     *
     */
    protected VerifiedMessage getVerifiedMessage(String tipo) {

        if (tipo != null) {
            return (VerifiedMessage) Logger.GetInstance().getInfo().get(tipo);
        }
        return null;

    }

    /**
     * Ritorna il tipo di colloquio.
     * 
     * @return String
     */
    public String getTipoColloquio() {

        return _tipoColloquio;
    }

}
