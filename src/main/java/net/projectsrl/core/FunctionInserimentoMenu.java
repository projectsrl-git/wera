package net.projectsrl.core;

import java.util.HashMap;
import java.util.Iterator;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.DbUtils;
import net.projectsrl.db.PjDAO_base;
import net.projectsrl.wm.core.FunctionWM_base;
import net.projectsrl.wm.utils.Utils;


/**
 * FunctionInserimento
 * 
 */
public class FunctionInserimentoMenu extends FunctionWM_base {

	private String _pageMostra = "";
	private String _pageElabora = "";
	private String _datasetTestata="";
	
	public FunctionInserimentoMenu() {
		super();
	}

	public FunctionInserimentoMenu(ApplicationServices_itf applServices, String functionID, String functionName) {
		super(applServices, functionID, functionName);
	}

	@SuppressWarnings("unchecked")
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {
		
		HashMap templateData = (HashMap) setCommonTags(req, userInfo);
		templateData.put("ID_DIPENDENTE_SESSIONE", (String) req.getSession(false).getAttribute("ID_DIPENDENTE_SESSIONE"));
        templateData.put("AZIENDA_SESSIONE", (String) req.getSession(false).getAttribute("AZIENDA_SESSIONE"));
		templateData = loadVarStandard(templateData, req);
		templateData.put("RUOLO_SESSIONE", getSessionRole(req));
		templateData.put("SALVATO", "");
		templateData.put("SALVATO_REMINDER", "");
		templateData.put("ID_DIPENDENTE_SELEZIONATO", req.getField("ID_DIPENDENTE"));
		_applicationSrv.displayPage(_pageMostra, templateData, setPageDatasetParam(_pageMostra, req,templateData), res);
	}
	
	@SuppressWarnings("unchecked")
	public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

		HashMap templateData = (HashMap) setCommonTags(req, userInfo);

		if (refresh(_pageElabora,req, templateData, res)) {
			return;
		}
		
	    // pulisce template dettagli
		//
		templateData = pulisceTuttiTemplateDettagli(templateData);

		templateData = saveVarStandard(templateData, req, res);
		
		// se salvo i dati da ora in avanti sono in modifica dela testata
		//
		//templateData.put(OPZIONE_INSERIMENTO_MODIFICA, OPZIONE_MODIFICA); spostato nella saveVarStandard

		// quando mi appare la maschera di modifica della testata non devo avere
		// attiva l'opzione di inserimento dei dettagli altrimenti continua ad inserire
		//
		templateData=nessunaOpzioniDettagli(templateData);
		templateData.put("RUOLO_SESSIONE", getSessionRole(req));
		templateData.put("ID_DIPENDENTE_SESSIONE", (String) req.getSession(false).getAttribute("ID_DIPENDENTE_SESSIONE"));
        templateData.put("AZIENDA_SESSIONE", (String) req.getSession(false).getAttribute("AZIENDA_SESSIONE"));
		templateData.put("SALVATO", "Salvataggio effettuato con successo");
		templateData.put("SALVATO_REMINDER", "Ultimo salvataggio effettuato alle ore "+Utils.getOrario());
		_applicationSrv.displayPage(_pageElabora, templateData, setPageDatasetParam(_pageElabora, req,templateData), res);
		
	}

	/**
	* Impostare a NESSUNA_OPZIONE le OPZIONE_INSERIMENTO_MODIFICA_DETTAGLI
	* quando mi appare la maschera di modifica della testata non devo avere
	* attiva l'opzione di inserimento dei dettagli altrimenti continua ad inserire
	* 
	* Ad esempio:
	* 
	*  templateData.put(OPZIONE_INSERIMENTO_MODIFICA_DETTAGLI,NESSUNA_OPZIONE);
	*  
	*/
	@SuppressWarnings("unchecked")
	protected HashMap nessunaOpzioniDettagli(HashMap templateData)  {
		
		if (!ciSonoDettagli()) {
			return templateData;
		}		
		
		String[] arr = getArrayDsDettagli();

		for (int i = 0; i < arr.length; i++) {
			
			templateData.put("OPZIONE_INSERIMENTO_MODIFICA_DETTAGLI_"+(i+1),NESSUNA_OPZIONE);
			
		}
				
		return templateData;
	}


	@SuppressWarnings("unchecked")
	protected HashMap loadVar(HashMap templateData, SsbServletRequest req) throws AppCrash {
		return templateData;
	}
		
	@SuppressWarnings("unchecked")
	protected HashMap saveVar(HashMap templateData, SsbServletRequest req) throws AppCrash {
		return templateData;
	}
	

	@SuppressWarnings("unchecked")
	protected HashMap loadVarStandard(HashMap templateData, SsbServletRequest req) throws AppCrash {
		setTemplateDataFromRequest(templateData, req);

		templateData=loadVar(templateData, req);

		String option = req.getField(OPZIONE_INSERIMENTO_MODIFICA);

		if (option == null || option.equals("") || option.equals(OPZIONE_INSERIMENTO)) {
			templateData.put(OPZIONE_INSERIMENTO_MODIFICA, OPZIONE_INSERIMENTO);
			

			return templateData;

		}
		
		templateData=valorizzaTemplateDataDaDB(req, templateData,getDatasetTestata());

		return templateData;

	}	
	
	@SuppressWarnings("unchecked")
	protected HashMap saveVarStandard(HashMap templateData, SsbServletRequest req, SsbServletResponse res) throws AppCrash {
		
		templateData = setTemplateDataFromRequest(templateData, req);
		
		// verifica esistenza codice 
	    templateData.put("AZIENDA_SESSIONE",(String) req.getSession(false).getAttribute("AZIENDA_SESSIONE"));
			
		templateData=saveVar(templateData,req);
		
		try {

			PjDAO_base testataDAO = DbUtils.makeDAOFromDsName(getDatasetTestata());
			
			testataDAO = setDAOFieldsFromRequestPrivate(req, testataDAO);

			String option = req.getField(OPZIONE_INSERIMENTO_MODIFICA);
			
			// se faccio INSERIMENTO
			//
			if (option == null || option.equals("") || option.equals(OPZIONE_INSERIMENTO)) {
				//testataDAO.insert();
			} else {
			// se faccio MODIFICA 
				//testataDAO.update();
			}
			
			if (ciSonoDettagli()) {
				templateData=elaboraTuttiDettagli(req, templateData);
			}
			

		} catch (Throwable t) {
			AppCrash ap = new AppCrash(t);
			throw ap;
		}
	    templateData.put(OPZIONE_INSERIMENTO_MODIFICA, OPZIONE_MODIFICA);
		
		return templateData;
	}
	
	
	
	/**
	 * Popola la HashMap templateData in input con tuti i campi/valori provenienti dai campi di un PjDAO_base (in input)
	 * 
	 * @param HashMap templateData 
	 * @param PjDAO_base dao
	 * @return HashMap templateData 
	 * @throws AppCrash
	 */
	@SuppressWarnings("unchecked")
	protected HashMap putTemplateFieldFromDAO(HashMap templateData, PjDAO_base dao) throws AppCrash {
		// condizioni iniziali sui parametri
		//
		ErrDetector.GetInstance().preCond(templateData!=null, "putTemplateFieldFromDAO - templateData!=null");
		ErrDetector.GetInstance().preCond(dao!=null, "putTemplateFieldFromDAO - dao!=null");
		
		Iterator daoFields=dao.iterator();
		
		while (daoFields!=null && daoFields.hasNext()) {
			String element = (String) daoFields.next();
			String daoElement=element;
			if (element.startsWith("?")) {
				daoElement=element.substring(2);
			}			
			String elementValue=dao.getField(element);
			templateData.put(daoElement, elementValue);
		}
		
		
		return templateData;
	}
	
	
	/**
	 * Pulisce la HashMap templateData in input per tuti i campi provenienti dai campi di un PjDAO_base (in input)
	 * 
	 * @param HashMap templateData 
	 * @param PjDAO_base dao
	 * @return HashMap templateData 
	 * @throws AppCrash
	 */
	@SuppressWarnings("unchecked")
	protected HashMap clearTemplateFieldFromDAO(HashMap templateData, PjDAO_base dao) throws AppCrash {
		// condizioni iniziali sui parametri
		//
		ErrDetector.GetInstance().preCond(templateData!=null, "putTemplateFieldFromDAO - templateData!=null");
		ErrDetector.GetInstance().preCond(dao!=null, "putTemplateFieldFromDAO - dao!=null");
		
		Iterator daoFields=dao.iterator();
		
		while (daoFields!=null && daoFields.hasNext()) {
			String element = (String) daoFields.next();
			if (element.startsWith("?")) {
				element=element.substring(2);
			}						
			templateData.put(element, "");
		}
		
		
		return templateData;
	}
	
	

	
	
	@SuppressWarnings("unchecked")
	private HashMap pulisceTuttiTemplateDettagli(HashMap templateData) throws AppCrash {
		
		if (!ciSonoDettagli()) {
			return templateData;
		}		
		
		String[] arr = getArrayDsDettagli();

		for (int i = 0; i < arr.length; i++) {
			String dsName=arr[i];
			
			clearTemplateFieldFromDAO(templateData,DbUtils.makeDAOFromDsName(dsName));
			
		}
		
		return templateData;
	}

	
	
	@SuppressWarnings("unchecked")
	protected HashMap elaboraTuttiDettagli(SsbServletRequest req, HashMap templateData) throws AppCrash {
		
		String[] arr = getArrayDsDettagli();

		for (int i = 0; i < arr.length; i++) {
			String dsName=arr[i];
			
			templateData=elaboraSingoloDettaglio(i,dsName,templateData,req);
			
		}
		
		return templateData;
		
	}	
	
	
	@SuppressWarnings("unchecked")
	protected HashMap elaboraSingoloDettaglio(int i, String dsName, HashMap templateData, SsbServletRequest req) throws AppCrash {
		
		String optionDettagli = req.getField("OPZIONE_INSERIMENTO_MODIFICA_DETTAGLI_"+(i+1));
		
		if (optionDettagli != null && !optionDettagli.equals("") && optionDettagli.equals(OPZIONE_INSERIMENTO)) {
			inseriscoDettaglio(req,DbUtils.makeDAOFromDsName(dsName));
			// pulisce template dettagli
			templateData = clearTemplateFieldFromDAO(templateData,DbUtils.makeDAOFromDsName(dsName));
		} else if (optionDettagli != null && !optionDettagli.equals("") && optionDettagli.equals(OPZIONE_MODIFICA)) {
			templateData = valorizzaTemplateDataDaDB(req,templateData,dsName);
		} else if (optionDettagli != null && !optionDettagli.equals("") && optionDettagli.equals(OPZIONE_CANCELLA)) {
			cancellaDettagliDaDB(req,dsName);
			templateData = clearTemplateFieldFromDAO(templateData,DbUtils.makeDAOFromDsName(dsName));
		} else if (optionDettagli != null && !optionDettagli.equals("") && optionDettagli.equals(OPZIONE_DUPLICA)) {
			duplicaDettaglio(req,dsName);
		}
		return templateData;
	}

	@SuppressWarnings("unchecked")
	private void duplicaDettaglio(SsbServletRequest req, String dsName) throws AppCrash {
		PjDAO_base dettaglioDAO=DbUtils.makeDAOFromDsName(dsName);
		dettaglioDAO.setField(dettaglioDAO.getUniqueIdentifier(), req.getField(dettaglioDAO.getUniqueIdentifier()));
		dettaglioDAO.retrieve();
		
		PjDAO_base dettaglioDuplicatoDAO=DbUtils.makeDAOFromDsName(dsName);
		
		Iterator daoFields=dettaglioDuplicatoDAO.iterator();
		
		while (daoFields!=null && daoFields.hasNext()) {
			String element = (String) daoFields.next();
			dettaglioDuplicatoDAO.setField(element, dettaglioDAO.getField(element));
		}
		
		dettaglioDuplicatoDAO.setField(dettaglioDuplicatoDAO.getUniqueIdentifier(),Utils.getUnique());
		dettaglioDuplicatoDAO.insert();
		
	}

	protected void cancellaDettagliDaDB(SsbServletRequest req, String dsName) throws AppCrash {
		PjDAO_base dettaglioDAO=DbUtils.makeDAOFromDsName(dsName);
		dettaglioDAO.setField(dettaglioDAO.getUniqueIdentifier(), req.getField(dettaglioDAO.getUniqueIdentifier()));
		dettaglioDAO.retrieve();
		dettaglioDAO.delete();
		
	}

	@SuppressWarnings("unchecked")
	protected HashMap valorizzaTemplateDataDaDB(SsbServletRequest req, HashMap templateData, String dsName) throws AppCrash {
		PjDAO_base dao=DbUtils.makeDAOFromDsName(dsName);
		dao.setField(dao.getUniqueIdentifier(), req.getField(dao.getUniqueIdentifier()));
		dao.retrieve();
		
		templateData=putTemplateFieldFromDAO(templateData, dao);
		
		return templateData;
	}

	protected void inseriscoDettaglio(SsbServletRequest req,PjDAO_base dettaglioDAO) throws AppCrash {
		
		dettaglioDAO=setDAOFieldsFromRequestPrivate(req,dettaglioDAO);
		dettaglioDAO.setField("DAGGANCIO", req.getField("TAGGANCIO"));
		
		String id_dettaglio = req.getField(dettaglioDAO.getUniqueIdentifier());
		if (id_dettaglio.equals("")) {
			dettaglioDAO.setField(dettaglioDAO.getUniqueIdentifier(), Utils.getUnique());
			dettaglioDAO.insert();

		} else {
			dettaglioDAO.setField(dettaglioDAO.getUniqueIdentifier(), id_dettaglio);
			dettaglioDAO.update();
		}

	}	
	
	/*
	 * Prepara la where condition per le query di pagina Per agganciare i
	 * dettagli, l'unica condizione di JOIN che serve è TAGGANGIO=DAGGANCIO
	 * 
	 */
	@SuppressWarnings("unchecked")
	protected HashMap prepareWhereCondition(SsbServletRequest req, HashMap<String, String> queryParameter) {
		queryParameter.put("AZIENDA_SESSIONE", (String) req.getSession(false).getAttribute("AZIENDA_SESSIONE"));
		return queryParameter;
	}
	
	private String[] getArrayDsDettagli() {
		String elencoDataSet=Config.GetInstance().getProperty("DS." + _datasetTestata + ".ElencoDSDettagli");
		String[] arr = elencoDataSet.split("\\,");
		return arr;
	}

	public void setPageMostra(String page) {
		_pageMostra = page;
	}
	
	public void setPageElabora(String page) {
		_pageElabora = page;
	}
	
	public void setDatasetTestata(String page) {
		_datasetTestata = page;
	}
	
	public String getDatasetTestata() {
		return _datasetTestata;
	}
	
	protected boolean ciSonoDettagli() {
		String elencoDsDettagli=Config.GetInstance().getProperty("DS."+_datasetTestata+".ElencoDSDettagli");
		if (elencoDsDettagli!=null && !elencoDsDettagli.equals("")) {
			return true;
		} else {
			return false;
		}

	}
    /**
     * Popola il PjDAO_base dao in input con tuti i campi/valori provenienti dalla SsbServletRequest req (in input)
     * aventi gli stessi nomi dei campi del dao
     * 
     * @param SsbServletRequest req
     * @param PjDAO_base dao
     * @return PjDAO_base dao
     * @throws AppCrash
     */
    protected PjDAO_base setDAOFieldsFromRequestPrivate(SsbServletRequest req, PjDAO_base dao) throws AppCrash {

        // condizioni iniziali sui parametri
        //
        ErrDetector.GetInstance().preCond(req != null, "setDAOFieldsFromRequest - req!=null");
        ErrDetector.GetInstance().preCond(dao != null, "setDAOFieldsFromRequest - dao!=null");

        Iterator daoFields = dao.iterator();

        while (daoFields != null && daoFields.hasNext()) {
            String element = (String) daoFields.next();
            String reqElement = element;
            if (element.startsWith("?")) {
                reqElement = element.substring(2);
            }
            dao.setField(element, req.getField(reqElement));
        }

        return dao;
    }	
	
}
