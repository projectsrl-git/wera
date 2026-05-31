
package project.servlet.gui;

import net.project.errors.AppCrash;
import net.project.servlet.gui.FreeMarkerPage;
import project.misc.Utils;

public class FreeMarkerPageWord extends FreeMarkerPage {

    public FreeMarkerPageWord() {

        super();
    }

    public FreeMarkerPageWord(String name, String cfName) throws AppCrash {

        super(name, cfName);
    }

    @Override
    public String convertValue(String value) {

        return Utils.converteCaratteriSpeciali(value);
    }

}
