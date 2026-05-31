/*
  DBLoginOnlySecurityProvider_base

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 07/06/2001

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.servlet.security;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.project.errors.AppCrash;

/**
 * Security Provider utilizzato nel caso in cui l'applicazione richieda solo la login dell'utente. Dopo la fase di login
 * l'utente può accedere ad ogni funzione dell'applicazione. Carica in memoria l'elenco dei menu' dell'applicazione
 * partendo da un DB.
 *
 * Utilizza un ruolo costante "USER" che deve essere settato nel metodo authenticate dell'authentication provider
 * dell'applicazione.
 */
public class DBLoginOnlySecurityProvider extends SecurityProvider_base {

    // Hashtable che riporta l'unico ruolo associato all'applicazione:
    // viene utilizzato un ruolo fisso: "USER"
    // Campo chiave = new String("<ID_RUOLO>");
    // Campo valore = new Integer(0)
    private Map _roleList = null;

    /**
     * Costruttore
     *
     * @param applId java.lang.String Nome del file config.
     */
    public DBLoginOnlySecurityProvider(String applId) {

        super(applId);
    }

    /**
     * Verifica la funzione richiesta possa essere utilizzata dal ruolo fornito in input.
     *
     * @param role net.project.servlet.security.UserSecurityInfo E' il ruolo che deve essere verificato.
     * @param functionID java.lang.String E' l'Id della funzione (alias del menù) che deve essere utilizzata
     * @return true Se l'utente ha i permessi per accedere alla funzione, false altrimenti.
     * @exception net.project.errors.AppCrash.
     */
    @Override
    public boolean checkPermission(UserSecurityInfo role, String functionID) throws AppCrash {

        init();

        // controlla la presenza della userid in sessione
        if (role.getUserId() == null) {
            return false;
        }
        return true;

    }

    /**
     * Carica in memoria per tutti i ruoli l'elenco delle funzioni assegnate.
     *
     * N.B. L'unico ruolo presente è il ruolo costante USER che deve essere settato nel metodo authenticate
     * dell'authentication provider dell'applicazione.
     *
     * @return java.util.Hashtable Hashtable contenente l'elenco delle funzioni assegnate ad ogni ruolo. Campo chiave =
     *         new String("<ID_RUOLO>-<ID_FUNZ>"); Campo valore = new Integer(0)
     * @exception net.project.errors.AppCrash.
     */
    @Override
    public Map makeFunctionList() throws AppCrash {

        // crea l'hashtable dei ruoli, contenente l'unico ruolo fisso USER
        _roleList = new HashMap();
        _roleList.put("USER", new Integer(0));
        // l'hashtable contenente l'elenco delle funzioni assegnate ad ogni ruolo non esiste.
        return null;
    }

    /**
     * Crea una hashtable contenente tutti i menù assegnati all'applicazione. Note: copiato da
     * net.project.servlet.security.DBSecurityProvider
     *
     * @exception net.project.errors.AppCrash.
     * @return java.util.Hashtable Hashtable contenente l'elenco dei Menu' assegnati al generico unico ruolo USER. L'id
     *         del ruolo è usato come campo chiave (String), il Menu è usato come valore (Menu)
     */
    @Override
    public Map makeMenu() throws AppCrash {

        Map menuList = new HashMap();

        Iterator roles = _roleList.keySet().iterator();
        String role_id = null;
        List menu = null;

        // Scandaglio l'elenco dei ruoli assegnati all'applicazione
        while (roles.hasNext()) {
            role_id = (String) roles.next();
            // Per ogni ruolo viene creato il suo menu' partendo dal menu' principale "0"
            DataSetMenu dsMenu = new DataSetMenu(role_id, getApplId());
            menu = dsMenu.readMenu();
            // Inserisco il menu generato nella lista dei menu
            menuList.put(role_id.trim(), menu);
        }
        return menuList;
    }

}
