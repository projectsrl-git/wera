
package project.db;

import java.sql.ResultSet;

import net.project.errors.AppCrash;
import net.project.errors.ParamCrash;
import project.misc.Utils;

public class PjDBRowWord extends PjDBRow {

    public PjDBRowWord(ResultSet set) throws ParamCrash {

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

    @Override
    protected Object elaboraValore(Object fieldValue) throws AppCrash {

        fieldValue = super.elaboraValore(fieldValue);

        if (fieldValue == null) {

            return fieldValue;
        }

        try {
            if (fieldValue instanceof String) {
                fieldValue = ((String) fieldValue)
                        .replaceAll(
                                "\n",
                                "</w:t></w:r></w:p><w:p><w:pPr><w:rPr><w:rFonts w:ascii=\"Arial\" w:h-ansi=\"Arial\" w:cs=\"Arial\"/><wx:font wx:val=\"Arial\"/><w:sz w:val=\"16\"/><w:sz-cs w:val=\"16\"/></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:h-ansi=\"Arial\" w:cs=\"Arial\"/><wx:font wx:val=\"Arial\"/><w:sz w:val=\"16\"/><w:sz-cs w:val=\"16\"/></w:rPr><w:t>");

                return Utils.converteCaratteriSpeciali((String) fieldValue);
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
     * 
     */
    @Override
    protected String elaboraCarattere(int c) {

        String clobString = "";

        char clobChar = (char) c;

        // if (c==13) {
        // //clobString="</w:t></w:r></w:p><w:p><w:pPr><w:rPr><w:rFonts w:ascii=\"Arial\" w:h-ansi=\"Arial\" w:cs=\"Arial\"/><wx:font wx:val=\"Arial\"/><w:sz w:val=\"16\"/><w:sz-cs w:val=\"16\"/></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:h-ansi=\"Arial\" w:cs=\"Arial\"/><wx:font wx:val=\"Arial\"/><w:sz w:val=\"16\"/><w:sz-cs w:val=\"16\"/></w:rPr><w:t>";
        // clobString="";
        // } else {
        // clobString=clobString+clobChar;
        // }
        clobString = clobString + clobChar;
        return clobString;
    }
}
