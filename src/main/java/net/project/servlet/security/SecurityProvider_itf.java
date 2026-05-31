/*
  SecurityProvider_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 06/10/2000

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.servlet.security;

import java.util.List;

import net.project.errors.AppCrash;

/**
 * Definisce l'interfaccia utilizzata per implementare la sicurezza.
 */
public interface SecurityProvider_itf {

    /**
     * LOG della transazione.
     *
     * @exception net.project.errors.AppCrash.
     */
    public void logAudit() throws AppCrash;

    /**
     * Verifica la funzione richiesta possa essere utilizzata dal ruolo fornito in input.
     *
     * @param role net.project.servlet.security.UserSecurityInfo E' il ruolo che deve essere verificato.
     * @param functionID java.lang.String E' l'Id della funzione (alias del menù) che deve essere utilizzata
     * @return true Se l'utente ha i permessi per accedere alla funzione, false altrimenti.
     * @exception net.project.errors.AppCrash.
     */
    public boolean checkPermission(UserSecurityInfo role, String functionID) throws AppCrash;

    /**
     * Restituisce il livello richiesto del menù assegnato al ruolo fornito in input.
     *
     * @param role net.project.servlet.security.UserSecurityInfo Ruolo di cui si deve ricercare il menù.
     * @param idMenu java.lang.String Identificatore del menù (alias della funzione). Se null si intende tutto il menù
     *            assegnato al ruolo, se diverso da null si intende il menù o il sottomenù di partenza.
     * @return net.project.servlet.menu.Menu_itf il reference al menu richiesto o null in caso di errore.
     * @exception net.project.errors.AppCrash.
     */
    public List getMenu(UserSecurityInfo role, String idMenu) throws AppCrash;

    /**
     * Effettua un reset dei permessi e dei menu associati ai ruoli.
     *
     * @exception net.project.errors.AppCrash.
     */
    public void reset() throws AppCrash;

}
