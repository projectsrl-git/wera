/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.servlet.gui;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.project.errors.AppCrash;
import freemarker.ext.servlet.FreemarkerServlet;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.ext.servlet.HttpRequestParametersHashModel;
import freemarker.ext.servlet.HttpSessionHashModel;
import freemarker.template.ObjectWrapper;

/**
 * Questa classe e' un decorator per Page_itf da utilizzarsi in una servlet application con FreeMarker per aggiungere
 * automaticamente alla Map del tamplate gli oggetti HttpRequest e session
 */
public class HTTPObjsPageDecorator extends PageDecorator {

    /**
     * Costruttore necessario per l'instanziazione dinamica
     * 
     * @param page
     */
    public HTTPObjsPageDecorator(Page_itf page) {

        super(page);

    }

    /**
     * Questo metodo aggiunge alla Map della pagina gli oggetti Request e Session
     * 
     * @param paramHash
     * @throws AppCrash
     * 
     * @see net.project.servlet.gui.PageDecorator#setPageRootData(java.util.Map)
     */
    @Override
    public void setPageRootData(Map paramHash) throws AppCrash {

        HttpServletRequest req = (HttpServletRequest) paramHash.get(".ServletApplication_base.SSBRequest");

        ObjectWrapper wrapper = ObjectWrapper.DEFAULT_WRAPPER;

        try {
            // Create hash model wrapper for session
            HttpSessionHashModel sessionModel;
            HttpSession session = req.getSession(false);
            if (session != null) {
                sessionModel = new HttpSessionHashModel(session, wrapper);
            } else {
                sessionModel = new HttpSessionHashModel(null, req, null, wrapper);
            }
            paramHash.put(FreemarkerServlet.KEY_SESSION, sessionModel);

            // Create hash model wrapper for request
            HttpRequestHashModel requestModel = new HttpRequestHashModel(req, null, wrapper);
            paramHash.put(FreemarkerServlet.KEY_REQUEST, requestModel);

            // Create hash model wrapper for request parameters
            HttpRequestParametersHashModel requestParametersModel = new HttpRequestParametersHashModel(req);
            paramHash.put(FreemarkerServlet.KEY_REQUEST_PARAMETERS, requestParametersModel);

            super.setPageRootData(paramHash);
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            throw ac;
        }
    }

}
