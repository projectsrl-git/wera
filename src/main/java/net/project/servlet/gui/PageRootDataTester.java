/*
  PageRootDataTester.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione:

  Autore: Luca M.

  Note:

  Modifiche:

 */

package net.project.servlet.gui;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.errors.ErrDetector;
import net.project.misc.Config;

/**
 * Classe che effettua il test del metodo setPageRootData(DataSet_itf, String) della classe
 * net.project.servlet.gui.FreeMarkerPage
 */
public class PageRootDataTester {

    public PageRootDataTester() {

    }

    /**
     * Metodo che effettua il test.
     * 
     * @param java.lang.String[] args array di parametri
     * @param java.lang.String args[0] nome completo del file di configurazione
     * @param java.lang.String args[1] nome completo del file di output
     */
    public static void main(String[] args) throws Throwable {

        Config.InitInstance(args[0]);
        System.out.println("Letto configfile");
        PageFactory pf = PageFactory.getInstance();
        System.out.println("Istanziata classe PageFactory");
        Page_itf page = pf.makePage("", "Tester");
        System.out.println("Istanziata classe che implementa Page_itf");
        DataSetFactory dsf = DataSetFactory.getInstance();
        System.out.println("Istanziata classe DataSetFactory");
        DataSet_itf dataset = dsf.makeDataSet("", "guiDataSet");
        ErrDetector.GetInstance().param(dataset);
        System.out.println("Istanziata classe che implementa DataSet_itf");
        page.setPageRootData(dataset, "node");
        System.out.println("Effettuato setPageRootData(DataSet_itf, String)");
        OutputStream outSt = new FileOutputStream(args[1]);
        System.out.println("Istanziata classe FileOutputStream");
        PrintWriter pw = new PrintWriter(outSt);
        System.out.println("Istanziata classe PrintWriter");
        page.display(pw);
        System.out.println("Effettuato display di Page su PrintWriter");
        pw.flush();
        System.out.println("Effettuato flush di PrintWriter");
        pw.close();
        System.out.println("Effettuato close di PrintWriter");

    }

}
