
package net.projectsrl.alibow.core;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;

import net.project.errors.AppCrash;
import net.project.misc.Base64;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;

public class FunctionCreaFirma extends FunctionProjectWebApp_base {

    public FunctionCreaFirma(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    private String creaFirma(String signFromPage) throws AppCrash {

        String imageCode = signFromPage;

        String imageString = imageCode.split(",")[1];
        // tokenize the data

        // create a buffered image
        BufferedImage image = new BufferedImage(350, 200, BufferedImage.TYPE_BYTE_GRAY);
        byte[] imageByte = null;
        
        File outputfile = null;

        try {
            imageByte = Base64.decode(imageString);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            image = ImageIO.read(bis);
            bis.close();

            // write the image to a file
            outputfile = new File(_applicationSrv.getRoot() + "/Output/firma.png");
            ImageIO.write(image, "png", outputfile);
        } catch (Throwable e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return outputfile.toString();

    }
    

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        String directory = _applicationSrv.getRoot() + "/Output/";
        String signFromPage = req.getField("FIRMA_PDF");
        String filename = creaFirma(signFromPage);

        File file = new File(filename);
        String contentType = getContentType(filename);
        System.out.println(contentType);
        res.setContentType(contentType);
        res.setHeader("Content-Disposition", "attachment; filename=" + filename.replace(directory, ""));
        int length = (int) file.length();

        if (length > Integer.MAX_VALUE) {
        }

        byte[] bytes = new byte[length];

        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);

            fin.read(bytes);

            ServletOutputStream os = res.getOutputStream();
            os.write(bytes);
            os.flush();
        } catch (Throwable ac) {
            new AppCrash(ac);
        }

    }

    String getContentType(String fileName) {

        String extension[] = { // File Extensions
                "txt", // 0 - plain text
                "htm", // 1 - hypertext
                "jpg", // 2 - JPEG image
                "png", // 2 - JPEG image
                "gif", // 3 - gif image
                "pdf", // 4 - adobe pdf
                "doc", // 5 - Microsoft Word
                "docx", }; // you can add more
        String mimeType[] = { // mime types
                "text/plain", // 0 - plain text
                "text/html", // 1 - hypertext
                "image/jpg", // 2 - image
                "image/jpg", // 2 - image
                "image/gif", // 3 - image
                "application/pdf", // 4 - Adobe pdf
                "application/msword", // 5 - Microsoft Word
                "application/msword", // 5 - Microsoft Word
        }, // you can add more
                contentType = "text/html"; // default type
        // dot + file extension
        int dotPosition = fileName.lastIndexOf('.');
        // get file extension
        String fileExtension = fileName.substring(dotPosition + 1);
        // match mime type to extension
        for (int index = 0; index < mimeType.length; index++) {
            if (fileExtension.equalsIgnoreCase(extension[index])) {
                contentType = mimeType[index];
                break;
            }
        }
        return contentType;
    }

    private String creaFilePdf(SsbServletRequest req) throws AppCrash {

        String nomeFileCompleto = null;

        String logo = req.getField("FIRMA_PDF");

        /*
         * String imageCode = logo; String base64Image = imageCode.split(",")[1];
         * 
         * String imageString = imageCode.split(",")[1]; // tokenize the data
         * 
         * 
         * // create a buffered image BufferedImage image = new BufferedImage(350, 200, BufferedImage.TYPE_BYTE_GRAY);
         * byte[] imageByte = null;
         * 
         * BASE64Decoder decoder = new BASE64Decoder(); try { imageByte = decoder.decodeBuffer(imageString); } catch
         * (IOException e1) { // TODO Auto-generated catch block e1.printStackTrace(); } ByteArrayInputStream bis = new
         * ByteArrayInputStream(imageByte); try { image = ImageIO.read(bis); } catch (IOException e1) { // TODO
         * Auto-generated catch block e1.printStackTrace(); } try { bis.close(); } catch (IOException e1) { // TODO
         * Auto-generated catch block e1.printStackTrace(); }
         * 
         * // write the image to a file File outputfile = new File( _applicationSrv.getRoot() +
         * Config.GetInstance().getProperty("cartella.pdf") + "FIRMA_" + Utils.getUnique() + ".jpeg"); try {
         * ImageIO.write(image, "jpeg", outputfile); } catch (IOException e) { // TODO Auto-generated catch block
         * e.printStackTrace();
         * 
         * String firma=outputfile.toString();
         * 
         * creaFilePdf(req, firma, userInfo);
         * 
         * }
         */

        return nomeFileCompleto;

    }

}
