
package project.db;

import java.io.Reader;
import java.sql.ResultSet;

import net.project.dataset.DBRow;
import net.project.errors.AppCrash;
import net.project.errors.ParamCrash;
import net.sourceforge.jtds.jdbc.ClobImpl;

public class PjGridDBRow extends DBRow {

    public PjGridDBRow(ResultSet set) throws ParamCrash {

        super(set);
    }

    @Override
    public Object getField(String fieldName) throws AppCrash {

        Object fieldValue = super.getField(fieldName);
        return elaboraValore(fieldValue);
    }

    @Override
    public Object getField(int fieldNo) throws AppCrash {

        Object fieldValue = super.getField(fieldNo);
        return elaboraValore(fieldValue);
    }

    protected Object elaboraValore(Object fieldValue) throws AppCrash {

        if (fieldValue == null) {

            return fieldValue;
        }

        try {
            if (fieldValue instanceof String) {
                String stringValue = ((String) fieldValue).trim();
                if (stringValue.contains("€")) {
                    stringValue = stringValue.replace("€", "&#8364;");
                }
                return stringValue;
            }

            if (fieldValue instanceof java.math.BigDecimal) {
                String stringValue = fieldValue.toString();
                return stringValue;
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

    /**
     * Definisce il comportamento per i caratteri speciali come "a capo"
     * 
     * @param int c codifica del carattere da trattare
     * 
     * @return String stringa profotta a seguito della lettura del carattere
     */
    protected String elaboraCarattere(int c) {

        StringBuilder clobString = new StringBuilder();

        char clobChar = (char) c;

        if (c == 13) {
            // clobString = "<br>";
            clobString.append("   ");
        } else {
            clobString.append(clobChar);
        }

        return clobString.toString();
    }

}
