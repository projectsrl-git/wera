/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

/*
 * Created on 22-set-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

package net.project.dataset.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.ErrDetector_itf;
import net.project.mess.MsgFactory_base;
import net.project.mess.MsgReader_itf;
import net.project.mess.MsgWriter_itf;
import net.project.misc.Config;
import net.project.misc.FileParser;

import org.xml.sax.InputSource;

/**
 * Questa classe e' un generico traduttore di file da formato XML a formato SETIF. La traduzione e' basata su un file di
 * descrizione espresso in XML. La conversione fa in modo che un determinato tipo di element del file XML con i suoi
 * attributi e sotto elementi venga trasformato in una disposizione del file SETIF con i corrispondenti record. Es:
 * <p>
 * Il file di descrizione rappresenta la disposizione SETIF e dove pescare nel record XML il valore da assegnare ai vari
 * campi:
 * 
 * <?xml version="1.0" encoding="ISO-8859-1" ?> <traduzione recordelement = "Autorizzazione" > <record tipo="12"
 * elementchiave="TipoPag"> <campo setif = "12c1" xml="$Tautor"/> <campo setif = "12c2" xml="$MAC"/> <campo setif =
 * "12c3" xml="$doc:pop"/> <campo setif = "12c4" xml="12928"/> </record> <record tipo="61" elementchiave="Circuito">
 * <campo setif = "61c1" xml="$Circuito:bt"/> <campo setif = "61c2" xml="$Circuito"/> <campo setif = "61c3"
 * xml="!CampoProva"/> </record> <record tipo="30" elementchiave="NumOrdine"> <campo setif = "30c1" xml="$NumOrdine"/>
 * </record> <record tipo="70" elementchiave="ImportoContab"> <campo setif = "70c1" xml="$:at1"/> <campo setif = "70c2"
 * xml="$:at2"/> <campo setif = "70c3" xml="$doc:att"/> <campo setif = "70c4" xml="$doc:caca"/> </record> </traduzione>
 *
 * @author zorzetti
 */
public abstract class TraduttoreXml2Setif {

    protected static final byte[] NEW_LINE                 = "\n".getBytes();

    // Nomi di alcuni campi del setif
    protected static final String TIPO_RECORD              = "TIPO_RECORD";
    protected static final String NUMERO_DISPOSIZIONE      = "NUMERO_DISPOSIZIONE";
    protected static final String DATA_CREAZIONE           = "DATA_CREAZIONE";
    protected static final String CAUSALE                  = "CAUSALE";
    protected static final String ORDINANTE                = "ORDINANTE";
    protected static final String DESTINATARIO             = "DESTINATARIO";
    protected static final String CODICE_DIVISA_OPERAZIONE = "CODICE_DIVISA_OPERAZIONE";
    protected static final String CODICE_RIFERIMENTO       = "CODICE_RIFERIMENTO";
    protected static final String CANALE                   = "CANALE";

    // Possibile valore dell'attributo 'code' del tag 'Error'
    // dell'xml da tradurre
    protected static final String CODE_999                 = "999";

    private Hashtable             _regole;
    private String                _fileElencoNomiRegole;
    private String                _fileDaTradurre;
    private XMLRecordParser       _recordsXML;
    private int                   _numeroDiDisposizioniSetif;
    private int                   _numeroDiRecordSetif;
    private FileOutputStream      _output;
    private ErrDetector_itf       _ed;

    // Hashtable destinata a contenere un MsgReader per ogni PassThru:
    // il suo scopo e' evitare di istanziare piu' MsgReader per lo stesso PassThru
    private Hashtable             _passThruMsgReaders;

    /**
     * Crea un nuovo oggetto TraduttoreXml2Setif .
     * 
     * @param fileElencoNomiRegole java.lang.String Il path del file contenente i nomi dei file contenenti le regole di
     *            traduzione. NON PUO' ESSERE NULL, NE' STRINGA VUOTA.
     * @param fileDaTradurre java.lang.String Il path del file da tradurre. NON PUO' ESSERE NULL, NE' STRINGA VUOTA.
     * @exception net.project.errors.AppCrash Se i parametri in ingresso non sono formalmente corretti.
     */
    public TraduttoreXml2Setif(String fileElencoNomiRegole, String fileDaTradurre) throws AppCrash {

        _ed = ErrDetector.GetInstance();
        _ed.param(fileElencoNomiRegole);
        _ed.param(fileDaTradurre);

        _fileDaTradurre = fileDaTradurre;
        _fileElencoNomiRegole = fileElencoNomiRegole;
        _regole = new Hashtable();
        _passThruMsgReaders = new Hashtable();

    }

    /**
     * Questo metodo inizializza il traduttore. Deve essere chiamato prima di translate.
     * 
     * @param output java.io.File Il file sul quale scrivere l'output. NON PUO' ESSERE NULL E DEVE INDICARE UN FILE
     *            ACCESSIBILE IN SCRITTURA.
     * @exception net.project.errors.AppCrash Se il parametro in ingresso e' null, o non rappresenta correttamente un
     *                file accessibile in scrittura, o in caso di errori durante l'esecuzione del metodo.
     */
    public void init(File output) throws AppCrash {

        checkFileAsWritable(output);

        try {

            _output = new FileOutputStream(output);
            _ed.param(_output != null, "Errore nella creazione del file setif");

            costruisciRegoleTraduzione();

            // Creo il dataset XML con con il file da tradurre
            HashSet set = new HashSet();
            String recordElement = getAndCheck("RecordElement");
            set.add(recordElement);

            InputStream src = new FileInputStream(_fileDaTradurre);
            InputSource file = new InputSource(src);
            _recordsXML = new XMLRecordParser(set, file);

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("TraduttoreXml2Setif", "File da tradurre:" + _fileDaTradurre + " File elenco nomi regole:"
                    + _fileElencoNomiRegole);
            throw ac;
        }

    }

    protected void checkFileAsWritable(File file) throws AppCrash {

        _ed.preCond(file != null, "Il file di output e' null!");
        _ed.preCond(file.exists(), "Il file di output non esiste!");
        _ed.preCond(file.isFile(), "Il file di output non e' un file valido!");
        _ed.preCond(file.canRead(), "Il file di output non puo' essere letto!");
        _ed.preCond(file.canWrite(), "Il file di output non puo' essere scritto!");

    }

    // Costruisce una hashtable che ha per chiavi i numeri di causale
    // e per valori le regole di traduzione associate a quella causale.
    // Ogni regola di traduzione e' un oggetto Vector.
    protected void costruisciRegoleTraduzione() throws AppCrash {

        final int POSIZIONE_NOME_CAUSALE = 0;
        final int POSIZIONE_NOME_FILE_REGOLA = 1;
        try {
            Vector elencoNomiRegole = FileParser.parse(_fileElencoNomiRegole);

            for (int numeroNomeRegola = 0; numeroNomeRegola < elencoNomiRegole.size(); numeroNomeRegola++) {

                Vector nomeRegolaCorrente = (Vector) elencoNomiRegole.elementAt(numeroNomeRegola);

                // Leggo il file con le regole di traduzione
                // e lo metto nel Vector nomeRegolaCorrente
                String nomeFileRegola = (String) nomeRegolaCorrente.elementAt(POSIZIONE_NOME_FILE_REGOLA);
                InputStream stream = new FileInputStream(nomeFileRegola);
                InputSource regole = new InputSource(stream);
                HashSet recordElementNames = new HashSet();
                recordElementNames.add("record");
                XMLRecordParser parserRegole = new XMLRecordParser(recordElementNames, regole);
                parserRegole.start();

                Map riga;
                Vector regolaCorrente = new Vector();
                while ((riga = parserRegole.parseNext()) != null) {
                    regolaCorrente.add(riga);
                }
                _regole.put(nomeRegolaCorrente.elementAt(POSIZIONE_NOME_CAUSALE), regolaCorrente);

            }

        } catch (FileNotFoundException fnfe) {
            AppCrash ac = new AppCrash(fnfe);
            ac.logContext("TraduttoreXml2Setif", toString());
            throw ac;
        }

    }

    /**
     * Questo metodo esegue la vera e propria traduzione interpretando il file di traduzione ed eseguendo il parsing del
     * file da tradurre.
     *
     * @exception net.project.errors.AppCrash In caso di errori nell'esecuzione del metodo.
     */
    public void translate() throws AppCrash {

        Map row = null;
        Map docAttributeRow = null;

        _ed.invariant(_recordsXML != null, "Traduttore non inizializzato");
        _ed.invariant(_regole.size() > 0, "Regole del traduttore non lette");

        int recordCount = 0;
        try {
            _recordsXML.start();

            while ((row = _recordsXML.parseNext()) != null) {
                recordCount++;

                if (row.get("recordtype").equals("doc")) {
                    docAttributeRow = row;
                    scriviRecordTesta(docAttributeRow);
                } else {
                    updateStatistics(row);
                    Vector regoleTraduzione = trovaRegoleTraduzione(docAttributeRow, row);
                    if (!disposizioneDaIgnorare(docAttributeRow, row)) {
                        scriviDisposizione(docAttributeRow, row, regoleTraduzione);
                    }
                }
            }
            scriviRecordCoda(docAttributeRow, _numeroDiDisposizioniSetif, _numeroDiRecordSetif + 1);

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("TraduttoreXml2Setif", "Record: " + recordCount);
            ac.logContext("TraduttoreXml2Setif", "Row: " + row);
            ac.logContext("TraduttoreXml2Setif", "DocAttribute: " + docAttributeRow);
            ac.logContext("TraduttoreXml2Setif", "Regole: " + _regole);
            ac.logContext("TraduttoreXml2Setif", "File elenco nomi regole: " + _fileElencoNomiRegole);
            ac.logContext("TraduttoreXml2Setif", "File da tradurre: " + _fileDaTradurre);
            throw ac;

        } finally {
            try {
                _recordsXML.stop();

            } finally {
                if (_output != null) {
                    try {
                        _output.flush();
                    } catch (IOException ioe) {
                        throw new AppCrash(ioe);

                    } finally {
                        try {
                            _output.close();
                        } catch (IOException ioe) {
                            throw new AppCrash(ioe);
                        }
                    }

                }
            }

        }

    }

    /**
     * Trova le regole di traduzione per una determinata riga.
     * 
     * @param docAttributeRow java.util.Map Mappa di coppie chiave-valore degli attributi del messaggio xml.
     * @param row java.util.Map La riga della quale si richiedono le regole di traduzione.
     * @return java.util.Vector Le regole di traduzione richieste.
     * @exception net.project.errors.AppCrash In caso di errori nella ricerca delle regole di traduzione.
     *
     */
    protected abstract Vector trovaRegoleTraduzione(Map docAttributeRow, Map row) throws AppCrash;

    /**
     * Dice se una disposizione e' da ignorare. Di default, restituisce false. E' definito protected per permetterne
     * l'override.
     * 
     * @param docAttributeRow java.util.Map Mappa di coppie chiave-valore degli attributi del messaggio xml.
     * @param row java.util.Map La riga nella quale si vuole controllare la presenza del codice errore 999.
     * @return boolean true se la disposizione e' da ignorare, false altrimenti.
     *
     */
    protected boolean disposizioneDaIgnorare(Map docAttributeRow, Map row) throws AppCrash {

        return false;
    }

    /**
     * Non fa nulla.
     *
     * @param docAttributeRow java.util.Map Mappa di coppie chiave-valore degli attributi del messaggio xml.
     * @exception net.project.errors.AppCrash Mai lanciato in questa implementazione.
     */
    protected void scriviRecordTesta(Map docAttributeRow) throws AppCrash {

    }

    /**
     * Questo metodo scrive i record che costituiscono una disposizione del flusso setif.
     *
     * @param docAttributeRow java.util.Map Mappa di coppie chiave-valore degli attributi del messaggio xml.
     * @param row java.util.Map Mappa di coppie chiave-valore degli attributi dei campi del tag corrente del messaggio
     *            xml.
     * @exception net.project.errors.AppCrash In caso di errori nella scrittura.
     */
    protected void scriviDisposizione(Map docAttributeRow, Map row, Vector regoleTraduzione) throws AppCrash {

        // Se il tag che identifica una disposizione contiene dei sottotag,
        // allora la variabile progrSottotag conta i sottotag con lo stesso nome.
        // Esempio:
        // <Disp>
        // <alfa> ... </alfa> --> progrSottotag = 1
        // <alfa> ... </alfa> --> progrSottotag = 2
        // <alfa> ... </alfa> --> progrSottotag = 3
        // <beta> ... </beta> --> progrSottotag = 1
        // <beta> ... </beta> --> progrSottotag = 2
        // </Disp>
        // (dove <Disp> e' il tag che identifica una disposizione)
        int progrSottotag = 0;
        int numeroRegola = 0;
        int numeroCampo = 0;
        Map rigaRegole = null;
        String nomeCampoSetif = null;
        String nomeCampoXml = null;
        String valoreXml = null;
        MsgWriter_itf recordSetif = null;

        try {
            // Scorre tutte le regole di traduzione: una per ogni record SETIF della disposizione
            for (numeroRegola = 0; numeroRegola < regoleTraduzione.size(); numeroRegola++) {
                rigaRegole = (Map) regoleTraduzione.elementAt(numeroRegola);

                String recordType = (String) rigaRegole.get(":tipo");
                String campoChiave = (String) rigaRegole.get(":elementchiave");

                _ed.param(recordType);
                _ed.param(campoChiave);

                // Creo un record SETIF del tipo corrente per ogni element di tipo
                // elementchiave che trovo all'interno del record XML corrente
                progrSottotag = 0;
                while (true) {
                    progrSottotag++;

                    // Se non e' presente l'elemento chiave passo al record SETIF successivo
                    if (elementChiavePresente(row, campoChiave, progrSottotag) == false) {
                        break;
                    }
                    recordSetif = MsgFactory_base.GetInstance().MakeMsgWriter(recordType);

                    int numeroCampi = (rigaRegole.keySet().size() / 2) - 2;

                    // Popolo il record SETIF in base al mapping fornito dalla regola di traduzione corrente
                    for (numeroCampo = 1; numeroCampo <= numeroCampi; numeroCampo++) {
                        nomeCampoSetif = (String) rigaRegole.get("campo" + numeroCampo + ":setif");
                        nomeCampoXml = (String) rigaRegole.get("campo" + numeroCampo + ":xml");

                        valoreXml = ricavaValoreCampoXml(progrSottotag, nomeCampoXml, row, docAttributeRow, campoChiave);

                        if (valoreXml != null) {
                            recordSetif.setField(nomeCampoSetif, valoreXml);
                        }
                    }

                    scriviRecordSetif(recordSetif);
                }

            }
            _numeroDiDisposizioniSetif++;

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("TraduttoreXml2Setif", "Regola in errore: " + numeroRegola + "; Campo: " + numeroCampo);
            ac.logContext("TraduttoreXml2Setif", "Nome setif: -" + nomeCampoSetif + "-; Nome xml: -" + nomeCampoXml
                    + "-; Valore xml: -" + valoreXml + "-");
            throw ac;
        }

    }

    /**
     * Non fa nulla.
     *
     * @param docAttributeRow java.util.Map Mappa di coppie chiave-valore degli attributi del messaggio xml.
     * @param numeroDiDisposizioni int Numero totale di disposizioni nel setif.
     * @param numeroDiRecord int Numero totale di record nel setif, compresi il record di testa ed il record di coda.
     * @exception net.project.errors.AppCrash Mai lanciato in questa implementazione.
     */
    protected void scriviRecordCoda(Map docAttributeRow, int numeroDiDisposizioni, int numeroDiRecord) throws AppCrash {

    }

    /**
     * Questo metodo
     *
     * @param row DOCUMENT ME!
     */
    protected void updateStatistics(Map row) {

    }

    /**
     * Questo metodo scrive un record nel flusso setif, andando a capo prima di scrivere il record.
     * 
     * @param recordSetif net.project.mess.MsgWriter_itf Il record da scrivere.
     * @exception net.project.errors.AppCrash In caso di errori nella scrittura del record.
     */
    protected void scriviRecordSetif(MsgWriter_itf recordSetif) throws AppCrash {

        scriviRecordSetif(recordSetif, true);
    }

    /**
     * Questo metodo scrive un record nel flusso setif.
     * 
     * @param recordSetif net.project.mess.MsgWriter_itf Il record da scrivere.
     * @param addNewLine boolean Se true, va a capo prima di scrivere il record. Se false, non va a capo prima di
     *            scrivere il record.
     * @exception net.project.errors.AppCrash In caso di errori nella scrittura del record.
     */
    protected void scriviRecordSetif(MsgWriter_itf recordSetif, boolean addNewLine) throws AppCrash {

        setTipoRecord(recordSetif);
        byte[] msg = recordSetif.getMessage();
        final byte[] BLANK = " ".getBytes();
        byte[] formattedMsg = new byte[msg.length - 3];
        System.arraycopy(BLANK, 0, formattedMsg, 0, 1);
        System.arraycopy(msg, 4, formattedMsg, 1, formattedMsg.length - 1);
        try {
            if (addNewLine) {
                _output.write(NEW_LINE);
            }
            _output.write(formattedMsg);
            _numeroDiRecordSetif++;

        } catch (IOException ioe) {
            AppCrash ac = new AppCrash(ioe);
            ac.logContext("TraduttoreXML2Setif", "record: -" + new String(msg) + "-");
            throw ac;
        }

    }

    protected void setTipoRecord(MsgWriter_itf recordSetif) throws AppCrash {

        recordSetif.setField(TIPO_RECORD, recordSetif.getType().substring(2));
    }

    protected String ricavaValoreCampoXml(int iter, String nomeCampoXml, Map row, Map docAttributeRow,
            String campoChiave) throws AppCrash {

        String valoreXml = null;

        // Se inizia con ! allora e' un valore da calcolare richiamando il metodo getNOMECAMPO()
        if (nomeCampoXml.startsWith("!")) {
            try {
                Method mthd = this.getClass().getMethod("get" + nomeCampoXml.substring(1),
                        new Class[] { Map.class, Map.class, String.class });
                return (String) mthd.invoke(this, new Object[] { row, docAttributeRow, campoChiave });
            } catch (Throwable e) {
                AppCrash ac = new AppCrash(e);
                ac.logContext("TraduttoreXml2Setif", "Metodo chiamato: " + "get" + nomeCampoXml.substring(1));
                throw ac;
            }
        }

        // Se non inizia con $ allora e' esso stesso il valore costante da assegnare al campo SETIF
        if (nomeCampoXml.startsWith("$") == false) {
            return nomeCampoXml;
        }

        // Indica un campo del PassThru
        if (nomeCampoXml.startsWith("$:PT:")) {
            String passThru = (String) row.get("PassThru1:PassThruBuffer");
            return getPassThruField(passThru, nomeCampoXml.substring(5));
        }

        // Indica un attributo dell'elemento piu' esterno del documento Xml
        if (nomeCampoXml.startsWith("$doc:")) {
            valoreXml = (String) docAttributeRow.get(nomeCampoXml.substring(1));
            return valoreXml;
        }

        // Indica un valore che e' in un sotto sottoelemento
        if (nomeCampoXml.indexOf("/") >= 0) {
            _ed.invariant(false, "Funzione non ancora implementata");
        } else if (nomeCampoXml.indexOf(":") > 1) { // Indica un attributo di un sotto elemento interno all'elemento
                                                    // record
            String attributo = nomeCampoXml.substring(nomeCampoXml.indexOf(":"));
            nomeCampoXml = nomeCampoXml.substring(0, nomeCampoXml.indexOf(":")) + iter + attributo;
        } else if (nomeCampoXml.indexOf(":") < 0) {
            // Indica un sotto elemento dell'elemento record
            nomeCampoXml = nomeCampoXml + iter;
        }

        valoreXml = (String) row.get(nomeCampoXml.substring(1)); // Viene ignorato il $ iniziale

        return valoreXml;

    }

    protected String getPassThruField(Map row, String fieldName) throws AppCrash {

        String passThru = (String) row.get("PassThru1:PassThruBuffer");
        return getPassThruField(passThru, fieldName);

    }

    protected String getPassThruField(String passThru, String fieldName) throws AppCrash {

        _ed.preCond(isNotEmpty(passThru), "PassThru non valido: -" + passThru + "-");
        _ed.preCond(isNotEmpty(fieldName), "Campo del PassThru non valido: -" + fieldName + "-");

        MsgReader_itf passThruReader = (MsgReader_itf) _passThruMsgReaders.get(passThru);
        if (passThruReader == null) {
            passThruReader = MsgFactory_base.GetInstance().MakeMsgReader(passThru.getBytes());
            _passThruMsgReaders.put(passThru, passThruReader);
        }
        return passThruReader.getField(fieldName);

    }

    public boolean isNotEmpty(String what) {

        return ((what != null) && (what.trim().length() > 0));
    }

    /**
     * Questo metodo dice se l'elemento chiave e' presente nella riga.
     * 
     * @param row java.util.Map La riga corrente.
     * @param campoChiave java.lang.String Il nome del campo chiave.
     * @param numeroRecord int Il numero del record corrente.
     */
    public boolean elementChiavePresente(Map row, String campoChiave, int progrSottotag) {

        if (campoChiave.equals("unoperrecord") && (progrSottotag == 1)) {
            return true;
        }
        if (row.containsKey(campoChiave + progrSottotag)) {
            return true;
        }

        // Se non e' presente l'elemento come chiave cerco
        // un attributo dell'elemento chiave cercato.
        Iterator iter = row.keySet().iterator();
        String attrib = campoChiave + progrSottotag + ":";
        while (iter.hasNext()) {
            String key = (String) iter.next();
            if (key.startsWith(attrib)) {
                return true;
            }
        }
        return false;

    }

    /**
     * Recupera una proprieta' dal file di configurazione controllando che sia non vuota.
     * 
     * @param propertyName java.lang.String Il nome della proprieta'. NON PUO' ESSERE NULL, NE' STRINGA VUOTA, NE'
     *            STRINGA DI SOLI BLANK.
     * @return java.lang.String Il valore della proprieta'. NON E' MAI NULL, NE' NE' STRINGA VUOTA, NE' STRINGA DI SOLI
     *         BLANK.
     * @exception net.project.errors.AppCrash Se il parametro in ingresso non e' formalmente corretto, o se la
     *                proprieta' non e' valorizzata nel file di configurazione.
     */
    protected String getAndCheck(String propertyName) throws AppCrash {

        _ed.preCond(isNotEmpty(propertyName), "il NOME_UTENTE della proprieta' richiesta non e' valido: -" + propertyName
                + "-");
        String propertyValue = Config.GetInstance().getProperty(propertyName);
        _ed.postCond(isNotEmpty(propertyValue), "proprieta' " + propertyName
                + " non valorizzata nel file di configurazione");
        return propertyValue;

    }

    protected Hashtable getRegole() {

        return (Hashtable) _regole.clone();
    }

    protected String getFileElencoNomiRegole() {

        return _fileElencoNomiRegole;
    }

    protected String getFileDaTradurre() {

        return _fileDaTradurre;
    }

    protected XMLRecordParser getRecordsXML() {

        return _recordsXML;
    }

    protected int getNumeroDiDisposizioniSetif() {

        return _numeroDiDisposizioniSetif;
    }

    public int getNumeroDiRecordSetif() {

        return _numeroDiRecordSetif;
    }

    protected FileOutputStream getOutput() {

        return _output;
    }

    protected ErrDetector_itf getErrDetector() {

        return _ed;
    }

    /**
     * Setter da utilizzare in caso di override di metodi che aggiornano l'attributo _numeroDiDisposizioniSetif.
     * 
     * @param numeroDiDisposizioniSetif int Il nuovo valore che si desidera assegnare all'attributo
     *            _numeroDiDisposizioniSetif.
     */
    protected void setNumeroDiDisposizioniSetif(int numeroDiDisposizioniSetif) {

        _numeroDiDisposizioniSetif = numeroDiDisposizioniSetif;
    }

    /**
     * Setter da utilizzare in caso di override di metodi che aggiornano l'attributo _numeroDiRecordSetif.
     * 
     * @param numeroDiRecordSetif int Il nuovo valore che si desidera assegnare all'attributo _numeroDiRecordSetif.
     */
    protected void setNumeroDiRecordSetif(int numeroDiRecordSetif) {

        _numeroDiRecordSetif = numeroDiRecordSetif;
    }

    @Override
    public String toString() {

        StringBuffer description = new StringBuffer();
        description.append("_fileElencoNomiRegole: -");
        description.append(_fileElencoNomiRegole);
        description.append("-; _fileDaTradurre: -");
        description.append(_fileDaTradurre);
        description.append("-; _numeroDiDisposizioniSetif = ");
        description.append(_numeroDiDisposizioniSetif);
        description.append("; _numeroDiRecordSetif = ");
        description.append(_numeroDiRecordSetif);
        return description.toString();

    }

}