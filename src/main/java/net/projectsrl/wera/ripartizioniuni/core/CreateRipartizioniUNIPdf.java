
package net.projectsrl.wera.ripartizioniuni.core;

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
import net.projectsrl.wera.ripartizioniuni.db.RipartizioniUNIDettagliDAO;

public class CreateRipartizioniUNIPdf extends CreateALIMODPdf_base {

    public CreateRipartizioniUNIPdf(String outDirectory, Map<String, Object> map) throws Exception {

        super(outDirectory, map);
        
        String alias=map.get("ALIAS_AZIENDA").toString();
        if (alias==null || alias.isEmpty()){
        	alias="";
        }else{
        	alias="-"+alias;
        }
        
        String idModulo = map.get("ID_MODULO").toString();
        String[] dati = new String[setDimensionUltimePagine(idModulo)+1];
        Integer numero=1;
        DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        dati[0] = "RIPARTIZIONI-UNI-TESTATA"+alias+".pdf";
        Integer ultimePagine=1;
        Integer inizioTotali=0;
        
        try {
            dataSet = dsFactory.makeDataSet("", "DSRipartizioniUNIDettaglioStampa");

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("WHERECONDITION", " WHERE RI.ID_MODULO=" + idModulo);
            param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
            dataSet.setParam(param);
            dataSet.open();
            
            
            inizioTotali=numero;
            numero=1;
        	while (dataSet.hasMoreElements()) {
        		Row_itf dbRow = (Row_itf) dataSet.nextElement();
        		
            	if (numero%27==0){
            		creazioneUltimePagine(ultimePagine,alias);
            		dati[inizioTotali]="RIPARTIZIONI-UNI-ULTIMA-PAGINA-"+ultimePagine+".pdf";
            		numero= numero+1;
            		ultimePagine=ultimePagine+1;
            		inizioTotali=inizioTotali+1;
            	}
            	
            	numero= numero+1;
            }
        	
        	creazioneUltimePagine(ultimePagine,alias);
            dati[inizioTotali]="RIPARTIZIONI-UNI-ULTIMA-PAGINA-"+ultimePagine+".pdf";
                      
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
        

        setFixedNumberOfPages(setDimensionUltimePagine(idModulo)+1);
        setFixedPagesTemplateArray(dati);
    }

    
    @Override
    protected void putIndexedFieldInMap(Map<String, Object> map, int i, String fieldName, Object fieldValue) {

        if (RipartizioniUNIDettagliDAO.DENOMINAZIONE.equals(fieldName)) {
            map.put(fieldValue + "." + i, Constants_itf.ACROBAT_CHECKED);
        } else {
            super.putIndexedFieldInMap(map, i, fieldName, fieldValue);
        }
    }
    
    
    private int setDimensionUltimePagine(String idModulo) {
   	 DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        Integer dimensione=0;

        try {
            dataSet = dsFactory.makeDataSet("", "DSRipartizioniUNIDettaglioStampaNumeroRighe");

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("WHERECONDITION", " WHERE RI.ID_MODULO=" + idModulo);
            param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	dimensione=(Integer) dbRow.getField("NUMERO_ULTIME");
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
    
    
    
	private void creazioneUltimePagine(Integer numero, String alias) throws DocumentException {
		try {
        String src=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RIPARTIZIONI-UNI-ULTIMA-PAGINA"+alias+".pdf";
        String dest=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RIPARTIZIONI-UNI-ULTIMA-PAGINA-"+numero+".pdf";
        Integer numeroNuovo=numero%27;
        PdfReader reader;
		reader = new PdfReader(src);
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        AcroFields form = stamper.getAcroFields();
        
        if (numeroNuovo>1){
        	for (int i=1;i<27;i++){
	        	form.renameField("DENOMINAZIONE_"+i, "DENOMINAZIONE_"+(i+(26*(numeroNuovo-1))));
	        	form.renameField("NUMERO_RIPARTITORI_"+i, "NUMERO_RIPARTITORI_"+(i+(26*(numeroNuovo-1))));
	            form.renameField("MILLESIMI_CLIMA_"+i, "MILLESIMI_CLIMA_"+(i+(26*(numeroNuovo-1))));
	            form.renameField("MILLESIMI_ACS_"+i, "MILLESIMI_ACS"+(i+(26*(numeroNuovo-1))));
	            form.renameField("LETTURA_"+i, "LETTURA_"+(i+(26*(numeroNuovo-1))));
	            form.renameField("LETTURA_ACS_"+i, "LETTURA_ACS_"+(i+(26*(numeroNuovo-1))));
	            form.renameField("SPESA_ENERGIA_TERMICA_RISCALDAMENTO_"+i, "SPESA_ENERGIA_TERMICA_RISCALDAMENTO_"+(i+(26*(numeroNuovo-1))));
	            form.renameField("SPESA_POTENZA_TERMICA_RISCALDAMENTO_"+i, "SPESA_POTENZA_TERMICA_RISCALDAMENTO_"+(i+(26*(numeroNuovo-1))));
	            form.renameField("SPESA_TOTALE_RISCALDAMENTO_"+i, "SPESA_TOTALE_RISCALDAMENTO_"+(i+(26*(numeroNuovo-1))));
	            form.renameField("SPESA_ENERGIA_TERMICA_ACS_"+i, "SPESA_ENERGIA_TERMICA_ACS_"+(i+(26*(numeroNuovo-1))));
	            form.renameField("SPESA_POTENZA_TERMICA_ACS_"+i, "SPESA_POTENZA_TERMICA_ACS_"+(i+(26*(numeroNuovo-1))));
	            form.renameField("SPESA_TOTALE_ACS_"+i, "SPESA_TOTALE_ACS_"+(i+(26*(numeroNuovo-1))));
	            form.renameField("SPESA_TOTALE_APPARTAMENTO_"+i, "SPESA_TOTALE_APPARTAMENTO_"+(i+(26*(numeroNuovo-1))));

        	}
        }
        
       
      
        stamper.close();
        reader.close();
        reader = new PdfReader(dest);
        } catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
		@Override
    protected String setPDFOutFileName(Map<String, Object> map) {

        String nomeFileOutput;
        String nrModulo = (String) map.get("NR_MODULO");
        String condominio = (String) map.get("DENOMINAZIONE_CONDOMINIO");
        //nomeFileOutput = "Ripartizione-" + nrModulo + "-" + condominio.replace(" ", "_") + ".pdf";
        String dataDal = (String) map.get("DATA_DAL");
        String dataAl = (String) map.get("DATA_AL");
        dataDal=dataDal.replace("/", "-");
        dataAl=dataAl.replace("/", "-");
        nomeFileOutput = condominio.replace(" ", "_") +"-dal-"+dataDal+"-al-"+dataAl+"-Ripartizione-nr-" + nrModulo+".pdf";
        
        return nomeFileOutput;
    }

    @Override
    protected void setAcroField(Map<String, Object> map, String acroFieldName) {

         super.setAcroField(map, acroFieldName);
    }
}
