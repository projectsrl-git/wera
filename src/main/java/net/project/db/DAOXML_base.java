
package net.project.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.project.db.ser.SerializerFactory_base;
import net.project.db.ser.Serializer_itf;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;

/**
 * Questa classe estende DAO_base ed aggiunge ad essa due capacita':
 * <P>
 * 1- la capacita' di memorizzare oggetti java all'interno di una colonna di una tabella DB2 serializzandoli in stringhe
 * XML che vengono poi compresse con zip e trasformate in base64 2- la capacita' di definire campi virtuali all'interno
 * di una colonna di una tabella. Questo permette di memorizzare tanti record type con una sola tabella
 *
 * Il meccanismo di serializzazione utilizzato in entrambi i casi e' parametrizzato tramite una factory.
 * <P>
 * La factory e' indicata dalla proprieta' <b>SerializerFactoryClass.DB</b> che deve essere presente in config.
 * Attualmente la classe disponibile nel package come facory e': DBXMLSerializerFactory La factory
 * DBXMLSerializerFactory peremette di definire la classe utilizzata per effettuare veramente la
 * serializzazione-deserializzazione. Attualmente la classe disponibile nel package e' SkaringaXMLSerializer. Se si
 * utilizza questa classe gli oggetti che devono essere memorizzati in una colonna devono avere un constructor public
 * senza parametri. La stessa cosa deve valere per tutti gli oggetti in essi contenuti.
 * <P>
 * Esempio di file di configurazione:
 * <P>
 * <P>
 * SerializerFactoryClass.DB=net.project.db.ser.DBXMLSerializerFactory
 * <P>
 * DB.DBXMLSerializer=net.project.db.ser.SkaringaXMLSerializer
 * <P>
 *
 *
 * @author Simone
 */
public abstract class DAOXML_base extends DAO_base {

    private Map _fields = new HashMap();

    abstract protected String getRecordColName();

    abstract protected boolean storeFlexibleData();

    /**
     * Costruttore con il solo nome tabella.
     *
     * @param tableName DOCUMENT ME!
     */
    public DAOXML_base(String tableName) throws AppCrash {

        super(tableName);
    }

    /**
     * Costruttore con nome file configurazione.
     *
     * @param tableName DOCUMENT ME!
     * @param configName DOCUMENT ME!
     */
    public DAOXML_base(String tableName, String configName) throws AppCrash {

        super(tableName, configName);
    }

    /**
     * Costruttore con DBTransaction.
     *
     * @param transact DOCUMENT ME!
     * @param tableName DOCUMENT ME!
     */
    public DAOXML_base(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    /**
     * Ritorna il valore dell'attributo identificato da attribID.
     *
     * @param attribID java.lang.String
     *
     * @return java.lang.Object
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    public Object getAttribute(String attribID) throws AppCrash {

        ErrDetector.GetInstance().preCond(checkFieldName(attribID), "Campo non permesso" + attribID);

        try {

            if (attribID.startsWith("?X")) {
                return _fields.get(attribID);
            }
            return super.getAttribute(attribID);

        } catch (AppCrash e) {
            e.logContext("DAOXML_base", "Campo: " + attribID);
            throw e;
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("DAOXML_base", "Campo: " + attribID);
            throw ac;
        }

    }

    /**
     * Ritorna il valore dell'attributo identificato da attribID memorizzato come oggetto XML. Usa come
     * SerializerFactory la classe indicata dalla proprieta' <b>SerializerFactoryClass.DB</b>
     *
     * @param attribID java.lang.String
     *
     * @return java.lang.Object
     * @throws AppCrash DOCUMENT ME!
     */
    public Object getObjectAttribute(String attribID) throws AppCrash {

        ErrDetector.GetInstance().preCond(checkFieldName(attribID), "Campo non permesso" + attribID);

        try {

            String data = (String) super.getAttribute(attribID);

            Serializer_itf serializer = SerializerFactory_base.GetInstance("DB").makeSerializer();
            Object field = serializer.deserializeFromString(data);

            return field;

        } catch (AppCrash e) {
            e.logContext("DAOXML_base", "Campo: " + attribID);
            throw e;
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("DAOXML_base", "Campo: " + attribID);
            throw ac;
        }

    }

    /**
     * Impostata una entry della hashtable.
     * 
     * @param attribID DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @throws AppCrash DOCUMENT ME!
     */
    @Override
    public void setAttribute(String attribID, Object value) {

        try {
            ErrDetector.GetInstance().preCond(checkFieldName(attribID), "Campo non permesso" + attribID);

            if (attribID.startsWith("?X")) {
                _fields.put(attribID, value);
            } else {
                super.setAttribute(attribID, value);
            }
        } catch (AppCrash e) {
            Error er = new Error("Campo non permesso in setAttribute DAO_base");
            throw er;
        }
    }

    /**
     * Imposta una colonna del DAO traducendo l'oggetto passato in XML.
     * 
     * @param attribID nome del campo
     * @param value oggetto da memorizzare
     * @throws AppCrash
     */
    public void setObjectAttribute(String attribID, Object value) {

        try {
            ErrDetector.GetInstance().preCond(checkFieldName(attribID), "Campo non permesso" + attribID);

            Serializer_itf serializer = SerializerFactory_base.GetInstance("DB").makeSerializer();
            String xml = serializer.serializeToString(value);

            setAttribute(attribID, xml);

        } catch (AppCrash e) {
            Error er = new Error("Campo non permesso in setAttribute DAO_base");
            throw er;
        }
    }

    /**
     * Ritorna true se viene recuperata una riga dal database altrimenti false.
     * 
     * @return boolean
     */
    @Override
    public boolean retrieve() throws AppCrash {

        boolean result = super.retrieve();

        if (storeFlexibleData()) {
            _fields = (Map) getObjectAttribute(getRecordColName());
        }
        return result;
    }

    /**
     * Inserisce una riga nel database.
     */
    @Override
    public void insert() throws AppCrash {

        try {

            if (storeFlexibleData()) {
                setObjectAttribute(getRecordColName(), _fields);
            }
            super.insert();

        } catch (AppCrash e) {
            e.logContext("DAOXML_base", "Dati: " + this.toString());
            throw e;
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("DAOXML_base", "Dati: " + this.toString());
            throw ac;
        }

    }

    /**
     * Aggiorna una riga del database.
     */
    @Override
    public void update() throws AppCrash {

        try {
            if (storeFlexibleData()) {
                setObjectAttribute(getRecordColName(), _fields);
            }
            super.update();
        } catch (AppCrash e) {
            e.logContext("DAOXML_base", "Dati: " + this.toString());
            throw e;
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("DAOXML_base", "Dati: " + this.toString());
            throw ac;
        }
    }

    @Override
    public String toString() {

        String res = super.toString();
        return (res + "RecordColName: " + getRecordColName() + " FlexData: " + storeFlexibleData());
    }

    /**
     * Questo metodo protetto viene utilizzato per recuperare la query da eseguire. Viene qui' ridefinito per eliminare
     * i campi che iniziano con ?X in modo che non appaiano nella select dato che non esistono realmente fra le colonne
     * del DB.
     *
     * @exception AppCrash
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

            // Elimina i campi memorizzati nella colonna multivalue in XML
            if (col.startsWith("?X")) {
                continue;
            }

            // Se il nome dell'attributo inizia con ? devo eliminare i primi 2 caratteri
            if (col.startsWith("?")) {
                col = col.substring(2);
            }
            query.append(col);
            if (colums.hasNext()) query.append(", ");
        }
        query.append(" from " + super.getType() + " " + whereCondition());
        String que = query.toString();
        return que;
    }
}
