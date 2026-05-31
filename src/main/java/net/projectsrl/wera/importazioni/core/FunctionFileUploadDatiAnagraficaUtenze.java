package net.projectsrl.wera.importazioni.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.alibow.core.Constants_itf;
import net.projectsrl.dafne.core.DafneCostanti_itf;
import net.projectsrl.webapp.authentication.MenuItem;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.wera.utils.WeraUtils;
import net.projectsrl.wm.importdata.UploadedFiles;
import net.projectsrl.wm.utils.Utils;



/**
 * FunctionFileUpload
 * 
 */
@SuppressWarnings("deprecation")
public class FunctionFileUploadDatiAnagraficaUtenze extends UtilsControlliImportAnagraficaUtenzeXLS {

	private static final String PAGE = "importdatianagraficautenze";
	private static final int _sizeMax = 100000000;

	public FunctionFileUploadDatiAnagraficaUtenze() {

		super();
	}

	public FunctionFileUploadDatiAnagraficaUtenze(ApplicationServices_itf applServices, String functionID,
			String functionName) {

		super(applServices, functionID, functionName);
	}

	public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

		Map<String, Object> templateData = createMapFromRequest(req, userInfo);

		templateData.put(UploadedFiles.FILE_TYPE, req.getField(UploadedFiles.FILE_TYPE));
		templateData.put("MESSAGGIO_ATTESA", "Trasferimento file in corso...");

		_applicationSrv.displayPage(PAGE, templateData, res);
	}

	public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

		uploadFiles(req);
		
		String nomeFile = (String) req.getSession(false).getAttribute("FILE_NAME_CEDOLINO");
		String msg="";
		if (nomeFile.toLowerCase().endsWith(".xlsx")){
			msg = creaAnagraficaXLSX(req, res, userInfo);
			 
		}else{
			msg = creaAnagraficaXLS(req, res, userInfo);
		}
		

		//String msg = creaAnagrafica(req, res, userInfo);

		// Map<String, Object> templateData = createMapFromRequest(req,
		// userInfo);
		// templateData.put("MSG", msg);
		req.getSession(false).setAttribute("MSG", msg);

		// _applicationSrv.displayPage(PAGE,
		// templateData,setPageDatasetParam(PAGE, req,templateData), res);

	}

	@Override
	protected String getPathDescription(Map<String, Object> map) {

		String pathDescri = "";

		try {
			MenuItem selectedMenuItem = (MenuItem) map.get(WebAppConstants_itf.SELECTED_MENU_ITEM);
			if (selectedMenuItem != null) {
				if (selectedMenuItem.getPathDescri() != null) {
					pathDescri = selectedMenuItem.getPathDescri();
				}
			}

		} catch (ClassCastException e) {

			String menuId = "11";
			String menuIdSup = "2";
			String languageISO = "it";
			String label = "Anagrafica Utenze";
			String function = "FileUploadAnagraficaUtenze";
			String link = "astro?FUNCTIONID=FileUploadAnagraficaUtenze";
			int itemLevel = 2;
			String path = "1520";
			String linkChain = "astro?FUNCTIONID=Home;#;astro?FUNCTIONID=FileUploadAnagraficaUtenze";
			boolean readOnly = false;
			int nrOfChildren = 0;
			String icon = "";

			pathDescri = "Home / Importazione dati / Anagrafica utenze";

			MenuItem selectedMenuItem = new MenuItem(menuId, menuIdSup, languageISO, label, function, link, itemLevel,
					path, pathDescri, linkChain, readOnly, nrOfChildren, icon);

			map.put(WebAppConstants_itf.SELECTED_MENU_ITEM, selectedMenuItem);

			map.put(WebAppConstants_itf.INCLUDED_MENU, "include/included_menu.include");
			map.put(WebAppConstants_itf.SELECTED_PATH, path);

		}

		return pathDescri;

	}

	@SuppressWarnings("deprecation")
	private boolean uploadFiles(SsbServletRequest req) {

		String fileType = req.getField(UploadedFiles.FILE_TYPE);

		String fileName = "";
		DiskFileUpload fu = new DiskFileUpload();
		// If file size exceeds, a FileUploadException will be thrown
		fu.setSizeMax(_sizeMax);
		try {
			List<FileItem> fileItems = fu.parseRequest(req);
			Iterator<FileItem> itr = fileItems.iterator();

			// ciclo per i file
			while (itr.hasNext()) {
				FileItem fi = (FileItem) itr.next();

				// Check if not form field so as to only handle the file inputs
				// else condition handles the submit button input
				if (!fi.isFormField()) {
					fileName = fi.getName();

					int positionOfLastSlash = fileName.lastIndexOf("\\");
					fileName = fileName.substring(positionOfLastSlash + 1);
					req.getSession(false).setAttribute("FILE_NAME_CEDOLINO", fileName);

					File fNew = new File(
							Config.GetInstance().getProperty("directory.external_files",_applicationSrv.getRoot()) + Config.GetInstance().getProperty("cartella.upload"), fileName);
					UploadedFiles.setStatus(fileType, UploadedFiles.UPLOADING, fileName, "");

					fi.write(fNew);

					UploadedFiles.setStatus(fileType, UploadedFiles.UPLOAD_COMPLETED, fileName, "");
				}
			}

			return true;

		} catch (Throwable e) {
			new AppCrash(e);
			UploadedFiles.setStatus(fileType, UploadedFiles.UPLOAD_ERROR, fileName, "");
			return false;
		}

	}

	private String creaAnagraficaXLS(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo)
			throws AppCrash {
		String dirSCARICO = Config.GetInstance().getProperty("directory.external_files",_applicationSrv.getRoot()) + Config.GetInstance().getProperty("cartella.upload");
		new File(dirSCARICO).mkdir();

		// SISTEMARE
		Integer azienda = WeraUtils.trovaIdAziendaUtente(getSpecificUserInfo(userInfo).getIdUtente().toString());
		

		String msg = "";

		try {
			
			String inFile = dirSCARICO + (String) req.getSession(false).getAttribute("FILE_NAME_CEDOLINO");

			String path = dirSCARICO + (String) req.getSession(false).getAttribute("FILE_NAME_CEDOLINO");
			String realPath = req.getServletContext().getRealPath(path);
			inFile=path;

			FileInputStream fileInputStream = new FileInputStream(inFile);
			
			
			HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);
			HSSFSheet worksheetNetwork = workbook.getSheet("Generale");

			Boolean importa = true;

			String msgCrediti = "";
			String msgSchede = "";
			String msgAntenne = "";
			String msgContatoriAcquaCalda = "";
			String msgContatoriRiscaldamento = "";
			String msgcontatoriMetano = "";
			String msgcontatoriElettrici = "";
			String msgContatoriCisterna = "";
			String msgRilevatori = "";
			String msgRilevatoriDuplicati = "";

			// verificaCrediti
			int creditiDaAcquistare = verificaCreditiXLS(userInfo, workbook);
			if (creditiDaAcquistare > 0) {
				msgCrediti = "Acquistare " + creditiDaAcquistare + " crediti" + Constants_itf.A_CAPO_HTML;
			}

			String schedeErrate = UtilsControlliImportAnagraficaUtenzeXLS.verificaNomeSchede(workbook);
			if (Utils.IsNotEmpty(schedeErrate)) {
				msgSchede = "Sistemare l'etichetta delle schede: " + schedeErrate + Constants_itf.A_CAPO_HTML;
				importa = false;
			}

			String antenneDoppie = UtilsControlliImportAnagraficaUtenzeXLS.verificaAntenne(worksheetNetwork);
			if (Utils.IsNotEmpty(antenneDoppie)) {
				msgAntenne = "Sistemare l'anagrafica antenne: " + antenneDoppie + Constants_itf.A_CAPO_HTML;
				importa = false;
			}

			String contatoriAcquaCaldaDoppi = UtilsControlliImportAnagraficaUtenzeXLS.verificaContatoriAcquaCalda(worksheetNetwork);
			if (Utils.IsNotEmpty(contatoriAcquaCaldaDoppi)) {
				msgContatoriAcquaCalda = "Sistemare l'anagrafica contatori acqua calda: " + contatoriAcquaCaldaDoppi
						+ Constants_itf.A_CAPO_HTML;
				importa = false;
			}

			String contatoriRiscaldamentoDoppi = UtilsControlliImportAnagraficaUtenzeXLS.verificaContatoriRiscaldamento(worksheetNetwork);
			if (Utils.IsNotEmpty(contatoriRiscaldamentoDoppi)) {
				msgContatoriRiscaldamento = "Sistemare l'anagrafica contatori riscaldamento: "
						+ contatoriRiscaldamentoDoppi + Constants_itf.A_CAPO_HTML;
				importa = false;
			}

			String contatoriMetanoDoppi = UtilsControlliImportAnagraficaUtenzeXLS.verificaContatoriMetano(worksheetNetwork);
			if (Utils.IsNotEmpty(contatoriMetanoDoppi)) {
				msgcontatoriMetano = "Sistemare l'anagrafica contatori metano: " + contatoriMetanoDoppi
						+ Constants_itf.A_CAPO_HTML;
				importa = false;
			}

			String contatoriElettriciDoppi = UtilsControlliImportAnagraficaUtenzeXLS.verificaContatoriElettrici(worksheetNetwork);
			if (Utils.IsNotEmpty(contatoriElettriciDoppi)) {
				msgcontatoriElettrici = "Sistemare l'anagrafica contatori elettrici: " + contatoriElettriciDoppi
						+ Constants_itf.A_CAPO_HTML;
				importa = false;
			}

			String contatoriCisternaDoppi = UtilsControlliImportAnagraficaUtenzeXLS.verificaContatoriCisterna(worksheetNetwork);
			if (Utils.IsNotEmpty(contatoriCisternaDoppi)) {
				msgContatoriCisterna = "Sistemare l'anagrafica contatori cisterna: " + contatoriCisternaDoppi
						+ Constants_itf.A_CAPO_HTML;
				importa = false;
			}

			String rilevatoriDoppi = UtilsControlliImportAnagraficaUtenzeXLS.verificaRilevatori(workbook);
			if (Utils.IsNotEmpty(rilevatoriDoppi)) {
				msgRilevatori = "Sistemare l'anagrafica rilevatori delle utenze: " + rilevatoriDoppi;
				importa = false;
			}

			String rilevatoriDuplicati = UtilsControlliImportAnagraficaUtenzeXLS.verificaUtenzeConStessoContatore(workbook);
			if (Utils.IsNotEmpty(rilevatoriDuplicati)) {
				msgRilevatoriDuplicati = "Sistemare il contenuto del file ed in particolare i seguenti rilevatori doppi: "
						+ rilevatoriDuplicati;
				importa = false;
			}

			try {
				msg=UtilsControlliImportAnagraficaUtenzeXLS.verificaUtenze(azienda, workbook);
			} catch (Exception e) {
				msg = "L'importazione del file non &egrave riuscita. Verificare il formato del contenuto del file ed in particolare: nome scheda, formule, lunghezza dei dati presenti nei campi del file excel."
						+ Constants_itf.A_CAPO_HTML;
				// e.printStackTrace();
				importa = false;
				return msg;
			}

			if (!importa) {
				msg = msg
						+ "Per procedere con l'importazione del file &egrave; necessario compiere le seguenti azioni preliminari:"
						+ Constants_itf.A_CAPO_HTML;
				msg = msg + msgCrediti + msgSchede + msgAntenne + msgContatoriAcquaCalda + msgContatoriRiscaldamento
						+ msgcontatoriMetano + msgcontatoriElettrici + msgContatoriCisterna + msgRilevatori+msgRilevatoriDuplicati;
				return msg;
			}

			if (msg.isEmpty()){
				String dataOggi = Utils.getStringDataOggiIndex();

				Integer idCondominio = UtilsInserimentiImportAnagraficaUtenzeXLS.inserimentoDatiCondominio(req, azienda, worksheetNetwork, dataOggi);

				UtilsInserimentiImportAnagraficaUtenzeXLS.inserimentoAntenne(azienda, worksheetNetwork, idCondominio, dataOggi);

				UtilsInserimentiImportAnagraficaUtenzeXLS.inserimentoContatoriAcquaCalda(azienda, worksheetNetwork, idCondominio, dataOggi);

				UtilsInserimentiImportAnagraficaUtenzeXLS.inserimentoContatoriRiscaldamento(azienda, worksheetNetwork, idCondominio, dataOggi);

				//UtilsInserimentiImportAnagraficaUtenzeXLS.inserimentoContatoriMetano(azienda, worksheetNetwork, idCondominio, dataOggi);

				UtilsInserimentiImportAnagraficaUtenzeXLS.inserimentoContatoriElettrici(azienda, worksheetNetwork, idCondominio, dataOggi);

				//UtilsInserimentiImportAnagraficaUtenzeXLS.inserimentoContatoriCisterna(azienda, worksheetNetwork, idCondominio, dataOggi);

				UtilsInserimentiImportAnagraficaUtenzeXLS.inserimentoUtenze(azienda, workbook, dataOggi, idCondominio);
			}
			

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			msg = "L'importazione del file non &egrave riuscita. Verificare il contenuto del file ed in particolare: nome scheda, formule, lunghezza dei dati presenti nei campi del file excel.";
			req.getSession(false).setAttribute("MSG", msg);
			e.printStackTrace();
		}

		/*
		 * // cancello cartelle non più utilizzate File cartellaUPLOAD = new
		 * File(_applicationSrv.getRoot() +
		 * Config.GetInstance().getProperty("cartella.upload")); File[]
		 * filesUPLOAD = cartellaUPLOAD.listFiles(); for (File f : filesUPLOAD)
		 * f.delete(); // fine cancellazione cartelle
		 */
		return msg;

		// updateApici();
	}
	
	
	
	
	
	private String creaAnagraficaXLSX(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo)
			throws AppCrash {
		String dirSCARICO = Config.GetInstance().getProperty("directory.external_files",_applicationSrv.getRoot()) + Config.GetInstance().getProperty("cartella.upload");
		new File(dirSCARICO).mkdir();

		Integer azienda = WeraUtils.trovaIdAziendaUtente(getSpecificUserInfo(userInfo).getIdUtente().toString());
		String msg = "";

		try {
			
			String inFile = dirSCARICO + (String) req.getSession(false).getAttribute("FILE_NAME_CEDOLINO");

			String path = dirSCARICO + (String) req.getSession(false).getAttribute("FILE_NAME_CEDOLINO");
			String realPath = req.getServletContext().getRealPath(path);
			inFile=path;
			
			FileInputStream fileInputStream = new FileInputStream(inFile);
			
			
			XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
			XSSFSheet worksheetNetwork = workbook.getSheet("Generale");

			Boolean importa = true;

			String msgCrediti = "";
			String msgSchede = "";
			String msgAntenne = "";
			String msgContatoriAcquaCalda = "";
			String msgContatoriRiscaldamento = "";
			String msgcontatoriMetano = "";
			String msgcontatoriElettrici = "";
			String msgContatoriCisterna = "";
			String msgRilevatori = "";
			String msgRilevatoriDuplicati = "";

			// verificaCrediti
			int creditiDaAcquistare = verificaCreditiXLSX(userInfo, workbook);
			if (creditiDaAcquistare > 0) {
				msgCrediti = "Acquistare " + creditiDaAcquistare + " crediti" + Constants_itf.A_CAPO_HTML;
			}

			String schedeErrate = UtilsControlliImportAnagraficaUtenzeXLSX.verificaNomeSchede(workbook);
			if (Utils.IsNotEmpty(schedeErrate)) {
				msgSchede = "Sistemare l'etichetta delle schede: " + schedeErrate + Constants_itf.A_CAPO_HTML;
				importa = false;
			}

			String antenneDoppie = UtilsControlliImportAnagraficaUtenzeXLSX.verificaAntenne(worksheetNetwork);
			if (Utils.IsNotEmpty(antenneDoppie)) {
				msgAntenne = "Sistemare l'anagrafica antenne: " + antenneDoppie + Constants_itf.A_CAPO_HTML;
				importa = false;
			}

			String contatoriAcquaCaldaDoppi = UtilsControlliImportAnagraficaUtenzeXLSX.verificaContatoriAcquaCalda(worksheetNetwork);
			if (Utils.IsNotEmpty(contatoriAcquaCaldaDoppi)) {
				msgContatoriAcquaCalda = "Sistemare l'anagrafica contatori acqua calda: " + contatoriAcquaCaldaDoppi
						+ Constants_itf.A_CAPO_HTML;
				importa = false;
			}

			String contatoriRiscaldamentoDoppi = UtilsControlliImportAnagraficaUtenzeXLSX.verificaContatoriRiscaldamento(worksheetNetwork);
			if (Utils.IsNotEmpty(contatoriRiscaldamentoDoppi)) {
				msgContatoriRiscaldamento = "Sistemare l'anagrafica contatori riscaldamento: "
						+ contatoriRiscaldamentoDoppi + Constants_itf.A_CAPO_HTML;
				importa = false;
			}

			String contatoriMetanoDoppi = UtilsControlliImportAnagraficaUtenzeXLSX.verificaContatoriMetano(worksheetNetwork);
			if (Utils.IsNotEmpty(contatoriMetanoDoppi)) {
				msgcontatoriMetano = "Sistemare l'anagrafica contatori metano: " + contatoriMetanoDoppi
						+ Constants_itf.A_CAPO_HTML;
				importa = false;
			}

			String contatoriElettriciDoppi = UtilsControlliImportAnagraficaUtenzeXLSX.verificaContatoriElettrici(worksheetNetwork);
			if (Utils.IsNotEmpty(contatoriElettriciDoppi)) {
				msgcontatoriElettrici = "Sistemare l'anagrafica contatori elettrici: " + contatoriElettriciDoppi
						+ Constants_itf.A_CAPO_HTML;
				importa = false;
			}

			String contatoriCisternaDoppi = UtilsControlliImportAnagraficaUtenzeXLSX.verificaContatoriCisterna(worksheetNetwork);
			if (Utils.IsNotEmpty(contatoriCisternaDoppi)) {
				msgContatoriCisterna = "Sistemare l'anagrafica contatori cisterna: " + contatoriCisternaDoppi
						+ Constants_itf.A_CAPO_HTML;
				importa = false;
			}

			String rilevatoriDoppi = UtilsControlliImportAnagraficaUtenzeXLSX.verificaRilevatori(workbook);
			if (Utils.IsNotEmpty(rilevatoriDoppi)) {
				msgRilevatori = "Sistemare l'anagrafica rilevatori delle utenze: " + rilevatoriDoppi;
				importa = false;
			}

			String rilevatoriDuplicati = UtilsControlliImportAnagraficaUtenzeXLSX.verificaUtenzeConStessoContatore(workbook);
			if (Utils.IsNotEmpty(rilevatoriDuplicati)) {
				msgRilevatoriDuplicati = "Sistemare il contenuto del file ed in particolare i seguenti rilevatori doppi: "
						+ rilevatoriDuplicati;
				importa = false;
			}

			try {
				msg=UtilsControlliImportAnagraficaUtenzeXLSX.verificaUtenze(azienda, workbook);
			} catch (Exception e) {
				msg = "L'importazione del file non &egrave riuscita. Verificare il formato del contenuto del file ed in particolare: nome scheda, formule, lunghezza dei dati presenti nei campi del file excel."
						+ Constants_itf.A_CAPO_HTML;
				// e.printStackTrace();
				importa = false;
				return msg;
			}

			if (!importa) {
				msg = msg
						+ "Per procedere con l'importazione del file &egrave; necessario compiere le seguenti azioni preliminari:"
						+ Constants_itf.A_CAPO_HTML;
				msg = msg + msgCrediti + msgSchede + msgAntenne + msgContatoriAcquaCalda + msgContatoriRiscaldamento
						+ msgcontatoriMetano + msgcontatoriElettrici + msgContatoriCisterna + msgRilevatori+msgRilevatoriDuplicati;
				return msg;
			}
			
			if (msg.isEmpty()){
				String dataOggi = Utils.getStringDataOggiIndex();

				Integer idCondominio = UtilsInserimentiImportAnagraficaUtenzeXLSX.inserimentoDatiCondominio(req, azienda, worksheetNetwork, dataOggi);

				UtilsInserimentiImportAnagraficaUtenzeXLSX.inserimentoAntenne(azienda, worksheetNetwork, idCondominio, dataOggi);

				UtilsInserimentiImportAnagraficaUtenzeXLSX.inserimentoContatoriAcquaCalda(azienda, worksheetNetwork, idCondominio, dataOggi);

				UtilsInserimentiImportAnagraficaUtenzeXLSX.inserimentoContatoriRiscaldamento(azienda, worksheetNetwork, idCondominio, dataOggi);

				//UtilsInserimentiImportAnagraficaUtenzeXLSX.inserimentoContatoriMetano(azienda, worksheetNetwork, idCondominio, dataOggi);

				UtilsInserimentiImportAnagraficaUtenzeXLSX.inserimentoContatoriElettrici(azienda, worksheetNetwork, idCondominio, dataOggi);

				//UtilsInserimentiImportAnagraficaUtenzeXLSX.inserimentoContatoriCisterna(azienda, worksheetNetwork, idCondominio, dataOggi);

				UtilsInserimentiImportAnagraficaUtenzeXLSX.inserimentoUtenze(azienda, workbook, dataOggi, idCondominio);

			}

			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			msg = msg+"L'importazione del file non &egrave riuscita. Verificare il contenuto del file ed in particolare: nome scheda, formule.";
			req.getSession(false).setAttribute("MSG", msg);
			e.printStackTrace();
		}

		/*
		 * // cancello cartelle non più utilizzate File cartellaUPLOAD = new
		 * File(_applicationSrv.getRoot() +
		 * Config.GetInstance().getProperty("cartella.upload")); File[]
		 * filesUPLOAD = cartellaUPLOAD.listFiles(); for (File f : filesUPLOAD)
		 * f.delete(); // fine cancellazione cartelle
		 */
		return msg;

		// updateApici();
	}

	

	

	

	private int verificaCreditiXLS(UserSecurityInfo userInfo, HSSFWorkbook workbook) throws AppCrash {
		int creditiNecessari = 0;

		for (int i = 1; i <= workbook.getNumberOfSheets() - 1; i++) {
			HSSFSheet worksheet = workbook.getSheet("Foglio " + i);
			for (int j = 11; j < 30; j++) {
				HSSFRow rowGrid = worksheet.getRow(j);
				HSSFCell cellL = rowGrid.getCell(11);
				cellL.setCellType(CellType.STRING);
				String etichetta = cellL.getStringCellValue();
				if (!etichetta.equals("")) {
					creditiNecessari = creditiNecessari + 1;
				}
			}
		}

		String whereConditionAziende = getSpecificUserInfo(userInfo).getField(DafneCostanti_itf.WHERECONDITION_AZIENDE);

		int creditiUtilizzabili = WeraUtils.contaCrediti(whereConditionAziende)
				- WeraUtils.contaCreditiUtilizzati(whereConditionAziende);
		int creditiDaAcquistare = creditiNecessari - creditiUtilizzabili;

		// Boolean crediti = creditiNecessari > creditiUtilizzabili;
		// return crediti;
		return creditiDaAcquistare;

	}
	
	
	
	private int verificaCreditiXLSX(UserSecurityInfo userInfo, XSSFWorkbook workbook) throws AppCrash {
		int creditiNecessari = 0;

		for (int i = 1; i <= workbook.getNumberOfSheets() - 1; i++) {
			XSSFSheet worksheet = workbook.getSheet("Foglio " + i);
			for (int j = 11; j < 30; j++) {
				XSSFRow rowGrid = worksheet.getRow(j);
				XSSFCell cellL = rowGrid.getCell(11);
				cellL.setCellType(CellType.STRING);
				String etichetta = cellL.getStringCellValue();
				if (!etichetta.equals("")) {
					creditiNecessari = creditiNecessari + 1;
				}
			}
		}

		String whereConditionAziende = getSpecificUserInfo(userInfo).getField(DafneCostanti_itf.WHERECONDITION_AZIENDE);

		int creditiUtilizzabili = WeraUtils.contaCrediti(whereConditionAziende)
				- WeraUtils.contaCreditiUtilizzati(whereConditionAziende);
		int creditiDaAcquistare = creditiNecessari - creditiUtilizzabili;

		// Boolean crediti = creditiNecessari > creditiUtilizzabili;
		// return crediti;
		return creditiDaAcquistare;

	}

	

	

	

	

	private Boolean countCondominioDoppi(String denominazione, String indirizzo, String localita, String provincia)
			throws AppCrash {

		Boolean doppi = false;

		DataSet_itf dataSet = null;
		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dsFactory = DataSetFactory.getInstance();
			dataSet = dsFactory.makeDataSet("", "DataSetCondomini");
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("WHERECONDITION", "WHERE DENOMINAZIONE='" + denominazione + "' AND INDIRIZZO='" + indirizzo
					+ "' AND LOCALITA='" + localita + "' AND PROVINCIA='" + provincia + "'");
			params.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
			dataSet.setParam(params);
			dataSet.open();
			if (dataSet.hasMoreElements()) {
				doppi = true;
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
		return doppi;
	}

	private Integer trovaIdCondominio(String denominazione, String indirizzo, String localita, String provincia,
			UserSecurityInfo userInfo) throws AppCrash {

		Integer idCondominio = 0;

		DataSet_itf dataSet = null;
		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dsFactory = DataSetFactory.getInstance();
			dataSet = dsFactory.makeDataSet("", "DataSetCondomini");
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("WHERECONDITION",
					"WHERE CONDOMINI.DENOMINAZIONE='" + denominazione + "' AND CONDOMINI.INDIRIZZO='" + indirizzo
							+ "' AND CONDOMINI.LOCALITA='" + localita + "' AND CONDOMINI.PROVINCIA='" + provincia
							+ "'");
			params.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
			dataSet.setParam(params);
			dataSet.open();
			if (dataSet.hasMoreElements()) {
				Row_itf dbRow = (Row_itf) dataSet.nextElement();
				idCondominio = (Integer) dbRow.getField("ID_MODULO");
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
		return idCondominio;
	}

	

	

}