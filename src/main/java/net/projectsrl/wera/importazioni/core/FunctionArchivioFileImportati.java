/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.projectsrl.wera.importazioni.core;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;


public class FunctionArchivioFileImportati extends FunctionProjectWebApp_base {

    public FunctionArchivioFileImportati(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }
    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

	     // Oggi
	     Calendar today = Calendar.getInstance();
	
	     // Calcola il giorno della settimana corrente (1 = domenica, 2 = lunedì, ...)
	     int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
	
	     // Calcola quanti giorni togliere per arrivare a lunedì
	     int daysToMonday = (dayOfWeek == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - dayOfWeek;
	
	     // Inizio settimana (lunedì)
	     Calendar startOfWeek = (Calendar) today.clone();
	     startOfWeek.add(Calendar.DAY_OF_MONTH, daysToMonday);
	
	     // Fine settimana (domenica)
	     Calendar endOfWeek = (Calendar) startOfWeek.clone();
	     endOfWeek.add(Calendar.DAY_OF_MONTH, 6);
	
	     // Formatta le date
	     String dataInizioStr = sdf.format(startOfWeek.getTime());
	     String dataFineStr = sdf.format(endOfWeek.getTime());
	
	     // Costruisci la clausola WHERE
	     String whereCond = "EFIR.DATA_IMPORT >= '" + dataInizioStr + "' AND EFIR.DATA_IMPORT <= '" + dataFineStr + "'";
	
	     templateData.put("WHERECONDITION", whereCond);

        
                
        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }
    
}
