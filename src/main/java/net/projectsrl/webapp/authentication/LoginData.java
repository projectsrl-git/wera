/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.projectsrl.webapp.authentication;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Util;
import net.project.servlet.frame.SsbServletRequest;

public class LoginData {

    public static final String PASSWORD    = "PASSWORD";
    public static final String USERNAME    = "USERNAME";
    public static final String OLDPASSWORD = "OLDPASSWORD";

    private final String       _user;
    private final String       _password;

    public LoginData(SsbServletRequest req) {

        super();

        String password = null;
        String user = null;

        try {

            ErrDetector.GetInstance().param(req != null, "HttpServletRequest is null");

            user = req.getField(USERNAME);
            ErrDetector.GetInstance().param(Util.IsNotEmpty(user), "user is empty");
            user = user.toLowerCase();

            password = req.getField(PASSWORD);
            ErrDetector.GetInstance().param(Util.IsNotEmpty(password), "password is empty");

        } catch (Exception e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "errore nella creazione dell'oggetto LoginData");
        } finally {
            _password = password;
            _user = user;

        }

    }

    /**
     * @return Ritorna il campo user.
     */
    public String getUser() {

        return _user;
    }

    /**
     * @return Ritorna il campo password.
     */
    public String getPassword() {

        return _password;
    }

    @Override
    public String toString() {

        return "LoginData [_user=" + _user + ", _password=" + _password + ", toString()=" + super.toString() + "]";
    }

}
