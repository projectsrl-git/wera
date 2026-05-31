/*
  StreamLogWriter_base.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 05/03/2002

  Autore: Ilaria

  Note:

  Modifiche:

 */

package net.project.errors;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

public abstract class StreamLogWriter_base implements LogWriter_itf {

    private String           _nomeProg;
    private SimpleDateFormat _formatter;
    private String           _logType;
    private String           _confName;

    public StreamLogWriter_base(String logType, String confName) {

        _logType = logType;
        _confName = confName;
        _nomeProg = ErrorConfig.GetInstance().getProperty("Logger.ProgramName");
        _formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SS ");
    }

    protected String getLogType() {

        return _logType;
    }

    protected String getProgramName() {

        return _nomeProg;
    }

    protected String getConfName() {

        return _confName;
    }

    protected abstract void writeToStream(String message);

    /**
     * Aggiunge al file di log dell'applicazione una stringa passata.
     *
     * @param what e' la stringa da aggiungere al log
     */
    @Override
    public void WriteLog(String what, Hashtable info) {

        String thredId = Thread.currentThread().getName() + ": ";
        Date oggi = new Date();
        writeToStream(_formatter.format(oggi) + thredId + what);

    }

    @Override
    public void WriteLog(String type, String message, Hashtable info) {

        WriteLog(type + " - " + message, info);
    }

    @Override
    public void WriteLog(Exception ex, Hashtable info) {

        String thredId = Thread.currentThread().getName() + ":";
        Date oggi = new Date();
        writeToStream("----------");
        writeToStream("Crash start: " + _formatter.format(oggi) + "Thread: " + thredId);
        writeToStream("-");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        pw.close();
        writeToStream(sw.toString());

    }

    @Override
    public void WriteLog(Throwable ex, Hashtable info) {

        String thredId = Thread.currentThread().getName() + ":";
        Date oggi = new Date();
        writeToStream("----------");
        writeToStream("Crash start: " + _formatter.format(oggi) + "Thread: " + thredId);
        writeToStream("-");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        pw.close();
        writeToStream(sw.toString());
    }

    /**
     * Aggiunge al file di log dell'applicazione una stringa passata.
     *
     * @param info e' l'hashtable da aggiungere
     */
    @Override
    public void WriteLog(Hashtable info) {

        dumpInfo(info);
    }

    @Override
    public void flush() {

    }

    @Override
    public void dumpInfo(Hashtable info) {

        String thredId = Thread.currentThread().getName() + ": ";
        Date oggi = new Date();
        String dataDump = _formatter.format(oggi);

        java.util.Iterator iter = info.keySet().iterator();

        while (iter.hasNext()) {

            String nome = (String) iter.next();
            Object obj = info.get(nome);

            try {
                if (obj instanceof StorableLog_itf) {
                    ((StorableLog_itf) obj).store();
                } else if (obj instanceof String) {
                    writeToStream(dataDump + thredId + "Info " + nome + " - Valore " + (String) obj);
                }
            } catch (Throwable e) {
                writeToStream(dataDump + thredId + "Errore in dumpInfo chiave: " + nome);
            }

        }

    }

}
