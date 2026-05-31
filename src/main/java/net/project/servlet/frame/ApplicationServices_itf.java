/*
  ApplicationServices_itf.java
	
  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 03/10/2000

  Autore: Anna L.

  Note:

  Modifiche:
  
 */

package net.project.servlet.frame;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.servlet.security.AuthenticationProvider_itf;
import net.project.servlet.security.SecurityProvider_itf;

/**
 * Definisce l'interfaccia che fornisce servizi alle servlet application.
 */
public interface ApplicationServices_itf {

    /**
     * Visualizza una pagina senza PageRootData e DataSourceParam.
     * 
     * @param pageName java.lang.String Il nome della pagina da visualizzare.
     * @param resp net.project.servlet.frame.SsbServletResponse La response http.
     * @return void
     */
    public void displayPage(String pageName, SsbServletResponse resp) throws AppCrash;

    /**
     * Visualizza una pagina con soli PageRootData.
     * 
     * @param pageName java.lang.String Il nome della pagina da visualizzare.
     * @param pageRootData java.util.Hashtable I parametri che valorizzano la pagina.
     * @param resp net.project.servlet.frame.SsbServletResponse La response http.
     * @return void
     */
    public void displayPage(String pageName, Map pageRootData, SsbServletResponse resp) throws AppCrash;

    /**
     * Visualizza una pagina con soli DataSourceParam.
     * 
     * @param pageName java.lang.String Il nome della pagina da visualizzare.
     * @param dataSourceParam Array di hashtable contenenti i parametri per i singoli dataset. L'array deve avere tanti
     *            campi quanti sono i diversi dataset presenti nella pagina. L'indice dell'array corrisponde al numero
     *            del dataset. Se uno dei dataset non prevede parametri, la corrispondente posizione nell'array dovrà
     *            essere a null.
     * @param resp net.project.servlet.frame.SsbServletResponse La response http.
     * @return void
     */
    public void displayPage(String pageName, Map dataSourceParam[], SsbServletResponse resp) throws AppCrash;

    /**
     * Visualizza una pagina con PageRootData e DataSourceParam.
     *
     * @param pageName java.lang.String Il nome della pagina da visualizzare.
     * @param pageRootData java.util.Hashtable I parametri che valorizzano la pagina.
     * @param dataSourceParam Array di hashtable contenenti i parametri per i singoli dataset. L'array deve avere tanti
     *            campi quanti sono i diversi dataset presenti nella pagina. L'indice dell'array corrisponde al numero
     *            del dataset. Se uno dei dataset non prevede parametri, la corrispondente posizione nell'array dovrà
     *            essere a null.
     * @param resp net.project.servlet.frame.SsbServletResponse La response http.
     * @return void
     */
    public void displayPage(String pageName, Map pageRootData, Map dataSourceParam[], SsbServletResponse resp)
            throws AppCrash;

    /**
     * Ricava lo stato dell'applicazione.
     *
     * @return integer
     */
    public int getStatus();

    /**
     * Ritorna il nome della configurazione.
     *
     * @return java.lang.String Il nome della configurazione.
     */
    public String getConfigName();

    /**
     * Ritorna un'istanza della classe SecurityProvider.
     *
     * @return net.project.servlet.security.SecurityProvider_itf.
     */
    public SecurityProvider_itf getSecurityProvider();

    /**
     * Ritorna un'istanza della classe AuthenticationProvider.
     *
     * @param functionName java.lang.String Nome della funzione.
     * @exception net.project.errors.AppCrash.
     * @return net.project.servlet.security.AuthenticationProvider_itf.
     */
    public AuthenticationProvider_itf getAuthenticationProvider(String functionName) throws AppCrash;

    /**
     * Ritorna true se il meccanismo di trace della servlet application e' abilitato
     * 
     * @return boolean true se trace abilitata
     */
    public boolean isTraceEnabled();

    /**
     * Questo metodo viene richiamato dalla doGet e doPost della function_base nel caso in cui il controllo sulla
     * presenza della sessione sia fallito. Le servlet application devono ridefire quel metodo in modo da ottenere il
     * comportameno voluto in caso di sessione scaduta. Di default il metodo esegue la displayPage() della pagina
     * 'SessioneScaduta' se esiste.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     * @return void
     */
    public void sessionExpired(SsbServletRequest req, SsbServletResponse res) throws AppCrash;

    /**
     * Questo metodo ritorna la root nel file system della macchina dove e' avvenuto il deployment della servlet
     * application (modulo war che la contiene)
     * 
     * @return String root del deployment
     */
    public String getRoot();

}
