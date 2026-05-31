
package net.projectsrl.wera.ripartizioniuni2018.core;

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
import net.projectsrl.wera.ripartizioniuni.db.RipartizioniUNIDAO;
import net.projectsrl.wera.ripartizioniuni2018.db.RipartizioniUNI2018DAO;
import net.projectsrl.wera.ripartizioniuni2018.db.RipartizioniUNI2018DettagliDAO;
import net.projectsrl.wera.ripartizioniuni2018.db.RipartizioniUNI2018DettaglioSingoloDAO;
import net.projectsrl.wera.utils.WeraUtils;

public class FunctionInserimentoRipartizioniUNI2018 extends FunctionInserimentoALIMOD_base<RipartizioniUNI2018DAO> {
	
	 private static final String DATASET_CONSUMI_TOTALI_CONDOMINIO = "DataSetConsumiTotaliCondominio";
     private static final String DATASET_CONSUMI_TOTALI_SINGOLI = "DataSetConsumiTotaliSingoli";
     private static final String DATASET_CONSUMO_CONTATORE_CONDOMINIO = "DataSetConsumoContatoreCondominio";
     private static final String DATASET_FRAZIONE_CONSUMO_INVOLONTARIO_TABELLA = "DataSetFrazioneConsumoInvolontarioTabella";
     private static final String DATASET_CONTATORI_RIPARTITORI = "DataSetContatoriRipartitori";
     private static final String DATASET_CONDOMINI = "DataSetDatiCondominio";

    public FunctionInserimentoRipartizioniUNI2018(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(RipartizioniUNI2018DAO.ID_MODULO, "");
            templateData.put(RipartizioniUNI2018DAO.DT_MODULO, project.misc.Utils.getStringDataOggi());
            templateData.put(RipartizioniUNI2018DAO.ID_UTENTE_INS, getSpecificUserInfo(userInfo).getIdUtente());
            
            templateData.put("SALVATAGGIO_IN_CORSO",false);
           
        } else {
            PjNDAO_base rowToUpdate = new RipartizioniUNI2018DAO();
            String idRow = req.getField(RipartizioniUNI2018DAO.ID_MODULO);
            rowToUpdate.setAttribute(RipartizioniUNI2018DAO.ID_MODULO, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), RipartizioniUNI2018DAO.ID_MODULO + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
            templateData.put("SALVATAGGIO_IN_CORSO",rowToUpdate.getAttribute(RipartizioniUNIDAO.SALVATAGGIO_IN_CORSO));
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

    @Override
    protected boolean isAnInsert(SsbServletRequest req) {

        return Util.IsEmpty(req.getField(RipartizioniUNI2018DAO.ID_MODULO));
    }
    
    
    @Override
    protected void sendResponseJSON(SsbServletResponse res, boolean result, String title, String message, RipartizioniUNI2018DAO formDao) {

        try {
            PrintWriter out = res.getWriter();

            Integer id = (Integer) formDao.getAttribute(AliModDAO_base.ID_MODULO);
            String nr = (String) formDao.getAttribute(AliModDAO_base.NR_MODULO);
            String stato = (String) formDao.getAttribute(AliModDAO_base.STATO);
            
            WeraUtils.setRipartizioneSalvataggio("RIPARTIZIONI_UNI_2018",true,id);
            
            WeraUtils.eliminaDatiRilevatoriDuplicati();
            eliminaRipartizioneEsistente(formDao,id);
            eliminaRipartizioneEsistenteSingolo(formDao,id);
            creaRipartizione(formDao);
            
            String resultString = "{\"result\":" + result + ",\"title\":'" + title + "',\"message\":'" + message
                    + "',\"id\":" + id + ",\"nr\":'" + nr + "',\"stato\":'" + stato + "'}";
            
            
            WeraUtils.setRipartizioneSalvataggio("RIPARTIZIONI_UNI_2018",false,id);
            out.println(resultString);
            out.close();

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "errore writing succesful response");
        }
    }
    
    private void eliminaRipartizioneEsistenteSingolo(RipartizioniUNI2018DAO formDao, Integer id) throws NumberFormatException, AppCrash {
		 DataSet_itf dataSet = null;
       DataSetFactory dsFactory = DataSetFactory.getInstance();
       
       Integer idDettaglio;
       
       try {
           dataSet = dsFactory.makeDataSet("", "DSRipartizioniUNI2018CondominioSingolo");

           HashMap<String, String> param = new HashMap<String, String>();
           param.put("WHERECONDITION", "WHERE ID_MODULO="+id);
           param.put("WHERECONDITION_PROFILI_LETTURE", " IS NOT NULL ");
           dataSet.setParam(param);
           dataSet.open();

           while (dataSet.hasMoreElements()) {
               Row_itf dbRow = (Row_itf) dataSet.nextElement();

               idDettaglio= (Integer) dbRow.getField("ID_DETTAGLIO");
               RipartizioniUNI2018DettaglioSingoloDAO rd = new RipartizioniUNI2018DettaglioSingoloDAO();
          	 rd.setAttribute(RipartizioniUNI2018DettaglioSingoloDAO.ID_DETTAGLIO,idDettaglio);
          	 rd.delete();
               
           }
       } catch (AppCrash ac) {
           ac.logContext(this.getClass().getName(),
                   "Errore nella ricerca dell'ultimo progressivo del dataset " + "DSRipartizioniUNI2018CondominioSingolo");
           throw ac;
       } finally {
           // chiude il dataset per il conteggio degli elementi trovati
           if (dataSet != null) {
               try {
                   dataSet.close();
               } catch (Throwable t) {
                   AppCrash ac = new AppCrash(t);
                   ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DSRipartizioniUNI2018CondominioSingolo");
               }
           }
       }

	}
    
    private void eliminaRipartizioneEsistente(RipartizioniUNI2018DAO formDao, Integer id) throws NumberFormatException, AppCrash {
		 DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        
        Integer idDettaglio;
        
        try {
            dataSet = dsFactory.makeDataSet("", "DSRipartizioniUNI2018Condominio");

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("WHERECONDITION", "WHERE ID_MODULO="+id);
            param.put("WHERECONDITION_PROFILI_LETTURE", " IS NOT NULL ");
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                idDettaglio= (Integer) dbRow.getField("ID_DETTAGLIO");
                RipartizioniUNI2018DettagliDAO rd = new RipartizioniUNI2018DettagliDAO();
           	 rd.setAttribute(RipartizioniUNI2018DettagliDAO.ID_DETTAGLIO,idDettaglio);
           	 rd.delete();
                
            }
        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),
                    "Errore nella ricerca dell'ultimo progressivo del dataset " + "DSRipartizioniUNI2018Condominio");
            throw ac;
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DSRipartizioniUNI2018Condominio");
                }
            }
        }
        
    		
		
	}
    
    
   
    @SuppressWarnings("unused")
	private void creaRipartizione(RipartizioniUNI2018DAO formDao) throws NumberFormatException, AppCrash {

    	Integer idModulo = (Integer) formDao.getAttribute(RipartizioniUNI2018DAO.ID_MODULO);
    	Integer stabile = (Integer) formDao.getAttribute(RipartizioniUNI2018DAO.ID_CONDOMINIO);
        String dataDal = (String) formDao.getAttributeAsString(RipartizioniUNI2018DAO.DATA_DAL);
        String dataAl = (String) formDao.getAttributeAsString(RipartizioniUNI2018DAO.DATA_AL);
        Integer idAzienda = (Integer) formDao.getAttribute(RipartizioniUNI2018DAO.ID_AZIENDA);
        
    	String tipoRipartizione = (String) formDao.getAttributeAsString(RipartizioniUNI2018DAO.TIPO_RIPARTIZIONE).trim();
    	String tipoContabilizzazione = (String) formDao.getAttributeAsString(RipartizioniUNI2018DAO.TIPO_CONTABILIZZAZIONE).trim();
    	
    	// spese gestionali - consuntivo
    	Float sgScmTotale= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.SG_SCM_TOTALE_CONS));
    	Float sgScrTotale= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.SG_SCR_TOTALE_CONS));
    	Float sgSgTotale= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.SG_SG_TOTALE_CONS));
    	
    	// vettori energetici - consuntivo
    	Float vetCombQ_VEH= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.VET_COMB_Q_VE_H_CONS));
    	Float vetCombQ_VEW= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.VET_COMB_Q_VE_W_CONS));
    	Float vetCombCVe= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.VET_COMB_C_VE_CONS));
    	Float vetElettrQ_VEH= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.VET_ELETTR_Q_VE_H_CONS));
    	Float vetElettrQ_VEW= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.VET_ELETTR_Q_VE_W_CONS));
    	Float vetElettrCVe= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.VET_ELETTR_C_VE_CONS));
    	
    	// spese totali - consuntivo
    	Float stRiscSe= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.ST_RISC_SE_CONS));
    	Float stRiscSg= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.ST_RISC_SG_CONS));
    	Float stRiscSt= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.ST_RISC_ST_CONS));
    	
    	Float stACSSe= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.ST_ACS_SE_CONS));
    	Float stACSSg= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.ST_ACS_SG_CONS));
    	Float stACSSt= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.ST_ACS_ST_CONS));
    	
    	// generatori - consuntivo
    	Float fabbisognoAnnuoClima =  Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.FABBISOGNO_ANNUO_CLIMA));
    	Float fabbisognoAnnuoACS =  Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.FABBISOGNO_ANNUO_ACS));
    	Float perditeImpianto =  Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.PERDITE_IMPIANTO));
    	
    	double genQgnH= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.GEN_QGN_H_CONS));
    	double genQgnW= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.GEN_QGN_W_CONS));
    	
    	double qHtot= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.GEN_QGN_H_CONS));
    	double qWtot= Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNI2018DAO.GEN_QGN_W_CONS));
    	
    	 if (genQgnH>0){
         }else{
        	 genQgnH=trovaLetturaConsumoContatoreCondominio(dataDal,dataAl,stabile,"v_contatori_riscaldamento");
         }
         
         if (genQgnW>0){
         }else{
        	 genQgnW=trovaLetturaConsumoContatoreCondominio(dataDal,dataAl,stabile,"v_contatori_acqua_calda");
         }
         
         Float[] arrayLettureCondominio = lettureTotaliCondominio(stabile,dataDal,dataAl,DATASET_CONSUMI_TOTALI_CONDOMINIO);
         
         double lettureCondominio=arrayLettureCondominio[0];
         double lettureCondominioACS=arrayLettureCondominio[1];
         
         Integer[] arrayNumeroContatoriRipartitori = contaContatoriRipartitori(dataDal,dataAl,stabile);
         Integer ripartitoriHCA = arrayNumeroContatoriRipartitori[0];
         Integer contatoriHCA = arrayNumeroContatoriRipartitori[1];
         Integer contatoriACS = arrayNumeroContatoriRipartitori[2];
         Integer contatoriACSEdificio = arrayNumeroContatoriRipartitori[3];
         Integer contatoriHCAEdificio = arrayNumeroContatoriRipartitori[4];
         
         
         String[] arrayDatiCondominio = datiCondominio(stabile);
         String responsabileImpianto = arrayDatiCondominio[0];
         String idSoftware = arrayDatiCondominio[1];
         String idTecnico = arrayDatiCondominio[2];
         String idAmministratore = arrayDatiCondominio[3];
         
    
       	calcolaValoriPerRipartizione(formDao, idModulo, stabile, dataDal, dataAl,idAzienda,fabbisognoAnnuoClima,fabbisognoAnnuoACS,qHtot,qWtot,stRiscSe,stACSSe,genQgnH,genQgnW,lettureCondominio,lettureCondominioACS,stRiscSg,stRiscSt,stACSSg,stACSSt,perditeImpianto,ripartitoriHCA,contatoriHCA,contatoriACS,responsabileImpianto,idSoftware,idTecnico,idAmministratore,contatoriACSEdificio,contatoriHCAEdificio);                
        
    }
    
    
    
   

    private void calcolaValoriPerRipartizione(RipartizioniUNI2018DAO formDao, Integer idModulo, Integer stabile, String dataDal, String dataAl, Integer idAzienda, Float fabbisognoAnnuoClima, Float fabbisognoAnnuoACS, double qHtot, double qWtot, Float stRiscSe, Float stACSSe, double genQgnH, double genQgnW, double lettureCondominio, double lettureCondominioACS, Float stRiscSg, Float stRiscSt, Float stACSSg, Float stACSSt, Float perditeImpianto, Integer ripartitoriHCA, Integer contatoriHCA, Integer contatoriACS, String responsabileImpianto, String idSoftware, String idTecnico, String idAmministratore, Integer contatoriACSEdificio, Integer contatoriHCAEdificio) throws AppCrash {
        
    	double fHuso= 0; 
    	double fWuso= 0;
    	double fXinv = 0;
    	
    	double fHinv=0;
    	double fWinv=0;
    	double qHinv=0;
    	double qWinv=0;
    	double qHinvSenza=0;
    	double qWinvSenza=0;
    	double qHvol=0;
    	double qWvol=0;
    	
    	double qHobb=0;
    	double qWobb=0;
    	
    	double cH=0;
    	double cW=0;
    	
    	double speseRiscVol=0;
    	double speseRiscInv=0;
    	double speseRiscObb=0;
    	double speseRiscE=0; 
    	double speseRiscG=0;
    	double speseRiscC=0;
    	double speseRiscP=0;
    	double speseRiscUC=0;
    	double speseRiscTot=0;
    	
    	double speseACSVol=0;
    	double speseACSInv=0;
    	double speseACSObb=0;
    	double speseACSE=0; 
    	double speseACSG=0;
    	double speseACSC=0;
    	double speseACSP=0;
    	double speseACSUC=0;
    	double speseACSTot=0;
    	
    	
    	boolean contabilizzazioneACS=false;
    	boolean contabilizzazioneDiretta=false;
    	boolean compresenzaSistemiContabilizzazioneDifferenti=false;
    	boolean assenzaContabilizzazione=false;
    	
    	String mill="";
    	String servizioRiscaldamentoUtenza="-";
    	String servizioAcsUtenza="-";
    	String servizioRiscaldamentoEdifici="-";
    	String servizioACSEdifici="-";
    	
    	
    	if (contatoriACSEdificio>0){
    		servizioACSEdifici="Contatori";
    	}
    	if (contatoriHCAEdificio>0){
    		servizioRiscaldamentoEdifici="Contatori";
    	}
    	
    	if (contatoriHCA==0 && ripartitoriHCA==0 && contatoriACS==0){
    		assenzaContabilizzazione=true;
    		mill="Millesimi proprietà";
    	}else{
    		mill="Millesimi fabbisogno";
    		if (contatoriACS>0){
        		contabilizzazioneACS=true;
        		servizioAcsUtenza="Contatori volumetrici";
        	}
    		
			if (ripartitoriHCA>0){
				servizioRiscaldamentoUtenza="Ripartitori";
        	}
        	
        	if (contatoriHCA>0 && ripartitoriHCA==0){
        		contabilizzazioneDiretta=true;
        		servizioRiscaldamentoUtenza="Contatori";
        	}
        	
        	if (contatoriHCA>0 && ripartitoriHCA>0){
        		compresenzaSistemiContabilizzazioneDifferenti=true;
        		servizioRiscaldamentoUtenza="Contatori e Ripartitori";
        	}
        	
        	
    	}
    	
    	
    	
    	if (!assenzaContabilizzazione){
    		// fattore d'uso - bisogna fare verifica della piena occupazione
        	fHuso= qHtot/fabbisognoAnnuoClima;
        	if (fabbisognoAnnuoACS>0){
        		fWuso=qWtot/fabbisognoAnnuoACS;
        	}
        	
        	fXinv = trovaFrazioneConsumoInvolontarioPienaUtilizzazione(stabile,fabbisognoAnnuoClima,fabbisognoAnnuoACS,perditeImpianto);
        	
        	
        	// frazione consumo involontario
        	if (fHuso>0.8){		// piena utilizzazione edificio
        		fHinv=fXinv;
        		fWinv=fXinv;
        	}
        	if (fHuso<=0.8){	// parziale utilizzazione edificio
        		fHinv=1-((1-fXinv)/0.8)*fHuso;
        	    fWinv=1-((1-fXinv)/0.8)*fWuso;
        	}
        	
        	
        	
        	//consumo involontario totale - unità senza dispositivi contabilizzazione
        	qHinvSenza= fabbisognoAnnuoClima*fHinv;
        	qWinvSenza= fabbisognoAnnuoACS*fWinv;
        	
        	
        	if (contabilizzazioneACS || contabilizzazioneDiretta){	// se contabilizzazione ACS
        		//consumo volontario totale
            	qHvol=lettureCondominio;
            	qWvol=lettureCondominioACS;
            	
        		//consumo involontario totale
            	qHinv= qHtot-qHvol;
            	qWinv= qWtot-qWvol;
        	}else{
        		//consumo involontario totale
            	qHinv= qHtot*fHinv;
            	qWinv= qWtot*fWinv;
            	
            	//consumo volontario totale
            	qHvol=qHtot-qHinv;
            	qWvol=qWtot-qWinv;
        		
        	}
        	
        	
        	
    	}
    	
    	
    	//costi unitari dell'energia termica utile
    	if (lettureCondominio>0){
    		cH=stRiscSe/lettureCondominio;
    	}
    	if (contabilizzazioneACS){
    		cW=stACSSe/lettureCondominioACS;
    	}
    	
    	
    	
    	
    	
    	
    	
    	
    	//spese del condominio
    	speseRiscVol=qHvol*cH;
    	speseRiscInv=qHinv*cH;
    	speseRiscObb=0;
    	speseRiscE=stRiscSe; 
    	speseRiscG=stRiscSg;
    	speseRiscC=speseRiscVol;
    	speseRiscP=speseRiscInv+speseRiscG;
    	speseRiscUC=0;
    	speseRiscTot=stRiscSt;
    	
    	speseACSVol=qWvol*cW;
    	speseACSInv=qWinv*cW;
    	speseACSObb=0;
    	speseACSE=stACSSe; 
    	speseACSG=stACSSg;
    	speseACSC=speseACSVol;
    	speseACSP=speseACSInv+speseACSG;
    	speseACSUC=0;
    	speseACSTot=stACSSt;
    	
    	
    	RipartizioniUNI2018DAO ripartizioniuni = new RipartizioniUNI2018DAO();
        ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.ID_MODULO, idModulo);

        if (ripartizioniuni.retrieve()) {
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.CONSUMI_RISC_Q_VOL,WeraUtils.doubleToString(qHvol,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.CONSUMI_RISC_Q_INV,WeraUtils.doubleToString(qHinv,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.CONSUMI_RISC_Q_OBB,WeraUtils.doubleToString(qHobb,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.CONSUMI_RISC_Q_TOT,WeraUtils.doubleToString(qHtot,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.CONSUMI_RISC_F_INV,WeraUtils.doubleToString(fHinv,2));
            
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.CONSUMI_ACS_Q_VOL,WeraUtils.doubleToString(qWvol,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.CONSUMI_ACS_Q_INV,WeraUtils.doubleToString(qWinv,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.CONSUMI_ACS_Q_OBB,WeraUtils.doubleToString(qWobb,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.CONSUMI_ACS_Q_TOT,WeraUtils.doubleToString(qWtot,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.CONSUMI_ACS_F_INV,WeraUtils.doubleToString(fWinv,2));
            
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.COSTI_ETU_CLIMA_INVERN_CH,WeraUtils.doubleToString(cH,4));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.COSTI_ETU_ACS_CW,WeraUtils.doubleToString(cW,4));
            
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_RISC_S_VOL,WeraUtils.doubleToString(speseRiscVol,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_RISC_S_INV,WeraUtils.doubleToString(speseRiscInv,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_RISC_S_OBB,WeraUtils.doubleToString(speseRiscObb,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_RISC_S_E,WeraUtils.doubleToString(speseRiscE,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_RISC_S_G,WeraUtils.doubleToString(speseRiscG,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_RISC_S_C,WeraUtils.doubleToString(speseRiscC,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_RISC_S_P,WeraUtils.doubleToString(speseRiscP,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_RISC_S_UC,WeraUtils.doubleToString(speseRiscUC,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_RISC_S_TOT,WeraUtils.doubleToString(speseRiscTot,2));
            
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_ACS_S_VOL,WeraUtils.doubleToString(speseACSVol,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_ACS_S_INV,WeraUtils.doubleToString(speseACSInv,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_ACS_S_OBB,WeraUtils.doubleToString(speseACSObb,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_ACS_S_E,WeraUtils.doubleToString(speseACSE,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_ACS_S_G,WeraUtils.doubleToString(speseACSG,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_ACS_S_C,WeraUtils.doubleToString(speseACSC,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_ACS_S_P,WeraUtils.doubleToString(speseACSP,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_ACS_S_UC,WeraUtils.doubleToString(speseACSUC,2));
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SPESE_ACS_S_TOT,WeraUtils.doubleToString(speseACSTot,2));
            
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.ID_AMMINISTRATORE,idAmministratore);
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.RESPONSABILE_IMPIANTO,responsabileImpianto);
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.ID_SOFTWARE_CALCOLO,idSoftware);
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.ID_TECNICO,idTecnico);

            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.MILL,mill);
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SERVIZIO_RISCALDAMENTO_UTENZA,servizioRiscaldamentoUtenza);
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SERVIZIO_ACS_UTENZA,servizioAcsUtenza);
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SERVIZIO_RISCALDAMENTO_CENTRALE,"Per singolo generatore");
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SERVIZIO_ACS_CENTRALE,"Per singolo generatore");
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SERVIZIO_RISCALDAMENTO_EDIFICI,servizioRiscaldamentoEdifici);
            ripartizioniuni.setAttribute(RipartizioniUNI2018DAO.SERVIZIO_ACS_EDIFICI,servizioACSEdifici);
            		
            ripartizioniuni.update();
        }
        
        
 
        
        
        
        // DETTAGLI RIPARTIZIONE - CONSUMI SINGOLE UNITA' IMMOBILIARI (generale)
        
       
    	
    	
        
        String idCondomino;
        String denominazione;
        Float letturaCondominoACS;
        Float letturaCondomino;
        Float millesimi;
        Float millesimiClimaCondomino;
        Float millesimiACSCondomino;
        Integer numeroRipartitori;
        Float fabbisognoClimaCondomino;
        Float fabbisognoACSCondomino;
        String fattoreEnergia;
        String tipoUtenza;
        
        double qHvol_condomino=0;
        double qHinv_condomino=0;
        double qHobb_condomino=0;
        double qHtot_condomino=0;
        double qWvol_condomino=0;
        double qWinv_condomino=0;
        double qWobb_condomino=0;
        double qWtot_condomino=0;
        
        //double letturaCondominioACS = 0;
        //double letturaCondominio = 0;
        
        double qHvol_condominoSomma=0;
        
        
        // valori costanti
    	double caloreSpecificoAcquaPressioneCostante=(1.162*0.001);
        double temperaturaMediaAcquaACS=48;		//settare default e censire in anagrafica condominio
        double temperaturaMediaAcquaFredda=15;	//settare default e censire in anagrafica condominio
        double massaVolumicaAcqua=1000;
        
        
        double speseRiscVol_condomino=0;
    	double speseRiscInv_condomino=0;
    	double speseRiscObb_condomino=0;
    	double speseRiscE_condomino=0;
    	double speseRiscG_condomino=0;
    	double speseRiscC_condomino=0;
    	double speseRiscP_condomino=0;
    	double speseRiscUC_condomino=0;
    	double speseRiscTot_condomino=0;
    	
    	double speseACSVol_condomino=0;
    	double speseACSInv_condomino=0;
    	double speseACSObb_condomino=0;
    	double speseACSE_condomino=0;
    	double speseACSG_condomino=0;
    	double speseACSC_condomino=0;
    	double speseACSP_condomino=0;
    	double speseACSUC_condomino=0;
    	double speseACSTot_condomino=0;
    	
    	double speseGLTot_condomino=0;
        
        
        RipartizioniUNI2018DettagliDAO ripartizioniunidettaglio = new RipartizioniUNI2018DettagliDAO();
        
        DataSet_itf dataSet = null;
        try {

            DataSetFactory dsFactory = DataSetFactory.getInstance();

            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_CONSUMI_TOTALI_SINGOLI);
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
                letturaCondomino = Float.valueOf(dbRow.getField("LETTURA_CONDOMINO").toString().trim());
                letturaCondominoACS=Float.valueOf(dbRow.getField("LETTURA_CONDOMINO_ACS").toString().trim());
                millesimi = Float.valueOf(dbRow.getField("MILLESIMI").toString().trim());
                millesimiClimaCondomino = Float.valueOf(dbRow.getField("MILLESIMI_CLIMA").toString().trim());
                millesimiACSCondomino = Float.valueOf(dbRow.getField("MILLESIMI_ACS").toString().trim());
                numeroRipartitori=Integer.parseInt(dbRow.getField("NUMERO_RIPARTITORI").toString().trim());
                
                fabbisognoClimaCondomino = Float.valueOf(dbRow.getField("FABBISOGNO_CLIMA").toString().trim());
                fabbisognoACSCondomino = Float.valueOf(dbRow.getField("FABBISOGNO_ACS").toString().trim());
                
                fattoreEnergia = dbRow.getField("FATTORE_ENERGIA").toString().trim();
                tipoUtenza = dbRow.getField("TIPO_UTENZA").toString().trim();		// serve x distinguere locali uso collettivo
                
                //letturaCondominioACS=letturaCondominioACS+letturaCondominoACS;
                //letturaCondominio=letturaCondominio+letturaCondomino;
                
                boolean contabilizzazioneCondominoACS=false;
                boolean contabilizzazioneCondominoDiretta=false;
                
                if (!assenzaContabilizzazione){
                	if (fattoreEnergia.equals("6")){
                		contabilizzazioneCondominoACS=true;
                	}
                    
                    if (fattoreEnergia.equals("13")){
                    	contabilizzazioneCondominoDiretta=true;
                	}
                    
                    
                }
                
               
                if (assenzaContabilizzazione){
                	
                	//spese del condomino
                	speseRiscE_condomino=speseRiscE*(millesimi/1000);
                	speseRiscG_condomino=speseRiscG*(millesimiClimaCondomino/1000);
                	speseRiscTot_condomino=speseRiscE_condomino+speseRiscG_condomino;
                	
                	speseACSE_condomino=speseACSE*(millesimi/1000);
                	speseACSG_condomino=speseACSG*(millesimiACSCondomino/1000);
                	speseACSTot_condomino=speseACSE_condomino+speseACSG_condomino;
                	
                }else{
                	qHinv_condomino=qHinv*(millesimiClimaCondomino/1000);
                    qHobb_condomino=0;
                    
                    // se sprovvisto di dispositivi contabilizzazione (o rotti) 
                    if (numeroRipartitori==0){
                    	 qHvol_condomino=(fabbisognoAnnuoClima-qHinvSenza)*(fabbisognoClimaCondomino/fabbisognoAnnuoClima);
                         qWvol_condomino=(fabbisognoAnnuoACS-qWinvSenza)*(fabbisognoACSCondomino/fabbisognoAnnuoACS);
                    }else{
                    	
                    	if (compresenzaSistemiContabilizzazioneDifferenti){	   // sistemi di contabilizzazione differenti
                    		if (contabilizzazioneCondominoDiretta){
                        		qHvol_condomino=letturaCondomino;
                        		qHvol_condominoSomma=qHvol_condominoSomma+qHvol_condomino;
                        	}else{
                        		qHvol_condomino=(qHvol-qHinv-qHobb-qHvol_condominoSomma)*(letturaCondomino);
                        	}
                    	}else{
                    		if (contabilizzazioneCondominoDiretta){
                        		qHvol_condomino=letturaCondomino;
                        	}else{
                        		qHvol_condomino=qHvol*(letturaCondomino/lettureCondominio);
                        	}
                    	}
                    	
                    	if (contabilizzazioneCondominoACS){
                    		qWvol_condomino= letturaCondominoACS*caloreSpecificoAcquaPressioneCostante * massaVolumicaAcqua * (temperaturaMediaAcquaACS-temperaturaMediaAcquaFredda);
                    	}
                    	
                    	
                    }
                   
                    //qWvol_condomino=qWvol*(letturaCondominoACS/lettureCondominioACS);

                    qWinv_condomino=qWinv*(millesimiACSCondomino/1000);
                    qWobb_condomino=0;
                    qWtot_condomino=qWvol_condomino+qWinv_condomino+qWobb_condomino;
                    qHtot_condomino=qHvol_condomino+qHinv_condomino+qHobb_condomino;
                    
                    if (tipoUtenza.equals("000")){
                    	speseRiscUC_condomino=speseRiscUC*(millesimi/1000);
                    	speseACSUC_condomino=speseACSUC*(millesimi/1000);
                    }
                    
                    //spese del condomino
                	speseRiscVol_condomino=qHvol_condomino*cH;
                	speseRiscInv_condomino=qHinv_condomino*cH;
                	speseRiscObb_condomino=0;
                	speseRiscE_condomino=speseRiscVol_condomino+speseRiscInv_condomino;
                	speseRiscG_condomino=stRiscSg*(millesimiClimaCondomino/1000);
                	speseRiscC_condomino=speseRiscVol_condomino;
                	speseRiscP_condomino=speseRiscInv+speseRiscG_condomino;
                	//speseRiscUC_condomino=0;
                	speseRiscTot_condomino=speseRiscC_condomino+speseRiscP_condomino;
                	
                	speseACSVol_condomino=qWvol_condomino*cW;
                	speseACSInv_condomino=qWinv_condomino*cW;
                	speseACSObb_condomino=0;
                	speseACSE_condomino=speseACSVol_condomino+speseACSInv_condomino;
                	speseACSG_condomino=stACSSg*(millesimiACSCondomino/1000);
                	speseACSC_condomino=speseACSVol_condomino;
                	speseACSP_condomino=speseACSInv_condomino+speseACSG_condomino;
                	//speseACSUC_condomino=0;
                	speseACSTot_condomino=speseACSC_condomino+speseACSP_condomino;
                }
                
            	
            	speseGLTot_condomino=speseRiscTot_condomino+speseACSTot_condomino;
            	
                
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.ID_MODULO, idModulo);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.ID_UTENTE_INS, 0);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.ID_AZIENDA, idAzienda);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.ID_CONDOMINO, idCondomino);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.DENOMINAZIONE, denominazione);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.LETTURA, WeraUtils.doubleToString(letturaCondomino,0));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.LETTURA_ACS, WeraUtils.doubleToString(letturaCondominoACS,2));
                
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.CONSUMO_RISCALDAMENTO_Q_VOL, WeraUtils.doubleToString(qHvol_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.CONSUMO_RISCALDAMENTO_Q_INV, WeraUtils.doubleToString(qHinv_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.CONSUMO_RISCALDAMENTO_Q_OBB, WeraUtils.doubleToString(qHobb_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.CONSUMO_RISCALDAMENTO_Q_TOT, WeraUtils.doubleToString(qHtot_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.CONSUMO_ACS_Q_VOL, WeraUtils.doubleToString(qWvol_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.CONSUMO_ACS_Q_INV, WeraUtils.doubleToString(qWinv_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.CONSUMO_ACS_Q_OBB, WeraUtils.doubleToString(qWobb_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.CONSUMO_ACS_Q_TOT, WeraUtils.doubleToString(qWtot_condomino,2));
                
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.MILLESIMI, WeraUtils.doubleToString(millesimi,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.MILLESIMI_CLIMA, WeraUtils.doubleToString(millesimiClimaCondomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.MILLESIMI_ACS, WeraUtils.doubleToString(millesimiACSCondomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.NUMERO_RIPARTITORI, numeroRipartitori);
                
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_RISCALDAMENTO_S_VOL,WeraUtils.doubleToString(speseRiscVol_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_RISCALDAMENTO_S_INV,WeraUtils.doubleToString(speseRiscInv_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_RISCALDAMENTO_S_OBB,WeraUtils.doubleToString(speseRiscObb_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_RISCALDAMENTO_S_E,WeraUtils.doubleToString(speseRiscE_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_RISCALDAMENTO_S_G,WeraUtils.doubleToString(speseRiscG_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_RISCALDAMENTO_S_C,WeraUtils.doubleToString(speseRiscC_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_RISCALDAMENTO_S_P,WeraUtils.doubleToString(speseRiscP_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_RISCALDAMENTO_S_UC,WeraUtils.doubleToString(speseRiscUC_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_RISCALDAMENTO_S_TOT,WeraUtils.doubleToString(speseRiscTot_condomino,2));
                
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_ACS_S_VOL,WeraUtils.doubleToString(speseACSVol_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_ACS_S_INV,WeraUtils.doubleToString(speseACSInv_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_ACS_S_OBB,WeraUtils.doubleToString(speseACSObb_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_ACS_S_E,WeraUtils.doubleToString(speseACSE_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_ACS_S_G,WeraUtils.doubleToString(speseACSG_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_ACS_S_C,WeraUtils.doubleToString(speseACSC_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_ACS_S_P,WeraUtils.doubleToString(speseACSP_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_ACS_S_UC,WeraUtils.doubleToString(speseACSUC_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_ACS_S_TOT,WeraUtils.doubleToString(speseACSTot_condomino,2));
                
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_TOTALI_S_H_TOT,WeraUtils.doubleToString(speseRiscTot_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_TOTALI_S_W_TOT,WeraUtils.doubleToString(speseACSTot_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.SPESE_TOTALI_S_GL_TOT,WeraUtils.doubleToString(speseGLTot_condomino,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNI2018DettagliDAO.TIPO_UTENZA,tipoUtenza);
    
                ripartizioniunidettaglio.insert();
                
                //locale uso collettivo
                if (tipoUtenza.equals("001")){
                	speseRiscUC=speseRiscUC+speseRiscTot_condomino;
                	speseACSUC=speseACSUC+speseACSTot_condomino;
                }
                
                
                // DETTAGLI RIPARTIZIONE - dettaglio unità immobiliare
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
       
        
        
        
        
        
        
       
        
        
        
        
    }
    
   
    
    

	private void ripartizioniSingolo(Integer idModulo, Integer stabile, String dataDal, String dataAl, String idCondomino,DataSetFactory dsFactory) throws AppCrash {
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
		        dataSet2 = dsFactory2.makeDataSet("", "DSDatiRipartizioniUNI2018Singolo");

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
		             
		            RipartizioniUNI2018DettaglioSingoloDAO rds = new RipartizioniUNI2018DettaglioSingoloDAO();
		            
		        	 rds.setAttribute(RipartizioniUNI2018DettaglioSingoloDAO.ID_MODULO,idModulo);
		        	 rds.setAttribute(RipartizioniUNI2018DettaglioSingoloDAO.ID_CONDOMINO,idCondomino);
					 rds.setAttribute(RipartizioniUNI2018DettaglioSingoloDAO.DENOMINAZIONE,denominazione);
					 rds.setAttribute(RipartizioniUNI2018DettaglioSingoloDAO.RIPARTITORE_LETTURA,WeraUtils.doubleToString(letturaCondomino,2));
					 rds.setAttribute(RipartizioniUNI2018DettaglioSingoloDAO.CONTATORE_LETTURA,WeraUtils.doubleToString(letturaCondominoACS,2));
					 rds.setAttribute(RipartizioniUNI2018DettaglioSingoloDAO.STANZA,stanza);
					 rds.setAttribute(RipartizioniUNI2018DettaglioSingoloDAO.RILEVATORE,rilevatore);
					 rds.setAttribute(RipartizioniUNI2018DettaglioSingoloDAO.NUMERO_RIPARTITORI,numeroRipartitori);
					 
					 rds.setAttribute(RipartizioniUNI2018DettaglioSingoloDAO.ID_UTENTE_INS,0);
					 rds.insert();
		            
		        }
		    } catch (AppCrash ac) {
		        ac.logContext(this.getClass().getName(),
		                "Errore nella ricerca dell'ultimo progressivo del dataset " + "DSDatiRipartizioniUNI2018Singolo");
		        throw ac;
		    } finally {
		        // chiude il dataset per il conteggio degli elementi trovati
		        if (dataSet2 != null) {
		            try {
		                dataSet2.close();
		            } catch (Throwable t) {
		                AppCrash ac = new AppCrash(t);
		                ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + "DSDatiRipartizioniUNI2018Singolo");
		            }
		        }
		    }
		    // ripartizioni singolo fine
	}
    
    
    
	private double trovaFrazioneConsumoInvolontarioPienaUtilizzazione(Integer stabile, Float fabbisognoAnnuoClima, Float fabbisognoAnnuoACS, Float perditeImpianto) throws AppCrash {

		double FHinvTab=0;
	    DataSet_itf dataSet = null;
	    try {

	        DataSetFactory dsFactory = DataSetFactory.getInstance();

	        dsFactory = DataSetFactory.getInstance();
	        dataSet = dsFactory.makeDataSet("", DATASET_FRAZIONE_CONSUMO_INVOLONTARIO_TABELLA);
	        HashMap<String, String> params = new HashMap<String, String>();
	        params.put("STABILE", stabile.toString());
	        
	        dataSet.setParam(params);
	        dataSet.open();

	        while (dataSet.hasMoreElements()) {
	            Row_itf dbRow = (Row_itf) dataSet.nextElement();
	            FHinvTab = (double) dbRow.getField("DESCRIZIONE");
	            if (FHinvTab==0){
	            	FHinvTab=(double) (perditeImpianto/(fabbisognoAnnuoClima+fabbisognoAnnuoACS));
	            }
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
	        return FHinvTab;
	    }
    
    
private double trovaLetturaConsumoContatoreCondominio(String dataDal, String dataAl, Integer stabile, String tabella) throws AppCrash {

	Float letturaContatore=(float) 0;
    DataSet_itf dataSet = null;
    try {

        DataSetFactory dsFactory = DataSetFactory.getInstance();

        dsFactory = DataSetFactory.getInstance();
        dataSet = dsFactory.makeDataSet("", DATASET_CONSUMO_CONTATORE_CONDOMINIO);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("STABILE", stabile.toString());
        params.put("DATA_DAL", dataDal);
        params.put("DATA_AL", dataAl);
        params.put("TABELLA", tabella);
        
        
        dataSet.setParam(params);
        dataSet.open();

        while (dataSet.hasMoreElements()) {
            Row_itf dbRow = (Row_itf) dataSet.nextElement();
            letturaContatore = Float.parseFloat(dbRow.getField("LETTURA_CONTATORE").toString());
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
        return letturaContatore;
    }



	private Integer[] contaContatoriRipartitori(String dataDal, String dataAl, Integer stabile) throws AppCrash {

	Integer[] arrayNumeroContatoriRipartitori=new Integer[5];
    DataSet_itf dataSet = null;
    try {

        DataSetFactory dsFactory = DataSetFactory.getInstance();

        dsFactory = DataSetFactory.getInstance();
        dataSet = dsFactory.makeDataSet("", DATASET_CONTATORI_RIPARTITORI);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("STABILE", stabile.toString());
        params.put("DATA_DAL", dataDal);
        params.put("DATA_AL", dataAl);
        
        
        dataSet.setParam(params);
        dataSet.open();

        while (dataSet.hasMoreElements()) {
            Row_itf dbRow = (Row_itf) dataSet.nextElement();
            arrayNumeroContatoriRipartitori[0] = (Integer) dbRow.getField("RIPARTITORE_HCA");
            arrayNumeroContatoriRipartitori[1] = (Integer) dbRow.getField("CONTATORE_HCA");
            arrayNumeroContatoriRipartitori[2] = (Integer) dbRow.getField("CONTATORE_ACS");
            arrayNumeroContatoriRipartitori[3] = (Integer) dbRow.getField("CONTATORE_ACS_EDIFICIO");
            arrayNumeroContatoriRipartitori[4] = (Integer) dbRow.getField("CONTATORE_HCA_EDIFICIO");
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
        return arrayNumeroContatoriRipartitori;
    }

	
	
	
	private String[] datiCondominio(Integer stabile) throws AppCrash {

		String[] arrayDatiCondominio=new String[4];
	    DataSet_itf dataSet = null;
	    try {

	        DataSetFactory dsFactory = DataSetFactory.getInstance();

	        dsFactory = DataSetFactory.getInstance();
	        dataSet = dsFactory.makeDataSet("", DATASET_CONDOMINI);
	        HashMap<String, String> params = new HashMap<String, String>();
	        params.put("STABILE", stabile.toString());
	     
	        dataSet.setParam(params);
	        dataSet.open();

	        while (dataSet.hasMoreElements()) {
	            Row_itf dbRow = (Row_itf) dataSet.nextElement();
	            if (dbRow.getField("RESPONSABILE_IMPIANTO")==null){
	            	arrayDatiCondominio[0]="";
	            }else{
	            	arrayDatiCondominio[0] = dbRow.getField("RESPONSABILE_IMPIANTO").toString().trim();
	            }
	            
	            if (dbRow.getField("ID_SOFTWARE")==null){
	            	arrayDatiCondominio[1]="";
	            }else{
	            	arrayDatiCondominio[1] = dbRow.getField("ID_SOFTWARE").toString().trim();
	            }
	            
	            if (dbRow.getField("ID_TECNICO")==null){
	            	arrayDatiCondominio[2]="";
	            }else{
	            	arrayDatiCondominio[2] = dbRow.getField("ID_TECNICO").toString().trim();
	            }
	            
	            if (dbRow.getField("ID_AMMINISTRATORE")==null){
	            	arrayDatiCondominio[3]="";
	            }else{
	            	arrayDatiCondominio[3] = dbRow.getField("ID_AMMINISTRATORE").toString().trim();
	            }
	            
	            
	            
	            
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
	        return arrayDatiCondominio;
	    }
    
    
       
    
    
    
    private Float[] lettureTotaliCondominio(Integer stabile, String dataDal, String dataAl, String datasetConsumiTotali) throws AppCrash {

    	Float[] dati= new Float[2];
        DataSet_itf dataSet = null;
        try {

            DataSetFactory dsFactory = DataSetFactory.getInstance();

            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", datasetConsumiTotali);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("STABILE", stabile.toString());
            params.put("DATA_DAL", dataDal);
            params.put("DATA_AL", dataAl);
            dataSet.setParam(params);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                dati[0] = Float.parseFloat(dbRow.getField("LETTURA_CONDOMINIO").toString());
                dati[1] = Float.parseFloat(dbRow.getField("LETTURA_ACS_CONDOMINIO").toString());
                
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

        return dati;
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
