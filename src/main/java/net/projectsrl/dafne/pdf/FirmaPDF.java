
package net.projectsrl.dafne.pdf;

import java.io.FileOutputStream;

import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import net.project.errors.AppCrash;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.pdf.FunctionPDF;

public class FirmaPDF extends FunctionPDF {

   

    public void createPDF(int numTimes, String numOrdine, String nomeFileCompleto, String nomeFileInput, String firma,
            SsbServletRequest req, UserSecurityInfo userInfo) throws AppCrash {


        try {
            FileOutputStream out = new FileOutputStream(nomeFileCompleto);
            PdfReader reader = new PdfReader(nomeFileInput);
            PdfStamper stamper = new PdfStamper(reader, out);
            
            PdfContentByte cb = stamper.getOverContent(reader.getNumberOfPages());
            
            
            
            
            Image eye = Image.getInstance(firma);
            eye.setAbsolutePosition(100,100);
            cb.addImage(eye, true);
            //cb.addImage(eye, 0, 0, 0, 0, 200, 250, true);
            //Adds an Image to the page. The positioning of the Image is done with the transformation matrix.
            //To position an image at (x,y) use addImage(image, image_width, 0, 0, image_height, x, y,
            // inlineImage).
            // The image can be placed inline.

            

            stamper.setFormFlattening(true);

            stamper.close();
            reader.close();

        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            throw ac;
        }
    }

}
