/*
  HashtableDBDataSet.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione: 19/7/2002

  Autore: Luca M.

  Note:

  Modifiche:

 */

package net.project.dataset;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Hashtable;

import net.project.errors.AppCrash;
import net.project.errors.DBCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;

/**
 * Rappresenta un DataSet ottenuto come risultato di una query fatta sul database. Le righe sono oggetti costruiti
 * attorno ad una Hashtable.
 * <P>
 * Proprietà lette dal file di configurazione: DS.dsName.Query = query che genera lo HashtableDBDataSet DS.dsName.Row =
 * classe implementante Row_itf che dev'essere istanziata per rappresentare le righe del HashtableDBDataSet corrente.
 * DEVE avere come parametro nel costruttore java.util.Hashtable. Di default, è net.project.dataset.HashtableRow.
 */
public class HashtableDBDataSet extends DBDataSet {

    /**
     * Costruttore.
     * 
     * @param configName java.lang.String nome logico della configurazione
     * @param dsName java.lang.String nome logico del dataset
     * @exception net.project.errors.AppCrash in caso di errori nella costruzione dell'oggetto
     */
    public HashtableDBDataSet(String configName, String dsName) throws AppCrash {

        super(configName, dsName);
    }

    // Metodo protected che crea un oggetto di tipo Row_itf
    // leggendo dal file di configurazione
    // la specifica classe da istanziare.
    // Di default, è net.project.dataset.HashtableRow.
    // @return Row_itf l'oggetto costruito
    // @exception net.project.errors.AppCrash
    @Override
    protected Row_itf createRow(ResultSet set) throws AppCrash {

        try {
            ResultSetMetaData rsmd = set.getMetaData();
            int colsCount = rsmd.getColumnCount();
            Hashtable rowHash = new Hashtable(colsCount);
            for (int i = 1; i < colsCount; i++) {
                rowHash.put(rsmd.getColumnName(i), formatIfNull(set.getString(i)));
            }
            String className = Config.GetInstance(getConfigName()).getProperty("DS." + getDsName() + ".Row",
                    "net.project.dataset.HashtableRow");
            ErrDetector.GetInstance().param(className);
            Class rowClass = Class.forName(className);
            Constructor constructor = rowClass.getConstructor(new Class[] { Hashtable.class });
            Row_itf row = (Row_itf) constructor.newInstance(new Object[] { rowHash });
            return row;

        } catch (SQLException sqle) {
            DBCrash dbc = new DBCrash(sqle);
            dbc.logContext("HashtableDBDataSet", toString());
            throw dbc;
        } catch (ClassNotFoundException cnfe) {
            AppCrash ac = new AppCrash(cnfe);
            ac.logContext("HashtableDBDataSet", toString());
            throw ac;
        } catch (NoSuchMethodException nsme) {
            AppCrash ac = new AppCrash(nsme);
            ac.logContext("HashtableDBDataSet", toString());
            throw ac;
        } catch (InstantiationException ie) {
            AppCrash ac = new AppCrash(ie);
            ac.logContext("HashtableDBDataSet", toString());
            throw ac;
        } catch (InvocationTargetException ite) {
            AppCrash ac = new AppCrash(ite);
            ac.logContext("HashtableDBDataSet", toString());
            throw ac;
        } catch (IllegalAccessException iae) {
            AppCrash ac = new AppCrash(iae);
            ac.logContext("HashtableDBDataSet", toString());
            throw ac;
        }

    }

    private Object formatIfNull(String what) {

        if (what == null) {
            return new HashtableRow.SqlNull();
        } else {
            return what;
        }

    }

}
