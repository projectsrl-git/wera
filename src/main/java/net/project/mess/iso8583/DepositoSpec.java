
package net.project.mess.iso8583;

/*
 DepositoSpec.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 28/05/1999

 Autore: Simone Z.

 Note:

 Modifiche:
 25/11/99	TL17 ottimizzazione uso stringhe. Rosella V.

 */

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.misc.Config;
import net.project.misc.FileParser;

/**
 * Questa rappresenta il deposito delle specifiche sintattiche dei messaggi ISO8583
 */
public class DepositoSpec {

    private static DepositoSpec _Instance;
    private Hashtable           _deposito = new Hashtable(31);

    private DepositoSpec() throws AppCrash {

        StringTokenizer parser = null;
        String path = Config.GetInstance().getProperty("ISO8583.Path");
        // Path relativo cerco di aggiungere in testa la root della applicazione
        path = Config.GetInstance().makeAbsolutePath(path);

        String descrittori = Config.GetInstance().getProperty("ISO8583.Descrittori");
        Logger.GetInstance().log2("DepositoSpec constructor invocation");
        try {
            parser = new StringTokenizer(descrittori);
            String file = null;
            Vector vectSpecifica = null;
            SpecificaMess specMess = null;

            for (int i = 0; parser.hasMoreTokens(); i++) {
                file = new StringBuffer().append(path).append(parser.nextToken()).toString();
                vectSpecifica = FileParser.parse(file);
                specMess = new SpecificaMess(vectSpecifica);
                _deposito.put(specMess.getTipoMess(), specMess);
            }
        } catch (AppCrash e) {
            e.logContext("DepositoSpec", "Path: " + path + " Descrittori: " + descrittori);
            throw e;
        }
    }

    /**
     * @roseuid 374E541403C5
     */
    public static DepositoSpec GetInstance() throws AppCrash {

        // questo e' un "Double checked lock" design pattern
        if (_Instance == null) {
            synchronized (DepositoSpec.class) {
                if (_Instance == null) {
                    _Instance = new DepositoSpec();
                }
            }
        }
        return _Instance;
    }

    /**
     * @roseuid 374E545B031D
     */
    public Enumeration getSpecifica(String tipoMess) throws AppCrash {

        Enumeration enumer;
        try {
            enumer = ((SpecificaMess) _deposito.get(tipoMess)).elements();
            ErrDetector.GetInstance().invariant(enumer != null);
        } catch (AppCrash err) {
            err.logContext("DepositoSpec", "Specifica non trovata: " + tipoMess);
            throw err;
        }
        return enumer;
    }

}
