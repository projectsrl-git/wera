
package net.projectsrl.wera.ripartizioni.core;

import java.io.PrintWriter;
import java.text.DecimalFormat;
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
import net.projectsrl.wera.ripartizioni.db.RipartizioniDAO;
import net.projectsrl.wera.ripartizioni.db.RipartizioniDettaglioDAO;
import net.projectsrl.wera.ripartizioni.db.RipartizioniDettaglioSingoloDAO;
import net.projectsrl.wera.ripartizioniuni.db.RipartizioniUNIDAO;
import net.projectsrl.wera.utils.WeraUtils;

public class FunctionInserimentoRipartizioni extends FunctionInserimentoALIMOD_base<RipartizioniDAO> {

    public FunctionInserimentoRipartizioni(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(RipartizioniDAO.ID_MODULO, "");
            templateData.put(RipartizioniDAO.DT_MODULO, project.misc.Utils.getStringDataOggi());
            templateData.put(RipartizioniDAO.ID_UTENTE_INS, getSpecificUserInfo(userInfo).getIdUtente());
            templateData.put(RipartizioniDAO.ID_AZIENDA, WeraUtils.trovaIdAziendaUtente(getSpecificUserInfo(userInfo).getIdUtente().toString()));
                        
            templateData.put(RipartizioniDAO.CC_AC_QTA, 1);
            templateData.put(RipartizioniDAO.CC_FM_QTA, 1);
            templateData.put(RipartizioniDAO.CC_SI_QTA, 1);
            templateData.put(RipartizioniDAO.CMI_CIS_QTA, 1);
            templateData.put(RipartizioniDAO.CMI_CCT_QTA, 1);
            
            templateData.put(RipartizioniDAO.TC_CC_RIP, 100);
            templateData.put(RipartizioniDAO.TC_AC_RIP, 100);
            templateData.put(RipartizioniDAO.TC_CCT_RIP, 100);
            templateData.put(RipartizioniDAO.TC_CFM_RIP, 100);
            templateData.put(RipartizioniDAO.TC_CL_RIP, 0);
            
            templateData.put(RipartizioniDAO.CR_PF_RIP, 30);
            templateData.put(RipartizioniDAO.CR_PR_RIP, 70);
            templateData.put("SALVATAGGIO_IN_CORSO",false);
            
        } else {
            PjNDAO_base rowToUpdate = new RipartizioniDAO();
            String idRow = req.getField(RipartizioniDAO.ID_MODULO);
            rowToUpdate.setAttribute(RipartizioniDAO.ID_MODULO, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), RipartizioniDAO.ID_MODULO + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
            templateData.put("SALVATAGGIO_IN_CORSO",rowToUpdate.getAttribute(RipartizioniUNIDAO.SALVATAGGIO_IN_CORSO));
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

    @Override
    protected boolean isAnInsert(SsbServletRequest req) {

        return Util.IsEmpty(req.getField(RipartizioniDAO.ID_MODULO));
    }
    
    
    @Override
    protected void sendResponseJSON(SsbServletResponse res, boolean result, String title, String message, RipartizioniDAO formDao) {

        try {
            PrintWriter out = res.getWriter();

            Integer id = (Integer) formDao.getAttribute(AliModDAO_base.ID_MODULO);
            String nr = (String) formDao.getAttribute(AliModDAO_base.NR_MODULO);
            String stato = (String) formDao.getAttribute(AliModDAO_base.STATO);
            
            WeraUtils.setRipartizioneSalvataggio("RIPARTIZIONI",true,id);
            
            WeraUtils.eliminaDatiRilevatoriDuplicati();
            eliminaRipartizioneEsistente(formDao,id);
            eliminaRipartizioneEsistenteSingolo(formDao,id);
            creaRipartizione(formDao);
            
            String resultString = "{\"result\":" + result + ",\"title\":'" + title + "',\"message\":'" + message
                    + "',\"id\":" + id + ",\"nr\":'" + nr + "',\"stato\":'" + stato + "'}";
            
            
            WeraUtils.setRipartizioneSalvataggio("RIPARTIZIONI",false,id);
            out.println(resultString);
            out.close();

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "errore writing succesful response");
        }
    }
    
    
    
    
	private void eliminaRipartizioneEsistente(RipartizioniDAO formDao, Integer id) throws NumberFormatException, AppCrash {
		 DataSet_itf dataSet = null;
         DataSetFactory dsFactory = DataSetFactory.getInstance();
         
         Integer idDettaglio;
         
         try {
             dataSet = dsFactory.makeDataSet("", "DSRipartizioniCondominio");

             HashMap<String, String> param = new HashMap<String, String>();
             param.put("WHERECONDITION", "WHERE ID_MODULO="+id);
             param.put("WHERECONDITION_PROFILI_LETTURE", " IS NOT NULL ");
             dataSet.setParam(param);
             dataSet.open();

             while (dataSet.hasMoreElements()) {
                 Row_itf dbRow = (Row_itf) dataSet.nextElement();

                 idDettaglio= (Integer) dbRow.getField("ID_DETTAGLIO");
                 RipartizioniDettaglioDAO rd = new RipartizioniDettaglioDAO();
            	 rd.setAttribute(RipartizioniDettaglioDAO.ID_DETTAGLIO,idDettaglio);
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
	
	
	private void eliminaRipartizioneEsistenteSingolo(RipartizioniDAO formDao, Integer id) throws NumberFormatException, AppCrash {
		 DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        
        Integer idDettaglio;
        
        try {
            dataSet = dsFactory.makeDataSet("", "DSRipartizioniCondominioSingolo");

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("WHERECONDITION", "WHERE ID_MODULO="+id);
            param.put("WHERECONDITION_PROFILI_LETTURE", " IS NOT NULL ");
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                idDettaglio= (Integer) dbRow.getField("ID_DETTAGLIO");
                RipartizioniDettaglioSingoloDAO rd = new RipartizioniDettaglioSingoloDAO();
           	 rd.setAttribute(RipartizioniDettaglioSingoloDAO.ID_DETTAGLIO,idDettaglio);
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
	private void creaRipartizione(RipartizioniDAO formDao) throws NumberFormatException, AppCrash {
		 Float costiComburente = Float.parseFloat(formDao.getAttributeAsString(RipartizioniDAO.TC_CC_TOTALE));
         Float conduzioneCT = Float.parseFloat(formDao.getAttributeAsString(RipartizioniDAO.TC_CCT_TOTALE));
         Float costiFM = Float.parseFloat(formDao.getAttributeAsString(RipartizioniDAO.TC_CFM_TOTALE));
         Float costiLetture = Float.parseFloat(formDao.getAttributeAsString(RipartizioniDAO.CMI_CL_TOTALE));
         Float totaleCosti = Float.parseFloat(formDao.getAttributeAsString(RipartizioniDAO.TC_CL_TOTALE));
         Float percentualeFissa = Float.parseFloat(formDao.getAttributeAsString(RipartizioniDAO.CR_PF_RIP));
         Float percentualeARipartizione= Float.parseFloat(formDao.getAttributeAsString(RipartizioniDAO.CR_PR_RIP));
         
         Double totaleMillesimi= Double.parseDouble(formDao.getAttributeAsString(RipartizioniDAO.PU_CPF));
         Double totaleConsumi= Double.parseDouble(formDao.getAttributeAsString(RipartizioniDAO.PU_CR));
         Double numRilevatori= Double.parseDouble(formDao.getAttributeAsString(RipartizioniDAO.CMI_CL_QTA));
         
         String idCondominio= (formDao.getAttributeAsString(RipartizioniDAO.ID_CONDOMINIO).toString());
         String dataDal= (formDao.getAttributeAsString(RipartizioniDAO.DATA_DAL).toString());
         String dataAl= (formDao.getAttributeAsString(RipartizioniDAO.DATA_AL).toString());
         Integer idModulo = Integer.parseInt(formDao.getAttributeAsString(RipartizioniDAO.ID_MODULO));
         
         Double percentualeFissaTotale= Double.parseDouble(formDao.getAttributeAsString(RipartizioniDAO.CR_PF_TOTALE));
         Double percentualeARipartizioneTotale= Double.parseDouble(formDao.getAttributeAsString(RipartizioniDAO.CR_PR_TOTALE));
         
         Double costiPercentualeFissaTotale= costiComburente/100*percentualeFissa/totaleMillesimi;
         Double costiARipartizione= costiComburente/100*percentualeARipartizione/totaleConsumi;
         if (totaleConsumi==0.00){
        	 costiARipartizione=0.00;
         }
         
         Double costiStraordinari = Double.parseDouble(formDao.getAttributeAsString(RipartizioniDAO.CMI_CIS_TOTALE));
         
         DataSet_itf dataSet = null;
         DataSetFactory dsFactory = DataSetFactory.getInstance();

         
         Integer idCondomino = 0;
         String denominazione="";
         Integer numRilevatoriCondomino = 0;
         Integer letturaCondomino = 0;
         Double letturaCondominoACS= (double) 0;
         Double millesimiCondomino = (double) 0;
         Double metanoMillesimiCondomino =(double) 0;
         Double metanoFisso =(double) 0;
         Double forzaMotriceCondomino =(double) 0;
         Double conduzioneCondomino=(double) 0;
         Double conduzioneAnticipoCondomino=(double) 0;
         Double conduzioneSaldoCondomino=(double) 0;
         Double manutenzioneOrdinariaCondomino=(double) 0;
         Double manutenzioneStraordinariaCondomino=(double) 0;
         Double costiLettureCondomino=(double) 0;
         Double totaleSenzaLettureCondomino=(double) 0;
         Double totaleConLettureCondomino=(double) 0;
         
         Double metanoCondomino=(double) 0;
         Double subtotaleCostiMillesimiCondomino=(double) 0;
         
         DecimalFormat df2 = new DecimalFormat("#.##");
         
         try {
             dataSet = dsFactory.makeDataSet("", "DSDatiRipartizioni");

             HashMap<String, String> param = new HashMap<String, String>();
             param.put("ID_CONDOMINIO", idCondominio);
             param.put("DATA_DAL", dataDal);
             param.put("DATA_AL", dataAl);
             dataSet.setParam(param);
             dataSet.open();

             while (dataSet.hasMoreElements()) {
                 Row_itf dbRow = (Row_itf) dataSet.nextElement();

                 idCondomino= (Integer) dbRow.getField("ID_CONDOMINO");
                 denominazione = (String) dbRow.getField("DENOMINAZIONE");
                 numRilevatoriCondomino = (Integer) dbRow.getField("RILEVATORI_CONDOMINO");
                 letturaCondomino = (Integer) dbRow.getField("LETTURA_CONDOMINO");
                 letturaCondominoACS = Double.parseDouble(dbRow.getField("LETTURA_CONDOMINO_ACS").toString());
                 millesimiCondomino = Double.parseDouble(dbRow.getField("MILLESIMI_CONDOMINO").toString());
                 
                 metanoMillesimiCondomino = costiPercentualeFissaTotale*millesimiCondomino;
                 metanoFisso = costiARipartizione*letturaCondomino;
                 conduzioneCondomino = conduzioneCT/totaleMillesimi*millesimiCondomino;
                 forzaMotriceCondomino = costiFM/totaleMillesimi*millesimiCondomino;
                 conduzioneAnticipoCondomino = conduzioneCondomino/2;
                 conduzioneSaldoCondomino = conduzioneCondomino-conduzioneAnticipoCondomino;
                 //manutenzioneOrdinariaCondomino = ;
                 manutenzioneStraordinariaCondomino = costiStraordinari/totaleMillesimi*millesimiCondomino;
                 costiLettureCondomino = costiLetture/numRilevatori*numRilevatoriCondomino;
                 totaleSenzaLettureCondomino = metanoMillesimiCondomino+metanoFisso+conduzioneCondomino+forzaMotriceCondomino;
                 totaleConLettureCondomino = totaleSenzaLettureCondomino+costiLettureCondomino;
                 metanoCondomino=metanoMillesimiCondomino+metanoFisso;
                 subtotaleCostiMillesimiCondomino=forzaMotriceCondomino+conduzioneCondomino;
                		 
                		 
                 metanoMillesimiCondomino= Double.parseDouble(WeraUtils.doubleToString(metanoMillesimiCondomino,2));
                 metanoFisso= Double.parseDouble(WeraUtils.doubleToString(metanoFisso,2));
                 conduzioneCondomino= Double.parseDouble(WeraUtils.doubleToString(conduzioneCondomino,2));
                 forzaMotriceCondomino= Double.parseDouble(WeraUtils.doubleToString(forzaMotriceCondomino,2));
                 conduzioneAnticipoCondomino= Double.parseDouble(WeraUtils.doubleToString(conduzioneAnticipoCondomino,2));
                 conduzioneSaldoCondomino= conduzioneCondomino-conduzioneAnticipoCondomino;
                 costiLettureCondomino= Double.parseDouble(WeraUtils.doubleToString(costiLettureCondomino,2));
                 totaleSenzaLettureCondomino= Double.parseDouble(WeraUtils.doubleToString(totaleSenzaLettureCondomino,2));
                 totaleConLettureCondomino= Double.parseDouble(WeraUtils.doubleToString(totaleConLettureCondomino,2));
                 
                 metanoCondomino= Double.parseDouble(WeraUtils.doubleToString(metanoCondomino,2));
                 subtotaleCostiMillesimiCondomino=Double.parseDouble(WeraUtils.doubleToString(subtotaleCostiMillesimiCondomino,2));
                 
                 
                 
                 RipartizioniDettaglioDAO rd = new RipartizioniDettaglioDAO();
                 
                 
         		
             	 rd.setAttribute(RipartizioniDettaglioDAO.ID_MODULO,idModulo);
             	 rd.setAttribute(RipartizioniDettaglioDAO.ID_CONDOMINO,idCondomino);
     			 rd.setAttribute(RipartizioniDettaglioDAO.DENOMINAZIONE,denominazione);
      			 rd.setAttribute(RipartizioniDettaglioDAO.MILLESIMI,millesimiCondomino);
     			 rd.setAttribute(RipartizioniDettaglioDAO.NUMERO_RIPARTITORI,numRilevatoriCondomino);
     			 rd.setAttribute(RipartizioniDettaglioDAO.LETTURA,letturaCondomino);
     			 rd.setAttribute(RipartizioniDettaglioDAO.LETTURA_ACS,letturaCondominoACS);
     			 rd.setAttribute(RipartizioniDettaglioDAO.METANO_FISSO,metanoFisso);
     			 rd.setAttribute(RipartizioniDettaglioDAO.METANO_MILLESIMI,metanoMillesimiCondomino);
     			 rd.setAttribute(RipartizioniDettaglioDAO.FORZA_MOTRICE,forzaMotriceCondomino);
     			 rd.setAttribute(RipartizioniDettaglioDAO.CONDUZIONE,conduzioneCondomino);
     			 rd.setAttribute(RipartizioniDettaglioDAO.CONDUZIONE_ANTICIPO,conduzioneAnticipoCondomino);
     			 rd.setAttribute(RipartizioniDettaglioDAO.CONDUZIONE_SALDO,conduzioneSaldoCondomino);
     			 rd.setAttribute(RipartizioniDettaglioDAO.MANUTENZIONE_ORDINARIA,manutenzioneOrdinariaCondomino);
     			 rd.setAttribute(RipartizioniDettaglioDAO.MANUTENZIONE_STRAORDINARIA,manutenzioneStraordinariaCondomino);
     			 rd.setAttribute(RipartizioniDettaglioDAO.COSTI_LETTURE,costiLettureCondomino);
     			 rd.setAttribute(RipartizioniDettaglioDAO.TOTALE_SENZA_LETTURE,totaleSenzaLettureCondomino+manutenzioneStraordinariaCondomino);
     			 rd.setAttribute(RipartizioniDettaglioDAO.TOTALE_CON_LETTURE,totaleConLettureCondomino+manutenzioneStraordinariaCondomino);
     			 rd.setAttribute(RipartizioniDettaglioDAO.METANO,metanoCondomino);
     			 rd.setAttribute(RipartizioniDettaglioDAO.SUBTOTALE_COSTI_MILLESIMI,subtotaleCostiMillesimiCondomino);
     			 rd.setAttribute(RipartizioniDettaglioDAO.ID_UTENTE_INS,0);
     			 rd.insert();
                 
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
         
         
         
         
         
         
         
         
         
         
         String stanza="";
         String rilevatore="";
         try {
             dataSet = dsFactory.makeDataSet("", "DSDatiRipartizioniSingolo");

             HashMap<String, String> param = new HashMap<String, String>();
             param.put("ID_CONDOMINIO", idCondominio);
             param.put("DATA_DAL", dataDal);
             param.put("DATA_AL", dataAl);
             dataSet.setParam(param);
             dataSet.open();

             while (dataSet.hasMoreElements()) {
                 Row_itf dbRow = (Row_itf) dataSet.nextElement();

                 idCondomino= (Integer) dbRow.getField("ID_CONDOMINO");
                 denominazione = (String) dbRow.getField("DENOMINAZIONE");
                 letturaCondomino = (Integer) dbRow.getField("LETTURA_CONDOMINO");
                 letturaCondominoACS = Double.parseDouble(dbRow.getField("LETTURA_CONDOMINO_ACS").toString());
                 stanza= (String) dbRow.getField("STANZA");
                 rilevatore= (String) dbRow.getField("RILEVATORE");
                 
                 if (letturaCondomino==0){
                	 metanoCondomino=0.00;
                 }else{
                	 metanoCondomino = costiComburente/100*percentualeFissa/totaleConsumi*letturaCondomino;
                 }
                 
                 
                 
                  
                 RipartizioniDettaglioSingoloDAO rd = new RipartizioniDettaglioSingoloDAO();
                 
             	 rd.setAttribute(RipartizioniDettaglioSingoloDAO.ID_MODULO,idModulo);
             	 rd.setAttribute(RipartizioniDettaglioSingoloDAO.ID_CONDOMINO,idCondomino);
     			 rd.setAttribute(RipartizioniDettaglioSingoloDAO.DENOMINAZIONE,denominazione);
     			 rd.setAttribute(RipartizioniDettaglioSingoloDAO.LETTURA,letturaCondomino);
     			 rd.setAttribute(RipartizioniDettaglioSingoloDAO.LETTURA_ACS,letturaCondominoACS);
     			 rd.setAttribute(RipartizioniDettaglioSingoloDAO.STANZA,stanza);
     			 rd.setAttribute(RipartizioniDettaglioSingoloDAO.RILEVATORE,rilevatore);
     			 rd.setAttribute(RipartizioniDettaglioSingoloDAO.METANO,metanoCondomino);
     			 rd.setAttribute(RipartizioniDettaglioSingoloDAO.ID_UTENTE_INS,0);
     			 rd.insert();
                 
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
