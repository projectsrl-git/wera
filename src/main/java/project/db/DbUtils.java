
package project.db;

import java.lang.reflect.Constructor;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;

public class DbUtils {

    /**
     * Questo metodo istanzia un oggetto PjDAO partendo dalla sua descrizione dsName associata ad un DataSet
     * 
     * @param String dsName
     * 
     */
    public static PjDAO_base makeDAOFromDsName(String configName, String dsName) throws AppCrash {

        // Controllo formale dei parametri in ingresso.
        ErrDetector.GetInstance().preCond(configName != null, "DbUtils.makeDAOFromDsName - configName = null");
        ErrDetector.GetInstance().preCond(dsName != null, "DbUtils.makeDAOFromDsName - dsName = null");

        String className = Config.GetInstance(configName).getProperty("DS." + dsName + ".DAOClass", "");
        boolean pageClassStringEsistente = (className != null) && (className.length() > 0);
        ErrDetector.GetInstance().postCond(
                pageClassStringEsistente,
                "DbUtils.makeDAOFromDsName - proprietà " + "DS." + dsName + ".DAOClass"
                        + " mancante nel file di configurazione");

        Class daoClass = null;
        Constructor constructor = null;
        PjDAO_base tableDAO = null;

        try {
            daoClass = Class.forName(className);

            constructor = daoClass.getConstructor(new Class[] {});
            // Istanzio la classe "base"
            tableDAO = (PjDAO_base) constructor.newInstance(new Object[] {});
            return tableDAO;

        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            throw ac;
        }

    }

    public static PjDAO_base makeDAOFromDsName(String dsName) throws AppCrash {

        return makeDAOFromDsName("", dsName);
    }

}
