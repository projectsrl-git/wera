
package net.projectsrl.alibow.authentication;

import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.projectsrl.alibow.core.Constants_itf;
import net.projectsrl.alibow.core.ProfiliUtente;
import net.projectsrl.bow.authentication.BOWAuthenticationProvider;
import net.projectsrl.dafne.core.DafneCostanti_itf;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.webapp.security.WebAppUserSecurityInfo;

public class AliBOWAuthenticationProvider extends BOWAuthenticationProvider {

    public AliBOWAuthenticationProvider() {
        super();
    }

    public AliBOWAuthenticationProvider(String configName) {
        super(configName);
    }

    @Override
    protected void setSpecificUserSecurityInfo(WebAppUserSecurityInfo<Integer> userInfo, Row_itf dbRow)
            throws AppCrash {

        super.setSpecificUserSecurityInfo(userInfo, dbRow);

        String includedFooter = Config.GetInstance().getProperty("Page.Footer.PdLWeb.include");
        userInfo.setField(Constants_itf.INCLUDED_FOOTER_PDLWEB, includedFooter);

    }

    @Override
    protected void configureUserCompanies(WebAppUserSecurityInfo<Integer> userInfo, Row_itf dbRow) throws AppCrash {

        super.configureUserCompanies(userInfo, dbRow);
        
        String profilo = (String) dbRow.getField(WebAppConstants_itf.PROFILO);
        
        String whereConditionAziende =  userInfo.getField(DafneCostanti_itf.WHERECONDITION_AZIENDE);
        if (profilo.contains(ProfiliUtente.AGENZIA.getCode())) {
            whereConditionAziende = " IN (SELECT id_azienda from aziende where tipo_azienda like '%BIM%' and codice != 'SED') ";
        }
        userInfo.setField(DafneCostanti_itf.WHERECONDITION_AZIENDE, whereConditionAziende);
    }
    
    
    

}
