/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.servlet.gui;

import net.project.errors.AppCrash;

/**
 * Questa classe e' un decorator per oggetti esclusivamente di tipo FreeMarkerPage. E' semplicemente una classe di
 * utilita' per facilitare la scrittura di decorator di questo tipo
 */
public class FreeMarkerPageDecorator extends PageDecorator {

    // Attributo dichiarato protetto intenzionalmente: deve essere accedibile dalle
    // sottoclassi che implementano la decorazione vera e propria
    protected FreeMarkerPage _theFMPage;

    /**
     * Il costruttore esegue il cast a FreeMarkerPage e lancia una AppCrash nel caso ci sia un errore
     * 
     * @param page
     */
    public FreeMarkerPageDecorator(Page_itf page) throws AppCrash {

        super(page);
        if (page instanceof FreeMarkerPage) {
            _theFMPage = (FreeMarkerPage) page;
        } else {
            throw new AppCrash("FreeMarkerPageDecorator - Page non e' una FreeMarkerPage");
        }
    }

}
