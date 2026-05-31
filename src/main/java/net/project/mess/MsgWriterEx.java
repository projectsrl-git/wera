/*
  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore: Simone Z.

  Note:


 */

package net.project.mess;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;

/**
 * Questa classe e' un decorator per "decorare" classi di tipo MsgWriter_itf e farle diventare MsgWriterEx_itf. La
 * classe implementa l'interfaccia MsgWriterEx_itf ed i suoi campi sono accedibili tramite i metodi getField() e
 * setField(). La caratteristica che la contraddistingue e' che i nomi dei campi che si possono utilizzare e' stabilita
 * e verificata. Il modo di stabilire l'elenco dei nomi campi validi viene fornito dalle sottoclassi implementando il
 * metodo astratto getFieldList().
 * 
 * @author Simone
 */
public class MsgWriterEx implements MsgWriterEx_itf {

    private MsgWriter_itf _msgwr       = null;
    private Set           _fieldList   = new HashSet();
    private boolean       _initialized = false;
    private String        _type        = null;

    /**
     * Costruttore presente solo per permettere la serializzazione/deserializzazione di oggetti di questa classe. <b>Non
     * deve essere utilizzato</b>
     */
    public MsgWriterEx() {

    }

    /**
     * Crea un nuovo MsgWriterEx che utilizza come MsgWriter base un HashtableReadWrite. Questo constructor e' presente
     * per permettere alle sottoclassi la creazione di oggetti di questo tipo. Il _type del messaggio deve essere
     * impostato dalle sottoclassi che probabilmente redifiniranno anche il metodo getFieldList per impostare i nomi dei
     * campi validi
     * 
     * @throws AppCrash
     */
    protected MsgWriterEx(String type) throws AppCrash {

        _msgwr = new HashtableReadWrite();
        _type = type;
    }

    /**
     * Crea un nuovo MsgWriterEx che utilizza come base il MsgWriter un HashtableReadWrite ma legge la specifica del
     * tipo messaggio del MsgReader in ingresso. I nomi dei campi validi sono solo quelli compresi nella specifica
     * letta.
     * 
     * @param msg MsgReader_itf usato per stabilire il tipo di messaggio, e quindi la specifica da leggere
     * @throws AppCrash
     */
    public MsgWriterEx(MsgReader_itf msg) throws AppCrash {

        _msgwr = new HashtableReadWrite();
        _type = msg.getType();

    }

    /**
     * Crea un nuovo MsgWriterEx che utilizza come base il MsgWriter passato.I nomi dei campi validi sono solo quelli
     * compresi nella specifica del tipo messaggio in ingresso.
     * 
     * @param msg MsgWriter_itf da utilizzare come base.
     */
    public MsgWriterEx(MsgWriter_itf msg) {

        _msgwr = msg;
    }

    /**
     * Crea un nuovo MsgWriterEx che utilizza come base il MsgWriterEx passato.I nomi dei campi validi sono solo quelli
     * compresi restituiti dal metodo iterator() del messaggio passato.
     * 
     * @param msg MsgWriterEx_itf da utilizzare come base.
     */
    public MsgWriterEx(MsgWriterEx_itf msg) throws AppCrash {

        _msgwr = msg;

        Iterator fields = msg.iterator();

        while (fields.hasNext()) {
            _fieldList.add(fields.next());
        }

        _initialized = true;
    }

    /**
     * Questo metodo serve per impostare il valore di un campo del messaggio
     * 
     * @param name name nome del campo.
     * @param value value valore del campo.
     * 
     * @exception AppCrash
     */
    @Override
    public void setField(String name, String value) throws AppCrash {

        try {
            ErrDetector.GetInstance().preCond(checkFieldName(name), "Campo non permesso:" + name);

            _msgwr.setField(name, value);
        } catch (AppCrash e) {
            e.logContext("MsgWriterEx", this.toString());
            throw e;
        }
    }

    /**
     * Questo metodo serve per recuperare il valore di un campo dal messaggio
     * 
     * @param name Name nome del campo da leggere
     * 
     * @return java.lang.String valore del campo richiesto
     * 
     * @exception AppCrash Nel caso vi fossere problemi nella lettura del campo.
     */
    @Override
    public String getField(String name) throws AppCrash {

        try {
            ErrDetector.GetInstance().preCond(checkFieldName(name), "Campo non permesso:" + name);

            return _msgwr.getField(name);
        } catch (AppCrash e) {
            e.logContext("MsgWriterEx", this.toString());
            throw e;
        }
    }

    /**
     * Questo metodo restituisce il messaggio costruito dal MsgWriter di base
     * 
     * @return byte[] un array di byte che contiene il messaggio costruito
     * 
     * @throws AppCrash
     */
    @Override
    public byte[] getMessage() throws AppCrash {

        return _msgwr.getMessage();
    }

    /**
     * Questo metodo
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public String getType() {

        if (_type != null) {
            return _type;
        }

        return _msgwr.getType();
    }

    /**
     * Questo metodo permette di settare i campi di un MsgWriter a partire dai campi di un array. Per eseguire il
     * mapping prende in ingresso una matrice che contiene per ogni riga una coppia "destinazione" - "valore"
     * 
     * @param mapping matrice "destinazione" - "valore" .
     * 
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    public void copy(String[][] mapping) throws AppCrash {

        ErrDetector.GetInstance().param(mapping);

        int j = 0;

        for (j = 0; j < mapping.length; j++) {
            setField(mapping[j][0], mapping[j][1]);
        }
    }

    /**
     * Questo metodo cerca di estrarre da un MsgReader tutti i campi che sono indicati come validi per il MsgWriterEx
     * del quale fa parte.
     * 
     * @param msg MsgReader_itf il MsgReader dal quale prelevare i campi
     * 
     * @throws AppCrash
     */
    @Override
    public void copy(MsgReader_itf msg) throws AppCrash {

        Iterator iter = this.iterator();

        while (iter.hasNext()) {
            String campo = (String) iter.next();
            _msgwr.setField(campo, msg.getField(campo));
        }
    }

    /**
     * Questo metodo cerca di estrarre da un MsgReader tutti i campi che sono indicati nell'array di copiarli
     * all'interno dell'oggetto del quale fa parte.
     * 
     * @param msg MsgReader_itf il MsgReader dal quale prelevare i campi
     * @param mapping matrice che contiene il mapping dei campi da copiare nel formato "destinazione" - "sorgente"
     */
    @Override
    public void copy(MsgReader_itf msg, String[][] campi) throws AppCrash {

        int j = 0;
        try {
            for (j = 0; j < campi.length; j++) {
                setField(campi[j][0], msg.getField(campi[j][1]));
            }
        } catch (AppCrash e) {
            e.logContext("MsgWriterEx", "Riga corrente " + campi[j][0] + " = " + campi[j][1] + " indice " + j + " -- "
                    + this.toString());
        }
    }

    /**
     * Questo metodo serve per recuperare l'elenco dei nomi dei campi che fanno legalmente parte del MsgWriterEx
     * 
     * @return Iterator contiene i nomi dei campi
     * 
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    public Iterator iterator() throws AppCrash {

        if (_initialized == false) {
            init();
        }

        return _fieldList.iterator();
    }

    /**
     * Questo metodo
     * 
     * @param level DOCUMENT ME!
     * 
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    public void logDebug(int level) throws AppCrash {

        Iterator iter = this.iterator();

        while (iter.hasNext()) {
            String campo = (String) iter.next();
            String valore = _msgwr.getField(campo);

            switch (level) {
                case 0:
                    Logger.GetInstance().log0(campo + " = " + valore);

                    break;

                case 1:
                    Logger.GetInstance().log1(campo + " = " + valore);

                    break;

                case 2:
                    Logger.GetInstance().log2(campo + " = " + valore);

                    break;

                case 3:
                    Logger.GetInstance().log3(campo + " = " + valore);

                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Questo metodo restituisce l'insieme dei campi ammissibili per il tipo di messaggio
     * 
     * @return Iterator campi
     * 
     * @throws AppCrash in caso di eccezione
     */
    protected Iterator getFieldList() throws AppCrash {

        LogicalMessageSpecReader specifica = new LogicalMessageSpecReader(this.getType());

        Set nomeCampi = new HashSet();

        // Prelevo dalla specifica logica del messaggio tutti i nomi dei campi obbligatori
        // e facoltativi
        Iterator iter = specifica.getMandatoryFields();

        while (iter.hasNext()) {
            nomeCampi.add(iter.next());
        }

        iter = specifica.getFacoltativeFields();

        while (iter.hasNext()) {
            nomeCampi.add(iter.next());
        }

        return nomeCampi.iterator();
    }

    /**
     * Questo metodo
     * 
     * @param name DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws AppCrash DOCUMENT ME!
     */
    protected boolean checkFieldName(String name) throws AppCrash {

        if (_initialized == false) {
            init();
        }

        return _fieldList.contains(name);
    }

    /**
     * Questo metodo
     * 
     * @throws AppCrash DOCUMENT ME!
     */
    private void init() throws AppCrash {

        Iterator fields = getFieldList();

        while (fields.hasNext()) {
            _fieldList.add(fields.next());
        }

        _initialized = true;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer("Campi permessi:");
        try {
            Iterator fields = iterator();

            while (fields.hasNext()) {
                sb.append((String) fields.next()).append("-");
            }
        } catch (AppCrash e) {
            sb.append("errore recuperando elenco campi");
        }

        return sb.toString();
    }
}
