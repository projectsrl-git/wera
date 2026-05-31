/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.servlet.gui;

import java.util.Map;

import javax.servlet.GenericServlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.project.errors.AppCrash;
import freemarker.ext.jsp.TaglibFactory;
import freemarker.ext.servlet.FreemarkerServlet;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.ext.servlet.HttpRequestParametersHashModel;
import freemarker.ext.servlet.HttpSessionHashModel;
import freemarker.ext.servlet.ServletContextHashModel;
import freemarker.template.ObjectWrapper;

/**
 * Questa classe e' un decorator per Page_itf da utilizzarsi in servlet application per aggiungere il supporto
 * all'utilizzo di custom tags JSP nel template. Vengono aggiunti nella Map passata al template le seguenti chiavi:
 * <p>
 * Request contiene l'oggetto request
 * <p>
 * Session contiene l'oggetto session
 * <p>
 * Response contiene l'oggetto response
 * <p>
 * RequestParameters contiene i parametri della request
 * <p>
 * Application contiene il servletcontext
 * <p>
 * JspTaglibs contiene l'oggetto che permette l'utilizzo dei tag JSP
 * <p>
 * <p>
 * Dichiara ed usa una tag lib
 * <p>
 * <#assign dt=JspTaglibs["/WEB-INF/taglibs-datetime.tld"]>
 * <p>
 * <@dt.currentTime/>
 * <p>
 * <p>
 */
public class JspPageDecorator extends PageDecorator {

    // Note these names start with dot, so they're essentially invisible from
    // a freemarker script.
    private static final String ATTR_APPLICATION_MODEL = ".freemarker.Application";

    private static final String ATTR_JSP_TAGLIBS_MODEL = ".freemarker.JspTaglibs";

    /**
     * Constructor necessario per l'istanziazione dinamica
     * 
     * @param page
     */
    public JspPageDecorator(Page_itf page) {

        super(page);
    }

    /**
     * Questo metodo aggiunge alla map che verra' utilizzata dal template il supporto per JSPtag libs
     * 
     * @param paramHash
     * @throws AppCrash
     * 
     * @see net.project.servlet.gui.PageDecorator#setPageRootData(java.util.Map)
     */
    @Override
    public void setPageRootData(Map paramHash) throws AppCrash {

        ServletContext sc = (ServletContext) paramHash.get(".ServletApplication_base.SSBContext");
        HttpServletRequest req = (HttpServletRequest) paramHash.get(".ServletApplication_base.SSBRequest");
        HttpServletResponse res = (HttpServletResponse) paramHash.get(".ServletApplication_base.SSBResponse");
        GenericServlet servlet = (GenericServlet) paramHash.get(".ServletApplication_base.SSBServlet");

        addJSPSupport(paramHash, sc, req, res, servlet);
        super.setPageRootData(paramHash);
    }

    protected void addJSPSupport(Map params, ServletContext servletContext, HttpServletRequest request,
            HttpServletResponse response, GenericServlet serv) throws AppCrash {

        ObjectWrapper wrapper = ObjectWrapper.DEFAULT_WRAPPER;

        try {

            // Create hash model wrapper for servlet context (the application)
            ServletContextHashModel servletContextModel = (ServletContextHashModel) servletContext
                    .getAttribute(ATTR_APPLICATION_MODEL);
            if (servletContextModel == null) {
                servletContextModel = new ServletContextHashModel(serv, wrapper);
                servletContext.setAttribute(ATTR_APPLICATION_MODEL, servletContextModel);
                TaglibFactory taglibs = new TaglibFactory(servletContext);
                servletContext.setAttribute(ATTR_JSP_TAGLIBS_MODEL, taglibs);
            }
            params.put(FreemarkerServlet.KEY_APPLICATION, servletContextModel);
            params.put(FreemarkerServlet.KEY_APPLICATION_PRIVATE, servletContextModel);
            params.put(FreemarkerServlet.KEY_JSP_TAGLIBS, servletContext.getAttribute(ATTR_JSP_TAGLIBS_MODEL));

            // Create hash model wrapper for session
            HttpSessionHashModel sessionModel;
            HttpSession session = request.getSession(false);
            if (session != null) {
                sessionModel = new HttpSessionHashModel(session, wrapper);
            } else {
                sessionModel = new HttpSessionHashModel(null, request, response, wrapper);
            }
            params.put(FreemarkerServlet.KEY_SESSION, sessionModel);

            // Create hash model wrapper for request
            HttpRequestHashModel requestModel = new HttpRequestHashModel(request, response, wrapper);
            params.put(FreemarkerServlet.KEY_REQUEST, requestModel);
            params.put(FreemarkerServlet.KEY_REQUEST_PRIVATE, requestModel);

            // Create hash model wrapper for request parameters
            HttpRequestParametersHashModel requestParametersModel = new HttpRequestParametersHashModel(request);

            params.put(FreemarkerServlet.KEY_REQUEST_PARAMETERS, requestParametersModel);

        } catch (Throwable e) {
            throw new AppCrash(e);
        }
    }

}
