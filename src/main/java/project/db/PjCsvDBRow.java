
package project.db;

import java.io.Reader;
import java.sql.ResultSet;

import net.project.errors.AppCrash;
import net.project.errors.ParamCrash;
import net.sourceforge.jtds.jdbc.ClobImpl;

public class PjCsvDBRow extends PjGridDBRow {

    public PjCsvDBRow(ResultSet set) throws ParamCrash {

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
                stringValue.replace("E-14", "");

                return stringValue;
            }

            if (fieldValue instanceof java.math.BigDecimal) {
                String stringValue = fieldValue.toString();
                stringValue = stringValue.replace("E-14", "");
                return stringValue.replace(".", ",");
            }

            if (fieldValue instanceof ClobImpl) {
                ClobImpl clobValue = (ClobImpl) fieldValue;
                Reader is = clobValue.getCharacterStream();

                StringBuffer sb = new StringBuffer();
                int c;
                while ((c = is.read()) != -1) {

                    String clobString = elaboraCarattere(c);
                    sb.append(clobString);

                }

                String stringValue = sb.toString();

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
