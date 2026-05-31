/*
  UserSecurityInfo.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 06/10/2000

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.servlet.security;

/**
 * Contiene le informazioni minime necessarie da tenere nella sessione utente ai fini della sicurezza. La classe
 * UserSessionInfo che rappresenta la sessione di un utente eredita da questa classe.
 */
public class UserSecurityInfo {

    // Costante per indicare che lo userid dell'utente non e' a disposizione
    static public final String NOUSER         = "NoUser";

    private String             _IPAddress     = null;
    private String             _roleId        = null;
    private String             _userId        = null;
    private String             _applicationId = null;

    /**
     * Costruttore di default
     */
    public UserSecurityInfo() {

        super();
    }

    /**
     * Costruttore con identificatore ruolo
     * 
     * @param roleId String Identificatore del ruolo
     */
    public UserSecurityInfo(String roleId) {

        super();
        _roleId = roleId;
    }

    /**
     * Costruttore con identificatore ruolo e user id
     * 
     * @param roleId java.lang.String Identificatore del ruolo
     * @param userId java.lang.String Identificatore dello user
     */
    public UserSecurityInfo(String roleId, String userId) {

        super();
        _roleId = roleId;
        _userId = userId;
    }

    /**
     * Costruttore con UserSecurityInfo.
     * 
     * @param userInfo net.project.servlet.security.UserSecurityInfo.
     */
    public UserSecurityInfo(UserSecurityInfo userInfo) {

        setIPAddress(userInfo.getIPAddress());
        setRoleId(userInfo.getRoleId());
        setUserId(userInfo.getUserId());
        setApplicationId(userInfo.getApplicationId());
    }

    /**
     * Restituisce l'identificativo del ruolo
     * 
     * @return String Identificativo del ruolo
     */
    public String getRoleId() {

        return (_roleId);
    }

    /**
     * Imposta l'identificativo del ruolo.
     * 
     * @param value java.lang.String Identificativo del ruolo.
     */
    public void setRoleId(String value) {

        _roleId = value;
    }

    /**
     * Restituisce l'identificativo dell'utente
     * 
     * @return String Identificativo dell'utente
     */
    public String getUserId() {

        return (_userId);
    }

    /**
     * Imposta l'identificativo dell'utente.
     * 
     * @param value java.lang.String Identificativo dell'utente.
     */
    public void setUserId(String value) {

        _userId = value;
    }

    /**
     * Restituisce l'identificativo dell'applicazione
     * 
     * @return String Identificativo dell'applicazione
     */
    public String getApplicationId() {

        return (_applicationId);
    }

    /**
     * Imposta l'identificativo dell'applicazione.
     * 
     * @param value java.lang.String Identificativo dell'applicazione.
     */
    public void setApplicationId(String value) {

        _applicationId = value;
    }

    /**
     * Restituisce l'indirizzo IP
     * 
     * @return String Indirizzo IP
     */
    public String getIPAddress() {

        return (_IPAddress);
    }

    /**
     * Imposta l'indirizzo IP.
     * 
     * @param value java.lang.String Indirizzo IP.
     */
    public void setIPAddress(String value) {

        _IPAddress = value;
    }

    /**
     * Copia il contenuto dell'oggetto UserSecurityInfo passato se diverso da null
     */
    public void copy(UserSecurityInfo userInfo) {

        if (userInfo == null) return;

        setIPAddress(userInfo.getIPAddress());
        setRoleId(userInfo.getRoleId());
        setUserId(userInfo.getUserId());
        setApplicationId(userInfo.getApplicationId());
    }

}
