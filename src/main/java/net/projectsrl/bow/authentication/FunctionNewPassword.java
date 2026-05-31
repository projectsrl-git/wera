
package net.projectsrl.bow.authentication;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.servlet.frame.ApplicationServices_itf;
import net.projectsrl.dafne.db.PasswordDAO;
import net.projectsrl.dafne.db.UtentiDAO;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.webapp.authentication.FunctionNewPassword_base;
import net.projectsrl.webapp.authentication.LoginData;
import net.projectsrl.webapp.core.WebAppUtils;

public class FunctionNewPassword extends FunctionNewPassword_base {

    private static final String FUNCTION_HOME = "Home";
    private static final int DAYS_PASSWORD_VALIDITY = 60;    

    public FunctionNewPassword(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);

    }

    @Override
    protected String getHomeFunction() {

        return FUNCTION_HOME;
    }

    @Override
    protected void storeNewPassword(LoginData loginData, String encryptedPassword) throws AppCrash {

        PjNDAO_base userDAO = new UtentiDAO();

        userDAO.setAttribute(UtentiDAO.USERNAME, loginData.getUser());

        ErrDetector.GetInstance().postCond(userDAO.retrieve(), "user not found - user:" + loginData.getUser());

        Integer userId = (Integer) userDAO.getAttribute(UtentiDAO.ID_UTENTE);

        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, DAYS_PASSWORD_VALIDITY);
        date = cal.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ITALY);
        String expirationDate = sdf.format(date);

        PjNDAO_base passwordDAO = new PasswordDAO();

        passwordDAO.setAttribute(PasswordDAO.ID_UTENTE, userId);
        passwordDAO.setAttribute(PasswordDAO.PASSWORD, encryptedPassword);
        passwordDAO.setAttribute(PasswordDAO.DT_SCADENZA, expirationDate);
        passwordDAO.insert();

    }

    @Override
    protected String encryptPassword(String password) throws AppCrash {

        return WebAppUtils.encryptSHA1(password);

    }

}
