
package net.projectsrl.wera.ripartizioniletture.core;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.qhse.moduli.core.FunctionInserimentoALIMOD_base;
import net.projectsrl.webapp.authentication.MenuItem;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.wera.base.db.AliModDAO_base;
import net.projectsrl.wera.ripartizioniletture.db.RipartizioniLettureDAO;
import net.projectsrl.wera.ripartizioniletture.db.RipartizioniLettureDettagliDAO;
import net.projectsrl.wera.ripartizioniletture.db.RipartizioniLettureDettaglioSingoloDAO;
import net.projectsrl.wera.ripartizioniuni.db.RipartizioniUNIDAO;
import net.projectsrl.wera.utils.WeraUtils;

public class FunctionInserimentoRipartizioniLetture extends FunctionInserimentoALIMOD_base<RipartizioniLettureDAO> {
	
     private static final String DATASET_CONSUMI_TOTALI_SINGOLI_LETTURE = "DataSetConsumiTotaliSingoliLetture";

    public FunctionInserimentoRipartizioniLetture(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(RipartizioniLettureDAO.ID_MODULO, "");
            templateData.put(RipartizioniLettureDAO.DT_MODULO, project.misc.Utils.getStringDataOggi());
            templateData.put(RipartizioniLettureDAO.ID_UTENTE_INS, getSpecificUserInfo(userInfo).getIdUtente());
            templateData.put("SALVATAGGIO_IN_CORSO",false);
           
        } else {
            PjNDAO_base rowToUpdate = new RipartizioniLettureDAO();
            String idRow = req.getField(RipartizioniLettureDAO.ID_MODULO);
            rowToUpdate.setAttribute(RipartizioniLettureDAO.ID_MODULO, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), RipartizioniLettureDAO.ID_MODULO + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
            templateData.put("SALVATAGGIO_IN_CORSO",rowToUpdate.getAttribute(RipartizioniUNIDAO.SALVATAGGIO_IN_CORSO));
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

    @Override
    protected boolean isAnInsert(SsbServletRequest req) {

        return Util.IsEmpty(req.getField(RipartizioniLettureDAO.ID_MODULO));
    }
    
    
    @Override
    protected void sendResponseJSON(SsbServletResponse res, boolean result, String title, String message, RipartizioniLettureDAO formDao) {

        try {
            PrintWriter out = res.getWriter();

            Integer id = (Integer) formDao.getAttribute(AliModDAO_base.ID_MODULO);
            String nr = (String) formDao.getAttribute(AliModDAO_base.NR_MODULO);
            String stato = (String) formDao.getAttribute(AliModDAO_base.STATO);
            
            WeraUtils.setRipartizioneSalvataggio("RIPARTIZIONI_LETTURE",true,id);
            
            WeraUtils.eliminaDatiRilevatoriDuplicati();
            eliminaRipartizioneEsistente(formDao,id);
            eliminaRipartizioneEsistenteSingolo(formDao,id);
            creaRipartizione(formDao);
            
            String resultString = "{\"result\":" + result + ",\"title\":'" + title + "',\"message\":'" + message
                    + "',\"id\":" + id + ",\"nr\":'" + nr + "',\"stato\":'" + stato + "'}";
            
            
            WeraUtils.setRipartizioneSalvataggio("RIPARTIZIONI_LETTURE",false,id);
            out.println(resultString);
            out.close();

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "errore writing succesful response");
        }
    }
    
    private void eliminaRipartizioneEsistenteSingolo(RipartizioniLettureDAO formDao, Integer id) throws NumberFormatException, AppCrash {
		 DataSet_itf dataSet = null;
       DataSetFactory dsFactory = DataSetFactory.getInstance();
       
       Integer idDettaglio;
       
       try {
           dataSet = dsFactory.makeDataSet("", "DSRipartizioniLettureCondominioSingolo");

           HashMap<String, String> param = new HashMap<String, String>();
           param.put("WHERECONDITION", "WHERE ID_MODULO="+id);
           param.put("WHERECONDITION_PROFILI_LETTURE", " IS NOT NULL ");
           dataSet.setParam(param);
           dataSet.open();

           while (dataSet.hasMoreElements()) {
               Row_itf dbRow = (Row_itf) dataSet.nextElement();

               idDettaglio= (Integer) dbRow.getField("ID_DETTAGLIO");
               RipartizioniLettureDettaglioSingoloDAO rd = new RipartizioniLettureDettaglioSingoloDAO();
          	 rd.setAttribute(RipartizioniLettureDettaglioSingoloDAO.ID_DETTAGLIO,idDettaglio);
          	 rd.delete();
               
           }
       } catch (AppCrash ac) {
           ac.logContext(this.getClass().getName(),
                   "Errore nella ricerca dell'ultimo progressivo del dataset " + "DSDatiRipartizioni");
           throw ac;
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
    
    
    private void eliminaRipartizioneEsistente(RipartizioniLettureDAO formDao, Integer id) throws NumberFormatException, AppCrash {
		 DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        
        Integer idDettaglio;
        
        try {
            dataSet = dsFactory.makeDataSet("", "DSRipartizioniLettureCondominio");

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("WHERECONDITION", "WHERE ID_MODULO="+id);
            param.put("WHERECONDITION_PROFILI_LETTURE", " IS NOT NULL ");
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                idDettaglio= (Integer) dbRow.getField("ID_DETTAGLIO");
                RipartizioniLettureDettagliDAO rd = new RipartizioniLettureDettagliDAO();
           	 rd.setAttribute(RipartizioniLettureDettagliDAO.ID_DETTAGLIO,idDettaglio);
           	 rd.delete();
                
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),
                    "Errore nella ricerca dell'ultimo progressivo del dataset " + "DSDatiRipartizioni");
            throw ac;
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
    
    
   
    @SuppressWarnings("unused")
	private void creaRipartizione(RipartizioniLettureDAO formDao) throws NumberFormatException, AppCrash {

    	Integer idModulo = (Integer) formDao.getAttribute(RipartizioniLettureDAO.ID_MODULO);
    	Integer stabile = (Integer) formDao.getAttribute(RipartizioniLettureDAO.ID_CONDOMINIO);
        String dataDal = (String) formDao.getAttributeAsString(RipartizioniLettureDAO.DATA_DAL);
        String dataAl = (String) formDao.getAttributeAsString(RipartizioniLettureDAO.DATA_AL);
        Integer idAzienda = (Integer) formDao.getAttribute(RipartizioniLettureDAO.ID_AZIENDA);
        
    	String tipoRipartizione = (String) formDao.getAttributeAsString(RipartizioniLettureDAO.TIPO_RIPARTIZIONE).trim();
    	String tipoContabilizzazione = (String) formDao.getAttributeAsString(RipartizioniLettureDAO.TIPO_CONTABILIZZAZIONE).trim();
    	
    	//Float generatoreConsumo=  Float.parseFloat(formDao.getAttributeAsString(RipartizioniLettureDAO.GENERATORE_CONSUMO));
    	//Float generatoreConsumoACS=  Float.parseFloat(formDao.getAttributeAsString(RipartizioniLettureDAO.GENERATORE_CONSUMO_ACS));
    	
    	//Float generatoreFabbisognoClima=  Float.parseFloat(formDao.getAttributeAsString(RipartizioniLettureDAO.GENERATORE_FABBISOGNO_CLIMA));
    	//Float generatoreFabbisognoACS=  Float.parseFloat(formDao.getAttributeAsString(RipartizioniLettureDAO.GENERATORE_FABBISOGNO_ACS));
    	
    	String datasetGeneratore="";
    	Float generatoreFabbisognoClima=(float) 0;
    	Float generatoreFabbisognoACS=(float) 0;
    	
    	solaLettura(formDao, idModulo, stabile, dataDal, dataAl,idAzienda,generatoreFabbisognoClima,generatoreFabbisognoACS);
    	
    	//Integer differenzaPercentualeLettura=differenzaPercentuale(formDao, idModulo, stabile, dataDal, dataAl,idAzienda)[0];
    	//Integer differenzaPercentualeLetturaACS=differenzaPercentuale(formDao, idModulo, stabile, dataDal, dataAl,idAzienda)[1];
    	
        
    }
    
    
    

	private int[] differenzaPercentuale(RipartizioniLettureDAO formDao, Integer idModulo, Integer stabile,
			String dataDal, String dataAl, Integer idAzienda) throws AppCrash {

		int[] differenza = new int[2];
		 DataSet_itf dataSet = null;
	        DataSetFactory dsFactory = DataSetFactory.getInstance();
		try {
            dataSet = dsFactory.makeDataSet("", "DSRipartizioniDifferenzaLetturaPercentuale");

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("ID_CONDOMINIO", stabile.toString());
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                differenza[0]= (Integer) dbRow.getField("DIFFERENZA_LETTURA_PERCENTUALE");
                differenza[1]= (Integer) dbRow.getField("DIFFERENZA_LETTURA_ACS_PERCENTUALE");
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),
                    "Errore nella ricerca dell'ultimo progressivo del dataset " + "DSDatiRipartizioni");
            throw ac;
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
		
		return differenza;
		
	}

	private void ripartizioniSingolo(Integer idModulo, Integer stabile, String dataDal, String dataAl, String idCondomino, DataSetFactory dsFactory) throws AppCrash {
		String denominazione;
		double letturaCondomino;
		double letturaCondominoACS;
		
		// ripartizioni singolo inizio
		    DataSet_itf dataSet2 = null;
		    DataSetFactory dsFactory2 = DataSetFactory.getInstance();
		    dsFactory2 = DataSetFactory.getInstance();
		    String stanza="";
		    String rilevatore="";
		    String numeroRipartitori="";
		    try {
		        dataSet2 = dsFactory2.makeDataSet("", "DSDatiRipartizioniLettureSingolo");

		        HashMap<String, String> param2 = new HashMap<String, String>();
		        param2.put("ID_CONDOMINIO", stabile.toString());
		        param2.put("ID_CONDOMINO", idCondomino.toString());
		        param2.put("DATA_DAL", dataDal);
		        param2.put("DATA_AL", dataAl);
		        dataSet2.setParam(param2);
		        dataSet2.open();

		        while (dataSet2.hasMoreElements()) {
		            Row_itf dbRow2 = (Row_itf) dataSet2.nextElement();

		            //idCondomino= (Integer) dbRow2.getField("ID_CONDOMINO"); già definito prima
		            denominazione = (String) dbRow2.getField("DENOMINAZIONE");
		            letturaCondomino = (Integer) dbRow2.getField("LETTURA_CONDOMINO");
		            letturaCondominoACS = Float.parseFloat(dbRow2.getField("LETTURA_ACS_CONDOMINO").toString());
		            stanza= (String) dbRow2.getField("STANZA");
		            rilevatore= (String) dbRow2.getField("RILEVATORE");
		            numeroRipartitori=dbRow2.getField("RILEVATORI_CONDOMINO").toString().trim();
		            
		            RipartizioniLettureDettaglioSingoloDAO rds = new RipartizioniLettureDettaglioSingoloDAO();
		            
		        	 rds.setAttribute(RipartizioniLettureDettaglioSingoloDAO.ID_MODULO,idModulo);
		        	 rds.setAttribute(RipartizioniLettureDettaglioSingoloDAO.ID_CONDOMINO,idCondomino);
					 rds.setAttribute(RipartizioniLettureDettaglioSingoloDAO.DENOMINAZIONE,denominazione);
					 rds.setAttribute(RipartizioniLettureDettaglioSingoloDAO.LETTURA,WeraUtils.doubleToString(letturaCondomino,2));
					 rds.setAttribute(RipartizioniLettureDettaglioSingoloDAO.LETTURA_ACS,WeraUtils.doubleToString(letturaCondominoACS,2));
					 rds.setAttribute(RipartizioniLettureDettaglioSingoloDAO.STANZA,stanza);
					 rds.setAttribute(RipartizioniLettureDettaglioSingoloDAO.RILEVATORE,rilevatore);
					 rds.setAttribute(RipartizioniLettureDettaglioSingoloDAO.NUMERO_RIPARTITORI,numeroRipartitori);
					 
					 rds.setAttribute(RipartizioniLettureDettaglioSingoloDAO.ID_UTENTE_INS,0);
					 rds.insert();
		            
		        }
		    } catch (AppCrash ac) {
		        ac.logContext(this.getClass().getName(),
		                "Errore nella ricerca dell'ultimo progressivo del dataset " + "DSDatiRipartizioni");
		        throw ac;
		    } finally {
		        // chiude il dataset per il conteggio degli elementi trovati
		        if (dataSet2 != null) {
		            try {
		                dataSet2.close();
		            } catch (Throwable t) {
		                AppCrash ac = new AppCrash(t);
		                ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DSDatiRipartizioni");
		            }
		        }
		    }
		    // ripartizioni singolo fine
	}
    
    
    
    
private void solaLettura(RipartizioniLettureDAO formDao, Integer idModulo, Integer stabile, String dataDal, String dataAl, Integer idAzienda, Float generatoreFabbisognoClima, Float generatoreFabbisognoACS) throws AppCrash {
        
        // DETTAGLI RIPARTIZIONE FASE 1- INIZIO
       
        String idCondomino="";
        String denominazione="";
        
        String numeroRipartitori="";
        
        double M_qh_cli_1=0;
        double M_qh_acs_1=0;
        
        double letturaCondomino=0;
        double letturaCondominoACS=0;
        double letturaCondominoAFS=0;
        double letturaTotale=0;
        double letturaTotaleACS=0;
        double letturaTotaleAFS=0;
      
        double totaleMillesimiRiscaldamento=0;
        double totaleMillesimiACS=0;

        RipartizioniLettureDettagliDAO ripartizioniletturedettaglio = new RipartizioniLettureDettagliDAO();
        
        
        DataSet_itf dataSet = null;
        try {

            DataSetFactory dsFactory = DataSetFactory.getInstance();

            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_CONSUMI_TOTALI_SINGOLI_LETTURE);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("STABILE", stabile.toString());
            params.put("DATA_DAL", dataDal);
            params.put("DATA_AL", dataAl);
            dataSet.setParam(params);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                idCondomino = dbRow.getField("ID_CONDOMINO").toString().trim();
                denominazione = dbRow.getField("DENOMINAZIONE").toString().trim();
                letturaCondominoACS=Float.valueOf(dbRow.getField("LETTURA_CONDOMINO_ACS").toString().trim());
                letturaCondominoAFS=Float.valueOf(dbRow.getField("LETTURA_CONDOMINO_AFS").toString().trim());
                letturaCondomino = Float.valueOf(dbRow.getField("LETTURA_CONDOMINO").toString().trim());
                M_qh_cli_1 = Float.valueOf(dbRow.getField("MILLESIMI_CLIMA").toString().trim());
                M_qh_acs_1 = Float.valueOf(dbRow.getField("MILLESIMI_ACS").toString().trim());
                numeroRipartitori=dbRow.getField("NUMERO_RIPARTITORI").toString().trim();
               
                
                
                ripartizioniletturedettaglio.setAttribute(RipartizioniLettureDettagliDAO.ID_MODULO, idModulo);
                ripartizioniletturedettaglio.setAttribute(RipartizioniLettureDettagliDAO.ID_UTENTE_INS, 0);
                ripartizioniletturedettaglio.setAttribute(RipartizioniLettureDettagliDAO.ID_AZIENDA, idAzienda);
                ripartizioniletturedettaglio.setAttribute(RipartizioniLettureDettagliDAO.ID_CONDOMINO, idCondomino);
                ripartizioniletturedettaglio.setAttribute(RipartizioniLettureDettagliDAO.DENOMINAZIONE, denominazione);
                ripartizioniletturedettaglio.setAttribute(RipartizioniLettureDettagliDAO.LETTURA, WeraUtils.doubleToString(letturaCondomino,0));
                ripartizioniletturedettaglio.setAttribute(RipartizioniLettureDettagliDAO.LETTURA_ACS, WeraUtils.doubleToString(letturaCondominoACS,2));
                ripartizioniletturedettaglio.setAttribute(RipartizioniLettureDettagliDAO.LETTURA_AFS, WeraUtils.doubleToString(letturaCondominoAFS,2));
                ripartizioniletturedettaglio.setAttribute(RipartizioniLettureDettagliDAO.MILLESIMI_CLIMA, WeraUtils.doubleToString(M_qh_cli_1,2));
                ripartizioniletturedettaglio.setAttribute(RipartizioniLettureDettagliDAO.MILLESIMI_ACS, WeraUtils.doubleToString(M_qh_acs_1,2));
                ripartizioniletturedettaglio.setAttribute(RipartizioniLettureDettagliDAO.NUMERO_RIPARTITORI, numeroRipartitori);
                ripartizioniletturedettaglio.insert();

                
                letturaTotale=letturaTotale+letturaCondomino;
                letturaTotaleACS=letturaTotaleACS+letturaCondominoACS;
                letturaTotaleAFS=letturaTotaleAFS+letturaCondominoAFS;
                
                totaleMillesimiRiscaldamento = totaleMillesimiRiscaldamento+M_qh_cli_1;
                totaleMillesimiACS = totaleMillesimiACS+M_qh_acs_1;
                
                
                aggiornaRipartizioniLetture(idModulo,  letturaTotale, letturaTotaleACS,letturaTotaleAFS, totaleMillesimiRiscaldamento, totaleMillesimiACS);
                
                
                ripartizioniSingolo(idModulo, stabile, dataDal, dataAl, idCondomino, dsFactory);
                
            }

            dataSet.close();

        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            throw ac;
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (AppCrash ac) {
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
                }
            }
        }
        
        
      
        // RIPARTIZIONI DETTAGLI FASE 1- FINE
        
    }
    
    
    
    

    public void aggiornaRipartizioniLetture(Integer idModulo, double letturaTotale,
            double letturaTotaleACS, double letturaTotaleAFS, double totaleMillesimiRiscaldamento, double totaleMillesimiACS) throws AppCrash {

        RipartizioniLettureDAO ripartizioniletturetotale = new RipartizioniLettureDAO();
        ripartizioniletturetotale.setAttribute(RipartizioniLettureDAO.ID_MODULO, idModulo);

        
        if (ripartizioniletturetotale.retrieve()) {
            ripartizioniletturetotale.setAttribute(RipartizioniLettureDAO.LETTURA, WeraUtils.doubleToString(letturaTotale,0));
            ripartizioniletturetotale.setAttribute(RipartizioniLettureDAO.LETTURA_ACS, WeraUtils.doubleToString(letturaTotaleACS,2));
            ripartizioniletturetotale.setAttribute(RipartizioniLettureDAO.LETTURA_AFS, WeraUtils.doubleToString(letturaTotaleAFS,2));
            ripartizioniletturetotale.setAttribute(RipartizioniLettureDAO.TOTALE_MILLESIMI_RISCALDAMENTO, WeraUtils.doubleToString(totaleMillesimiRiscaldamento,2));
            ripartizioniletturetotale.setAttribute(RipartizioniLettureDAO.TOTALE_MILLESIMI_ACS, WeraUtils.doubleToString(totaleMillesimiACS,2));
            
            ripartizioniletturetotale.update();
        }
        
    }
    

    
    
  
 // per ruolo utente 
 	 @Override
 	    protected String getPathDescription(Map<String, Object> map) {

 	        String pathDescri = "";

 	        try {
 	            MenuItem selectedMenuItem = (MenuItem) map.get(WebAppConstants_itf.SELECTED_MENU_ITEM);
 	            if (selectedMenuItem != null) {
 	                if (selectedMenuItem.getPathDescri() != null) {
 	                    pathDescri = selectedMenuItem.getPathDescri();
 	                }
 	            }

 	        } catch (ClassCastException e) {

 	            String menuId = "11";
 	            String menuIdSup = "2";
 	            String languageISO = "it";
 	            String label = "Scarico rilevatori";
 	            String function = "ImportDatiRilevatori";
 	            String link = "astro?FUNCTIONID=ImportDatiRilevatori";
 	            int itemLevel = 2;
 	            String path = "1520";
 	            String linkChain = "astro?FUNCTIONID=Home;#;astro?FUNCTIONID=ImportDatiRilevatori";
 	            boolean readOnly = false;
 	            int nrOfChildren = 0;
 	            String icon = "";

 	            pathDescri = "Home / Importazione dati / Riepilogo importazione scarico dati rilevatori";

 	            MenuItem selectedMenuItem = new MenuItem(menuId, menuIdSup, languageISO, label, function, link, itemLevel,
 	                    path, pathDescri, linkChain, readOnly, nrOfChildren, icon);

 	            map.put(WebAppConstants_itf.SELECTED_MENU_ITEM, selectedMenuItem);

 	            map.put(WebAppConstants_itf.INCLUDED_MENU, "include/included_menu.include");
 	            map.put(WebAppConstants_itf.SELECTED_PATH, path);

 	        }

 	        return pathDescri;

 	    }
    
    
}
