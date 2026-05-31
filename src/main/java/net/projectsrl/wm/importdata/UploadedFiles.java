
package net.projectsrl.wm.importdata;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import net.project.errors.Logger;
import net.projectsrl.wm.utils.Utils;

public class UploadedFiles {

    private static HashMap<String, String> _uploadingStatus         = null;
    private static HashMap<String, String> _uploadingFileNames      = null;
    private static HashMap<String, String> _uploadingUsers          = null;
    private static HashMap<String, String> _uploadingDate           = null;
    private static HashMap<String, String> _uploadingTime           = null;
    private static HashMap<String, String> _uploadingDateLastModify = null;
    private static HashMap<String, String> _uploadingTimeLastModify = null;

    private static String                  _fileDirectory           = null;

    // file da caricare
    public final static String             FILE_CARICATO            	= "FILE_CARICATO";
    public final static String             PDFANAGRAFICHE          	= "PDFANAGRAFICHE";

    // possibili stati di caricamento
    public final static String             NO_FILE                  = "NO_FILE";
    public final static String             UPLOADING                = "UPLOADING";
    public final static String             UPLOAD_ERROR             = "UPLOAD_ERROR";
    public final static String             UPLOAD_COMPLETED         = "UPLOAD_COMPLETED";
    public final static String             UPDATING                 = "UPDATING";
    public final static String             UPDATE_COMPLETED         = "UPDATE_COMPLETED";
    public final static String             UPDATE_ERROR             = "UPDATE_ERROR";
    public final static String             UPDATE_CED_COMPLETED     = "UPDATE_CED_COMPLETED";
    public final static String             UPDATE_COMPLETED_CV      = "UPDATE_COMPLETED_CV";

    private final static String            MESSAGE_NO_FILE          = "NON ESISTE NESSUN FILE DISPONIBILE PER IL CARICAMENTO DEI DATI.<br/>PRIMA DI AVVIARE LA PROCEDURA DI CARICAMENTO E' NECESSARIO TRASFERIRE SUL SERVER UN FILE";
    private final static String            MESSAGE_UPLOADING        = "TRASFERIMENTO FILE IN CORSO";
    private final static String            MESSAGE_UPLOAD_ERROR     = "SI E' VERIFICATO UN ERRORE DURANTE IL TRASFERIMENTO DEL FILE";
    private final static String            MESSAGE_UPLOAD_COMPLETED = "FILE TRASFERITO E DISPONIBILE PER IL CARICAMENTO";
    private static final String            MESSAGE_UPDATING         = "AGGIORNAMENTO ARCHIVI IN CORSO";
    private static final String            MESSAGE_UPDATE_COMPLETED = "AGGIORNAMENTO ARCHIVI TERMINATO CORRETTAMENTE.<br>Per eseguire un nuovo caricamento è necessario selezionare e trasferire un nuovo file";
    private final static String            MESSAGE_UPDATE_ERROR     = "SI E' VERIFICATO UN ERRORE DURANTE L'AGGIORNAMENTO DEGLI ARCHIVI";
    private static final String            MESSAGE_UPDATE_CED_COMPLETED = "ULTIMA CREAZIONE FILES CEDOLINI TERMINATA CORRETTAMENTE.<br>Per eseguire un nuovo caricamento e' necessario selezionare e trasferire un nuovo file";
    private static final String            MESSAGE_UPDATE_COMPLETED_CV = "ULTIMA IMPORTAZIONE CURRICULUM TERMINATA CORRETTAMENTE.<br>Per eseguire un nuovo caricamento e' necessario selezionare e trasferire un nuovo file";

    public static final String             FILE_TYPE                = "FILE_TYPE";
    public static final String             FILE_STATUS              = "FILE_STATUS";
    public static final String             FILE_STATUS_MESSAGE      = "FILE_STATUS_MESSAGE";
    public static final String             FILE_NAME                = "FILE_NAME";
    public static final String             FILE_USER                = "FILE_USER";
    public static final String             FILE_DATE                = "FILE_DATE";
    public static final String             FILE_TIME                = "FILE_TIME";
    private static final String            FILE_DATE_LAST_MODIFY    = "FILE_DATE_LAST_MODIFY";
    private static final String            FILE_TIME_LAST_MODIFY    = "FILE_TIME_LAST_MODIFY";

    /**
     * Costruttore. La classe non e' istanziabile
     */
    private UploadedFiles() {

    }

    /**
     * Inizializza una configurazione in base al nome ed al file di configurazione.
     */
    static public synchronized void InitInstance() {

        _uploadingStatus = new HashMap<String, String>();
        _uploadingFileNames = new HashMap<String, String>();
        _uploadingUsers = new HashMap<String, String>();
        _uploadingDate = new HashMap<String, String>();
        _uploadingTime = new HashMap<String, String>();
        _uploadingDateLastModify = new HashMap<String, String>();
        _uploadingTimeLastModify = new HashMap<String, String>();
        _fileDirectory = "";
    }

    public static synchronized void setDirectory(String fileDirectory) {

        if (_uploadingStatus == null) {
            InitInstance();
        }
        _fileDirectory = fileDirectory;
    }

    public static synchronized String getDirectory() {

        return _fileDirectory;
    }

    public static synchronized void setStatus(String fileDescription, String status, String fileName, String user) {

        if (_uploadingStatus == null) {
            InitInstance();
        }
        _uploadingFileNames.put(fileDescription, fileName);
        _uploadingStatus.put(fileDescription, status);
        _uploadingUsers.put(fileDescription, user);
        _uploadingDate.put(fileDescription, Utils.getStringDataOggi());
        _uploadingTime.put(fileDescription, Utils.getStringOra());

        File file = new File(UploadedFiles.getDirectory() + "/" + fileName);
        Date dataUltimaModifica = new Date(file.lastModified());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", java.util.Locale.ITALY);
        String data = sdf.format(dataUltimaModifica);
        sdf = new SimpleDateFormat("HH:mm:ss", java.util.Locale.ITALY);
        String ora = sdf.format(dataUltimaModifica);

        _uploadingDateLastModify.put(fileDescription, data);
        _uploadingTimeLastModify.put(fileDescription, ora);
    }

    public static String getStatusMessage(String status) {

        if (status.equals(NO_FILE)) {
            return MESSAGE_NO_FILE;
        } else if (status.equals(UPLOADING)) {
            return MESSAGE_UPLOADING;
        } else if (status.equals(UPLOAD_COMPLETED)) {
            return MESSAGE_UPLOAD_COMPLETED;
        } else if (status.equals(UPDATING)) {
            return MESSAGE_UPDATING;
        } else if (status.equals(UPDATE_COMPLETED)) {
            return MESSAGE_UPDATE_COMPLETED;
        } else if (status.equals(UPLOAD_ERROR)) {
            return MESSAGE_UPLOAD_ERROR;
        } else if (status.equals(UPDATE_ERROR)) {
            return MESSAGE_UPDATE_ERROR;
        }else if (status.equals(UPDATE_CED_COMPLETED)) {
            return MESSAGE_UPDATE_CED_COMPLETED;
        }else if (status.equals(UPDATE_COMPLETED_CV)) {
            return MESSAGE_UPDATE_COMPLETED_CV;
        }
        return "";
    }

    public static synchronized String getStatus(String fileDescription) {

        if (_uploadingStatus == null) {
            InitInstance();
        }

        if (_uploadingStatus.get(fileDescription) == null) {
            _uploadingFileNames.put(fileDescription, "");
            _uploadingStatus.put(fileDescription, NO_FILE);
            _uploadingUsers.put(fileDescription, "");
            _uploadingDate.put(fileDescription, "");
            _uploadingTime.put(fileDescription, "");
            _uploadingDateLastModify.put(fileDescription, "");
            _uploadingTimeLastModify.put(fileDescription, "");

        }

        return _uploadingStatus.get(fileDescription);

    }

    public static synchronized String getFileName(String fileDescription) {

        getStatus(fileDescription);

        return _uploadingFileNames.get(fileDescription);

    }

    public static synchronized String getUser(String fileDescription) {

        getStatus(fileDescription);

        return _uploadingUsers.get(fileDescription);

    }

    public static synchronized String getDate(String fileDescription) {

        getStatus(fileDescription);

        return _uploadingDate.get(fileDescription);

    }

    public static synchronized String getTime(String fileDescription) {

        getStatus(fileDescription);

        return _uploadingTime.get(fileDescription);

    }

    public static synchronized String getDateLastModify(String fileDescription) {

        getStatus(fileDescription);

        return _uploadingDateLastModify.get(fileDescription);

    }

    public static synchronized String getTimeLastModify(String fileDescription) {

        getStatus(fileDescription);

        return _uploadingTimeLastModify.get(fileDescription);

    }

    public static synchronized void logUploadingStatus() {

        String logString = "";

        logString += "==============================================================\n";

        Logger.GetInstance().log0(logString);

    }

    public static synchronized void setTemplateData(HashMap<String, Object> templateData, String fileType) {

        templateData.put(UploadedFiles.FILE_TYPE, fileType);
        templateData.put(UploadedFiles.FILE_NAME, getFileName(fileType));
        templateData.put(UploadedFiles.FILE_STATUS, getStatus(fileType));
        templateData.put(UploadedFiles.FILE_STATUS_MESSAGE, getStatusMessage(getStatus(fileType)));
        templateData.put(UploadedFiles.FILE_USER, getUser(fileType));
        templateData.put(UploadedFiles.FILE_DATE, getDate(fileType));
        templateData.put(UploadedFiles.FILE_TIME, getTime(fileType));
        templateData.put(UploadedFiles.FILE_DATE_LAST_MODIFY, getDateLastModify(fileType));
        templateData.put(UploadedFiles.FILE_TIME_LAST_MODIFY, getTimeLastModify(fileType));
    }

    public static void setStatus(String fileDescription, String status) {

        if (_uploadingStatus == null) {
            InitInstance();
        }
        _uploadingStatus.put(fileDescription, status);
        _uploadingDate.put(fileDescription, Utils.getStringDataOggi());
        _uploadingTime.put(fileDescription, Utils.getStringOra());

    }

}
