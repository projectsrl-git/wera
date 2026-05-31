/*
  AuthenticationProvider.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 25/10/2000

  Autore: Simone Z.

  Note:

  Modifiche:

 */

package net.project.servlet.security;

import java.lang.reflect.Constructor;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.errors.ParamCrash;
import net.project.misc.Config;

/**
 * Questa classe implementa un AuthenticationPovider_itf che non esegue alcuna autenticazione. E' utile per quelle
 * applicazioni che non hanno bisogno di autenticare gli utenti. Essa contiene il factory method per la creazione degli
 * AuthenticationProvider veri e propri
 */
public class AuthenticationProvider implements AuthenticationProvider_itf {

    /**
     * Constructor di default, non fa nulla
     */
    public AuthenticationProvider() {

        super();
    }

    /**
     * Questo e' il factory method per la creazione degli AuthenticationProvider veri e propri. Il nome della classe da
     * istanziare e' letto dalla configurazione passata recuperando la proprieta'
     * <code> servlet.authentication.class </code>. Se tale proprieta' non e' trovata viene istanziata questa classe
     * stessa.
     *
     * @param configType java.lang.String Tipo di implementazione.
     * @param functionName java.lang.String Nome della funzione.
     * @exception net.project.errors.AppCrash.
     * @return AuthenticationProvider_itf
     */
    public static AuthenticationProvider_itf MakeAuthenticationProvider(String configType, String functionName)
            throws AppCrash {

        AuthenticationProvider_itf obj = null;
        try {
            ErrDetector.GetInstance().param(functionName);
        } catch (ParamCrash pc) {
            pc.logContext("AuthenticationProvider", "Parametro mancante: function name = " + functionName);
            throw (pc);
        }
        // Recupero dal file di configurazione il tipo di implementazione da instanziare.
        String className = Config.GetInstance(configType).getProperty(
                new String("Servlet." + functionName + ".authentication.class"));
        if (className == null) {
            // Se la proprieta' servlet.authentication.class non e' trovata viene istanziata questa classe stessa.
            return new AuthenticationProvider();
        }
        try {
            Class c = Class.forName(className);
            Constructor factory = c.getConstructor(new Class[] { String.class });
            obj = (AuthenticationProvider_itf) factory.newInstance(new Object[] { configType });
        } catch (Throwable ex) {
            AppCrash err = new AppCrash(ex);
            err.logContext("AuthenticationProvider", "Errore durante la creazione dell'oggetto " + className + " - "
                    + ex.getMessage());
            throw (err);
        }
        return (obj);
    }

    /**
     * Metodo dummy di autenticazione.
     *
     * @param user net.project.servlet.security.UserSecurityInfo.
     * @param identity java.lang.Object.
     * @exception net.project.errors.AppCrash.
     */
    @Override
    public void authenticate(UserSecurityInfo user, Object identity) throws AppCrash {

        user.setUserId("NoUser");
        user.setRoleId("NoAuthenticationRequired");
        Logger.GetInstance().log0("WARNING: e' stato invocato il dummy authenticate di AuthenticationProvider");
    }

}
