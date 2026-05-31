/*
  EMail.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 27/10/1999

  Autore: Rosella V.

  Note:

  Modifiche:	19/01/2000	FIX TL28 aggiunti campi From: e To: agli header

 */

package net.project.misc;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;

/**
 * Questa classe fornisce le funzionalità di un semplice SMTP client.
 */
public class EMail {

    private final String     _contentType = "text/plain";
    private String           _host        = null;
    private String           _message     = null;
    private String           _to          = null;
    private String           _from        = null;
    private String           _subject     = null;
    private String           _replayto    = null;
    private Socket           _socket      = null;
    private DataOutputStream _output      = null;
    // private DataInputStream _input = null;
    // private BufferedWriter _output =null;
    private BufferedReader   _input       = null;

    /**
     * Costruttore.
     * 
     * @exception net.project.errors.AppCrash. Se si è verificato un errore di input/output.
     */
    public EMail() throws AppCrash {

        _host = Config.GetInstance().getProperty("EMailHost");
        try {
            _socket = new Socket(getHost(), 25);
            _input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            _output = new DataOutputStream(_socket.getOutputStream());
        } catch (IOException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("EMail", toString());
            throw err;
        }
    }

    /**
     * Costruttore.
     * 
     * @param host java.lang.String host SMTP letto dal file di configurazione.
     * @param from java.lang.String mittente del messaggio.
     * @param to java.lang.String destinatario del messaggio.
     * @param message java.lang.String messaggio da spedire.
     * @exception net.project.errors.AppCrash. Se si è verificato un errore di input/output.
     */
    public EMail(String from, String to) throws AppCrash {

        _host = Config.GetInstance().getProperty("EMailHost");
        _from = from;
        _to = to;
        try {
            ErrDetector.GetInstance().param(_host != null && _from != null && _to != null);
            _socket = new Socket(getHost(), 25);
            // _input = new DataInputStream(_socket.getInputStream());
            // _output = new DataOutputStream(_socket.getOutputStream());
            _input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            _output = new DataOutputStream(_socket.getOutputStream());
        } catch (AppCrash e) {
            e.logContext("EMail", "Param error: " + toString());
            throw e;
        } catch (IOException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("EMail", toString());
            throw err;
        }
    }

    /**
     * Imposta l'host.
     * 
     * @param host java.lang.String host.
     */
    public void setHost(String host) {

        _host = host;
    }

    /**
     * Ritorna l'host.
     * 
     * @return java.lang.String L'host.
     */
    private String getHost() {

        return _host;
    }

    /**
     * Imposta il messaggio da spedire.
     * 
     * @param message java.lang.String messaggio da spedire.
     */
    public void setMessage(String message) {

        _message = message;
    }

    /**
     * Ritorna il messaggio da spedire.
     * 
     * @return java.lang.String Il messaggio da spedire.
     */
    private String getMessage() {

        return _message;
    }

    /**
     * Imposta il destinatario del messaggio.
     * 
     * @param to java.lang.String destinatario del messaggio.
     */
    public void setTo(String to) {

        _to = to;
    }

    /**
     * Ritorna il destinatario del messaggio.
     * 
     * @return java.lang.String Il destinatario del messaggio.
     */
    private String getTo() {

        return _to;
    }

    /**
     * Imposta il mittente del messaggio.
     * 
     * @param to java.lang.String mittente del messaggio.
     */
    public void setFrom(String from) {

        _from = from;
    }

    /**
     * Ritorna il mittente del messaggio.
     * 
     * @return java.lang.String Il mittente del messaggio.
     */
    private String getFrom() {

        return _from;
    }

    /**
     * Imposta l'indirizzo da usare come "Replay to:"
     * 
     * @param replayto java.lang.String indirizzo.
     */
    public void setRepayTo(String replayto) {

        _replayto = replayto;
    }

    /**
     * Ottiene l'indirizzo da usare come "Replay to:"
     * 
     * @return java.lang.String indirizzo usato come "Replay to:".
     */
    public String getRepayTo() {

        return _replayto;
    }

    /**
     * Imposta il subject.
     * 
     * @param subject java.lang.String soggetto dell'e-mail.
     */
    public void setSubject(String subject) {

        _subject = subject;
    }

    /**
     * Ritorna il subject.
     * 
     * @return java.lang.String Il soggetto dell'e-mail.
     */
    private String getSubject() {

        return _subject;
    }

    /**
     * Inizia la comunicazione con l'host per l'invio della posta.
     *
     * @exception net.project.errors.AppCrash
     */
    private void start() throws AppCrash {

        String domain = null;
        try {
            if (checkResponse(_input.readLine(), "220")) {
                _output.writeBytes("helo" + "\n");
                _output.flush();
                if (checkResponse(_input.readLine(), "250")) {
                } else {
                    AppCrash err = new AppCrash();
                    err.logContext("EMail starting error in helo command", toString());
                    exit();
                    throw err;
                }
            } else {
                AppCrash err = new AppCrash();
                err.logContext("EMail starting error", toString());
                exit();
                throw err;
            }

        } catch (IOException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("EMail starting error", toString());
            throw err;
        } catch (AppCrash e) {
            e.logContext("EMailt", "Param error: domain = " + domain);
            throw e;
        }
    }

    /**
     * Invia il messaggio specificato all'host sulla porta 25.
     * 
     * @param message java.lang.String messaggio da spedire.
     * @exception net.project.errors.AppCrash Se si è verificato un errore di I/O durante l'invio del messaggio.
     */
    public void send(String message) throws AppCrash {

        String t = null;
        try {
            ErrDetector.GetInstance().param(_host != null && _from != null && _to != null && message != null);
            start();
            _message = message;
            _output.writeBytes("mail from:<" + getFrom().trim() + ">" + "\n");
            _output.flush();
            if (checkResponse(_input.readLine(), "250")) {
                StringTokenizer parser = new StringTokenizer(getTo(), ",");
                while (parser.hasMoreTokens()) {
                    t = "rcpt to:<" + parser.nextToken().trim() + ">";
                    _output.writeBytes(t + "\n");
                    _output.flush();
                    if (!checkResponse(_input.readLine(), "250")) {
                        Logger.GetInstance().logError("EMail: sending error to: " + t);
                    }
                }
                _output.writeBytes("data" + "\n");
                _output.flush();
                if (checkResponse(_input.readLine(), "354")) {
                    _output.writeBytes(composeMessage() + "\n");
                    _output.flush();
                    _output.writeBytes("." + "\n");
                    _output.flush();
                    if (checkResponse(_input.readLine(), "250")) {
                    } else {
                        Logger.GetInstance().logError("EMail: sending error in message");
                    }
                } else {
                    Logger.GetInstance().logError("EMail: sending error in data");
                }
            } else {
                Logger.GetInstance().logError("EMail: sending error in from");
            }
            exit();
        } catch (IOException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("EMail", "I/O error sending message");
            throw err;
        } catch (AppCrash e) {
            throw e;
        }

    }

    /**
     * Chiude la sessione.
     *
     * @exception net.project.errors.AppCrash Se si è verificato un errore di I/O durante la chiusura della sessione.
     */
    private void exit() throws AppCrash {

        try {
            _output.writeBytes("quit");
            _socket.close();
        } catch (IOException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("EMail", toString());
            throw err;
        }
    }

    /**
     * Controlla la risposta dell'SMTP server.
     * 
     * @param response java.lang.String risposta dell'SMTP server.
     * @param code java.lang.String response code del SMTP server.
     * @return boolean true se la risposta è positiva, false altrimenti.
     */
    private boolean checkResponse(String response, String code) {

        boolean check = true;
        if (response.indexOf(code) != 0) {
            check = false;
        }
        return check;
    }

    /**
     * Ritorna il messaggio comprensivo degli headers.
     * 
     * @return java.lang.String Il messaggio.
     */
    private String composeMessage() {

        StringBuffer temp = new StringBuffer();
        // aggiungo il subject
        temp.append("From: ").append(getFrom());
        temp.append("\n");
        temp.append("To: ").append(getTo());
        temp.append("\n");

        String replayto = getRepayTo();
        if (replayto != null) {
            temp.append("Replay To: ").append(replayto);
            temp.append("\n");
        }

        temp.append("Subject: ").append(getSubject());
        temp.append("\n");
        temp.append("Content-Type: ").append(_contentType);
        temp.append("\n");
        temp.append("\n");
        temp.append(getMessage());
        return temp.toString();
    }

    /**
     * Ritorna una stringa riportante host, mittente, destinatario e messaggio dell'e-mail.
     * 
     * @return java.lang.String.
     */
    @Override
    public String toString() {

        return new String("Host: " + _host + " Sender: " + _from + "Receiver: " + _to + "Message: " + _message);
    }

}
