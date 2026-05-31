package net.projectsrl.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.fop.apps.FOPException;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.gui.PageFactory;
import net.project.servlet.gui.Page_itf;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.DbUtils;
import net.projectsrl.db.PjDAO_base;
import net.projectsrl.wm.core.FunctionWM_base;
import net.projectsrl.wm.utils.Utils;


/**
 * FunctionInserimento
 * 
 */
public class FunctionInserimentoSenzaControlloPreEsistenza extends FunctionWM_base {

	private String _pageMostra = "";
	private String _pageElabora = "";
	private String _datasetTestata="";
	
	public FunctionInserimentoSenzaControlloPreEsistenza() {
		super();
	}

	public FunctionInserimentoSenzaControlloPreEsistenza(ApplicationServices_itf applServices, String functionID, String functionName) {
		super(applServices, functionID, functionName);
	}

	@SuppressWarnings("unchecked")
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {
		
		HashMap templateData = (HashMap) setCommonTags(req, userInfo);
		templateData.put("ID_DIPENDENTE_SESSIONE", (String) req.getSession(false).getAttribute("ID_DIPENDENTE_SESSIONE"));
        templateData.put("AZIENDA_SESSIONE", (String) req.getSession(false).getAttribute("AZIENDA_SESSIONE"));
        
        if (!req.getField("ID_ALLEGATO").equals("") && req.getField("AZIONE").equals("aggiorna")){
        	String queryAggiorna="update "+req.getField("TABELLA")+" set descrizione='', non_lavorativo='', festivo='', chiusura_aziendale='' where "+req.getField("ID_TABELLA")+" = '"+req.getField("ID_ALLEGATO")+"'";
        	net.projectsrl.wm.utils.WMUtils.executeQuery(queryAggiorna);
        }
        String annoCalendario=req.getField("ANNO");
		templateData = loadVarStandard(templateData, req);
		templateData.put("ANNO", annoCalendario);
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
		
		if (req.getField("ASSOCIAZIONE").equals("COMMESSA") && req.getField("PREDEFINITA").equals("S")){
			String queryUpdate="UPDATE COMMESSA_X_DIPENDENTE SET PREDEFINITA='' WHERE ID_DIPENDENTE='"+req.getField("ID_DIPENDENTE")+"'";
			net.projectsrl.wm.utils.WMUtils.executeQuery(queryUpdate);
		}
		
		if (req.getField("ASSOCIAZIONE").equals("ATTIVITA") && req.getField("PREDEFINITA").equals("S")){
			String queryUpdate="UPDATE COMMESSA_X_ATTIVITA SET PREDEFINITA='' WHERE ID_COMMESSA='"+req.getField("ID_COMMESSA")+"'";
			net.projectsrl.wm.utils.WMUtils.executeQuery(queryUpdate);
		}
		
		if (req.getField("ASSOCIAZIONE").equals("PROFILO_ORARIO") && req.getField("PREDEFINITO").equals("S")){
			String queryUpdate="UPDATE PROFILO_X_DIPENDENTE SET PREDEFINITO='' WHERE ID_DIPENDENTE='"+req.getField("ID_DIPENDENTE")+"'";
			net.projectsrl.wm.utils.WMUtils.executeQuery(queryUpdate);
		}
		
		String idToUpdate=req.getField("ID");
		String idUnivoco=req.getField("ID_UNIVOCO");
	    String sqlUpdate1 ="";
	    String sqlUpdate2 ="";
	    if (req.getField("TIPO").equals("PROFILO_ORARIO")){
    		sqlUpdate1 ="UPDATE PROFILO_X_DIPENDENTE SET PREDEFINITO = 'S' WHERE ID_PROXDIP='"+idUnivoco+"'";
	    	net.projectsrl.wm.utils.WMUtils.executeQuery(sqlUpdate1);  	
	    	sqlUpdate2 ="UPDATE PROFILO_X_DIPENDENTE SET PREDEFINITO = 'N' WHERE ID_DIPENDENTE='"+idToUpdate+"' AND ID_PROXDIP<>'"+idUnivoco+"'";
	    	net.projectsrl.wm.utils.WMUtils.executeQuery(sqlUpdate2);
	    }
	    
	    if (req.getField("TIPO").equals("ATTIVITA")){
    		sqlUpdate1 ="UPDATE COMMESSA_X_ATTIVITA SET PREDEFINITA = 'S' WHERE ID_COMATT='"+idUnivoco+"'";
	    	net.projectsrl.wm.utils.WMUtils.executeQuery(sqlUpdate1);  	
	    	sqlUpdate2 ="UPDATE COMMESSA_X_ATTIVITA SET PREDEFINITA = 'N' WHERE ID_COMMESSA='"+idToUpdate+"' AND ID_COMATT<>'"+idUnivoco+"'";
	    	net.projectsrl.wm.utils.WMUtils.executeQuery(sqlUpdate2);
	    }
	    
	    if (req.getField("TIPO").equals("COMMESSA")){
	    	sqlUpdate1 ="UPDATE COMMESSA_X_DIPENDENTE SET PREDEFINITA = 'S' WHERE ID_COMDIP='"+idUnivoco+"'";
	    	net.projectsrl.wm.utils.WMUtils.executeQuery(sqlUpdate1);  	
	    	sqlUpdate2 ="UPDATE COMMESSA_X_DIPENDENTE SET PREDEFINITA = 'N' WHERE ID_DIPENDENTE='"+idToUpdate+"' AND ID_COMDIP<>'"+idUnivoco+"'";
	    	net.projectsrl.wm.utils.WMUtils.executeQuery(sqlUpdate2);
	    }
	    

		templateData = saveVarStandard(templateData, req, res);
		
		// se salvo i dati da ora in avanti sono in modifica dela testata
		//
		//templateData.put(OPZIONE_INSERIMENTO_MODIFICA, OPZIONE_MODIFICA); spostato nella saveVarStandard

		// quando mi appare la maschera di modifica della testata non devo avere
		// attiva l'opzione di inserimento dei dettagli altrimenti continua ad inserire
		
		if (req.getField("TIPO").equals("PROFILO_ORARIO")){
			String sqlDeleteVuoti="DELETE FROM PROFILO_X_DIPENDENTE WHERE ID_PROXDIP='' OR ID_PROFILO='' OR ID_DIPENDENTE=''";
		    net.projectsrl.wm.utils.WMUtils.executeQuery(sqlDeleteVuoti);
		}
		if (req.getField("TIPO").equals("ATTIVITA")){
			String sqlDeleteVuoti="DELETE FROM COMMESSA_X_ATTIVITA WHERE ID_COMATT='' OR ID_COMMESSA='' OR ID_ATTIVITA= ''";
		    net.projectsrl.wm.utils.WMUtils.executeQuery(sqlDeleteVuoti);
		}
		if (req.getField("TIPO").equals("COMMESSA")){
			String sqlDeleteVuoti="DELETE FROM COMMESSA_X_DIPENDENTE WHERE ID_COMDIP='' OR ID_COMMESSA='' OR ID_DIPENDENTE=''";
		    net.projectsrl.wm.utils.WMUtils.executeQuery(sqlDeleteVuoti);
		}
	    
		
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
				testataDAO.insert();
			} else {
			// se faccio MODIFICA 
				testataDAO.update();
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
	protected String salvaFormatoPDF(String fileName, String page, HashMap templateData, Map[] dataSourceParam,
            String xmlName, String xslName) throws AppCrash {

        if (fileName.contains("Modulitrasferte")) {
            String allegato = templateData.get("AZIENDA_TENDINA") + "/" + templateData.get("DIPENDENTE") + "/"
                    + templateData.get("ID_MODTRASFERTA") + "/" + Utils.normalizeASCIIFilename(fileName) + ".pdf";
            String queryUpdate = "UPDATE MOD_TRASFERTE SET ALLEGATO='" + allegato + "', FILENAME='"
                    + Utils.normalizeASCIIFilename(fileName) + "' WHERE ID_MODTRASFERTA='"
                    + templateData.get("ID_MODTRASFERTA") + "'";
            net.projectsrl.wm.utils.WMUtils.executeQuery(queryUpdate);
            fileName = _applicationSrv.getRoot() + Config.GetInstance().getProperty("cartella.moduli.trasferte")
                    + allegato;
        }

        if (fileName.contains("Rendicontazione")) {
            String allegato = templateData.get("AZIENDA_TENDINA") + "/" + templateData.get("DIPENDENTE") + "/"
                    + templateData.get("ID_RENDICONTAZIONE") + "/" + Utils.normalizeASCIIFilename(fileName) + ".pdf";
            String queryUpdate = "UPDATE RENDICONTAZIONI SET ALLEGATO='" + allegato + "', FILENAME='"
                    + Utils.normalizeASCIIFilename(fileName) + "' WHERE ID_RENDICONTAZIONE='"
                    + templateData.get("ID_RENDICONTAZIONE") + "'";
            net.projectsrl.wm.utils.WMUtils.executeQuery(queryUpdate);
            fileName = _applicationSrv.getRoot() + Config.GetInstance().getProperty("cartella.moduli.rendicontazioni")
                    + allegato;

        }

        if (fileName.contains("InfortunioINAIL")) {
            fileName = _applicationSrv.getRoot() + Config.GetInstance().getProperty("Page." + page + ".storePath", "./")
                    + "/" + templateData.get("AZIENDA_TENDINA") + "/Infortuni_INAIL/"
                    + Utils.normalizeASCIIFilename(fileName) + ".pdf";

            String queryUpdate = "UPDATE INFORTUNIO_INAIL SET ALLEGATO='" + "areadocumenti/"
                    + templateData.get("AZIENDA_TENDINA") + "/Infortuni_INAIL/InfortunioINAIL_"
                    + templateData.get("TAGGANCIO") + ".pdf" + "', FILENAME='InfortunioINAIL_"
                    + templateData.get("TAGGANCIO") + ".pdf' WHERE TAGGANCIO='" + templateData.get("TAGGANCIO") + "'";
            net.projectsrl.wm.utils.WMUtils.executeQuery(queryUpdate);
        }

        if (!fileName.contains("Modulitrasferte") && !fileName.contains("Rendicontazione")
                && !fileName.contains("InfortunioINAIL")) {
            fileName = _applicationSrv.getRoot() + Config.GetInstance().getProperty("Page." + page + ".storePath", "./")
                    + "/" + Utils.normalizeASCIIFilename(fileName) + ".pdf";
        }

        File baseDir = new File(_applicationSrv.getRoot());
        File xsltfile = new File(baseDir, "xsl/" + xslName);
        File xmltfile = new File(baseDir, "WEB-INF/template/" + xmlName);

        PageFactory pf = PageFactory.getInstance();

        Page_itf template = pf.makePage(page);

        template.setPageRootData(templateData);
        for (int i = 0; i < dataSourceParam.length; i++) {
            if (dataSourceParam[i] != null) {
                template.setDataSourceParam(dataSourceParam[i], i);
            }
        }

        writeByteArrayOutputStreamToPDFFile(fileName, template, xmltfile, xsltfile);
        return fileName;
    }

    private void writeByteArrayOutputStreamToPDFFile(String fileName, Page_itf template, File xmlFile, File xslFileName)
            throws AppCrash {
    
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
        String xmlFileName = xmlFile.getName() + ".tmp";
        // Creazione file XML
        try {
            PrintWriter pw = new PrintWriter(baos);
            template.display(pw);
            pw.flush();
    
            FileOutputStream fos = new FileOutputStream(xmlFileName, false);
            OutputStreamWriter wrtout = new OutputStreamWriter(fos);
    
            String s = baos.toString("UTF-8");
    
            wrtout.write(s);
    
            wrtout.flush();
            wrtout.close();
    
        } catch (Throwable e) {
            throw new AppCrash(e);
        }
    
        File pdfFile = new File(fileName);
    
        // Creazione file PDF
        try {
            PDFCreator.convertXML2PDF(xmlFileName, xslFileName, pdfFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FOPException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
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
