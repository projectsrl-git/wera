
package it.project.errors;

import java.util.Calendar;

import net.project.errors.ErrorConfig;
import net.project.errors.FileLogWriter;
import net.project.misc.Config;

public class FileErrorLoggerToday extends FileLogWriter {

    private String _nomeLog;

    /**
     * Constructor . Legge il nome della directory di crash log dalla proprieta' "Logger.CrashDir"
     */
    public FileErrorLoggerToday(String tipo, String confName) {

        super(tipo, confName);
        _nomeLog = ErrorConfig.GetInstance(getConfName()).getProperty("Logger.CrashDir");
    }

    @Override
    public String getFileName() {

        Calendar cal = Calendar.getInstance();
        String giorno = "0" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        giorno = giorno.substring(giorno.length() - 2);
        String mese = "0" + String.valueOf(cal.get(Calendar.MONTH) + 1);
        mese = mese.substring(mese.length() - 2);
        String anno = String.valueOf(cal.get(Calendar.YEAR));

        String threadId = Thread.currentThread().getName();
        return (_nomeLog + Config.GetInstance().getProperty("DBEntity.NomeDB", "") + "_" + anno + "-" + mese + "-"
                + giorno + "_" + threadId);
    }
}
