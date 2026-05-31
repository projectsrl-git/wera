package net.projectsrl.wera.importazioni.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;

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
import net.projectsrl.bow.parameters.ParametriDAO;
import net.projectsrl.webapp.authentication.MenuItem;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.wera.importazioni.db.ElencoFileImportatiRilevatoriDAO;
import net.projectsrl.wera.importazioni.db.ScaricoDAO;
import net.projectsrl.wera.importbo.Siemeca;
import net.projectsrl.wera.utils.UtilsDatiRilevatori;
import net.projectsrl.wera.utils.WeraUtils;
import net.projectsrl.wm.importdata.UploadedFiles;
import net.projectsrl.wm.utils.Utils;

/**
 * FunctionFileUpload
 * 
 */
@SuppressWarnings("deprecation")
public class FunctionFileUploadDatiRilevatori extends FunctionProjectWebApp_base {

	private static final String PAGE = "importdatirilevatori";
	private static final String PAGE_RESULT = "riepilogorilevatori";
	private static final int _sizeMax = 100000000;
	
	private static ArrayList<String>   _fieldValues             = new ArrayList<String>();
    private static final String SEPARATOR                = "\t";

    
    private static final String DATASET_DATI_MATRICE = "DataSetDatiMatrice";
    private static final String DATASET_DATI_MATRICE_DIMENSIONE = "DataSetDatiMatriceDimensione";
    
    
	public FunctionFileUploadDatiRilevatori() {

		super();
	}

	public FunctionFileUploadDatiRilevatori(ApplicationServices_itf applServices, String functionID,
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
		
		String msg = creaDati(req, res, userInfo);

		if(msg.isEmpty()){
			msg="Dati importati correttamente.";
		}
		req.getSession(false).setAttribute("MSG", msg);
		

		Map<String, Object> templateData = createMapFromRequest(req, userInfo);
		templateData.put("MSG",msg);
		
		_applicationSrv.displayPage(PAGE,templateData,setPageDatasetParam(PAGE, req,templateData), res);
		
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
			String label = "Scarico rilevatori";
			String function = "FileUploadDatiRilevatori";
			String link = "astro?FUNCTIONID=FileUploadDatiRilevatori";
			int itemLevel = 2;
			String path = "1520";
			String linkChain = "astro?FUNCTIONID=Home;#;astro?FUNCTIONID=FileUploadDatiRilevatori";
			boolean readOnly = false;
			int nrOfChildren = 0;
			String icon = "";

			pathDescri = "Home / Importazione dati / Riepilogo importazione scarico dati rilevatori";

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

	private String creaDati(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {
		String dirSCARICO = Config.GetInstance().getProperty("directory.external_files",_applicationSrv.getRoot()) + Config.GetInstance().getProperty("cartella.upload");
		new File(dirSCARICO).mkdir();

		Integer azienda = WeraUtils.trovaIdAziendaUtente(getSpecificUserInfo(userInfo).getIdUtente().toString());

		String nomeFile = (String) req.getSession(false).getAttribute("FILE_NAME_CEDOLINO");
		nomeFile=nomeFile.toLowerCase();

		String msg = "";

		try {
			//String inFile = dirSCARICO.replace("/", "\\\\") + (String) req.getSession(false).getAttribute("FILE_NAME_CEDOLINO");
			String inFile = dirSCARICO + (String) req.getSession(false).getAttribute("FILE_NAME_CEDOLINO");

			String path = dirSCARICO + (String) req.getSession(false).getAttribute("FILE_NAME_CEDOLINO");
			String realPath = req.getServletContext().getRealPath(path);
			//File file = new File(realPath);
			inFile=path;
			
			
			FileInputStream fileInputStream = new FileInputStream(inFile);

			Boolean importa = true;

			String msgEstensioneFile = "";
			
			Boolean fileGiaImportato = WeraUtils.verificaFileDoppio(nomeFile);
			if (fileGiaImportato){
				msg = "Il file selezionato &egrave; gi&agrave; stato importato in precedenza" + Constants_itf.A_CAPO_HTML;
				return msg;
			}

			Boolean estensioneFileValida = verificaNomeFile(userInfo, nomeFile);
			if (!estensioneFileValida) {
				msgEstensioneFile = "Il file deve avere estensione .bil oppure .xml oppure .csv" + Constants_itf.A_CAPO_HTML;
				msg = msg + msgEstensioneFile;
				return msg;
			}

			if (!importa) {
				msg = msg+ "Per procedere con l'importazione del file &egrave; necessario compiere le seguenti azioni preliminari:"+ Constants_itf.A_CAPO_HTML;
				msg = msg + msgEstensioneFile;
				return msg;
			}

			
			
			////////////////////////////////////////////////
			String dataImport = Utils.getStringDataOggiRibaltata();
			String oraImport = Utils.getOrario();
			
			
			if (inFile.toLowerCase().endsWith("xml")) {
				importazioneFileXML(azienda, nomeFile, inFile, req, dataImport, oraImport);
				scriviInElencoFileImportati(azienda, nomeFile, dataImport, oraImport);
			}
			
			
			if (inFile.toLowerCase().endsWith("bil")) {
				importazioneFileBIL(req, res, userInfo, azienda, inFile, dataImport, oraImport);
				scriviInElencoFileImportati(azienda, nomeFile, dataImport, oraImport);
			}
			
			if (inFile.toLowerCase().endsWith("csv")) {
				importazioneFileCSV(req, azienda, inFile, dataImport, oraImport);
				scriviInElencoFileImportati(azienda, nomeFile, dataImport, oraImport);
			}
			
			
                

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			msg = "L'importazione del file non &egrave riuscita. Verificare il contenuto o il formato del file.";
			req.getSession(false).setAttribute("MSG", msg);
			e.printStackTrace();
		}

		
		String sqlRefreshMaterializedView="REFRESH MATERIALIZED VIEW CONCURRENTLY vista_scarico_data_massima";
		net.projectsrl.wm.utils.WMUtils.executeQuery(sqlRefreshMaterializedView);
		
		
		/*// cancello cartelle non più utilizzate
		File cartellaUPLOAD = new File(_applicationSrv.getRoot() + Config.GetInstance().getProperty("cartella.upload"));
		File[] filesUPLOAD = cartellaUPLOAD.listFiles();
		for (File f : filesUPLOAD)
			f.delete();
		// fine cancellazione cartelle
		*/		
		return msg;

	}

	

	public static void scriviInElencoFileImportati(Integer azienda, String nomeFile, String dataImport, String oraImport) throws AppCrash {
		ElencoFileImportatiRilevatoriDAO elenco = new ElencoFileImportatiRilevatoriDAO();
		elenco.setAttribute(ElencoFileImportatiRilevatoriDAO.ID_AZIENDA, azienda);
		elenco.setAttribute(ElencoFileImportatiRilevatoriDAO.NOME_FILE, nomeFile);
        elenco.setAttribute(ElencoFileImportatiRilevatoriDAO.DATA_IMPORT, dataImport);
        elenco.setAttribute(ElencoFileImportatiRilevatoriDAO.ORA_IMPORT, oraImport);
        elenco.setAttribute(ScaricoDAO.ID_UTENTE_INS, 0);
        elenco.insert();
	}
	
	private static String cleanTextContent(String text) 
    {
        // strips off all non-ASCII characters
        text = text.replaceAll("[^\\x00-\\x7F]", "");
 
        // erases all the ASCII control characters
        text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
         
        // removes non-printable characters from Unicode
        text = text.replaceAll("\\p{C}", "");
 
        return text.trim();
    }
	
	
	public static void importazioneFileCSV(SsbServletRequest req, Integer azienda, String inFile, String dataImport, String oraImport) throws AppCrash {


		String csvFile = inFile;
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ";";
        String nomeFileCSV = "";
        Boolean titoli=false;
        String[] intestazione = null;
        String lineWithoutDelimiters="";
       

        
        int rowCounter=0;
       
        try {

            br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile),"UTF-8"));
            while ((line = br.readLine()) != null) {
            	// serve saltare le linee vuote che hanno solo doppio apice e punto virgola
            	String s = line;
            	byte[] b = s.getBytes(StandardCharsets.UTF_8);
            	line = new String(b, StandardCharsets.US_ASCII);
            	line=cleanTextContent(line);
            	String[] cols = line.split(";");
            	
            	lineWithoutDelimiters=line.replace("\"","").replace(";", "").trim();
            	
            	if (!lineWithoutDelimiters.isEmpty()){
            		// use comma as separator
                	if (rowCounter==0){
                		intestazione = line.split(cvsSplitBy);
                	}
                	String[] dati = line.split(cvsSplitBy);
                	
                	if (!titoli){
                		Arrays.fill( intestazione, null );
                	}
                	
                    
    				String lingua="ITA";
    				String dataReport="";
    				String oraReport="";
    				String rifImpianto="";
    				String descrizione="";
    				String totCablati="";
    				String totWireless="";
    				String totNonRicevuti="";
    				String totConcentratori="";

    				
    				String nProgrDisp ="";
    				String nFabbricaDisp="";
    				String fattoreEnergia="";
    				String unitaDiMisura="";
    				String network ="";
    				String modello="";
    				String dataLettura ="";
    				String oraLettura="";
    				String statoComunicazione="";
    				String dataOraDispositivo="";
    				String udrTotali="";
    				String dataStorico="";
    				String udrStorico="";
    				String dataErrore="";
    				
    				String primary_address="";
    				String name_device="";
    				String device_description="";
    				String device_detail="";
    				
    				Integer idCondominio=0;
    				String oraImportCompleta = WeraUtils.getOrarioCompleto();
    				
    				
    				String volume1="";
    				String volume2="";
    				String date1="";
    				String energy1="";
    				String energy2="";
    				String volumeFlow="";
    				String power="";
    				String flowTemperature="";
    				String returnTemperature="";
    				Boolean fullCsvAntenneNuove=false;
    				Boolean udrOK=false;
    				Boolean udrStoricoOK=false;
    				Boolean intestazioneVolumeOK=false;
    				Boolean intestazioneEnergyOK=false;
    				
    				if(!dati[0].isEmpty()){
    					if (dati[0].toLowerCase().contains("nome file") || dati[0].toLowerCase().contains("file name") || dati[0].toLowerCase().contains("count")){
    						if (dati[0].toLowerCase().contains("file name")){
    							lingua="ENG";
    						}
    						intestazione = line.split(cvsSplitBy);
    						for (int y=0;y<dati.length;y++){
    							intestazione[y]=dati[y];
    							titoli=true;
    							rowCounter=rowCounter+1;
    						}
    					}else{
    						for (int y=0;y<dati.length;y++){
    								
    								titoli=false;
    								
    								if (dati[0].toUpperCase().contains("RAW")){
    									nomeFileCSV=dati[0].replaceAll("\"", "")+".csv";
    								}
    							
    								if (dati[y]!=null){
    									dati[y]=dati[y].replaceAll("\"", "");
    								}
    								
    								
    								if (intestazione[y].contains("Data Report") || intestazione[y].contains("Report date")){
    									dataReport = dati[y];
    								}
    								if (intestazione[y].contains("Ora Report") || intestazione[y].contains("Report time")){
    									oraReport = dati[y];
    								}
    								if (intestazione[y].contains("Riferimento impianto") || intestazione[y].contains("Plant reference")){
    									rifImpianto = dati[y];
    								}
    								if (intestazione[y].contains("Descrizione") || intestazione[y].contains("Description")){
    									descrizione = dati[y];
    								}
    								if (intestazione[y].contains("Totale dispositivi cablati") || intestazione[y].contains("Total wired devices")){
    									totCablati = dati[y];
    								}
    								if (intestazione[y].contains("Totale dispositivi wireless") || intestazione[y].contains("Total wireless devices")){
    									totWireless = dati[y];
    								}
    								if (intestazione[y].contains("Totale dispositivi non ricevuti") || intestazione[y].contains("Total untransmitted devices")){
    									totNonRicevuti = dati[y];
    								}
    								if (intestazione[y].contains("Totale concentratori") || intestazione[y].contains("Total gateways")){
    									totConcentratori = dati[y];
    								}
    								
    								
    								if (dati[y]!=null){
    									dati[y]=dati[y].replaceAll("\"", "").replace(".","").replace(",", ".").replace("31/MM/yy","").replace("30/MM/yy","").replace("29/MM/yy","").replace("28/MM/yy","");
    								}
    								


    								if (intestazione[y].contains("count")){
    									nProgrDisp = dati[y];
    								}
    								if (intestazione[y].contains("device_serial_number")){
    									nFabbricaDisp = dati[y];
    								}
    								if (intestazione[y].contains("device_measure_hex")){
    									fattoreEnergia = dati[y];
    									unitaDiMisura = dati[y];
    								}
    								if (intestazione[y].contains("0=wired|1=wireless|2=smart gateway")){
    									network = dati[y];
    								}  
    								if (intestazione[y].contains("model_id")){
    									if (dati[y]!=null){
    										modello = dati[y];
    									}
    								}   
    								if (intestazione[y].contains("readout_date")){
    									dataLettura = dati[y].replaceAll("-", "/");
    								}   
    								if (intestazione[y].contains("readout_time")){
    									oraLettura = dati[y];
    								}
    								if (intestazione[y].contains("communication_status")){
    									statoComunicazione = dati[y];
    								}  
    								
    								
    								
    								//parte variabile
    								
    								
    								if (intestazione.length>15){
    									if (intestazione[y].contains("Data ora dispositivo (Date and time)") || intestazione[y].contains("Device date time (Date and time)") || intestazione[16].contains("DATE_TIME")){ 
        									if (intestazione[y].contains("Data ora dispositivo (Date and time)") || intestazione[y].contains("Device date time (Date and time)")){
        										dataOraDispositivo = dati[y];
        									}else{
        										dataOraDispositivo = dati[16].replaceAll("\"", "");
        									}
        								}   
        								if (intestazione[y].contains("Udr totali") || intestazione[y].contains("Total HCA") || intestazione[12].contains("HCA")){
        									if (intestazione[y].contains("Udr totali") || intestazione[y].contains("Total HCA")){
        										udrTotali = dati[y];
        										udrOK=true;
        									}else{
        										if (!udrOK){
        											udrTotali = dati[12].replaceAll("\"", "").replace(".", "").replaceAll(",",".");
        										}
        										udrOK=true;
        									}
        								} 
        								if (intestazione[y].contains("Data storico 1 (date)")  || intestazione[y].contains("Monthly date 1 (date)") || intestazione[13].contains("DATE")){
        									if (intestazione[y].contains("Data storico 1 (date)") || intestazione[y].contains("Monthly date 1 (date)")){
        										dataStorico = dati[y];
        									}else{
        										dataStorico = dati[13].replaceAll("\"", "");
        									}
        								}
        								if (intestazione[y].contains("Udr mese 1") || intestazione[y].contains("HCA month 1") || intestazione[14].contains("HCA")){
        									if (intestazione[y].contains("Udr mese 1") || intestazione[y].contains("HCA month 1")){
        										udrStorico = dati[y];
        										udrStoricoOK=true;
        									}else{
        										if (!udrStoricoOK){
        											udrStorico = dati[14].replaceAll("\"", "").replace(".", "").replaceAll(",",".");
        										}
        										udrStoricoOK=true;
        									}
        								}  
        								if (intestazione[y].contains("Tempo in errore (date)") || intestazione[y].contains("Error on time (date)") || intestazione[15].contains("DATE")){
        									if (intestazione[y].contains("Tempo in errore (date)") || intestazione[y].contains("Error on time (date)")){
        										dataErrore = dati[y];
        									}else{
        										dataErrore = dati[15].replaceAll("\"", "").replace("31/MM/yy","").replace("30/MM/yy","").replace("29/MM/yy","").replace("28/MM/yy","");
        									}
        								}   
        								
        								
        								
        								if (intestazione.length>15){
	        								if (intestazione[12].contains("VOLUME") && !intestazioneVolumeOK){
	        									volume1=dati[12].replaceAll("\"", "").replace(",", ".").replace("31/MM/yy","").replace("30/MM/yy","").replace("29/MM/yy","").replace("28/MM/yy","");
	        									volume2=dati[13].replaceAll("\"", "").replace(",", ".").replace("31/MM/yy","").replace("30/MM/yy","").replace("29/MM/yy","").replace("28/MM/yy","");
	        									date1=dati[14].replaceAll("\"", "").replace(",", ".").replace("31/MM/yy","").replace("30/MM/yy","").replace("29/MM/yy","").replace("28/MM/yy","");
	        									//fullCsvAntenneNuove=true;
	        									//intestazioneVolumeOK=true;
	        								}
        								}
        			    				
        								if (intestazione.length>17){
        									if (intestazione[12].contains("ENERGY") &&!intestazioneEnergyOK){
            									energy1=dati[12].replaceAll("\"", "").replace(",", ".").replace("31/MM/yy","").replace("30/MM/yy","").replace("29/MM/yy","").replace("28/MM/yy","");
                			    				energy2=dati[13].replaceAll("\"", "").replace(",", ".").replace("31/MM/yy","").replace("30/MM/yy","").replace("29/MM/yy","").replace("28/MM/yy","");
                			    				volume1=dati[14].replaceAll("\"", "").replace(",", ".").replace("31/MM/yy","").replace("30/MM/yy","").replace("29/MM/yy","").replace("28/MM/yy","");
                			    				volumeFlow=dati[15].replaceAll("\"", "").replace(",", ".").replace("31/MM/yy","").replace("30/MM/yy","").replace("29/MM/yy","").replace("28/MM/yy","");
                			    				power=dati[16].replaceAll("\"", "").replace(",", ".").replace("31/MM/yy","").replace("30/MM/yy","").replace("29/MM/yy","").replace("28/MM/yy","");
                			    				flowTemperature=dati[17].replaceAll("\"", "").replace(",", ".").replace("31/MM/yy","").replace("30/MM/yy","").replace("29/MM/yy","").replace("28/MM/yy","");
                			    				returnTemperature=dati[18].replaceAll("\"", "").replace(",", ".").replace("31/MM/yy","").replace("30/MM/yy","").replace("29/MM/yy","").replace("28/MM/yy","");
       											dataOraDispositivo = dati[19].replaceAll("\"", "").replace(",", ".").replace("31/MM/yy","").replace("30/MM/yy","").replace("29/MM/yy","").replace("28/MM/yy","");
       											//fullCsvAntenneNuove=true;
       											//intestazioneEnergyOK=true; fare sempre differenza con antenne nuove
            								}
        								}
        								
        			    				
        			    				
        			    				
        			    				
        			    				
    								}else{
    									if (intestazione[y].contains("Data ora dispositivo (Date and time)")){ 
       										dataOraDispositivo = dati[y];
        								}   
        								if (intestazione[y].contains("Udr totali")){
       										udrTotali = dati[y];
        								} 
        								if (intestazione[y].contains("Data storico 1 (date)")){
       										dataStorico = dati[y];
        								}
        								if (intestazione[y].contains("Udr mese 1")){
       										udrStorico = dati[y];
        								}  
        								if (intestazione[y].contains("Tempo in errore (date)")){
       										dataErrore = dati[y];
        								}   
    								}
    								
    								
    								
    								
    								

    								if (intestazione[y].contains("primary_address")){
    									primary_address = dati[y];
    								}
    								if (intestazione[y].contains("name_device")){
    									name_device = dati[y];
    								}
    								if (intestazione[y].contains("device_description")){
    									device_description = dati[y];
    								}
    								if (intestazione[y].contains("device_detail")){
    									device_detail = dati[y];
    								}
    									
    						        if (!nFabbricaDisp.isEmpty()){
    						        	idCondominio=WeraUtils.trovaIdCondominio(nFabbricaDisp);
        						        String datiCondominio=WeraUtils.trovaDatiCondominio(nFabbricaDisp);
        						        if (!datiCondominio.equals("") && req!=null){
        						        	req.getSession(false).setAttribute("DATI_CONDOMINIO", datiCondominio);
        						        }
    						        }
    						        
    						        
    						}

    						if (!dati[0].toUpperCase().contains("RAW")){
    				        	if (StringUtils.isNumeric(nProgrDisp) && !nProgrDisp.isEmpty()){
    					        	System.out.println(idCondominio+" --- "+nProgrDisp+" --- "+nFabbricaDisp+" --- "+fattoreEnergia+" --- "+unitaDiMisura+" --- "+network+" --- "+modello+" --- "+dataLettura+" --- "+oraLettura+" --- "+statoComunicazione+" --- "+dataOraDispositivo+" --- "+udrTotali+" --- "+dataStorico+" --- "+udrStorico+" --- "+dataErrore+" --- "+primary_address+" --- "+name_device+" --- "+device_description+" --- "+device_detail);
    					        	creaDatiScaricoCSV(req, nomeFileCSV, azienda, dataImport, oraImport ,oraImportCompleta, idCondominio, nProgrDisp, nFabbricaDisp, fattoreEnergia, unitaDiMisura, network, modello, dataLettura, oraLettura, statoComunicazione, dataOraDispositivo, udrTotali, dataStorico, udrStorico, dataErrore, primary_address, name_device, device_description, device_detail, dataReport, dataLettura,fullCsvAntenneNuove, volume1,  volume2,  date1,  energy1,  energy2,  volumeFlow,  power,  flowTemperature,  returnTemperature, lingua);
    					        }
    				        }
    				        
    					}
    				}
            	}
            	
            	

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
		
	

	private static void creaDatiScaricoCSV(SsbServletRequest req,
			String nomeFileCSV, Integer azienda, String dataImport, String oraImport, String oraImportCompleta, Integer idCondominio, String nProgrDisp, String nFabbricaDisp, String fattoreEnergia, String unitaDiMisura, String network, String modello, String dataLettura, String oraLettura, String statoComunicazione, String dataOraDispositivo, String udrTotali, String dataStorico, String udrStorico, String dataErrore, String primary_address, String name_device, String device_description, String device_detail, String dataReport, String dataLettura2, Boolean fullCsvAntenneNuove, String volume1, String volume2, String date1, String energy1, String energy2, String volumeFlow, String power, String flowTemperature, String returnTemperature, String lingua) throws AppCrash {
		
		
		String codiceAnomalia="";

		DataSet_itf dataSetERR = null;
		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dsFactory = DataSetFactory.getInstance();
			dataSetERR = dsFactory.makeDataSet("", "DSErrori");
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("DESCRIZIONE", statoComunicazione.toUpperCase());
			dataSetERR.setParam(params);
			dataSetERR.open();
			
			boolean trovato = false;
			while (dataSetERR.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSetERR.nextElement();
                codiceAnomalia = (String) dbRow.getField("CODICE");
                trovato = true;
            }
			dataSetERR.close();
			
			
			//popola parametri
			String codiceUnicoERR="";
			if (!statoComunicazione.toUpperCase().equals("OK")){
				ParametriDAO para = new ParametriDAO();
				para.setAttribute(ParametriDAO.DOMINIO, "ERR");
				para.setAttribute(ParametriDAO.DESCRIZIONE, statoComunicazione.toUpperCase());

				if (para.retrieve()) {
					codiceUnicoERR=para.getAttribute(ParametriDAO.CODICE).toString().trim();
				}else{
				    String codiceBaseERR = WeraUtils.creaCodiceBase(statoComunicazione.toUpperCase());
				    codiceUnicoERR = WeraUtils.trovaCodiceDisponibile(codiceBaseERR,"ERR");
				    
				    para.setAttribute(ParametriDAO.DOMINIO, "ERR");
				    para.setAttribute(ParametriDAO.CODICE, codiceUnicoERR);
				    para.setAttribute(ParametriDAO.DESCRIZIONE, statoComunicazione.toUpperCase());
				    para.insert();
				    codiceAnomalia=codiceUnicoERR;
				}
			}
			
			
			
			
			
		} catch (Throwable t) {
			AppCrash ac = new AppCrash(t);
			throw ac;
		} finally {
			if (dataSetERR != null) {
				try {
					dataSetERR.close();
				} catch (AppCrash ac) {
					//ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
				}
			}
		}
		
		if (fattoreEnergia.replace("|", "").trim().isEmpty()){
			unitaDiMisura="";
		}else{
			unitaDiMisura=UtilsDatiRilevatori.setUnitaDiMisura(fattoreEnergia);
		}
		

		ScaricoDAO scarico = new ScaricoDAO();
        scarico.setAttribute(ScaricoDAO.ID_AZIENDA, azienda);
        scarico.setAttribute(ScaricoDAO.ID_CONDOMINIO, idCondominio);
        scarico.setAttribute(ScaricoDAO.DATA_IMPORT, dataImport);
        scarico.setAttribute(ScaricoDAO.ORA_IMPORT, oraImport);
        scarico.setAttribute(ScaricoDAO.ORA_IMPORT_COMPLETA, oraImportCompleta);
        scarico.setAttribute(ScaricoDAO.NOME_FILE, nomeFileCSV);
        scarico.setAttribute(ScaricoDAO.N_CLIENTE_NETWORK, 0);
        scarico.setAttribute(ScaricoDAO.N_FABBRICA_NETWORK, 0);
        scarico.setAttribute(ScaricoDAO.VERSIONE_SOFTWARE_I, 0);
        scarico.setAttribute(ScaricoDAO.DATA_DI_LETTURA_I, "");
        scarico.setAttribute(ScaricoDAO.GIORNO_SETTIMANA, "");
        scarico.setAttribute(ScaricoDAO.ORA_DI_LETTURA_I, "");
        scarico.setAttribute(ScaricoDAO.OPERAT_HOURS, "");
        scarico.setAttribute(ScaricoDAO.ANOMALIA_I, "");
        scarico.setAttribute(ScaricoDAO.DATA_ANOMALIA_I, "");
        scarico.setAttribute(ScaricoDAO.ORA_ANOMALIA_I, "");
        scarico.setAttribute(ScaricoDAO.DISP_RADIO_TROVATI, "");
        scarico.setAttribute(ScaricoDAO.MODELLO_ANTENNA, "");
        scarico.setAttribute(ScaricoDAO.N_PROGRESSIVO_DISPOSITIVO, nProgrDisp);
        scarico.setAttribute(ScaricoDAO.ANTENNA_DI_RIFERIMENTO, network);
        scarico.setAttribute(ScaricoDAO.DATA_DI_LETTURA, dataLettura);
        scarico.setAttribute(ScaricoDAO.ORA_DI_LETTURA, oraLettura);
        scarico.setAttribute(ScaricoDAO.N_FABBRICA_DISPOSITIVO, nFabbricaDisp);
        scarico.setAttribute(ScaricoDAO.CODICE_DI_PRODUZIONE, 0);
        scarico.setAttribute(ScaricoDAO.VERSIONE_SOFTWARE, 0);
        scarico.setAttribute(ScaricoDAO.ANOMALIA, codiceAnomalia);
        scarico.setAttribute(ScaricoDAO.DATA_ANOMALIA, dataErrore);
        scarico.setAttribute(ScaricoDAO.ORA_ANOMALIA, "");
        //scarico.setAttribute(ScaricoDAO.LETTURA_ATTUALE, udrTotali);
        if (unitaDiMisura.equals("Wh")){
        	scarico.setAttribute(ScaricoDAO.LETTURA_ATTUALE, energy1);
        	if (energy1.isEmpty()){
        		scarico.setAttribute(ScaricoDAO.LETTURA_ATTUALE, udrTotali);
        		unitaDiMisura="HCA";
        	}
        }
        if (unitaDiMisura.equals("m3")){
        	scarico.setAttribute(ScaricoDAO.VOLUME_ATTUALE, volume1);
        }
        
        scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA, unitaDiMisura);
        //scarico.setAttribute(ScaricoDAO.VOLUME_ATTUALE, 0);
        scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA1, unitaDiMisura);
        
        if (fattoreEnergia.replace("|", "").trim().isEmpty()){
        	scarico.setAttribute(ScaricoDAO.FATTORE_ENERGIA, "");
        }else{
        	scarico.setAttribute(ScaricoDAO.FATTORE_ENERGIA, WeraUtils.pulisciFattoreEnergia(fattoreEnergia));
        }
        
        scarico.setAttribute(ScaricoDAO.LETTURA_A_DATA_DI_SCARICO, udrStorico);
        scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA2, unitaDiMisura);
        scarico.setAttribute(ScaricoDAO.DATA_DI_SCARICO, dataStorico);
        scarico.setAttribute(ScaricoDAO.DATA_INIZIO_STATISTICA, "");
        scarico.setAttribute(ScaricoDAO.STAT_VALUE1, 0);
        scarico.setAttribute(ScaricoDAO.STAT_VALUE2, 0);
        scarico.setAttribute(ScaricoDAO.STAT_VALUE3, 0);
        scarico.setAttribute(ScaricoDAO.STAT_VALUE4, 0);
        scarico.setAttribute(ScaricoDAO.STAT_VALUE5, 0);
        scarico.setAttribute(ScaricoDAO.STAT_VALUE6, 0);
        scarico.setAttribute(ScaricoDAO.STAT_VALUE7, 0);
        scarico.setAttribute(ScaricoDAO.STAT_VALUE8, 0);
        scarico.setAttribute(ScaricoDAO.STAT_VALUE9, 0);
        scarico.setAttribute(ScaricoDAO.STAT_VALUE10, 0);
        scarico.setAttribute(ScaricoDAO.STAT_VALUE11, 0);
        scarico.setAttribute(ScaricoDAO.STAT_VALUE12, 0);
        scarico.setAttribute(ScaricoDAO.STAT_VALUE13, 0);
        scarico.setAttribute(ScaricoDAO.STAT_VALUE14, 0);
        scarico.setAttribute(ScaricoDAO.STAT_VALUE15, 0);
        scarico.setAttribute(ScaricoDAO.STAT_VALUE16, 0);
        scarico.setAttribute(ScaricoDAO.STAT_VALUE17, 0);
        scarico.setAttribute(ScaricoDAO.STAT_VALUE18, 0);
        scarico.setAttribute(ScaricoDAO.UNIT_OF_STAT, unitaDiMisura);
        scarico.setAttribute(ScaricoDAO.CURRVALUE_OF_TARIFF1, "");
        scarico.setAttribute(ScaricoDAO.UNIT_OF_TARIFF1, "");
        scarico.setAttribute(ScaricoDAO.SET_DAY_VAL_OF_TARIFF1, "");
        scarico.setAttribute(ScaricoDAO.SET_DAY_UNIT_OF_TARIFF1, "");
        scarico.setAttribute(ScaricoDAO.KDEVADR292, "");
        scarico.setAttribute(ScaricoDAO.ZDAT, "");
        scarico.setAttribute(ScaricoDAO.ZUHR, "");
        scarico.setAttribute(ScaricoDAO.CBS104, "");
        scarico.setAttribute(ScaricoDAO.ZFDAUER251, "");
        scarico.setAttribute(ScaricoDAO.VOLUMECUMUL, "");
        scarico.setAttribute(ScaricoDAO.UNVOLUMECUMUL, "");
        scarico.setAttribute(ScaricoDAO.CVOLSTLVAL108, "");
        scarico.setAttribute(ScaricoDAO.CVOLSTLDIM108, "");
        scarico.setAttribute(ScaricoDAO.CENSTVLVAL111, "");
        scarico.setAttribute(ScaricoDAO.CENSTVLDIM111, "");
        scarico.setAttribute(ScaricoDAO.CVOLSTVLVAL112, "");
        scarico.setAttribute(ScaricoDAO.CVOLSTVLDIM112, "");
        scarico.setAttribute(ScaricoDAO.ZDATSTVL114, "");
        scarico.setAttribute(ScaricoDAO.CPQMWMAXVAL169, "");
        scarico.setAttribute(ScaricoDAO.CPQMWMAXDIM169, "");
        scarico.setAttribute(ScaricoDAO.ZDATMWMAX170, "");
        scarico.setAttribute(ScaricoDAO.VALCUMULTAR1, "");
        scarico.setAttribute(ScaricoDAO.UMVALCUMULTAR1, "");
        scarico.setAttribute(ScaricoDAO.CVXTAR2VAL194, "");
        scarico.setAttribute(ScaricoDAO.CVXTAR2DIM194, "");
        scarico.setAttribute(ScaricoDAO.CVXTAR1STLVAL193, "");
        scarico.setAttribute(ScaricoDAO.CVXTAR1STLDIM193, "");
        scarico.setAttribute(ScaricoDAO.CVXTAR2STLVAL194, "");
        scarico.setAttribute(ScaricoDAO.CVXTAR2STLDIM194, "");
        scarico.setAttribute(ScaricoDAO.VISIBILE, false);
        scarico.setAttribute(ScaricoDAO.ID_UTENTE_INS, 0);
        
        // per antenne nuove 2023
        scarico.setAttribute(ScaricoDAO.VOLUME1,volume1);
        scarico.setAttribute(ScaricoDAO.VOLUME2,volume2);
		scarico.setAttribute(ScaricoDAO.DATE1,date1);
		scarico.setAttribute(ScaricoDAO.ENERGY1,energy1);
		scarico.setAttribute(ScaricoDAO.ENERGY2,energy2);
		scarico.setAttribute(ScaricoDAO.VOLUME_FLOW,volumeFlow);
		scarico.setAttribute(ScaricoDAO.POWER,power);
		scarico.setAttribute(ScaricoDAO.FLOW_TEMPERATURE,flowTemperature);
		scarico.setAttribute(ScaricoDAO.RETURN_TEMPERATURE,returnTemperature);
    	
        scarico.insert();
        
        
        String oraAnomalia="";
        String volumeCumul="0";
        
        String annoDaSalvare="";
        String meseDaSalvare="";
        if (dataLettura.length()==10){
        	if (lingua.equals("ITA")){
        		annoDaSalvare=dataLettura.substring(0,4);
            	meseDaSalvare=dataLettura.substring(5,7);
        	}else{
        		annoDaSalvare=dataLettura.substring(6,10);
            	meseDaSalvare=dataLettura.substring(3,5);
        	}
        }
        
        
        String lettura=udrTotali;
        
        //UtilsDatiRilevatori.pulisciDoppi(idCondominio,nFabbricaDisp,meseDaSalvare,annoDaSalvare); non utilizzato
        
       
        if (fullCsvAntenneNuove){
        	//per antenne nuove report esteso non serve fare la differenza
        }else{
        	if (!meseDaSalvare.isEmpty() && !annoDaSalvare.isEmpty()){
           	 
             
             if (fattoreEnergia.contains("06") || fattoreEnergia.contains("07")){
   				volume1 = UtilsDatiRilevatori.calcolaDifferenzaStessoMeseAcqua(idCondominio,nFabbricaDisp,meseDaSalvare,annoDaSalvare,volume1,fattoreEnergia).toString();
   	            //volume1 = UtilsDatiRilevatori.calcolaDifferenzaTraMesiAcqua(idCondominio,nFabbricaDisp,meseDaSalvare,annoDaSalvare,volume1,fattoreEnergia).toString();
   	            volume1 = UtilsDatiRilevatori.calcolaDifferenzaTraMesiAcquaOttimizzato(idCondominio,nFabbricaDisp,meseDaSalvare,annoDaSalvare,volume1,fattoreEnergia).toString();
     		}else{
     			lettura = UtilsDatiRilevatori.calcolaDifferenzaStessoMese(idCondominio,nFabbricaDisp,meseDaSalvare,annoDaSalvare,lettura,dataStorico,udrStorico).toString();
                //lettura = UtilsDatiRilevatori.calcolaDifferenzaTraMesi(idCondominio,nFabbricaDisp,meseDaSalvare,annoDaSalvare,lettura,dataStorico,udrStorico).toString();
                lettura = UtilsDatiRilevatori.calcolaDifferenzaTraMesiOttimizzato(idCondominio,nFabbricaDisp,meseDaSalvare,annoDaSalvare,lettura).toString();
     		}
             
             
           }
        };
        
        
        
        //unitaDiMisura=UtilsDatiRilevatori.setUnitaDiMisura(unitaDiMisura);
        
        UtilsDatiRilevatori.scriviDatiRilevatoriCSV(idCondominio, dataStorico,udrTotali, volumeCumul, unitaDiMisura, dataImport, oraImport, dataLettura, oraLettura, codiceAnomalia, dataErrore, oraAnomalia, unitaDiMisura,lettura,nomeFileCSV,nFabbricaDisp,meseDaSalvare,annoDaSalvare,azienda,fattoreEnergia, volume1);
        
		
        
		
	}

	private void importazioneFileBIL(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo,
			Integer azienda, String inFile, String dataImport, String oraImport) {
		try {
		    InputStream ist = new FileInputStream(inFile);
		    BufferedReader istream = new BufferedReader(new InputStreamReader(ist));
		    int rowCounter = 0;
		    String text = "";

		    String nomeFileBil = "";
		    String networkCliente = "";
		    String networkFabbrica = "";
		    String swVersion = "";
		    
		   

		    while (true) {

		        text = istream.readLine();

		        if (text == null) {
		            break;
		        }
		        if (text.equals("")) {
		            continue;
		        }

		        if (rowCounter == 1) {
		            setAttributeValues(text);
		            nomeFileBil = _fieldValues.get(0).trim();
		            networkCliente = _fieldValues.get(1).trim();
		            networkFabbrica = _fieldValues.get(2).trim();
		            swVersion = _fieldValues.get(3).trim();
		        }

		        if (rowCounter > 2) {
		            setAttributeValues(text);
		            scriviDatiScarico(req, res, userInfo, rowCounter, nomeFileBil,networkCliente, networkFabbrica, swVersion, azienda, dataImport, oraImport);
		            
		        }
		        loop:
		        rowCounter++;
		        
		    }
		} catch (Exception e) {
		    //req.getSession(false).setAttribute("ERRORE_IMPORT", "SI");
		    e.printStackTrace();
		}
		
		
		
	}
	
	
	
	public static void setAttributeValues(String text) {

        int index = 0;
        StringTokenizer campi = new StringTokenizer(text, SEPARATOR);
        while (campi.hasMoreTokens()) {
            String singoloCampo = campi.nextToken();
            _fieldValues.add(index, singoloCampo);
            index++;
        }
    }
	

	public static void importazioneFileXML(Integer azienda, String nomeFile, String inFile, SsbServletRequest req, String dataImport, String oraImport) throws JAXBException, AppCrash {
		

			JAXBContext jaxbContext = JAXBContext.newInstance(Siemeca.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Siemeca emps = (Siemeca) jaxbUnmarshaller.unmarshal(new File(inFile));

			scriviScarico(azienda, nomeFile, req, dataImport, oraImport, emps);
			scriviDatiRilevatori(azienda, nomeFile, req, dataImport, oraImport, emps);
		
	}
	
	
	private static void scriviScarico(Integer azienda, String nomeFile, SsbServletRequest req, String dataImport,
			String oraImport, Siemeca emps) throws AppCrash {
		
		
		ScaricoDAO scarico = new ScaricoDAO();

		String identnrNw = "";
		String versionNw = "";
		
		
		String oraImportCompleta = WeraUtils.getOrarioCompleto();
		

		for (int x = 0; x < emps.getNetwork().size(); x++) {
			if (!(emps.getNetwork().get(0).getColectordev() == null)) {
				for (int y = 0; y < emps.getNetwork().get(0).getColectordev().size(); y++) {
					// String fabnrNw = emps.getNetwork().get(x).getColectordev().get(y).getFabnr();
					// String modelNw = emps.getNetwork().get(x).getColectordev().get(y).getModel();
					// String paramsetNw = emps.getNetwork().get(x).getColectordev().get(y).getParamset();
					// String hoursonNw = emps.getNetwork().get(x).getColectordev().get(y).getHourson();
					// String errorflagsNw = emps.getNetwork().get(x).getColectordev().get(y).getErrorflags();
					// String subnetNw = emps.getNetwork().get(x).getColectordev().get(y).getSubnet();
					String errordateNw = emps.getNetwork().get(x).getColectordev().get(y).getErrordate();
					String dateNw = emps.getNetwork().get(x).getColectordev().get(y).getDate();
					String timeNw = emps.getNetwork().get(x).getColectordev().get(y).getTime();
					String receiverNw = emps.getNetwork().get(x).getColectordev().get(y).getReceiver();
					String powerstatusNw = emps.getNetwork().get(x).getColectordev().get(y).getPowerstatus();

					identnrNw = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0)
							.getIdentnr();
					String manufactNw = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0)
							.getManufact();
					versionNw = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0)
							.getVersion();
					String devtypeNw = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0)
							.getDevtype();
					String statusNw = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0)
							.getStatus();
					// String accessnrNw = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0).getAccessnr();
					// String signatureNw = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0).getSignature();

					String unitaDiMisura=UtilsDatiRilevatori.setUnitaDiMisura(devtypeNw);


					
					if (dateNw.length()>10){
						dateNw=dateNw.substring(0, 10);
					}
					
					if (errordateNw.length()>10){
						errordateNw=errordateNw.substring(0, 10);
					}
					
					
					Integer idCondominio=WeraUtils.trovaIdCondominio(identnrNw);
				    String datiCondominio=WeraUtils.trovaDatiCondominio(identnrNw);
				    if (!datiCondominio.equals("") && req!=null){
			        	req.getSession(false).setAttribute("DATI_CONDOMINIO", datiCondominio);
			        }
				    scarico.setAttribute(ScaricoDAO.ID_CONDOMINIO, idCondominio);
				    
					scarico.setAttribute(ScaricoDAO.NOME_FILE, nomeFile);
					scarico.setAttribute(ScaricoDAO.ID_UTENTE_INS, 0);
					scarico.setAttribute(ScaricoDAO.ID_AZIENDA, azienda);
					scarico.setAttribute(ScaricoDAO.DATA_IMPORT, dataImport);
					scarico.setAttribute(ScaricoDAO.ORA_IMPORT, oraImport);
					scarico.setAttribute(ScaricoDAO.ORA_IMPORT_COMPLETA, oraImportCompleta);
					scarico.setAttribute(ScaricoDAO.N_CLIENTE_NETWORK, identnrNw);
					scarico.setAttribute(ScaricoDAO.N_FABBRICA_NETWORK, identnrNw);
					scarico.setAttribute(ScaricoDAO.VERSIONE_SOFTWARE_I, versionNw);

					scarico.setAttribute(ScaricoDAO.N_PROGRESSIVO_DISPOSITIVO, "");
					scarico.setAttribute(ScaricoDAO.ANTENNA_DI_RIFERIMENTO, receiverNw);
					
					scarico.setAttribute(ScaricoDAO.DATA_DI_SCARICO, dateNw.replace(".", "/"));
					scarico.setAttribute(ScaricoDAO.ORA_DI_LETTURA, timeNw);
					scarico.setAttribute(ScaricoDAO.N_FABBRICA_DISPOSITIVO, identnrNw);
					scarico.setAttribute(ScaricoDAO.CODICE_DI_PRODUZIONE, manufactNw);
					scarico.setAttribute(ScaricoDAO.VERSIONE_SOFTWARE, versionNw);
					scarico.setAttribute(ScaricoDAO.FATTORE_ENERGIA, WeraUtils.pulisciFattoreEnergia(devtypeNw));
					if (statusNw.equals("0")) {
						statusNw = "";
					}
					scarico.setAttribute(ScaricoDAO.ANOMALIA, statusNw);
					scarico.setAttribute(ScaricoDAO.DATA_ANOMALIA, errordateNw.replace(".", "/"));
					scarico.setAttribute(ScaricoDAO.LETTURA_ATTUALE, powerstatusNw.replace("%", ""));
					//if (powerstatusNw.contains("%")) {
					//	scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA, "%");
					//}
					scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA, unitaDiMisura);

					scarico.insert();
					

				}
			}
			if (!(emps.getNetwork().get(0).getMeasuredev() == null)) {
				String dataInizioStat = "";
				for (int z = 0; z < emps.getNetwork().get(0).getMeasuredev().size(); z++) {
					// String fabnr = emps.getNetwork().get(x).getMeasuredev().get(z).getFabnr();
					// String model = emps.getNetwork().get(x).getMeasuredev().get(z).getModel();
					// String paramset = emps.getNetwork().get(x).getMeasuredev().get(z).getParamset();
					// String hourson = emps.getNetwork().get(x).getMeasuredev().get(z).getHourson();
					// String errorflags = emps.getNetwork().get(x).getMeasuredev().get(z).getErrorflags();
					// String subnet = emps.getNetwork().get(x).getMeasuredev().get(z).getSubnet();
					String errordate = emps.getNetwork().get(x).getMeasuredev().get(z).getErrordate();
					String date = emps.getNetwork().get(x).getMeasuredev().get(z).getDate();
					String time = emps.getNetwork().get(x).getMeasuredev().get(z).getTime();
					String receiver = emps.getNetwork().get(x).getMeasuredev().get(z).getReceiver();
					String powerstatus = emps.getNetwork().get(x).getMeasuredev().get(z).getPowerstatus();

					String identnr = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0)
							.getIdentnr();
					String manufact = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0)
							.getManufact();
					String version = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0)
							.getVersion();
					String devtype = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0)
							.getDevtype();
					String status = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0)
							.getStatus();
					// String accessnr = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0).getAccessnr();
					// String signature = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0).getSignature();
					
					if (date.length()>10){
						date=date.substring(0, 10);
					}
					if (errordate.length()>10){
						errordate=errordate.substring(0, 10);
					}
					
					Integer idCondominio=WeraUtils.trovaIdCondominio(identnr);
				    String datiCondominio=WeraUtils.trovaDatiCondominio(identnr);
				    if (!datiCondominio.equals("") && req!=null){
			        	req.getSession(false).setAttribute("DATI_CONDOMINIO", datiCondominio);
			        }
				    scarico.setAttribute(ScaricoDAO.ID_CONDOMINIO, idCondominio);
				    scarico.setAttribute(ScaricoDAO.ID_UTENTE_INS, 0);
					scarico.setAttribute(ScaricoDAO.N_CLIENTE_NETWORK, identnrNw);
					scarico.setAttribute(ScaricoDAO.N_FABBRICA_NETWORK, identnrNw);
					scarico.setAttribute(ScaricoDAO.VERSIONE_SOFTWARE_I, versionNw);

					String nProgrDisp = Integer.toString(z + 1);
					scarico.setAttribute(ScaricoDAO.N_PROGRESSIVO_DISPOSITIVO, nProgrDisp);

					scarico.setAttribute(ScaricoDAO.ANTENNA_DI_RIFERIMENTO, receiver);
					scarico.setAttribute(ScaricoDAO.DATA_DI_SCARICO, date.replace(".", "/"));
					scarico.setAttribute(ScaricoDAO.ORA_DI_LETTURA, time);
					scarico.setAttribute(ScaricoDAO.N_FABBRICA_DISPOSITIVO, identnr);
					scarico.setAttribute(ScaricoDAO.CODICE_DI_PRODUZIONE, manufact);
					scarico.setAttribute(ScaricoDAO.VERSIONE_SOFTWARE, version);
					scarico.setAttribute(ScaricoDAO.FATTORE_ENERGIA, WeraUtils.pulisciFattoreEnergia(devtype));
					if (status.equals("0")) {
						status = "";
					}
					scarico.setAttribute(ScaricoDAO.ANOMALIA, status);
					scarico.setAttribute(ScaricoDAO.DATA_ANOMALIA, errordate.replace(".", "/"));
					String letturaAttuale = "";
					if (!powerstatus.equals("")) {
						letturaAttuale = powerstatus;
					}
					scarico.setAttribute(ScaricoDAO.LETTURA_ATTUALE, letturaAttuale);

					String stat1 = "";
					String stat2 = "";
					String stat3 = "";
					String stat4 = "";
					String stat5 = "";
					String stat6 = "";
					String stat7 = "";
					String stat8 = "";
					String stat9 = "";
					String stat10 = "";
					String stat11 = "";
					String stat12 = "";
					String stat13 = "";
					String stat14 = "";
					String stat15 = "";
					String stat16 = "";
					String stat17 = "";
					String stat18 = "";
					String dataScarico = date.replace(".", "/");
					String energiaCumul = powerstatus;
					String volumeCumul = "";
					String umEnergiaCumul = "";
					String dataLettura = "";
					String oraLettura = "";
					String anomalia = status;
					String dataAnomalia = errordate.replace(".", "/");
					String oraAnomalia = "";
					String unitStat = "";
					
					String nFabbricaDisp = identnr;

					String valuePrecedente = "";
					Boolean tipo13=false;

					if (!(emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees() == null)) {
						for (int w = 0; w < emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees()
								.size(); w++) {

							String value = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees().get(w)
									.getValue();
							String dimension = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees()
									.get(w).getDimension();
							// String tariff = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees().get(w).getTariff();
							// String subunit = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees().get(w).getSubunit();

							if (dimension.toUpperCase().equals("WH")) {
								dimension = "kWh";
								value = Float.toString((Float.parseFloat(value) / 1000));
							}
							
							String unitaDiMisura=UtilsDatiRilevatori.setUnitaDiMisura(dimension);
							

							if (w == 0) {
								if (!dimension.toLowerCase().equals("date")) {
									scarico.setAttribute(ScaricoDAO.LETTURA_ATTUALE, value.replace(",", "."));
									//scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA,dimension.replace(".", ""));
									//scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA2,dimension.replace(".", ""));
									scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA,unitaDiMisura);
									scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA2,unitaDiMisura);
									umEnergiaCumul = dimension.replace(".", "");
									valuePrecedente = value.replace(",", ".");
								}
							}

							if (w == 1) {
								if (!dimension.toLowerCase().equals("date") && valuePrecedente.equals("")) {
									scarico.setAttribute(ScaricoDAO.LETTURA_ATTUALE, value.replace(",", "."));
									//scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA,dimension.replace(".", ""));
									//scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA2,dimension.replace(".", ""));
									scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA,unitaDiMisura);
									scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA2,unitaDiMisura);
									umEnergiaCumul = dimension.replace(".", "");
								}
							}

							if (w == 2) {
								if (value.contains("x")) {
									value = "";
								}

								if (!dimension.toLowerCase().equals("date")) {
									if (value.contains(":")) {
										value = "";
									}
									if (dimension.toUpperCase().equals("DATE")) {
										dimension = "HCA";
										scarico.setAttribute(ScaricoDAO.LETTURA_A_DATA_DI_SCARICO,
												value.replace(",", "."));
									}
									if (dimension.toUpperCase().equals("M3")) {
										scarico.setAttribute(ScaricoDAO.VOLUME_ATTUALE,WeraUtils.notNumericToNumeric(volumeCumul));
										volumeCumul = value.toLowerCase().replace(",", ".").replace("x", "").trim();
									}

									if (dimension.replace(".", "").trim().equals("")) {
										dimension = umEnergiaCumul;
									}

									//scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA2,dimension.replace(".", ""));
									scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA2,unitaDiMisura);
									
								} else {
									dataLettura = WeraUtils.formattaData(value);
									scarico.setAttribute(ScaricoDAO.DATA_DI_LETTURA, dataLettura);
									
								}
							}

							if (w == 3) {
								if (!dimension.toLowerCase().equals("date")) {
									if (dimension.toUpperCase().equals("DATE")) {
										dimension = "HCA";
										scarico.setAttribute(ScaricoDAO.LETTURA_A_DATA_DI_SCARICO,
												value.replace(",", "."));
									}
									if (dimension.toUpperCase().equals("M3")) {
										scarico.setAttribute(ScaricoDAO.VOLUME_ATTUALE,WeraUtils.notNumericToNumeric(volumeCumul));
										volumeCumul = value.toLowerCase().replace(",", ".").replace("x", "").trim();
									}

									if (dimension.replace(".", "").trim().equals("")) {
										dimension = umEnergiaCumul;
									}
									//scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA2,dimension.replace(".", ""));
									scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA2,unitaDiMisura);
								} else {
									dataLettura = WeraUtils.formattaData(value);
									scarico.setAttribute(ScaricoDAO.DATA_DI_LETTURA, dataLettura);
									
								}

							}

							if (w == 5) {
								if (value.contains("x")) {
									value = date.replace(".", "/");
								}
								if (value.length()>10){
									value=value.substring(0, 10);
								}
								scarico.setAttribute(ScaricoDAO.DATA_INIZIO_STATISTICA,
										value.replace(".", "/"));
								dataInizioStat = value.replace(".", "/");
							}

							if (w == 7) {
								if(!dimension.toLowerCase().equals("date")){
									scarico.setAttribute(ScaricoDAO.STAT_VALUE1, value.replace(",", "."));
									//scarico.setAttribute(ScaricoDAO.UNIT_OF_STAT, dimension.replace(".", ""));
									scarico.setAttribute(ScaricoDAO.UNIT_OF_STAT,unitaDiMisura);
									unitStat = dimension;
									stat1 = value.replace(",", ".");
								}else{
		                            tipo13=true;
		                        }
							}
							
							if (!tipo13){
		                        if(w==8){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE2, value.replace(",", "."));
		                            stat2=value.replace(",", ".");
		                        }
		                        if(w==9){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE3, value.replace(",", "."));
		                            stat3=value.replace(",", ".");
		                        }
		                        if(w==10){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE4, value.replace(",", "."));
		                            stat4=value.replace(",", ".");
		                        }
		                        if(w==11){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE5, value.replace(",", "."));
		                            stat5=value.replace(",", ".");
		                        }
		                        if(w==12){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE6, value.replace(",", "."));
		                            stat6=value.replace(",", ".");
		                        }
		                        if(w==13){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE7, value.replace(",", "."));
		                            stat7=value.replace(",", ".");
		                        }
		                        if(w==14){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE8, value.replace(",", "."));
		                            stat8=value.replace(",", ".");
		                        }
		                        if(w==15){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE9, value.replace(",", "."));
		                            stat9=value.replace(",", ".");
		                        }
		                        if(w==16){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE10, value.replace(",", "."));
		                            stat10=value.replace(",", ".");
		                        }
		                        if(w==17){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE11, value.replace(",", "."));
		                            stat11=value.replace(",", ".");
		                        }
		                        if(w==18){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE12, value.replace(",", "."));
		                            stat12=value.replace(",", ".");
		                        }
		                        if(w==19){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE13, value.replace(",", "."));
		                            stat13=value.replace(",", ".");
		                        }
		                        if(w==20){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE14, value.replace(",", "."));
		                            stat14=value.replace(",", ".");
		                        }
		                        if(w==21){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE15, value.replace(",", "."));
		                            stat15=value.replace(",", ".");
		                        }
		                        if(w==22){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE16, value.replace(",", "."));
		                            stat16=value.replace(",", ".");
		                        }
		                        if(w==23){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE17, value.replace(",", "."));
		                            stat17=value.replace(",", ".");
		                        }
		                        if(w==24){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE18, value.replace(",", "."));
		                            stat18=value.replace(",", ".");
		                        }
		                    }else{
		                        if(w==9){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE1, value.replace(",", "."));
		                            stat1=value.replace(",", ".");
		                        }
		                        if(w==10){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE2, value.replace(",", "."));
		                            stat2=value.replace(",", ".");
		                        }
		                        if(w==11){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE3, value.replace(",", "."));
		                            stat3=value.replace(",", ".");
		                        }
		                        if(w==12){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE4, value.replace(",", "."));
		                            stat4=value.replace(",", ".");
		                        }
		                        if(w==13){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE5, value.replace(",", "."));
		                            stat5=value.replace(",", ".");
		                        }
		                        if(w==14){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE6, value.replace(",", "."));
		                            stat6=value.replace(",", ".");
		                        }
		                        if(w==15){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE7, value.replace(",", "."));
		                            stat7=value.replace(",", ".");
		                        }
		                        if(w==16){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE8, value.replace(",", "."));
		                            stat8=value.replace(",", ".");
		                        }
		                        if(w==17){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE9, value.replace(",", "."));
		                            stat9=value.replace(",", ".");
		                        }
		                        if(w==18){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE10, value.replace(",", "."));
		                            stat10=value.replace(",", ".");
		                        }
		                        if(w==19){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE11, value.replace(",", "."));
		                            stat11=value.replace(",", ".");
		                        }
		                        if(w==20){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE12, value.replace(",", "."));
		                            stat12=value.replace(",", ".");
		                        }
		                        if(w==21){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE13, value.replace(",", "."));
		                            stat13=value.replace(",", ".");
		                        }
		                        if(w==22){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE14, value.replace(",", "."));
		                            stat14=value.replace(",", ".");
		                        }
		                        if(w==23){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE15, value.replace(",", "."));
		                            stat15=value.replace(",", ".");
		                        }
		                        if(w==24){
		                            scarico.setAttribute(ScaricoDAO.STAT_VALUE16, value.replace(",", "."));
		                            stat16=value.replace(",", ".");
		                        }
		                    }
						}
					}

					scarico.setAttribute(ScaricoDAO.NOME_FILE, nomeFile);
					scarico.setAttribute(ScaricoDAO.ID_AZIENDA, azienda);
					scarico.setAttribute(ScaricoDAO.DATA_IMPORT, dataImport);
					scarico.setAttribute(ScaricoDAO.ORA_IMPORT, oraImport);
					scarico.setAttribute(ScaricoDAO.ORA_IMPORT_COMPLETA, oraImportCompleta);

					scarico.insert();
					
					
				}
				
				

			}

		}
	}
	
	

	private static void scriviDatiRilevatori(Integer azienda, String nomeFile, SsbServletRequest req, String dataImport,
			String oraImport, Siemeca emps) throws AppCrash {
		
		String identnrNw = "";
		String versionNw = "";
		String[][] arrayCondominio = null;
		
		String oraImportCompleta = WeraUtils.getOrarioCompleto();
		

		for (int x = 0; x < emps.getNetwork().size(); x++) {
			if (!(emps.getNetwork().get(0).getColectordev() == null)) {
				for (int y = 0; y < emps.getNetwork().get(0).getColectordev().size(); y++) {
					// String fabnrNw = emps.getNetwork().get(x).getColectordev().get(y).getFabnr();
					// String modelNw = emps.getNetwork().get(x).getColectordev().get(y).getModel();
					// String paramsetNw = emps.getNetwork().get(x).getColectordev().get(y).getParamset();
					// String hoursonNw = emps.getNetwork().get(x).getColectordev().get(y).getHourson();
					// String errorflagsNw = emps.getNetwork().get(x).getColectordev().get(y).getErrorflags();
					// String subnetNw = emps.getNetwork().get(x).getColectordev().get(y).getSubnet();
					String errordateNw = emps.getNetwork().get(x).getColectordev().get(y).getErrordate();
					String dateNw = emps.getNetwork().get(x).getColectordev().get(y).getDate();
					String timeNw = emps.getNetwork().get(x).getColectordev().get(y).getTime();
					String receiverNw = emps.getNetwork().get(x).getColectordev().get(y).getReceiver();
					String powerstatusNw = emps.getNetwork().get(x).getColectordev().get(y).getPowerstatus();

					identnrNw = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0)
							.getIdentnr();
					String manufactNw = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0)
							.getManufact();
					versionNw = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0)
							.getVersion();
					String devtypeNw = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0)
							.getDevtype();
					String statusNw = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0)
							.getStatus();
					// String accessnrNw = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0).getAccessnr();
					// String signatureNw = emps.getNetwork().get(x).getColectordev().get(y).getEmployees().get(0).getSignature();

					String unitaDiMisura=UtilsDatiRilevatori.setUnitaDiMisura(devtypeNw);


					
					if (dateNw.length()>10){
						dateNw=dateNw.substring(0, 10);
					}
					
					if (errordateNw.length()>10){
						errordateNw=errordateNw.substring(0, 10);
					}
					
					
					Integer idCondominio=WeraUtils.trovaIdCondominio(identnrNw);
				    String datiCondominio=WeraUtils.trovaDatiCondominio(identnrNw);
				    if (!datiCondominio.equals("") && req!=null){
			        	req.getSession(false).setAttribute("DATI_CONDOMINIO", datiCondominio);
			        }
				   
					if (statusNw.equals("0")) {
						statusNw = "";
					}
										
					arrayCondominio=creaArrayCondominio(idCondominio.toString());

				}
			}
			if (!(emps.getNetwork().get(0).getMeasuredev() == null)) {
				String dataInizioStat = "";
				for (int z = 0; z < emps.getNetwork().get(0).getMeasuredev().size(); z++) {
					// String fabnr = emps.getNetwork().get(x).getMeasuredev().get(z).getFabnr();
					// String model = emps.getNetwork().get(x).getMeasuredev().get(z).getModel();
					// String paramset = emps.getNetwork().get(x).getMeasuredev().get(z).getParamset();
					// String hourson = emps.getNetwork().get(x).getMeasuredev().get(z).getHourson();
					// String errorflags = emps.getNetwork().get(x).getMeasuredev().get(z).getErrorflags();
					// String subnet = emps.getNetwork().get(x).getMeasuredev().get(z).getSubnet();
					String errordate = emps.getNetwork().get(x).getMeasuredev().get(z).getErrordate();
					String date = emps.getNetwork().get(x).getMeasuredev().get(z).getDate();
					String time = emps.getNetwork().get(x).getMeasuredev().get(z).getTime();
					String receiver = emps.getNetwork().get(x).getMeasuredev().get(z).getReceiver();
					String powerstatus = emps.getNetwork().get(x).getMeasuredev().get(z).getPowerstatus();

					String identnr = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0)
							.getIdentnr();
					String manufact = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0)
							.getManufact();
					String version = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0)
							.getVersion();
					String devtype = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0)
							.getDevtype();
					String status = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0)
							.getStatus();
					// String accessnr = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0).getAccessnr();
					// String signature = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees2().get(0).getSignature();
					
					if (date.length()>10){
						date=date.substring(0, 10);
					}
					if (errordate.length()>10){
						errordate=errordate.substring(0, 10);
					}
					
					Integer idCondominio=WeraUtils.trovaIdCondominio(identnr);
				    String datiCondominio=WeraUtils.trovaDatiCondominio(identnr);
				    if (!datiCondominio.equals("") && req!=null){
			        	req.getSession(false).setAttribute("DATI_CONDOMINIO", datiCondominio);
			        }
				    
					String nProgrDisp = Integer.toString(z + 1);
										if (status.equals("0")) {
						status = "";
					}
					String letturaAttuale = "";
					if (!powerstatus.equals("")) {
						letturaAttuale = powerstatus;
					}
					
					String stat1 = "";
					String stat2 = "";
					String stat3 = "";
					String stat4 = "";
					String stat5 = "";
					String stat6 = "";
					String stat7 = "";
					String stat8 = "";
					String stat9 = "";
					String stat10 = "";
					String stat11 = "";
					String stat12 = "";
					String stat13 = "";
					String stat14 = "";
					String stat15 = "";
					String stat16 = "";
					String stat17 = "";
					String stat18 = "";
					String dataScarico = date.replace(".", "/");
					String energiaCumul = powerstatus;
					String volumeCumul = "";
					String umEnergiaCumul = "";
					String dataLettura = "";
					String oraLettura = "";
					String anomalia = status;
					String dataAnomalia = errordate.replace(".", "/");
					String oraAnomalia = "";
					String unitStat = "";
					
					String nFabbricaDisp = identnr;

					String valuePrecedente = "";
					Boolean tipo13=false;

					if (!(emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees() == null)) {
						for (int w = 0; w < emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees()
								.size(); w++) {

							String value = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees().get(w)
									.getValue();
							String dimension = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees()
									.get(w).getDimension();
							// String tariff = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees().get(w).getTariff();
							// String subunit = emps.getNetwork().get(x).getMeasuredev().get(z).getEmployees().get(w).getSubunit();

							if (dimension.toUpperCase().equals("WH")) {
								dimension = "kWh";
								value = Float.toString((Float.parseFloat(value) / 1000));
							}
							
							String unitaDiMisura=UtilsDatiRilevatori.setUnitaDiMisura(dimension);
							

							if (w == 0) {
								if (!dimension.toLowerCase().equals("date")) {
									umEnergiaCumul = dimension.replace(".", "");
									valuePrecedente = value.replace(",", ".");
								}
							}

							if (w == 1) {
								if (!dimension.toLowerCase().equals("date") && valuePrecedente.equals("")) {
									umEnergiaCumul = dimension.replace(".", "");
								}
							}

							if (w == 2) {
								if (value.contains("x")) {
									value = "";
								}

								if (!dimension.toLowerCase().equals("date")) {
									if (value.contains(":")) {
										value = "";
									}
									if (dimension.toUpperCase().equals("DATE")) {
										dimension = "HCA";
									}
									if (dimension.toUpperCase().equals("M3")) {
										volumeCumul = value.toLowerCase().replace(",", ".").replace("x", "").trim();
									}

									if (dimension.replace(".", "").trim().equals("")) {
										dimension = umEnergiaCumul;
									}

									
								} else {
									dataLettura = WeraUtils.formattaData(value);
								}
							}

							if (w == 3) {
								if (!dimension.toLowerCase().equals("date")) {
									if (dimension.toUpperCase().equals("DATE")) {
										dimension = "HCA";
									}
									if (dimension.toUpperCase().equals("M3")) {
										volumeCumul = value.toLowerCase().replace(",", ".").replace("x", "").trim();
									}

									if (dimension.replace(".", "").trim().equals("")) {
										dimension = umEnergiaCumul;
									}
								} else {
									dataLettura = WeraUtils.formattaData(value);
								}

							}

							if (w == 5) {
								if (value.contains("x")) {
									value = date.replace(".", "/");
								}
								if (value.length()>10){
									value=value.substring(0, 10);
								}

								dataInizioStat = value.replace(".", "/");
							}

							if (w == 7) {
								if(!dimension.toLowerCase().equals("date")){
									unitStat = dimension;
									stat1 = value.replace(",", ".");
								}else{
		                            tipo13=true;
		                        }
							}
							
							if (!tipo13){
		                        if(w==8){
		                            stat2=value.replace(",", ".");
		                        }
		                        if(w==9){
		                            stat3=value.replace(",", ".");
		                        }
		                        if(w==10){
		                            stat4=value.replace(",", ".");
		                        }
		                        if(w==11){
		                            stat5=value.replace(",", ".");
		                        }
		                        if(w==12){
		                            stat6=value.replace(",", ".");
		                        }
		                        if(w==13){
		                            stat7=value.replace(",", ".");
		                        }
		                        if(w==14){
		                            stat8=value.replace(",", ".");
		                        }
		                        if(w==15){
		                            stat9=value.replace(",", ".");
		                        }
		                        if(w==16){
		                            stat10=value.replace(",", ".");
		                        }
		                        if(w==17){
		                            stat11=value.replace(",", ".");
		                        }
		                        if(w==18){
		                            stat12=value.replace(",", ".");
		                        }
		                        if(w==19){
		                            stat13=value.replace(",", ".");
		                        }
		                        if(w==20){
		                            stat14=value.replace(",", ".");
		                        }
		                        if(w==21){
		                            stat15=value.replace(",", ".");
		                        }
		                        if(w==22){
		                            stat16=value.replace(",", ".");
		                        }
		                        if(w==23){
		                            stat17=value.replace(",", ".");
		                        }
		                        if(w==24){
		                            stat18=value.replace(",", ".");
		                        }
		                    }else{
		                        if(w==9){
		                            stat1=value.replace(",", ".");
		                        }
		                        if(w==10){
		                            stat2=value.replace(",", ".");
		                        }
		                        if(w==11){
		                            stat3=value.replace(",", ".");
		                        }
		                        if(w==12){
		                            stat4=value.replace(",", ".");
		                        }
		                        if(w==13){
		                            stat5=value.replace(",", ".");
		                        }
		                        if(w==14){
		                            stat6=value.replace(",", ".");
		                        }
		                        if(w==15){
		                            stat7=value.replace(",", ".");
		                        }
		                        if(w==16){
		                            stat8=value.replace(",", ".");
		                        }
		                        if(w==17){
		                            stat9=value.replace(",", ".");
		                        }
		                        if(w==18){
		                            stat10=value.replace(",", ".");
		                        }
		                        if(w==19){
		                            stat11=value.replace(",", ".");
		                        }
		                        if(w==20){
		                            stat12=value.replace(",", ".");
		                        }
		                        if(w==21){
		                            stat13=value.replace(",", ".");
		                        }
		                        if(w==22){
		                            stat14=value.replace(",", ".");
		                        }
		                        if(w==23){
		                            stat15=value.replace(",", ".");
		                        }
		                        if(w==24){
		                            stat16=value.replace(",", ".");
		                        }
		                    }
						}
					}
					

					// popola DATI_RILEVATORI

					popolaDatiRilevatori(azienda, nomeFile, dataImport, oraImport, arrayCondominio, dataInizioStat,
							identnr, devtype, idCondominio, stat1, stat2, stat3, stat4, stat5, stat6, stat7, stat8,
							stat9, stat10, stat11, stat12, stat13, stat14, stat15, stat16, stat17, stat18,
							dataScarico, energiaCumul, volumeCumul, umEnergiaCumul, dataLettura, oraLettura,
							anomalia, dataAnomalia, oraAnomalia, unitStat, nFabbricaDisp);
					
					
				}
				
				

			}

		}
	}

	private static void popolaDatiRilevatori(Integer azienda, String nomeFile, String dataImport, String oraImport,
			String[][] arrayCondominio, String dataInizioStat, String identnr, String devtype, Integer idCondominio,
			String stat1, String stat2, String stat3, String stat4, String stat5, String stat6, String stat7,
			String stat8, String stat9, String stat10, String stat11, String stat12, String stat13, String stat14,
			String stat15, String stat16, String stat17, String stat18, String dataScarico, String energiaCumul,
			String volumeCumul, String umEnergiaCumul, String dataLettura, String oraLettura, String anomalia,
			String dataAnomalia, String oraAnomalia, String unitStat, String nFabbricaDisp) throws AppCrash {
		if (!identnr.startsWith("1")) {
			String idRilevatore = Utils.getUnique();
			String lettura = "0";

			String letturaTemp = lettura.replaceAll("0", "");
			if (letturaTemp.trim().equals("")) {
				lettura = "0";
			}

			String meseStat = "";
			String anno = "";
			String annoDaSalvare = "";


			if (dataInizioStat.length() == 10) {
				meseStat = dataInizioStat.substring(3, 5);
				anno = dataInizioStat.substring(6);

				if (stat18.toLowerCase().contains("x") || (stat18.equals(""))) {
					stat18 = "0";
				}
				if (stat17.toLowerCase().contains("x") || (stat17.equals(""))) {
					stat17 = stat18;
				}
				if (stat16.toLowerCase().contains("x") || (stat16.equals(""))) {
					stat16 = stat17;
				}
				if (stat15.toLowerCase().contains("x") || (stat15.equals(""))) {
					stat15 = stat16;
				}
				if (stat14.toLowerCase().contains("x") || (stat14.equals(""))) {
					stat14 = stat15;
				}
				if (stat13.toLowerCase().contains("x") || (stat13.equals(""))) {
					stat13 = stat14;
				}
				if (stat12.toLowerCase().contains("x") || (stat12.equals(""))) {
					stat12 = stat13;
				}
				if (stat11.toLowerCase().contains("x") || (stat11.equals(""))) {
					stat11 = stat12;
				}
				if (stat10.toLowerCase().contains("x") || (stat10.equals(""))) {
					stat10 = stat11;
				}
				if (stat9.toLowerCase().contains("x") || (stat9.equals(""))) {
					stat9 = stat10;
				}
				if (stat8.toLowerCase().contains("x") || (stat8.equals(""))) {
					stat8 = stat9;
				}
				if (stat7.toLowerCase().contains("x") || (stat7.equals(""))) {
					stat7 = stat8;
				}
				if (stat6.toLowerCase().contains("x") || (stat6.equals(""))) {
					stat6 = stat7;
				}
				if (stat5.toLowerCase().contains("x") || (stat5.equals(""))) {
					stat5 = stat6;
				}
				if (stat4.toLowerCase().contains("x") || (stat4.equals(""))) {
					stat4 = stat5;
				}
				if (stat3.toLowerCase().contains("x") || (stat3.equals(""))) {
					stat3 = stat4;
				}
				if (stat2.toLowerCase().contains("x") || (stat2.equals(""))) {
					stat2 = stat3;
				}
				if (stat1.toLowerCase().contains("x") || (stat1.equals(""))) {
					stat1 = stat2;
				}

				if (energiaCumul.toLowerCase().contains("x") || (energiaCumul.equals(""))) {
					energiaCumul = "0";
				}

				if (volumeCumul.toLowerCase().contains("x") || (volumeCumul.equals(""))) {
					volumeCumul = "0";
				}

				Float floatStat1 = Float.parseFloat(stat1);
				Float floatStat2 = Float.parseFloat(stat2);
				Float floatStat3 = Float.parseFloat(stat3);
				Float floatStat4 = Float.parseFloat(stat4);
				Float floatStat5 = Float.parseFloat(stat5);
				Float floatStat6 = Float.parseFloat(stat6);
				Float floatStat7 = Float.parseFloat(stat7);
				Float floatStat8 = Float.parseFloat(stat8);
				Float floatStat9 = Float.parseFloat(stat9);
				Float floatStat10 = Float.parseFloat(stat10);
				Float floatStat11 = Float.parseFloat(stat11);
				Float floatStat12 = Float.parseFloat(stat12);
				Float floatStat13 = Float.parseFloat(stat13);
				Float floatStat14 = Float.parseFloat(stat14);
				Float floatStat15 = Float.parseFloat(stat15);
				Float floatStat16 = Float.parseFloat(stat16);
				Float floatStat17 = Float.parseFloat(stat17);
				Float floatStat18 = Float.parseFloat(stat18);
				int intMeseStat = Integer.parseInt(meseStat);
				
				
				

				UtilsDatiRilevatori.doValoreMensile(idCondominio, dataScarico, energiaCumul,
						volumeCumul, umEnergiaCumul, dataImport, oraImport, dataLettura, oraLettura,
						anomalia, dataAnomalia, oraAnomalia, unitStat, nomeFile,
						nFabbricaDisp, idRilevatore, lettura, meseStat, anno, annoDaSalvare,
						floatStat1, floatStat2, floatStat3, floatStat4, floatStat5, floatStat6,
						floatStat7, floatStat8, floatStat9, floatStat10, floatStat11, floatStat12,
						floatStat13, floatStat14, floatStat15, floatStat16, floatStat17,
						floatStat18, intMeseStat,azienda,devtype,arrayCondominio);

			}
		}
	}
	
	
	private static Integer dimensioneArrayCondominio(String idCondominio) throws AppCrash {

		Integer dimensione=0;
		DataSet_itf dataSet = null;
        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_DATI_MATRICE_DIMENSIONE);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("ID_CONDOMINIO", idCondominio);
            dataSet.setParam(params);
            dataSet.open();
            if (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	dimensione=(Integer) dbRow.getField("DIMENSIONE");
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
                    //ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
                }
            }
        }
		return dimensione;
	}
	
	

        
    private static String[][] creaArrayCondominio(String idCondominio) throws AppCrash {
        String[][] matrix = new String[dimensioneArrayCondominio(idCondominio)][2];

        Integer i=0;
        DataSet_itf dataSet = null;
        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_DATI_MATRICE);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("ID_CONDOMINIO", idCondominio);
            dataSet.setParam(params);
            dataSet.open();
            while (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	matrix[i][0]=dbRow.getField("RILEVATORE").toString().trim();
            	matrix[i][1]=dbRow.getField("MESEANNO").toString().trim();
            	i=i+1;
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
                    //ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
                }
            }
        }
        return matrix;
    }
	
	
	private static String[] creaArrayRilevatore(String identnr) throws AppCrash {

		String[] array = new String[2];

        DataSet_itf dataSet = null;
        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_DATI_MATRICE);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("RILEVATORE", identnr);
            dataSet.setParam(params);
            dataSet.open();
            if (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	array[0]=identnr;
            	array[1]=dbRow.getField("MESEANNO").toString().trim();
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
                    //ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
                }
            }
        }
        return array;
    }
	

	private boolean verificaNomeFile(UserSecurityInfo userInfo, String nomeFile) throws AppCrash {

		if (!nomeFile.toLowerCase().endsWith(".xml") && !nomeFile.toLowerCase().endsWith(".bil")  && !nomeFile.toLowerCase().endsWith(".csv")) {
			return false;
		} else {
			return true;
		}

	}
	
	
	
	
	
	 private void scriviDatiScarico(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo,
	            int rowCounter, String nomeFileBil, String networkCliente,
	            String networkFabbrica, String swVersion, Integer azienda, String dataImport, String oraImport) throws AppCrash {

	        
		 	String[][] arrayCondominio = null;
	        
	        String nProgrDisp = _fieldValues.get(0).trim();
	        String antennaRif = _fieldValues.get(1).trim();
	        String dataScarico = WeraUtils.formattaData(_fieldValues.get(25).trim()); 
	        String oraLettura = _fieldValues.get(3).trim();
	        String KDevAdr292 = _fieldValues.get(4).trim();
	        String nFabbricaDisp = _fieldValues.get(5).trim();
	        String codProd = _fieldValues.get(7).trim();
	        String versSW = _fieldValues.get(8).trim();
	        String fattEnergia = _fieldValues.get(9).trim();
	        String ZDat = _fieldValues.get(10).trim();
	        String ZUhr = _fieldValues.get(11).trim();
	        String CBS104 = _fieldValues.get(12).trim();

	        String anomalia = _fieldValues.get(13).trim();
	        if (anomalia.equals("0")) {
	            anomalia = "";
	        }
	        
	        String unitaDiMisura=UtilsDatiRilevatori.setUnitaDiMisura(fattEnergia);

	        String dataAnomalia = _fieldValues.get(14).trim().replace(".","/");
	        String oraAnomalia = _fieldValues.get(15).trim();
	        String ZFDauer251 = _fieldValues.get(16).trim();
	        String energiaCumul = _fieldValues.get(17).trim().replace(",",".");
	        String umEnergiaCumul 	= _fieldValues.get(18).trim().replace(".","");
	        String volumeCumul = _fieldValues.get(19).trim().replace(",",".");
	        String unVolumeCumul 	= _fieldValues.get(20).trim().replace(".","");
	        String letturaScarico = _fieldValues.get(21).trim().replace(",",".");
	        //String unitaMisura = _fieldValues.get(22).trim().replace(".","");
	        String unitaMisura=unitaDiMisura;
	        String CVolStLVal108 = _fieldValues.get(23).trim();
	        String CVolStLDim108 = _fieldValues.get(24).trim();
	        String dataLettura = _fieldValues.get(2).trim().replace(".","/");
	        String CEnStVlVal111 = _fieldValues.get(26).trim();
	        String CEnStVlDim111 = _fieldValues.get(27).trim();
	        String CVolStVlVal112 = _fieldValues.get(28).trim();
	        String CVolStVlDim112 = _fieldValues.get(29).trim();
	        String ZDatStVl114 = _fieldValues.get(30).trim();
	        String CPQMwMaxVal169 = _fieldValues.get(31).trim();
	        String CPQMwMaxDim169 = _fieldValues.get(32).trim();
	        String ZDatMwMax170 = _fieldValues.get(33).trim();
	        String valCumulTar1 = _fieldValues.get(34).trim();
	        String umValCumulTar1 = _fieldValues.get(35).trim();
	        String CVxTar2Val194 = _fieldValues.get(36).trim();
	        String CVxTar2Dim194 = _fieldValues.get(37).trim();
	        String CVxTar1StLVal193 = _fieldValues.get(38).trim();
	        String CVxTar1StLDim193 = _fieldValues.get(39).trim();
	        String CVxTar2StLVal194 = _fieldValues.get(40).trim();
	        String CVxTar2StLDim194 = _fieldValues.get(41).trim();
	        String dataInizioStat = _fieldValues.get(42).trim().replace(".","/");
	        String stat1 = _fieldValues.get(43).trim().toLowerCase().replace(",",".");
	        String stat2 = _fieldValues.get(44).trim().toLowerCase().replace(",",".");
	        String stat3 = _fieldValues.get(45).trim().toLowerCase().replace(",",".");
	        String stat4 = _fieldValues.get(46).trim().toLowerCase().replace(",",".");
	        String stat5 = _fieldValues.get(47).trim().toLowerCase().replace(",",".");
	        String stat6 = _fieldValues.get(48).trim().toLowerCase().replace(",",".");
	        String stat7 = _fieldValues.get(49).trim().toLowerCase().replace(",",".");
	        String stat8 = _fieldValues.get(50).trim().toLowerCase().replace(",",".");
	        String stat9 = _fieldValues.get(51).trim().toLowerCase().replace(",",".");
	        String stat10 = _fieldValues.get(52).trim().toLowerCase().replace(",",".");
	        String stat11 = _fieldValues.get(53).trim().toLowerCase().replace(",",".");
	        String stat12 = _fieldValues.get(54).trim().toLowerCase().replace(",",".");
	        String stat13 = _fieldValues.get(55).trim().toLowerCase().replace(",",".");
	        String stat14 = _fieldValues.get(56).trim().toLowerCase().replace(",",".");
	        String stat15 = _fieldValues.get(57).trim().toLowerCase().replace(",",".");
	        String stat16 = _fieldValues.get(58).trim().toLowerCase().replace(",",".");
	        String stat17 = _fieldValues.get(59).trim().toLowerCase().replace(",",".");
	        String stat18 = _fieldValues.get(60).trim().toLowerCase().replace(",",".");
	        //String unitStat = _fieldValues.get(61).trim().replace(".","");
	        String unitStat = unitaDiMisura;
	        
	        String oraImportCompleta = WeraUtils.getOrarioCompleto();
	        
	        Integer idCondominio=WeraUtils.trovaIdCondominio(nFabbricaDisp);
	        String datiCondominio=WeraUtils.trovaDatiCondominio(nFabbricaDisp);
	        if (!datiCondominio.equals("") && req!=null){
	        	req.getSession(false).setAttribute("DATI_CONDOMINIO", datiCondominio);
	        }

	        ScaricoDAO scarico = new ScaricoDAO();
	        scarico.setAttribute(ScaricoDAO.ID_AZIENDA, azienda);
	        scarico.setAttribute(ScaricoDAO.ID_CONDOMINIO, idCondominio);
	        scarico.setAttribute(ScaricoDAO.DATA_IMPORT, dataImport);
	        scarico.setAttribute(ScaricoDAO.ORA_IMPORT, oraImport);
	        scarico.setAttribute(ScaricoDAO.ORA_IMPORT_COMPLETA, oraImportCompleta);
	        scarico.setAttribute(ScaricoDAO.NOME_FILE, nomeFileBil);
	        scarico.setAttribute(ScaricoDAO.N_CLIENTE_NETWORK, networkCliente);
	        scarico.setAttribute(ScaricoDAO.N_FABBRICA_NETWORK, networkFabbrica);
	        scarico.setAttribute(ScaricoDAO.VERSIONE_SOFTWARE_I, swVersion);
	        scarico.setAttribute(ScaricoDAO.DATA_DI_LETTURA_I, "");
	        scarico.setAttribute(ScaricoDAO.GIORNO_SETTIMANA, "");
	        scarico.setAttribute(ScaricoDAO.ORA_DI_LETTURA_I, "");
	        scarico.setAttribute(ScaricoDAO.OPERAT_HOURS, "");
	        scarico.setAttribute(ScaricoDAO.ANOMALIA_I, "");
	        scarico.setAttribute(ScaricoDAO.DATA_ANOMALIA_I, "");
	        scarico.setAttribute(ScaricoDAO.ORA_ANOMALIA_I, "");
	        scarico.setAttribute(ScaricoDAO.DISP_RADIO_TROVATI, "");
	        scarico.setAttribute(ScaricoDAO.MODELLO_ANTENNA, "");
	        scarico.setAttribute(ScaricoDAO.N_PROGRESSIVO_DISPOSITIVO, nProgrDisp);
	        scarico.setAttribute(ScaricoDAO.ANTENNA_DI_RIFERIMENTO, antennaRif);
	        scarico.setAttribute(ScaricoDAO.DATA_DI_LETTURA, dataLettura);
	        scarico.setAttribute(ScaricoDAO.ORA_DI_LETTURA, oraLettura);
	        scarico.setAttribute(ScaricoDAO.N_FABBRICA_DISPOSITIVO, nFabbricaDisp);
	        scarico.setAttribute(ScaricoDAO.CODICE_DI_PRODUZIONE, codProd);
	        scarico.setAttribute(ScaricoDAO.VERSIONE_SOFTWARE, versSW);
	        scarico.setAttribute(ScaricoDAO.ANOMALIA, anomalia);
	        scarico.setAttribute(ScaricoDAO.DATA_ANOMALIA, dataAnomalia);
	        scarico.setAttribute(ScaricoDAO.ORA_ANOMALIA, oraAnomalia);
	        scarico.setAttribute(ScaricoDAO.LETTURA_ATTUALE, energiaCumul);
	        scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA, umEnergiaCumul);
	        scarico.setAttribute(ScaricoDAO.VOLUME_ATTUALE, WeraUtils.notNumericToNumeric(volumeCumul));
	        scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA1, unVolumeCumul);
	        scarico.setAttribute(ScaricoDAO.FATTORE_ENERGIA, WeraUtils.pulisciFattoreEnergia(fattEnergia));
	        scarico.setAttribute(ScaricoDAO.LETTURA_A_DATA_DI_SCARICO, letturaScarico);
	        if (unitaMisura.trim().equals("")){
	            unitaMisura=umEnergiaCumul;
	        }
	        scarico.setAttribute(ScaricoDAO.UNITA_DI_MISURA2, unitaMisura);
	        scarico.setAttribute(ScaricoDAO.DATA_DI_SCARICO, dataScarico);
	        scarico.setAttribute(ScaricoDAO.DATA_INIZIO_STATISTICA, dataInizioStat);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE1, stat1);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE2, stat2);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE3, stat3);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE4, stat4);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE5, stat5);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE6, stat6);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE7, stat7);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE8, stat8);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE9, stat9);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE10, stat10);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE11, stat11);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE12, stat12);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE13, stat13);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE14, stat14);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE15, stat15);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE16, stat16);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE17, stat17);
	        scarico.setAttribute(ScaricoDAO.STAT_VALUE18, stat18);
	        scarico.setAttribute(ScaricoDAO.UNIT_OF_STAT, unitStat);
	        scarico.setAttribute(ScaricoDAO.CURRVALUE_OF_TARIFF1, "");
	        scarico.setAttribute(ScaricoDAO.UNIT_OF_TARIFF1, "");
	        scarico.setAttribute(ScaricoDAO.SET_DAY_VAL_OF_TARIFF1, "");
	        scarico.setAttribute(ScaricoDAO.SET_DAY_UNIT_OF_TARIFF1, "");
	        scarico.setAttribute(ScaricoDAO.KDEVADR292, KDevAdr292);
	        scarico.setAttribute(ScaricoDAO.ZDAT, ZDat);
	        scarico.setAttribute(ScaricoDAO.ZUHR, ZUhr);
	        scarico.setAttribute(ScaricoDAO.CBS104, CBS104);
	        scarico.setAttribute(ScaricoDAO.ZFDAUER251, ZFDauer251);
	        scarico.setAttribute(ScaricoDAO.VOLUMECUMUL, volumeCumul);
	        scarico.setAttribute(ScaricoDAO.UNVOLUMECUMUL, unVolumeCumul);
	        scarico.setAttribute(ScaricoDAO.CVOLSTLVAL108, CVolStLVal108);
	        scarico.setAttribute(ScaricoDAO.CVOLSTLDIM108, CVolStLDim108);
	        scarico.setAttribute(ScaricoDAO.CENSTVLVAL111, CEnStVlVal111);
	        scarico.setAttribute(ScaricoDAO.CENSTVLDIM111, CEnStVlDim111);
	        scarico.setAttribute(ScaricoDAO.CVOLSTVLVAL112, CVolStVlVal112);
	        scarico.setAttribute(ScaricoDAO.CVOLSTVLDIM112, CVolStVlDim112);
	        scarico.setAttribute(ScaricoDAO.ZDATSTVL114, ZDatStVl114);
	        scarico.setAttribute(ScaricoDAO.CPQMWMAXVAL169, CPQMwMaxVal169);
	        scarico.setAttribute(ScaricoDAO.CPQMWMAXDIM169, CPQMwMaxDim169);
	        scarico.setAttribute(ScaricoDAO.ZDATMWMAX170, ZDatMwMax170);
	        scarico.setAttribute(ScaricoDAO.VALCUMULTAR1, valCumulTar1);
	        scarico.setAttribute(ScaricoDAO.UMVALCUMULTAR1, umValCumulTar1);
	        scarico.setAttribute(ScaricoDAO.CVXTAR2VAL194, CVxTar2Val194);
	        scarico.setAttribute(ScaricoDAO.CVXTAR2DIM194, CVxTar2Dim194);
	        scarico.setAttribute(ScaricoDAO.CVXTAR1STLVAL193, CVxTar1StLVal193);
	        scarico.setAttribute(ScaricoDAO.CVXTAR1STLDIM193, CVxTar1StLDim193);
	        scarico.setAttribute(ScaricoDAO.CVXTAR2STLVAL194, CVxTar2StLVal194);
	        scarico.setAttribute(ScaricoDAO.CVXTAR2STLDIM194, CVxTar2StLDim194);
	        scarico.setAttribute(ScaricoDAO.VISIBILE, false);
	        scarico.setAttribute(ScaricoDAO.ID_UTENTE_INS, 0);
	        scarico.insert();
	        
	        arrayCondominio=creaArrayCondominio(idCondominio.toString());
	        
	        // popola DATI_RILEVATORI

	        
	        if (!nFabbricaDisp.startsWith("1")) {
	            String idRilevatore = Utils.getUnique();
	            String lettura = "0";
	            String meseStat = "";
	            String anno = "";
	            String annoDaSalvare = "";
	            
	          
	           
	            if (dataInizioStat.length() == 10) {
	                meseStat = dataInizioStat.substring(3, 5);
	                anno = dataInizioStat.substring(6);

	                if (stat18.toLowerCase().contains("x")) {
	                    stat18 = "0";
	                }
	                if (stat17.toLowerCase().contains("x")) {
	                    stat17 = stat18;
	                }
	                if (stat16.toLowerCase().contains("x")) {
	                    stat16 = stat17;
	                }
	                if (stat15.toLowerCase().contains("x")) {
	                    stat15 = stat16;
	                }
	                if (stat14.toLowerCase().contains("x")) {
	                    stat14 = stat15;
	                }
	                if (stat13.toLowerCase().contains("x")) {
	                    stat13 = stat14;
	                }
	                if (stat12.toLowerCase().contains("x")) {
	                    stat12 = stat13;
	                }
	                if (stat11.toLowerCase().contains("x")) {
	                    stat11 = stat12;
	                }
	                if (stat10.toLowerCase().contains("x")) {
	                    stat10 = stat11;
	                }
	                if (stat9.toLowerCase().contains("x")) {
	                    stat9 = stat10;
	                }
	                if (stat8.toLowerCase().contains("x")) {
	                    stat8 = stat9;
	                }
	                if (stat7.toLowerCase().contains("x")) {
	                    stat7 = stat8;
	                }
	                if (stat6.toLowerCase().contains("x")) {
	                    stat6 = stat7;
	                }
	                if (stat5.toLowerCase().contains("x")) {
	                    stat5 = stat6;
	                }
	                if (stat4.toLowerCase().contains("x")) {
	                    stat4 = stat5;
	                }
	                if (stat3.toLowerCase().contains("x")) {
	                    stat3 = stat4;
	                }
	                if (stat2.toLowerCase().contains("x")) {
	                    stat2 = stat3;
	                }
	                if (stat1.toLowerCase().contains("x")) {
	                    stat1 = stat2;
	                }
	                
	                if (energiaCumul.toLowerCase().contains("x") || (energiaCumul.equals(""))) {
	                    energiaCumul = "0";
	                }
	                
	                if (volumeCumul.toLowerCase().contains("x") || (volumeCumul.equals(""))) {
	                    volumeCumul = "0";
	                }

	               
	                Float floatStat1=Float.parseFloat(stat1);
	                Float floatStat2=Float.parseFloat(stat2);
	                Float floatStat3=Float.parseFloat(stat3);
	                Float floatStat4=Float.parseFloat(stat4);
	                Float floatStat5=Float.parseFloat(stat5);
	                Float floatStat6=Float.parseFloat(stat6);
	                Float floatStat7=Float.parseFloat(stat7);
	                Float floatStat8=Float.parseFloat(stat8);
	                Float floatStat9=Float.parseFloat(stat9);
	                Float floatStat10=Float.parseFloat(stat10);
	                Float floatStat11=Float.parseFloat(stat11);
	                Float floatStat12=Float.parseFloat(stat12);
	                Float floatStat13=Float.parseFloat(stat13);
	                Float floatStat14=Float.parseFloat(stat14);
	                Float floatStat15=Float.parseFloat(stat15);
	                Float floatStat16=Float.parseFloat(stat16);
	                Float floatStat17=Float.parseFloat(stat17);
	                Float floatStat18=Float.parseFloat(stat18);
	                int intMeseStat=Integer.parseInt(meseStat);
	                
	              
	   
					
	                UtilsDatiRilevatori.doValoreMensile(idCondominio, dataScarico, energiaCumul, volumeCumul,umEnergiaCumul, dataImport, oraImport,
	                        dataLettura, oraLettura, anomalia, dataAnomalia, oraAnomalia, unitStat,
	                        nomeFileBil, nFabbricaDisp, idRilevatore, lettura, meseStat, anno, annoDaSalvare,
	                        floatStat1, floatStat2, floatStat3, floatStat4, floatStat5, floatStat6, floatStat7, floatStat8, floatStat9, floatStat10, floatStat11, floatStat12,
	                        floatStat13, floatStat14, floatStat15, floatStat16, floatStat17, floatStat18,intMeseStat,azienda,fattEnergia,arrayCondominio);
	                
	                
	                
	            }
	        }
	       
	    }

	
	
	

}