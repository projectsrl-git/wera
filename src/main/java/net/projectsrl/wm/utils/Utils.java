
package net.projectsrl.wm.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import it.project.iride.db.LogDAO;
import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.errors.Logger;
import net.project.misc.DateTimeFormat;
import net.project.servlet.frame.SsbServletRequest;


public class Utils {

    private static Random   _randomSource  = new Random();

    public static String[]  MESI           = { "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio",
            "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre" };

    private static String[] _giorni        = { "Domenica", "Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì",
            "Sabato"                      };

    private static int[]    _giorniDelMese = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    /**
     * Questo metodo restituisce il timestamp con le ultime 3 cifre random.
     * 
     */
    public static String getTimestamp() throws AppCrash {

        DateTimeFormat dateTimeFormat = null;
        String timestamp = null;
        String random = "";

        try {
            dateTimeFormat = new DateTimeFormat();
            timestamp = dateTimeFormat.getTimestamp();

            while (random.length() < 3) {
                random = generateRandom(true);
            }

            random = random.substring(random.length() - 3, random.length());

            timestamp = timestamp.substring(0, timestamp.length() - 3) + random;
        } catch (Throwable e) {
            throw new AppCrash(e);
        }

        return timestamp;
    }

    /**
     * Dice se una stringa non e' vuota. Per 'vuota' s'intende null, stringa vuota, o stringa di soli blank.
     * 
     * @param what java.lang.String La stringa da controllare.
     * 
     * @return boolean true se la stringa non e' null, non e' vuota e non e' di soli blank; false altrimenti.
     */
    public static boolean IsNotEmpty(String what) {

        return ((what != null) && (what.trim().length() > 0));
    }

    /**
     * Ribalta la data in input da formato gg/mm/AAAA in formato AAAA/mm/gg.
     * 
     * @param String data
     * 
     * @return String dataRibaltata
     */
    public static String ribaltaData(String data) {

        if (data == null || data.equals("")) {
            return "";
        }

        String dataRibaltata = "";

        String anno = data.substring(6, 10);
        String mese = data.substring(3, 5);
        String giorno = data.substring(0, 2);

        dataRibaltata = anno + "/" + mese + "/" + giorno;

        return dataRibaltata;
    }

    /**
     * Ribalta la data in input da formato AAAAmmgg in formato gg/mm/AAAA.
     * 
     * @param String data
     * 
     * @return String data
     */
    public static String raddrizzaData(String data) {

        if (data == null || data.equals("")) {
            return "";
        }

        String dataRaddrizzata = "";

        String giorno = data.substring(8);
        String mese = data.substring(5, 7);
        String anno = data.substring(0, 4);

        dataRaddrizzata = giorno + "/" + mese + "/" + anno;

        return dataRaddrizzata;
    }

    public static String getStringDataCompletaOggi() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ITALY);
        return sdf.format(new Date());
    }

    public static String getStringDataOggiRibaltata() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ITALY);
        return sdf.format(new Date());
    }

    public static String getStringOra() {

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss", Locale.ITALY);
        return sdf.format(new Date());
    }

    public static String getStringDataOggiIndex() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/dd/MM", Locale.ITALY);
        return sdf.format(new Date());
    }

    public static String getStringDataOggi() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY);
        return sdf.format(new Date());
    }

    /**
     * NUMERO UNIVOCO DI 20 CARATTERI
     * 
     * Restituisce un timestamp + 3 caratteri random yyyyMMddHHmmssSSS + 3 char = 20 char totali
     * 
     * @return String numero univoco di 20 caratteri
     */
    public static String getUnique() {

        String random = "";
        String aggancio = "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", java.util.Locale.ITALY);

        String timestamp = sdf.format(new Date());

        try {
            while (random.length() < 3) {
                random = generateRandom(true);
            }

            random = random.substring(random.length() - 3, random.length());

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            aggancio = timestamp;
            ac.logContext("Utils.getAggangio", "Errore nel calcolo del numero random");
        }

        aggancio = timestamp + random;

        return aggancio;

    }
    
    public static String getOrario() {

        SimpleDateFormat sdf = new SimpleDateFormat("HH.mm", java.util.Locale.ITALY);
        return sdf.format(new Date());

    }

    public static String getMeseOggi() {

        SimpleDateFormat sdf = new SimpleDateFormat("MM", java.util.Locale.ITALY);
        return sdf.format(new Date());
    }
    
    
    public static String getUltimoGiornoMese(String mese, String anno) {

    	String ultimo="31";
        if (mese.equals("04") || mese.equals("06") || mese.equals("09") || mese.equals("11")){
        	ultimo="30";
        }
        if (mese.equals("02")){
        	float divisione=Integer.parseInt(anno)%4;
    		if (divisione==0){
    			ultimo="29";
    		}else{
    			ultimo="28";
    		}
        }
        return ultimo;
    }
    
    

    @SuppressWarnings("deprecation")
	public static String getMesePrecedente() {

        SimpleDateFormat sdf = new SimpleDateFormat("MM", java.util.Locale.ITALY);
        Date data = new Date();
        data.setMonth(data.getMonth() - 1);
        return sdf.format(data);
    }
    
    public static String getGiornoDellaSettimana(int giornoDellaSettimana) {

        return _giorni[giornoDellaSettimana - 1];
    }

    public static int getGiorniDelmese(int mese, int anno) {

        if (mese != 2) {
            return _giorniDelMese[mese - 1];
        }

        if (((anno % 4 == 0 && anno % 100 != 0) || anno % 400 == 0)) {
            return 29;
        }

        return 28;
    }

    public static String getDescrizioneMese(int meseDellAnno) {

        return MESI[meseDellAnno - 1];
    }

    @SuppressWarnings("deprecation")
	public static String getAnnoMesePrecedente() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", java.util.Locale.ITALY);
        Date data = new Date();
        data.setMonth(data.getMonth()-1);
        return sdf.format(data);
    }

    public static String getAnnoOggi() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", java.util.Locale.ITALY);
        return sdf.format(new Date());
    }
    
    public static String getAnnoPrecedente() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", java.util.Locale.ITALY);
        Date data = new Date();
        data.setYear(data.getYear()-1);
        return sdf.format(data);
    }

    public static String getGiornoOggi() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd", java.util.Locale.ITALY);
        return sdf.format(new Date());
    }

    
    
    /**
     * Questo metodo genera un intero randomicamente.
     * 
     * @param boolean isShort TRUE= numero random a 3 cifre /FALSE = numero random a 9 cifre
     * 
     * @return String stringa corrispondente al numero random
     * @throws AppCrash
     * 
     */

    public static String generateRandom(boolean isShort) throws AppCrash {

        int rand = 0;

        if (isShort) {
            rand = _randomSource.nextInt(999);
        } else {
            rand = _randomSource.nextInt(999999999);
        }

        return Integer.toString(rand);
    }

    public static String getProssimoCodice(String dsName) throws AppCrash {

        return getProssimoCodice(dsName, "");
    }

    @SuppressWarnings("unchecked")
    public static String getProssimoCodice(String dsName, String prefissoParametro) throws AppCrash {

        String codice = "";

        DataSet_itf dataSet = null;

        DataSetFactory dsFactory = DataSetFactory.getInstance();
        Integer maxpk = new Integer(0);

        try {
            dataSet = dsFactory.makeDataSet("", dsName);

            HashMap parametri = new HashMap();
            parametri.put("PREFISSO_PARAMETRO", prefissoParametro);
            dataSet.setParam(parametri);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                if (dbRow != null) {
                    String strUltimoValore = (String) dbRow.getField("ultimo");
                    if (strUltimoValore != null && !strUltimoValore.trim().equals("")) {
                        maxpk = Integer.valueOf(strUltimoValore);
                        maxpk += 1;
                    }
                }
            }
        } catch (AppCrash ac) {
            ac.logContext("Utils.getProssimoCodice", "Errore nella ricerca del max del dataset " + dsName);
            throw ac;
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext("Utils.getProssimoCodice", "Errore nella close del dataset " + dsName);
                }
            }
        }

        if (maxpk == null || maxpk.intValue() == 0) {
            maxpk = new Integer(1);
        }

        codice = codice + maxpk.toString();
        DecimalFormat myFormatter = new DecimalFormat("00000");
        codice = myFormatter.format(maxpk);
        

        return codice;
    }

    public static String getProssimoCodiceAnnuo(String dsName, String anno) throws AppCrash {

        return getProssimoCodiceAnnuo(dsName, anno, "");
    }

    @SuppressWarnings("unchecked")
    public static String getProssimoCodiceAnnuo(String dsName, String anno, String prefissoParametro) throws AppCrash {

        String codice = "00000";

        DataSet_itf dataSet = null;

        DataSetFactory dsFactory = DataSetFactory.getInstance();
        Integer maxpk = new Integer(0);

        try {
            dataSet = dsFactory.makeDataSet("", dsName);

            HashMap parametri = new HashMap();
            parametri.put("PREFISSO_PARAMETRO", prefissoParametro);
            parametri.put("ANNO", anno);
            dataSet.setParam(parametri);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                if (dbRow != null) {
                    String strUltimoValore = (String) dbRow.getField("ultimo");
                    if (strUltimoValore != null && !strUltimoValore.trim().equals("")) {
                        maxpk = Integer.valueOf(strUltimoValore);
                        maxpk += 1;
                    }
                }
            }
        } catch (AppCrash ac) {
            ac.logContext("Utils.getProssimoCodice", "Errore nella ricerca del max del dataset " + dsName);
            throw ac;
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext("Utils.getProssimoCodice", "Errore nella close del dataset " + dsName);
                }
            }
        }

        if (maxpk == null || maxpk.intValue() == 0) {
            maxpk = new Integer(1);
        }

        codice = codice + maxpk.toString();
        codice = prefissoParametro.trim() + codice.substring(codice.length() - 5);

        return codice;
    }

    @SuppressWarnings("unchecked")
    public static HashMap getIntestazioneWord(String dsName, String prefissoParametro) throws AppCrash {

        HashMap intestazione = new HashMap();

        DataSet_itf dataSet = null;

        DataSetFactory dsFactory = DataSetFactory.getInstance();

        try {
            dataSet = dsFactory.makeDataSet("", dsName);

            HashMap parametri = new HashMap();
            parametri.put("codice_anagrafica", prefissoParametro);
            dataSet.setParam(parametri);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                if (dbRow != null) {
                    String strRagsoc = (String) dbRow.getField("ragsoc");
                    String strIndir = (String) dbRow.getField("indir");
                    String strCAP = (String) dbRow.getField("cap");
                    String strLocalita = (String) dbRow.getField("locali");

                    intestazione.put("RAGSOC", strRagsoc);
                    intestazione.put("INDIR", strIndir);
                    intestazione.put("CAP", strCAP);
                    intestazione.put("LOCALI", strLocalita);
                }
            }
        } catch (AppCrash ac) {
            ac.logContext("Utils.getIntestazioneWord", "Errore nella ricerca del intestazione del dataset " + dsName);
            throw ac;
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext("Utils.getIntestazioneWord", "Errore nella close del dataset " + dsName);
                }
            }
        }
        return intestazione;
    }

    @SuppressWarnings("unchecked")
    public static HashMap getCampiDaPara(String dsName, String prefissoParametro) throws AppCrash {

        HashMap para = new HashMap();

        DataSet_itf dataSet = null;

        DataSetFactory dsFactory = DataSetFactory.getInstance();

        try {
            dataSet = dsFactory.makeDataSet("", dsName);

            HashMap parametri = new HashMap();
            parametri.put("chiave_para", prefissoParametro);
            dataSet.setParam(parametri);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                if (dbRow != null) {
                    String strCodice = (String) dbRow.getField("codice");
                    String strDescri = (String) dbRow.getField("descri");
                    String strLibera = (String) dbRow.getField("libera");

                    para.put("CODICE", strCodice);
                    para.put("DESCRI", strDescri);
                    para.put("LIBERA", strLibera);
                }
            }
        } catch (AppCrash ac) {
            ac.logContext("Utils.getCampiDaPara", "Errore nella ricerca campi del dataset " + dsName);
            throw ac;
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext("Utils.getCampiDaPara", "Errore nella close del dataset " + dsName);
                }
            }
        }
        return para;
    }

    @SuppressWarnings("unchecked")
    public static HashMap getRisorsaUmana(String dsName, String prefissoParametro) throws AppCrash {

        HashMap risumana = new HashMap();

        DataSet_itf dataSet = null;

        DataSetFactory dsFactory = DataSetFactory.getInstance();

        try {
            dataSet = dsFactory.makeDataSet("", dsName);

            HashMap parametri = new HashMap();
            parametri.put("codice_risorsa", prefissoParametro);
            dataSet.setParam(parametri);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                if (dbRow != null) {
                    String strCognome = (String) dbRow.getField("cognome");
                    String strNome = (String) dbRow.getField("nome");

                    risumana.put("COGNOME", strCognome);
                    risumana.put("NOME", strNome);
                }
            }
        } catch (AppCrash ac) {
            ac.logContext("Utils.getRisorsaUmana", "Errore nella ricerca campi del dataset " + dsName);
            throw ac;
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext("Utils.getRisorsaUmana", "Errore nella close del dataset " + dsName);
                }
            }
        }
        return risumana;
    }

    @SuppressWarnings("unchecked")
    public static HashMap get_U_AZI_AN(String dsName) throws AppCrash {

        HashMap uazian = new HashMap();

        DataSet_itf dataSet = null;

        DataSetFactory dsFactory = DataSetFactory.getInstance();

        try {
            dataSet = dsFactory.makeDataSet("", dsName);

            HashMap parametri = new HashMap();
            dataSet.setParam(parametri);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                if (dbRow != null) {
                    String strAziRagsoc = (String) dbRow.getField("AZI_RAGSOC");
                    uazian.put("AZI_RAGSOC", strAziRagsoc);
                }
            }
        } catch (AppCrash ac) {
            ac.logContext("Utils.get_U_AZI_AN", "Errore nella ricerca campi del dataset " + dsName);
            throw ac;
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext("Utils.get_U_AZI_AN", "Errore nella close del dataset " + dsName);
                }
            }
        }
        return uazian;
    }

    /**
     * Separa i valori dei campi String che arrivano dal template in questo formato String strToSplit =
     * "F:IDANAAZIRIC=000000001;F:ANAAZIRIC=PROJECT SRL DI CAVA MANARA"
     * 
     * ad esempio:
     * 
     * String strToSplit="F:IDANAAZIRIC=W00000001;F:ANAAZIRIC=PROJECT SRL DI CAVA MANARA"; String
     * campoDaCercare="IDANAAZIRIC";
     * 
     * Utils.leggeStringaElencoCampi(campoDaCercare,strToSplit);
     * 
     * restituirà
     * 
     * (java.lang.String) =W00000001
     * 
     * @param String campoDaCercare
     * @param String strToSplit
     * @return String valoreCampoDaCercare
     */
    public static String leggeStringaElencoCampi(String campoDaCercare, String strToSplit) {

        String[] arr = strToSplit.split("\\;");
        String valoreCampoDaCercare = "";

        for (int i = 0; i < arr.length; i++) {
            if (arr[i].contains("F:" + campoDaCercare)) {
                valoreCampoDaCercare = arr[i].substring(arr[i].indexOf("=") + 1).trim();
                break;
            }
        }

        return valoreCampoDaCercare;
    }

    /**
     * Converte le stringhe con caratteri speciali
     * 
     * @param String stringaConCaratteriSpeciali stringa da convertite
     * @return String stringa convertita
     */
    public static String converteCaratteriSpeciali(String stringaConCaratteriSpeciali) {

        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("à", "a'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("é", "e'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("è", "e'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("ò", "o'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("ù", "u'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("ì", "i'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("&", "&amp;");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("°", "'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("À", "A'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("Á", "A'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("€", "&euro;");

        return stringaConCaratteriSpeciali;
    }

    /**
     * Converte le stringhe con caratteri speciali per farli interpretare all' XSL
     * 
     * @param String stringaConCaratteriSpeciali stringa da convertite
     * @return String stringa convertita
     */
    public static String converteCaratteriSpecialiPerPDF(String stringaConCaratteriSpeciali) {

        stringaConCaratteriSpeciali = converteCaratteriSpeciali(stringaConCaratteriSpeciali);

        return stringaConCaratteriSpeciali;
    }

    /**
     * Elimina da una stringa che deve essere nome file, i caratteri che sono utilizzati dai filesystem
     * 
     * @param String fileName nome file da normalizzare
     * @return String nome file normalizzato
     */
    public static String normalizeASCIIFilename(String fileName) {

        fileName = fileName.replace("/", "_");
        fileName = fileName.replace("\\", "_");
        return fileName;
    }

    /**
     * Elimina da una stringa che deve essere nome file, i caratteri che sono utilizzati dai filesystem
     * 
     * @param String fileName nome file da normalizzare
     * @return String nome file normalizzato
     */
    public static String normalizeFullPath(String fileName) {

        fileName = fileName.replace(".\\\\", "/");
        fileName = fileName.replace("./", "/");
        fileName = fileName.replace("\\\\", "/");
        fileName = fileName.replace("\\", "/");
        fileName = fileName.replace("//", "/");
        return fileName;
    }

    /**
     * Questo metodo
     * 
     * @param attribute DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static String notNull(String attribute) {

        if (attribute == null) {
            return "";
        }

        return attribute;
    }

    /**
     * Questo metodo
     * 
     * @param attribute DOCUMENT ME!
     * @param default_value DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static String notNull(String attribute, String default_value) {

        if (attribute == null) {
            return default_value;
        }

        return attribute;
    }

   
    /**
     * Realizza escaping di un paramtero che valorizza un campo di una stringa sql sostituendo agli apici singoli ' due
     * apici singoli ''
     * 
     * @param String sqlParameter parametro da controllare
     * 
     * @return String paramtero dopo escaping
     * @throws AppCrash
     */
    public static String escapeSqlParameter(String sqlParameter) {

        if (sqlParameter.indexOf("'") > 0) {
            sqlParameter = sqlParameter.replaceAll("'", "''");
        }

        return sqlParameter;
    }

    public static String getAnnoData(String dataDiritta) {

        return dataDiritta.substring(6);
    }

    public static String getMeseData(String dataDiritta) {

        return dataDiritta.substring(3, 5);
    }

    public static final double roundDouble(double d, int places) {

        return Math.round(d * Math.pow(10, (double) places)) / Math.pow(10, (double) places);
    }

    public static void logOperation(String userid, String userDescription, String whatIDo, String request) {

        try {
            LogDAO log = new LogDAO();
            String dataOra = getStringDataCompletaOggi();
            log.setField(LogDAO.DATA, dataOra.substring(0, 10));
            log.setField(LogDAO.ORA, dataOra.substring(11, 19));
            log.setField(LogDAO.OPERATORE, userid);
            log.setField(LogDAO.OPERAZIONE, whatIDo);
            log.setField(LogDAO.DESCRI, userDescription);
            log.setField(LogDAO.HTTP_PARAMS, request);
            log.insert();

        } catch (Throwable th) {
            Logger.GetInstance().logApplication(userid + " - " + userDescription + " - " + whatIDo);
        }

    }
    
    @SuppressWarnings("unchecked")
	public static String getRequestParameters(SsbServletRequest req) {

        StringBuilder params = new StringBuilder();
        // legge tutti i parametri della request
        Enumeration param = req.getParameterNames();
        String name = "";
        while (param.hasMoreElements()) {
            name = (String) param.nextElement();
            params.append(name + "=" + req.getField(name) + ";");
        }

        return params.toString();
    }
    

}
