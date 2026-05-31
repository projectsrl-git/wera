/*
  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore: Anna L.

  Note:


 */

package net.project.mess.fixlen;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import net.project.errors.AppCrash;
import net.project.mess.MsgReader_itf;
import net.project.mess.MsgWriterEx_itf;
import net.project.mess.MsgWriter_itf;

/**
 * Questa classe estende il decorator MsgWriterEx per i messaggi a lunghezza fissa. Ridefinisce il metodo getFieldList()
 * per ottenere l'elenco dei nomi campi validi
 *
 * @author Anna
 */
public class FixReadEx extends net.project.mess.MsgWriterEx {

    /**
     * Costruttore presente solo per permettere la serializzazione/deserializzazione di oggetti di questa classe. <b>Non
     * deve essere utilizzato</b>
     */
    public FixReadEx() {

        super();
    }

    /**
     * Crea un nuovo MsgWriterEx che utilizza come MsgWriter base un HashtableReadWrite. Questo constructor e' presente
     * per permettere alle sottoclassi la creazione di oggetti di questo tipo. Il _type del messaggio deve essere
     * impostato dalle sottoclassi che probabilmente redifiniranno anche il metodo getFieldList per impostare i nomi dei
     * campi validi
     *
     * @throws AppCrash
     */
    protected FixReadEx(String type) throws AppCrash {

        super(type);
    }

    /**
     * Crea un nuovo MsgWriterEx che utilizza come base il MsgWriter un HashtableReadWrite ma legge la specifica del
     * tipo messaggio del MsgReader in ingresso. I nomi dei campi validi sono solo quelli compresi nella specifica
     * letta.
     *
     * @param msg MsgReader_itf usato per stabilire il tipo di messaggio, e quindi la specifica da leggere
     * @throws AppCrash
     */
    public FixReadEx(MsgReader_itf msg) throws AppCrash {

        super(msg);
    }

    /**
     * Crea un nuovo MsgWriterEx che utilizza come base il MsgWriter passato.I nomi dei campi validi sono solo quelli
     * compresi nella specifica del tipo messaggio in ingresso.
     *
     * @param msg MsgWriter_itf da utilizzare come base.
     */
    public FixReadEx(MsgWriter_itf msg) {

        super(msg);
    }

    /**
     * Crea un nuovo MsgWriterEx che utilizza come base il MsgWriterEx passato.I nomi dei campi validi sono solo quelli
     * compresi restituiti dal metodo iterator() del messaggio passato.
     *
     * @param msg MsgWriterEx_itf da utilizzare come base.
     */
    public FixReadEx(MsgWriterEx_itf msg) throws AppCrash {

        super(msg);
    }

    /**
     * Questo metodo
     *
     * @return DOCUMENT ME!
     *
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    protected Iterator getFieldList() throws AppCrash {

        Hashtable ht = MessageSpec.GetInstance().getSpec(super.getType());
        Enumeration fields = ht.keys();

        Set nomeCampi = new HashSet();

        while (fields.hasMoreElements()) {
            nomeCampi.add(fields.nextElement());
        }

        return nomeCampi.iterator();
    }
}
