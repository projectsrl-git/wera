/*
  PageTester.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione:

  Autore: Pietro G. e Luca M.

  Note:

  Modifiche:

 */

package net.project.servlet.gui;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import net.project.errors.ErrDetector;
import net.project.misc.Config;

/**
 * Classe che effettua il test del package net.project.servlet.gui.
 */
public class PageTester {

    /**
     * Costruttore.
     */
    public PageTester() {

    }

    /**
     * Effettua il test del package net.project.servlet.gui.
     * 
     * @param java.lang.String[] args array di parametri in ingresso
     * @param java.lang.String args[0] nome della configurazione
     * @param java.lang.String args[1] nome della pagina da costruire
     * @param java.lang.String args[2] nome del file di output
     * @param java.lang.String args[3] nome del file di configurazione
     */
    public static void main(String[] args) {

        // Preparazione dati per logging di eventuali crash
        StringBuffer temp = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            temp.append(" args[");
            temp.append(i);
            temp.append("] = ");
            temp.append(args[i]);
        }
        String status = temp.toString();

        // Istanziazione della pagina
        try {
            Config.InitInstance(args[3]);
            Config.InitInstance(args[0], args[3]);
            System.out.println();
            System.out.println("adesso sto per scrivere sul file di log");
            System.out.println();
            System.out.println("configurazione inizializzata");
            System.out.println();
            PageFactory pf = PageFactory.getInstance();
            ErrDetector.GetInstance().param(pf);
            System.out.println("PageFactory istanziata");
            System.out.println();
            Page_itf page = pf.makePage(args[0], args[1]);
            ErrDetector.GetInstance().param(page);
            System.out.println("Page istanziata");
            System.out.println();
            OutputStream outSt = new FileOutputStream(args[2]);
            System.out.println("OutputStream istanziato");
            System.out.println();
            PrintWriter pw = new PrintWriter(outSt);
            System.out.println("PrintWriter istanziato");
            System.out.println();
            page.display(pw);
            System.out.println("display() di page eseguita");
            System.out.println();
            pw.close();
            System.out.println("Test concluso.");
            System.out.println();
            System.out.println("Verificare la corretta creazione del file di output: " + args[2]);
            System.out.println();

        } catch (Throwable t) {
            System.out.println(t.getMessage() + " - " + status);
        } finally {
            System.out.println("(Se necessario, premere CTRL^C per sbloccare il cursore)");
            System.out.println();
        }
    }

}
