/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.projectsrl.wera.ripartizioniuni2018.core;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;


public class FunctionRicercaRipartizioniUNI2018 extends FunctionProjectWebApp_base {

    public FunctionRicercaRipartizioniUNI2018(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }
    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

	     // Oggi
	     Calendar today = Calendar.getInstance();
	
	     // Inizio mese (1° giorno)
	     Calendar startOfMonth = (Calendar) today.clone();
	     startOfMonth.set(Calendar.DAY_OF_MONTH, 1);
	
	     // Fine mese (ultimo giorno)
	     Calendar endOfMonth = (Calendar) startOfMonth.clone();
	     endOfMonth.set(Calendar.DAY_OF_MONTH, startOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
	
	     // Formatta le date
	     String dataInizioStr = sdf.format(startOfMonth.getTime());
	     String dataFineStr = sdf.format(endOfMonth.getTime());
	
	     // Costruisci la clausola WHERE
	     String whereCond = "to_char(DATE (ru.ts_ins::timestamp::date), 'YYYY/MM/DD') >= '" + dataInizioStr + 
	                        "' AND to_char(DATE (ru.ts_ins::timestamp::date), 'YYYY/MM/DD') <= '" + dataFineStr + "'";
	
	     templateData.put("WHERECONDITION", whereCond);

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }
}
