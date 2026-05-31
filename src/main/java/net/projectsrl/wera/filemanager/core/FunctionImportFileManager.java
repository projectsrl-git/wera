
package net.projectsrl.wera.filemanager.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.wm.importdata.UploadedFiles;

/**
 * FunctionImportFileManager
 * 
 */
public class FunctionImportFileManager extends FunctionProjectWebApp_base {


    
    boolean        _cancellarePrecedenteCaricamento = false;
    
    public FunctionImportFileManager() {

        super();
    }

    public FunctionImportFileManager(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {
    	
    	 Map<String, Object> templateData = createMapFromRequest(req, userInfo);
    	 
        if (UploadedFiles.getStatus(UploadedFiles.FILE_CARICATO).equals(UploadedFiles.UPDATE_COMPLETED)) {
            templateData.put("ESITO_KO", "");
            templateData.put("ESITO_OK", UploadedFiles.getStatusMessage(UploadedFiles
                    .getStatus(UploadedFiles.FILE_CARICATO)));
            UploadedFiles.setStatus(UploadedFiles.FILE_CARICATO, UploadedFiles.UPDATE_CED_COMPLETED);
        } else {
            templateData.put("ESITO_KO", UploadedFiles.getStatusMessage(UploadedFiles
                    .getStatus(UploadedFiles.FILE_CARICATO)));
            templateData.put("ESITO_OK", "");
            UploadedFiles.setStatus(UploadedFiles.FILE_CARICATO, UploadedFiles.UPDATE_CED_COMPLETED);
        }
        
        templateData.put("MESSAGGIO_ATTESA", "Elaborazione in corso...");

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);

    }

    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

    	Map<String, Object> templateData = createMapFromRequest(req, userInfo);
        
       
        if (UploadedFiles.getStatus(UploadedFiles.FILE_CARICATO).equals(UploadedFiles.UPDATE_CED_COMPLETED)) {
            templateData.put("ESITO_KO", "");
            templateData.put("ESITO_OK", UploadedFiles.getStatusMessage(UploadedFiles
                    .getStatus(UploadedFiles.FILE_CARICATO)));
            UploadedFiles.setStatus(UploadedFiles.FILE_CARICATO, UploadedFiles.UPDATE_CED_COMPLETED);
        } else {
            templateData.put("ESITO_KO", UploadedFiles.getStatusMessage(UploadedFiles
                    .getStatus(UploadedFiles.FILE_CARICATO)));
            templateData.put("ESITO_OK", "");
            UploadedFiles.setStatus(UploadedFiles.FILE_CARICATO, UploadedFiles.UPDATE_CED_COMPLETED);
        }
    
        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

}
