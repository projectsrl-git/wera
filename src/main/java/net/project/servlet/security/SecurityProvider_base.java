/*
  SecurityProvider_base

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 09/10/2000

  Autore: Andrea R.

  Note:

  Modifiche:

 */

package net.project.servlet.security;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.misc.Config;

/**
 * Fornisce le funzionalità base per la gestione della sicurezza del sistema: Gestione permessi e menù associati ai
 * ruoli.
 */
public abstract class SecurityProvider_base implements SecurityProvider_itf {

    private String _applId   = "";

    // Elenco dei ruoli caricati con la makeFunctionList()
    // KEY = String "<ID_RUOLO>-<ID_FUNZ>" dove:
    // ID_RUOLO è l'identificatore del ruolo e
    // ID_FUNZ è l'identificatore del della funzione
    // VALUE = Null
    private Map    _roleList = null;

    // Elenco dei menù caricati con la makeMenu()
    // KEY = String Identificatore del ruolo
    // VALUE = Menù (List contenente il menu)
    private Map    _menuList = null;

    /**
     * Restituisce una implementazione di ScurityProvider_itf
     *
     * @param idConfig java.lang.String Identificativo del file config assegnato alla applicazione
     * @exception net.project.errors.AppCrash.
     */
    public static SecurityProvider_itf MakeSecurityProvider(String idConfig) throws AppCrash {

        SecurityProvider_itf obj = null;

        // Recupero dal file di configurazione il tipo di implementazione da instanziare.
        String className = Config.GetInstance(idConfig).getProperty("servlet.SecurityProvider.class");
        if (className == null) {
            return null;
        }
        String applId = Config.GetInstance(idConfig).getProperty("servlet.SecurityProvider.applicationId");
        try {
            Class c = Class.forName(className);
            Constructor factory = c.getConstructor(new Class[] { String.class });
            obj = (SecurityProvider_itf) factory.newInstance(new Object[] { applId });
        } catch (Throwable ex) {
            throw (new AppCrash("Errore durante la creazione dell'oggetto " + className + " - " + ex.getMessage()));
        }
        return (obj);
    }

    /**
     * Realizza una instanza di SecurityProvider_base in base all'ID dell'applicazione fornita in input
     *
     * @param applId java.lang.String Identificativo dell'applicazione
     */
    public SecurityProvider_base(String applId) {

        _applId = applId;
    }

    /**
     * Carica in memoria per tutti i ruoli l'elenco dei id. menù assegnati
     *
     * @return java.util.Hashtable hashtable contenente l'elenco delle funzioni assegnate ad ogni ruolo. Campo chiave =
     *         new String("<ID_RUOLO>-<ID_FUNZ>"); Campo valore = new Integer(0)
     * @exception net.project.errors.AppCrash.
     */
    protected abstract Map makeFunctionList() throws AppCrash;

    /**
     * Crea una hashtable contente tutti i menù assegnati a tutti i ruoli.
     *
     * @exception net.project.errors.AppCrash.
     * @return java.util.Hashtable hashtable contenente l'elenco dei Menu' assegnati ai ruoli. L'id del ruolo è usato
     *         come campo chiave (String), il Menu è usato come valore (Menu)
     */
    protected abstract Map makeMenu() throws AppCrash;

    /**
     * LOG della transazione.
     *
     * @exception net.project.errors.AppCrash.
     */
    @Override
    public void logAudit() throws AppCrash {

        // deve ancora essere implementato
        return;
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

        if ((_roleList == null) && (_menuList == null)) {
            init();
        }

        boolean rc = false;

        try {
            if (Logger.GetInstance().getLogLevel() >= 3) {
                Logger.GetInstance().log3(
                        "CheckPermission: ruolo=" + (Object) role.getRoleId() + " funzione=" + functionID);
            }
            rc = _roleList.containsKey((Object) role.getRoleId() + "-" + functionID);
        } catch (Throwable ex) {
            throw (new AppCrash("Errore generico durante la verifica dei permessi per il ruolo " + role.getRoleId()));
        }
        return (rc);
    }

    /**
     * Questo metodo serve per recuperare il sotto menu' richiesto dall'albero dei menu' assegnati al ruolo specificato.
     * Se viene fornito null come identificativo del menu' viene restituito il menu' principale assegnato al ruolo
     * (tutto il suo albero).
     *
     * @param role net.project.servlet.security.UserSecurityInfo Ruolo di cui si deve ricercare il menù.
     * @param idMenu java.lang.String Identificatore del menù (alias della funzione). Se null si intende tutto il menù
     *            assegnato al ruolo, se diverso da null si intende il menù o il sottomenù di partenza.
     * @return net.project.servlet.menu.Menu_itf il reference al menu richiesto o null in caso di errore.
     * @exception net.project.errors.AppCrash.
     */
    @Override
    public List getMenu(UserSecurityInfo role, String idMenu) throws AppCrash {

        init();

        try {
            List root = (List) _menuList.get(role.getRoleId());
            ErrDetector.GetInstance().invariant(!(root == null), "Nessun menù associato al ruolo " + role.getRoleId());

            if (idMenu == null) return root;

            // Ricerco il sotto menù richiesto
            return SecurityProvider_base.findMenu(root, idMenu);
        } catch (ClassCastException ex) {
            throw (new AppCrash("Al ruolo " + role.getRoleId() + " non è stato assegnato un Menu"));
        } catch (Throwable ex) {
            throw (new AppCrash("Errore generico durante il reperimento del menù assegnato al ruolo "
                    + role.getRoleId()));
        }
    }

    /**
     * Trova un sotto menù del menù fornito in input. Se il sotto menù identificato dall' id. fornito in input non viene
     * trovato viene restituito null, altrimenti viene restituito il sotto menù trovato.
     *
     * @param root net.project.servlet.menu.Menu Menu di partenza.
     * @param id java.lang.String Identificativo del sotto menù da cercare.
     * @return net.project.servlet.menu.Menu Sottomenù identificato.
     * @exception net.project.errors.AppCrash.
     */
    private static List findMenu(List root, String id) throws AppCrash {

        List menu = new LinkedList();

        Iterator iter = root.iterator();
        while (iter.hasNext()) {
            MenuItem menuItem = (MenuItem) iter.next();

            if (menuItem.getMenuId().startsWith(id)) {
                menu.add(menuItem);
            }
        }

        return menu;
    }

    /**
     * Chiama i metodi makeFunctionList() e makeMenu() garantendo che entrambe le chiamate abbiano avuto esito positivo
     *
     * @exception net.project.errors.AppCrash.
     */
    protected synchronized void init() throws AppCrash {

        if ((_roleList == null) && (_menuList == null)) {
            try {
                _roleList = makeFunctionList();
                _menuList = makeMenu();
            } catch (AppCrash ex) {
                throw (new AppCrash(ex.getMessage()));
            } catch (Throwable ex) {
                throw (new AppCrash(ex));
            }
        }
        return;
    }

    /**
     * Effettua un reset dei permessi e dei menu associati ai ruoli.
     *
     * @exception net.project.errors.AppCrash.
     */
    @Override
    public synchronized void reset() throws AppCrash {

        // pulisce le hashtable
        if (_roleList != null) {
            _roleList.clear();
        }
        if (_menuList != null) {
            _menuList.clear();
        }
        // ricarica le hashtable
        try {
            _roleList = makeFunctionList();
            _menuList = makeMenu();
        } catch (AppCrash ex) {
            _roleList = null;
            _menuList = null;
            throw (new AppCrash(ex.getMessage()));
        } catch (Throwable ex) {
            _roleList = null;
            _menuList = null;
            throw (new AppCrash(ex));
        }
    }

    /**
     * @return Ritorna il campo applId.
     */
    public String getApplId() {

        return _applId;
    }
}
