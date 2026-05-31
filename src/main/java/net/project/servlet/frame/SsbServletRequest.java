/*
  SsbServletRequest.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 30/03/2001

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.servlet.frame;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.errors.ParamCrash;
import net.project.mess.LogColloquio;
import net.project.mess.MsgWriter_itf;
import net.project.misc.Config;
import net.project.misc.Converter;

/**
 * Questa classe e' un wrapper dell'interfaccia HttpServletRequest. E' stata introdotta per aumentare l'interfaccia
 * standard con i metodi dell'interfaccia MsgWriter_itf propria del framework. Questo permette, ad esempio, di
 * effettuare la validazione dei parametri in ingresso ad una Function tramite il meccanismo del Verifier ed annesso
 * vocabolario.
 * <P>
 * 
 */

public class SsbServletRequest extends HttpServletRequestWrapper implements MsgWriter_itf {

    /**
     * InetAddress to make the UID globally unique
     */
    private static String         _IPaddress;

    /**
     * a random number
     */
    private static String         _AppUnique;

    /**
     * Usato per la sincronizzazione
     */
    private static Object         _Mutex;

    private static long           _LastTime;
    private static long           _DELAY;

    /**
     * Catena dei certificati client
     */
    private X509Certificate[]     _certChain    = null;

    // Headers comunemente inseriti dai proxy per mandare indirizzo IP del client
    private static final String[] _headersProxy = { "X-Remote-Addr", "X-Forwarded-For", "Forwarded", "X-Client-IP",
            "Client-IP"                        };

    static {
        _AppUnique = Integer.toString(Math.abs((new Object()).hashCode()), 16) + "000000000";
        _AppUnique = _AppUnique.substring(0, 8);
        _Mutex = new Object();
        _LastTime = System.currentTimeMillis();
        _DELAY = 10; // in milliseconds
        try {
            BigInteger ip = new BigInteger(InetAddress.getLocalHost().getAddress());
            ip = ip.abs();
            _IPaddress = ip.toString(16) + "000000000";
            _IPaddress = _IPaddress.substring(0, 8);
        } catch (UnknownHostException ex) {
            _IPaddress = _AppUnique;
        }
    }

    private HttpServletRequest    _request;

    /**
     * Costruttore.
     */
    public SsbServletRequest(HttpServletRequest req) {

        super(req);

        _request = req;
        String logEnabled = Config.GetInstance().getProperty("Servlet.logtype", "NONE");
        if (logEnabled.equalsIgnoreCase("HTTP")) {
            logRequest();
        }
        setCertChain(req);
    }

    /**
     * Recupera dalla request il certificato client.
     */
    private void setCertChain(HttpServletRequest req) {

        String cipherSuite = (String) req.getAttribute("javax.net.ssl.cipher_suite");
        if (cipherSuite != null) {
            _certChain = (X509Certificate[]) req.getAttribute("javax.net.ssl.peer_certificates");
        }
        if (_certChain != null) return;

        // Da Servlet spec 2.2 in poi sono cambiati gli attributi. Tomcat non supporta quelli vecchi
        cipherSuite = (String) req.getAttribute("javax.servlet.request.cipher_suite");
        if (cipherSuite != null) {
            _certChain = (X509Certificate[]) req.getAttribute("javax.servlet.request.X509Certificate");
        }
        if (_certChain != null) return;

        // Cerco Header HTTP SSL_Client_Cert inserito dai Web Application Firewall
        String sslCertHeader = req.getHeader("SSL_Client_Cert");

        if (sslCertHeader != null && sslCertHeader.trim().startsWith("-----BEGIN")) {
            // mod_headers converte i '\n' in spazi ' ' percio' bisogna ricostruire il certificato
            String strcert1 = sslCertHeader.replace(' ', '\n');
            String strcert2 = strcert1.substring(28, strcert1.length() - 26);
            String strcert3 = new String("-----BEGIN CERTIFICATE-----\n");
            String strcert4 = strcert3.concat(strcert2);
            sslCertHeader = strcert4.concat("\n-----END CERTIFICATE-----\n");

            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                ByteArrayInputStream bais = new ByteArrayInputStream(sslCertHeader.getBytes());
                _certChain = new X509Certificate[1];
                _certChain[0] = (X509Certificate) cf.generateCertificate(bais);
            } catch (CertificateException e) {
                AppCrash ac = new AppCrash(e);
                ac.logContext("", "Header SSL_Client_Cert: " + sslCertHeader);
            }

        }
    }

    public X509Certificate[] getCertChain() {

        return _certChain;
    }

    /**
     * Ritorna il distinguish name del certificato
     */
    public String getDistinguishName() {

        if (_certChain != null) {
            return _certChain[0].getSubjectDN().getName();
        }
        return null;
    }

    /**
     * Ritorna l'informazione richiesta dal DN del certificato.
     */
    private String getInfoFromDN(String stringInfo) {

        String clientDN = getDistinguishName();
        if (clientDN == null) {
            return null;
        }

        if (checkStringInDN(clientDN, stringInfo) == false) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(clientDN, ",");
        String token;
        while (st.hasMoreTokens()) {
            token = st.nextToken();
            if (token.indexOf(stringInfo) != -1) {
                return token.substring(stringInfo.length()).trim();
            }
        }
        return null;
    }

    /**
     * Ritorna l'abi associato al certificato.
     */
    public String getAbiFromCertificate() throws AppCrash {

        String stringAbi = Config.GetInstance().getProperty("Certificate.DN.abi");
        ErrDetector.GetInstance().param(stringAbi);
        return getInfoFromDN(stringAbi);
    }

    /**
     * Ritorna il nome del servizio per cui è stato richiesto il certificato.
     */
    public String getServiceFromCertificate() throws AppCrash {

        String stringServizio = Config.GetInstance().getProperty("Certificate.DN.servizio");
        ErrDetector.GetInstance().param(stringServizio);
        return getInfoFromDN(stringServizio);
    }

    /**
     * Ritorna il common name CN associato al certificato.
     */
    public String getCommonNameFromCertificate() throws AppCrash {

        String stringCN = Config.GetInstance().getProperty("Certificate.DN.CN");
        ErrDetector.GetInstance().param(stringCN);
        return getInfoFromDN(stringCN);
    }

    /**
     * Controlla la presenza di una data stringa nel DN del certificato.
     */
    private boolean checkStringInDN(String DN, String stringToCheck) {

        int posizione = DN.indexOf(stringToCheck);
        if (posizione == -1) {
            Logger.GetInstance().log0(
                    "SsbServletRequest - La stringa '" + stringToCheck
                            + "' non e' presente nel DN del certificato. DN=" + DN);
            return false;
        }
        return true;
    }

    /**
     * Ritorna il valore del parametro o dell'attributo.
     * 
     * @param name java.lang.String Il nome del parametro richiesto.
     * @return Il valore dell'attributo o del parametro richiesto.
     */

    public String getParameter(String name) {

        // Cerca un attributo con il nome indicato
        String value = null;
        try {
            value = (String) _request.getAttribute(name);
        } catch (ClassCastException cce) {
        }

        if (value != null) {
            return value;
        }
        // Se non trova l'attributo, cerca un parametro
        value = _request.getParameter(name);
        if (value == null) {
            return "";
        }
        return value;
    }

    /**
     * Returns the IP address of the agent that sent the request. Same as the CGI variable REMOTE_ADDR. Questo metodo
     * prova a determinare l'indirizzo IP del cliente verificando la presenza degli header comunemente inseriti dai
     * proxy (WAF ed Apache compresi. Se non ne trova di validi ritorna quanto restituito da getRemoteAddr
     */

    public String getRemoteAddr() {

        String ip = null;

        // Il metodo getHeader e' case insensitive
        for (int k = 0; k < _headersProxy.length; k++) {
            ip = getHeader(_headersProxy[k]);
            if (ip != null && Character.isDigit(ip.charAt(0))) {
                ip = ip.trim();
                String ips[] = ip.split(",");
                if (ips[0].length() < 16) {
                    return ips[0];
                }
            }
        }

        return _request.getRemoteAddr();
    }

    /**
     * 
     * Returns a boolean indicating whether this request was made using a secure channel, such as HTTPS.
     * 
     * 
     * @return a boolean indicating if the request was made using a secure channel
     * 
     */
    public boolean isSecure() {

        if (_request.isSecure()) return true;

        String sslCertHeader = _request.getHeader("SSL_Client_Cert");
        if (sslCertHeader != null && sslCertHeader.trim().startsWith("-----BEGIN")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Ritorna il campo corrispondente a nome.
     * 
     * @param nome java.lang.String nome del campo.
     * @return java.lang.String Il valore del campo oppure "" se il campo non esiste.
     */
    public String getField(String nome) {

        return this.getParameter(nome);
    }

    /**
     * Ritorna il campo corrispondente a nome.
     * 
     * @param nome java.lang.String nome del campo.
     * @return java.lang.String Il valore del campo oppure "" se il campo non esiste.
     */
    public String getFieldSqlUnEscaped(String nome) {

        try {
            return Converter.sqlUnEscape(this.getParameter(nome));
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Ritorna il tipo di messaggio.
     * 
     * @return String
     */
    public String getType() {

        return _request.getProtocol();
    }

    /**
     * Questo metodo serve per impostare il valore di un campo del messaggio
     * 
     * @param java.lang.String name nome del campo.
     * @param java.lang.String value valore del campo.
     * @exception net.ssb.errors.AppCrash.
     */
    public void setField(String name, String value) throws AppCrash {

        try {
            ErrDetector.GetInstance().param(name);
            ErrDetector.GetInstance().param(value);
        } catch (ParamCrash ex) {
            ex.logContext("SsbServletRequest", "Name = " + name + " Value = " + value);
            throw (ex);
        }
        _request.setAttribute(name, value);
    }

    /**
     * Questo metodo serve per recuperare il messaggio che e' stato composto.
     * 
     * @return byte[] byte array contenente il messaggio
     * @exception net.ssb.errors.AppCrash.
     */
    public byte[] getMessage() throws AppCrash {

        return null;
    }

    /**
     * Questo metodo restituisce una Map con tutti i parametri della request.
     * <p>
     * La Map e' ottenuta richiamando getParameter() con i nomi dei parametri ottenuti dalla getParameterNames() in modo
     * da restituire una Map fedele, ovvero che ricomprenda eventuali campi modificati tramite setField().
     * <p>
     * 
     * @return Map la Map con i parametri
     * 
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map getParameterMap() {

        Enumeration names = _request.getParameterNames();
        Map paramMap = new HashMap();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String[] param = new String[1];
            param[0] = getParameter(name);
            paramMap.put(name, param);
        }
        return paramMap;
    }

    /**
     * Ricava una stringa dalla richiesta indicando l'indirizzo ip del client che ha inviato la richiesta, il metodo con
     * cui è stata inviata, l'url e la lista di tutti i parametri.
     */
    public String toString() {

        StringBuffer buffer = new StringBuffer(200);
        // ricava indirizzo ip
        buffer.append(getRemoteAddr() + " - ");
        // ricava il metodo con cui è stata effettuata la richiesta
        buffer.append(_request.getMethod() + "  ");
        // ricava l'url
        buffer.append(getRequestURL()).append("?");
        if ("GET".equals(getMethod())) {
            buffer.append(getQueryString());
        } else {
            // ricava la lista dei parametri
            Enumeration parameters = _request.getParameterNames();
            while (parameters.hasMoreElements()) {
                String paramName = (String) parameters.nextElement();
                buffer.append(paramName + "=" + _request.getParameter(paramName) + "&");
            }

        }
        return (buffer.toString());
    }

    /**
     * Ricava uno Unique Message Identifier a partire dalla richiesta ricevuta. L'identificatore viene composto
     * concatenando l'indirizzo IP della macchina, il tempo corrente in millisecondi ed un identificativo random
     * ricavato allo start della applicazione
     * 
     * @return java.lang.String Umsgid per la richiesta.
     */
    public String getUMsgId() {

        synchronized (_Mutex) {
            boolean done = false;
            while (!done) {
                long time = System.currentTimeMillis();
                if (time < _LastTime + _DELAY) {
                    // pause for a second to wait for time to change
                    try {
                        Thread.sleep(_DELAY);
                    } catch (java.lang.InterruptedException e) {
                    } // ignore exception
                    continue;
                } else {
                    _LastTime = time;
                    done = true;
                }
            }
        }

        String time = Long.toString(_LastTime, 16) + "0000000000000000";
        String umsgid = _IPaddress + time.substring(0, 16) + _AppUnique;

        return umsgid;

    }

    /**
     * Questo metodo trasforma un indirizzo IP in un Long
     * 
     * @param ip indirizzo IPv4 (x.y.z.k)
     * @return il long ip o zero se il formato e' errato
     */
    public long fromIPAddr2Long(String ip) throws AppCrash {

        ErrDetector.GetInstance().param(ip);
        long iplong = 0;

        String[] dots = ip.split("\\.");

        if (dots.length != 4) return 0;

        iplong = Long.parseLong(dots[3]);
        iplong = iplong + (Long.parseLong(dots[2]) << 8);
        iplong = iplong + (Long.parseLong(dots[1]) << 16);
        iplong = iplong + (Long.parseLong(dots[0]) << 24);

        return iplong;
    }

    /**
     * Questo metodo verifica se un indirizzo ip e' privato usando i range IANA standard 10.0.0.0 - 10.255.255.255 (10/8
     * prefix) 172.16.0.0 - 172.31.255.255 (172.16/12 prefix) 192.168.0.0 - 192.168.255.255 (192.168/16 prefix)
     * 
     * @param ip IPv4 n formato x.y.z.k
     * @return true/false true se formato in input errato
     */
    public boolean isIPAddrPrivate(String ip) throws AppCrash {

        long ipl = fromIPAddr2Long(ip);

        // 2130706433 == 127.0.0.1
        if (ipl == 0 || ipl == 2130706433) return true;

        if (ipl >> 24 == 0x0a) return true;

        if (ipl >> 20 == 0xac1) return true;

        if (ipl >> 16 == 0xc0a8) return true;

        return false;
    }

    private void logRequest() {

        LogColloquio log = (LogColloquio) Logger.GetInstance().getInfo().get(LogColloquio.RQ_HTTP_ENTRANTE);

        if (log == null) {
            // creo il log http entrante sse non è già presente in sessione
            log = LogColloquio.makeLogColloquio(LogColloquio.RQ_HTTP_ENTRANTE);
            log.setProtocollo(LogColloquio.MESSAGGI_HTTP);

            try {
                String requestString = this.toString();

                log.setSorgente(getRemoteAddr());

                String nomeProg = Config.GetInstance().getProperty("Logger.ProgramName");
                log.setDestinazione(nomeProg);
                log.setRichiesta(requestString);

            } catch (Throwable e) {
                log.setRichiesta("Errore nella creazione di LogColloquio HTTP");
            }

            // aggiungo il log http entrante in sessione
            Logger.GetInstance().addInfo(LogColloquio.RQ_HTTP_ENTRANTE, log);
        }
    }

    /**
     * @return Ritorna il campo request originale.
     */
    public HttpServletRequest getOriginalRequest() {

        return _request;
    }

}
