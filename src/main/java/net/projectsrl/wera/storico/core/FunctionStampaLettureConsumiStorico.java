
package net.projectsrl.wera.storico.core;

import java.util.HashMap;
import java.util.Map;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.projectsrl.bow.pdf.FunctionStampaPDFGeneric_base;
import net.projectsrl.wm.utils.Utils;

public class FunctionStampaLettureConsumiStorico extends FunctionStampaPDFGeneric_base<CreateLettureConsumiStoricoPdf> {

    public FunctionStampaLettureConsumiStorico() {

        super();
    }

    public FunctionStampaLettureConsumiStorico(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    protected void setData(SsbServletRequest req, Map<String, Object> data) throws AppCrash {

        HashMap<String, String> param = new HashMap<String, String>();
        String idCondominio = req.getField("ID_CONDOMINIO");
        String dataInizioStatistica = req.getField("DATA_DI_LETTURA");
        param.put("WHERECONDITION", " WHERE SCARICO.ID_CONDOMINIO =" + idCondominio + " AND DATA_DI_LETTURA='"+Utils.ribaltaData(dataInizioStatistica)+"'");
        param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
        param.put("WHERECONDITION_PROFILI_LETTURE", " IS NOT NULL ");

        fillDataFromSingleRowDataSet("DSLettureConsumiCondominiDettaglioStoricoStampa", data, param);
        data.put("DATA_OGGI", Utils.getStringDataOggi());
        
        
        DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        Integer conta=0;
        Integer idCondomino=0;
        Integer contaDettaglio=0;
        Boolean specific=false;

        try {
            dataSet = dsFactory.makeDataSet("", "DSLettureConsumiCondominiDettaglioStoricoStampa");
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
           		conta=conta+1;
        		idCondomino=(Integer) dbRow.getField("ID_CONDOMINO");
            	
            	fillMapFromRowRipartizioni(data, dbRow, dataSet.getColumnNames(),conta,contaDettaglio,specific);
                
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),"Errore nella ricerca dell'ultimo progressivo del dataset " + "DSLettureConsumiCondominiDettaglioStoricoStampa");
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
        


    }

}
