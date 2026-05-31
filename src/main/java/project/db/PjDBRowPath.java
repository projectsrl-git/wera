
package project.db;

import java.sql.ResultSet;

import net.project.errors.AppCrash;
import net.project.errors.ParamCrash;

public class PjDBRowPath extends PjDBRow {

    public PjDBRowPath(ResultSet set) throws ParamCrash {

        super(set);
    }

    @Override
    protected Object elaboraValore(Object fieldValue) throws AppCrash {

        if (fieldValue == null) {

            return fieldValue;
        }

        try {
            if (fieldValue instanceof String) {

                String stringValue = ((String) fieldValue).trim();
                // stringValue=stringValue.replaceAll("\\","/");
                return stringValue;
            }

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(),
                    "Metodo elaboraValore: errore nella impostazione del comportamento specifico di freemarker per le differenti classi");
            throw ac;
        }

        return fieldValue;
    }
}
