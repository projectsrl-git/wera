
package project.errors;

import net.project.errors.ErrorConfig;
import net.project.errors.FileDebugLogger;

public class FileDebugLoggerToday extends FileDebugLogger {

    private String _nomeLog;

    public FileDebugLoggerToday(String tipo, String confName) {

        super(tipo, confName);
        _nomeLog = ErrorConfig.GetInstance(getConfName()).getProperty("Logger.DebugFile");
    }

    @Override
    public String getFileName() {

        return _nomeLog;
    }

}
