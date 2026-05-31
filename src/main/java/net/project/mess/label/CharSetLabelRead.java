/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.mess.label;

/*
 CharSetLabelRead.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 28/09/2004

 Autore: Simona T.

 Note:

 Modifiche:

 */
import net.project.errors.AppCrash;
import net.project.mess.MsgReader_itf;
import net.project.misc.Config;
import net.project.misc.Converter;

/**
 * La classe implementa un reader per i messaggi label in dipendenza dalla codifica stabilita dalla proprietà
 * CharSetLabelRead.SourceCharSet in configurazione.
 */
public class CharSetLabelRead implements MsgReader_itf {

    // costante per la lettura della conversione da applicare
    public static final String ENCODING_TYPE         = "CharSetLabelRead.SourceCharSet";

    // codifica di default
    public static final String DEFAULT_ENCODING_TYPE = "8859_1";                        // ASCII
    public static final String NONE_ENCODING         = "NONE";
    private byte[]             _messaggio;
    private String             _encoding;
    private MsgReader_itf      _reader;

    /**
     * Costruttore. Effettua la codifica del messaggio in base al parametro letto da file. di configurazione.
     *
     * @param messaggio byte[] messaggio.
     */
    public CharSetLabelRead(byte[] messaggio) throws AppCrash {

        // leggo da file di configurazione la codifica da applicare
        try {
            _encoding = Config.GetInstance().getProperty(ENCODING_TYPE, DEFAULT_ENCODING_TYPE);

            if (_encoding.equals(NONE_ENCODING)) {
                _messaggio = messaggio;
            } else {
                _messaggio = Converter.convertIn(messaggio, _encoding);
            }

            // istanzio il reader
            _reader = new LabelRead(_messaggio);
        } catch (AppCrash e) {
            e.logContext("CharSetLabelRead", "messaggio = " + new String(messaggio) + ", codifica = " + _encoding
                    + ", messaggio = " + new String(_messaggio));
            throw e;
        }
    }

    /**
     * Ritorna il campo corrispondente a nome.
     *
     * @param nome java.lang.String nome del campo.
     *
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
