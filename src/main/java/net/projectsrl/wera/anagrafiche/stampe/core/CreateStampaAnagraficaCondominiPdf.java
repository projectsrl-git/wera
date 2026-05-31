
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
import net.projectsrl.wera.anagrafiche.db.CondominiDAO;


public class CreateStampaAnagraficaCondominiPdf extends CreateALIMODPdf_base {

    public CreateStampaAnagraficaCondominiPdf(String outDirectory, Map<String, Object> map) throws Exception {

        super(outDirectory, map);
        
        
        String idModulo = map.get("ID_CONDOMINIO").toString();
        //String[] dati = new String[setDimensionUltimePagine(idModulo)+1];
        String[] dati = new String[setDimension(idModulo)+setDimensionUltimePagine(idModulo)];
        Integer numero=1;
        DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        
        String alias=map.get("ALIAS_AZIENDA").toString();
        if (alias==null || alias.isEmpty()){
        	alias="";
        }else{
        	alias="-"+alias;
        }
        
        dati[0] = "ANAGRAFICA-CONDOMINI-TESTATA"+alias+".pdf";
        Integer ultimePagine=1;
        Integer inizioTotali=0;
        
        
        try {
            dataSet = dsFactory.makeDataSet("", "DataSetUtenze");

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("WHERECONDITION", " WHERE ID_CONDOMINIO=" + idModulo);
            param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
            param.put("WHERECONDITION_PROFILI", " IS NOT NULL ");
            dataSet.setParam(param);
            dataSet.open();
            
            while (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	//if (numero>1){
            		creazionePagineConNomiCampi(numero,alias);
            	//}
            	dati[numero]="ANAGRAFICA-UTENZE-CONDOMINI-"+numero+".pdf";
                numero= numero+1;
            }
            
            dataSet.rewind();
            
            inizioTotali=numero;
            numero=1;
        	/*while (dataSet.hasMoreElements()) {
        		Row_itf dbRow = (Row_itf) dataSet.nextElement();
        		
            	if (numero%27==0){
            		creazioneUltimePagine(ultimePagine);
            		dati[inizioTotali]="ANAGRAFICA-UTENZE-CONDOMINI-ULTIMA-"+ultimePagine+".pdf";
            		numero= numero+1;
            		ultimePagine=ultimePagine+1;
            		inizioTotali=inizioTotali+1;
            	}
            	
            	numero= numero+1;
            }*/
            
            creazioneUltimePagine(ultimePagine,alias);
            dati[inizioTotali]="ANAGRAFICA-UTENZE-CONDOMINI-ULTIMA-"+ultimePagine+".pdf";
            
            
            
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),
                    "Errore nella ricerca dell'ultimo progressivo del dataset " + "DataSetUtenze");
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
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DataSetUtenze");
                }
            }
        }

        //setFixedNumberOfPages(setDimensionUltimePagine(idModulo)+1);
        setFixedNumberOfPages(setDimension(idModulo)+setDimensionUltimePagine(idModulo));
        setFixedPagesTemplateArray(dati);
    }

    
    
    @Override
    protected void putIndexedFieldInMap(Map<String, Object> map, int i, String fieldName, Object fieldValue) {

        if (CondominiDAO.DENOMINAZIONE.equals(fieldName)) {
            map.put(fieldValue + "." + i, Constants_itf.ACROBAT_CHECKED);
        } else {
            super.putIndexedFieldInMap(map, i, fieldName, fieldValue);
        }
    }
    
    
    private void creazionePagineConNomiCampi(Integer numero, String alias) throws DocumentException {
		try {
        String src=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"ANAGRAFICA-UTENZE-CONDOMINI-0"+alias+".pdf";
        String dest=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"ANAGRAFICA-UTENZE-CONDOMINI-"+numero+".pdf";
        String numeroNuovo=numero.toString();
        PdfReader reader;
		reader = new PdfReader(src);
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        AcroFields form = stamper.getAcroFields();
        form.renameField("DENOMINAZIONE_1", "DENOMINAZIONE_"+numeroNuovo);
        form.renameField("N_TELEFONO_1", "N_TELEFONO_"+numeroNuovo);
        form.renameField("SCALA_1", "SCALA_"+numeroNuovo);
        form.renameField("PIANO_1", "PIANO_"+numeroNuovo);
        form.renameField("INTERNO_1", "INTERNO_"+numeroNuovo);
        form.renameField("MILLESIMI_1", "MILLESIMI_"+numeroNuovo);
        form.renameField("MILLESIMI_ACS_1", "MILLESIMI_ACS_"+numeroNuovo);
        form.renameField("MILLESIMI_CLIMA_1", "MILLESIMI_CLIMA_"+numeroNuovo);
        form.renameField("PARZIALE_POTENZA_1", "PARZIALE_POTENZA_"+numeroNuovo);
        
        
        for (int q=1;q<35;q++){
        	form.renameField("STANZA_1_"+q, "STANZA_"+numeroNuovo+"_"+q);
        	form.renameField("RILEVATORE_1_"+q, "RILEVATORE_"+numeroNuovo+"_"+q);
        	form.renameField("TIPO_1_"+q, "TIPO_"+numeroNuovo+"_"+q);
        	form.renameField("MARCA_1_"+q, "MARCA_"+numeroNuovo+"_"+q);
        	form.renameField("LARGHEZZA_1_"+q, "LARGHEZZA_"+numeroNuovo+"_"+q);
        	form.renameField("ALTEZZA_1_"+q, "ALTEZZA_"+numeroNuovo+"_"+q);
        	form.renameField("PROFONDITA_1_"+q, "PROFONDITA_"+numeroNuovo+"_"+q);
        	form.renameField("ELEMENTI_1_"+q, "ELEMENTI_"+numeroNuovo+"_"+q);
        	form.renameField("COEFF_1_"+q, "COEFF_"+numeroNuovo+"_"+q);
        	form.renameField("POTENZA_1_"+q, "POTENZA_"+numeroNuovo+"_"+q);
        	form.renameField("ESP_1_1_"+q, "ESP_1_"+numeroNuovo+"_"+q);
        	form.renameField("ESP_2_1_"+q, "ESP_2_"+numeroNuovo+"_"+q);
        	form.renameField("TIPO_VALVOLA_1_"+q, "TIPO_VALVOLA_"+numeroNuovo+"_"+q);
        	form.renameField("DIAMETRO_1_"+q, "DIAMETRO_"+numeroNuovo+"_"+q);
        	form.renameField("MAT_TUBO_1_"+q, "MAT_TUBO_"+numeroNuovo+"_"+q);
        	form.renameField("PREREG_1_"+q, "PREREG_"+numeroNuovo+"_"+q);
        }
        
         
        stamper.close();
        reader.close();
        reader = new PdfReader(dest);
        } catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
    
    
    
    private int setDimension(String idModulo) {
   	 DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        Integer dimensione=0;

        try {
            dataSet = dsFactory.makeDataSet("", "DataSetUtenze");

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("WHERECONDITION", " WHERE ID_CONDOMINIO=" + idModulo);
            param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
            param.put("WHERECONDITION_PROFILI", " IS NOT NULL ");
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	dimensione=dimensione+1;
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),"Errore nella ricerca dell'ultimo progressivo del dataset " + "DSRipartizioniLettureDettaglioStampaNumeroRighe");
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DSRipartizioniLettureDettaglioStampaNumeroRighe");
                }
            }
        }
		return dimensione;
	}
    
    
    
    private int setDimensionUltimePagine(String idModulo) {
   	    Integer dimensione=2;
		return dimensione;
	}
    
    
    
	private void creazioneUltimePagine(Integer numero, String alias) throws DocumentException {
		try {
        String src=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"ANAGRAFICA-UTENZE-CONDOMINI-ULTIMA"+alias+".pdf";
        String dest=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"ANAGRAFICA-UTENZE-CONDOMINI-ULTIMA-"+numero+".pdf";
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
	            form.renameField("MILLESIMI_ACS_"+i, "MILLESIMI_ACS_"+(i+(26*(numeroNuovo-1))));
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
        nomeFileOutput = "Anagrafica-Condominio-"+ condominio.replace(" ", "_") + ".pdf";
        
        return nomeFileOutput;
    }

    @Override
    protected void setAcroField(Map<String, Object> map, String acroFieldName) {

         super.setAcroField(map, acroFieldName);
    }
}
