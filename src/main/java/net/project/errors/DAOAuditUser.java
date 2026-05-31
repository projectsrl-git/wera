/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.errors;

/**
 * Questa
 */
public class DAOAuditUser {

    private final String _userId;

    /**
     * Costruttore
     */
    public DAOAuditUser(String userId) {

        super();
        _userId = userId;
    }

    /**
     * @return Ritorna il campo userId.
     */
    protected String getUserId() {

        return _userId;
    }

    /**
     * Questo metodo ritorna la stringa che rappresenta lo user
     *
     * @return
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return _userId;
    }

}
