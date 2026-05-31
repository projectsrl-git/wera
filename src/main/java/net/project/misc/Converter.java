/*
  Converter.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 26/07/2001

  Autore: Rosella V.

  Note:

  Modifiche:

 */

package net.project.misc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import net.project.errors.AppCrash;
import net.project.errors.InvariantCrash;

/**
 * Classe per la conversione nelle diverse codifiche.
 */

public class Converter {

    public static final String ASCII_ENCODING  = "8859_1";
    public static final String EBCDIC_ENCODING = "Cp500";

    /**
     * Costruttore.
     */
    private Converter() {

        super();
    }

    /**
     * Converte un array di byte in ingresso secondo la codifica richiesta.
     * 
     * @param input byte[] array di byte da convertire.
     * @param encoding java.lang.String codifica da applicare.
     * @return byte[].
     * @exception net.project.errors.AppCrash.
     */
    public static byte[] convertIn(byte[] input, String encoding) throws AppCrash {

        try {
            input = new String(input, encoding).getBytes();

        } catch (IOException e) {
            AppCrash appCrash = new AppCrash(e);
            appCrash.logContext("net.project.Converter", "errore di coversione : input = " + input + ", encoding = "
                    + encoding);
            throw appCrash;
        }
        return input;
    }

    /**
     * Converte un array di byte in ingresso secondo la codifica richiesta.
     * 
     * @param input byte[] array di byte da convertire.
     * @param encoding java.lang.String codifica da applicare.
     * @return byte[].
     * @exception net.project.errors.AppCrash.
     */
    public static byte[] convertOut(byte[] input, String encoding) throws AppCrash {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(baos, encoding);
            osw.write(new String(input), 0, input.length);
            osw.flush();
            input = baos.toByteArray();
            osw.close();
            baos.close();

        } catch (IOException e) {
            AppCrash appCrash = new AppCrash(e);
            appCrash.logContext("net.project.Converter", "errore di coversione : input = " + input + ", encoding = "
                    + encoding);
            throw appCrash;
        }
        return input;
    }

    /**
     * Converte una stringa in ingresso secondo la codifica richiesta.
     * 
     * @param input java.lang.String stringa da convertire.
     * @param encoding java.lang.String codifica da applicare.
     * @return java.lang.String.
     * @exception net.project.errors.AppCrash.
     */
    public static String convertOut(String input, String encoding) throws AppCrash {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(baos, encoding);
            osw.write(input, 0, input.length());
            osw.flush();
            byte[] temp = baos.toByteArray();
            input = new String(temp);
            osw.close();
            baos.close();

        } catch (IOException e) {
            AppCrash appCrash = new AppCrash(e);
            appCrash.logContext("net.project.Converter", "errore di coversione : input = " + input + ", encoding = "
                    + encoding);
            throw appCrash;
        }
        return input;
    }

    /**
     * Questo metodo converte una stringa esadecimale in un array binario della lunghezza voluta. Se la stringa in
     * ingresso e' piu' corta del necessario vengono aggiunti zeri alla fine del buffer.
     *
     * @param hex stringa esedecimale da convertire
     * @param num lunghezza voluta
     * @return byte[] il buffer convertito
     * @exception net.project.errors.AppCrash
     */
    public static byte[] hexStringToNBytes(String hex, int num) throws AppCrash {

        byte[] result = new byte[num];
        for (int i = 0; i < num; i++) {
            result[i] = 0;
        }

        char ch = 0;
        byte b = 0;
        int k = 0;
        for (int j = 0; j < hex.length(); j++) {
            ch = hex.charAt(j);
            b = (byte) (hexValue(ch) * (byte) 16);

            j++;
            ch = hex.charAt(j);
            b = (byte) (b + hexValue(ch));
            result[k] = b;
            k++;
        }

        return result;
    }

    /**
     * Questo metodo esegue l'escaping SQL DB2: converte gli ' in ''
     *
     * @param field la stringa da convertire
     * @return la stringa convertita
     */
    static public String sqlEscape(String field) {

        if (field != null && field.indexOf("'") != -1) {
            field = field.replaceAll("'", "''");
        }

        return field;
    }

    /**
     * Questo metodo esegue l'unescaping SQL DB2: converte i '' in '
     *
     * @param field la stringa da convertire
     * @return la stringa convertita
     * @throws AppCrash nel caso in cui il campo passato sia null
     */
    static public String sqlUnEscape(String field) throws AppCrash {

        if (field != null && field.indexOf("''") != -1) {
            field = field.replaceAll("''", "'");
        }

        return field;
    }

    private static byte hexValue(char ch) throws AppCrash {

        switch (ch) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'a':
            case 'A':
                return 10;
            case 'b':
            case 'B':
                return 11;
            case 'c':
            case 'C':
                return 12;
            case 'd':
            case 'D':
                return 13;
            case 'e':
            case 'E':
                return 14;
            case 'f':
            case 'F':
                return 15;
            default:
                throw new InvariantCrash("Hex char non permesso : " + ch);
        }
    }

}
