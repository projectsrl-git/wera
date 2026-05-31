
package net.projectsrl.wera.ripartizioniuni2018.core;

import java.util.HashMap;
import java.util.Map;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.projectsrl.bow.pdf.FunctionStampaPDFGeneric_base;

public class FunctionStampaRipartizioniUNI2018 extends FunctionStampaPDFGeneric_base<CreateRipartizioniUNI2018Pdf> {

	public static final String RIPARTIZIONI_UNI_2018_STAMPA = "DSRipartizioniUNI2018Stampa";
	public static final String RIPARTIZIONI_UNI_2018_DETTAGLIO_LOCATARI_STAMPA = "DSRipartizioniUNI2018DettaglioLocatariStampa";
			
    public FunctionStampaRipartizioniUNI2018() {

        super();
    }

    public FunctionStampaRipartizioniUNI2018(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    protected void setData(SsbServletRequest req, Map<String, Object> data) throws AppCrash {

        HashMap<String, String> param = new HashMap<String, String>();
        String idModulo = req.getField("ID_MODULO");
        param.put("WHERECONDITION", " WHERE RI.ID_MODULO=" + idModulo);
        param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
        
        fillDataFromSingleRowDataSet(RIPARTIZIONI_UNI_2018_STAMPA, data, param);
        
        DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        Integer conta=0;
        Integer idCondomino=0;
        Integer contaDettaglio=0;
        Boolean specific=false;
        

        try {
            dataSet = dsFactory.makeDataSet("", RIPARTIZIONI_UNI_2018_DETTAGLIO_LOCATARI_STAMPA);
            param.put("WHERECONDITION", " WHERE RI.ID_MODULO=" + idModulo+ " AND TIPO_UTENZA='000'");
            param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
            param.put("ID_MODULO", idModulo);
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	if (idCondomino.equals((Integer) dbRow.getField("ID_CONDOMINO"))){
            		//contaDettaglio=1;
            		contaDettaglio=contaDettaglio+1;
            	}else{
            		contaDettaglio=1;
            		conta=conta+1;
            	}
        		idCondomino=(Integer) dbRow.getField("ID_CONDOMINO");
            	
            	fillMapFromRowRipartizioni(data, dbRow, dataSet.getColumnNames(),conta,contaDettaglio,specific);
                
            }
            
            //locali uso comune (consumi)
            dataSet.close();
        	param.put("WHERECONDITION", " WHERE RI.ID_MODULO=" + idModulo+ " AND TIPO_UTENZA='001'");
            param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
            dataSet.setParam(param);
            dataSet.open();
            while (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	if (idCondomino.equals((Integer) dbRow.getField("ID_CONDOMINO"))){
            		//contaDettaglio=1;
            		contaDettaglio=contaDettaglio+1;
            	}else{
            		contaDettaglio=1;
            		conta=conta+1;
            	}
        		idCondomino=(Integer) dbRow.getField("ID_CONDOMINO");
            	
            	fillMapFromRowRipartizioni(data, dbRow, dataSet.getColumnNames(),conta,contaDettaglio,specific);
            }
            
            
            
         // locali uso comune - pagina dedicata (spese) 
            conta=0;
            idCondomino=0;
            contaDettaglio=0;
            specific=true;
        	dataSet.close();
        	param.put("WHERECONDITION", " WHERE RI.ID_MODULO=" + idModulo+ " AND TIPO_UTENZA='001'");
            param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
            dataSet.setParam(param);
            dataSet.open();
            
            while (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	if (idCondomino.equals((Integer) dbRow.getField("ID_CONDOMINO"))){
            		//contaDettaglio=1;
            		contaDettaglio=contaDettaglio+1;
            	}else{
            		contaDettaglio=1;
            		conta=conta+1;
            	}
        		idCondomino=(Integer) dbRow.getField("ID_CONDOMINO");
            	
            	fillMapFromRowRipartizioni(data, dbRow, dataSet.getColumnNames(),conta,contaDettaglio,specific);
                
            }
            
            
            
            
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),"Errore nella ricerca dell'ultimo progressivo del dataset " + "DSRipartizioniUNI2018DettaglioLettureStampa");
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DSRipartizioniUNIDettaglioLettureStampa");
                }
            }
        }
        
    }
    
}


