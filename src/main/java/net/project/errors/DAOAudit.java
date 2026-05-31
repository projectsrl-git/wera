/*
 Copyright (c) by SSB Spa Societa' per i Servizi Bancari

 Note:

 */

package net.project.errors;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Questa classe rappresenta un log di audit per le modifiche fatte al DB tramite le classi della gerarchia derivante da
 * DB2DBUpdatableEntity_base. Gli oggetti di questa classe vengono creati da DB2DBUpdatableEntity_basee messi nel thread
 * local di Logger ad ogni operazione di insert, delete o update.
 * <p>
 * Lo store scrive il log su logApplication.
 *
 */
public class DAOAudit implements StorableLog_itf {

    public static final String OP_INSERT   = "INSERT";
    public static final String OP_UPDATE   = "UPDATE";
    public static final String OP_DELETE   = "DELETE";
    public static final String INFOUSERKEY = "DBAUDITUSER";

    private String             _whereCond  = null;
    private Map                _fields     = null;
    private String             _operation  = null;
    private String             _table      = null;

    /**
     * Costruttore
     * 
     * @param table il nome della tabella
     */
    public DAOAudit(String table) {

        super();
        _table = table;
    }

    /**
     * Questo metodo esegue il log vero e proprio delle info di audit. L'utente che ha eseguito l'operazione viene
     * recuperato dalla proprieta' DAOAudit.INFOUSERKEY nel thread local Logger.GetInstance().getInfo(). Il log viene
     * eseguito su una riga del log application
     *
     * @see net.project.errors.StorableLog_itf#store()
     */
    @Override
    public void store() {

        try {
            DAOAuditUser user = (DAOAuditUser) Logger.GetInstance().getInfo().get(INFOUSERKEY);
            StringBuffer str = new StringBuffer();

            str.append("DBAUDIT USER=").append(user.getUserId());
            str.append(" TABLE=").append(_table);
            str.append(" OPERATION=").append(_operation);

            if (_fields != null) {
                Set entries = _fields.entrySet();
                Iterator it = entries.iterator();
                while (it.hasNext()) {
                    Entry elem = (Entry) it.next();
                    str.append(" ").append(elem.getKey()).append(":").append(elem.getValue()).append("|");
                }
            }

            if (_whereCond != null) {
                str.append(" RECORD=").append(_whereCond);
            }
            Logger.GetInstance().logApplication(str.toString());
        } catch (Throwable e) {
            // Se eccezione non loggo ma non vado in errore
        }
    }

    /**
     * Questo metodo imposta il parametro wherecondition
     *
     * @param whereCond identifica il record modificato
     */
    public void setWhereCondition(String whereCond) {

        _whereCond = whereCond;
    }

    /**
     * @param fields il fields da impostare.
     */
    public void setFields(Map fields) {

        _fields = fields;
    }

    /**
     * @param operation il operation da impostare.
     */
    public void setOperation(String operation) {

        _operation = operation;
    }
}
