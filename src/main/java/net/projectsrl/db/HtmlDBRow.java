
package net.projectsrl.db;

import java.sql.ResultSet;

import net.project.errors.AppCrash;
import net.project.errors.ParamCrash;

import org.apache.commons.lang.StringEscapeUtils;

import project.misc.Utils;

public class HtmlDBRow extends PjDBRow {

    public HtmlDBRow(ResultSet set) throws ParamCrash {

        super(set);
    }


    protected Object elaboraValore(Object fieldValue) throws AppCrash {

        if (fieldValue == null) {

            return fieldValue;
        }

        try {

            if (fieldValue instanceof String) {

                String stringValue = ((String) fieldValue).trim();

                // se è una data ed è ribaltata
                // la raddrizzo
                //
                if (stringValue.length() == 10) {
                    if ((stringValue.substring(4, 5) + stringValue.substring(7, 8)).equals("//")) {
                        return Utils.raddrizzaData(stringValue);
                    }
                }
                
                return StringEscapeUtils.escapeHtml(stringValue);
            }

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "fieldValue: " + fieldValue);
            throw ac;
        }

        return fieldValue;
    }

}
