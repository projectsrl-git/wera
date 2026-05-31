/*
  XmlReadWrite.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 10/7/2003

  Autore: Luca M.
 
  Note:

  Modifiche: 	Aggiunta scrittura di attributi e sottotag.
  				(1/3/2004 - Luca M.)

 */

package net.project.mess.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.mess.MsgWriter_itf;
import net.project.misc.Config;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Lettore e scrittore di messaggi xml. Per il funzionamento della presente classe è necessario indicare nel file di
 * configurazione il path di un file contenente un template xml. E' possibile valorizzare i soli campi scalari.
 * Proprieta' lette dal file di configurazione (#messageType# deve intendersi pari al secondo parametro passato nel
 * costruttore).
 * <p>
 * XmlReadWrite.#messageType#.TemplateFilePath
 * </p>
 * (OBBLIGATORIA) path del file contenente il template del messaggio xml
 * <p>
 * XmlReadWrite.TemplateDirectoryPath
 * </p>
 * (FACOLTATIVA) path della directory contenente i template di tutti i messaggi xml; di default i template sono cercati
 * nella directory corrente.
 * 
 */
public class XmlReadWrite implements MsgWriter_itf {

    private String    _configName  = null;
    private String    _messageType = null;
    private Hashtable _fields      = null;
    private Template  _template    = null;

    /**
     * Costruttore presente solo per permettere la serializzazione/deserializzazione di oggetti di questa classe. <b>Non
     * deve essere utilizzato</b>
     */
    public XmlReadWrite() {

    }

    /**
     * Costruttore.
     * 
     * @param configName java.lang.String Nome della configurazione. NON PUO' ESSERE NULL.
     * @param type java.lang.String Tipo del messaggio. NON PUO' ESSERE NULL, ne' stringa vuota.
     * @exception net.project.errors.AppCrash Se il parametro configName e' null, se il parametro type e' null o stringa
     *                vuota, oppure in caso di errori nella costruzione del messaggio, o di accesso al file contenente
     *                il template xml.
     */
    public XmlReadWrite(String configName, String messageType) throws AppCrash {

        ErrDetector.GetInstance()
                .preCond(configName != null, "il primo parametro del costruttore non puo' essere null");
        ErrDetector.GetInstance().preCond(messageType != null,
                "il secondo parametro del costruttore non puo' essere null");

        String templateFilePathProperty = "XmlReadWrite." + messageType + ".TemplateFilePath";
        String templateFilePath = Config.GetInstance(configName).getProperty(templateFilePathProperty);
        ErrDetector.GetInstance().preCond(isNotEmpty(templateFilePath),
                "Proprieta' " + templateFilePathProperty + " non valorizzata" + " sul file di configurazione");
        String templateDirectoryPath = Config.GetInstance(configName).getProperty("XmlReadWrite.TemplateDirectoryPath",
                "./");
        String templateFileFullPath = templateDirectoryPath + templateFilePath;
        _template = XmlTemplateFactory.GetInstance().getTemplate(templateFileFullPath);
        _configName = configName;
        _messageType = messageType;
        _fields = new Hashtable();

    }

    /**
     * Costruttore.
     * 
     * @param type java.lang.String Tipo del messaggio. NON PUO' ESSERE NULL, ne' stringa vuota.
     * @exception net.project.errors.AppCrash Se il parametro configName e' null, se il parametro type e' null o stringa
     *                vuota, oppure in caso di errori nella costruzione del messaggio, o di accesso al file contenente
     *                il template xml.
     */
    public XmlReadWrite(String messageType) throws AppCrash {

        this("", messageType);
    }

    private boolean isNotEmpty(String what) {

        return ((what != null) && (what.trim().length() > 0));
    }

    /**
     * Imposta il valore di un tag del messaggio
     *
     * @param name java.lang.String Il nome del tag. Non può essere NULL.
     * @param value java.lang.String Il valore del tag. Non può essere NULL.
     * @exception net.project.errors.AppCrash Se uno dei due parametri in ingresso è NULL.
     */
    @Override
    public void setField(String name, String value) throws AppCrash {

        ErrDetector.GetInstance().preCond(name != null,
                "il primo parametro di setField(String, String) non puo' essere null!");
        ErrDetector.GetInstance().preCond(value != null,
                "il secondo parametro di setField(String, String) non puo' essere null!");

        _fields.put(name, value);

    }

    /**
     * Recupera il messaggio che e' stato composto.
     *
     * @return byte[] Byte array contenente il messaggio.
     * @exception net.project.errors.AppCrash In caso di errori nel processing del template.
     */
    @Override
    public byte[] getMessage() throws AppCrash {

        ByteArrayOutputStream message = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(message);
        try {
            _template.process(new SimpleHash(_fields), writer);
            writer.flush();
            return message.toByteArray();

        } catch (IOException ioe) {
            AppCrash ac = new AppCrash(ioe);
            ac.logContext("XmlReadWrite", toString());
            throw ac;
        } catch (TemplateException te) {
            AppCrash ac = new AppCrash(te);
            ac.logContext("XmlReadWrite", toString());
            throw ac;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } finally {
                    if (message != null) {
                        try {
                            message.close();
                        } catch (IOException ioe) {
                            new AppCrash(ioe);
                        }
                    }
                }
            }
        }

    }

    /**
     * Recupera il valore di un tag dal messaggio. Se il tag non e' presente nel messaggio viene una ritornata stringa
     * vuota
     *
     * @param name java.lang.String Il nome del tag richiesto. Non può essere NULL.
     * @return java.lang.String Il valore del tag richiesto.
     * @exception net.project.errors.AppCrash Se il parametro in ingresso è NULL.
     */
    @Override
    public String getField(String name) throws AppCrash {

        ErrDetector.GetInstance().preCond(name != null, "il parametro di getField(String) non puo' essere null!");

        return (String) _fields.get(name);

    }

    /**
     * Restituisce il valore della proprieta' <b>tipo</b> del messaggio applicativo
     *
     * @return java.lang.String tipo del messaggio
     */
    @Override
    public String getType() {

        return null;
    }

    @Override
    public String toString() {

        StringBuffer description = new StringBuffer();
        description.append("_fields = ");
        description.append(_fields.toString());
        description.append("; _messageType: -");
        description.append(_messageType);
        description.append("-; _configName: -");
        description.append(_configName);
        description.append("-");
        return description.toString();

    }

}
