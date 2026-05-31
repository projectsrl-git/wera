
package net.projectsrl.wera.importazioni.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.wm.importdata.UploadedFiles;

/**
 * FunctionImportCedolini
 * 
 */
public class FunctionImportDatiRilevatori extends FunctionProjectWebApp_base {


    
    boolean        _cancellarePrecedenteCaricamento = false;
    
    public FunctionImportDatiRilevatori() {

        super();
    }

    public FunctionImportDatiRilevatori(ApplicationServices_itf applServices, String functionID, String functionName) {

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
        
        
        DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();

        List<String> listaNomeFile = new ArrayList<>();  // lista per accumulare i nomi file

        try {
            dataSet = dsFactory.makeDataSet("", "DSNomeFileImportati");

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("WHERECONDITION_AZIENDE", "IS NOT NULL");
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                String nomeFile = dbRow.getField("NOME_FILE").toString();
                listaNomeFile.add(nomeFile);  // aggiungo il valore alla lista
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),
                    "Errore nella ricerca dell'ultimo progressivo del dataset " + "DSDatiRipartizioni");
            throw ac;
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DSDatiRipartizioni");
                }
            }
        }

        // Ora puoi mettere la lista nel templateData, ad esempio
        templateData.put("NOME_FILE_ARRAY", listaNomeFile);


        
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
