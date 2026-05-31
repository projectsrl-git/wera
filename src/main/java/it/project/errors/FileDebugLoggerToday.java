
package it.project.errors;

import java.util.Calendar;

import net.project.errors.FileDebugLogger;
import net.project.misc.Config;

public class FileDebugLoggerToday extends FileDebugLogger {

    public FileDebugLoggerToday(String tipo, String confName) {

        super(tipo, confName);
    }

    @Override
    public String getFileName() {

        Calendar cal = Calendar.getInstance();
        String giorno = "0" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        giorno = giorno.substring(giorno.length() - 2);
        String mese = "0" + String.valueOf(cal.get(Calendar.MONTH) + 1);
        mese = mese.substring(mese.length() - 2);
        String anno = String.valueOf(cal.get(Calendar.YEAR));

        return super.getFileName() + "_" + Config.GetInstance().getProperty("DBEntity.NomeDB", "") + "_" + anno + "-"
                + mese + "-" + giorno + ".log";
    }
}
