/*
  FreeMarkerPage.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione:

  Autore: Pietro G. e Luca M.

  Note:

  Modifiche:

 */

package net.project.servlet.gui;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.WindowDataSet_itf;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.misc.Config;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleNumber;
import freemarker.template.Template;
import freemarker.template.TemplateCollectionModel;

/**
 * Classe che rappresenta una pagina parametrizzata con freemarker.
 * <P>
 * Proprietà lette dal file di configurazione: Page.FreemarkerOld (facoltativo)= se true tutti gli oggetti sono stati
 * trasformati in stringhe, default a true Page.nomeLogico.FreemarkerOld (facoltativo)= page override, se true tutti gli
 * oggetti sono stati trasformati in stringhe, defualt prende il valore di Page.FreemarkerOld Page.nomeLogico.DSNum
 * (facoltativo) = numero di DataSet, default a zero Page.nomeLogico.Template.Name = nome del template
 * Page.DSdsNum.CampiDaFormattare (facoltativo) = stringa indicante i numeri o i nomi delle colonne dei campi da
 * formattare (importo e valuta); Page.nomeLogico.DSdsNum = nome del DataSet n. dsNum Page.nomeLogico.DSdsNum.list =
 * nome della SimpleList associata al DataSet n. dsNum DS.nomeNodoModelRoot.CampiDaFormattare (facoltativo)= stringa
 * indicante i numeri o i nomi delle colonne dei campi da formattare relativi al dataset esterno inserito al nodo della
 * modelRoot di nome 'nomeNodoModelRoot'
 */
public class FreeMarkerPage implements Page_itf {

    private Template            _template           = null;
    private TemplateFactory_itf _templateCache      = null;
    private DataSetFactory      _dsFactory          = null;
    private SimpleHash          _modelRoot          = null;
    private Vector              _externalDataSets   = null;
    private String              _freemarkerOld      = null;

    // Numero dei DataSet della pagina
    private int                 _dsCont             = 0;

    // Nome della pagina
    private String              _name               = null;

    // Nome della configurazione
    private String              _cfName             = null;

    // Vettore contenente i DataSet della pagina
    private Vector              _dsVector           = null;

    // Vettore contenente i nomi dei DataSet della pagina
    private Vector              _dsNameVector       = null;

    // Vettore contenente le hashtable dei parametri dei DataSet della pagina
    private Vector              _dsHashVector       = null;

    // Costanti indicanti i nomi delle proprietà del file di configurazione
    public static final String  PAGE                = "Page";
    public static final String  TEMPLATE            = "Template";
    public static final String  DS_NUM              = "DSNum";
    public static final String  DS                  = "DS";
    public static final String  LIST                = "list";
    public static final String  NAME                = "Name";
    public static final String  CAMPI_DA_FORMATTARE = "CampiDaFormattare";

    public FreeMarkerPage() {

    }

    /**
     * Costruttore.
     * 
     * @param java.lang.String name nome della pagina
     * @param java.lang.String cfName nome della configurazione
     * @exception net.project.errors.AppCrash
     * @roseuid 3A6D44EF00F0
     */
    public FreeMarkerPage(String name, String cfName) throws AppCrash {

        // Controllo formale dei parametri in ingresso.
        ErrDetector.GetInstance().param(name);
        ErrDetector.GetInstance().preCond(cfName != null, "FreeMarkerPage - cfName = null");

        _name = name;
        _cfName = cfName;

        _freemarkerOld = Config.GetInstance().getProperty(PAGE + "." + "FreemarkerOld", "false");
        _freemarkerOld = Config.GetInstance().getProperty(PAGE + "." + _name + "." + "FreemarkerOld", _freemarkerOld);

        // Recupero del numero di DataSet dal file di configurazione.
        String dsContString = Config.GetInstance(_cfName).getProperty(PAGE + "." + _name + "." + DS_NUM, "0");

        try {
            _dsCont = Integer.parseInt(dsContString);

        } catch (NumberFormatException nfe) {
            AppCrash ac = new AppCrash(nfe);
            ac.logContext("FreeMarkerPage", toString() + "; " + DS_NUM + " = " + dsContString);
            throw ac;

        } catch (IllegalArgumentException iae) {
            AppCrash ac = new AppCrash(iae);
            ac.logContext("FreeMarkerPage", toString() + "; " + DS_NUM + " = " + dsContString);
            throw ac;
        }

        try {
            if (_dsCont > 0) {

                // Istanziazione del vettore che conterrà i DataSet.
                _dsVector = new Vector(_dsCont);

                // Istanziazione del vettore che conterrà i nomi dei DataSet.
                _dsNameVector = new Vector(_dsCont);

                // Istanziazione del vettore che conterrà le hashtable dei parametri dei DataSet.
                _dsHashVector = new Vector(_dsCont);

                makeDataSets();
            }

        } catch (AppCrash ac) {
            ac.logContext("FreeMarkerPage", toString());
            throw ac;
        }

        // Istanziazione di _templateCache
        try {
            _templateCache = TemplateFactory.GetInstance(_cfName);

        } catch (AppCrash ac) {
            ac.logContext("FreeMarkerPage", "errore nell'istanziazione di _templateCache - " + toString());
            throw ac;
        }

        // Istanziazione di _modelRoot
        _modelRoot = new SimpleHash();
        _modelRoot.put("fmt", new FreemarkerFormat());

        // Se l'istanziazione del template e' late (a carico di qualcun altro) ho finito
        String lateTemplate = Config.GetInstance(_cfName).getProperty("Page.lateTemplate", "false");
        if (lateTemplate.equals("true")) return;

        // Istanziazione di _template
        String templateName = getTemplateName();
        try {
            _template = _templateCache.getTemplate(templateName);

        } catch (AppCrash ac) {
            ac.logContext("FreeMarkerPage", "templateName = " + templateName);
            throw ac;
        }
        ErrDetector.GetInstance().postCond(_template != null,
                "Errore nell'istanziazione di _template - templateName = " + templateName);

    }

    /**
     * Questo metodo recupera il nome del template da utilizzare per mostrare la pagina.
     * 
     * @return String nome del tamplate
     * @throws AppCrash
     */
    protected String getTemplateName() throws AppCrash {

        String tempProp = PAGE + "." + _name + "." + TEMPLATE + "." + NAME;
        String templateName = Config.GetInstance(_cfName).getProperty(tempProp);
        boolean templateNameEsistente = (templateName != null) && (templateName.length() > 0);
        ErrDetector.GetInstance().postCond(templateNameEsistente,
                "proprietà " + tempProp + " mancante nel file di configurazione");
        return templateName;
    }

    private void makeDataSets() throws AppCrash {

        try {
            _dsFactory = DataSetFactory.getInstance();
            for (int i = 0; i < _dsCont; i++) {
                String dsName = Config.GetInstance(_cfName).getProperty(createDataSetPropertyString(i));
                boolean dsNameEsistente = (dsName != null) && (dsName.length() > 0);
                ErrDetector.GetInstance().postCond(dsNameEsistente,
                        "proprietà " + createDataSetPropertyString(i) + " mancante nel file di configurazione");
                DataSet_itf ds = _dsFactory.makeDataSet(_cfName, dsName);
                _dsVector.insertElementAt(ds, i);
                if (Logger.GetInstance().getLogLevel() >= 3) {
                    Logger.GetInstance().log3("caricamento DataSet n. " + i + " ok");
                }
                _dsNameVector.insertElementAt(dsName, i);
                if (Logger.GetInstance().getLogLevel() >= 3) {
                    Logger.GetInstance().log3("caricamento nome del DataSet n. " + i + " ok");
                }
            }

        } catch (ArrayIndexOutOfBoundsException aioobe) {
            AppCrash ac = new AppCrash(aioobe);
            ac.logContext("FreeMarkerPage", toString());
            throw ac;
        } catch (AppCrash ac) {
            ac.logContext("FreeMarkerPage", toString());
            throw ac;
        }

    }

    /**
     * Questo metodo ritorna il nome della pagina
     * 
     * @return String nome della pagina
     * 
     * @see net.project.servlet.gui.Page_itf#getName()
     */
    @Override
    public String getName() {

        return _name;
    }

    /**
     * Valorizza i parametri di uno dei DataSet che serviranno a costruire la pagina.
     * 
     * @param param java.util.Hashtable hashtable dei parametri del DataSet
     * @param dsNum int identificativo del DataSet
     * @return void
     * @exception
     * @roseuid 3A6D43890218
     */
    @Override
    public void setDataSourceParam(Map param, int dsNum) throws AppCrash {

        // Controllo formale dei parametri in ingresso.
        ErrDetector.GetInstance().param(param);
        ErrDetector.GetInstance().preCond(dsNum < _dsCont, "dsNum = " + dsNum + "; _dsCont = " + _dsCont);

        try {
            DataSet_itf ds = (DataSet_itf) _dsVector.elementAt(dsNum);
            ds.setParam(param);
            _dsHashVector.insertElementAt(param, dsNum);

        } catch (ArrayIndexOutOfBoundsException aioobe) {
            AppCrash ac = new AppCrash(aioobe);
            ac.logContext("FreeMarkerPage", toString() + "; dsNum = " + dsNum);
            throw ac;
        } catch (AppCrash ac) {
            ac.logContext("FreeMarkerPage", toString() + "; dsNum = " + dsNum);
            throw ac;
        }

    }

    // Metodo privato che costruisce la stringa del file di configurazione
    // alla quale è associato il nome del DataSet del numero passato in input
    // @param int dsNum numero del DataSet
    private String createDataSetPropertyString(int dsNum) {

        return PAGE + "." + _name + "." + DS + (dsNum);
    }

    /**
     * Se gli oggetti contenuti come valori nella hashtable sono istanze di DataSet_itf, allora richiama su ognuno di
     * essi il metodo setPageRootData(DataSet_itf, String) assumendo che le rispettive chiavi siano stringhe contenenti
     * il nome del nodo della _modelRoot; altrimenti carica nella _modelRoot tanti oggetti SimpleScalar quanti sono gli
     * elementi della hashtable stessa
     * 
     * @param param java.util.Hashtable paramHash hashtable contenente i parametri passati dal client
     * @return void
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void setPageRootData(Map paramHash) throws AppCrash {

        // Controllo formale del parametro in ingresso
        ErrDetector.GetInstance().param(paramHash);

        // Recupero delle chiavi di paramHash
        Iterator keys = paramHash.keySet().iterator();

        // Iterazione sulle chiavi di paramHash
        while (keys.hasNext()) {
            try {

                // Recupero chiave corrente di paramHash
                Object key = keys.next();

                // Recupero valore corrispondente alla chiave corrente di paramHash
                Object value = paramHash.get(key);

                if ((key instanceof String) && (value instanceof DataSet_itf)) {
                    String extDataSetName = (String) key;
                    DataSet_itf extDataSet = (DataSet_itf) value;
                    setPageRootData(extDataSet, extDataSetName);
                    if (_externalDataSets == null) {
                        _externalDataSets = new Vector();
                    }
                    _externalDataSets.add(extDataSet);

                } else {
                    // Caricamento di un SimpleScalar in _modelRoot contenente
                    // il valore corrispondente alla chiave corrente di paramHash
                    if (value instanceof String) {
                        _modelRoot.put(key.toString(), convertValue((String) value));

                    } else {
                        _modelRoot.put(key.toString(), value);

                    }
                }

            } catch (NoSuchElementException nsee) {
                AppCrash ac = new AppCrash(nsee);
                ac.logContext("FreeMarkerPage", "paramHash = " + paramHash + "; " + toString());
                throw ac;
            }
        }

    }

    public String convertValue(String value) {

        return value;
    }

    /**
     * Riceve un dataset e carica in un nodo della _modelRoot un oggetto DataSetToSimpleListAdapter (ottenuto dal
     * dataset stesso)
     * 
     * @param ds net.project.dataset.DataSet_itf il dataset in ingresso
     * @param name java.lang.String il nome del nodo della _modelRoot cui assegnare l'oggetto DataSetToSimpleListAdapter
     * @return void
     * @exception net.project.errors.ParamCrash
     */
    @Override
    public void setPageRootData(DataSet_itf ds, String name) throws AppCrash {

        ErrDetector.GetInstance().param(ds);
        ErrDetector.GetInstance().param(name);

        try {
            String campiDaFormattare = Config.GetInstance(_cfName).getProperty("DS." + name + ".CampiDaFormattare", "");
            DataSetToSimpleListAdapter adapter = new DataSetToSimpleListAdapter(ds, campiDaFormattare, _freemarkerOld,
                    _cfName);
            _modelRoot.put(name, adapter);

            // se il dataset è un WindowDataSet, rendo disponibili nel template
            // tutti i tags per la gestione delle pagine
            if (ds instanceof WindowDataSet_itf) {
                setPagingTags((WindowDataSet_itf) ds, name);
            }
        } catch (AppCrash ac) {
            ac.logContext("FreeMarkerPage", "nodo di _modelRoot = " + name + "; " + toString());
            throw ac;
        }
    }

    /**
     * Imposta i tags necessari alla gestione di dataset paginati. Tali tags, a disposizione nei template freemarker
     * sono di seguito elencati: * nomeDataset_numberOfRows = il numero totale di righe presenti nel dataset *
     * nomeDataset_currentPage = il numero della pagina corrente (la numerazione parte da 1) * nomeDataset_nextPage = il
     * numero della pagina successiva alla corrente (-1 se non ci sono più pagine) * nomeDataset_hasNextPage = 1 se
     * esiste una pagina successiva alla corrente, 0 altrimenti * nomeDataset_previousPage = il numero della pagina
     * precedente la pagina corrente (-1 se non ci sono pagine prima) * nomeDataset_hasPreviousPage = 1 se esiste una
     * pagina precedente alla pagina corrente, 0 altrimenti * nomeDataset_numberOfPageRows = il numero di righe presenti
     * in una pagina * nomeDataset_numberOfPages = il numero di pagine in cui il dataset è stato suddiviso
     * 
     * @param ds net.project.dataset.WindowDataSet_itf Il dataset.
     * @param dsName java.lang.String il nome del dataset
     * @return void
     * @exception net.project.errors.AppCrash
     */
    private void setPagingTags(WindowDataSet_itf ds, String dsName) throws AppCrash {

        String variableName = "";
        try {
            variableName = "_numberOfRows";
            _modelRoot.put(dsName + variableName, new SimpleNumber(ds.getNumberOfRows()));

            variableName = "_currentPage";
            _modelRoot.put(dsName + variableName, new SimpleNumber(ds.getCurrentWindow()));

            variableName = "_nextPage";
            _modelRoot.put(dsName + variableName, new SimpleNumber(ds.getNextWindow()));

            variableName = "_hasNextPage";
            int hasNextPage = (ds.existsNextWindow() ? 1 : 0);
            _modelRoot.put(dsName + variableName, new SimpleNumber(hasNextPage));

            variableName = "_previousPage";
            _modelRoot.put(dsName + variableName, new SimpleNumber(ds.getPrevWindow()));

            variableName = "_hasPreviousPage";
            int hasPrevPage = (ds.existsPrevWindow() ? 1 : 0);
            _modelRoot.put(dsName + variableName, new SimpleNumber(hasPrevPage));

            variableName = "_numberOfPageRows";
            _modelRoot.put(dsName + variableName, new SimpleNumber(ds.getWindowSize()));

            variableName = "_numberOfPages";
            _modelRoot.put(dsName + variableName, new SimpleNumber(ds.getNumberOfWindows()));

        } catch (AppCrash ac) {
            ac.logContext("FreeMarkerPage", "nodo di _modelRoot = " + dsName + variableName + "; " + toString());
            throw ac;
        }
    }

    /**
     * Mostra la pagina creata.
     * 
     * @param out java.io.OutputStream l'oggetto sul quale mostrare la pagina
     * @return void
     * @exception
     * @roseuid 3A6D43F6039B
     */
    @Override
    public void display(PrintWriter pw) throws AppCrash {

        // Controllo formale del parametro in ingresso.
        ErrDetector.GetInstance().param(pw);

        Logger.GetInstance().log0("#Trace=FMP " + _name + " - " + _template.getName());

        try {

            openExternalDataSets();

            // Iterazione su tutti i dataset della pagina
            System.out.println("");
            for (int i = 0; i < _dsCont; i++) {
                DataSet_itf ds = null;
                try {
                    // Recupero di un dataset
                    ds = (DataSet_itf) _dsVector.elementAt(i);
                    ErrDetector.GetInstance().postCond(ds != null,
                            "FreeMarkerPage - errore nel recupero del dataset n. " + i);
                    ds.open();

                    // Recupero proprietà CampiDaFormattare dal file di configurazione
                    String campiProp = PAGE + "." + _name + "." + DS + i + "." + CAMPI_DA_FORMATTARE;
                    String campiPropString = Config.GetInstance(_cfName).getProperty(campiProp);
                    if (campiPropString == null) {
                        campiPropString = "";
                    }

                    // Istanziazione di un DataSetToSimpleListAdapter per contenere le righe del dataset
                    TemplateCollectionModel list = new DataSetToSimpleListAdapter(ds, campiPropString, _freemarkerOld,
                            _cfName);

                    // Recupero nome della SimpleList dal file di configurazione.
                    String prop = PAGE + "." + _name + "." + DS + i + "." + LIST;
                    String listName = Config.GetInstance(_cfName).getProperty(prop);
                    boolean listNameEsistente = (listName != null) && (listName.length() > 0);
                    ErrDetector.GetInstance().postCond(listNameEsistente,
                            "proprietà " + prop + " mancante nel file di configurazione");

                    // Setta l'elemento che indica la presenza di righe nel dataset
                    int hasRows = (ds.hasMoreElements() ? 1 : 0);
                    _modelRoot.put(listName + "_hasrows", new SimpleNumber(hasRows));

                    // Caricamento di _modelRoot con il DataSetToSimpleListAdapter creato
                    _modelRoot.put(listName, list);

                } catch (AppCrash ac) {
                    ac.logContext("FreeMarkerPage", "errore nel recupero del dataset n. " + i + "; " + toString());
                    throw ac;
                }
            }

            // Process del template
            _template.process(_modelRoot, pw);

        } catch (AppCrash ac) {
            ac.logContext("FreeMarkerPage", toString());
            throw ac;
        } catch (Throwable aioobe) {
            AppCrash ac = new AppCrash(aioobe);
            ac.logContext("FreeMarkerPage", toString());
            throw ac;
        } finally {

            closeExternalDataSets();
            _externalDataSets = null;

            // chiusura dei dataset
            int i = 0;
            try {
                for (i = 0; i < _dsCont; i++) {
                    DataSet_itf ds = (DataSet_itf) _dsVector.elementAt(i);
                    if (ds != null) {
                        ds.close();
                    }
                }

            } catch (AppCrash ac) {
                ac.logContext("FreeMarkerPage", "Errore nella chiusura del dataset n. " + i);
                throw ac;
            }
        }

    }

    private void openExternalDataSets() throws AppCrash {

        if (_externalDataSets != null) {
            for (int dsCont = 0; dsCont < _externalDataSets.size(); dsCont++) {
                try {
                    ((DataSet_itf) _externalDataSets.elementAt(dsCont)).open();

                } catch (AppCrash ac) {
                    ac.logContext("FreeMarkerPage", "errore in apertura del dataset esterno n. " + dsCont);
                    throw ac;
                }
            }
        }

    }

    private void closeExternalDataSets() throws AppCrash {

        if (_externalDataSets != null) {
            for (int dsCont = 0; dsCont < _externalDataSets.size(); dsCont++) {
                try {
                    ((DataSet_itf) _externalDataSets.elementAt(dsCont)).close();

                } catch (AppCrash ac) {
                    ac.logContext("FreeMarkerPage", "errore in chiusura del dataset esterno n. " + dsCont);
                    throw ac;
                }
            }
        }

    }

    /**
     * Costruisce una stringa descrivente lo stato del sistema.
     * 
     * @return java.lang.String la stringa costruita
     */
    @Override
    public String toString() {

        StringBuffer temp = new StringBuffer();
        temp.append("_dsCont = ");
        temp.append(_dsCont);
        temp.append("; _name = ");
        temp.append(_name);
        temp.append("; _cfName = ");
        temp.append(_cfName);
        temp.append("; _dsVector = ");
        temp.append(_dsVector == null ? "null" : _dsVector.toString());
        temp.append("; _dsNameVector = ");
        temp.append(_dsNameVector == null ? "null" : _dsNameVector.toString());
        temp.append("; _dsHashVector = ");
        temp.append(_dsHashVector == null ? "null" : _dsHashVector.toString());
        return temp.toString();

    }

    /**
     * Questo metodo ritorna il nome della configurazione usata per creare la pagina
     * 
     * @return String nome della configurazione
     */
    @Override
    public String getConfigName() {

        return _cfName;
    }

    /**
     * @return Ritorna il campo modelRoot.
     */
    public SimpleHash getModelRoot() {

        return _modelRoot;
    }

    /**
     * @param modelRoot il modelRoot da impostare.
     */
    public void setModelRoot(SimpleHash modelRoot) {

        _modelRoot = modelRoot;
    }

    /**
     * @return Ritorna il campo template.
     */
    public Template getTemplate() {

        return _template;
    }

    /**
     * @param template il template da impostare.
     */
    public void setTemplate(Template template) {

        _template = template;
    }
}
