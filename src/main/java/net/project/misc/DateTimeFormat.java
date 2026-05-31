/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.misc;

/*
 DateTimeFormat.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 03/06/1999

 Autore: Rosella V.

 Note:

 Modifiche:
 25/11/99    TL17 ottimizzazione uso stringhe. Rosella V.
 13/04/00    Aggiunta formattazione per AAAAMMGG o AAMMGG
 17/08/01    Aggiunta formattazione per Timestamp DB2. Rosella V.

 */
import java.util.GregorianCalendar;
import java.util.Random;

/**
 * Consente la formattazione di ora e data attraverso una maschera di formattazione.
 */
public class DateTimeFormat {

    public final static String DEFAULT_DATE_MASK_4YEAR = "YYYYMMDD";
    public final static String DEFAULT_DATE_MASK_2YEAR = "YYMMDD";
    public final static String DEFAULT_DATE_MASK       = "MMDD";
    public final static String DEFAULT_TIME_MASK       = "hhmmss";
    public final static String DB2_DATE_MASK           = "YYYY-MM-DD";
    public final static String SQL_DATE_MASK           = "YYYY/MM/DD";
    public final static String DB2_TIME_MASK           = "hh.mm.ss";
    public final static String INF_TIME_MASK           = "hh:mm:ss";
    public final static String DB2_TIMESTAMP_MASK      = "YYYY-MM-DD-hh.mm.ss";
    public final static String DEFAULT_TIMESTAMP_MASK  = "YYYY-MM-DD-hh.mm.ss";
    private static Random      _randomSource           = new Random();
    private String             mask                    = null;
    private int                numY                    = 0;
    private int                numM                    = 0;
    private int                numD                    = 0;
    private int                numh                    = 0;
    private int                numm                    = 0;
    private int                nums                    = 0;

    /**
     * Costruttore. Se nessuna maschera viene impostata vengono considerate quelle di default.
     */
    public DateTimeFormat() {

    }

    /**
     * Costruttore. Imposta la maschera di formattazione.
     *
     * @param mask java.lang.String maschera di formattazione per data o ora.
     */
    public DateTimeFormat(String mask) {

        this.mask = mask;
    }

    /**
     * Controlla che la maschera sia un modello valido per la data o l'ora.
     *
     * @param maskModel java.lang.String La maschera di formattazione.
     *
     * @return boolean true se la maschera per la data o l'ora è valida, false altrimenti
     */
    private boolean checkMask(String maskModel) {

        boolean isValidMask = false;
        String tempMask = null;

        if (maskModel.equals(DEFAULT_DATE_MASK_2YEAR)) {
            numY = 2;
            numM = 2;
            numD = 2;

            isValidMask = true;
        }

        return isValidMask;
    }

    /**
     * Ritorna la data formattata in base alla maschera scelta.
     *
     * @param month int mese.
     * @param day int giorno.
     *
     * @return java.lang.String La data formattata oppure null se non è stato possibile formattare la data (maschera
     *         errata).
     */
    public String dateFormat(int month, int day) {

        String date = null;
        boolean check = true;
        StringBuffer tempDate = null;
        String tempString = null;

        if ((mask == null) || (mask.equals(DEFAULT_DATE_MASK))) { // se non è stata impostata nessuna maschera viene
                                                                  // considerata DEFAULT_DATE_MASK

            if (mask == null) {
                mask = DEFAULT_DATE_MASK;
            }

            numM = 2;
            numD = 2;
        } else {
            check = checkMask(mask);
        }

        if (check) {
            tempDate = new StringBuffer();
            tempString = Integer.toString(month);

            if (tempString.length() < numM) {
                for (int i = 0; i < (numM - tempString.length()); i++) {
                    tempDate.append("0");
                }
            }

            tempDate.append(tempString);
            tempString = Integer.toString(day);

            if (tempString.length() < numD) {
                for (int i = 0; i < (numD - tempString.length()); i++) {
                    tempDate.append("0");
                }
            }

            tempDate.append(tempString);
            date = tempDate.toString();
        }

        return date; // ritorna null se non è stato possibile formattare la data (maschera errata)
    }

    /**
     * Ritorna data formattata in base alla maschera scelta.
     *
     * @param year int anno
     * @param month int mese
     * @param day int giorno
     *
     * @return java.lang.String
     */
    public String dateFormat(int year, int month, int day) {

        // Logger.GetInstance().log3("Class: DateTimeFormat; Method: dateFormat(int, int, int); mask = " + mask);
        String date = null;
        boolean check = true;
        StringBuffer tempDate = null;
        String tempString = null;

        // se non è stata impostata nessuna maschera viene considerata DEFAULT_DATE_MASK_YEAR
        if ((mask == null) || mask.equals(DB2_DATE_MASK) || mask.equals(DEFAULT_DATE_MASK_4YEAR)) {
            if (mask == null) {
                mask = DEFAULT_DATE_MASK_4YEAR;
            }

            numM = 2;
            numD = 2;
            numY = 4;
        } else {
            check = checkMask(mask);
        }

        if (check) {
            tempDate = new StringBuffer();
            tempString = Integer.toString(year);

            if (tempString.length() < numY) {
                for (int i = 0; i < (numY - tempString.length()); i++) {
                    tempDate.append("0");
                }
            }

            tempDate.append(tempString);
            tempString = Integer.toString(month);

            if (tempString.length() < numM) {
                for (int i = 0; i < (numM - tempString.length()); i++) {
                    tempDate.append("0");
                }
            }

            tempDate.append(tempString);
            tempString = Integer.toString(day);

            if (tempString.length() < numD) {
                for (int i = 0; i < (numD - tempString.length()); i++) {
                    tempDate.append("0");
                }
            }

            tempDate.append(tempString);
            date = tempDate.toString();
        }

        // se la maschera è DB2_DATE_MASK, si aggiungono i separatori alla data formattata secondo la maschera di
        // default
        if (mask.equals(DB2_DATE_MASK)) {
            // Logger.GetInstance().log3("Class: DateTimeFormat; Method: dateFormat(int, int, int); date = " + date);
            date = addSeparators(date);

            // Logger.GetInstance().log3("Class: DateTimeFormat; Method: dateFormat(int, int, int); formattedDate = " +
            // date);
        }

        return date; // ritorna null se non è stato possibile formattare la data (maschera errata)
    }

    // Metodo privato che riceve una data formattata secondo la maschera di default a 4 cifre per l'anno (YYYYMMDD)
    // e restituisce la stessa data formattata con i separatori (YYYY-MM-DD)
    private String addSeparators(String date) {

        StringBuffer temp = new StringBuffer();
        temp.append(date.substring(0, 4));
        temp.append("-");
        temp.append(date.substring(4, 6));
        temp.append("-");
        temp.append(date.substring(6, 8));

        return temp.toString();
    }

    /**
     * Imposta la maschera di formattazione.
     *
     * @param mask java.lang.String maschera di formattazione.
     */
    public void setMask(String mask) {

        this.mask = mask;
    }

    /**
     * Ritorna l'ora formattata in base alla maschera scelta.
     *
     * @param hour int ora
     * @param minutes int minuti
     * @param seconds int secondi
     *
     * @return java.lang.String L'ora formattata oppure null se non è stato possibile formattare l'ora (maschera
     *         errata).
     */
    public String timeFormat(int hour, int minutes, int seconds) {

        String time = null;
        boolean check = true;
        StringBuffer tempTime = null;
        String tempString = null;

        if ((mask == null)
                || (mask.equals(INF_TIME_MASK) || (mask.equals(DB2_TIME_MASK) || (mask.equals(DEFAULT_TIME_MASK))))) { // se
                                                                                                                       // non
                                                                                                                       // è
                                                                                                                       // stata
                                                                                                                       // impostata
                                                                                                                       // nessuna
                                                                                                                       // maschera
                                                                                                                       // viene
                                                                                                                       // considerata
                                                                                                                       // DEFAULT_TIME_MASK

            if (mask == null) {
                mask = DEFAULT_TIME_MASK;
            }

            numh = 2;
            numm = 2;
            nums = 2;
        } else {
            check = checkMask(mask);
        }

        if (check) {
            tempTime = new StringBuffer();
            tempString = Integer.toString(hour);

            if (tempString.length() < numh) {
                for (int i = 0; i < (numh - tempString.length()); i++) {
                    tempTime.append("0");
                }
            }

            tempTime.append(tempString);
            tempString = Integer.toString(minutes);

            if (tempString.length() < numm) {
                for (int i = 0; i < (numm - tempString.length()); i++) {
                    tempTime.append("0");
                }
            }

            tempTime.append(tempString);
            tempString = Integer.toString(seconds);

            if (tempString.length() < nums) {
                for (int i = 0; i < (nums - tempString.length()); i++) {
                    tempTime.append("0");
                }
            }

            tempTime.append(tempString);
            time = tempTime.toString();

            if (mask.equals(DB2_TIME_MASK)) {
                StringBuffer temp = new StringBuffer();
                temp.append(time.substring(0, 2));
                temp.append(".");
                temp.append(time.substring(2, 4));
                temp.append(".");
                temp.append(time.substring(4, 6));
                time = temp.toString();
            } else if (mask.equals(INF_TIME_MASK)) {
                StringBuffer temp = new StringBuffer();
                temp.append(time.substring(0, 2));
                temp.append(":");
                temp.append(time.substring(2, 4));
                temp.append(":");
                temp.append(time.substring(4, 6));
                time = temp.toString();
            }
        }

        return time; // ritorna null se non è stato possibile formattare l'ora (maschera errata)
    }

    /**
     * Restituisce il timestamp corrente formattato secondo la maschera impostata.
     *
     * @return java.lang.String Timestamp formattato.
     */
    public String getTimestamp() {

        StringBuffer timestampBuffer = new StringBuffer();
        String timestamp = null;

        // per ora è gestito solo il formato DB2
        // Nel caso in cui venga impostata una maschera diversa da tale formato
        // viene restituita una stringa nulla
        if (mask == null) {
            mask = DEFAULT_TIMESTAMP_MASK;
        }

        if (mask.equals(DB2_TIMESTAMP_MASK)) {
            DateTimeFormat dtf = new DateTimeFormat();
            GregorianCalendar calendar = new GregorianCalendar();
            dtf.setMask(DB2_DATE_MASK);
            timestampBuffer.append(dtf.dateFormat(calendar.get(java.util.Calendar.YEAR),
                    calendar.get(java.util.Calendar.MONTH) + 1, calendar.get(java.util.Calendar.DAY_OF_MONTH)));
            timestampBuffer.append("-");
            dtf.setMask(DB2_TIME_MASK);
            timestampBuffer.append(dtf.timeFormat(calendar.get(java.util.Calendar.HOUR_OF_DAY),
                    calendar.get(java.util.Calendar.MINUTE), calendar.get(java.util.Calendar.SECOND)));

            String currentMillis = Integer.toString(calendar.get(java.util.Calendar.MILLISECOND));
            int num = _randomSource.nextInt(999);

            // fill con zeri perche' i millisecondi potrebbero essere meno di 100
            currentMillis = (currentMillis + "000").substring(0, 3);

            String millis = currentMillis + Integer.toString(num);
            timestampBuffer.append("." + millis);
            timestamp = timestampBuffer.toString();
        }

        return timestamp; // ritorna null se non è stato possibile formattare il timestamp (maschera non gestita)
    }

    /**
     * Restituisce il timestamp in ingresso formattato secondo la maschera impostata.
     * 
     * @param calendar GregorianCalendar Timestamp in ingresso
     * @return java.lang.String Timestamp formattato.
     */
    public String getTimestamp(GregorianCalendar calendar) {

        StringBuffer timestampBuffer = new StringBuffer();
        String timestamp = null;

        // per ora è gestito solo il formato DB2
        // Nel caso in cui venga impostata una maschera diversa da tale formato
        // viene restituita una stringa nulla
        if (mask == null) {
            mask = DEFAULT_TIMESTAMP_MASK;
        }

        if (mask.equals(DB2_TIMESTAMP_MASK)) {
            DateTimeFormat dtf = new DateTimeFormat();
            dtf.setMask(DB2_DATE_MASK);
            timestampBuffer.append(dtf.dateFormat(calendar.get(java.util.Calendar.YEAR),
                    calendar.get(java.util.Calendar.MONTH) + 1, calendar.get(java.util.Calendar.DAY_OF_MONTH)));
            timestampBuffer.append("-");
            dtf.setMask(DB2_TIME_MASK);
            timestampBuffer.append(dtf.timeFormat(calendar.get(java.util.Calendar.HOUR_OF_DAY),
                    calendar.get(java.util.Calendar.MINUTE), calendar.get(java.util.Calendar.SECOND)));

            String currentMillis = Integer.toString(calendar.get(java.util.Calendar.MILLISECOND));
            int num = _randomSource.nextInt(999);

            // fill con zeri perche' i millisecondi potrebbero essere meno di 100
            currentMillis = (currentMillis + "000").substring(0, 3);

            String millis = currentMillis + Integer.toString(num);
            timestampBuffer.append("." + millis);
            timestamp = timestampBuffer.toString();
        }

        return timestamp; // ritorna null se non è stato possibile formattare il timestamp (maschera non gestita)
    }

    /**
     * Restituisce la data in ingresso formattato secondo la maschera impostata.
     * 
     * @param calendar GregorianCalendar Timestamp in ingresso
     * @return java.lang.String Timestamp formattato.
     */
    public String getDate(GregorianCalendar calendar) {

        StringBuffer timestampBuffer = new StringBuffer();
        String timestamp = null;

        if (mask == null) {
            mask = SQL_DATE_MASK;
        }

        if (mask.equals(SQL_DATE_MASK)) {
            DateTimeFormat dtf = new DateTimeFormat();
            dtf.setMask(SQL_DATE_MASK);
            timestampBuffer.append(dtf.dateFormat(calendar.get(java.util.Calendar.YEAR),
                    calendar.get(java.util.Calendar.MONTH) + 1, calendar.get(java.util.Calendar.DAY_OF_MONTH)));
            timestamp = timestampBuffer.toString();
        }

        return timestamp; // ritorna null se non è stato possibile formattare il timestamp (maschera non gestita)
    }
}
