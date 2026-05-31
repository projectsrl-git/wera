
package net.projectsrl.core;

import java.util.HashMap;
import java.util.Iterator;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.DbUtils;
import net.projectsrl.db.PjDAO_base;
import net.projectsrl.wm.core.FunctionWM_base;
import net.projectsrl.wm.utils.Utils;

/**
 * FunctionInserimento
 * 
 */
public class FunctionInserimento extends FunctionWM_base {

    private String              _pageMostra            = "";
    private String              _pageElabora           = "";
    private String              _datasetTestata        = "";
    private static final String DATASET_DIPENDENTI     = "DataSetDipendenti";
    private static final String DATASET_CATASTO        = "DataSetParametriCatastoCerca";
    private static final String DATASET_CATASTO_CODICE = "DataSetParametriCatastoCodiceCerca";
    private static final String DATASET_CCNL_AZIENDA   = "DataSetCCNLAzienda";
    private static final String DATASET_NAZIONE        = "DataSetCittadinanzaCerca";

    public FunctionInserimento() {
        super();
    }

    public FunctionInserimento(ApplicationServices_itf applServices, String functionID, String functionName) {
        super(applServices, functionID, functionName);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        HashMap templateData = (HashMap) setCommonTags(req, userInfo);
        String dirAllegatiFp=_applicationSrv.getRoot()+Config.GetInstance().getProperty("cartella.upload.richieste");
		templateData.put("CARTELLA_FERIE_PERMESSI", dirAllegatiFp);
		String dirModuliTrasferte=_applicationSrv.getRoot()+Config.GetInstance().getProperty("cartella.moduli.trasferte");
        templateData.put("CARTELLA_MODULI_TRASFERTE", dirModuliTrasferte);
        String dirModuliRendicontazioni=_applicationSrv.getRoot()+Config.GetInstance().getProperty("cartella.moduli.rendicontazioni");
        templateData.put("CARTELLA_MODULI_RENDICONTAZIONI", dirModuliRendicontazioni);
        String dirRendicontazioni=_applicationSrv.getRoot()+Config.GetInstance().getProperty("cartella.upload.rendicontazioni");
        templateData.put("CARTELLA_RENDICONTAZIONI", dirRendicontazioni);
        
        String dirAllegatiFpBreve=Config.GetInstance().getProperty("cartella.upload.richieste");
    	templateData.put("CARTELLA_FERIE_PERMESSI_BREVE", dirAllegatiFpBreve);
    	String dirModuliTrasferteBreve=Config.GetInstance().getProperty("cartella.moduli.trasferte");
        templateData.put("CARTELLA_MODULI_TRASFERTE_BREVE", dirModuliTrasferteBreve);
        String dirModuliRendicontazioniBreve=Config.GetInstance().getProperty("cartella.moduli.rendicontazioni");
        templateData.put("CARTELLA_MODULI_RENDICONTAZIONI_BREVE", dirModuliRendicontazioniBreve);
        String dirRendicontazioniBreve=Config.GetInstance().getProperty("cartella.upload.rendicontazioni");
        templateData.put("CARTELLA_RENDICONTAZIONI_BREVE", dirRendicontazioniBreve);
        
        templateData.put("ID_DIPENDENTE_SESSIONE",
                (String) req.getSession(false).getAttribute("ID_DIPENDENTE_SESSIONE"));
        templateData.put("AZIENDA_SESSIONE", (String) req.getSession(false).getAttribute("AZIENDA_SESSIONE"));
        templateData.put("CATPROT", req.getField("CATPROT"));
        templateData.put("AZIENDA_CV", req.getField("AZIENDA_CV"));

        String azi = req.getField("AZIENDA_CV");

        if (req.getField("OPZIONE_INSERIMENTO_MODIFICA").equals(OPZIONE_MODIFICA)) {
            String tipologia = req.getField("TIPOLOGIA");
            templateData = loadVarStandard(templateData, req);
            templateData.put("TIPOLOGIA", tipologia);
            templateData.put("AZIENDA_CV", req.getField("AZIENDA_CV"));
            templateData.put("CATPROT", req.getField("CATPROT"));
        } else {
            templateData = loadVarStandard(templateData, req);
        }

        if (req.getField("CCNL").equals("")) {
            templateData.put("CCNL", getCCNLAzienda(azi));
        }

        if (!req.getField("ID_ALLEGATO").equals("")) {
            String queryDelete = "delete from " + req.getField("TABELLA") + " where " + req.getField("ID_TABELLA")
                    + " = '" + req.getField("ID_ALLEGATO") + "'";
            net.projectsrl.wm.utils.WMUtils.executeQuery(queryDelete);
        }

        if (!req.getField("ID_1").equals("")) {
            String queryDelete = "delete from " + req.getField("TABELLA") + " where " + req.getField("ID_1") + " = '"
                    + req.getField("ID_3") + "' AND " + req.getField("ID_2") + " = '" + req.getField("ID_4") + "'";
            net.projectsrl.wm.utils.WMUtils.executeQuery(queryDelete);
        }

        if (!req.getField("DIPENDENTE").equals("")
                && templateData.get("FUNCTIONID").equals("InserimentoRendicontazioni")) {
            templateData.put("NOMINATIVO", getDatiUtenteRichieste(req.getField("DIPENDENTE"))[0]);
            templateData.put("DATA_NASCITA", getDatiUtenteRichieste(req.getField("DIPENDENTE"))[1]);
            templateData.put("CITTA_NASCITA", getDatiUtenteRichieste(req.getField("DIPENDENTE"))[2]);
            templateData.put("PROV_NASCITA", getDatiUtenteRichieste(req.getField("DIPENDENTE"))[3]);
            templateData.put("INDIR_RESIDENZA", getDatiUtenteRichieste(req.getField("DIPENDENTE"))[4]);
            templateData.put("CITTA_RESIDENZA", getDatiUtenteRichieste(req.getField("DIPENDENTE"))[5]);
            templateData.put("CAP_RESIDENZA", getDatiUtenteRichieste(req.getField("DIPENDENTE"))[6]);
            templateData.put("COD_FISCALE", getDatiUtenteRichieste(req.getField("DIPENDENTE"))[7]);
        }

        if (!req.getField("LOCRESIDENZ").equals("")) {
            templateData.put("COD_CATASTO_RESID", getCodiceCatastoComune(req.getField("LOCRESIDENZ"))[0]);
            templateData.put("PROVRESIDENZ", getCodiceCatastoComune(req.getField("LOCRESIDENZ"))[1]);
            templateData.put("CAPRESIDENZ", getCodiceCatastoComune(req.getField("LOCRESIDENZ"))[2]);
            templateData.put("LOCRESIDENZ", req.getField("LOCRESIDENZ"));
        }
        if (!req.getField("LOCDOMICIL").equals("")) {
            templateData.put("COD_CATASTO_DOMIC", getCodiceCatastoComune(req.getField("LOCDOMICIL"))[0]);
            templateData.put("PROVDOMICIL", getCodiceCatastoComune(req.getField("LOCDOMICIL"))[1]);
            templateData.put("CAPDOMICIL", getCodiceCatastoComune(req.getField("LOCDOMICIL"))[2]);
            templateData.put("LOCDOMICIL", req.getField("LOCDOMICIL"));
        }
        if (!req.getField("LOCNASCITA").equals("")) {
            templateData.put("COD_CATASTO_NASCITA", getCodiceCatastoComune(req.getField("LOCNASCITA"))[0]);
            templateData.put("PROVNASCITA", getCodiceCatastoComune(req.getField("LOCNASCITA"))[1]);
            templateData.put("LOCNASCITA", req.getField("LOCNASCITA"));
        }

        if (!req.getField("COD_CATASTO_RESID").equals("")) {
            templateData.put("LOCRESIDENZ", getCodiceCatasto(req.getField("COD_CATASTO_RESID"))[0]);
            templateData.put("PROVRESIDENZ", getCodiceCatasto(req.getField("COD_CATASTO_RESID"))[1]);
            templateData.put("COD_CATASTO_RESID", req.getField("COD_CATASTO_RESID"));
        }
        if (!req.getField("COD_CATASTO_DOMIC").equals("")) {
            templateData.put("LOCDOMICIL", getCodiceCatasto(req.getField("COD_CATASTO_DOMIC"))[0]);
            templateData.put("PROVDOMICIL", getCodiceCatasto(req.getField("COD_CATASTO_DOMIC"))[1]);
            templateData.put("COD_CATASTO_DOMIC", req.getField("COD_CATASTO_DOMIC"));
        }
        if (!req.getField("COD_CATASTO_NASCITA").equals("")) {
            templateData.put("LOCNASCITA", getCodiceCatasto(req.getField("COD_CATASTO_NASCITA"))[0]);
            templateData.put("PROVNASCITA", getCodiceCatasto(req.getField("COD_CATASTO_NASCITA"))[1]);
            templateData.put("COD_CATASTO_NASCITA", req.getField("COD_CATASTO_NASCITA"));
        }

        if (!req.getField("CITTA").equals("")) {
            templateData.put("COD_CATASTO", getCodiceCatastoComune(req.getField("CITTA"))[0]);
            templateData.put("CITTA", req.getField("CITTA"));
        }

        if (!req.getField("IDNAZIONE").equals("")) {
            templateData.put("CITTADINANZA", getCittadinanza(req.getField("IDNAZIONE")));
            templateData.put("IDNAZIONE", req.getField("IDNAZIONE"));
        }

        templateData.put("RUOLO_SESSIONE", getSessionRole(req));
        templateData.put("SALVATO", "");
        templateData.put("SALVATO_REMINDER", "");
        _applicationSrv.displayPage(_pageMostra, templateData, setPageDatasetParam(_pageMostra, req, templateData),
                res);

    }

    private String getCCNLAzienda(String ccnl) throws AppCrash {

        String codCCNL = "";
        DataSet_itf dataSet = null;
        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_CCNL_AZIENDA);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("CCNL_AZIENDA", ccnl);
            dataSet.setParam(params);
            dataSet.open();
            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                codCCNL = dbRow.getField("CCNL").toString().trim();
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
        return codCCNL;
    }

    private String[] getCodiceCatastoComune(String comune) throws AppCrash {

        String[] dati = { "", "", "" };
        dati[0] = "";
        dati[1] = "";
        dati[2] = "";
        DataSet_itf dataSet = null;
        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_CATASTO);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("DESCRI", comune);
            dataSet.setParam(params);
            dataSet.open();
            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                dati[0] = dbRow.getField("CODICE").toString().trim().replace("CAT", "");
                dati[1] = dbRow.getField("LIBERA").toString().trim();
                dati[2] = dbRow.getField("CAP").toString().trim();
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

    private String[] getCodiceCatasto(String codice) throws AppCrash {

        String[] dati = { "", "", "" };
        dati[0] = "";
        dati[1] = "";
        dati[2] = "";
        DataSet_itf dataSet = null;
        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_CATASTO_CODICE);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("CODICE", "CAT" + codice);
            dataSet.setParam(params);
            dataSet.open();
            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                dati[0] = dbRow.getField("DESCRI").toString().trim();
                dati[1] = dbRow.getField("LIBERA").toString().trim();
                dati[2] = dbRow.getField("CAP").toString().trim();
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

    private String getCittadinanza(String nazione) throws AppCrash {

        String cittadinanza = "";
        DataSet_itf dataSet = null;
        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_NAZIONE);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("DESCRI", "STA" + nazione);
            dataSet.setParam(params);
            dataSet.open();
            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                cittadinanza = dbRow.getField("LIBERA").toString().trim();
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
        return cittadinanza;
    }

    private String[] getDatiUtenteRichieste(String idDipendente) throws AppCrash {

        String[] dati = { "", "", "", "", "", "", "", "" };
        String nominativo = "";
        String dataNascita = "";
        String cittaNascita = "";
        String provNascita = "";
        String indResidenza = "";
        String cittaResidenza = "";
        String capResidenza = "";
        String codFisc = "";
        DataSet_itf dataSet = null;
        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_DIPENDENTI);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("ID_DIPENDENTE", "'" + idDipendente + "'");
            dataSet.setParam(params);
            dataSet.open();
            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                if (idDipendente.equals(dbRow.getField("ID_DIPENDENTE").toString().trim())) {
                    nominativo = dbRow.getField("NOMINATIVO").toString().trim();
                    dataNascita = dbRow.getField("DATA_NASCITA").toString().trim();
                    cittaNascita = dbRow.getField("CITTA_NASCITA").toString().trim();
                    provNascita = dbRow.getField("PROV_NASCITA").toString().trim();
                    indResidenza = dbRow.getField("INDIR_RESIDENZA").toString().trim();
                    cittaResidenza = dbRow.getField("CITTA_RESIDENZA").toString().trim();
                    capResidenza = dbRow.getField("CAP_RESIDENZA").toString().trim();
                    codFisc = dbRow.getField("COD_FISCALE").toString().trim();
                }
                dati[0] = nominativo;
                dati[1] = dataNascita;
                dati[2] = cittaNascita;
                dati[3] = provNascita;
                dati[4] = indResidenza;
                dati[5] = cittaResidenza;
                dati[6] = capResidenza;
                dati[7] = codFisc;
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

    @SuppressWarnings("unchecked")
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        HashMap templateData = (HashMap) setCommonTags(req, userInfo);

        if (refresh(_pageElabora, req, templateData, res)) {
            return;
        }

        // pulisce template dettagli
        //
        templateData = pulisceTuttiTemplateDettagli(templateData);

        templateData = saveVarStandard(templateData, req, res);

        // se salvo i dati da ora in avanti sono in modifica dela testata
        //
        // templateData.put(OPZIONE_INSERIMENTO_MODIFICA, OPZIONE_MODIFICA); spostato nella saveVarStandard

        // quando mi appare la maschera di modifica della testata non devo avere
        // attiva l'opzione di inserimento dei dettagli altrimenti continua ad inserire
        //

        // per upload in configurazione guidata
        if (req.getField("AVANTI").equals("NO")) {
            templateData.put("OPZIONE_INSERIMENTO_MODIFICA", OPZIONE_INSERIMENTO);
        } else {
            templateData.put("OPZIONE_INSERIMENTO_MODIFICA", OPZIONE_MODIFICA);
            if (templateData.get("ESISTE").equals("SI")) {
                templateData.put("OPZIONE_INSERIMENTO_MODIFICA", OPZIONE_INSERIMENTO);
            }
            if (req.getField("AVANTI").equals("OK")) {
                templateData.put("OPZIONE_INSERIMENTO_MODIFICA", OPZIONE_MODIFICA);
            }
        }
        templateData = nessunaOpzioniDettagli(templateData);
        templateData.put("RUOLO_SESSIONE", getSessionRole(req));
        templateData.put("ID_DIPENDENTE_SESSIONE",
                (String) req.getSession(false).getAttribute("ID_DIPENDENTE_SESSIONE"));
        templateData.put("AZIENDA_SESSIONE", (String) req.getSession(false).getAttribute("AZIENDA_SESSIONE"));
        templateData.put("SALVATO", "Salvataggio effettuato con successo");
        templateData.put("SALVATO_REMINDER", "Ultimo salvataggio effettuato alle ore " + Utils.getOrario());
        _applicationSrv.displayPage(_pageElabora, templateData, setPageDatasetParam(_pageElabora, req, templateData),
                res);

    }

    /**
     * Impostare a NESSUNA_OPZIONE le OPZIONE_INSERIMENTO_MODIFICA_DETTAGLI quando mi appare la maschera di modifica
     * della testata non devo avere attiva l'opzione di inserimento dei dettagli altrimenti continua ad inserire
     * 
     * Ad esempio:
     * 
     * templateData.put(OPZIONE_INSERIMENTO_MODIFICA_DETTAGLI,NESSUNA_OPZIONE);
     * 
     */
    @SuppressWarnings("unchecked")
    protected HashMap nessunaOpzioniDettagli(HashMap templateData) {

        if (!ciSonoDettagli()) {
            return templateData;
        }

        String[] arr = getArrayDsDettagli();

        for (int i = 0; i < arr.length; i++) {

            templateData.put("OPZIONE_INSERIMENTO_MODIFICA_DETTAGLI_" + (i + 1), NESSUNA_OPZIONE);

        }

        return templateData;
    }

    @SuppressWarnings("unchecked")
    protected HashMap loadVar(HashMap templateData, SsbServletRequest req) throws AppCrash {

        return templateData;
    }

    @SuppressWarnings("unchecked")
    protected HashMap saveVar(HashMap templateData, SsbServletRequest req) throws AppCrash {

        return templateData;
    }

    @SuppressWarnings("unchecked")
    protected HashMap loadVarStandard(HashMap templateData, SsbServletRequest req) throws AppCrash {

        setTemplateDataFromRequest(templateData, req);

        templateData = loadVar(templateData, req);
        String option = req.getField(OPZIONE_INSERIMENTO_MODIFICA);

        if (option == null || option.equals("") || option.equals(OPZIONE_INSERIMENTO)) {
            templateData.put(OPZIONE_INSERIMENTO_MODIFICA, OPZIONE_INSERIMENTO);

            return templateData;

        }

        templateData = valorizzaTemplateDataDaDB(req, templateData, getDatasetTestata());

        return templateData;

    }

    @SuppressWarnings("unchecked")
    protected HashMap saveVarStandard(HashMap templateData, SsbServletRequest req, SsbServletResponse res)
            throws AppCrash {

        templateData = setTemplateDataFromRequest(templateData, req);
        // verifica esistenza codice
        String nomeCampo = req.getField("NOME_CAMPO_DA_CONTROLLARE");
        String valoreCampo = req.getField("VALORE_CAMPO_DA_CONTROLLARE");
        String dataset = req.getField("DATASET");
        String inserimentoModifica = req.getField("OPZIONE_INSERIMENTO_MODIFICA");
        String mailSalvata = getUtente(req.getField("TAGGANCIO"))[1];

        String idUnivoco = req.getField("ID_UNIVOCO");
        String idUnivocoNome = req.getField("ID_UNIVOCO_NOME");

        templateData.put("ESISTE_GIA", "");
        String esiste = "";
        if (dataset.equals("NoCtrlEsistenza")) {
            esiste = "NO";
        } else {
            esiste = getEsistenza(nomeCampo, valoreCampo, dataset, idUnivoco, idUnivocoNome)[0];
        }

        String nomeCampo2 = req.getField("NOME_CAMPO_DA_CONTROLLARE_2");
        String valoreCampo2 = req.getField("VALORE_CAMPO_DA_CONTROLLARE_2");

        if (!nomeCampo2.equals("") && !valoreCampo2.equals("")) {
            if (dataset.equals("NoCtrlEsistenza")) {
                esiste = "NO";
            } else {
                esiste = getEsistenzaDoppioCampo(nomeCampo, valoreCampo, nomeCampo2, valoreCampo2, dataset, idUnivoco,
                        idUnivocoNome)[0];
            }
        }

        templateData.put("AZIENDA_SESSIONE", (String) req.getSession(false).getAttribute("AZIENDA_SESSIONE"));
        templateData.put("ESISTE", esiste);

        if (!nomeCampo2.equals("") && !valoreCampo2.equals("")) {
            if ((esiste.equals("SI") && (!inserimentoModifica.equals(OPZIONE_MODIFICA)))
                    || (esiste.equals("SI") && (inserimentoModifica.equals(OPZIONE_MODIFICA))
                            && !getEsistenzaDoppioCampo(nomeCampo, valoreCampo, nomeCampo2, valoreCampo2, dataset,
                                    idUnivoco, idUnivocoNome)[1].equals(idUnivoco))
                    || (esiste.equals("SI") && (inserimentoModifica.equals(OPZIONE_MODIFICA))
                            && (dataset.equals("DataSetCurricul")) && (!mailSalvata.equals(req.getField("EMAIL"))))) {
                templateData.put("ESISTE", "SI");
                templateData.put("ESISTE_GIA", "SI");
            } else {
                // fine verifica esistenza codice

                templateData = saveVar(templateData, req);

                try {

                    PjDAO_base testataDAO = DbUtils.makeDAOFromDsName(getDatasetTestata());

                    testataDAO = setDAOFieldsFromRequestPrivate(req, testataDAO);

                    String option = req.getField(OPZIONE_INSERIMENTO_MODIFICA);

                    // se faccio INSERIMENTO
                    //
                    if (option == null || option.equals("") || option.equals(OPZIONE_INSERIMENTO)) {
                        testataDAO.insert();
                    } else {
                        // se faccio MODIFICA
                        testataDAO.update();
                    }

                    if (ciSonoDettagli()) {
                        templateData = elaboraTuttiDettagli(req, templateData);
                    }

                } catch (Throwable t) {
                    AppCrash ap = new AppCrash(t);
                    throw ap;
                }
                templateData.put(OPZIONE_INSERIMENTO_MODIFICA, OPZIONE_MODIFICA);
            }
            return templateData;
        } else {
            if ((esiste.equals("SI") && (!inserimentoModifica.equals(OPZIONE_MODIFICA)))
                    || (esiste.equals("SI") && (inserimentoModifica.equals(OPZIONE_MODIFICA))
                            && !getEsistenza(nomeCampo, valoreCampo, dataset, idUnivoco, idUnivocoNome)[1]
                                    .equals(idUnivoco))
                    || (esiste.equals("SI") && (inserimentoModifica.equals(OPZIONE_MODIFICA))
                            && (dataset.equals("DataSetCurricul")) && (!mailSalvata.equals(req.getField("EMAIL"))))) {
                templateData.put("ESISTE", "SI");
                templateData.put("ESISTE_GIA", "SI");
                
            } else {
                // fine verifica esistenza codice
                templateData.put("ESISTE", "");
                templateData.put("ESISTE_GIA", "");
                templateData = saveVar(templateData, req);

                try {

                    PjDAO_base testataDAO = DbUtils.makeDAOFromDsName(getDatasetTestata());

                    testataDAO = setDAOFieldsFromRequestPrivate(req, testataDAO);

                    String option = req.getField(OPZIONE_INSERIMENTO_MODIFICA);

                    // se faccio INSERIMENTO
                    //
                    if (option == null || option.equals("") || option.equals(OPZIONE_INSERIMENTO)) {
                        testataDAO.insert();
                    } else {
                        // se faccio MODIFICA
                        testataDAO.update();
                    }

                    if (ciSonoDettagli()) {
                        templateData = elaboraTuttiDettagli(req, templateData);
                    }

                } catch (Throwable t) {
                    AppCrash ap = new AppCrash(t);
                    throw ap;
                }
                templateData.put(OPZIONE_INSERIMENTO_MODIFICA, OPZIONE_MODIFICA);
            }
            return templateData;
        }

    }

    /**
     * Popola la HashMap templateData in input con tuti i campi/valori provenienti dai campi di un PjDAO_base (in input)
     * 
     * @param HashMap templateData
     * @param PjDAO_base dao
     * @return HashMap templateData
     * @throws AppCrash
     */
    @SuppressWarnings("unchecked")
    protected HashMap putTemplateFieldFromDAO(HashMap templateData, PjDAO_base dao) throws AppCrash {

        // condizioni iniziali sui parametri
        //
        ErrDetector.GetInstance().preCond(templateData != null, "putTemplateFieldFromDAO - templateData!=null");
        ErrDetector.GetInstance().preCond(dao != null, "putTemplateFieldFromDAO - dao!=null");

        Iterator daoFields = dao.iterator();

        while (daoFields != null && daoFields.hasNext()) {
            String element = (String) daoFields.next();
            String daoElement = element;
            if (element.startsWith("?")) {
                daoElement = element.substring(2);
            }
            String elementValue = dao.getField(element);
            templateData.put(daoElement, elementValue);
        }

        return templateData;
    }

    /**
     * Pulisce la HashMap templateData in input per tuti i campi provenienti dai campi di un PjDAO_base (in input)
     * 
     * @param HashMap templateData
     * @param PjDAO_base dao
     * @return HashMap templateData
     * @throws AppCrash
     */
    @SuppressWarnings("unchecked")
    protected HashMap clearTemplateFieldFromDAO(HashMap templateData, PjDAO_base dao) throws AppCrash {

        // condizioni iniziali sui parametri
        //
        ErrDetector.GetInstance().preCond(templateData != null, "putTemplateFieldFromDAO - templateData!=null");
        ErrDetector.GetInstance().preCond(dao != null, "putTemplateFieldFromDAO - dao!=null");

        Iterator daoFields = dao.iterator();

        while (daoFields != null && daoFields.hasNext()) {
            String element = (String) daoFields.next();
            if (element.startsWith("?")) {
                element = element.substring(2);
            }
            templateData.put(element, "");
        }

        return templateData;
    }

    @SuppressWarnings("unchecked")
    private HashMap pulisceTuttiTemplateDettagli(HashMap templateData) throws AppCrash {

        if (!ciSonoDettagli()) {
            return templateData;
        }

        String[] arr = getArrayDsDettagli();

        for (int i = 0; i < arr.length; i++) {
            String dsName = arr[i];

            clearTemplateFieldFromDAO(templateData, DbUtils.makeDAOFromDsName(dsName));

        }

        return templateData;
    }

    @SuppressWarnings("unchecked")
    protected HashMap elaboraTuttiDettagli(SsbServletRequest req, HashMap templateData) throws AppCrash {

        String[] arr = getArrayDsDettagli();

        for (int i = 0; i < arr.length; i++) {
            String dsName = arr[i];

            templateData = elaboraSingoloDettaglio(i, dsName, templateData, req);

        }

        return templateData;

    }

    @SuppressWarnings("unchecked")
    protected HashMap elaboraSingoloDettaglio(int i, String dsName, HashMap templateData, SsbServletRequest req)
            throws AppCrash {

        String optionDettagli = req.getField("OPZIONE_INSERIMENTO_MODIFICA_DETTAGLI_" + (i + 1));

        if (optionDettagli != null && !optionDettagli.equals("") && optionDettagli.equals(OPZIONE_INSERIMENTO)) {
            inseriscoDettaglio(req, DbUtils.makeDAOFromDsName(dsName));
            // pulisce template dettagli
            templateData = clearTemplateFieldFromDAO(templateData, DbUtils.makeDAOFromDsName(dsName));
        } else if (optionDettagli != null && !optionDettagli.equals("") && optionDettagli.equals(OPZIONE_MODIFICA)) {
            templateData = valorizzaTemplateDataDaDB(req, templateData, dsName);
        } else if (optionDettagli != null && !optionDettagli.equals("") && optionDettagli.equals(OPZIONE_CANCELLA)) {
            cancellaDettagliDaDB(req, dsName);
            templateData = clearTemplateFieldFromDAO(templateData, DbUtils.makeDAOFromDsName(dsName));
        } else if (optionDettagli != null && !optionDettagli.equals("") && optionDettagli.equals(OPZIONE_DUPLICA)) {
            duplicaDettaglio(req, dsName);
        }
        return templateData;
    }

    @SuppressWarnings("unchecked")
    private void duplicaDettaglio(SsbServletRequest req, String dsName) throws AppCrash {

        PjDAO_base dettaglioDAO = DbUtils.makeDAOFromDsName(dsName);
        dettaglioDAO.setField(dettaglioDAO.getUniqueIdentifier(), req.getField(dettaglioDAO.getUniqueIdentifier()));
        dettaglioDAO.retrieve();

        PjDAO_base dettaglioDuplicatoDAO = DbUtils.makeDAOFromDsName(dsName);

        Iterator daoFields = dettaglioDuplicatoDAO.iterator();

        while (daoFields != null && daoFields.hasNext()) {
            String element = (String) daoFields.next();
            dettaglioDuplicatoDAO.setField(element, dettaglioDAO.getField(element));
        }

        dettaglioDuplicatoDAO.setField(dettaglioDuplicatoDAO.getUniqueIdentifier(), Utils.getUnique());
        dettaglioDuplicatoDAO.insert();

    }

    protected void cancellaDettagliDaDB(SsbServletRequest req, String dsName) throws AppCrash {

        PjDAO_base dettaglioDAO = DbUtils.makeDAOFromDsName(dsName);
        dettaglioDAO.setField(dettaglioDAO.getUniqueIdentifier(), req.getField(dettaglioDAO.getUniqueIdentifier()));
        dettaglioDAO.retrieve();
        dettaglioDAO.delete();

    }

    @SuppressWarnings("unchecked")
    protected HashMap valorizzaTemplateDataDaDB(SsbServletRequest req, HashMap templateData, String dsName)
            throws AppCrash {

        PjDAO_base dao = DbUtils.makeDAOFromDsName(dsName);
        dao.setField(dao.getUniqueIdentifier(), req.getField(dao.getUniqueIdentifier()));
        dao.retrieve();

        templateData = putTemplateFieldFromDAO(templateData, dao);
        if (!req.getField("CATPROT").equals("")) {
            templateData.put("CATPROT", req.getField("CATPROT"));
        }
        if (!req.getField("AZIENDA_CV").equals("")) {
            templateData.put("AZIENDA_CV", req.getField("AZIENDA_CV"));
        }
        return templateData;
    }

    protected void inseriscoDettaglio(SsbServletRequest req, PjDAO_base dettaglioDAO) throws AppCrash {

        dettaglioDAO = setDAOFieldsFromRequestPrivate(req, dettaglioDAO);
        dettaglioDAO.setField("DAGGANCIO", req.getField("TAGGANCIO"));

        String id_dettaglio = req.getField(dettaglioDAO.getUniqueIdentifier());
        if (id_dettaglio.equals("")) {
            dettaglioDAO.setField(dettaglioDAO.getUniqueIdentifier(), Utils.getUnique());
            dettaglioDAO.insert();

        } else {
            dettaglioDAO.setField(dettaglioDAO.getUniqueIdentifier(), id_dettaglio);
            dettaglioDAO.update();
        }

    }

    /*
     * Prepara la where condition per le query di pagina Per agganciare i dettagli, l'unica condizione di JOIN che serve
     * è TAGGANGIO=DAGGANCIO
     * 
     */
    @SuppressWarnings("unchecked")
    protected HashMap prepareWhereCondition(SsbServletRequest req, HashMap<String, String> queryParameter) {

        queryParameter.put("AZIENDA_SESSIONE", (String) req.getSession(false).getAttribute("AZIENDA_SESSIONE"));
        return queryParameter;
    }

    private String[] getArrayDsDettagli() {

        String elencoDataSet = Config.GetInstance().getProperty("DS." + _datasetTestata + ".ElencoDSDettagli");
        String[] arr = elencoDataSet.split("\\,");
        return arr;
    }

    public void setPageMostra(String page) {

        _pageMostra = page;
    }

    public void setPageElabora(String page) {

        _pageElabora = page;
    }

    public void setDatasetTestata(String page) {

        _datasetTestata = page;
    }

    public String getDatasetTestata() {

        return _datasetTestata;
    }

    protected boolean ciSonoDettagli() {

        String elencoDsDettagli = Config.GetInstance().getProperty("DS." + _datasetTestata + ".ElencoDSDettagli");
        if (elencoDsDettagli != null && !elencoDsDettagli.equals("")) {
            return true;
        } else {
            return false;
        }

    }

    private String[] getEsistenza(String nomeCampo, String valoreCampo, String dataset, String idUnivoco,
            String idUnivocoNome) throws AppCrash {

        String[] dati = { "", "" };
        DataSet_itf dataSet = null;
        dati[0] = "NO";
        dati[1] = "";

        try {

            DataSetFactory dsFactory = DataSetFactory.getInstance();

            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", dataset);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(nomeCampo, valoreCampo);
            dataSet.setParam(params);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                String campoDatabase = dbRow.getField(nomeCampo).toString().trim().toUpperCase();
                if (campoDatabase.equals(valoreCampo.toUpperCase())) {
                    dati[0] = "SI";
                    dati[1] = dbRow.getField(idUnivocoNome).toString().trim().toUpperCase();
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
        return dati;
    }

    private String[] getEsistenzaDoppioCampo(String nomeCampo, String valoreCampo, String nomeCampo2,
            String valoreCampo2, String dataset, String idUnivoco, String idUnivocoNome) throws AppCrash {

        String[] dati = { "", "" };
        DataSet_itf dataSet = null;
        dati[0] = "NO";
        dati[1] = "";

        try {

            DataSetFactory dsFactory = DataSetFactory.getInstance();

            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", dataset);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(nomeCampo, valoreCampo);
            dataSet.setParam(params);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                String campoDatabase = dbRow.getField(nomeCampo).toString().trim().toUpperCase();
                String campoDatabase2 = dbRow.getField(nomeCampo2).toString().trim().toUpperCase();
                if (campoDatabase.equals(valoreCampo.toUpperCase())
                        && campoDatabase2.equals(valoreCampo2.toUpperCase())) {
                    dati[0] = "SI";
                    dati[1] = dbRow.getField(idUnivocoNome).toString().trim().toUpperCase();
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
        return dati;
    }

    private String[] getUtente(String idDipendente) throws AppCrash {

        String[] dati = { "", "", "" };
        String idTrovato = "";
        DataSet_itf dataSet = null;
        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_DIPENDENTI);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("ID_DIPENDENTE", idDipendente);
            dataSet.setParam(params);
            dataSet.open();
            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                idTrovato = dbRow.getField("ID_DIPENDENTE").toString().trim();
                if (idDipendente.equals(idTrovato)) {
                    dati[0] = "S";
                    dati[1] = dbRow.getField("EMAIL").toString().trim();
                    dati[2] = dbRow.getField("AZIENDA").toString().trim();
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
        return dati;
    }



}
