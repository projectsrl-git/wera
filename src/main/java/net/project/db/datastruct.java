
package net.project.db;

import net.project.errors.AppCrash;
import net.project.mess.MsgWriter_itf;
import net.project.mess.atdat.ATDATURLReadWrite;

/**
 * @author Simone
 *
 *         To change this generated comment edit the template variable "typecomment": Window>Preferences>Java>Templates.
 *         To enable and disable the creation of type comments go to Window>Preferences>Java>Code Generation.
 */
public class datastruct {

    private String        scampo  = "pippo";
    private int           icampo  = 10;
    private long          lcampo  = 1000000000;
    private String[]      ascampo = { "primo", "secondo", "terzo" };
    private MsgWriter_itf msg     = null;

    public void init() throws AppCrash {

        msg = new ATDATURLReadWrite("RCCAO");
        msg.setField("pippo", "pippo1");
        msg.setField("pippo2", "pippo2");
    }

    public datastruct() {

    }

    @Override
    public String toString() {

        return "a";
    }

}
