
package net.project.db;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.project.errors.AppCrash;
import net.project.errors.DAOAudit;
import net.project.errors.DBCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.misc.Config;
import net.project.misc.Converter;

/**
 * Questa classe rappresenta la classe astratta base per gli oggetti di tipo DAO: Data Access Object.
 * <P>
 * Le classi che estendono NDAO_base devono dichiarare tutti i nomi dei campi del DAO (corrispondenti alle colonne della
 * tabella-vista), che verranno acceduti, come static public final String. ATTENZIONE: tutte queste costanti public sono
 * ritenute essere nomi di colonne e sono percio' inserite nella select list creata dinamicamente quando viene
 * effettuata la query. Se occorrono costanti che non siano corrispondenti a colonne devono essere dichiarate
 * diversamente (private o non final)
 *
 * @author Simone
 */
public abstract class NDAO_base {

    public static final DAONull NULLSTRING      = new DAONull();
    protected static final int  FREE_CONNECTION = 1 + Types.OTHER;
    protected static final int  KEEP_CONNECTION = 2;
    private List                _fieldList      = new ArrayList();

    // true se il firewall dei parametri per le web app e' abilitato
    private boolean             _firewall       = false;
    private DBConnection_itf    _connection     = null;
    private Map                 _tableColumns   = new HashMap();
    private int                 _mode           = 0;
    private String              _tableName      = null;
    private String              _configName     = "";
    private WhereCondition      _whereCondition = null;
    private Set                 _dirtyFields    = new HashSet();

    // Flag per abilitare l'audit delle operazioni eseguite dal DAO
    private boolean             _auditEnabled   = false;
    private Map                 _auditMap       = null;

    /**
     * Costruttore con il solo nome tabella.
     *
     * @param tableName nome tabella
     */
    public NDAO_base(String tableName) throws AppCrash {

        _mode = FREE_CONNECTION;
        _tableName = tableName;

        try {
            init();
            checkAudit();
        } catch (AppCrash e) {
            e.logContext("DAO_base", "tableName = " + tableName);
            throw e;
        }
    }

    /**
     * Costruttore con il nome della tabella ed nome della configurazione da usare.
     *
     * @param tableName nome della tabella
     * @param configName nome della configurazione
     */
    public NDAO_base(String tableName, String configName) throws AppCrash {

        _mode = FREE_CONNECTION;
        _tableName = tableName;
        _configName = configName;

        try {
            init();
            checkAudit();
        } catch (AppCrash e) {
            e.logContext("DAO_base", "tableName = " + tableName + " - config = " + configName);
            throw e;
        }
    }

    /**
     * Costruttore con DBTransaction e nome tabella.
     *
     * @param transact transazione
     * @param tableName nome della tabella
     */
    public NDAO_base(DBTransaction transact, String tableName) throws AppCrash {

        _tableName = tableName;
        _connection = transact.getDBConnection();
        _mode = KEEP_CONNECTION;

        try {
            init();
            checkAudit();
        } catch (AppCrash e) {
            e.logContext("DAO_base con DBTransaction ", "tableName = " + tableName);
            throw e;
        }
    }

    /**
     * Metodo
     *
     * @return DBConnection_itf
     */
    protected DBConnection_itf getConnection() throws AppCrash {

        try {
            if (_connection == null) {
                _connection = ConnectionPool.GetInstance(_configName).getAConnection();
            }
        } catch (AppCrash e) {
            e.logContext("DBEntity_base", "errore di recupero connessione da ConnectionPool");
            throw e;
        }

        return _connection;
    }

    protected void freeConnection() {

        if ((_mode != KEEP_CONNECTION) && (_connection != null)) {
            _connection.free();
            _connection = null;
        }
    }

    /**
     * Controlla l'impostazione come FREE_CONNECTION o KEEP_CONNECTION.
     *
     * @return int
     */
    protected int getMode() {

        return _mode;
    }

    /**
     * Ritorna true se viene recuperata una riga dal database altrimenti false.
     *
     * @return boolean
     */
    public boolean retrieve() throws AppCrash {

        boolean ret = false;
        PreparedStatement st = null;
        StringBuffer query = new StringBuffer();

        try {
            query.append(getQuery());

            _connection = getConnection();

            long start = System.currentTimeMillis();
            String completeQuery = query.toString();
            st = _connection.prepareStatement(completeQuery);

            _whereCondition = whereCondition();
            setWhereCondPart(st, _whereCondition.getValues(), 1);

            if (Logger.GetInstance().getLogLevel() >= 3) {
                Logger.GetInstance().log3("Retrieve " + completeQuery + " - " + _whereCondition.toString());
            }

            ResultSet rs = st.executeQuery();

            long end = System.currentTimeMillis();

            if ((end - start) > 2000) {
                Logger.GetInstance().log0(
                        "TIMEretrieve;" + (end - start) + ";" + this.getClass().getName() + ";"
                                + _whereCondition.toString());
            }

            if (rs.next()) {
                // per ogni colonna in result set
                Iterator iter = _fieldList.iterator();

                while (iter.hasNext()) {
                    String col = (String) iter.next();
                    // Se il nome dell'attributo inizia con ? devo eliminare i primi 2 caratteri
                    if (col.startsWith("?")) {
                        col = col.substring(2);
                    }
                    setAttribute(col, rs.getObject(col));
                }

                ret = true;

                // Resetto i flag dirty per i campi
                clearDirtyFieldFlags();

                // controllo che sia stata recuperata una sola riga
                ErrDetector.GetInstance().invariant(!rs.next());
            }
        } catch (SQLException e) {
            DBCrash ex = new DBCrash(e);
            ex.logContext(this.getClass().getName(), "Query: " + query.toString());
            throw ex;
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "Query: " + query.toString());
            throw ac;
        } finally {
            try {
                if (st != null) {
                    st.close();
                }

                freeConnection();
            } catch (SQLException e) {
                DBCrash ex = new DBCrash(e);
                ex.logContext(this.getClass().getName(), "Errore chiudendo lo statement");
                throw ex;
            }
        }

        return ret;
    }

    /**
     * Restiruisce la condizione where che identifica univocamente la riga.
     */
    protected abstract WhereCondition whereCondition() throws AppCrash;

    /**
     * Questo metodo Imposta il valore di un attributo del DAO
     *
     * @param attribID nome attributo
     * @param value valore
     * @throws AppCrash in caso di eccezione
     */
    public void setAttribute(String attribID, Object value) throws AppCrash {

        ErrDetector.GetInstance().preCond(checkFieldName(attribID), "Campo non permesso" + attribID);

        Object objVal = null;

        if (value == null) {
            objVal = NULLSTRING;
        } else {
            objVal = value;
        }

        // Se e' una stringa ed il FW e' abilitato faccio escape
        if (_firewall && objVal instanceof String) {
            objVal = Converter.sqlUnEscape((String) objVal);
        }

        if (_auditEnabled) {
            try {
                _auditMap.put(attribID, (String) _tableColumns.get(attribID) + " -> " + objVal);
            } catch (Throwable t) {
                // Se c'e' un problema non eseguo l'audit ma non vado in errore
            }
        }

        _dirtyFields.add(attribID);
        _tableColumns.put(attribID, objVal);
    }

    /**
     * Questo metodo Imposta il valore di un attributo del DAO con un intero lungo
     *
     * @param attribID nome attributo
     * @param value valore
     * @throws AppCrash in caso di eccezione
     */
    public void setAttribute(String attribID, int value) throws AppCrash {

        setAttribute(attribID, new Integer(value));
    }

    /**
     * Esecuzione di una operazione di insert/update su una connessione.
     */
    protected void doWork(String query) throws AppCrash {

        PreparedStatement ps = null;
        DBConnection_itf connection = null;
        boolean auto = true;

        try {
            if (Logger.GetInstance().getLogLevel() >= 3) {
                Logger.GetInstance().log3("Dowork " + query);
            }

            connection = getConnection();
            auto = connection.getAutoCommit();

            if (auto == true) {
                connection.setAutoCommit(false);
            }

            ps = connection.prepareStatement(query);

            int start = setValuesPart(ps);

            if (_whereCondition != null) {
                setWhereCondPart(ps, _whereCondition.getValues(), start);
            }

            int row = ps.executeUpdate();

            ErrDetector.GetInstance().invariant(row == 1, "Errore! Aggiornata piu' di una riga: " + row);

            if (auto == true) {
                connection.commit();
            }

            // rinnova la where condition in modo da riflettere la riga attuale
            _whereCondition = whereCondition();
        } catch (SQLException e) {
            DBCrash ex = new DBCrash(e);
            ex.logContext("NDAO_base", "Query: " + query);
            throw ex;
        } catch (Throwable err) {
            AppCrash ac = new AppCrash(err);
            ac.logContext("NDAO_base", "Query: " + query);

            if (auto == true) {
                connection.rollback();
            }

            throw ac;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }

                if (auto == true) {
                    connection.setAutoCommit(true);
                }

                freeConnection();
            } catch (SQLException e) {
                DBCrash ex = new DBCrash(e);
                ex.logContext("NDAO_base", "Errore chiudendo lo statement");
                throw ex;
            }
        }
    }

    /**
     * Inserisce una riga nel database.
     */
    public void insert() throws AppCrash {

        StringBuffer tempBuffer = new StringBuffer();
        StringBuffer tempBufferVal = new StringBuffer();
        String insert = null;
        _whereCondition = null;

        try {
            tempBuffer.append("insert into ").append(_tableName).append(" (");
            tempBufferVal.append(" ) values  ( ");

            Logger.GetInstance().log3("=========== IMPOSTA LE COLONNE PER PREPARARE LA INSERT =============");

            for (Iterator colonne = _dirtyFields.iterator(); colonne.hasNext();) {
                String col = (String) colonne.next();

                // Se il nome dell'attributo inizia con ? devo eliminare i primi 2 caratteri
                if (col.startsWith("?")) {
                    col = col.substring(2);
                }

                Logger.GetInstance().log3("---- Campo: " + col);

                tempBuffer.append(col);
                tempBufferVal.append(" ? ");

                if (colonne.hasNext()) {
                    tempBuffer.append(", ");
                    tempBufferVal.append(", ");
                }
            }

            tempBufferVal.append((" ) "));
            tempBuffer.append(tempBufferVal);
            insert = tempBuffer.toString();

            doWork(insert);
            clearDirtyFieldFlags();
        } catch (AppCrash e) {
            e.logContext("NDAO_base", "insert : " + toString());
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
    public void update() throws AppCrash {

        String update = null;
        StringBuffer tempBuffer = new StringBuffer();

        try {
            if (_whereCondition == null) {
                _whereCondition = whereCondition();

                List nomi = _whereCondition.getNames();
                Iterator iter = nomi.iterator();

                while (iter.hasNext()) {
                    _dirtyFields.remove(iter.next());
                }
            }

            tempBuffer.append("update ").append(_tableName).append(" set ");

            for (Iterator colonne = _dirtyFields.iterator(); colonne.hasNext();) {
                String col = (String) colonne.next();

                // Se il nome dell'attributo inizia con ? devo eliminare i primi 2 caratteri
                if (col.startsWith("?")) {
                    col = col.substring(2);
                }

                tempBuffer.append(col).append(" = ? ");

                if (colonne.hasNext()) {
                    tempBuffer.append(", ");
                }
            }

            tempBuffer.append(_whereCondition.getSQL());
            update = tempBuffer.toString();

            doWork(update);
            clearDirtyFieldFlags();
        } catch (AppCrash e) {
            e.logContext("NDAO_base", "update : " + toString());
            throw e;
        }

        if (_auditEnabled) {
            try {
                DAOAudit audit = makeDAOAudit(_tableName);
                audit.setFields(_auditMap);
                audit.setOperation(DAOAudit.OP_UPDATE);
                audit.setWhereCondition(_whereCondition.toString());
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
            StringBuffer tempBuffer = new StringBuffer();

            if (_whereCondition == null) {
                _whereCondition = whereCondition();
            }

            tempBuffer.append("delete from ").append(_tableName).append(_whereCondition.getSQL());

            String query = tempBuffer.toString();
            /*
             * query=query.replace("where","where cast(");
             * 
             * 
             * StringBuilder b = new StringBuilder(query); b.replace(query.lastIndexOf("="), query.lastIndexOf("=") + 1,
             * " as varchar) = " ); query = b.toString();
             */

            clearDirtyFieldFlags();
            doWork(query);
        } catch (AppCrash e) {
            e.logContext("NDAO_base", "delete : " + toString());
            throw e;
        }

        if (_auditEnabled) {
            try {
                DAOAudit audit = makeDAOAudit(_tableName);
                audit.setOperation(DAOAudit.OP_DELETE);
                audit.setWhereCondition(_whereCondition.toString());
                Logger.GetInstance().addInfo(Integer.toString(audit.hashCode()), audit);
            } catch (Throwable t) {
                // Se c'e' un problema non eseguo l'audit ma non vado in errore
            }
        }
    }

    /**
     * Questo metodo elimina l'entita' rappresentata dall'oggetto dal DB usando la transazione passata.
     *
     * @param trans DBTransaction da usare per eseguire la delete
     * @throws AppCrash
     */
    public void delete(DBTransaction trans) throws AppCrash {

        DBConnection_itf tempCon = _connection;
        int tempMode = _mode;
        _connection = trans.getDBConnection();
        _mode = KEEP_CONNECTION;
        delete();
        _connection = tempCon;
        _mode = tempMode;
    }

    /**
     * Questo metodo aggiorna l'entita' rappresentata dall'oggetto dal DB usando la transazione passata.
     *
     * @param trans DBTransaction da usare per eseguire la update
     * @throws AppCrash
     */
    public void update(DBTransaction trans) throws AppCrash {

        DBConnection_itf tempCon = _connection;
        int tempMode = _mode;
        _connection = trans.getDBConnection();
        _mode = KEEP_CONNECTION;
        update();
        _connection = tempCon;
        _mode = tempMode;
    }

    /**
     * Questo metodo inserisce l'entita' rappresentata dall'oggetto dal DB usando la transazione passata.
     *
     * @param trans DBTransaction da usare per eseguire la insert
     * @throws AppCrash
     */
    public void insert(DBTransaction trans) throws AppCrash {

        DBConnection_itf tempCon = _connection;
        int tempMode = _mode;
        _connection = trans.getDBConnection();
        _mode = KEEP_CONNECTION;
        insert();
        _connection = tempCon;
        _mode = tempMode;
    }

    /**
     * Questo metodo ritorna true o false a seconda che il DAO sia stato modificato o meno dall'ultima retrieve. E'
     * basato sull'analisi dei dirtyfields
     *
     * @return true-false
     */
    public boolean isChanged() {

        return (_dirtyFields.isEmpty() == false);
    }

    /**
     * Questo metodo azzera i flag che indicano che un campo e' stato modificato ed azzera la map usata per l'audit
     *
     */
    public void clearDirtyFieldFlags() {

        _dirtyFields.clear();

        if (_auditEnabled) {
            _auditMap.clear();
        }
    }

    protected int setValuesPart(PreparedStatement ps) throws AppCrash {

        int index = 1;
        Object temp = null;
        String col = null;

        Logger.GetInstance().log3("=========== VALORIZZA LE COLONNE =============");

        try {
            for (Iterator colonne = _dirtyFields.iterator(); colonne.hasNext();) {
                col = (String) colonne.next();

                // Se il nome dell'attributo inizia con ? devo eliminare i primi 2 caratteri
                if (col.startsWith("?")) {
                    col = col.substring(2);
                }

                temp = _tableColumns.get(col);

                Logger.GetInstance().log3("---- Campo: " + col);

                setPsValue(ps, temp, index);

                index++;
            }

            return index;
        } catch (SQLException e) {
            DBCrash ex = new DBCrash(e);
            ex.logContext("NDAO_base", "Colonna = " + col + " Valore = " + temp);
            throw ex;
        }
    }

    protected int setWhereCondPart(PreparedStatement ps, List values, int start) throws AppCrash {

        Object obj = null;

        try {
            Iterator iter = values.iterator();

            while (iter.hasNext()) {
                obj = iter.next();
                setPsValue(ps, obj, start);
                start++;
            }

            return start;
        } catch (SQLException e) {
            DBCrash ac = new DBCrash(e);
            ac.logContext("NDAO_base", "Start = " + start + " Value = " + obj);
            throw ac;
        }
    }

    private void setPsValue(PreparedStatement ps, Object value, int index) throws SQLException, AppCrash {

        if ((value == null) || value instanceof DAONull) {

            Logger.GetInstance().log3("---- ---- NULL");

            ps.setString(index, null);
        } else if (value instanceof String) {

            String psValue = (String) value;
            Logger.GetInstance().log3("---- ---- String >>>>>>" + psValue + "<<<");

            ps.setString(index, (String) value);
        } else if (value instanceof Timestamp) {

            Timestamp psValue = (Timestamp) value;
            Logger.GetInstance().log3("---- ---- Timestamp >>>>>>" + psValue + "<<<");

            ps.setTimestamp(index, (Timestamp) value);

        } else if (value instanceof Integer) {

            Integer psValue = ((Integer) value).intValue();
            Logger.GetInstance().log3("---- ---- Integer >>>>>>" + psValue + "<<<");

            ps.setInt(index, ((Integer) value).intValue());
        } else if (value instanceof Long) {

            Long psValue = ((Long) value).longValue();
            Logger.GetInstance().log3("---- ---- Long >>>>>>" + psValue + "<<<");

            ps.setLong(index, ((Long) value).longValue());
        } else if (value instanceof Float) {

            Float psValue = ((Float) value).floatValue();
            Logger.GetInstance().log3("---- ---- Float >>>>>>" + psValue + "<<<");

            ps.setFloat(index, ((Float) value).floatValue());
        } else if (value instanceof Double) {

            Double psValue = ((Double) value).doubleValue();
            Logger.GetInstance().log3("---- ---- Double >>>>>>" + psValue + "<<<");

            ps.setDouble(index, ((Double) value).doubleValue());
        } else if (value instanceof BigDecimal) {

            BigDecimal psValue = (BigDecimal) value;
            Logger.GetInstance().log3("---- ---- BigDecimal >>>>>>" + psValue + "<<<");

            ps.setBigDecimal(index, (BigDecimal) value);
        } else if (value instanceof byte[]) {

            byte[] psValue = (byte[]) value;
            Logger.GetInstance().log3("---- ---- byte[] >>>>>>" + psValue + "<<<");

            ps.setBytes(index, (byte[]) value);
        } else if (value instanceof Date) {

            Date psValue = (Date) value;
            Logger.GetInstance().log3("---- ---- Date >>>>>>" + psValue + "<<<");

            ps.setDate(index, (Date) value);
        } else if (value instanceof Time) {

            Time psValue = (Time) value;
            Logger.GetInstance().log3("---- ---- Time >>>>>>" + psValue + "<<<");

            ps.setTime(index, (Time) value);
        } else if (value instanceof File) {
            try {
                int len = (int) ((File) value).length();
                InputStream fin = new java.io.FileInputStream((File) value);
                ps.setBinaryStream(index, fin, len);
            } catch (Throwable e) {
                AppCrash ac = new AppCrash(e);
                ac.logContext("NDAO_base", "Errore blob:" + ((File) value).getAbsolutePath());
                throw ac;
            }

            // fabiano - gestione InputStream per inserimento BLOB
            // INIZIO
        } else if (value instanceof InputStream) {
            try {
                InputStream is = (InputStream) value;

                int len = is.available();

                ps.setBinaryStream(index, is, len);
            } catch (Throwable e) {
                AppCrash ac = new AppCrash(e);
                ac.logContext("NDAO_base", "Errore blob: InputStream");
                throw ac;
            }
            // fabiano - gestione InputStream per inserimento BLOB
            // INIZIO
        } else if (value instanceof Clob) {
            ps.setClob(index, (Clob) value);
        } else if (value instanceof Blob) {
            ps.setBlob(index, (Blob) value);
        } else if (value instanceof UUID) {
            ps.setObject(index, value);
        } else if (value instanceof Boolean) {
            ps.setBoolean(index, ((Boolean) value).booleanValue());
        } else {
            throw new AppCrash("Tipo sconosciuto per index " + index + " " + value.getClass().getName());
        }
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

    /**
     * Questo metodo serve per recuperare il valore di un campo dall'entita' tramite l'interfaccia MsgReader_itf Se
     * l'oggetto nella colonna e' null viene restituito stringa vuota per aderire alla semantica dell'interfaccia
     * MsgReader_itf come definita da tutte le sue altre implementazioni.
     * <p>
     * Se chiamato all'interno di un metodo whereCondition esegue l'escaping SQL perche' i campi vengono usati per
     * comporre l'SQL di ricerca
     *
     * @param name Name nome del campo da leggere
     *
     * @return java.lang.String valore del campo richiesto
     *
     * @exception AppCrash Nel caso vi fossere problemi nella lettura del campo.
     */
    public String getAttributeAsString(String name) throws AppCrash {

        Object obj = getAttribute(name);

        if (obj == null) {
            return "";
        }

        return obj.toString();
    }

    /**
     * Questo metodo recupera l'elenco dei campi che fanno parte di questo DAO ricercando tutti gli attributi dichiarati
     * come public static final di tipo String I valori assegnati a tali attributi rappresentano i nomi dei campi.
     *
     * @return un Iterator con i nomi dei campi
     *
     * @throws AppCrash DOCUMENT ME!
     */
    public Iterator iterator() throws AppCrash {

        Field[] fields = this.getClass().getFields();
        Set campi = new HashSet();

        int j = 0;
        String nome = null;

        try {
            for (j = 0; j < fields.length; j++) {
                nome = fields[j].getName();

                Class tipo = fields[j].getType();

                // se non e' di tipo stringa lo ignoro
                if (!tipo.getName().equals("java.lang.String")) {
                    continue;
                }

                int modif = fields[j].getModifiers();

                // Se a questo punto il campo e' una stringa public static final allora il suo valore fa parte dei nomi
                // campi accettati
                if (Modifier.isStatic(modif) && Modifier.isPublic(modif) && Modifier.isFinal(modif)) {
                    campi.add((fields[j].get(this)));
                }
            }
        } catch (IllegalAccessException e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("NDAO_base", "Errore sul campo " + j + " - " + nome);
        }

        return campi.iterator();
    }

    /**
     * Questo metodo verifica che il nome passato faccia parte dei campi dichiarati dal DAO
     *
     * @param name nome del campo da verificare
     *
     * @return true se OK altrimenti false
     *
     * @throws AppCrash
     */
    protected boolean checkFieldName(String name) throws AppCrash {

        return (_fieldList.contains(name) || _fieldList.contains("?I" + name));

    }

    /**
     * Questo metodo inizializza il DAO
     *
     * @throws AppCrash DOCUMENT ME!
     */
    protected void init() throws AppCrash {

        if (Config.GetInstance().getProperty("Servlet.Firewall", "false").equalsIgnoreCase("true")) {
            _firewall = true;
        }

        Iterator fields = iterator();

        while (fields.hasNext()) {
            _fieldList.add(fields.next());
        }
    }

    /**
     * Ritorna il valore dell'attributo identificato da attribID.
     *
     * @param attribID java.lang.String
     * @return java.lang.Object
     */
    public Object getAttribute(String attribID) throws AppCrash {

        ErrDetector.GetInstance().preCond(checkFieldName(attribID), "Campo non permesso" + attribID);

        Object attribute = _tableColumns.get(attribID);

        if (attribute instanceof DAONull) {
            attribute = null;
        }

        return attribute;
    }

    /**
     * Questo metodo imposta il valore di un attributo di tipo Timestamp. Effettua la conversione in stringa con il
     * formato di default di java.sql.Timestamp e rimpiazzando i caratteri non graditi a DB2
     *
     * @param String name nome dell'attributo
     * @param long value valore in millisecondi
     * @throws AppCrash in caso di eccezione
     */
    public void setAttributeAsTS(String name, long value) throws AppCrash {

        Timestamp ts = new Timestamp(value);

        int nanos = (int) Math.round(Math.random() * 999);
        ts.setNanos(ts.getNanos() + nanos);

        setAttribute(name, ts);
    }

    /**
     * Questo metodo protetto viene utilizzato per recuperare la query da eseguire. Di default viene restituito "select
     * x,z,y from " + whereCondition() dove x,y,z sono le costanti dichiarate dal DAO.
     * <P>
     * Le sottoclassi possono ridefinirlo per modificare questo comportamento (ad esempio per definire query complesse
     * con join) .
     */
    protected String getQuery() throws AppCrash {

        StringBuffer query = new StringBuffer();
        query.append("select ");

        // ricavo l'elenco delle colonne
        Iterator colums = _fieldList.iterator();
        String col = null;

        while (colums.hasNext()) {
            col = (String) colums.next();

            // Se il nome dell'attributo inizia con ? devo eliminare i primi 2 caratteri
            if (col.startsWith("?")) {
                col = col.substring(2);
            }
            query.append(col);

            if (colums.hasNext()) {
                query.append(", ");
            }
        }

        query.append(" from " + _tableName + " " + whereCondition().getSQL());

        String que = query.toString();

        return que;
    }

    // Verifica se getAttribute o getField sono stati chiamati dal metodo whereCondition
    // di un DAO concreto. Viene ispezionato lo stacktrace nelle posizioni 2, 3 o 4
    // 2 whereCondition -> getAttribute -> calledByWhereCondition
    // 3 whereCondition -> getField -> getAttribute -> calledByWhereCondition
    // 4 whereCondition -> DAOXML.getfield -> DAOXML.getAttribute -> getAttribute -> calledByWhereCondition
    private boolean calledByWhereCondition() {

        Throwable e = new Throwable();
        StackTraceElement[] el = e.getStackTrace();

        if (el[2].getMethodName().equals("whereCondition")) {
            return true;
        }

        if ((el.length >= 3) && el[3].getMethodName().equals("whereCondition")) {
            return true;
        }

        if ((el.length >= 4) && el[4].getMethodName().equals("whereCondition")) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {

        String temp;
        Object colValue = new Object();
        StringBuffer tempBuffer = new StringBuffer("\nColonne: ");
        tempBuffer.append(" ( ");

        for (Iterator colonne = _fieldList.iterator(); colonne.hasNext();) {
            colValue = colonne.next();

            tempBuffer.append(colValue).append("=");

            if ((_tableColumns.get(colValue) == null)) {
                temp = "null";
            } else {
                temp = _tableColumns.get(colValue).toString();
            }

            tempBuffer.append(temp);

            if (colonne.hasNext()) {
                tempBuffer.append(",");
            }
        }

        tempBuffer.append(") ");

        return tempBuffer.toString();
    }

    /**
     * Questa classe rappresenta il valore null per una colonna
     */
    private static final class DAONull {

        @Override
        public String toString() {

            return "null";
        }
    }
}
