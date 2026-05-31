/*
 * Created on 6-apr-2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

package net.projectsrl.core;

import java.util.HashMap;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.wm.utils.Utils;


/**
 * FunctionRicercaPreEsistenza
 * 
 */
public class FunctionRicercaPreEsistenza extends FunctionCrossover_base {


	public static final String PAGE = "ricerca_preesistenza";

	public FunctionRicercaPreEsistenza() {

		super();
	}

	public FunctionRicercaPreEsistenza(ApplicationServices_itf applServices, String functionID, String functionName) {

		super(applServices, functionID, functionName);
	}

	public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

		elabora(req,res,userInfo);
	}
	
	@SuppressWarnings("unchecked")
	public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

		HashMap templateData=setTemplateDataFromRequest(new HashMap(),req);
		
		String userid=(req.getField("USER"));
		
		templateData.put("USER",userid);
		
		// F:CAMPO_COMUNE=;F:CAMPO_PROVINCIA=;F:CAMPO_CAP=;
		
		String stringaCampi=req.getField("CAMPI");
		
		if (!stringaCampi.equals("")) {
			String campoComune=Utils.leggeStringaElencoCampi("CAMPO_COMUNE",stringaCampi);
			String campoProvincia=Utils.leggeStringaElencoCampi("CAMPO_PROVINCIA",stringaCampi);
			String campoSuccessivo=Utils.leggeStringaElencoCampi("CAMPO_SUCCESSIVO",stringaCampi);
			
			templateData.put("CAMPO_COMUNE",campoComune);
			templateData.put("CAMPO_PROVINCIA",campoProvincia);
			templateData.put("CAMPO_SUCCESSIVO",campoSuccessivo);
		}
		
		_applicationSrv.displayPage(PAGE,templateData,setPageDatasetParam(PAGE, req,templateData), res);
	}

	
	@Override	
	protected boolean checkSession(SsbServletRequest req) {
		return true;
	}   
	
	
	@SuppressWarnings("unchecked")
	protected HashMap prepareWhereCondition(SsbServletRequest req, HashMap<String, String> queryParameter) {
		String codice=(req.getField("CODICE"));

		
		if (codice.equals("")) {
			codice=(req.getField("COMUNE"));  //COMUNE
		}
		queryParameter.put("COMUNE",codice);
		return queryParameter;
	}
	


}
