
package net.projectsrl.wera.ripartizioniuni2018.core;

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
import net.projectsrl.wera.ripartizioniuni2018.db.RipartizioniUNI2018DettagliDAO;

public class CreateRipartizioniUNI2018Pdf extends CreateALIMODPdf_base {
	
	public static final String RIPARTIZIONI_UNI_2018_DETTAGLIO_STAMPA = "DSRipartizioniUNI2018DettaglioStampa";
	public static final String RIPARTIZIONI_UNI_2018_DETTAGLIO_STAMPA_NUMERO_RIGHE = "DSRipartizioniUNI2018DettaglioStampaNumeroRighe";

    public CreateRipartizioniUNI2018Pdf(String outDirectory, Map<String, Object> map) throws Exception {

        super(outDirectory, map);
        
        String alias=map.get("ALIAS_AZIENDA").toString();
        if (alias==null || alias.isEmpty()){
        	alias="";
        }else{
        	alias="-"+alias;
        }
        
        String idModulo = map.get("ID_MODULO").toString();
        String tipoRipartizione = map.get("TIPO_RIPARTIZIONE").toString();
        
        Integer ultimePagine=1;
        Integer inizioTotali=0;
        Integer righePerPaginaConsumiPrima=20;
        Integer righePerPaginaSpesePrima=17;
        Integer righePerPaginaConsumiAggiuntiva=25;
        Integer righePerPaginaSpeseAggiuntiva=26;
        Integer righePerPaginaLocaliComuni=26;
        Integer numeroPagine=5;
        
        
        Boolean localiComuni=false;
        String whereCondLocaliComuni="";
        Integer pagineAggiuntiveConsumi=setDimensionPaginaAggiuntiva(idModulo,righePerPaginaConsumiPrima,righePerPaginaConsumiAggiuntiva,whereCondLocaliComuni,localiComuni);
        
        whereCondLocaliComuni=" AND TIPO_UTENZA='000'";
        Integer pagineAggiuntiveSpese=setDimensionPaginaAggiuntiva(idModulo,righePerPaginaSpesePrima,righePerPaginaSpeseAggiuntiva,whereCondLocaliComuni,localiComuni);
        
        localiComuni = true;
        whereCondLocaliComuni=" AND TIPO_UTENZA='001'";
        Integer paginaLocaliComuni=setDimensionPaginaAggiuntiva(idModulo,righePerPaginaSpesePrima,righePerPaginaSpeseAggiuntiva,whereCondLocaliComuni,localiComuni);
        
        
        Integer pagineUtenza=setDimensionPaginaUtenza(idModulo)[0];
        Integer numeroLocatari=setDimensionPaginaUtenza(idModulo)[1];
        
        
        String[] dati = new String[numeroPagine+pagineAggiuntiveConsumi+pagineAggiuntiveSpese+paginaLocaliComuni+pagineUtenza];
        
        
        if (tipoRipartizione.equals(Constants_itf.CONSUNTIVO)){
        	dati[0] = "RipartizioniUni102002018-Consuntivo-0"+alias+".pdf";
            dati[1] = "RipartizioniUni102002018-Consuntivo-1"+alias+".pdf";
            dati[2] = "RipartizioniUni102002018-Consuntivo-2"+alias+".pdf";
            dati[3] = "RipartizioniUni102002018-consumi"+alias+".pdf";
        }

        if (tipoRipartizione.equals(Constants_itf.PREVISIONALE)){
        	dati[0] = "RipartizioniUni102002018-Previsionale-0"+alias+".pdf";
            dati[1] = "RipartizioniUni102002018-Previsionale-1"+alias+".pdf";
            dati[2] = "RipartizioniUni102002018-Previsionale-2"+alias+".pdf";
            dati[3] = "RipartizioniUni102002018-consumi"+alias+".pdf";
        }
        
        dati[4+pagineAggiuntiveConsumi] = "RipartizioniUni102002018-spese"+alias+".pdf";
        
        
        impostaPagineAggiuntive(idModulo, ultimePagine, inizioTotali, righePerPaginaConsumiPrima, dati, righePerPaginaSpesePrima,numeroPagine,pagineAggiuntiveConsumi,pagineAggiuntiveSpese,righePerPaginaConsumiAggiuntiva,righePerPaginaSpeseAggiuntiva, tipoRipartizione,paginaLocaliComuni,righePerPaginaLocaliComuni,alias);
        
        for (int w=0;w<numeroLocatari;w++){
        	rinominaCampiPagina1Utenza(w,alias);
        	rinominaCampiPagina2Utenza(w,alias);
        	dati[numeroPagine+pagineAggiuntiveConsumi+pagineAggiuntiveSpese+paginaLocaliComuni+(4*w)] = "RipartizioniUni102002018-Utenza-0.pdf";
            dati[numeroPagine+pagineAggiuntiveConsumi+pagineAggiuntiveSpese+paginaLocaliComuni+1+(4*w)] = "RipartizioniUni102002018-Utenza-1-"+w+".pdf";
            dati[numeroPagine+pagineAggiuntiveConsumi+pagineAggiuntiveSpese+paginaLocaliComuni+2+(4*w)] = "RipartizioniUni102002018-Utenza-2-"+w+".pdf";
            dati[numeroPagine+pagineAggiuntiveConsumi+pagineAggiuntiveSpese+paginaLocaliComuni+3+(4*w)] = "RipartizioniUni102002018-Utenza-legenda.pdf";
        }
        

        setFixedNumberOfPages(numeroPagine+pagineAggiuntiveConsumi+pagineAggiuntiveSpese+paginaLocaliComuni+pagineUtenza);
        setFixedPagesTemplateArray(dati);
    }


	private Integer[] setDimensionPaginaUtenza(String idModulo) {
		Integer[] dati = new Integer[2];
		Integer numero=0;
		Integer numeroPagineUtenza=4;
		
		DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        try {
            dataSet = dsFactory.makeDataSet("", RIPARTIZIONI_UNI_2018_DETTAGLIO_STAMPA);

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("WHERECONDITION", " WHERE RI.ID_MODULO=" + idModulo);
            param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
            dataSet.setParam(param);
            dataSet.open();
          
            
        	while (dataSet.hasMoreElements()) {
        		Row_itf dbRow = (Row_itf) dataSet.nextElement();
        	   		numero=numero+1;
            }
        	dati[0]=numeroPagineUtenza*numero;
        	dati[1]=numero;
        	
                      
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),
                    "Errore nella ricerca dell'ultimo progressivo del dataset " + "DSRipartizioniUNI2018DettaglioStampa");
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
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DSRipartizioniUNI2018DettaglioStampa");
                }
            }
        }
        
		return dati;
	}
	
	
	
	


	private void impostaPagineAggiuntive(String idModulo, Integer ultimePagine, Integer inizioTotali,
			Integer righePerPaginaConsumiPrima, String[] dati,  Integer righePerPaginaSpesePrima, Integer numeroPagine, Integer pagineAggiuntiveConsumi, Integer pagineAggiuntiveSpese, Integer righePerPaginaConsumiAggiuntiva, Integer righePerPaginaSpeseAggiuntiva, String tipoRipartizione, Integer paginaLocaliComuni, Integer righePerPaginaLocaliComuni, String alias)
			throws DocumentException {
		
			
		DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        
		try {
            dataSet = dsFactory.makeDataSet("", RIPARTIZIONI_UNI_2018_DETTAGLIO_STAMPA);

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("WHERECONDITION", " WHERE RI.ID_MODULO=" + idModulo);
            param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
            dataSet.setParam(param);
            dataSet.open();
          
            Integer numero=1;
            Integer contaPagina=0;
        	while (dataSet.hasMoreElements()) {
        		Row_itf dbRow = (Row_itf) dataSet.nextElement();
        		        		
        		if (numero>righePerPaginaConsumiPrima && numero%(righePerPaginaConsumiPrima+1)==0){
            		paginaAggiuntivaConsumi(ultimePagine,righePerPaginaConsumiPrima,righePerPaginaConsumiAggiuntiva,alias);

            		dati[numeroPagine+contaPagina]="RipartizioniUni102002018-consumi-aggiuntivo-"+ultimePagine+".pdf";
            		
            		numero= numero+1;
            		contaPagina=contaPagina+1;
            		righePerPaginaConsumiPrima=25;
            	}
            	
            	numero= numero+1;
            }
        	
        	//dataSet.rewind();
        	dataSet.close();
        	param.put("WHERECONDITION", " WHERE RI.ID_MODULO=" + idModulo+ " AND TIPO_UTENZA='000'");
            param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
            dataSet.setParam(param);
            dataSet.open();
        	
        	
        	numero=1;
        	contaPagina=0;
        	
        	while (dataSet.hasMoreElements()) {
        		Row_itf dbRow = (Row_itf) dataSet.nextElement();        		
        		        		
        		if (numero>righePerPaginaSpesePrima && numero%(righePerPaginaSpesePrima+1)==0){
            		paginaAggiuntivaSpese(ultimePagine,righePerPaginaSpesePrima,righePerPaginaSpeseAggiuntiva,alias);
            		
            		dati[numeroPagine+pagineAggiuntiveConsumi+contaPagina]="RipartizioniUni102002018-spese-aggiuntivo-"+ultimePagine+".pdf";
            		
            		contaPagina=contaPagina+1;
            		numero= numero+1;
            		righePerPaginaSpesePrima=26;
            	}
            	
            	numero= numero+1;
            }
        	
        	
        	
        	// locali uso comune da qui in poi
        	dataSet.close();
        	param.put("WHERECONDITION", " WHERE RI.ID_MODULO=" + idModulo+ " AND TIPO_UTENZA='001'");
            param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
            dataSet.setParam(param);
            dataSet.open();
        	
        	numero=1;
        	contaPagina=0;
        	
        	while (dataSet.hasMoreElements()) {
        		Row_itf dbRow = (Row_itf) dataSet.nextElement();
        		        		
        		if (numero==1){
            		dati[numeroPagine+pagineAggiuntiveConsumi+pagineAggiuntiveSpese+contaPagina]="RipartizioniUni102002018-spese-locali-uso-comune"+alias+".pdf";
            	}
            	numero= numero+1;
            }
        	
                      
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),
                    "Errore nella ricerca dell'ultimo progressivo del dataset " + "DSRipartizioniUNI2018DettaglioStampa");
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
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DSRipartizioniUNI2018DettaglioStampa");
                }
            }
        }
	}
	

    
    @Override
    protected void putIndexedFieldInMap(Map<String, Object> map, int i, String fieldName, Object fieldValue) {

        if (RipartizioniUNI2018DettagliDAO.DENOMINAZIONE.equals(fieldName)) {
            map.put(fieldValue + "." + i, Constants_itf.ACROBAT_CHECKED);
        } else {
            super.putIndexedFieldInMap(map, i, fieldName, fieldValue);
        }
    }
    
    
    private int setDimensionPaginaAggiuntiva(String idModulo, Integer righePerPagina, Integer righePerPaginaAggiuntiva, String whereCondLocaliComuni, Boolean localiComuni) {
   	 DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        Integer dimensione=0;
        Integer numeroRighe=0;
        
        double righeAvanzo=0;

        try {
            dataSet = dsFactory.makeDataSet("", RIPARTIZIONI_UNI_2018_DETTAGLIO_STAMPA_NUMERO_RIGHE);

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("WHERECONDITION", " WHERE RI.ID_MODULO=" + idModulo + whereCondLocaliComuni);
            param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
            param.put("NUMERO_RIGHE_PRIMA", righePerPagina.toString());
            
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	
           		righeAvanzo=(Integer) dbRow.getField("RIGHE_AVANZO");
           		numeroRighe=(Integer) dbRow.getField("NUMERO");
           		if (righeAvanzo>0){
           			dimensione=(int) Math.ceil(righeAvanzo/righePerPaginaAggiuntiva);
           		}
           		
           		if (localiComuni){
           			if (numeroRighe>=1){
           				dimensione=1;
           			}
           		}
           		
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),"Errore nella ricerca dell'ultimo progressivo del dataset " + "DSRipartizioniUNI2018DettaglioStampaNumeroRighe");
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DSRipartizioniUNI2018DettaglioStampaNumeroRighe");
                }
            }
        }
        
		return dimensione;
	}
    
    
    
	private void paginaAggiuntivaConsumi(Integer numero, Integer righePerPaginaConsumi, Integer righePerPaginaAggiuntiva, String alias) throws DocumentException {
		
		try {
			String src=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RipartizioniUni102002018-consumi-aggiuntivo"+alias+".pdf";
			String dest=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RipartizioniUni102002018-consumi-aggiuntivo-"+numero+".pdf";
			
	        Integer numeroNuovo=numero%righePerPaginaConsumi;
	        PdfReader reader;
			reader = new PdfReader(src);
			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
	        AcroFields form = stamper.getAcroFields();
	        
        	//for (int j=1;j<=righePerPaginaAggiuntiva;j++){
        	for (int j=righePerPaginaAggiuntiva;j>0;j--){
	        	form.renameField("DENOMINAZIONE_"+j, "DENOMINAZIONE_"+(j+righePerPaginaConsumi*(numeroNuovo)));
	        	form.renameField("MILLESIMI_"+j, "MILLESIMI_"+(j+righePerPaginaConsumi*(numeroNuovo)));
	            form.renameField("MILLESIMI_CLIMA_"+j, "MILLESIMI_CLIMA_"+(j+righePerPaginaConsumi*(numeroNuovo)));
	            form.renameField("MILLESIMI_ACS_"+j, "MILLESIMI_ACS_"+(j+righePerPaginaConsumi*(numeroNuovo)));
	            form.renameField("NUMERO_RIPARTITORI_"+j, "NUMERO_RIPARTITORI_"+(j+righePerPaginaConsumi*(numeroNuovo)));
	            form.renameField("LETTURA_"+j, "LETTURA_"+(j+righePerPaginaConsumi*(numeroNuovo)));
	            form.renameField("LETTURA_ACS_"+j, "LETTURA_ACS_"+(j+righePerPaginaConsumi*(numeroNuovo)));
	            form.renameField("CONSUMO_RISCALDAMENTO_Q_VOL_"+j, "CONSUMO_RISCALDAMENTO_Q_VOL_"+(j+righePerPaginaConsumi*(numeroNuovo)));
	            form.renameField("CONSUMO_RISCALDAMENTO_Q_INV_"+j, "CONSUMO_RISCALDAMENTO_Q_INV_"+(j+righePerPaginaConsumi*(numeroNuovo)));
	            form.renameField("CONSUMO_RISCALDAMENTO_Q_OBB_"+j, "CONSUMO_RISCALDAMENTO_Q_OBB_"+(j+righePerPaginaConsumi*(numeroNuovo)));
	            form.renameField("CONSUMO_RISCALDAMENTO_Q_TOT_"+j, "CONSUMO_RISCALDAMENTO_Q_TOT_"+(j+righePerPaginaConsumi*(numeroNuovo)));
	            form.renameField("CONSUMO_ACS_Q_VOL_"+j, "CONSUMO_ACS_Q_VOL_"+(j+righePerPaginaConsumi*(numeroNuovo)));
	            form.renameField("CONSUMO_ACS_Q_INV_"+j, "CONSUMO_ACS_Q_INV_"+(j+righePerPaginaConsumi*(numeroNuovo)));
	            form.renameField("CONSUMO_ACS_Q_OBB_"+j, "CONSUMO_ACS_Q_OBB_"+(j+righePerPaginaConsumi*(numeroNuovo)));
	            form.renameField("CONSUMO_ACS_Q_TOT_"+j, "CONSUMO_ACS_Q_TOT_"+(j+righePerPaginaConsumi*(numeroNuovo)));

        	}
	        stamper.close();
	        reader.close();
	        reader = new PdfReader(dest);
	        } catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}
		
	
	
	
	private void paginaAggiuntivaSpese(Integer numero, Integer righePerPaginaSpese, Integer righePerPaginaSpeseAggiuntiva, String alias) throws DocumentException {
	
		try {
			
			String src=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RipartizioniUni102002018-spese-aggiuntivo"+alias+".pdf";
			String dest=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RipartizioniUni102002018-spese-aggiuntivo-"+numero+".pdf";
			
	        Integer numeroNuovo=numero%righePerPaginaSpese;
	        PdfReader reader;
			reader = new PdfReader(src);
			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
	        AcroFields form = stamper.getAcroFields();

        	//for (int y=1;y<=righePerPaginaSpeseAggiuntiva;y++){
        	for (int y=righePerPaginaSpeseAggiuntiva;y>0;y--){
	        	form.renameField("DENOMINAZIONE_"+y, "DENOMINAZIONE_"+(y+righePerPaginaSpese*(numeroNuovo)));
	        	form.renameField("SPESE_RISCALDAMENTO_S_VOL_"+y, "SPESE_RISCALDAMENTO_S_VOL_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_RISCALDAMENTO_S_INV_"+y, "SPESE_RISCALDAMENTO_S_INV_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_RISCALDAMENTO_S_OBB_"+y, "SPESE_RISCALDAMENTO_S_OBB_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_RISCALDAMENTO_S_E_"+y, "SPESE_RISCALDAMENTO_S_E_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_RISCALDAMENTO_S_G_"+y, "SPESE_RISCALDAMENTO_S_G_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_RISCALDAMENTO_S_C_"+y, "SPESE_RISCALDAMENTO_S_C_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_RISCALDAMENTO_S_P_"+y, "SPESE_RISCALDAMENTO_S_P_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_RISCALDAMENTO_S_UC_"+y, "SPESE_RISCALDAMENTO_S_UC_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_RISCALDAMENTO_S_TOT_"+y, "SPESE_RISCALDAMENTO_S_TOT_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_ACS_S_VOL_"+y, "SPESE_ACS_S_VOL_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_ACS_S_INV_"+y, "SPESE_ACS_S_INV_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_ACS_S_OBB_"+y, "SPESE_ACS_S_OBB_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_ACS_S_E_"+y, "SPESE_ACS_S_E_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_ACS_S_G_"+y, "SPESE_ACS_S_G_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_ACS_S_C_"+y, "SPESE_ACS_S_C_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_ACS_S_P_"+y, "SPESE_ACS_S_P_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_ACS_S_UC_"+y, "SPESE_ACS_S_UC_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_ACS_S_TOT_"+y, "SPESE_ACS_S_TOT_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            form.renameField("SPESE_TOTALI_S_GL_TOT_"+y, "SPESE_TOTALI_S_GL_TOT_"+(y+righePerPaginaSpese*(numeroNuovo)));
	            
        	}		       
	      
	        stamper.close();
	        reader.close();
	        reader = new PdfReader(dest);
	        } catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}
	
	
	
	
	
	
	private void rinominaCampiPagina1Utenza(Integer numero, String alias) throws DocumentException {
		
		try {
			String src=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RipartizioniUni102002018-Utenza-1"+alias+".pdf";
			String dest=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RipartizioniUni102002018-Utenza-1-"+numero+".pdf";
			
	        PdfReader reader;
			reader = new PdfReader(src);
			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
	        AcroFields form = stamper.getAcroFields();
	        
	        Integer numeroRiga=numero+1;
        	form.renameField("C_DENOMINAZIONE", "DENOMINAZIONE_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_INTERNO", "INTERNO_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_SCALA", "SCALA_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_PIANO", "PIANO_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	
        	form.renameField("C_LETTURA", "LETTURA_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_LETTURA_ACS", "LETTURA_ACS_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_MILLESIMI", "MILLESIMI_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	
        	form.renameField("C_CONSUMO_RISCALDAMENTO_Q_VOL", "CONSUMO_RISCALDAMENTO_Q_VOL_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_CONSUMO_RISCALDAMENTO_Q_INV", "CONSUMO_RISCALDAMENTO_Q_INV_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_CONSUMO_RISCALDAMENTO_Q_TOT", "CONSUMO_RISCALDAMENTO_Q_TOT_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_MILLESIMI_CLIMA", "MILLESIMI_CLIMA_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_SPESE_RISCALDAMENTO_S_VOL", "SPESE_RISCALDAMENTO_S_VOL_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_SPESE_RISCALDAMENTO_S_INV", "SPESE_RISCALDAMENTO_S_INV_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_SPESE_RISCALDAMENTO_S_E", "SPESE_RISCALDAMENTO_S_E_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_SPESE_RISCALDAMENTO_S_G", "SPESE_RISCALDAMENTO_S_G_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_SPESE_RISCALDAMENTO_S_C", "SPESE_RISCALDAMENTO_S_C_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_SPESE_RISCALDAMENTO_S_P", "SPESE_RISCALDAMENTO_S_P_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_SPESE_RISCALDAMENTO_S_TOT", "SPESE_RISCALDAMENTO_S_TOT_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	
        	form.renameField("C_CONSUMO_ACS_Q_VOL", "CONSUMO_ACS_Q_VOL_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_CONSUMO_ACS_Q_INV", "CONSUMO_ACS_Q_INV_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_CONSUMO_ACS_Q_TOT", "CONSUMO_ACS_Q_TOT_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_MILLESIMI_ACS", "MILLESIMI_ACS_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_SPESE_ACS_S_VOL", "SPESE_ACS_S_VOL_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_SPESE_ACS_S_INV", "SPESE_ACS_S_INV_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_SPESE_ACS_S_E", "SPESE_ACS_S_E_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_SPESE_ACS_S_G", "SPESE_ACS_S_G_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_SPESE_ACS_S_C", "SPESE_ACS_S_C_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_SPESE_ACS_S_P", "SPESE_ACS_S_P_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	form.renameField("C_SPESE_ACS_S_TOT", "SPESE_ACS_S_TOT_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        	
        	form.renameField("C_SPESE_TOTALI_S_GL_TOT", "SPESE_TOTALI_S_GL_TOT_"+(numeroRiga)+"_"+(numeroRiga)+"_"+(numeroRiga));
        		

	        stamper.close();
	        reader.close();
	        reader = new PdfReader(dest);
	        } catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
		}
	}
	
	
	private void rinominaCampiPagina2Utenza(Integer numero, String alias) throws DocumentException {
		
		try {
			String src=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RipartizioniUni102002018-Utenza-2"+alias+".pdf";
			String dest=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RipartizioniUni102002018-Utenza-2-"+numero+".pdf";
			
	        PdfReader reader;
			reader = new PdfReader(src);
			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
	        AcroFields form = stamper.getAcroFields();
	        
	        Integer numeroRiga=numero+1;
        	
        	for (int i=1;i<43;i++){
        		
        		form.renameField("STANZA_1_"+i, "STANZA_"+(numeroRiga)+"_"+i);
        		form.renameField("RILEVATORE_1_"+i, "RILEVATORE_"+(numeroRiga)+"_"+i);
        		form.renameField("SERVIZIO_1_"+i, "SERVIZIO_"+(numeroRiga)+"_"+i);
        		form.renameField("CONTATORE_LETTURA_1_"+i, "CONTATORE_LETTURA_"+(numeroRiga)+"_"+i);
            	form.renameField("RIPARTITORE_LETTURA_1_"+i, "RIPARTITORE_LETTURA_"+(numeroRiga)+"_"+i);
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
        
        if (acroFieldName.equals("LOCALITA")){
        	String cap= (String) map.get("CAP");
        	String localita= (String) map.get("LOCALITA");
        	String provincia= (String) map.get("PROVINCIA");
        	String comune=cap+"    "+localita+" ("+provincia.toUpperCase()+")";
    		setAcroFieldValue("COMUNE", comune);
        }
        
        
        if (acroFieldName.equals("TECNICO")){
        	String indirizzoTecnico= (String) map.get("INDIRIZZO_TECNICO");
        	String localitaTecnico= (String) map.get("LOCALITA_TECNICO");
        	String indirizzoTecnicoCompleto="";
        	
        	if (indirizzoTecnico!=null && localitaTecnico!=null){
       			indirizzoTecnicoCompleto=indirizzoTecnico+" \r"+localitaTecnico;
        	}
        	
        	setAcroFieldValue("INDIRIZZO_COMPLETO_TECNICO", indirizzoTecnicoCompleto);
        }
        
        if (acroFieldName.equals("DENOMINAZIONE_STUDIO")){
        	String indirizzoStudio= (String) map.get("INDIRIZZO_STUDIO");
        	String localitaStudio= (String) map.get("LOCALITA_STUDIO");
        	String indirizzoStudioCompleto="";
        	
        	if (indirizzoStudio!=null && localitaStudio!=null){
       			indirizzoStudioCompleto=indirizzoStudio+" \r"+localitaStudio;
        	}
        	
        	setAcroFieldValue("INDIRIZZO_COMPLETO_STUDIO", indirizzoStudioCompleto);
        }
        
             
        //setAcroFieldValue("DATA_OGGI", Utils.getStringDataOggi());
        
    }
    
    
    /*private void testSpostamento() throws DocumentException {
		try {
        String src=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RipartizioniUni102002018-Consuntivo-2.pdf";
        String dest=getOutDirectory().replace(WebAppConstants_itf.OUTPUT_PATH, WebAppConstants_itf.TEMPLATES_PATH)+"RipartizioniUni102002018-Consuntivo-2-test.pdf";
        
        PdfReader reader;
		reader = new PdfReader(src);
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        AcroFields form = stamper.getAcroFields();
       
        Item item = form.getFieldItem("timezone2");
        PdfDictionary widget = item.getWidget(0);
        PdfArray rect = widget.getAsArray(PdfName.RECT);
        
        //llx, lly, urx and ury
        //rect.set(0, new PdfNumber(rect.getAsNumber(0).floatValue() + 50f));
        rect.set(1, new PdfNumber(rect.getAsNumber(1).floatValue() - 50f));
        
        
        //rect.set(2, new PdfNumber(rect.getAsNumber(2).floatValue() + 50f));
        rect.set(3, new PdfNumber(rect.getAsNumber(3).floatValue() - 50f));
        
        
       
      
        stamper.close();
        reader.close();
        reader = new PdfReader(dest);
        } catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}*/
	
}
