
package it.project.iride.importdata;

import it.project.iride.core.FunctionIride_base;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;

/**
 * FunctionFileUpload
 * 
 */
public abstract class FunctionFileUpload_base extends FunctionIride_base {

    private static final String PAGE        = "file_upload";
    private static final String PAGE_CHIUDI = "file_upload_chiudi";
    private static final int    _sizeMax    = 100000000;

    public FunctionFileUpload_base() {

        super();
    }

    public FunctionFileUpload_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        HashMap<String, Object> templateData = setCommonTags(req, userInfo);
        templateData = setTemplateDataFromRequest(setCommonTags(req, userInfo), req);

        templateData.put("MESSAGGIO_ATTESA", "Trasferimento file in corso...");

        _applicationSrv.displayPage(PAGE, templateData, res);
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        HashMap<String, Object> templateData = setCommonTags(req, userInfo);
        templateData = setTemplateDataFromRequest(templateData, req);

        String absolutePathFileName = uploadFiles(req);

        loadData(getSessionUser(req), absolutePathFileName, req);

        _applicationSrv.displayPage(getPaginaChiusuraPopup(), templateData, res);

    }

    protected String getPaginaChiusuraPopup() {

        return PAGE_CHIUDI;
    }

    protected abstract void loadData(String sessionUser, String absolutePathFileName, SsbServletRequest req)
            throws AppCrash;

    @SuppressWarnings("deprecation")
    private String uploadFiles(SsbServletRequest req) {

        String fileName = "";
        String uploadedFileName = "";
        DiskFileUpload fu = new DiskFileUpload();
        // If file size exceeds, a FileUploadException will be thrown
        fu.setSizeMax(_sizeMax);
        try {
            List<FileItem> fileItems = fu.parseRequest(req);
            Iterator<FileItem> itr = fileItems.iterator();

            // ciclo per i file
            while (itr.hasNext()) {
                FileItem fi = itr.next();

                // Check if not form field so as to only handle the file inputs
                // else condition handles the submit button input
                if (!fi.isFormField()) {
                    fileName = fi.getName();

                    int positionOfLastSlash = fileName.lastIndexOf("\\");
                    fileName = fileName.substring(positionOfLastSlash + 1);

                    String directory = _applicationSrv.getRoot() + "Import";
                    File fNew = new File(directory, fileName);
                    fi.write(fNew);
                    uploadedFileName = fNew.getAbsolutePath();

                }
            }

            req.getSession(false).setAttribute("FILE_UPLOADED", uploadedFileName);

            return uploadedFileName;
        } catch (Throwable e) {
            new AppCrash(e);
            return null;
        }

    }
}
