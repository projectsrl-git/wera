/*
  ATDATURLCharSetRead.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 18/06/2001

  Autore: Rosella V.

  Note:

  Modifiche:

 */

package net.project.mess.atdat;

import net.project.errors.AppCrash;
import net.project.mess.MsgReader_itf;
import net.project.misc.Config;
import net.project.misc.Converter;

/**
 * Classe per la lettura e la codifica di un messaggio ATDAT. La codifica da applicare può essere letta da file di
 * configurazione (ATDATURLCharSetRead.SourceCharSet) o può essere esplicitamente indicata nel costruttore. Tale
 * proprietà deve essere valorizzata con la costante relativa alla codifica voluta. Possibili valori sono ad esempio:
 * 8859_1,Cp1046.
 */
public class ATDATURLCharSetRead implements MsgReader_itf {

    // costante per la lettura della conversione da applicare
    public static final String ENCODING_TYPE         = "ATDATURLCharSetRead.SourceCharSet";

    // codifica di default
    public static final String DEFAULT_ENCODING_TYPE = "8859_1";
    public static final String NONE_ENCODING         = "NONE";

    private byte[]             _messaggio;
    private String             _encoding;
    private MsgReader_itf      _reader;

    /**
     * Costruttore presente solo per permettere la serializzazione/deserializzazione di oggetti di questa classe. <b>Non
     * deve essere utilizzato</b>
     */
    public ATDATURLCharSetRead() {

    }

    /**
     * Costruttore. Effettua la codifica del messaggio in base al parametro letto da file. di configurazione.
     * 
     * @param messaggio byte[] messaggio.
     */
    public ATDATURLCharSetRead(byte[] messaggio) throws AppCrash {

        // leggo da file di configurazione la codifica da applicare
        try {
            _encoding = Config.GetInstance().getProperty(ENCODING_TYPE, DEFAULT_ENCODING_TYPE);
            if (_encoding.equals(NONE_ENCODING)) {
                _messaggio = messaggio;
            } else {
                _messaggio = Converter.convertIn(messaggio, _encoding);
            }
            // istanzio il reader
            _reader = new ATDATURLRead(_messaggio);
        } catch (AppCrash e) {
            e.logContext("ATDATURLCharSetRead", "messaggio = " + new String(messaggio) + ", codifica = " + _encoding
                    + ", messaggio = " + new String(_messaggio));
            throw e;
        }
    }

    /**
     * Ritorna il campo corrispondente a nome.
     * 
     * @param nome java.lang.String nome del campo.
     * @return String
     */
    @Override
    public String getField(String nome) throws AppCrash {

        return _reader.getField(nome);
    }

    /**
     * Ritorna il tipo di messaggio.
     * 
     * @return String
     */
    @Override
    public String getType() {

        return _reader.getType();
    }

}