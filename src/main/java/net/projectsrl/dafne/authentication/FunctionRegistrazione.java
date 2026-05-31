/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.projectsrl.dafne.authentication;

import java.util.HashMap;
import java.util.Random;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.projectsrl.dafne.db.PasswordDAO;
import net.projectsrl.dafne.db.UtentiAziendeDAO;
import net.projectsrl.dafne.db.UtentiDAO;
import net.projectsrl.dafne.db.UtentiProfiliDAO;
import net.projectsrl.dafne.db.UtentiRegistrazioneDAO;
import net.projectsrl.dafne.db.UtentiUtenzeDAO;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.webapp.authentication.FunctionRegistrazione_base;
import net.projectsrl.webapp.core.WebAppUtils;
import net.projectsrl.webapp.security.PasswordGenerator;
import project.misc.Utils;

/**
 * Questa
 */
public class FunctionRegistrazione extends FunctionRegistrazione_base {

    private static final String DATASET_RESET_PASSWORD = "DataSetResetPassword";
    private static final String DATASET_UTENZA_DA_RILEVATORE = "DataSetUtenzaDaRilevatore";

    public FunctionRegistrazione(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    protected boolean isUniqueEmail(String email) throws AppCrash {

        ErrDetector.GetInstance().param(Util.IsNotEmpty(email), "email is empty");

        DataSet_itf dataSet = null;

        try {

            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_RESET_PASSWORD);

            HashMap<String, String> params = new HashMap<String, String>();
            params.put(EMAIL_FIELD, email);

            dataSet.setParam(params);
            dataSet.open();



            int userCounter = 0;
            while (dataSet.hasMoreElements()) {
                dataSet.nextElement();
                userCounter++;
            }

            if (userCounter > 1) {
            	ErrDetector.GetInstance().param(dataSet.hasMoreElements(), "utente già esistente - mail:" + email);
                return false;
            }

        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext(this.getClass().getName(),
                    "errore nella creazione della nuova registrazione per lo user con email=" + email);
            throw ac;
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (AppCrash ac) {
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
                }
            }
        }

        return true;
    }

    @Override
    protected String generateRandomPassword() throws AppCrash {

         Random random = new Random();

        int passwordLenght = random.nextInt(10) + 10;

        String password = (new PasswordGenerator()).generate(passwordLenght);

        return password;
    }

    @Override
    protected String encryptPassword(String password) throws AppCrash {

        return WebAppUtils.encryptSHA1(password);

    }

    @Override
    protected void storeEncryptedPassword(String encryptedPassword, String email) throws AppCrash {

        PjNDAO_base userDAO = new UtentiDAO();

        userDAO.setAttribute(UtentiDAO.EMAIL, email);

        ErrDetector.GetInstance().postCond(userDAO.retrieve(), "user not found - email:" + email);

        int userId = (int) userDAO.getAttribute(UtentiDAO.ID_UTENTE);

        PjNDAO_base passwordDAO = new PasswordDAO();

        passwordDAO.setAttribute(PasswordDAO.ID_UTENTE, userId);
        passwordDAO.setAttribute(PasswordDAO.PASSWORD, encryptedPassword);
        passwordDAO.setAttribute(PasswordDAO.DT_SCADENZA, Utils.getStringDataOggiRibaltata());
        passwordDAO.insert();

    }

    @Override
    protected String getUserNameFromEmailAddress(String email) throws AppCrash {

        PjNDAO_base userDAO = new UtentiDAO();

        userDAO.setAttribute(UtentiDAO.EMAIL, email);

        ErrDetector.GetInstance().postCond(userDAO.retrieve(), "user not found - email:" + email);

        String username = (String) userDAO.getAttribute(UtentiDAO.USERNAME);

        return username;

    }
    
    
    @Override
    protected void generateUser(String email, String nome, String cognome, String rilevatore) throws AppCrash {

        PjNDAO_base userDAO = new UtentiRegistrazioneDAO();

        userDAO.setAttribute(UtentiRegistrazioneDAO.USERNAME, email);
        userDAO.setAttribute(UtentiRegistrazioneDAO.EMAIL, email);
        userDAO.setAttribute(UtentiRegistrazioneDAO.NOME, nome);
        userDAO.setAttribute(UtentiRegistrazioneDAO.COGNOME, cognome);
        userDAO.setAttribute(UtentiRegistrazioneDAO.FL_DISATTIVO, false);
        userDAO.insert();

        ErrDetector.GetInstance().postCond(userDAO.retrieve(), "user not created - email:" + email);
        
        int userId = (int) userDAO.getAttribute(UtentiRegistrazioneDAO.ID_UTENTE);

        PjNDAO_base utentiUtenzeDAO = new UtentiUtenzeDAO();
        utentiUtenzeDAO.setAttribute(UtentiUtenzeDAO.ID_UTENTE, userId);
        utentiUtenzeDAO.setAttribute(UtentiUtenzeDAO.ID_UTENZA, getDatiUtenza(rilevatore)[0]);
        utentiUtenzeDAO.insert();
        
        PjNDAO_base utentiProfiliDAO = new UtentiProfiliDAO();
        utentiProfiliDAO.setAttribute(UtentiProfiliDAO.ID_UTENTE, userId);
        utentiProfiliDAO.setAttribute(UtentiProfiliDAO.ID_PROFILO, "4");
        utentiProfiliDAO.insert();
        
        PjNDAO_base utentiAziendeDAO = new UtentiAziendeDAO();
        utentiAziendeDAO.setAttribute(UtentiAziendeDAO.ID_UTENTE, userId);
        utentiAziendeDAO.setAttribute(UtentiAziendeDAO.ID_AZIENDA, getDatiUtenza(rilevatore)[1]);
        utentiAziendeDAO.insert();

    }
    
    
    
    private String[] getDatiUtenza(String rilevatore) throws AppCrash {
    	String[] dati= new String[2];
    	DataSet_itf dataSet = null;

        try {

            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_UTENZA_DA_RILEVATORE);

            HashMap<String, String> params = new HashMap<String, String>();
            params.put(RILEVATORE_FIELD, rilevatore);

            dataSet.setParam(params);
            dataSet.open();

            ErrDetector.GetInstance().param(dataSet.hasMoreElements(), "utenza non trovata - rilevatore:" + rilevatore);

            int userCounter = 0;
            while (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	dati[0] = dbRow.getField("ID_MODULO").toString();
            	dati[1] = dbRow.getField("ID_AZIENDA").toString();
                dataSet.nextElement();
                userCounter++;
            }

            if (userCounter > 1) {
                return dati;
            }

        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext(this.getClass().getName(),
                    "errore nella creazione della nuova registrazione per lo user con email=" + rilevatore);
            throw ac;
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (AppCrash ac) {
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
                }
            }
        }
		return dati;
	}

	

}
