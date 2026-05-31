/*
  FileParser.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 19/05/1999

  Autore: Rosella V.

  Note:

  Modifiche:
				25/11/99	TL17 ottimizzazione uso stringhe. Rosella V.

 */

package net.project.misc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.StringTokenizer;
import java.util.Vector;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;

/**
 * Classe per il parser di un generico file in cui le proprietà siano organizzate su singole righe. Non vengono
 * considerati i commenti contenuti nelle righe del file (# = inizio commento su una riga) Viene letta la proprieta' di
 * configurazione <code>FileParser.encoding</code> che, se presente, indica il character encoding con il quale leggere i
 * file. Se non presente si utilizza il default della piattaforma.
 */

public abstract class FileParser {

    /**
     * Costruttore.
     */
    FileParser() {

    }

    /**
     * Ritorna un vettore di vettori. Gli elementi del vettore ritornato contengono tutte le informazioni. lette su una
     * linea.
     * 
     * @param fileName java.lang.String nome del file di cui fare il parsing.
     * @return java.util.Vector.
     * @exception net.project.errors.AppCrash Se il nome del file di cui fare il parsing è null.
     * @exception java.io.IOException.
     */
    public static Vector parse(String fileName) throws AppCrash {

        Vector vector = new Vector(); // vettore corrispondente al parsing del file in ingresso
        Vector value = null;

        try {
            String line = null;
            ErrDetector.GetInstance().param(fileName != null);

            String encoding = Config.GetInstance().getProperty("FileParser.encoding");

            Reader reader;
            if (encoding != null) {
                reader = new InputStreamReader(new FileInputStream(fileName), encoding);
            } else {
                reader = new FileReader(fileName);
            }
            BufferedReader file = new BufferedReader(reader);

            // su tutto il file
            while ((line = file.readLine()) != null) {
                value = parseLine(line); // parsing su una linea
                if (!value.isEmpty()) {
                    vector.addElement(value); // se su una linea esisteva almeno una informazione
                }
            }
            file.close();
        } catch (AppCrash e) {
            e.logContext("FileParser: ", fileName);
            throw e;
        } catch (IOException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("FileParser: ", fileName);
            throw err;
        }
        return vector;
    }

    /**
     * Ritorna un vettore i cui elementi sono le informazioni trovate su una linea.
     * 
     * @param line java.lang.String riga del file di cui fare il parsing.
     * @return java.util.Vector.
     * @exception net.project.errors.AppCrash Se si è verificato un errore durante il parsing di una linea del file.
     */
    private static Vector parseLine(String line) throws AppCrash {

        Vector vector = new Vector(); // vettore i cui elementi sono tutte le informazioni trovate su una linea

        try {
            ErrDetector.GetInstance().param(line != null);
            StringTokenizer parser = new StringTokenizer(line);
            String col = null;
            while (parser.hasMoreTokens()) {
                col = parser.nextToken();
                if (!col.startsWith("#")) {
                    // aggiunta al vettore di una campo trovato
                    vector.addElement(col);
                } else {
                    break;
                }
            }

        } catch (AppCrash e) {
            e.logContext("FileParser: ", "errore lettura linea -" + line + "-");
            throw e;
        }
        return vector;
    }

}
