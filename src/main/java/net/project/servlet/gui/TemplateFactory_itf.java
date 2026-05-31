/*
  TemplateFactory_itf.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione:

  Autore: Pietro G. e Luca M.

  Note:

  Modifiche:

 */

package net.project.servlet.gui;

import net.project.errors.AppCrash;
import freemarker.template.Template;

/**
 * Interfaccia che definisce le funzionalità di una TemplateFactory.
 */

public interface TemplateFactory_itf {

    public Template getTemplate(String name) throws AppCrash;

}
