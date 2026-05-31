/*
  LobDBRow.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione: 26/06/2003

  Autore: Fabio F. e Luca M.

  Note:

  Modifiche:

 */

package net.project.dataset;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.project.errors.AppCrash;
import net.project.errors.DBCrash;
import net.project.errors.ErrDetector;
import net.project.errors.ParamCrash;

/**
 * Classe che specializza DBRow permettendo di leggere anche i CLOB e i BLOB
 */
public class LobDBRow extends DBRow {

    /**
     * @param set
     * @throws ParamCrash
     */
    public LobDBRow(ResultSet set) throws ParamCrash {

        super(set);
    }

    /**
	 * 
	 */
    public LobDBRow() {

        super();
    }

    /**
     * Restituisce il valore della colonna richiesta gestendo eventuali BLOB o CLOB
     * 
     * @param java.lang String fieldName il nome del campo
     * @return java.lang.Object il valore del campo
     * @exception net.project.errors.ParamCrash se il parametro in ingresso è null
     * @exception net.project.errors.AppCrash in caso di errore nel recupero del valore del campo
     */
    @Override
    public Object getField(String fieldName) throws AppCrash {

        // Controllo formale del parametro in ingresso.
        ErrDetector.GetInstance().param(fieldName);

        try {
            Object obj = getResultSet().getObject(fieldName);
            if (obj instanceof Blob) {
                return readBlob((Blob) obj);
            }
            if (obj instanceof Clob) {
                return readClob((Clob) obj);
            }
            return super.getField(fieldName);

        } catch (SQLException sqle) {
            DBCrash dbc = new DBCrash(sqle);
            dbc.logContext("LobDBRow", "fieldName = " + fieldName);
            throw dbc;
        }

    }

    /**
     * Restituisce il valore della colonna richiesta gestendo eventuali BLOB o CLOB
     * 
     * @param int fieldNo il numero del campo
     * @return java.lang.Object il valore del campo
     * @exception net.project.errors.ParamCrash se il parametro in ingresso è negativo
     * @exception net.project.errors.AppCrash in caso di errore nel recupero del valore del campo
     */
    @Override
    public Object getField(int fieldNo) throws AppCrash {

        // Controllo formale del parametro in ingresso.
        ErrDetector.GetInstance().preCond(fieldNo > 0);

        try {
            Object obj = getResultSet().getObject(fieldNo);
            if (obj instanceof Blob) {
                return readBlob((Blob) obj);
            }
            if (obj instanceof Clob) {
                return readClob((Clob) obj);
            }
            return super.getField(fieldNo);

        } catch (SQLException sqle) {
            DBCrash dbc = new DBCrash(sqle);
            dbc.logContext("LobDBRow", "fieldNo = " + fieldNo);
            throw dbc;
        }

    }

    private Object readClob(Clob clob) throws AppCrash {

        try {

            return clob.getSubString(1, (int) clob.length());

        } catch (SQLException sqle) {
            throw new DBCrash(sqle);
        }
    }

    private Object readBlob(Blob blob) throws AppCrash {

        try {

            return blob.getBytes(1, (int) blob.length());

        } catch (SQLException sqle) {
            throw new DBCrash(sqle);
        }
    }
}
