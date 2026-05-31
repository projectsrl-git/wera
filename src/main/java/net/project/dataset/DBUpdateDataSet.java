/*
  DBUpdateDataSet.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione:

  Autore: Simone Z.

  Note:

  Modifiche:

 */

package net.project.dataset;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import net.project.db.ConnectionPool;
import net.project.db.ConnectionPool_itf;
import net.project.db.DBConnection_itf;
import net.project.errors.AppCrash;
import net.project.errors.DBCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.errors.ParamCrash;
import net.project.misc.Config;
import net.project.misc.TextTemplate;

/**
 * Rappresenta un DataSet ottenuto come risultato di una query fatta sul database.
 * <P>
 * Proprietà lette dal file di configurazione: DS.dsName.Query = query che genera il DBUpdateDataSet DS.dsName.Row =
 * classe implementante Row_itf che dev'essere istanziata per rappresentare le righe del DBUpdateDataSet corrente
 */
public class DBUpdateDataSet implements DataSet_itf {

    private String             _sql            = null;
    private String             _configName     = null;
    private String             _dsName         = null;
    private ConnectionPool_itf _connectionPool = null;
    private DBConnection_itf   _dbConnection   = null;
    private Statement          _statement      = null;
    private Row_itf            _row            = null;
    private boolean            _opened         = false;
    private boolean            _nextEseguita   = false;
    private int                _columnNo       = 0;

    /**
     * Costruttore.
     * 
     * @roseuid 3A5B327E0326
     */
    public DBUpdateDataSet(String configName, String dsName) throws AppCrash {

        // controllo formale dei parametri in ingresso
        ErrDetector.GetInstance().invariant(configName != null);
        ErrDetector.GetInstance().param(dsName);

        _configName = configName;
        _dsName = dsName;
        _sql = Config.GetInstance(_configName).getProperty("DS." + _dsName + ".Query");
        ErrDetector.GetInstance().param(_sql);

    }

    /**
     * Apre una connessione al database ed esegue l'operazione di aggiornamento contenuta nella query.
     * 
     * @return void
     * @exception net.project.errors.AppCrash in caso di errore nella connessione al database o nella parametrizzazione
     *                con TextTemplate
     * @roseuid 3A5D7D5200AC
     */
    @Override
    public void open() throws AppCrash {

        if (_opened) {
            return;
        }

        _connectionPool = ConnectionPool.GetInstance();
        _dbConnection = _connectionPool.getAConnection();

        try {
            _statement = _dbConnection.createStatement();
            Logger.GetInstance().log3("sto per eseguire la seguente query: " + _sql);
            int updateCount = _statement.executeUpdate(_sql);
            _opened = true;
            _statement.close();
        } catch (SQLException sqle) {
            DBCrash dbc = new DBCrash(sqle);
            dbc.logContext("DBUpdateDataSet", toString());
            throw dbc;
        } finally {
            _dbConnection.free();
            _dbConnection = null;
        }

    }

    /**
     * Posiziona il cursore del ResultSet davanti alla prima riga. Metodo non supportato
     * 
     * @return void
     * @exception net.project.errors.AppCrash in caso di SQLException o di Resultset = null
     * @roseuid 3A5D7D5200D4
     */
    @Override
    public void rewind() throws AppCrash {

        AppCrash dbc = new AppCrash("Rewind non supportato! ");
        dbc.logContext("DBUpdateDataSet.java", toString());
        throw dbc;

    }

    /**
     * Libera le risorse del database. Qui' non ha nulla da fare
     * 
     * @return void
     * @exception net.project.AppCrash.errors lanciata dal metodo shutdown di ConnectionPool_itf oppure (come DBCrash)
     *                in caso di SQLException
     * @roseuid 3A5D7D520107
     */
    @Override
    public void close() throws AppCrash {

        return;
    }

    /**
     * Valorizza l'attributo _sql, contenente la query da eseguire.
     * 
     * @param java.util.Map parametri Map per valorizzare la parte parametrica della query scritta nel file di
     *            configurazione
     * @return void
     * @exception net.project.errors.ParamCrash
     * @roseuid 3A5D7D52016B
     */
    @Override
    public void setParam(Map parametri) throws AppCrash {

        // controllo formale del parametro in ingresso
        ErrDetector.GetInstance().param(parametri);

        Iterator columnsEnumeration = parametri.keySet().iterator();
        String[] columnNames = new String[parametri.size()];
        int columnNo = 0;
        while (columnsEnumeration.hasNext()) {
            columnNames[columnNo] = (String) columnsEnumeration.next();
            columnNo++;
        }
        _row = new HashtableRow(parametri, columnNames);
        _columnNo = parametri.size();

        TextTemplate tt = null;
        try {
            tt = new TextTemplate(_sql);

        } catch (ParamCrash pc) {
            pc.logContext("DBUpdateDataSet", toString() + "; parametri = " + parametri);
            throw pc;
        }

        if (!tt.replace(parametri)) {
            AppCrash ac = new AppCrash();
            ac.logContext("DBUpdateDataSet", toString() + "; parametri = " + parametri);
            throw ac;
        }
        _sql = tt.getText();

    }

    /**
     * Restituisce true la prima volta che viene invocato dopo l'apertura del dataset. Il ResultSet di un update
     * contiene una sola riga: quella inserita o aggiornata con le sole colonne modificate.
     * 
     * @return boolean true se il ResultSet contiene altri elementi, false altrimenti
     */
    @Override
    public boolean hasMoreElements() {

        return (!_nextEseguita);
    }

    /**
     * Restituisce l'elemento del ResultSet successivo a quello corrente.Il ResultSet di un update contiene una sola
     * riga: quella inserita o aggiornata con le sole colonne modificate.
     * 
     * @return java.lang.Object il successivo elemento del DBUpdateDataSet
     * @exception NoSuchElementException se non ci sono più elementi nel DBUpdateDataSet
     */
    @Override
    public Object nextElement() throws NoSuchElementException {

        if (_nextEseguita == false) {
            _nextEseguita = true;
            return _row;
        }
        NoSuchElementException nsee = new NoSuchElementException("DBUpdateDataSet doppio next: " + toString());
        throw nsee;

    }

    /**
     * Restituisce una stringa composta dai nomi di determinati attributi della classe, ognuno seguito dal proprio
     * valore corrente.
     * 
     * @return java.lang.String la stringa restituita
     */
    @Override
    public String toString() {

        StringBuffer temp = new StringBuffer();
        temp.append("_sql = ");
        temp.append(_sql);
        temp.append("; _configName = ");
        temp.append(_configName);
        temp.append("; _dsName = ");
        temp.append(_dsName);
        temp.append("; _opened = ");
        temp.append(_opened);
        temp.append("; _nextEseguita = ");
        temp.append(_nextEseguita);
        return temp.toString();

    }

    /**
     * Conta il numero di colonne del DataSet.
     * 
     * @return int numero di colonne del DataSet
     * @exception net.project.errors.AppCrash
     */
    @Override
    public int getColumnNo() throws AppCrash {

        return _columnNo;
    }

    /**
     * Ritorna i nomi delle colonne.
     * 
     * @return java.lang.String[] nomu delle colonne.
     * @exception net.project.errors.AppCrash
     */
    @Override
    public String[] getColumnNames() throws AppCrash {

        AppCrash dbc = new AppCrash("Recuper nomi colonne non supportato! ");
        dbc.logContext("DBUpdateDataSet.java", toString());
        throw dbc;

    }

}
