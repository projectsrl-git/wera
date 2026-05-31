
package net.project.servlet.frame;

import net.project.errors.AppCrash;
import net.project.servlet.security.UserSecurityInfo;

/*
 XMLFunction_base.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 11/10/2001

 Autore: Assunta C.

 Note:

 Modifiche:

 */

/**
 * Questa classe è la rappresentazione astratta di una funzione che elabora richieste XML. E' una estensione della
 * classe Function_base. I metodi mostra() ed elabora() sono resi equivalenti e viene lasciato da implementare il solo
 * metodo elabora().
 * 
 */
public class XMLFunction_base extends Function_base {

    /**
     * XMLFunction_base constructor comment.
     */
    public XMLFunction_base() {

        super();
    }

    /**
     * XMLFunction_base constructor comment.
     * 
     * @param applServices net.project.servlet.frame.ApplicationServices_itf
     * @param functionID java.lang.String
     * @param functionName java.lang.String
     */
    public XMLFunction_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    /**
     * Esegue l'azione da compiere in caso di fallimento dell'autenticazione.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     */
    @Override
    protected void authenticationFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

    }

    /**
     * Controlla la presenza e la validità dei campi della richiesta.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @return true se i campi sono formattati correttamente, false altrimenti.
     */
    @Override
    protected boolean checkField(SsbServletRequest req) {

        return false;
    }

    /**
     * Esegue l'azione da compiere in caso di non corretta formattazione dei campi necessari all'autenticazione.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     */
    @Override
    protected void checkFieldAutFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

    }

    /**
     * Esegue l'azione da compiere in caso di non corretta formattazione dei campi della richiesta.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @exception net.project.errors.AppCrash.
     */
    @Override
    protected void checkFieldFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

    }

    /**
     * Esegue l'operazione post http.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @param userInfo net.project.servlet.security.UserSecurityInfo.
     * @exception net.project.errors.AppCrash.
     * @return void
     */
    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res,
            net.project.servlet.security.UserSecurityInfo userInfo) throws AppCrash {

    }

    /**
     * Esegue l'operazione get http.
     *
     * @param req SsbServletRequest che incapsula la richiesta al servlet.
     * @param res SsbServletResponse che incapsula la risposta dal servlet.
     * @param userInfo net.project.servlet.security.UserSecurityInfo.
     * @exception net.project.errors.AppCrash.
     * @return void
     */
    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        elabora(req, res, userInfo);
    }
}
