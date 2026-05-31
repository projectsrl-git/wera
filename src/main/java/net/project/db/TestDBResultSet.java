/*
  TestDBResultSet.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 11/10/2000

  Autore: Andrea R.

  Note:

  Modifiche:

 */

package net.project.db;

import net.project.errors.AppCrash;
import net.project.errors.Logger;
import net.project.misc.Config;

/**
 * Usata come test per la classe DBResulSet_base Legge tabella be_vw_gsmres
 */
public class TestDBResultSet extends DBResultSet_base {

    /**
     * main. Usato solo per test
     */
    public static void main(String args[]) {

        try {
            Config.InitInstance(args[0]);

            Logger.GetInstance().log0("TestDBResultSet.main()<> INIZIO <>");
            TestDBResultSet test = new TestDBResultSet();
            boolean rc = test.retrieve();
            Logger.GetInstance().log0("TestDBResultSet.main()<> retrieve return code=" + rc + "<>");
            if (rc == true) {
                String mid = null;
                while (test.next()) {
                    mid = (String) test.getAttribute("mid");
                    Logger.GetInstance().log0("TestDBResultSet.main()<> mid=" + mid + "<>");
                }
            }
        } catch (Throwable e) {
            Logger.GetInstance().log0("TestDBResultSet.main()<> Throwable=" + e.getMessage() + "<>");
        }
    }

    /**
     * Costruttore di default
     */
    public TestDBResultSet() {

        super("be_vw_gsmres");
    }

    /**
     * Costruttore.
     * 
     * @param transaction net.project.db.DBTransaction
     */
    public TestDBResultSet(net.project.db.DBTransaction transaction) {

        super(transaction, "be_vw_gsmres");
    }

    /**
     * Ritorna la where condition per la tabella "be_vw_gsmres".
     * 
     * @return java.lang.String
     * @exception AppCrash
     */
    @Override
    protected String whereCondition() throws AppCrash {

        return ("");
    }
}
