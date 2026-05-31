
package net.projectsrl.qhse.moduli.core;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

import net.project.errors.AppCrash;
import net.projectsrl.pdf.CreateAcroFieldsPDF_base;
import net.projectsrl.random.RandomIdentifier;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.wera.base.db.AliModDAO_base;
import project.misc.Utils;

public abstract class CreateALIMODPdf_base extends CreateAcroFieldsPDF_base {

    public CreateALIMODPdf_base(String outDirectory, Map<String, Object> map) {

        super(outDirectory, map);

    }

    @Override
    protected void addAttachments(String fileName) throws AppCrash {

        try {

            String uploadDirectory = getTemplateDirectory().replace(WebAppConstants_itf.TEMPLATES_PATH,
                    WebAppConstants_itf.UPLOAD_PATH);
            String listaAllegati = null;

            if (getMap().get(AliModDAO_base.LISTA_ALLEGATI) == null) {
                return;
            }

            listaAllegati = (String) getMap().get(AliModDAO_base.LISTA_ALLEGATI);
            if (Utils.IsEmpty(listaAllegati)) {
                return;
            }

            
            String[] arrayListaAllegati = listaAllegati.split(";");
            boolean attachmentToPrint=false;
            for (int i = 0; i < arrayListaAllegati.length; i++) {
                if (arrayListaAllegati[i].toLowerCase().endsWith("png") || arrayListaAllegati[i].toLowerCase().endsWith("jpg")) {
                    attachmentToPrint=true;
                }

            }

            if (!attachmentToPrint) {
                return;
            }

            
            String randomFileName = RandomIdentifier.GetInstance().getIdentifier(15) + ".pdf";
            String tempFileName = fileName + "_";

            (new File(fileName)).renameTo(new File(tempFileName));

            float maxHeight = 350;
            float maxWidth = 520;


            String fileOut = getOutDirectory() + randomFileName;
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileOut));

            document.open();

            int pageCounter = 1;
            for (int i = 0; i < arrayListaAllegati.length; i++) {
                if (!arrayListaAllegati[i].toLowerCase().endsWith("png") && !arrayListaAllegati[i].toLowerCase().endsWith("jpg")) {
                    continue;
                }

                Image img = Image.getInstance(uploadDirectory + arrayListaAllegati[i]);

                if ((i & 1) == 0 && i > 0) {
                    document.newPage();
                    ++pageCounter;
                }

                img.scaleToFit(maxWidth, maxHeight);
                document.add(img);

            }

            document.close();

            PdfReader originalPDF = null;
            PdfReader attachmentsPDF = null;
            PdfStamper stamper = null;

            originalPDF = new PdfReader(tempFileName);
            attachmentsPDF = new PdfReader(fileOut);
            stamper = new PdfStamper(originalPDF, new FileOutputStream(fileName));

            for (int i = 1; i <= pageCounter; i++) {
                stamper.insertPage(getPageNumber() + i, attachmentsPDF.getPageSizeWithRotation(i));
                stamper.getOverContent(getPageNumber() + i).addTemplate(stamper.getImportedPage(attachmentsPDF, i), 0,
                        0);
            }

            stamper.close();
            originalPDF.close();
            attachmentsPDF.close();

            (new File(tempFileName)).delete();

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            throw ac;
        }
    }

}
