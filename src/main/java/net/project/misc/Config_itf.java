/*
  Config_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione: 06/07/2000

  Autore: Simone Z.

  Note:

  Modifiche:

 */

package net.project.misc;

import java.io.OutputStream;

/**
 * Definisce l'interfaccia utilizzata per recuperare le proprieta' di configurazione delle applicazioni
 */
public interface Config_itf {

    /**
     * Recupera il valore della proprieta' specificata
     */
    public String getProperty(String propertyName);

    /**
     * Recupera il valore della proprieta' specificata se presente; altrimenti restiruisce il valore di default indicato
     */
    public String getProperty(String propertyName, String defaultVal);

    /**
     * Esegue il dump sullo stream passato di tutte le proprieta' definite; prima della lista delle proprieta' stampa la
     * stringa di header passata
     */
    public void dump(OutputStream out, String header);

    /**
     * Imposta il valore della proprieta' specificata
     */
    public void setProperty(String propertyName, String val);

    /**
     * Ritorna la stringa passata con in testa il valore della proprieta' di configurazione Application.root se essa non
     * e' vuota e se la stringa passata non inizia con '/' (ovvero se e' un path relativo). Non viene fatto nessun
     * controllo della reale esistenza del path.
     */
    public String makeAbsolutePath(String path);
}
