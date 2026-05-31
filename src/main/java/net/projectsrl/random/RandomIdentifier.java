
package net.projectsrl.random;

import net.project.errors.AppCrash;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomSeedGenerator;

/**
 * Classe contenitore di metodi per la creazione di stringhe random.
 */
public class RandomIdentifier {

    private static final String           FILLER    = "abcdefghilmnopqrstuvz";
    private static final RandomIdentifier _Instance = new RandomIdentifier();
    private final MersenneTwister         _rndGen;

    /**
     * Costruttore.
     * 
     * @exception net.ssb.errors.AppCrash errori durante l'instanziazione dell'oggetto corrente.
     */
    private RandomIdentifier() {

        long t = System.currentTimeMillis();
        RandomSeedGenerator rsg = new RandomSeedGenerator((int) t, (int) (t >>> 16));
        _rndGen = new MersenneTwister(rsg.nextSeed());

    }

    /**
     * Implementazione del pattern Singleton. Restituisce l'istanza dell'oggetto.
     * 
     * @return it.ssb.pay4.util.RandomIdentifier Istanza dell'oggetto corrente.
     * @exception net.ssb.errors.AppCrash errori durante l'instanziazione dell'oggetto corrente.
     */
    public static RandomIdentifier GetInstance() throws AppCrash {

        return _Instance;
    }

    /**
     * Questo metodo restituisce un id alfanumerico della lunghezza desiderata con appeso in fondo la stringa code
     * passata in ingresso. La lunghezza e' da considerarsi complessiva: code e' compreso.
     * 
     * @param length lunghezza dell'id desiderato
     * @param code codice da appendere
     * @return String l'id generato
     */
    public String getIdentifier(int length, String code) {

        long rn;

        // Aggiunge in fondo alla parte Random, prima di code, un carattere che rappresenta l'anno in corso
        int anno = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) % Character.MAX_RADIX;
        String annoId = Integer.toString(anno, Character.MAX_RADIX);
        if (code == null || code.length() == 0) {
            code = annoId;
        } else {
            code = annoId + code;
        }

        int codeLen = code.length();

        synchronized (RandomIdentifier.class) {
            rn = _rndGen.nextLong();
        }
        String bigString = Long.toString(rn, Character.MAX_RADIX);
        if (rn < 0) {
            bigString = bigString.replace('-', (char) (97 + (Math.abs(_rndGen.nextInt()) % 26)));
        }

        // se devo generare un id lungo piu' di 11 char creo un altro random
        if (length > 11) {
            long rn2;

            synchronized (RandomIdentifier.class) {
                rn2 = _rndGen.nextLong();
            }
            bigString = bigString + Long.toString(rn2, Character.MAX_RADIX);
            if (rn2 < 0) {
                bigString = bigString.replace('-', (char) (97 + (Math.abs(_rndGen.nextInt()) % 26)));
            }

        }

        String result = null;

        if (bigString.length() >= length - codeLen) {
            result = bigString.substring(0, length - codeLen) + code;
        } else {
            result = bigString + FILLER.substring(0, length - bigString.length() - codeLen) + code;
        }

        return result;
    }

    /**
     * Questo metodo restituisce un id numerico della lunghezza desiderata con appeso in fondo la stringa code passata
     * in ingresso. La lunghezza e' da considerarsi complessiva: code e' compreso.
     * 
     * @param length lunghezza dell'id desiderato
     * @param code codice da appendere
     * @return String l'id generato
     */
    public String getIdentifierNumeric(int length, String code) {

        long rn;

        // Aggiunge in fondo alla parte Random, prima di code, un carattere che rappresenta l'anno in corso
        int anno = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) % 10;
        String annoId = Integer.toString(anno, 10);
        if (code == null || code.length() == 0) {
            code = annoId;
        } else {
            code = annoId + code;
        }

        int codeLen = code.length();

        synchronized (RandomIdentifier.class) {
            rn = _rndGen.nextLong();
        }

        String bigString = Long.toString(rn, 10);
        if (rn < 0) {
            bigString = bigString.replace('-', (char) (48 + (Math.abs(_rndGen.nextInt()) % 10)));
        }

        // se devo generare un id lungo piu' di 11 char creo un altro random
        if (length > 11) {
            long rn2;

            synchronized (RandomIdentifier.class) {
                rn2 = _rndGen.nextLong();
            }

            bigString = bigString + Long.toString(rn2, 10);
            if (rn2 < 0) {
                bigString = bigString.replace('-', (char) (48 + (Math.abs(_rndGen.nextInt()) % 10)));
            }

        }

        String result = null;

        if (bigString.length() >= length - codeLen) {
            result = bigString.substring(0, length - codeLen) + code;
        } else {
            result = bigString + FILLER.substring(0, length - bigString.length() - codeLen) + code;
        }

        return result;
    }

    /**
     * Restituisce un identificatore univoco generato in modo random della lunghezza voluta.
     * 
     * @param length lunghezza dell'id desiderato
     * @return String l'id generato
     */
    public String getIdentifier(int length) {

        String identifier = null;
        String idMacchina = System.getProperty("net.ssb.errors.servername", "p41");

        identifier = getIdentifier(length, idMacchina.substring(idMacchina.length() - 1));

        return identifier;
    }

    /**
     * Restituisce un identificatore univoco generato in modo random della lunghezza voluta.
     * 
     * @param length lunghezza dell'id desiderato
     * @return String l'id generato
     */
    public String getIdentifierNumeric(int length) {

        String identifier = null;
        String idMacchina = System.getProperty("net.ssb.errors.servername", "p41");

        identifier = getIdentifierNumeric(length, idMacchina.substring(idMacchina.length() - 1));

        return identifier;
    }
}