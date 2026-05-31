/*
  Function_itf.java
    
  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 04/10/2000

  Autore: Anna L.

  Note:

  Modifiche:
  
 */

package net.project.servlet.frame;

import net.project.errors.AppCrash;

/**
 * Definisce l'interfaccia utilizzata per gestire le funzioni.
 */
public interface Function_itf {

    /**
     * Attiva una funzione.
     * 
     */
    public void start();

    /**
     * Ferma una funzione.
     * 
     */
    public void stop();

    /**
     * La doget base verifica che l'applicazione e/o la funzione siano attive (Stato), e che l'utente possa accedere
     * alla funzione (richiamando checkPermission() della ServletApplication). Se tutto OK richiama la processGet().
     * 
     * @param req net.project.servlet.frame.SsbServletRequest che incapsula la richiesta al servlet.
     * @param res net.project.servlet.frame.SsbServletResponse che incapsula la risposta dal servlet.
     * 
     * @exception AppCrash
     */
    public void doGet(SsbServletRequest req, SsbServletResponse res) throws AppCrash;

    /**
     * La doPost base verifica che l'applicazione e/o la funzione siano attive (Stato), e che l'utente possa accedere
     * alla funzione (richiamando checkPermission() della ServletApplication). Se tutto OK richiama la processPost().
     * 
     * @param req net.project.servlet.frame.SsbServletRequest che incapsula la richiesta al servlet.
     * @param res net.project.servlet.frame.SsbServletResponse che incapsula la risposta dal servlet.
     * 
     * @exception AppCrash
     */
    public void doPost(SsbServletRequest req, SsbServletResponse res) throws AppCrash;

    /**
     * Effettua operazioni propedeutiche all'elaborazione della get o post.
     * 
     * @param req net.project.servlet.frame.SsbServletRequest oggetto che incapsula la richiesta dal client alla servlet
     * @param res net.project.servlet.frame.SsbServletResponse oggetto che incapsula la risposta della servlet al
     *            client.
     * @exception net.project.errors.AppCrash
     */
    public void prepareForRequest(SsbServletRequest req, SsbServletResponse res) throws AppCrash;

    /**
     * Questo metodo ritorna il nome della funzione
     *
     * @return String nome della funzione
     */
    public String getName();

    /**
     * Questo metodo ritorna il function ID della funzione
     *
     * @return String function ID
     */
    public String getFunctionID();

}