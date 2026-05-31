
package project.servlet.gui;

import net.project.errors.AppCrash;
import net.project.servlet.gui.FreeMarkerPage;
import project.misc.Utils;

public class FreeMarkerPagePDF extends FreeMarkerPage {

    public FreeMarkerPagePDF() {

        super();
    }

    public FreeMarkerPagePDF(String name, String cfName) throws AppCrash {

        super(name, cfName);
    }

    @Override
    public String convertValue(String value) {

        return Utils.converteCaratteriSpeciali(value);
    }

}
