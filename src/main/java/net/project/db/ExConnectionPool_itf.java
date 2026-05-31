/*

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore: Simone Z.

  Note:

 */

package net.project.db;

/**
 * Questa interfaccia rappresenta l'estensione dei ConnectionPool per la gestione sicura dei rilasci delle connessioni
 *
 * @author Simone Z.
 */
public interface ExConnectionPool_itf extends ConnectionPool_itf {

    /**
     * Esegue la free() di tutte le connessioni del thread chimante che non sono ancora state liberate. Il metodo
     * funziona solo se nelle proprieta' di configurazione e' definita la proprieta' DB.SafeConnection=true che indica
     * al singleton DBConnectionPool di istanziare un connection pool che permetta questo tipo di operazione
     */
    public void freeAllThreadConnections();
}