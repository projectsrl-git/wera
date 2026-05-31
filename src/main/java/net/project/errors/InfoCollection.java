/*
  InfoCollection.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 14/05/1999

  Autore: Ilaria

  Note:

  Modifiche:

 */

package net.project.errors;

import java.util.Hashtable;

public class InfoCollection extends ThreadLocal {

    public Hashtable _field = new Hashtable(); ;

    public InfoCollection() {

    }

    @Override
    public Object initialValue() {

        return new InfoCollection();
    }

    /**
     * Il metodo add mette nell'hashtable il nome del campo con il suo valore
     */
    public void add(String infoNome, Object info) {

        _field.put(infoNome, info);
    }

    /**
     * Il metodo add mette nell'hashtable l'Hashtable passata dopo aver recuperato la chiave
     */
    public void add(Hashtable info) {

        java.util.Iterator iter = info.keySet().iterator();
        while (iter.hasNext()) {
            String name = (String) iter.next();
            _field.put(name, info.get(name));
        }

    }

    /*
     * Il metodo remove cancella dall'HashTable la chiaveche viene passata
     */

    public void remove(String infoNome) {

        _field.remove(infoNome);
    }

    /*
     * Il metodo remove cancella dall'HashTable la chiaveche viene passata
     */

    public void remove(String[] infoNames) {

        for (int i = 0; i < infoNames.length; i++) {
            _field.remove(infoNames[i]);
        }

    }

    /*
     * Il metodo resetInfo pulisce l'HashTable dalle informazioni
     */

    public void resetInfo() {

        _field.clear();
    }

    public Hashtable getInfo() {

        return _field;
    }

}
