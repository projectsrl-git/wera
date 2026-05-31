/*
  SocketLogWriter.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 15/05/2001

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.errors;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SocketLogWriter extends StreamLogWriter_base {

    private String        _host;
    private int           _port;
    private String        _srcIP;
    private String        _alternateFileName;
    private boolean       _remote;

    private static String _testString;

    private OutputStream  _out;
    private Socket        _socket;
    private long          _lastConnectError = 0;

    /**
     * Costruisce un SocketLogWriter per uno specifico tipo di log leggendo le proprieta' dalla configurazione passata.
     * Le proprieta' lette sono:
     * <p>
     * Logger.socket.+ tipo + .host nome host/IP destinazione dei log
     * <p>
     * Logger.socket.+ tipo + .port porta di destinazione dei log
     * <p>
     * Logger.socket.+ tipo + .src nome/IP sorgente da utilizzare nella connessione (facoltativo)
     * <p>
     * Logger.socket.alternatefile base per il nome del file sul quale eseguire i log se la connessione fallisce
     * facoltativo. Di default vanno in /tmp/getProgramName()
     */
    public SocketLogWriter(String tipo, String confName) {

        super(tipo, confName);

        _testString = getProgramName() + "???" + getLogType() + "???XX???TEST";

        _host = ErrorConfig.GetInstance(getConfName()).getProperty("Logger.socket." + tipo + ".host");
        String porta = ErrorConfig.GetInstance(getConfName()).getProperty("Logger.socket." + tipo + ".port");
        _port = Integer.parseInt(porta);
        _srcIP = ErrorConfig.GetInstance(getConfName()).getProperty("Logger.socket." + tipo + ".src");
        _alternateFileName = ErrorConfig.GetInstance(getConfName()).getProperty("Logger.socket.alternatefile",
                "/tmp/" + getProgramName());
    }

    private void connect() {

        try {

            // Se non sono trascorsi almeno 5 minuti dall'ultimo tentativo di connessione non ci provo neanche
            if ((System.currentTimeMillis() - _lastConnectError) < 300000) {
                _remote = false;
                return;
            }

            // Creo la socket con un connection timeout di 4 secondi
            if (_srcIP == null) {
                _socket = SSBSocketFactory.MakeSocket(InetAddress.getByName(_host), _port, 4000);
            } else {
                InetAddress localIP = InetAddress.getByName(_srcIP);
                _socket = SSBSocketFactory.MakeSocket(InetAddress.getByName(_host), _port, localIP, 4000);
            }
            _socket.setTcpNoDelay(true);
            _out = _socket.getOutputStream();
            _remote = true; // si puo' loggare in remoto
        } catch (Throwable ex) {
            System.out.println((new SimpleDateFormat()).format(new Date()) + " - SocketLogWriter: connessione KO Host "
                    + _host + "Port " + _port + "Src " + _srcIP + " Exception msg:" + ex.getMessage());
            _remote = false; // la connessione non e' riuscita, non si puo' loggare in remoto
            _lastConnectError = System.currentTimeMillis();
        }
    }

    @Override
    protected void writeToStream(String message) {

        String threadId = Thread.currentThread().getName() + ":";

        StringBuffer bufToSend = new StringBuffer(getProgramName());
        bufToSend.append("???").append(getLogType());
        bufToSend.append("???").append(threadId);
        bufToSend.append("???").append(message);
        String toSend = bufToSend.toString();

        _remote = true; // Provo prima a loggare in remoto

        for (int j = 1; j <= 2; j++) {
            try {
                if (_remote) {
                    ByteArrayOutputStream bao = new ByteArrayOutputStream(1000);
                    DataOutputStream dao = new DataOutputStream(bao);

                    dao.writeInt(toSend.length());
                    dao.writeBytes(toSend);

                    _out.write(bao.toByteArray());

                    dao.close();
                    bao.close();

                    break;
                }

                PrintWriter out = new PrintWriter(new FileWriter(getFileName(), true));
                out.println(toSend);
                out.close();
            } catch (Throwable ex) {
                connectSyncronizer();
            }
        }
    }

    private synchronized void connectSyncronizer() {

        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream(20);
            DataOutputStream dao = new DataOutputStream(bao);

            dao.writeInt(_testString.length());
            dao.writeBytes(_testString);

            _out.write(bao.toByteArray());
            dao.close();
            bao.close();
        } catch (Throwable ex) {
            connect();
        }
    }

    private String getFileName() {

        return _alternateFileName + "." + getLogType() + ".log";
    }

}
