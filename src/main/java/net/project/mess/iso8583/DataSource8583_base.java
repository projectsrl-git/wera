
package net.project.mess.iso8583;

/*
 DataSource8583_base.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 28/05/1999

 Autore: Simone Z.

 Note:

 Modifiche:

 */
import java.util.Enumeration;
import java.util.Hashtable;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;

/**
 * Questa classe e' l'astrazione di base delle sorgenti dati per i messaggi in formato ISO8583.
 */
public abstract class DataSource8583_base {

    private Hashtable _datas;
    private String    _type;

    /**
     * Costruttore
     * 
     * @param messageType il tipo di messaggio ISO8585
     */
    protected DataSource8583_base(String messageType) {

        _type = messageType;
        _datas = new Hashtable(31, .5f);
    }

    /**
     * Ritorna il valore di un campo.
     * 
     * @param int chiave del campo.
     * @return java.lang.String
     * @exception AppCrash
     */
    public String getData(int elementName) throws AppCrash {

        String data;
        try {
            data = (String) _datas.get(new Integer(elementName).toString());
            ErrDetector.GetInstance().invariant(data != null);
        } catch (AppCrash err) {
            err.logContext("DataSource8583", "Elemento non trovato:" + elementName);
            throw err;
        }
        return data;
    }

    protected abstract String getElement(int dataElementNum) throws AppCrash;

    /**
     * This method was created in VisualAge.
     * 
     * @return java.lang.String
     */
    public String getType() {

        return _type;
    }

    /**
     * Inizializza i campi relativi ad un data source
     */
    public void init() throws AppCrash {

        Enumeration enumer = DepositoSpec.GetInstance().getSpecifica(getType());

        startInit();
        while (enumer.hasMoreElements()) {
            SpecificaCampo specCampo = (SpecificaCampo) enumer.nextElement(); // specCampo è la chiave
            if (specCampo.isDataSourceConst()) {
                setData(Integer.toString(specCampo.getDataElement()), specCampo.getDataSourceName());
            } else {
                String value = getElement(specCampo.getDataElement());
                setData(Integer.toString(specCampo.getDataElement()), value);
            }
        }
    }

    /**
     * Aggiunta elemento in hashtable (elementName-value).
     * 
     * @param java.lang.String chiave
     * @param java.lang.String valore
     */
    private void setData(String elementName, String value) {

        _datas.put(elementName, value);
    }

    /**
     * Questo metodo viene richiamato dal metodo init prima di compiere la sua elaborazione.
     * <p>
     * E' un punto di estensione per permettere alle sottoclassi concrete di far precedere init() da codice proprio.
     *
     * @throws AppCrash in caso di eccezione
     */
    protected abstract void startInit() throws AppCrash;
}
