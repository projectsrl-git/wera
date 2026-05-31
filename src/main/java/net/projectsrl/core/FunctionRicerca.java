/*
 * Created on 6-apr-2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

package net.projectsrl.core;

import java.util.HashMap;

import freemarker.template.SimpleNumber;
import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.wm.core.FunctionWM_base;
import net.projectsrl.wm.utils.Utils;

/**
 * FunctionRicerca
 * 
 */
public class FunctionRicerca extends FunctionWM_base {

    private String _pageImposta = "";
    private String _pageRisultati = "";
    // PARTE NUOVA - INIZIO
    private static final String DATASET_DIP = "DataSetDipendentiCerca";
    // PARTE NUOVA - FINE

    public FunctionRicerca() {

        super();
    }

    public FunctionRicerca(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @SuppressWarnings("unchecked")
	public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

    	HashMap templateData=(HashMap) setCommonTags(req,userInfo);
    	//String idDipendenteSessione = (String) req.getSession(false).getAttribute("ID_DIPENDENTE_SESSIONE");
    	String dirAllegatiFp=_applicationSrv.getRoot()+Config.GetInstance().getProperty("cartella.upload.richieste");
    	templateData.put("CARTELLA_FERIE_PERMESSI", dirAllegatiFp);
    	String dirModuliTrasferte=_applicationSrv.getRoot()+Config.GetInstance().getProperty("cartella.moduli.trasferte");
        templateData.put("CARTELLA_MODULI_TRASFERTE", dirModuliTrasferte);
        String dirModuliRendicontazioni=_applicationSrv.getRoot()+Config.GetInstance().getProperty("cartella.moduli.rendicontazioni");
        templateData.put("CARTELLA_MODULI_RENDICONTAZIONI", dirModuliRendicontazioni);
        String dirRendicontazioni=_applicationSrv.getRoot()+Config.GetInstance().getProperty("cartella.upload.rendicontazioni");
        templateData.put("CARTELLA_RENDICONTAZIONI", dirRendicontazioni);
        
        
        String dirAllegatiFpBreve=Config.GetInstance().getProperty("cartella.upload.richieste");
    	templateData.put("CARTELLA_FERIE_PERMESSI_BREVE", dirAllegatiFpBreve);
    	String dirModuliTrasferteBreve=Config.GetInstance().getProperty("cartella.moduli.trasferte");
        templateData.put("CARTELLA_MODULI_TRASFERTE_BREVE", dirModuliTrasferteBreve);
        String dirModuliRendicontazioniBreve=Config.GetInstance().getProperty("cartella.moduli.rendicontazioni");
        templateData.put("CARTELLA_MODULI_RENDICONTAZIONI_BREVE", dirModuliRendicontazioniBreve);
        String dirRendicontazioniBreve=Config.GetInstance().getProperty("cartella.upload.rendicontazioni");
        templateData.put("CARTELLA_RENDICONTAZIONI_BREVE", dirRendicontazioniBreve);
        
        
        
        
    	templateData.put("SUBMENU", getFunctionID());
    	templateData.put("SUBMENU_BREVE", getFunctionID().substring(0, 2));
    	templateData=setTemplateDataFromRequest(templateData,req);
    	templateData.put("ESITORICERCA",new SimpleNumber(0));
    	templateData.put("RUOLO_SESSIONE", getSessionRole(req));
    	templateData.put("APPROVA_FPS_SESSIONE", getSessionApprovaFPS(req));
    	templateData.put("APPROVA_RENDICONTAZIONI_SESSIONE", getSessionApprovaRendicontazioni(req));
    	templateData.put("ID_DIPENDENTE_SESSIONE", (String) req.getSession(false).getAttribute("ID_DIPENDENTE_SESSIONE"));
        templateData.put("AZIENDA_SESSIONE", (String) req.getSession(false).getAttribute("AZIENDA_SESSIONE"));
        templateData.put(PREFISSO_PARAMETRO, req.getField("PREFISSO_PARAMETRO"));
		templateData.put(DESCRIZIONE_PARAMETRO, req.getField("DESCRIZIONE_PARAMETRO"));
		templateData.put("ANNO_CALENDARIO_LAVORATIVO",Utils.getAnnoOggi());
		templateData.put("CERCA_SUBITO","S");
        _applicationSrv.displayPage(_pageImposta, templateData, setPageDatasetParam(_pageImposta, req,templateData), res);
    }

    
	@SuppressWarnings("unchecked")
	public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

		HashMap templateData = (HashMap) setCommonTags(req, userInfo);
		String dirAllegatiFp=_applicationSrv.getRoot()+Config.GetInstance().getProperty("cartella.upload.richieste");
		templateData.put("CARTELLA_FERIE_PERMESSI", dirAllegatiFp);
		String dirModuliTrasferte=_applicationSrv.getRoot()+Config.GetInstance().getProperty("cartella.moduli.trasferte");
        templateData.put("CARTELLA_MODULI_TRASFERTE", dirModuliTrasferte);
        String dirModuliRendicontazioni=_applicationSrv.getRoot()+Config.GetInstance().getProperty("cartella.moduli.rendicontazioni");
        templateData.put("CARTELLA_MODULI_RENDICONTAZIONI", dirModuliRendicontazioni);
        String dirRendicontazioni=_applicationSrv.getRoot()+Config.GetInstance().getProperty("cartella.upload.rendicontazioni");
        templateData.put("CARTELLA_RENDICONTAZIONI", dirRendicontazioni);
        
        String dirAllegatiFpBreve=Config.GetInstance().getProperty("cartella.upload.richieste");
    	templateData.put("CARTELLA_FERIE_PERMESSI_BREVE", dirAllegatiFpBreve);
    	String dirModuliTrasferteBreve=Config.GetInstance().getProperty("cartella.moduli.trasferte");
        templateData.put("CARTELLA_MODULI_TRASFERTE_BREVE", dirModuliTrasferteBreve);
        String dirModuliRendicontazioniBreve=Config.GetInstance().getProperty("cartella.moduli.rendicontazioni");
        templateData.put("CARTELLA_MODULI_RENDICONTAZIONI_BREVE", dirModuliRendicontazioniBreve);
        String dirRendicontazioniBreve=Config.GetInstance().getProperty("cartella.upload.rendicontazioni");
        templateData.put("CARTELLA_RENDICONTAZIONI_BREVE", dirRendicontazioniBreve);
        
		templateData.put("SUBMENU", getFunctionID());
		templateData.put("SUBMENU_BREVE", getFunctionID().substring(0, 2));
    	templateData=setTemplateDataFromRequest(templateData,req);
    	templateData.put("ESITORICERCA",new SimpleNumber(1));
		String report_dettagliato = req.getField("REPORT_DETTAGLIATO").toUpperCase();
		// REPORT DETTAGLIATO
		if (report_dettagliato.trim().equals("ON")) {
			templateData.put("REPORT_DETTAGLIATO","SI");
		} else {
			templateData.put("REPORT_DETTAGLIATO","NO");
		}
		templateData.put("RUOLO_SESSIONE", getSessionRole(req));
		templateData.put("APPROVA_FPS_SESSIONE", getSessionApprovaFPS(req));
    	templateData.put("APPROVA_RENDICONTAZIONI_SESSIONE", getSessionApprovaRendicontazioni(req));
		templateData.put("ID_DIPENDENTE_SESSIONE", (String) req.getSession(false).getAttribute("ID_DIPENDENTE_SESSIONE"));
        templateData.put("AZIENDA_SESSIONE", (String) req.getSession(false).getAttribute("AZIENDA_SESSIONE"));
        templateData.put("CERCA_SUBITO","");
        
        
        // parte nuova per archivio - inizio
        templateData.put("MATRICOLA", getDatiDipendente(req.getField("DIPENDENTE"))[0]);
		templateData.put("AZIENDA", getDatiDipendente(req.getField("DIPENDENTE"))[1]);
		if (templateData.get("AZIENDA_TENDINA")==null || templateData.get("AZIENDA_TENDINA").equals("")){
			templateData.put("AZIENDA_TENDINA", (String) req.getSession(false).getAttribute("AZIENDA_SESSIONE"));
		}
		putIdFeriePermessiInSession(req, templateData);
        // parte nuova per archivio - fine
		
		
		_applicationSrv.displayPage(_pageRisultati, templateData, setPageDatasetParam(_pageRisultati, req,templateData), res);

	}
	
	public void setPageImposta(String imposta) {
		_pageImposta = imposta;
	}

	public void setPageRisultati(String risultati) {
		_pageRisultati = risultati;
	}

	
	// parte nuova - inizio
	@SuppressWarnings("unchecked")
    private void putIdFeriePermessiInSession(SsbServletRequest req, HashMap templateData) throws AppCrash {

        
        if(getSessionRole(req).equals("D")){
        	templateData.put("AZIENDA_UPLOAD",(String) req.getSession(false).getAttribute("AZIENDA_CEDOLINI"));
        	templateData.put("MATRICOLA_UPLOAD", (String) req.getSession(false).getAttribute("MATRICOLA"));
        	 req.getSession(false).setAttribute(Costanti_itf.AZIENDA_FERIEPERMESSO, (String) req.getSession(false).getAttribute("AZIENDA_CEDOLINI"));
             req.getSession(false).setAttribute(Costanti_itf.MATRICOLA_FERIEPERMESSO, (String) req.getSession(false).getAttribute("MATRICOLA"));
             req.getSession(false).setAttribute(Costanti_itf.ID_DIPENDENTE_ARCHIVIO, req.getField("DIPENDENTE"));
        }else{
        	if ((String) templateData.get("DIPENDENTE")==null){
        		templateData.put("MATRICOLA", "");
        		templateData.put("AZIENDA", "");
                req.getSession(false).setAttribute(Costanti_itf.MATRICOLA_FERIEPERMESSO, "");
                req.getSession(false).setAttribute(Costanti_itf.AZIENDA_FERIEPERMESSO, "");
                req.getSession(false).setAttribute(Costanti_itf.ID_DIPENDENTE_ARCHIVIO, "");
        	}else{
        		templateData.put("MATRICOLA", getDatiDipendente((String) templateData.get("DIPENDENTE"))[0]);
        		templateData.put("AZIENDA", getDatiDipendente((String) templateData.get("DIPENDENTE"))[1]);
                req.getSession(false).setAttribute(Costanti_itf.MATRICOLA_FERIEPERMESSO, getDatiDipendente((String) templateData.get("DIPENDENTE"))[0]);
                req.getSession(false).setAttribute(Costanti_itf.AZIENDA_FERIEPERMESSO, getDatiDipendente((String) templateData.get("DIPENDENTE"))[1]);
                req.getSession(false).setAttribute(Costanti_itf.ID_DIPENDENTE_ARCHIVIO, req.getField("DIPENDENTE"));
        	}
        }
    }
	
	private String[] getDatiDipendente(String dipendente) throws AppCrash {
        String[] dati = {"",""};
        DataSet_itf dataSet = null;
        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_DIP);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("DIPENDENTE", dipendente);
            dataSet.setParam(params);
            dataSet.open();
            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
              	String matricola=dbRow.getField("MATRICOLA").toString().trim();
              	String azienda=dbRow.getField("AZIENDA").toString().trim();
            	dati[0]= matricola;
            	dati[1]= azienda;
            }
            dataSet.close();
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
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
	
	// parte nuova - fine
}
