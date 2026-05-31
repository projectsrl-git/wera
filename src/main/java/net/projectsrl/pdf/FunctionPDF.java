
package net.projectsrl.pdf;

import java.io.IOException;
import java.io.OutputStream;

import net.project.errors.AppCrash;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

public class FunctionPDF {

    /**
     * 
     * 
     * 
     * @param percorsoPrestampato Il percorso del prestampato
     * 
     * @param out Lo stream dove verrà inviato il pdf finito
     * 
     * @throws IOException
     * 
     * @throws DocumentException
     * @throws AppCrash
     */

    public void riempiAcroPDF(String percorsoPrestampato, OutputStream out, String numOrdine, String tipologia)
            throws IOException, DocumentException, AppCrash

    {

        // Leggo il prestampato

        PdfReader reader = new PdfReader(percorsoPrestampato);

        // Lo stamper che scriverà il nuovo pdf riempito

        PdfStamper stamper = new PdfStamper(reader, out);

        // Con questo metodo mi riprendo i campi del FORM presente nel pdf, quelli che, in alcuni casi, si possono
        // riempire anche con Acrobat Reader

        //AcroFields form = stamper.getAcroFields();

        // Ho considerato un esempio di riempimento di alcuni dati di una fattura

        // Il nome del campo assegnato al PDF è il primo parametro, il secondo è il valore che vedremo scritto dopo
        // l'operazione.

        // form.setField("NUM_ORDINE", numOrdine);

        // Mi riprendo il contenuto del livello sopra quello di default

        // PdfContentByte cb = stamper.getOverContent(1);

        // Genero un barcode con questo metodo che sotto commenterò

        // Image img = getBarCodeImage("010002541258974521", cb);
        // Image img = getBarCodeImage(numOrdine, cb);

        // Setto la posizione

        // img.setAbsolutePosition(503, 620);
        // img.setAbsolutePosition(435, 29);

        // Lo giro di 90 gradi

        // img.setRotation((float) Math.PI / 2);

        // lo aggiungo al livello

        // cb.addImage(img);

        // Questo metodo è molto importante perchè serve a chiudere il form in modo che non sia modificabile dall'utente

        stamper.setFormFlattening(true);

        // Questo invece finalizza tutto il pdf

        stamper.close();

    }

    /**
     * 
     * Metodo che genera un BARCODE
     * 
     * @param testo Il testo che dovrà essere trasformato in BARCODE
     * 
     * @param cb il contenuto dove scrivere il barcode
     * 
     * @return
     */

    public Image getBarCodeImage(String testo, PdfContentByte cb)

    {

        // La classe che mi serve per generare il BARCODE

        Barcode128 code128 = new Barcode128();

        // Setto il testo che dovrà essere scritto

        code128.setCode(testo);

        // genero e ritorno l'immagine

        return code128.createImageWithBarcode(cb, null, null);

    }

}
