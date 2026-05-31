
package net.projectsrl.dafne.db;

import java.sql.Timestamp;

import net.project.db.DBTransaction;
import net.project.db.NDAO_base;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.misc.Util;
import net.project.servlet.frame.SsbServletRequest;
import net.projectsrl.dafne.core.DafneCostanti_itf;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.webapp.core.WebAppUtils;

public class UtentiRegistrazioneDAO extends PjNDAO_base {

    private static final String PARAM_DT_SCADENZA = "dt_scadenza";

    private static final String TABLE_NAME        = "UTENTI";

    public static final String  ID_UTENTE         = "ID_UTENTE";
    public static final String  USERNAME          = "USERNAME";
    public static final String  COGNOME           = "COGNOME";
    public static final String  NOME              = "NOME";
    public static final String  EMAIL             = "EMAIL";
    public static final String  TS_LOGIN          = "TS_LOGIN";
    public static final String  FL_DISATTIVO      = "FL_DISATTIVO";
    public static final String  ID_UTENTE_INS     = "ID_UTENTE_INS";
    
    public static final String  INDIRIZZO              = "INDIRIZZO";
    public static final String  LOCALITA              = "LOCALITA";
    public static final String  PROVINCIA              = "PROVINCIA";
    public static final String  CAP              = "CAP";
    public static final String  CODFISC              = "CODFISC";
    public static final String  TELEFONO              = "TELEFONO";

    private String              _profili          = null;
    private String              _dataScadenzaPwd  = null;
    private String              _aziende          = null;
    private String              _utenze          = null;

    public UtentiRegistrazioneDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public UtentiRegistrazioneDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    /**
     * Questo metodo
     * 
     * @return
     * @throws AppCrash
     * 
     * @see net.ssb.db.NDAO_base#whereCondition()
     */
    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_UTENTE))) {
            appendField(ID_UTENTE, whereCondition);
        } else if (Util.IsNotEmpty(getAttribute(USERNAME))) {
            appendField(USERNAME, whereCondition);
        } else if (Util.IsNotEmpty(getAttribute(EMAIL))) {
            appendField(EMAIL, whereCondition);
        }

        return whereCondition;

    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_UTENTE, Integer.class);
        addNoStringField(TS_LOGIN, Timestamp.class);
        addNoStringField(FL_DISATTIVO, Boolean.class);
        addNoStringField(ID_UTENTE_INS, Integer.class);

    }

    protected void insertFirstExpiredPassword(Integer idUtente) throws AppCrash {

        if (Util.IsEmpty(_dataScadenzaPwd)) {
            // _dataScadenzaPwd = Utils.getStringDataOggiRibaltata();
            _dataScadenzaPwd = "2017/12/31";
        }

        String initialPwd = Config.GetInstance().getProperty("Utenti.Password.Iniziale");

        PasswordDAO pwdDao = new PasswordDAO();
        pwdDao.setAttribute(PasswordDAO.ID_UTENTE, idUtente);
        pwdDao.setAttribute(PasswordDAO.PASSWORD, WebAppUtils.encryptSHA1(initialPwd));
        pwdDao.setAttribute(PasswordDAO.DT_SCADENZA, _dataScadenzaPwd);
        pwdDao.setAttribute(PasswordDAO.FL_VALIDA, Boolean.TRUE);
        pwdDao.insert();
    }

    private void updateScadenzaPassword(Integer idUtente) throws AppCrash {

        if (Util.IsEmpty(_dataScadenzaPwd)) {
            return;
        }

        PasswordDAO pwdDao = new PasswordDAO();
        pwdDao.setAttribute(PasswordDAO.ID_UTENTE, idUtente);
        pwdDao.setAttribute(PasswordDAO.FL_VALIDA, Boolean.TRUE);
        pwdDao.setAttribute(PasswordDAO.DT_SCADENZA, _dataScadenzaPwd);
        pwdDao.update();

    }

    @Override
    public void insert() throws AppCrash {

        super.insert();

        retrieve();

        Integer idUtente = (Integer) getAttribute(ID_UTENTE);

        // inserisce lingua default
        NDAO_base lingua = new LingueISODAO();
        lingua.setAttribute(LingueISODAO.CODICE_ISO, WebAppConstants_itf.DEFAULT_ISO_LANGUAGE);
        ErrDetector.GetInstance().preCond(lingua.retrieve(),
                "not found CODICE_ISO:" + WebAppConstants_itf.DEFAULT_ISO_LANGUAGE);
        Integer idLinguaIso = (Integer) lingua.getAttribute(LingueISODAO.ID_LINGUE_ISO);

        NDAO_base linguaUtente = new UtentiLingueDAO();
        linguaUtente.setAttribute(ID_UTENTE, idUtente);
        linguaUtente.setAttribute(UtentiLingueDAO.ID_LINGUE_ISO, idLinguaIso);
        linguaUtente.setAttribute(UtentiLingueDAO.FL_DEFAULT, true);
        linguaUtente.insert();

        // inserisce profilo
        //new UtentiProfiliDAO().insert(_profili, idUtente);

        // inserisce aziende
        //new UtentiAziendeDAO().insert(_aziende, idUtente);
        
        //if (Util.IsNotEmpty(_profili) && _profili.contains("4")){
        	// inserisce utenze
        //   new UtentiUtenzeDAO().insert(_utenze, idUtente);
        //}
        

        // inserisce password
        //insertFirstExpiredPassword(idUtente);
    }

    @Override
    public void delete() throws AppCrash {

        String idUtente = getAttributeAsString(ID_UTENTE);

        new PasswordDAO().delete(idUtente);
        new UtentiLingueDAO().delete(idUtente);
        new UtentiProfiliDAO().delete(idUtente);
        new UtentiAziendeDAO().delete(idUtente);
       	new UtentiUtenzeDAO().delete(idUtente);

        super.delete();

    }

    @Override
    public void update() throws AppCrash {

        super.update();

        Integer idUtente = (Integer) getAttribute(ID_UTENTE);
        String idUtenteString = idUtente.toString();

        if (Util.IsNotEmpty(_profili)) {
            new UtentiProfiliDAO().delete(idUtenteString);
            new UtentiProfiliDAO().insert(_profili, idUtente);
        }

        if (Util.IsNotEmpty(_aziende)) {
            new UtentiAziendeDAO().delete(idUtenteString);
            new UtentiAziendeDAO().insert(_aziende, idUtente);
        }
        
        if (Util.IsNotEmpty(_profili) && _profili.contains("4")){
	        if (Util.IsNotEmpty(_utenze)) {
	            new UtentiUtenzeDAO().delete(idUtenteString);
	            new UtentiUtenzeDAO().insert(_utenze, idUtente);
	        }
        }

        updateScadenzaPassword(idUtente);

    }

    @Override
    public void setAttributesFromRequest(SsbServletRequest req) throws AppCrash {

        super.setAttributesFromRequest(req);

        _profili = req.getField(DafneCostanti_itf.PROFILO_MULTIPLO);
        _aziende = req.getField(DafneCostanti_itf.AZIENDE_MULTIPLE);
        _utenze = req.getField(DafneCostanti_itf.UTENZE_MULTIPLE);
        _dataScadenzaPwd = req.getField(PARAM_DT_SCADENZA);

    }

}