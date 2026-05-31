package net.projectsrl.wera.filemanager.core;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;

import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.authentication.MenuItem;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.wera.filemanager.db.ElencoFileImportatiFileManagerDAO;
import net.projectsrl.wera.utils.WeraUtils;
import net.projectsrl.wm.importdata.UploadedFiles;
import net.projectsrl.wm.utils.Utils;

/**
 * FunctionFileUpload
 * 
 */
@SuppressWarnings("deprecation")
public class FunctionFileUploadFileManager extends FunctionProjectWebApp_base {

	private static final String PAGE = "importdatianagraficautenze";
	private static final int _sizeMax = 100000000;

	public FunctionFileUploadFileManager() {

		super();
	}

	public FunctionFileUploadFileManager(ApplicationServices_itf applServices, String functionID,
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

		uploadFiles(req,userInfo);

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
			String function = "FileUploadFileManager";
			String link = "astro?FUNCTIONID=FileUploadFileManager";
			int itemLevel = 2;
			String path = "1520";
			String linkChain = "astro?FUNCTIONID=Home;#;astro?FUNCTIONID=FileUploadFileManager";
			boolean readOnly = false;
			int nrOfChildren = 0;
			String icon = "";

			pathDescri = "Home / File Manager / Importazione file";

			MenuItem selectedMenuItem = new MenuItem(menuId, menuIdSup, languageISO, label, function, link, itemLevel,
					path, pathDescri, linkChain, readOnly, nrOfChildren, icon);

			map.put(WebAppConstants_itf.SELECTED_MENU_ITEM, selectedMenuItem);

			map.put(WebAppConstants_itf.INCLUDED_MENU, "include/included_menu.include");
			map.put(WebAppConstants_itf.SELECTED_PATH, path);

		}

		return pathDescri;

	}

	@SuppressWarnings("deprecation")
	private boolean uploadFiles(SsbServletRequest req, UserSecurityInfo userInfo) {

		String fileType = req.getField(UploadedFiles.FILE_TYPE);
		String name = "";
		String idCondominio = "";
		String idCategoria= "";
		String dataPrimaSegnalazione= "";
		String ggSegnalazione ="";

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
							Config.GetInstance().getProperty("directory.external_files",_applicationSrv.getRoot()) + Config.GetInstance().getProperty("cartella.upload.filemanager"), fileName);
					UploadedFiles.setStatus(fileType, UploadedFiles.UPLOADING, fileName, "");

					fi.write(fNew);

					UploadedFiles.setStatus(fileType, UploadedFiles.UPLOAD_COMPLETED, fileName, "");
					creaElencoFile(fileName, idCondominio, idCategoria, dataPrimaSegnalazione, ggSegnalazione, userInfo);
				}else{
					name = fi.getFieldName();
					if (name.equals("ID_CONDOMINIO")){
						idCondominio = fi.getString();
					}
					if (name.equals("ID_CATEGORIA")){
						idCategoria = fi.getString();
					}
					if (name.equals("DATA_PRIMA_SEGNALAZIONE")){
						dataPrimaSegnalazione = fi.getString();
					}
					if (name.equals("GG_SEGNALAZIONE")){
						ggSegnalazione = fi.getString();
					}
					
				    
				}
			}

			return true;

		} catch (Throwable e) {
			new AppCrash(e);
			UploadedFiles.setStatus(fileType, UploadedFiles.UPLOAD_ERROR, fileName, "");
			return false;
		}

	}

	private void creaElencoFile(String filename, String idCondominio, String idCategoria, String dataPrimaSegnalazione, String ggSegnalazione, UserSecurityInfo userInfo) throws AppCrash {
		Integer azienda = WeraUtils.trovaIdAziendaUtente(getSpecificUserInfo(userInfo).getIdUtente().toString());
		String dataImport = Utils.getStringDataOggiRibaltata();
		String oraImport = Utils.getOrario();
		
		ElencoFileImportatiFileManagerDAO dao = new ElencoFileImportatiFileManagerDAO();
		dao.setAttribute(ElencoFileImportatiFileManagerDAO.ID_AZIENDA, azienda);
		dao.setAttribute(ElencoFileImportatiFileManagerDAO.ID_CONDOMINIO, idCondominio);
		dao.setAttribute(ElencoFileImportatiFileManagerDAO.ID_CATEGORIA, idCategoria);
		dao.setAttribute(ElencoFileImportatiFileManagerDAO.NOME_FILE, filename);
		dao.setAttribute(ElencoFileImportatiFileManagerDAO.DATA_PRIMA_SEGNALAZIONE, dataPrimaSegnalazione);
		dao.setAttribute(ElencoFileImportatiFileManagerDAO.GG_SEGNALAZIONE, ggSegnalazione);
		dao.setAttribute(ElencoFileImportatiFileManagerDAO.DATA_IMPORT, dataImport);
		dao.setAttribute(ElencoFileImportatiFileManagerDAO.ORA_IMPORT, oraImport);
		dao.setAttribute(ElencoFileImportatiFileManagerDAO.ID_UTENTE_INS, 0);
		dao.insert();
	}

	

}