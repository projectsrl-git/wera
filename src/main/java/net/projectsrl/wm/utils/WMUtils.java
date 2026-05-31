
package net.projectsrl.wm.utils;

import it.project.iride.core.Costanti_itf;
import it.project.webapp.accesscontrol.UtentiDAOLogin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpSession;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.project.errors.Logger;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.projectsrl.mail.MyAuthenticator;
import net.projectsrl.wm.mail.DeferredMailSender;
import net.projectsrl.wm.mail.SendSMTPMail;

public class WMUtils {

   
    private static final String TIME_FORMAT                                     = "HH:mm:ss";
    private static final String DATE_FORMAT                                     = "yyyy/MM/dd";
    private static final String CONTI_AIS                                       = "CONTI_AIS";
    private static final String DATASET_DIP = "DataSetDipendentiCerca";

    /**
     * Imposta la data iniziale di caricamento dell'immatricolato.<br>
     * Non devo caricare telai immatricolati prima di questa data<br>
     * o comunque non più vecchi di sei mesi rispetto alla data in cui effettuo il caricamento.
     * 
     * @param SsbServletRequest req
     * @return String baseline date di caricamento (GG/MM/AAAA)
     * 
     */
    public static String getDataInizialeCaricamentoImmaticolato(SsbServletRequest req) {

        String baselineDate = req.getField(Costanti_itf.DATA_IMMATRICOLATO_DAL);
        if (baselineDate.equals("")) {
            baselineDate = Config.GetInstance().getProperty("Immatricolato.dataInizioImportazione", "");
        } else {
            baselineDate = Utils.ribaltaData(baselineDate);
        }
        return baselineDate;

    }

    /**
     * Imposta la data finale di caricamento dell'immatricolato.<br>
     * Non devo caricare telai immatricolati dopo questa data<br>
     * 
     * @param SsbServletRequest req
     * @return String deathline date di caricamento (GG/MM/AAAA)
     * 
     */
    public static String getDataFinaleCaricamentoImmaticolato(SsbServletRequest req) {

        String date = req.getField(Costanti_itf.DATA_IMMATRICOLATO_AL);
        if (date.equals("")) {
            date = Config.GetInstance().getProperty("Immatricolato.dataFineImportazione", "");
        } else {
            date = Utils.ribaltaData(date);
        }

        if (date.equals("")) {
            Calendar cal = Calendar.getInstance();
            String mese = "0" + String.valueOf(cal.get(Calendar.MONTH) + 1);
            mese = mese.substring(mese.length() - 2);
            String anno = String.valueOf(cal.get(Calendar.YEAR));
            String giorno = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
            date = anno + "/" + mese + "/" + giorno;
        }

        return date;

    }

    public static String getMonthOfQuarter(int _quarter, int _mese) {

        String meseNelQuarter = "";

        if (_quarter <= 0 && _quarter > 4) {
            return meseNelQuarter;
        }

        int mese = _mese + (_quarter - 1) * 3;
        meseNelQuarter = WMUtils.fillWithZerosLeft(String.valueOf(mese), 2);

        return meseNelQuarter;
    }

    public static int getQuarterIncludesMonth(String meseOggi) {

        int quarter = (int) (Math.floor(Integer.parseInt(meseOggi)) - 1) / 3 + 1;

        return quarter;
    }

    public static String getPreviousMonthFirstDay() {

        return getMonthFirstDay(-1);

    }

    public static String getMonthFirstDay(int differentMonthFromCurrent) {

        String monthFirstDay = "";

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, differentMonthFromCurrent);
        String meseIniziale = "0" + String.valueOf(cal.get(Calendar.MONTH));
        meseIniziale = meseIniziale.substring(meseIniziale.length() - 2);
        String annoIniziale = String.valueOf(cal.get(Calendar.YEAR));
        monthFirstDay = "01/" + meseIniziale + "/" + annoIniziale;

        return monthFirstDay;

    }

    public static String getAnnoMeseElaborazione() {

        String annomese = Utils.getAnnoOggi() + Utils.getMeseOggi();

        DataSet_itf dataSetRiga = null;

        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dataSetRiga = dsFactory.makeDataSet("", "DataSetAnnoMeseElaborazione");
            dataSetRiga.open();

            while (dataSetRiga.hasMoreElements()) {

                Row_itf dbRow = (Row_itf) dataSetRiga.nextElement();

                annomese = (String) dbRow.getField("ANNOMESE");

            }

        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("IrideUtils.getAnnoMeseElaborazione",
                    "Errore nella elaborazione del dataset DataSetAnnoMeseElaborazione");
        } finally {
            if (dataSetRiga != null) {
                try {
                    dataSetRiga.close();
                } catch (AppCrash ac) {
                    ac.logContext("IrideUtils.getAnnoMeseElaborazione",
                            "Errore nella close del dataset DataSetAnnoMeseElaborazione");
                }
            }
        }

        return annomese;

    }

    /**
     * Se OUTPUT QUARTER è già stato calcolato per il QUARTER + ANNO selezionato<br>
     * restituisce TRUE altrimenti FALSE
     * 
     * @param String anno
     * @param String quarter (1 char)
     * 
     * @return boolean
     */
    public static boolean quarterCalcolato(String anno, String quarter) {

        boolean giaCalcolato = false;
        DataSet_itf dataSetRiga = null;

        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dataSetRiga = dsFactory.makeDataSet("", "DataSetQuarterVB");
            HashMap<String, String> parametri = new HashMap<String, String>();
            parametri.put("QUARTER", quarter);
            parametri.put("ANNO", anno);
            dataSetRiga.setParam(parametri);
            dataSetRiga.open();

            return dataSetRiga.hasMoreElements();

        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("IrideUtils.getQuarterCalcolato", "Errore nella elaborazione del dataset DataSetQuarterVB");
        } finally {
            if (dataSetRiga != null) {
                try {
                    dataSetRiga.close();
                } catch (AppCrash ac) {
                    ac.logContext("IrideUtils.getQuarterCalcolato",
                            "Errore nella elaborazione del dataset DataSetQuarterVB");
                }
            }
        }

        return giaCalcolato;

    }

    

    public static boolean creaDir(String directory) {

        boolean success = (new File(directory)).mkdir();

        if (success) {
            Logger.GetInstance().log0("Ho creato: " + directory);
        } else {
            Logger.GetInstance().log0("Impossibile creare: " + directory);
            success = (new File(directory)).mkdirs();

            if (success) {
                Logger.GetInstance().log0("Ho creato: " + directory);
            } else {
                Logger.GetInstance().log0("Impossibile creare: " + directory);
            }
        }

        return success;
    }

    /**
     * 
     * Formatta un timestamp passato in ingresso nel formato passato in ingresso.
     * 
     * @param formato java.lang.String Stringa contenente il formato che si vuole utilizzare
     * 
     * @param timestamp Timestamp timestamp da formattare
     * 
     * @return java.lang.String Il timestamp nel formato desiderato.
     */

    public static String formatTimestamp(String formato, Timestamp timestamp) {

        SimpleDateFormat s = new SimpleDateFormat(formato);

        String dateString = s.format(timestamp);

        return dateString;

    }

    /**
     * Formatta un timestamp in millisecondi (long timemillis) passato in ingresso nel formato AAAA/MM/GG.
     * 
     * @param timemillis Timestamp timestamp da formattare
     * 
     * @return java.lang.String Il timestamp nel formato desiderato.
     */

    public static String getFormattedDateFromTimeMillis(long timemillis) {

        Calendar gcal = GregorianCalendar.getInstance();

        gcal.setTimeInMillis(timemillis);

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

        return sdf.format(gcal.getTime());

    }

    /**
     * Formatta un timestamp in millisecondi (long timemillis) passato in ingresso nel formato HH:MM:SS.
     * 
     * @param timemillis Timestamp timestamp da formattare
     * 
     * @return java.lang.String Il timestamp nel formato desiderato.
     */

    public static String getFormattedTimeFromTimeMillis(long timemillis) {

        Calendar gcal = GregorianCalendar.getInstance();

        gcal.setTimeInMillis(timemillis);

        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);

        return sdf.format(gcal.getTime());

    }

    /**
     * Formatta la data nel formato specificato in DATE_FORMAT
     * 
     * @return String data formattata
     */
    public static String[] getDate() {

        String[] dataOra = { "", "" };
        Date currentTime = new Date();

        SimpleDateFormat formatoOra = new SimpleDateFormat(TIME_FORMAT);
        String oraAttuale = formatoOra.format(currentTime);

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(currentTime);
        currentTime = calendar.getTime();

        SimpleDateFormat formatoData = new SimpleDateFormat(DATE_FORMAT);
        String dataString = formatoData.format(currentTime);

        dataOra[0] = dataString;
        dataOra[1] = oraAttuale;
        return dataOra;
    }

   

    /**
     * Recupera la DESCRIZIONE dell'utente dalla tabella delle risorse UTENTI <br>
     * ATTENZIONE! - QUESTA SI USA SE SU UTENTI SONO GIA' PRESENTI COGNOME+NOME<br>
     * Altimenti bisogna usare la project.misc.Utils.getUserDesc()<br>
     * 
     * @param String user codice utente
     * 
     * @return String descrizione utente
     * @throws AppCrash
     */
    public static String getUserDesc(String user) throws AppCrash {

        String descrizioneUtente = "Descrizione utente non trovata";
        if (user == null || user.equals("")) {
            return descrizioneUtente;
        }

        UtentiDAOLogin utente = new UtentiDAOLogin();
        utente.setField(UtentiDAOLogin.USERID, user);

        try {
            if (utente.retrieve()) {
                String cognome = utente.getField(UtentiDAOLogin.COGNOME).toUpperCase();
                String nome = utente.getField(UtentiDAOLogin.NOME).toUpperCase();

                if (nome.equals(cognome)) {
                    descrizioneUtente = cognome;
                } else {
                    descrizioneUtente = nome + " " + cognome;
                }
            }

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("metodo it.project.iride.core.IrideUtils.getUserDesc()", "\nUSER = ");
            descrizioneUtente = "Utente non univoco";
        }

        return descrizioneUtente;
    }

    /**
     * TODO ATTENZIONE! sarebbe meglio trovare una soluzione più intelligente per sistemare questo problema.<br>
     * <br>
     * Se un dealer ha un unico MESE di target caricato, NON FUNZIONA il calcolo della qualifica del dealer.<br>
     * La query che calcola la qualifica si posiziona sul MESE di target e<br>
     * determina se per il prossimo mese il dealer è o non è qualificato.<br>
     * Il problema è che con questo ragionamento non c'è nulla che determina se per quel mese il dealer è qualificato.<br>
     * Visto che l'unica percentuale che mi serve per "attivare" il ragionamento sulla qualifica è quella del 70%<br>
     * (vedi vista QUALIFICHE_DEALER) è sufficiente inserire un record con 70% e target=0 per tutti quei delaer che
     * hanno un unico target caricato.
     * 
     * 
     * @param DBTransaction dbtransaction
     * @throws AppCrash
     */
    /*public static void creaRecorTappoPerDealerConUnSoloTarget() throws AppCrash {

        DBTransaction dbTransaction = new DBTransaction();
        PreparedStatement ps = null;
        String creaRecordTappo = "";

        try {

            creaRecordTappo = Config.GetInstance().getProperty("TargetVB.CreaRecordTappo");

            // ==== insert
            ps = dbTransaction.prepareStatement(creaRecordTappo);
            ps.execute();
            Logger.GetInstance().log0("---- CreaRecordTappo            =" + creaRecordTappo);
            dbTransaction.commit();

        } catch (Throwable e) {
            if (dbTransaction != null) {
                dbTransaction.rollBack();
            }
            AppCrash ac = new AppCrash(e);
            throw ac;
        } finally {
            try {

                if (ps != null) {
                    ps.close();
                }

                if (dbTransaction != null) {
                    dbTransaction.end();
                }

            } catch (Throwable close) {
                AppCrash appCrashClose = new AppCrash(close);
                appCrashClose.logContext("Worksheet.storeToOutput", "Errore durante la chiusura del PreparedStatement");
                throw appCrashClose;
            }

        }

    }
*/
    public static void resetTabelle(String tableNames) throws AppCrash {

        DBTransaction dbtransaction = new DBTransaction();
        PreparedStatement ps = null;
        String delete = "";

        try {

            ArrayList<String> tableList = getTokenList(tableNames);

            for (int i = 0; i < tableList.size(); i++) {
                Logger.GetInstance().log0("INIZIO " + delete);
                delete = "DELETE FROM " + tableList.get(i);
                ps = dbtransaction.prepareStatement(delete);
                ps.execute();
                Logger.GetInstance().log0("FINE " + delete);
            }
            dbtransaction.commit();

        } catch (Throwable e) {
            if (dbtransaction != null) {
                dbtransaction.rollBack();
            }
            AppCrash ac = new AppCrash(e);
            throw ac;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable close) {
                AppCrash appCrashClose = new AppCrash(close);
                appCrashClose.logContext("resetTabelle", "Errore durante la chiusura del PreparedStatement");
                throw appCrashClose;
            }
        }

    }

    public static ArrayList<String> getTokenList(String text) {

        ArrayList<String> tokenList = new ArrayList<String>();
        int index = 0;
        StringTokenizer campi = new StringTokenizer(text, ";");
        while (campi.hasMoreTokens()) {
            String token = campi.nextToken();
            tokenList.add(token);
            index++;
        }
        return tokenList;
    }

    

    public static void resetSessionContiAIS(HttpSession session) {

        if (session == null) {
            return;
        }
        session.removeAttribute(CONTI_AIS);
    }

    /**
     * Zippa il contenuto di una directory Se il file zip è già esistente, prima lo cancella
     * 
     */
    public static void zipDirectory(String dir, String zipfilename) throws IOException, IllegalArgumentException {

        File zipFile = new File(zipfilename);
        if (zipFile.exists()) {
            zipFile.delete();
        }

        // Check that the directory is a directory, and get its contents
        File d = new File(dir);
        if (!d.isDirectory()) throw new IllegalArgumentException("Not a directory:  " + dir);
        String[] entries = d.list();
        byte[] buffer = new byte[4096]; // Create a buffer for copying
        int bytesRead;

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfilename));

        for (int i = 0; i < entries.length; i++) {
            File f = new File(d, entries[i]);
            if (f.isDirectory()) continue;// Ignore directory
            FileInputStream in = new FileInputStream(f); // Stream to read file
            ZipEntry entry = new ZipEntry(f.getName()); // Make a ZipEntry
            out.putNextEntry(entry); // Store entry
            while ((bytesRead = in.read(buffer)) != -1)
                out.write(buffer, 0, bytesRead);
            in.close();
        }
        out.close();
    }

    /**
     * Identifica se necessaria approvazione SOX. Significa che c'è almeno una variazione SOX non approvata
     * 
     * @return boolean required/not required
     * 
     * @throws AppCrash
     */
    public static boolean soxApprovalRequired(String type) throws AppCrash {

        boolean soxApprovalReqired = false;
        DataSet_itf dataSet = null;

        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", "DataSetSoxNotApproved");
            dataSet.open();

            while (dataSet.hasMoreElements()) {

                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                if (type.equals(dbRow.getField("TIPO_SOX"))) {
                    soxApprovalReqired = true;
                    break;
                }

            }

        } catch (AppCrash ac) {
            ac.logContext("soxApprovalRequired", "errore nell'apertura del dataset DataSetSox");
            throw ac;

        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (AppCrash ac) {
                    ac.logContext("soxApprovalRequired", "errore nella chiusura del dataset DataSetSox");
                    throw ac;
                }
            }
        }

        return soxApprovalReqired;
    }

    public static boolean inviaMailSox(ApplicationServices_itf applicationSrv, String ruolo) throws AppCrash {

        boolean mailSent = false;

        String from = Config.GetInstance().getProperty("mail.from");

        String destinatari_mail = getEmailRuolo(ruolo);
        if (destinatari_mail != null && destinatari_mail.indexOf(",") != -1) {
            destinatari_mail = destinatari_mail.replaceAll(",", ";");
        }

        

        String oggetto = "";
        

        String corpo = "Gentile Utente,\nquesta è una email di avviso inviata dall'applicazione web <b>IRIDE</b> (<a href='http://www.chevroletiride.com'>www.chevroletiride.com</a>).\n\nE' stata richiesta l'approvazione di una o più modifiche nei parametri del sistema\n\n";
        corpo += "\n\nLa invitiamo a collegarsi al sito <a href='http://www.chevroletiride.com'>www.chevroletiride.com</a> per poterne completare la gestione.\n\n\nCordiali Saluti\nIride";
        

        //invioMail.setFooter("\n\n" + Config.GetInstance().getProperty("mail.privacy"));

        @SuppressWarnings("unused")
		String firmaDigitale = applicationSrv.getRoot() + "img/logo_piccolo.png";
        //invioMail.setSignFileName(firmaDigitale);
        System.out.println(destinatari_mail);
        SendSMTPMail sendSMTPMail = new SendSMTPMail();
        sendSMTPMail.setFrom(from);
        sendSMTPMail.setSubject(oggetto);
        sendSMTPMail.setBody(corpo);
        sendSMTPMail.setTo(destinatari_mail);
        sendSMTPMail.setServer(Config.GetInstance().getProperty("mail.SMTPHost"));

        try {
            MyAuthenticator auth = null;
            if (!Config.GetInstance().getProperty("mail.SMTPHost.user", "").equals("")) {
                auth = new MyAuthenticator();
            }
            sendSMTPMail.prepareMail(auth, false, "", "", "S");

            DeferredMailSender.getInstance().offer(sendSMTPMail);
            //templateData.put("EMAIL_INVIATA", "OK");
            //templateData.put("EMAIL_INVIATA_MESSAGE",
                   // Config.GetInstance().getProperty("Message.email_inviata_ok", NO_MESSAGE));

        } catch (Throwable e) {
            //templateData.put("EMAIL_INVIATA", "KO");
            //templateData.put("EMAIL_INVIATA_MESSAGE",
                  //  Config.GetInstance().getProperty("Message.email_inviata_ko", NO_MESSAGE));
            new AppCrash(e);
        }
        
        return mailSent;
    }

    private static String getEmailRuolo(String ruolo) {

        DataSet_itf dataSet = null;
        String email = "";

        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", "DataSetMailRuolo");
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("RUOLO", ruolo);
            dataSet.setParam(parameters);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf row = (Row_itf) dataSet.nextElement();
                email += (String) row.getField("EMAIL") + ",";

            }

        } catch (AppCrash ac) {
            ac.logContext("getEmail", "errore nell'apertura del dataset DataSetMailRuolo");

        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (AppCrash ac) {
                    ac.logContext("getEmail", "errore nella chiusura del dataset DataSetMailRuolo");
                }
            }
        }

        return email;
    }

    /**
     * Data di inizio del QUARTER corrente
     * 
     * @return String data inizio Quarter
     */
    public static String getCurrentQuarterBeginDate() {

        String quarterBeginDate = "";

        switch (getQuarterIncludesMonth(Utils.getMeseOggi())) {
            case 1:
                quarterBeginDate = "01/01/" + Utils.getAnnoOggi();
                break;
            case 2:
                quarterBeginDate = "01/04/" + Utils.getAnnoOggi();
                break;
            case 3:
                quarterBeginDate = "01/07/" + Utils.getAnnoOggi();
                break;
            case 4:
                quarterBeginDate = "01/10/" + Utils.getAnnoOggi();
                break;
        }

        return quarterBeginDate;
    }

    /**
     * Data di fine del QUARTER corrente
     * 
     * @return String data fine Quarter
     */
    public static String getCurrentQuarterEndDate() {

        String quarterEndDate = "";

        switch (getQuarterIncludesMonth(Utils.getMeseOggi())) {
            case 1:
                quarterEndDate = "31/03/" + Utils.getAnnoOggi();
                break;
            case 2:
                quarterEndDate = "30/06/" + Utils.getAnnoOggi();
                break;
            case 3:
                quarterEndDate = "30/09/" + Utils.getAnnoOggi();
                break;
            case 4:
                quarterEndDate = "31/12/" + Utils.getAnnoOggi();
                break;
        }

        return quarterEndDate;
    }



    public static String fillWithZerosLeft(String stringToFillLeft, int stringLenght) {

        String filled = "";
        for (int i = 0; i < stringLenght; i++) {
            filled += "0";
        }
        filled += stringToFillLeft.trim();
        filled = filled.substring(filled.length() - stringLenght);

        return filled;
    }

    public static String fillWithBlanksLeft(String stringToFillLeft, int stringLenght) {

        String filled = "";
        for (int i = 0; i < stringLenght; i++) {
            filled += " ";
        }
        filled += stringToFillLeft.trim();
        filled = filled.substring(filled.length() - stringLenght);

        return filled;
    }

    /**
     * Data di fine mese dell'ultimo mese elaborato Sales To Trade
     * 
     * @return String data fine mese dell'ultimo mese elaborato
     */
    public static String[] getDataFineUltimoMeseElaboratoSalesToTrade() {

        DataSet_itf dataSetRiga = null;
        String anno = "";
        String mese = "";

        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dataSetRiga = dsFactory.makeDataSet("", "DataSetUltimoMeseElaboratoSalesToTrade");
            dataSetRiga.open();

            while (dataSetRiga.hasMoreElements()) {

                Row_itf dbRow = (Row_itf) dataSetRiga.nextElement();

                anno = (String) dbRow.getField("ANNO");
                mese = (String) dbRow.getField("MESE");

            }

        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("IrideUtils.getAnnoMeseElaborazione",
                    "Errore nella elaborazione del dataset DataSetAnnoMeseElaborazione");
        } finally {
            if (dataSetRiga != null) {
                try {
                    dataSetRiga.close();
                } catch (AppCrash ac) {
                    ac.logContext("IrideUtils.getAnnoMeseElaborazione",
                            "Errore nella close del dataset DataSetAnnoMeseElaborazione");
                }
            }
        }

        String[] result = new String[] { "", "", "" };
        if (!anno.equals("") && !mese.equals("")) {
            result[0] = "31/" + mese + "/" + anno;
            result[1] = Utils.getDescrizioneMese(Integer.parseInt(mese));
            result[2] = anno;
        }

        return result;
    }

    

    public static void executeQuery(String sqlQuery) throws AppCrash {

        DBTransaction dbtransaction = new DBTransaction();

        try {
            executeQuery(dbtransaction, sqlQuery);
            dbtransaction.commit();
            dbtransaction.end();
            
        } catch (Throwable e) {
            if (dbtransaction != null) {
                dbtransaction.rollBack();
            }
            AppCrash ac = new AppCrash(e);
            throw ac;
        }

    }

    public static void executeQuery(DBTransaction dbtransaction, String sqlQuery) throws AppCrash {

        if (sqlQuery == null || sqlQuery.equals("")) {
            return;
        }

        PreparedStatement ps = null;

        try {

            ps = dbtransaction.prepareStatement(sqlQuery);
            ps.execute();

            dbtransaction.commit();

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            throw ac;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable close) {
                AppCrash appCrashClose = new AppCrash(close);
                appCrashClose.logContext("IrideUtils.executeQuery",
                        "Errore durante la chiusura del PreparedStatement - sqlQuery:" + sqlQuery);
                throw appCrashClose;
            }
        }
    }
    
    
    public static String getDatiDipendente(String dipendente) throws AppCrash {
        String dati = "";
        DataSet_itf dataSet = null;
        try {
            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", DATASET_DIP);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("DIPENDENTE", dipendente);
            dataSet.setParam(params);
            dataSet.open();
            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
              	String azienda=dbRow.getField("AZIENDA").toString().trim();
            	dati= azienda;
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
                    //ac.logContext(getClass().getName(), "Errore nella close del dataset");
                }
            }
        }
        return dati;
    }

}
