
package net.projectsrl.dafne.db;

import net.projectsrl.webapp.core.DeleteQueryRows;

public class DeleteAziendeUtente extends DeleteQueryRows<UtentiAziendeDAO, Integer> {

    public DeleteAziendeUtente() {
        super("DataSetAziendeUtente", UtentiAziendeDAO.ID_UTENTE, UtentiAziendeDAO.ID_UTENTE_AZIENDA);

    }

}
