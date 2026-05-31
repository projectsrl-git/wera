
package net.projectsrl.wera.storico.core;

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
import net.projectsrl.wera.ripartizioni.db.RipartizioniDettaglioDAO;
import net.projectsrl.wm.utils.Utils;

public class CreateLettureConsumiStoricoPdf extends CreateALIMODPdf_base {

    public CreateLettureConsumiStoricoPdf(String outDirectory, Map<String, Object> map) throws Exception {

        super(outDirectory, map);
        
        
        String idCondominio = map.get("ID_CONDOMINIO").toString();
        String dataInizioStatistica = Utils.ribaltaData(map.get("DATA_DI_LETTURA").toString());
        String[] dati = new String[setDimension(idCondominio,dataInizioStatistica)+1];
        Integer numero=1;
        DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        
        String alias=map.get("ALIAS_AZIENDA").toString();
        if (alias==null || alias.isEmpty()){
        	alias="";
        }else{
        	alias="-"+alias;
        }
        dati[0] = "LETTURE_CONSUMI"+alias+".pdf";
        Integer y=0;
        
        
        try {
            dataSet = dsFactory.makeDataSet("", "DSLettureConsumiCondominiDettaglioStoricoStampa");

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("WHERECONDITION", " WHERE SCARICO.ID_CONDOMINIO =" + idCondominio+" and data_di_lettura='"+dataInizioStatistica+"'");
            param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
            param.put("WHERECONDITION_PROFILI_LETTURE", " IS NOT NULL ");
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	
            	if (numero==1){
            		creazionePagineConNomiCampi(numero,y, alias);
                }
            	
            	if (numero%40==0){
            		y=y+1;
            		creazionePagineConNomiCampi(numero,y, alias);
            		dati[y]="LETTURE_CONSUMI-"+y+".pdf";
            	}
            	
            	
            	
                numero= numero+1;
                
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),
                    "Errore nella ricerca dell'ultimo progressivo del dataset " + "DSLettureConsumiCondominiDettaglioStoricoStampa");
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
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DSLettureConsumiCondominiDettaglioStoricoStampa");
                }
            }
        }
        
        setFixedNumberOfPages(setDimension(idCondominio,dataInizioStatistica)+1);
        setFixedPagesTemplateArray(dati);
        
        //setFixedNumberOfPages(3);
        //setFixedPagesTemplateArray(new String[] { "RIPARTIZIONI-TESTATA.pdf", "RIPARTIZIONI-DETTAGLI.pdf", "RIPARTIZIONI-DETTAGLI.pdf" });
        

    }
    


	private void creazionePagineConNomiCampi(Integer numero, Integer y, String alias) throws DocumentException {
		try {
        String src=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"LETTURE_CONSUMI"+alias+".pdf";
        String dest=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"LETTURE_CONSUMI-"+y+".pdf";
        Integer numeroNuovo=numero;
        PdfReader reader;
		reader = new PdfReader(src);
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        AcroFields form = stamper.getAcroFields();
        
        	for (int i=1;i<=40;i++){
	        	form.renameField("DATA_DI_LETTURA_"+i, "DATA_DI_LETTURA_"+(i+numeroNuovo));
	        	form.renameField("CONDOMINO_"+i, "CONDOMINO_"+(i+numeroNuovo));
	            form.renameField("LETTURA_ATTUALE_"+i, "LETTURA_ATTUALE_"+(i+numeroNuovo));
	            form.renameField("LETTURA_ATTUALE_ACS_"+i, "LETTURA_ATTUALE_ACS_"+(i+numeroNuovo));
	            form.renameField("LETTURA_ATTUALE_AFS_"+i, "LETTURA_ATTUALE_AFS_"+(i+numeroNuovo));
	            form.renameField("NUM_RILEVATORI_"+i, "NUM_RILEVATORI_"+(i+numeroNuovo));
	            form.renameField("VOLUME_ATTUALE_"+i, "VOLUME_ATTUALE_"+(i+numeroNuovo));
	            form.renameField("NUM_CONTATORI_"+i, "NUM_CONTATORI_"+(i+numeroNuovo));
	            
        	}
        
        form.setField("NR_PAG",String.valueOf(y+1));
        	
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
    

    private int setDimension(String idCondominio, String dataInizioStatistica) {
    	 DataSet_itf dataSet = null;
         DataSetFactory dsFactory = DataSetFactory.getInstance();
         Integer dimensione=0;

         try {
             //dataSet = dsFactory.makeDataSet("", "DSLettureConsumiStampaNumeroRigheStorico");
        	 dataSet = dsFactory.makeDataSet("","DSLettureConsumiCondominiDettaglioStoricoStampa");

             HashMap<String, String> param = new HashMap<String, String>();
             param.put("WHERECONDITION", " WHERE SCARICO.ID_CONDOMINIO =" + idCondominio+" and data_di_lettura='"+dataInizioStatistica+"'");
             param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
             param.put("WHERECONDITION_PROFILI_LETTURE", " IS NOT NULL ");
             dataSet.setParam(param);
             dataSet.open();

             while (dataSet.hasMoreElements()) {
             	Row_itf dbRow = (Row_itf) dataSet.nextElement();
             	dimensione=(Integer) dbRow.getField("NUMERO")/40;
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
        //String nrModulo = (String) map.get("NR_MODULO");
        String condominio = (String) map.get("DENOMINAZIONE");
        nomeFileOutput = "Lettura consumi-" + condominio.replace(" ", "_") + ".pdf";
        
        return nomeFileOutput;
    }

    @Override
    protected void setAcroField(Map<String, Object> map, String acroFieldName) {

         super.setAcroField(map, acroFieldName);
    }
}
