
package net.project.mess.verify;

/*
 Vocabulary.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 17/05/2001

 Autore: Assunta Ciervo

 Note:

 Modifiche:

 */

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.mess.verify.datatype.FieldVerifier_itf;
import net.project.mess.verify.datatype.XMLField_itf;
import net.project.misc.Config;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Il vocabolario e' un insieme di verificatori di campi. Ogni specifica logica di messaggio e' associata ad un
 * vocabolario che contiene la definizione tutti i suoi campi. Il vocabolario e' riconosciuto tramite il nome del file
 * XML che lo contiene. I vocabolari sono tutti inseriti nella directory letta dalla prprieta'
 * <code> verifier.vocabDir </code>
 */
public class Vocabulary {

    private String    _vocabolaryName;
    private Hashtable _fields = null;

    /**
     * Vocabulary constructor.
     *
     * @param vocabularyName String nome del vocabolario da creare
     */
    public Vocabulary(String vocabularyName) throws AppCrash {

        super();

        _vocabolaryName = vocabularyName;
    }

    /**
     * Recupera l'istanza dell'interfaccia FieldVerify_itf associata al campo passato.
     *
     * @param name String nome del campo
     * @return FieldVerifier_itf verificatore del campo o null se campo non presente
     */
    public FieldVerifier_itf getFieldVerifier(String name) throws AppCrash {

        // Name è il nome del campo da recuperare nel file xml
        // Dal tipo recupero il FieldVerify
        FieldVerifier_itf field = (FieldVerifier_itf) _fields.get(name);

        return field;

    }

    /*
     * Parser XML documento
     * 
     * @return Document
     */
    private Document parserDocument() throws AppCrash {

        Document document = null;

        String dirFilename = Config.GetInstance().getProperty("verifier.vocabDir");
        ErrDetector.GetInstance().param(dirFilename);

        // Path relativo cerco di aggiungere in testa la root della applicazione
        dirFilename = Config.GetInstance().makeAbsolutePath(dirFilename);

        // Recupero file XML che rappresenta il vocabolario
        String filename = dirFilename + _vocabolaryName;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new File(filename));
        } catch (SAXException sxe) {
            // Errore generato durante il parsing
            Exception x = sxe;
            if (sxe.getException() != null) x = sxe.getException();
            AppCrash ap = new AppCrash(x);
            ap.logContext("Vocabulary", "Errore durante il parsing del vocabolario XML");
            throw ap;
        } catch (ParserConfigurationException pce) {
            // ParserConfigurationException
            AppCrash ap = new AppCrash(pce);
            ap.logContext("Vocabulary", "ParserConfigurationException del vocabolario XML");
            throw ap;
        } catch (IOException ioe) {
            // I/O error
            AppCrash ap = new AppCrash(ioe);
            ap.logContext("Vocabulary", "I/O error del vocabolario XML");
            throw ap;
        }
        return document;
    }

    /*
     * Read XML File
     * 
     * @return void
     */
    public void readDocument() throws AppCrash {

        try {
            // Parser XML File
            Document document = parserDocument();

            // Get Root Node
            Node root = document.getDocumentElement();
            NodeList nodeList = root.getChildNodes();

            int len = nodeList.getLength();

            _fields = new Hashtable(len);

            for (int i = 0; i < len; i++) {

                String fieldName = "";
                // Recupero NODO che rappresenta un campo del vocabolario
                Node node = nodeList.item(i);

                if (Node.ELEMENT_NODE != node.getNodeType()) continue;
                // Costruisco oggetto FieldVocabulary

                NodeList nodeListParam = node.getChildNodes();

                int lenParam = nodeListParam.getLength();

                Hashtable params = new Hashtable(lenParam);

                // Recupero NODI che rappresentano i parametri del campo del vocabolario che si sta analizzando
                for (int i2 = 0; i2 < lenParam; i2++) {
                    Node parm = nodeListParam.item(i2);
                    if (Node.ELEMENT_NODE != parm.getNodeType()) continue;
                    NodeList nodeFinal = parm.getChildNodes();
                    if (((Element) parm).getNodeName().equals(XMLField_itf.NAME_NODE)) {
                        // Recupera il nome del campo da verificare
                        fieldName = nodeFinal.item(0).getNodeValue().trim();
                        continue;
                    }
                    params.put(((Element) parm).getNodeName(), nodeFinal.item(0).getNodeValue().trim());
                }
                // Istanzio FieldVerify del campo del vocabolario che si sta analizzando
                FieldVerifier_itf verify = createFieldVerifier(params, fieldName);

                // Put campo nella Hashtable
                fillFieldHashtable(fieldName, verify);
            }
        } catch (Throwable e) {
            AppCrash ap = new AppCrash(e);
            ap.logContext("Vocabulary", "Lettura documento XML");
        }
    }

    /**
     * Questo metodo istanzia il FieldVerify_itf per il controllo del campo passato.
     *
     * @param param Hashtable Tabella dei parametri del field del vocabolario
     * @param name String Nome del field del vocabolario
     * @return FieldVerifier_itf
     */
    private FieldVerifier_itf createFieldVerifier(Hashtable param, String name) throws AppCrash {

        try {
            String classType = (String) param.get(XMLField_itf.TYPE_NODE);
            if (classType == null) {
                return null; // Funzione inesistente
            }
            // istanzio la classe per il controllo del campo
            Class tempClasse = Class.forName(classType);
            Constructor procBaseConstr = tempClasse.getConstructor(new Class[] { Hashtable.class, String.class });
            // Istanzio la classe tramite il costruttore ottenuto
            FieldVerifier_itf verify = (FieldVerifier_itf) procBaseConstr.newInstance(new Object[] { param, name });
            return verify;
        } catch (ClassNotFoundException cnfe) {
            AppCrash ac = new AppCrash(cnfe);
            ac.logContext("Vocabulary", toString());
            throw ac;
        } catch (NoSuchMethodException nsme) {
            AppCrash ac = new AppCrash(nsme);
            ac.logContext("Vocabulary", toString());
            throw ac;
        } catch (InstantiationException ie) {
            AppCrash ac = new AppCrash(ie);
            ac.logContext("Vocabulary", toString());
            throw ac;
        } catch (InvocationTargetException ite) {
            AppCrash ac = new AppCrash(ite);
            ac.logContext("Vocabulary", toString());
            throw ac;
        } catch (IllegalAccessException iae) {
            AppCrash ac = new AppCrash(iae);
            ac.logContext("Vocabulary", toString());
            throw ac;
        }

    }

    /**
     * Fill hashtable del vocabolario, contiene i suoi FieldVerifier_itf
     * 
     * @param name String nome field
     * @param field FieldVerifier_itf FieldVerifier_itf che contiene tutte le informazioni necessarie per la verifica
     *            del campo
     * @return void
     */
    protected void fillFieldHashtable(String name, FieldVerifier_itf field) {

        _fields.put(name, field);
    }
}
