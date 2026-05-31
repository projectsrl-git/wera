
package project.db;

import java.sql.ResultSet;

import net.project.errors.ParamCrash;

public class PjDBRowMail extends PjDBRow {

    public PjDBRowMail(ResultSet set) throws ParamCrash {

        super(set);
    }

    /**
     * Definisce il comportamento per i caratteri speciali come "a capo"
     * 
     * @param int c codifica del carattere da trattare
     * 
     * @return String stringa profotta a seguito della lettura del carattere
     */
    @Override
    protected String elaboraCarattere(int c) {

        String clobString = "";

        char clobChar = (char) c;
        clobString = clobString + clobChar;

        return clobString;
    }

}
