
package net.projectsrl.dafne.db;

import net.projectsrl.webapp.core.DeleteQueryRows;

public class DeleteProfiliUtente extends DeleteQueryRows<UtentiProfiliDAO, Integer> {

    public DeleteProfiliUtente() {
        super("DataSetProfiliUtente", UtentiProfiliDAO.ID_UTENTE, UtentiProfiliDAO.ID_UTENTE_PROFILO);

    }

}
