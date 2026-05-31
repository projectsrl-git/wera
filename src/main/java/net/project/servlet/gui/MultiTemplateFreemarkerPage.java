/*
 Copyright (c) by SSB Spa Societa' per i Servizi Bancari

 Note:

 */

package net.project.servlet.gui;

import java.io.PrintWriter;

import net.project.errors.AppCrash;
import freemarker.template.Template;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Questa classe permette di utilizzare per la stessa pagina un template diverso, personalizzato runtime aggiungendo un
 * prefisso al nome del template. Il prefisso e' tratto dalla proprieta' del modelroot
 * .MultiTemplateFreemarkerPage.prefix
 */
public class MultiTemplateFreemarkerPage extends FreeMarkerPage {

    private String _templateName = null;
    private String _prefix       = null;

    /**
     * Costruttore.
     * 
     * @param java.lang.String name nome della pagina
     * @param java.lang.String cfName nome della configurazione
     * @exception net.project.errors.AppCrash
     */
    public MultiTemplateFreemarkerPage(String name, String cfName) throws AppCrash {

        super(name, cfName);
    }

    /**
     * Questo metodo mostra la pagina. Prima della visualizzazione crea il template da utilizzare richiamando la
     * TemplateFactory. Il nome del template utilizzato e' tratto dal normale nome del template dedotto dalla
     * configurazione piu' un prefisso tratto dalla proprieta' del modelroot .MultiTemplateFreemarkerPage.prefix
     * 
     * @param pw
     * @throws AppCrash
     * 
     * @see net.project.servlet.gui.FreeMarkerPage#display(java.io.PrintWriter)
     */
    @Override
    public void display(PrintWriter pw) throws AppCrash {

        try {
            String tempName = getTemplateName();

            Template template = TemplateFactory.GetInstance(getConfigName()).getTemplate(tempName);
            setTemplate(template);

            super.display(pw);
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("MultiTemplateFreemarkerPage", "Template: " + _templateName + " Prefix: " + _prefix);
            ac.logContext("MultiTemplateFreemarkerPage", "FM: " + this.toString());
            throw ac;
        }
    }

    /**
     * Questo metodo ritorna il nome del template da utilizzare per creare la pagina
     *
     * @return
     * @throws AppCrash
     *
     * @see net.project.servlet.gui.FreeMarkerPage#getTemplateName()
     */
    @Override
    protected String getTemplateName() throws AppCrash {

        try {
            String _templateName = super.getTemplateName();
            TemplateModel prefix = getModelRoot().get(".MultiTemplateFreemarkerPage.prefix");
            String _prefix = "";
            if (prefix != null) {
                _prefix = prefix.toString();
            }

            return _prefix + _templateName;
        } catch (TemplateModelException e) {
            AppCrash ac = new AppCrash(e);
            throw ac;
        }

    }

}
