
package net.projectsrl.dafne.db;

import net.projectsrl.webapp.core.DeleteQueryRows;

public class DeletePasswordUtente extends DeleteQueryRows<PasswordDAO, Integer> {

    public DeletePasswordUtente() {

        super("DataSetPasswordUtenti", PasswordDAO.ID_UTENTE, PasswordDAO.ID_PASSWORD);

    }

}
