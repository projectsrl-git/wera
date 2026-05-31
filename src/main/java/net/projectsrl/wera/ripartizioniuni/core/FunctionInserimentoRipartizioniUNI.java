
package net.projectsrl.wera.ripartizioniuni.core;

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
import net.projectsrl.wera.ripartizioniuni.db.RipartizioniUNIDettagliDAO;
import net.projectsrl.wera.ripartizioniuni.db.RipartizioniUNIDettaglioSingoloDAO;
import net.projectsrl.wera.utils.WeraUtils;

public class FunctionInserimentoRipartizioniUNI extends FunctionInserimentoALIMOD_base<RipartizioniUNIDAO> {
	
	 private static final String DATASET_CONSUMI_TOTALI_CONDOMINIO = "DataSetConsumiTotaliCondominio";
     private static final String DATASET_CONSUMI_TOTALI_SINGOLI = "DataSetConsumiTotaliSingoli";
     private static final String DATASET_CONSUMO_CONTATORE = "DataSetConsumoContatoreCondominio";
     private static final String DATASET_SOMME_CONSUMI_ENERGIA_TERMICA = "DataSetSommeConsumiEnergiaTermica";
     private static final String DATASET_CONDOMINI_FABBISOGNO_PERDITE = "DataSetCondominiFabbisognoPerdite";

    public FunctionInserimentoRipartizioniUNI(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(RipartizioniUNIDAO.ID_MODULO, "");
            templateData.put(RipartizioniUNIDAO.DT_MODULO, project.misc.Utils.getStringDataOggi());
            templateData.put(RipartizioniUNIDAO.ID_UTENTE_INS, getSpecificUserInfo(userInfo).getIdUtente());
            
            templateData.put(RipartizioniUNIDAO.CC_AC_QTA, 1);
            templateData.put(RipartizioniUNIDAO.CC_FM_QTA, 1);
            templateData.put(RipartizioniUNIDAO.CC_SI_QTA, 1);
            templateData.put(RipartizioniUNIDAO.CMI_SI_QTA, 1);
            templateData.put(RipartizioniUNIDAO.CMI_CIS_QTA, 1);
            templateData.put(RipartizioniUNIDAO.CMI_CCT_QTA, 1);
            templateData.put(RipartizioniUNIDAO.CMI_CIS_QTA, 1);
            
            templateData.put(RipartizioniUNIDAO.TC_CC_RIP, 100);
            templateData.put(RipartizioniUNIDAO.TC_CFM_RIP, 100);
            templateData.put(RipartizioniUNIDAO.TC_AC_RIP, 100);
            templateData.put(RipartizioniUNIDAO.TC_AF_RIP, 100);
            templateData.put(RipartizioniUNIDAO.TC_CCT_RIP, 100);
            
            templateData.put(RipartizioniUNIDAO.CC_AC_LORDO,0);
            templateData.put(RipartizioniUNIDAO.CC_FM_LORDO,0);
            templateData.put(RipartizioniUNIDAO.CC_SI_LORDO,0);
            templateData.put(RipartizioniUNIDAO.CMI_SI_LORDO,0);
            templateData.put(RipartizioniUNIDAO.CMI_CIS_LORDO,0);
            templateData.put(RipartizioniUNIDAO.CMI_CCT_LORDO,0);
            templateData.put(RipartizioniUNIDAO.CMI_CL_LORDO,0);
            templateData.put("SALVATAGGIO_IN_CORSO",false);
            
        } else {
            PjNDAO_base rowToUpdate = new RipartizioniUNIDAO();
            String idRow = req.getField(RipartizioniUNIDAO.ID_MODULO);
            rowToUpdate.setAttribute(RipartizioniUNIDAO.ID_MODULO, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), RipartizioniUNIDAO.ID_MODULO + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
            
            templateData.put("SALVATAGGIO_IN_CORSO",rowToUpdate.getAttribute(RipartizioniUNIDAO.SALVATAGGIO_IN_CORSO));
        }
        
        
        

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

    @Override
    protected boolean isAnInsert(SsbServletRequest req) {

        return Util.IsEmpty(req.getField(RipartizioniUNIDAO.ID_MODULO));
    }
    
    
    @Override
    protected void sendResponseJSON(SsbServletResponse res, boolean result, String title, String message, RipartizioniUNIDAO formDao) {

        try {
            PrintWriter out = res.getWriter();

            Integer id = (Integer) formDao.getAttribute(AliModDAO_base.ID_MODULO);
            String nr = (String) formDao.getAttribute(AliModDAO_base.NR_MODULO);
            String stato = (String) formDao.getAttribute(AliModDAO_base.STATO);

            WeraUtils.setRipartizioneSalvataggio("RIPARTIZIONI_UNI",true,id);
            
            WeraUtils.eliminaDatiRilevatoriDuplicati();
            eliminaRipartizioneEsistente(formDao,id);
            eliminaRipartizioneEsistenteSingolo(formDao,id);
            creaRipartizione(formDao);
            
            String resultString = "{\"result\":" + result + ",\"title\":'" + title + "',\"message\":'" + message
                    + "',\"id\":" + id + ",\"nr\":'" + nr + "',\"stato\":'" + stato + "'}";
            
            
            WeraUtils.setRipartizioneSalvataggio("RIPARTIZIONI_UNI",false,id);
            out.println(resultString);
            out.close();

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "errore writing succesful response");
        }
    }
    
    private void eliminaRipartizioneEsistenteSingolo(RipartizioniUNIDAO formDao, Integer id) throws NumberFormatException, AppCrash {
		 DataSet_itf dataSet = null;
       DataSetFactory dsFactory = DataSetFactory.getInstance();
       
       Integer idDettaglio;
       
       try {
           dataSet = dsFactory.makeDataSet("", "DSRipartizioniUNICondominioSingolo");

           HashMap<String, String> param = new HashMap<String, String>();
           param.put("WHERECONDITION", "WHERE ID_MODULO="+id);
           param.put("WHERECONDITION_PROFILI_LETTURE", " IS NOT NULL ");
           dataSet.setParam(param);
           dataSet.open();

           while (dataSet.hasMoreElements()) {
               Row_itf dbRow = (Row_itf) dataSet.nextElement();

               idDettaglio= (Integer) dbRow.getField("ID_DETTAGLIO");
               RipartizioniUNIDettaglioSingoloDAO rd = new RipartizioniUNIDettaglioSingoloDAO();
          	 rd.setAttribute(RipartizioniUNIDettaglioSingoloDAO.ID_DETTAGLIO,idDettaglio);
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
    
    
    private void eliminaRipartizioneEsistente(RipartizioniUNIDAO formDao, Integer id) throws NumberFormatException, AppCrash {
		 DataSet_itf dataSet = null;
        DataSetFactory dsFactory = DataSetFactory.getInstance();
        
        Integer idDettaglio;
        
        try {
            dataSet = dsFactory.makeDataSet("", "DSRipartizioniUNICondominio");

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("WHERECONDITION", "WHERE ID_MODULO="+id);
            param.put("WHERECONDITION_PROFILI_LETTURE", " IS NOT NULL ");
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                idDettaglio= (Integer) dbRow.getField("ID_DETTAGLIO");
                RipartizioniUNIDettagliDAO rd = new RipartizioniUNIDettagliDAO();
           	 rd.setAttribute(RipartizioniUNIDettagliDAO.ID_DETTAGLIO,idDettaglio);
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
	private void creaRipartizione(RipartizioniUNIDAO formDao) throws NumberFormatException, AppCrash {

    	Integer idModulo = (Integer) formDao.getAttribute(RipartizioniUNIDAO.ID_MODULO);
    	Integer stabile = (Integer) formDao.getAttribute(RipartizioniUNIDAO.ID_CONDOMINIO);
        String dataDal = (String) formDao.getAttributeAsString(RipartizioniUNIDAO.DATA_DAL);
        String dataAl = (String) formDao.getAttributeAsString(RipartizioniUNIDAO.DATA_AL);
        Integer idAzienda = (Integer) formDao.getAttribute(RipartizioniUNIDAO.ID_AZIENDA);
        
    	String tipoRipartizione = (String) formDao.getAttributeAsString(RipartizioniUNIDAO.TIPO_RIPARTIZIONE).trim();
    	String tipoContabilizzazione = (String) formDao.getAttributeAsString(RipartizioniUNIDAO.TIPO_CONTABILIZZAZIONE).trim();
    	
    	Float generatoreConsumo=  Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNIDAO.GENERATORE_CONSUMO));
    	Float generatoreConsumoACS=  Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNIDAO.GENERATORE_CONSUMO_ACS));
    	
    	Float generatoreFabbisognoClima=  Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNIDAO.GENERATORE_FABBISOGNO_CLIMA));
    	Float generatoreFabbisognoACS=  Float.parseFloat(formDao.getAttributeAsString(RipartizioniUNIDAO.GENERATORE_FABBISOGNO_ACS));
    	
    	String datasetGeneratore="";
    	
    	
        if(tipoRipartizione.equals("L")){
            solaLettura(formDao, idModulo, stabile, dataDal, dataAl,idAzienda,generatoreFabbisognoClima,generatoreFabbisognoACS);
        }else{
        	if(tipoContabilizzazione.equals("1")){
                calcolaValoriPerRipartizioneContatoriContatori_ContabilizzazioneDiretta(formDao, idModulo, stabile, dataDal, dataAl,idAzienda,generatoreFabbisognoClima,generatoreFabbisognoACS);
            }
            if(tipoContabilizzazione.equals("2")){
            	calcolaValoriPerRipartizioneRipartitoriContatori_ContabilizzazioneIndiretta(formDao, idModulo, stabile, dataDal, dataAl,idAzienda,generatoreFabbisognoClima,generatoreFabbisognoACS);                
            }
            if(tipoContabilizzazione.equals("3")){
                calcolaValoriPerRipartizioneRipartitoriNoACS_ContabilizzazioneIndiretta(formDao, idModulo, stabile, dataDal, dataAl,idAzienda,generatoreFabbisognoClima,generatoreFabbisognoACS);                
            }
        }
        
        
        
    }
    
    
    
   

    private void calcolaValoriPerRipartizioneRipartitoriContatori_ContabilizzazioneIndiretta(RipartizioniUNIDAO formDao, Integer idModulo, Integer stabile, String dataDal, String dataAl, Integer idAzienda, Float generatoreFabbisognoClima, Float generatoreFabbisognoACS) throws AppCrash {
        
        // CALCOLI SCHEDA 5 - CONSUMI VETTORI ENERGETICI - INIZIO
        double L2_ct_1_meno_L1_ct_1=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V1_COMBUSTIBILE_CONSUMO));
        double Q1_ve_cli_1=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V1_COMBUSTIBILE_FABBISOGNO_CLIMA));
        double Q1_ve_acs_1=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V1_COMBUSTIBILE_FABBISOGNO_ACS));
        double C_ve_1=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V1_COMBUSTIBILE_COSTO_UNITARIO));
        
        double L2_ct_2_meno_L1_ct_2=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V2_ENERGIA_ELETTRICA_CONSUMO));
        double Q1_ve_cli_2=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V2_ENERGIA_ELETTRICA_FABBISOGNO_CLIMA));
        double Q1_ve_acs_2=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V2_ENERGIA_ELETTRICA_FABBISOGNO_ACS));
        double C_ve_2=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V2_ENERGIA_ELETTRICA_COSTO_UNITARIO));
        
        double Q_gn_cli_input = Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.GENERATORE_CONSUMO));          
        double Q_gn_acs_input = Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.GENERATORE_CONSUMO_ACS));   
        
        if (Q_gn_cli_input>0){
        }else{
            Q_gn_cli_input=trovaQ_gn_input(dataDal,dataAl,stabile,"v_contatori_riscaldamento");
        }
        
        if (Q_gn_acs_input>0){
        }else{
            Q_gn_acs_input=trovaQ_gn_input(dataDal,dataAl,stabile,"v_contatori_acqua_calda");
        }
        
        
        double Q1_gn_cli = Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.GENERATORE_FABBISOGNO_CLIMA));
        double Q1_gn_acs = Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.GENERATORE_FABBISOGNO_ACS));
        
        
        double K_cli_1 = Q1_ve_cli_1/(Q1_ve_cli_1+Q1_ve_acs_1);
        double K_acs_1 = Q1_ve_acs_1/(Q1_ve_cli_1+Q1_ve_acs_1);
        double Q_ve_cli_1 = (L2_ct_1_meno_L1_ct_1)*K_cli_1;
        double Q_ve_acs_1= (L2_ct_1_meno_L1_ct_1)*K_acs_1;

        double K_cli_2 =Q1_ve_cli_2/(Q1_ve_cli_2+Q1_ve_acs_2);
        double K_acs_2 = Q1_ve_acs_2/(Q1_ve_cli_2+Q1_ve_acs_2);
        double Q_ve_cli_2 = (L2_ct_2_meno_L1_ct_2)*K_cli_2;
        double Q_ve_acs_2= (L2_ct_2_meno_L1_ct_2)*K_acs_2;

        //double Q_gn_cli = Q_gn_cli_input;   // da verificare formula con Domenico - sul manuale pag.66 prospetto F.19 (forse va diviso) - va diviso solo se non ho letture del contatore di calore
        //double Q_gn_acs = Q_gn_acs_input;   // da verificare formula con Domenico - sul manuale pag.66 prospetto F.19 (forse va diviso) - va diviso solo se non ho letture del contatore di calore
        double Q_gn_cli = Q_gn_cli_input*K_cli_1;   
        double Q_gn_acs = Q_gn_acs_input*K_acs_1;

        double Q_cli = Q_gn_cli;
        double Q_acs = Q_gn_acs;
        double Q_t = Q_gn_cli+Q_gn_acs;
        
        // CALCOLI SCHEDA 5 - CONSUMI VETTORI ENERGETICI - FINE
        
        if (Q1_ve_cli_2==0){
            K_cli_2=0;
            Q_ve_cli_2=0;
        }
        if (Q1_ve_acs_2==0){
            K_acs_2=0;
            Q_ve_acs_2=0;
        }
        
        // CALCOLI SCHEDA 6 - SPESE E COSTI - INIZIO
        
        String spesaConduzioneCentraleTermica = formDao.getAttributeAsString(RipartizioniUNIDAO.CMI_CCT_LORDO);
        //String spesaTotaleImpianto = formDao.getAttributeAsString(RipartizioniUNIDAO.TC_TC_TOTALE");
        String spesaTotaleGestione = formDao.getAttributeAsString(RipartizioniUNIDAO.TC_CL_TOTALE);
        
        double S_cm=Float.valueOf(spesaConduzioneCentraleTermica);
        double S_cr=Float.valueOf(spesaTotaleGestione);
        
        double P_cli=Float.valueOf(fabbisogniPerditeCondominio(stabile,dataDal,dataAl)[0]);
        
        double Q_h_cli=generatoreFabbisognoClima;
        double Q_h_acs=generatoreFabbisognoACS;
        
        if (generatoreFabbisognoClima.isNaN() || generatoreFabbisognoClima==0){
        	Q_h_cli=Float.valueOf(fabbisogniPerditeCondominio(stabile,dataDal,dataAl)[1]);
        }
        
        if (generatoreFabbisognoACS.isNaN() || generatoreFabbisognoACS==0){
        	Q_h_acs=Float.valueOf(fabbisogniPerditeCondominio(stabile,dataDal,dataAl)[2]);
        }
        
        //double Q_h_cli=Float.valueOf(fabbisogniPerditeCondominio(stabile,dataDal,dataAl)[1]);
        //double Q_h_acs=Float.valueOf(fabbisogniPerditeCondominio(stabile,dataDal,dataAl)[2]);
        
        double S_e_cli = (C_ve_1*Q_ve_cli_1)+(C_ve_2*Q_ve_cli_2);
        double S_e_acs =  (C_ve_1*Q_ve_acs_1)+(C_ve_2*Q_ve_acs_2);
        double K_cli = Q_h_cli/(Q_h_cli+Q_h_acs);
        double K_acs = Q_h_acs/(Q_h_cli+Q_h_acs);
        double S_cm_cli = S_cm*K_cli;
        double S_cm_acs = S_cm*K_acs;
        double S_cr_cli = S_cr*K_cli;
        double S_cr_acs = S_cr*K_acs;
        double S_cli = S_e_cli+S_cm_cli+S_cr_cli; 
        double S_acs = S_e_acs+S_cm_acs+S_cr_acs;
        double S_t = S_cli+S_acs;
        double C_cli = S_e_cli/Q_cli;
        double C_acs = 0;
        if (Q_acs>0){
        	C_acs = S_e_acs/Q_acs;
        }
        double Q_inv_cli=P_cli;
        
        
        // CALCOLI SCHEDA 6 - SPESE E COSTI - FINE
        
        
        
        if (Q1_ve_cli_1==0){
            K_cli_1=0;
            Q_ve_cli_1=0;
            Q_gn_cli=0;
            Q_cli=0;
            S_e_cli=0;
            S_cli=0;
        }
        
        if (Q1_ve_acs_1==0){
            K_acs_1=0;
            Q_ve_acs_1=0;
            Q_gn_acs=0;
            Q_acs=0;
            S_e_acs=0;
        }
        
        
        if (Q1_gn_cli==0){
            K_cli=0;
        }
        if (Q1_gn_acs==0){
            K_acs=0;
        }
        
        
        if (Q_h_cli==0){
            K_cli=0;
            S_cm_cli=0;
            S_cr_cli=0;
            S_cli=0;
        }
        
        if (Q_h_acs==0){
            S_cm_acs=0;
            S_cr_acs=0;
        }
        
        if ((Q_h_cli+Q_h_acs)==0){
            K_acs=0;
        }
        
        
        
        
        
        RipartizioniUNIDAO ripartizioniuni = new RipartizioniUNIDAO();
        ripartizioniuni.setAttribute(RipartizioniUNIDAO.ID_MODULO, idModulo);

        if (ripartizioniuni.retrieve()) {
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V1_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO,WeraUtils.doubleToString(K_cli_1,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V1_COEFFICIENTE_RIPARTIZIONE_ACS,WeraUtils.doubleToString(K_acs_1,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V1_CONSUMO_RISCALDAMENTO,WeraUtils.doubleToString(Q_ve_cli_1,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V1_CONSUMO_ACS,WeraUtils.doubleToString(Q_ve_acs_1,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V2_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO,WeraUtils.doubleToString(K_cli_2,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V2_COEFFICIENTE_RIPARTIZIONE_ACS,WeraUtils.doubleToString(K_acs_2,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V2_CONSUMO_RISCALDAMENTO,WeraUtils.doubleToString(Q_ve_cli_2,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V2_CONSUMO_ACS,WeraUtils.doubleToString(Q_ve_acs_2,2));
            
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.ENERGIA_TERMICA_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO,WeraUtils.doubleToString(K_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.ENERGIA_TERMICA_COEFFICIENTE_RIPARTIZIONE_ACS,WeraUtils.doubleToString(K_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.ENERGIA_TERMICA_CONSUMO_RISCALDAMENTO,WeraUtils.doubleToString(Q_gn_cli,0));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.ENERGIA_TERMICA_CONSUMO_ACS,WeraUtils.doubleToString(Q_gn_acs,0));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.CONSUMO_TOTALE_RISCALDAMENTO,WeraUtils.doubleToString(Q_cli,0));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.CONSUMO_TOTALE_ACS,WeraUtils.doubleToString(Q_acs,0));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.CONSUMO_TOTALE_RISCALDAMENTO_E_ACS,WeraUtils.doubleToString(Q_t,0));
            
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_RISCALDAMENTO,WeraUtils.doubleToString(S_e_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_ACS,WeraUtils.doubleToString(S_e_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO,WeraUtils.doubleToString(K_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.COEFFICIENTE_RIPARTIZIONE_ACS,WeraUtils.doubleToString(K_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_CONDUZIONE_RISCALDAMENTO,WeraUtils.doubleToString(S_cm_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_CONDUZIONE_ACS,WeraUtils.doubleToString(S_cm_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_GESTIONE_RISCALDAMENTO,WeraUtils.doubleToString(S_cr_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_GESTIONE_ACS,WeraUtils.doubleToString(S_cr_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_TOTALE_RISCALDAMENTO,WeraUtils.doubleToString(S_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_TOTALE_ACS,WeraUtils.doubleToString(S_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_TOTALE_RISCALDAMENTO_ACS,WeraUtils.doubleToString(S_t,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.COSTO_UNITARIO_ENERGIA_TERMICA_RISCALDAMENTO,WeraUtils.doubleToString(C_cli,3));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.COSTO_UNITARIO_ENERGIA_TERMICA_ACS,WeraUtils.doubleToString(C_acs,3));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.CONSUMO_INVOLONTARIO,WeraUtils.doubleToString(Q_inv_cli,2));
 
            ripartizioniuni.update();
        }
        
        
 
        
        
        
        // DETTAGLI RIPARTIZIONE FASE 1- INIZIO
       
        String idCondomino="";
        String denominazione="";
        
        String numeroRipartitori="";
        
        double M_qh_cli_1=0;
        double M_qh_acs_1=0;
        double L2_cv_1_meno_L1_cv_1=0;
        double caloreSpecificoAcquaPressioneCostante=(1.162*0.001);
        double temperaturaMediaAcquaACS=48;
        double temperaturaMediaAcquaFredda=15;
        double massaVolumicaAcqua=1000;
        
        double letturaCondomino=0;
        double letturaCondominoACS=0;
        double letturaTotale=0;
        double letturaTotaleACS=0;
        
        
        double consumiCondominio=Float.valueOf(consumiTotaliCondominio(stabile,dataDal,dataAl,DATASET_CONSUMI_TOTALI_CONDOMINIO));
        //double consumiCondominioACS=Float.valueOf(consumiTotaliCondominio(stabile,dataDal,dataAl,"DataSetConsumiTotaliCondominioACS"));
        
        double totaleEnergiaTermicaRiscaldamento = 0;
        double totaleEnergiaTermicaACS = 0;
        double totalePotenzaTermicaRiscaldamento = 0;
        double totalePotenzaTermicaACS = 0;
        double totaleRiscaldamento = 0;
        double totaleACS = 0;
        double totaleAppartamento=0;
        double totaleMillesimiRiscaldamento=0;
        double totaleMillesimiACS=0;
        
        
        double consumiEnergiaRiscaldamento=Float.valueOf(sommeConsumiEnergiaTermica(idModulo)[0]);     //Q_ui_cli_1 + Q_ui_cli_2 + Q_ui_cli_3 +... somme
        double consumiEnergiaACS=Float.valueOf(sommeConsumiEnergiaTermica(idModulo)[1]);               //Q_ui_acs_1 + Q_ui_acs_2 + Q_ui_acs_3 +... somme
        
        
        double Q_ui_cli_t = consumiEnergiaRiscaldamento;
        double Q_ui_acs_t = consumiEnergiaACS;
        double Q_inv_acs = Q_acs-Q_ui_acs_t;
        
        // spese totali per consumo e potenza - inizio
        double S_ui_cli = C_cli*Q_ui_cli_t;   
        double S_ui_acs = C_acs*Q_ui_acs_t;
        double S_p_cli = (C_cli*Q_inv_cli)+S_cm_cli+S_cr_cli;
        double S_p_acs = (C_acs*Q_inv_acs)+S_cm_acs+S_cr_acs;
        // spese totali - fine
        
        
        RipartizioniUNIDettagliDAO ripartizioniunidettaglio = new RipartizioniUNIDettagliDAO();
        
        
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
                letturaCondominoACS=Float.valueOf(dbRow.getField("LETTURA_CONDOMINO_ACS").toString().trim());
                letturaCondomino = Float.valueOf(dbRow.getField("LETTURA_CONDOMINO").toString().trim());
                M_qh_cli_1 = Float.valueOf(dbRow.getField("MILLESIMI_CLIMA").toString().trim());
                M_qh_acs_1 = Float.valueOf(dbRow.getField("MILLESIMI_ACS").toString().trim());
                numeroRipartitori=dbRow.getField("NUMERO_RIPARTITORI").toString().trim();
                L2_cv_1_meno_L1_cv_1=Float.valueOf(dbRow.getField("LETTURA_CONDOMINO_ACS").toString().trim());
               
                
                double Q_ui_cli_1 = (Q_cli-Q_inv_cli)*(letturaCondomino/consumiCondominio);
                double Q_ui_acs_1 = L2_cv_1_meno_L1_cv_1 *caloreSpecificoAcquaPressioneCostante * massaVolumicaAcqua * (temperaturaMediaAcquaACS-temperaturaMediaAcquaFredda);
                
                
                double S_ui_cli_1=C_cli*Q_ui_cli_1;
                double S_ui_acs_1=C_acs*Q_ui_acs_1;
                double S_p_cli_1=S_p_cli*(M_qh_cli_1/1000);
                double S_p_acs_1=S_p_acs*(M_qh_acs_1/1000);
                double S_cli_1=S_ui_cli_1+S_p_cli_1;
                double S_acs_1=S_ui_acs_1+S_p_acs_1;
                double S_t_1=S_cli_1+S_acs_1;
                
                
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.ID_MODULO, idModulo);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.ID_UTENTE_INS, 0);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.ID_AZIENDA, idAzienda);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.ID_CONDOMINO, idCondomino);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.DENOMINAZIONE, denominazione);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.LETTURA, WeraUtils.doubleToString(letturaCondomino,0));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.LETTURA_ACS, WeraUtils.doubleToString(letturaCondominoACS,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.CONSUMI_ENERGIA_TERMICA_RISCALDAMENTO, WeraUtils.doubleToString(Q_ui_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.CONSUMI_ENERGIA_TERMICA_ACS, WeraUtils.doubleToString(Q_ui_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_ENERGIA_TERMICA_RISCALDAMENTO, WeraUtils.doubleToString(S_ui_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_ENERGIA_TERMICA_ACS, WeraUtils.doubleToString(S_ui_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_POTENZA_TERMICA_RISCALDAMENTO, WeraUtils.doubleToString(S_p_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_POTENZA_TERMICA_ACS, WeraUtils.doubleToString(S_p_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_TOTALE_RISCALDAMENTO, WeraUtils.doubleToString(S_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_TOTALE_ACS, WeraUtils.doubleToString(S_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_TOTALE_APPARTAMENTO, WeraUtils.doubleToString(S_t_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.MILLESIMI_CLIMA, WeraUtils.doubleToString(M_qh_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.MILLESIMI_ACS, WeraUtils.doubleToString(M_qh_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.NUMERO_RIPARTITORI, numeroRipartitori);
                ripartizioniunidettaglio.insert();

                
                letturaTotale=letturaTotale+letturaCondomino;
                letturaTotaleACS=letturaTotaleACS+letturaCondominoACS;
                
                totaleRiscaldamento=totaleRiscaldamento+S_cli_1;
                totaleACS=totaleACS+S_acs_1;
                totaleAppartamento = totaleRiscaldamento+totaleACS;
                totaleMillesimiRiscaldamento = totaleMillesimiRiscaldamento+M_qh_cli_1;
                totaleMillesimiACS = totaleMillesimiACS+M_qh_acs_1;
                
                
                aggiornaRipartizioniUNI(idModulo, letturaTotale, letturaTotaleACS,
                        totaleEnergiaTermicaRiscaldamento, totaleEnergiaTermicaACS, totalePotenzaTermicaRiscaldamento,
                        totalePotenzaTermicaACS, totaleRiscaldamento, totaleACS, totaleAppartamento, S_ui_cli, S_ui_acs,
                        S_p_cli, S_p_acs, totaleMillesimiRiscaldamento, totaleMillesimiACS);
                
                
                
             ripartizioniSingolo(idModulo, stabile, dataDal, dataAl, Q_cli, C_cli, C_acs, Q_inv_cli, idCondomino,
					M_qh_cli_1, M_qh_acs_1, L2_cv_1_meno_L1_cv_1, caloreSpecificoAcquaPressioneCostante,
					temperaturaMediaAcquaACS, temperaturaMediaAcquaFredda, massaVolumicaAcqua, consumiCondominio,
					totaleAppartamento, S_p_cli, S_p_acs, dsFactory);
                
                
                
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
    
    
    
    
 private void calcolaValoriPerRipartizioneRipartitoriNoACS_ContabilizzazioneIndiretta(RipartizioniUNIDAO formDao, Integer idModulo, Integer stabile, String dataDal, String dataAl, Integer idAzienda, Float generatoreFabbisognoClima, Float generatoreFabbisognoACS) throws AppCrash {
        
        // CALCOLI SCHEDA 5 - CONSUMI VETTORI ENERGETICI - INIZIO
        double L2_ct_1_meno_L1_ct_1=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V1_COMBUSTIBILE_CONSUMO));
        double Q1_ve_cli_1=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V1_COMBUSTIBILE_FABBISOGNO_CLIMA));
        double Q1_ve_acs_1=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V1_COMBUSTIBILE_FABBISOGNO_ACS));
        double C_ve_1=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V1_COMBUSTIBILE_COSTO_UNITARIO));
        
        double L2_ct_2_meno_L1_ct_2=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V2_ENERGIA_ELETTRICA_CONSUMO));
        double Q1_ve_cli_2=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V2_ENERGIA_ELETTRICA_FABBISOGNO_CLIMA));
        double Q1_ve_acs_2=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V2_ENERGIA_ELETTRICA_FABBISOGNO_ACS));
        double C_ve_2=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V2_ENERGIA_ELETTRICA_COSTO_UNITARIO));
        
        double Q_gn_cli_input = Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.GENERATORE_CONSUMO));          
        double Q_gn_acs_input = Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.GENERATORE_CONSUMO_ACS));   
        
        if (Q_gn_cli_input>0){
        }else{
            Q_gn_cli_input=trovaQ_gn_input(dataDal,dataAl,stabile,"v_contatori_riscaldamento");
        }
        
        double Q1_gn_cli = Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.GENERATORE_FABBISOGNO_CLIMA));
        double Q1_gn_acs = Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.GENERATORE_FABBISOGNO_ACS));
        
        
        double K_cli_1 = Q1_ve_cli_1/(Q1_ve_cli_1+Q1_ve_acs_1);
        double K_acs_1 = Q1_ve_acs_1/(Q1_ve_cli_1+Q1_ve_acs_1);
        double Q_ve_cli_1 = (L2_ct_1_meno_L1_ct_1)*K_cli_1;
        double Q_ve_acs_1= (L2_ct_1_meno_L1_ct_1)*K_acs_1;

        double K_cli_2 =Q1_ve_cli_2/(Q1_ve_cli_2+Q1_ve_acs_2);
        double K_acs_2 = Q1_ve_acs_2/(Q1_ve_cli_2+Q1_ve_acs_2);
        double Q_ve_cli_2 = (L2_ct_2_meno_L1_ct_2)*K_cli_2;
        double Q_ve_acs_2= (L2_ct_2_meno_L1_ct_2)*K_acs_2;

        //double Q_gn_cli = Q_gn_cli_input;   // da verificare formula con Domenico - sul manuale pag.66 prospetto F.19 (forse va diviso) - va diviso solo se non ho letture del contatore di calore
        //double Q_gn_acs = Q_gn_acs_input;   // da verificare formula con Domenico - sul manuale pag.66 prospetto F.19 (forse va diviso) - va diviso solo se non ho letture del contatore di calore
        double Q_gn_cli = Q_gn_cli_input*K_cli_1;   
        double Q_gn_acs = Q_gn_acs_input*K_acs_1;

        double Q_cli = Q_gn_cli;
        double Q_acs = Q_gn_acs;
        double Q_t = Q_gn_cli;
        
        // CALCOLI SCHEDA 5 - CONSUMI VETTORI ENERGETICI - FINE
        
        if (Q1_ve_cli_2==0){
            K_cli_2=0;
            Q_ve_cli_2=0;
        }
        if (Q1_ve_acs_2==0){
            K_acs_2=0;
            Q_ve_acs_2=0;
        }
        
        // CALCOLI SCHEDA 6 - SPESE E COSTI - INIZIO
        
        String spesaConduzioneCentraleTermica = formDao.getAttributeAsString(RipartizioniUNIDAO.CMI_CCT_LORDO);
        //String spesaTotaleImpianto = formDao.getAttributeAsString(RipartizioniUNIDAO.TC_TC_TOTALE");
        String spesaTotaleGestione = formDao.getAttributeAsString(RipartizioniUNIDAO.TC_CL_TOTALE);
        
        double S_cm=Float.valueOf(spesaConduzioneCentraleTermica);
        double S_cr=Float.valueOf(spesaTotaleGestione);
        
        double P_cli=Float.valueOf(fabbisogniPerditeCondominio(stabile,dataDal,dataAl)[0]);
        
        double Q_h_cli=generatoreFabbisognoClima;
        double Q_h_acs=generatoreFabbisognoACS;
        
        if (generatoreFabbisognoClima.isNaN() || generatoreFabbisognoClima==0){
        	Q_h_cli=Float.valueOf(fabbisogniPerditeCondominio(stabile,dataDal,dataAl)[1]);
        }
        
        if (generatoreFabbisognoACS.isNaN() || generatoreFabbisognoACS==0){
        	Q_h_acs=Float.valueOf(fabbisogniPerditeCondominio(stabile,dataDal,dataAl)[2]);
        }
        
        //double Q_h_cli=Float.valueOf(fabbisogniPerditeCondominio(stabile,dataDal,dataAl)[1]);
        //double Q_h_acs=Float.valueOf(fabbisogniPerditeCondominio(stabile,dataDal,dataAl)[2]);
        
        double S_e_cli = (C_ve_1*Q_ve_cli_1)+(C_ve_2*Q_ve_cli_2);
        double S_e_acs =  (C_ve_1*Q_ve_acs_1)+(C_ve_2*Q_ve_acs_2);
        double K_cli = Q_h_cli/(Q_h_cli+Q_h_acs);
        double K_acs = Q_h_acs/(Q_h_cli+Q_h_acs);
        double S_cm_cli = S_cm*K_cli;
        double S_cm_acs = S_cm*K_acs;
        double S_cr_cli = S_cr*K_cli;
        double S_cr_acs = S_cr*K_acs;
        double S_cli = S_e_cli+S_cm_cli+S_cr_cli; 
        double S_acs = S_e_acs+S_cm_acs+S_cr_acs;
        double S_t = S_cli;
        double C_cli = S_e_cli/Q_cli;
        double C_acs = 0;
        double Q_inv_cli=P_cli;
        
        
        // CALCOLI SCHEDA 6 - SPESE E COSTI - FINE
        
        
        
        if (Q1_ve_cli_1==0){
            K_cli_1=0;
            Q_ve_cli_1=0;
            Q_gn_cli=0;
            Q_cli=0;
            S_e_cli=0;
            S_cli=0;
        }
        
        if (Q1_ve_acs_1==0){
            K_acs_1=0;
            Q_ve_acs_1=0;
            Q_gn_acs=0;
            Q_acs=0;
            S_e_acs=0;
        }
        
        
        if (Q1_gn_cli==0){
            K_cli=0;
        }
        if (Q1_gn_acs==0){
            K_acs=0;
        }
        
        
        if (Q_h_cli==0){
            K_cli=0;
            S_cm_cli=0;
            S_cr_cli=0;
            S_cli=0;
        }
        
        if (Q_h_acs==0){
            S_cm_acs=0;
            S_cr_acs=0;
        }
        
        if ((Q_h_cli+Q_h_acs)==0){
            K_acs=0;
        }
        
        
        
        
        
        RipartizioniUNIDAO ripartizioniuni = new RipartizioniUNIDAO();
        ripartizioniuni.setAttribute(RipartizioniUNIDAO.ID_MODULO, idModulo);

        if (ripartizioniuni.retrieve()) {
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V1_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO,WeraUtils.doubleToString(K_cli_1,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V1_COEFFICIENTE_RIPARTIZIONE_ACS,WeraUtils.doubleToString(K_acs_1,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V1_CONSUMO_RISCALDAMENTO,WeraUtils.doubleToString(Q_ve_cli_1,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V1_CONSUMO_ACS,WeraUtils.doubleToString(Q_ve_acs_1,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V2_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO,WeraUtils.doubleToString(K_cli_2,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V2_COEFFICIENTE_RIPARTIZIONE_ACS,WeraUtils.doubleToString(K_acs_2,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V2_CONSUMO_RISCALDAMENTO,WeraUtils.doubleToString(Q_ve_cli_2,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V2_CONSUMO_ACS,WeraUtils.doubleToString(Q_ve_acs_2,2));
            
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.ENERGIA_TERMICA_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO,WeraUtils.doubleToString(K_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.ENERGIA_TERMICA_COEFFICIENTE_RIPARTIZIONE_ACS,WeraUtils.doubleToString(K_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.ENERGIA_TERMICA_CONSUMO_RISCALDAMENTO,WeraUtils.doubleToString(Q_gn_cli,0));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.ENERGIA_TERMICA_CONSUMO_ACS,WeraUtils.doubleToString(Q_gn_acs,0));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.CONSUMO_TOTALE_RISCALDAMENTO,WeraUtils.doubleToString(Q_cli,0));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.CONSUMO_TOTALE_ACS,WeraUtils.doubleToString(Q_acs,0));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.CONSUMO_TOTALE_RISCALDAMENTO_E_ACS,WeraUtils.doubleToString(Q_t,0));
            
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_RISCALDAMENTO,WeraUtils.doubleToString(S_e_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_ACS,WeraUtils.doubleToString(S_e_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO,WeraUtils.doubleToString(K_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.COEFFICIENTE_RIPARTIZIONE_ACS,WeraUtils.doubleToString(K_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_CONDUZIONE_RISCALDAMENTO,WeraUtils.doubleToString(S_cm_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_CONDUZIONE_ACS,WeraUtils.doubleToString(S_cm_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_GESTIONE_RISCALDAMENTO,WeraUtils.doubleToString(S_cr_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_GESTIONE_ACS,WeraUtils.doubleToString(S_cr_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_TOTALE_RISCALDAMENTO,WeraUtils.doubleToString(S_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_TOTALE_ACS,WeraUtils.doubleToString(S_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_TOTALE_RISCALDAMENTO_ACS,WeraUtils.doubleToString(S_t,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.COSTO_UNITARIO_ENERGIA_TERMICA_RISCALDAMENTO,WeraUtils.doubleToString(C_cli,3));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.COSTO_UNITARIO_ENERGIA_TERMICA_ACS,WeraUtils.doubleToString(C_acs,3));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.CONSUMO_INVOLONTARIO,WeraUtils.doubleToString(Q_inv_cli,2));
 
            ripartizioniuni.update();
        }
        
        
 
        
        
        
        // DETTAGLI RIPARTIZIONE FASE 1- INIZIO
       
        String idCondomino="";
        String denominazione="";
        
        String numeroRipartitori="";
        
        double M_qh_cli_1=0;
        double M_qh_acs_1=0;
        double L2_cv_1_meno_L1_cv_1=0;
        double caloreSpecificoAcquaPressioneCostante=(1.162*0.001);
        double temperaturaMediaAcquaACS=48;
        double temperaturaMediaAcquaFredda=15;
        double massaVolumicaAcqua=1000;
        
        double letturaCondomino=0;
        double letturaCondominoACS=0;
        double letturaTotale=0;
        double letturaTotaleACS=0;
        
        
        double consumiCondominio=Float.valueOf(consumiTotaliCondominio(stabile,dataDal,dataAl,DATASET_CONSUMI_TOTALI_CONDOMINIO));
        //double consumiCondominioACS=Float.valueOf(consumiTotaliCondominio(stabile,dataDal,dataAl,"DataSetConsumiTotaliCondominioACS"));
        
        double totaleEnergiaTermicaRiscaldamento = 0;
        double totaleEnergiaTermicaACS = 0;
        double totalePotenzaTermicaRiscaldamento = 0;
        double totalePotenzaTermicaACS = 0;
        double totaleRiscaldamento = 0;
        double totaleACS = 0;
        double totaleAppartamento=0;
        double totaleMillesimiRiscaldamento=0;
        double totaleMillesimiACS=0;
        
        
        double consumiEnergiaRiscaldamento=Float.valueOf(sommeConsumiEnergiaTermica(idModulo)[0]);     //Q_ui_cli_1 + Q_ui_cli_2 + Q_ui_cli_3 +... somme
        double consumiEnergiaACS=Float.valueOf(sommeConsumiEnergiaTermica(idModulo)[1]);               //Q_ui_acs_1 + Q_ui_acs_2 + Q_ui_acs_3 +... somme
        
        
        double Q_ui_cli_t = consumiEnergiaRiscaldamento;
        double Q_ui_acs_t = consumiEnergiaACS;
        double Q_inv_acs = Q_acs-Q_ui_acs_t;
        
        // spese totali per consumo e potenza - inizio
        double S_ui_cli = C_cli*Q_ui_cli_t;   
        double S_ui_acs = C_acs*Q_ui_acs_t;
        double S_p_cli = (C_cli*Q_inv_cli)+S_cm_cli+S_cr_cli;
        double S_p_acs = (C_acs*Q_inv_acs)+S_cm_acs+S_cr_acs;
        // spese totali - fine
        
        
        RipartizioniUNIDettagliDAO ripartizioniunidettaglio = new RipartizioniUNIDettagliDAO();
        
        
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
                M_qh_cli_1 = Float.valueOf(dbRow.getField("MILLESIMI_CLIMA").toString().trim());
                numeroRipartitori=dbRow.getField("NUMERO_RIPARTITORI").toString().trim();
               
                
                double Q_ui_cli_1 = (Q_cli-Q_inv_cli)*(letturaCondomino/consumiCondominio);
                double Q_ui_acs_1 = L2_cv_1_meno_L1_cv_1 *caloreSpecificoAcquaPressioneCostante * massaVolumicaAcqua * (temperaturaMediaAcquaACS-temperaturaMediaAcquaFredda);
                
                
                double S_ui_cli_1=C_cli*Q_ui_cli_1;
                double S_ui_acs_1=0;
                double S_p_cli_1=S_p_cli*(M_qh_cli_1/1000);
                double S_p_acs_1=0;
                double S_cli_1=S_ui_cli_1+S_p_cli_1;
                double S_acs_1=S_ui_acs_1+S_p_acs_1;
                double S_t_1=S_cli_1;
                
                
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.ID_MODULO, idModulo);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.ID_UTENTE_INS, 0);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.ID_AZIENDA, idAzienda);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.ID_CONDOMINO, idCondomino);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.DENOMINAZIONE, denominazione);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.LETTURA, WeraUtils.doubleToString(letturaCondomino,0));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.LETTURA_ACS, WeraUtils.doubleToString(letturaCondominoACS,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.CONSUMI_ENERGIA_TERMICA_RISCALDAMENTO, WeraUtils.doubleToString(Q_ui_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.CONSUMI_ENERGIA_TERMICA_ACS, WeraUtils.doubleToString(Q_ui_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_ENERGIA_TERMICA_RISCALDAMENTO, WeraUtils.doubleToString(S_ui_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_ENERGIA_TERMICA_ACS, WeraUtils.doubleToString(S_ui_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_POTENZA_TERMICA_RISCALDAMENTO, WeraUtils.doubleToString(S_p_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_POTENZA_TERMICA_ACS, WeraUtils.doubleToString(S_p_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_TOTALE_RISCALDAMENTO, WeraUtils.doubleToString(S_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_TOTALE_ACS, WeraUtils.doubleToString(S_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_TOTALE_APPARTAMENTO, WeraUtils.doubleToString(S_t_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.MILLESIMI_CLIMA, WeraUtils.doubleToString(M_qh_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.MILLESIMI_ACS, WeraUtils.doubleToString(M_qh_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.NUMERO_RIPARTITORI, numeroRipartitori);
                ripartizioniunidettaglio.insert();

                
                letturaTotale=letturaTotale+letturaCondomino;
                letturaTotaleACS=letturaTotaleACS+letturaCondominoACS;
                
                totaleRiscaldamento=totaleRiscaldamento+S_cli_1;
                totaleACS=totaleACS+S_acs_1;
                totaleAppartamento = totaleRiscaldamento+totaleACS;
                totaleMillesimiRiscaldamento = totaleMillesimiRiscaldamento+M_qh_cli_1;
                totaleMillesimiACS = totaleMillesimiACS+M_qh_acs_1;
                
                
                aggiornaRipartizioniUNI(idModulo, letturaTotale, letturaTotaleACS,
                        totaleEnergiaTermicaRiscaldamento, totaleEnergiaTermicaACS, totalePotenzaTermicaRiscaldamento,
                        totalePotenzaTermicaACS, totaleRiscaldamento, totaleACS, totaleAppartamento, S_ui_cli, S_ui_acs,
                        S_p_cli, S_p_acs, totaleMillesimiRiscaldamento, totaleMillesimiACS);
                
                
                
             ripartizioniSingolo(idModulo, stabile, dataDal, dataAl, Q_cli, C_cli, C_acs, Q_inv_cli, idCondomino,
					M_qh_cli_1, M_qh_acs_1, L2_cv_1_meno_L1_cv_1, caloreSpecificoAcquaPressioneCostante,
					temperaturaMediaAcquaACS, temperaturaMediaAcquaFredda, massaVolumicaAcqua, consumiCondominio,
					totaleAppartamento, S_p_cli, S_p_acs, dsFactory);
                
                
                
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
 

	private void ripartizioniSingolo(Integer idModulo, Integer stabile, String dataDal, String dataAl, double Q_cli,
			double C_cli, double C_acs, double Q_inv_cli, String idCondomino, double M_qh_cli_1, double M_qh_acs_1,
			double L2_cv_1_meno_L1_cv_1, double caloreSpecificoAcquaPressioneCostante, double temperaturaMediaAcquaACS,
			double temperaturaMediaAcquaFredda, double massaVolumicaAcqua, double consumiCondominio,
			double totaleAppartamento, double S_p_cli, double S_p_acs, DataSetFactory dsFactory) throws AppCrash {
		String denominazione;
		double letturaCondomino;
		double letturaCondominoACS;
		double Q_ui_cli_1;
		double Q_ui_acs_1;
		double S_ui_cli_1;
		double S_ui_acs_1;
		double S_p_cli_1;
		double S_p_acs_1;
		double S_cli_1;
		double S_acs_1;
		double S_t_1;
		// ripartizioni singolo inizio
		    DataSet_itf dataSet2 = null;
		    DataSetFactory dsFactory2 = DataSetFactory.getInstance();
		    dsFactory2 = DataSetFactory.getInstance();
		    String stanza="";
		    String rilevatore="";
		    String numeroRipartitori="";
		    try {
		        dataSet2 = dsFactory2.makeDataSet("", "DSDatiRipartizioniUNISingolo");

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
		            
		            Q_ui_cli_1 = (Q_cli-Q_inv_cli)*(letturaCondomino/consumiCondominio);
		            Q_ui_acs_1 = L2_cv_1_meno_L1_cv_1 *caloreSpecificoAcquaPressioneCostante * massaVolumicaAcqua * (temperaturaMediaAcquaACS-temperaturaMediaAcquaFredda);
		            
		            
		            S_ui_cli_1=C_cli*Q_ui_cli_1;
		            S_ui_acs_1=C_acs*Q_ui_acs_1;
		            S_p_cli_1=S_p_cli*(M_qh_cli_1/1000);
		            S_p_acs_1=S_p_acs*(M_qh_acs_1/1000);
		            S_cli_1=S_ui_cli_1+S_p_cli_1;
		            S_acs_1=S_ui_acs_1+S_p_acs_1;
		            S_t_1=S_cli_1+S_acs_1;
		            
		             
		            RipartizioniUNIDettaglioSingoloDAO rds = new RipartizioniUNIDettaglioSingoloDAO();
		            
		        	 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.ID_MODULO,idModulo);
		        	 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.ID_CONDOMINO,idCondomino);
					 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.DENOMINAZIONE,denominazione);
					 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.LETTURA,WeraUtils.doubleToString(letturaCondomino,2));
					 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.LETTURA_ACS,WeraUtils.doubleToString(letturaCondominoACS,2));
					 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.STANZA,stanza);
					 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.RILEVATORE,rilevatore);
					 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.NUMERO_RIPARTITORI,numeroRipartitori);
					 
					 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.CONSUMI_ENERGIA_TERMICA_RISCALDAMENTO, WeraUtils.doubleToString(Q_ui_cli_1,2));
					 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.CONSUMI_ENERGIA_TERMICA_ACS, WeraUtils.doubleToString(Q_ui_acs_1,2));
					 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.SPESA_ENERGIA_TERMICA_RISCALDAMENTO, WeraUtils.doubleToString(S_ui_cli_1,2));
					 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.SPESA_ENERGIA_TERMICA_ACS, WeraUtils.doubleToString(S_ui_acs_1,2));
					 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.SPESA_POTENZA_TERMICA_RISCALDAMENTO, WeraUtils.doubleToString(S_p_cli_1,2));
					 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.SPESA_POTENZA_TERMICA_ACS, WeraUtils.doubleToString(S_p_acs_1,2));
					 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.SPESA_TOTALE_RISCALDAMENTO, WeraUtils.doubleToString(S_cli_1,2));
					 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.SPESA_TOTALE_ACS, WeraUtils.doubleToString(S_acs_1,2));
					 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.SPESA_TOTALE_APPARTAMENTO, WeraUtils.doubleToString(S_t_1,2));
					 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.SPESA_TOTALE_APPARTAMENTO,totaleAppartamento);
					 
					 rds.setAttribute(RipartizioniUNIDettaglioSingoloDAO.ID_UTENTE_INS,0);
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
    
    
    
    
    
    
private double trovaQ_gn_input(String dataDal, String dataAl, Integer stabile, String tabella) throws AppCrash {

    double letturaContatore=0.00;
    DataSet_itf dataSet = null;
    try {

        DataSetFactory dsFactory = DataSetFactory.getInstance();

        dsFactory = DataSetFactory.getInstance();
        dataSet = dsFactory.makeDataSet("", DATASET_CONSUMO_CONTATORE);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("STABILE", stabile.toString());
        params.put("DATA_DAL", dataDal);
        params.put("DATA_AL", dataAl);
        params.put("TABELLA", tabella);
        
        
        dataSet.setParam(params);
        dataSet.open();

        while (dataSet.hasMoreElements()) {
            Row_itf dbRow = (Row_itf) dataSet.nextElement();
            //letturaContatore = (Double) dbRow.getField("LETTURA_CONTATORE");
            letturaContatore = Float.valueOf(dbRow.getField("LETTURA_CONTATORE").toString().trim());
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

private void solaLettura(RipartizioniUNIDAO formDao, Integer idModulo, Integer stabile, String dataDal, String dataAl, Integer idAzienda, Float generatoreFabbisognoClima, Float generatoreFabbisognoACS) throws AppCrash {
        
        // CALCOLI SCHEDA 5 - CONSUMI VETTORI ENERGETICI - INIZIO
		
        double L2_ct_1_meno_L1_ct_1=0;
        double Q1_ve_cli_1=0;
        double Q1_ve_acs_1=0;
        double C_ve_1=0;
        
        double L2_ct_2_meno_L1_ct_2=0;
        double Q1_ve_cli_2=0;
        double Q1_ve_acs_2=0;
        double C_ve_2=0;
        
        double Q_gn_cli_input = 0;
        double Q_gn_acs_input = 0;
        double Q1_gn_cli = 0;
        double Q1_gn_acs = 0;
        
        
        
        double K_cli_1 = 0;
        double K_acs_1 =0;
        double Q_ve_cli_1 = 0;
        double Q_ve_acs_1= 0;

        double K_cli_2 =0;
        double K_acs_2 = 0;
        double Q_ve_cli_2 = 0;
        double Q_ve_acs_2= 0;

        double Q_gn_cli = 0;
        double Q_gn_acs = 0;

        double Q_cli = 0;
        double Q_acs = 0;
        double Q_t = 0;
        
        // CALCOLI SCHEDA 5 - CONSUMI VETTORI ENERGETICI - FINE
        
        if (Q1_ve_cli_2==0){
            K_cli_2=0;
            Q_ve_cli_2=0;
        }
        if (Q1_ve_acs_2==0){
            K_acs_2=0;
            Q_ve_acs_2=0;
        }
        
        // CALCOLI SCHEDA 6 - SPESE E COSTI - INIZIO
        
        String spesaConduzioneCentraleTermica = "0";
        //String spesaTotaleImpianto = formDao.getAttributeAsString(RipartizioniUNIDAO.TC_TC_TOTALE");
        String spesaTotaleGestione = "0";
        
        double S_cm=Float.valueOf(spesaConduzioneCentraleTermica);
        double S_cr=Float.valueOf(spesaTotaleGestione);
        
        
        double Q_h_cli=0;
        double Q_h_acs=0;
        
        double consumiEnergiaContatoriCalore=Float.valueOf(sommeConsumiEnergiaTermica(idModulo)[0]);     //Q_ui_cli_1 + Q_ui_cli_2 + Q_ui_cli_3 +... somme
        double consumiEnergiaACS=Float.valueOf(sommeConsumiEnergiaTermica(idModulo)[1]);               //Q_ui_acs_1 + Q_ui_acs_2 + Q_ui_acs_3 +... somme
        double Q_ui_cli_t = consumiEnergiaContatoriCalore;
        double Q_ui_acs_t = consumiEnergiaACS;
        
        double S_e_cli = 0;
        double S_e_acs =  0;
        double K_cli = 0;
        double K_acs = 0;
        double S_cm_cli = 0;
        double S_cm_acs = 0;
        double S_cr_cli = 0;
        double S_cr_acs = 0;
        double S_cli = 0; 
        double S_acs = 0;
        double S_t = 0;
        double C_cli = 0;
        double C_acs = 0;
        double Q_inv_cli=0;
        
        
        // CALCOLI SCHEDA 6 - SPESE E COSTI - FINE
        
        
        
        if (Q1_ve_cli_1==0){
            K_cli_1=0;
            Q_ve_cli_1=0;
            Q_gn_cli=0;
            Q_cli=0;
            S_e_cli=0;
            S_cli=0;
        }
        
        if (Q1_ve_acs_1==0){
            K_acs_1=0;
            Q_ve_acs_1=0;
            Q_gn_acs=0;
            Q_acs=0;
            S_e_acs=0;
        }
        
        
        if (Q1_gn_cli==0){
            K_cli=0;
        }
        if (Q1_gn_acs==0){
            K_acs=0;
        }
        
        
        if (Q_h_cli==0){
            K_cli=0;
            S_cm_cli=0;
            S_cr_cli=0;
            S_cli=0;
        }
        
        if (Q_h_acs==0){
            S_cm_acs=0;
            S_cr_acs=0;
        }
        
        if ((Q_h_cli+Q_h_acs)==0){
            K_acs=0;
        }
        
        
        
        
        
        RipartizioniUNIDAO ripartizioniuni = new RipartizioniUNIDAO();
        ripartizioniuni.setAttribute(RipartizioniUNIDAO.ID_MODULO, idModulo);

        if (ripartizioniuni.retrieve()) {
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V1_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO,WeraUtils.doubleToString(K_cli_1,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V1_COEFFICIENTE_RIPARTIZIONE_ACS,WeraUtils.doubleToString(K_acs_1,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V1_CONSUMO_RISCALDAMENTO,WeraUtils.doubleToString(Q_ve_cli_1,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V1_CONSUMO_ACS,WeraUtils.doubleToString(Q_ve_acs_1,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V2_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO,WeraUtils.doubleToString(K_cli_2,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V2_COEFFICIENTE_RIPARTIZIONE_ACS,WeraUtils.doubleToString(K_acs_2,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V2_CONSUMO_RISCALDAMENTO,WeraUtils.doubleToString(Q_ve_cli_2,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V2_CONSUMO_ACS,WeraUtils.doubleToString(Q_ve_acs_2,2));
            
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.ENERGIA_TERMICA_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO,WeraUtils.doubleToString(K_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.ENERGIA_TERMICA_COEFFICIENTE_RIPARTIZIONE_ACS,WeraUtils.doubleToString(K_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.ENERGIA_TERMICA_CONSUMO_RISCALDAMENTO,WeraUtils.doubleToString(Q_gn_cli,0));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.ENERGIA_TERMICA_CONSUMO_ACS,WeraUtils.doubleToString(Q_gn_acs,0));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.CONSUMO_TOTALE_RISCALDAMENTO,WeraUtils.doubleToString(Q_cli,0));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.CONSUMO_TOTALE_ACS,WeraUtils.doubleToString(Q_acs,0));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.CONSUMO_TOTALE_RISCALDAMENTO_E_ACS,WeraUtils.doubleToString(Q_t,0));
            
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_RISCALDAMENTO,WeraUtils.doubleToString(S_e_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_ACS,WeraUtils.doubleToString(S_e_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO,WeraUtils.doubleToString(K_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.COEFFICIENTE_RIPARTIZIONE_ACS,WeraUtils.doubleToString(K_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_CONDUZIONE_RISCALDAMENTO,WeraUtils.doubleToString(S_cm_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_CONDUZIONE_ACS,WeraUtils.doubleToString(S_cm_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_GESTIONE_RISCALDAMENTO,WeraUtils.doubleToString(S_cr_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_GESTIONE_ACS,WeraUtils.doubleToString(S_cr_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_TOTALE_RISCALDAMENTO,WeraUtils.doubleToString(S_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_TOTALE_ACS,WeraUtils.doubleToString(S_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_TOTALE_RISCALDAMENTO_ACS,WeraUtils.doubleToString(S_t,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.COSTO_UNITARIO_ENERGIA_TERMICA_RISCALDAMENTO,WeraUtils.doubleToString(C_cli,3));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.COSTO_UNITARIO_ENERGIA_TERMICA_ACS,WeraUtils.doubleToString(C_acs,3));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.CONSUMO_INVOLONTARIO,WeraUtils.doubleToString(Q_inv_cli,2));
 
            ripartizioniuni.update();
        }
        
 
        
        
        
        // DETTAGLI RIPARTIZIONE FASE 1- INIZIO
       
        String idCondomino="";
        String denominazione="";
        
        String numeroRipartitori="";
        
        double M_qh_cli_1=0;
        double M_qh_acs_1=0;
        double L2_cv_1_meno_L1_cv_1=0;
        double L2_cc_1_meno_L1_cc_1=0;
        double caloreSpecificoAcquaPressioneCostante=(1.162*0.001);
        double temperaturaMediaAcquaACS=48;
        double temperaturaMediaAcquaFredda=15;
        double massaVolumicaAcqua=1000;
        
        double letturaCondomino=0;
        double letturaCondominoACS=0;
        double letturaTotale=0;
        double letturaTotaleACS=0;
        //double consumiCondominio=Float.valueOf(consumiTotaliCondominio(stabile,dataDal,dataAl,"DataSetConsumiTotaliCondominio"));
        //double consumiCondominioACS=Float.valueOf(consumiTotaliCondominio(stabile,dataDal,dataAl,"DataSetConsumiTotaliCondominioACS"));
        
        double consumiCondominio=Float.valueOf(consumiTotaliCondominio(stabile,dataDal,dataAl,DATASET_CONSUMI_TOTALI_CONDOMINIO));
        
        double totaleEnergiaTermicaRiscaldamento = 0;
        double totaleEnergiaTermicaACS = 0;
        double totalePotenzaTermicaRiscaldamento = 0;
        double totalePotenzaTermicaACS = 0;
        double totaleRiscaldamento = 0;
        double totaleACS = 0;
        double totaleAppartamento=0;
        double totaleMillesimiRiscaldamento=0;
        double totaleMillesimiACS=0;
        
        
       
        double Q_inv_acs = Q_acs-Q_ui_acs_t;
        
        // spese totali per consumo e potenza - inizio
        double S_ui_cli = 0;   
        double S_ui_acs = 0;
        double S_p_cli = 0;
        double S_p_acs = 0;
        // spese totali - fine
        
        
        RipartizioniUNIDettagliDAO ripartizioniunidettaglio = new RipartizioniUNIDettagliDAO();
        
        
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
                letturaCondominoACS=Float.valueOf(dbRow.getField("LETTURA_CONDOMINO_ACS").toString().trim());
                letturaCondomino = Float.valueOf(dbRow.getField("LETTURA_CONDOMINO").toString().trim());
                M_qh_cli_1 = Float.valueOf(dbRow.getField("MILLESIMI_CLIMA").toString().trim());
                M_qh_acs_1 = Float.valueOf(dbRow.getField("MILLESIMI_ACS").toString().trim());
                numeroRipartitori=dbRow.getField("NUMERO_RIPARTITORI").toString().trim();
                L2_cv_1_meno_L1_cv_1=Float.valueOf(dbRow.getField("LETTURA_CONDOMINO_ACS").toString().trim());
                L2_cc_1_meno_L1_cc_1=Float.valueOf(dbRow.getField("LETTURA_CONDOMINO_ACS").toString().trim());  // deve essere lettura del contatore di calore KwH del singolo condomino (1 per condomino invece degli N ripartitori su singolo calorifero)
               
                
                double Q_ui_cli_1 = 0;
                double Q_ui_acs_1 = 0;
                
                
                double S_ui_cli_1=0;
                double S_ui_acs_1=0;
                double S_p_cli_1=0;
                double S_p_acs_1=0;
                double S_cli_1=0;
                double S_acs_1=0;
                double S_t_1=0;
                
                
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.ID_MODULO, idModulo);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.ID_UTENTE_INS, 0);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.ID_AZIENDA, idAzienda);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.ID_CONDOMINO, idCondomino);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.DENOMINAZIONE, denominazione);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.LETTURA, WeraUtils.doubleToString(letturaCondomino,0));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.LETTURA_ACS, WeraUtils.doubleToString(letturaCondominoACS,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.CONSUMI_ENERGIA_TERMICA_RISCALDAMENTO, WeraUtils.doubleToString(Q_ui_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.CONSUMI_ENERGIA_TERMICA_ACS, WeraUtils.doubleToString(Q_ui_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_ENERGIA_TERMICA_RISCALDAMENTO, WeraUtils.doubleToString(S_ui_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_ENERGIA_TERMICA_ACS, WeraUtils.doubleToString(S_ui_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_POTENZA_TERMICA_RISCALDAMENTO, WeraUtils.doubleToString(S_p_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_POTENZA_TERMICA_ACS, WeraUtils.doubleToString(S_p_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_TOTALE_RISCALDAMENTO, WeraUtils.doubleToString(S_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_TOTALE_ACS, WeraUtils.doubleToString(S_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_TOTALE_APPARTAMENTO, WeraUtils.doubleToString(S_t_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.MILLESIMI_CLIMA, WeraUtils.doubleToString(M_qh_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.MILLESIMI_ACS, WeraUtils.doubleToString(M_qh_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.NUMERO_RIPARTITORI, numeroRipartitori);
                ripartizioniunidettaglio.insert();

                
                letturaTotale=letturaTotale+letturaCondomino;
                letturaTotaleACS=letturaTotaleACS+letturaCondominoACS;
                
                totaleRiscaldamento=totaleRiscaldamento+S_cli_1;
                totaleACS=totaleACS+S_acs_1;
                totaleAppartamento = totaleRiscaldamento+totaleACS;
                totaleMillesimiRiscaldamento = totaleMillesimiRiscaldamento+M_qh_cli_1;
                totaleMillesimiACS = totaleMillesimiACS+M_qh_acs_1;
                
                
                aggiornaRipartizioniUNI(idModulo,  letturaTotale, letturaTotaleACS,
                        totaleEnergiaTermicaRiscaldamento, totaleEnergiaTermicaACS, totalePotenzaTermicaRiscaldamento,
                        totalePotenzaTermicaACS, totaleRiscaldamento, totaleACS, totaleAppartamento, S_ui_cli, S_ui_acs,
                        S_p_cli, S_p_acs, totaleMillesimiRiscaldamento, totaleMillesimiACS);
                
                
                ripartizioniSingolo(idModulo, stabile, dataDal, dataAl, Q_cli, C_cli, C_acs, Q_inv_cli, idCondomino,
    					M_qh_cli_1, M_qh_acs_1, L2_cv_1_meno_L1_cv_1, caloreSpecificoAcquaPressioneCostante,
    					temperaturaMediaAcquaACS, temperaturaMediaAcquaFredda, massaVolumicaAcqua, consumiCondominio,
    					totaleAppartamento, S_p_cli, S_p_acs, dsFactory);
                
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
    
    
    
    
private void calcolaValoriPerRipartizioneContatoriContatori_ContabilizzazioneDiretta(RipartizioniUNIDAO formDao, Integer idModulo, Integer stabile, String dataDal, String dataAl, Integer idAzienda, Float generatoreFabbisognoClima, Float generatoreFabbisognoACS) throws AppCrash {
        
        // CALCOLI SCHEDA 5 - CONSUMI VETTORI ENERGETICI - INIZIO
        double L2_ct_1_meno_L1_ct_1=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V1_COMBUSTIBILE_CONSUMO));
        double Q1_ve_cli_1=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V1_COMBUSTIBILE_FABBISOGNO_CLIMA));
        double Q1_ve_acs_1=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V1_COMBUSTIBILE_FABBISOGNO_ACS));
        double C_ve_1=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V1_COMBUSTIBILE_COSTO_UNITARIO));
        
        double L2_ct_2_meno_L1_ct_2=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V2_ENERGIA_ELETTRICA_CONSUMO));
        double Q1_ve_cli_2=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V2_ENERGIA_ELETTRICA_FABBISOGNO_CLIMA));
        double Q1_ve_acs_2=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V2_ENERGIA_ELETTRICA_FABBISOGNO_ACS));
        double C_ve_2=Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.V2_ENERGIA_ELETTRICA_COSTO_UNITARIO));
        
        double Q_gn_cli_input = Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.GENERATORE_CONSUMO));          // da verificare formula con Domenico - sul manuale pag.66 prospetto F.19 (forse va diviso)
        double Q_gn_acs_input = Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.GENERATORE_CONSUMO_ACS));      // da verificare formula con Domenico - sul manuale pag.66 prospetto F.19 (forse va diviso)
        double Q1_gn_cli = Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.GENERATORE_FABBISOGNO_CLIMA));
        double Q1_gn_acs = Float.valueOf(formDao.getAttributeAsString(RipartizioniUNIDAO.GENERATORE_FABBISOGNO_ACS));
        
        if (Q_gn_cli_input>0){
        }else{
            Q_gn_cli_input=trovaQ_gn_input(dataDal,dataAl,stabile,"v_contatori_riscaldamento");
        }
        
        if (Q_gn_acs_input>0){
        }else{
            Q_gn_acs_input=trovaQ_gn_input(dataDal,dataAl,stabile,"v_contatori_acqua_calda");
        }
        
        
        double K_cli_1 = Q1_ve_cli_1/(Q1_ve_cli_1+Q1_ve_acs_1);
        double K_acs_1 = Q1_ve_acs_1/(Q1_ve_cli_1+Q1_ve_acs_1);
        double Q_ve_cli_1 = (L2_ct_1_meno_L1_ct_1)*K_cli_1;
        double Q_ve_acs_1= (L2_ct_1_meno_L1_ct_1)*K_acs_1;

        double K_cli_2 =Q1_ve_cli_2/(Q1_ve_cli_2+Q1_ve_acs_2);
        double K_acs_2 = Q1_ve_acs_2/(Q1_ve_cli_2+Q1_ve_acs_2);
        double Q_ve_cli_2 = (L2_ct_2_meno_L1_ct_2)*K_cli_2;
        double Q_ve_acs_2= (L2_ct_2_meno_L1_ct_2)*K_acs_2;

        //double Q_gn_cli = Q_gn_cli_input;   // da verificare formula con Domenico - sul manuale pag.66 prospetto F.19 (forse va diviso)
        //double Q_gn_acs = Q_gn_acs_input;   // da verificare formula con Domenico - sul manuale pag.66 prospetto F.19 (forse va diviso)
        double Q_gn_cli = Q_gn_cli_input*K_cli_1;   
        double Q_gn_acs = Q_gn_acs_input*K_acs_1;

        double Q_cli = Q_gn_cli;
        double Q_acs = Q_gn_acs;
        double Q_t = Q_gn_cli+Q_gn_acs;
        
        // CALCOLI SCHEDA 5 - CONSUMI VETTORI ENERGETICI - FINE
        
        if (Q1_ve_cli_2==0){
            K_cli_2=0;
            Q_ve_cli_2=0;
        }
        if (Q1_ve_acs_2==0){
            K_acs_2=0;
            Q_ve_acs_2=0;
        }
        
        // CALCOLI SCHEDA 6 - SPESE E COSTI - INIZIO
        
        String spesaConduzioneCentraleTermica = formDao.getAttributeAsString(RipartizioniUNIDAO.CMI_CCT_LORDO);
        //String spesaTotaleImpianto = formDao.getAttributeAsString(RipartizioniUNIDAO.TC_TC_TOTALE");
        String spesaTotaleGestione = formDao.getAttributeAsString(RipartizioniUNIDAO.TC_CL_TOTALE);
        
        double S_cm=Float.valueOf(WeraUtils.emptyToZero(spesaConduzioneCentraleTermica));
        double S_cr=Float.valueOf(WeraUtils.emptyToZero(spesaTotaleGestione));
        
        double Q_h_cli=generatoreFabbisognoClima;
        double Q_h_acs=generatoreFabbisognoACS;
        
        if (generatoreFabbisognoClima.isNaN() || generatoreFabbisognoClima==0){
        	Q_h_cli=Float.valueOf(fabbisogniPerditeCondominio(stabile,dataDal,dataAl)[1]);
        }
        
        if (generatoreFabbisognoACS.isNaN() || generatoreFabbisognoACS==0){
        	Q_h_acs=Float.valueOf(fabbisogniPerditeCondominio(stabile,dataDal,dataAl)[2]);
        }
        
        
        
        
        double consumiEnergiaContatoriCalore=Float.valueOf(sommeConsumiEnergiaTermica(idModulo)[0]);     //Q_ui_cli_1 + Q_ui_cli_2 + Q_ui_cli_3 +... somme
        double consumiEnergiaACS=Float.valueOf(sommeConsumiEnergiaTermica(idModulo)[1]);               //Q_ui_acs_1 + Q_ui_acs_2 + Q_ui_acs_3 +... somme
        double Q_ui_cli_t = consumiEnergiaContatoriCalore;
        double Q_ui_acs_t = consumiEnergiaACS;
        
        double S_e_cli = (C_ve_1*Q_ve_cli_1)+(C_ve_2*Q_ve_cli_2);
        double S_e_acs =  (C_ve_1*Q_ve_acs_1)+(C_ve_2*Q_ve_acs_2);
        double K_cli = Q_h_cli/(Q_h_cli+Q_h_acs);
        double K_acs = Q_h_acs/(Q_h_cli+Q_h_acs);
        double S_cm_cli = S_cm*K_cli;
        double S_cm_acs = S_cm*K_acs;
        double S_cr_cli = S_cr*K_cli;
        double S_cr_acs = S_cr*K_acs;
        double S_cli = S_e_cli+S_cm_cli+S_cr_cli; 
        double S_acs = S_e_acs+S_cm_acs+S_cr_acs;
        double S_t = S_cli+S_acs;
        double C_cli = S_e_cli/Q_cli;
        double C_acs = S_e_acs/Q_acs;
        double Q_inv_cli=Q_cli-Q_ui_cli_t;
        
        
        // CALCOLI SCHEDA 6 - SPESE E COSTI - FINE
        
        
        
        if (Q1_ve_cli_1==0){
            K_cli_1=0;
            Q_ve_cli_1=0;
            Q_gn_cli=0;
            Q_cli=0;
            S_e_cli=0;
            S_cli=0;
        }
        
        if (Q1_ve_acs_1==0){
            K_acs_1=0;
            Q_ve_acs_1=0;
            Q_gn_acs=0;
            Q_acs=0;
            S_e_acs=0;
        }
        
        
        if (Q1_gn_cli==0){
            K_cli=0;
        }
        if (Q1_gn_acs==0){
            K_acs=0;
        }
        
        
        if (Q_h_cli==0){
            K_cli=0;
            S_cm_cli=0;
            S_cr_cli=0;
            S_cli=0;
        }
        
        if (Q_h_acs==0){
            S_cm_acs=0;
            S_cr_acs=0;
        }
        
        if ((Q_h_cli+Q_h_acs)==0){
            K_acs=0;
        }
        
        
        
        
        
        RipartizioniUNIDAO ripartizioniuni = new RipartizioniUNIDAO();
        ripartizioniuni.setAttribute(RipartizioniUNIDAO.ID_MODULO, idModulo);

        if (ripartizioniuni.retrieve()) {
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V1_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO,WeraUtils.doubleToString(K_cli_1,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V1_COEFFICIENTE_RIPARTIZIONE_ACS,WeraUtils.doubleToString(K_acs_1,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V1_CONSUMO_RISCALDAMENTO,WeraUtils.doubleToString(Q_ve_cli_1,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V1_CONSUMO_ACS,WeraUtils.doubleToString(Q_ve_acs_1,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V2_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO,WeraUtils.doubleToString(K_cli_2,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V2_COEFFICIENTE_RIPARTIZIONE_ACS,WeraUtils.doubleToString(K_acs_2,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V2_CONSUMO_RISCALDAMENTO,WeraUtils.doubleToString(Q_ve_cli_2,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.V2_CONSUMO_ACS,WeraUtils.doubleToString(Q_ve_acs_2,2));
            
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.ENERGIA_TERMICA_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO,WeraUtils.doubleToString(K_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.ENERGIA_TERMICA_COEFFICIENTE_RIPARTIZIONE_ACS,WeraUtils.doubleToString(K_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.ENERGIA_TERMICA_CONSUMO_RISCALDAMENTO,WeraUtils.doubleToString(Q_gn_cli,0));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.ENERGIA_TERMICA_CONSUMO_ACS,WeraUtils.doubleToString(Q_gn_acs,0));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.CONSUMO_TOTALE_RISCALDAMENTO,WeraUtils.doubleToString(Q_cli,0));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.CONSUMO_TOTALE_ACS,WeraUtils.doubleToString(Q_acs,0));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.CONSUMO_TOTALE_RISCALDAMENTO_E_ACS,WeraUtils.doubleToString(Q_t,0));
            
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_RISCALDAMENTO,WeraUtils.doubleToString(S_e_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_ACS,WeraUtils.doubleToString(S_e_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO,WeraUtils.doubleToString(K_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.COEFFICIENTE_RIPARTIZIONE_ACS,WeraUtils.doubleToString(K_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_CONDUZIONE_RISCALDAMENTO,WeraUtils.doubleToString(S_cm_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_CONDUZIONE_ACS,WeraUtils.doubleToString(S_cm_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_GESTIONE_RISCALDAMENTO,WeraUtils.doubleToString(S_cr_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_GESTIONE_ACS,WeraUtils.doubleToString(S_cr_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_TOTALE_RISCALDAMENTO,WeraUtils.doubleToString(S_cli,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_TOTALE_ACS,WeraUtils.doubleToString(S_acs,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.SPESA_TOTALE_RISCALDAMENTO_ACS,WeraUtils.doubleToString(S_t,2));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.COSTO_UNITARIO_ENERGIA_TERMICA_RISCALDAMENTO,WeraUtils.doubleToString(C_cli,3));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.COSTO_UNITARIO_ENERGIA_TERMICA_ACS,WeraUtils.doubleToString(C_acs,3));
            ripartizioniuni.setAttribute(RipartizioniUNIDAO.CONSUMO_INVOLONTARIO,WeraUtils.doubleToString(Q_inv_cli,2));
 
            ripartizioniuni.update();
        }
        
        
 
        
        
        
        // DETTAGLI RIPARTIZIONE FASE 1- INIZIO
       
        String idCondomino="";
        String denominazione="";
        
        String numeroRipartitori="";
        
        double M_qh_cli_1=0;
        double M_qh_acs_1=0;
        double L2_cv_1_meno_L1_cv_1=0;
        double L2_cc_1_meno_L1_cc_1=0;
        double caloreSpecificoAcquaPressioneCostante=(1.162*0.001);
        double temperaturaMediaAcquaACS=48;
        double temperaturaMediaAcquaFredda=15;
        double massaVolumicaAcqua=1000;
        
        double letturaCondomino=0;
        double letturaCondominoACS=0;
        double letturaTotale=0;
        double letturaTotaleACS=0;
        //double consumiCondominio=Float.valueOf(consumiTotaliCondominio(stabile,dataDal,dataAl,"DataSetConsumiTotaliCondominio"));
        //double consumiCondominioACS=Float.valueOf(consumiTotaliCondominio(stabile,dataDal,dataAl,"DataSetConsumiTotaliCondominioACS"));
        
        double consumiCondominio=Float.valueOf(consumiTotaliCondominio(stabile,dataDal,dataAl,DATASET_CONSUMI_TOTALI_CONDOMINIO));
        
        double totaleEnergiaTermicaRiscaldamento = 0;
        double totaleEnergiaTermicaACS = 0;
        double totalePotenzaTermicaRiscaldamento = 0;
        double totalePotenzaTermicaACS = 0;
        double totaleRiscaldamento = 0;
        double totaleACS = 0;
        double totaleAppartamento=0;
        double totaleMillesimiRiscaldamento=0;
        double totaleMillesimiACS=0;
        
        
       
        double Q_inv_acs = Q_acs-Q_ui_acs_t;
        
        // spese totali per consumo e potenza - inizio
        double S_ui_cli = C_cli*Q_ui_cli_t;   
        double S_ui_acs = C_acs*Q_ui_acs_t;
        double S_p_cli = (C_cli*Q_inv_cli)+S_cm_cli+S_cr_cli;
        double S_p_acs = (C_acs*Q_inv_acs)+S_cm_acs+S_cr_acs;
        // spese totali - fine
        
        
        
        RipartizioniUNIDettagliDAO ripartizioniunidettaglio = new RipartizioniUNIDettagliDAO();
        
        
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
                letturaCondominoACS=Float.valueOf(dbRow.getField("LETTURA_CONDOMINO_ACS").toString().trim());
                letturaCondomino = Float.valueOf(dbRow.getField("LETTURA_CONDOMINO").toString().trim());
                M_qh_cli_1 = Float.valueOf(dbRow.getField("MILLESIMI_CLIMA").toString().trim());
                M_qh_acs_1 = Float.valueOf(dbRow.getField("MILLESIMI_ACS").toString().trim());
                numeroRipartitori=dbRow.getField("NUMERO_RIPARTITORI").toString().trim();
                L2_cv_1_meno_L1_cv_1=Float.valueOf(dbRow.getField("LETTURA_CONDOMINO_ACS").toString().trim());
                L2_cc_1_meno_L1_cc_1=Float.valueOf(dbRow.getField("LETTURA_CONDOMINO_ACS").toString().trim());  // deve essere lettura del contatore di calore KwH del singolo condomino (1 per condomino invece degli N ripartitori su singolo calorifero)
               
                
                double Q_ui_cli_1 = L2_cc_1_meno_L1_cc_1;
                double Q_ui_acs_1 = L2_cv_1_meno_L1_cv_1 *caloreSpecificoAcquaPressioneCostante * massaVolumicaAcqua * (temperaturaMediaAcquaACS-temperaturaMediaAcquaFredda);
                
                
                double S_ui_cli_1=C_cli*Q_ui_cli_1;
                double S_ui_acs_1=C_acs*Q_ui_acs_1;
                double S_p_cli_1=S_p_cli*(M_qh_cli_1/1000);
                double S_p_acs_1=S_p_acs*(M_qh_acs_1/1000);
                double S_cli_1=S_ui_cli_1+S_p_cli_1;
                double S_acs_1=S_ui_acs_1+S_p_acs_1;
                double S_t_1=S_cli_1+S_acs_1;
                
                
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.ID_MODULO, idModulo);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.ID_UTENTE_INS, 0);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.ID_AZIENDA, idAzienda);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.ID_CONDOMINO, idCondomino);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.DENOMINAZIONE, denominazione);
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.LETTURA, WeraUtils.doubleToString(letturaCondomino,0));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.LETTURA_ACS, WeraUtils.doubleToString(letturaCondominoACS,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.CONSUMI_ENERGIA_TERMICA_RISCALDAMENTO, WeraUtils.doubleToString(Q_ui_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.CONSUMI_ENERGIA_TERMICA_ACS, WeraUtils.doubleToString(Q_ui_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_ENERGIA_TERMICA_RISCALDAMENTO, WeraUtils.doubleToString(S_ui_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_ENERGIA_TERMICA_ACS, WeraUtils.doubleToString(S_ui_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_POTENZA_TERMICA_RISCALDAMENTO, WeraUtils.doubleToString(S_p_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_POTENZA_TERMICA_ACS, WeraUtils.doubleToString(S_p_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_TOTALE_RISCALDAMENTO, WeraUtils.doubleToString(S_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_TOTALE_ACS, WeraUtils.doubleToString(S_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.SPESA_TOTALE_APPARTAMENTO, WeraUtils.doubleToString(S_t_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.MILLESIMI_CLIMA, WeraUtils.doubleToString(M_qh_cli_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.MILLESIMI_ACS, WeraUtils.doubleToString(M_qh_acs_1,2));
                ripartizioniunidettaglio.setAttribute(RipartizioniUNIDettagliDAO.NUMERO_RIPARTITORI, numeroRipartitori);
                ripartizioniunidettaglio.insert();

                
                letturaTotale=letturaTotale+letturaCondomino;
                letturaTotaleACS=letturaTotaleACS+letturaCondominoACS;
                
                totaleRiscaldamento=totaleRiscaldamento+S_cli_1;
                totaleACS=totaleACS+S_acs_1;
                totaleAppartamento = totaleRiscaldamento+totaleACS;
                totaleMillesimiRiscaldamento = totaleMillesimiRiscaldamento+M_qh_cli_1;
                totaleMillesimiACS = totaleMillesimiACS+M_qh_acs_1;
                
                
                aggiornaRipartizioniUNI(idModulo,  letturaTotale, letturaTotaleACS,
                        totaleEnergiaTermicaRiscaldamento, totaleEnergiaTermicaACS, totalePotenzaTermicaRiscaldamento,
                        totalePotenzaTermicaACS, totaleRiscaldamento, totaleACS, totaleAppartamento, S_ui_cli, S_ui_acs,
                        S_p_cli, S_p_acs, totaleMillesimiRiscaldamento, totaleMillesimiACS);
                
                
                ripartizioniSingolo(idModulo, stabile, dataDal, dataAl, Q_cli, C_cli, C_acs, Q_inv_cli, idCondomino,
    					M_qh_cli_1, M_qh_acs_1, L2_cv_1_meno_L1_cv_1, caloreSpecificoAcquaPressioneCostante,
    					temperaturaMediaAcquaACS, temperaturaMediaAcquaFredda, massaVolumicaAcqua, consumiCondominio,
    					totaleAppartamento, S_p_cli, S_p_acs, dsFactory);
                
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

    public void aggiornaRipartizioniUNI(Integer idModulo, double letturaTotale,
            double letturaTotaleACS, double totaleEnergiaTermicaRiscaldamento, double totaleEnergiaTermicaACS,
            double totalePotenzaTermicaRiscaldamento, double totalePotenzaTermicaACS, double totaleRiscaldamento,
            double totaleACS, double totaleAppartamento, double S_ui_cli, double S_ui_acs, double S_p_cli,
            double S_p_acs, double totaleMillesimiRiscaldamento, double totaleMillesimiACS) throws AppCrash {

        RipartizioniUNIDAO ripartizioniunitotale = new RipartizioniUNIDAO();
        ripartizioniunitotale.setAttribute(RipartizioniUNIDAO.ID_MODULO, idModulo);

        
        if (ripartizioniunitotale.retrieve()) {
            ripartizioniunitotale.setAttribute(RipartizioniUNIDAO.TOTALE_ENERGIA_TERMICA_RISCALDAMENTO, WeraUtils.doubleToString(S_ui_cli,2));
            ripartizioniunitotale.setAttribute(RipartizioniUNIDAO.TOTALE_ENERGIA_TERMICA_ACS, WeraUtils.doubleToString(S_ui_acs,2));
            ripartizioniunitotale.setAttribute(RipartizioniUNIDAO.TOTALE_POTENZA_TERMICA_RISCALDAMENTO, WeraUtils.doubleToString(S_p_cli,2));
            ripartizioniunitotale.setAttribute(RipartizioniUNIDAO.TOTALE_POTENZA_TERMICA_ACS, WeraUtils.doubleToString(S_p_acs,2));
            ripartizioniunitotale.setAttribute(RipartizioniUNIDAO.TOTALE_RISCALDAMENTO, WeraUtils.doubleToString(totaleRiscaldamento,2));
            ripartizioniunitotale.setAttribute(RipartizioniUNIDAO.TOTALE_ACS, WeraUtils.doubleToString(totaleACS,2));
            ripartizioniunitotale.setAttribute(RipartizioniUNIDAO.TOTALE_APPARTAMENTO, WeraUtils.doubleToString(totaleAppartamento,2));
            ripartizioniunitotale.setAttribute(RipartizioniUNIDAO.LETTURA, WeraUtils.doubleToString(letturaTotale,0));
            ripartizioniunitotale.setAttribute(RipartizioniUNIDAO.LETTURA_ACS, WeraUtils.doubleToString(letturaTotaleACS,2));
            ripartizioniunitotale.setAttribute(RipartizioniUNIDAO.TOTALE_MILLESIMI_RISCALDAMENTO, WeraUtils.doubleToString(totaleMillesimiRiscaldamento,2));
            ripartizioniunitotale.setAttribute(RipartizioniUNIDAO.TOTALE_MILLESIMI_ACS, WeraUtils.doubleToString(totaleMillesimiACS,2));
            
            ripartizioniunitotale.update();
        }
        
    }
    

       
    
    
    
    private String consumiTotaliCondominio(Integer stabile, String dataDal, String dataAl, String datasetConsumiTotali) throws AppCrash {

        String dati="0";
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
                dati = dbRow.getField("LETTURA_CONDOMINIO").toString().trim();
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
    
    
    
    
    private String[] sommeConsumiEnergiaTermica(Integer idModulo) throws AppCrash {

        String[] dati= new String[2];
        DataSet_itf dataSet = null;
        try {

            DataSetFactory dsFactory = DataSetFactory.getInstance();
            
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_SOMME_CONSUMI_ENERGIA_TERMICA);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("ID_MODULO", idModulo.toString());
            dataSet.setParam(params);
            dataSet.open();
            

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                dati[0] = dbRow.getField("CONSUMI_ENERGIA_TERMICA_RISCALDAMENTO").toString().trim();
                dati[1] = dbRow.getField("CONSUMI_ENERGIA_TERMICA_ACS").toString().trim();
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
    
    
    
    private String[] fabbisogniPerditeCondominio(Integer stabile, String dataDal, String dataAl) throws AppCrash {

        String[] dati= new String[3];
        DataSet_itf dataSet = null;
        try {

            DataSetFactory dsFactory = DataSetFactory.getInstance();
            
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_CONDOMINI_FABBISOGNO_PERDITE);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("ID_MODULO", stabile.toString());
            dataSet.setParam(params);
            dataSet.open();
            

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                dati[0] = dbRow.getField("PERDITE_IMPIANTO").toString().trim();
                dati[1] = dbRow.getField("FABBISOGNO_ANNUO_CLIMA").toString().trim();
                dati[2] = dbRow.getField("FABBISOGNO_ANNUO_ACS").toString().trim();
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
