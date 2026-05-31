
package net.project.mess.iso8583;

/*
 SpecificaCampo.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 28/05/1999

 Autore: Simone Z.

 Note:

 Modifiche:

 */

import net.project.errors.AppCrash;

/**
 * Questa classe rappresenta la specifica di un campo di un messaggio ISO8583
 */
public class SpecificaCampo {

    private String  _dataSource  = null;
    private int     _dataElement = 0;
    private boolean _constant    = false;

    SpecificaCampo(String dataElement, String dataSource) throws AppCrash {

        try {
            _dataElement = Integer.parseInt(dataElement);
        } catch (NumberFormatException e) {
            AppCrash err = new AppCrash(e);
            err.logContext("SpecificaCampo", "Data Element error: " + dataElement + " - " + dataSource);
            throw err;
        }

        if (dataSource.startsWith("\"")) {
            _constant = true;
            _dataSource = dataSource.substring(1, dataSource.length() - 1);
        } else {
            _dataSource = dataSource;
        }
    }

    /**
     * Ritorna il codice relativo al data element.
     * 
     * @return int
     */
    public int getDataElement() {

        return _dataElement;
    }

    /**
     * @roseuid 374E59C90344
     */
    public String getDataSourceName() {

        return _dataSource;
    }

    /**
     * @roseuid 374E59C90358
     */
    public boolean isDataSourceConst() {

        return _constant;
    }
}
