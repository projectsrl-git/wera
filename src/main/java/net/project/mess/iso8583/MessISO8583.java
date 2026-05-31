
package net.project.mess.iso8583;

/*
 MessISO8583.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 04/06/1999

 Autore: Rosella V.

 Note:

 Modifiche:

 */

import java.util.Enumeration;

import net.project.errors.AppCrash;
import net.project.mess.MsgFactory_base;
import net.project.mess.MsgWriter_itf;

/**
 * Questa classe rappresenta un messaggio ISO8583. Il contenuto dei campi del messaggio viene costruito a partire dalla
 * sorgente dati passata nel costruttore
 */
public class MessISO8583 {

    private static final String PREFIX_IF_EMPTY_NOT_PRESENT = "IF_EMPTY_NOT_PRESENT";
    private DataSource8583_base _dataSource8583Base;
    private MsgWriter_itf       _constructorISO8583Itf;

    /**
     * Costruttore. Vengono impostati: la specifica del messaggio e il data source.
     * 
     * @param net.project.mess.iso8583.DataSource8583_base sorgente dati del messaggio.
     * @exception AppCrash
     */
    public MessISO8583(DataSource8583_base dataSource) throws AppCrash {

        _dataSource8583Base = dataSource;
        _constructorISO8583Itf = MsgFactory_base.GetInstance().MakeMsgWriter(dataSource.getType());
    }

    /**
     * Costruisce e ritorna un generico messaggio ISO8583.
     * 
     * @return byte[]
     */
    public byte[] buildMessage() throws AppCrash {

        byte[] message = null;
        SpecificaCampo specCampo = null;
        String temp = null;

        try {
            String messageType = _dataSource8583Base.getType(); // tipo del messaggio : 0100, 0420 ...
            Enumeration enumer = DepositoSpec.GetInstance().getSpecifica(messageType);
            while (enumer.hasMoreElements()) {
                specCampo = (SpecificaCampo) enumer.nextElement(); // specCampo è la chiave
                temp = new String(_dataSource8583Base.getData(specCampo.getDataElement()));
                // valuto se il campo, quando non valorizzato, deve non essere presente nel messaggio
                if (!((specCampo.getDataSourceName().startsWith(PREFIX_IF_EMPTY_NOT_PRESENT)) && (temp.length() == 0))) {
                    _constructorISO8583Itf.setField(Integer.toString(specCampo.getDataElement()), temp);
                }
            }
            message = _constructorISO8583Itf.getMessage();
        } catch (AppCrash e) {
            e.logContext("MessISO8583", "errore buil messaggio");
            throw (e);
        }

        return message;
    }
}
