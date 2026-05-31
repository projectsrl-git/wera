/*
  Clock.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 11/03/2002

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.misc;

import net.project.errors.AppCrash;
import net.project.errors.Logger;

public class Clock implements Runnable {

    private long          _time;
    private Clockable_itf _clockable;

    /**
     * Costruttore.
     */
    public Clock(long time, Clockable_itf clockable) {

        _time = time;
        _clockable = clockable;
    }

    @Override
    public void run() {

        Logger.GetInstance().log0("ATTIVATO CLOCK");
        while (true) {
            try {
                Thread.sleep(_time);
            } catch (InterruptedException ie) {
                AppCrash ap = new AppCrash(ie);
                ap.logContext("Clock", "InterruptedException in run()");
            }
            _clockable.tick();
        }
    }
}
