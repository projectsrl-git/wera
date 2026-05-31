/*
  DeferredLogWriter.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 16/03/2002

  Autore: Simone Z.

  Note:

  Modifiche: 

 */

package net.project.errors;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Hashtable;

public class DeferredLogWriter extends StreamLogWriter_base {

    private StreamLogWriter_base _theLogWriter;
    private LogStore             _theLogStore;

    public DeferredLogWriter(String tipo, String confName) {

        super(tipo, confName);

        _theLogStore = new LogStore();

        String debugClass = ErrorConfig.GetInstance(getConfName()).getProperty("Logger.deferred." + tipo + ".class");

        try {
            Class tempClassDeb = Class.forName(debugClass);
            Constructor procBaseConstrDeb = tempClassDeb.getConstructor(new Class[] { String.class, String.class });
            _theLogWriter = (StreamLogWriter_base) procBaseConstrDeb.newInstance(new Object[] { tipo, confName });
        } catch (Throwable t) {
        }

    }

    @Override
    protected void writeToStream(String message) {

        ((LogStore) _theLogStore.get()).add(message);
    }

    @Override
    public void flush() {

        ArrayList logs = ((LogStore) _theLogStore.get()).getLogs();

        try {

            java.util.Iterator iter = logs.iterator();

            while (iter.hasNext()) {
                String line = (String) iter.next();
                _theLogWriter.writeToStream(line);
            }
            _theLogWriter.flush();
        } finally {
            logs.clear();
        }

    }

    private static class LogStore extends ThreadLocal {

        private ArrayList _logStore;

        public LogStore() {

            _logStore = new ArrayList();
        }

        @Override
        public Object initialValue() {

            return new LogStore();
        }

        public void add(String message) {

            _logStore.add(message);
        }

        public ArrayList getLogs() {

            return _logStore;
        }

    }

    @Override
    public void dumpInfo(Hashtable info) {

        _theLogWriter.dumpInfo(info);
    }
}
