
package net.project.db;

/*
 DB2DBUpdatableEntity_base.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 03/12/2001

 Autore: Rosella V.

 Note:

 Modifiche:

 */

import java.lang.reflect.Constructor;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.DAOAudit;
import net.project.errors.DBCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.misc.Config;

/**
 * Questa classe rappresenta una DBEntity_base aggiornabile. E' equivalente alla class DBUpdatableEntity_base, ma
 * utilizza per aggiornare il DB un PreparedStatement Oltre a questa particolarita' essa utilizza i primi due caratteri
 * del nome dei campi per stabilire il loro tipo ed utilizzare il corretto metodo per aggiornare la colonna.
 * <p>
 * <p>
 * L'auditing delle operazioni fatte dagli utenti sul DB viene eseguito solo se e' presente nella configurazione una
 * proprieta' del tipo DBAudit.[nome completo della classe] con valore true.
 * <p>
 * 
 * @author simone
 */
public abstract class DB2DBUpdatableEntity_base extends DBEntity_base {

    protected String            _whereCondition    = null;
    private Hashtable           _dirtyFields       = new Hashtable();

    /** Prefissi che indicano i tipi numerici nel DB Q_ P_ M_ e V_ */
    private static final String _NumericTypePrefix = "?IQ_P_M_V_";

    // Flag per abilitare l'audit delle operazioni eseguite dal DAO
    private boolean             _auditEnabled      = false;
    private Map                 _auditMap          = null;

    /**
     * Costruttore.
     */
    public DB2DBUpdatableEntity_base(String tableName) {

        super(tableName);
        checkAudit();
    }

    /**
     * Costruttore con nome file configurazione.
     */
    public DB2DBUpdatableEntity_base(String tableName, String configName) {

        super(tableName, configName);
        checkAudit();
    }

    /**
     * Costruttore con connessione.
     */
    public DB2DBUpdatableEntity_base(DBTransaction transact, String tableName) {

        super(transact, tableName);
        checkAudit();
    }

    /**
     * Ritorna true se viene recuperata una riga dal database altrimenti false.
     * 
     * @return boolean
     */
    @Override
    public boolean retrieve() throws AppCrash {

        boolean result = false;

        result = super.retrieve();
        _whereCondition = whereCondition();

        return result;
    }

    /**
     * Esecuzione di una operazione di insert/update su una connessione.
     */
    protected void doWork(String query) throws AppCrash {

        PreparedStatement ps = null;
        DBConnection_itf connection = null;

        try {
            if (Logger.GetInstance().getLogLevel() >= 3) {
                Logger.GetInstance().log3("Dowork " + query);
            }
            connection = super.getConnection();
            ps = connection.prepareStatement(query);

            setValuesPart(ps);
            int row = ps.executeUpdate();
            // rinnova la where condition in modo da riflettere la riga attuale
            _whereCondition = whereCondition();

        } catch (SQLException e) {
            DBCrash ex = new DBCrash(e);
            ex.logContext("DBUpdatableEntity_base", "Query: " + query);
            throw ex;
        } catch (AppCrash err) {
            err.logContext("DBUpdatableEntity_base", "Query: " + query);
            throw err;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                freeConnection();
            } catch (SQLException e) {
                DBCrash ex = new DBCrash(e);
                ex.logContext("DBUpdatableEntity_base", "Errore chiudendo lo statement");
                throw ex;
            }
        }

    }

    /**
     * Inserisce una riga nel database.
     */
    public void insert() throws AppCrash {

        StringBuffer tempBuffer = new StringBuffer();
        String insert = null;

        try {
            ErrDetector.GetInstance().invariant(!_tableColumns.isEmpty());
            tempBuffer.append("insert into ").append(_tableName);
            tempBuffer.append(createColumnsPart());
            tempBuffer.append(" values ");
            tempBuffer.append(createValuesPart());
            insert = tempBuffer.toString();
            doWork(insert);
            clearDirtyFieldFlags();
        } catch (AppCrash e) {
            e.logContext("DBUpdatableEntity_base", "insert : " + toString());
            throw e;
        }
        if (_auditEnabled) {
            try {
                DAOAudit audit = makeDAOAudit(_tableName);
                audit.setFields(_auditMap);
                audit.setOperation(DAOAudit.OP_INSERT);
                Logger.GetInstance().addInfo(Integer.toString(audit.hashCode()), audit);
            } catch (Throwable t) {
                // Se c'e' un problema non eseguo l'audit ma non vado in errore
            }

        }
    }

    /**
     * Aggiorna una riga del database.
     */
    public void updateDB2() throws AppCrash {

        String update = null;
        StringBuffer tempBuffer = new StringBuffer();

        try {
            ErrDetector.GetInstance().invariant(!_tableColumns.isEmpty());
            ErrDetector.GetInstance().invariant(_whereCondition != null);

            tempBuffer.append("update ").append(_tableName).append(" set ");
            tempBuffer.append(createColumnsPart());
            tempBuffer.append(" = ");
            tempBuffer.append(createValuesPart());

            tempBuffer.append(_whereCondition);
            update = tempBuffer.toString();
            doWork(update);
            clearDirtyFieldFlags();
        } catch (AppCrash e) {
            e.logContext("DBUpdatableEntity_base", "update : " + toString());
            throw e;
        }
        if (_auditEnabled) {
            try {
                DAOAudit audit = makeDAOAudit(_tableName);
                audit.setFields(_auditMap);
                audit.setOperation(DAOAudit.OP_UPDATE);
                audit.setWhereCondition(_whereCondition);
                Logger.GetInstance().addInfo(Integer.toString(audit.hashCode()), audit);
            } catch (Throwable t) {
                // Se c'e' un problema non eseguo l'audit ma non vado in errore
            }

        }

    }

    /**
     * Aggiorna una riga del database.
     */
    public void update() throws AppCrash {

        String update = null;
        StringBuffer tempBuffer = new StringBuffer();

        try {
            ErrDetector.GetInstance().invariant(!_tableColumns.isEmpty());
            ErrDetector.GetInstance().invariant(_whereCondition != null);

            tempBuffer.append("update ").append(_tableName).append(" set ");

            for (Enumeration colonne = _dirtyFields.keys(); colonne.hasMoreElements();) {
                String col = (String) colonne.nextElement();

                if (col.startsWith("?")) {
                    col = col.substring(2);
                }

                tempBuffer.append(col).append(" = ? ");

                if (colonne.hasMoreElements()) {
                    tempBuffer.append(", ");
                }
            }

            tempBuffer.append(_whereCondition);
            update = tempBuffer.toString();
            doWork(update);
            clearDirtyFieldFlags();
        } catch (AppCrash e) {
            e.logContext("DBUpdatableEntity_base", "update : " + toString());
            throw e;
        }
        if (_auditEnabled) {
            try {
                DAOAudit audit = makeDAOAudit(_tableName);
                audit.setFields(_auditMap);
                audit.setOperation(DAOAudit.OP_UPDATE);
                audit.setWhereCondition(_whereCondition);
                Logger.GetInstance().addInfo(Integer.toString(audit.hashCode()), audit);
            } catch (Throwable t) {
                // Se c'e' un problema non eseguo l'audit ma non vado in errore
            }

        }

    }

    /**
     * Eliminazione dell'entita' rappresentata dall'oggetto dal DB
     */
    public void delete() throws AppCrash {

        try {
            ErrDetector.GetInstance().invariant(!_tableColumns.isEmpty());
            ErrDetector.GetInstance().invariant(_whereCondition != null);
            StringBuffer tempBuffer = new StringBuffer();
            tempBuffer.append("delete from ").append(_tableName).append(_whereCondition);

            String query = tempBuffer.toString();
            doWork(query);
        } catch (AppCrash e) {
            e.logContext("DBUpdatableEntity_base", "delete : " + toString());
            throw e;
        }
        if (_auditEnabled) {
            try {
                DAOAudit audit = makeDAOAudit(_tableName);
                audit.setOperation(DAOAudit.OP_DELETE);
                audit.setWhereCondition(_whereCondition);
                Logger.GetInstance().addInfo(Integer.toString(audit.hashCode()), audit);
            } catch (Throwable t) {
                // Se c'e' un problema non eseguo l'audit ma non vado in errore
            }

        }

    }

    /**
     * Impostata una entry della hashtable.
     */
    @Override
    public void setAttribute(String attribID, Object value) {

        if (_auditEnabled) {
            try {
                _auditMap.put(attribID, (String) _tableColumns.get(attribID) + " -> " + (String) value);
            } catch (Throwable t) {
                // Se c'e' un problema non eseguo l'audit ma non vado in errore
            }
        }
        _dirtyFields.put(attribID.toUpperCase(), new Integer(1));
        super.setAttribute(attribID.toUpperCase(), value); // metto i valori in una Hashtable usando il nome della
        // colonna come chiave
    }

    @Override
    public void clearDirtyFieldFlags() {

        _dirtyFields.clear();
        if (_auditEnabled) {
            _auditMap.clear();
        }
    }

    protected String createColumnsPart() {

        String col = null;
        StringBuffer tempBuffer = new StringBuffer();
        tempBuffer.append(" (");
        for (Enumeration colonne = _dirtyFields.keys(); colonne.hasMoreElements();) {
            col = (String) colonne.nextElement();
            if (col.startsWith("?")) {
                col = col.substring(2);
            }
            tempBuffer.append(col);
            if (colonne.hasMoreElements()) {
                tempBuffer.append(",");
            }
        }
        tempBuffer.append(") ");
        return tempBuffer.toString();
    }

    protected String createValuesPart() {

        String col = null;
        StringBuffer tempBuffer = new StringBuffer();
        tempBuffer.append(" (");
        for (Enumeration colonne = _dirtyFields.keys(); colonne.hasMoreElements();) {
            col = (String) colonne.nextElement();
            tempBuffer.append("?,");
        }
        tempBuffer.setCharAt(tempBuffer.length() - 1, ')');
        return tempBuffer.toString();
    }

    protected void setValuesPart(PreparedStatement ps) throws AppCrash {

        Object colValue = new Object();
        int index = 1;
        String temp;
        try {
            for (Enumeration colonne = _dirtyFields.keys(); colonne.hasMoreElements();) {
                colValue = colonne.nextElement();
                if (_tableColumns.get(colValue) != null) {
                    temp = (String) _tableColumns.get(colValue);
                } else {
                    temp = null;
                }

                // La naming convention per i campi del DB su DB2 ci permette di desumere il tipo di dato
                // della colonna dalle prime due lettere del suo nome
                String typeId = ((String) colValue).substring(0, 2);
                if (_NumericTypePrefix.indexOf(typeId) >= 0) {
                    if ((_tableColumns.get(colValue) == null)
                            || (((String) _tableColumns.get(colValue)).equals("#null#"))) {
                        ps.setBigDecimal(index, null);
                    } else {
                        if (temp != null && !temp.equals("")) {
                            ps.setBigDecimal(index, new java.math.BigDecimal(temp));
                        } else {
                            ps.setBigDecimal(index, null);
                        }
                    }
                } else {
                    if ((_tableColumns.get(colValue) == null)
                            || (((String) _tableColumns.get(colValue)).equals("#null#"))) {
                        ps.setString(index, null);
                    } else {
                        ps.setString(index, temp);
                    }
                }

                index++;
            }
        } catch (SQLException e) {
            DBCrash ex = new DBCrash(e);
            ex.logContext("DBUpdatableEntity_base", "PreparedStatement = " + ps.toString());
            throw ex;
        }
    }

    @Override
    public String toString() {

        String temp;
        Object colValue = new Object();
        StringBuffer tempBuffer = new StringBuffer("\nColonne: " + createColumnsPart() + "\nValori: ");
        tempBuffer.append(" ( ");
        for (Enumeration colonne = _dirtyFields.keys(); colonne.hasMoreElements();) {
            colValue = colonne.nextElement();
            if ((_tableColumns.get(colValue) == null) || (((String) _tableColumns.get(colValue)).equals("#null#"))) {
                temp = "null";
                tempBuffer.append(temp);
                if (colonne.hasMoreElements()) {
                    tempBuffer.append(",");
                }
            } else {
                temp = (String) _tableColumns.get(colValue);
                temp = temp.replace('\'', ' ');

                // La naming convention per i campi del DB su DB2 ci permette di desumere il tipo di dato
                // della colonna dalle prime due lettere del suo nome
                String typeId = ((String) colValue).substring(0, 2);
                if (_NumericTypePrefix.indexOf(typeId) >= 0) {
                    tempBuffer.append(temp);
                } else {
                    tempBuffer.append("'").append(temp).append("'");
                }

                if (colonne.hasMoreElements()) {
                    tempBuffer.append(",");
                }
            }

        }
        tempBuffer.append(") ");

        return tempBuffer.toString();
    }

    /**
     * Ritorna il valore dell'attributo identificato da attribID.
     * 
     * @param attribID java.lang.String
     * @return java.lang.Object
     */
    @Override
    public Object getAttribute(String attribID) throws AppCrash {

        String col = attribID;

        // Se il nome dell'attributo inizia con ? devo eliminare i primi 2 caratteri
        if (attribID.startsWith("?")) {
            col = attribID.substring(2);
        }

        Object valore = super.getAttribute(col);

        if (valore != null) return valore;

        return super.getAttribute(attribID);
    }

    // Verifica se l'audit e' abilitato per questa classe
    private void checkAudit() {

        String nome = this.getClass().getName();
        if (Config.GetInstance().getProperty("DBAudit." + nome, "false").equalsIgnoreCase("true")) {
            _auditEnabled = true;
            _auditMap = new HashMap();
        }
    }

    /**
     * Questo metodo metodo viene utilizzato dalla classe per creare oggetti di tipo DAOAudit per eseguire il log delle
     * modifiche fatte dagli utenti al DB. Il metodo puo' essere ridefinito dalle sottoclassi per fornire oggetti che
     * loggano in modo personalizzato.
     * <p>
     * Di deafult viene letta la proprieta di configurazione DBAudit.class e viene istanziata la classe corrispondente;
     * se la proprieta' non c'e' o si verifica un errore viene istanziata la classe base della gerarchia
     * net.project.errors.DAOAudit.
     * <p>
     * <p>
     * 
     * @param table java.lang.String la tabella
     * @return DAOAudit l'oggetto per eseguire il log
     */
    protected DAOAudit makeDAOAudit(String table) {

        try {
            String auditClass = Config.GetInstance(_configName).getProperty("DBAudit.class",
                    "net.project.errors.DAOAudit");
            Class tempClass = Class.forName(auditClass);
            Constructor constr = tempClass.getConstructor(new Class[] { String.class });
            return (DAOAudit) constr.newInstance(new Object[] { table });
        } catch (Throwable e) {
            return new DAOAudit(table);
        }

    }
}
