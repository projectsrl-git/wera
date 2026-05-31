
package net.projectsrl.wera.anagrafiche.stampe.core;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.projectsrl.alibow.core.Constants_itf;
import net.projectsrl.qhse.moduli.core.CreateALIMODPdf_base;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.webapp.security.WebAppUserSecurityInfo;
import net.projectsrl.wera.ripartizioni.db.RipartizioniDettaglioDAO;

public class CreateAnagraficaUtenzePdf extends CreateALIMODPdf_base {

    public CreateAnagraficaUtenzePdf(String outDirectory, Map<String, Object> map) throws Exception {

        super(outDirectory, map);
        
        
        
        String idModulo= map.get("ID_MODULO").toString();
        String[] dati = new String[setDimension(idModulo)+1];
        Integer numero=1;
        DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        
        String alias=map.get("ALIAS_AZIENDA").toString();
        if (alias==null || alias.isEmpty()){
        	alias="";
        }else{
        	alias="-"+alias;
        }
        
        dati[0] = "ANAGRAFICA-UTENZE"+alias+".pdf";
        
        
        
        
        try {
            dataSet = dsFactory.makeDataSet("", "DSUtenzeDettaglio");

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("WHERECONDITION", " WHERE UT.ID_MODULO =" + idModulo);
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	if (numero%27==0){
            		creazionePagineConNomiCampi(numero, alias);
            		dati[numero%27]="ANAGRAFICA-UTENZE-"+numero%27+".pdf";
            	}
            	
            	
                numero= numero+1;
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),
                    "Errore nella ricerca dell'ultimo progressivo del dataset " + "DSDatiRipartizioni");
            try {
				throw ac;
			} catch (AppCrash e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DSDatiRipartizioni");
                }
            }
        }
        
        setFixedNumberOfPages(numero%27);
        setFixedPagesTemplateArray(dati);
        
        //setFixedNumberOfPages(3);
        //setFixedPagesTemplateArray(new String[] { "RIPARTIZIONI-TESTATA.pdf", "RIPARTIZIONI-DETTAGLI.pdf", "RIPARTIZIONI-DETTAGLI.pdf" });
        

    }


	private void creazionePagineConNomiCampi(Integer numero, String alias) throws DocumentException {
		try {
        String src=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"ANAGRAFICA-UTENZE"+alias+".pdf";
        String dest=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"ANAGRAFICA-UTENZE-"+numero+".pdf";
        String numeroNuovo=numero.toString();
        PdfReader reader;
		reader = new PdfReader(src);
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        AcroFields form = stamper.getAcroFields();
        
        form.renameField("DATA_DI_LETTURA_1", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_2", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_3", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_4", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_5", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_6", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_7", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_8", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_9", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_10", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_11", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_12", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_13", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_14", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_15", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_16", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_17", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_18", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_19", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_20", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_21", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_22", "DATA_DI_LETTURA_"+numeroNuovo);
        form.renameField("DATA_DI_LETTURA_23", "DATA_DI_LETTURA_"+numeroNuovo);
        
        
        stamper.close();
        reader.close();
        reader = new PdfReader(dest);
        } catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
    
    
    @Override
    protected void putIndexedFieldInMap(Map<String, Object> map, int i, String fieldName, Object fieldValue) {

        if (RipartizioniDettaglioDAO.DENOMINAZIONE.equals(fieldName)) {
            map.put(fieldValue + "." + i, Constants_itf.ACROBAT_CHECKED);
        } else {
            super.putIndexedFieldInMap(map, i, fieldName, fieldValue);
        }
    }
    

    private int setDimension(String idModulo) {
    	 DataSet_itf dataSet = null;
         DataSetFactory dsFactory = DataSetFactory.getInstance();
         Integer dimensione=0;

         try {
             dataSet = dsFactory.makeDataSet("", "DSUtenzeDettaglioStampaNumeroRighe");

             HashMap<String, String> param = new HashMap<String, String>();
             param.put("WHERECONDITION", " WHERE UT.ID_MODULO =" + idModulo);
             dataSet.setParam(param);
             dataSet.open();

             while (dataSet.hasMoreElements()) {
             	Row_itf dbRow = (Row_itf) dataSet.nextElement();
             	dimensione=(Integer) dbRow.getField("NUMERO")%27;
             }
         } catch (AppCrash ac) {
             ac.logContext(this.getClass().getName(),"Errore nella ricerca dell'ultimo progressivo del dataset " + "DSDatiRipartizioni");
         } finally {
             // chiude il dataset per il conteggio degli elementi trovati
             if (dataSet != null) {
                 try {
                     dataSet.close();
                 } catch (Throwable t) {
                     AppCrash ac = new AppCrash(t);
                     ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DSDatiRipartizioni");
                 }
             }
         }
		return dimensione;
	}

	@Override
    protected String setPDFOutFileName(Map<String, Object> map) {

        String nomeFileOutput;
        String condominio = (String) map.get("CONDOMINIO");
        String condomino = (String) map.get("DENOMINAZIONE");
        nomeFileOutput = "Anagrafica utenza - " + condominio.replace(" ", "_") + condomino.replace(" ", "_") + ".pdf";
        
        return nomeFileOutput;
    }

    @Override
    protected void setAcroField(Map<String, Object> map, String acroFieldName) {

         super.setAcroField(map, acroFieldName);
    }
}
