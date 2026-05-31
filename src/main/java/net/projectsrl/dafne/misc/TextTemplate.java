
package net.projectsrl.dafne.misc;

import java.util.Map;
import java.util.StringTokenizer;

import net.project.errors.ErrDetector;
import net.project.errors.ParamCrash;

public class TextTemplate extends net.project.misc.TextTemplate {

    public TextTemplate(String text) throws ParamCrash {
        super(text);
    }

    @Override
    public boolean replace(Map table) throws ParamCrash {

        String newText = "";
        String tableValue = "";
        boolean tuttoOk = true;

        try {
            ErrDetector.GetInstance().param(table);

            StringTokenizer st = new StringTokenizer(getText(), "#");
            while (st.hasMoreTokens()) {
                newText = newText + st.nextToken();
                // recupera il valore nella hashtable
                if (st.hasMoreTokens()) {
                    tableValue = (String) table.get(st.nextToken());
                    if (tableValue == null) {
                        // se non ha trovato corrispondenza nella hashtable ...
                        tuttoOk = false;
                    } else {
                        newText = newText + tableValue;
                    }
                }
            }
            setText(newText);

            return tuttoOk;
        } catch (ParamCrash ex) {
            ex.logContext("TextTemplate", "Errore in replace: parametri errati");
            throw ex;
        }
    }

}
