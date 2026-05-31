
package net.projectsrl.wera.ripartizioniletture.core;

import java.util.HashMap;
import java.util.Map;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.projectsrl.bow.pdf.FunctionStampaPDFGeneric_base;

public class FunctionStampaRipartizioniLetture extends FunctionStampaPDFGeneric_base<CreateRipartizioniLetturePdf> {

    public FunctionStampaRipartizioniLetture() {

        super();
    }

    public FunctionStampaRipartizioniLetture(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    protected void setData(SsbServletRequest req, Map<String, Object> data) throws AppCrash {

        HashMap<String, String> param = new HashMap<String, String>();
        String idModulo = req.getField("ID_MODULO");
        param.put("WHERECONDITION", " WHERE RI.ID_MODULO=" + idModulo);
        param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
        
        fillDataFromSingleRowDataSet("DSRipartizioniLettureStampa", data, param);
        
        DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        Integer conta=0;
        Integer idCondomino=0;
        Integer contaDettaglio=0;
        Boolean specific=false;

        try {
            dataSet = dsFactory.makeDataSet("", "DSRipartizioniLettureDettaglioStampa");
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	if (idCondomino.equals((Integer) dbRow.getField("ID_CONDOMINO"))){
            		contaDettaglio=contaDettaglio+1;
            	}else{
            		contaDettaglio=1;
            		conta=conta+1;
            	}
        		idCondomino=(Integer) dbRow.getField("ID_CONDOMINO");
            	
            	fillMapFromRowRipartizioni(data, dbRow, dataSet.getColumnNames(),conta,contaDettaglio,specific);
                
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
        


    }

}
