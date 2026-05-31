/*
  Tester.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 22/05/2001

  Autore: Assunta Ciervo

  Note: Classe che si occupa di calcolare il MAC.

  Modifiche:
  
 */

package net.project.mess.verify;

import java.io.IOException;
import java.io.RandomAccessFile;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.mess.MsgFactory_base;
import net.project.mess.MsgWriter_itf;
import net.project.misc.Config;
//import net.ssb.eps.payapi.dbaccess.*;
//import HTTPClient.*;
//import net.project.eps.payapi.*;

public class Tester {

    private MsgWriter_itf _msgWriter = null;

    private String        _url       = null;
    // private NVPair[] _formData = null;
    private String        _param     = null;
    private String        _data      = null;
    private String        _mac       = null;

    /**
     * Tester constructor .
     */
    public Tester() {

        super();
    }

    /**
     * Lettura parametri (name=vale) dal file dei prametri.
     * 
     * @param file RandomAccessFile File dei parametri.
     * @param line String Riga del file in lettura.
     * @excpetion net.project.errors.AppCrash.
     * @return boolean true se OK
     */
    private void creaMsg(RandomAccessFile file, String line) throws AppCrash {

        int i = -1;
        try {
            if (line.startsWith("#") == false) {
                i = line.indexOf("=");
                if (i != -1) {
                    if (!line.startsWith("TIPOMESSAGGIO")) {
                        setAttribute(line.substring(0, i).trim(), line.substring(i + 1).trim());
                    } else
                        _msgWriter = MsgFactory_base.GetInstance().MakeMsgWriter(line.substring(i + 1).trim());
                }
            }
            line = file.readLine();
            while (line != null) {
                if (line.startsWith("----") == false) {
                    i = line.indexOf("=");
                    Logger.GetInstance().log0(line);
                    if (i != -1) {
                        if (!line.startsWith("TIPOMESSAGGIO")) {
                            setAttribute(line.substring(0, i).trim(), line.substring(i + 1).trim());
                        } else
                            _msgWriter = MsgFactory_base.GetInstance().MakeMsgWriter(line.substring(i + 1).trim());
                    }
                    line = file.readLine();
                } else
                    break;
            }
        } catch (IOException io) {
            new AppCrash(io);
        }
    }

    /**
     * Restituisce il valore di un campo del messaggio di richiesta di pagamento
     * 
     * @param key String Nome del campo
     * @return String Valore del campo. Null se il campo non è stato trovato
     * @exception AppCrash Nel caso i paramtri ricevuti non siano corretti o se la lista dei campi è vuota
     */
    public String getAttribute(String key) throws AppCrash {

        // Controlli parametri in input
        ErrDetector.GetInstance().invariant(!(key == null), "Parametro key non valido. key=" + key);

        // return campo richiesto
        return (_msgWriter.getField(key));
    }

    /**
     * Client per testare API.
     *
     * @param args String[] array di parametri in ingresso.
     * @exception java.io.IOException.
     * @excpetion net.project.errors.AppCrash.
     */
    public static void main(String[] args) {

        Tester test = new Tester();
        try {
            // Legge il file di configurazione
            Config.InitInstance(args[0]);

            test.runVerifyMessage();

        } catch (IOException ioe) {
            System.out.println("Non riesco a leggere il file di inizializzazione " + args[0]);
            Logger.GetInstance().log0("tester non riesce a leggere il file di inizializzazione " + args[0]);
        } catch (Exception e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("tester", "Errore in main");
        }
    }

    /**
     * Lancio Verify LogicalMessageSpec.
     *
     * @param modalita String Modalità GET/POST letto dal file di configurazione.
     * @exception java.io.IOException.
     * @excpetion net.project.errors.AppCrash.
     * @excpetion java.lang.Exception.
     */
    public void runVerifyMessage() {

        String paramFile = "";

        int nVerify = 1;
        RandomAccessFile file = null;
        try {

            paramFile = Config.GetInstance().getProperty("verifier.parametri");
            ErrDetector.GetInstance().param(paramFile);

            String line = "";
            file = new RandomAccessFile(paramFile, "r");

            ErrDetector.GetInstance().preCond(file != null);
            // su tutto il file
            Logger.GetInstance().log0("------------------------------ Inizio -----------------------------------");
            while ((line = file.readLine()) != null) {
                Logger.GetInstance().log0("---------- Verify LogicalMessageSpec: " + nVerify);
                nVerify++;

                // Crea messaggio da verificare
                creaMsg(file, line);

                // Verifica messaggio
                Verifier verifier = Verifier.GetInstance();
                String result = verifier.verifyMessage(_msgWriter);
                // oppure String result = verifier.verifyMessage(_msgWriter.getType(), _msgWriter);
                if (result != null) {
                    break;
                }
            }
            Logger.GetInstance()
                    .log0("--------------------------------Fine-------------------------------------------");
        } catch (IOException ioe) {
            Logger.GetInstance().log0("Tester non riesce a leggere il file di dei parametri " + paramFile);
        } catch (AppCrash ex) {
            ex.logContext("Tester", "Errore in main parametri");
        } catch (Exception e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("Tester", "Errore in main");
        } finally {
            try {
                file.close();
            } catch (IOException ioe) {
            }

        }
    }

    /**
     * Imposta un campo del messaggio di richiesta di pagamento
     * 
     * @param key String Nome del campo
     * @param val String Valore del campo
     * @exception AppCrash Nel caso i paramtri ricevuti non siano corretti
     */
    public void setAttribute(String key, String val) throws AppCrash {

        // Controlli parametri in input
        ErrDetector.GetInstance().invariant(!(key == null), "Parametro key non valido. key=" + key);
        ErrDetector.GetInstance().invariant(!(val == null), "Parametro val non valido. val=" + val);

        // set campo in MsgWriter_itf
        _msgWriter.setField(key, val);
    }
}
