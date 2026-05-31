/*

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore:

  Note:

 */

package net.project.db;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.mess.MsgReader_itf;
import net.project.mess.MsgWriterEx;
import net.project.mess.MsgWriterEx_itf;
import net.project.misc.Config;
import net.project.misc.Converter;

/**
 * Questa classe rappresenta la classe astratta base per gli oggetti di tipo DAO: Data Access Object. Essa estende la
 * classe DB2UpdatableEntity_base ed implementa l'interfaccia MsgWriterEx_itf. E' il punto di unione delle due
 * gerarchie.
 * <P>
 * Le classi che estendono DAO_base devono dichiarare tutti i nomi dei campi del DAO (corrispondenti alle colonne della
 * tabella-vista), che verranno acceduti, come static public final String. Okkio: tutte queste costanti public sono
 * ritenute essere nomi di colonne e sono percio' inserite nella select list creata dinamicamente quando viene
 * effettuata la query. Se occorrono costanti che non siano corrispondenti a colonne devono essere dichiarate
 * diversamente (private o non final)
 *
 * @author Simone
 */
public abstract class DAO_base extends DB2DBUpdatableEntity_base implements MsgWriterEx_itf {

    Set             _fieldList = new HashSet();
    MsgWriterEx     _utility   = null;
    String          _type      = null;

    // true se il firewall dei parametri per le web app e' abilitato
    private boolean _firewall  = false;

    /**
     * Costruttore con il solo nome tabella.
     *
     * @param tableName DOCUMENT ME!
     */
    public DAO_base(String tableName) throws AppCrash {

        super(tableName);
        try {
            _type = tableName;
            init();
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
    public DAO_base(String tableName, String configName) throws AppCrash {

        super(tableName, configName);
        try {
            _type = tableName;
            init();
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
    public DAO_base(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
        try {
            _type = tableName;
            init();
        } catch (AppCrash e) {
            e.logContext("DAO_base con DBTransaction ", "tableName = " + tableName);
            throw e;
        }
    }

    /**
     * Ritorna il valore dell'attributo identificato da attribID; attribID deve essere una delle costanti dichiarate
     * public static final String.
     * <p>
     * Se chiamato all'interno di un metodo whereCondition esegue l'escaping SQL perche' i campi vengono usati per
     * comporre l'SQL di ricerca
     *
     * @param attribID java.lang.String nome dell'attributo
     *
     * @return java.lang.Object
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    public Object getAttribute(String attribID) throws AppCrash {

        ErrDetector.GetInstance().preCond(checkFieldName(attribID), "Campo non permesso" + attribID);

        // I valori sono sicuramente String perche' la classe DBEntity_base usa getString da JDBC
        String value = (String) super.getAttribute(attribID);
        if (value != null && value.indexOf("'") != -1 && calledByWhereCondition()) {
            // Se chiamato all'interno di un metodo whereCondition esegue l'escaping SQL
            // perche' i campi vengono usati per comporre l'SQL di ricerca
            return Converter.sqlEscape(value);
        }
        return value;
    }

    /**
     * Ritorna il valore dell'attributo identificato da attribID; attribID deve essere una delle costanti dichiarate
     * public static final String.
     * <p>
     * Gli attributi sono String. Questo metodo ritorna il loro valore dopo averne fatto l'escape SQL
     *
     * @param attribID java.lang.String nome dell'attributo
     *
     * @return java.lang.Object
     * @throws AppCrash nel caso di problemi
     */
    public Object getAttributeSqlEsc(String attribID) throws AppCrash {

        return Converter.sqlEscape(getField(attribID));
    }

    /**
     * Impostata il valore di un attributo del DAO
     * 
     * @param attribID nome del campo
     * @param value valore da assegnare al campo
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    public void setAttribute(String attribID, Object value) {

        try {
            ErrDetector.GetInstance().preCond(checkFieldName(attribID), "Campo non permesso" + attribID);

            if (_firewall) {
                super.setAttribute(attribID, Converter.sqlUnEscape((String) value));
            } else {
                super.setAttribute(attribID, value);
            }
        } catch (AppCrash e) {
            Error er = new Error("Campo non permesso in setAttribute DAO_base " + attribID);
            throw er;
        }
    }

    /**
     * Questo metodo serve per impostare il valore di un campo dell'entita'
     *
     * @param name name nome del campo.
     * @param value value valore del campo.
     *
     * @exception AppCrash
     */
    @Override
    public void setField(String name, String value) throws AppCrash {

        setAttribute(name, value);
    }

    /**
     * Questo metodo ritorna il tipo di messaggio; in questo caso viene restituito il nome della tabella
     *
     * @return il nome della tabella
     */
    @Override
    public String getType() {

        return _type;
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
    @Override
    public String getField(String name) throws AppCrash {

        String obj = (String) getAttribute(name);
        if (obj == null) return "";
        return obj;
    }

    /**
     * Questo metodo serve per recuperare il valore di un campo dall'entita' tramite l'interfaccia MsgReader_itf Se
     * l'oggetto nella colonna e' null viene restituito stringa vuota per aderire alla semantica dell'interfaccia
     * MsgReader_itf come definita da tutte le sue altre implementazioni.
     * <p>
     * Il campo subisce un escaping SQL.
     * <p>
     *
     * @param name Name nome del campo da leggere
     *
     * @return java.lang.String valore del campo richiesto
     *
     * @exception AppCrash Nel caso vi fossere problemi nella lettura del campo.
     */
    public String getFieldSqlEsc(String name) throws AppCrash {

        return Converter.sqlEscape(getField(name));
    }

    /**
     * Questo metodo non e' supportato
     *
     * @return byte[] un array di byte che contiene il messaggio costruito
     *
     * @throws AppCrash
     */
    @Override
    public byte[] getMessage() throws AppCrash {

        throw new AppCrash("Metodo non supportato");
    }

    /**
     * Questo metodo recupera l'elenco dei campi che fanno parte di questo DAO ricercando tutti gli attributi dichiarati
     * come public static final di tipo String I valori assegnati a tali attributi rappresentano i nomi dei campi.
     *
     * @return un Iterator con i nomi dei campi
     *
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
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
                    campi.add(((String) fields[j].get(this)).toUpperCase());
                }
            }
        } catch (IllegalAccessException e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("DAO_base", "Errore sul campo " + j + " - " + nome);
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

        return (_fieldList.contains(name.toUpperCase()) || _fieldList.contains("?I" + name.toUpperCase()));
    }

    /**
     * Questo metodo scrive sul log di debug di tutti i campi del DAO
     *
     * @param level livello di log da adottare (0 1 2 3)
     *
     * @throws AppCrash
     */
    @Override
    public void logDebug(int level) throws AppCrash {

        _utility.logDebug(level);
    }

    /**
     * Questo metodo permette di settare i campi di un MsgWriter a partire dai campi di un array. Per eseguire il
     * mapping prende in ingresso una matrice che contiene per ogni riga una coppia "destinazione" - "valore"
     *
     * @param mapping matrice "destinazione" - "valore" .
     *
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    public void copy(String[][] mapping) throws AppCrash {

        _utility.copy(mapping);
    }

    /**
     * Questo metodo cerca di estrarre da un MsgReader tutti i campi che sono indicati nell'array di copiarli
     * all'interno dell'oggetto del quale fa parte.
     *
     * @param msg MsgReader_itf il MsgReader dal quale prelevare i campi
     * @param campi matrice che contiene il mapping dei campi da copiare nel formato "destinazione" - "sorgente"
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    public void copy(MsgReader_itf msg, String[][] campi) throws AppCrash {

        _utility.copy(msg, campi);
    }

    /**
     * Questo metodo cerca di estrarre da un MsgReader tutti i campi che sono indicati come validi per il MsgWriterEx
     * del quale fa parte.
     *
     * @param msg MsgReader_itf il MsgReader dal quale prelevare i campi
     *
     * @throws AppCrash
     */
    @Override
    public void copy(MsgReader_itf msg) throws AppCrash {

        _utility.copy(msg);
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

        _utility = new MsgWriterEx(this);
    }

    /**
     * Questo metodo restituisce il valore di un attributo convertendolo da stringa a numero Long. E' utile perche' il
     * metodo getAttribute pur restituendo formalente un Object e' basato su una implementazione che ritorna sempre un
     * dato String.
     *
     * @param String name nome dell'attributo
     * @return Long il valore numerico dell'attributo se diverso da null
     * @throws AppCrash eccezione se il campo non esiste o non e' convertibile
     */
    public Long getAttributeAsLong(String name) throws AppCrash {

        String attr = null;
        try {
            attr = (String) this.getAttribute(name);
            if (attr == null) return null;

            return (new Long(attr));
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("DAO_base", "Errore nella conversione in Long: campo:" + name + " valore:" + attr);
            throw ac;
        }
    }

    /**
     * Questo metodo restituisce il valore di un attributo convertendolo da stringa a data. E' utile perche' il metodo
     * getAttribute pur restituendo formalente un Object e' basato su una implementazione che ritorna sempre un dato
     * String.
     *
     * @param String name nome dell'attributo
     * @return Date il valore data dell'attributo se diverso da null
     * @throws AppCrash eccezione se il campo non esiste o non e' convertibile
     */
    public Date getAttributeAsDate(String name) throws AppCrash {

        String attr = null;
        try {
            attr = (String) this.getAttribute(name);
            if (attr == null) return null;

            SimpleDateFormat smdf = new SimpleDateFormat();

            return smdf.parse(attr);
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("DAO_base", "Errore nella conversione in Date: campo:" + name + " valore:" + attr);
            throw ac;
        }
    }

    /**
     * Questo metodo imposta il valore di un attributo di tipo Date. Effettua la conversione in stringa con il formato
     * YYYY-MM-DD
     *
     * @param String name nome dell'attributo
     * @param Date value valore
     * @throws AppCrash in caso di eccezione
     */
    public void setAttributeAsDate(String name, Date value) throws AppCrash {

        SimpleDateFormat smdf = new SimpleDateFormat("YYYY-MM-DD");
        setAttribute(name, smdf.format(value));
    }

    /**
     * Questo metodo imposta il valore di un attributo di tipo Timestamp. Effettua la conversione in stringa con il
     * formato di default di java.sql.Timestamp e rimpiazzando i caratteri non graditi a DB2
     *
     * @param String name nome dell'attributo
     * @param java.sql.Timestamp value valore da impostare
     * @throws AppCrash in caso di eccezione
     */
    public void setAttributeAsTS(String name, Timestamp value) throws AppCrash {

        String val = value.toString();
        setAttribute(name, convertTimestampToDB2(val));
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

        setAttributeAsTS(name, ts);

    }

    /**
     * Questo metodo protetto viene utilizzato per recuperare la query da eseguire. Di default viene restituito
     * "select x,z,y from " + whereCondition() dove x,y,z sono le costanti dichiarate dal DAO.
     * <P>
     * Le sottoclassi possono ridefinirlo per modificare questo comportamento (ad esempio per definire query complesse
     * con join) .
     */
    @Override
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
            if (colums.hasNext()) query.append(", ");
        }
        query.append(" from " + _type + " " + whereCondition());
        String que = query.toString();
        return que;
    }

    /**
     * Questo metodo trasforma la stringa passata in ingresso da un formato timestamp normale ottenuto da un
     * java.sql.Timestamp.toString() ad un timestamp in formato DB2: vengono semplicemente rimpiazzati gli spazi con '-'
     * ed i ':' con '.'
     * <p>
     * Non vengono effettuati controlli. Il metodo e' utile per il trattamento dei campi timestamp da usare nelle
     * whereCondition()
     *
     * @param timestamp il timestamp da convertire
     */
    protected String convertTimestampToDB2(String timestamp) {

        String tmp1 = timestamp.replace(' ', '-');
        String valore = tmp1.replace(':', '.');

        return valore;
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

        if (el.length >= 3 && el[3].getMethodName().equals("whereCondition")) {
            return true;
        }

        if (el.length >= 4 && el[4].getMethodName().equals("whereCondition")) {
            return true;
        }

        return false;
    }
}
