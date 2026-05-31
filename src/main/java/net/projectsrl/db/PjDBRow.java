
package net.projectsrl.db;

import java.io.Reader;
import java.sql.ResultSet;

import net.project.dataset.DBRow;
import net.project.errors.AppCrash;
import net.project.errors.ParamCrash;
import net.sourceforge.jtds.jdbc.ClobImpl;
import project.misc.Utils;

public class PjDBRow extends DBRow {

    public PjDBRow(ResultSet set) throws ParamCrash {

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

                // se � una data ed � ribaltata
                // la raddrizzo
                //
                if (stringValue.length() == 10) {
                    if ((stringValue.substring(4, 5) + stringValue.substring(7, 8)).equals("//")) {
                        return Utils.raddrizzaData(stringValue);
                    }
                }

                return stringValue;
            }

//            if (fieldValue instanceof java.math.BigDecimal) {
//                String stringValue = fieldValue.toString();
//                return stringValue;
//            }


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

        String clobString = "";

        char clobChar = (char) c;

        if (c == 13) {
            // clobString = "<br>";
            clobString = "   ";
        } else {
            clobString = clobString + clobChar;
        }

        return clobString;
    }

}
