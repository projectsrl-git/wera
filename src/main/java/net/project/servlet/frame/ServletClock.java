
package net.project.servlet.frame;

import net.project.errors.AppCrash;
import net.project.errors.Logger;

public class ServletClock implements Runnable {

    private long                    _time;
    private ServletApplication_base _servlet;
    private boolean                 _canRun = true;

    public ServletClock(long time, ServletApplication_base servlet) {

        _time = time;
        _servlet = servlet;
    }

    public void ferma() {

        _canRun = false;
    }

    @Override
    public void run() {

        Logger.GetInstance().log0("ATTIVATO CLOCK DELLA SERVLET");
        Logger.GetInstance().flush();
        _canRun = true;

        while (_canRun) {
            try {
                Thread.sleep(_time);
            } catch (InterruptedException ie) {
                AppCrash ap = new AppCrash(ie);
                ap.logContext("ServletClock", "InterruptedException in run()");
            }
            _servlet.setTickNeeded();
            Logger.GetInstance().flush();
        }
    }
}
