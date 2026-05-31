
package net.projectsrl.wera.anagrafiche.stampe.core;

import java.util.HashMap;
import java.util.Map;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.projectsrl.bow.pdf.FunctionStampaPDFGeneric_base;

public class FunctionStampaAnagraficaUtenze extends FunctionStampaPDFGeneric_base<CreateAnagraficaUtenzePdf> {

    public FunctionStampaAnagraficaUtenze() {

        super();
    }

    public FunctionStampaAnagraficaUtenze(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    protected void setData(SsbServletRequest req, Map<String, Object> data) throws AppCrash {

        HashMap<String, String> param = new HashMap<String, String>();
        String idModulo = req.getField("ID_MODULO");
        param.put("WHERECONDITION", " WHERE UD.id_modulo =" + idModulo);
        param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
        param.put("WHERECONDITION_PROFILI", " IS NOT NULL and UTENZE.id_modulo="+ idModulo);

        fillDataFromSingleRowDataSet("DSUtenze", data, param);
        
        
        DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        Integer conta=0;
        Integer contaDettaglio=0;
        Boolean specific=false;

        try {
            dataSet = dsFactory.makeDataSet("", "DSUtenzeDettaglio");
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
           		conta=conta+1;
            	fillMapFromRowRipartizioni(data, dbRow, dataSet.getColumnNames(),conta,contaDettaglio,specific);
                
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),"Errore nella ricerca dell'ultimo progressivo del dataset " + "DSUtenzeDettaglio");
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DSUtenzeDettaglio");
                }
            }
        }
        


    }

}
