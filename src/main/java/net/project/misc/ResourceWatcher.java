/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.misc;

import java.io.File;

/**
 * Questa classe serve per sorvegliare una risorsa (file) in modo da poter conoscere se e' stata cambiata. Il controllo
 * vero e proprio avviene solo ad intervalli impostati dal parametro delay
 *
 * @author zorzetti
 */
public class ResourceWatcher {

    private File _resource;
    private int  _delay;
    private long _lastChecked;

    /**
     * Creates a new ResourceWatcher object.
     *
     * @param fileName file da sorvegliare
     * @param delay intervallo di controllo
     */
    public ResourceWatcher(String fileName, int delay) {

        // Non sono stati inseriti check dei parametri in ingresso perche' non e' possibile lanciare eccezioni
        // dato che viene utilizzato anche dalla classe Config (una eccezione creerebbe un loop)
        _resource = new File(fileName);
        _delay = delay;
        _lastChecked = 0;
    }

    /**
     * Creates a new ResourceWatcher object.
     *
     * @param file file da sorvegliare
     * @param delay intervallo di controllo
     */
    public ResourceWatcher(File file, int delay) {

        // Non sono stati inseriti check dei parametri in ingresso perche' non e' possibile lanciare eccezioni
        // dato che viene utilizzato anche dalla classe Config (una eccezione creerebbe un loop)
        _resource = file;
        _delay = delay;
        _lastChecked = 0;
    }

    /**
     * Questo metodo ritorna true se il file e' stato modificato dall'ultima volta che si e' effettuato il controllo. Il
     * controllo vero e proprio avviene solo ad intervalli impostati dal parametro delay
     *
     * @return
     */
    public boolean hasBeenModified() {

        long now = System.currentTimeMillis();

        if ((now - _lastChecked) <= _delay) {
            return false;
        }
        try {
            if (_resource.lastModified() > _lastChecked) {
                System.out.println("Reload" + _resource.getName());
                return true;
            }

            return false;
        } finally {
            _lastChecked = now;
        }
    }
}
