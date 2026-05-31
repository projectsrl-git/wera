
package net.projectsrl.dafne.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;

import org.apache.commons.codec.binary.Base64;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.webapp.security.WebAppUserSecurityInfo;
import project.misc.Utils;

public class FunctionCreaFirmaPDF extends FunctionProjectWebApp_base {


    public FunctionCreaFirmaPDF(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }


    private void test(SsbServletRequest req ) throws AppCrash {


        
        String logo=req.getField("FIRMA_PDF");
        
        
       /* String imageCode = logo;
        String base64Image = imageCode.split(",")[1];
        
        String imageString = imageCode.split(",")[1];
     // tokenize the data


     // create a buffered image
         BufferedImage image = new BufferedImage(350, 200, BufferedImage.TYPE_BYTE_GRAY);
         byte[] imageByte = null;
    
         BASE64Decoder decoder = new BASE64Decoder();
         try {
            imageByte = decoder.decodeBuffer(imageString);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
         ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
         try {
            image = ImageIO.read(bis);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
         try {
            bis.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    
         // write the image to a file
         File outputfile = new File( _applicationSrv.getRoot() + Config.GetInstance().getProperty("cartella.pdf")
                 + "FIRMA_" + Utils.getUnique() + ".jpeg");
         try {
            ImageIO.write(image, "jpeg", outputfile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            
             String firma=outputfile.toString();
  
        creaFilePdf(req, firma, userInfo);

        }*/
        
    
        String nomeFileCompleto = _applicationSrv.getRoot() + "/Output/"
                + "FIRMA_" + Utils.getUnique() + ".pdf";

       
        String nomeFileInput = _applicationSrv.getRoot() + "/Output/"
                + "/LIMOD16.pdf";

       

     // Create output PDF
        String b64Image = logo.split(",")[1];
        Document document = new Document(PageSize.A4.rotate());
        File file = new File(nomeFileCompleto);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PdfWriter writer = null;
        try {
            writer = PdfWriter.getInstance(document, outputStream);
        } catch (DocumentException e3) {
            // TODO Auto-generated catch block
            e3.printStackTrace();
        }
        document.open();
        PdfContentByte cb = writer.getDirectContent();

        // Load existing PDF
        PdfReader reader = null;
        try {
            reader = new PdfReader(nomeFileInput);
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        PdfImportedPage page = writer.getImportedPage(reader, 1); 

        // Copy first page of existing PDF into output PDF
        document.newPage();
        cb.addTemplate(page, 0, 0);

        // Add your new data / text here
        // for example...
        byte[] decoded = Base64.decodeBase64(b64Image.getBytes());
        Image image = null;
        try {
            image = Image.getInstance(decoded);
        } catch (BadElementException | IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        float scaler = 50;

        image.scalePercent(scaler);
        try {
           /* image.setAbsolutePosition(
                    (PageSize.POSTCARD.getWidth() - image.getScaledWidth()) / 2,
                    (PageSize.POSTCARD.getHeight() - image.getScaledHeight()) / 2);*/
            image.setAbsolutePosition(530, 15);
            document.add(image);
            
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        document.close();
        
        
        
        
        
       /* Document document = new Document();
        //
        String b64Image = logo.split(",")[1];
        String output = "d:/output.pdf";
        try {
            PdfWriter.getInstance(document, new FileOutputStream(output));
            document.open();
            byte[] decoded = Base64.decodeBase64(b64Image.getBytes());
            Image image = Image.getInstance(decoded);
            float scaler = 50;

            image.scalePercent(scaler);
            document.add(image);
            document.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }*/
     

    }


    @Override
    protected Map<String, Object> createMapFromRequest(SsbServletRequest req, UserSecurityInfo userInfo)
            throws AppCrash {

        Map<String, Object> map = new HashMap<String, Object>();

        setMapFromRequest(map, req);

        String includedHead = Config.GetInstance().getProperty("Page.Head.include");
        map.put(WebAppConstants_itf.INCLUDED_HEAD, includedHead);

        WebAppUserSecurityInfo<?> webAppUserInfo;
        // rilancio il ClassCastException
        try {
            webAppUserInfo = (WebAppUserSecurityInfo<?>) userInfo;
            Map<String, Object> sessionMap = webAppUserInfo.getSessionMap();

            map.putAll(sessionMap);
        } catch (Throwable e) {
            throw new AppCrash(e);
        }

        return map;
    }


    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        String directory = _applicationSrv.getRoot() + "/Output/";
        String filename = creaFilePdf(req, userInfo);

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

    private String creaFilePdf(SsbServletRequest req, UserSecurityInfo userInfo) throws AppCrash {

        String nomeFileCompleto = null;


        
        String logo=req.getField("FIRMA_PDF");
        
        
       /* String imageCode = logo;
        String base64Image = imageCode.split(",")[1];
        
        String imageString = imageCode.split(",")[1];
     // tokenize the data


     // create a buffered image
         BufferedImage image = new BufferedImage(350, 200, BufferedImage.TYPE_BYTE_GRAY);
         byte[] imageByte = null;
    
         BASE64Decoder decoder = new BASE64Decoder();
         try {
            imageByte = decoder.decodeBuffer(imageString);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
         ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
         try {
            image = ImageIO.read(bis);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
         try {
            bis.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    
         // write the image to a file
         File outputfile = new File( _applicationSrv.getRoot() + Config.GetInstance().getProperty("cartella.pdf")
                 + "FIRMA_" + Utils.getUnique() + ".jpeg");
         try {
            ImageIO.write(image, "jpeg", outputfile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            
             String firma=outputfile.toString();
  
        creaFilePdf(req, firma, userInfo);

        }*/
        
    
        nomeFileCompleto = _applicationSrv.getRoot() + "/Output/"
                + "FIRMA_" + Utils.getUnique() + ".pdf";

       
        String nomeFileInput = _applicationSrv.getRoot() + "/Output/"
                + "/LIMOD16.pdf";

       

     // Create output PDF
        String b64Image = logo.split(",")[1];
        Document document = new Document(PageSize.A4.rotate());
        File file = new File(nomeFileCompleto);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PdfWriter writer = null;
        try {
            writer = PdfWriter.getInstance(document, outputStream);
        } catch (DocumentException e3) {
            // TODO Auto-generated catch block
            e3.printStackTrace();
        }
        document.open();
        PdfContentByte cb = writer.getDirectContent();

        // Load existing PDF
        PdfReader reader = null;
        try {
            reader = new PdfReader(nomeFileInput);
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        PdfImportedPage page = writer.getImportedPage(reader, 1); 

        // Copy first page of existing PDF into output PDF
        document.newPage();
        cb.addTemplate(page, 0, 0);

        // Add your new data / text here
        // for example...
        byte[] decoded = Base64.decodeBase64(b64Image.getBytes());
        Image image = null;
        try {
            image = Image.getInstance(decoded);
        } catch (BadElementException | IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        float scaler = 50;

        image.scalePercent(scaler);
        try {
           /* image.setAbsolutePosition(
                    (PageSize.POSTCARD.getWidth() - image.getScaledWidth()) / 2,
                    (PageSize.POSTCARD.getHeight() - image.getScaledHeight()) / 2);*/
            image.setAbsolutePosition(530, 15);
            document.add(image);
            
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        document.close();
        

        return nomeFileCompleto;

    }

}
