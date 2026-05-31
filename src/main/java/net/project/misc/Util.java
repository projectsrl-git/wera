
package net.project.misc;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Questa
 */
public class Util {

    /**
     * Costruttore privato in quanto la classe contiene solo metodi static e non deve percio' essere istanziata.
     */
    private Util() {

        super();
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
     * Dice se una stringa e' vuota. Per 'vuota' s'intende null, stringa vuota, o stringa di soli blank.
     * 
     * @param what java.lang.String La stringa da controllare.
     * 
     * @return boolean true se la stringa e' null, e' vuota o e' di soli blank; false altrimenti.
     */
    public static boolean IsEmpty(String what) {

        return ((what == null) || (what.trim().length() == 0));
    }

    /**
     * Questo metodo verifica se l'oggetto stringa contiene un valore diverso stringa vuota o null.
     * 
     * @param valore java.lang.Object Oggetto stringa da verificare
     * 
     * @return true se la stringa contiene un valore diverso da stringa vuota o null, altrimenti false
     */
    public static boolean IsNotEmpty(Object valore) {

        if (valore != null) {
            return IsNotEmpty(valore.toString());
        } else {
            return false;
        }
    }

    /**
     * Questo metodo verifica se l'oggetto stringa contiene un valore stringa vuota o null.
     * 
     * @param valore java.lang.Object Oggetto stringa da verificare
     * 
     * @return true se la stringa contiene un valore uguale da stringa vuota o null, altrimenti false
     */
    public static boolean IsEmpty(Object valore) {

        if (valore != null) {
            return IsEmpty(valore.toString());
        } else {
            return true;
        }
    }

    /**
     * Ritorna la data corrente nel formato passato in ingresso
     * 
     * @param formato java.lang.String Stringa contenente il formato che si vuole utilizzare
     * 
     * @return java.lang.String Il timestamp corrente.
     */
    public static String getCurrentTimestamp(String formato) {

        SimpleDateFormat s = new SimpleDateFormat(formato);
        Date currentTime = new Date();
        String dateString = s.format(currentTime);

        return dateString;
    }

    /**
     * Formatta una stringa rappresentazione di un timestamp passato in ingresso nel formato passato in ingresso.
     * 
     * @param formato java.lang.String Stringa contenente il formato che si vuole utilizzare
     * @param timestamp java.lang.String string contenente il timestamp da formattare
     * @return java.lang.String Il timestamp nel formato desiderato.
     */
    public static String formatStringTimestamp(String formato, String timestamp) {

        SimpleDateFormat s = new SimpleDateFormat(formato);
        String dateString = s.format(timestamp);

        return dateString;
    }

    /**
     * Formatta un timestamp passato in ingresso nel formato passato in ingresso.
     * 
     * @param formato java.lang.String Stringa contenente il formato che si vuole utilizzare
     * @param timestamp Timestamp timestamp da formattare
     * @return java.lang.String Il timestamp nel formato desiderato.
     */
    public static String formatTimestamp(String formato, Timestamp timestamp) {

        SimpleDateFormat s = new SimpleDateFormat(formato);
        String dateString = s.format(timestamp);

        return dateString;
    }

}
