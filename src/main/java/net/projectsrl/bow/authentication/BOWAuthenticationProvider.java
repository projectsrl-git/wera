
package net.projectsrl.bow.authentication;

import java.sql.Timestamp;

import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.dafne.core.DafneCostanti_itf;
import net.projectsrl.dafne.core.ProfiliUtente;
import net.projectsrl.dafne.db.UtentiDAO;
import net.projectsrl.webapp.authentication.UsernamePasswordAuthenticationProvider;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.webapp.core.WebAppUtils;
import net.projectsrl.webapp.security.WebAppUserSecurityInfo;

public class BOWAuthenticationProvider extends UsernamePasswordAuthenticationProvider<Integer> {

    public BOWAuthenticationProvider() {

        super();
    }

    public BOWAuthenticationProvider(String configName) {

        super(configName);
    }

    @Override
    protected void setSpecificUserSecurityInfo(WebAppUserSecurityInfo<Integer> userInfo, Row_itf dbRow)
            throws AppCrash {

        super.setSpecificUserSecurityInfo(userInfo, dbRow);
        setUserLastLoginTimestamp(userInfo);

        String includedDivsAndForm = Config.GetInstance().getProperty("Page.DivsAndForm.include");
        userInfo.setField(DafneCostanti_itf.INCLUDED_DIVS_AND_FORMS, includedDivsAndForm);

        String includedHeader = Config.GetInstance().getProperty("Page.Header.include");
        userInfo.setField(DafneCostanti_itf.INCLUDED_HEADER, includedHeader);

        String includedBoxTitle = Config.GetInstance().getProperty("Page.BoxTitle.include");
        userInfo.setField(DafneCostanti_itf.INCLUDED_BOX_TITLE, includedBoxTitle);

        String includedHeadInsertForm = Config.GetInstance().getProperty("Page.HeadInsertForm.include");
        userInfo.setField(DafneCostanti_itf.INCLUDED_HEAD_INSERT_FORM, includedHeadInsertForm);

        String includedFooter = Config.GetInstance().getProperty("Page.Footer.include");
        userInfo.setField(DafneCostanti_itf.INCLUDED_FOOTER, includedFooter);
        
        String includedBoxCrediti = Config.GetInstance().getProperty("Page.BoxCrediti.include");
        userInfo.setField(DafneCostanti_itf.INCLUDED_BOX_CREDITI, includedBoxCrediti);

        configureUserCompanies(userInfo, dbRow);
        
        configureUserDepartments(userInfo, dbRow);
        
        configureUserProfiles(userInfo, dbRow);
        
        configureUserProfilesLetture(userInfo, dbRow);
        
        configureUserProfilesErrori(userInfo, dbRow);


    }

    private void configureUserDepartments(WebAppUserSecurityInfo<Integer> userInfo, Row_itf dbRow) throws AppCrash {

        String profilo = (String) dbRow.getField(WebAppConstants_itf.PROFILO);        
        String direzioniUtente = (String) dbRow.getField(DafneCostanti_itf.DIREZIONI_UTENTE);
        String whereConditionDirezioni = "  IN (" + direzioniUtente + ") ";
        
        userInfo.setField(DafneCostanti_itf.WHERECONDITION_DIREZIONI, whereConditionDirezioni);
    }

    protected void configureUserCompanies(WebAppUserSecurityInfo<Integer> userInfo, Row_itf dbRow) throws AppCrash {

        String aziendeUtenti = (String) dbRow.getField(DafneCostanti_itf.AZIENDE_UTENTE);
        userInfo.setField(DafneCostanti_itf.AZIENDE_UTENTE, aziendeUtenti.split(","));
        userInfo.setField(DafneCostanti_itf.LISTA_AZIENDE_UTENTE, aziendeUtenti);

        String aliasAziendaUtenti = (String) dbRow.getField(DafneCostanti_itf.ALIAS_AZIENDA_UTENTE);
        userInfo.setField(DafneCostanti_itf.ALIAS_AZIENDA_UTENTE, aliasAziendaUtenti);
        
        String codiceAziendaUtente = (String) dbRow.getField(DafneCostanti_itf.CODICE_AZIENDA_UTENTE);
        userInfo.setField(DafneCostanti_itf.AZIENDA_MENU, codiceAziendaUtente);
        userInfo.setField(DafneCostanti_itf.AZIENDA_SESSIONE, codiceAziendaUtente);

        String profilo = (String) dbRow.getField(WebAppConstants_itf.PROFILO);

        String whereConditionAziende = " IS NOT NULL ";
        if (!profilo.contains(ProfiliUtente.SUPER_ADMINISTRATOR.getCode())) {
            
            if (Util.IsNotEmpty(aziendeUtenti)) {
                whereConditionAziende = " IN (" + aziendeUtenti + ") ";
            }

        }
        userInfo.setField(DafneCostanti_itf.WHERECONDITION_AZIENDE, whereConditionAziende);
    }
    
    
    protected void configureUserProfiles(WebAppUserSecurityInfo<Integer> userInfo, Row_itf dbRow) throws AppCrash {

        String profilo = (String) dbRow.getField(WebAppConstants_itf.PROFILO);
        Integer idUtente = (Integer) dbRow.getField(WebAppConstants_itf.ID_UTENTE);

        String whereConditionProfili = " IS NOT NULL ";
        if (profilo.contains(ProfiliUtente.AMMINISTRATORE_CONDOMINIO.getCode())) {
           	whereConditionProfili = " IN (SELECT ID_MODULO FROM CONDOMINI WHERE ID_AMMINISTRATORE="+idUtente+") ";
        }
        if (profilo.contains(ProfiliUtente.CONDOMINO.getCode())) {
        	whereConditionProfili = " in (select id_condominio from utenze as ut where ut.id_modulo in (select id_utenza from utenti_utenze where id_utente="+idUtente+")) ";
        }
        userInfo.setField(DafneCostanti_itf.WHERECONDITION_PROFILI, whereConditionProfili);
    }
    
    
    
    protected void configureUserProfilesLetture(WebAppUserSecurityInfo<Integer> userInfo, Row_itf dbRow) throws AppCrash {

        String profilo = (String) dbRow.getField(WebAppConstants_itf.PROFILO);
        Integer idUtente = (Integer) dbRow.getField(WebAppConstants_itf.ID_UTENTE);

        String whereConditionProfiliLetture = " IS NOT NULL ";
        if (profilo.contains(ProfiliUtente.CONDOMINO.getCode())) {
        	whereConditionProfiliLetture = " in (select id_modulo from utenze as ut where ut.id_modulo in (select id_utenza from utenti_utenze where id_utente="+idUtente+")) ";
        }
        userInfo.setField(DafneCostanti_itf.WHERECONDITION_PROFILI_LETTURE, whereConditionProfiliLetture);
    }
    
    
    protected void configureUserProfilesErrori(WebAppUserSecurityInfo<Integer> userInfo, Row_itf dbRow) throws AppCrash {

        String profilo = (String) dbRow.getField(WebAppConstants_itf.PROFILO);

        String whereConditionProfiliErrori = "";
        String whereConditionProfiliFunzionanti = "";
        if (profilo.contains(ProfiliUtente.AMMINISTRATORE_CONDOMINIO.getCode())) {
        	whereConditionProfiliErrori = " and visibile = true and now()::date >= to_date(data_vis, 'YYYY-MM-DD') ";
        	whereConditionProfiliFunzionanti = " or (ANOMALIA<>'' and (visibile = false or (visibile = true and now()::date < to_date(data_vis, 'YYYY-MM-DD')) )) ";
        }
        userInfo.setField(DafneCostanti_itf.WHERECONDITION_PROFILI_ERRORI, whereConditionProfiliErrori);
        userInfo.setField(DafneCostanti_itf.WHERECONDITION_PROFILI_FUNZIONANTI, whereConditionProfiliFunzionanti);
    }
    

    private void setUserLastLoginTimestamp(WebAppUserSecurityInfo<Integer> userInfo) throws AppCrash {

        UtentiDAO utente = new UtentiDAO();
        utente.setAttribute(UtentiDAO.ID_UTENTE, userInfo.getIdUtente());
        utente.setAttribute(UtentiDAO.TS_LOGIN, new Timestamp(System.currentTimeMillis()));
        utente.update();
    }

    @Override
    protected boolean isValidPassword(String dbPassword, String reqPassword, String expireDate) throws AppCrash {
        
        String sha1ReqPwd=WebAppUtils.encryptSHA1(reqPassword);

        boolean isValidPassword = sha1ReqPwd.equals(dbPassword);

        return isValidPassword;
    }

}
