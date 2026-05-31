
package net.project.db;

import java.util.Iterator;

import net.project.errors.AppCrash;
import net.project.errors.Logger;
import net.project.misc.Config;

/**
 * @author Simone
 */
public class TestXMLDAO {

    private static class pib25 extends DAOXML_base {

        public static final String S_PK_OP   = "S_PK_OP";
        public static final String C_TP_OP   = "C_TP_OP";
        public static final String T_MSG     = "T_MSG";
        public static final String T_MSG_RSP = "T_MSG_RSP";

        public static final String T_MSG_1   = "?XT_MSG_1";
        public static final String T_MSG_2   = "?XT_MSG_2";
        public static final String T_MSG_3   = "?XT_MSG_3";
        public static final String T_MSG_4   = "?XT_MSG_4";
        public static final String T_MSG_5   = "?XT_MSG_5";

        public pib25() throws AppCrash {

            super("D111.PIB001.PIBV0250");
        }

        @Override
        protected boolean storeFlexibleData() {

            return true;
        }

        @Override
        protected String getRecordColName() {

            return T_MSG;
        }

        @Override
        protected String whereCondition() throws AppCrash {

            return ("where S_PK_OP = '" + getField(S_PK_OP) + "'");
        }
    }

    public static void main(String[] args) {

        try {
            Config.InitInstance(args[0]);

            Logger.GetInstance().log0("Start");
            pib25 p25 = new pib25();

            p25.setField(pib25.C_TP_OP, "TP02");
            p25.setField(pib25.T_MSG_1, "uno1>");
            p25.setField(pib25.T_MSG_2, "due2!~<");
            p25.setField(pib25.T_MSG_3, "tre3[]>");
            p25.setField(pib25.T_MSG_4, "quattro4''-%$@}{");
            p25.setField(pib25.T_MSG_5, "cin<que>5''-%$@");

            p25.setField(pib25.S_PK_OP, "0H2292851ZC");

            datastruct ds = new datastruct();
            ds.init();

            p25.setObjectAttribute(pib25.T_MSG_RSP, ds);

            Logger.GetInstance().log0("pre insert");
            p25.insert();
            Logger.GetInstance().log0("insert done");

            pib25 p25l = new pib25();
            p25l.setField(pib25.S_PK_OP, "0H2292851ZC");

            p25l.retrieve();

            printfields(p25l);

            datastruct ds1 = (datastruct) p25l.getObjectAttribute(pib25.T_MSG_RSP);

        } catch (AppCrash e) {
            e.printStackTrace(System.out);
        } catch (Throwable ex) {
            ex.printStackTrace(System.out);
        }

    }

    public static void printfields(DAOXML_base dao) throws Exception {

        Iterator iter = dao.iterator();
        while (iter.hasNext()) {
            String name = (String) iter.next();
            Logger.GetInstance().log0("Campo " + name + "-" + dao.getField(name) + "-");
        }

    }
}
