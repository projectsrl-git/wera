
package net.projectsrl.wera.ripartizioniletture.core;

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
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.wera.pdf.CreateWERAPdf_base;
import net.projectsrl.wera.ripartizioniletture.db.RipartizioniLettureDettagliDAO;

public class CreateRipartizioniLettureConDettagliPdf extends CreateWERAPdf_base {

    public CreateRipartizioniLettureConDettagliPdf(String outDirectory, Map<String, Object> map) throws Exception {

        super(outDirectory, map);
        
        String alias=map.get("ALIAS_AZIENDA").toString();
        if (alias==null || alias.isEmpty()){
        	alias="";
        }else{
        	alias="-"+alias;
        }
        
        String idModulo = map.get("ID_MODULO").toString();
        //String[] dati = new String[setDimensionUltimePagine(idModulo)+1];
        Integer nrUltimePagine=setDimensionUltimePagine(idModulo);
        String[] dati = new String[setDimension(idModulo)+nrUltimePagine];
        Integer numero=1;
        DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        
        dati[0] = "RIPARTIZIONI-LETTURE-TESTATA"+alias+".pdf";
        Integer ultimePagine=1;
        Integer inizioTotali=0;
        Integer scalaPagine=nrUltimePagine-1;
        
        
        try {
            dataSet = dsFactory.makeDataSet("", "DSRipartizioniLettureDettaglioStampa");

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("WHERECONDITION", " WHERE RI.ID_MODULO=" + idModulo);
            param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
            dataSet.setParam(param);
            dataSet.open();
            
            while (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	//if (numero>1){
            		creazionePagineConNomiCampi(numero, alias);
            	//}else{
            		setNumeroPagina(numero, alias);
            	//}
            	dati[numero]="RIPARTIZIONI-LETTURE-DETTAGLI-"+numero+".pdf";
                numero= numero+1;
            }
            
            dataSet.rewind();
            
            inizioTotali=numero;
            numero=1;
        	while (dataSet.hasMoreElements()) {
        		Row_itf dbRow = (Row_itf) dataSet.nextElement();
        		
            	if (numero%27==0){
            		creazioneUltimePagine(ultimePagine, inizioTotali, alias);
            		dati[inizioTotali-scalaPagine]="RIPARTIZIONI-LETTURE-ULTIMA-PAGINA-"+ultimePagine+".pdf";
            		scalaPagine=scalaPagine-1;
            		numero= numero+1;
            		ultimePagine=ultimePagine+1;
            		inizioTotali=inizioTotali+1;
            	}
            	
            	numero= numero+1;
            }
            
            creazioneUltimePagine(ultimePagine,inizioTotali, alias);
            dati[inizioTotali-1]="RIPARTIZIONI-LETTURE-ULTIMA-PAGINA-"+ultimePagine+".pdf";
            
            
            
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),
                    "Errore nella ricerca dell'ultimo progressivo del dataset " + "DSRipartizioniLettureDettaglioStampa");
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
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DSRipartizioniLettureDettaglioStampa");
                }
            }
        }

        //setFixedNumberOfPages(setDimensionUltimePagine(idModulo)+1);
        setFixedNumberOfPages(setDimension(idModulo)+setDimensionUltimePagine(idModulo));
        setFixedPagesTemplateArray(dati);
    }

    
    
    @Override
    protected void putIndexedFieldInMap(Map<String, Object> map, int i, String fieldName, Object fieldValue) {

        if (RipartizioniLettureDettagliDAO.DENOMINAZIONE.equals(fieldName)) {
            map.put(fieldValue + "." + i, Constants_itf.ACROBAT_CHECKED);
        } else {
            super.putIndexedFieldInMap(map, i, fieldName, fieldValue);
        }
    }
    
    
    private void creazionePagineConNomiCampi(Integer numero, String alias) throws DocumentException {
		try {
        String src=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RIPARTIZIONI-LETTURE-DETTAGLI-0"+alias+".pdf";
        String dest=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RIPARTIZIONI-LETTURE-DETTAGLI-"+numero+".pdf";
        String numeroNuovo=numero.toString();
        PdfReader reader;
		reader = new PdfReader(src);
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        AcroFields form = stamper.getAcroFields();
        form.renameField("DENOMINAZIONE_1", "DENOMINAZIONE_"+numeroNuovo);
        form.renameField("MILLESIMI_1", "MILLESIMI_"+numeroNuovo);
        form.renameField("LETTURA_1", "LETTURA_"+numeroNuovo);
        form.renameField("LETTURA_ACS_1", "LETTURA_ACS_"+numeroNuovo);
        form.renameField("LETTURA_AFS_1", "LETTURA_AFS_"+numeroNuovo);
        form.renameField("METANO_FISSO_1", "METANO_FISSO_"+numeroNuovo);
        form.renameField("METANO_MILLESIMI_1", "METANO_MILLESIMI_"+numeroNuovo);
        form.renameField("METANO_1", "METANO_"+numeroNuovo);
        form.renameField("FORZA_MOTRICE_1", "FORZA_MOTRICE_"+numeroNuovo);
        form.renameField("CONDUZIONE_ANTICIPO_1", "CONDUZIONE_ANTICIPO_"+numeroNuovo);
        form.renameField("CONDUZIONE_SALDO_1", "CONDUZIONE_SALDO_"+numeroNuovo);
        form.renameField("SUBTOTALE_COSTI_MILLESIMI_1", "SUBTOTALE_COSTI_MILLESIMI_"+numeroNuovo);
        form.renameField("CMI_CL_LORDO_1", "CMI_CL_LORDO_"+numeroNuovo);
        form.renameField("NUMERO_RIPARTITORI_1", "NUMERO_RIPARTITORI_"+numeroNuovo);
        form.renameField("COSTI_LETTURE_1", "COSTI_LETTURE_"+numeroNuovo);
        form.renameField("TOTALE_CON_LETTURE_1", "TOTALE_CON_LETTURE_"+numeroNuovo);
        
        
        form.renameField("RILEVATORE_1_1", "RILEVATORE_"+numeroNuovo+"_1");
        form.renameField("RILEVATORE_1_2", "RILEVATORE_"+numeroNuovo+"_2");
        form.renameField("RILEVATORE_1_3", "RILEVATORE_"+numeroNuovo+"_3");
        form.renameField("RILEVATORE_1_4", "RILEVATORE_"+numeroNuovo+"_4");
        form.renameField("RILEVATORE_1_5", "RILEVATORE_"+numeroNuovo+"_5");
        form.renameField("RILEVATORE_1_6", "RILEVATORE_"+numeroNuovo+"_6");
        form.renameField("RILEVATORE_1_7", "RILEVATORE_"+numeroNuovo+"_7");
        form.renameField("RILEVATORE_1_8", "RILEVATORE_"+numeroNuovo+"_8");
        form.renameField("RILEVATORE_1_9", "RILEVATORE_"+numeroNuovo+"_9");
        form.renameField("RILEVATORE_1_10", "RILEVATORE_"+numeroNuovo+"_10");
        form.renameField("RILEVATORE_1_11", "RILEVATORE_"+numeroNuovo+"_11");
        form.renameField("RILEVATORE_1_12", "RILEVATORE_"+numeroNuovo+"_12");
        form.renameField("RILEVATORE_1_13", "RILEVATORE_"+numeroNuovo+"_13");
        form.renameField("RILEVATORE_1_14", "RILEVATORE_"+numeroNuovo+"_14");
        form.renameField("RILEVATORE_1_15", "RILEVATORE_"+numeroNuovo+"_15");
        form.renameField("RILEVATORE_1_16", "RILEVATORE_"+numeroNuovo+"_16");
        form.renameField("RILEVATORE_1_17", "RILEVATORE_"+numeroNuovo+"_17");
        form.renameField("RILEVATORE_1_18", "RILEVATORE_"+numeroNuovo+"_18");
        form.renameField("RILEVATORE_1_19", "RILEVATORE_"+numeroNuovo+"_19");
        form.renameField("RILEVATORE_1_20", "RILEVATORE_"+numeroNuovo+"_20");
        form.renameField("RILEVATORE_1_21", "RILEVATORE_"+numeroNuovo+"_21");
        form.renameField("RILEVATORE_1_22", "RILEVATORE_"+numeroNuovo+"_22");
        form.renameField("RILEVATORE_1_23", "RILEVATORE_"+numeroNuovo+"_23");
        form.renameField("RILEVATORE_1_24", "RILEVATORE_"+numeroNuovo+"_24");
        form.renameField("RILEVATORE_1_25", "RILEVATORE_"+numeroNuovo+"_25");
        form.renameField("RILEVATORE_1_26", "RILEVATORE_"+numeroNuovo+"_26");
        form.renameField("RILEVATORE_1_27", "RILEVATORE_"+numeroNuovo+"_27");
        form.renameField("RILEVATORE_1_28", "RILEVATORE_"+numeroNuovo+"_28");
        form.renameField("RILEVATORE_1_29", "RILEVATORE_"+numeroNuovo+"_29");
        form.renameField("RILEVATORE_1_30", "RILEVATORE_"+numeroNuovo+"_30");
        form.renameField("RILEVATORE_1_31", "RILEVATORE_"+numeroNuovo+"_31");
        form.renameField("RILEVATORE_1_32", "RILEVATORE_"+numeroNuovo+"_32");
        form.renameField("RILEVATORE_1_33", "RILEVATORE_"+numeroNuovo+"_33");
        form.renameField("RILEVATORE_1_34", "RILEVATORE_"+numeroNuovo+"_34");
        
        form.renameField("STANZA_1_1", "STANZA_"+numeroNuovo+"_1");
        form.renameField("STANZA_1_2", "STANZA_"+numeroNuovo+"_2");
        form.renameField("STANZA_1_3", "STANZA_"+numeroNuovo+"_3");
        form.renameField("STANZA_1_4", "STANZA_"+numeroNuovo+"_4");
        form.renameField("STANZA_1_5", "STANZA_"+numeroNuovo+"_5");
        form.renameField("STANZA_1_6", "STANZA_"+numeroNuovo+"_6");
        form.renameField("STANZA_1_7", "STANZA_"+numeroNuovo+"_7");
        form.renameField("STANZA_1_8", "STANZA_"+numeroNuovo+"_8");
        form.renameField("STANZA_1_9", "STANZA_"+numeroNuovo+"_9");
        form.renameField("STANZA_1_10", "STANZA_"+numeroNuovo+"_10");
        form.renameField("STANZA_1_11", "STANZA_"+numeroNuovo+"_11");
        form.renameField("STANZA_1_12", "STANZA_"+numeroNuovo+"_12");
        form.renameField("STANZA_1_13", "STANZA_"+numeroNuovo+"_13");
        form.renameField("STANZA_1_14", "STANZA_"+numeroNuovo+"_14");
        form.renameField("STANZA_1_15", "STANZA_"+numeroNuovo+"_15");
        form.renameField("STANZA_1_16", "STANZA_"+numeroNuovo+"_16");
        form.renameField("STANZA_1_17", "STANZA_"+numeroNuovo+"_17");
        form.renameField("STANZA_1_18", "STANZA_"+numeroNuovo+"_18");
        form.renameField("STANZA_1_19", "STANZA_"+numeroNuovo+"_19");
        form.renameField("STANZA_1_20", "STANZA_"+numeroNuovo+"_20");
        form.renameField("STANZA_1_21", "STANZA_"+numeroNuovo+"_21");
        form.renameField("STANZA_1_22", "STANZA_"+numeroNuovo+"_22");
        form.renameField("STANZA_1_23", "STANZA_"+numeroNuovo+"_23");
        form.renameField("STANZA_1_24", "STANZA_"+numeroNuovo+"_24");
        form.renameField("STANZA_1_25", "STANZA_"+numeroNuovo+"_25");
        form.renameField("STANZA_1_26", "STANZA_"+numeroNuovo+"_26");
        form.renameField("STANZA_1_27", "STANZA_"+numeroNuovo+"_27");
        form.renameField("STANZA_1_28", "STANZA_"+numeroNuovo+"_28");
        form.renameField("STANZA_1_29", "STANZA_"+numeroNuovo+"_29");
        form.renameField("STANZA_1_30", "STANZA_"+numeroNuovo+"_30");
        form.renameField("STANZA_1_31", "STANZA_"+numeroNuovo+"_31");
        form.renameField("STANZA_1_32", "STANZA_"+numeroNuovo+"_32");
        form.renameField("STANZA_1_33", "STANZA_"+numeroNuovo+"_33");
        form.renameField("STANZA_1_34", "STANZA_"+numeroNuovo+"_34");
        
        form.renameField("LETTURA_SINGOLO_1_1", "LETTURA_SINGOLO_"+numeroNuovo+"_1");
        form.renameField("LETTURA_SINGOLO_1_2", "LETTURA_SINGOLO_"+numeroNuovo+"_2");
        form.renameField("LETTURA_SINGOLO_1_3", "LETTURA_SINGOLO_"+numeroNuovo+"_3");
        form.renameField("LETTURA_SINGOLO_1_4", "LETTURA_SINGOLO_"+numeroNuovo+"_4");
        form.renameField("LETTURA_SINGOLO_1_5", "LETTURA_SINGOLO_"+numeroNuovo+"_5");
        form.renameField("LETTURA_SINGOLO_1_6", "LETTURA_SINGOLO_"+numeroNuovo+"_6");
        form.renameField("LETTURA_SINGOLO_1_7", "LETTURA_SINGOLO_"+numeroNuovo+"_7");
        form.renameField("LETTURA_SINGOLO_1_8", "LETTURA_SINGOLO_"+numeroNuovo+"_8");
        form.renameField("LETTURA_SINGOLO_1_9", "LETTURA_SINGOLO_"+numeroNuovo+"_9");
        form.renameField("LETTURA_SINGOLO_1_10", "LETTURA_SINGOLO_"+numeroNuovo+"_10");
        form.renameField("LETTURA_SINGOLO_1_11", "LETTURA_SINGOLO_"+numeroNuovo+"_11");
        form.renameField("LETTURA_SINGOLO_1_12", "LETTURA_SINGOLO_"+numeroNuovo+"_12");
        form.renameField("LETTURA_SINGOLO_1_13", "LETTURA_SINGOLO_"+numeroNuovo+"_13");
        form.renameField("LETTURA_SINGOLO_1_14", "LETTURA_SINGOLO_"+numeroNuovo+"_14");
        form.renameField("LETTURA_SINGOLO_1_15", "LETTURA_SINGOLO_"+numeroNuovo+"_15");
        form.renameField("LETTURA_SINGOLO_1_16", "LETTURA_SINGOLO_"+numeroNuovo+"_16");
        form.renameField("LETTURA_SINGOLO_1_17", "LETTURA_SINGOLO_"+numeroNuovo+"_17");
        form.renameField("LETTURA_SINGOLO_1_18", "LETTURA_SINGOLO_"+numeroNuovo+"_18");
        form.renameField("LETTURA_SINGOLO_1_19", "LETTURA_SINGOLO_"+numeroNuovo+"_19");
        form.renameField("LETTURA_SINGOLO_1_20", "LETTURA_SINGOLO_"+numeroNuovo+"_20");
        form.renameField("LETTURA_SINGOLO_1_21", "LETTURA_SINGOLO_"+numeroNuovo+"_21");
        form.renameField("LETTURA_SINGOLO_1_22", "LETTURA_SINGOLO_"+numeroNuovo+"_22");
        form.renameField("LETTURA_SINGOLO_1_23", "LETTURA_SINGOLO_"+numeroNuovo+"_23");
        form.renameField("LETTURA_SINGOLO_1_24", "LETTURA_SINGOLO_"+numeroNuovo+"_24");
        form.renameField("LETTURA_SINGOLO_1_25", "LETTURA_SINGOLO_"+numeroNuovo+"_25");
        form.renameField("LETTURA_SINGOLO_1_26", "LETTURA_SINGOLO_"+numeroNuovo+"_26");
        form.renameField("LETTURA_SINGOLO_1_27", "LETTURA_SINGOLO_"+numeroNuovo+"_27");
        form.renameField("LETTURA_SINGOLO_1_28", "LETTURA_SINGOLO_"+numeroNuovo+"_28");
        form.renameField("LETTURA_SINGOLO_1_29", "LETTURA_SINGOLO_"+numeroNuovo+"_29");
        form.renameField("LETTURA_SINGOLO_1_30", "LETTURA_SINGOLO_"+numeroNuovo+"_30");
        form.renameField("LETTURA_SINGOLO_1_31", "LETTURA_SINGOLO_"+numeroNuovo+"_31");
        form.renameField("LETTURA_SINGOLO_1_32", "LETTURA_SINGOLO_"+numeroNuovo+"_32");
        form.renameField("LETTURA_SINGOLO_1_33", "LETTURA_SINGOLO_"+numeroNuovo+"_33");
        form.renameField("LETTURA_SINGOLO_1_34", "LETTURA_SINGOLO_"+numeroNuovo+"_34");
        
        
        form.renameField("LETTURA_ACS_SINGOLO_1_1", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_1");
        form.renameField("LETTURA_ACS_SINGOLO_1_2", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_2");
        form.renameField("LETTURA_ACS_SINGOLO_1_3", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_3");
        form.renameField("LETTURA_ACS_SINGOLO_1_4", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_4");
        form.renameField("LETTURA_ACS_SINGOLO_1_5", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_5");
        form.renameField("LETTURA_ACS_SINGOLO_1_6", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_6");
        form.renameField("LETTURA_ACS_SINGOLO_1_7", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_7");
        form.renameField("LETTURA_ACS_SINGOLO_1_8", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_8");
        form.renameField("LETTURA_ACS_SINGOLO_1_9", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_9");
        form.renameField("LETTURA_ACS_SINGOLO_1_10", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_10");
        form.renameField("LETTURA_ACS_SINGOLO_1_11", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_11");
        form.renameField("LETTURA_ACS_SINGOLO_1_12", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_12");
        form.renameField("LETTURA_ACS_SINGOLO_1_13", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_13");
        form.renameField("LETTURA_ACS_SINGOLO_1_14", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_14");
        form.renameField("LETTURA_ACS_SINGOLO_1_15", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_15");
        form.renameField("LETTURA_ACS_SINGOLO_1_16", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_16");
        form.renameField("LETTURA_ACS_SINGOLO_1_17", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_17");
        form.renameField("LETTURA_ACS_SINGOLO_1_18", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_18");
        form.renameField("LETTURA_ACS_SINGOLO_1_19", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_19");
        form.renameField("LETTURA_ACS_SINGOLO_1_20", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_20");
        form.renameField("LETTURA_ACS_SINGOLO_1_21", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_21");
        form.renameField("LETTURA_ACS_SINGOLO_1_22", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_22");
        form.renameField("LETTURA_ACS_SINGOLO_1_23", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_23");
        form.renameField("LETTURA_ACS_SINGOLO_1_24", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_24");
        form.renameField("LETTURA_ACS_SINGOLO_1_25", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_25");
        form.renameField("LETTURA_ACS_SINGOLO_1_26", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_26");
        form.renameField("LETTURA_ACS_SINGOLO_1_27", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_27");
        form.renameField("LETTURA_ACS_SINGOLO_1_28", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_28");
        form.renameField("LETTURA_ACS_SINGOLO_1_29", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_29");
        form.renameField("LETTURA_ACS_SINGOLO_1_30", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_30");
        form.renameField("LETTURA_ACS_SINGOLO_1_31", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_31");
        form.renameField("LETTURA_ACS_SINGOLO_1_32", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_32");
        form.renameField("LETTURA_ACS_SINGOLO_1_33", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_33");
        form.renameField("LETTURA_ACS_SINGOLO_1_34", "LETTURA_ACS_SINGOLO_"+numeroNuovo+"_34");


        form.renameField("LETTURA_AFS_SINGOLO_1_1", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_1");
        form.renameField("LETTURA_AFS_SINGOLO_1_2", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_2");
        form.renameField("LETTURA_AFS_SINGOLO_1_3", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_3");
        form.renameField("LETTURA_AFS_SINGOLO_1_4", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_4");
        form.renameField("LETTURA_AFS_SINGOLO_1_5", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_5");
        form.renameField("LETTURA_AFS_SINGOLO_1_6", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_6");
        form.renameField("LETTURA_AFS_SINGOLO_1_7", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_7");
        form.renameField("LETTURA_AFS_SINGOLO_1_8", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_8");
        form.renameField("LETTURA_AFS_SINGOLO_1_9", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_9");
        form.renameField("LETTURA_AFS_SINGOLO_1_10", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_10");
        form.renameField("LETTURA_AFS_SINGOLO_1_11", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_11");
        form.renameField("LETTURA_AFS_SINGOLO_1_12", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_12");
        form.renameField("LETTURA_AFS_SINGOLO_1_13", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_13");
        form.renameField("LETTURA_AFS_SINGOLO_1_14", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_14");
        form.renameField("LETTURA_AFS_SINGOLO_1_15", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_15");
        form.renameField("LETTURA_AFS_SINGOLO_1_16", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_16");
        form.renameField("LETTURA_AFS_SINGOLO_1_17", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_17");
        form.renameField("LETTURA_AFS_SINGOLO_1_18", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_18");
        form.renameField("LETTURA_AFS_SINGOLO_1_19", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_19");
        form.renameField("LETTURA_AFS_SINGOLO_1_20", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_20");
        form.renameField("LETTURA_AFS_SINGOLO_1_21", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_21");
        form.renameField("LETTURA_AFS_SINGOLO_1_22", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_22");
        form.renameField("LETTURA_AFS_SINGOLO_1_23", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_23");
        form.renameField("LETTURA_AFS_SINGOLO_1_24", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_24");
        form.renameField("LETTURA_AFS_SINGOLO_1_25", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_25");
        form.renameField("LETTURA_AFS_SINGOLO_1_26", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_26");
        form.renameField("LETTURA_AFS_SINGOLO_1_27", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_27");
        form.renameField("LETTURA_AFS_SINGOLO_1_28", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_28");
        form.renameField("LETTURA_AFS_SINGOLO_1_29", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_29");
        form.renameField("LETTURA_AFS_SINGOLO_1_30", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_30");
        form.renameField("LETTURA_AFS_SINGOLO_1_31", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_31");
        form.renameField("LETTURA_AFS_SINGOLO_1_32", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_32");
        form.renameField("LETTURA_AFS_SINGOLO_1_33", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_33");
        form.renameField("LETTURA_AFS_SINGOLO_1_34", "LETTURA_AFS_SINGOLO_"+numeroNuovo+"_34");
        
        form.setField("NR_PAG_1",String.valueOf(numero+1));
        
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
            dataSet = dsFactory.makeDataSet("", "DSRipartizioniLettureDettaglioStampaNumeroRighe");

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("WHERECONDITION", " WHERE RI.ID_MODULO=" + idModulo);
            param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	dimensione=(Integer) dbRow.getField("NUMERO");
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
   	 DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        Integer dimensione=0;

        try {
            dataSet = dsFactory.makeDataSet("", "DSRipartizioniLettureDettaglioStampaNumeroRighe");

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
    
    
    
	private void creazioneUltimePagine(Integer numero, Integer inizioTotali, String alias) throws DocumentException {
		try {
        String src=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RIPARTIZIONI-LETTURE-ULTIMA-PAGINA"+alias+".pdf";
        String dest=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RIPARTIZIONI-LETTURE-ULTIMA-PAGINA-"+numero+".pdf";
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
	            form.renameField("LETTURA_AFS_"+i, "LETTURA_AFS_"+(i+(26*(numeroNuovo-1))));
	            //form.renameField("NR_PAG_"+i, "NR_PAG_"+(i+(26*(numeroNuovo-1))));
	            form.setField("NR_PAG",String.valueOf(inizioTotali));
        	}
        }
        
        form.setField("NR_PAG",String.valueOf(inizioTotali));
       
      
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
    
    
    
    private void setNumeroPagina(Integer numero, String alias) throws DocumentException {
		try {
        String src=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RIPARTIZIONI-LETTURE-DETTAGLI"+alias+".pdf";
        String dest=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RIPARTIZIONI-LETTURE-DETTAGLI-1.pdf";
        PdfReader reader;
		reader = new PdfReader(src);
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        AcroFields form = stamper.getAcroFields();
        
        form.setField("NR_PAG_1",String.valueOf(numero+1));
        
        stamper.close();
        reader.close();
        reader = new PdfReader(dest);
        } catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
