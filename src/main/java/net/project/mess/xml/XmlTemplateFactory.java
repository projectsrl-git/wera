/*
  XmlTemplateFactory.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 25/2/2004

  Autore: Luca M.

  Note:

  Modifiche:

 */

package net.project.mess.xml;

import java.io.File;
import java.io.IOException;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Factory di template per la scrittura di xml. E' un singleton.
 * <p>
 * Proprieta' lette dal file di configurazione:
 * <p>
 * XmlTemplateFactory.DirectoryForTemplateLoading: (OBBLIGATORIA) directory nella quale caricare i template; se tale
 * proprieta' non e' valorizzata, o se il valore di tale proprieta' non rappresenta correttamente il nome di una
 * directory valida, IL COSTRUTTORE LANCIA UN'ECCEZIONE.
 * <p>
 * XmlTemplateFactory.TemplateUpdateDelay: (FACOLTATIVA) numero di secondi che si vogliono far intercorrere tra due
 * verifiche consecutive della presenza di una nuova versione dei template da caricare; se il valore di tale proprieta'
 * non rappresenta correttamente un numero intero, essa e' ignorata
 * <p>
 * XmlTemplateFactory.StricSyntaxMode: (FACOLTATIVA) variabile booleana che indica se si desidera che ogni direttiva
 * freemarker dev'essere indicata premettendo il carattere '#' se si vuole che sia riconosciuta come tale, e non come
 * testo; gli unici valori accettabili sono true e false (NON E' CASE SENSITIVE); ogni altro valore e' ignorato
 * <p>
 * XmlTemplateFactory.ClassicCompatible: (FACOLTATIVA) variabile booleana che indica se si desidera che Freemarker sia
 * eseguito in modo 'classic compatible' (v. documentazione Freemarker); gli unici valori accettabili sono true e false
 * (NON E' CASE SENSITIVE); ogni altro valore e' ignorato
 * <p>
 * XmlTemplateFactory.NumberFormat: (FACOLTATIVA) stringa descrivente il NumberFormat per convertire i numeri in
 * stringhe (v. documentazione Freemarker).
 */
public class XmlTemplateFactory {

    // Costanti indicanti i valori booleani
    public static final String        TRUE              = "TRUE";
    public static final String        FALSE             = "FALSE";

    private static XmlTemplateFactory _Instance         = null;

    private Configuration             _freemarkerConfig = null;

    // Costruttore privato.
    private XmlTemplateFactory() throws AppCrash {

        _freemarkerConfig = new Configuration();

        String directoryForTemplateLoading = readProperty("XmlTemplateFactory.DirectoryForTemplateLoading");
        ErrDetector.GetInstance().preCond(isNotEmpty(directoryForTemplateLoading),
                "XmlTemplateFactory.DirectoryForTemplateLoading: -" + directoryForTemplateLoading + "-");
        try {
            _freemarkerConfig.setDirectoryForTemplateLoading(new File(directoryForTemplateLoading));
        } catch (IOException ioe) {
            AppCrash ac = new AppCrash(ioe);
            ac.logContext("XmlTemplateFactory", "XmlTemplateFactory.DirectoryForTemplateLoading: -"
                    + directoryForTemplateLoading + "-");
            throw ac;
        }

        String templateUpdateDelay = readProperty("XmlTemplateFactory.TemplateUpdateDelay");
        if (isInteger(templateUpdateDelay)) {
            _freemarkerConfig.setTemplateUpdateDelay((Integer.parseInt(templateUpdateDelay)));
        }

        String stricSyntaxMode = readProperty("XmlTemplateFactory.StricSyntaxMode");
        if (isBoolean(stricSyntaxMode)) {
            _freemarkerConfig.setStrictSyntaxMode(Boolean.valueOf(stricSyntaxMode).booleanValue());
        }

        String classicCompatible = readProperty("XmlTemplateFactory.ClassicCompatible");
        if (isBoolean(classicCompatible)) {
            _freemarkerConfig.setClassicCompatible(Boolean.valueOf(classicCompatible).booleanValue());
        }

        String numberFormat = readProperty("XmlTemplateFactory.NumberFormat");
        if (isNotEmpty(numberFormat)) {
            _freemarkerConfig.setNumberFormat(numberFormat);
        }

    }

    /**
     * Metodo di accesso al singleton.
     * 
     * @return net.project.mess.xml.XmlTemplateFactory Il singleton.
     * @exception net.project.errors.AppCrash In caso di errori al momento della costruzione del singleton.
     */
    public static XmlTemplateFactory GetInstance() throws AppCrash {

        if (_Instance == null) {
            synchronized (XmlTemplateFactory.class) {
                if (_Instance == null) {
                    _Instance = new XmlTemplateFactory();
                }
            }
        }
        return _Instance;

    }

    private String readProperty(String propertyName) {

        return Config.GetInstance().getProperty(propertyName);
    }

    private boolean isNotEmpty(String what) {

        return ((what != null) && (what.trim().length() > 0));
    }

    private boolean isInteger(String what) {

        if (isNotEmpty(what)) {
            try {
                Integer.parseInt(what);
                return true;
            } catch (NumberFormatException nfe) {
                return false;
            }
        } else {
            return false;
        }

    }

    private boolean isBoolean(String what) {

        return (TRUE.equalsIgnoreCase(what) || FALSE.equalsIgnoreCase(what));

    }

    /**
     * Costruisce un template.
     * 
     * @param name java.lang.String Il nome del template.
     * @return freemarker.template.Template Il template costruito.
     * @exception net.project.errors.AppCrash In caso di errori nella costruzione del template.
     */
    public Template getTemplate(String name) throws AppCrash {

        ErrDetector.GetInstance().preCond(isNotEmpty(name),
                "argomento di getTemplate(String) non accettabile: -" + name + "-");

        try {
            Template template = _freemarkerConfig.getTemplate(name);
            ErrDetector.GetInstance().postCond(template != null,
                    "errore nell'istanziazione del template: -" + name + "-");
            return template;

        } catch (IOException ioe) {
            AppCrash ac = new AppCrash(ioe);
            ac.logContext("XmlTemplateFactory", "template name: -" + name + "-");
            throw ac;
        }

    }

}
