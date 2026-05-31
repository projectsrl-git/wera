
package net.project.misc;

/*
 CacheKey_itf.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 13/06/2001

 Autore: Rosella V.

 Note:

 Modifiche:

 */

public interface CacheKey_itf {

    @Override
    public boolean equals(Object obj);

    @Override
    public int hashCode();
}