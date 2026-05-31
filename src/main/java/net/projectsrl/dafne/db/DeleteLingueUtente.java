
package net.projectsrl.dafne.db;

import net.projectsrl.webapp.core.DeleteQueryRows;

public class DeleteLingueUtente extends DeleteQueryRows<UtentiLingueDAO, Integer> {

    public DeleteLingueUtente() {
        super("DataSetLingueUtente", UtentiLingueDAO.ID_UTENTE, UtentiLingueDAO.ID_UTENTI_LINGUE);
    }

}
