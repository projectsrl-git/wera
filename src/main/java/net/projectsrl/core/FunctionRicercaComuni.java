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
import net.projectsrl.wm.core.FunctionWebApp_base;
import net.projectsrl.wm.utils.Utils;

/**
 * FunctionRicercaComuni
 * 
 */
public class FunctionRicercaComuni extends FunctionWebApp_base {

	public static final String PAGE = "ricerca_comuni";

	public FunctionRicercaComuni() {

		super();
	}

	public FunctionRicercaComuni(ApplicationServices_itf applServices, String functionID, String functionName) {

		super(applServices, functionID, functionName);
	}

	public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

		elabora(req,res,userInfo);
	}
	
	@SuppressWarnings("unchecked")
	public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

		HashMap templateData=setTemplateDataFromRequest(new HashMap(),req);
		
		String comune=eliminaSiglaProvinciaDaDescrizioneComune(req.getField("COMUNE"));
		
		templateData.put("COMUNE",comune);
		
		// F:CAMPO_COMUNE=;F:CAMPO_PROVINCIA=;F:CAMPO_CAP=;
		
		String stringaCampi=req.getField("CAMPI");
		
		if (!stringaCampi.equals("")) {
			String campoComune=Utils.leggeStringaElencoCampi("CAMPO_COMUNE",stringaCampi);
			String campoComuneProvincia=Utils.leggeStringaElencoCampi("CAMPO_COMUNE_PROVINCIA",stringaCampi);
			String campoProvincia=Utils.leggeStringaElencoCampi("CAMPO_PROVINCIA",stringaCampi);
			String campoCap=Utils.leggeStringaElencoCampi("CAMPO_CAP",stringaCampi);
			String campoSuccessivo=Utils.leggeStringaElencoCampi("CAMPO_SUCCESSIVO",stringaCampi);
			
			templateData.put("CAMPO_COMUNE",campoComune);
			templateData.put("CAMPO_PROVINCIA",campoProvincia);
			templateData.put("CAMPO_CAP",campoCap);
			templateData.put("CAMPO_COMUNE_PROVINCIA",campoComuneProvincia);
			templateData.put("CAMPO_SUCCESSIVO",campoSuccessivo);
		}
		
		_applicationSrv.displayPage(PAGE,templateData,setPageDatasetParam(PAGE, req,templateData), res);
	}

	@SuppressWarnings("unchecked")
	protected HashMap prepareWhereCondition(SsbServletRequest req, HashMap<String, String> queryParameter) {
		
		String comune=eliminaSiglaProvinciaDaDescrizioneComune(req.getField("COMUNE"));
		
		queryParameter.put("COMUNE",comune);
		
		return queryParameter;
	}

	private String eliminaSiglaProvinciaDaDescrizioneComune(String comune) {
		
		if (comune.equals("")) {
			comune="Inserire una stringa da ricercare";
		} else {
			if (comune.contains("(") && comune.contains(")")) {
				comune=comune.substring(0,comune.indexOf("(")).trim();
			}
		}
		
		return comune;

	}
}
