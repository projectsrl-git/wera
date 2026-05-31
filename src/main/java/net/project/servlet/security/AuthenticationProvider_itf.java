/*
  AuthenticationProvider_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 25/10/2000

  Autore: Simone Z.

  Note:

  Modifiche:
  
 */

package net.project.servlet.security;

import net.project.errors.AppCrash;

/**
 * Questa interfaccia provvede a fornire il servizio di autenticazione dell'utente nel sistema per quanto riguarda gli
 * aspetti di sicurezza ( accesso alle funzioni)
 */
public interface AuthenticationProvider_itf {

    public final static String ERRORE_PARAMETRI = "ERRORE_PARAMETRI";

    /**
     * Questo metodo esegue l'autenticazione dell'utente. Esso prende in ingresso un oggeto UserSecurityInfo, ed un
     * generico object e popola il security token. Il security token e' la quantita' che viene poi usata per sapere a
     * quali funzioni l'utente puo' accedere.
     *
     * @param user net.project.servlet.security.UserSecurityInfo.
     * @param identity java.lang.Object.
     * @exception net.project.errors.AppCrash.
     * @return void
     */
    public void authenticate(UserSecurityInfo user, Object identity) throws AppCrash;

}
