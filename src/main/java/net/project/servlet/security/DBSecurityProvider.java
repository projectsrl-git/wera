/*
  DBSecurityProvider_base

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 09/10/2000

  Autore: Andrea R.

  Note:

  Modifiche:

 */

package net.project.servlet.security;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;

/**
 * Carica in memoria l'elenco dei menu' e dei ruoli partendo da un DB
 */
public class DBSecurityProvider extends SecurityProvider_base {

    private static final String ID_RUOLO  = "ID_RUOLO";
    private static final String ID_FUNZ   = "ID_FUNZ";
    private static final String ID_APPL   = "APPID";

    // Hashtable che riporta la lista dei ruoli associati all'applicazione
    // Campo chiave = new String("<ID_RUOLO>");
    // Campo valore = new Integer(0)
    private Map                 _roleList = null;

    /**
     * Costruttore
     *
     * @param applId java.lang.String Nome del file config.
     */
    public DBSecurityProvider(String applId) {

        super(applId);
    }

    /**
     * Carica in memoria per tutti i ruoli l'elenco delle funzioni assegnate.
     *
     * @return java.util.Hashtable Hashtable contenente l'elenco delle funzioni assegnate ad ogni ruolo. Campo chiave =
     *         new String("<ID_RUOLO>-<ID_FUNZ>"); Campo valore = new Integer(0)
     * @exception net.project.errors.AppCrash.
     */
    @Override
    public Map makeFunctionList() throws AppCrash {

        Map roleFunctionBind = new HashMap();
        _roleList = new Hashtable();
        DataSet_itf dataSet = null;

        try {
            Hashtable queryParameter = new Hashtable();
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", "FunctionList");
            queryParameter.put(ID_APPL, getApplId());
            dataSet.setParam(queryParameter);
            dataSet.open();
            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                roleFunctionBind.put((dbRow.getField(ID_RUOLO).toString().trim() + "-" + dbRow.getField(ID_FUNZ)
                        .toString().trim()), new Integer(0));
                _roleList.put(dbRow.getField(ID_RUOLO).toString(), new Integer(0));
            }
        } catch (AppCrash ex) {
            ex.logContext("DBSecurityProvider", "Errore in makeFunctionList");
            throw ex;
        } finally {
            // chiude il dataset
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (AppCrash ac) {
                    ac.logContext("DBSecurityProvider", "Errore nella close del dataset");
                    throw ac;
                }
            }
        }
        return roleFunctionBind;
    }

    /**
     * Crea una hashtable contenente tutti i menù asseganti a tutti i ruoli dell'applicazione.
     *
     * @exception net.project.errors.AppCrash.
     * @return java.util.Hashtable Hashtable contenente l'elenco dei Menu' assegnati ai ruoli. L'id del ruolo è usato
     *         come campo chiave (String), la List con il menu è usato come valore
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
            // Per ogni ruolo viene creato il suo menu'
            DataSetMenu dsMenu = new DataSetMenu(role_id, getApplId());
            menu = dsMenu.readMenu();
            // Inserisco il menu generato nella lista dei menu
            menuList.put(role_id.trim(), menu);
        }
        return menuList;
    }

}
