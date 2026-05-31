
package net.project.misc;

/*
 Repository.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 08/06/2001

 Autore: Rosella V.

 Note:

 Modifiche:

 */

import java.util.Hashtable;

import net.project.errors.AppCrash;

public class Repository {

    public static final String NUM_CACHE         = "Repository.numCache";
    public static final String NAME_CACHE_PREFIX = "Repository.Cache";
    public static final String NAME_CACHE_SUFFIX = ".name";
    private static Repository  _Instance;
    private Hashtable          _repository;

    private ResourceWatcher    _reloadFile       = null;

    /**
     * Costruttore. Viene letta la proprieta' di configurazione <code>Repository.reloadFile</code>; se presente indica
     * il file da utilizzare come risorsa da monitorare per il reload automatico del repository: se quel file viene
     * modificato il repository viene ricaricato. Questa feature e' da utilizzare sono in sviluppo/test
     * 
     */
    private Repository() throws AppCrash {

        // inizializzazione repository
        _repository = new Hashtable(10, 0.75F);
        init();
        String reload = Config.GetInstance().getProperty("Repository.reloadFile", "NONE");

        if (reload.equalsIgnoreCase("NONE")) {
            return;
        }
        _reloadFile = new ResourceWatcher(reload, 5000);

    }

    /**
     * Ritorna l'istanza della classe. Il reload in caso di accesso concorrente provoca un doppio reload.
     * 
     * @exception net.project.errors.AppCrash.
     * @return net.project.misc.Repository.
     */
    public static Repository GetInstance() throws AppCrash {

        // questo e' un "Double checked lock" design pattern
        boolean reload = false;
        if (_Instance != null) {
            reload = _Instance.shouldReload();
        }
        if (_Instance == null || reload) {
            synchronized (Repository.class) {
                if (_Instance == null || reload) {
                    _Instance = new Repository();
                }
            }
        }
        return _Instance;
    }

    /**
     * Ritorna l'istanza di Cache dal repository corrispondente alla chiave passata.
     * 
     * @param key java.lang.Object chiave della Cache da recuperare
     */
    public Cache get(Object key) {

        return (Cache) _repository.get(key);
    }

    /**
     * Aggiunge una istanza di Cache al repository.
     * 
     * @param key java.lang.String chiave della cache
     * @param cache net.project.misc.Cache cache da aggiungere al repository
     */
    public void put(String key, Cache cache) {

        _repository.put(key, cache);
    }

    /**
     * Invalida l'istanza di Cache dal repository corrispondente alla chiave passata.
     * 
     * @param key java.lang.Object chiave della Cache da recuperare
     */
    public void invalidate(Object key) {

        _repository.remove(key);
    }

    /**
     * Verifica se il repository deve essere ricaricato o meno
     * 
     * @return true se deve essere ricaricato
     */
    public boolean shouldReload() {

        if (_reloadFile != null && _reloadFile.hasBeenModified()) {
            return true;
        } else {
            return false;
        }

    }

    private void init() throws AppCrash {

        // leggo il numero di cache da inizializzare
        String nCache = Config.GetInstance().getProperty(NUM_CACHE);
        int numCache = 0;
        try {
            numCache = Integer.parseInt(nCache.trim());
            String nameCache = "";
            if ((numCache > 0)) {
                // carico nel repository le cache
                for (int i = 1; i <= numCache; i++) {
                    nameCache = Config.GetInstance().getProperty(NAME_CACHE_PREFIX + i + NAME_CACHE_SUFFIX).trim();
                    _repository.put(nameCache, CacheFactory.MakeCache(nameCache));
                }
            }
        } catch (Throwable e) {
            AppCrash appCrash = new AppCrash(e);
            appCrash.logContext("Repository", "Num cache : " + numCache
                    + " ,Errore nella inizializzazione del repository ");
            throw appCrash;
        }

    }
}