
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
import net.projectsrl.wm.utils.Utils;

public class FunctionStampaAnagraficaCondomini extends FunctionStampaPDFGeneric_base<CreateStampaAnagraficaCondominiPdf> {

    public FunctionStampaAnagraficaCondomini() {

        super();
    }

    public FunctionStampaAnagraficaCondomini(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    protected void setData(SsbServletRequest req, Map<String, Object> data) throws AppCrash {

        HashMap<String, String> param = new HashMap<String, String>();
        String idModulo = req.getField("ID_MODULO");
        param.put("WHERECONDITION", " WHERE CO.ID_MODULO=" + idModulo);
        param.put("WHERECONDITION_AZIENDE", " IS NOT NULL ");
        param.put("WHERECONDITION_CONDOMINIO", " WHERE ID_CONDOMINIO=" + idModulo);
        
        fillDataFromSingleRowDataSet("DataSetAnagraficheCondominiStampa", data, param);
        data.put("DATA_ELABORAZIONE", Utils.getStringDataOggi());
        
        
        
        
        
        DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        Integer conta=0;
        Integer idCondomino=0;
        Integer contaDettaglio=0;
        Boolean specific=false;

        try {
            dataSet = dsFactory.makeDataSet("", "DSUtenzeDettaglioCondomini");
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSet.nextElement();
            	if (idCondomino.equals((Integer) dbRow.getField("ID_MODULO"))){
            		contaDettaglio=contaDettaglio+1;
            	}else{
            		contaDettaglio=1;
            		conta=conta+1;
            	}
        		idCondomino=(Integer) dbRow.getField("ID_MODULO");
            	
            	fillMapFromRowRipartizioni(data, dbRow, dataSet.getColumnNames(),conta,contaDettaglio,specific);
                
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),"Errore nella ricerca dell'ultimo progressivo del dataset " + "DSUtenzeDettaglioCondomini");
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DSUtenzeDettaglioCondomini");
                }
            }
        }
        
        
        
        
        DataSet_itf dataSetTotali = null;
        DataSetFactory dsFactoryTotali = DataSetFactory.getInstance();
        String matTubo="";
        String diametro="";
        String totale="";
        data.put("NON_SPECIFICATO","0");
        data.put("NON_SPECIFICATO_MATERIALE","0");
        data.put("NON_SPECIFICATO_DIAMETRO","0");

        try {
        	dataSetTotali = dsFactoryTotali.makeDataSet("", "DataSetAnagraficheCondominiStampaTotali");
            dataSetTotali.setParam(param);
            dataSetTotali.open();

            while (dataSetTotali.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSetTotali.nextElement();
            	matTubo = (String) dbRow.getField("MAT_TUBO");
            	diametro = (String) dbRow.getField("DIAMETRO");
            	totale = dbRow.getField("TOTALE").toString();
            	
            	if(!matTubo.trim().isEmpty() && !diametro.trim().isEmpty()){
            		data.put(matTubo+"_"+diametro, totale);
            	}else{
            		if(matTubo.trim().isEmpty() && diametro.trim().isEmpty()){
            			data.remove("NON_SPECIFICATO");
            			data.put("NON_SPECIFICATO",totale);
                	}else{
                		if(matTubo.trim().isEmpty()){
                			data.remove("NON_SPECIFICATO_MATERIALE");
                			data.put("NON_SPECIFICATO_MATERIALE", totale);
                		}
                		if(diametro.trim().isEmpty()){
                			data.remove("NON_SPECIFICATO_DIAMETRO");
                			data.put("NON_SPECIFICATO_DIAMETRO", totale);
                		}
                	}
            	}
            
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),"Errore nella ricerca dell'ultimo progressivo del dataset " + "DataSetAnagraficheCondominiStampaTotali");
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSetTotali != null) {
                try {
                	dataSetTotali.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DataSetAnagraficheCondominiStampaTotali");
                }
            }
        }
        
        
        
        
        DataSet_itf dataSetContatori = null;
        DataSetFactory dsFactoryContatori = DataSetFactory.getInstance();
        String tipologia="";
        String contatore="";

        try {
        	dataSetContatori = dsFactoryContatori.makeDataSet("", "DataSetAnagraficheCondominiStampaContatori");
            dataSetContatori.setParam(param);
            dataSetContatori.open();

            while (dataSetContatori.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSetContatori.nextElement();
            	tipologia = (String) dbRow.getField("ID_TIPOLOGIA");
            	totale = dbRow.getField("TOTALE").toString();
            	contatore = (String) dbRow.getField("CONTATORE");
            	data.put("CONTATORE_"+tipologia+"_QTA", totale);
            	data.put("CONTATORE_"+tipologia, contatore);
                
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),"Errore nella ricerca dell'ultimo progressivo del dataset " + "DataSetAnagraficheCondominiStampaContatori");
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSetContatori != null) {
                try {
                	dataSetContatori.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DataSetAnagraficheCondominiStampaContatori");
                }
            }
        }
        
        
        
        DataSet_itf dataSetAntenne = null;
        DataSetFactory dsFactoryAntenne = DataSetFactory.getInstance();
        String antenna="";
        String gateway="";
        String totaleRiga="";
        String riga="";

        try {
        	dataSetAntenne = dsFactoryAntenne.makeDataSet("", "DataSetAnagraficheCondominiStampaAntenne");
            dataSetAntenne.setParam(param);
            dataSetAntenne.open();

            while (dataSetAntenne.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSetAntenne.nextElement();
            	riga = dbRow.getField("RIGA").toString();
            	antenna = (String) dbRow.getField("ANTENNA");
            	gateway = (String) dbRow.getField("GATEWAY");
            	totaleRiga = dbRow.getField("TOTALE").toString();
            	data.put("RIGA_"+riga+"_ANTENNA", antenna);
            	data.put("RIGA_"+riga+"_GATEWAY", gateway);
            	data.put("RIGA_"+riga+"_TOTALE", totaleRiga);
                
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),"Errore nella ricerca dell'ultimo progressivo del dataset " + "DataSetAnagraficheCondominiStampaAntenne");
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSetAntenne != null) {
                try {
                	dataSetAntenne.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DataSetAnagraficheCondominiStampaAntenne");
                }
            }
        }
        
        
        DataSet_itf dataSetPotenza = null;
        DataSetFactory dsFactoryPotenza = DataSetFactory.getInstance();
        String potenza="";

        try {
        	dataSetPotenza = dsFactoryPotenza.makeDataSet("", "DataSetAnagraficheCondominiStampaPotenza");
            dataSetPotenza.setParam(param);
            dataSetPotenza.open();

            while (dataSetPotenza.hasMoreElements()) {
            	Row_itf dbRow = (Row_itf) dataSetPotenza.nextElement();
            	potenza = dbRow.getField("TOTALE_POTENZA").toString();
            	data.put("POTENZA_TOTALE",potenza);
                
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),"Errore nella ricerca dell'ultimo progressivo del dataset " + "DataSetAnagraficheCondominiStampaPotenza");
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSetPotenza != null) {
                try {
                	dataSetPotenza.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DataSetAnagraficheCondominiStampaPotenza");
                }
            }
        }


    }

}
