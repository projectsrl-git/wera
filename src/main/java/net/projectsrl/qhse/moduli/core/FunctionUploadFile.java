
package net.projectsrl.qhse.moduli.core;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.alibow.core.Constants_itf;
import net.projectsrl.webapp.core.FunctionNoAuthentication;
import net.projectsrl.wera.base.db.AliModDAO_base;

public class FunctionUploadFile extends FunctionNoAuthentication {

    private static final int _sizeMax = 100000000;

    public FunctionUploadFile(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        String fileName = "";
        String uploadedFileName = "";
        String idDocumentazione = "";
        String normalizedFileName = "";
        String modulo = "";
        String idModulo = "";
        String listaAllegati="";
        boolean result = false;
        AliModDAO_base dao=null;

        DiskFileUpload fu = new DiskFileUpload();
        // If file size exceeds, a FileUploadException will be thrown
        fu.setSizeMax(_sizeMax);
        try {

            List<FileItem> fileItems = fu.parseRequest(req);


            // ciclo per i parameters
            for (FileItem fi : fileItems) {

                // Check if not form field so as to only handle the file inputs
                // else condition handles the submit button input
                if (!fi.isFormField()) {
                    continue;
                }

                if (fi.getFieldName().equals("MODULO")) {
                    modulo = fi.getString();
                }
                
                if (fi.getFieldName().equals("ID_MODULO")) {
                    idModulo = fi.getString();
                }                

            }

            // ciclo per i file
            for (FileItem fi : fileItems) {

                // Check if not form field so as to only handle the file inputs
                // else condition handles the submit button input
                if (fi.isFormField()) {
                    continue;
                }

                fileName = fi.getName();

                int positionOfLastSlash = fileName.lastIndexOf(File.separator);
                fileName = fileName.substring(positionOfLastSlash + 1);

                String directory = _applicationSrv.getRoot() + "upload" + File.separator + idDocumentazione;

                File dir = new File(directory);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                
                if (modulo.equals(Constants_itf.MODULO_ALIMOD80)) {
                    
                } else if (modulo.equals(Constants_itf.MODULO_ALIMOD67)) {
                    
                } else if (modulo.equals(Constants_itf.MODULO_ALIMOD50)) {
                    
                } else if (modulo.equals(Constants_itf.MODULO_ALIMOD20)) {
                    
                } else if (modulo.equals(Constants_itf.MODULO_ALIMOD05)) {
                    
                } else if (modulo.equals(Constants_itf.MODULO_LIMOD13)) {
                    
                } else if (modulo.equals(Constants_itf.MODULO_LIMOD53)) {
                	
                } 
     
                //normalizedFileName = Normalizer.normalize(fileName, Normalizer.Form.NFD);
                //normalizedFileName = normalizedFileName.replaceAll("[^\\p{ASCII}]", "");
                //normalizedFileName = normalizedFileName.replaceAll("[^a-zA-Z0-9.-]", "_");

                File fNew = new File(directory, fileName);
                fi.write(fNew);
                
                dao.setAttribute(AliModDAO_base.ID_MODULO, idModulo);
                dao.retrieve();
                
                if (dao.getAttribute(AliModDAO_base.LISTA_ALLEGATI)!=null) {
                    listaAllegati = (String) dao.getAttribute(AliModDAO_base.LISTA_ALLEGATI);
                }
                
                dao.setAttribute(AliModDAO_base.ID_MODULO, idModulo);
                dao.setAttribute(AliModDAO_base.LISTA_ALLEGATI, listaAllegati+(listaAllegati.equals("")?"":";")+fileName);
                dao.update();
                
                uploadedFileName = fNew.getAbsolutePath();

            }

            result = true;

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(),
                    "error uploading fileName:" + fileName + " - uploadedFileName:" + uploadedFileName);
        } finally {
            sendResponseJSON(res, normalizedFileName, "upload/" + idDocumentazione + "/" + normalizedFileName, result);

        }

    }

    /**
     * Questo metodo
     * 
     * @param res
     * @param fileName
     * @param formDao
     */
    private void sendResponseJSON(SsbServletResponse res, String fileName, String fileUrl, boolean result) {

        try {
            res.setContentType("application/json");
            PrintWriter out = res.getWriter();
            String resultString = "{\"result\":" + result + "}";
            out.println(resultString);
            out.close();

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "errore writing succesful response");
        }
    }

}
